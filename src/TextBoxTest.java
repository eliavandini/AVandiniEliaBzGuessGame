import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
    A(new byte[]{97}, new byte[]{65}, new byte[]{0}, new byte[]{27, 97}, new byte[]{27, 65}),
    B(new byte[]{98}, new byte[]{66}, new byte[]{1}, new byte[]{27, 98}, new byte[]{27, 65}),
    C(new byte[]{99}, new byte[]{67}, new byte[]{2}, new byte[]{27, 99}, new byte[]{27, 66}),
    D(new byte[]{100}, new byte[]{68}, new byte[]{3}, new byte[]{27, 100}, new byte[]{27, 67}),
    E(new byte[]{101}, new byte[]{69}, new byte[]{4}, new byte[]{27, 101}, new byte[]{27, 68}),
    F(new byte[]{102}, new byte[]{70}, new byte[]{5}, new byte[]{27, 102}, new byte[]{27, 69}),
    G(new byte[]{103}, new byte[]{71}, new byte[]{6}, new byte[]{27, 103}, new byte[]{27, 70}),
    H(new byte[]{104}, new byte[]{72}, new byte[]{7}, new byte[]{27, 104}, new byte[]{27, 71}),
    I(new byte[]{105}, new byte[]{73}, new byte[]{8}, new byte[]{27, 105}, new byte[]{27, 72}),
    J(new byte[]{106}, new byte[]{74}, new byte[]{9}, new byte[]{27, 106}, new byte[]{27, 73}),
    K(new byte[]{107}, new byte[]{75}, new byte[]{10}, new byte[]{27, 107}, new byte[]{27, 74}),
    L(new byte[]{108}, new byte[]{76}, new byte[]{11}, new byte[]{27, 108}, new byte[]{27, 75}),
    M(new byte[]{109}, new byte[]{77}, new byte[]{12}, new byte[]{27, 109}, new byte[]{27, 76}),
    N(new byte[]{110}, new byte[]{78}, new byte[]{13}, new byte[]{27, 110}, new byte[]{27, 77}),
    O(new byte[]{111}, new byte[]{79}, new byte[]{14}, new byte[]{27, 111}, new byte[]{27, 78}),
    P(new byte[]{112}, new byte[]{80}, new byte[]{15}, new byte[]{27, 112}, new byte[]{27, 79}),
    Q(new byte[]{113}, new byte[]{81}, new byte[]{16}, new byte[]{27, 113}, new byte[]{27, 80}),
    R(new byte[]{114}, new byte[]{82}, new byte[]{17}, new byte[]{27, 114}, new byte[]{27, 81}),
    S(new byte[]{115}, new byte[]{83}, new byte[]{18}, new byte[]{27, 115}, new byte[]{27, 82}),
    T(new byte[]{116}, new byte[]{84}, new byte[]{19}, new byte[]{27, 116}, new byte[]{27, 83}),
    U(new byte[]{117}, new byte[]{85}, new byte[]{20}, new byte[]{27, 117}, new byte[]{27, 84}),
    V(new byte[]{118}, new byte[]{86}, new byte[]{21}, new byte[]{27, 118}, new byte[]{27, 85}),
    W(new byte[]{119}, new byte[]{87}, new byte[]{22}, new byte[]{27, 119}, new byte[]{27, 86}),
    X(new byte[]{120}, new byte[]{88}, new byte[]{23}, new byte[]{27, 120}, new byte[]{27, 87}),
    Y(new byte[]{121}, new byte[]{89}, new byte[]{24}, new byte[]{27, 121}, new byte[]{27, 88}),
    Z(new byte[]{122}, new byte[]{90}, new byte[]{25}, new byte[]{27, 122}, new byte[]{27, 89}),
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

    static Queue<byte[]> active_keys = new LinkedList<>();
    static Map<byte[], Consumer<byte[]>> keymap = new Hashtable<>();

    public void run() {

        try {
            if (running) {
                throw new RuntimeException("keythread already running");
            }
            running = true;
            // Enable raw mode for terminal
            enableRawMode();
            while (running) {
                byte[] b = new byte[8];
                int l = System.in.read(b);
//                System.out.println("\r" + Arrays.toString(b));
                byte[] buffer = new byte[l];

                System.arraycopy(b, 0, buffer, 0, l);
                active_keys.add(buffer);
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
            // Restore terminal to cooked mode
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

    static boolean printable(char d) {
        if ((d > 31 && d < 128) || (d > 160)) {
            return true;
        }
        return false;
    }
}

public class TextBoxTest implements Serializable {

    static StringBuilder text = new StringBuilder();
    static int cursor_pos = 0;
    static Point selection_pos = new Point(-1, -1);
    static boolean command_mode = true;
    static ArrayList<String> command_history = new ArrayList<>();
    static int history_index = 0;

    static void set_base_keymap() {
        KeyListenenThread.keymap.clear();
//        KeyListenenThread.keymap.put(KeyCodes.ESCAPE.getCode())
        KeyListenenThread.keymap.put(KeyCodes.ENTER.getCode(), n -> submit());
        KeyListenenThread.keymap.put(KeyCodes.DELETE.getCode(), n -> delete(false));
        KeyListenenThread.keymap.put(KeyCodes.BACKSPACE.getCode(), n -> delete(true));
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

    }

    static void set_guess_mode_keymap() {
        set_base_keymap();
        KeyListenenThread.keymap.put(KeyCodes.A.getCode(), n -> insert("A"));
        KeyListenenThread.keymap.put(KeyCodes.B.getCode(), n -> insert("B"));
        KeyListenenThread.keymap.put(KeyCodes.C.getCode(), n -> insert("C"));
        KeyListenenThread.keymap.put(KeyCodes.D.getCode(), n -> insert("D"));
        KeyListenenThread.keymap.put(KeyCodes.E.getCode(), n -> insert("E"));
        KeyListenenThread.keymap.put(KeyCodes.F.getCode(), n -> insert("F"));
        KeyListenenThread.keymap.put(KeyCodes.G.getCode(), n -> insert("G"));
        KeyListenenThread.keymap.put(KeyCodes.H.getCode(), n -> insert("H"));
        KeyListenenThread.keymap.put(KeyCodes.I.getCode(), n -> insert("I"));
        KeyListenenThread.keymap.put(KeyCodes.J.getCode(), n -> insert("J"));
        KeyListenenThread.keymap.put(KeyCodes.K.getCode(), n -> insert("K"));
        KeyListenenThread.keymap.put(KeyCodes.L.getCode(), n -> insert("L"));
        KeyListenenThread.keymap.put(KeyCodes.M.getCode(), n -> insert("M"));
        KeyListenenThread.keymap.put(KeyCodes.N.getCode(), n -> insert("N"));
        KeyListenenThread.keymap.put(KeyCodes.O.getCode(), n -> insert("O"));
        KeyListenenThread.keymap.put(KeyCodes.P.getCode(), n -> insert("P"));
        KeyListenenThread.keymap.put(KeyCodes.Q.getCode(), n -> insert("Q"));
        KeyListenenThread.keymap.put(KeyCodes.R.getCode(), n -> insert("R"));
        KeyListenenThread.keymap.put(KeyCodes.S.getCode(), n -> insert("S"));
        KeyListenenThread.keymap.put(KeyCodes.T.getCode(), n -> insert("T"));
        KeyListenenThread.keymap.put(KeyCodes.U.getCode(), n -> insert("U"));
        KeyListenenThread.keymap.put(KeyCodes.V.getCode(), n -> insert("V"));
        KeyListenenThread.keymap.put(KeyCodes.W.getCode(), n -> insert("W"));
        KeyListenenThread.keymap.put(KeyCodes.X.getCode(), n -> insert("X"));
        KeyListenenThread.keymap.put(KeyCodes.Y.getCode(), n -> insert("Y"));
        KeyListenenThread.keymap.put(KeyCodes.Z.getCode(), n -> insert("Z"));
    }

    static void set_comand_mode_keymap() {
        set_base_keymap();
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

        KeyListenenThread.keymap.put(KeyCodes.SPACE.getCode(), n -> insert(" "));
    }

    private static void submit() {
        String result = text.toString();
        command_history.set(0, text.toString());
        history_index = 0;
        text = new StringBuilder();
        cursor_pos = 0;
        command_history.addFirst("");
    }

    static void cycle_history_up() {
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

    static void cycle_history_down() {
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

    static void cycle_history_top() {
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

    static void cycle_history_bottom() {
        if (command_history.isEmpty()) {
            return;
        }
        history_index = 0;
        text = new StringBuilder(command_history.get(history_index));
        cursor_pos = text.length();
    }

    public static int findWordEndToRight(String input, int start_pos) {
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

    public static int findWordStartToLeft(String input, int start_pos) {
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

    static void move_cursor(KeyModifier modifier, int pos) {
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

    static void delete(boolean backspace) {
        if (selection_pos.x < selection_pos.y) {
            text.delete(selection_pos.x, selection_pos.y);
            cursor_pos = selection_pos.x;
            selection_pos.y = selection_pos.x;
        } else if (selection_pos.y < selection_pos.x) {
            text.delete(selection_pos.y, selection_pos.x);
            cursor_pos = selection_pos.y;
            selection_pos.x = selection_pos.y;
        } else if (backspace) {
            if (cursor_pos > 0) {
                text.delete(cursor_pos - 1, cursor_pos);
                cursor_pos--;
            }
        } else {
            if (cursor_pos < text.length()) {
                text.delete(cursor_pos, cursor_pos + 1);
            }
        }
    }

    static void insert(String s) {
        text.insert(cursor_pos, s);
        cursor_pos += s.length();
        validate_cursor_pos();
        validate_selection_pos();
    }

    static void validate_cursor_pos() {
        if (cursor_pos > text.length()) {
            cursor_pos = text.length();
        }
        if (cursor_pos < 0) {
            cursor_pos = 0;
        }
    }

    static void validate_selection_pos() {
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

    public static void KeyHandling() {
        for (byte[] pressed_key : new ArrayList<>(KeyListenenThread.active_keys)) {
            for (byte[] key : KeyListenenThread.keymap.keySet()) {
                if (Arrays.equals(pressed_key, key)) {
                    KeyListenenThread.active_keys.remove(pressed_key);
                    Consumer<byte[]> function = KeyListenenThread.keymap.get(key);
                    function.accept(key);
                    validate_cursor_pos();
                    validate_selection_pos();
                    break;
                }
            }
        }
        KeyListenenThread.active_keys.clear();
    }

    static char getCharSafely(StringBuilder sb, int index, char fallback) {
        if (index >= 0 && index < sb.length()) {
            return sb.charAt(index);
        } else {
            return fallback;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread kyThread = new KeyListenenThread();
        kyThread.start();

        set_guess_mode_keymap();

        long last_tick = System.currentTimeMillis();
        AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.BLINKING_BAR);
//        AVandiniEliaBzGuessGame.setCursorStyle(CursorStyles.STEADY_BAR);

        if (command_history.isEmpty()) {
            command_history = new ArrayList<>();
            command_history.addFirst(text.toString());
        }

        while (true) {
            if (System.currentTimeMillis() < last_tick + 1) {
                continue;
            }
            last_tick = System.currentTimeMillis();


            KeyHandling();
            if (getCharSafely(text, 0, ' ') == '.' && !command_mode) {
                command_mode = true;
                set_comand_mode_keymap();
            } else if (getCharSafely(text, 0, ' ') != '.' && command_mode) {
                command_mode = false;
                set_guess_mode_keymap();
            }

            AVandiniEliaBzGuessGame.eraseLine();
            AVandiniEliaBzGuessGame.resetAttrributes();
            AVandiniEliaBzGuessGame.setAttribute(FColors.GREEN);
            AVandiniEliaBzGuessGame.resetAttrributes();
            System.out.print("20> ");
            System.out.print(text);
            if (selection_pos.x != selection_pos.y) {
//                System.out.print(selection_pos.x + " != " + selection_pos.y);
                AVandiniEliaBzGuessGame.moveCursor(min(selection_pos.x, selection_pos.y) + 5, CursorMoveDirection.COLUMN);
                AVandiniEliaBzGuessGame.setAttribute(TextAttributes.INVERSE);
                System.out.print(text.substring(min(selection_pos.x, selection_pos.y), max(selection_pos.x, selection_pos.y)));
                AVandiniEliaBzGuessGame.resetAttrributes();
                AVandiniEliaBzGuessGame.moveCursor(text.length() + 5, CursorMoveDirection.COLUMN);
            }
            AVandiniEliaBzGuessGame.resetAttrributes();
            System.out.print(" ");
//            System.out.print(command_history);

            AVandiniEliaBzGuessGame.moveCursor(5 + cursor_pos, CursorMoveDirection.COLUMN);
        }

    }
}

