/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.v078.vidhrdw;

import static arcadeflex.v078.generic.funcPtr.*;
import arcadeflex.v078.mame.drawgfxH.rectangle;
import static arcadeflex.v078.mame.tilemapC.*;
import static arcadeflex.v078.mame.tilemapH.*;
import static arcadeflex056.fucPtr.*;
import static mame056.commonH.*;
import static mame056.vidhrdw.generic.*;

public class _4enraya
{
	
	static struct_tilemap tilemap;
	
	public static WriteHandlerPtr fenraya_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
        
		videoram.write((offset&0x3ff)*2, data);
		videoram.write((offset&0x3ff)*2+1, (offset&0xc00)>>10);
		tilemap_mark_tile_dirty(tilemap,offset&0x3ff);
	} };
	
	static GetTileInfoPtr get_tile_info = new GetTileInfoPtr() {
            public void handler(int tile_index) {
                System.out.println("get_tile_info!!!!");
		int code = videoram.read(tile_index*2)+(videoram.read(tile_index*2+1)<<8);
		SET_TILE_INFO(
			0,
			code,
			0,
			0);
	} };
	
	public static VhStartPtr video_start_4enraya = new VhStartPtr() {
            @Override
            public int handler() {
                tilemap = (struct_tilemap) tilemap_create( get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32 );
		return video_start_generic.handler();
            }
        };
		
	public static VhUpdatePtr video_update_4enraya = new VhUpdatePtr() { public void handler(mame_bitmap bitmap,rectangle cliprect) 
	{
                tilemap_draw(bitmap,cliprect,tilemap, 0,0);
                tilemap_draw(bitmap, cliprect, tilemap, 0, 0);
            }
        };
	
}
