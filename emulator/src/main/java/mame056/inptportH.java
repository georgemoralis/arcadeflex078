/**
 * Ported to 0.56
 */
package mame056;

import static arcadeflex.v078.mame.inptportH.IPT_DIPSWITCH_NAME;
import static arcadeflex.v078.mame.inptportH.IPT_DIPSWITCH_SETTING;
import static arcadeflex.v078.mame.inptportH.IPT_END;
import static arcadeflex.v078.mame.inptportH.IPT_EXTENSION;
import static arcadeflex.v078.mame.inptportH.IPT_PORT;
import java.util.ArrayList;
import static mame056.inptport.*;
import static arcadeflex.v078.mame.inputH.*;

public class inptportH {

    /* input ports handling */

 /* Don't confuse this with the I/O ports in memory.h. This is used to handle game */
 /* inputs (joystick, coin slots, etc). Typically, you will read them using */
 /* input_port_[n]_r(), which you will associate to the appropriate memory */
 /* address or I/O port. */
    /**
     * ************************************************************************
     */
    public static class InputPortTiny {

        private InputPortTiny(int mask, int default_value, int type, String name) {
            this.mask = mask;
            this.default_value = default_value;
            this.type = type;
            this.name = name;
        }
        public int/*UINT16*/ mask;/* bits affected */
        public int/*UINT16*/ default_value;/* default value for the bits affected  you can also use one of the IP_ACTIVE defines below */
        public int/*UINT32*/ type;/* see defines below */
        public String name;/* name to display */
    }

    public static class InputPort {

        public int /*UINT16*/ mask;/* bits affected */
        public int /*UINT16*/ default_value;/* default value for the bits affected */
 /* you can also use one of the IP_ACTIVE defines below */
        public int /*UINT32*/ type;/* see defines below */
        public String name;/* name to display */
        public int[] seq = new int[SEQ_MAX];/* input sequence affecting the input bits */

        public InputPort() {
            for (int i = 0; i < SEQ_MAX; i++) {
                seq[i] = 0;
            }
        }
    }

    public static final int IP_ACTIVE_HIGH = 0x0000;
    public static final int IP_ACTIVE_LOW = 0xffff;


    public static final int IPF_MASK = 0xffffff00;
    public static final int IPF_UNUSED = 0x80000000;/* The bit is not used by this game, but is used */
 /* by other games running on the same hardware. */
 /* This is different from IPT_UNUSED, which marks */
 /* bits not connected to anything. */

    public static final int IPT_UNUSED = IPF_UNUSED;
    public static final int IPT_SPECIAL = IPT_UNUSED;/* special meaning handled by custom functions */

    public static final int IPF_CHEAT = 0x40000000;/* Indicates that the input bit is a "cheat" key */
 /* (providing invulnerabilty, level advance, and */
 /* so on). MAME will not recognize it when the */
 /* -nocheat command line option is specified. */

    public static final int IPF_PLAYERMASK = 0x00030000;/* use IPF_PLAYERn if more than one person can */
    public static final int IPF_PLAYER1 = 0;/* play at the same time. The IPT_ should be the same */
    public static final int IPF_PLAYER2 = 0x00010000;/* for all players (e.g. IPT_BUTTON1 | IPF_PLAYER2) */
    public static final int IPF_PLAYER3 = 0x00020000;/* IPF_PLAYER1 is the default and can be left out to */
    public static final int IPF_PLAYER4 = 0x00030000;/* increase readability. */

    public static final int IPF_8WAY = 0;/* Joystick modes of operation. 8WAY is the default, */
    public static final int IPF_4WAY = 0x00080000;/* it prevents left/right or up/down to be pressed at */
    public static final int IPF_2WAY = 0;/* the same time. 4WAY prevents diagonal directions. */
 /* 2WAY should be used for joysticks wich move only */
 /* on one axis (e.g. Battle Zone) */
    public static final int IPF_COCKTAIL = IPF_PLAYER2;/* the bit is used in cocktail mode only */

    public static final int IPF_IMPULSE = 0x00100000;/* When this is set, when the key corrisponding to */
 /* the input bit is pressed it will be reported as */
 /* pressed for a certain number of video frames and */
 /* then released, regardless of the real status of */
 /* the key. This is useful e.g. for some coin inputs. */
 /* The number of frames the signal should stay active */
 /* is specified in the "arg" field. */
    public static final int IPF_TOGGLE = 0x00200000;/* When this is set, the key acts as a toggle - press */
 /* it once and it goes on, press it again and it goes off. */
 /* useful e.g. for sone Test Mode dip switches. */
    public static final int IPF_REVERSE = 0x00400000;/* By default, analog inputs like IPT_TRACKBALL increase */
 /* when going right/up. This flag inverts them. */

    public static final int IPF_CENTER = 0x00800000;/* always preload in->default, autocentering the STICK/TRACKBALL */

    public static final int IPF_CUSTOM_UPDATE = 0x01000000;/* normally, analog ports are updated when they are accessed. */
 /* When this flag is set, they are never updated automatically, */
 /* it is the responsibility of the driver to call */
 /* update_analog_port(int port). */

    public static final int IPF_RESETCPU = 0x02000000;/* when the key is pressed, reset the first CPU */


 /* The "arg" field contains 4 bytes fields */
    public static int IPF_SENSITIVITY(int percent) {
        return ((percent & 0xff) << 8);
    }

    public static int IPF_DELTA(int val) {
        return ((val & 0xff) << 16);
    }

    public static int IP_GET_IMPULSE(InputPort[] ports, int port) {
        return ((ports[port].type >> 8) & 0xff);
    }

    public static int IP_GET_SENSITIVITY(InputPort[] ports, int port) {
        return (ports[port + 1].type >> 8) & 0xff;
    }

    public static void IP_SET_SENSITIVITY(InputPort[] ports, int port, int val) {
        ports[port + 1].type = (ports[port + 1].type & 0xffff00ff) | ((val & 0xff) << 8);
    }

    public static int IP_GET_DELTA(InputPort[] ports, int port) {
        return ((ports[port + 1].type >> 16) & 0xff);
    }

    public static void IP_SET_DELTA(InputPort[] ports, int port, int val) {
        ports[port + 1].type = (ports[port + 1].type & 0xff00ffff) | ((val & 0xff) << 16);
    }

    public static int IP_GET_MIN(InputPort[] ports, int port) {
        return (ports[port + 1].mask);
    }

    public static int IP_GET_MAX(InputPort[] ports, int port) {
        return (ports[port + 1].default_value);
    }

    public static int IP_GET_CODE_OR1(InputPortTiny port) {
        return port.mask;
    }

    public static int IP_GET_CODE_OR2(InputPortTiny port) {
        return port.default_value;
    }

    public static final String IP_NAME_DEFAULT = "-1";

    /* Wrapper for compatibility */
    public static final int IP_KEY_DEFAULT = CODE_DEFAULT;
    public static final int IP_JOY_DEFAULT = CODE_DEFAULT;
    public static final int IP_KEY_PREVIOUS = CODE_PREVIOUS;
    public static final int IP_JOY_PREVIOUS = CODE_PREVIOUS;
    public static final int IP_KEY_NONE = CODE_NONE;
    public static final int IP_JOY_NONE = CODE_NONE;

    /* start of table */
    public static InputPortTiny[] input_macro = null;
    public static ArrayList<InputPortTiny> inputload = new ArrayList<InputPortTiny>();

    /* end of table */
    public static void INPUT_PORTS_END() {
        inputload.add(new InputPortTiny(0, 0, IPT_END, null));
        input_macro = inputload.toArray(new InputPortTiny[inputload.size()]);
        inputload.clear();
    }

    /* start of a new input port */
    public static void PORT_START() {
        inputload.add(new InputPortTiny(0, 0, IPT_PORT, null));
    }

    /* input bit definition */
    public static void PORT_BIT_NAME(int mask, int _default, int type, String name) {
        inputload.add(new InputPortTiny(mask, _default, type, name));
    }

    public static void PORT_BIT(int mask, int _default, int type) {
        PORT_BIT_NAME(mask, _default, type, IP_NAME_DEFAULT);
    }

    /* impulse input bit definition */
    public static void PORT_BIT_IMPULSE_NAME(int mask, int _default, int type, int duration, String name) {
        PORT_BIT_NAME(mask, _default, type | IPF_IMPULSE | ((duration & 0xff) << 8), name);
    }

    public static void PORT_BIT_IMPULSE(int mask, int _default, int type, int duration) {
        PORT_BIT_IMPULSE_NAME(mask, _default, type, duration, IP_NAME_DEFAULT);
    }

    /* key/joy code specification */
    public static void PORT_CODE(int key, int joy) {
        inputload.add(new InputPortTiny(key, joy, IPT_EXTENSION, null));
    }

    /* input bit definition with extended fields */
    public static void PORT_BITX(int mask, int _default, int type, String name, int key, int joy) {
        PORT_BIT_NAME(mask, _default, type, name);
        PORT_CODE(key, joy);
    }

    /* analog input */
    public static void PORT_ANALOG(int mask, int _default, int type, int sensitivity, int delta, int min, int max) {
        PORT_BIT(mask, _default, type);
        inputload.add(new InputPortTiny(min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT));
    }

    public static void PORT_ANALOGX(int mask, int _default, int type, int sensitivity, int delta, int min, int max, int keydec, int keyinc, int joydec, int joyinc) {
        PORT_BIT(mask, _default, type);
        inputload.add(new InputPortTiny(min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT));
        PORT_CODE(keydec, joydec);
        PORT_CODE(keyinc, joyinc);
    }

    /* dip switch definition */
    public static void PORT_DIPNAME(int mask, int _default, String name) {
        PORT_BIT_NAME(mask, _default, IPT_DIPSWITCH_NAME, name);
    }

    public static void PORT_DIPSETTING(int _default, String name) {
        PORT_BIT_NAME(0, _default, IPT_DIPSWITCH_SETTING, name);
    }

    public static void PORT_SERVICE(int mask, int _default) {
        PORT_BITX(mask, mask & _default, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
        PORT_DIPSETTING(mask & _default, DEF_STR("Off"));
        PORT_DIPSETTING(mask & ~_default, DEF_STR("On"));
    }

    public static String DEF_STR(String num) {
        return ipdn_defaultstrings.get(num);
    }
    public static final int MAX_INPUT_PORTS = 20;

    public static class ipd {

        public /*UINT32 */ int type;
        public String name;
        public /*InputSeq*/ int[] seq;

        public ipd(int type, String name, int[] seq) {
            this.type = type;
            this.name = name;
            this.seq = seq;
        }
    }
}
