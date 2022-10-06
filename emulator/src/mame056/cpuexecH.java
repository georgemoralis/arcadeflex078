/**
 * Ported to 0.56
 */
package mame056;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuint.*;

//TODO
import static arcadeflex.v078.mame.cpuintrfH.IRQ_LINE_NMI;


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

        public MachineCPU(int cpu_type, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptHandlerPtr vblank_interrupt, int vblank_interrupts_per_frame, InterruptHandlerPtr timed_interrupt, int timed_interrupts_per_second, Object reset_param) {
            this.cpu_type = cpu_type;
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
        }

        public MachineCPU(int cpu_type, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptHandlerPtr vblank_interrupt, int vblank_interrupts_per_frame, InterruptHandlerPtr timed_interrupt, int timed_interrupts_per_second) {
            this.cpu_type = cpu_type;
            this.cpu_clock = cpu_clock;
            this.memory_read = memory_read;
            this.memory_write = memory_write;
            this.port_read = port_read;
            this.port_write = port_write;
            this.vblank_interrupt = vblank_interrupt;
            this.vblank_interrupts_per_frame = vblank_interrupts_per_frame;
            this.timed_interrupt = timed_interrupt;
            this.timed_interrupts_per_second = timed_interrupts_per_second;
            this.reset_param = null;
        }

        public MachineCPU(int cpu_type, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptHandlerPtr vblank_interrupt, int vblank_interrupts_per_frame) {
            this.cpu_type = cpu_type;
            this.cpu_clock = cpu_clock;
            this.memory_read = memory_read;
            this.memory_write = memory_write;
            this.port_read = port_read;
            this.port_write = port_write;
            this.vblank_interrupt = vblank_interrupt;
            this.vblank_interrupts_per_frame = vblank_interrupts_per_frame;
            this.timed_interrupt = null;
            this.timed_interrupts_per_second = 0;
            this.reset_param = null;
        }

        public MachineCPU() {
            this(0, 0, null, null, null, null, null, 0, null, 0, null);
        }

        public static MachineCPU[] create(int n) {
            MachineCPU[] a = new MachineCPU[n];
            for (int k = 0; k < n; k++) {
                a[k] = new MachineCPU();
            }
            return a;
        }
        public int cpu_type;/* see #defines below. */
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

    }

    /**
     * ***********************************
     *
     * CPU flag constants
     *
     ************************************
     */

    /* flags for CPU go into upper byte */
    public static final int CPU_FLAGS_MASK = 0xff00;

    /* set this if the CPU is used as a slave for audio. It will not be emulated if sound is disabled, therefore speeding up a lot the emulation. */
    public static final int CPU_AUDIO_CPU = 0x8000;

    /* the Z80 can be wired to use 16 bit addressing for I/O ports */
    public static final int CPU_16BIT_PORT = 0x4000;


}
