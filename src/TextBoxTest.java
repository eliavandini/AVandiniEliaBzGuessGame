import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;


class KeyListenenThread extends Thread {

    static boolean printable(char d) {
        if ((d > 31 && d < 128) || (d > 160)) {
            return true;
        }
        return false;
    }

    public void run() {

        try {
            // Enable raw mode for terminal
            enableRawMode();
            System.out.println("Press keys (Press 'q' to quit):");
            while (true) {
                int key = System.in.read(); // Read a single character
                if (key == '\033' && System.in.read() == '[') {
                    System.out.print('\r');
                    switch (System.in.read()) {
                        case 'A':
                            System.out.println("Up arrow pressed");
                            break;
                        case 'B':
                            System.out.println("Down arrow pressed");
                            break;
                        case 'C':
                            System.out.println("Right arrow pressed");
                            break;
                        case 'D':
                            System.out.println("Left arrow pressed");
                            break;
                    }
                } else {
                    System.out.print('\r');
                    System.out.print("ASCII: " + key);
                    if (printable((char) key)) {
                        System.out.print(", Key: " + (char) key);
                    }
                    System.out.println();
                    if (key == 'q') { // Quit on 'q'
                        break;
                    }
                }
            }
            // Restore terminal to cooked mode
            disableRawMode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enableRawMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty raw -echo < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }

    private static void disableRawMode() throws IOException {
        String[] command = {"/bin/sh", "-c", "stty cooked echo < /dev/tty"};
        Runtime.getRuntime().exec(command);
    }
}

class RichChar {
    char charValue;
    FColors fColor;
    BColors bColor;
    HashSet<TextAttributes> textAttributes;

    public RichChar(char charValue, FColors fColor, BColors bColor, HashSet<TextAttributes> textAttributes) {
        this.charValue = charValue;
        this.fColor = fColor;
        this.bColor = bColor;
        this.textAttributes = textAttributes;
    }

    public RichChar() {
        this.charValue = ' ';
        this.fColor = FColors.WHITE;
        this.bColor = BColors.BLACK;
        this.textAttributes = new HashSet<TextAttributes>();
    }

    HashSet<TextAttributes> get_to_reset_attr(RichChar nextChar) {
        HashSet<TextAttributes> result = new HashSet<TextAttributes>(textAttributes);
        result.removeAll(nextChar.textAttributes);
        return result;
    }

    HashSet<TextAttributes> get_to_set_attr(RichChar nextChar) {
        HashSet<TextAttributes> result = new HashSet<TextAttributes>(nextChar.textAttributes);
        result.removeAll(textAttributes);
        return result;

    }

//    get_setted_attr()

    boolean has_same_attibutes(RichChar c) {
        return textAttributes.equals(c.textAttributes);
    }
}

class Screen {
    int w;
    int h;
    int termWidth;
    int termHeight;
    RichChar[] chars;

    public Screen(int h, int w) {
        this.h = h;
        this.w = w;
        chars = new RichChar[w * h];
    }

    void updateTerminalsize() {

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty size"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                String[] dimensions = line.trim().split("\\s+");

                termHeight = Integer.parseInt(dimensions[0]);
                termWidth = Integer.parseInt(dimensions[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void render() {
        RichChar lastChar = new RichChar();

        if (termHeight < this.h || termWidth < this.w) {
            return;
        }

        int startX = (termWidth - w) / 2;
        int startY = (termWidth - h) / 2;


        StringBuilder result = new StringBuilder();
        result.append("\033[s\u001B[2J\u001B[").append(startX).append(';').append(startY).append(startY).append('H');


        for (int i = 0; i < chars.length; i++) {

            result.append("\u001b[0");
            for (TextAttributes attr : chars[i].textAttributes) {
                result.append(";").append(attr);
            }
            result.append('m').append(chars[i].charValue);
            lastChar = chars[i];
            if (i % w == 0) {
                result.append("\n").append("\033[").append(startX).append("G");
            }
        }
        result.append("\033[u");

    }

    class Surface {
        int x;
        int y;
        int w;
        int h;
        RichChar[] chars;

        public Surface(int h, int w, int y, int x) {
            this.h = h;
            this.w = w;
            this.y = y;
            this.x = x;
            chars = new RichChar[w * h];
        }

        public Surface(int h, int w, int y, int x, String string) {
            this.h = h;
            this.w = w;
            this.y = y;
            this.x = x;
            chars = new RichChar[w * h];

//            string.
        }
    }

    public class TextBoxTest {

        Surface surface = new Surface(h, w, 0, 0);

        public static void main(String[] args) {
//        Thread kyThread = new KeyListenenThread();
//        kyThread.start();
//        while(kyThread.isAlive()){
//            System.out.println("Press keys (Press 'q' to quit):");
        }
    }
}
