/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuintrf.*;
//TODO
import static mame056.cpuintrf.cpuintrf;

public class cpuintrfH {

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	cpuintrf.h
/*TODO*///
/*TODO*///	Core CPU interface functions and definitions.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#ifndef CPUINTRF_H
/*TODO*///#define CPUINTRF_H
/*TODO*///
/*TODO*///#include "osd_cpu.h"
/*TODO*///#include "memory.h"
/*TODO*///#include "timer.h"
/*TODO*///
/*TODO*///#ifdef __cplusplus
/*TODO*///extern "C" {
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Enum listing all the CPUs
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*////* the following list is automatically generated by makelist.pl - don't edit manually! */
/*TODO*///enum
/*TODO*///{
/*TODO*///	CPU_DUMMY,
/*TODO*///#if (HAS_Z80)
/*TODO*///	CPU_Z80,
/*TODO*///#endif
/*TODO*///#if (HAS_Z180)
/*TODO*///	CPU_Z180,
/*TODO*///#endif
/*TODO*///#if (HAS_8080)
/*TODO*///	CPU_8080,
/*TODO*///#endif
/*TODO*///#if (HAS_8085A)
/*TODO*///	CPU_8085A,
/*TODO*///#endif
/*TODO*///#if (HAS_M6502)
/*TODO*///	CPU_M6502,
/*TODO*///#endif
/*TODO*///#if (HAS_M65C02)
/*TODO*///	CPU_M65C02,
/*TODO*///#endif
/*TODO*///#if (HAS_M65SC02)
/*TODO*///	CPU_M65SC02,
/*TODO*///#endif
/*TODO*///#if (HAS_M65CE02)
/*TODO*///	CPU_M65CE02,
/*TODO*///#endif
/*TODO*///#if (HAS_M6509)
/*TODO*///	CPU_M6509,
/*TODO*///#endif
/*TODO*///#if (HAS_M6510)
/*TODO*///	CPU_M6510,
/*TODO*///#endif
/*TODO*///#if (HAS_M6510T)
/*TODO*///	CPU_M6510T,
/*TODO*///#endif
/*TODO*///#if (HAS_M7501)
/*TODO*///	CPU_M7501,
/*TODO*///#endif
/*TODO*///#if (HAS_M8502)
/*TODO*///	CPU_M8502,
/*TODO*///#endif
/*TODO*///#if (HAS_N2A03)
/*TODO*///	CPU_N2A03,
/*TODO*///#endif
/*TODO*///#if (HAS_DECO16)
/*TODO*///	CPU_DECO16,
/*TODO*///#endif
/*TODO*///#if (HAS_M4510)
/*TODO*///	CPU_M4510,
/*TODO*///#endif
/*TODO*///#if (HAS_H6280)
/*TODO*///	CPU_H6280,
/*TODO*///#endif
/*TODO*///#if (HAS_I86)
/*TODO*///	CPU_I86,
/*TODO*///#endif
/*TODO*///#if (HAS_I88)
/*TODO*///	CPU_I88,
/*TODO*///#endif
/*TODO*///#if (HAS_I186)
/*TODO*///	CPU_I186,
/*TODO*///#endif
/*TODO*///#if (HAS_I188)
/*TODO*///	CPU_I188,
/*TODO*///#endif
/*TODO*///#if (HAS_I286)
/*TODO*///	CPU_I286,
/*TODO*///#endif
/*TODO*///#if (HAS_V20)
/*TODO*///	CPU_V20,
/*TODO*///#endif
/*TODO*///#if (HAS_V30)
/*TODO*///	CPU_V30,
/*TODO*///#endif
/*TODO*///#if (HAS_V33)
/*TODO*///	CPU_V33,
/*TODO*///#endif
/*TODO*///#if (HAS_V60)
/*TODO*///	CPU_V60,
/*TODO*///#endif
/*TODO*///#if (HAS_V70)
/*TODO*///	CPU_V70,
/*TODO*///#endif
/*TODO*///#if (HAS_I8035)
/*TODO*///	CPU_I8035,
/*TODO*///#endif
/*TODO*///#if (HAS_I8039)
/*TODO*///	CPU_I8039,
/*TODO*///#endif
/*TODO*///#if (HAS_I8048)
/*TODO*///	CPU_I8048,
/*TODO*///#endif
/*TODO*///#if (HAS_N7751)
/*TODO*///	CPU_N7751,
/*TODO*///#endif
/*TODO*///#if (HAS_I8X41)
/*TODO*///	CPU_I8X41,
/*TODO*///#endif
/*TODO*///#if (HAS_M6800)
/*TODO*///	CPU_M6800,
/*TODO*///#endif
/*TODO*///#if (HAS_M6801)
/*TODO*///	CPU_M6801,
/*TODO*///#endif
/*TODO*///#if (HAS_M6802)
/*TODO*///	CPU_M6802,
/*TODO*///#endif
/*TODO*///#if (HAS_M6803)
/*TODO*///	CPU_M6803,
/*TODO*///#endif
/*TODO*///#if (HAS_M6808)
/*TODO*///	CPU_M6808,
/*TODO*///#endif
/*TODO*///#if (HAS_HD63701)
/*TODO*///	CPU_HD63701,
/*TODO*///#endif
/*TODO*///#if (HAS_NSC8105)
/*TODO*///	CPU_NSC8105,
/*TODO*///#endif
/*TODO*///#if (HAS_M6805)
/*TODO*///	CPU_M6805,
/*TODO*///#endif
/*TODO*///#if (HAS_M68705)
/*TODO*///	CPU_M68705,
/*TODO*///#endif
/*TODO*///#if (HAS_HD63705)
/*TODO*///	CPU_HD63705,
/*TODO*///#endif
/*TODO*///#if (HAS_HD6309)
/*TODO*///	CPU_HD6309,
/*TODO*///#endif
/*TODO*///#if (HAS_M6809)
/*TODO*///	CPU_M6809,
/*TODO*///#endif
/*TODO*///#if (HAS_KONAMI)
/*TODO*///	CPU_KONAMI,
/*TODO*///#endif
/*TODO*///#if (HAS_M68000)
/*TODO*///	CPU_M68000,
/*TODO*///#endif
/*TODO*///#if (HAS_M68010)
/*TODO*///	CPU_M68010,
/*TODO*///#endif
/*TODO*///#if (HAS_M68EC020)
/*TODO*///	CPU_M68EC020,
/*TODO*///#endif
/*TODO*///#if (HAS_M68020)
/*TODO*///	CPU_M68020,
/*TODO*///#endif
/*TODO*///#if (HAS_T11)
/*TODO*///	CPU_T11,
/*TODO*///#endif
/*TODO*///#if (HAS_S2650)
/*TODO*///	CPU_S2650,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34010)
/*TODO*///	CPU_TMS34010,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34020)
/*TODO*///	CPU_TMS34020,
/*TODO*///#endif
/*TODO*///#if (HAS_TI990_10)
/*TODO*///	CPU_TI990_10,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9900)
/*TODO*///	CPU_TMS9900,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9940)
/*TODO*///	CPU_TMS9940,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9980)
/*TODO*///	CPU_TMS9980,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9985)
/*TODO*///	CPU_TMS9985,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9989)
/*TODO*///	CPU_TMS9989,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9995)
/*TODO*///	CPU_TMS9995,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS99105A)
/*TODO*///	CPU_TMS99105A,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS99110A)
/*TODO*///	CPU_TMS99110A,
/*TODO*///#endif
/*TODO*///#if (HAS_Z8000)
/*TODO*///	CPU_Z8000,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS32010)
/*TODO*///	CPU_TMS32010,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS32025)
/*TODO*///	CPU_TMS32025,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS32031)
/*TODO*///	CPU_TMS32031,
/*TODO*///#endif
/*TODO*///#if (HAS_CCPU)
/*TODO*///	CPU_CCPU,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2100)
/*TODO*///	CPU_ADSP2100,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2101)
/*TODO*/// CPU_ADSP2101,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2104)
/*TODO*///	CPU_ADSP2104,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2105)
/*TODO*///	CPU_ADSP2105,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2115)
/*TODO*///	CPU_ADSP2115,
/*TODO*///#endif
/*TODO*///#if (HAS_PSXCPU)
/*TODO*///	CPU_PSXCPU,
/*TODO*///#endif
/*TODO*///#if (HAS_ASAP)
/*TODO*///	CPU_ASAP,
/*TODO*///#endif
/*TODO*///#if (HAS_UPD7810)
/*TODO*///	CPU_UPD7810,
/*TODO*///#endif
/*TODO*///#if (HAS_UPD7807)
/*TODO*///	CPU_UPD7807,
/*TODO*///#endif
/*TODO*///#if (HAS_JAGUAR)
/*TODO*///	CPU_JAGUARGPU,
/*TODO*///	CPU_JAGUARDSP,
/*TODO*///#endif
/*TODO*///#if (HAS_R3000)
/*TODO*///	CPU_R3000BE,
/*TODO*///	CPU_R3000LE,
/*TODO*///#endif
/*TODO*///#if (HAS_R4600)
/*TODO*///	CPU_R4600BE,
/*TODO*///	CPU_R4600LE,
/*TODO*///#endif
/*TODO*///#if (HAS_R5000)
/*TODO*///	CPU_R5000BE,
/*TODO*///	CPU_R5000LE,
/*TODO*///#endif
/*TODO*///#if (HAS_ARM)
/*TODO*///	CPU_ARM,
/*TODO*///#endif
/*TODO*///#if (HAS_SH2)
/*TODO*///	CPU_SH2,
/*TODO*///#endif
/*TODO*///#if (HAS_DSP32C)
/*TODO*///	CPU_DSP32C,
/*TODO*///#endif
/*TODO*///#if (HAS_PIC16C54)
/*TODO*///	CPU_PIC16C54,
/*TODO*///#endif
/*TODO*///#if (HAS_PIC16C55)
/*TODO*///	CPU_PIC16C55,
/*TODO*///#endif
/*TODO*///#if (HAS_PIC16C56)
/*TODO*///	CPU_PIC16C56,
/*TODO*///#endif
/*TODO*///#if (HAS_PIC16C57)
/*TODO*///	CPU_PIC16C57,
/*TODO*///#endif
/*TODO*///#if (HAS_PIC16C58)
/*TODO*///	CPU_PIC16C58,
/*TODO*///#endif
/*TODO*///#if (HAS_G65816)
/*TODO*///	CPU_G65816,
/*TODO*///#endif
/*TODO*///#if (HAS_SPC700)
/*TODO*///	CPU_SPC700,
/*TODO*///#endif
/*TODO*///#if (HAS_E132XS)
/*TODO*///	CPU_E132XS,
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///#if (HAS_APEXC)
/*TODO*///	CPU_APEXC,
/*TODO*///#endif
/*TODO*///#if (HAS_CDP1802)
/*TODO*///	CPU_CDP1802,
/*TODO*///#endif
/*TODO*///#if (HAS_CP1600)
/*TODO*///	CPU_CP1600,
/*TODO*///#endif
/*TODO*///#if (HAS_F8)
/*TODO*///	CPU_F8,
/*TODO*///#endif
/*TODO*///#if (HAS_LH5801)
/*TODO*///	CPU_LH5801,
/*TODO*///#endif
/*TODO*///#if (HAS_PDP1)
/*TODO*///	CPU_PDP1,
/*TODO*///#endif
/*TODO*///#if (HAS_SATURN)
/*TODO*///	CPU_SATURN,
/*TODO*///#endif
/*TODO*///#if (HAS_SC61860)
/*TODO*///	CPU_SC61860,
/*TODO*///#endif
/*TODO*///#if (HAS_Z80GB)
/*TODO*///	CPU_Z80GB,
/*TODO*///#endif
/*TODO*///#endif
/*TODO*///    CPU_COUNT
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * Interrupt line constants
     *
     ************************************
     */
    /* line states */
    public static final int CLEAR_LINE = 0;/* clear (a fired, held or pulsed) line */
    public static final int ASSERT_LINE = 1;/* assert an interrupt immediately */
    public static final int HOLD_LINE = 2;/* hold interrupt line until acknowledged */
    public static final int PULSE_LINE = 3;/* pulse interrupt line for one instruction */

 /* internal flags (not for use by drivers!) */
    public static final int INTERNAL_CLEAR_LINE = 100 + CLEAR_LINE;
    public static final int INTERNAL_ASSERT_LINE = 100 + ASSERT_LINE;

    /* interrupt parameters */
    public static final int MAX_IRQ_LINES = 16;/* maximum number of IRQ lines per CPU */
    public static final int IRQ_LINE_NMI = 127;/* IRQ line for NMIs */


    /**
     * ***********************************
     *
     * CPU information constants
     *
     ************************************
     */

    /* get_reg/set_reg constants */
    public static final int MAX_REGS = 128;/* maximum number of register of any CPU */

 /* This value is passed to activecpu_get_reg to retrieve the previous
	 * program counter value, ie. before a CPU emulation started
	 * to fetch opcodes and arguments for the current instrution. */
    public static final int REG_PREVIOUSPC = -1;

    /* This value is passed to activecpu_get_reg to retrieve the current
	 * program counter value. */
    public static final int REG_PC = -2;

    /* This value is passed to activecpu_get_reg to retrieve the current
	 * stack pointer value. */
    public static final int REG_SP = -3;

    /* This value is passed to activecpu_get_reg/activecpu_set_reg, instead of one of
	 * the names from the enum a CPU core defines for it's registers,
	 * to get or set the contents of the memory pointed to by a stack pointer.
	 * You can specify the n'th element on the stack by (REG_SP_CONTENTS-n),
	 * ie. lower negative values. The actual element size (UINT16 or UINT32)
	 * depends on the CPU core. */
    public static final int REG_SP_CONTENTS = -4;

    /* endianness constants */
    public static final int CPU_IS_LE = 0;/* emulated CPU is little endian */
    public static final int CPU_IS_BE = 1;/* emulated CPU is big endian */

 /* Values passed to the cpu_info function of a core to retrieve information */
    public static final int CPU_INFO_REG = 0;
    public static final int CPU_INFO_FLAGS = MAX_REGS;
    public static final int CPU_INFO_NAME = MAX_REGS + 1;
    public static final int CPU_INFO_FAMILY = MAX_REGS + 2;
    public static final int CPU_INFO_VERSION = MAX_REGS + 3;
    public static final int CPU_INFO_FILE = MAX_REGS + 4;
    public static final int CPU_INFO_CREDITS = MAX_REGS + 5;
    public static final int CPU_INFO_REG_LAYOUT = MAX_REGS + 6;
    public static final int CPU_INFO_WIN_LAYOUT = MAX_REGS + 7;

    /**
     * ***********************************
     *
     * Core CPU interface structure
     *
     ************************************
     */
    public static abstract class cpu_interface {

        /* index (used to make sure we mach the enum above */
        public int cpu_num;

        /* table of core functions */
        public abstract void init();

        public abstract void reset(Object param);

        public abstract void exit();

        public abstract int execute(int cycles);
        public BurnHandlerPtr burn;

        public abstract Object init_context();//not in mame , used specific for arcadeflex

        public abstract Object get_context();//different from mame returns reg object and not size since java doesn't support references

        public abstract void set_context(Object reg);

        public abstract int[] get_cycle_table(int which);

        public abstract void set_cycle_table(int which, int[] new_table);

        public abstract int get_reg(int regnum);

        public abstract void set_reg(int regnum, int val);

        public abstract void set_irq_line(int irqline, int linestate);

        public abstract void set_irq_callback(IrqCallbackHandlerPtr callback);

        public abstract String cpu_info(Object context, int regnum);

        public abstract String cpu_dasm(String buffer, int pc);

        /* IRQ and clock information */
        public int/*unsigned*/ num_irqs;
        public int default_vector;
        public int[] icount;
        public double overclock;
        /* memory information */
        public int databus_width;

        public abstract int memory_read(int offset);

        public abstract void memory_write(int offset, int data);

        public abstract int internal_read(int offset);

        public abstract void internal_write(int offset, int data);
        public int pgm_memory_base;

        public abstract void set_op_base(int pc);
        public int address_shift;
        public int/*unsigned*/ address_bits;
        public int/*unsigned*/ endianess;
        public int/*unsigned*/ align_unit;
        public int/*unsigned*/ max_inst_len;

        public abstract int mem_address_bits_of_cpu();//arcadeflex function (based on the above table)
        /*	{ 16, cpu_readmem16 },
    	{ 20, cpu_readmem20 },
    	{ 21, cpu_readmem21 },
    	{ 24, cpu_readmem24 },
    
    	{ 16, cpu_readmem16bew },
    	{ 24, cpu_readmem24bew },
    	{ 32, cpu_readmem32bew },
    
    	{ 16, cpu_readmem16lew },
    	{ 17, cpu_readmem17lew },
    	{ 24, cpu_readmem24lew },
    	{ 29, cpu_readmem29lew },
    	{ 32, cpu_readmem32lew },
    
    	{ 24, cpu_readmem24bedw },
    	{ 29, cpu_readmem29bedw },
    	{ 32, cpu_readmem32bedw },
    
    	{ 26, cpu_readmem26ledw },
    	{ 29, cpu_readmem29ledw },
    	{ 32, cpu_readmem32ledw },
    
    	{ 18, cpu_readmem18bedw }*/
    }

    /**
     * ***********************************
     *
     * Macros
     *
     ************************************
     */
    public static int activecpu_get_previouspc() {
        return activecpu_get_reg(REG_PREVIOUSPC);
    }

    public static int activecpu_get_pc() {
        return activecpu_get_reg(REG_PC);
    }

    /*TODO*///#define		activecpu_get_sp()			activecpu_get_reg(REG_SP)
/*TODO*///#define		activecpu_set_pc(val)		activecpu_set_reg(REG_PC, val)
/*TODO*///#define		activecpu_set_sp(val)		activecpu_set_reg(REG_SP, val)
/*TODO*///
/*TODO*///#define		cpunum_get_previouspc(cpu)	cpunum_get_reg(cpu, REG_PREVIOUSPC)
/*TODO*///#define		cpunum_get_pc(cpu)			cpunum_get_reg(cpu, REG_PC)
/*TODO*///#define		cpunum_get_sp(cpu)			cpunum_get_reg(cpu, REG_SP)
/*TODO*///#define		cpunum_set_pc(cpu, val)		cpunum_set_reg(cpu, REG_PC, val)
/*TODO*///#define		cpunum_set_sp(cpu, val)		cpunum_set_reg(cpu, REG_SP, val)

    /* this is kind of gross - is it necessary */
    public static int cpu_geturnpc() {
        return activecpu_get_reg(REG_SP_CONTENTS);
    }

    /**
     * ***********************************
     *
     * CPU interface accessors
     *
     ************************************
     */

    /* return a pointer to the interface struct for a given CPU type */
    public static cpu_interface cputype_get_interface(int cputype) {
        return cpuintrf[cputype];
    }

    /* return a the index of the active CPU */
    public static int cpu_getactivecpu() {
        return activecpu;
    }

    /*TODO*///
/*TODO*///
/*TODO*////* return a the index of the executing CPU */
/*TODO*///INLINE int cpu_getexecutingcpu(void)
/*TODO*///{
/*TODO*///	extern int executingcpu;
/*TODO*///	return executingcpu;
/*TODO*///}
/*TODO*///
/*TODO*///
    /* return a the total number of registered CPUs */
    public static int cpu_gettotalcpu() {
        return totalcpu;
    }
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#ifdef __cplusplus
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///#endif	/* CPUINTRF_H */
/*TODO*///
/*TODO*///    
}
