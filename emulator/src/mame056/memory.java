/**
 * Ported to 0.56
 */
package mame056;

import arcadeflex.v078.generic.funcPtr.ReadHandlerPtr;
import arcadeflex.v078.generic.funcPtr.WriteHandlerPtr;
import static arcadeflex.v078.mame.memory.*;
import static arcadeflex.v078.mame.memoryH.*;
import static common.ptr.*;
import static common.libc.cstring.*;
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
	set_static_handler - handy shortcut for
	setting all 6 handlers for a given index
-------------------------------------------------*/
    static void set_static_handler(int idx,
            ReadHandlerPtr r8handler, /*read16_handler r16handler, read32_handler r32handler,*/
            WriteHandlerPtr w8handler/*, write16_handler w16handler, write32_handler w32handler*/) {

        /*TODO*///	rmemhandler8s[idx] = r8handler;
/*TODO*///	wmemhandler8s[idx] = w8handler;
/*TODO*///
        rmemhandler8[idx].handler = r8handler;
        /*TODO*///	rmemhandler16[idx].handler = (void *)r16handler;
/*TODO*///	rmemhandler32[idx].handler = (void *)r32handler;
        wmemhandler8[idx].handler = w8handler;
        /*TODO*///	wmemhandler16[idx].handler = (void *)w16handler;
/*TODO*///	wmemhandler32[idx].handler = (void *)w32handler;
/*TODO*///
        rporthandler8[idx].handler = r8handler;
        /*TODO*///	rporthandler16[idx].handler = (void *)r16handler;
/*TODO*///	rporthandler32[idx].handler = (void *)r32handler;
        wporthandler8[idx].handler = w8handler;
        /*TODO*///	wporthandler16[idx].handler = (void *)w16handler;
/*TODO*///	wporthandler32[idx].handler = (void *)w32handler;
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
	init_memport - initialize the mem/port data
	structure
    -------------------------------------------------*/
    static int init_memport(int cpunum, memport_data data, int abits, int dbits, int ismemory) {
        /* determine the address and data bits */
        data.cpunum = cpunum;
        data.abits = abits;
        data.dbits = dbits;
        data.ebits = abits - DATABITS_TO_SHIFT(dbits);
        data.mask = 0xffffffff >>> (32 - abits);

        /* allocate memory */
        data.read.table = new UBytePtr(1 << LEVEL1_BITS(data.ebits));
        data.write.table = new UBytePtr(1 << LEVEL1_BITS(data.ebits));

        /* initialize everything to unmapped */
        memset(data.read.table, STATIC_UNMAP, 1 << LEVEL1_BITS(data.ebits));
        memset(data.write.table, STATIC_UNMAP, 1 << LEVEL1_BITS(data.ebits));

        /* initialize the pointers to the handlers */
        if (ismemory != 0) {
            data.read.handlers = (dbits == 32) ? rmemhandler32 : (dbits == 16) ? rmemhandler16 : rmemhandler8;
            data.write.handlers = (dbits == 32) ? wmemhandler32 : (dbits == 16) ? wmemhandler16 : wmemhandler8;
        } else {
            data.read.handlers = (dbits == 32) ? rporthandler32 : (dbits == 16) ? rporthandler16 : rporthandler8;
            data.write.handlers = (dbits == 32) ? wporthandler32 : (dbits == 16) ? wporthandler16 : wporthandler8;
        }
        return 1;
    }

    /*-------------------------------------------------
            verify_memory - verify the memory structs
            and track which banks are referenced
    -------------------------------------------------*/
    public static int verify_memory() {
        int cpunum;

        /* zap the bank data */
        for (int i = 0; i < MAX_BANKS; i++) {
            bankdata[i] = new bank_data();//memset(&bankdata, 0, sizeof(bankdata));
        }

        /* loop over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {

            int width;
            int bank;

            /* determine the desired width */
            switch (cpunum_databus_width(cpunum)) {
                case 8:
                    width = MEMPORT_WIDTH_8;
                    break;
                case 16:
                    width = MEMPORT_WIDTH_16;
                    break;
                case 32:
                    width = MEMPORT_WIDTH_32;
                    break;
                default:
                    return fatalerror("cpu #%d has invalid memory width!\n", cpunum);
            }
            Object mra_obj = Machine.drv.cpu[cpunum].memory_read;
            Object mwa_obj = Machine.drv.cpu[cpunum].memory_write;

            /* verify the read handlers */
            if (mra_obj != null) {
                if (mra_obj instanceof Memory_ReadAddress[]) {
                    Memory_ReadAddress[] mra = (Memory_ReadAddress[]) mra_obj;
                    int mra_ptr = 0;
                    /* verify the MEMPORT_READ_START header */
                    if (mra[mra_ptr].start == MEMPORT_MARKER && mra[mra_ptr].end != 0) {
                        if ((mra[mra_ptr].end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_MEM) {
                            return fatalerror("cpu #%d has port handlers in place of memory read handlers!\n", cpunum);
                        }
                        if ((mra[mra_ptr].end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_READ) {
                            return fatalerror("cpu #%d has memory write handlers in place of memory read handlers!\n", cpunum);
                        }
                        if ((mra[mra_ptr].end & MEMPORT_WIDTH_MASK) != width) {
                            return fatalerror("cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n", cpunum, cpunum_databus_width(cpunum), mra[mra_ptr].end);
                        }
                        mra_ptr++;
                    }

                    /* track banks used */
                    for (; !IS_MEMPORT_END(mra[mra_ptr]); mra_ptr++) {
                        if (!IS_MEMPORT_MARKER(mra[mra_ptr]) && HANDLER_IS_BANK(mra[mra_ptr].handler)) {
                            bank = HANDLER_TO_BANK(mra[mra_ptr].handler);
                            bankdata[bank].used = 1;
                            bankdata[bank].cpunum = -1;
                        }
                    }
                } else {
                    //do the same for 16,32bit handlers
                    throw new UnsupportedOperationException("Unsupported");
                }
            }
            /* verify the write handlers */
            if (mwa_obj != null) {
                if (mwa_obj instanceof Memory_WriteAddress[]) {
                    Memory_WriteAddress[] mwa = (Memory_WriteAddress[]) mwa_obj;
                    int mwa_ptr = 0;
                    /* verify the MEMPORT_WRITE_START header */
                    if (mwa[mwa_ptr].start == MEMPORT_MARKER && mwa[mwa_ptr].end != 0) {
                        if ((mwa[mwa_ptr].end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_MEM) {
                            return fatalerror("cpu #%d has port handlers in place of memory write handlers!\n", cpunum);
                        }
                        if ((mwa[mwa_ptr].end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_WRITE) {
                            return fatalerror("cpu #%d has memory read handlers in place of memory write handlers!\n", cpunum);
                        }
                        if ((mwa[mwa_ptr].end & MEMPORT_WIDTH_MASK) != width) {
                            return fatalerror("cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n", cpunum, cpunum_databus_width(cpunum), mwa[mwa_ptr].end);
                        }
                        mwa_ptr++;
                    }

                    /*TODO*///
/*TODO*///			/* track banks used */
/*TODO*///			for (; !IS_MEMPORT_END(mwa); mwa++)
/*TODO*///				if (!IS_MEMPORT_MARKER(mwa) && HANDLER_IS_BANK(mwa->handler))
/*TODO*///				{
/*TODO*///					bank = HANDLER_TO_BANK(mwa->handler);
/*TODO*///					bankdata[bank].used = 1;
/*TODO*///					bankdata[bank].cpunum = -1;
/*TODO*///				}
/*TODO*///				mwa++;
                } else {
                    //do the same for 16,32bit handlers
                    throw new UnsupportedOperationException("Unsupported");
                }
            }

            /*TODO*///		const struct Memory_ReadAddress *mra = Machine->drv->cpu[cpunum].memory_read;
/*TODO*///		const struct Memory_WriteAddress *mwa = Machine->drv->cpu[cpunum].memory_write;
/*TODO*///
/*TODO*///		/* verify the read handlers */
/*TODO*///		if (mra)
/*TODO*///		{
/*TODO*///			/* verify the MEMPORT_READ_START header */
/*TODO*///			if (mra->start == MEMPORT_MARKER && mra->end != 0)
/*TODO*///			{
/*TODO*///				if ((mra->end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_MEM)
/*TODO*///					return fatalerror("cpu #%d has port handlers in place of memory read handlers!\n", cpunum);
/*TODO*///				if ((mra->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_READ)
/*TODO*///					return fatalerror("cpu #%d has memory write handlers in place of memory read handlers!\n", cpunum);
/*TODO*///				if ((mra->end & MEMPORT_WIDTH_MASK) != width)
/*TODO*///					return fatalerror("cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n", cpunum,cpunum_databus_width(cpunum),mra->end);
/*TODO*///				mra++;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* track banks used */
/*TODO*///			for ( ; !IS_MEMPORT_END(mra); mra++)
/*TODO*///				if (!IS_MEMPORT_MARKER(mra) && HANDLER_IS_BANK(mra->handler))
/*TODO*///				{
/*TODO*///					bank = HANDLER_TO_BANK(mra->handler);
/*TODO*///					bankdata[bank].used = 1;
/*TODO*///					bankdata[bank].cpunum = -1;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* verify the write handlers */
/*TODO*///		if (mwa)
/*TODO*///		{
/*TODO*///			/* verify the MEMPORT_WRITE_START header */
/*TODO*///			if (mwa->start == MEMPORT_MARKER && mwa->end != 0)
/*TODO*///			{
/*TODO*///				if ((mwa->end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_MEM)
/*TODO*///					return fatalerror("cpu #%d has port handlers in place of memory write handlers!\n", cpunum);
/*TODO*///				if ((mwa->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_WRITE)
/*TODO*///					return fatalerror("cpu #%d has memory read handlers in place of memory write handlers!\n", cpunum);
/*TODO*///				if ((mwa->end & MEMPORT_WIDTH_MASK) != width)
/*TODO*///					return fatalerror("cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n", cpunum,cpunum_databus_width(cpunum),mwa->end);
/*TODO*///				mwa++;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* track banks used */
/*TODO*///			for (; !IS_MEMPORT_END(mwa); mwa++)
/*TODO*///				if (!IS_MEMPORT_MARKER(mwa) && HANDLER_IS_BANK(mwa->handler))
/*TODO*///				{
/*TODO*///					bank = HANDLER_TO_BANK(mwa->handler);
/*TODO*///					bankdata[bank].used = 1;
/*TODO*///					bankdata[bank].cpunum = -1;
/*TODO*///				}
/*TODO*///				mwa++;
/*TODO*///		}
        }
        return 1;
    }

    /*-------------------------------------------------
    	verify_ports - verify the port structs
    -------------------------------------------------*/
    public static int verify_ports() {
        int cpunum;

        /* loop over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            int/*UINT32*/ width;

            /* determine the desired width */
            switch (cpunum_databus_width(cpunum)) {
                case 8:
                    width = MEMPORT_WIDTH_8;
                    break;
                case 16:
                    width = MEMPORT_WIDTH_16;
                    break;
                case 32:
                    width = MEMPORT_WIDTH_32;
                    break;
                default:
                    return fatalerror("cpu #%d has invalid memory width!\n", cpunum);
            }
            Object mra_obj = Machine.drv.cpu[cpunum].port_read;
            Object mwa_obj = Machine.drv.cpu[cpunum].port_write;

            /* verify the read handlers */
            if (mra_obj != null) {
                if (mra_obj instanceof IO_ReadPort[]) {
                    IO_ReadPort[] mra = (IO_ReadPort[]) mra_obj;
                    int mra_ptr = 0;
                    /* verify the PORT_READ_START header */
                    if (mra[mra_ptr].start == MEMPORT_MARKER && mra[mra_ptr].end != 0) {
                        if ((mra[mra_ptr].end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_IO) {
                            return fatalerror("cpu #%d has memory handlers in place of I/O read handlers!\n", cpunum);
                        }
                        if ((mra[mra_ptr].end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_READ) {
                            return fatalerror("cpu #%d has port write handlers in place of port read handlers!\n", cpunum);
                        }
                        if ((mra[mra_ptr].end & MEMPORT_WIDTH_MASK) != width) {
                            return fatalerror("cpu #%d uses wrong data width port handlers! (width = %d, memory = %08x)\n", cpunum, cpunum_databus_width(cpunum), mra[mra_ptr].end);
                        }
                    }
                } else {
                    //do the same for 16,32bit handlers
                    throw new UnsupportedOperationException("Unsupported");
                }
            }

            /* verify the write handlers */
            if (mwa_obj != null) {
                if (mwa_obj instanceof IO_WritePort[]) {
                    IO_WritePort[] mwa = (IO_WritePort[]) mwa_obj;
                    int mwa_ptr = 0;
                    /* verify the PORT_WRITE_START header */
                    if (mwa[mwa_ptr].start == MEMPORT_MARKER && mwa[mwa_ptr].end != 0) {
                        if ((mwa[mwa_ptr].end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_IO) {
                            return fatalerror("cpu #%d has memory handlers in place of I/O write handlers!\n", cpunum);
                        }
                        if ((mwa[mwa_ptr].end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_WRITE) {
                            return fatalerror("cpu #%d has port read handlers in place of port write handlers!\n", cpunum);
                        }
                        if ((mwa[mwa_ptr].end & MEMPORT_WIDTH_MASK) != width) {
                            return fatalerror("cpu #%d uses wrong data width port handlers! (width = %d, memory = %08x)\n", cpunum, cpunum_databus_width(cpunum), mwa[mwa_ptr].end);
                        }
                    }
                } else {
                    //do the same for 16,32bit handlers
                    throw new UnsupportedOperationException("Unsupported");
                }
            }

            /*TODO*///		const struct IO_ReadPort *mra = Machine->drv->cpu[cpunum].port_read;
            /*TODO*///		const struct IO_WritePort *mwa = Machine->drv->cpu[cpunum].port_write;
            /*TODO*///
            /*TODO*///		/* verify the read handlers */
            /*TODO*///		if (mra)
            /*TODO*///		{
            /*TODO*///			/* verify the PORT_READ_START header */
            /*TODO*///			if (mra->start == MEMPORT_MARKER && mra->end != 0)
            /*TODO*///			{
            /*TODO*///				if ((mra->end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_IO)
            /*TODO*///					return fatalerror("cpu #%d has memory handlers in place of I/O read handlers!\n", cpunum);
            /*TODO*///				if ((mra->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_READ)
            /*TODO*///					return fatalerror("cpu #%d has port write handlers in place of port read handlers!\n", cpunum);
            /*TODO*///				if ((mra->end & MEMPORT_WIDTH_MASK) != width)
            /*TODO*///					return fatalerror("cpu #%d uses wrong data width port handlers! (width = %d, memory = %08x)\n", cpunum,cpunum_databus_width(cpunum),mra->end);
            /*TODO*///			}
            /*TODO*///		}
            /*TODO*///
            /*TODO*///		/* verify the write handlers */
            /*TODO*///		if (mwa)
            /*TODO*///		{
            /*TODO*///			/* verify the PORT_WRITE_START header */
            /*TODO*///			if (mwa->start == MEMPORT_MARKER && mwa->end != 0)
            /*TODO*///			{
            /*TODO*///				if ((mwa->end & MEMPORT_TYPE_MASK) != MEMPORT_TYPE_IO)
            /*TODO*///					return fatalerror("cpu #%d has memory handlers in place of I/O write handlers!\n", cpunum);
            /*TODO*///				if ((mwa->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_WRITE)
            /*TODO*///					return fatalerror("cpu #%d has port read handlers in place of port write handlers!\n", cpunum);
            /*TODO*///				if ((mwa->end & MEMPORT_WIDTH_MASK) != width)
            /*TODO*///					return fatalerror("cpu #%d uses wrong data width port handlers! (width = %d, memory = %08x)\n", cpunum,cpunum_databus_width(cpunum),mwa->end);
            /*TODO*///			}
            /*TODO*///		}
        }
        return 1;
    }


    /*-------------------------------------------------
    	get address bits from a read handler
    -------------------------------------------------*/
    public static int mem_address_bits_of_cpu(int cputype) {
        return cputype_get_interface(cputype).mem_address_bits_of_cpu();
    }

    /*-------------------------------------------------
    	init_static - sets up the static memory
    	handlers
    -------------------------------------------------*/
    public static int init_static() {
        /*TODO*///	memset(rmemhandler8,  0, sizeof(rmemhandler8));
        /*TODO*///	memset(rmemhandler8s, 0, sizeof(rmemhandler8s));
        /*TODO*///	memset(rmemhandler16, 0, sizeof(rmemhandler16));
        /*TODO*///	memset(rmemhandler32, 0, sizeof(rmemhandler32));
        /*TODO*///	memset(wmemhandler8,  0, sizeof(wmemhandler8));
        /*TODO*///	memset(wmemhandler8s, 0, sizeof(wmemhandler8s));
        /*TODO*///	memset(wmemhandler16, 0, sizeof(wmemhandler16));
        /*TODO*///	memset(wmemhandler32, 0, sizeof(wmemhandler32));
        /*TODO*///
        /*TODO*///	memset(rporthandler8,  0, sizeof(rporthandler8));
        /*TODO*///	memset(rporthandler16, 0, sizeof(rporthandler16));
        /*TODO*///	memset(rporthandler32, 0, sizeof(rporthandler32));
        /*TODO*///	memset(wporthandler8,  0, sizeof(wporthandler8));
        /*TODO*///	memset(wporthandler16, 0, sizeof(wporthandler16));
        /*TODO*///	memset(wporthandler32, 0, sizeof(wporthandler32));
        /*TODO*///
        set_static_handler(STATIC_BANK1, mrh8_bank1, mwh8_bank1);/*TODO*///	set_static_handler(STATIC_BANK1,  mrh8_bank1,  NULL,         NULL,         mwh8_bank1,  NULL,         NULL);
        set_static_handler(STATIC_BANK2, mrh8_bank2, mwh8_bank2);/*TODO*///	set_static_handler(STATIC_BANK2,  mrh8_bank2,  NULL,         NULL,         mwh8_bank2,  NULL,         NULL);
        set_static_handler(STATIC_BANK3, mrh8_bank3, mwh8_bank3);/*TODO*///	set_static_handler(STATIC_BANK3,  mrh8_bank3,  NULL,         NULL,         mwh8_bank3,  NULL,         NULL);
        set_static_handler(STATIC_BANK4, mrh8_bank4, mwh8_bank4);/*TODO*///	set_static_handler(STATIC_BANK4,  mrh8_bank4,  NULL,         NULL,         mwh8_bank4,  NULL,         NULL);
        set_static_handler(STATIC_BANK5, mrh8_bank5, mwh8_bank5);/*TODO*///	set_static_handler(STATIC_BANK5,  mrh8_bank5,  NULL,         NULL,         mwh8_bank5,  NULL,         NULL);
        set_static_handler(STATIC_BANK6, mrh8_bank6, mwh8_bank6);/*TODO*///	set_static_handler(STATIC_BANK6,  mrh8_bank6,  NULL,         NULL,         mwh8_bank6,  NULL,         NULL);
        set_static_handler(STATIC_BANK7, mrh8_bank7, mwh8_bank7);/*TODO*///	set_static_handler(STATIC_BANK7,  mrh8_bank7,  NULL,         NULL,         mwh8_bank7,  NULL,         NULL);
        set_static_handler(STATIC_BANK8, mrh8_bank8, mwh8_bank8);/*TODO*///	set_static_handler(STATIC_BANK8,  mrh8_bank8,  NULL,         NULL,         mwh8_bank8,  NULL,         NULL);
        set_static_handler(STATIC_BANK9, mrh8_bank9, mwh8_bank9);/*TODO*///	set_static_handler(STATIC_BANK9,  mrh8_bank9,  NULL,         NULL,         mwh8_bank9,  NULL,         NULL);
        set_static_handler(STATIC_BANK10, mrh8_bank10, mwh8_bank10);/*TODO*///	set_static_handler(STATIC_BANK10, mrh8_bank10, NULL,         NULL,         mwh8_bank10, NULL,         NULL);
        set_static_handler(STATIC_BANK11, mrh8_bank11, mwh8_bank11);/*TODO*///	set_static_handler(STATIC_BANK11, mrh8_bank11, NULL,         NULL,         mwh8_bank11, NULL,         NULL);
        set_static_handler(STATIC_BANK12, mrh8_bank12, mwh8_bank12);/*TODO*///	set_static_handler(STATIC_BANK12, mrh8_bank12, NULL,         NULL,         mwh8_bank12, NULL,         NULL);
        set_static_handler(STATIC_BANK13, mrh8_bank13, mwh8_bank13);/*TODO*///	set_static_handler(STATIC_BANK13, mrh8_bank13, NULL,         NULL,         mwh8_bank13, NULL,         NULL);
        set_static_handler(STATIC_BANK14, mrh8_bank14, mwh8_bank14);/*TODO*///	set_static_handler(STATIC_BANK14, mrh8_bank14, NULL,         NULL,         mwh8_bank14, NULL,         NULL);
        set_static_handler(STATIC_BANK15, mrh8_bank15, mwh8_bank15);/*TODO*///	set_static_handler(STATIC_BANK15, mrh8_bank15, NULL,         NULL,         mwh8_bank15, NULL,         NULL);
        set_static_handler(STATIC_BANK16, mrh8_bank16, mwh8_bank16);/*TODO*///	set_static_handler(STATIC_BANK16, mrh8_bank16, NULL,         NULL,         mwh8_bank16, NULL,         NULL);
        set_static_handler(STATIC_BANK17, mrh8_bank17, mwh8_bank17);/*TODO*///	set_static_handler(STATIC_BANK17, mrh8_bank17, NULL,         NULL,         mwh8_bank17, NULL,         NULL);
        set_static_handler(STATIC_BANK18, mrh8_bank18, mwh8_bank18);/*TODO*///	set_static_handler(STATIC_BANK18, mrh8_bank18, NULL,         NULL,         mwh8_bank18, NULL,         NULL);
        set_static_handler(STATIC_BANK19, mrh8_bank19, mwh8_bank19);/*TODO*///	set_static_handler(STATIC_BANK19, mrh8_bank19, NULL,         NULL,         mwh8_bank19, NULL,         NULL);
        set_static_handler(STATIC_BANK20, mrh8_bank20, mwh8_bank20);/*TODO*///	set_static_handler(STATIC_BANK20, mrh8_bank20, NULL,         NULL,         mwh8_bank20, NULL,         NULL);
        set_static_handler(STATIC_BANK21, mrh8_bank21, mwh8_bank21);/*TODO*///	set_static_handler(STATIC_BANK21, mrh8_bank21, NULL,         NULL,         mwh8_bank21, NULL,         NULL);
        set_static_handler(STATIC_BANK22, mrh8_bank22, mwh8_bank22);/*TODO*///	set_static_handler(STATIC_BANK22, mrh8_bank22, NULL,         NULL,         mwh8_bank22, NULL,         NULL);
        set_static_handler(STATIC_BANK23, mrh8_bank23, mwh8_bank23);/*TODO*///	set_static_handler(STATIC_BANK23, mrh8_bank23, NULL,         NULL,         mwh8_bank23, NULL,         NULL);
        set_static_handler(STATIC_BANK24, mrh8_bank24, mwh8_bank24);/*TODO*///	set_static_handler(STATIC_BANK24, mrh8_bank24, NULL,         NULL,         mwh8_bank24, NULL,         NULL);
        set_static_handler(STATIC_UNMAP, mrh8_bad, mwh8_bad);/*TODO*///	set_static_handler(STATIC_UNMAP,  mrh8_bad,    mrh16_bad,    mrh32_bad,    mwh8_bad,    mwh16_bad,    mwh32_bad);
        set_static_handler(STATIC_NOP, mrh8_nop, mwh8_nop);/*TODO*///	set_static_handler(STATIC_NOP,    mrh8_nop,    mrh16_nop,    mrh32_nop,    mwh8_nop,    mwh16_nop,    mwh32_nop);
        set_static_handler(STATIC_RAM, mrh8_ram, mwh8_ram);/*TODO*///	set_static_handler(STATIC_RAM,    mrh8_ram,    NULL,         NULL,         mwh8_ram,    NULL,         NULL);
        set_static_handler(STATIC_ROM, null, mwh8_rom);/*TODO*///	set_static_handler(STATIC_ROM,    NULL,        NULL,         NULL,         mwh8_rom,    mwh16_rom,    mwh32_rom);
        set_static_handler(STATIC_RAMROM, null, mwh8_ramrom);/*TODO*///	set_static_handler(STATIC_RAMROM, NULL,        NULL,         NULL,         mwh8_ramrom, mwh16_ramrom, mwh32_ramrom);

        /* override port unmapped handlers */
        rporthandler8[STATIC_UNMAP].handler = prh8_bad;
        /*TODO*///	rporthandler16[STATIC_UNMAP].handler = (void *)prh16_bad;
        /*TODO*///	rporthandler32[STATIC_UNMAP].handler = (void *)prh32_bad;
        wporthandler8[STATIC_UNMAP].handler = pwh8_bad;
        /*TODO*///	wporthandler16[STATIC_UNMAP].handler = (void *)pwh16_bad;
        /*TODO*///	wporthandler32[STATIC_UNMAP].handler = (void *)pwh32_bad;
        /*TODO*///
        return 1;
    }

}
