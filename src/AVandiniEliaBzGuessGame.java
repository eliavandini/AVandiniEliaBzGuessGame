import java.util.*;

enum CommandCategory {
    BASIC,
    INGAME,
    SECRET
}

interface AbstarctAttributes {
    int getIndex();
}

enum TextAttributes implements AbstarctAttributes {
    RESET(0),
    BRIGHT(1),
    DIM(2),
    ITALIC(3),
    UNDERLINE(4),
    BLINKING(5),
    INVERSE(7),
    HIDDEN(8),
    STRIKETHROUGH(9),

    RESET_BRIGHT(22),
    RESET_DIM(22),
    RESET_ITALIC(23),
    RESET_UNDERLINE(24),
    RESET_BLINKING(25),
    RESET_INVERSE(27),
    RESET_HIDDEN(28),
    RESET_STRIKETHROUGH(29);


    private final int index;

    TextAttributes(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}


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

class InvalidInputException extends Exception {
    public InvalidInputException() {
    }

    public InvalidInputException(String message) {
        super(message);
    }
}

class Command {
    String fullName;
    String longc;
    String shortc;
    CommandCategory category;
    String description;

    void exec(String[] args) {

    }

    Command() {
    }

}

class CommandHelp extends Command {

    CommandHelp() {
        super();
        category = CommandCategory.BASIC;
        longc = "help";
        shortc = "h";
        description = "Shows this Help message";
        fullName = "Help";
    }

    void exec(String[] args) {
//                              h | help
        String space_storage = "                 ";

        for (Command command : AVandiniEliaBzGuessGame.comands) {
            String help_string = command.shortc + " | " + command.longc;
            help_string = help_string + space_storage.substring(help_string.length()) + command.description;
            System.out.println(help_string);
        }
    }
}

class CommandP extends Command {

    CommandP() {
        super();
        category = CommandCategory.BASIC;
        longc = "p";
        shortc = "p";
        description = "reveals the code";
        fullName = "p";
    }

    void exec(String[] args) {
        System.out.println(AVandiniEliaBzGuessGame.current_game.code);
    }
}

class Game {
    char[] options = {'A', 'B', 'C', 'D', 'E', 'F'};
    char[] code = new char[4];

    int attempts_left = 20;

    String history = "";

    boolean won = false;
    boolean lost = false;

    public Game() {
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            code[i] = options[r.nextInt(options.length)];
        }
    }

    void startGame() {
        this.history = "Game started at " + new Date();
        while (!lost && !won) {
            execeTurn();
        }
        finishGame();
        if (won) {
            AVandiniEliaBzGuessGame.winScreen();
        } else if (lost) {
            AVandiniEliaBzGuessGame.loosescreen();
        }
    }

    void execeTurn() {
        parseInput();
        if (attempts_left <= 0) {
            lost = true;
        }
    }

    String askInput() {
        Scanner sc = new Scanner(System.in);
        System.out.print(attempts_left + ">");
        String inp = sc.nextLine();
        return inp;
    }

    void parseCommand(String input, String[] args) throws InvalidInputException {
        for (Command command : AVandiniEliaBzGuessGame.comands) {
            if (command.shortc.equals(input) || command.longc.equals(input)) {
                command.exec(args);
                return;
            }
        }
        throw new InvalidInputException("Unkown comand");
    }

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
//            AVandiniEliaBzGuessGame.moveCursor(1, CursorMoveDirection.UP);
            System.out.println("\r " + parseGuess(input));
            attempts_left--;
            if (attempts_left <= 0) {
                lost = true;
            }
        } catch (InvalidInputException e) {
            AVandiniEliaBzGuessGame.eraseLinesUp(1);
            System.out.print(attempts_left + "> ");
            AVandiniEliaBzGuessGame.setAttribute(FColors.RED);
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
//            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BLINKING);
            System.out.print(e.getMessage());
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.RESET);
            AVandiniEliaBzGuessGame.wait(3000);
            AVandiniEliaBzGuessGame.eraseLine();
            parseInput();
        }
    }

    String parseGuess(String input) {
        int score = 0;
        ArrayList<Character> result = new ArrayList<Character>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (code[i] == Character.toUpperCase(input.charAt(j))) {
                    if (i == j) {
                        result.add('X');
                        score++;
                    } else {
                        result.add('-');
                    }
                }
            }
        }
        if (score >= 4) {
            won = true;
        }
        result.sort(Comparator.comparingInt(a -> a));
        StringBuilder sb = new StringBuilder();
        for (Character character : result) {
            sb.append(character);
        }

        return sb.toString();
    }

    void finishGame() {
        // change history to include secrect code
        // save highscore
    }

}

class AVandiniEliaBzGuessGame {
    static int highscore = 0;
    static Command[] comands = new Command[]{new CommandHelp(), new CommandP()};
    static ArrayList<Game> games = new ArrayList<Game>();
    static Game current_game;

    public static void main(String[] args) {
        greeting();
        newGame();
    }

    static void greeting() {
        System.out.println("Programmed by Vandini Elia");
    }

    static void rules() {
        System.out.println("rules");
    }

    static void winScreen() {
        System.out.println("Congratulations");
        askIfPlayAgain();
    }

    static void loosescreen() {
        System.out.println("You lost!");
        askIfPlayAgain();
    }

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
            System.out.print("Want to try again? (Y/N)");
            AVandiniEliaBzGuessGame.setAttribute(FColors.RED);
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BRIGHT);
//            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.BLINKING);
            System.out.print(e.getMessage());
            AVandiniEliaBzGuessGame.setAttribute(TextAttributes.RESET);
            AVandiniEliaBzGuessGame.wait(3000);
            AVandiniEliaBzGuessGame.eraseLine();
            askIfPlayAgain();
        }

    }

    static Game newGame() {
        Game g = new Game();
        games.add(g);
        current_game = g;
        g.startGame();
        return g;
    }

    static void eraseLine() {
        System.out.print("\u001b[2K\r");
    }

    static void eraseLinesUp(int lines) {
        moveCursor(lines, CursorMoveDirection.UPX);
        System.out.print("\u001b[0J");
    }

    static void setAttribute(AbstarctAttributes attr) {
        System.out.printf("\u001b[%dm", attr.getIndex());
    }

    static void setAttribute(AbstarctAttributes[] attr) {
        for (AbstarctAttributes a : attr) {
            setAttribute(a);
        }
    }

    static void resetAttrributes() {
        setAttribute(TextAttributes.RESET);
    }

    static void clearScreen() {
        System.out.print("\u001b[H\u001b[2J");
    }

    static void hideCursor() {
        System.out.print("\u001b[?25l");
    }

    static void showCursor() {
        System.out.print("\u001b[?25h");
    }

    static void moveCursor(int pos, CursorMoveDirection direction) {
        System.out.print("\u001b[" + pos + direction.getLabel());
    }

    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}