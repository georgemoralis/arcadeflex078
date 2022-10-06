/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

public class timerH {
/*TODO*///
/*TODO*///extern double cycles_to_sec[];
/*TODO*///extern double sec_to_cycles[];
/*TODO*///
/*TODO*///#define TIME_IN_HZ(hz)        (1.0 / (double)(hz))
/*TODO*///#define TIME_IN_CYCLES(c,cpu) ((double)(c) * cycles_to_sec[cpu])
/*TODO*///#define TIME_IN_SEC(s)        ((double)(s))
/*TODO*///#define TIME_IN_MSEC(ms)      ((double)(ms) * (1.0 / 1000.0))
/*TODO*///#define TIME_IN_USEC(us)      ((double)(us) * (1.0 / 1000000.0))
/*TODO*///#define TIME_IN_NSEC(us)      ((double)(us) * (1.0 / 1000000000.0))
/*TODO*///
/*TODO*///#define TIME_NOW              (0.0)
/*TODO*///#define TIME_NEVER            (1.0e30)
/*TODO*///
/*TODO*///#define TIME_TO_CYCLES(cpu,t) ((int)((t) * sec_to_cycles[cpu]))
/*TODO*///
/*TODO*///typedef struct _mame_timer mame_timer;
/*TODO*///
/*TODO*///
/*TODO*///void timer_init(void);
/*TODO*///void timer_free(void);
/*TODO*///double timer_time_until_next_timer(void);
/*TODO*///void timer_adjust_global_time(double delta);
/*TODO*///mame_timer *timer_alloc(void (*callback)(int));
/*TODO*///void timer_adjust(mame_timer *which, double duration, int param, double period);
/*TODO*///void timer_pulse(double period, int param, void (*callback)(int));
/*TODO*///void timer_set(double duration, int param, void (*callback)(int));
/*TODO*///void timer_reset(mame_timer *which, double duration);
/*TODO*///void timer_remove(mame_timer *which);
/*TODO*///int timer_enable(mame_timer *which, int enable);
/*TODO*///double timer_timeelapsed(mame_timer *which);
/*TODO*///double timer_timeleft(mame_timer *which);
/*TODO*///double timer_get_time(void);
/*TODO*///double timer_starttime(mame_timer *which);
/*TODO*///double timer_firetime(mame_timer *which);
/*TODO*///
/*TODO*///#ifdef __cplusplus
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
