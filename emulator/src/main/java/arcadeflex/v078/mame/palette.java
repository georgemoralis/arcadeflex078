/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

import arcadeflex.v078.generic.funcPtr.PaletteInitHandlerPtr;
import arcadeflex.v078.generic.funcPtr.ReadHandlerPtr;
import static arcadeflex.v078.mame.paletteH.*;
import static arcadeflex036.osdepend.logerror;
import static common.libc.expressions.sizeof;
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import static mame056.driverH.VIDEO_HAS_HIGHLIGHTS;
import static mame056.driverH.VIDEO_HAS_SHADOWS;
import static mame056.driverH.VIDEO_RGB_DIRECT;
import static mame056.mame.Machine;

public class palette {

    /*TODO*///#define VERBOSE 0
/*TODO*///

    /*-------------------------------------------------
	CONSTANTS
    -------------------------------------------------*/
    public static final int PEN_BRIGHTNESS_BITS = 8;
    public static final int MAX_PEN_BRIGHTNESS = (4 << PEN_BRIGHTNESS_BITS);

    public static final int PALETTIZED_16BIT = 0;
    public static final int DIRECT_15BIT = 1;
    public static final int DIRECT_32BIT = 2;
    public static final int DIRECT_RGB = DIRECT_15BIT | DIRECT_32BIT;

    /*TODO*////*-------------------------------------------------
/*TODO*///	GLOBAL VARIABLES
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///UINT32 direct_rgb_components[3];
/*TODO*///UINT16 *palette_shadow_table;
/*TODO*///
    public static UBytePtr paletteram = new UBytePtr();
    public static UBytePtr paletteram_2 = new UBytePtr();/* use when palette RAM is split in two parts */
 /*TODO*///data16_t *paletteram16;
/*TODO*///data16_t *paletteram16_2;
/*TODO*///data32_t *paletteram32;
/*TODO*///
    /*-------------------------------------------------
	LOCAL VARIABLES
    -------------------------------------------------*/
    static int[]/*rgb_t*/ game_palette;/* RGB palette as set by the driver */
    static int[]/*rgb_t*/ adjusted_palette;/* actual RGB palette after brightness/gamma adjustments */
    static int[] /*UINT32*/ dirty_palette;
    static char[] pen_brightness;

    static int/*UINT8*/ adjusted_palette_dirty;

    /*TODO*///static UINT16 shadow_factor, highlight_factor;
    static double global_brightness, global_brightness_adjust, global_gamma;

    static int/*UINT8*/ colormode, highlight_method;
    static int/*pen_t*/ total_colors;

    static int/*pen_t*/ total_colors_with_ui;

    static int[] color_correct_table = new int[(MAX_PEN_BRIGHTNESS * MAX_PEN_BRIGHTNESS) >> PEN_BRIGHTNESS_BITS];

    /*TODO*////*-------------------------------------------------
/*TODO*///	rgb_to_direct15 - convert an RGB triplet to
/*TODO*///	a 15-bit OSD-specified RGB value
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///INLINE UINT16 rgb_to_direct15(rgb_t rgb)
/*TODO*///{
/*TODO*///	return  (  RGB_RED(rgb) >> 3) * (direct_rgb_components[0] / 0x1f) +
/*TODO*///			(RGB_GREEN(rgb) >> 3) * (direct_rgb_components[1] / 0x1f) +
/*TODO*///			( RGB_BLUE(rgb) >> 3) * (direct_rgb_components[2] / 0x1f);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	rgb_to_direct32 - convert an RGB triplet to
/*TODO*///	a 32-bit OSD-specified RGB value
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///INLINE UINT32 rgb_to_direct32(rgb_t rgb)
/*TODO*///{
/*TODO*///	return    RGB_RED(rgb) * (direct_rgb_components[0] / 0xff) +
/*TODO*///			RGB_GREEN(rgb) * (direct_rgb_components[1] / 0xff) +
/*TODO*///			 RGB_BLUE(rgb) * (direct_rgb_components[2] / 0xff);
/*TODO*///}
/*TODO*///
/*TODO*///

    /*-------------------------------------------------
	adjust_palette_entry - adjust a palette
	entry for brightness and gamma
    -------------------------------------------------*/
    static int/*rgb_t*/ adjust_palette_entry(int/*rgb_t*/ entry, int pen_bright) {
        int r = color_correct_table[(RGB_RED(entry) * pen_bright) >> PEN_BRIGHTNESS_BITS];
        int g = color_correct_table[(RGB_GREEN(entry) * pen_bright) >> PEN_BRIGHTNESS_BITS];
        int b = color_correct_table[(RGB_BLUE(entry) * pen_bright) >> PEN_BRIGHTNESS_BITS];
        return MAKE_RGB(r, g, b);
    }

    /*-------------------------------------------------
	mark_pen_dirty - mark a given pen index dirty
    -------------------------------------------------*/
    static void mark_pen_dirty(int pen) {
        dirty_palette[pen / 32] |= 1 << (pen % 32);
    }

    /*-------------------------------------------------
	palette_start - palette initialization that
	takes place before the display is created
    -------------------------------------------------*/
    public static int palette_start() {
        /* init statics */
        adjusted_palette_dirty = 1;
        /*TODO*///	debug_palette_dirty = 1;
/*TODO*///
/*TODO*///	shadow_factor = (int)(PALETTE_DEFAULT_SHADOW_FACTOR * (double)(1 << PEN_BRIGHTNESS_BITS));
/*TODO*///	highlight_factor = (int)(PALETTE_DEFAULT_HIGHLIGHT_FACTOR * (double)(1 << PEN_BRIGHTNESS_BITS));
        global_brightness = 1.0;
        /*TODO*///(options.brightness > .001) ? options.brightness : 1.0;
        global_brightness_adjust = 1.0;
        global_gamma = 1.0;
        /*TODO*///(options.gamma > .001) ? options.gamma : 1.0;

        /* determine the color mode */
        if (Machine.color_depth == 15) {
            colormode = DIRECT_15BIT;
        } else if (Machine.color_depth == 32) {
            colormode = DIRECT_32BIT;
        } else {
            colormode = PALETTIZED_16BIT;
        }

        highlight_method = 0;

        /* ensure that RGB direct video modes don't have a colortable */
        if ((Machine.drv.video_attributes & VIDEO_RGB_DIRECT) != 0
                && Machine.drv.color_table_len != 0) {
            logerror("Error: VIDEO_RGB_DIRECT requires color_table_len to be 0.\n");
            return 1;
        }

        /* compute the total colors, including shadows and highlights */
        total_colors = Machine.drv.total_colors;
        if ((Machine.drv.video_attributes & VIDEO_HAS_SHADOWS) != 0 && (colormode & DIRECT_RGB) == 0) {
            total_colors += Machine.drv.total_colors;
        }
        if ((Machine.drv.video_attributes & VIDEO_HAS_HIGHLIGHTS) != 0 && (colormode & DIRECT_RGB) == 0) {
            total_colors += Machine.drv.total_colors;
        }
        total_colors_with_ui = total_colors;

        /* make sure we still fit in 16 bits */
        if (total_colors > 65536) {
            logerror("Error: palette has more than 65536 colors.\n");
            return 1;
        }

        /* allocate all the data structures */
        if (palette_alloc() != 0) {
            return 1;
        }

        /*TODO*///	/* set up save/restore of the palette */
/*TODO*///	state_save_register_UINT32("palette", 0, "colors", game_palette, total_colors);
/*TODO*///	state_save_register_UINT16("palette", 0, "brightness", pen_brightness, Machine->drv->total_colors);
/*TODO*///	state_save_register_func_postload(palette_reset);
        return 0;
    }

    /*TODO*///#define MAX_SHADOW_PRESETS 4
/*TODO*///
/*TODO*///static UINT32 *shadow_table_base[MAX_SHADOW_PRESETS];
/*TODO*///
/*TODO*///
/*TODO*///static void internal_set_shadow_preset(int mode, double factor, int dr, int dg, int db, int noclip, int style, int init)
/*TODO*///{
/*TODO*///#define FP 16
/*TODO*///#define FMAX (0x1f<<FP)
/*TODO*///
/*TODO*///	static double oldfactor[MAX_SHADOW_PRESETS] = {-1,-1,-1,-1};
/*TODO*///	static int oldRGB[MAX_SHADOW_PRESETS][3] = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
/*TODO*///	static int oldclip;
/*TODO*///
/*TODO*///	UINT32 *table_ptr32;
/*TODO*///	int i, fl, ov, r, g, b, d32;
/*TODO*///
/*TODO*///	if (mode < 0 || mode >= MAX_SHADOW_PRESETS) return;
/*TODO*///
/*TODO*///	if ((table_ptr32 = shadow_table_base[mode]) == NULL) return;
/*TODO*///
/*TODO*///	if (style) // monotone shadows(style 1) or highlights(style 2)
/*TODO*///	{
/*TODO*///		if (factor < 0) factor = 0;
/*TODO*///
/*TODO*///		if (!init && oldfactor[mode] == factor) return;
/*TODO*///
/*TODO*///		oldfactor[mode] = factor;
/*TODO*///		oldRGB[mode][2] = oldRGB[mode][1] = oldRGB[mode][0] = -1;
/*TODO*///
/*TODO*///		if (!(colormode & DIRECT_RGB))
/*TODO*///		{
/*TODO*///			switch (style)
/*TODO*///			{
/*TODO*///				// modify shadows(first upper palette)
/*TODO*///				case 1:
/*TODO*///					palette_set_shadow_factor(factor);
/*TODO*///				break;
/*TODO*///
/*TODO*///				// modify highlights(second upper palette)
/*TODO*///				case 2:
/*TODO*///					palette_set_highlight_factor(factor);
/*TODO*///				break;
/*TODO*///
/*TODO*///				default: return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			d32 = (colormode == DIRECT_32BIT);
/*TODO*///
/*TODO*///			if (factor <= 1.0)
/*TODO*///			{
/*TODO*///				fl = (int)(factor * (1<<FP));
/*TODO*///
/*TODO*///				for (i=0; i<32768; i++)
/*TODO*///				{
/*TODO*///					r = (i & 0x7c00) * fl;
/*TODO*///					g = (i & 0x03e0) * fl;
/*TODO*///					b = (i & 0x001f) * fl;
/*TODO*///
/*TODO*///					r = r>>FP & 0x7c00;
/*TODO*///					g = g>>FP & 0x03e0;
/*TODO*///					b = b>>FP & 0x001f;
/*TODO*///
/*TODO*///					if (d32)
/*TODO*///						table_ptr32[i] = (UINT32)(r<<9 | g<<6 | b<<3);
/*TODO*///					else
/*TODO*///						((UINT16*)table_ptr32)[i] = (UINT16)(r | g | b);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (highlight_method == 0)
/*TODO*///				{
/*TODO*///					fl = (int)(factor * (1<<FP));
/*TODO*///
/*TODO*///					for (i=0; i<32768; i++)
/*TODO*///					{
/*TODO*///						r = (i>>10 & 0x1f) * fl;
/*TODO*///						g = (i>>5  & 0x1f) * fl;
/*TODO*///						b = (i     & 0x1f) * fl;
/*TODO*///
/*TODO*///						if (r >= FMAX) r = 0x7c00; else r = r>>(FP-10) & 0x7c00;
/*TODO*///						if (g >= FMAX) g = 0x03e0; else g = g>>(FP-5)  & 0x03e0;
/*TODO*///						if (b >= FMAX) b = 0x001f; else b = b>>(FP);
/*TODO*///
/*TODO*///						if (d32)
/*TODO*///							table_ptr32[i] = (UINT32)(r<<9 | g<<6 | b<<3);
/*TODO*///						else
/*TODO*///							((UINT16*)table_ptr32)[i] = (UINT16)(r | g | b);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else if (highlight_method == 1)
/*TODO*///				{
/*TODO*///					fl = (int)(factor * (1<<FP));
/*TODO*///
/*TODO*///					for (i=0; i<32768; i++)
/*TODO*///					{
/*TODO*///						r = (i>>10 & 0x1f) * fl;
/*TODO*///						g = (i>>5  & 0x1f) * fl;
/*TODO*///						b = (i     & 0x1f) * fl;
/*TODO*///						ov = 0;
/*TODO*///
/*TODO*///						if (r > FMAX) ov += r - FMAX;
/*TODO*///						if (g > FMAX) ov += g - FMAX;
/*TODO*///						if (b > FMAX) ov += b - FMAX;
/*TODO*///
/*TODO*///						if (ov) { ov >>= 2;  r += ov;  g += ov;  b += ov; }
/*TODO*///
/*TODO*///						if (r >= FMAX) r = 0x7c00; else r = r>>(FP-10) & 0x7c00;
/*TODO*///						if (g >= FMAX) g = 0x03e0; else g = g>>(FP-5)  & 0x03e0;
/*TODO*///						if (b >= FMAX) b = 0x001f; else b = b>>(FP);
/*TODO*///
/*TODO*///						if (d32)
/*TODO*///							table_ptr32[i] = (UINT32)(r<<9 | g<<6 | b<<3);
/*TODO*///						else
/*TODO*///							((UINT16*)table_ptr32)[i] = (UINT16)(r | g | b);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					fl = (int)(factor * 31 - 31);
/*TODO*///					dr = fl<<10;
/*TODO*///					dg = fl<<5;
/*TODO*///					db = fl;
/*TODO*///
/*TODO*///					for (i=0; i<32768; i++)
/*TODO*///					{
/*TODO*///						r = (i & 0x7c00) + dr;
/*TODO*///						g = (i & 0x03e0) + dg;
/*TODO*///						b = (i & 0x001f) + db;
/*TODO*///
/*TODO*///						if (r > 0x7c00) r = 0x7c00;
/*TODO*///						if (g > 0x03e0) g = 0x03e0;
/*TODO*///						if (b > 0x001f) b = 0x001f;
/*TODO*///
/*TODO*///						if (d32)
/*TODO*///							table_ptr32[i] = (UINT32)(r<<9 | g<<6 | b<<3);
/*TODO*///						else
/*TODO*///							((UINT16*)table_ptr32)[i] = (UINT16)(r | g | b);
/*TODO*///					}
/*TODO*///				} // end of highlight_methods
/*TODO*///			} // end of factor
/*TODO*///		} // end of colormode
/*TODO*///
/*TODO*///		#if VERBOSE
/*TODO*///			usrintf_showmessage("shadow %d recalc factor:%1.2f style:%d", mode, factor, style);
/*TODO*///		#endif
/*TODO*///	}
/*TODO*///	else // color shadows or highlights(style 0)
/*TODO*///	{
/*TODO*///		if (!(colormode & DIRECT_RGB)) return;
/*TODO*///
/*TODO*///		if (dr < -0xff) dr = -0xff; else if (dr > 0xff) dr = 0xff;
/*TODO*///		if (dg < -0xff) dg = -0xff; else if (dg > 0xff) dg = 0xff;
/*TODO*///		if (db < -0xff) db = -0xff; else if (db > 0xff) db = 0xff;
/*TODO*///		dr >>= 3; dg >>= 3; db >>= 3;
/*TODO*///
/*TODO*///		if (!init && oldclip==noclip && oldRGB[mode][0]==dr && oldRGB[mode][1]==dg && oldRGB[mode][2]==db) return;
/*TODO*///
/*TODO*///		oldclip = noclip;
/*TODO*///		oldRGB[mode][0] = dr; oldRGB[mode][1] = dg; oldRGB[mode][2] = db;
/*TODO*///		oldfactor[mode] = -1;
/*TODO*///
/*TODO*///		#if VERBOSE
/*TODO*///			usrintf_showmessage("shadow %d recalc %d %d %d %02x", mode, dr, dg, db, noclip);
/*TODO*///		#endif
/*TODO*///
/*TODO*///		dr <<= 10; dg <<= 5;
/*TODO*///		d32 = (colormode == DIRECT_32BIT);
/*TODO*///
/*TODO*///		if (noclip)
/*TODO*///		{
/*TODO*///			for (i=0; i<32768; i++)
/*TODO*///			{
/*TODO*///				r = (i & 0x7c00) + dr;
/*TODO*///				g = (i & 0x03e0) + dg;
/*TODO*///				b = (i & 0x001f) + db;
/*TODO*///
/*TODO*///				r &= 0x7c00;
/*TODO*///				g &= 0x03e0;
/*TODO*///				b &= 0x001f;
/*TODO*///
/*TODO*///				if (d32)
/*TODO*///					table_ptr32[i] = (UINT32)(r<<9 | g<<6 | b<<3);
/*TODO*///				else
/*TODO*///					((UINT16*)table_ptr32)[i] = (UINT16)(r | g | b);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			for (i=0; i<32768; i++)
/*TODO*///			{
/*TODO*///				r = (i & 0x7c00) + dr;
/*TODO*///				g = (i & 0x03e0) + dg;
/*TODO*///				b = (i & 0x001f) + db;
/*TODO*///
/*TODO*///				if (r < 0) r = 0; else if (r > 0x7c00) r = 0x7c00;
/*TODO*///				if (g < 0) g = 0; else if (g > 0x03e0) g = 0x03e0;
/*TODO*///				if (b < 0) b = 0; else if (b > 0x001f) b = 0x001f;
/*TODO*///
/*TODO*///				if (d32)
/*TODO*///					table_ptr32[i] = (UINT32)(r<<9 | g<<6 | b<<3);
/*TODO*///				else
/*TODO*///					((UINT16*)table_ptr32)[i] = (UINT16)(r | g | b);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///#undef FP
/*TODO*///#undef FMAX
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void palette_set_shadow_mode(int mode)
/*TODO*///{
/*TODO*///	if (mode >= 0 && mode < MAX_SHADOW_PRESETS) palette_shadow_table = (UINT16*)shadow_table_base[mode];
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void palette_set_shadow_factor32(double factor)
/*TODO*///{
/*TODO*///	internal_set_shadow_preset(0, factor, 0, 0, 0, 0, 1, 0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void palette_set_highlight_factor32(double factor)
/*TODO*///{
/*TODO*///	internal_set_shadow_preset(1, factor, 0, 0, 0, 0, 2, 0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void palette_set_shadow_dRGB32(int mode, int dr, int dg, int db, int noclip)
/*TODO*///{
/*TODO*///	internal_set_shadow_preset(mode, 0, dr, dg, db, noclip, 0, 0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void palette_set_highlight_method(int method)
/*TODO*///{
/*TODO*///	highlight_method = method;
/*TODO*///}
/*TODO*///
    /*-------------------------------------------------
	palette_alloc - allocate memory for palette
	structures
    -------------------------------------------------*/
    static int palette_alloc() {
        int max_total_colors = total_colors + 2;
        int i;

        /* allocate memory for the raw game palette */
        game_palette = new int[max_total_colors * 4];
        for (i = 0; i < max_total_colors; i++) {
            game_palette[i] = MAKE_RGB((i & 1) * 0xff, ((i >> 1) & 1) * 0xff, ((i >> 2) & 1) * 0xff);
        }

        /* allocate memory for the adjusted game palette */
        adjusted_palette = new int[max_total_colors * 4];
        for (i = 0; i < max_total_colors; i++) {
            adjusted_palette[i] = game_palette[i];
        }

        /* allocate memory for the dirty palette array */
        dirty_palette = new int[(max_total_colors + 31) / 32 * 4];
        for (i = 0; i < max_total_colors; i++) {
            mark_pen_dirty(i);
        }

        /* allocate memory for the pen table */
        Machine.pens = new int[total_colors * 4];

        for (i = 0; i < total_colors; i++) {
            Machine.pens[i] = i;
        }

        /* allocate memory for the per-entry brightness table */
        pen_brightness = new char[Machine.drv.total_colors * 2];
        for (i = 0; i < Machine.drv.total_colors; i++) {
            pen_brightness[i] = 1 << PEN_BRIGHTNESS_BITS;
        }

        /* allocate memory for the colortables, if needed */
        if (Machine.drv.color_table_len != 0) {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		/* first for the raw colortable */
/*TODO*///		Machine->game_colortable = auto_malloc(Machine->drv->color_table_len * sizeof(Machine->game_colortable[0]));
/*TODO*///		if (!Machine->game_colortable)
/*TODO*///			return 1;
/*TODO*///		for (i = 0; i < Machine->drv->color_table_len; i++)
/*TODO*///			Machine->game_colortable[i] = i % total_colors;
/*TODO*///
/*TODO*///		/* then for the remapped colortable */
/*TODO*///		Machine->remapped_colortable = auto_malloc(Machine->drv->color_table_len * sizeof(Machine->remapped_colortable[0]));
/*TODO*///		if (!Machine->remapped_colortable)
/*TODO*///			return 1;
        } /* otherwise, keep the game_colortable NULL and point the remapped_colortable to the pens */ else {
            Machine.game_colortable = null;
            Machine.remapped_colortable = new IntArray(Machine.pens);
            /* straight 1:1 mapping from palette to colortable */
        }
        /*TODO*///	{
/*TODO*///		UINT16 *table_ptr16;
/*TODO*///		UINT32 *table_ptr32;
/*TODO*///		int c = Machine->drv->total_colors;
/*TODO*///		int cx2 = c << 1;
/*TODO*///
/*TODO*///		for (i=0; i<MAX_SHADOW_PRESETS; i++) shadow_table_base[i] = NULL;
/*TODO*///
/*TODO*///		if (!(colormode & DIRECT_RGB))
/*TODO*///		{
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_HAS_SHADOWS)
/*TODO*///			{
/*TODO*///				if (!(table_ptr16 = auto_malloc(65536 * sizeof(UINT16)))) return 1;
/*TODO*///
/*TODO*///				shadow_table_base[0] = shadow_table_base[2] = (UINT32*)table_ptr16;
/*TODO*///
/*TODO*///				for (i=0; i<c; i++) table_ptr16[i] = c + i;
/*TODO*///				for (i=c; i<65536; i++) table_ptr16[i] = i;
/*TODO*///
/*TODO*///				internal_set_shadow_preset(0, PALETTE_DEFAULT_SHADOW_FACTOR32, 0, 0, 0, 0, 1, 1);
/*TODO*///			}
/*TODO*///
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_HAS_HIGHLIGHTS)
/*TODO*///			{
/*TODO*///				if (!(table_ptr16 = auto_malloc(65536 * sizeof(UINT16)))) return 1;
/*TODO*///
/*TODO*///				shadow_table_base[1] = shadow_table_base[3] = (UINT32*)table_ptr16;
/*TODO*///
/*TODO*///				for (i=0; i<c; i++) table_ptr16[i] = cx2 + i;
/*TODO*///				for (i=c; i<65536; i++) table_ptr16[i] = i;
/*TODO*///
/*TODO*///				internal_set_shadow_preset(1, PALETTE_DEFAULT_HIGHLIGHT_FACTOR32, 0, 0, 0, 0, 2, 1);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_HAS_SHADOWS)
/*TODO*///			{
/*TODO*///				if (!(table_ptr32 = auto_malloc(65536 * sizeof(UINT32)))) return 1;
/*TODO*///
/*TODO*///				shadow_table_base[0] = table_ptr32;
/*TODO*///				shadow_table_base[2] = table_ptr32 + 32768;
/*TODO*///
/*TODO*///				internal_set_shadow_preset(0, PALETTE_DEFAULT_SHADOW_FACTOR32, 0, 0, 0, 0, 1, 1);
/*TODO*///			}
/*TODO*///
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_HAS_HIGHLIGHTS)
/*TODO*///			{
/*TODO*///				if (!(table_ptr32 = auto_malloc(65536 * sizeof(UINT32)))) return 1;
/*TODO*///
/*TODO*///				shadow_table_base[1] = table_ptr32;
/*TODO*///				shadow_table_base[3] = table_ptr32 + 32768;
/*TODO*///
/*TODO*///				internal_set_shadow_preset(1, PALETTE_DEFAULT_HIGHLIGHT_FACTOR32, 0, 0, 0, 0, 2, 1);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		palette_shadow_table = (UINT16*)shadow_table_base[0];
/*TODO*///	}
        return 0;
    }

    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_init - palette initialization that
/*TODO*///	takes place after the display is created
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///int palette_init(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* recompute the default palette and initalize the color correction table */
/*TODO*///	recompute_adjusted_palette(1);
/*TODO*///
/*TODO*///	/* now let the driver modify the initial palette and colortable */
/*TODO*///	if (Machine->drv->init_palette)
/*TODO*///		(*Machine->drv->init_palette)(Machine->game_colortable, memory_region(REGION_PROMS));
/*TODO*///
/*TODO*///	/* switch off the color mode */
/*TODO*///	switch (colormode)
/*TODO*///	{
/*TODO*///		/* 16-bit paletteized case */
/*TODO*///		case PALETTIZED_16BIT:
/*TODO*///		{
/*TODO*///			/* refresh the palette to support shadows in static palette games */
/*TODO*///			for (i = 0; i < Machine->drv->total_colors; i++)
/*TODO*///				palette_set_color(i, RGB_RED(game_palette[i]), RGB_GREEN(game_palette[i]), RGB_BLUE(game_palette[i]));
/*TODO*///
/*TODO*///			/* map the UI pens */
/*TODO*///			if (total_colors_with_ui <= 65534)
/*TODO*///			{
/*TODO*///				game_palette[total_colors + 0] = adjusted_palette[total_colors + 0] = MAKE_RGB(0x00,0x00,0x00);
/*TODO*///				game_palette[total_colors + 1] = adjusted_palette[total_colors + 1] = MAKE_RGB(0xff,0xff,0xff);
/*TODO*///				Machine->uifont->colortable[0] = Machine->uifont->colortable[3] = total_colors_with_ui++;
/*TODO*///				Machine->uifont->colortable[1] = Machine->uifont->colortable[2] = total_colors_with_ui++;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				game_palette[0] = adjusted_palette[0] = MAKE_RGB(0x00,0x00,0x00);
/*TODO*///				game_palette[65535] = adjusted_palette[65535] = MAKE_RGB(0xff,0xff,0xff);
/*TODO*///				Machine->uifont->colortable[0] = Machine->uifont->colortable[3] = 0;
/*TODO*///				Machine->uifont->colortable[1] = Machine->uifont->colortable[2] = 65535;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* 15-bit direct case */
/*TODO*///		case DIRECT_15BIT:
/*TODO*///		{
/*TODO*///			/* remap the game palette into direct RGB pens */
/*TODO*///			for (i = 0; i < total_colors; i++)
/*TODO*///				Machine->pens[i] = rgb_to_direct15(game_palette[i]);
/*TODO*///
/*TODO*///			/* map the UI pens */
/*TODO*///			Machine->uifont->colortable[0] = Machine->uifont->colortable[3] = rgb_to_direct15(MAKE_RGB(0x00,0x00,0x00));
/*TODO*///			Machine->uifont->colortable[1] = Machine->uifont->colortable[2] = rgb_to_direct15(MAKE_RGB(0xff,0xff,0xff));
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		case DIRECT_32BIT:
/*TODO*///		{
/*TODO*///			/* remap the game palette into direct RGB pens */
/*TODO*///			for (i = 0; i < total_colors; i++)
/*TODO*///				Machine->pens[i] = rgb_to_direct32(game_palette[i]);
/*TODO*///
/*TODO*///			/* map the UI pens */
/*TODO*///			Machine->uifont->colortable[0] = Machine->uifont->colortable[3] = rgb_to_direct32(MAKE_RGB(0x00,0x00,0x00));
/*TODO*///			Machine->uifont->colortable[1] = Machine->uifont->colortable[2] = rgb_to_direct32(MAKE_RGB(0xff,0xff,0xff));
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* now compute the remapped_colortable */
/*TODO*///	for (i = 0; i < Machine->drv->color_table_len; i++)
/*TODO*///	{
/*TODO*///		pen_t color = Machine->game_colortable[i];
/*TODO*///
/*TODO*///		/* check for invalid colors set by Machine->drv->init_palette */
/*TODO*///		if (color < total_colors)
/*TODO*///			Machine->remapped_colortable[i] = Machine->pens[color];
/*TODO*///		else
/*TODO*///			usrintf_showmessage("colortable[%d] (=%d) out of range (total_colors = %d)",
/*TODO*///					i,color,total_colors);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* all done */
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_get_total_colors_with_ui - returns
/*TODO*///	the total number of palette entries including
/*TODO*///	UI
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///int palette_get_total_colors_with_ui(void)
/*TODO*///{
/*TODO*///	int result = Machine->drv->total_colors;
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_HAS_SHADOWS && !(colormode & DIRECT_RGB))
/*TODO*///		result += Machine->drv->total_colors;
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_HAS_HIGHLIGHTS && !(colormode & DIRECT_RGB))
/*TODO*///		result += Machine->drv->total_colors;
/*TODO*///	if (result <= 65534)
/*TODO*///		result += 2;
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_update_display - update the display
/*TODO*///	state with our latest info
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_update_display(struct mame_display *display)
/*TODO*///{
/*TODO*///	/* palettized case: point to the palette info */
/*TODO*///	if (colormode == PALETTIZED_16BIT)
/*TODO*///	{
/*TODO*///		display->game_palette = adjusted_palette;
/*TODO*///		display->game_palette_entries = total_colors_with_ui;
/*TODO*///		display->game_palette_dirty = dirty_palette;
/*TODO*///
/*TODO*///		if (adjusted_palette_dirty)
/*TODO*///			display->changed_flags |= GAME_PALETTE_CHANGED;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* direct case: no palette mucking */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		display->game_palette = NULL;
/*TODO*///		display->game_palette_entries = 0;
/*TODO*///		display->game_palette_dirty = NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* debugger always has a palette */
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	display->debug_palette = debugger_palette;
/*TODO*///	display->debug_palette_entries = DEBUGGER_TOTAL_COLORS;
/*TODO*///#endif
/*TODO*///
/*TODO*///	/* update the dirty state */
/*TODO*///	if (debug_palette_dirty)
/*TODO*///		display->changed_flags |= DEBUG_PALETTE_CHANGED;
/*TODO*///
/*TODO*///	/* clear the dirty flags */
/*TODO*///	adjusted_palette_dirty = 0;
/*TODO*///	debug_palette_dirty = 0;
/*TODO*///}
/*TODO*///
/*TODO*///

    /*-------------------------------------------------
	internal_modify_single_pen - change a single
	pen and recompute its adjusted RGB value
-------------------------------------------------*/
    static void internal_modify_single_pen(int/*pen_t*/ pen, int/*rgb_t*/ color, int pen_bright) {
        int/*rgb_t*/ adjusted_color;

        /* skip if out of bounds or not ready */
        if (pen >= total_colors) {
            return;
        }

        /* update the raw palette */
        game_palette[pen] = color;

        /* now update the adjusted color if it's different */
        adjusted_color = adjust_palette_entry(color, pen_bright);
        if (adjusted_color != adjusted_palette[pen]) {
            /* change the adjusted palette entry */
            adjusted_palette[pen] = adjusted_color;
            adjusted_palette_dirty = 1;

            /* update the pen value or mark the palette dirty */
            switch (colormode) {
                /* 16-bit palettized: just mark it dirty for later */
                case PALETTIZED_16BIT:
                    mark_pen_dirty(pen);
                    break;

                /* 15/32-bit direct: update the Machine->pens array */
                case DIRECT_15BIT:
                    throw new UnsupportedOperationException("Unsupported");
                /*TODO*///				Machine->pens[pen] = rgb_to_direct15(adjusted_color);
/*TODO*///				break;

                case DIRECT_32BIT:
                    throw new UnsupportedOperationException("Unsupported");
                /*TODO*///				Machine->pens[pen] = rgb_to_direct32(adjusted_color);
/*TODO*///				break;
            }
        }
    }

    /*-------------------------------------------------
	internal_modify_pen - change a pen along with
	its corresponding shadow/highlight
    -------------------------------------------------*/
    static void internal_modify_pen(int/*pen_t*/ pen, int/*rgb_t*/ color, int pen_bright) //* new highlight operation
    {
        /*TODO*///#define FMAX (0xff<<PEN_BRIGHTNESS_BITS)
/*TODO*///
        int r, g, b, fl, ov;

        /* first modify the base pen */
        internal_modify_single_pen(pen, color, pen_bright);

        /* see if we need to handle shadow/highlight */
        if (pen < Machine.drv.total_colors) {
            /* check for shadows */
            if ((Machine.drv.video_attributes & VIDEO_HAS_SHADOWS) != 0) {
                throw new UnsupportedOperationException("Unsupported");
                /*TODO*///			pen += Machine->drv->total_colors;
/*TODO*///
/*TODO*///			if (shadow_factor > (1 << PEN_BRIGHTNESS_BITS) && highlight_method) // luminance > 1.0
/*TODO*///			{
/*TODO*///				r = color>>16 & 0xff;
/*TODO*///				g = color>>8  & 0xff;
/*TODO*///				b = color     & 0xff;
/*TODO*///
/*TODO*///				if (highlight_method == 1)
/*TODO*///				{
/*TODO*///					fl = shadow_factor;
/*TODO*///
/*TODO*///					r *= fl;  g *= fl;  b *= fl;
/*TODO*///					ov = 0;
/*TODO*///
/*TODO*///					if (r > FMAX) ov += r - FMAX;
/*TODO*///					if (g > FMAX) ov += g - FMAX;
/*TODO*///					if (b > FMAX) ov += b - FMAX;
/*TODO*///
/*TODO*///					if (ov) { ov >>= 2;  r += ov;  g += ov;  b += ov; }
/*TODO*///
/*TODO*///					if (r >= FMAX) r = 0xff0000; else r = (r >> PEN_BRIGHTNESS_BITS) << 16;
/*TODO*///					if (g >= FMAX) g = 0x00ff00; else g = (g >> PEN_BRIGHTNESS_BITS) << 8;
/*TODO*///					if (b >= FMAX) b = 0x0000ff; else b = (b >> PEN_BRIGHTNESS_BITS);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					fl = ((shadow_factor - (1 << PEN_BRIGHTNESS_BITS)) * 255) >> PEN_BRIGHTNESS_BITS;
/*TODO*///
/*TODO*///					r += fl;  g += fl;  b += fl;
/*TODO*///
/*TODO*///					if (r >= 0xff) r = 0xff0000; else r <<= 16;
/*TODO*///					if (g >= 0xff) g = 0x00ff00; else g <<= 8;
/*TODO*///					if (b >= 0xff) b = 0x0000ff;
/*TODO*///				}
/*TODO*///
/*TODO*///				internal_modify_single_pen(pen, r|g|b, pen_bright);
/*TODO*///			}
/*TODO*///			else // luminance <= 1.0
/*TODO*///				internal_modify_single_pen(pen, color, (pen_bright * shadow_factor) >> PEN_BRIGHTNESS_BITS);
            }

            /* check for highlights */
            if ((Machine.drv.video_attributes & VIDEO_HAS_HIGHLIGHTS) != 0) {
                throw new UnsupportedOperationException("Unsupported");
                /*TODO*///			pen += Machine->drv->total_colors;
/*TODO*///
/*TODO*///			if (highlight_factor > (1 << PEN_BRIGHTNESS_BITS) && highlight_method) // luminance > 1.0
/*TODO*///			{
/*TODO*///				r = color>>16 & 0xff;
/*TODO*///				g = color>>8  & 0xff;
/*TODO*///				b = color     & 0xff;
/*TODO*///
/*TODO*///				if (highlight_method == 1)
/*TODO*///				{
/*TODO*///					fl = highlight_factor;
/*TODO*///
/*TODO*///					r *= fl;  g *= fl;  b *= fl;
/*TODO*///					ov = 0;
/*TODO*///
/*TODO*///					if (r > FMAX) ov += r - FMAX;
/*TODO*///					if (g > FMAX) ov += g - FMAX;
/*TODO*///					if (b > FMAX) ov += b - FMAX;
/*TODO*///
/*TODO*///					if (ov) { ov >>= 2;  r += ov;  g += ov;  b += ov; }
/*TODO*///
/*TODO*///					if (r >= FMAX) r = 0xff0000; else r = (r >> PEN_BRIGHTNESS_BITS) << 16;
/*TODO*///					if (g >= FMAX) g = 0x00ff00; else g = (g >> PEN_BRIGHTNESS_BITS) << 8;
/*TODO*///					if (b >= FMAX) b = 0x0000ff; else b = (b >> PEN_BRIGHTNESS_BITS);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					fl = ((highlight_factor - (1 << PEN_BRIGHTNESS_BITS)) * 255) >> PEN_BRIGHTNESS_BITS;
/*TODO*///
/*TODO*///					r += fl;  g += fl;  b += fl;
/*TODO*///
/*TODO*///					if (r >= 0xff) r = 0xff0000; else r <<= 16;
/*TODO*///					if (g >= 0xff) g = 0x00ff00; else g <<= 8;
/*TODO*///					if (b >= 0xff) b = 0x0000ff;
/*TODO*///				}
/*TODO*///
/*TODO*///				internal_modify_single_pen(pen, r|g|b, pen_bright);
/*TODO*///			}
/*TODO*///			else // luminance <= 1.0
/*TODO*///				internal_modify_single_pen(pen, color, (pen_bright * highlight_factor) >> PEN_BRIGHTNESS_BITS);
            }
        }
    }

    /*TODO*///
/*TODO*///

    /*-------------------------------------------------
	recompute_adjusted_palette - recompute the
	entire palette after some major event
    -------------------------------------------------*/
    static void recompute_adjusted_palette(int brightness_or_gamma_changed) {
        int i;

        /* regenerate the color correction table if needed */
        if (brightness_or_gamma_changed != 0) {
            for (i = 0; i < sizeof(color_correct_table); i++) {
                int value = (int) (255.0 * (global_brightness * global_brightness_adjust) * Math.pow((double) i * (1.0 / 255.0), 1.0 / global_gamma) + 0.5);
                color_correct_table[i] = (value < 0) ? 0 : (value > 255) ? 255 : value;
            }
        }

        /* now update all the palette entries */
        for (i = 0; i < Machine.drv.total_colors; i++) {
            internal_modify_pen(i, game_palette[i], pen_brightness[i]);
        }
    }

    /*-------------------------------------------------
	palette_reset - called after restore to
	actually update the palette
    -------------------------------------------------*/
    public static void palette_reset() {
        /* recompute everything */
        recompute_adjusted_palette(0);
    }

    /*-------------------------------------------------
	palette_set_color - set a single palette
	entry
    -------------------------------------------------*/
    public static void palette_set_color(int/*pen_t*/ pen, int/*UINT8*/ r, int/*UINT8*/ g, int/*UINT8*/ b) {
        /* make sure we're in range */
        if (pen >= total_colors) {
            logerror("error: palette_set_color() called with color %d, but only %d allocated.\n", pen, total_colors);
            return;
        }

        /* set the pen value */
        internal_modify_pen(pen, MAKE_RGB(r, g, b), pen_brightness[pen]);
    }

    /*TODO*////* handy wrapper for palette_set_color */
/*TODO*///void palette_set_colors(pen_t color_base, const UINT8 *colors, int color_count)
/*TODO*///{
/*TODO*///        while(color_count--)
/*TODO*///        {
/*TODO*///                palette_set_color(color_base++, colors[0], colors[1], colors[2]);
/*TODO*///                colors += 3;
/*TODO*///        }
/*TODO*///}
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_get_color - return a single palette
/*TODO*///	entry
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_get_color(pen_t pen, UINT8 *r, UINT8 *g, UINT8 *b)
/*TODO*///{
/*TODO*///	/* special case the black pen */
/*TODO*///	if (pen == get_black_pen())
/*TODO*///		*r = *g = *b = 0;
/*TODO*///
/*TODO*///	/* record the result from the game palette */
/*TODO*///	else if (pen < total_colors)
/*TODO*///	{
/*TODO*///		*r = RGB_RED(game_palette[pen]);
/*TODO*///		*g = RGB_GREEN(game_palette[pen]);
/*TODO*///		*b = RGB_BLUE(game_palette[pen]);
/*TODO*///	}
/*TODO*///	else
/*TODO*///		usrintf_showmessage("palette_get_color() out of range");
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_set_brightness - set the per-pen
/*TODO*///	brightness factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_set_brightness(pen_t pen, double bright)
/*TODO*///{
/*TODO*///	/* compute the integral brightness value */
/*TODO*///	int brightval = (int)(bright * (double)(1 << PEN_BRIGHTNESS_BITS));
/*TODO*///	if (brightval > MAX_PEN_BRIGHTNESS)
/*TODO*///		brightval = MAX_PEN_BRIGHTNESS;
/*TODO*///
/*TODO*///	/* if it changed, update the array and the adjusted palette */
/*TODO*///	if (pen_brightness[pen] != brightval)
/*TODO*///	{
/*TODO*///		pen_brightness[pen] = brightval;
/*TODO*///		internal_modify_pen(pen, game_palette[pen], brightval);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_set_shadow_factor - set the global
/*TODO*///	shadow brightness factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_set_shadow_factor(double factor)
/*TODO*///{
/*TODO*///	/* compute the integral shadow factor value */
/*TODO*///	int factorval = (int)(factor * (double)(1 << PEN_BRIGHTNESS_BITS));
/*TODO*///	if (factorval > MAX_PEN_BRIGHTNESS)
/*TODO*///		factorval = MAX_PEN_BRIGHTNESS;
/*TODO*///
/*TODO*///	/* if it changed, update the entire palette */
/*TODO*///	if (shadow_factor != factorval)
/*TODO*///	{
/*TODO*///		shadow_factor = factorval;
/*TODO*///		recompute_adjusted_palette(0);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_set_highlight_factor - set the global
/*TODO*///	highlight brightness factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_set_highlight_factor(double factor)
/*TODO*///{
/*TODO*///	/* compute the integral highlight factor value */
/*TODO*///	int factorval = (int)(factor * (double)(1 << PEN_BRIGHTNESS_BITS));
/*TODO*///	if (factorval > MAX_PEN_BRIGHTNESS)
/*TODO*///		factorval = MAX_PEN_BRIGHTNESS;
/*TODO*///
/*TODO*///	/* if it changed, update the entire palette */
/*TODO*///	if (highlight_factor != factorval)
/*TODO*///	{
/*TODO*///		highlight_factor = factorval;
/*TODO*///		recompute_adjusted_palette(0);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_set_global_gamma - set the global
/*TODO*///	gamma factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_set_global_gamma(double _gamma)
/*TODO*///{
/*TODO*///	/* if the gamma changed, recompute */
/*TODO*///	if (global_gamma != _gamma)
/*TODO*///	{
/*TODO*///		global_gamma = _gamma;
/*TODO*///		recompute_adjusted_palette(1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_get_global_gamma - return the global
/*TODO*///	gamma factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double palette_get_global_gamma(void)
/*TODO*///{
/*TODO*///	return global_gamma;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_set_global_brightness - set the global
/*TODO*///	brightness factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_set_global_brightness(double brightness)
/*TODO*///{
/*TODO*///	/* if the gamma changed, recompute */
/*TODO*///	if (global_brightness != brightness)
/*TODO*///	{
/*TODO*///		global_brightness = brightness;
/*TODO*///		recompute_adjusted_palette(1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_set_global_brightness_adjust - set
/*TODO*///	the global brightness adjustment factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void palette_set_global_brightness_adjust(double adjustment)
/*TODO*///{
/*TODO*///	/* if the gamma changed, recompute */
/*TODO*///	if (global_brightness_adjust != adjustment)
/*TODO*///	{
/*TODO*///		global_brightness_adjust = adjustment;
/*TODO*///		recompute_adjusted_palette(1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	palette_get_global_brightness - return the global
/*TODO*///	brightness factor
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double palette_get_global_brightness(void)
/*TODO*///{
/*TODO*///	return global_brightness;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	get_black_pen - use this if you need to
/*TODO*///	fillbitmap() the background with black
/*TODO*///-------------------------------------------------*/
/*TODO*///
    public static int /*pen_t*/ get_black_pen() {
        return Machine.uifont.colortable.read(0);
    }

    /**
     * ****************************************************************************
     *
     * Commonly used palette RAM handling functions
     *
     *****************************************************************************
     */
    public static ReadHandlerPtr paletteram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return paletteram.read(offset);
        }
    };
    
    public static ReadHandlerPtr paletteram_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return paletteram_2.read(offset);
        }
    };

/*TODO*///READ16_HANDLER( paletteram16_word_r )
/*TODO*///{
/*TODO*///	return paletteram16[offset];
/*TODO*///}
/*TODO*///
/*TODO*///READ16_HANDLER( paletteram16_2_word_r )
/*TODO*///{
/*TODO*///	return paletteram16_2[offset];
/*TODO*///}
/*TODO*///
/*TODO*///READ32_HANDLER( paletteram32_r )
/*TODO*///{
/*TODO*///	return paletteram32[offset];
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_RRRGGGBB_w )
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///	int bit0,bit1,bit2;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	/* red component */
/*TODO*///	bit0 = (data >> 5) & 0x01;
/*TODO*///	bit1 = (data >> 6) & 0x01;
/*TODO*///	bit2 = (data >> 7) & 0x01;
/*TODO*///	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* green component */
/*TODO*///	bit0 = (data >> 2) & 0x01;
/*TODO*///	bit1 = (data >> 3) & 0x01;
/*TODO*///	bit2 = (data >> 4) & 0x01;
/*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* blue component */
/*TODO*///	bit0 = 0;
/*TODO*///	bit1 = (data >> 0) & 0x01;
/*TODO*///	bit2 = (data >> 1) & 0x01;
/*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///
/*TODO*///	palette_set_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_BBBGGGRR_w )
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///	int bit0,bit1,bit2;
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	/* blue component */
/*TODO*///	bit0 = (data >> 5) & 0x01;
/*TODO*///	bit1 = (data >> 6) & 0x01;
/*TODO*///	bit2 = (data >> 7) & 0x01;
/*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* green component */
/*TODO*///	bit0 = (data >> 2) & 0x01;
/*TODO*///	bit1 = (data >> 3) & 0x01;
/*TODO*///	bit2 = (data >> 4) & 0x01;
/*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* blue component */
/*TODO*///	bit0 = (data >> 0) & 0x01;
/*TODO*///	bit1 = (data >> 1) & 0x01;
/*TODO*///	r = 0x55 * bit0 + 0xaa * bit1;
/*TODO*///
/*TODO*///	palette_set_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_BBGGGRRR_w )
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///	int bit0,bit1,bit2;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	/* red component */
/*TODO*///	bit0 = (data >> 0) & 0x01;
/*TODO*///	bit1 = (data >> 1) & 0x01;
/*TODO*///	bit2 = (data >> 2) & 0x01;
/*TODO*///	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* green component */
/*TODO*///	bit0 = (data >> 3) & 0x01;
/*TODO*///	bit1 = (data >> 4) & 0x01;
/*TODO*///	bit2 = (data >> 5) & 0x01;
/*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* blue component */
/*TODO*///	bit0 = 0;
/*TODO*///	bit1 = (data >> 6) & 0x01;
/*TODO*///	bit2 = (data >> 7) & 0x01;
/*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///
/*TODO*///	palette_set_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_IIBBGGRR_w )
/*TODO*///{
/*TODO*///	int r,g,b,i;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	i = (data >> 6) & 0x03;
/*TODO*///	/* red component */
/*TODO*///	r = (data << 2) & 0x0c;
/*TODO*///	if (r) r |= i;
/*TODO*///	r *= 0x11;
/*TODO*///	/* green component */
/*TODO*///	g = (data >> 0) & 0x0c;
/*TODO*///	if (g) g |= i;
/*TODO*///	g *= 0x11;
/*TODO*///	/* blue component */
/*TODO*///	b = (data >> 2) & 0x0c;
/*TODO*///	if (b) b |= i;
/*TODO*///	b *= 0x11;
/*TODO*///
/*TODO*///	palette_set_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_BBGGRRII_w )
/*TODO*///{
/*TODO*///	int r,g,b,i;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	i = (data >> 0) & 0x03;
/*TODO*///	/* red component */
/*TODO*///	r = (((data >> 0) & 0x0c) | i) * 0x11;
/*TODO*///	/* green component */
/*TODO*///	g = (((data >> 2) & 0x0c) | i) * 0x11;
/*TODO*///	/* blue component */
/*TODO*///	b = (((data >> 4) & 0x0c) | i) * 0x11;
/*TODO*///
/*TODO*///	palette_set_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxBBBBGGGGRRRR(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 0) & 0x0f;
/*TODO*///	g = (data >> 4) & 0x0f;
/*TODO*///	b = (data >> 8) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBGGGGRRRR_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBGGGGRRRR_swap_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBGGGGRRRR_split1_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBGGGGRRRR_split2_w )
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xxxxBBBBGGGGRRRR_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxBBBBRRRRGGGG(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 4) & 0x0f;
/*TODO*///	g = (data >> 0) & 0x0f;
/*TODO*///	b = (data >> 8) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBRRRRGGGG_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBRRRRGGGG_swap_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBRRRRGGGG_split1_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxBBBBRRRRGGGG_split2_w )
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xxxxBBBBRRRRGGGG_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxRRRRBBBBGGGG(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 8) & 0x0f;
/*TODO*///	g = (data >> 0) & 0x0f;
/*TODO*///	b = (data >> 4) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxRRRRBBBBGGGG_split1_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxRRRRBBBBGGGG_split2_w )
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxRRRRGGGGBBBB(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 8) & 0x0f;
/*TODO*///	g = (data >> 4) & 0x0f;
/*TODO*///	b = (data >> 0) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxRRRRGGGGBBBB_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xxxxRRRRGGGGBBBB_swap_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xxxxRRRRGGGGBBBB_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_RRRRGGGGBBBBxxxx(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 12) & 0x0f;
/*TODO*///	g = (data >>  8) & 0x0f;
/*TODO*///	b = (data >>  4) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_RRRRGGGGBBBBxxxx_swap_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_RRRRGGGGBBBBxxxx_split1_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_RRRRGGGGBBBBxxxx_split2_w )
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_RRRRGGGGBBBBxxxx_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_BBBBGGGGRRRRxxxx(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >>  4) & 0x0f;
/*TODO*///	g = (data >>  8) & 0x0f;
/*TODO*///	b = (data >> 12) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_BBBBGGGGRRRRxxxx_swap_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_BBBBGGGGRRRRxxxx_split1_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_BBBBGGGGRRRRxxxx_split2_w )
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_BBBBGGGGRRRRxxxx_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xBBBBBGGGGGRRRRR(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >>  0) & 0x1f;
/*TODO*///	g = (data >>  5) & 0x1f;
/*TODO*///	b = (data >> 10) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xBBBBBGGGGGRRRRR_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xBBBBBGGGGGRRRRR_swap_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xBBBBBGGGGGRRRRR_split1_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xBBBBBGGGGGRRRRR_split2_w )
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xBBBBBGGGGGRRRRR_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xRRRRRGGGGGBBBBB(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 10) & 0x1f;
/*TODO*///	g = (data >>  5) & 0x1f;
/*TODO*///	b = (data >>  0) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_xRRRRRGGGGGBBBBB_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xRRRRRGGGGGBBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xRRRRRGGGGGBBBBB_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xRRRRRGGGGGBBBBB(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xGGGGGRRRRRBBBBB(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >>  5) & 0x1f;
/*TODO*///	g = (data >> 10) & 0x1f;
/*TODO*///	b = (data >>  0) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xGGGGGRRRRRBBBBB_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xGGGGGRRRRRBBBBB(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xGGGGGBBBBBRRRRR(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >>  0) & 0x1f;
/*TODO*///	g = (data >> 10) & 0x1f;
/*TODO*///	b = (data >>  5) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xGGGGGBBBBBRRRRR_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_xGGGGGBBBBBRRRRR(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_RRRRRGGGGGBBBBBx(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 11) & 0x1f;
/*TODO*///	g = (data >>  6) & 0x1f;
/*TODO*///	b = (data >>  1) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE_HANDLER( paletteram_RRRRRGGGGGBBBBBx_w )
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_RRRRRGGGGGBBBBBx(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_RRRRRGGGGGBBBBBx_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_RRRRRGGGGGBBBBBx(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_IIIIRRRRGGGGBBBB(pen_t color,int data)
/*TODO*///{
/*TODO*///	int i,r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	static const int ztable[16] =
/*TODO*///		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };
/*TODO*///
/*TODO*///	i = ztable[(data >> 12) & 15];
/*TODO*///	r = ((data >> 8) & 15) * i;
/*TODO*///	g = ((data >> 4) & 15) * i;
/*TODO*///	b = ((data >> 0) & 15) * i;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///
/*TODO*///	if (!(Machine->drv->video_attributes & VIDEO_NEEDS_6BITS_PER_GUN))
/*TODO*///		usrintf_showmessage("driver should use VIDEO_NEEDS_6BITS_PER_GUN flag");
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_IIIIRRRRGGGGBBBB_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_IIIIRRRRGGGGBBBB(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_RRRRGGGGBBBBIIII(pen_t color,int data)
/*TODO*///{
/*TODO*///	int i,r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	static const int ztable[16] =
/*TODO*///		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };
/*TODO*///
/*TODO*///	i = ztable[(data >> 0) & 15];
/*TODO*///	r = ((data >> 12) & 15) * i;
/*TODO*///	g = ((data >>  8) & 15) * i;
/*TODO*///	b = ((data >>  4) & 15) * i;
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///
/*TODO*///	if (!(Machine->drv->video_attributes & VIDEO_NEEDS_6BITS_PER_GUN))
/*TODO*///		usrintf_showmessage("driver should use VIDEO_NEEDS_6BITS_PER_GUN flag");
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_RRRRGGGGBBBBIIII_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_RRRRGGGGBBBBIIII(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xrgb_word_w )
/*TODO*///{
/*TODO*///	int r, g, b;
/*TODO*///	data16_t data0, data1;
/*TODO*///
/*TODO*///	COMBINE_DATA(paletteram16 + offset);
/*TODO*///
/*TODO*///	offset &= ~1;
/*TODO*///
/*TODO*///	data0 = paletteram16[offset];
/*TODO*///	data1 = paletteram16[offset + 1];
/*TODO*///
/*TODO*///	r = data0 & 0xff;
/*TODO*///	g = data1 >> 8;
/*TODO*///	b = data1 & 0xff;
/*TODO*///
/*TODO*///	palette_set_color(offset>>1, r, g, b);
/*TODO*///
/*TODO*///	if (!(Machine->drv->video_attributes & VIDEO_NEEDS_6BITS_PER_GUN))
/*TODO*///		usrintf_showmessage("driver should use VIDEO_NEEDS_6BITS_PER_GUN flag");
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_xbgr_word_w )
/*TODO*///{
/*TODO*///	int r, g, b;
/*TODO*///	data16_t data0, data1;
/*TODO*///
/*TODO*///	COMBINE_DATA(paletteram16 + offset);
/*TODO*///
/*TODO*///	offset &= ~1;
/*TODO*///
/*TODO*///	data0 = paletteram16[offset];
/*TODO*///	data1 = paletteram16[offset + 1];
/*TODO*///
/*TODO*///	b = data0 & 0xff;
/*TODO*///	g = data1 >> 8;
/*TODO*///	r = data1 & 0xff;
/*TODO*///
/*TODO*///	palette_set_color(offset>>1, r, g, b);
/*TODO*///
/*TODO*///	if (!(Machine->drv->video_attributes & VIDEO_NEEDS_6BITS_PER_GUN))
/*TODO*///		usrintf_showmessage("driver should use VIDEO_NEEDS_6BITS_PER_GUN flag");
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_RRRRGGGGBBBBRGBx(pen_t color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///	r = ((data >> 11) & 0x1e) | ((data>>3) & 0x01);
/*TODO*///	g = ((data >>  7) & 0x1e) | ((data>>2) & 0x01);
/*TODO*///	b = ((data >>  3) & 0x1e) | ((data>>1) & 0x01);
/*TODO*///	r = (r<<3) | (r>>2);
/*TODO*///	g = (g<<3) | (g>>2);
/*TODO*///	b = (b<<3) | (b>>2);
/*TODO*///
/*TODO*///	palette_set_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///WRITE16_HANDLER( paletteram16_RRRRGGGGBBBBRGBx_word_w )
/*TODO*///{
/*TODO*///	COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	changecolor_RRRRGGGGBBBBRGBx(offset,paletteram16[offset]);
/*TODO*///}
/*TODO*///
    /**
     * ****************************************************************************
     *
     * Commonly used color PROM handling functions
     *
     *****************************************************************************
     */
    /**
     * *************************************************************************
     *
     * Standard black and white palette. Color 0 is pure black, color 1 is pure
     * white.
     *
     **************************************************************************
     */
    public static PaletteInitHandlerPtr black_and_white = new PaletteInitHandlerPtr() {
        public void handler(char[] colortable, UBytePtr color_prom) {
            palette_set_color(0, 0x00, 0x00, 0x00);/* black */
            palette_set_color(1, 0xff, 0xff, 0xff);/* white */
        }
    };

    /**
     * *************************************************************************
     *
     * This assumes the commonly used resistor values:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static PaletteInitHandlerPtr RRRR_GGGG_BBBB = new PaletteInitHandlerPtr() {
        public void handler(char[] colortable, UBytePtr color_prom) {
            int i;

            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3, r, g, b;

                /* red component */
                bit0 = (color_prom.read(i) >> 0) & 0x01;
                bit1 = (color_prom.read(i) >> 1) & 0x01;
                bit2 = (color_prom.read(i) >> 2) & 0x01;
                bit3 = (color_prom.read(i) >> 3) & 0x01;
                r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
                /* green component */
                bit0 = (color_prom.read(i + Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(i + Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(i + Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(i + Machine.drv.total_colors) >> 3) & 0x01;
                g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
                /* blue component */
                bit0 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 3) & 0x01;
                b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

                palette_set_color(i, r, g, b);
            }
        }
    };

}
