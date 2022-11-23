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
import static arcadeflex.v078.mame.palette.*;
import static mame056.common.memory_region;
import static mame056.usrintrf.usrintf_showmessage;
import static arcadeflex036.osdepend.logerror;
import static arcadeflex056.fucPtr.*;

import static common.ptr.*;
import static mame056.driverH.VIDEO_HAS_SHADOWS;
import static mame056.mame.Machine;

import static arcadeflex.v078.mame.palette.*;

public class palette {

    /*TODO*///#define VERBOSE 0
    
    /*TODO*///
    
    
    
    /*TODO*///UINT32 direct_rgb_components[3];
/*TODO*///
/*TODO*///
/*TODO*///
    //public static char[] palette_shadow_table;
/*TODO*///
   
    
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
        
        System.out.println("Color Mode="+colormode);

        switch (colormode) {
            case PALETTIZED_16BIT: {
                if (osd_allocate_colors(total_colors, game_palette, null) != 0) {
                    return 1;
                }
                System.out.println("1");

                for (i = 0; i < total_colors; i++) {
                    Machine.pens[i] = i;
                }

                /* refresh the palette to support shadows in PROM games */
                for (i = 0; i < Machine.drv.total_colors; i++) {
                    palette_set_color(i, game_palette[3 * i + 0], game_palette[3 * i + 1], game_palette[3 * i + 2]);
                }
                System.out.println("2");
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
System.out.println("Machine.drv.color_table_len: "+Machine.drv.color_table_len);
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

        //palette_initialized = 1;

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
