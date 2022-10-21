/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.v078.mame;

public class osdependH
{
	
/*TODO*///	#ifdef __cplusplus
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/* The Win32 port requires this constant for variable arg routines. */
/*TODO*///	#ifndef CLIB_DECL
/*TODO*///	#define CLIB_DECL
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef __LP64__
/*TODO*///	#define FPTR unsigned long   /* 64bit: sizeof(void *) is sizeof(long)  */
/*TODO*///	#else
/*TODO*///	#define FPTR unsigned int
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	int osd_init(void);
/*TODO*///	void osd_exit(void);
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		Display
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	/* mame_bitmap used to be declared here, but has moved to common.c */
/*TODO*///	/* sadly, the include order requires that at least this forward declaration is here */
/*TODO*///	struct mame_bitmap;
/*TODO*///	struct mame_display;
/*TODO*///	struct performance_info;
/*TODO*///	struct rectangle;
/*TODO*///	struct rom_load_data;
/*TODO*///	
/*TODO*///	
/*TODO*///	/* these are the parameters passed into osd_create_display */
/*TODO*///	struct osd_create_params
/*TODO*///	{
/*TODO*///		int width, height;			/* width and height */
/*TODO*///		int aspect_x, aspect_y;		/* aspect ratio X:Y */
/*TODO*///		int depth;					/* depth, either 16(palette), 15(RGB) or 32(RGB) */
/*TODO*///		int colors;					/* colors in the palette (including UI) */
/*TODO*///		float fps;					/* frame rate */
/*TODO*///		int video_attributes;		/* video flags from driver */
/*TODO*///		int orientation;			/* orientation requested by the user */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Create a display screen, or window, of the given dimensions (or larger). It is
/*TODO*///	  acceptable to create a smaller display if necessary, in that case the user must
/*TODO*///	  have a way to move the visibility window around.
/*TODO*///	
/*TODO*///	  The params contains all the information the
/*TODO*///	  Attributes are the ones defined in driver.h, they can be used to perform
/*TODO*///	  optimizations, e.g. dirty rectangle handling if the game supports it, or faster
/*TODO*///	  blitting routines with fixed palette if the game doesn't change the palette at
/*TODO*///	  run time. The VIDEO_PIXEL_ASPECT_RATIO flags should be honored to produce a
/*TODO*///	  display of correct proportions.
/*TODO*///	  Orientation is the screen orientation (as defined in driver.h) which will be done
/*TODO*///	  by the core. This can be used to select thinner screen modes for vertical games
/*TODO*///	  (ORIENTATION_SWAP_XY set), or even to ask the user to rotate the monitor if it's
/*TODO*///	  a pivot model. Note that the OS dependent code must NOT perform any rotation,
/*TODO*///	  this is done entirely in the core.
/*TODO*///	  Depth can be 8 or 16 for palettized modes, meaning that the core will store in the
/*TODO*///	  bitmaps logical pens which will have to be remapped through a palette at blit time,
/*TODO*///	  and 15 or 32 for direct mapped modes, meaning that the bitmaps will contain RGB
/*TODO*///	  triplets (555 or 888). For direct mapped modes, the VIDEO_RGB_DIRECT flag is set
/*TODO*///	  in the attributes field.
/*TODO*///	
/*TODO*///	  Returns 0 on success.
/*TODO*///	*/
/*TODO*///	int osd_create_display(const struct osd_create_params *params, UINT32 *rgb_components);
/*TODO*///	void osd_close_display(void);
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  osd_skip_this_frame() must return 0 if the current frame will be displayed.
/*TODO*///	  This can be used by drivers to skip cpu intensive processing for skipped
/*TODO*///	  frames, so the function must return a consistent result throughout the
/*TODO*///	  current frame. The function MUST NOT check timers and dynamically determine
/*TODO*///	  whether to display the frame: such calculations must be done in
/*TODO*///	  osd_update_video_and_audio(), and they must affect the FOLLOWING frames, not
/*TODO*///	  the current one. At the end of osd_update_video_and_audio(), the code must
/*TODO*///	  already know exactly whether the next frame will be skipped or not.
/*TODO*///	*/
/*TODO*///	int osd_skip_this_frame(void);
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Update video and audio. game_bitmap contains the game display, while
/*TODO*///	  debug_bitmap an image of the debugger window (if the debugger is active; NULL
/*TODO*///	  otherwise). They can be shown one at a time, or in two separate windows,
/*TODO*///	  depending on the OS limitations. If only one is shown, the user must be able
/*TODO*///	  to toggle between the two by pressing IPT_UI_TOGGLE_DEBUG; moreover,
/*TODO*///	  osd_debugger_focus() will be used by the core to force the display of a
/*TODO*///	  specific bitmap, e.g. the debugger one when the debugger becomes active.
/*TODO*///	
/*TODO*///	  leds_status is a bitmask of lit LEDs, usually player start lamps. They can be
/*TODO*///	  simulated using the keyboard LEDs, or in other ways e.g. by placing graphics
/*TODO*///	  on the window title bar.
/*TODO*///	*/
/*TODO*///	void osd_update_video_and_audio(struct mame_display *display);
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Provides a hook to allow the OSD system to override processing of a
/*TODO*///	  snapshot.  This function will either return a new bitmap, for which the
/*TODO*///	  caller is responsible for freeing.
/*TODO*///	*/
/*TODO*///	struct mame_bitmap *osd_override_snapshot(struct mame_bitmap *bitmap, struct rectangle *bounds);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Returns a pointer to the text to display when the FPS display is toggled.
/*TODO*///	  This normally includes information about the frameskip, FPS, and percentage
/*TODO*///	  of full game speed.
/*TODO*///	*/
/*TODO*///	const char *osd_get_fps_text(const struct performance_info *performance);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		Sound
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  osd_start_audio_stream() is called at the start of the emulation to initialize
/*TODO*///	  the output stream, then osd_update_audio_stream() is called every frame to
/*TODO*///	  feed new data. osd_stop_audio_stream() is called when the emulation is stopped.
/*TODO*///	
/*TODO*///	  The sample rate is fixed at Machine->sample_rate. Samples are 16-bit, signed.
/*TODO*///	  When the stream is stereo, left and right samples are alternated in the
/*TODO*///	  stream.
/*TODO*///	
/*TODO*///	  osd_start_audio_stream() and osd_update_audio_stream() must return the number
/*TODO*///	  of samples (or couples of samples, when using stereo) required for next frame.
/*TODO*///	  This will be around Machine->sample_rate / Machine->drv->frames_per_second,
/*TODO*///	  the code may adjust it by SMALL AMOUNTS to keep timing accurate and to
/*TODO*///	  maintain audio and video in sync when using vsync. Note that sound emulation,
/*TODO*///	  especially when DACs are involved, greatly depends on the number of samples
/*TODO*///	  per frame to be roughly constant, so the returned value must always stay close
/*TODO*///	  to the reference value of Machine->sample_rate / Machine->drv->frames_per_second.
/*TODO*///	  Of course that value is not necessarily an integer so at least a +/- 1
/*TODO*///	  adjustment is necessary to avoid drifting over time.
/*TODO*///	*/
/*TODO*///	int osd_start_audio_stream(int stereo);
/*TODO*///	int osd_update_audio_stream(INT16 *buffer);
/*TODO*///	void osd_stop_audio_stream(void);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  control master volume. attenuation is the attenuation in dB (a negative
/*TODO*///	  number). To convert from dB to a linear volume scale do the following:
/*TODO*///		volume = MAX_VOLUME;
/*TODO*///		while (attenuation++ < 0)
/*TODO*///			volume /= 1.122018454;		//	= (10 ^ (1/20)) = 1dB
/*TODO*///	*/
/*TODO*///	void osd_set_mastervolume(int attenuation);
/*TODO*///	int osd_get_mastervolume(void);
/*TODO*///	
/*TODO*///	void osd_sound_enable(int enable);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		Keyboard
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  return a list of all available keys (see input.h)
/*TODO*///	*/
/*TODO*///	const struct KeyboardInfo *osd_get_key_list(void);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  tell whether the specified key is pressed or not. keycode is the OS dependent
/*TODO*///	  code specified in the list returned by osd_get_key_list().
/*TODO*///	*/
/*TODO*///	int osd_is_key_pressed(int keycode);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Return the Unicode value of the most recently pressed key. This
/*TODO*///	  function is used only by text-entry routines in the user interface and should
/*TODO*///	  not be used by drivers. The value returned is in the range of the first 256
/*TODO*///	  bytes of Unicode, e.g. ISO-8859-1. A return value of 0 indicates no key down.
/*TODO*///	
/*TODO*///	  Set flush to 1 to clear the buffer before entering text. This will avoid
/*TODO*///	  having prior UI and game keys leak into the text entry.
/*TODO*///	*/
/*TODO*///	int osd_readkey_unicode(int flush);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		Joystick & Mouse/Trackball
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  return a list of all available joystick inputs (see input.h)
/*TODO*///	*/
/*TODO*///	const struct JoystickInfo *osd_get_joy_list(void);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  tell whether the specified joystick direction/button is pressed or not.
/*TODO*///	  joycode is the OS dependent code specified in the list returned by
/*TODO*///	  osd_get_joy_list().
/*TODO*///	*/
/*TODO*///	int osd_is_joy_pressed(int joycode);
	
	
	/* We support 4 players for each analog control / trackball */
	public static final int OSD_MAX_JOY_ANALOG	= 4;
	public static final int X_AXIS			= 0;
        public static final int Y_AXIS			= 1;
	public static final int Z_AXIS			= 2;
	public static final int PEDAL_AXIS		= 3;
	public static final int MAX_ANALOG_AXES         = 4;
	
/*TODO*///	/* added for building joystick seq for analog inputs */
/*TODO*///	int osd_is_joystick_axis_code(int joycode);
/*TODO*///	
/*TODO*///	/* Joystick calibration routines BW 19981216 */
/*TODO*///	/* Do we need to calibrate the joystick at all? */
/*TODO*///	int osd_joystick_needs_calibration(void);
/*TODO*///	/* Preprocessing for joystick calibration. Returns 0 on success */
/*TODO*///	void osd_joystick_start_calibration(void);
/*TODO*///	/* Prepare the next calibration step. Return a description of this step. */
/*TODO*///	/* (e.g. "move to upper left") */
/*TODO*///	const char *osd_joystick_calibrate_next(void);
/*TODO*///	/* Get the actual joystick calibration data for the current position */
/*TODO*///	void osd_joystick_calibrate(void);
/*TODO*///	/* Postprocessing (e.g. saving joystick data to config) */
/*TODO*///	void osd_joystick_end_calibration(void);
/*TODO*///	
/*TODO*///	void osd_lightgun_read(int player, int *deltax, int *deltay);
/*TODO*///	void osd_trak_read(int player, int *deltax, int *deltay);
/*TODO*///	
/*TODO*///	/* return values in the range -128 .. 128 (yes, 128, not 127) */
/*TODO*///	void osd_analogjoy_read(int player,int analog_axis[MAX_ANALOG_AXES], InputCode analogjoy_input[MAX_ANALOG_AXES]);
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  inptport.c defines some general purpose defaults for key and joystick bindings.
/*TODO*///	  They may be further adjusted by the OS dependent code to better match the
/*TODO*///	  available keyboard, e.g. one could map pause to the Pause key instead of P, or
/*TODO*///	  snapshot to PrtScr instead of F12. Of course the user can further change the
/*TODO*///	  settings to anything he/she likes.
/*TODO*///	  This function is called on startup, before reading the configuration from disk.
/*TODO*///	  Scan the list, and change the keys/joysticks you want.
/*TODO*///	*/
/*TODO*///	void osd_customize_inputport_defaults(struct ipd *defaults);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		File I/O
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	/* inp header */
/*TODO*///	typedef struct
/*TODO*///	{
/*TODO*///		char name[9];      /* 8 bytes for game->name + NUL */
/*TODO*///		char version[3];   /* byte[0] = 0, byte[1] = version byte[2] = beta_version */
/*TODO*///		char reserved[20]; /* for future use, possible store game options? */
/*TODO*///	} INP_HEADER;
/*TODO*///	
/*TODO*///	
/*TODO*///	typedef struct _osd_file osd_file;
/*TODO*///	
/*TODO*///	
/*TODO*///	/* These values are returned by osd_get_path_info */
/*TODO*///	enum
/*TODO*///	{
/*TODO*///		PATH_NOT_FOUND,
/*TODO*///		PATH_IS_FILE,
/*TODO*///		PATH_IS_DIRECTORY
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Return the number of paths for a given type */
/*TODO*///	int osd_get_path_count(int pathtype);
/*TODO*///	
/*TODO*///	/* Get information on the existence of a file */
/*TODO*///	int osd_get_path_info(int pathtype, int pathindex, const char *filename);
/*TODO*///	
/*TODO*///	/* Attempt to open a file with the given name and mode using the specified path type */
/*TODO*///	osd_file *osd_fopen(int pathtype, int pathindex, const char *filename, const char *mode);
/*TODO*///	
/*TODO*///	/* Seek within a file */
/*TODO*///	int osd_fseek(osd_file *file, INT64 offset, int whence);
/*TODO*///	
/*TODO*///	/* Return current file position */
/*TODO*///	UINT64 osd_ftell(osd_file *file);
/*TODO*///	
/*TODO*///	/* Return 1 if we're at the end of file */
/*TODO*///	int osd_feof(osd_file *file);
/*TODO*///	
/*TODO*///	/* Read bytes from a file */
/*TODO*///	UINT32 osd_fread(osd_file *file, void *buffer, UINT32 length);
/*TODO*///	
/*TODO*///	/* Write bytes to a file */
/*TODO*///	UINT32 osd_fwrite(osd_file *file, const void *buffer, UINT32 length);
/*TODO*///	
/*TODO*///	/* Close an open file */
/*TODO*///	void osd_fclose(osd_file *file);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		Timing
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	typedef INT64 cycles_t;
/*TODO*///	
/*TODO*///	/* return the current number of cycles, or some other high-resolution timer */
/*TODO*///	cycles_t osd_cycles(void);
/*TODO*///	
/*TODO*///	/* return the number of cycles per second */
/*TODO*///	cycles_t osd_cycles_per_second(void);
/*TODO*///	
/*TODO*///	/* return the current number of cycles, or some other high-resolution timer.
/*TODO*///	   This call must be the fastest possible because it is called by the profiler;
/*TODO*///	   it isn't necessary to know the number of ticks per seconds. */
/*TODO*///	cycles_t osd_profiling_ticks(void);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/******************************************************************************
/*TODO*///	
/*TODO*///		Miscellaneous
/*TODO*///	
/*TODO*///	******************************************************************************/
/*TODO*///	
/*TODO*///	/* called while loading ROMs. It is called a last time with name == 0 to signal */
/*TODO*///	/* that the ROM loading process is finished. */
/*TODO*///	/* return non-zero to abort loading */
/*TODO*///	int osd_display_loading_rom_message(const char *name,struct rom_load_data *romdata);
/*TODO*///	
/*TODO*///	/* called when the game is paused/unpaused, so the OS dependent code can do special */
/*TODO*///	/* things like changing the title bar or darkening the display. */
/*TODO*///	/* Note that the OS dependent code must NOT stop processing input, since the user */
/*TODO*///	/* interface is still active while the game is paused. */
/*TODO*///	void osd_pause(int paused);
/*TODO*///	
/*TODO*///	/* aborts the program in some unexpected fatal way */
/*TODO*///	#ifdef __GNUC__
/*TODO*///	void CLIB_DECL osd_die(const char *text,...)
/*TODO*///	      __attribute__ ((format (printf, 1, 2)));
/*TODO*///	#else
/*TODO*///	void CLIB_DECL osd_die(const char *text,...);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	#if defined MAME_NET || defined XMAME_NET
/*TODO*///	/* network */
/*TODO*///	int osd_net_init(void);
/*TODO*///	#ifdef XMAME_NET
/*TODO*///	int osd_net_active(void);
/*TODO*///	#endif
/*TODO*///	int osd_net_send(int player, unsigned char buf[], int *size);
/*TODO*///	int osd_net_recv(int player, unsigned char buf[], int *size);
/*TODO*///	#ifdef MAME_NET
/*TODO*///	int osd_net_sync(void);
/*TODO*///	#elif defined XMAME_NET
/*TODO*///	void osd_net_sync(unsigned short input_port_values[MAX_INPUT_PORTS],
/*TODO*///			unsigned short input_port_defaults[MAX_INPUT_PORTS]);
/*TODO*///	#endif
/*TODO*///	int osd_net_input_sync(void);
/*TODO*///	int osd_net_exit(void);
/*TODO*///	int osd_net_add_player(void);
/*TODO*///	int osd_net_remove_player(int player);
/*TODO*///	int osd_net_game_init(void);
/*TODO*///	int osd_net_game_exit(void);
/*TODO*///	#endif /* MAME_NET */
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///	/* this is here to follow the current mame file hierarchy style */
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef __GNUC__
/*TODO*///	void CLIB_DECL logerror(const char *text,...)
/*TODO*///	      __attribute__ ((format (printf, 1, 2)));
/*TODO*///	#else
/*TODO*///	void CLIB_DECL logerror(const char *text,...);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef __cplusplus
/*TODO*///	}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#endif
}
