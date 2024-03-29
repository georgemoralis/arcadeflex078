/** *************************************************************************
 *
 * vidhrdw.c
 *
 * Functions to emulate the video hardware of the machine.
 *
 ************************************************************************** */

/*
 * ported to v0.56
 * using automatic conversion tool v0.01
 */
package mame056.vidhrdw;

import static arcadeflex.v078.generic.funcPtr.*;
import static arcadeflex.v078.mame.cpuexecH.*;
import static arcadeflex.v078.mame.cpuintrfH.*;
import arcadeflex.v078.mame.drawgfxH.rectangle;
import static arcadeflex.v078.mame.tilemapH.*;
import static arcadeflex056.fucPtr.*;
import common.ptr.UBytePtr;
import static mame056.common.coin_counter_w;
import static mame056.mame.*;
import static arcadeflex.v078.mame.tilemapC.*;
//import static mame056.tilemapH.*;
import static arcadeflex.v078.vidhrdw.generic.*;
import static mame056.commonH.flip_screen;
import mame056.commonH.mame_bitmap;
import static mame056.drawgfx.drawgfx;
import static mame056.drawgfxH.TRANSPARENCY_PEN;

import static arcadeflex.v078.mame.palette.palette_set_color;

public class _1942 {

    public static UBytePtr c1942_fgvideoram = new UBytePtr();
    public static UBytePtr c1942_bgvideoram = new UBytePtr();

    static int c1942_palette_bank;
    static struct_tilemap fg_tilemap, bg_tilemap;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * 1942 has three 256x4 palette PROMs (one per gun) and three 256x4 lookup
     * table PROMs (one for characters, one for sprites, one for background
     * tiles). The palette PROMs are connected to the RGB output this way:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static int TOTAL_COLORS(int gfxn) {
        return (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity);
    }

    public static void COLOR(char[] colortable, int gfxn, int offs, int value) {
        (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs]) = (char) value;
    }

    public static PaletteInitHandlerPtr c1942_vh_convert_color_prom = new PaletteInitHandlerPtr() {
        @Override
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

            color_prom.inc(3 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 128-143 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                COLOR(colortable, 0, i, color_prom.readinc() + 128);
            }

            /* background tiles use colors 0-63 in four banks */
            for (i = 0; i < TOTAL_COLORS(1) / 4; i++) {
                COLOR(colortable, 1, i, color_prom.read());
                COLOR(colortable, 1, i + 32 * 8, color_prom.read() + 16);
                COLOR(colortable, 1, i + 2 * 32 * 8, color_prom.read() + 32);
                COLOR(colortable, 1, i + 3 * 32 * 8, color_prom.read() + 48);
                color_prom.inc();
            }

            /* sprites use colors 64-79 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                COLOR(colortable, 2, i, color_prom.readinc() + 64);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() {
        public void handler(int tile_index) {

            int code, color;

            code = c1942_fgvideoram.read(tile_index);
            color = c1942_fgvideoram.read(tile_index + 0x400);
            SET_TILE_INFO(
                    0,
                    code + ((color & 0x80) << 1),
                    color & 0x3f,
                    0);
        }
    };

    static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() {
        public void handler(int tile_index) {
            int code, color;

            tile_index = (tile_index & 0x0f) | ((tile_index & 0x01f0) << 1);

            //System.out.println(c1942_bgvideoram.memory.length);
            code = c1942_bgvideoram.read(tile_index);
            color = c1942_bgvideoram.read(tile_index + 0x10);
            SET_TILE_INFO(
                    1,
                    code + ((color & 0x80) << 1),
                    (color & 0x1f) + (0x20 * c1942_palette_bank),
                    TILE_FLIPYX((color & 0x60) >> 5));
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VideoStartHandlerPtr c1942_vh_start = new VideoStartHandlerPtr() {
        public int handler() {
            fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);
            bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_cols, TILEMAP_OPAQUE, 16, 16, 32, 16);

            if (fg_tilemap == null || bg_tilemap == null) {
                return 1;
            }

            /*TODO*///		tilemap_set_transparent_pen(fg_tilemap,0);
            fg_tilemap.transparent_pen = 0;

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */
    public static WriteHandlerPtr c1942_fgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            c1942_fgvideoram.write(offset, data);
            tilemap_mark_tile_dirty(fg_tilemap, offset & 0x3ff);
        }
    };

    public static WriteHandlerPtr c1942_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            c1942_bgvideoram.write(offset, data);
            tilemap_mark_tile_dirty(bg_tilemap, (offset & 0x0f) | ((offset >> 1) & 0x01f0));
        }
    };

    public static WriteHandlerPtr c1942_palette_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (c1942_palette_bank != data) {
                c1942_palette_bank = data;
                tilemap_mark_all_tiles_dirty(bg_tilemap);
            }
        }
    };

    static int[] scroll = new int[2];

    public static WriteHandlerPtr c1942_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll[offset] = data;
            tilemap_set_scrollx(bg_tilemap, 0, scroll[0] | (scroll[1] << 8));
        }
    };

    public static WriteHandlerPtr c1942_c804_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 7: flip screen
	       bit 4: cpu B reset
		   bit 0: coin counter */

            coin_counter_w.handler(0, data & 0x01);

            cpu_set_reset_line(1, (data & 0x10) != 0 ? ASSERT_LINE : CLEAR_LINE);

            flip_screen_set(data & 0x80);
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
    static void draw_sprites(mame_bitmap bitmap, rectangle cliprect) {
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            int i, code, col, sx, sy, dir;

            code = (spriteram.read(offs) & 0x7f) + 4 * (spriteram.read(offs + 1) & 0x20)
                    + 2 * (spriteram.read(offs) & 0x80);
            col = spriteram.read(offs + 1) & 0x0f;
            sx = spriteram.read(offs + 3) - 0x10 * (spriteram.read(offs + 1) & 0x10);
            sy = spriteram.read(offs + 2);
            dir = 1;
            if (flip_screen() != 0) {
                sx = 240 - sx;
                sy = 240 - sy;
                dir = -1;
            }

            /* handle double / quadruple height */
            i = (spriteram.read(offs + 1) & 0xc0) >> 6;
            if (i == 2) {
                i = 3;
            }

            do {
                drawgfx(bitmap, Machine.gfx[2],
                        code + i, col,
                        flip_screen(), flip_screen(),
                        sx, sy + 16 * i * dir,
                        cliprect, TRANSPARENCY_PEN, 15);

                i--;
            } while (i >= 0);
        }

    }

    public static VideoUpdateHandlerPtr c1942_vh_screenrefresh = new VideoUpdateHandlerPtr() {
        public void handler(mame_bitmap bitmap, rectangle cliprect) {
            tilemap_draw(bitmap, cliprect, bg_tilemap, 0, 0);
            draw_sprites(bitmap, cliprect);
            tilemap_draw(bitmap, cliprect, fg_tilemap, 0, 0);
        }
    };
}
