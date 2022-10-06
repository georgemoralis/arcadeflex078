/**
 * Ported to 0.56
 */
package mame056;

import static arcadeflex.v078.mame.cpuexec.cpu_computerate;
import static arcadeflex.v078.mame.cpuexec.cpu_timedintcallback;
import static arcadeflex.v078.mame.cpuexecH.SUSPEND_ANY_REASON;
import static arcadeflex.v078.mame.cpuexecH.SUSPEND_REASON_DISABLE;
import static arcadeflex.v078.mame.cpuexecH.SUSPEND_REASON_HALT;
import static arcadeflex.v078.mame.cpuexecH.SUSPEND_REASON_RESET;
import static arcadeflex.v078.mame.cpuint.cpu_irq_callbacks;
import static arcadeflex.v078.mame.cpuint.cpuint_init;
import static arcadeflex.v078.mame.cpuint.cpuint_reset_cpu;
import static arcadeflex.v078.mame.cpuintrf.activecpu_get_icount;
import static arcadeflex.v078.mame.cpuintrf.cpuintrf_exit_cpu;
import static arcadeflex.v078.mame.cpuintrf.cpuintrf_init;
import static arcadeflex.v078.mame.cpuintrf.cpuintrf_init_cpu;
import static arcadeflex.v078.mame.cpuintrf.cpuintrf_pop_context;
import static arcadeflex.v078.mame.cpuintrf.cpuintrf_push_context;
import static arcadeflex.v078.mame.cpuintrf.cpunum_execute;
import static arcadeflex.v078.mame.cpuintrf.cpunum_reset;
import static arcadeflex.v078.mame.cpuintrfH.ASSERT_LINE;
import static arcadeflex.v078.mame.cpuintrfH.CLEAR_LINE;
import static arcadeflex.v078.mame.cpuintrfH.CPU_DUMMY;
import static arcadeflex.v078.mame.cpuintrfH.PULSE_LINE;
import static arcadeflex.v078.mame.cpuintrfH.cpu_getactivecpu;
import static arcadeflex.v078.mame.cpuintrfH.cpu_gettotalcpu;

import static mame056.cpuexecH.*;
import static mame056.driverH.*;
import static mame056.hiscore.*;
import static mame056.sndintrf.*;
import static mame056.mame.*;
import static mame056.timer.*;
import static mame056.timerH.*;
import static arcadeflex036.osdepend.*;
import static mame056.inptport.*;

public class cpuexec {

    /*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Debug logging
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	logerror x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Macros to help verify active CPU
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///#define VERIFY_ACTIVECPU(retval, name)						
/*TODO*///	int activecpu = cpu_getactivecpu();						
/*TODO*///	if (activecpu < 0)										
/*TODO*///	{														
/*TODO*///		logerror(#name "() called with no active cpu!\n");	
/*TODO*///		return retval;										
/*TODO*///	}
/*TODO*///
/*TODO*///#define VERIFY_ACTIVECPU_VOID(name)							
/*TODO*///	int activecpu = cpu_getactivecpu();						
/*TODO*///	if (activecpu < 0)										
/*TODO*///	{														
/*TODO*///		logerror(#name "() called with no active cpu!\n");	
/*TODO*///		return;												
/*TODO*///	}
    /**
     * ***********************************
     *
     * Triggers for the timer system
     *
     ************************************
     */
    public static final int TRIGGER_TIMESLICE = -1000;
    public static final int TRIGGER_INT = -2000;
    public static final int TRIGGER_YIELDTIME = -3000;
    public static final int TRIGGER_SUSPENDTIME = -4000;

    /**
     * ***********************************
     *
     * Internal CPU info structure
     *
     ************************************
     */
    public static class cpuinfo {

        int iloops;/* number of interrupts remaining this frame */
        int totalcycles;/* total CPU cycles executed */
        int vblankint_countdown;/* number of vblank callbacks left until we interrupt */
        int vblankint_multiplier;/* number of vblank callbacks per interrupt */
        Object vblankint_timer;/* reference to elapsed time counter */
        double vblankint_period;/* timing period of the VBLANK interrupt */
        Object timedint_timer;/* reference to this CPU's timer */
        double timedint_period;/* timing period of the timed interrupt */

        public static cpuinfo[] create(int n) {
            cpuinfo[] a = new cpuinfo[n];
            for (int k = 0; k < n; k++) {
                a[k] = new cpuinfo();
            }
            return a;
        }
    }

    /**
     * ***********************************
     *
     * General CPU variables
     *
     ************************************
     */
    static cpuinfo[] cpu_exec = cpuinfo.create(MAX_CPU);
    static int time_to_reset;
    static int time_to_quit;

    static int vblank;
    static int current_frame;
    public static int watchdog_counter;

    static int[] cycles_running = new int[1];

    /**
     * ***********************************
     *
     * CPU interrupt variables
     *
     ************************************
     */
    //static int[]/*UINT8*/ interrupt_enable = new int[MAX_CPU];
    //static int[] interrupt_vector = new int[MAX_CPU];
    //static int[][]/*UINT8*/ irq_line_state = new int[MAX_CPU][MAX_IRQ_LINES];
    //static int[][] irq_line_vector = new int[MAX_CPU][MAX_IRQ_LINES];
    /**
     * ***********************************
     *
     * Timer variables
     *
     ************************************
     */
    static Object vblank_timer;
    static int vblank_countdown;
    static int vblank_multiplier;
    static double vblank_period;

    static Object refresh_timer;
    static double refresh_period;
    static double refresh_period_inv;

    static Object timeslice_timer;
    static double timeslice_period;

    static double scanline_period;
    static double scanline_period_inv;

    /**
     * ***********************************
     *
     * Initialize all the CPUs
     *
     ************************************
     */
    public static int cpu_init() {
        int cpunum;

        /* initialize the interfaces first */
        if (cpuintrf_init() != 0) {
            return 1;
        }

        /* count how many CPUs we have to emulate */
        for (cpunum = 0; cpunum < MAX_CPU; cpunum++) {
            int cputype = Machine.drv.cpu[cpunum].cpu_type & ~CPU_FLAGS_MASK;

            /* stop when we hit a dummy */
            if (cputype == CPU_DUMMY) {
                break;
            }

            /* set the save state tag */
 /*TODO*///		state_save_set_current_tag(cpunum + 1);

            /* initialize this CPU */
            if (cpuintrf_init_cpu(cpunum, cputype) != 0) {
                return 1;
            }

        }
        cpuint_init();
        /* init the timer system */
        timer_init();
        timeslice_timer = refresh_timer = vblank_timer = null;

        return 0;
    }

    /**
     * ***********************************
     *
     * Prepare the system for execution
     *
     ************************************
     */
    static void cpu_pre_run() {
        int cpunum;

        logerror("Machine reset\n");

        /* read hi scores information from hiscore.dat */
        hs_open(Machine.gamedrv.name);
        hs_init();

        /* initialize the various timers (suspends all CPUs at startup) */
        cpu_inittimers();
        watchdog_counter = -1;

        /* reset sound chips */
        sound_reset();

        /* first pass over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            /* enable all CPUs (except for audio CPUs if the sound is off) */
            if ((Machine.drv.cpu[cpunum].cpu_type & CPU_AUDIO_CPU) == 0 || Machine.sample_rate != 0) {
                timer_suspendcpu(cpunum, 0, SUSPEND_ANY_REASON);
            } else {
                timer_suspendcpu(cpunum, 1, SUSPEND_REASON_DISABLE);
            }

            cpuint_reset_cpu(cpunum);

            /* reset the total number of cycles */
            cpu_exec[cpunum].totalcycles = 0;
        }

        vblank = 0;

        /* do this AFTER the above so init_machine() can use cpu_halt() to hold the */
 /* execution of some CPUs, or disable interrupts */
        if (Machine.drv.init_machine != null) {
            (Machine.drv.init_machine).handler();
        }

        /* now reset each CPU */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            cpunum_reset(cpunum, Machine.drv.cpu[cpunum].reset_param, cpu_irq_callbacks[cpunum]);
        }

        /* reset the globals */
        cpu_vblankreset();
        current_frame = 0;
        /*TODO*///	state_save_dump_registry();
    }

    /**
     * ***********************************
     *
     * Finish up execution
     *
     ************************************
     */
    static void cpu_post_run() {
        /* write hi scores to disk - No scores saving if cheat */
        hs_close();

    }

    /**
     * ***********************************
     *
     * Execute until done
     *
     ************************************
     */
    public static void cpu_run() {
        /* loop over multiple resets, until the user quits */
        time_to_quit = 0;
        int[] cpunum = new int[1];
        while (time_to_quit == 0) {
            /* prepare everything to run */
            cpu_pre_run();

            /* loop until the user quits or resets */
            time_to_reset = 0;
            while (time_to_quit == 0 && time_to_reset == 0) {

                /* if we have a load/save scheduled, handle it */
 /*TODO*///			if (loadsave_schedule != LOADSAVE_NONE)
/*TODO*///				handle_loadsave();

                /* ask the timer system to schedule */
                if (timer_schedule_cpu(cpunum, cycles_running) != 0) {
                    int ran;

                    /* run for the requested number of cycles */
                    ran = cpunum_execute(cpunum[0], cycles_running[0]);

                    /* update based on how many cycles we really ran */
                    cpu_exec[cpunum[0]].totalcycles += ran;

                    /* update the timer with how long we actually ran */
                    timer_update_cpu(cpunum[0], ran);
                }

            }

            /* finish up this iteration */
            cpu_post_run();
        }
    }

    /**
     * ***********************************
     *
     * Deinitialize all the CPUs
     *
     ************************************
     */
    public static void cpu_exit() {
        int cpunum;

        /* shut down the CPU cores */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            cpuintrf_exit_cpu(cpunum);
        }
    }

    /**
     * ***********************************
     *
     * Force a reset at the end of this timeslice
     *
     ************************************
     */
    public static void machine_reset() {
        time_to_reset = 1;
    }

    /**
     * ***********************************
     *
     * Handle reset line changes
     *
     ************************************
     */
    public static timer_callback reset_callback = new timer_callback() {
        public void handler(int param) {
            int cpunum = param & 0xff;
            int state = param >> 8;

            /* if we're asserting the line, just halt the CPU */
            if (state == ASSERT_LINE) {
                timer_suspendcpu(cpunum, 1, SUSPEND_REASON_RESET);
                return;
            }

            /* if we're clearing the line that was previously asserted, or if we're just */
 /* pulsing the line, reset the CPU */
            if ((state == CLEAR_LINE && timer_iscpususpended(cpunum, SUSPEND_REASON_RESET) != 0) || state == PULSE_LINE) {
                cpunum_reset(cpunum, Machine.drv.cpu[cpunum].reset_param, cpu_irq_callbacks[cpunum]);
            }

            /* if we're clearing the line, make sure the CPU is not halted */
            timer_suspendcpu(cpunum, 0, SUSPEND_REASON_RESET);
        }
    };

    public static void cpu_set_reset_line(int cpunum, int state) {
        timer_set(TIME_NOW, (cpunum & 0xff) | (state << 8), reset_callback);
    }

    /**
     * ***********************************
     *
     * Handle halt line changes
     *
     ************************************
     */
    public static timer_callback halt_callback = new timer_callback() {
        public void handler(int param) {
            int cpunum = param & 0xff;
            int state = param >> 8;

            /* if asserting, halt the CPU */
            if (state == ASSERT_LINE) {
                timer_suspendcpu(cpunum, 1, SUSPEND_REASON_HALT);
            } /* if clearing, unhalt the CPU */ else if (state == CLEAR_LINE) {
                timer_suspendcpu(cpunum, 0, SUSPEND_REASON_HALT);
            }
        }
    };

    public static void cpu_set_halt_line(int cpunum, int state) {
        timer_set(TIME_NOW, (cpunum & 0xff) | (state << 8), halt_callback);
    }

    /**
     * ***********************************
     *
     * Return suspended status of CPU
     *
     ************************************
     */
    public static int cpu_getstatus(int cpunum) {
        if (cpunum < cpu_gettotalcpu()) {
            return timer_iscpususpended(cpunum, SUSPEND_REASON_HALT | SUSPEND_REASON_RESET | SUSPEND_REASON_DISABLE) == 0 ? 1 : 0;
        }
        return 0;
    }

    /**
     * ***********************************
     *
     * Return cycles ran this iteration
     *
     ************************************
     */
    static int cycles_currently_ran() {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cycles_currently_ran() called with no active cpu!\n");
            return 0;
        }
        return cycles_running[0] - activecpu_get_icount();
    }

    /*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return cycles remaining in this
/*TODO*/// *	iteration
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cycles_left_to_run(void)
/*TODO*///{
/*TODO*///	VERIFY_ACTIVECPU(0, cycles_left_to_run);
/*TODO*///	return activecpu_get_icount();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return total number of CPU cycles
/*TODO*/// *	for the active CPU.
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*////*--------------------------------------------------------------
/*TODO*///
/*TODO*///	IMPORTANT: this value wraps around in a relatively short
/*TODO*///	time. For example, for a 6MHz CPU, it will wrap around in
/*TODO*///	2^32/6000000 = 716 seconds = 12 minutes.
/*TODO*///	Make sure you don't do comparisons between values returned
/*TODO*///	by this function, but only use the difference (which will
/*TODO*///	be correct regardless of wraparound).
/*TODO*///
/*TODO*///--------------------------------------------------------------*/
/*TODO*///
    public static int cpu_gettotalcycles() {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cpu_gettotalcycles() called with no active cpu!\n");
            return 0;
        }
        return cpu_exec[activecpu].totalcycles + cycles_currently_ran();
    }

    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return cycles until next interrupt
/*TODO*/// *	handler call
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpu_geticount(void)
/*TODO*///{
/*TODO*///	int result;
/*TODO*///
/*TODO*////* remove me - only used by mamedbg, m92 */
/*TODO*///	VERIFY_ACTIVECPU(0, cpu_geticount);
/*TODO*///	result = TIME_TO_CYCLES(activecpu, cpu[activecpu].vblankint_period - timer_timeelapsed(cpu[activecpu].vblankint_timer));
/*TODO*///	return (result < 0) ? 0 : result;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Scales a given value by the fraction
/*TODO*/// *	of time elapsed between refreshes
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
    public static int cpu_scalebyfcount(int value) {
        int result = (int) ((double) value * timer_timeelapsed(refresh_timer) * refresh_period_inv);
        if (value >= 0) {
            return (result < value) ? result : value;
        } else {
            return (result > value) ? result : value;
        }
    }

    /**
     * ***********************************
     *
     * Returns the current scanline
     *
     ************************************
     */

    /*--------------------------------------------------------------

           Note: cpu_getscanline() counts from 0, 0 being the first
           visible line. You might have to adjust this value to match
           the hardware, since in many cases the first visible line
           is >0.

   --------------------------------------------------------------*/
    public static int cpu_getscanline() {
        return (int) (timer_timeelapsed(refresh_timer) * scanline_period_inv);
    }

    /**
     * ***********************************
     *
     * Returns time until given scanline
     *
     ************************************
     */
    public static double cpu_getscanlinetime(int scanline) {
        double scantime = timer_starttime(refresh_timer) + (double) scanline * scanline_period;
        double abstime = timer_get_time();
        double result;

        /* if we're already past the computed time, count it for the next frame */
        if (abstime >= scantime) {
            scantime += TIME_IN_HZ(Machine.drv.frames_per_second);
        }

        /* compute how long from now until that time */
        result = scantime - abstime;

        /* if it's small, just count a whole frame */
        if (result < TIME_IN_NSEC(1)) {
            result = TIME_IN_HZ(Machine.drv.frames_per_second);
        }
        return result;
    }

    /**
     * ***********************************
     *
     * Returns time for one scanline
     *
     ************************************
     */
    public static double cpu_getscanlineperiod() {
        return scanline_period;
    }

    /**
     * ***********************************
     *
     * Returns a crude approximation of the horizontal position of the bream
     *
     ************************************
     */
    public static int cpu_gethorzbeampos() {
        double elapsed_time = timer_timeelapsed(refresh_timer);
        int scanline = (int) (elapsed_time * scanline_period_inv);
        double time_since_scanline = elapsed_time - (double) scanline * scanline_period;
        return (int) (time_since_scanline * scanline_period_inv * (double) Machine.drv.screen_width);
    }

    /**
     * ***********************************
     *
     * Returns the VBLANK state
     *
     ************************************
     */
    public static int cpu_getvblank() {
        return vblank;
    }

    /**
     * ***********************************
     *
     * Returns the current frame count
     *
     ************************************
     */
    public static int cpu_getcurrentframe() {
        return current_frame;
    }

    /**
     * ***********************************
     *
     * Generate a specific trigger
     *
     ************************************
     */
    public static timer_callback cpu_trigger = new timer_callback() {
        public void handler(int trigger) {
            timer_trigger(trigger);
        }
    };

    /**
     * ***********************************
     *
     * Generate a trigger in the future
     *
     ************************************
     */
    public static void cpu_triggertime(double duration, int trigger) {
        timer_set(duration, trigger, cpu_trigger);
    }

    /**
     * ***********************************
     *
     * Generate a trigger for an int
     *
     ************************************
     */
    public static void cpu_triggerint(int cpunum) {
        timer_trigger(TRIGGER_INT + cpunum);
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles until a trigger
     *
     ************************************
     */
    public static void cpu_spinuntil_trigger(int trigger) {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cpu_yielduntil_trigger() called with no active cpu!\n");
            return;
        }
        timer_suspendcpu_trigger(activecpu, trigger);
    }

    public static void cpu_yielduntil_trigger(int trigger) {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cpu_yielduntil_trigger() called with no active cpu!\n");
            return;
        }
        timer_holdcpu_trigger(activecpu, trigger);
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles until an interrupt
     *
     ************************************
     */
    public static void cpu_spinuntil_int() {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cpu_spinuntil_int() called with no active cpu!\n");
            return;
        }
        cpu_spinuntil_trigger(TRIGGER_INT + activecpu);
    }

    /*TODO*///
/*TODO*///
/*TODO*///void cpu_yielduntil_int(void)
/*TODO*///{
/*TODO*///	VERIFY_ACTIVECPU_VOID(cpu_yielduntil_int);
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_INT + activecpu);
/*TODO*///}
    /**
     * ***********************************
     *
     * Burn/yield CPU cycles until the end of the current timeslice
     *
     ************************************
     */
    public static void cpu_spin() {
        cpu_spinuntil_trigger(TRIGGER_TIMESLICE);
    }

    public static void cpu_yield() {
        cpu_yielduntil_trigger(TRIGGER_TIMESLICE);
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles for a specific period of time
     *
     ************************************
     */
    static int timetrig_spinuntil_time = 0;

    public static void cpu_spinuntil_time(double duration) {
        cpu_spinuntil_trigger(TRIGGER_SUSPENDTIME + timetrig_spinuntil_time);
        cpu_triggertime(duration, TRIGGER_SUSPENDTIME + timetrig_spinuntil_time);
        timetrig_spinuntil_time = (timetrig_spinuntil_time + 1) & 255;
    }

    static int timetrig = 0;

    public static void cpu_yielduntil_time(double duration) {

        cpu_yielduntil_trigger(TRIGGER_YIELDTIME + timetrig);
        cpu_triggertime(duration, TRIGGER_YIELDTIME + timetrig);
        timetrig = (timetrig + 1) & 255;
    }

    /**
     * ***********************************
     *
     * Returns the number of times the interrupt handler will be called before
     * the end of the current video frame.
     *
     ************************************
     */

    /*--------------------------------------------------------------

            This can be useful to interrupt handlers to synchronize
            their operation. If you call this from outside an interrupt
            handler, add 1 to the result, i.e. if it returns 0, it means
            that the interrupt handler will be called once.

    --------------------------------------------------------------*/
    public static int cpu_getiloops() {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cpu_getiloops() called with no active cpu!\n");
            return 0;
        }
        return cpu_exec[activecpu].iloops;
    }

    /**
     * ***********************************
     *
     * Hook for updating things on the real VBLANK (once per frame)
     *
     ************************************
     */
    static void cpu_vblankreset() {
        int cpunum;

        /* read hi scores from disk */
        hs_update();

        /* read keyboard & update the status of the input ports */
        update_input_ports();

        /* reset the cycle counters */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            if (timer_iscpususpended(cpunum, SUSPEND_REASON_DISABLE) == 0) {
                cpu_exec[cpunum].iloops = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame - 1;
            } else {
                cpu_exec[cpunum].iloops = -1;
            }
        }
    }

    /**
     * ***********************************
     *
     * First-run callback for VBLANKs
     *
     ************************************
     */
    public static timer_callback cpu_firstvblankcallback = new timer_callback() {
        public void handler(int param) {
            /* now that we're synced up, pulse from here on out */
            vblank_timer = timer_pulse(vblank_period, param, cpu_vblankcallback);

            /* but we need to call the standard routine as well */
            cpu_vblankcallback.handler(param);
        }
    };
    /**
     * ***********************************
     *
     * VBLANK core handler
     *
     ************************************
     */

    public static timer_callback cpu_vblankcallback = new timer_callback() {
        public void handler(int param) {
            int cpunum;

            /* loop over CPUs */
            for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
                /* if the interrupt multiplier is valid */
                if (cpu_exec[cpunum].vblankint_multiplier != -1) {
                    /* decrement; if we hit zero, generate the interrupt and reset the countdown */
                    if (--cpu_exec[cpunum].vblankint_countdown == 0) {
                        /* a param of -1 means don't call any callbacks */
                        if (param != -1) {
                            /* if the CPU has a VBLANK handler, call it */
                            if (Machine.drv.cpu[cpunum].vblank_interrupt != null && cpu_getstatus(cpunum) != 0) {
                                cpuintrf_push_context(cpunum);
                                //cpu_cause_interrupt(cpunum, Machine.drv.cpu[cpunum].vblank_interrupt.handler());
                                Machine.drv.cpu[cpunum].vblank_interrupt.handler();
                                cpuintrf_pop_context();
                            }

                            /* update the counters */
                            cpu_exec[cpunum].iloops--;
                        }

                        /* reset the countdown and timer */
                        cpu_exec[cpunum].vblankint_countdown = cpu_exec[cpunum].vblankint_multiplier;
                        timer_reset(cpu_exec[cpunum].vblankint_timer, TIME_NEVER);
                    }
                } /* else reset the VBLANK timer if this is going to be a real VBLANK */ else if (vblank_countdown == 1) {
                    timer_reset(cpu_exec[cpunum].vblankint_timer, TIME_NEVER);
                }
            }

            /* is it a real VBLANK? */
            if (--vblank_countdown == 0) {
                /* do we update the screen now? */
                if ((Machine.drv.video_attributes & VIDEO_UPDATE_AFTER_VBLANK) == 0) {
                    time_to_quit = updatescreen();
                }

                /* Set the timer to update the screen */
                timer_set(TIME_IN_USEC(Machine.drv.vblank_duration), 0, cpu_updatecallback);
                vblank = 1;

                /* reset the globals */
                cpu_vblankreset();

                /* reset the counter */
                vblank_countdown = vblank_multiplier;
            }
        }
    };

    /**
     * ***********************************
     *
     * End-of-VBLANK callback
     *
     ************************************
     */
    public static timer_callback cpu_updatecallback = new timer_callback() {
        public void handler(int param) {
            /* update the screen if we didn't before */
            if ((Machine.drv.video_attributes & VIDEO_UPDATE_AFTER_VBLANK) != 0) {
                time_to_quit = updatescreen();
            }
            vblank = 0;

            /* update IPT_VBLANK input ports */
            inputport_vblank_end();

            /* check the watchdog */
            if (watchdog_counter > 0) {
                if (--watchdog_counter == 0) {
                    logerror("reset caused by the watchdog\n");
                    machine_reset();
                }
            }

            /* track total frames */
            current_frame++;

            /* reset the refresh timer */
            timer_reset(refresh_timer, TIME_NEVER);
        }
    };

    /**
     * ***********************************
     *
     * Callback to force a timeslice
     *
     ************************************
     */
    public static timer_callback cpu_timeslicecallback = new timer_callback() {
        public void handler(int i) {
            timer_trigger(TRIGGER_TIMESLICE);
        }
    };

    /**
     * ***********************************
     *
     * Setup all the core timers
     *
     ************************************
     */
    static void cpu_inittimers() {
        double first_time;
        int cpunum, max, ipf;

        /* remove old timers */
        if (timeslice_timer != null) {
            timer_remove(timeslice_timer);
        }
        if (refresh_timer != null) {
            timer_remove(refresh_timer);
        }
        if (vblank_timer != null) {
            timer_remove(vblank_timer);
        }

        /* allocate a dummy timer at the minimum frequency to break things up */
        ipf = Machine.drv.cpu_slices_per_frame;
        if (ipf <= 0) {
            ipf = 1;
        }
        timeslice_period = TIME_IN_HZ(Machine.drv.frames_per_second * ipf);
        timeslice_timer = timer_pulse(timeslice_period, 0, cpu_timeslicecallback);

        /* allocate an infinite timer to track elapsed time since the last refresh */
        refresh_period = TIME_IN_HZ(Machine.drv.frames_per_second);
        refresh_period_inv = 1.0 / refresh_period;
        refresh_timer = timer_set(TIME_NEVER, 0, null);

        /* while we're at it, compute the scanline times */
        if (Machine.drv.vblank_duration != 0) {
            scanline_period = (refresh_period - TIME_IN_USEC(Machine.drv.vblank_duration))
                    / (double) (Machine.visible_area.max_y - Machine.visible_area.min_y + 1);
        } else {
            scanline_period = refresh_period / (double) Machine.drv.screen_height;
        }
        scanline_period_inv = 1.0 / scanline_period;

        /*
	 *	The following code finds all the CPUs that are interrupting in sync with the VBLANK
	 *	and sets up the VBLANK timer to run at the minimum number of cycles per frame in
	 *	order to service all the synced interrupts
         */

 /* find the CPU with the maximum interrupts per frame */
        max = 1;
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;
            if (ipf > max) {
                max = ipf;
            }
        }

        /* now find the LCD with the rest of the CPUs (brute force - these numbers aren't huge) */
        vblank_multiplier = max;
        while (true) {
            for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
                ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;
                if (ipf > 0 && (vblank_multiplier % ipf) != 0) {
                    break;
                }
            }
            if (cpunum == cpu_gettotalcpu()) {
                break;
            }
            vblank_multiplier += max;
        }

        /* initialize the countdown timers and intervals */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;
            if (ipf > 0) {
                cpu_exec[cpunum].vblankint_countdown = cpu_exec[cpunum].vblankint_multiplier = vblank_multiplier / ipf;
            } else {
                cpu_exec[cpunum].vblankint_countdown = cpu_exec[cpunum].vblankint_multiplier = -1;
            }
        }

        /* allocate a vblank timer at the frame rate * the LCD number of interrupts per frame */
        vblank_period = TIME_IN_HZ(Machine.drv.frames_per_second * vblank_multiplier);
        vblank_timer = timer_pulse(vblank_period, 0, cpu_vblankcallback);
        vblank_countdown = vblank_multiplier;

        /*
	 *		The following code creates individual timers for each CPU whose interrupts are not
	 *		synced to the VBLANK, and computes the typical number of cycles per interrupt
         */

 /* start the CPU interrupt timers */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;

            /* remove old timers */
            if (cpu_exec[cpunum].vblankint_timer != null) {
                timer_remove(cpu_exec[cpunum].vblankint_timer);
            }
            if (cpu_exec[cpunum].timedint_timer != null) {
                timer_remove(cpu_exec[cpunum].timedint_timer);
            }

            /* compute the average number of cycles per interrupt */
            if (ipf <= 0) {
                ipf = 1;
            }
            cpu_exec[cpunum].vblankint_period = TIME_IN_HZ(Machine.drv.frames_per_second * ipf);
            cpu_exec[cpunum].vblankint_timer = timer_set(TIME_NEVER, 0, null);

            /* see if we need to allocate a CPU timer */
            ipf = Machine.drv.cpu[cpunum].timed_interrupts_per_second;
            if (ipf != 0) {
                cpu_exec[cpunum].timedint_period = cpu_computerate(ipf);
                cpu_exec[cpunum].timedint_timer = timer_pulse(cpu_exec[cpunum].timedint_period, cpunum, cpu_timedintcallback);
            }
        }

        /* note that since we start the first frame on the refresh, we can't pulse starting
	   immediately; instead, we back up one VBLANK period, and inch forward until we hit
	   positive time. That time will be the time of the first VBLANK timer callback */
        timer_remove(vblank_timer);

        first_time = -TIME_IN_USEC(Machine.drv.vblank_duration) + vblank_period;
        while (first_time < 0) {
            cpu_vblankcallback.handler(-1);
            first_time += vblank_period;
        }
        vblank_timer = timer_set(first_time, 0, cpu_firstvblankcallback);
    }

}
