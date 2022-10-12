/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuexec.*;

public class cpuexecH {

    /**
     * *************************************************************************
     *
     * cpuexec.h
     *
     * Core multi-CPU execution engine.
     *
     **************************************************************************
     */
    /**
     * ***********************************
     *
     * CPU description for drivers
     *
     ************************************
     */
    public static class MachineCPU {

        public MachineCPU(int cpu_type, int cpu_flags, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptHandlerPtr vblank_interrupt, int vblank_interrupts_per_frame, InterruptHandlerPtr timed_interrupt, int timed_interrupts_per_second, Object reset_param, String tag) {
            this.cpu_type = cpu_type;
            this.cpu_flags = cpu_flags;
            this.cpu_clock = cpu_clock;
            this.memory_read = memory_read;
            this.memory_write = memory_write;
            this.port_read = port_read;
            this.port_write = port_write;
            this.vblank_interrupt = vblank_interrupt;
            this.vblank_interrupts_per_frame = vblank_interrupts_per_frame;
            this.timed_interrupt = timed_interrupt;
            this.timed_interrupts_per_second = timed_interrupts_per_second;
            this.reset_param = reset_param;
            this.tag = tag;
        }

        public MachineCPU() {
            this(0, 0, 0, null, null, null, null, null, 0, null, 0, null, null);
        }

        public static MachineCPU[] create(int n) {
            MachineCPU[] a = new MachineCPU[n];
            for (int k = 0; k < n; k++) {
                a[k] = new MachineCPU();
            }
            return a;
        }
        public int cpu_type;/* index for the CPU type */
        public int cpu_flags;/* flags; see #defines below */
        public int cpu_clock;/* in Hertz */
        public Object memory_read;/* struct Memory_ReadAddress */
        public Object memory_write;/* struct Memory_WriteAddress */
        public Object port_read;
        public Object port_write;
        public InterruptHandlerPtr vblank_interrupt;/* for interrupts tied to VBLANK */
        public int vblank_interrupts_per_frame;/* usually 1 */
        public InterruptHandlerPtr timed_interrupt;/* for interrupts not tied to VBLANK */
        public int timed_interrupts_per_second;
        public Object reset_param;/* parameter for cpu_reset */
        public String tag;

        public MachineCPU(int CPU_Z80, int i, memoryH.Memory_ReadAddress[] readmem, memoryH.Memory_WriteAddress[] writemem, Object object, Object object0, InterruptHandlerPtr irq0_line_hold, int i0) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    /**
     * ***********************************
     *
     * CPU flag constants
     *
     ************************************
     */

    /* set this if the CPU is used as a slave for audio. It will not be emulated if */
 /* sound is disabled, therefore speeding up a lot the emulation. */
    public static final int CPU_AUDIO_CPU = 0x0002;

    /* the Z80 can be wired to use 16 bit addressing for I/O ports */
    public static final int CPU_16BIT_PORT = 0x0001;

    /**
     * ***********************************
     *
     * Save/restore
     *
     ************************************
     */

    /* Load or save the game state */
    public static final int LOADSAVE_NONE = 0;
    public static final int LOADSAVE_SAVE = 1;
    public static final int LOADSAVE_LOAD = 2;

    /**
     * ***********************************
     *
     * CPU halt/reset lines
     *
     ************************************
     */

    /* Backwards compatibility */
    public static void cpu_set_reset_line(int cpunum, int state) {
        cpunum_set_reset_line(cpunum, state);
    }
    /*TODO*///#define cpu_set_halt_line 		cpunum_set_halt_line
    /**
     * ***********************************
     *
     * CPU scheduling
     *
     ************************************
     */

    /* Suspension reasons */
    public static final int SUSPEND_REASON_HALT = 0x0001;
    public static final int SUSPEND_REASON_RESET = 0x0002;
    public static final int SUSPEND_REASON_SPIN = 0x0004;
    public static final int SUSPEND_REASON_TRIGGER = 0x0008;
    public static final int SUSPEND_REASON_DISABLE = 0x0010;
    public static final int SUSPEND_ANY_REASON = ~0; //should be ok?? (shadow note)

    /*TODO*////* Backwards compatibility */
/*TODO*///#define timer_suspendcpu(cpunum, suspend, reason)	do { if (suspend) cpunum_suspend(cpunum, reason, 1); else cpunum_resume(cpunum, reason); } while (0)
/*TODO*///#define timer_holdcpu(cpunum, suspend, reason)		do { if (suspend) cpunum_suspend(cpunum, reason, 0); else cpunum_resume(cpunum, reason); } while (0)
    public static int cpu_getstatus(int cpunum) {
        return (cpunum_is_suspended(cpunum, SUSPEND_REASON_HALT | SUSPEND_REASON_RESET | SUSPEND_REASON_DISABLE) == 0) ? 1 : 0;
    }

    /*TODO*///#define timer_get_overclock(cpunum)					cpunum_get_clockscale(cpunum)
/*TODO*///#define timer_set_overclock(cpunum, overclock)		cpunum_set_clockscale(cpunum, overclock)
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * Timing helpers
     *
     ************************************
     */
    /*TODO*////* Backwards compatibility */
/*TODO*///#define cpu_gettotalcycles cpunum_gettotalcycles
/*TODO*///#define cpu_gettotalcycles64 cpunum_gettotalcycles64
/*TODO*///
    /**
     * ***********************************
     *
     * Z80 daisy chain
     *
     ************************************
     */

    /* daisy-chain link */
    public static class Z80_DaisyChain {

        public DaisyChainResetPtr reset;/* reset callback     */
        public DaisyChainInterruptEntryPtr interrupt_entry;/* entry callback     */
        public DaisyChainInterruptRetiPtr interrupt_reti;/* reti callback      */
        public int irq_param;

        /* callback paramater */
        public Z80_DaisyChain(DaisyChainResetPtr reset, DaisyChainInterruptEntryPtr interrupt_entry, DaisyChainInterruptRetiPtr interrupt_reti, int irq_param) {
            this.reset = reset;
            this.interrupt_entry = interrupt_entry;
            this.interrupt_reti = interrupt_reti;
            this.irq_param = irq_param;
        }
    }

    public static final int Z80_MAXDAISY = 4;/* maximum of daisy chan device */

    public static final int Z80_INT_REQ = 0x01;/* interrupt request mask       */
    public static final int Z80_INT_IEO = 0x02;/* interrupt disable mask(IEO)  */

    public static int Z80_VECTOR(int device, int state) {
        return (((device) << 8) & 0xFF | (state) & 0xFF);
    }
}
