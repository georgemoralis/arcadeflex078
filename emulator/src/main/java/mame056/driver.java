/**
 * ported to v0.56
 */
package mame056;

import static arcadeflex.v078.drivers._4enraya.driver_4enraya;
import static mame056.driverH.*;
import static mame056.drivers._1942.driver_1942;

import static mame056.drivers.minivadr.*;

public class driver {

    public static GameDriver drivers[] = {
        /**
         * Working
         */
        /*minivadr*/driver_minivadr,
        driver_1942,
        driver_4enraya,
        null
    };
}
