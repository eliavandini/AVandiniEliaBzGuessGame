import java.util.ArrayList;
import java.util.Date;

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
    UNDERSCORE(3),
    BLINK(4),
    REVERSE(5),
    HIDDEN(6);

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

class InputEmptyException extends Exception
{
      public InputEmptyException() {}

      public InputEmptyException(String message)
      {
         super(message);
      }
 }




public class AVandiniEliaBzGuessGame {

    static int highscore = 0;
    static Command[] comands = new Command[] {};
    static ArrayList<Game> games = new ArrayList<Game>();



    public static void main(String[] args) {
        greeting();
        newGame();


    }

    static void greeting(){
        System.out.println("Programmed by Vandini Elia");
    }

    static void rules(){
        System.out.println("rules");
    }

    static void winScreen(){
        System.out.println("Congratulations");
    }

    static void loosescreen(){
        System.out.println("Try again");
    }

    static Game getLastGame(){
        return games.getLast();
    }

    static Game newGame(){
        games.add(new Game());
        return getLastGame();
    }

    static void eraseLine(){

    }

    static void eraseLinesUp(int lines){

    }

    static void setAttribute(AbstarctAttributes attr){
        System.out.printf("\033[%dm", attr.getIndex());
    }

    static void setAttribute(AbstarctAttributes[] attr){
        for(AbstarctAttributes a : attr){
            setAttribute(a);
        }
    }

    static void resetAttrributes(){
        setAttribute(TextAttributes.RESET);
    }

    static void clearScreen(){
        System.out.print("\033[H\033[2J");
    }

    static void hideCursor() {
        System.out.println("\033[?25l");
    }

    static void showCursor() {
        System.out.println("\033[?25h");
    }
}

class Command{
    String fullName;
    String command;
    String shortComand;
    CommandCategory category;
    String description;
    AVandiniEliaBzGuessGame parent;

    public Command(String fullName, String command, String shortComand, CommandCategory category, String description) {
        this.fullName = fullName;
        this.command = command;
        this.shortComand = shortComand;
        this.category = category;
        this.description = description;
    }

    static void exec(String args){
        return;
    }

}

class Game{

    char[] code = new char[4];
    int attempts_left = 20;

    String history = "";

    boolean won = false;
    boolean lost = false;

    public Game() {
        this.history = "Game started at " + new Date();
        while (!lost || !won){
            execeTurn();
        }
    }

    void execeTurn(){
        parseInput();
        if (attempts_left <= 0){
            lost = true;
        }
        if (won || lost){
            finishGame();
        }
    }

    String askInput(){
        System.out.println(attempts_left + ">");
        return "";
    }

    void parseInput(){
        try{
            String input = askInput();
            if (input.isEmpty()){
                throw new InputEmptyException("please input a command or 4 character sequence");
            }
        } catch (Exception e){

        }
    }

    void finishGame(){
        // change history to include secrect code
        // save highscore
    }



}