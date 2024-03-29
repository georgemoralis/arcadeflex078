/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuexecH.*;
import static arcadeflex.v078.mame.mame.*;
import static arcadeflex.v078.mame.memoryH.*;
//TODO
import arcadeflex056.fucPtr.VhConvertColorPromPtr;
import mame056.drawgfxH.GfxDecodeInfo;
import arcadeflex.v078.mame.drawgfxH.rectangle;
import mame056.sndintrfH.MachineSound;

public class driverH {

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Macros for declaring common callbacks
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#define DRIVER_INIT(name)		void init_##name(void)
/*TODO*///
/*TODO*///#define INTERRUPT_GEN(func)		void func(void)
/*TODO*///
/*TODO*///#define MACHINE_INIT(name)		void machine_init_##name(void)
/*TODO*///#define MACHINE_STOP(name)		void machine_stop_##name(void)
/*TODO*///
/*TODO*///#define NVRAM_HANDLER(name)		void nvram_handler_##name(mame_file *file, int read_or_write)
/*TODO*///
/*TODO*///#define PALETTE_INIT(name)		void palette_init_##name(UINT16 *colortable, const UINT8 *color_prom)
/*TODO*///
/*TODO*///#define VIDEO_START(name)		int video_start_##name(void)
/*TODO*///#define VIDEO_STOP(name)		void video_stop_##name(void)
/*TODO*///#define VIDEO_EOF(name)			void video_eof_##name(void)
/*TODO*///#define VIDEO_UPDATE(name)		void video_update_##name(struct mame_bitmap *bitmap, const struct rectangle *cliprect)
/*TODO*///
/*TODO*////* NULL versions */
/*TODO*///#define init_NULL				NULL
/*TODO*///#define machine_init_NULL 		NULL
/*TODO*///#define nvram_handler_NULL 		NULL
/*TODO*///#define palette_init_NULL		NULL
/*TODO*///#define video_start_NULL 		NULL
/*TODO*///#define video_stop_NULL 		NULL
/*TODO*///#define video_eof_NULL 			NULL
/*TODO*///#define video_update_NULL 		NULL
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Macros for building machine drivers
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* use this to declare external references to a machine driver */
/*TODO*///#define MACHINE_DRIVER_EXTERN(game)										\
/*TODO*///	void construct_##game(struct InternalMachineDriver *machine)		\
/*TODO*///
/*TODO*///
/* start/end tags for the machine driver */
    static MachineCPU temp_cpu = null;//reference to current cpu
    static InternalMachineDriver temp_machine = null;//reference to current machine

    public static void MACHINE_DRIVER_START(InternalMachineDriver machine) {
        temp_machine = machine;
    }

    public static void MACHINE_DRIVER_END() {
        //clear temp variables
        temp_cpu = null;
        temp_machine = null;
    }

    /*TODO*///
/*TODO*///
/*TODO*////* importing data from other machine drivers */
/*TODO*///#define MDRV_IMPORT_FROM(game) 											\
/*TODO*///	construct_##game(machine); 											\
/*TODO*///
/*TODO*///
//* add/modify/remove/replace CPUs */
    public static void MDRV_CPU_ADD_TAG(String tag, int type, int clock) {
        temp_cpu = machine_add_cpu(temp_machine, tag, type, clock);
    }

    public static void MDRV_CPU_ADD(int type, int clock) {
        MDRV_CPU_ADD_TAG(null, type, clock);
    }

    /*TODO*///#define MDRV_CPU_MODIFY(tag)											\
/*TODO*///	cpu = machine_find_cpu(machine, tag);								\
/*TODO*///
/*TODO*///#define MDRV_CPU_REMOVE(tag)											\
/*TODO*///	machine_remove_cpu(machine, tag);									\
/*TODO*///	cpu = NULL;															\
/*TODO*///
/*TODO*///#define MDRV_CPU_REPLACE(tag, type, clock)								\
/*TODO*///	cpu = machine_find_cpu(machine, tag);								\
/*TODO*///	if (cpu)															\
/*TODO*///	{																	\
/*TODO*///		cpu->cpu_type = (CPU_##type);									\
/*TODO*///		cpu->cpu_clock = (clock);										\
/*TODO*///	}																	\
/*TODO*///

    /* CPU parameters */
    public static void MDRV_CPU_FLAGS(int flags) {
        if (temp_cpu != null) {
            temp_cpu.cpu_flags = (flags);
        }
    }

    /*TODO*///#define MDRV_CPU_CONFIG(config)											\
/*TODO*///	if (cpu)															\
/*TODO*///		cpu->reset_param = &(config);									\
/*TODO*///
    public static void MDRV_CPU_MEMORY(Object[] readmem, Object[] writemem) {
        if (temp_cpu != null) {
            temp_cpu.memory_read = readmem;
            temp_cpu.memory_write = writemem;
        }
    }

    public static void MDRV_CPU_PORTS(IO_ReadPort[] readport, IO_WritePort[] writeport) {
        if (temp_cpu != null) {
            temp_cpu.port_read = (readport);
            temp_cpu.port_write = (writeport);
        }
    }

    public static void MDRV_CPU_VBLANK_INT(InterruptHandlerPtr func, int rate) {
        if (temp_cpu != null) {
            temp_cpu.vblank_interrupt = func;
            temp_cpu.vblank_interrupts_per_frame = (rate);
        }
    }

    /*TODO*///#define MDRV_CPU_PERIODIC_INT(func, rate)								\
/*TODO*///	if (cpu)															\
/*TODO*///	{																	\
/*TODO*///		cpu->timed_interrupt = func;									\
/*TODO*///		cpu->timed_interrupts_per_second = (rate);						\
/*TODO*///	}																	\
/*TODO*///
/*TODO*///
    /* core parameters */
    public static void MDRV_FRAMES_PER_SECOND(int rate) {
        temp_machine.frames_per_second = (rate);
    }

    public static void MDRV_VBLANK_DURATION(int duration) {
        temp_machine.vblank_duration = (duration);
    }

    /*TODO*///#define MDRV_INTERLEAVE(interleave)										\
/*TODO*///	machine->cpu_slices_per_frame = (interleave);						\
/*TODO*///

    /* core functions */
    public static void MDRV_MACHINE_INIT(MachineInitHandlerPtr machine_init_name) {
	temp_machine.machine_init = machine_init_name;
    }

/*TODO*///#define MDRV_MACHINE_STOP(name)											\
/*TODO*///	machine->machine_stop = machine_stop_##name;						\
/*TODO*///
/*TODO*///#define MDRV_NVRAM_HANDLER(name)										\
/*TODO*///	machine->nvram_handler = nvram_handler_##name;						\
/*TODO*///
/*TODO*///
    /* core video parameters */
    public static void MDRV_VIDEO_ATTRIBUTES(int flags) {
        temp_machine.video_attributes = (flags);
    }

    /*TODO*///#define MDRV_ASPECT_RATIO(num, den)										\
/*TODO*///	machine->aspect_x = (num);											\
/*TODO*///	machine->aspect_y = (den);											\
/*TODO*///
    public static void MDRV_SCREEN_SIZE(int width, int height) {
        temp_machine.screen_width = (width);
        temp_machine.screen_height = (height);
    }

    public static void MDRV_VISIBLE_AREA(int minx, int maxx, int miny, int maxy) {
        temp_machine.default_visible_area = new rectangle();
        temp_machine.default_visible_area.min_x = (minx);
        temp_machine.default_visible_area.max_x = (maxx);
        temp_machine.default_visible_area.min_y = (miny);
        temp_machine.default_visible_area.max_y = (maxy);
    }

    public static void MDRV_GFXDECODE(GfxDecodeInfo[] gfx) {
        temp_machine.gfxdecodeinfo = (gfx);
    }

    public static void MDRV_PALETTE_LENGTH(int length) {
        temp_machine.total_colors = (length);
    }

    public static void MDRV_COLORTABLE_LENGTH(int length) {
        temp_machine.color_table_len = (length);
    }

    /* core video functions */
    public static void MDRV_PALETTE_INIT(PaletteInitHandlerPtr name) {
        temp_machine.init_palette = name;
    }

    public static void MDRV_VIDEO_START(VideoStartHandlerPtr name) {
        temp_machine.video_start = name;
    }

    /*TODO*///#define MDRV_VIDEO_STOP(name)											\
/*TODO*///	machine->video_stop = video_stop_##name;							\
/*TODO*///
/*TODO*///#define MDRV_VIDEO_EOF(name)											\
/*TODO*///	machine->video_eof = video_eof_##name;								\
/*TODO*///
    public static void MDRV_VIDEO_UPDATE(VideoUpdateHandlerPtr name) {
        temp_machine.video_update = name;
    }

    /*TODO*////* core sound parameters */
/*TODO*///#define MDRV_SOUND_ATTRIBUTES(flags)									\
/*TODO*///	machine->sound_attributes = (flags);								\
/*TODO*///
/*TODO*///
/*TODO*////* add/remove/replace sounds */
/*TODO*///#define MDRV_SOUND_ADD_TAG(tag, type, interface)						\
/*TODO*///	machine_add_sound(machine, (tag), SOUND_##type, &(interface));		\
/*TODO*///
/*TODO*///#define MDRV_SOUND_ADD(type, interface)									\
/*TODO*///	MDRV_SOUND_ADD_TAG(NULL, type, interface)							\
/*TODO*///
/*TODO*///#define MDRV_SOUND_REMOVE(tag)											\
/*TODO*///	machine_remove_sound(machine, tag);									\
/*TODO*///
/*TODO*///#define MDRV_SOUND_REPLACE(tag, type, interface)						\
/*TODO*///	{																	\
/*TODO*///		struct MachineSound *sound = machine_find_sound(machine, tag);	\
/*TODO*///		if (sound)														\
/*TODO*///		{																\
/*TODO*///			sound->sound_type = SOUND_##type;							\
/*TODO*///			sound->sound_interface = &(interface);						\
/*TODO*///		}																\
/*TODO*///	}																	\
/*TODO*///
/*TODO*///
/*TODO*///struct MachineCPU *machine_add_cpu(struct InternalMachineDriver *machine, const char *tag, int type, int cpuclock);
/*TODO*///struct MachineCPU *machine_find_cpu(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///void machine_remove_cpu(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///
/*TODO*///struct MachineSound *machine_add_sound(struct InternalMachineDriver *machine, const char *tag, int type, void *sndintf);
/*TODO*///struct MachineSound *machine_find_sound(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///void machine_remove_sound(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     *
     * Internal representation of a machine driver, built from the constructor
     *
     **************************************************************************
     */
    public static final int MAX_CPU = 8;/* MAX_CPU is the maximum number of CPUs which cpuintrf.c  can run at the same time. Currently, 8 is enough. */
    public static final int MAX_SOUND = 5;/* MAX_SOUND is the maximum number of sound subsystems which can run at the same time. Currently, 5 is enough. */

    public static class InternalMachineDriver {

        public MachineCPU cpu[] = MachineCPU.create(MAX_CPU);
        public float frames_per_second;
        public int vblank_duration;
        public int /*UINT32*/ cpu_slices_per_frame;

        public MachineInitHandlerPtr machine_init;
        public MachineStopHandlerPtr machine_stop;
        /*TODO*///	void (*nvram_handler)(mame_file *file, int read_or_write);

        public int /*UINT32*/ video_attributes;
        public int /*UINT32*/ aspect_x, aspect_y;
        public int screen_width, screen_height;
        public rectangle default_visible_area;
        public GfxDecodeInfo[] gfxdecodeinfo;
        public int /*UINT32*/ total_colors;
        public int /*UINT32*/ color_table_len;
        /*TODO*///
        public PaletteInitHandlerPtr init_palette;
        public VideoStartHandlerPtr video_start;
        /*TODO*///	void (*video_stop)(void);
/*TODO*///	void (*video_eof)(void);
        public VideoUpdateHandlerPtr video_update;

        public int /*UINT32*/ sound_attributes;
        public MachineSound sound[] = MachineSound.create(MAX_SOUND);
    }

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Machine driver constants and flags
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* VBlank is the period when the video beam is outside of the visible area and */
/*TODO*////* returns from the bottom to the top of the screen to prepare for a new video frame. */
/*TODO*////* VBlank duration is an important factor in how the game renders itself. MAME */
/*TODO*////* generates the vblank_interrupt, lets the game run for vblank_duration microseconds, */
/*TODO*////* and then updates the screen. This faithfully reproduces the behaviour of the real */
/*TODO*////* hardware. In many cases, the game does video related operations both in its vblank */
/*TODO*////* interrupt, and in the normal game code; it is therefore important to set up */
/*TODO*////* vblank_duration accurately to have everything properly in sync. An example of this */
/*TODO*////* is Commando: if you set vblank_duration to 0, therefore redrawing the screen BEFORE */
/*TODO*////* the vblank interrupt is executed, sprites will be misaligned when the screen scrolls. */
/*TODO*///
/*TODO*////* Here are some predefined, TOTALLY ARBITRARY values for vblank_duration, which should */
/*TODO*////* be OK for most cases. I have NO IDEA how accurate they are compared to the real */
/*TODO*////* hardware, they could be completely wrong. */
/*TODO*///#define DEFAULT_60HZ_VBLANK_DURATION 0
/*TODO*///#define DEFAULT_30HZ_VBLANK_DURATION 0
/*TODO*////* If you use IPT_VBLANK, you need a duration different from 0. */
/*TODO*///#define DEFAULT_REAL_60HZ_VBLANK_DURATION 2500
/*TODO*///#define DEFAULT_REAL_30HZ_VBLANK_DURATION 2500
/*TODO*///
/*TODO*///
/*TODO*////* ----- flags for video_attributes ----- */
/*TODO*///
/*TODO*////* bit 0 of the video attributes indicates raster or vector video hardware */
/*TODO*///#define	VIDEO_TYPE_RASTER			0x0000
/*TODO*///#define	VIDEO_TYPE_VECTOR			0x0001
/*TODO*///
/*TODO*////* bit 3 of the video attributes indicates that the game's palette has 6 or more bits */
/*TODO*////*       per gun, and would therefore require a 24-bit display. This is entirely up to */
/*TODO*////*       the OS dependant layer, the bitmap will still be 16-bit. */
/*TODO*///#define VIDEO_NEEDS_6BITS_PER_GUN	0x0008
/*TODO*///
/*TODO*////* ASG 980417 - added: */
/*TODO*////* bit 4 of the video attributes indicates that the driver wants its refresh after */
/*TODO*////*       the VBLANK instead of before. */
/*TODO*///#define	VIDEO_UPDATE_BEFORE_VBLANK	0x0000
/*TODO*///#define	VIDEO_UPDATE_AFTER_VBLANK	0x0010
/*TODO*///
/*TODO*////* In most cases we assume pixels are square (1:1 aspect ratio) but some games need */
/*TODO*////* different proportions, e.g. 1:2 for Blasteroids */
/*TODO*///#define VIDEO_PIXEL_ASPECT_RATIO_MASK 0x0060
/*TODO*///#define VIDEO_PIXEL_ASPECT_RATIO_1_1 0x0000
/*TODO*///#define VIDEO_PIXEL_ASPECT_RATIO_1_2 0x0020
/*TODO*///#define VIDEO_PIXEL_ASPECT_RATIO_2_1 0x0040
/*TODO*///
/*TODO*///#define VIDEO_DUAL_MONITOR			0x0080
/*TODO*///
/*TODO*////* Mish 181099:  See comments in vidhrdw/generic.c for details */
/*TODO*///#define VIDEO_BUFFERS_SPRITERAM		0x0100
/*TODO*///
/*TODO*////* game wants to use a hicolor or truecolor bitmap (e.g. for alpha blending) */
/*TODO*///#define VIDEO_RGB_DIRECT 			0x0200
/*TODO*///
/*TODO*////* automatically extend the palette creating a darker copy for shadows */
/*TODO*///#define VIDEO_HAS_SHADOWS			0x0400
/*TODO*///
/*TODO*////* automatically extend the palette creating a brighter copy for highlights */
/*TODO*///#define VIDEO_HAS_HIGHLIGHTS		0x0800
/*TODO*///
/*TODO*///
/*TODO*////* ----- flags for sound_attributes ----- */
/*TODO*///#define	SOUND_SUPPORTS_STEREO		0x0001
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Game driver structure
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///struct GameDriver
/*TODO*///{
/*TODO*///	const char *source_file;	/* set this to __FILE__ */
/*TODO*///	const struct GameDriver *clone_of;	/* if this is a clone, point to */
/*TODO*///										/* the main version of the game */
/*TODO*///	const char *name;
/*TODO*///	const struct SystemBios *bios;	/* if this system has alternate bios roms use this */
/*TODO*///									/* structure to list names and ROM_BIOSFLAGS. */
/*TODO*///	const char *description;
/*TODO*///	const char *year;
/*TODO*///	const char *manufacturer;
/*TODO*///	void (*drv)(struct InternalMachineDriver *);
/*TODO*///	const struct InputPortTiny *input_ports;
/*TODO*///	void (*driver_init)(void);	/* optional function to be called during initialization */
/*TODO*///								/* This is called ONCE, unlike Machine->init_machine */
/*TODO*///								/* which is called every time the game is reset. */
/*TODO*///
/*TODO*///	const struct RomModule *rom;
/*TODO*///#ifdef MESS
/*TODO*///	void (*sysconfig_ctor)(struct SystemConfigurationParamBlock *cfg);
/*TODO*///	const struct GameDriver *compatible_with;
/*TODO*///#endif
/*TODO*///
/*TODO*///	UINT32 flags;	/* orientation and other flags; see defines below */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Game driver flags
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* ----- values for the flags field ----- */
/*TODO*///
/*TODO*///#define ORIENTATION_MASK        	0x0007
/*TODO*///#define	ORIENTATION_FLIP_X			0x0001	/* mirror everything in the X direction */
/*TODO*///#define	ORIENTATION_FLIP_Y			0x0002	/* mirror everything in the Y direction */
/*TODO*///#define ORIENTATION_SWAP_XY			0x0004	/* mirror along the top-left/bottom-right diagonal */
/*TODO*///
/*TODO*///#define GAME_NOT_WORKING			0x0008
/*TODO*///#define GAME_UNEMULATED_PROTECTION	0x0010	/* game's protection not fully emulated */
/*TODO*///#define GAME_WRONG_COLORS			0x0020	/* colors are totally wrong */
/*TODO*///#define GAME_IMPERFECT_COLORS		0x0040	/* colors are not 100% accurate, but close */
/*TODO*///#define GAME_IMPERFECT_GRAPHICS		0x0080	/* graphics are wrong/incomplete */
/*TODO*///#define GAME_NO_COCKTAIL			0x0100	/* screen flip support is missing */
/*TODO*///#define GAME_NO_SOUND				0x0200	/* sound is missing */
/*TODO*///#define GAME_IMPERFECT_SOUND		0x0400	/* sound is known to be wrong */
/*TODO*///#define NOT_A_DRIVER				0x4000	/* set by the fake "root" driver_0 and by "containers" */
/*TODO*///											/* e.g. driver_neogeo. */
/*TODO*///#ifdef MESS
/*TODO*///#define GAME_COMPUTER               0x8000  /* Driver is a computer (needs full keyboard) */
/*TODO*///#define GAME_COMPUTER_MODIFIED      0x0800	/* Official? Hack */
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Macros for building game drivers
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#define GAME(YEAR,NAME,PARENT,MACHINE,INPUT,INIT,MONITOR,COMPANY,FULLNAME)	\
/*TODO*///extern const struct GameDriver driver_##PARENT;	\
/*TODO*///const struct GameDriver driver_##NAME =		\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	system_bios_0,							\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	construct_##MACHINE,					\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	MONITOR									\
/*TODO*///};
/*TODO*///
/*TODO*///#define GAMEX(YEAR,NAME,PARENT,MACHINE,INPUT,INIT,MONITOR,COMPANY,FULLNAME,FLAGS)	\
/*TODO*///extern const struct GameDriver driver_##PARENT;	\
/*TODO*///const struct GameDriver driver_##NAME =		\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	system_bios_0,							\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	construct_##MACHINE,					\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	(MONITOR)|(FLAGS)						\
/*TODO*///};
/*TODO*///
/*TODO*///#define GAMEB(YEAR,NAME,PARENT,BIOS,MACHINE,INPUT,INIT,MONITOR,COMPANY,FULLNAME)	\
/*TODO*///extern const struct GameDriver driver_##PARENT;	\
/*TODO*///const struct GameDriver driver_##NAME =		\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	system_bios_##BIOS,						\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	construct_##MACHINE,					\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	MONITOR									\
/*TODO*///};
/*TODO*///
/*TODO*///#define GAMEBX(YEAR,NAME,PARENT,BIOS,MACHINE,INPUT,INIT,MONITOR,COMPANY,FULLNAME,FLAGS)	\
/*TODO*///extern const struct GameDriver driver_##PARENT;	\
/*TODO*///const struct GameDriver driver_##NAME =		\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	system_bios_##BIOS,						\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	construct_##MACHINE,					\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	(MONITOR)|(FLAGS)						\
/*TODO*///};
/*TODO*///
/*TODO*////* monitor parameters to be used with the GAME() macro */
/*TODO*///#define	ROT0	0
/*TODO*///#define	ROT90	(ORIENTATION_SWAP_XY|ORIENTATION_FLIP_X)	/* rotate clockwise 90 degrees */
/*TODO*///#define	ROT180	(ORIENTATION_FLIP_X|ORIENTATION_FLIP_Y)		/* rotate 180 degrees */
/*TODO*///#define	ROT270	(ORIENTATION_SWAP_XY|ORIENTATION_FLIP_Y)	/* rotate counter-clockwise 90 degrees */
/*TODO*///
/*TODO*////* this allows to leave the INIT field empty in the GAME() macro call */
/*TODO*///#define init_0 0
/*TODO*///
/*TODO*////* this allows to leave the BIOS field empty in the GAMEB() macro call */
/*TODO*///#define system_bios_0 0
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Global variables
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///extern const struct GameDriver *drivers[];
/*TODO*///extern const struct GameDriver *test_drivers[];
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
