/**
 * ported to v0.56
 * ported to v0.37b7
 */
package mame056.drivers;

import static arcadeflex.v078.drivers.minivadr.input_ports_minivadr;
import static arcadeflex.v078.drivers.minivadr.machine_driver_minivadr;
import static arcadeflex056.fucPtr.*;

import static mame056.commonH.*;
import static arcadeflex.v078.mame.commonH.*;
import static mame056.driverH.*;

public class minivadr {


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
