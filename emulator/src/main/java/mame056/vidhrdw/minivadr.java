/**
 * ported to v0.56
 * ported to v0.37b7
 * ported to v0.36
 */
package mame056.vidhrdw;

import arcadeflex.v078.generic.funcPtr.WriteHandlerPtr;
import static arcadeflex056.fucPtr.*;

import static common.ptr.*;

import static mame056.drawgfx.*;
import static mame056.mame.*;
import static mame056.commonH.*;
import static mame056.vidhrdw.generic.*;

public class minivadr {

    /**
     * *****************************************************************
     *
     * Palette Setting.
     *
     ******************************************************************
     */
    static char minivadr_palette[]
            = {
                0x00, 0x00, 0x00, /* black */
                0xff, 0xff, 0xff /* white */};

    public static VhConvertColorPromPtr palette_init_minivadr = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {

            //memcpy(game_palette, minivadr_palette, sizeof(minivadr_palette));
            for (int i = 0; i < minivadr_palette.length; i++) {
                palette[i] = minivadr_palette[i];
            }
        }
    };

    /**
     * *****************************************************************
     *
     * Draw Pixel.
     *
     ******************************************************************
     */
    public static WriteHandlerPtr minivadr_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;
            int x, y;
            int color;

            videoram.write(offset, data);

            x = (offset % 32) * 8;
            y = (offset / 32);

            if (x >= Machine.visible_area.min_x
                    && x <= Machine.visible_area.max_x
                    && y >= Machine.visible_area.min_y
                    && y <= Machine.visible_area.max_y) {
                for (i = 0; i < 8; i++) {
                    color = Machine.pens[((data >> i) & 0x01)];

                    plot_pixel.handler(Machine.scrbitmap, x + (7 - i), y, color);
                }
            }
        }
    };

    public static VhUpdatePtr video_update_minivadr = new VhUpdatePtr() {
        public void handler(mame_bitmap bitmap, int full_refresh) {
            if (full_refresh != 0) {
                int offs;

                /* redraw bitmap */
                for (offs = 0; offs < videoram_size[0]; offs++) {
                    minivadr_videoram_w.handler(offs, videoram.read(offs));
                }
            }
        }
    };

}