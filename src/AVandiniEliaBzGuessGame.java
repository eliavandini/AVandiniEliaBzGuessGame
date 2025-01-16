import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * Enum representing different categories of commands.
 */
enum CommandCategory {
    BASIC,
    INGAME,
    STORE,
    SECRET
}

/**
 * Interface for attributes with an index.
 */
interface AbstarctAttributes {
    int getIndex();
}

enum CursorStyles implements AbstarctAttributes {
    BLINK_BLOCK(0),
    BLINKING_BLOCK(1),
    STEADY_BLOCK(2),
    BLINKING_UNDERLINE(3),
    STEADY_UNDERLINE(4),
    BLINKING_BAR(5),
    STEADY_BAR(6);

    private final int index;

    CursorStyles(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

/**
 * Enum for text attributes used in console output.
 */
enum TextAttributes implements AbstarctAttributes {
    RESET(0),
    BRIGHT(1),
    DIM(2),
    ITALIC(3),
    UNDERLINE(4),
    BLINKING(5),
    INVERSE(7),
    HIDDEN(8),
    STRIKETHROUGH(9);

    private final int index;

    TextAttributes(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

/**
 * Enum for resetting text attributes.
 */
enum ResetTextAttributes implements AbstarctAttributes {
    RESET_BRIGHT(22),
    RESET_DIM(22),
    RESET_ITALIC(23),
    RESET_UNDERLINE(24),
    RESET_BLINKING(25),
    RESET_INVERSE(27),
    RESET_HIDDEN(28),
    RESET_STRIKETHROUGH(29);

    private final int index;

    ResetTextAttributes(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

/**
 * Enum for foreground colors.
 */
enum FColors implements AbstarctAttributes {
    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    WHITE(37);

    private final int index;

    FColors(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

/**
 * Enum for background colors.
 */
enum BColors implements AbstarctAttributes {
    BLACK(40),
    RED(41),
    GREEN(42),
    YELLOW(43),
    BLUE(44),
    MAGENTA(45),
    CYAN(46),
    WHITE(47);

    private final int index;

    BColors(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

/**
 * Enum for cursor move directions.
 */
enum CursorMoveDirection {
    UP('A'),
    DOWN('B'),
    RIGHT('C'),
    LEFT('D'),
    DOWNX('E'),
    UPX('F'),
    COLUMN('G');

    private final char label;

    CursorMoveDirection(char index) {
        this.label = index;
    }

    public char getLabel() {
        return label;
    }
}

/**
 * Exception thrown for invalid inputs.
 */
class InvalidInputException extends Exception {
    public InvalidInputException() {
    }

    public InvalidInputException(String message) {
        super(message);
    }
}

/**
 * Class to serialize and manage game state.
 */
class GameSerilizer implements Serializable {
    long highscore = 0;
    ArrayList<Game> games = new ArrayList<Game>();
    Game current_game;
    CursorStyles cursorStyles;

    public GameSerilizer(long highscore, ArrayList<Game> games, Game current_game, CursorStyles cursorStyles) {
        this.highscore = highscore;
        this.games = games;
        this.current_game = current_game;
        this.cursorStyles = cursorStyles;
    }

    /**
     * Saves the game state to a specified file.
     *
     * @param gameState the game state to save
     * @param filePath  the path to the file
     * @throws IOException if an I/O error occurs
     */
    public static void saveGameState(GameSerilizer gameState, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gameState);  // Serialize the GameState object
        }
    }

    /**
     * Loads the game state from a specified file.
     *
     * @param filePath the path to the file
     * @return the loaded game state
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    public static GameSerilizer loadGameState(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameSerilizer) ois.readObject();  // Deserialize and cast the object
        }
    }
}

/**
 * Main class for the BzGuessGame.
 */
class AVandiniEliaBzGuessGame {
    public static final String Author = "Eia Vandini";
    public static final String Version = "v1.34";
    static char[] options = {'A', 'B', 'C', 'D', 'E', 'F'};
    static CursorStyles cursorStyle = CursorStyles.BLINKING_BAR;
    static long highscore = 0;
    static Command[] comands = new Command[]{new CommandHelp(), new CommandP(), new CommandSetCode(), new CommandRemains(), new CommandBuy(), new CommandQuit(), new CommandNew(), new CommandHistory(), new CommandRules(), new CommandClose(), new CommandBuyAI(), new CommandAI(), new CommandUnlimitedAttempts(), new CommandChangeCursorStyle()};
    static KeyBind[] global_keybinds = new KeyBind[]{new KeyBindClose(), new KeyBindNew()};
    static ArrayList<Game> games = new ArrayList<Game>();
    static Game current_game;
    static GameSerilizer gameSerilizer = new GameSerilizer(highscore, games, current_game, cursorStyle);
    static Thread kyThread = new KeyListenenThread();

    /**
     * Main method to start the game.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        AVandiniEliaBzGuessGame.setCursorStyle(cursorStyle);
        greeting();
        kyThread.start();
        loadGamestate();
        saveGameState();
    }

    /**
     * Loads the game state from a file if it exists, otherwise starts a new game.
     */
    static void loadGamestate() {
        File f = new File("GameState.ser");
        if (f.exists()) {
            try {
                gameSerilizer = GameSerilizer.loadGameState("GameState.ser");
                highscore = gameSerilizer.highscore;
                games = gameSerilizer.games;
                current_game = gameSerilizer.current_game;
                if (current_game.won || current_game.lost) {
                    newGame();
                } else {
                    current_game.gameloop();
                }
            } catch (Exception e) {
                setAttribute(FColors.YELLOW);
                System.out.println("WARNING: unable to read gamestate");
                resetAttrributes();
                newGame();
            }
        } else {
            newGame();
        }
    }

    /**
     * Saves the current game state to a file.
     */
    static void saveGameState() {
        gameSerilizer.highscore = highscore;
        gameSerilizer.games = games;
        gameSerilizer.current_game = current_game;
        try {
            GameSerilizer.saveGameState(gameSerilizer, "GameState.ser");
        } catch (IOException e) {
            setAttribute(FColors.YELLOW);
            System.out.println("WARNING: unable to save gamestate");
            resetAttrributes();
        }
    }

    /**
     * Displays a greeting message.
     */
    static void greeting() {
        System.out.println("\rProgrammed by Vandini Elia");
        new CommandHelp().exec(new String[]{""});
        AVandiniEliaBzGuessGame.eraseLinesUp(2);
    }

    /**
     * Displays the win screen with score.
     */
    static void winScreen() {
        System.out.println("\rCongratulations, Score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
        askIfPlayAgain();
    }

    /**
     * Displays the win screen with score.
     */
    static void aiWinScreen() {
        System.out.println("\rHypothetical score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
        askIfPlayAgain();
    }

    /**
     * Displays the lose screen with the secret code.
     */
    static void loosescreen() {
        System.out.println("\rYou lost! Secret code was " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code) + ".");
        askIfPlayAgain();
    }

    /**
     * Displays the lose screen with the secret code.
     */
    static void aiLooseScreen() {
        System.out.println("\rNot even the AI could save you☠\uFE0F. Secret code was " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code) + ".");
        askIfPlayAgain();
    }

    /**
     * Asks the player if they want to play again.
     */
    static void askIfPlayAgain() {


        AtomicBoolean loop = new AtomicBoolean(true);
        AtomicBoolean cancel = new AtomicBoolean(false);
        AtomicInteger seleciton = new AtomicInteger();

        KeyListenenThread.keymap.clear();
        KeyListenenThread.keymap.put(KeyCodes.ENTER.getCode(), n -> loop.set(false));
        KeyListenenThread.keymap.put(KeyCodes.RIGHT_ARROW.getCode(), n -> seleciton.getAndIncrement());
        KeyListenenThread.keymap.put(KeyCodes.LEFT_ARROW.getCode(), n -> seleciton.getAndDecrement());

        AVandiniEliaBzGuessGame.hideCursor();
        System.out.println("\rWant to play again?");
        System.out.println("\r");

        while (loop.get()) {

            System.out.print("    ");
            if (seleciton.get() == 0) {
                AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
            }
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
            System.out.print('Y');
            AVandiniEliaBzGuessGame.setAttribute(ResetTextAttributes.RESET_BRIGHT);
            System.out.print("es");
            AVandiniEliaBzGuessGame.resetAttrributes();
            System.out.print("/");
            if (seleciton.get() == 1) {
                AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
            }
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
            System.out.print('N');
            AVandiniEliaBzGuessGame.setAttribute(ResetTextAttributes.RESET_BRIGHT);
            System.out.print("o");
            AVandiniEliaBzGuessGame.resetAttrributes();


            while (!AVandiniEliaBzGuessGame.KeyHandling()) {
                AVandiniEliaBzGuessGame.wait(10);
            }
            if (seleciton.get() < 0) {
                seleciton.set(1);
            }
            if (seleciton.get() > 1) {
                seleciton.set(0);
            }
            System.out.println();
            AVandiniEliaBzGuessGame.eraseLinesUp(1);
        }
        AVandiniEliaBzGuessGame.eraseLinesUp(3);
        if (seleciton.get() == 0) {
            newGame();
        } else {
            System.out.println("\n\rSee you soon!\r");
            new CommandClose().exec(new String[]{});

        }
    }

    /**
     * Initializes a new game.
     *
     * @return the new game instance
     */
    static Game newGame() {
        Game g = new Game();
        games.add(g);
        current_game = g;
        g.startGame();
        return g;
    }

    /**
     * Erases the current line in the console.
     */
    static void eraseLine() {
        System.out.print("\u001b[2K\r");
    }

    /**
     * Erases a specified number of lines above the current cursor position.
     *
     * @param lines the number of lines to erase
     */
    static void eraseLinesUp(int lines) {
        moveCursor(lines, CursorMoveDirection.UPX);
        System.out.print("\u001b[0J");
    }

    /**
     * Sets text attributes for console output.
     *
     * @param attr the attribute to set
     */
    static void setAttribute(AbstarctAttributes attr) {
        System.out.printf("\u001b[%dm", attr.getIndex());
    }

    /**
     * Sets multiple text attributes for console output.
     *
     * @param attr an array of attributes to set
     */
    static void setAttribute(AbstarctAttributes[] attr) {
        for (AbstarctAttributes a : attr) {
            setAttribute(a);
        }
    }

    /**
     * Resets text attributes to default.
     */
    static void resetAttrributes() {
        setAttribute(TextAttributes.RESET);
    }

    /**
     * Clears the console screen.
     */
    static void clearScreen() {
        System.out.print("\u001b[H\u001b[2J");
    }

    /**
     * Hides the console cursor.
     */
    static void hideCursor() {
        System.out.print("\u001b[?25l");
    }

    /**
     * Shows the console cursor.
     */
    static void showCursor() {
        System.out.print("\u001b[?25h");
    }

    /**
     * Moves the cursor in the console.
     *
     * @param pos       the position to move
     * @param direction the direction to move
     */
    static void moveCursor(int pos, CursorMoveDirection direction) {
        System.out.print("\u001b[" + pos + direction.getLabel());
    }

    static void saveCursorPosition() {
        System.out.println("\u001b[s");
    }

    static void restoreCursorPosition() {
        System.out.println("\u001b[u");
    }

    static void setCursorStyle(CursorStyles style) {
        System.out.print("\u001b[" + style.getIndex() + " q");
    }


    static void fancyprint(String res) {
        for (char c : res.toCharArray()) {
            System.out.print(c + "●");
            AVandiniEliaBzGuessGame.moveCursor(1, CursorMoveDirection.LEFT);
            AVandiniEliaBzGuessGame.wait(20);
            if (c == '.') {
                AVandiniEliaBzGuessGame.wait(500);
            }
        }
        System.out.println(" ");
        System.out.print("\r");
        AVandiniEliaBzGuessGame.showCursor();
    }

    /**
     * Waits for a specified number of milliseconds.
     *
     * @param ms milliseconds to wait
     */
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean KeyHandling() {
        boolean processed_key = false;
        for (byte[] pressed_key : new ArrayList<>(KeyListenenThread.active_keys)) {
            for (byte[] key : KeyListenenThread.keymap.keySet()) {
                if (Arrays.equals(pressed_key, key)) {
                    KeyListenenThread.active_keys.remove(pressed_key);
                    Consumer<byte[]> function = KeyListenenThread.keymap.get(key);
                    function.accept(key);
                    processed_key = true;
                    break;
                }
            }
        }
        KeyListenenThread.active_keys.clear();
        return processed_key;
    }
}

class KeyBind {
    String shortel;
    String description;
    byte[][] keys;

    void exec(byte[] pressedKey) {
    }
}

class KeyBindClose extends KeyBind {

    KeyBindClose() {
        shortel = "^Q";
        description = "Saves and close the game";
        keys = new byte[][]{KeyCodes.Q.getCtrlCode()};
    }

    @Override
    void exec(byte[] pressedKey) {
        new CommandClose().exec(new String[]{});
    }
}

class KeyBindNew extends KeyBind {

    KeyBindNew() {
        shortel = "^N";
        description = "Starts a new game";
        keys = new byte[][]{KeyCodes.N.getCtrlCode()};
    }

    @Override
    void exec(byte[] pressedKey) {
        new CommandNew().exec(new String[]{});
    }
}

/**
 * Abstract class representing a command in the game.
 */
class Command {
    String fullName;
    String longc;
    String shortc;
    CommandCategory category;
    String description;

    void exec(String[] args) throws InvalidInputException {
    }
}

/**
 * Command to display help information.
 */
class CommandHelp extends Command {

    CommandHelp() {
        super();
        category = CommandCategory.BASIC;
        longc = "help";
        shortc = "h";
        description = "Display a help screen explaining the game commands";
        fullName = "Help";
    }

    void exec(String[] args) {
        boolean show_secret = false;
        if (args.length > 0 && args[0].equals("-s")) {
            show_secret = true;
        }
        String space_storage = "                 ";

        for (CommandCategory c : CommandCategory.values()) {
            if (c == CommandCategory.SECRET && !show_secret) {
                continue;
            }
            System.out.println("\n\r" + c.toString().toUpperCase());
            for (Command command : AVandiniEliaBzGuessGame.comands) {
                if (c != command.category) {
                    continue;
                }
                String help_string = "\r." + command.shortc + " | ." + command.longc;
                help_string = help_string + space_storage.substring(help_string.length()) + command.description;
                System.out.println(help_string);
            }
        }
        System.out.println();
        System.out.println("\rSoftware by " + AVandiniEliaBzGuessGame.Author + ". Version " + AVandiniEliaBzGuessGame.Version);
    }
}

/**
 * Command to display the game rules.
 */
class CommandRules extends Command {

    CommandRules() {
        super();
        category = CommandCategory.BASIC;
        longc = "rules";
        shortc = "r";
        description = "Display a help screen explaining the game rules";
        fullName = "Rules";
    }

    void exec(String[] args) {
        System.out.println("\r╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("\r║                             BzGuessGame Help                              ║");
        System.out.println("\r║                                                                           ║");
        System.out.println("\r║ Welcome to my BzGuessGame! Your goal is to guess the secret code.         ║");
        System.out.println("\r║                                                                           ║");
        System.out.println("\r║ - The secret code consists of 4 characters from {a, b, c, d, e, f}.       ║");
        System.out.println("\r║ - Characters can appear zero to four times.                               ║");
        System.out.println("\r║ - You have 20 attempts to guess the code.                                 ║");
        System.out.println("\r║ - After each guess, you'll receive feedback:                              ║");
        System.out.println("\r║   X: Correct character at the correct position.                           ║");
        System.out.println("\r║   -: Correct character but at the wrong position.                         ║");
        System.out.println("\r║                                                                           ║");
        System.out.println("\r║ Check out available commands with .help or .h!                            ║");
        System.out.println("\r║ To execute a command prefix a '.' before the command.                     ║");
        System.out.println("\r║ 'HELP' will be interpreted as a guess while '.help' or '.h' is a command. ║");
        System.out.println("\r║                                                                           ║ ");
        System.out.println("\r║ Good luck!                                                                ║");
        System.out.println("\r╚═══════════════════════════════════════════════════════════════════════════╝ ");
    }
}

/**
 * Command to reveal the secret code.
 */
class CommandP extends Command {

    CommandP() {
        super();
        category = CommandCategory.SECRET;
        longc = "p";
        shortc = "p";
        description = "reveals the code";
        fullName = "p";
    }

    void exec(String[] args) {
        System.out.println("\r" + AVandiniEliaBzGuessGame.current_game.code);
    }
}

/**
 * Command to quit the game.
 */
class CommandQuit extends Command {

    CommandQuit() {
        super();
        category = CommandCategory.BASIC;
        longc = "quit";
        shortc = "q";
        description = "reveals the code and quits the game";
        fullName = "Quit";
    }

    void exec(String[] args) {
        AVandiniEliaBzGuessGame.current_game.lost = true;
    }
}

/**
 * Command to close the game.
 */
class CommandClose extends Command {

    CommandClose() {
        super();
        category = CommandCategory.BASIC;
        longc = "close";
        shortc = "c";
        description = "saves game and quits";
        fullName = "Close";
    }

    void exec(String[] args) {
        KeyListenenThread.running = false;
        try {
            KeyListenenThread.disableRawMode();
        } catch (Exception ignored) {
        }
        AVandiniEliaBzGuessGame.saveGameState();
        System.out.println("\r");
        AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.BLINKING_BLOCK);
        System.exit(0); // not ideal but cleaner than manually exiting the game loop from here
    }
}

/**
 * Command to start a new game.
 */
class CommandNew extends Command {

    CommandNew() {
        super();
        category = CommandCategory.INGAME;
        longc = "new";
        shortc = "n";
        description = "Start over a new game";
        fullName = "New Game";
    }

    void exec(String[] args) {
        System.out.println(AVandiniEliaBzGuessGame.current_game.code);
        AVandiniEliaBzGuessGame.current_game.lost = true;
        AVandiniEliaBzGuessGame.newGame();
    }
}

/**
 * Command to set the secret code.
 */
class CommandSetCode extends Command {

    CommandSetCode() {
        super();
        category = CommandCategory.SECRET;
        longc = "setcode";
        shortc = "S";
        description = "set the secret code to a user input";
        fullName = "SetCode";
    }

    void exec(String[] args) throws InvalidInputException {
        if (args.length > 1) {
            throw new InvalidInputException("only one argument expected");
        }
        if (args.length < 1) {
            throw new InvalidInputException("at least one argument expected");
        }
        if (args[0].length() != 4) {
            throw new InvalidInputException("new code may only contain 4 characters");
        }
        for (int i = 0; i < args[0].length(); i++) {
            char ch = args[0].toUpperCase().charAt(i);
            boolean char_found = false;
            for (int j = 0; j < AVandiniEliaBzGuessGame.options.length; j++) {
                if (AVandiniEliaBzGuessGame.options[j] == ch) {
                    char_found = true;
                }
            }
            if (!char_found) {
                throw new InvalidInputException("new code contains invalid characters");
            }
        }
        AVandiniEliaBzGuessGame.current_game.code = args[0].toUpperCase().toCharArray();
        System.out.println("the secret code has been updated");
    }
}

/**
 * Command to buy a letter of the secret code.
 */
class CommandBuy extends Command {

    CommandBuy() {
        super();
        category = CommandCategory.STORE;
        longc = "buy";
        shortc = "b";
        description = "Buy one letter of the secret code at its right position. Costs 5 attempts";
        fullName = "Buy";
    }

    void exec(String[] args) {
        Random r = new Random();
        char[] res_string = {'_', '_', '_', '_'};
        int pos = r.nextInt(4);
        res_string[pos] = AVandiniEliaBzGuessGame.current_game.code[pos];

        AVandiniEliaBzGuessGame.current_game.solver.possibleCodes.removeIf(n -> n[pos] != AVandiniEliaBzGuessGame.current_game.code[pos]);

        AVandiniEliaBzGuessGame.current_game.attempts_left -= 5;
        AVandiniEliaBzGuessGame.current_game.history += AVandiniEliaBzGuessGame.current_game.attempts_left + "> The User bought " + Arrays.toString(res_string) + " using up 5 attempts\n";
        System.out.println("\r" + res_string);
    }
}

/**
 * Command to display the history of guesses.
 */
class CommandHistory extends Command {

    CommandHistory() {
        super();
        category = CommandCategory.INGAME;
        longc = "history";
        shortc = "H";
        description = "Show history of all guesses and evaluations of past games";
        fullName = "History";
    }

    void exec(String[] args) throws InvalidInputException {

        if (AVandiniEliaBzGuessGame.games.isEmpty()) {
            throw new InvalidInputException("start a game before trying to access the game history");
        }

        AtomicBoolean loop = new AtomicBoolean(true);
        AtomicBoolean cancel = new AtomicBoolean(false);
        AtomicInteger seleciton = new AtomicInteger();

        KeyListenenThread.keymap.clear();
        KeyListenenThread.keymap.put(KeyCodes.ENTER.getCode(), n -> loop.set(false));
        KeyListenenThread.keymap.put(KeyCodes.DOWN_ARROW.getCode(), n -> seleciton.getAndIncrement());
        KeyListenenThread.keymap.put(KeyCodes.UP_ARROW.getCode(), n -> seleciton.getAndDecrement());
        KeyListenenThread.keymap.put(KeyCodes.C.getCode(), n -> cancel.set(true));
        KeyListenenThread.keymap.put(KeyCodes.Q.getCode(), n -> cancel.set(true));
        KeyListenenThread.keymap.put(KeyCodes.ESCAPE.getCode(), n -> cancel.set(true));

        AVandiniEliaBzGuessGame.hideCursor();
        System.out.println("\rSelect a game history to view");
        System.out.println("\rUse Arrow keys to navigate selection, enter to view a game and C or esc to cancel");
        System.out.println("\r");

        while (loop.get() && !cancel.get()) {

            for (int i = 0; i < AVandiniEliaBzGuessGame.games.size(); i++) {
                StringBuilder sb = new StringBuilder("\r");
                Game g = AVandiniEliaBzGuessGame.games.get(i);
//                sb.append(AVandiniEliaBzGuessGame.games.size() - 1 - i).append(") ");
                if (AVandiniEliaBzGuessGame.current_game == g) {
                    sb.append(" @ ");
                } else if (g.lost) {
                    sb.append(" L ");
                } else if (g.won) {
                    sb.append(" W ");
                } else {
                    sb.append("   ");
                }
                if (g.lost || g.won) {
                    sb.append(g.code).append(" | ");
                } else {
                    sb.append("____ | ");
                }
                sb.append(g.attempts_left).append(" | ");
                sb.append(g.score).append(" | ");
                sb.append(g.start_date);
                if (i == seleciton.get()) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.println(sb);
                if (i == seleciton.get()) {
                    AVandiniEliaBzGuessGame.resetAttrributes();
                }
            }
            while (!AVandiniEliaBzGuessGame.KeyHandling()) {
                AVandiniEliaBzGuessGame.wait(10);
            }
            if (seleciton.get() < 0) {
                seleciton.set(AVandiniEliaBzGuessGame.games.size() - 1);
            }
            if (seleciton.get() >= AVandiniEliaBzGuessGame.games.size()) {
                seleciton.set(0);
            }

            AVandiniEliaBzGuessGame.eraseLinesUp(AVandiniEliaBzGuessGame.games.size());
        }
        AVandiniEliaBzGuessGame.eraseLinesUp(3);

        if (cancel.get()) {
            AVandiniEliaBzGuessGame.showCursor();
            return;
        }

        AtomicBoolean quit = new AtomicBoolean(false);

        KeyListenenThread.keymap.clear();
        KeyListenenThread.keymap.put(KeyCodes.Q.getCode(), n -> quit.set(true));
        KeyListenenThread.keymap.put(KeyCodes.C.getCode(), n -> quit.set(true));

        System.out.println("Press Q to close this game\n");
        System.out.println("\r" + AVandiniEliaBzGuessGame.games.get(seleciton.get()).history.replace("\n", "\n\r"));

        AVandiniEliaBzGuessGame.hideCursor();
        while (!quit.get()) {
            while (!AVandiniEliaBzGuessGame.KeyHandling()) {
                AVandiniEliaBzGuessGame.wait(10);
            }
        }

        AVandiniEliaBzGuessGame.eraseLinesUp(AVandiniEliaBzGuessGame.games.get(seleciton.get()).history.split("\n").length + 3);

        exec(args);
    }
}


/**
 * Command to display the history of guesses.
 */
class CommandChangeCursorStyle extends Command {

    CommandChangeCursorStyle() {
        super();
        category = CommandCategory.INGAME;
        longc = "cursor";
        shortc = "C";
        description = "Change cursor style (may not work on some devices)";
        fullName = "Change cursor style";
    }

    void exec(String[] args) throws InvalidInputException {
        AtomicInteger seleciton = new AtomicInteger(AVandiniEliaBzGuessGame.cursorStyle.getIndex());

        try {
            seleciton.set(Integer.parseInt(args[0]));
        } catch (Exception e) {

            AtomicBoolean loop = new AtomicBoolean(true);
            AtomicBoolean cancel = new AtomicBoolean(false);

            KeyListenenThread.keymap.clear();
            KeyListenenThread.keymap.put(KeyCodes.ENTER.getCode(), n -> loop.set(false));
            KeyListenenThread.keymap.put(KeyCodes.DOWN_ARROW.getCode(), n -> seleciton.getAndIncrement());
            KeyListenenThread.keymap.put(KeyCodes.UP_ARROW.getCode(), n -> seleciton.getAndDecrement());
            KeyListenenThread.keymap.put(KeyCodes.C.getCode(), n -> cancel.set(true));
            KeyListenenThread.keymap.put(KeyCodes.Q.getCode(), n -> cancel.set(true));
            KeyListenenThread.keymap.put(KeyCodes.ESCAPE.getCode(), n -> cancel.set(true));

            AVandiniEliaBzGuessGame.hideCursor();
            System.out.println("\rSelect a cursor style to apply");
            System.out.println("\rUse Arrow keys to navigate selection, enter to confirm and C or esc to cancel");
            System.out.println("\r");

            while (loop.get() && !cancel.get()) {


                if (seleciton.get() == 0) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rBlink Block ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                if (seleciton.get() == 1) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rBlinking Block ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                if (seleciton.get() == 2) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rSteady Block ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                if (seleciton.get() == 3) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rBlinking Underline ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                if (seleciton.get() == 4) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rSteady Underline ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                if (seleciton.get() == 5) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rBlinking Bar ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                if (seleciton.get() == 6) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                }
                System.out.print("\rSteady Bar ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                System.out.println();


                long start = System.currentTimeMillis();
                while (!AVandiniEliaBzGuessGame.KeyHandling() && System.currentTimeMillis() < start + 500) {
                    AVandiniEliaBzGuessGame.wait(10);
                }
                if (seleciton.get() < 0) {
                    seleciton.set(6);
                }
                if (seleciton.get() > 6) {
                    seleciton.set(0);
                }

                AVandiniEliaBzGuessGame.eraseLinesUp(7);
            }
            AVandiniEliaBzGuessGame.eraseLinesUp(3);
        }

        switch (seleciton.get()) {
            case (0):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.BLINK_BLOCK);
                break;
            case (1):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.BLINKING_BLOCK);
                break;
            case (2):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.STEADY_BLOCK);
                break;
            case (3):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.BLINKING_UNDERLINE);
                break;
            case (4):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.STEADY_UNDERLINE);
                break;
            case (5):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.BLINKING_BAR);
                break;
            case (6):
                AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.STEADY_BAR);
                break;

        }
    }
}

class CommandAI extends Command {

    CommandAI() {
        super();
        category = CommandCategory.STORE;
        longc = "ai";
        shortc = "a";
        description = "plays the game for you";
        fullName = "AI";
    }

    void exec(String[] args) {
        try {
            Game g = AVandiniEliaBzGuessGame.current_game;
            g.ai = true;
            g.history += "Player activated AI.\n";
            while (!g.won && !g.lost) {
                AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.RED, TextAttributes.BRIGHT});
                System.out.print("\r" + g.attempts_left + ">");
                AVandiniEliaBzGuessGame.resetAttrributes();
                char[] nextGuess = g.solver.minimaxBestGuess(g.matches, g.guesses);
                for (char c : nextGuess) {
                    TimeUnit.MILLISECONDS.sleep(250);
                    System.out.print(c);
                }
                TimeUnit.MILLISECONDS.sleep(250);
                String res = g.parseGuess(new String(nextGuess));
                System.out.println(" " + res);
                g.attempts_left--;
                if (g.attempts_left <= 0) {
                    g.lost = true;
                }
                AVandiniEliaBzGuessGame.saveGameState();
            }
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class CommandBuyAI extends Command {

    CommandBuyAI() {
        super();
        category = CommandCategory.STORE;
        longc = "bai";
        shortc = "B";
        description = "Uses \uD83C\uDF1FAI\uD83C\uDF1F to generate a optimal guess. Costs 5 attempts, no API key needed.";
        fullName = "Buy AI guess";
    }

    void exec(String[] args) throws InvalidInputException {
        Game g = AVandiniEliaBzGuessGame.current_game;
        g.attempts_left -= 5;
        char[] nextGuess = g.solver.minimaxBestGuess(g.matches, g.guesses);
        g.history += "User generated a optimal guess: " + new String(nextGuess) + ", using up 5 attempts\n";
        String res = "\r" + "Sure, here is a optimal 4-letter guess: [" + new String(nextGuess) + "] .Let me know if you have anymore questions!";

        AVandiniEliaBzGuessGame.hideCursor();
        AVandiniEliaBzGuessGame.eraseLine();
        System.out.print(".");
        AVandiniEliaBzGuessGame.wait(250);
        AVandiniEliaBzGuessGame.eraseLine();
        System.out.print("..");
        AVandiniEliaBzGuessGame.wait(250);
        AVandiniEliaBzGuessGame.eraseLine();
        System.out.print("...");
        AVandiniEliaBzGuessGame.wait(1000);
        AVandiniEliaBzGuessGame.eraseLine();
        AVandiniEliaBzGuessGame.wait(250);
        System.out.print(".");
        AVandiniEliaBzGuessGame.wait(250);
        AVandiniEliaBzGuessGame.eraseLine();
        System.out.print("..");
        AVandiniEliaBzGuessGame.wait(250);
        AVandiniEliaBzGuessGame.eraseLine();
        System.out.print("...");
        AVandiniEliaBzGuessGame.wait(1000);
        AVandiniEliaBzGuessGame.eraseLine();
        AVandiniEliaBzGuessGame.fancyprint(res);
    }
}

class CommandRemains extends Command {

    CommandRemains() {
        super();
        category = CommandCategory.STORE;
        longc = "remains";
        shortc = "R";
        description = "counts available solutions based on the guesse's feedback. Costs 2 attempts";
        fullName = "Remains";
    }

    void exec(String[] args) throws InvalidInputException {
        Game g = AVandiniEliaBzGuessGame.current_game;
        int solution_count = g.solver.reduceCodes(g.matches, g.guesses);
        g.attempts_left--;
        g.attempts_left--;
        g.history += "Game counted " + solution_count + " possible solutions still available based on guess feedback. 2 attempts where used up\n";
        String res = "\r" + "Based on past guesses feedback there are " + solution_count + " viable solutions.";
        AVandiniEliaBzGuessGame.fancyprint(res);
    }
}

class CommandUnlimitedAttempts extends Command {

    CommandUnlimitedAttempts() {
        super();
        category = CommandCategory.SECRET;
        longc = "attempthack";
        shortc = "u";
        description = "gives you (almost) unlimited attempts";
        fullName = "Unlimited attempts";
    }

    void exec(String[] args) throws InvalidInputException {
        Game g = AVandiniEliaBzGuessGame.current_game;
        g.attempts_left = Long.MAX_VALUE;
    }
}

/**
 * Class representing a game instance.
 */
class Game implements Serializable {
    char[] code = new char[4];
    long attempts_left = 20L;
    String history = "";
    boolean won = false;
    boolean lost = false;
    Date start_date = new Date();
    long score = 0;
    Solver solver = new Solver();
    ArrayList<Point> matches = new ArrayList<Point>();
    ArrayList<char[]> guesses = new ArrayList<char[]>();
    boolean ai = false;

    static TextBox textBox = new TextBox();

    public Game() {
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            code[i] = AVandiniEliaBzGuessGame.options[r.nextInt(AVandiniEliaBzGuessGame.options.length)];
        }
    }

    /**
     * Starts the game.
     */
    void startGame() {
        this.history = "Game started at " + start_date + '\n';
        this.gameloop();
    }

    /**
     * Main game loop.
     */
    void gameloop() {
        while (!lost && !won) {

            execeTurn();
            AVandiniEliaBzGuessGame.saveGameState();
            if (AVandiniEliaBzGuessGame.current_game != this) {
                return;
            }
        }
        finishGame();
    }

    /**
     * Executes a turn in the game.
     */
    void execeTurn() {
        parseInput();
        if (attempts_left <= 0) {
            lost = true;
        }
    }

    /**
     * Asks for user input.
     *
     * @return the input from the user
     */
    String askInput() {
        Scanner sc = new Scanner(System.in);
        AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
        System.out.print(attempts_left + ">");
        AVandiniEliaBzGuessGame.resetAttrributes();
        String inp = sc.nextLine();
        return inp;
    }

    /**
     * Parses the command input.
     *
     * @param input the input string
     * @param args  arguments for the command
     * @throws InvalidInputException if the command is unknown
     */
    void parseCommand(String input, String[] args) throws InvalidInputException {
        for (Command command : AVandiniEliaBzGuessGame.comands) {
            if (command.shortc.equals(input) || command.longc.equalsIgnoreCase(input)) {
                command.exec(args);
                AVandiniEliaBzGuessGame.saveGameState();
                return;
            }
        }
        throw new InvalidInputException("Unknown command");
    }

    /**
     * Parses user input.
     */
    void parseInput() {
        String input = "";
        try {
            input = textBox.get_input(this);
            if (lost || won || ai || AVandiniEliaBzGuessGame.current_game != this) {
                return;
            }
            if (input.isEmpty()) {
                throw new InvalidInputException("please input a command or 4 character sequence");
            }
            if (input.charAt(0) == '.') {
                input = input.substring(1);
                String[] args = input.split(" ");
                parseCommand(args[0], Arrays.copyOfRange(args, 1, args.length));
                return;
            }
            if (input.length() != 4) {
                throw new InvalidInputException("input must be 4 characters long");
            }
            if (input.matches(".*[^ABCDEFabcdef].*")) {
                throw new InvalidInputException("input must consist of A, B, C, D, E or F");
            }
//            AVandiniEliaBzGuessGame.eraseLine();
            AVandiniEliaBzGuessGame.eraseLinesUp(1);

            AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
            System.out.print("\r" + attempts_left + "> ");
            AVandiniEliaBzGuessGame.resetAttrributes();
            System.out.println(input + " " + parseGuess(input));
            attempts_left--;
            if (attempts_left <= 0) {
                lost = true;
            }
        } catch (InvalidInputException e) {
            AVandiniEliaBzGuessGame.eraseLinesUp(1);
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
            AVandiniEliaBzGuessGame.setAttribute(FColors.RED);
            System.out.print(e.getMessage());
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.RESET);

            long start = System.currentTimeMillis();
            long last_tick = System.currentTimeMillis();

            AVandiniEliaBzGuessGame.hideCursor();

            while (System.currentTimeMillis() < start + 3000) {
                if (System.currentTimeMillis() < last_tick + 1) {
                    continue;
                }
                last_tick = System.currentTimeMillis();
                if (!KeyListenenThread.active_keys.isEmpty()) {
                    break;
                }
                AVandiniEliaBzGuessGame.eraseLine();

                AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
                System.out.print(attempts_left + "> ");
                AVandiniEliaBzGuessGame.resetAttrributes();
                AVandiniEliaBzGuessGame.setAttribute(TextAttributes.UNDERLINE);
                System.out.print(input);
                AVandiniEliaBzGuessGame.resetAttrributes();
                if (System.currentTimeMillis() % 1000 < 500) {
                    AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
                }
                AVandiniEliaBzGuessGame.setAttribute(FColors.RED);
                System.out.print(" " + e.getMessage());
                AVandiniEliaBzGuessGame.setAttribute(TextAttributes.RESET);
            }
            AVandiniEliaBzGuessGame.showCursor();
            parseInput();
        }
    }

    /**
     * Parses the player's guess.
     *
     * @param input the player's input
     * @return the result of the guess
     */
    String parseGuess(String input) {
        Point p = checkGuess(code, input.toCharArray());
        StringBuilder result = new StringBuilder();
        result.append("X".repeat(max(0, p.x)));
        result.append("-".repeat(max(0, p.y)));
        matches.add(p);
        guesses.add(input.toCharArray());
        if (p.x >= 4) {
            won = true;
        }
        AVandiniEliaBzGuessGame.current_game.history += attempts_left + ">" + input + " " + result + '\n';
        return result.toString();
    }

    static Point checkGuess(char[] code, char[] input) {
        Point p = new Point(0, 0);
        boolean[] cleared = {false, false, false, false};
        for (int j = 0; j < 4; j++) {
            if (code[j] == Character.toUpperCase(input[j])) {
                cleared[j] = true;
                p.x++;
            }
        }
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                if (cleared[k] || cleared[j]) {
                    continue;
                }
                if (code[k] == Character.toUpperCase(input[j])) {
                    p.y++;
                }
            }
        }
        return p;
    }

    /**
     * Calculates the score based on time and attempts left.
     *
     * @param time          the time taken
     * @param attempts_left the remaining attempts
     * @return the calculated score
     */
    static int score_calc(long time, int attempts_left) {
        double secs_per_attempt = (double) time / (20 - attempts_left) / 1000;
        if (secs_per_attempt > 60) {
            secs_per_attempt = 60;
        }
        double score = ((pow(secs_per_attempt, 2) / 180) - (secs_per_attempt * (13f / 20f)) + 20); // x^2/180 - x*13/20 + 20
        return (int) (score * 10) * (attempts_left * 10);
    }

    /**
     * Finishes the game and determines win/lose status.
     */
    void finishGame() {
        if (won) {
            long diff = abs(new Date().getTime() - this.start_date.getTime());
            score = score_calc(diff, (int) attempts_left);
            if (score > AVandiniEliaBzGuessGame.highscore && !AVandiniEliaBzGuessGame.current_game.ai) {
                AVandiniEliaBzGuessGame.highscore = score;
            }
            if (AVandiniEliaBzGuessGame.current_game.ai) {
                AVandiniEliaBzGuessGame.aiWinScreen();
                AVandiniEliaBzGuessGame.current_game.score = 0;
            } else {
                AVandiniEliaBzGuessGame.winScreen();
            }
        } else {
            score = 0;
            if (AVandiniEliaBzGuessGame.current_game.ai) {
                AVandiniEliaBzGuessGame.aiLooseScreen();
            } else {
                AVandiniEliaBzGuessGame.loosescreen();
            }
        }
    }
}

class Solver implements Serializable {
    ArrayList<char[]> possibleCodes;

    Solver() {
        possibleCodes = getPopulateCodes();
    }

    static ArrayList<char[]> getPopulateCodes() {
        ArrayList<char[]> genCodes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    for (int l = 0; l < 6; l++) {
                        char[] newCode = new char[]{AVandiniEliaBzGuessGame.options[i], AVandiniEliaBzGuessGame.options[j], AVandiniEliaBzGuessGame.options[k], AVandiniEliaBzGuessGame.options[l]};
                        boolean duplicate = false;
                        for (char[] existing : genCodes) {
                            if (Arrays.equals(existing, newCode)) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            genCodes.add(newCode);
                        }
                    }
                }
            }
        }
        return genCodes;
    }

    static ArrayList<Point> mockGame(char[] code, ArrayList<char[]> guesses) {
        ArrayList<Point> res = new ArrayList<Point>();
        for (int i = 0; i < guesses.size(); i++) {
            res.add(Game.checkGuess(code, guesses.get(i)));
        }
        return res;
    }

    static boolean equalMatches(char[] code, ArrayList<Point> p1, ArrayList<char[]> guesses) {
        ArrayList<Point> p2 = mockGame(code, guesses);
        boolean res = p1.equals(p2);
        return !res;
    }

    int reduceCodes(ArrayList<Point> matches, ArrayList<char[]> guesses) throws InvalidInputException {
        if (matches.size() != guesses.size()) {
            throw new InvalidInputException("param size mismatch");
        }
        if (matches.isEmpty()) {
            return possibleCodes.size();
        }
//        populateCodes();
        possibleCodes.removeIf(n -> equalMatches(n, matches, guesses));
        return possibleCodes.size();
    }

    char[] minimaxBestGuess(ArrayList<Point> matches, ArrayList<char[]> guesses) throws InvalidInputException {
        if (matches.size() != guesses.size()) {
            throw new InvalidInputException("param size mismatch");
        }
        if (matches.isEmpty() && possibleCodes.size() == 1296) {
            return new char[]{'A', 'A', 'B', 'B'};
        }
        reduceCodes(matches, guesses);
        if (possibleCodes.size() == 1) {
            return possibleCodes.getFirst();
        }
        int lowest_worst_score = Integer.MAX_VALUE;
        char[] lowest_worst_score_code = new char[]{'A', 'A', 'B', 'B'};
        for (char[] guess : getPopulateCodes()) {
//            if (guesses.contains(guess)) {
//                continue;
//            }
            Map<Point, Integer> counter = new HashMap<>();
            for (char[] code : possibleCodes) {
                Point p = Game.checkGuess(code, guess);

                counter.put(p, counter.getOrDefault(p, 0) + 1);
            }
            int highest;
            if (counter.values().stream().max(Integer::compare).isPresent()) {
                highest = counter.values().stream().max(Integer::compare).get();
                if (highest < lowest_worst_score) {
                    lowest_worst_score_code = guess;
                    lowest_worst_score = highest;
                }
            }
        }
        return lowest_worst_score_code;
    }
}

enum KeyCodes {
    BACKSPACE(new byte[]{127}, new byte[]{127}, new byte[]{8}, new byte[]{27, 127}, new byte[]{27, 127}),
    TAB(new byte[]{9}, new byte[]{27, 91}, new byte[]{9}, new byte[]{9}, new byte[]{9}),
    ENTER(new byte[]{13}, new byte[]{13}, new byte[]{13}, new byte[]{27, 13}, new byte[]{27, 13}),
    ESCAPE(new byte[]{27}, new byte[]{27}, new byte[]{27}, new byte[]{27}, new byte[]{27}),
    PAGE_UP(new byte[]{27, 91, 53, 126}, new byte[]{27, 91, 53, 126}, new byte[]{27, 91, 53, 59, 51, 126}, new byte[]{27, 91, 53, 59, 53, 126}, new byte[]{27, 91, 53, 59, 53, 126}),
    PAGE_DOWN(new byte[]{27, 91, 54, 126}, new byte[]{27, 91, 54, 126}, new byte[]{27, 91, 54, 59, 53, 126}, new byte[]{27, 91, 54, 59, 51, 126}, new byte[]{27, 91, 54, 59, 51, 126}),
    HOME(new byte[]{27, 91, 72}, new byte[]{27, 91, 72}, new byte[]{27, 91, 49, 59, 53, 72}, new byte[]{27, 91, 49, 59, 51, 72}, new byte[]{27, 91, 49, 59, 51, 72}),
    END(new byte[]{27, 91, 70}, new byte[]{27, 91, 70}, new byte[]{27, 91, 49, 59, 53, 70}, new byte[]{27, 91, 49, 59, 51, 70}, new byte[]{27, 91, 49, 59, 51, 70}),
    UP_ARROW(new byte[]{27, 91, 65}, new byte[]{27, 91, 49, 59, 50, 65}, new byte[]{27, 91, 49, 59, 53, 65}, new byte[]{27, 91, 49, 59, 51, 65}, new byte[]{27, 91, 49, 59, 52, 65}),
    DOWN_ARROW(new byte[]{27, 91, 66}, new byte[]{27, 91, 49, 59, 50, 66}, new byte[]{27, 91, 49, 59, 53, 66}, new byte[]{27, 91, 49, 59, 51, 66}, new byte[]{27, 91, 49, 59, 52, 66}),
    RIGHT_ARROW(new byte[]{27, 91, 67}, new byte[]{27, 91, 49, 59, 50, 67}, new byte[]{27, 91, 49, 59, 53, 67}, new byte[]{27, 91, 49, 59, 51, 67}, new byte[]{27, 91, 49, 59, 52, 67}),
    LEFT_ARROW(new byte[]{27, 91, 68}, new byte[]{27, 91, 49, 59, 50, 68}, new byte[]{27, 91, 49, 59, 53, 68}, new byte[]{27, 91, 49, 59, 51, 68}, new byte[]{27, 91, 49, 59, 52, 68}),
    DELETE(new byte[]{27, 91, 51, 126}, new byte[]{27, 91, 51, 59, 50, 126}, new byte[]{27, 91, 51, 59, 53, 126}, new byte[]{27, 91, 51, 59, 52, 126}, new byte[]{27, 91, 51, 59, 51, 126}),
    A(new byte[]{97}, new byte[]{65}, new byte[]{1}, new byte[]{27, 97}, new byte[]{27, 65}),
    B(new byte[]{98}, new byte[]{66}, new byte[]{2}, new byte[]{27, 98}, new byte[]{27, 65}),
    C(new byte[]{99}, new byte[]{67}, new byte[]{3}, new byte[]{27, 99}, new byte[]{27, 66}),
    D(new byte[]{100}, new byte[]{68}, new byte[]{4}, new byte[]{27, 100}, new byte[]{27, 67}),
    E(new byte[]{101}, new byte[]{69}, new byte[]{5}, new byte[]{27, 101}, new byte[]{27, 68}),
    F(new byte[]{102}, new byte[]{70}, new byte[]{6}, new byte[]{27, 102}, new byte[]{27, 69}),
    G(new byte[]{103}, new byte[]{71}, new byte[]{7}, new byte[]{27, 103}, new byte[]{27, 70}),
    H(new byte[]{104}, new byte[]{72}, new byte[]{8}, new byte[]{27, 104}, new byte[]{27, 71}),
    I(new byte[]{105}, new byte[]{73}, new byte[]{9}, new byte[]{27, 105}, new byte[]{27, 72}),
    J(new byte[]{106}, new byte[]{74}, new byte[]{10}, new byte[]{27, 106}, new byte[]{27, 73}),
    K(new byte[]{107}, new byte[]{75}, new byte[]{11}, new byte[]{27, 107}, new byte[]{27, 74}),
    L(new byte[]{108}, new byte[]{76}, new byte[]{12}, new byte[]{27, 108}, new byte[]{27, 75}),
    M(new byte[]{109}, new byte[]{77}, new byte[]{13}, new byte[]{27, 109}, new byte[]{27, 76}),
    N(new byte[]{110}, new byte[]{78}, new byte[]{14}, new byte[]{27, 110}, new byte[]{27, 77}),
    O(new byte[]{111}, new byte[]{79}, new byte[]{15}, new byte[]{27, 111}, new byte[]{27, 78}),
    P(new byte[]{112}, new byte[]{80}, new byte[]{16}, new byte[]{27, 112}, new byte[]{27, 79}),
    Q(new byte[]{113}, new byte[]{81}, new byte[]{17}, new byte[]{27, 113}, new byte[]{27, 80}),
    R(new byte[]{114}, new byte[]{82}, new byte[]{18}, new byte[]{27, 114}, new byte[]{27, 81}),
    S(new byte[]{115}, new byte[]{83}, new byte[]{19}, new byte[]{27, 115}, new byte[]{27, 82}),
    T(new byte[]{116}, new byte[]{84}, new byte[]{20}, new byte[]{27, 116}, new byte[]{27, 83}),
    U(new byte[]{117}, new byte[]{85}, new byte[]{21}, new byte[]{27, 117}, new byte[]{27, 84}),
    V(new byte[]{118}, new byte[]{86}, new byte[]{22}, new byte[]{27, 118}, new byte[]{27, 85}),
    W(new byte[]{119}, new byte[]{87}, new byte[]{23}, new byte[]{27, 119}, new byte[]{27, 86}),
    X(new byte[]{120}, new byte[]{88}, new byte[]{24}, new byte[]{27, 120}, new byte[]{27, 87}),
    Y(new byte[]{121}, new byte[]{89}, new byte[]{25}, new byte[]{27, 121}, new byte[]{27, 88}),
    Z(new byte[]{122}, new byte[]{90}, new byte[]{26}, new byte[]{27, 122}, new byte[]{27, 89}),
    NUM_1(new byte[]{49}, new byte[]{49}, new byte[]{49}, new byte[]{49}, new byte[]{49}),
    NUM_2(new byte[]{50}, new byte[]{50}, new byte[]{50}, new byte[]{50}, new byte[]{50}),
    NUM_3(new byte[]{51}, new byte[]{51}, new byte[]{51}, new byte[]{51}, new byte[]{51}),
    NUM_4(new byte[]{52}, new byte[]{52}, new byte[]{52}, new byte[]{52}, new byte[]{52}),
    NUM_5(new byte[]{53}, new byte[]{53}, new byte[]{53}, new byte[]{53}, new byte[]{53}),
    NUM_6(new byte[]{54}, new byte[]{54}, new byte[]{54}, new byte[]{54}, new byte[]{54}),
    NUM_7(new byte[]{55}, new byte[]{55}, new byte[]{55}, new byte[]{55}, new byte[]{55}),
    NUM_8(new byte[]{56}, new byte[]{56}, new byte[]{56}, new byte[]{56}, new byte[]{56}),
    NUM_9(new byte[]{57}, new byte[]{57}, new byte[]{57}, new byte[]{57}, new byte[]{57}),
    NUM_0(new byte[]{48}, new byte[]{48}, new byte[]{48}, new byte[]{48}, new byte[]{48}),
    //    MULTIPLY(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    ADD(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    SUBTRACT(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    DECIMAL_POINT(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    DIVIDE(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
    F1(new byte[]{27, 79, 80}, new byte[]{27, 79, 80}, new byte[]{27, 91, 49, 59, 53, 80}, new byte[]{27, 79, 80}, new byte[]{27, 79, 80}),
    F2(new byte[]{27, 79, 81}, new byte[]{27, 79, 81}, new byte[]{27, 91, 49, 59, 53, 81}, new byte[]{27, 79, 81}, new byte[]{27, 79, 81}),
    F3(new byte[]{27, 79, 82}, new byte[]{27, 79, 82}, new byte[]{27, 91, 49, 59, 53, 82}, new byte[]{27, 79, 82}, new byte[]{27, 79, 82}),
    F4(new byte[]{27, 79, 83}, new byte[]{27, 79, 83}, new byte[]{27, 91, 49, 59, 53, 83}, new byte[]{27, 79, 83}, new byte[]{27, 79, 83}),
    F5(new byte[]{27, 91, 49, 53, 126}, new byte[]{27, 91, 49, 53, 126}, new byte[]{27, 91, 49, 53, 59, 53, 126}, new byte[]{27, 91, 49, 53, 126}, new byte[]{27, 91, 49, 53, 126}),
    F6(new byte[]{27, 91, 49, 55, 126}, new byte[]{27, 91, 49, 55, 126}, new byte[]{27, 91, 49, 55, 59, 53, 126}, new byte[]{27, 91, 49, 55, 126}, new byte[]{27, 91, 49, 55, 126}),
    F7(new byte[]{27, 91, 49, 56, 126}, new byte[]{27, 91, 49, 56, 126}, new byte[]{27, 91, 49, 56, 59, 53, 126}, new byte[]{27, 91, 49, 56, 126}, new byte[]{27, 91, 49, 56, 126}),
    F8(new byte[]{27, 91, 49, 57, 126}, new byte[]{27, 91, 49, 57, 126}, new byte[]{27, 91, 49, 57, 126}, new byte[]{27, 91, 49, 57, 126}, new byte[]{27, 91, 49, 57, 126}),
    F9(new byte[]{27, 91, 50, 48, 126}, new byte[]{27, 91, 50, 48, 126}, new byte[]{27, 91, 50, 48, 126}, new byte[]{27, 91, 50, 48, 126}, new byte[]{27, 91, 50, 48, 126}),
    F10(new byte[]{27, 91, 50, 49, 126}, new byte[]{27, 91, 50, 49, 126}, new byte[]{27, 91, 50, 49, 126}, new byte[]{27, 91, 50, 49, 126}, new byte[]{27, 91, 50, 49, 126}),
    F11(new byte[]{27, 91, 50, 51, 126}, new byte[]{27, 91, 50, 51, 126}, new byte[]{27, 91, 50, 51, 126}, new byte[]{27, 91, 50, 51, 126}, new byte[]{27, 91, 50, 51, 126}),
    F12(new byte[]{27, 91, 50, 52, 126}, new byte[]{27, 91, 50, 52, 126}, new byte[]{27, 91, 50, 52, 126}, new byte[]{27, 91, 50, 52, 126}, new byte[]{27, 91, 50, 52, 126}),
    PERIOD(new byte[]{46}, new byte[]{46}, new byte[]{46}, new byte[]{27, 46}, new byte[]{27, 46}),
    SPACE(new byte[]{32}, new byte[]{32}, new byte[]{32}, new byte[]{27, 32}, new byte[]{27, 32}),

    ;
//    SEMICOLON(new byte[]{59}, new byte[]{59}, new byte[]{59}, new byte[]{59}, new byte[]{59}),
//    EQUAL_SIGN(new byte[]{61}, new byte[]{61}, new byte[]{61}, new byte[]{61}, new byte[]{61}),
//    COMMA(new byte[]{44}, new byte[]{44}, new byte[]{44}, new byte[]{44}, new byte[]{44}),
//    DASH(new byte[]{47}, new byte[]{47}, new byte[]{47}, new byte[]{47}, new byte[]{47}),
//    FORWARD_SLASH(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    GRAVE_ACCENT(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    OPEN_BRACKET(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    BACK_SLASH(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    CLOSE_BRACKET(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}),
//    SINGLE_QUOTE(new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{}, new byte[]{});

    private final byte[] code;
    private final byte[] shiftCode;
    private final byte[] ctrlCode;
    private final byte[] altCode;
    private final byte[] altShiftCode;

    KeyCodes(byte[] code, byte[] shiftCode, byte[] ctrlCode, byte[] altCode, byte[] altShiftCode) {
        this.code = code;
        this.shiftCode = shiftCode;
        this.ctrlCode = ctrlCode;
        this.altCode = altCode;
        this.altShiftCode = altShiftCode;
    }

    public byte[] getCode() {
        return code;
    }

    public byte[] getShiftCode() {
        return shiftCode;
    }

    public byte[] getCtrlCode() {
        return ctrlCode;
    }

    public byte[] getAltCode() {
        return altCode;
    }

    public byte[] getAltShiftCode() {
        return altShiftCode;
    }
}

enum KeyModifier {
    NONE,
    SHIFT,
    CTRL,
    ALT,
    ALT_SHIFT;
}

class KeyListenenThread extends Thread {

    static boolean running = false;
    static boolean globalKeyHandling = true;

    static Queue<byte[]> active_keys = new LinkedList<>();
    static Map<byte[], Consumer<byte[]>> keymap = new Hashtable<>();

    public void run() {

        try {
            if (running) {
                throw new RuntimeException("keythread already running");
            }
            running = true;
            enableRawMode();
            while (running) {
                byte[] b = new byte[8];
                int l = System.in.read(b);
//                System.out.println("\r" + Arrays.toString(b));
                byte[] buffer = new byte[l];

                System.arraycopy(b, 0, buffer, 0, l);
                active_keys.add(buffer);
                if (globalKeyHandling) {
                    GlobalKeyHandling();
                }
//                System.out.println("\r" + Arrays.toString(buffer));


//                Preserving old code is not about dwelling on the past,
//                but about arming the future with the wisdom
//                to avoid repeating its messiest spaghetticode.
//                ~"ChatGPT" _probably_ 2025*
//
//                int key = System.in.read();
//                System.in.read();
//                key_buffer.add(key);
////                if (last_key == 27) {
////                    continue;
////                }
//                long current_time = System.currentTimeMillis();
//                if (current_time - last_key_time > 5) {
//                    StringBuilder res = new StringBuilder("\rPressed: ");
//                    for (int k : key_buffer) {
//                        if (printable((char) k)) {
//                            res.append((char) k).append(", ");
//                        } else {
//                            res.append(k).append(", ");
//                        }
//                    }
//                    System.out.println(res.toString());
//                    key_buffer.clear();
//                    last_key_time = current_time;
//                }
//                last_key = key;


//                modifier = ModifierKeycodes.NONE;
//                int key = System.in.read(); // Read a single character
//                if (key == '\033' && System.in.read() == '[') {
//                    char c = (char) System.in.read();
//                    String res = "\rkey: " + c;
//                    if (c == '1') {
//                        int d = System.in.read();
//                        if (d != ';') {
//                            throw new RuntimeException("expected ';', got " + d);
//                        }
//                        c = (char) System.in.read();
//                        res += ";" + c;
//                    }
//                    c = (char) System.in.read();
//                    res += ";" + c;
//                    System.out.println(res);
//                    if (key == 27) {
//                        ReadableByteChannel inChannel = Channels.newChannel(System.in);
//                        ByteBuffer buffer = ByteBuffer.allocate(256);
//
//                        long startTime = System.currentTimeMillis();
//                        while (System.currentTimeMillis() - startTime < 100) { // 5-second timeout
//                            inChannel.read(buffer);
//                        }
//                        buffer.flip(); // Prepare for reading
//                        System.out.println("\r" + new String(buffer.array(), 0, buffer.limit()));
//                    } else {
//                        System.out.print('\r');
//                        System.out.print("ASCII: " + key);
//                        if (printable((char) key)) {
//                            System.out.print(", Key: " + (char) key);
//                        }
//                        System.out.println();
//                        if (key == 'q') { // Quit on 'q'
//                            break;
//                        }
//                    }
//                }
            }
            disableRawMode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
    }

    static void enableRawMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty raw -echo < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    static void enableRawEchoMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty raw < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    static void disableRawMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty cooked echo < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    public void GlobalKeyHandling() {
        ArrayList<byte[]> toRemove = new ArrayList<>();
//        System.out.print(Arrays.toString(active_keys.peek()));
//        System.out.print(", ");
//        System.out.println(Arrays.toString(KeyCodes.N.getCtrlCode()));
        for (KeyBind keybind : AVandiniEliaBzGuessGame.global_keybinds) {
            for (byte[] pressed_key : active_keys) {
                for (byte[] key : keybind.keys) {
                    if (Arrays.equals(pressed_key, key)) {
                        toRemove.add(key);
                        keybind.exec(key);
                        break;
                    }
                }
            }
        }
        KeyListenenThread.active_keys.removeAll(toRemove);
    }

    static boolean printable(char d) {
        if ((d > 31 && d < 128) || (d > 160)) {
            return true;
        }
        return false;
    }
}

class TextBox implements Serializable {

    StringBuilder text = new StringBuilder();
    int cursor_pos = 0;
    Point selection_pos = new Point(-1, -1);
    boolean command_mode = true;
    static ArrayList<String> command_history = new ArrayList<>();
    static int history_index = 0;
    String result;

    void set_base_keymap() {
        KeyListenenThread.keymap.clear();
//        KeyListenenThread.keymap.put(KeyCodes.ESCAPE.getCode())
        KeyListenenThread.keymap.put(KeyCodes.ENTER.getCode(), n -> submit());
        KeyListenenThread.keymap.put(KeyCodes.DELETE.getCode(), n -> delete(KeyModifier.NONE, false));
        KeyListenenThread.keymap.put(KeyCodes.BACKSPACE.getCode(), n -> delete(KeyModifier.NONE, true));
        KeyListenenThread.keymap.put(KeyCodes.DELETE.getCtrlCode(), n -> delete(KeyModifier.CTRL, false));
        KeyListenenThread.keymap.put(KeyCodes.BACKSPACE.getCtrlCode(), n -> delete(KeyModifier.CTRL, true));
//        KeyListenenThread.keymap.put(KeyCodes.UP_ARROW.getCode())
//        KeyListenenThread.keymap.put(KeyCodes.DOWN_ARROW.getCode())

        KeyListenenThread.keymap.put(KeyCodes.END.getCode(), n -> move_cursor(KeyModifier.ALT, +1));
        KeyListenenThread.keymap.put(KeyCodes.HOME.getCode(), n -> move_cursor(KeyModifier.ALT, -1));
        KeyListenenThread.keymap.put(KeyCodes.RIGHT_ARROW.getCode(), n -> move_cursor(KeyModifier.NONE, +1));
        KeyListenenThread.keymap.put(KeyCodes.LEFT_ARROW.getCode(), n -> move_cursor(KeyModifier.NONE, -1));
        KeyListenenThread.keymap.put(KeyCodes.RIGHT_ARROW.getShiftCode(), n -> move_cursor(KeyModifier.SHIFT, +1));
        KeyListenenThread.keymap.put(KeyCodes.LEFT_ARROW.getShiftCode(), n -> move_cursor(KeyModifier.SHIFT, -1));
        KeyListenenThread.keymap.put(KeyCodes.RIGHT_ARROW.getCtrlCode(), n -> move_cursor(KeyModifier.CTRL, +1));
        KeyListenenThread.keymap.put(KeyCodes.LEFT_ARROW.getCtrlCode(), n -> move_cursor(KeyModifier.CTRL, -1));
        KeyListenenThread.keymap.put(KeyCodes.RIGHT_ARROW.getAltCode(), n -> move_cursor(KeyModifier.ALT, +1));
        KeyListenenThread.keymap.put(KeyCodes.LEFT_ARROW.getAltCode(), n -> move_cursor(KeyModifier.ALT, -1));
        KeyListenenThread.keymap.put(KeyCodes.RIGHT_ARROW.getAltShiftCode(), n -> move_cursor(KeyModifier.ALT_SHIFT, +1));
        KeyListenenThread.keymap.put(KeyCodes.LEFT_ARROW.getAltShiftCode(), n -> move_cursor(KeyModifier.ALT_SHIFT, -1));

        KeyListenenThread.keymap.put(KeyCodes.DOWN_ARROW.getCode(), n -> cycle_history_down());
        KeyListenenThread.keymap.put(KeyCodes.UP_ARROW.getCode(), n -> cycle_history_up());
        KeyListenenThread.keymap.put(KeyCodes.DOWN_ARROW.getShiftCode(), n -> cycle_history_bottom());
        KeyListenenThread.keymap.put(KeyCodes.UP_ARROW.getShiftCode(), n -> cycle_history_top());

        KeyListenenThread.keymap.put(KeyCodes.NUM_1.getCode(), n -> insert("1"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_2.getCode(), n -> insert("2"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_3.getCode(), n -> insert("3"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_4.getCode(), n -> insert("4"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_5.getCode(), n -> insert("5"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_6.getCode(), n -> insert("6"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_7.getCode(), n -> insert("7"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_8.getCode(), n -> insert("8"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_9.getCode(), n -> insert("9"));
        KeyListenenThread.keymap.put(KeyCodes.NUM_0.getCode(), n -> insert("0"));
        KeyListenenThread.keymap.put(KeyCodes.PERIOD.getCode(), n -> insert("."));

        KeyListenenThread.keymap.put(KeyCodes.A.getCode(), n -> insert("a"));
        KeyListenenThread.keymap.put(KeyCodes.B.getCode(), n -> insert("b"));
        KeyListenenThread.keymap.put(KeyCodes.C.getCode(), n -> insert("c"));
        KeyListenenThread.keymap.put(KeyCodes.D.getCode(), n -> insert("d"));
        KeyListenenThread.keymap.put(KeyCodes.E.getCode(), n -> insert("e"));
        KeyListenenThread.keymap.put(KeyCodes.F.getCode(), n -> insert("f"));
        KeyListenenThread.keymap.put(KeyCodes.G.getCode(), n -> insert("g"));
        KeyListenenThread.keymap.put(KeyCodes.H.getCode(), n -> insert("h"));
        KeyListenenThread.keymap.put(KeyCodes.I.getCode(), n -> insert("i"));
        KeyListenenThread.keymap.put(KeyCodes.J.getCode(), n -> insert("j"));
        KeyListenenThread.keymap.put(KeyCodes.K.getCode(), n -> insert("k"));
        KeyListenenThread.keymap.put(KeyCodes.L.getCode(), n -> insert("l"));
        KeyListenenThread.keymap.put(KeyCodes.M.getCode(), n -> insert("m"));
        KeyListenenThread.keymap.put(KeyCodes.N.getCode(), n -> insert("n"));
        KeyListenenThread.keymap.put(KeyCodes.O.getCode(), n -> insert("o"));
        KeyListenenThread.keymap.put(KeyCodes.P.getCode(), n -> insert("p"));
        KeyListenenThread.keymap.put(KeyCodes.Q.getCode(), n -> insert("q"));
        KeyListenenThread.keymap.put(KeyCodes.R.getCode(), n -> insert("r"));
        KeyListenenThread.keymap.put(KeyCodes.S.getCode(), n -> insert("s"));
        KeyListenenThread.keymap.put(KeyCodes.T.getCode(), n -> insert("t"));
        KeyListenenThread.keymap.put(KeyCodes.U.getCode(), n -> insert("u"));
        KeyListenenThread.keymap.put(KeyCodes.V.getCode(), n -> insert("v"));
        KeyListenenThread.keymap.put(KeyCodes.W.getCode(), n -> insert("w"));
        KeyListenenThread.keymap.put(KeyCodes.X.getCode(), n -> insert("x"));
        KeyListenenThread.keymap.put(KeyCodes.Y.getCode(), n -> insert("y"));
        KeyListenenThread.keymap.put(KeyCodes.Z.getCode(), n -> insert("z"));

        KeyListenenThread.keymap.put(KeyCodes.A.getShiftCode(), n -> insert("A"));
        KeyListenenThread.keymap.put(KeyCodes.B.getShiftCode(), n -> insert("B"));
        KeyListenenThread.keymap.put(KeyCodes.C.getShiftCode(), n -> insert("C"));
        KeyListenenThread.keymap.put(KeyCodes.D.getShiftCode(), n -> insert("D"));
        KeyListenenThread.keymap.put(KeyCodes.E.getShiftCode(), n -> insert("E"));
        KeyListenenThread.keymap.put(KeyCodes.F.getShiftCode(), n -> insert("F"));
        KeyListenenThread.keymap.put(KeyCodes.G.getShiftCode(), n -> insert("G"));
        KeyListenenThread.keymap.put(KeyCodes.H.getShiftCode(), n -> insert("H"));
        KeyListenenThread.keymap.put(KeyCodes.I.getShiftCode(), n -> insert("I"));
        KeyListenenThread.keymap.put(KeyCodes.J.getShiftCode(), n -> insert("J"));
        KeyListenenThread.keymap.put(KeyCodes.K.getShiftCode(), n -> insert("K"));
        KeyListenenThread.keymap.put(KeyCodes.L.getShiftCode(), n -> insert("L"));
        KeyListenenThread.keymap.put(KeyCodes.M.getShiftCode(), n -> insert("M"));
        KeyListenenThread.keymap.put(KeyCodes.N.getShiftCode(), n -> insert("N"));
        KeyListenenThread.keymap.put(KeyCodes.O.getShiftCode(), n -> insert("O"));
        KeyListenenThread.keymap.put(KeyCodes.P.getShiftCode(), n -> insert("P"));
        KeyListenenThread.keymap.put(KeyCodes.Q.getShiftCode(), n -> insert("Q"));
        KeyListenenThread.keymap.put(KeyCodes.R.getShiftCode(), n -> insert("R"));
        KeyListenenThread.keymap.put(KeyCodes.S.getShiftCode(), n -> insert("S"));
        KeyListenenThread.keymap.put(KeyCodes.T.getShiftCode(), n -> insert("T"));
        KeyListenenThread.keymap.put(KeyCodes.U.getShiftCode(), n -> insert("U"));
        KeyListenenThread.keymap.put(KeyCodes.V.getShiftCode(), n -> insert("V"));
        KeyListenenThread.keymap.put(KeyCodes.W.getShiftCode(), n -> insert("W"));
        KeyListenenThread.keymap.put(KeyCodes.X.getShiftCode(), n -> insert("X"));
        KeyListenenThread.keymap.put(KeyCodes.Y.getShiftCode(), n -> insert("Y"));
        KeyListenenThread.keymap.put(KeyCodes.Z.getShiftCode(), n -> insert("Z"));

    }

    void set_guess_mode_keymap() {
        set_base_keymap();
    }

    void set_comand_mode_keymap() {
        set_base_keymap();

        KeyListenenThread.keymap.put(KeyCodes.SPACE.getCode(), n -> insert(" "));
    }

    void submit() {
        result = text.toString();
        command_history.set(0, text.toString());
        history_index = 0;
        text = new StringBuilder();
        cursor_pos = 0;
        command_history.addFirst("");
        System.out.println();
    }

    void cycle_history_up() {
        if (command_history.isEmpty()) {
            return;
        }
        if (history_index == 0) {
            command_history.set(0, text.toString());
        }
//        if (!text.toString().equals(command_history.get(history_index))) {
//            history_index = 0;
//        }
        if (history_index >= command_history.size() - 1) {
            return;
        }
        history_index++;
        text = new StringBuilder(command_history.get(history_index));
        cursor_pos = text.length();
    }

    void cycle_history_down() {
        if (command_history.isEmpty()) {
            return;
        }
//        if (!text.toString().equals(command_history.get(history_index))) {
//            history_index = 0;
//        }
        if (history_index <= 0) {
            return;
        }
        history_index--;
        text = new StringBuilder(command_history.get(history_index));
        cursor_pos = text.length();
    }

    void cycle_history_top() {
        if (command_history.isEmpty()) {
            return;
        }
        if (history_index == 0) {
            command_history.set(0, text.toString());
        }
        history_index = command_history.size() - 1;
        text = new StringBuilder(command_history.get(history_index));
        cursor_pos = text.length();
    }

    void cycle_history_bottom() {
        if (command_history.isEmpty()) {
            return;
        }
        history_index = 0;
        text = new StringBuilder(command_history.get(history_index));
        cursor_pos = text.length();
    }

    int findWordEndToRight(String input, int start_pos) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        if (start_pos == input.length()) {
            return input.length();
        }
        int i = start_pos + 1;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        if (i == input.length()) {
            return input.length();
        }
        while (i < input.length() && !Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return i;
    }

    int findWordStartToLeft(String input, int start_pos) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        if (start_pos == 0) {
            return 0;
        }
        int i = start_pos - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i))) {
            i--;
        }
        if (i < 0) {
            return 0;
        }
        while (i >= 0 && !Character.isWhitespace(input.charAt(i))) {
            i--;
        }
        return i + 1;
    }

    void move_cursor(KeyModifier modifier, int pos) {
        if (modifier == KeyModifier.ALT) {
            if (pos < 0) {
                cursor_pos = 0;
            } else if (pos > 0) {
                cursor_pos = text.length();
            }
        }
        if (modifier == KeyModifier.CTRL || modifier == KeyModifier.ALT_SHIFT) {
            if (pos < 0) {
                cursor_pos = findWordStartToLeft(text.toString(), cursor_pos);
            } else if (pos > 0) {
                cursor_pos = findWordEndToRight(text.toString(), cursor_pos);
            }
        }
        if (modifier == KeyModifier.SHIFT || modifier == KeyModifier.NONE) {
            cursor_pos += pos;
        }
        if (modifier == KeyModifier.SHIFT || modifier == KeyModifier.ALT_SHIFT) {
            selection_pos.y = cursor_pos;
        } else {
            selection_pos.x = cursor_pos;
            selection_pos.y = cursor_pos;
        }
    }

    void delete(KeyModifier modifier, boolean backspace) {
        if (selection_pos.x < selection_pos.y) {
            text.delete(selection_pos.x, selection_pos.y);
            cursor_pos = selection_pos.x;
            selection_pos.y = selection_pos.x;
        } else if (selection_pos.y < selection_pos.x) {
            text.delete(selection_pos.y, selection_pos.x);
            cursor_pos = selection_pos.y;
            selection_pos.x = selection_pos.y;
        } else if (backspace) {
            if (modifier == KeyModifier.CTRL) {
                int wordStartToLeft = findWordStartToLeft(text.toString(), cursor_pos);
                text.delete(wordStartToLeft, cursor_pos);
                cursor_pos = wordStartToLeft;
            } else if (cursor_pos > 0) {
                text.delete(cursor_pos - 1, cursor_pos);
                cursor_pos--;
            }
        } else {
            if (modifier == KeyModifier.CTRL) {
                text.delete(cursor_pos, findWordEndToRight(text.toString(), cursor_pos));
            } else if (cursor_pos < text.length()) {
                text.delete(cursor_pos, cursor_pos + 1);
            }
        }
    }

    void insert(String s) {
        text.insert(cursor_pos, s);
        cursor_pos += s.length();
        validate_cursor_pos();
        validate_selection_pos();
    }

    void validate_cursor_pos() {
        if (cursor_pos > text.length()) {
            cursor_pos = text.length();
        }
        if (cursor_pos < 0) {
            cursor_pos = 0;
        }
    }

    void validate_selection_pos() {
        if (selection_pos.x > text.length()) {
            selection_pos.x = text.length();
        }
        if (selection_pos.x < 0) {
            selection_pos.x = 0;
        }

        if (selection_pos.y > text.length()) {
            selection_pos.y = text.length();
        }
        if (selection_pos.y < 0) {
            selection_pos.y = 0;
        }
    }

    char getCharSafely(StringBuilder sb, int index, char fallback) {
        if (index >= 0 && index < sb.length()) {
            return sb.charAt(index);
        } else {
            return fallback;
        }
    }

    String get_input(Game parent) {
        result = "";

        set_guess_mode_keymap();

        long last_tick = System.currentTimeMillis();

        if (command_history.isEmpty()) {
            command_history = new ArrayList<>();
            command_history.addFirst(text.toString());
        }

        while (!parent.lost && !parent.won && !parent.ai && AVandiniEliaBzGuessGame.current_game == parent) {
            if (System.currentTimeMillis() < last_tick + 1) {
                continue;
            }
            last_tick = System.currentTimeMillis();


            AVandiniEliaBzGuessGame.KeyHandling();
            validate_cursor_pos();
            validate_selection_pos();
            if (!result.isEmpty()) {
                KeyListenenThread.keymap.clear();
                if (!command_mode) {
                    result = result.toUpperCase();
                }
                return result;
            }
            if (getCharSafely(text, 0, ' ') == '.' && !command_mode) {
                command_mode = true;
                set_comand_mode_keymap();
            } else if (getCharSafely(text, 0, ' ') != '.' && command_mode) {
                command_mode = false;
                set_guess_mode_keymap();
            }

            AVandiniEliaBzGuessGame.eraseLine();
            AVandiniEliaBzGuessGame.resetAttrributes();
            AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
            System.out.print(parent.attempts_left + "> ");
            AVandiniEliaBzGuessGame.resetAttrributes();
            String to_display = text.toString();
            if (!command_mode) {
                to_display = to_display.toUpperCase();
            }
            System.out.print(to_display);
            if (selection_pos.x != selection_pos.y) {
//                System.out.print(selection_pos.x + " != " + selection_pos.y);
                AVandiniEliaBzGuessGame.moveCursor(min(selection_pos.x, selection_pos.y) + 5, CursorMoveDirection.COLUMN);
                AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                System.out.print(to_display.substring(min(selection_pos.x, selection_pos.y), max(selection_pos.x, selection_pos.y)));
                AVandiniEliaBzGuessGame.resetAttrributes();
                AVandiniEliaBzGuessGame.moveCursor(to_display.length() + 5, CursorMoveDirection.COLUMN);
            }
            AVandiniEliaBzGuessGame.resetAttrributes();
            System.out.print(" ");
//            System.out.print(command_history);

            AVandiniEliaBzGuessGame.moveCursor(5 + cursor_pos, CursorMoveDirection.COLUMN);
        }
        return "";
    }
}