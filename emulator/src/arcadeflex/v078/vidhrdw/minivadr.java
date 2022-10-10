/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */
package arcadeflex.v078.vidhrdw;

public class minivadr {

    /*TODO*///	/*******************************************************************
/*TODO*///	
/*TODO*///		Palette Setting.
/*TODO*///	
/*TODO*///	*******************************************************************/
/*TODO*///	
/*TODO*///	PALETTE_INIT( minivadr )
/*TODO*///	{
/*TODO*///		palette_set_color(0,0x00,0x00,0x00);
/*TODO*///		palette_set_color(1,0xff,0xff,0xff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*******************************************************************
/*TODO*///	
/*TODO*///		Draw Pixel.
/*TODO*///	
/*TODO*///	*******************************************************************/
/*TODO*///	WRITE_HANDLER( minivadr_videoram_w )
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		int x, y;
/*TODO*///		int color;
/*TODO*///	
/*TODO*///	
/*TODO*///		videoram[offset] = data;
/*TODO*///	
/*TODO*///		x = (offset % 32) * 8;
/*TODO*///		y = (offset / 32);
/*TODO*///	
/*TODO*///		if (x >= Machine->visible_area.min_x &&
/*TODO*///				x <= Machine->visible_area.max_x &&
/*TODO*///				y >= Machine->visible_area.min_y &&
/*TODO*///				y <= Machine->visible_area.max_y)
/*TODO*///		{
/*TODO*///			for (i = 0; i < 8; i++)
/*TODO*///			{
/*TODO*///				color = Machine->pens[((data >> i) & 0x01)];
/*TODO*///	
/*TODO*///				plot_pixel(tmpbitmap, x + (7 - i), y, color);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	VIDEO_UPDATE( minivadr )
/*TODO*///	{
/*TODO*///		if (get_vh_global_attribute_changed())
/*TODO*///		{
/*TODO*///			int offs;
/*TODO*///	
/*TODO*///			/* redraw bitmap */
/*TODO*///	
/*TODO*///			for (offs = 0; offs < videoram_size; offs++)
/*TODO*///				minivadr_videoram_w(offs,videoram[offs]);
/*TODO*///		}
/*TODO*///		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->visible_area,TRANSPARENCY_NONE,0);
/*TODO*///	}
}
