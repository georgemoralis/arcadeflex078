/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuintrfH.*;
import static arcadeflex.v078.mame.cpuexec.*;
import static arcadeflex.v078.mame.timerH.*;
//TODO
import static mame056.driverH.MAX_CPU;
import static arcadeflex036.osdepend.*;

public class timer {

    public static final int MAX_TIMERS = 256;

    /*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	logerror x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
/*TODO*///
    /*-------------------------------------------------
	internal timer structure
    -------------------------------------------------*/
    public static class mame_timer {

        public mame_timer next;
        public mame_timer prev;
        public TimerCallbackHandlerPtr callback;
        public int callback_param;
        public int tag;
        public int enabled;
        public int temporary;
        public double period;
        public double start;
        public double expire;
    }

    /*-------------------------------------------------
	global variables
    -------------------------------------------------*/

 /* conversion constants */
    static double[] cycles_to_sec = new double[MAX_CPU];
    static double[] sec_to_cycles = new double[MAX_CPU];

    /* list of active timers */
    static mame_timer[] timers = new mame_timer[MAX_TIMERS];
    static mame_timer timer_head;
    static mame_timer timer_free_head;
    static mame_timer timer_free_tail;

    /* other internal states */
    static double global_offset;
    static mame_timer callback_timer;
    static int callback_timer_modified;
    static double callback_timer_expire_time;

    /*-------------------------------------------------
	get_relative_time - return the current time
	relative to the global_offset
-------------------------------------------------*/
    public static double get_relative_time() {
        int activecpu;

        /* if we're executing as a particular CPU, use its local time as a base */
        activecpu = cpu_getactivecpu();
        if (activecpu >= 0) {
            return cpunum_get_localtime(activecpu);
        }

        /* if we're currently in a callback, use the timer's expiration time as a base */
        if (callback_timer != null) {
            return callback_timer_expire_time;
        }

        /* otherwise, return 0 */
        return 0;
    }

    /*-------------------------------------------------
	timer_new - allocate a new timer
    -------------------------------------------------*/
    public static mame_timer timer_new() {
        mame_timer timer;

        /* remove an empty entry */
        if (timer_free_head == null) {
            return null;
        }
        timer = timer_free_head;
        timer_free_head = timer.next;
        if (timer_free_head == null) {
            timer_free_tail = null;
        }

        return timer;
    }

    /*-------------------------------------------------
            timer_list_insert - insert a new timer into
            the list at the appropriate location
    -------------------------------------------------*/
    public static void timer_list_insert(mame_timer timer) {
        double expire = timer.enabled != 0 ? timer.expire : TIME_NEVER;
        mame_timer t = null;
        mame_timer lt = null;

        /* loop over the timer list */
        for (t = timer_head; t != null; lt = t, t = t.next) {
            /* if the current list entry expires after us, we should be inserted before it */
 /* note that due to floating point rounding, we need to allow a bit of slop here */
 /* because two equal entries -- within rounding precision -- need to sort in */
 /* the order they were inserted into the list */
            if ((t.expire - expire) > TIME_IN_NSEC(1)) {
                /* link the new guy in before the current list entry */
                timer.prev = t.prev;
                timer.next = t;

                if (t.prev != null) {
                    t.prev.next = timer;
                } else {
                    timer_head = timer;
                }
                t.prev = timer;
                return;
            }
        }

        /* need to insert after the last one */
        if (lt != null) {
            lt.next = timer;
        } else {
            timer_head = timer;
        }
        timer.prev = lt;
        timer.next = null;
    }

    /*-------------------------------------------------
	timer_list_remove - remove a timer from the
	linked list
    -------------------------------------------------*/
    public static void timer_list_remove(mame_timer timer) {
        /* remove it from the list */
        if (timer.prev != null) {
            timer.prev.next = timer.next;
        } else {
            timer_head = timer.next;
        }
        if (timer.next != null) {
            timer.next.prev = timer.prev;
        }
    }

    /*-------------------------------------------------
	timer_init - initialize the timer system
    -------------------------------------------------*/
    public static void timer_init() {
        int i;

        /* we need to wait until the first call to timer_cyclestorun before using real CPU times */
        global_offset = 0.0;
        callback_timer = null;
        callback_timer_modified = 0;

        /* reset the timers */
        for (int x = 0; x < timers.length; x++) {
            timers[x] = new mame_timer();
        }

        /* initialize the lists */
        timer_head = null;
        timer_free_head = timers[0];
        for (i = 0; i < MAX_TIMERS - 1; i++) {
            timers[i].tag = -1;
            timers[i].next = timers[i + 1];
        }
        timers[MAX_TIMERS - 1].next = null;
        timer_free_tail = timers[MAX_TIMERS - 1];
    }

    /*-------------------------------------------------
	timer_free - remove all timers on the current
	resource tag
-------------------------------------------------*/
    public static void timer_free() {
        throw new UnsupportedOperationException("Unsupported");
        /*TODO*///	int tag = get_resource_tag();
/*TODO*///	mame_timer *timer, *next;
/*TODO*///
/*TODO*///	/* scan the list */
/*TODO*///	for (timer = timer_head; timer != NULL; timer = next)
/*TODO*///	{
/*TODO*///		/* prefetch the next timer in case we remove this one */
/*TODO*///		next = timer->next;
/*TODO*///
/*TODO*///		/* if this tag matches, remove it */
/*TODO*///		if (timer->tag == tag)
/*TODO*///			timer_remove(timer);
/*TODO*///	}
    }

    /*-------------------------------------------------
	timer_time_until_next_timer - return the
	amount of time until the next timer fires
    -------------------------------------------------*/
    public static double timer_time_until_next_timer() {
        double time = get_relative_time();
        return timer_head.expire - time;
    }

    /*-------------------------------------------------
            timer_adjust_global_time - adjust the global
            time; this is also where we fire the timers
    -------------------------------------------------*/
    public static void timer_adjust_global_time(double delta) {
        mame_timer timer;

        /* add the delta to the global offset */
        global_offset += delta;

        /* scan the list and adjust the times */
        for (timer = timer_head; timer != null; timer = timer.next) {
            timer.start -= delta;
            timer.expire -= delta;
        }

        //LOG(("timer_adjust_global_time: delta=%.9f head->expire=%.9f\n", delta, timer_head->expire));

        /* now process any timers that are overdue */
        while (timer_head.expire < TIME_IN_NSEC(1)) {
            int was_enabled = timer_head.enabled;

            /* if this is a one-shot timer, disable it now */
            timer = timer_head;
            if (timer.period == 0) {
                timer.enabled = 0;
            }

            /* set the global state of which callback we're in */
            callback_timer_modified = 0;
            callback_timer = timer;
            callback_timer_expire_time = timer.expire;

            /* call the callback */
            if (was_enabled != 0 && timer.callback != null) {
                //LOG(("Timer %08X fired (expire=%.9f)\n", (UINT32)timer, timer->expire));
                //profiler_mark(PROFILER_TIMER_CALLBACK);
                timer.callback.handler(timer.callback_param);
                //profiler_mark(PROFILER_END);
            }

            /* clear the callback timer global */
            callback_timer = null;

            /* reset or remove the timer, but only if it wasn't modified during the callback */
            if (callback_timer_modified == 0) {
                /* if the timer is temporary, remove it now */
                if (timer.temporary != 0) {
                    timer_remove(timer);
                } /* otherwise, reschedule it */ else {
                    timer.start = timer.expire;
                    timer.expire += timer.period;

                    timer_list_remove(timer);
                    timer_list_insert(timer);
                }
            }
        }
    }

    /*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_alloc - allocate a permament timer that
/*TODO*///	isn't primed yet
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///mame_timer *timer_alloc(void (*callback)(int))
/*TODO*///{
/*TODO*///	double time = get_relative_time();
/*TODO*///	mame_timer *timer = timer_new();
/*TODO*///
/*TODO*///	/* fail if we can't allocate a new entry */
/*TODO*///	if (!timer)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* fill in the record */
/*TODO*///	timer->callback = callback;
/*TODO*///	timer->callback_param = 0;
/*TODO*///	timer->enabled = 0;
/*TODO*///	timer->temporary = 0;
/*TODO*///	timer->tag = get_resource_tag();
/*TODO*///	timer->period = 0;
/*TODO*///
/*TODO*///	/* compute the time of the next firing and insert into the list */
/*TODO*///	timer->start = time;
/*TODO*///	timer->expire = TIME_NEVER;
/*TODO*///	timer_list_insert(timer);
/*TODO*///
/*TODO*///	/* return a handle */
/*TODO*///	return timer;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_adjust - adjust the time when this
/*TODO*///	timer will fire, and whether or not it will
/*TODO*///	fire periodically
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void timer_adjust(mame_timer *which, double duration, int param, double period)
/*TODO*///{
/*TODO*///	double time = get_relative_time();
/*TODO*///
/*TODO*///	/* if this is the callback timer, mark it modified */
/*TODO*///	if (which == callback_timer)
/*TODO*///		callback_timer_modified = 1;
/*TODO*///
/*TODO*///	/* compute the time of the next firing and insert into the list */
/*TODO*///	which->callback_param = param;
/*TODO*///	which->enabled = 1;
/*TODO*///
/*TODO*///	/* set the start and expire times */
/*TODO*///	which->start = time;
/*TODO*///	which->expire = time + duration;
/*TODO*///	which->period = period;
/*TODO*///
/*TODO*///	/* remove and re-insert the timer in its new order */
/*TODO*///	timer_list_remove(which);
/*TODO*///	timer_list_insert(which);
/*TODO*///
/*TODO*///	/* if this was inserted as the head, abort the current timeslice and resync */
/*TODO*///LOG(("timer_adjust %08X to expire @ %.9f\n", (UINT32)which, which->expire));
/*TODO*///	if (which == timer_head && cpu_getexecutingcpu() >= 0)
/*TODO*///		activecpu_abort_timeslice();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_pulse - allocate a pulse timer, which
/*TODO*///	repeatedly calls the callback using the given
/*TODO*///	period
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void timer_pulse(double period, int param, void (*callback)(int))
/*TODO*///{
/*TODO*///	mame_timer *timer = timer_alloc(callback);
/*TODO*///
/*TODO*///	/* fail if we can't allocate */
/*TODO*///	if (!timer)
/*TODO*///		return;
/*TODO*///
/*TODO*///	/* adjust to our liking */
/*TODO*///	timer_adjust(timer, period, param, period);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_set - allocate a one-shot timer, which
/*TODO*///	calls the callback after the given duration
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void timer_set(double duration, int param, void (*callback)(int))
/*TODO*///{
/*TODO*///	mame_timer *timer = timer_alloc(callback);
/*TODO*///
/*TODO*///	/* fail if we can't allocate */
/*TODO*///	if (!timer)
/*TODO*///		return;
/*TODO*///
/*TODO*///	/* mark the timer temporary */
/*TODO*///	timer->temporary = 1;
/*TODO*///
/*TODO*///	/* adjust to our liking */
/*TODO*///	timer_adjust(timer, duration, param, 0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_reset - reset the timing on a timer
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///void timer_reset(mame_timer *which, double duration)
/*TODO*///{
/*TODO*///	/* adjust the timer */
/*TODO*///	timer_adjust(which, duration, which->callback_param, which->period);
/*TODO*///}
/*TODO*///
/*TODO*///

    /*-------------------------------------------------
	timer_remove - remove a timer from the system
    -------------------------------------------------*/
    public static void timer_remove(mame_timer which) {
        /* error if this is an inactive timer */
        if (which.tag == -1) {
            logerror("timer_remove: removed an inactive timer!\n");
            return;
        }

        /* remove it from the list */
        timer_list_remove(which);

        /* mark it as dead */
        which.tag = -1;

        /* free it up by adding it back to the free list */
        if (timer_free_tail != null) {
            timer_free_tail.next = which;
        } else {
            timer_free_head = which;
        }
        which.next = null;
        timer_free_tail = which;
    }

    /*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_enable - enable/disable a timer
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///int timer_enable(mame_timer *which, int enable)
/*TODO*///{
/*TODO*///	int old;
/*TODO*///
/*TODO*///	/* set the enable flag */
/*TODO*///	old = which->enabled;
/*TODO*///	which->enabled = enable;
/*TODO*///
/*TODO*///	/* remove the timer and insert back into the list */
/*TODO*///	timer_list_remove(which);
/*TODO*///	timer_list_insert(which);
/*TODO*///
/*TODO*///	return old;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_timeelapsed - return the time since the
/*TODO*///	last trigger
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double timer_timeelapsed(mame_timer *which)
/*TODO*///{
/*TODO*///	double time = get_relative_time();
/*TODO*///	return time - which->start;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_timeleft - return the time until the
/*TODO*///	next trigger
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double timer_timeleft(mame_timer *which)
/*TODO*///{
/*TODO*///	double time = get_relative_time();
/*TODO*///	return which->expire - time;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_get_time - return the current time
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double timer_get_time(void)
/*TODO*///{
/*TODO*///	return global_offset + get_relative_time();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_starttime - return the time when this
/*TODO*///	timer started counting
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double timer_starttime(mame_timer *which)
/*TODO*///{
/*TODO*///	return global_offset + which->start;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	timer_firetime - return the time when this
/*TODO*///	timer will fire next
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///double timer_firetime(mame_timer *which)
/*TODO*///{
/*TODO*///	return global_offset + which->expire;
/*TODO*///}
/*TODO*///
}
