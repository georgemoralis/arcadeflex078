/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

import mame056.inptportH.InputPort;

public class inptportH {

    /*TODO*///#ifndef INPTPORT_H
/*TODO*///#define INPTPORT_H
/*TODO*///
/*TODO*///#include "memory.h"
/*TODO*///#include "input.h"
/*TODO*///
/*TODO*////* input ports handling */
/*TODO*///
/*TODO*////* Don't confuse this with the I/O ports in memory.h. This is used to handle game */
/*TODO*////* inputs (joystick, coin slots, etc). Typically, you will read them using */
/*TODO*////* input_port_[n]_r(), which you will associate to the appropriate memory */
/*TODO*////* address or I/O port. */
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*///#ifdef __cplusplus
/*TODO*///extern "C" {
/*TODO*///#endif
/*TODO*///
/*TODO*///struct InputPortTiny
/*TODO*///{
/*TODO*///	UINT16 mask;			/* bits affected */
/*TODO*///	UINT16 default_value;	/* default value for the bits affected */
/*TODO*///							/* you can also use one of the IP_ACTIVE defines below */
/*TODO*///	UINT32 type;			/* see defines below */
/*TODO*///	const char *name;		/* name to display */
/*TODO*///};
/*TODO*///
/*TODO*///struct InputPort
/*TODO*///{
/*TODO*///	UINT16 mask;			/* bits affected */
/*TODO*///	UINT16 default_value;	/* default value for the bits affected */
/*TODO*///							/* you can also use one of the IP_ACTIVE defines below */
/*TODO*///	UINT32 type;			/* see defines below */
/*TODO*///	const char *name;		/* name to display */
/*TODO*///	InputSeq seq;                  	/* input sequence affecting the input bits */
/*TODO*///#ifdef MESS
/*TODO*///	UINT32 arg;				/* extra argument needed in some cases */
/*TODO*///	UINT16 min, max;		/* for analog controls */
/*TODO*///#endif
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///#define IP_ACTIVE_HIGH 0x0000
/*TODO*///#define IP_ACTIVE_LOW 0xffff
/*TODO*///
    public static final int IPT_END = 1;
    public static final int IPT_PORT = 2;
    /* use IPT_JOYSTICK for panels where the player has one single joystick */
    public static final int IPT_JOYSTICK_UP = 3;
    public static final int IPT_JOYSTICK_DOWN = 4;
    public static final int IPT_JOYSTICK_LEFT = 5;
    public static final int IPT_JOYSTICK_RIGHT = 6;
    /* use IPT_JOYSTICKLEFT and IPT_JOYSTICKRIGHT for dual joystick panels */
    public static final int IPT_JOYSTICKRIGHT_UP = 7;
    public static final int IPT_JOYSTICKRIGHT_DOWN = 8;
    public static final int IPT_JOYSTICKRIGHT_LEFT = 9;
    public static final int IPT_JOYSTICKRIGHT_RIGHT = 10;
    public static final int IPT_JOYSTICKLEFT_UP = 11;
    public static final int IPT_JOYSTICKLEFT_DOWN = 12;
    public static final int IPT_JOYSTICKLEFT_LEFT = 13;
    public static final int IPT_JOYSTICKLEFT_RIGHT = 14;
    public static final int IPT_BUTTON1 = 15;
    public static final int IPT_BUTTON2 = 16;
    public static final int IPT_BUTTON3 = 17;
    public static final int IPT_BUTTON4 = 18;/* action buttons */
    public static final int IPT_BUTTON5 = 19;
    public static final int IPT_BUTTON6 = 20;
    public static final int IPT_BUTTON7 = 21;
    public static final int IPT_BUTTON8 = 22;
    public static final int IPT_BUTTON9 = 23;
    public static final int IPT_BUTTON10 = 24;

    /* analog inputs */
 /* the "arg" field contains the default sensitivity expressed as a percentage */
 /* (100 = default, 50 = half, 200 = twice) */
    public static final int IPT_ANALOG_START = 25;
    public static final int IPT_PADDLE = 26;
    public static final int IPT_PADDLE_V = 27;
    public static final int IPT_DIAL = 28;
    public static final int IPT_DIAL_V = 29;
    public static final int IPT_TRACKBALL_X = 30;
    public static final int IPT_TRACKBALL_Y = 31;
    public static final int IPT_AD_STICK_X = 32;
    public static final int IPT_AD_STICK_Y = 33;
    public static final int IPT_AD_STICK_Z = 34;
    public static final int IPT_LIGHTGUN_X = 35;
    public static final int IPT_LIGHTGUN_Y = 36;
    public static final int IPT_PEDAL = 37;
    public static final int IPT_PEDAL2 = 38;
    public static final int IPT_ANALOG_END = 39;

    public static final int IPT_START1 = 40;
    public static final int IPT_START2 = 41;
    public static final int IPT_START3 = 42;
    public static final int IPT_START4 = 43;/* start buttons */

    public static final int IPT_COIN1 = 44;
    public static final int IPT_COIN2 = 45;
    public static final int IPT_COIN3 = 46;
    public static final int IPT_COIN4 = 47;/* coin slots */

    public static final int IPT_SERVICE1 = 48;
    public static final int IPT_SERVICE2 = 49;
    public static final int IPT_SERVICE3 = 50;
    public static final int IPT_SERVICE4 = 51;/* service coin */

    public static final int IPT_SERVICE = 52;
    public static final int IPT_TILT = 53;
    public static final int IPT_DIPSWITCH_NAME = 54;
    public static final int IPT_DIPSWITCH_SETTING = 55;
    /* Many games poll an input bit to check for vertical blanks instead of using */
 /* interrupts. This special value allows you to handle that. If you set one of the */
 /* input bits to this, the bit will be inverted while a vertical blank is happening. */
    public static final int IPT_VBLANK = 56;
    public static final int IPT_UNKNOWN = 57;
    public static final int IPT_OSD_RESERVED = 58;
    public static final int IPT_OSD_1 = 59;
    public static final int IPT_OSD_2 = 60;
    public static final int IPT_OSD_3 = 61;
    public static final int IPT_OSD_4 = 62;
    public static final int IPT_EXTENSION = 63;
    /* this is an extension on the previous InputPort, not a real inputport. */
 /* It is used to store additional parameters for analog inputs */

 /* the following are special codes for user interface handling - not to be used by drivers! */
    public static final int IPT_UI_CONFIGURE = 64;
    public static final int IPT_UI_ON_SCREEN_DISPLAY = 65;
    public static final int IPT_UI_PAUSE = 66;
    public static final int IPT_UI_RESET_MACHINE = 67;
    public static final int IPT_UI_SHOW_GFX = 68;
    public static final int IPT_UI_FRAMESKIP_DEC = 69;
    public static final int IPT_UI_FRAMESKIP_INC = 70;
    public static final int IPT_UI_THROTTLE = 71;
    public static final int IPT_UI_SHOW_FPS = 72;
    public static final int IPT_UI_SNAPSHOT = 73;
    public static final int IPT_UI_TOGGLE_CHEAT = 74;
    public static final int IPT_UI_UP = 75;
    public static final int IPT_UI_DOWN = 76;
    public static final int IPT_UI_LEFT = 77;
    public static final int IPT_UI_RIGHT = 78;
    public static final int IPT_UI_SELECT = 79;
    public static final int IPT_UI_CANCEL = 80;
    public static final int IPT_UI_PAN_UP = 81;
    public static final int IPT_UI_PAN_DOWN = 82;
    public static final int IPT_UI_PAN_LEFT = 83;
    public static final int IPT_UI_PAN_RIGHT = 84;
    public static final int IPT_UI_SHOW_PROFILER = 85;
    public static final int IPT_UI_TOGGLE_UI = 86;
    public static final int IPT_UI_TOGGLE_DEBUG = 87;
    public static final int IPT_UI_SAVE_STATE = 88;
    public static final int IPT_UI_LOAD_STATE = 89;
    public static final int IPT_UI_ADD_CHEAT = 90;
    public static final int IPT_UI_DELETE_CHEAT = 91;
    public static final int IPT_UI_SAVE_CHEAT = 92;
    public static final int IPT_UI_WATCH_VALUE = 93;
    public static final int IPT_UI_EDIT_CHEAT = 94;
    public static final int IPT_UI_TOGGLE_CROSSHAIR = 95;

    /* 8 player support */
    public static final int IPT_START5 = 96;
    public static final int IPT_START6 = 97;
    public static final int IPT_START7 = 98;
    public static final int IPT_START8 = 99;
    public static final int IPT_COIN5 = 100;
    public static final int IPT_COIN6 = 101;
    public static final int IPT_COIN7 = 102;
    public static final int IPT_COIN8 = 103;
    public static final int __ipt_max = 104;

    /*TODO*///#define IPT_UNUSED     IPF_UNUSED
/*TODO*///#define IPT_SPECIAL    IPT_UNUSED	/* special meaning handled by custom functions */
/*TODO*///
/*TODO*///#define IPF_MASK       0xffffff00
/*TODO*///#define IPF_UNUSED     0x80000000	/* The bit is not used by this game, but is used */
/*TODO*///									/* by other games running on the same hardware. */
/*TODO*///									/* This is different from IPT_UNUSED, which marks */
/*TODO*///									/* bits not connected to anything. */
/*TODO*///#define IPF_COCKTAIL   IPF_PLAYER2	/* the bit is used in cocktail mode only */
/*TODO*///
/*TODO*///#define IPF_CHEAT      0x40000000	/* Indicates that the input bit is a "cheat" key */
/*TODO*///									/* (providing invulnerabilty, level advance, and */
/*TODO*///									/* so on). MAME will not recognize it when the */
/*TODO*///									/* -nocheat command line option is specified. */
/*TODO*///
    public static final int IPF_PLAYERMASK = 0x00070000;/* use IPF_PLAYERn if more than one person can */
    public static final int IPF_PLAYER1 = 0;/* play at the same time. The IPT_ should be the same */
    public static final int IPF_PLAYER2 = 0x00010000;/* for all players (e.g. IPT_BUTTON1 | IPF_PLAYER2) */
    public static final int IPF_PLAYER3 = 0x00020000;/* IPF_PLAYER1 is the default and can be left out to */
    public static final int IPF_PLAYER4 = 0x00030000;/* increase readability. */
    public static final int IPF_PLAYER5 = 0x00040000;
    public static final int IPF_PLAYER6 = 0x00050000;
    public static final int IPF_PLAYER7 = 0x00060000;
    public static final int IPF_PLAYER8 = 0x00070000;

    /*TODO*///
/*TODO*///#define IPF_8WAY       0         	/* Joystick modes of operation. 8WAY is the default, */
/*TODO*///#define IPF_4WAY       0x00080000	/* it prevents left/right or up/down to be pressed at */
/*TODO*///#define IPF_2WAY       0         	/* the same time. 4WAY prevents diagonal directions. */
/*TODO*///									/* 2WAY should be used for joysticks wich move only */
/*TODO*///                                 	/* on one axis (e.g. Battle Zone) */
/*TODO*///
/*TODO*///#define IPF_IMPULSE    0x00100000	/* When this is set, when the key corrisponding to */
/*TODO*///									/* the input bit is pressed it will be reported as */
/*TODO*///									/* pressed for a certain number of video frames and */
/*TODO*///									/* then released, regardless of the real status of */
/*TODO*///									/* the key. This is useful e.g. for some coin inputs. */
/*TODO*///									/* The number of frames the signal should stay active */
/*TODO*///									/* is specified in the "arg" field. */
/*TODO*///#define IPF_TOGGLE     0x00200000	/* When this is set, the key acts as a toggle - press */
/*TODO*///									/* it once and it goes on, press it again and it goes off. */
/*TODO*///									/* useful e.g. for sone Test Mode dip switches. */
/*TODO*///#define IPF_REVERSE    0x00400000	/* By default, analog inputs like IPT_TRACKBALL increase */
/*TODO*///									/* when going right/up. This flag inverts them. */
/*TODO*///
/*TODO*///#define IPF_CENTER     0x00800000	/* always preload in->default, autocentering the STICK/TRACKBALL */
/*TODO*///
/*TODO*///#define IPF_CUSTOM_UPDATE 0x01000000 /* normally, analog ports are updated when they are accessed. */
/*TODO*///									/* When this flag is set, they are never updated automatically, */
/*TODO*///									/* it is the responsibility of the driver to call */
/*TODO*///									/* update_analog_port(int port). */
/*TODO*///
/*TODO*///#define IPF_RESETCPU   0x02000000	/* when the key is pressed, reset the first CPU */
/*TODO*///
/*TODO*///
/*TODO*////* The "arg" field contains 4 bytes fields */
/*TODO*///#define IPF_SENSITIVITY(percent)	((percent & 0xff) << 8)
/*TODO*///#define IPF_DELTA(val)				((val & 0xff) << 16)
/*TODO*///
    public static int IP_GET_PLAYER(InputPort[] ports, int port) {
        return (ports[port].type >> 16) & 7;
    }
    /*TODO*///#define IP_GET_IMPULSE(port) (((port)->type >> 8) & 0xff)
/*TODO*///#define IP_GET_SENSITIVITY(port) ((((port)+1)->type >> 8) & 0xff)
/*TODO*///#define IP_SET_SENSITIVITY(port,val) ((port)+1)->type = ((port+1)->type & 0xffff00ff)|((val&0xff)<<8)
/*TODO*///#define IP_GET_DELTA(port) ((((port)+1)->type >> 16) & 0xff)
/*TODO*///#define IP_SET_DELTA(port,val) ((port)+1)->type = ((port+1)->type & 0xff00ffff)|((val&0xff)<<16)
/*TODO*///#define IP_GET_MIN(port) (((port)+1)->mask)
/*TODO*///#define IP_GET_MAX(port) (((port)+1)->default_value)
/*TODO*///#define IP_GET_CODE_OR1(port) ((port)->mask)
/*TODO*///#define IP_GET_CODE_OR2(port) ((port)->default_value)
/*TODO*///
/*TODO*///#define IP_NAME_DEFAULT ((const char *)-1)
/*TODO*///
/*TODO*////* Wrapper for compatibility */
/*TODO*///#define IP_KEY_DEFAULT CODE_DEFAULT
/*TODO*///#define IP_JOY_DEFAULT CODE_DEFAULT
/*TODO*///#define IP_KEY_PREVIOUS CODE_PREVIOUS
/*TODO*///#define IP_JOY_PREVIOUS CODE_PREVIOUS
/*TODO*///#define IP_KEY_NONE CODE_NONE
/*TODO*///#define IP_JOY_NONE CODE_NONE
/*TODO*///
/*TODO*////* start of table */
/*TODO*///#define INPUT_PORTS_START(name) \
/*TODO*///	static const struct InputPortTiny input_ports_##name[] = {
/*TODO*///
/*TODO*////* end of table */
/*TODO*///#define INPUT_PORTS_END \
/*TODO*///	{ 0, 0, IPT_END, 0  } \
/*TODO*///	};
/*TODO*////* start of a new input port */
/*TODO*///#define PORT_START \
/*TODO*///	{ 0, 0, IPT_PORT, 0 },
/*TODO*///
/*TODO*////* input bit definition */
/*TODO*///#define PORT_BIT_NAME(mask,default,type,name) \
/*TODO*///	{ mask, default, type, name },
/*TODO*///#define PORT_BIT(mask,default,type) \
/*TODO*///	PORT_BIT_NAME(mask, default, type, IP_NAME_DEFAULT)
/*TODO*///
/*TODO*////* impulse input bit definition */
/*TODO*///#define PORT_BIT_IMPULSE_NAME(mask,default,type,duration,name) \
/*TODO*///	PORT_BIT_NAME(mask, default, type | IPF_IMPULSE | ((duration & 0xff) << 8), name)
/*TODO*///#define PORT_BIT_IMPULSE(mask,default,type,duration) \
/*TODO*///	PORT_BIT_IMPULSE_NAME(mask, default, type, duration, IP_NAME_DEFAULT)
/*TODO*///
/*TODO*////* key/joy code specification */
/*TODO*///#define PORT_CODE(key,joy) \
/*TODO*///	{ key, joy, IPT_EXTENSION, 0 },
/*TODO*///
/*TODO*////* input bit definition with extended fields */
/*TODO*///#define PORT_BITX(mask,default,type,name,key,joy) \
/*TODO*///	PORT_BIT_NAME(mask, default, type, name) \
/*TODO*///	PORT_CODE(key,joy)
/*TODO*///
/*TODO*////* analog input */
/*TODO*///#define PORT_ANALOG(mask,default,type,sensitivity,delta,min,max) \
/*TODO*///	PORT_BIT(mask, default, type) \
/*TODO*///	{ min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT },
/*TODO*///
/*TODO*///#define PORT_ANALOGX(mask,default,type,sensitivity,delta,min,max,keydec,keyinc,joydec,joyinc) \
/*TODO*///	PORT_BIT(mask, default, type) \
/*TODO*///	{ min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT }, \
/*TODO*///	PORT_CODE(keydec,joydec) \
/*TODO*///	PORT_CODE(keyinc,joyinc)
/*TODO*///
/*TODO*////* dip switch definition */
/*TODO*///#define PORT_DIPNAME(mask,default,name) \
/*TODO*///	PORT_BIT_NAME(mask, default, IPT_DIPSWITCH_NAME, name)
/*TODO*///
/*TODO*///#define PORT_DIPSETTING(default,name) \
/*TODO*///	PORT_BIT_NAME(0, default, IPT_DIPSWITCH_SETTING, name)
/*TODO*///
/*TODO*///
/*TODO*///#define PORT_SERVICE(mask,default)	\
/*TODO*///	PORT_BITX(    mask, mask & default, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )	\
/*TODO*///	PORT_DIPSETTING(    mask & default, DEF_STR( Off ) )	\
/*TODO*///	PORT_DIPSETTING(    mask &~default, DEF_STR( On ) )
/*TODO*///
/*TODO*///#define PORT_SERVICE_NO_TOGGLE(mask,default)	\
/*TODO*///	PORT_BITX(    mask, mask & default, IPT_SERVICE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
/*TODO*///
/*TODO*///#define MAX_DEFSTR_LEN 20
/*TODO*///extern const char ipdn_defaultstrings[][MAX_DEFSTR_LEN];
/*TODO*///
/*TODO*////* this must match the ipdn_defaultstrings list in inptport.c */
/*TODO*///enum {
/*TODO*///	STR_Off,
/*TODO*///	STR_On,
/*TODO*///	STR_No,
/*TODO*///	STR_Yes,
/*TODO*///	STR_Lives,
/*TODO*///	STR_Bonus_Life,
/*TODO*///	STR_Difficulty,
/*TODO*///	STR_Demo_Sounds,
/*TODO*///	STR_Coinage,
/*TODO*///	STR_Coin_A,
/*TODO*///	STR_Coin_B,
/*TODO*///	STR_9C_1C,
/*TODO*///	STR_8C_1C,
/*TODO*///	STR_7C_1C,
/*TODO*///	STR_6C_1C,
/*TODO*///	STR_5C_1C,
/*TODO*///	STR_4C_1C,
/*TODO*///	STR_3C_1C,
/*TODO*///	STR_8C_3C,
/*TODO*///	STR_4C_2C,
/*TODO*///	STR_2C_1C,
/*TODO*///	STR_5C_3C,
/*TODO*///	STR_3C_2C,
/*TODO*///	STR_4C_3C,
/*TODO*///	STR_4C_4C,
/*TODO*///	STR_3C_3C,
/*TODO*///	STR_2C_2C,
/*TODO*///	STR_1C_1C,
/*TODO*///	STR_4C_5C,
/*TODO*///	STR_3C_4C,
/*TODO*///	STR_2C_3C,
/*TODO*///	STR_4C_7C,
/*TODO*///	STR_2C_4C,
/*TODO*///	STR_1C_2C,
/*TODO*///	STR_2C_5C,
/*TODO*///	STR_2C_6C,
/*TODO*///	STR_1C_3C,
/*TODO*///	STR_2C_7C,
/*TODO*///	STR_2C_8C,
/*TODO*///	STR_1C_4C,
/*TODO*///	STR_1C_5C,
/*TODO*///	STR_1C_6C,
/*TODO*///	STR_1C_7C,
/*TODO*///	STR_1C_8C,
/*TODO*///	STR_1C_9C,
/*TODO*///	STR_Free_Play,
/*TODO*///	STR_Cabinet,
/*TODO*///	STR_Upright,
/*TODO*///	STR_Cocktail,
/*TODO*///	STR_Flip_Screen,
/*TODO*///	STR_Service_Mode,
/*TODO*///	/*STR_Pause,
/*TODO*///	STR_Test,
/*TODO*///	STR_Tilt,
/*TODO*///	STR_Version,
/*TODO*///	STR_Region,
/*TODO*///	STR_International,
/*TODO*///	STR_Japan,
/*TODO*///	STR_USA,
/*TODO*///	STR_Europe,
/*TODO*///	STR_Asia,
/*TODO*///	STR_World,
/*TODO*///	STR_Hispanic,
/*TODO*///	STR_Language,
/*TODO*///	STR_English,
/*TODO*///	STR_Japanese,
/*TODO*///	STR_German,
/*TODO*///	STR_French,
/*TODO*///	STR_Italian,
/*TODO*///	STR_Spanish,
/*TODO*///	STR_Very_Easy,
/*TODO*///	STR_Easy,
/*TODO*///	STR_Normal,
/*TODO*///	STR_Medium,
/*TODO*///	STR_Hard,
/*TODO*///	STR_Harder,
/*TODO*///	STR_Hardest,
/*TODO*///	STR_Very_Hard,
/*TODO*///	STR_Very_Low,
/*TODO*///	STR_Low,
/*TODO*///	STR_High,
/*TODO*///	STR_Higher,
/*TODO*///	STR_Highest,
/*TODO*///	STR_Very_High,
/*TODO*///	STR_Players,
/*TODO*///	STR_Controls,
/*TODO*///	STR_Dual,
/*TODO*///	STR_Single,
/*TODO*///	STR_Game_Time,
/*TODO*///	STR_Continue_Price,
/*TODO*///	STR_Controller,
/*TODO*///	STR_Light_Gun,
/*TODO*///	STR_Joystick,
/*TODO*///	STR_Trackball,
/*TODO*///	STR_Continues,
/*TODO*///	STR_Allow_Continue,
/*TODO*///	STR_Level_Select,
/*TODO*///	STR_Infinite,
/*TODO*///	STR_Stereo,
/*TODO*///	STR_Mono,*/
/*TODO*///	STR_Unused,
/*TODO*///	STR_Unknown,
/*TODO*///	STR_TOTAL
/*TODO*///};
/*TODO*///
/*TODO*///enum { IKT_STD, IKT_IPT, IKT_IPT_EXT, IKT_OSD_KEY, IKT_OSD_JOY };
/*TODO*///
/*TODO*///#define DEF_STR(str_num) (ipdn_defaultstrings[STR_##str_num])
/*TODO*///
/*TODO*///#define MAX_INPUT_PORTS 30
/*TODO*///
/*TODO*///
/*TODO*///int load_input_port_settings(void);
/*TODO*///void save_input_port_settings(void);
/*TODO*///
/*TODO*///const char *input_port_name(const struct InputPort *in);
/*TODO*///InputSeq* input_port_type_seq(int type);
/*TODO*///InputSeq* input_port_seq(const struct InputPort *in);
/*TODO*///
/*TODO*///struct InputPort* input_port_allocate(const struct InputPortTiny *src);
/*TODO*///void input_port_free(struct InputPort* dst);
/*TODO*///
/*TODO*///#ifdef MAME_NET
/*TODO*///void set_default_player_controls(int player);
/*TODO*///#endif /* MAME_NET */
/*TODO*///
/*TODO*///void init_analog_seq(void);
/*TODO*///
/*TODO*///void update_analog_port(int port);
/*TODO*///void update_input_ports(void);	/* called by cpuintrf.c - not for external use */
/*TODO*///void inputport_vblank_end(void);	/* called by cpuintrf.c - not for external use */
/*TODO*///
/*TODO*///int readinputport(int port);
/*TODO*///READ_HANDLER( input_port_0_r );
/*TODO*///READ_HANDLER( input_port_1_r );
/*TODO*///READ_HANDLER( input_port_2_r );
/*TODO*///READ_HANDLER( input_port_3_r );
/*TODO*///READ_HANDLER( input_port_4_r );
/*TODO*///READ_HANDLER( input_port_5_r );
/*TODO*///READ_HANDLER( input_port_6_r );
/*TODO*///READ_HANDLER( input_port_7_r );
/*TODO*///READ_HANDLER( input_port_8_r );
/*TODO*///READ_HANDLER( input_port_9_r );
/*TODO*///READ_HANDLER( input_port_10_r );
/*TODO*///READ_HANDLER( input_port_11_r );
/*TODO*///READ_HANDLER( input_port_12_r );
/*TODO*///READ_HANDLER( input_port_13_r );
/*TODO*///READ_HANDLER( input_port_14_r );
/*TODO*///READ_HANDLER( input_port_15_r );
/*TODO*///READ_HANDLER( input_port_16_r );
/*TODO*///READ_HANDLER( input_port_17_r );
/*TODO*///READ_HANDLER( input_port_18_r );
/*TODO*///READ_HANDLER( input_port_19_r );
/*TODO*///READ_HANDLER( input_port_20_r );
/*TODO*///READ_HANDLER( input_port_21_r );
/*TODO*///READ_HANDLER( input_port_22_r );
/*TODO*///READ_HANDLER( input_port_23_r );
/*TODO*///READ_HANDLER( input_port_24_r );
/*TODO*///READ_HANDLER( input_port_25_r );
/*TODO*///READ_HANDLER( input_port_26_r );
/*TODO*///READ_HANDLER( input_port_27_r );
/*TODO*///READ_HANDLER( input_port_28_r );
/*TODO*///READ_HANDLER( input_port_29_r );
/*TODO*///
/*TODO*///READ16_HANDLER( input_port_0_word_r );
/*TODO*///READ16_HANDLER( input_port_1_word_r );
/*TODO*///READ16_HANDLER( input_port_2_word_r );
/*TODO*///READ16_HANDLER( input_port_3_word_r );
/*TODO*///READ16_HANDLER( input_port_4_word_r );
/*TODO*///READ16_HANDLER( input_port_5_word_r );
/*TODO*///READ16_HANDLER( input_port_6_word_r );
/*TODO*///READ16_HANDLER( input_port_7_word_r );
/*TODO*///READ16_HANDLER( input_port_8_word_r );
/*TODO*///READ16_HANDLER( input_port_9_word_r );
/*TODO*///READ16_HANDLER( input_port_10_word_r );
/*TODO*///READ16_HANDLER( input_port_11_word_r );
/*TODO*///READ16_HANDLER( input_port_12_word_r );
/*TODO*///READ16_HANDLER( input_port_13_word_r );
/*TODO*///READ16_HANDLER( input_port_14_word_r );
/*TODO*///READ16_HANDLER( input_port_15_word_r );
/*TODO*///READ16_HANDLER( input_port_16_word_r );
/*TODO*///READ16_HANDLER( input_port_17_word_r );
/*TODO*///READ16_HANDLER( input_port_18_word_r );
/*TODO*///READ16_HANDLER( input_port_19_word_r );
/*TODO*///READ16_HANDLER( input_port_20_word_r );
/*TODO*///READ16_HANDLER( input_port_21_word_r );
/*TODO*///READ16_HANDLER( input_port_22_word_r );
/*TODO*///READ16_HANDLER( input_port_23_word_r );
/*TODO*///READ16_HANDLER( input_port_24_word_r );
/*TODO*///READ16_HANDLER( input_port_25_word_r );
/*TODO*///READ16_HANDLER( input_port_26_word_r );
/*TODO*///READ16_HANDLER( input_port_27_word_r );
/*TODO*///READ16_HANDLER( input_port_28_word_r );
/*TODO*///READ16_HANDLER( input_port_29_word_r );
/*TODO*///
/*TODO*///struct ipd
/*TODO*///{
/*TODO*///	UINT32 type;
/*TODO*///	const char *name;
/*TODO*///	InputSeq seq;
/*TODO*///};
/*TODO*///
/*TODO*///struct ik
/*TODO*///{
/*TODO*///	const char *name;
/*TODO*///	UINT32 type;
/*TODO*///	UINT32 val;
/*TODO*///};
/*TODO*///extern struct ik input_keywords[];
/*TODO*///extern struct ik *osd_input_keywords;
/*TODO*///extern int num_ik;
/*TODO*///
/*TODO*///void seq_set_string(InputSeq* a, const char *buf);
/*TODO*///
/*TODO*///#ifdef __cplusplus
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
