/*************************************************************************

	Atari Night Driver hardware

*************************************************************************/

/*----------- defined in machine/nitedrvr.c -----------*/

extern unsigned char *nitedrvr_ram;



void nitedrvr_crash_toggle(int dummy);

/*----------- defined in vidhrdw/nitedrvr.c -----------*/

extern unsigned char *nitedrvr_hvc;
VIDEO_UPDATE( nitedrvr );
