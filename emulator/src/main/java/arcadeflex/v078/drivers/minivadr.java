/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */
package arcadeflex.v078.drivers;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuint.*;
import static arcadeflex.v078.mame.cpuintrfH.CPU_Z80;
import static arcadeflex.v078.mame.driverH.*;
import static arcadeflex.v078.mame.inptportH.IPT_BUTTON1;
import static arcadeflex.v078.mame.inptportH.IPT_COIN1;
import static arcadeflex.v078.mame.inptportH.IPT_JOYSTICK_LEFT;
import static arcadeflex.v078.mame.inptportH.IPT_JOYSTICK_RIGHT;
import static arcadeflex.v078.mame.inptportH.IPT_UNKNOWN;
import static arcadeflex.v078.mame.memoryH.*;
import static arcadeflex.v078.vidhrdw.generic.video_start_generic;
import static arcadeflex.v078.vidhrdw.generic.videoram;
import static arcadeflex.v078.vidhrdw.generic.videoram_size;

import arcadeflex056.fucPtr.InputPortPtr;
import static mame056.driverH.DEFAULT_60HZ_VBLANK_DURATION;
import static mame056.driverH.VIDEO_TYPE_RASTER;
import static mame056.inptport.input_port_0_r;
import static mame056.inptportH.INPUT_PORTS_END;
import static mame056.inptportH.IP_ACTIVE_LOW;
import static mame056.inptportH.PORT_BIT;
import static mame056.inptportH.PORT_START;
import static mame056.vidhrdw.minivadr.minivadr_videoram_w;
import static mame056.vidhrdw.minivadr.palette_init_minivadr;
import static mame056.vidhrdw.minivadr.video_update_minivadr;

public class minivadr {

    public static Memory_ReadAddress readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_ReadAddress(0x0000, 0x1fff, MRA_ROM),
        new Memory_ReadAddress(0xa000, 0xbfff, MRA_RAM),
        new Memory_ReadAddress(0xe008, 0xe008, input_port_0_r),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_WriteAddress(0x0000, 0x1fff, MWA_ROM),
        new Memory_WriteAddress(0xa000, 0xbfff, minivadr_videoram_w, videoram, videoram_size),
        new Memory_WriteAddress(0xe008, 0xe008, MWA_NOP), // ???
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static InputPortPtr input_ports_minivadr = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    public static MachineHandlerPtr machine_driver_minivadr = new MachineHandlerPtr() {/* basic machine hardware */
        public void handler(InternalMachineDriver machine) {
            MACHINE_DRIVER_START(machine);
            /* basic machine hardware */
            MDRV_CPU_ADD(CPU_Z80, 24000000 / 6);
            /* 4 MHz ? */
            MDRV_CPU_MEMORY(readmem, writemem);
            MDRV_CPU_VBLANK_INT(irq0_line_hold, 1);

            MDRV_FRAMES_PER_SECOND(60);
            MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION);

            /* video hardware */
            MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER);
            MDRV_SCREEN_SIZE(256, 256);
            MDRV_VISIBLE_AREA(0, 256 - 1, 16, 240 - 1);
            MDRV_PALETTE_LENGTH(2);

            MDRV_PALETTE_INIT(palette_init_minivadr);
            MDRV_VIDEO_START(video_start_generic);
            MDRV_VIDEO_UPDATE(video_update_minivadr);

            /* sound hardware */
            MACHINE_DRIVER_END();
        }
    };
    /*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	  Game driver(s)
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	ROM_START( minivadr )
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
/*TODO*///		ROM_LOAD( "d26-01.bin",	0x0000, 0x2000, CRC(a96c823d) SHA1(aa9969ff80e94b0fff0f3530863f6b300510162e) )
/*TODO*///	ROM_END
/*TODO*///	
/*TODO*///	
/*TODO*///	GAME( 1990, minivadr, 0, minivadr, minivadr, 0, ROT0, "Taito Corporation", "Minivader" )
}
