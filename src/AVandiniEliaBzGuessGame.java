import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;

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

    public GameSerilizer(long highscore, ArrayList<Game> games, Game current_game) {
        this.highscore = highscore;
        this.games = games;
        this.current_game = current_game;
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
    static long highscore = 0;
    static Command[] comands = new Command[]{new CommandHelp(), new CommandP(), new CommandSetCode(), new CommandRemains(), new CommandBuy(), new CommandQuit(), new CommandNew(), new CommandHistory(), new CommandRules(), new CommandClose(), new CommandBuyAI(), new CommandAI(), new CommandUnlimitedAttempts()};
    static ArrayList<Game> games = new ArrayList<Game>();
    static Game current_game;
    static GameSerilizer gameSerilizer = new GameSerilizer(highscore, games, current_game);

    /**
     * Main method to start the game.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        greeting();
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
        System.out.println("Programmed by Vandini Elia");
        new CommandHelp().exec(new String[]{""});
        AVandiniEliaBzGuessGame.eraseLinesUp(2);
    }

    /**
     * Displays the win screen with score.
     */
    static void winScreen() {
        System.out.println("Congratulations, Score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
        askIfPlayAgain();
    }

    /**
     * Displays the win screen with score.
     */
    static void aiWinScreen() {
        System.out.println("Hypothetical score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
        askIfPlayAgain();
    }

    /**
     * Displays the lose screen with the secret code.
     */
    static void loosescreen() {
        System.out.println("You lost! Secret code was " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code) + ".");
        askIfPlayAgain();
    }

    /**
     * Displays the lose screen with the secret code.
     */
    static void aiLooseScreen() {
        System.out.println("Not even the AI could save you☠\uFE0F. Secret code was " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code) + ".");
        askIfPlayAgain();
    }

    /**
     * Asks the player if they want to play again.
     */
    static void askIfPlayAgain() {
        try {
            System.out.print("Want to try again? (Y/N) ");
            Scanner sc = new Scanner(System.in);
            String inp = sc.nextLine();
            if (inp.equalsIgnoreCase("Y")) {
                newGame();
            } else if (inp.equalsIgnoreCase("N")) {
                System.out.println("See you soon!");
            } else {
                throw new InvalidInputException("please answer Y or N");
            }
        } catch (InvalidInputException e) {
            AVandiniEliaBzGuessGame.eraseLinesUp(1);
            askIfPlayAgain();
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

    static void setCursorStyle(CursorStyles style) {
        System.out.print("\u001b[" + style.getIndex() + " q");
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

    Command() {
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
            System.out.println("\n" + c.toString().toUpperCase());
            for (Command command : AVandiniEliaBzGuessGame.comands) {
                if (c != command.category) {
                    continue;
                }
                String help_string = command.shortc + " | " + command.longc;
                help_string = help_string + space_storage.substring(help_string.length()) + command.description;
                System.out.println(help_string);
            }
        }
        System.out.println();
        System.out.println("Software by " + AVandiniEliaBzGuessGame.Author + ". Version " + AVandiniEliaBzGuessGame.Version);
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
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                             BzGuessGame Help                              ║");
        System.out.println("║                                                                           ║");
        System.out.println("║ Welcome to my BzGuessGame! Your goal is to guess the secret code.         ║");
        System.out.println("║                                                                           ║");
        System.out.println("║ - The secret code consists of 4 characters from {a, b, c, d, e, f}.       ║");
        System.out.println("║ - Characters can appear zero to four times.                               ║");
        System.out.println("║ - You have 20 attempts to guess the code.                                 ║");
        System.out.println("║ - After each guess, you'll receive feedback:                              ║");
        System.out.println("║   X: Correct character at the correct position.                           ║");
        System.out.println("║   -: Correct character but at the wrong position.                         ║");
        System.out.println("║                                                                           ║");
        System.out.println("║ Check out available commands with .help or .h!                            ║");
        System.out.println("║ To execute a command prefix a '.' before the command.                     ║");
        System.out.println("║ 'HELP' will be interpreted as a guess while '.help' or '.h' is a command. ║");
        System.out.println("║                                                                           ║ ");
        System.out.println("║ Good luck!                                                                ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝ ");
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
        System.out.println(AVandiniEliaBzGuessGame.current_game.code);
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
        System.out.println(AVandiniEliaBzGuessGame.current_game.code);
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
        AVandiniEliaBzGuessGame.saveGameState();
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
        System.out.println(res_string);
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

    void exec(String[] args) {
        System.out.println("Select a game history to view");
        for (int i = 0; i < AVandiniEliaBzGuessGame.games.size(); i++) {
            StringBuilder sb = new StringBuilder();
            Game g = AVandiniEliaBzGuessGame.games.get(i);
            sb.append(AVandiniEliaBzGuessGame.games.size() - 1 - i).append(") ");
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
            sb.append(g.score);
            sb.append(g.attempts_left).append(" | ");
            sb.append(g.start_date);
            System.out.println(sb);
        }
        System.out.println();
        AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
        System.out.print("> ");
        AVandiniEliaBzGuessGame.resetAttrributes();
        Scanner sc = new Scanner(System.in);
        try {
            int selcted = sc.nextInt();
            System.out.println(AVandiniEliaBzGuessGame.games.reversed().get(selcted).history);
        } catch (InputMismatchException e) {
            System.out.println("Invalid Input");
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
                System.out.print(g.attempts_left + ">");
                AVandiniEliaBzGuessGame.resetAttrributes();
                char[] nextGuess = g.solver.minimaxBestGuess(g.matches, g.guesses);
                for (char c : nextGuess) {
                    TimeUnit.MILLISECONDS.sleep(250);
                    System.out.print(c);
                }
                TimeUnit.MILLISECONDS.sleep(250);
                System.out.println();
                String res = g.parseGuess(new String(nextGuess));
                System.out.println(res);
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
        System.out.println("Sure, here is a optimal 4-letter guess: [" + new String(nextGuess) + "] . let me know if you have anymore questions!");
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
        System.out.println("Based on past guesses feedback there are " + solution_count + " viable solutions.");
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
        try {
            String input;
            input = askInput();
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
            System.out.println(parseGuess(input));
            attempts_left--;
            if (attempts_left <= 0) {
                lost = true;
            }
        } catch (InvalidInputException e) {
            AVandiniEliaBzGuessGame.eraseLinesUp(1);
            AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
            System.out.print(attempts_left + "> ");
            AVandiniEliaBzGuessGame.resetAttrributes();
            AVandiniEliaBzGuessGame.setAttribute(FColors.RED);
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
            System.out.println(e.getMessage());
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.RESET);
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
        result.append("X".repeat(Math.max(0, p.x)));
        result.append("-".repeat(Math.max(0, p.y)));
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
            long diff = Math.abs(new Date().getTime() - this.start_date.getTime());
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