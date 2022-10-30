/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

public class paletteH {

    /*TODO*///struct mame_display;		/* declared elsewhere */
/*TODO*///
/*TODO*///typedef UINT32 pen_t;
/*TODO*///typedef UINT32 rgb_t;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	CONSTANTS
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///#define PALETTE_DEFAULT_SHADOW_FACTOR (0.6)
/*TODO*///#define PALETTE_DEFAULT_HIGHLIGHT_FACTOR (1/PALETTE_DEFAULT_SHADOW_FACTOR)
/*TODO*///
/*TODO*///#define PALETTE_DEFAULT_SHADOW_FACTOR32 (0.6)
/*TODO*///#define PALETTE_DEFAULT_HIGHLIGHT_FACTOR32 (1/PALETTE_DEFAULT_SHADOW_FACTOR32)
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	MACROS
/*TODO*///-------------------------------------------------*/
/*TODO*///
    public static int MAKE_RGB(int r, int g, int b) {
        return ((((r) & 0xff) << 16) | (((g) & 0xff) << 8) | ((b) & 0xff));
    }

    /*TODO*///#define MAKE_ARGB(a,r,g,b)	(MAKE_RGB(r,g,b) | (((a) & 0xff) << 24))
/*TODO*///#define RGB_ALPHA(rgb)		(((rgb) >> 24) & 0xff)
    public static int RGB_RED(int rgb) {
        return (((rgb) >> 16) & 0xff);
    }

    public static int RGB_GREEN(int rgb) {
        return (((rgb) >> 8) & 0xff);
    }

    public static int RGB_BLUE(int rgb) {
        return ((rgb) & 0xff);
    }

}
