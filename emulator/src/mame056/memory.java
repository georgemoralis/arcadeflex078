/**
 * Ported to 0.56
 */
package mame056;

import arcadeflex.v078.generic.funcPtr.ReadHandlerPtr;
import arcadeflex.v078.generic.funcPtr.WriteHandlerPtr;
import arcadeflex.v078.generic.funcPtr.setopbase;
import static arcadeflex.v078.mame.memory.*;
import static arcadeflex.v078.mame.memoryH.*;
import static common.ptr.*;
import static common.libc.cstring.*;
import static arcadeflex036.osdepend.*;
import static mame056.cpuintrfH.*;
import static mame056.cpuintrf.*;
import static mame056.cpuexecH.*;
import static mame056.common.*;
import static mame056.commonH.*;
import static mame056.driverH.*;
import static mame056.mame.Machine;
import static common.libc.cstdio.*;

public class memory {


    /*-------------------------------------------------
	memory_init - initialize the memory system
    -------------------------------------------------*/
    public static int memory_init() {
        /* init the static handlers */
        if (init_static() == 0) {
            return 0;
        }

        /* init the CPUs */
        if (init_cpudata() == 0) {
            return 0;
        }

        /* verify the memory handlers and check banks */
        if (verify_memory() == 0) {
            return 0;
        }
        if (verify_ports() == 0) {
            return 0;
        }

        /* allocate memory for sparse address spaces */
        if (allocate_memory() == 0) {
            return 0;
        }

        /* then fill in the tables */
        if (populate_memory() == 0) {
            return 0;
        }
        if (populate_ports() == 0) {
            return 0;
        }
        /*TODO*///	register_banks();
        /* dump the final memory configuration */
        mem_dump();
        return 1;
    }

    /*-------------------------------------------------
	memory_shutdown - free memory
    -------------------------------------------------*/
    public static void memory_shutdown() {
        /*TODO*///	struct ExtMemory *ext;
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	/* free all the tables */
/*TODO*///	for (cpunum = 0; cpunum < MAX_CPU; cpunum++ )
/*TODO*///	{
/*TODO*///		if (cpudata[cpunum].mem.read.table)
/*TODO*///			free(cpudata[cpunum].mem.read.table);
/*TODO*///		if (cpudata[cpunum].mem.write.table)
/*TODO*///			free(cpudata[cpunum].mem.write.table);
/*TODO*///		if (cpudata[cpunum].port.read.table)
/*TODO*///			free(cpudata[cpunum].port.read.table);
/*TODO*///		if (cpudata[cpunum].port.write.table)
/*TODO*///			free(cpudata[cpunum].port.write.table);
/*TODO*///	}
/*TODO*///	memset(&cpudata, 0, sizeof(cpudata));
/*TODO*///
/*TODO*///	/* free all the external memory */
/*TODO*///	for (ext = ext_memory; ext->data; ext++)
/*TODO*///		free(ext->data);
/*TODO*///	memset(ext_memory, 0, sizeof(ext_memory));
    }



  
    /*-------------------------------------------------
            memory_set_bankhandler_r - set readmemory
            handler for bank memory (8-bit only!)
    -------------------------------------------------*/
    public static void memory_set_bankhandler_r(int bank, int offset, int handler){
        ReadHandlerPtr _handler=new ReadHandlerPtr() {
            public int handler(int offset) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        memory_set_bankhandler_r(bank, offset, handler, _handler);
    }
    
    public static void memory_set_bankhandler_r(int bank, int offset, ReadHandlerPtr _handler){
        memory_set_bankhandler_r(bank, offset, -15000, _handler);
    }
    
    public static void memory_set_bankhandler_r(int bank, int offset, int handler, ReadHandlerPtr _handler)
    {
            /* determine the new offset */
            if (HANDLER_IS_RAM(handler) || HANDLER_IS_ROM(handler)){
                    rmemhandler8[bank].offset = 0 - offset;
                    handler = STATIC_RAM;
            } else if (HANDLER_IS_BANK(handler)) {
                    rmemhandler8[bank].offset = bankdata[HANDLER_TO_BANK(handler)].readoffset - offset;
            } else {
                    rmemhandler8[bank].offset = bankdata[bank].readoffset - offset;
            }

            /* set the new handler */
            if (HANDLER_IS_STATIC(handler))
                    _handler = (ReadHandlerPtr) rmemhandler8[handler].handler;
            rmemhandler8[bank].handler = _handler;
    }


    /*-------------------------------------------------
            memory_set_bankhandler_w - set writememory
            handler for bank memory (8-bit only!)
    -------------------------------------------------*/
    public static void memory_set_bankhandler_w(int bank, int offset, WriteHandlerPtr _handler){
        memory_set_bankhandler_w(bank, offset, -15000, _handler);
    }
    
    public static void memory_set_bankhandler_w(int bank, int offset, int handler){
        WriteHandlerPtr _handler = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        memory_set_bankhandler_w(bank, offset, handler, _handler);
    }
            
    public static void memory_set_bankhandler_w(int bank, int offset, int handler, WriteHandlerPtr _handler)
    {
            /* determine the new offset */
            if (HANDLER_IS_RAM(handler) || HANDLER_IS_ROM(handler) || HANDLER_IS_RAMROM(handler))
                    wmemhandler8[bank].offset = 0 - offset;
            else if (HANDLER_IS_BANK(handler))
                    wmemhandler8[bank].offset = bankdata[HANDLER_TO_BANK(handler)].writeoffset - offset;
            else
                    wmemhandler8[bank].offset = bankdata[bank].writeoffset - offset;

            /* set the new handler */
            if (HANDLER_IS_STATIC(handler))
                    _handler = (WriteHandlerPtr) wmemhandler8[handler].handler;
            wmemhandler8[bank].handler = _handler;
    }


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
    	allocate_memory - allocate memory for
    	sparse CPU address spaces
    -------------------------------------------------*/
    public static int allocate_memory() {
        int ext = 0;//struct ExtMemory *ext = ext_memory;
        int cpunum;

        /* don't do it for drivers that don't have ROM (MESS needs this) */
        if (Machine.gamedrv.rom == null) {
            return 1;
        }

        /* loop over all CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            int region = REGION_CPU1 + cpunum;
            int region_length = memory_region(region) != null ? memory_region_length(region) : 0;
            int size = region_length;

            /* keep going until we break out */
            while (true) {
                int lowest = Integer.MAX_VALUE, end = 0, lastend;
                Object mra_obj = Machine.drv.cpu[cpunum].memory_read;
                Object mwa_obj = Machine.drv.cpu[cpunum].memory_write;
                if (mra_obj instanceof Memory_ReadAddress[]) {
                    Memory_ReadAddress[] mra = (Memory_ReadAddress[]) mra_obj;
                    int mra_ptr = 0;
                    /* find the base of the lowest memory region that extends past the end */
                    for (mra_ptr = 0; !IS_MEMPORT_END(mra[mra_ptr]); mra_ptr++) {
                        if (!IS_MEMPORT_MARKER(mra[mra_ptr])) {
                            if (mra[mra_ptr].end >= size && mra[mra_ptr].start < lowest && needs_ram(cpunum, mra[mra_ptr].handler, mra[mra_ptr]._handler)) {
                                lowest = mra[mra_ptr].start;
                            }
                        }
                    }
                    Memory_WriteAddress[] mwa = (Memory_WriteAddress[]) mwa_obj;
                    int mwa_ptr = 0;

                    for (mwa_ptr = 0; !IS_MEMPORT_END(mwa[mwa_ptr]); mwa_ptr++) {
                        if (!IS_MEMPORT_MARKER(mwa[mwa_ptr])) {
                            if (mwa[mwa_ptr].end >= size && mwa[mwa_ptr].start < lowest && (mwa[mwa_ptr].base != null || needs_ram(cpunum, mwa[mwa_ptr].handler, mwa[mwa_ptr]._handler))) {
                                lowest = mwa[mwa_ptr].start;
                            }
                        }
                    }

                    /* done if nothing found */
                    if (lowest == Integer.MAX_VALUE) {
                        break;
                    }
                    throw new UnsupportedOperationException("Unimplemented");

                    /*TODO*///			/* now loop until we find the end of this contiguous block of memory */
                    /*TODO*///			lastend = ~0;
                    /*TODO*///			end = lowest;
                    /*TODO*///			while (end != lastend)
                    /*TODO*///			{
                    /*TODO*///				lastend = end;
                    /*TODO*///
                    /*TODO*///				/* find the end of the contiguous block of memory */
                    /*TODO*///				for (mra = Machine->drv->cpu[cpunum].memory_read; !IS_MEMPORT_END(mra); mra++)
                    /*TODO*///					if (!IS_MEMPORT_MARKER(mra))
                    /*TODO*///						if (mra->start <= end+1 && mra->end > end && needs_ram(cpunum, (void *)mra->handler))
                    /*TODO*///							end = mra->end;
                    /*TODO*///
                    /*TODO*///				for (mwa = Machine->drv->cpu[cpunum].memory_write; !IS_MEMPORT_END(mwa); mwa++)
                    /*TODO*///					if (!IS_MEMPORT_MARKER(mwa))
                    /*TODO*///						if (mwa->start <= end+1 && mwa->end > end && (mwa->base || needs_ram(cpunum, (void *)mwa->handler)))
                    /*TODO*///							end = mwa->end;
                    /*TODO*///			}
                }
                /*TODO*///			/* find the base of the lowest memory region that extends past the end */
                /*TODO*///			for (mra = Machine->drv->cpu[cpunum].memory_read; !IS_MEMPORT_END(mra); mra++)
                /*TODO*///				if (!IS_MEMPORT_MARKER(mra))
                /*TODO*///					if (mra->end >= size && mra->start < lowest && needs_ram(cpunum, (void *)mra->handler))
                /*TODO*///						lowest = mra->start;
                /*TODO*///
                /*TODO*///			for (mwa = Machine->drv->cpu[cpunum].memory_write; !IS_MEMPORT_END(mwa); mwa++)
                /*TODO*///				if (!IS_MEMPORT_MARKER(mwa))
                /*TODO*///					if (mwa->end >= size && mwa->start < lowest && (mwa->base || needs_ram(cpunum, (void *)mwa->handler)))
                /*TODO*///						lowest = mwa->start;
                /*TODO*///
                /*TODO*///			/* done if nothing found */
                /*TODO*///			if (lowest == ~0)
                /*TODO*///				break;
                /*TODO*///
                /*TODO*///			/* now loop until we find the end of this contiguous block of memory */
                /*TODO*///			lastend = ~0;
                /*TODO*///			end = lowest;
                /*TODO*///			while (end != lastend)
                /*TODO*///			{
                /*TODO*///				lastend = end;
                /*TODO*///
                /*TODO*///				/* find the end of the contiguous block of memory */
                /*TODO*///				for (mra = Machine->drv->cpu[cpunum].memory_read; !IS_MEMPORT_END(mra); mra++)
                /*TODO*///					if (!IS_MEMPORT_MARKER(mra))
                /*TODO*///						if (mra->start <= end+1 && mra->end > end && needs_ram(cpunum, (void *)mra->handler))
                /*TODO*///							end = mra->end;
                /*TODO*///
                /*TODO*///				for (mwa = Machine->drv->cpu[cpunum].memory_write; !IS_MEMPORT_END(mwa); mwa++)
                /*TODO*///					if (!IS_MEMPORT_MARKER(mwa))
                /*TODO*///						if (mwa->start <= end+1 && mwa->end > end && (mwa->base || needs_ram(cpunum, (void *)mwa->handler)))
                /*TODO*///							end = mwa->end;
                /*TODO*///			}
                /*TODO*///
                /*TODO*///			/* fill in the data structure */
                /*TODO*///			ext->start = lowest;
                /*TODO*///			ext->end = end;
                /*TODO*///			ext->region = region;
                /*TODO*///
                /*TODO*///			/* allocate memory */
                /*TODO*///			ext->data = malloc(end+1 - lowest);
                /*TODO*///			if (!ext->data)
                /*TODO*///				fatalerror("malloc(%d) failed (lowest: %x - end: %x)\n", end + 1 - lowest, lowest, end);
                /*TODO*///
                /*TODO*///			/* reset the memory */
                /*TODO*///			memset(ext->data, 0, end+1 - lowest);
                /*TODO*///
                /*TODO*///			/* prepare for the next loop */
                /*TODO*///			size = ext->end + 1;
                /*TODO*///			ext++;
            }
        }
        return 1;
    }

    /*-------------------------------------------------
    	populate_memory - populate the memory mapping
    	tables with entries
    -------------------------------------------------*/
    public static int populate_memory() {
        int cpunum;

        /* loop over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            Object mra_obj = Machine.drv.cpu[cpunum].memory_read;
            Object mwa_obj = Machine.drv.cpu[cpunum].memory_write;

            /* install the read handlers */
            if (mra_obj != null) {
                if (mra_obj instanceof Memory_ReadAddress[]) {
                    Memory_ReadAddress[] mra = (Memory_ReadAddress[]) mra_obj;
                    int mra_ptr = 0;
                    /* first find the end and check for address bits */
                    for (mra_ptr = 0; !IS_MEMPORT_END(mra[mra_ptr]); mra_ptr++) {
                        if (IS_MEMPORT_MARKER(mra[mra_ptr]) && ((mra[mra_ptr].end & MEMPORT_ABITS_MASK) != 0)) {
                            cpudata[cpunum].mem.mask = 0xffffffff >>> (32 - (mra[mra_ptr].end & MEMPORT_ABITS_VAL_MASK));
                        }
                    }

                    /* then work backwards */
                    for (mra_ptr--; mra_ptr >= 0; mra_ptr--) {
                        if (!IS_MEMPORT_MARKER(mra[mra_ptr])) {
                            install_mem_handler(cpudata[cpunum].mem, 0, mra[mra_ptr].start, mra[mra_ptr].end, mra[mra_ptr].handler, (Object) mra[mra_ptr]._handler);
                        }
                    }
                } else {
                    //16,32bit handling
                    throw new UnsupportedOperationException("Unsupported");
                }
            }

            /* install the write handlers */
            if (mwa_obj != null) {
                if (mwa_obj instanceof Memory_WriteAddress[]) {
                    Memory_WriteAddress[] mwa = (Memory_WriteAddress[]) mwa_obj;
                    int mwa_ptr = 0;
                    /* first find the end and check for address bits */
                    for (mwa_ptr = 0; !IS_MEMPORT_END(mwa[mwa_ptr]); mwa_ptr++) {
                        if (IS_MEMPORT_MARKER(mwa[mwa_ptr]) && (mwa[mwa_ptr].end & MEMPORT_ABITS_MASK) != 0) {
                            cpudata[cpunum].mem.mask = 0xffffffff >>> (32 - (mwa[mwa_ptr].end & MEMPORT_ABITS_VAL_MASK));
                        }
                    }

                    /* then work backwards */
                    for (mwa_ptr--; mwa_ptr >= 0; mwa_ptr--) {
                        if (!IS_MEMPORT_MARKER(mwa[mwa_ptr])) {
                            install_mem_handler(cpudata[cpunum].mem, 1, mwa[mwa_ptr].start, mwa[mwa_ptr].end, mwa[mwa_ptr].handler, mwa[mwa_ptr]._handler);
                            if (mwa[mwa_ptr].base != null) {
                                UBytePtr p = memory_find_base(cpunum, mwa[mwa_ptr].start);
                                mwa[mwa_ptr].base.memory = p.memory;
                                mwa[mwa_ptr].base.offset = p.offset;
                            }
                            if (mwa[mwa_ptr].size != null) {
                                mwa[mwa_ptr].size[0] = mwa[mwa_ptr].end - mwa[mwa_ptr].start + 1;
                            }
                        }
                    }
                } else {
                    //16,32bit handling
                    throw new UnsupportedOperationException("Unsupported");
                }
            }

            /*TODO*///		const struct Memory_ReadAddress *mra, *mra_start = Machine->drv->cpu[cpunum].memory_read;
/*TODO*///		const struct Memory_WriteAddress *mwa, *mwa_start = Machine->drv->cpu[cpunum].memory_write;
/*TODO*///
/*TODO*///		/* install the read handlers */
/*TODO*///		if (mra_start)
/*TODO*///		{
/*TODO*///			/* first find the end and check for address bits */
/*TODO*///			for (mra = mra_start; !IS_MEMPORT_END(mra); mra++)
/*TODO*///				if (IS_MEMPORT_MARKER(mra) && (mra->end & MEMPORT_ABITS_MASK))
/*TODO*///					cpudata[cpunum].mem.mask = 0xffffffffUL >> (32 - (mra->end & MEMPORT_ABITS_VAL_MASK));
/*TODO*///
/*TODO*///			/* then work backwards */
/*TODO*///			for (mra--; mra >= mra_start; mra--)
/*TODO*///				if (!IS_MEMPORT_MARKER(mra))
/*TODO*///					install_mem_handler(&cpudata[cpunum].mem, 0, mra->start, mra->end, (void *)mra->handler);
/*TODO*///		}
/*TODO*///
/*TODO*///		/* install the write handlers */
/*TODO*///		if (mwa_start)
/*TODO*///		{
/*TODO*///			/* first find the end and check for address bits */
/*TODO*///			for (mwa = mwa_start; !IS_MEMPORT_END(mwa); mwa++)
/*TODO*///				if (IS_MEMPORT_MARKER(mwa) && (mwa->end & MEMPORT_ABITS_MASK))
/*TODO*///					cpudata[cpunum].mem.mask = 0xffffffffUL >> (32 - (mwa->end & MEMPORT_ABITS_VAL_MASK));
/*TODO*///
/*TODO*///			/* then work backwards */
/*TODO*///			for (mwa--; mwa >= mwa_start; mwa--)
/*TODO*///				if (!IS_MEMPORT_MARKER(mwa))
/*TODO*///				{
/*TODO*///					install_mem_handler(&cpudata[cpunum].mem, 1, mwa->start, mwa->end, (void *)mwa->handler);
/*TODO*///					if (mwa->base) *mwa->base = memory_find_base(cpunum, mwa->start);
/*TODO*///					if (mwa->size) *mwa->size = mwa->end - mwa->start + 1;
/*TODO*///				}
/*TODO*///		}
        }
        return 1;
    }

    /*-------------------------------------------------
    	populate_ports - populate the port mapping
    	tables with entries
    -------------------------------------------------*/
    public static int populate_ports() {
        int cpunum;

        /* loop over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            Object mra_obj = Machine.drv.cpu[cpunum].port_read;
            Object mwa_obj = Machine.drv.cpu[cpunum].port_write;


            /* install the read handlers */
            if (mra_obj != null) {
                if (mra_obj instanceof IO_ReadPort[]) {
                    IO_ReadPort[] mra = (IO_ReadPort[]) mra_obj;
                    int mra_ptr = 0;
                    /* first find the end and check for address bits */
                    for (mra_ptr = 0; !IS_MEMPORT_END(mra[mra_ptr]); mra_ptr++) {
                        if (IS_MEMPORT_MARKER(mra[mra_ptr]) && (mra[mra_ptr].end & MEMPORT_ABITS_MASK) != 0) {
                            cpudata[cpunum].port.mask = 0xffffffff >>> (32 - (mra[mra_ptr].end & MEMPORT_ABITS_VAL_MASK));
                        }
                    }

                    /* then work backwards */
                    for (mra_ptr--; mra_ptr >= 0; mra_ptr--) {
                        if (!IS_MEMPORT_MARKER(mra[mra_ptr])) {
                            install_port_handler(cpudata[cpunum].port, 0, mra[mra_ptr].start, mra[mra_ptr].end, mra[mra_ptr].handler, mra[mra_ptr]._handler);
                        }
                    }
                } else {
                    //16bit -32 bit support
                    throw new UnsupportedOperationException("Unsupported");
                }
            }

            /* install the write handlers */
            if (mwa_obj != null) {
                if (mwa_obj instanceof IO_WritePort[]) {
                    IO_WritePort[] mwa = (IO_WritePort[]) mwa_obj;
                    int mwa_ptr = 0;
                    /* first find the end and check for address bits */
                    for (mwa_ptr = 0; !IS_MEMPORT_END(mwa[mwa_ptr]); mwa_ptr++) {
                        if (IS_MEMPORT_MARKER(mwa[mwa_ptr]) && (mwa[mwa_ptr].end & MEMPORT_ABITS_MASK) != 0) {
                            cpudata[cpunum].port.mask = 0xffffffff >>> (32 - (mwa[mwa_ptr].end & MEMPORT_ABITS_VAL_MASK));
                        }
                    }

                    /* then work backwards */
                    for (mwa_ptr--; mwa_ptr >= 0; mwa_ptr--) {
                        if (!IS_MEMPORT_MARKER(mwa[mwa_ptr])) {
                            install_port_handler(cpudata[cpunum].port, 1, mwa[mwa_ptr].start, mwa[mwa_ptr].end, mwa[mwa_ptr].handler, mwa[mwa_ptr]._handler);
                        }
                    }
                } else {
                    //16bit -32 bit support
                    throw new UnsupportedOperationException("Unsupported");
                }
            }
            /*TODO*///
            /*TODO*///		/* install the read handlers */
            /*TODO*///		if (mra_start)
            /*TODO*///		{
            /*TODO*///			/* first find the end and check for address bits */
            /*TODO*///			for (mra = mra_start; !IS_MEMPORT_END(mra); mra++)
            /*TODO*///				if (IS_MEMPORT_MARKER(mra) && (mra->end & MEMPORT_ABITS_MASK))
            /*TODO*///					cpudata[cpunum].port.mask = 0xffffffffUL >> (32 - (mra->end & MEMPORT_ABITS_VAL_MASK));
            /*TODO*///
            /*TODO*///			/* then work backwards */
            /*TODO*///			for (mra--; mra != mra_start; mra--)
            /*TODO*///				if (!IS_MEMPORT_MARKER(mra))
            /*TODO*///					install_port_handler(&cpudata[cpunum].port, 0, mra->start, mra->end, (void *)mra->handler);
            /*TODO*///		}
            /*TODO*///
            /*TODO*///		/* install the write handlers */
            /*TODO*///		if (mwa_start)
            /*TODO*///		{
            /*TODO*///			/* first find the end and check for address bits */
            /*TODO*///			for (mwa = mwa_start; !IS_MEMPORT_END(mwa); mwa++)
            /*TODO*///				if (IS_MEMPORT_MARKER(mwa) && (mwa->end & MEMPORT_ABITS_MASK))
            /*TODO*///					cpudata[cpunum].port.mask = 0xffffffffUL >> (32 - (mwa->end & MEMPORT_ABITS_VAL_MASK));
            /*TODO*///
            /*TODO*///			/* then work backwards */
            /*TODO*///			for (mwa--; mwa != mwa_start; mwa--)
            /*TODO*///				if (!IS_MEMPORT_MARKER(mwa))
            /*TODO*///					install_port_handler(&cpudata[cpunum].port, 1, mwa->start, mwa->end, (void *)mwa->handler);
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

    /*-------------------------------------------------
    	debugging
    -------------------------------------------------*/
    static void dump_map(FILE file, memport_data memport, table_data table) {
        String strings[]
                = {
                    "invalid", "bank 1", "bank 2", "bank 3",
                    "bank 4", "bank 5", "bank 6", "bank 7",
                    "bank 8", "bank 9", "bank 10", "bank 11",
                    "bank 12", "bank 13", "bank 14", "bank 15",
                    "bank 16", "bank 17", "bank 18", "bank 19",
                    "bank 20", "bank 21", "bank 22", "bank 23",
                    "bank 24", "RAM", "ROM", "RAMROM",
                    "nop", "unused 1", "unused 2", "unmapped"
                };

        int minbits = DATABITS_TO_SHIFT(memport.dbits);
        int l1bits = LEVEL1_BITS(memport.ebits);
        int l2bits = LEVEL2_BITS(memport.ebits);
        int l1count = 1 << l1bits;
        int l2count = 1 << l2bits;
        int i, j;

        fprintf(file, "  Address bits = %d\n", memport.abits);
        fprintf(file, "     Data bits = %d\n", memport.dbits);
        fprintf(file, "Effective bits = %d\n", memport.ebits);
        fprintf(file, "       L1 bits = %d\n", l1bits);
        fprintf(file, "       L2 bits = %d\n", l2bits);
        fprintf(file, "  Address mask = %X\n", memport.mask);
        fprintf(file, "\n");

        for (i = 0; i < l1count; i++) {
            char entry = table.table.read(i);
            if (entry != STATIC_UNMAP) {
                fprintf(file, "%05X  %08X-%08X    = %02X: ", i,
                        i << (l2bits + minbits),
                        ((i + 1) << (l2bits + minbits)) - 1, (int) entry);
                if (entry < STATIC_COUNT) {
                    fprintf(file, "%s [offset=%08X]\n", strings[entry], table.handlers[entry].offset);
                } else if (entry < SUBTABLE_BASE) {
                    fprintf(file, "handler(%08X) [offset=%08X]\n", table.handlers[entry].handler.hashCode(), table.handlers[entry].offset);
                } else {
                    fprintf(file, "subtable %d\n", entry & SUBTABLE_MASK);
                    entry &= SUBTABLE_MASK;

                    for (j = 0; j < l2count; j++) {
                        char/*UINT8*/ entry2 = table.table.read((1 << l1bits) + (entry << l2bits) + j);
                        if (entry2 != STATIC_UNMAP) {
                            fprintf(file, "   %05X  %08X-%08X = %02X: ", j,
                                    (i << (l2bits + minbits)) | (j << minbits),
                                    ((i << (l2bits + minbits)) | ((j + 1) << minbits)) - 1, (int) entry2);
                            if (entry2 < STATIC_COUNT) {
                                fprintf(file, "%s [offset=%08X]\n", strings[entry2], table.handlers[entry2].offset);
                            } else if (entry2 < SUBTABLE_BASE) {
                                fprintf(file, "handler(%08X) [offset=%08X]\n", table.handlers[entry2].handler.hashCode(), table.handlers[entry2].offset);
                            } else {
                                fprintf(file, "subtable %d???????????\n", entry2 & SUBTABLE_MASK);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void mem_dump() {
        FILE file = fopen("memdump.log", "w");
        int cpunum;

        /* skip if we can't open the file */
        if (file == null) {
            return;
        }

        /* loop over CPUs */
        for (cpunum
                = 0; cpunum
                < cpu_gettotalcpu(); cpunum++) {
            /* memory handlers */
            if (cpudata[cpunum].mem.abits != 0) {
                fprintf(file, "\n\n"
                        + "===============================\n"
                        + "CPU %d read memory handler dump\n"
                        + "===============================\n", cpunum);
                dump_map(file, cpudata[cpunum].mem, cpudata[cpunum].mem.read);

                fprintf(file, "\n\n"
                        + "================================\n"
                        + "CPU %d write memory handler dump\n"
                        + "================================\n", cpunum);
                dump_map(file, cpudata[cpunum].mem, cpudata[cpunum].mem.write);
            }

            /* port handlers */
            if (cpudata[cpunum].port.abits != 0) {
                fprintf(file, "\n\n"
                        + "=============================\n"
                        + "CPU %d read port handler dump\n"
                        + "=============================\n", cpunum);
                dump_map(file, cpudata[cpunum].port, cpudata[cpunum].port.read);

                fprintf(file, "\n\n"
                        + "==============================\n"
                        + "CPU %d write port handler dump\n"
                        + "==============================\n", cpunum);
                dump_map(file, cpudata[cpunum].port, cpudata[cpunum].port.write);
            }
        }
        fclose(file);
    }
}
