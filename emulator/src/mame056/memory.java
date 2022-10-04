/**
 * Ported to 0.56
 */
package mame056;

import static arcadeflex.v078.mame.memory.*;
import static arcadeflex.v078.mame.memoryH.*;
import static common.ptr.*;
import static mame056.cpuintrfH.*;
import static mame056.cpuintrf.*;
import static mame056.cpuexecH.*;
import static mame056.common.*;
import static mame056.commonH.*;
import static mame056.driverH.*;
import static mame056.mame.Machine;

public class memory {

    /*-------------------------------------------------
            memory_find_base - return a pointer to the
            base of RAM associated with the given CPU
            and offset
    -------------------------------------------------*/
    public static UBytePtr memory_find_base(int cpunum, int offset) {

        int region = REGION_CPU1 + cpunum;

        /* look in external memory first */
        for (ExtMemory ext : ext_memory) {
            if (ext.data == null) {
                break;
            }
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		if (ext->region == region && ext->start <= offset && ext->end >= offset)
/*TODO*///			return (void *)((UINT8 *)ext->data + (offset - ext->start));
        }
        return new UBytePtr(cpudata[cpunum].rambase, offset);
    }

    /*-------------------------------------------------
	init_cpudata - initialize the cpudata
	structure for each CPU
-------------------------------------------------*/
   public static int init_cpudata() {
        int cpunum;

        /* zap the cpudata structure */
        for (int i = 0; i < MAX_CPU; i++) {
            cpudata[i] = new cpu_data();
        }
        /* loop over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            int cputype = Machine.drv.cpu[cpunum].cpu_type & ~CPU_FLAGS_MASK;

            /* set the RAM/ROM base */
            cpudata[cpunum].rambase =  cpudata[cpunum].op_ram = cpudata[cpunum].op_rom =memory_region(REGION_CPU1 + cpunum);
            cpudata[cpunum].opbase = null;
            /*TODO*///		encrypted_opcode_start[cpunum] = 0;
/*TODO*///		encrypted_opcode_end[cpunum] = 0;

            /* initialize the readmem and writemem tables */
            if (init_memport(cpunum, cpudata[cpunum].mem, mem_address_bits_of_cpu(cputype), cpunum_databus_width(cpunum), 1) == 0) {
                return 0;
            }

            /* initialize the readport and writeport tables */
            if (init_memport(cpunum, cpudata[cpunum].port, port_address_bits_of_cpu(cputype), cpunum_databus_width(cpunum), 0) == 0) {
                return 0;
            }

            /* Z80 port mask kludge */
            if (cputype == CPU_Z80) {
                if ((Machine.drv.cpu[cpunum].cpu_type & CPU_16BIT_PORT) == 0) {
                    cpudata[cpunum].port.mask = 0xff;
                }
            }

        }
        return 1;
    }




    /*-------------------------------------------------
    	get address bits from a read handler
    -------------------------------------------------*/
    public static int mem_address_bits_of_cpu(int cputype) {
        return cputype_get_interface(cputype).mem_address_bits_of_cpu();
    }

}
