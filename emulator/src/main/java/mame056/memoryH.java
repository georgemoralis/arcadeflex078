/**
 * Ported to 0.56
 */
package mame056;

import static arcadeflex.v078.mame.memory.OP_RAM;
import static arcadeflex.v078.mame.memory.OP_ROM;
import static arcadeflex.v078.mame.memory.mem_amask;

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
