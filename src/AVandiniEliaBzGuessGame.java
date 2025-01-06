import java.io.*;
import java.util.*;

import static java.lang.Math.pow;

/**
 * Enum representing different categories of commands.
 */
enum CommandCategory {
    BASIC,
    INGAME,
    SECRET
}

/**
 * Interface for attributes with an index.
 */
interface AbstarctAttributes {
    int getIndex();
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
    int highscore = 0;
    ArrayList<Game> games = new ArrayList<Game>();
    Game current_game;

    public GameSerilizer(int highscore, ArrayList<Game> games, Game current_game) {
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
    static char[] options = {'A', 'B', 'C', 'D', 'E', 'F'};
    static int highscore = 0;
    static Command[] comands = new Command[]{new CommandHelp(), new CommandP(), new CommandSetCode(), new CommandBuy(), new CommandQuit(), new CommandNew(), new CommandHistory(), new CommandRules(), new CommandClose()};
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
                current_game.gameloop();
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("WARNING: unable to read gamestate");
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
            System.out.println("WARNING: unable to save gamestate");
        }
    }

    /**
     * Displays a greeting message.
     */
    static void greeting() {
        System.out.println("Programmed by Vandini Elia");
        new CommandHelp().exec(new String[]{""});
    }

    /**
     * Displays the game rules.
     */
    static void rules() {
        System.out.println("rules");
    }

    /**
     * Displays the win screen with score.
     */
    static void winScreen() {
        System.out.println("Congratulations, Score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
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
        System.out.println("====================================");
        System.out.println("           BzGuessGame Help         ");
        System.out.println();
        System.out.println("Welcome to my BzGuessGame! Your goal is to guess the secret code.");
        System.out.println();
        System.out.println("Rules:");
        System.out.println("- The secret code consists of 4 characters from {a, b, c, d, e, f}.");
        System.out.println("- Characters can appear zero to four times.");
        System.out.println("- You have 20 attempts to guess the code.");
        System.out.println("- After each guess, you'll receive feedback:");
        System.out.println("  X: Correct character at the correct position.");
        System.out.println("  -: Correct character but at the wrong position.");
        System.out.println();
        System.out.println("Check out available commands with .help or .h!");
        System.out.println("to execute a command prefix a '.' before the command.");
        System.out.println("'HELP' will be interpreted as a guess while '.help' or '.h' is a command.");
        System.out.println();
        System.out.println("Good luck!");
        System.out.println("====================================");
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
        shortc = "t";
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
        category = CommandCategory.INGAME;
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
        shortc = "s";
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

/**
 * Class representing a game instance.
 */
class Game implements Serializable {
    char[] code = new char[4];
    int attempts_left = 20;
    String history = "";
    boolean won = false;
    boolean lost = false;
    Date start_date = new Date();
    int score = 0;

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
            if (command.shortc.equals(input) || command.longc.equals(input)) {
                command.exec(args);
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
            for (char l : input.toCharArray()) {
                for (int i = 0; i < AVandiniEliaBzGuessGame.options.length; i++) {
                    if (l != AVandiniEliaBzGuessGame.options[i]) {
                        throw new InvalidInputException("input must consist of A, B, C, D, E or F");
                    }
                }
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
        int score = 0;
        StringBuilder result = new StringBuilder();
        boolean[] cleared = {false, false, false, false};
        for (int i = 0; i < 4; i++) {
            if (code[i] == Character.toUpperCase(input.charAt(i))) {
                result.append('X');
                cleared[i] = true;
                score++;
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (code[i] == Character.toUpperCase(input.charAt(j))) {
                    if (cleared[j] || cleared[i]) {
                        continue;
                    }
                    result.append('-');
                }
            }
        }
        if (score >= 4) {
            won = true;
        }
        AVandiniEliaBzGuessGame.current_game.history += attempts_left + ">" + input + " " + result + '\n';

        return result.toString();
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
            score = score_calc(diff, attempts_left);
            if (score > AVandiniEliaBzGuessGame.highscore) {
                AVandiniEliaBzGuessGame.highscore = score;
            }
            AVandiniEliaBzGuessGame.winScreen();
        } else {
            score = 0;
            AVandiniEliaBzGuessGame.loosescreen();
        }
    }
}

}