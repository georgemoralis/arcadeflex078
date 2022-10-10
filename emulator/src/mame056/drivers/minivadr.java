/**
 * ported to v0.56
 * ported to v0.37b7
 */
package mame056.drivers;

import static arcadeflex.v078.drivers.minivadr.input_ports_minivadr;
import static arcadeflex.v078.drivers.minivadr.readmem;
import static arcadeflex.v078.drivers.minivadr.writemem;
import static arcadeflex.v078.mame.cpuint.irq0_line_hold;
import static arcadeflex.v078.mame.cpuintrfH.CPU_Z80;
import static arcadeflex056.fucPtr.*;

import static mame056.commonH.*;
import static mame056.cpuexecH.*;
import static mame056.driverH.*;
import static mame056.drawgfxH.*;
import static mame056.vidhrdw.minivadr.*;

public class minivadr {


    static MachineDriver machine_driver_minivadr = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        24000000 / 6, /* 4 MHz ? */
                        readmem, writemem, null, null,
                        irq0_line_hold, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, new rectangle(0, 256 - 1, 16, 240 - 1),
            null,
            2, 0,
            minivadr_init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            null,
            null,
            minivadr_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, null
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_minivadr = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("d26-01.bin", 0x0000, 0x2000, 0xa96c823d);
            ROM_END();
        }
    };

    public static GameDriver driver_minivadr = new GameDriver("1990", "minivadr", "minivadr.java", rom_minivadr, null, machine_driver_minivadr, input_ports_minivadr, null, ROT0, "Taito Corporation", "Minivader");
}
