/**
 * ported to v0.78
 */
package arcadeflex.v078.generic;

//mame imports
import static arcadeflex.v078.mame.drawgfxH.*;
import static arcadeflex.v078.mame.driverH.*;
//common imports
import static common.ptr.*;

//TODO
import mame056.commonH.mame_bitmap;

public class funcPtr {

    /**
     * common functions
     */
    public abstract static interface ReadHandlerPtr {

        public abstract int handler(int offset);
    }

    public abstract static interface WriteHandlerPtr {

        public abstract void handler(int offset, int data);
    }

    public static abstract interface InterruptHandlerPtr {

        public abstract void handler();
    }

    public static abstract interface MachineHandlerPtr {

        public abstract void handler(InternalMachineDriver machine);
    }

    public static abstract interface MachineInitHandlerPtr {

        public abstract void handler();
    }

    public static abstract interface MachineStopHandlerPtr {

        public abstract void handler();
    }

    /**
     * memory related
     */
    public static abstract interface OpbaseHandlerPtr {

        public abstract int handler(int address);
    }

    public static abstract interface SetOpbaseHandlerPtr {

        public abstract void handler(int pc);
    }

    /**
     * cpu interface related
     */
    public static abstract interface BurnHandlerPtr {

        public abstract void handler(int cycles);
    }

    public static abstract interface IrqCallbackHandlerPtr {

        public abstract int handler(int irqline);
    }

    /**
     * Daisy chain related
     */
    public static abstract interface DaisyChainInterruptEntryPtr {

        public abstract int handler(int i);
    }

    public static abstract interface DaisyChainResetPtr {

        public abstract void handler(int i);
    }

    public static abstract interface DaisyChainInterruptRetiPtr {

        public abstract void handler(int i);
    }

    /**
     * Timer callback
     */
    public static abstract interface TimerCallbackHandlerPtr {

        public abstract void handler(int i);
    }

    /**
     * Video related
     */
    public static abstract interface PaletteInitHandlerPtr {

        public abstract void handler(char[] colortable, UBytePtr color_prom);
    }

    public static abstract interface VideoStartHandlerPtr {

        public abstract int handler();
    }

    public static abstract interface VideoUpdateHandlerPtr {

        public abstract void handler(mame_bitmap bitmap, rectangle cliprect);
    }
}
