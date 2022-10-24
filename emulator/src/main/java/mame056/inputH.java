/**
 * Ported to 0.56
 */
package mame056;

import static arcadeflex.v078.mame.inputH.CODE_NONE;
import static mame056.input.*;

public class inputH {

    public static class KeyboardInfo {

        public KeyboardInfo(String name, int code, int standardcode) {
            this.name = name;
            this.code = code;
            this.standardcode = standardcode;
        }
        public String name;/* OS dependant name; 0 terminates the list */
        public int code;/* OS dependant code */
        public int standardcode;/* CODE_xxx equivalent from list below, or CODE_OTHER if n/a */
    }
    /*TODO*///struct JoystickInfo
/*TODO*///{
/*TODO*///	char *name; /* OS dependant name; 0 terminates the list */
/*TODO*///	unsigned code; /* OS dependant code */
/*TODO*///	InputCode standardcode;	/* CODE_xxx equivalent from list below, or CODE_OTHER if n/a */
/*TODO*///};
/*TODO*///


 


 /* Wrapper for compatibility */
 /*TODO*///#define KEYCODE_OTHER CODE_OTHER
/*TODO*///#define JOYCODE_OTHER CODE_OTHER
/*TODO*///#define KEYCODE_NONE CODE_NONE
/*TODO*///#define JOYCODE_NONE CODE_NONE

    /*TODO*////* Wrappers for compatibility */
/*TODO*///#define keyboard_name                   code_name
/*TODO*///#define keyboard_pressed                code_pressed
    public static int keyboard_pressed_memory(int code) {
        return code_pressed_memory(code);
    }
    /*TODO*///#define keyboard_pressed_memory_repeat  code_pressed_memory_repeat
/*TODO*///#define keyboard_read_async             code_read_async
    /**
     * Sequence code funtions
     */
    /* NOTE: If you modify this value you need also to modify the SEQ_DEF declarations */
    public static final int SEQ_MAX = 16;

    public static int seq_get_1(int[] a) {
        return a[0];
    }

    /* NOTE: It's very important that this sequence is EXACLY long SEQ_MAX */
    public static int[] SEQ_DEF_6(int a, int b, int c, int d, int e, int f) {
        return new int[]{a, b, c, d, e, f, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE};
    }

    public static int[] SEQ_DEF_5(int a, int b, int c, int d, int e) {
        return SEQ_DEF_6(a, b, c, d, e, CODE_NONE);
    }

    public static int[] SEQ_DEF_4(int a, int b, int c, int d) {
        return SEQ_DEF_5(a, b, c, d, CODE_NONE);
    }

    public static int[] SEQ_DEF_3(int a, int b, int c) {
        return SEQ_DEF_4(a, b, c, CODE_NONE);
    }

    public static int[] SEQ_DEF_2(int a, int b) {
        return SEQ_DEF_3(a, b, CODE_NONE);
    }

    public static int[] SEQ_DEF_1(int a) {
        return SEQ_DEF_2(a, CODE_NONE);
    }

    public static int[] SEQ_DEF_0() {
        return SEQ_DEF_1(CODE_NONE);
    }
}
