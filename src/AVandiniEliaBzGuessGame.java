/**
 * MIT License
 * <p>
 * Copyright (c) 2025 Vandini Elia
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// check this project out at https://github.com/eliavandini/AVandiniEliaBzGuessGame/


/*
 * Dear Reader,
 * The following game was developed form december 2024 to february 2025.
 * It is a assignment from the course Intro to Programming at the Free University Bolzano/Boxen.
 * While the assignment itself was very straightforward and could have been completed
 * in less than a few hundred lines of code, I decided to challenge myself and program
 * something i could learn from and improve my java skills.
 *
 * Some goals i had in mind:
 *    - make use of ANSII escape codes
 *    - make use of inheritance (which was not discussed in the course)
 *    - Object oriented system for commands with arguments and dynamic help screen.
 *    - write a program that solves the game.
 *    - make the UI aesthetically pleasing
 *    - ditch the cooked terminal mode and take it raw
 *    - add dark fantasy adventurer lore for game immersion
 *    - use threads
 *
 * I was not able to complete every objective but still consider this a successful project
 * In the end i had a lot of fun and learned that the real treasure was the bugs i found along the way.
 * FWM life, can't wait to drop this project and work on something that pays the bills.
 *
 * E.V.
 */

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.lang.Math.*;

/*
 * Brief code explaination!!1!11!1!!
 *
 * AVandiniEliaBzGuessGame is the main class. it holds stuff such as highscore, list of commands,
 * list of keybindings and lots of miscellaneous logic as well as the main method.
 * The Game class represents a game, running or finished. while it is running it parses guesses,
 * executed commands if requested all in cycles of turns.
 * At the beginning of each turn the TextBox class will query the user for a console input.
 * All of the fancy UI shenanigans are based in this class and synthesised through
 * elaborate and finely crafted ANSII escape code magic.
 * It also listens for keybinds enabling a very smooth and natural navigation of the user input.
 * The keybind handling itself is only possible through the KeyListenThread which runs parallel to every other process.
 * Everything else is pretty selfexplanatorily or explained in the comments
 */


/**
 * Represents different categories of commands available in the game.
 * Each category groups commands based on their functionality.
 */
enum CommandCategory {
    BASIC,  // Basic commands like help or quit.
    INGAME, // Commands used during gameplay.
    STORE,  // Commands related to in-game purchases.
    SECRET  // Hidden or special commands.
}

/**
 * Provides a method to retrieve the index associated with the attribute.
 * This interface is used by enumerations representing styles, colors, or other attributes.
 */
interface AbstarctAttributes {
    /**
     * Retrieves the index of the attribute.
     *
     * @return the index representing the attribute.
     */
    int getIndex();
}

/**
 * Enum representing various cursor styles.
 * Each style has a unique index for terminal display configuration.
 */
enum CursorStyles implements AbstarctAttributes {
    BLINK_BLOCK(0),
    BLINKING_BLOCK(1),
    STEADY_BLOCK(2),
    BLINKING_UNDERLINE(3),
    STEADY_UNDERLINE(4),
    BLINKING_BAR(5),
    STEADY_BAR(6);

    private final int index;

    /**
     * Constructs a CursorStyles enumeration with the specified index.
     *
     * @param index the index associated with the cursor style.
     */
    CursorStyles(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return index;
    }

    /**
     * Retrieves a cursor style based on its index.
     *
     * @param index the index of the cursor style to retrieve.
     * @return the corresponding CursorStyles instance, or null if not found.
     */
    public static CursorStyles getCursorStyles(int index) {
        for (CursorStyles cursorStyles : CursorStyles.values()) {
            if (cursorStyles.getIndex() == index) {
                return cursorStyles;
            }
        }
        return null;
    }
}

/**
 * Enum representing text attributes for styling text in the terminal.
 * Each attribute is associated with a specific index.
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

    /**
     * Constructs a TextAttributes enumeration with the specified index.
     *
     * @param index the index associated with the text attribute.
     */
    TextAttributes(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return index;
    }
}

/**
 * Enum representing reset attributes for text styles.
 * These attributes are used to reset specific text styles in the terminal.
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

    /**
     * Constructs a ResetTextAttributes enumeration with the specified index.
     *
     * @param index the index associated with the reset attribute.
     */
    ResetTextAttributes(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return index;
    }
}

/**
 * Enum representing foreground colors for text in the terminal.
 * Each color is associated with a unique index for styling purposes.
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

    /**
     * Constructs an FColors enumeration with the specified index.
     *
     * @param index the index representing the foreground color.
     */
    FColors(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return index;
    }
}

/**
 * Enum representing background colors for text in the terminal.
 * Each color is associated with a unique index for styling purposes.
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

    /**
     * Constructs a BColors enumeration with the specified index.
     *
     * @param index the index representing the background color.
     */
    BColors(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return index;
    }
}

/**
 * Enum representing directions for cursor movement in the terminal.
 * Each direction is associated with a label used in terminal control sequences.
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

    /**
     * Constructs a CursorMoveDirection enumeration with the specified label.
     *
     * @param index the label representing the direction.
     */
    CursorMoveDirection(char index) {
        this.label = index;
    }

    /**
     * Retrieves the label associated with the cursor movement direction.
     *
     * @return the label as a character.
     */
    public char getLabel() {
        return label;
    }
}

/**
 * Exception representing invalid input scenarios in the game.
 * Used to handle user or programmatic errors with specific messages.
 */
class InvalidInputException extends Exception {
    /**
     * Constructs a default InvalidInputException.
     */
    public InvalidInputException() {
    }

    /**
     * Constructs an InvalidInputException with a specific error message.
     *
     * @param message the detailed error message.
     */
    public InvalidInputException(String message) {
        super(message);
    }
}


/**
 * Represents a point on a two-dimensional grid, but is not really used as such in this project.
 * Typically used for storing feedback values (x, y) for guesses.
 */
class Point implements Serializable {
    public int x; // x-coordinate
    public int y; // y-coordinate

    /**
     * Constructs a Point with specified x and y values.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Generates a string representation of the point in a readable format.
     *
     * @return the string representation of the point in the format [x=value, y=value].
     */
    @Override
    public String toString() {
        return "[x=" + this.x + ",y=" + this.y + "]";
    }

    /**
     * Checks whether this Point is equal to another object.
     * Two Points are considered equal if their x and y values are the same.
     *
     * @param obj the object to compare against.
     * @return {@code true} if the object is a Point with the same x and y values,
     * {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point pt)) {
            return super.equals(obj);
        } else {
            return this.x == pt.x && this.y == pt.y;
        }
    }

    /**
     * Generates a hash code for this Point.
     * Ensures consistency between equals and hashCode methods.
     *
     * @return the hash code value for this Point.
     */
    @Override
    public int hashCode() {
        return 31 * x + y; // Combines x and y into a single hash code. Any prime number would work as multiplicand.
    }
}

/**
 * Serializable class for managing game state persistence.
 * This class allows saving and loading the game's state, including high scores, game history,
 * and current game progress.
 */
class GameSerializer implements Serializable {
    long highscore = 0; // The highest score achieved.
    ArrayList<Game> games = new ArrayList<Game>(); // List of all games played.
    Game current_game; // The current active game.
    CursorStyles cursorStyles; // The style of the cursor used in the game.
    ArrayList<String> command_history; // The history of past guesses and commands

    /**
     * Constructs a GameSerializer with the provided game state data.
     *
     * @param highscore       the highest score achieved.
     * @param games           the list of games played.
     * @param current_game    the currently active game.
     * @param cursorStyles    the cursor style used in the game.
     * @param command_history the command and guess history of the game
     */
    public GameSerializer(long highscore, ArrayList<Game> games, Game current_game, CursorStyles cursorStyles, ArrayList<String> command_history) {
        this.highscore = highscore;
        this.games = games;
        this.current_game = current_game;
        this.cursorStyles = cursorStyles;
        this.command_history = command_history;
    }

    /**
     * Saves the current game state to a file.
     *
     * @param gameState the game state to save.
     * @param filePath  the path of the file where the game state will be saved.
     * @throws IOException if an error occurs during file writing.
     */
    public static void saveGameState(GameSerializer gameState, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gameState); // Serialize the GameSerializer object.
        }
    }

    /**
     * Loads the game state from a file.
     *
     * @param filePath the path of the file containing the saved game state.
     * @return the loaded GameSerializer object.
     * @throws IOException            if an error occurs during file reading.
     * @throws ClassNotFoundException if the class definition is not found.
     */
    public static GameSerializer loadGameState(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameSerializer) ois.readObject(); // Deserialize and return the GameSerializer object.
        }
    }
}

/**
 * Main class for the BzGuessGame application.
 * This class contains the game's entry point, configurations, and core methods.
 */
class AVandiniEliaBzGuessGame {
    public static final String Author = "Eia Vandini"; // The author of the game.
    public static final String Version = "v1.34"; // The current version of the game.

    static char[] options = {'A', 'B', 'C', 'D', 'E', 'F'}; // Possible characters in the secret code.
    static CursorStyles cursorStyle = CursorStyles.BLINKING_BAR; // Default cursor style.
    static long highscore = 0; // Current high score.
    static Command[] comands = new Command[]{
            new CommandHelp(), new CommandKeybinds(), new CommandP(), new CommandSetCode(), new CommandRemains(),
            new CommandBuy(), new CommandQuit(), new CommandNew(), new CommandHistory(),
            new CommandRules(), new CommandClose(), new CommandBuyAI(), new CommandAI(),
            new CommandUnlimitedAttempts(), new CommandChangeCursorStyle()
    }; // Array of game commands.

    static KeyBind[] global_keybinds = new KeyBind[]{new KeyBindClose(), new KeyBindNew()}; // Global key bindings.
    static ArrayList<Game> games = new ArrayList<Game>(); // List of games played.
    static Game current_game; // The current game instance.
    static GameSerializer gameSerailizer = new GameSerializer(highscore, games, current_game, cursorStyle, TextBox.command_history); // Serializer for game state.
    static Thread kyThread = new KeyListenenThread(); // Thread for key listening.

    /**
     * The main method for the application.
     * Initializes the game, loads saved state, and starts the key listening thread.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        AVandiniEliaBzGuessGame.setCursorStyle(cursorStyle);
        greeting();
        kyThread.start();
        loadGamestate();
        if (current_game.won || current_game.lost) {
            newGame();
        }
        current_game.gameloop();
        saveGameState();
    }

    /**
     * Loads the game state from the saved file if it exists.
     * If the game state cannot be loaded or is invalid, a new game is started.
     */
    static void loadGamestate() {
        File f = new File("GameState.ser");
        if (f.exists()) {
            try {
                gameSerailizer = GameSerializer.loadGameState("GameState.ser");
                highscore = gameSerailizer.highscore;
                games = gameSerailizer.games;
                current_game = gameSerailizer.current_game;
                TextBox.command_history = gameSerailizer.command_history;
            } catch (Exception e) {
                setAttribute(FColors.YELLOW);
                System.out.println("WARNING: unable to read gamestate (" + Arrays.toString(e.getStackTrace()) + ")");
                resetAttrributes();
                newGame();
            }
        } else {
            newGame();
        }
    }

    /**
     * Saves the current game state to a file.
     * If saving fails, a warning message is displayed.
     */
    static void saveGameState() {
        gameSerailizer.highscore = highscore;
        gameSerailizer.games = games;
        gameSerailizer.current_game = current_game;
        gameSerailizer.command_history = TextBox.command_history;
        try {
            GameSerializer.saveGameState(gameSerailizer, "GameState.ser");
        } catch (IOException e) {
            setAttribute(FColors.YELLOW);
            System.out.println("WARNING: unable to save gamestate (" + e.getMessage() + ")");
            resetAttrributes();
        }
    }

    /**
     * Displays a greeting message to the player at the start of the game.
     * Also provides help by executing the help command.
     */
    static void greeting() {
        System.out.println("\rProgrammed by Vandini Elia");
        new CommandHelp().exec(new String[]{""});
        AVandiniEliaBzGuessGame.eraseLinesUp(2);
    }

    /**
     * Displays a win screen message when the player successfully guesses the secret code.
     * Also prompts the player to decide whether to play again.
     */
    static void winScreen() {
        System.out.println("\rCongratulations, Score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
        askIfPlayAgain();
    }

    /**
     * Displays a hypothetical win screen message when the AI successfully completes the game.
     * Also prompts the player to decide whether to play again.
     */
    static void aiWinScreen() {
        System.out.println("\rHypothetical score is " + AVandiniEliaBzGuessGame.current_game.score + ", (Highscore: " + highscore + ")");
        askIfPlayAgain();
    }

    /**
     * Displays a loss screen message when the player fails to guess the secret code.
     * Also reveals the correct code and prompts the player to decide whether to play again.
     */
    static void loosescreen() {
        System.out.println("\rYou lost! Secret code was " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code) + ".");
        askIfPlayAgain();
    }

    /**
     * Displays a loss screen message when the AI fails to complete the game.
     * Also reveals the correct code and prompts the player to decide whether to play again.
     */
    static void aiLooseScreen() {
        System.out.println("\rNot even the AI could save you☠\uFE0F. Secret code was " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code) + ".");
        askIfPlayAgain();
    }

    /**
     * Prompts the player to decide whether to play another game.
     * Handles key inputs for "Yes" or "No" options and executes the appropriate action.
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
        AVandiniEliaBzGuessGame.showCursor();
        AVandiniEliaBzGuessGame.eraseLinesUp(2);
        if (seleciton.get() == 0) {
            newGame();
        } else {
            System.out.println("\n\rSee you soon!\r");
            new CommandClose().exec(new String[]{});

        }
    }

    /**
     * Starts a new game by creating a new instance of the Game class.
     * Adds the new game to the list of games and sets it as the current game.
     *
     * @return the new Game instance.
     */
    static Game newGame() {
        Game g = new Game();
        games.add(g);
        current_game = g;
        g.startGame();
        return g;
    }

    /**
     * Erases the current line in the terminal.
     * Clears the line and resets the cursor to the beginning of the line.
     */
    static void eraseLine() {
        System.out.print("\u001b[2K\r");
    }

    /**
     * Erases a specified number of lines above the current cursor position.
     * Moves the cursor up and clears lines in the terminal.
     *
     * @param lines the number of lines to erase.
     */
    static void eraseLinesUp(int lines) {
        if (lines == 0) {
            return;
        }
        moveCursor(lines, CursorMoveDirection.UPX);
        System.out.print("\u001b[0J");
    }

    /**
     * Erases all lines from the current cursor position to the end of the screen.
     */
    static void eraseLinesToEndOfScreen() {
        System.out.print("\u001b[0J");
    }

    /**
     * Sets a specific terminal text attribute.
     *
     * @param attr the attribute to set, represented as an AbstarctAttributes instance.
     */
    static void setAttribute(AbstarctAttributes attr) {
        System.out.printf("\u001b[%dm", attr.getIndex());
    }

    /**
     * Sets multiple terminal text attributes at once.
     *
     * @param attr an array of attributes to set.
     */
    static void setAttribute(AbstarctAttributes[] attr) {
        for (AbstarctAttributes a : attr) {
            setAttribute(a);
        }
    }

    /**
     * Sets a 8 bit color in foreground.
     *
     * @param color the color to set.
     */
    static void set8bitForeground(int color) {
        System.out.printf("\u001b[38;5;%dm", color);
    }

    /**
     * Sets a 8 bit color in background.
     *
     * @param color the color to set.
     */
    static void set8bitBackground(int color) {
        System.out.printf("\u001b[48;5;%dm", color);
    }


    /**
     * Resets all text attributes in the terminal to default.
     */
    static void resetAttrributes() {
        setAttribute(TextAttributes.RESET);
    }

    /**
     * Clears the entire terminal screen and moves the cursor to the top-left corner.
     */
    static void clearScreen() {
        System.out.print("\u001b[H\u001b[2J");
    }

    /**
     * Hides the terminal cursor to enhance display aesthetics.
     */
    static void hideCursor() {
        System.out.print("\u001b[?25l");
    }

    /**
     * Shows the terminal cursor if it is currently hidden.
     */
    static void showCursor() {
        System.out.print("\u001b[?25h");
    }

    /**
     * Moves the cursor in a specified direction by a given number of positions.
     *
     * @param pos       the number of positions to move.
     * @param direction the direction to move the cursor, represented by CursorMoveDirection.
     */
    static void moveCursor(int pos, CursorMoveDirection direction) {
        System.out.print("\u001b[" + pos + direction.getLabel());
    }

    /**
     * Saves the current cursor position in the terminal.
     */
    static void saveCursorPosition() {
        System.out.print("\u001b[s");
    }

    /**
     * Restores the cursor position to the last saved position in the terminal.
     */
    static void restoreCursorPosition() {
        System.out.print("\u001b[u");
    }

    /**
     * Sets the cursor style in the terminal.
     * May not work on all devices or terminal configurations.
     *
     * @param style the cursor style to apply, represented by CursorStyles.
     */
    static void setCursorStyle(CursorStyles style) {
        System.out.print("\u001b[" + style.getIndex() + " q");
    }

    /**
     * Prints a string to the terminal with an animated effect.
     * Each character is displayed with a delay, followed by a dot effect.
     *
     * @param res the string to print with the animation.
     */
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
     * Pauses the program execution for a specified duration in milliseconds.
     *
     * @param ms the duration of the pause in milliseconds.
     */
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handles key inputs during the game.
     * Processes active keys from the input queue and triggers the corresponding actions.
     *
     * @return true if a key was successfully processed; false otherwise.
     */
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

    /**
     * Shuffles the characters in a given array randomly.
     *
     * @param input the array of characters to shuffle.
     */
    static void shuffleCharArray(char[] input) {
        Random random = new Random();
        for (int i = input.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char x = input[index];
            input[index] = input[i];
            input[i] = x;
        }
    }
}

/**
 * Represents a key binding that links specific key sequences to an action.
 */
class KeyBind {
    String shortel; // The short description of the key binding.
    String description; // The detailed description of the key binding.
    byte[][] keys; // The byte sequences representing the keys for this binding.

    /**
     * Executes the action associated with this key binding.
     *
     * @param pressedKey the key that was pressed.
     */
    void exec(byte[] pressedKey) {
    }
}

/**
 * Key binding for closing the game.
 * Saves the game state and terminates the application.
 */
class KeyBindClose extends KeyBind {

    /**
     * Constructs a KeyBindClose with the predefined key sequence and description.
     */
    KeyBindClose() {
        shortel = "^Q";
        description = "Saves and close the game";
        keys = new byte[][]{KeyCodes.Q.getCtrlCode()};
    }

    /**
     * Executes the close action by invoking the CommandClose functionality.
     *
     * @param pressedKey the key that was pressed.
     */
    @Override
    void exec(byte[] pressedKey) {
        new CommandClose().exec(new String[]{});
    }
}

/**
 * Key binding for starting a new game.
 * Allows the player to reset the game and begin anew.
 */
class KeyBindNew extends KeyBind {

    /**
     * Constructs a KeyBindNew with the predefined key sequence and description.
     */
    KeyBindNew() {
        shortel = "^N";
        description = "Starts a new game";
        keys = new byte[][]{KeyCodes.N.getCtrlCode()};
    }

    /**
     * Executes the action to start a new game by invoking the CommandNew functionality.
     *
     * @param pressedKey the key that was pressed.
     */
    @Override
    void exec(byte[] pressedKey) {
        new CommandNew().exec(new String[]{});
    }
}

/**
 * Represents a game command, including its name, shortcuts, category, and functionality.
 * Commands are used to interact with the game or perform specific actions.
 */
class Command {
    String fullName; // The full name of the command.
    String longc; // The long form of the command trigger.
    String shortc; // The short form of the command trigger.
    CommandCategory category; // The category to which the command belongs.
    String description; // A detailed description of the command.

    /**
     * Executes the action defined by the command.
     *
     * @param args the arguments passed with the command.
     * @throws InvalidInputException if the input is invalid for the command.
     */
    void exec(String[] args) throws InvalidInputException {
    }
}

/**
 * Command to display help information for all available commands.
 * Lists commands grouped by their categories and provides descriptions.
 */
class CommandHelp extends Command {

    /**
     * Constructs a CommandHelp with predefined attributes.
     */
    CommandHelp() {
        super();
        category = CommandCategory.BASIC;
        longc = "help";
        shortc = "h";
        description = "Display a help screen explaining the game commands";
        fullName = "Help";
    }

    /**
     * Executes the help command by displaying available commands and their descriptions.
     *
     * @param args the arguments passed with the command (e.g., to show secret commands).
     */
    void exec(String[] args) {
        boolean show_secret = false;
        if (args.length > 0 && args[0].equals("-s")) {
            show_secret = true;
        }
        String space_storage = "                  ";

        for (CommandCategory c : CommandCategory.values()) {
            if (c == CommandCategory.SECRET && !show_secret) {
                continue;
            }
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
            System.out.println("\n\r" + c.toString().toUpperCase());
            AVandiniEliaBzGuessGame.resetAttrributes();
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
 * Command to display help information for all available Keybinds.
 * Lists Keybinds and provides descriptions.
 */
class CommandKeybinds extends Command {

    /**
     * Constructs a CommandKeybinds with predefined attributes.
     */
    CommandKeybinds() {
        super();
        category = CommandCategory.BASIC;
        longc = "keys";
        shortc = "k";
        description = "Display a help screen explaining the game keybinds";
        fullName = "Keybinds";
    }

    /**
     * Executes the help command by displaying available keybinds and their descriptions.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
    void exec(String[] args) {
        String space_storage = "         ";
        AVandiniEliaBzGuessGame.set8bitForeground(241);
        System.out.println();
        System.out.println("\r ^X stands for the ctrl modifyer (eg. ^C is ctrl+C)");
        AVandiniEliaBzGuessGame.resetAttrributes();
        System.out.println();
        for (KeyBind keyBind : AVandiniEliaBzGuessGame.global_keybinds) {
            String help_string = "\r    " + keyBind.shortel;
            help_string = help_string + space_storage.substring(help_string.length()) + keyBind.description;
            System.out.println(help_string);
        }
        System.out.println();
        AVandiniEliaBzGuessGame.set8bitForeground(241);
        System.out.println("\rYou can also use the arrow combined with alt and/or shift to move around the input bar."
                + "\n\rThe up and down arrow keys will let you cycle your input history");
        AVandiniEliaBzGuessGame.resetAttrributes();
    }
}

/**
 * Command to display the rules of the game.
 * Provides detailed instructions and guidelines for playing the game.
 */
class CommandRules extends Command {

    /**
     * Constructs a CommandRules with predefined attributes.
     */
    CommandRules() {
        super();
        category = CommandCategory.BASIC;
        longc = "rules";
        shortc = "r";
        description = "Display a help screen explaining the game rules";
        fullName = "Rules";
    }

    /**
     * Executes the rules command by displaying the game rules and gameplay instructions.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
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
        System.out.println("\r║ To execute a command, prefix a '.' before the command.                    ║");
        System.out.println("\r║ 'HELP' will be interpreted as a guess while '.help' or '.h' is a command. ║");
        System.out.println("\r║                                                                           ║ ");
        System.out.println("\r║ Good luck!                                                                ║");
        System.out.println("\r╚═══════════════════════════════════════════════════════════════════════════╝ ");
    }
}

/**
 * Command to reveal the secret code.
 * This is categorized as a secret command and is primarily for debugging or cheating.
 */
class CommandP extends Command {

    /**
     * Constructs a CommandP with predefined attributes.
     */
    CommandP() {
        super();
        category = CommandCategory.SECRET;
        longc = "p";
        shortc = "p";
        description = "Reveals the code";
        fullName = "Reveal Code";
    }

    /**
     * Executes the command to print the secret code to the terminal.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
    void exec(String[] args) {
        System.out.println("\rCode is " + Arrays.toString(AVandiniEliaBzGuessGame.current_game.code));
    }
}

/**
 * Command to quit the game.
 * Marks the current game as lost and ends the session.
 */
class CommandQuit extends Command {

    /**
     * Constructs a CommandQuit with predefined attributes.
     */
    CommandQuit() {
        super();
        category = CommandCategory.BASIC;
        longc = "quit";
        shortc = "q";
        description = "Reveals the code and quits the game";
        fullName = "Quit";
    }

    /**
     * Executes the quit command by marking the current game as lost.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
    void exec(String[] args) {
        AVandiniEliaBzGuessGame.current_game.lost = true;
    }
}

/**
 * Command to close the game.
 * Saves the game state and terminates the application cleanly.
 */
class CommandClose extends Command {

    /**
     * Constructs a CommandClose with predefined attributes.
     */
    CommandClose() {
        super();
        category = CommandCategory.BASIC;
        longc = "close";
        shortc = "c";
        description = "Saves game and quits";
        fullName = "Close Game";
    }

    /**
     * Executes the close command by saving the game state and exiting the application.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
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
 * Resets the current game and begins a new one.
 */
class CommandNew extends Command {

    /**
     * Constructs a CommandNew with predefined attributes.
     */
    CommandNew() {
        super();
        category = CommandCategory.INGAME;
        longc = "new";
        shortc = "n";
        description = "Start over a new game";
        fullName = "New Game";
    }

    /**
     * Executes the new game command by marking the current game as lost and starting a new game.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
    void exec(String[] args) {
        AVandiniEliaBzGuessGame.current_game.lost = true;
        AVandiniEliaBzGuessGame.newGame();
    }
}

/**
 * Command to set a custom secret code.
 * Allows users to define their own code for the game.
 */
class CommandSetCode extends Command {

    /**
     * Constructs a CommandSetCode with predefined attributes.
     */
    CommandSetCode() {
        super();
        category = CommandCategory.SECRET;
        longc = "setcode";
        shortc = "S";
        description = "Set the secret code to a user input";
        fullName = "Set Code";
    }

    /**
     * Executes the set code command, allowing the user to define a new secret code.
     * Validates the input for length and allowed characters.
     *
     * @param args the arguments passed with the command, containing the new code.
     * @throws InvalidInputException if the input is invalid.
     */
    void exec(String[] args) throws InvalidInputException {
        if (args.length > 1) {
            throw new InvalidInputException("Only one argument expected");
        }
        if (args.length < 1) {
            throw new InvalidInputException("At least one argument expected");
        }
        if (args[0].length() != 4) {
            throw new InvalidInputException("New code may only contain 4 characters");
        }
        if (args[0].matches(".*[^ABCDEFabcdef].*")) {
            throw new InvalidInputException("New code must consist of A, B, C, D, E, or F");
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
                throw new InvalidInputException("New code contains invalid characters");
            }
        }
        AVandiniEliaBzGuessGame.current_game.code = args[0].toUpperCase().toCharArray();
        System.out.println("\rThe secret code has been updated");
    }
}

/**
 * Command to buy a letter of the secret code.
 * Reveals one letter of the code in its correct position for a cost of 5 attempts.
 */
class CommandBuy extends Command {

    /**
     * Constructs a CommandBuy with predefined attributes.
     */
    CommandBuy() {
        super();
        category = CommandCategory.STORE;
        longc = "buy";
        shortc = "b";
        description = "Buy one letter of the secret code at its right position. Costs 5 attempts";
        fullName = "Buy";
    }

    /**
     * Executes the buy command, revealing a single character in the correct position.
     * Reduces the number of attempts by 5.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
    void exec(String[] args) {
        Random r = new Random();
        char[] res_string = {'_', '_', '_', '_'};
        int pos = r.nextInt(4); // Randomly selects a position to reveal.
        res_string[pos] = AVandiniEliaBzGuessGame.current_game.code[pos];

        // Removes possible codes that don't match the revealed letter.
        AVandiniEliaBzGuessGame.current_game.solver.possibleCodes.removeIf(
                n -> n[pos] != AVandiniEliaBzGuessGame.current_game.code[pos]
        );

        AVandiniEliaBzGuessGame.current_game.attempts_left -= 5;
        AVandiniEliaBzGuessGame.current_game.history += AVandiniEliaBzGuessGame.current_game.attempts_left
                + "> The User bought " + Arrays.toString(res_string) + " using up 5 attempts\n";
        System.out.println("\r" + Arrays.toString(res_string));


//        not implementing this yet
//        ArrayList<Integer> free_char_pos = new ArrayList<Integer>();
//        for (int i = 0; i < 4; i++) {
//            if (AVandiniEliaBzGuessGame.current_game.discovered_chars[i] == '_') {
//                free_char_pos.add(i);
//            }
//        }
//
//        long start = System.currentTimeMillis();
//
//        long last_tick = System.currentTimeMillis();
//
//        StringBuilder[] canvas = new StringBuilder[]{
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//                new StringBuilder("                                   "),
//        };
//
//        StringBuilder sequence = new StringBuilder();
//        for (int i = 65; i < 123; i++) {
//            sequence.append((char) i);
//        }
//
//        Random r = new Random();
//
//        int index1 = r.nextInt(122 - 65);
//
//        for (StringBuilder y : canvas) {
//            System.out.println("\r" + y);
//        }
//
//
//        while (System.currentTimeMillis() < start + 6000) {
//            if (System.currentTimeMillis() < last_tick + ((System.currentTimeMillis() - start) / 10)) {
//                continue;
//            }
//            AVandiniEliaBzGuessGame.eraseLinesUp(canvas.length);
//            last_tick = System.currentTimeMillis();
//
//            int i = index1;
//            String s = sequence.substring(i % sequence.length(), sequence.length()) + sequence.substring(0, i % sequence.length());
//            for (int j = 0; j < 8; j++) {
//                canvas[j + 1].setCharAt(2, s.charAt(8 - j));
//            }
//            index1 += 1;
//
//
//            for (StringBuilder y : canvas) {
//                System.out.println("\r" + y);
//            }
//
//        }


    }
}

/**
 * Command to display the history of all guesses and evaluations of past games.
 * Allows users to navigate through past game details.
 */
class CommandHistory extends Command {

    /**
     * Constructs a CommandHistory with predefined attributes.
     */
    CommandHistory() {
        super();
        category = CommandCategory.INGAME;
        longc = "history";
        shortc = "H";
        description = "Show history of all guesses and evaluations of past games";
        fullName = "History";
    }

    /**
     * Executes the history command, displaying a navigable list of game histories.
     *
     * @param args the arguments passed with the command (not used for this command).
     * @throws InvalidInputException if no game history is available.
     */
    void exec(String[] args) throws InvalidInputException {
        if (AVandiniEliaBzGuessGame.games.isEmpty()) {
            throw new InvalidInputException("Start a game before trying to access the game history");
        }


        AtomicBoolean loop = new AtomicBoolean(true);
        AtomicBoolean cancel = new AtomicBoolean(false);
        AtomicInteger seleciton = new AtomicInteger();
        seleciton.set(AVandiniEliaBzGuessGame.games.size() - 1);

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
                } else if (g.won) {
                    sb.append(" W ");
                } else if (g.lost) {
                    sb.append(" L ");
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
 * Command to change the cursor style during gameplay.
 * Allows users to select a different cursor style from the available options.
 */
class CommandChangeCursorStyle extends Command {

    /**
     * Constructs a CommandChangeCursorStyle with predefined attributes.
     */
    CommandChangeCursorStyle() {
        super();
        category = CommandCategory.INGAME;
        longc = "cursor";
        shortc = "C";
        description = "Change cursor style (may not work on some devices)";
        fullName = "Change Cursor Style";
    }

    /**
     * Executes the command to allow the user to change the cursor style.
     * Provides an interactive menu for selecting the style.
     *
     * @param args the arguments passed with the command, optionally containing the style index.
     * @throws InvalidInputException if the input is invalid or an error occurs.
     */
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

            System.out.println("\rSelect a cursor style to apply");
            System.out.println("\rUse Arrow keys to navigate selection, enter to confirm, and C or ESC to cancel");
            System.out.println("\r");

            CursorStyles prev_style = AVandiniEliaBzGuessGame.cursorStyle;

            ArrayList<String> list = new ArrayList<>();
            list.add("\r ● Blink Block ");
            list.add("\r ● Blinking Block ");
            list.add("\r ● Steady Block ");
            list.add("\r ● Blinking Underline ");
            list.add("\r ● Steady Underline ");
            list.add("\r ● Blinking Bar ");
            list.add("\r ● Steady Bar ");

            while (loop.get() && !cancel.get()) {
                for (int i = 0; i < list.size(); i++) {
                    if (seleciton.get() == i) {
                        AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                    }
                    System.out.print(list.get(i));
                    AVandiniEliaBzGuessGame.resetAttrributes();
                    if (seleciton.get() == i) {
                        System.out.print("  ");
                        AVandiniEliaBzGuessGame.saveCursorPosition();
                        AVandiniEliaBzGuessGame.setCursorStyle(Objects.requireNonNull(CursorStyles.getCursorStyles(i)));
                    }
                    System.out.println();
                }
                AVandiniEliaBzGuessGame.restoreCursorPosition();


//                long start = System.currentTimeMillis();
                int prev_seleciton = seleciton.get();
                while (!AVandiniEliaBzGuessGame.KeyHandling()) {
                    AVandiniEliaBzGuessGame.wait(10);
                }
                if (seleciton.get() < 0) {
                    seleciton.set(6);
                }
                if (seleciton.get() > 6) {
                    seleciton.set(0);
                }

                AVandiniEliaBzGuessGame.eraseLinesUp(prev_seleciton);
            }
            AVandiniEliaBzGuessGame.eraseLinesUp(3);
            AVandiniEliaBzGuessGame.eraseLinesToEndOfScreen();

            if (cancel.get()) {
                AVandiniEliaBzGuessGame.setCursorStyle(prev_style);
            } else {
                AVandiniEliaBzGuessGame.cursorStyle = CursorStyles.getCursorStyles(seleciton.get());
            }
        }
    }
}

/**
 * Command to enable AI to play the game for the user.
 * Automates gameplay by simulating guesses based on a minimax strategy.
 */
class CommandAI extends Command {

    /**
     * Constructs a CommandAI with predefined attributes.
     */
    CommandAI() {
        super();
        category = CommandCategory.STORE;
        longc = "ai";
        shortc = "a";
        description = "Plays the game for you";
        fullName = "AI";
    }

    /**
     * Executes the AI command, automating gameplay for the user.
     * Simulates guesses until the game is won or lost.
     *
     * @param args the arguments passed with the command (not used for this command).
     */
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

/**
 * Command to count the remaining possible solutions based on feedback from guesses.
 * Costs 2 attempts and updates the game history with the count.
 */
class CommandRemains extends Command {

    /**
     * Constructs a CommandRemains with predefined attributes.
     */
    CommandRemains() {
        super();
        category = CommandCategory.STORE;
        longc = "remains";
        shortc = "R";
        description = "Counts available solutions based on the guess's feedback. Costs 2 attempts";
        fullName = "Remains";
    }

    /**
     * Executes the command to count possible solutions based on feedback from previous guesses.
     *
     * @param args the arguments passed with the command (not used for this command).
     * @throws InvalidInputException if an error occurs during solution counting.
     */
    void exec(String[] args) throws InvalidInputException {
        Game g = AVandiniEliaBzGuessGame.current_game;
        int solution_count = g.solver.reduceCodes(g.matches, g.guesses);
        g.attempts_left -= 2;
        g.history += "Game counted " + solution_count + " possible solutions still available based on guess feedback. 2 attempts were used up\n";
        String res = "\r" + "Based on past guesses' feedback, there are " + solution_count + " viable solutions.";
        AVandiniEliaBzGuessGame.fancyprint(res);
    }
}

/**
 * Command to grant the player (almost) unlimited attempts.
 * This is categorized as a secret command and is primarily for debugging or cheating.
 */
class CommandUnlimitedAttempts extends Command {

    /**
     * Constructs a CommandUnlimitedAttempts with predefined attributes.
     */
    CommandUnlimitedAttempts() {
        super();
        category = CommandCategory.SECRET;
        longc = "unlimited";
        shortc = "u";
        description = "Gives you (almost) unlimited attempts";
        fullName = "Unlimited Attempts";
    }

    /**
     * Executes the command to grant the player an extremely high number of attempts.
     *
     * @param args the arguments passed with the command (not used for this command).
     * @throws InvalidInputException if an error occurs while processing the command.
     */
    void exec(String[] args) throws InvalidInputException {
        Game g = AVandiniEliaBzGuessGame.current_game;
        g.attempts_left = Long.MAX_VALUE;
    }
}

/**
 * Represents a single game instance.
 * Manages the gameplay loop, the secret code, attempts, score, and game history.
 */
class Game implements Serializable {
    char[] code = new char[4]; // The secret code for the game.
    long attempts_left = 20L; // The number of attempts the player has left.
    String history = ""; // The history of guesses and feedback.
    boolean won = false; // Indicates if the game is won.
    boolean lost = false; // Indicates if the game is lost.
    Date start_date = new Date(); // The start date of the game.
    long score = 0; // The player's score.
    Solver solver = new Solver(); // The solver instance for generating guesses and solutions.
    ArrayList<Point> matches = new ArrayList<>(); // The list of feedback points for guesses.
    ArrayList<char[]> guesses = new ArrayList<>(); // The list of guesses made by the player.
    boolean ai = false; // Indicates if the AI is playing.
    char[] discovered_chars = {'_', '_', '_', '_'}; // Partially discovered characters in the code.

    TextBox textBox = new TextBox(); // The text box for player input.

    /**
     * Constructs a new Game instance with a randomly generated secret code.
     */
    public Game() {
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            code[i] = AVandiniEliaBzGuessGame.options[r.nextInt(AVandiniEliaBzGuessGame.options.length)];
        }
    }

    /**
     * Starts the game by initializing history and entering the game loop.
     */
    void startGame() {
        this.history = "Game started at " + start_date + '\n';
        this.gameloop();
    }

    /**
     * Executes the main game loop.
     * Continuously processes player turns until the game is won or lost.
     * Saves the game state after each turn.
     */
    void gameloop() {
        while (!lost && !won) {

            execeTurn();
            AVandiniEliaBzGuessGame.saveGameState();
            if (AVandiniEliaBzGuessGame.current_game != this) {
                return; // Exit loop if the current game has changed.
            }
        }
        finishGame();
    }

    /**
     * Executes a single turn in the game.
     * Parses player input and checks if the game has been lost due to running out of attempts.
     */
    void execeTurn() {
        parseInput();
        if (attempts_left <= 0 && !won && !ai) {
            lost = true;
        }
    }

    /**
     * Prompts the player for input and returns the entered string.
     *
     * @return the player's input.
     */
    String askInput() {
        Scanner sc = new Scanner(System.in);
        AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
        System.out.print(attempts_left + ">");
        AVandiniEliaBzGuessGame.resetAttrributes();
        return sc.nextLine();
    }

    /**
     * Parses a command entered by the player and executes it.
     *
     * @param input the command to parse.
     * @param args  the arguments for the command.
     * @throws InvalidInputException if the command is invalid or unknown.
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
     * Parses player input and determines whether it's a command or a guess.
     * Handles validation and feedback for guesses.
     */
    void parseInput() {
        String input = "";
        try {
            input = textBox.get_input(this);
            if (lost || won || ai || AVandiniEliaBzGuessGame.current_game != this) {
                return;
            }
            if (input.isEmpty()) {
                throw new InvalidInputException("Please input a command or a 4-character sequence");
            }
            if (input.charAt(0) == '.') {
                String temp = input.substring(1);
                String[] args = temp.split(" ");
                parseCommand(args[0], Arrays.copyOfRange(args, 1, args.length));
                return;
            }
            if (input.length() != 4) {
                throw new InvalidInputException("Input must be 4 characters long");
            }
            if (input.matches(".*[^ABCDEFabcdef].*")) {
                throw new InvalidInputException("Input must consist of A, B, C, D, E, or F");
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
            handleInputError(input, e);
        }
    }

    /**
     * Handles errors during input parsing, providing feedback to the player.
     *
     * @param input the invalid input provided by the player.
     * @param e     the exception containing the error message.
     */
    private void handleInputError(String input, InvalidInputException e) {
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

    /**
     * Parses a player's guess and provides feedback in the form of X (correct position)
     * and - (correct character, wrong position). Updates the game's history.
     *
     * @param input the player's guess.
     * @return a string representing the feedback.
     */
    String parseGuess(String input) {
        Point p = checkGuess(code, input.toCharArray());
        StringBuilder result = new StringBuilder();
        result.append("X".repeat(p.x));
        result.append("-".repeat(p.y));
        matches.add(p);
        guesses.add(input.toCharArray());
        if (p.x >= 4) {
            won = true;
        }
        AVandiniEliaBzGuessGame.current_game.history += attempts_left + "> " + input + " " + result + '\n';
        return result.toString();
    }

    /**
     * Compares a guess against the secret code and determines feedback.
     * Feedback is provided in terms of the number of exact matches (X) and partial matches (-).
     *
     * @param code  the secret code.
     * @param guess the player's guess.
     * @return a Point object where `x` represents exact matches and `y` represents partial matches.
     */
    static Point checkGuess(char[] code, char[] guess) {
        Point p = new Point(0, 0);
        boolean[] cleared = {false, false, false, false};
        for (int j = 0; j < 4; j++) {
            if (code[j] == Character.toUpperCase(guess[j])) {
                cleared[j] = true;
                p.x++;
            }
        }
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                if (cleared[k] || cleared[j]) {
                    continue;
                }
                if (code[k] == Character.toUpperCase(guess[j])) {
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
    static long score_calc(long time, long attempts_left) {

        // prevents absurd scores when using cheat commands
        if (attempts_left > 19
        ) {
            attempts_left = 19;
        }
        double secs_per_attempt = (double) time / (20 - attempts_left) / 1000;
        if (secs_per_attempt > 60) {
            secs_per_attempt = 60;
        }
        double score = ((pow(secs_per_attempt, 2) / 180) - (secs_per_attempt * (13f / 20f)) + 20); // x^2/180 - x*13/20 + 20
        return (long) (score * 10) * (attempts_left * 10);
    }

    /**
     * Finalizes the game by saving the game state and displaying results.
     * Prompts the player with a win or lose screen based on the game outcome.
     */
    void finishGame() {
        if (won) {
            long diff = abs(new Date().getTime() - this.start_date.getTime());
            score = score_calc(diff, attempts_left);
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

/**
 * Represents a solver for the game, implementing logic to minimize
 * the number of guesses needed to identify a secret code.
 */
class Solver implements Serializable {

    /**
     * List of all possible codes that can be generated in the game.
     */
    ArrayList<char[]> possibleCodes;

    /**
     * Constructor for the Solver class. Initializes the possible codes.
     */
    Solver() {
        possibleCodes = getPopulateCodes();
    }

    /**
     * Generates all possible unique codes using the predefined game options.
     *
     * @return An ArrayList containing all possible combinations of game codes.
     */
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

    /**
     * Simulates the game using a given code and guesses, returning the results
     * for each guess.
     *
     * @param code    The secret code to be guessed.
     * @param guesses A list of guesses to be evaluated.
     * @return An ArrayList of results (as Points) for each guess.
     */
    static ArrayList<Point> mockGame(char[] code, ArrayList<char[]> guesses) {
        ArrayList<Point> res = new ArrayList<Point>();
        for (int i = 0; i < guesses.size(); i++) {
            res.add(Game.checkGuess(code, guesses.get(i)));
        }
        return res;
    }

    /**
     * Compares match results between two games to check for consistency.
     *
     * @param code    The secret code to be checked.
     * @param p1      The first list of match results to compare.
     * @param guesses The list of guesses used in the comparison.
     * @return True if the matches are not equal, false otherwise.
     */
    static boolean equalMatches(char[] code, ArrayList<Point> p1, ArrayList<char[]> guesses) {
        ArrayList<Point> p2 = mockGame(code, guesses);
        boolean res = p1.equals(p2);
        return !res;
    }

    /**
     * Reduces the set of possible codes based on match results and guesses.
     *
     * @param matches The list of match results.
     * @param guesses The list of guesses corresponding to the matches.
     * @return The size of the remaining possible codes.
     * @throws InvalidInputException If the size of matches and guesses differ.
     */
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

    /**
     * Determines the best guess using the minimax strategy.
     * This code is based on <a href="https://en.wikipedia.org/wiki/Mastermind_(board_game)#Worst_case:_Five-guess_algorithm">Donald Knuth's algorithm for codebreaking mastermind</a>
     *
     * @param matches The list of match results.
     * @param guesses The list of previous guesses.
     * @return The best guess as a character array.
     * @throws InvalidInputException If the size of matches and guesses differ.
     */
    char[] minimaxBestGuess(ArrayList<Point> matches, ArrayList<char[]> guesses) throws InvalidInputException {
//        System.out.println("\r" + possibleCodes.size());
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
//        System.out.println("\r" + possibleCodes.size());
        return lowest_worst_score_code;
    }
}

/**
 * Represents a collection of key codes and their variations for different key modifiers (e.g., Shift, Ctrl, Alt).
 * More specifically these byte arrays are read from the input stream when a key is pressed with raw terminal mode enabled
 * This listis not exhaustive and only includes keybinds i am interested in implementing in my program
 * many keycodes are for the same reason equale regardless of the modifyer
 * finally ctrl+shift keybinds are not listed because of technical limitations
 */
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
    SUBTRACT(new byte[]{45}, new byte[]{45}, new byte[]{45}, new byte[]{45}, new byte[]{45}),
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

    /**
     * Byte representation of the key when the Shift modifier is applied.
     */
    private final byte[] shiftCode;

    /**
     * Byte representation of the key when the Ctrl modifier is applied.
     */
    private final byte[] ctrlCode;

    /**
     * Byte representation of the key when the Alt modifier is applied.
     */
    private final byte[] altCode;

    /**
     * Byte representation of the key when both Alt and Shift modifiers are applied.
     */
    private final byte[] altShiftCode;

    /**
     * Constructor for the KeyCodes enum.
     *
     * @param code         Byte array representing the key without modifiers.
     * @param shiftCode    Byte array representing the key with the Shift modifier.
     * @param ctrlCode     Byte array representing the key with the Ctrl modifier.
     * @param altCode      Byte array representing the key with the Alt modifier.
     * @param altShiftCode Byte array representing the key with both Alt and Shift modifiers.
     */
    KeyCodes(byte[] code, byte[] shiftCode, byte[] ctrlCode, byte[] altCode, byte[] altShiftCode) {
        this.code = code;
        this.shiftCode = shiftCode;
        this.ctrlCode = ctrlCode;
        this.altCode = altCode;
        this.altShiftCode = altShiftCode;
    }

    /**
     * Retrieves the byte array representation of the key without modifiers.
     *
     * @return A byte array representing the key.
     */
    public byte[] getCode() {
        return code;
    }

    /**
     * Retrieves the byte array representation of the key with the Shift modifier.
     *
     * @return A byte array representing the key with the Shift modifier.
     */
    public byte[] getShiftCode() {
        return shiftCode;
    }

    /**
     * Retrieves the byte array representation of the key with the Ctrl modifier.
     *
     * @return A byte array representing the key with the Ctrl modifier.
     */
    public byte[] getCtrlCode() {
        return ctrlCode;
    }

    /**
     * Retrieves the byte array representation of the key with the Alt modifier.
     *
     * @return A byte array representing the key with the Alt modifier.
     */
    public byte[] getAltCode() {
        return altCode;
    }

    /**
     * Retrieves the byte array representation of the key with both Alt and Shift modifiers.
     *
     * @return A byte array representing the key with both Alt and Shift modifiers.
     */
    public byte[] getAltShiftCode() {
        return altShiftCode;
    }
}

/*
 * Represents different keymodifyers.
 */
enum KeyModifier {
    NONE,
    SHIFT,
    CTRL,
    ALT,
    ALT_SHIFT;
}

/**
 * A thread responsible for handling low-level key listening and processing.
 * It captures raw key inputs and invokes associated key bindings.
 */
class KeyListenenThread extends Thread {

    /**
     * Indicates whether the thread is actively running.
     */
    static boolean running = false;

    /**
     * Determines if global key handling is enabled.
     */
    static boolean globalKeyHandling = true;

    /**
     * A queue of currently active keys captured by the thread.
     * They will remain in the queue until properly handled and removed
     */
    static Queue<byte[]> active_keys = new LinkedList<>();

    /**
     * A mapping of keycodes sequences to their corresponding key handling actions.
     */
    static Map<byte[], Consumer<byte[]>> keymap = new Hashtable<>();

    /**
     * The main execution loop of the thread, responsible for listening to key inputs
     * and invoking appropriate handlers.
     */
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

    /**
     * Enables raw input mode in the terminal, disabling local echo and input buffering.
     *
     * @throws IOException If an error occurs while executing the system command.
     */
    static void enableRawMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty raw -echo < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    /**
     * Enables raw input mode while preserving local echo in the terminal.
     *
     * @throws IOException If an error occurs while executing the system command.
     */
    static void enableRawEchoMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty raw < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    /**
     * Disables raw input mode in the terminal, restoring normal input handling.
     *
     * @throws IOException If an error occurs while executing the system command.
     */
    static void disableRawMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty cooked echo < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    /**
     * Handles globally defined key bindings and invokes the corresponding actions.
     */
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

    /**
     * Determines whether a character is printable.
     *
     * @param d The character to evaluate.
     * @return True if the character is printable, false otherwise.
     */
    static boolean printable(char d) {
        return (d > 31 && d < 128) || (d > 160);
    }
}

/**
 * Represents a text box with features for text manipulation, cursor movement,
 * and command mode operations. Handles user input and history cycling.
 */
class TextBox implements Serializable {

    /**
     * The main text content of the text box.
     */
    StringBuilder text = new StringBuilder();

    /**
     * The current cursor position in the text.
     */
    int cursor_pos = 0;

    /**
     * Represents the selection range in the text.
     * `x` and `y` indicate the start and end positions of the selection.
     */
    Point selection_pos = new Point(0, 0);

    /**
     * Indicates whether the text box is in command mode.
     */
    boolean command_mode = true;

    /**
     * A history of previously entered commands.
     */
    static ArrayList<String> command_history = new ArrayList<>();

    /**
     * The index of the current command in the history.
     * Is used to cycle previously entered guesses without having to open the game history
     */
    static int history_index = 0;

    /**
     * The result of the current input operation.
     */
    String result;

    /**
     * Configures the default key mappings for basic text input.
     */
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
        KeyListenenThread.keymap.put(KeyCodes.TAB.getCode(), n -> insert(get_command_suggestion()));

    }

    /**
     * Configures key mappings specific to "guess mode."
     */
    void set_guess_mode_keymap() {
        set_base_keymap();
    }

    /**
     * Configures key mappings specific to "command mode."
     */
    void set_comand_mode_keymap() {
        set_base_keymap();
        KeyListenenThread.keymap.put(KeyCodes.SUBTRACT.getCode(), n -> insert("-"));
        KeyListenenThread.keymap.put(KeyCodes.SPACE.getCode(), n -> insert(" "));
    }

    /**
     * Submits the current input text and resets the text box.
     */
    void submit() {
        fancy_ui_printer(AVandiniEliaBzGuessGame.current_game.attempts_left, true);
        result = text.toString();
        command_history.set(0, text.toString());
        history_index = 0;
        text = new StringBuilder();
        cursor_pos = 0;
        command_history.addFirst("");
        System.out.println();
    }

    /**
     * Cycles to the previous command in the history.
     */
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

    /**
     * Cycles to the next command in the history.
     */
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

    /**
     * Jumps to the oldest command in the history.
     */
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

    /**
     * Jumps to the newest command in the history.
     */
    void cycle_history_bottom() {
        if (command_history.isEmpty()) {
            return;
        }
        history_index = 0;
        text = new StringBuilder(command_history.get(history_index));
        cursor_pos = text.length();
    }

    /**
     * Finds the position of the end of the next word to the right.
     *
     * @param input     The input text.
     * @param start_pos The starting position for the search.
     * @return The position of the end of the next word.
     */
    static int findWordEndToRight(String input, int start_pos) {
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

    /**
     * Finds the position of the start of the previous word to the left.
     *
     * @param input     The input text.
     * @param start_pos The starting position for the search.
     * @return The position of the start of the previous word.
     */
    static int findWordStartToLeft(String input, int start_pos) {
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

    /**
     * Moves the cursor within the text, optionally updating the selection range.
     *
     * @param modifier The key modifier affecting cursor movement.
     * @param pos      The direction and amount of movement (-1 for left, +1 for right).
     */
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

    /**
     * Deletes text from the text box based on the cursor and selection positions.
     *
     * @param modifier  The key modifier affecting the deletion behavior.
     * @param backspace True if backspace behavior is applied, false for forward delete.
     */
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

    /**
     * Inserts a string at the current cursor position.
     *
     * @param s The string to be inserted.
     */
    void insert(String s) {
        text.insert(cursor_pos, s);
        cursor_pos += s.length();
        validate_cursor_pos();
        validate_selection_pos();
    }

    /**
     * Ensures the cursor position is within valid bounds.
     */
    void validate_cursor_pos() {
        if (cursor_pos > text.length()) {
            cursor_pos = text.length();
        }
        if (cursor_pos < 0) {
            cursor_pos = 0;
        }
    }

    /**
     * Ensures the selection position is within valid bounds.
     */
    void validate_selection_pos() {
        if (selection_pos.x > text.length()) {
            selection_pos.x = text.length();
        }
        if (selection_pos.x < 0) {
            selection_pos.x = 0;
        }
    }

    /**
     * Safely retrieves a character from a StringBuilder, with a fallback value.
     *
     * @param sb       The StringBuilder to retrieve the character from.
     * @param index    The index of the character.
     * @param fallback The fallback character if the index is out of bounds.
     * @return The character at the given index, or the fallback value.
     */
    static char getCharSafely(StringBuilder sb, int index, char fallback) {
        if (index >= 0 && index < sb.length()) {
            return sb.charAt(index);
        } else {
            return fallback;
        }
    }

    /**
     * Provides a command suggestion based on the current text input.
     * the suggestion is then printed next to the user input in gray
     *
     * @return A string representing the suggested command completion or an empty string
     * if no suggestion is applicable.
     * - Returns ".help" if the text is empty.
     * - Returns an empty string if the input does not start with a period (`.`) or if no matching command is found.
     */
    String get_command_suggestion() {
        if (text.isEmpty()) {
            return ".help";
        }
        if (text.charAt(0) != '.') {
            return "";
        }
        String t = text.substring(1, text.length());
        for (Command command : AVandiniEliaBzGuessGame.comands) {
            if (command.longc.length() > t.length() && t.equals(command.longc.substring(0, t.length()))) {
                return command.longc.substring(t.length());
            }
        }
        return "";
    }

    /**
     * Displays the game's fancy user interface for the input prompt. Wow very fancy and aestheticspilled!
     *
     * @param attempts_left      The number of attempts remaining for the player.
     * @param disable_suggestion A flag indicating whether command suggestions should be disabled.
     */
    void fancy_ui_printer(long attempts_left, boolean disable_suggestion) {

        AVandiniEliaBzGuessGame.eraseLine();
        AVandiniEliaBzGuessGame.resetAttrributes();
        AVandiniEliaBzGuessGame.setAttribute(new AbstarctAttributes[]{FColors.GREEN, TextAttributes.BRIGHT});
        System.out.print(attempts_left + "> ");
        AVandiniEliaBzGuessGame.resetAttrributes();
        String to_display = text.toString();
        if (!command_mode) {
            to_display = to_display.toUpperCase();
        }
        System.out.print(to_display);
        int box_offset = String.valueOf(attempts_left).length() + 3;
        String cmd_suggestion = get_command_suggestion();
        if (!disable_suggestion && !cmd_suggestion.isEmpty() && (command_mode || text.isEmpty())) {
            AVandiniEliaBzGuessGame.set8bitForeground(241);
            System.out.print(cmd_suggestion);
            AVandiniEliaBzGuessGame.resetAttrributes();
        }
        if (selection_pos.x != selection_pos.y) {
//                System.out.print(selection_pos.x + " != " + selection_pos.y);
            AVandiniEliaBzGuessGame.moveCursor(min(selection_pos.x, selection_pos.y) + box_offset, CursorMoveDirection.COLUMN);
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
            System.out.print(to_display.substring(min(selection_pos.x, selection_pos.y), max(selection_pos.x, selection_pos.y)));
            AVandiniEliaBzGuessGame.resetAttrributes();
            AVandiniEliaBzGuessGame.moveCursor(to_display.length() + box_offset, CursorMoveDirection.COLUMN);
        }
        AVandiniEliaBzGuessGame.resetAttrributes();
        System.out.print(" ");
//            System.out.print(command_history);

        AVandiniEliaBzGuessGame.moveCursor(box_offset + cursor_pos, CursorMoveDirection.COLUMN);
    }

    /**
     * Retrieves user input while interacting with a parent game instance.
     * Basically the whole point of this big ahh class
     *
     * @param parent The parent game instance interacting with the text box.
     * @return The user input as a string.
     */
    String get_input(Game parent) {
        result = "";

        set_guess_mode_keymap();

        long last_tick = System.currentTimeMillis();

        if (command_history.isEmpty()) {
            command_history = new ArrayList<>();
            command_history.addFirst(text.toString());
        }

        while (!parent.lost && !parent.won && !parent.ai && AVandiniEliaBzGuessGame.current_game == parent) {
            if (System.currentTimeMillis() < last_tick + 2) {
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
            fancy_ui_printer(parent.attempts_left, false);
        }
        return "";
    }
}