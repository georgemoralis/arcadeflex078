/**
 * Ported to 0.56
 */
package mame056;

import arcadeflex.v078.generic.funcPtr.ReadHandlerPtr;
import arcadeflex.v078.generic.funcPtr.WriteHandlerPtr;
import static arcadeflex056.video.osd_allocate_colors;
import static arcadeflex056.video.osd_modify_pen;
import static common.subArrays.*;
import static arcadeflex.v078.mame.commonH.REGION_PROMS;
import static mame056.common.memory_region;
import static mame056.usrintrf.usrintf_showmessage;
import static arcadeflex036.osdepend.logerror;
import static arcadeflex056.fucPtr.*;

import static common.ptr.*;
import static mame056.driverH.VIDEO_HAS_SHADOWS;
import static mame056.mame.Machine;

public class palette {

    /*TODO*///#define VERBOSE 0
    public static char[] game_palette;/* RGB palette as set by the driver */
    public static char[] actual_palette;/* actual RGB palette after brightness adjustments */
    static double[] brightness;
    /*TODO*///
    static int colormode;
    public static final int PALETTIZED_16BIT = 0;
    public static final int DIRECT_15BIT = 1;
    public static final int DIRECT_32BIT = 2;

    static int total_colors;
    static double shadow_factor, highlight_factor;
    static int palette_initialized;

    /*TODO*///UINT32 direct_rgb_components[3];
/*TODO*///
/*TODO*///
/*TODO*///
    public static char[] palette_shadow_table;
/*TODO*///
    public static int palette_start() {
/*TODO*///        	int i;
/*TODO*///
/*TODO*///	if ((Machine->drv->video_attributes & VIDEO_RGB_DIRECT) &&
/*TODO*///			Machine->drv->color_table_len)
/*TODO*///	{
/*TODO*///		logerror("Error: VIDEO_RGB_DIRECT requires color_table_len to be 0.\n");
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
        total_colors = Machine.drv.total_colors;
        /*TODO*///	if (Machine->drv->video_attributes & VIDEO_HAS_SHADOWS)
/*TODO*///		total_colors += Machine->drv->total_colors;
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_HAS_HIGHLIGHTS)
/*TODO*///		total_colors += Machine->drv->total_colors;
/*TODO*///	if (total_colors > 65536)
/*TODO*///	{
/*TODO*///		logerror("Error: palette has more than 65536 colors.\n");
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	shadow_factor = PALETTE_DEFAULT_SHADOW_FACTOR;
/*TODO*///	highlight_factor = PALETTE_DEFAULT_HIGHLIGHT_FACTOR;
/*TODO*///
        game_palette = new char[3 * total_colors];
        actual_palette = new char[3 * total_colors];
        brightness = new double[Machine.drv.total_colors];
        /*TODO*///
/*TODO*///	if (Machine->color_depth == 15)
/*TODO*///		colormode = DIRECT_15BIT;
/*TODO*///	else if (Machine->color_depth == 32)
/*TODO*///		colormode = DIRECT_32BIT;
/*TODO*///	else
        colormode = PALETTIZED_16BIT;

        Machine.pens = new int[total_colors * 4];//malloc(total_colors * sizeof(*Machine->pens));

        if (Machine.drv.color_table_len != 0) {
            Machine.game_colortable = new char[Machine.drv.color_table_len * 4];//malloc(Machine.drv.color_table_len * sizeof(*Machine.game_colortable));
            Machine.remapped_colortable = new IntArray(Machine.drv.color_table_len * 4);//malloc(Machine.drv.color_table_len * sizeof(*Machine.remapped_colortable));
        } else {
            Machine.game_colortable = null;
            Machine.remapped_colortable = new IntArray(Machine.pens);/* straight 1:1 mapping from palette to colortable */
        }

        if (colormode == PALETTIZED_16BIT)
	{
		palette_shadow_table = new char[total_colors*2];
		if (palette_shadow_table == null)
		{
			palette_stop();
			return 1;
		}
		for (int i = 0;i < total_colors;i++)
		{
			palette_shadow_table[i] = (char)i;
			if (((Machine.drv.video_attributes & VIDEO_HAS_SHADOWS)!=0) && (i < Machine.drv.total_colors))
				palette_shadow_table[i] += Machine.drv.total_colors;
		}
	}
	else
		palette_shadow_table = null;

/*TODO*///	if ((Machine->drv->color_table_len && (Machine->game_colortable == 0 || Machine->remapped_colortable == 0))
/*TODO*///			|| game_palette == 0 || actual_palette == 0 || brightness == 0
/*TODO*///			|| Machine->pens == 0 || Machine->debug_pens == 0 || Machine->debug_remapped_colortable == 0)
/*TODO*///	{
/*TODO*///		palette_stop();
/*TODO*///		return 1;
/*TODO*///	}
        for (int i = 0; i < Machine.drv.total_colors; i++) {
            brightness[i] = 1.0;
        }

        /*TODO*///
/*TODO*///	state_save_register_UINT8("palette", 0, "colors", game_palette, total_colors*3);
/*TODO*///	state_save_register_UINT8("palette", 0, "actual_colors", actual_palette, total_colors*3);
/*TODO*///	state_save_register_double("palette", 0, "brightness", brightness, Machine->drv->total_colors);
/*TODO*///	switch (colormode)
/*TODO*///	{
/*TODO*///		case PALETTIZED_16BIT:
/*TODO*///			state_save_register_func_postload(palette_reset_16_palettized);
/*TODO*///			break;
/*TODO*///		case DIRECT_15BIT:
/*TODO*///			state_save_register_func_postload(palette_reset_15_direct);
/*TODO*///			break;
/*TODO*///		case DIRECT_32BIT:
/*TODO*///			state_save_register_func_postload(palette_reset_32_direct);
/*TODO*///			break;
/*TODO*///	}
/*TODO*///
        return 0;
    }

    public static void palette_stop() {
        /*TODO*///	free(game_palette);
/*TODO*///	game_palette = 0;
/*TODO*///	free(actual_palette);
/*TODO*///	actual_palette = 0;
/*TODO*///	free(brightness);
/*TODO*///	brightness = 0;
/*TODO*///	if (Machine->game_colortable)
/*TODO*///	{
/*TODO*///		free(Machine->game_colortable);
/*TODO*///		Machine->game_colortable = 0;
/*TODO*///		/* remapped_colortable is malloc()ed only when game_colortable is, */
/*TODO*///		/* otherwise it just points to Machine->pens */
/*TODO*///		free(Machine->remapped_colortable);
/*TODO*///	}
/*TODO*///	Machine->remapped_colortable = 0;
/*TODO*///	free(Machine->debug_remapped_colortable);
/*TODO*///	Machine->debug_remapped_colortable = 0;
/*TODO*///	free(Machine->pens);
/*TODO*///	Machine->pens = 0;
/*TODO*///	free(Machine->debug_pens);
/*TODO*///	Machine->debug_pens = 0;
/*TODO*///	free(palette_shadow_table);
/*TODO*///	palette_shadow_table = 0;
/*TODO*///
/*TODO*///	palette_initialized = 0;
    }

    public static int palette_init() {
        int i;

        /* We initialize the palette and colortable to some default values so that */
 /* drivers which dynamically change the palette don't need a vh_init_palette() */
 /* function (provided the default color table fits their needs). */
        for (i = 0; i < total_colors; i++) {
            game_palette[3 * i + 0] = actual_palette[3 * i + 0] = (char) (((i & 1) >> 0) * 0xff);
            game_palette[3 * i + 1] = actual_palette[3 * i + 1] = (char) (((i & 2) >> 1) * 0xff);
            game_palette[3 * i + 2] = actual_palette[3 * i + 2] = (char) (((i & 4) >> 2) * 0xff);
        }

        /* Preload the colortable with a default setting, following the same */
 /* order of the palette. The driver can overwrite this in */
 /* vh_init_palette() */
        for (i = 0; i < (Machine.drv.color_table_len * 4); i++) {
            Machine.game_colortable[i] = (char) (i % total_colors);
        }

        /* now the driver can modify the default values if it wants to. */
        if (Machine.drv.init_palette != null) {
            if (memory_region(REGION_PROMS) != null)
                (Machine.drv.init_palette).handler(game_palette, Machine.game_colortable, new UBytePtr(memory_region(REGION_PROMS)));
            else
                (Machine.drv.init_palette).handler(game_palette, Machine.game_colortable, null);                
        }

        switch (colormode) {
            case PALETTIZED_16BIT: {
                if (osd_allocate_colors(total_colors, game_palette, null) != 0) {
                    return 1;
                }

                for (i = 0; i < total_colors; i++) {
                    Machine.pens[i] = i;
                }

                /* refresh the palette to support shadows in PROM games */
                for (i = 0; i < Machine.drv.total_colors; i++) {
                    palette_set_color(i, game_palette[3 * i + 0], game_palette[3 * i + 1], game_palette[3 * i + 2]);
                }
            }
            break;
            /*TODO*///
/*TODO*///		case DIRECT_15BIT:
/*TODO*///		{
/*TODO*///			const UINT8 rgbpalette[3*3] = { 0xff,0x00,0x00, 0x00,0xff,0x00, 0x00,0x00,0xff };
/*TODO*///
/*TODO*///			if (osd_allocate_colors(3,rgbpalette,direct_rgb_components,debug_palette,debug_pens))
/*TODO*///				return 1;
/*TODO*///
/*TODO*///			for (i = 0;i < total_colors;i++)
/*TODO*///				Machine->pens[i] =
/*TODO*///						(game_palette[3*i + 0] >> 3) * (direct_rgb_components[0] / 0x1f) +
/*TODO*///						(game_palette[3*i + 1] >> 3) * (direct_rgb_components[1] / 0x1f) +
/*TODO*///						(game_palette[3*i + 2] >> 3) * (direct_rgb_components[2] / 0x1f);
/*TODO*///
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		case DIRECT_32BIT:
/*TODO*///		{
/*TODO*///			const UINT8 rgbpalette[3*3] = { 0xff,0x00,0x00, 0x00,0xff,0x00, 0x00,0x00,0xff };
/*TODO*///
/*TODO*///			if (osd_allocate_colors(3,rgbpalette,direct_rgb_components,debug_palette,debug_pens))
/*TODO*///				return 1;
/*TODO*///
/*TODO*///			for (i = 0;i < total_colors;i++)
/*TODO*///				Machine->pens[i] =
/*TODO*///						game_palette[3*i + 0] * (direct_rgb_components[0] / 0xff) +
/*TODO*///						game_palette[3*i + 1] * (direct_rgb_components[1] / 0xff) +
/*TODO*///						game_palette[3*i + 2] * (direct_rgb_components[2] / 0xff);
/*TODO*///
/*TODO*///			break;
/*TODO*///		}
        }

        for (i = 0; i < Machine.drv.color_table_len; i++) {
            int color = Machine.game_colortable[i];

            /* check for invalid colors set by Machine->drv->vh_init_palette */
            if (color < total_colors) {
                Machine.remapped_colortable.write(i, Machine.pens[color]);
            } else {
                usrintf_showmessage("colortable[%d] (=%d) out of range (total_colors = %d)",
                        i, color, total_colors);
            }
        }

        palette_initialized = 1;

        return 0;
    }

    /*TODO*///
/*TODO*///
/*TODO*///INLINE void palette_set_color_15_direct(int color,UINT8 red,UINT8 green,UINT8 blue)
/*TODO*///{
/*TODO*///	if (	actual_palette[3*color + 0] == red &&
/*TODO*///			actual_palette[3*color + 1] == green &&
/*TODO*///			actual_palette[3*color + 2] == blue)
/*TODO*///		return;
/*TODO*///	actual_palette[3*color + 0] = red;
/*TODO*///	actual_palette[3*color + 1] = green;
/*TODO*///	actual_palette[3*color + 2] = blue;
/*TODO*///	Machine->pens[color] =
/*TODO*///			(red   >> 3) * (direct_rgb_components[0] / 0x1f) +
/*TODO*///			(green >> 3) * (direct_rgb_components[1] / 0x1f) +
/*TODO*///			(blue  >> 3) * (direct_rgb_components[2] / 0x1f);
/*TODO*///}
/*TODO*///
/*TODO*///static void palette_reset_15_direct(void)
/*TODO*///{
/*TODO*///	int color;
/*TODO*///	for(color = 0; color < total_colors; color++)
/*TODO*///		Machine->pens[color] =
/*TODO*///				(game_palette[3*color + 0]>>3) * (direct_rgb_components[0] / 0x1f) +
/*TODO*///				(game_palette[3*color + 1]>>3) * (direct_rgb_components[1] / 0x1f) +
/*TODO*///				(game_palette[3*color + 2]>>3) * (direct_rgb_components[2] / 0x1f);
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void palette_set_color_32_direct(int color,UINT8 red,UINT8 green,UINT8 blue)
/*TODO*///{
/*TODO*///	if (	actual_palette[3*color + 0] == red &&
/*TODO*///			actual_palette[3*color + 1] == green &&
/*TODO*///			actual_palette[3*color + 2] == blue)
/*TODO*///		return;
/*TODO*///	actual_palette[3*color + 0] = red;
/*TODO*///	actual_palette[3*color + 1] = green;
/*TODO*///	actual_palette[3*color + 2] = blue;
/*TODO*///	Machine->pens[color] =
/*TODO*///			red   * (direct_rgb_components[0] / 0xff) +
/*TODO*///			green * (direct_rgb_components[1] / 0xff) +
/*TODO*///			blue  * (direct_rgb_components[2] / 0xff);
/*TODO*///}
/*TODO*///
/*TODO*///static void palette_reset_32_direct(void)
/*TODO*///{
/*TODO*///	int color;
/*TODO*///	for(color = 0; color < total_colors; color++)
/*TODO*///		Machine->pens[color] =
/*TODO*///				game_palette[3*color + 0] * (direct_rgb_components[0] / 0xff) +
/*TODO*///				game_palette[3*color + 1] * (direct_rgb_components[1] / 0xff) +
/*TODO*///				game_palette[3*color + 2] * (direct_rgb_components[2] / 0xff);
/*TODO*///}
    public static void palette_set_color_16_palettized(int color, int/*UINT8*/ red, int/*UINT8*/ green, int/*UINT8*/ blue) {
        if (actual_palette[3 * color + 0] == red
                && actual_palette[3 * color + 1] == green
                && actual_palette[3 * color + 2] == blue) {
            return;
        }

        actual_palette[3 * color + 0] = (char) red;
        actual_palette[3 * color + 1] = (char) green;
        actual_palette[3 * color + 2] = (char) blue;

        if (palette_initialized != 0) {
            osd_modify_pen(Machine.pens[color], red, green, blue);
        }
    }

    /*TODO*///
/*TODO*///static void palette_reset_16_palettized(void)
/*TODO*///{
/*TODO*///	if (palette_initialized)
/*TODO*///	{
/*TODO*///		int color;
/*TODO*///		for (color=0; color<total_colors; color++)
/*TODO*///			osd_modify_pen(Machine->pens[color],
/*TODO*///						   game_palette[3*color + 0],
/*TODO*///						   game_palette[3*color + 1],
/*TODO*///						   game_palette[3*color + 2]);
/*TODO*///   }
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void adjust_shadow(UINT8 *r,UINT8 *g,UINT8 *b,double factor)
/*TODO*///{
/*TODO*///	if (factor > 1)
/*TODO*///	{
/*TODO*///		int max = *r;
/*TODO*///		if (*g > max) max = *g;
/*TODO*///		if (*b > max) max = *b;
/*TODO*///
/*TODO*///		if ((int)(max * factor + 0.5) >= 256)
/*TODO*///			factor = 255.0 / max;
/*TODO*///	}
/*TODO*///
/*TODO*///	*r = *r * factor + 0.5;
/*TODO*///	*g = *g * factor + 0.5;
/*TODO*///	*b = *b * factor + 0.5;
/*TODO*///}
    public static void palette_set_color(int color, int/*UINT8*/ r, int/*UINT8*/ g, int/*UINT8*/ b) {
        if (color >= total_colors) {
            logerror("error: palette_set_color() called with color %d, but only %d allocated.\n", color, total_colors);
            return;
        }

        game_palette[3 * color + 0] = (char) (r & 0xFFFF);
        game_palette[3 * color + 1] = (char) (g & 0xFFFF);
        game_palette[3 * color + 2] = (char) (b & 0xFFFF);

        if (color < Machine.drv.total_colors && brightness[color] != 1.0) {
            r = (int) (r * brightness[color] + 0.5) & 0xFFFF;
            g = (int) (g * brightness[color] + 0.5) & 0xFFFF;
            b = (int) (b * brightness[color] + 0.5) & 0xFFFF;
        }

        switch (colormode) {
            case PALETTIZED_16BIT:
                palette_set_color_16_palettized(color, r, g, b);
                break;
            /*TODO*///		case DIRECT_15BIT:
/*TODO*///			palette_set_color_15_direct(color,r,g,b);
/*TODO*///			break;
/*TODO*///		case DIRECT_32BIT:
/*TODO*///			palette_set_color_32_direct(color,r,g,b);
/*TODO*///			break;
        }
        /*TODO*///
/*TODO*///	if (color < Machine->drv->total_colors)
/*TODO*///	{
/*TODO*///		/* automatically create darker shade for shadow handling */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_HAS_SHADOWS)
/*TODO*///		{
/*TODO*///			UINT8 nr=r,ng=g,nb=b;
/*TODO*///
/*TODO*///			adjust_shadow(&nr,&ng,&nb,shadow_factor);
/*TODO*///
/*TODO*///			color += Machine->drv->total_colors;	/* carry this change over to highlight handling */
/*TODO*///			palette_set_color(color,nr,ng,nb);
/*TODO*///		}
/*TODO*///
/*TODO*///		/* automatically create brighter shade for highlight handling */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_HAS_HIGHLIGHTS)
/*TODO*///		{
/*TODO*///			UINT8 nr=r,ng=g,nb=b;
/*TODO*///
/*TODO*///			adjust_shadow(&nr,&ng,&nb,highlight_factor);
/*TODO*///
/*TODO*///			color += Machine->drv->total_colors;
/*TODO*///			palette_set_color(color,nr,ng,nb);
/*TODO*///		}
/*TODO*///	}
    }
    /*TODO*///
/*TODO*///void palette_get_color(int color,UINT8 *r,UINT8 *g,UINT8 *b)
/*TODO*///{
/*TODO*///	*r = game_palette[3*color + 0];
/*TODO*///	*g = game_palette[3*color + 1];
/*TODO*///	*b = game_palette[3*color + 2];
/*TODO*///}
/*TODO*///
/*TODO*///void palette_set_brightness(int color,double bright)
/*TODO*///{
/*TODO*///	if (brightness[color] != bright)
/*TODO*///	{
/*TODO*///		brightness[color] = bright;
/*TODO*///
/*TODO*///		palette_set_color(color,game_palette[3*color + 0],game_palette[3*color + 1],game_palette[3*color + 2]);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_set_shadow_factor(double factor)
/*TODO*///{
/*TODO*///	if (shadow_factor != factor)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///
/*TODO*///		shadow_factor = factor;
/*TODO*///
/*TODO*///		if (palette_initialized)
/*TODO*///		{
/*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///				palette_set_color(i,game_palette[3*i + 0],game_palette[3*i + 1],game_palette[3*i + 2]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_set_highlight_factor(double factor)
/*TODO*///{
/*TODO*///	if (highlight_factor != factor)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///
/*TODO*///		highlight_factor = factor;
/*TODO*///
/*TODO*///		for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///			palette_set_color(i,game_palette[3*i + 0],game_palette[3*i + 1],game_palette[3*i + 2]);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///


}
