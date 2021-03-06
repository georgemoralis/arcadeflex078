/***************************************************************************

							-= Seta Hardware =-

***************************************************************************/

/* Variables and functions defined in drivers/seta.c */

void seta_coin_lockout_w(int data);


/* Variables and functions defined in vidhrdw/seta.c */

extern data16_t *seta_vram_0, *seta_vctrl_0;
extern data16_t *seta_vram_2, *seta_vctrl_2;
extern data16_t *seta_vregs;

extern data16_t *seta_workram; // Needed for zombraid Crosshair hack


WRITE16_HANDLER( twineagl_tilebank_w );

WRITE16_HANDLER( seta_vram_0_w );
WRITE16_HANDLER( seta_vram_2_w );
WRITE16_HANDLER( seta_vregs_w );

PALETTE_INIT( blandia );
PALETTE_INIT( gundhara );
PALETTE_INIT( jjsquawk );
PALETTE_INIT( usclssic );
PALETTE_INIT( zingzip );

VIDEO_START( seta_no_layers);
VIDEO_START( twineagl_1_layer);
VIDEO_START( seta_1_layer);
VIDEO_START( seta_2_layers);
VIDEO_START( oisipuzl_2_layers );

VIDEO_UPDATE( seta );
VIDEO_UPDATE( seta_no_layers );


/* Variables and functions defined in vidhrdw/seta2.c */

extern data16_t *seta2_vregs;

WRITE16_HANDLER( seta2_vregs_w );

VIDEO_START( seta2 );
VIDEO_START( seta2_offset );
VIDEO_UPDATE( seta2 );
VIDEO_EOF( seta2 );


/* Variables and functions defined in sndhrdw/seta.c */
#define	__uPD71054_TIMER	1


/* Variables and functions defined in vidhrdw/ssv.c */

extern data16_t *ssv_scroll;




READ16_HANDLER( ssv_vblank_r );
WRITE16_HANDLER( ssv_scroll_w );
WRITE16_HANDLER( paletteram16_xrgb_swap_word_w );
void ssv_enable_video(int enable);

VIDEO_START( ssv );
VIDEO_UPDATE( ssv );
