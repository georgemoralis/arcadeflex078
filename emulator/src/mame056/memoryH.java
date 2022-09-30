/**
 * Ported to 0.56
 */
package mame056;

import arcadeflex.v078.generic.funcPtr.setopbase;
import static arcadeflex056.fucPtr.*;
import static common.ptr.*;
import static mame056.cpuintrfH.*;
import static mame056.memory.*;

public class memoryH {
 
/* ----- opcode reading ----- */
    public static char cpu_readop(int A) {
        return OP_ROM.read(A & mem_amask);
    }

    public static char cpu_readop16(int A){
        /*TODO*///(*(data16_t *)&OP_ROM[(A) & mem_amask]);
        return OP_ROM.read(A & mem_amask);
    }
/*TODO*///#define cpu_readop32(A)				(*(data32_t *)&OP_ROM[(A) & mem_amask])
/*TODO*///
/* ----- opcode argument reading ----- */
    public static char cpu_readop_arg(int A) {
        return OP_RAM.read(A & mem_amask);
    }

}
