package arcadeflex056;

import arcadeflex.v078.mame.drawgfxH.rectangle;
import static common.ptr.*;
import static mame056.sndintrfH.*;
import static mame056.commonH.*;

public class fucPtr {
 
    public static abstract interface ReadHandlerPtr16 {

        public abstract int handler(int offset, int d2);
    }

    public static abstract interface WriteHandlerPtr16 {

        public abstract void handler(int offset, int data, int d2);
    }

    

    public static abstract interface InitDriverPtr {

        public abstract void handler();
    }

    public static abstract interface VhConvertColorPromPtr {

        public abstract void handler(char[] palette, char[] colortable, UBytePtr color_prom);
    }

    public static abstract interface VhEofCallbackPtr {

        public abstract void handler();
    }

    public static abstract interface VhStopPtr {

        public abstract void handler();
    }

    public static abstract interface VhUpdatePtr {

        public abstract void handler(mame_bitmap bitmap, rectangle cliprect);
    }

    public static abstract interface ShStartPtr {

        public abstract int handler(MachineSound msound);
    }

    public static abstract interface ShStopPtr {

        public abstract void handler();
    }

    public static abstract interface ShUpdatePtr {

        public abstract void handler();
    }

    public static abstract interface RomLoadPtr {

        public abstract void handler();
    }

    public static abstract interface InputPortPtr {

        public abstract void handler();
    }

    public static abstract interface nvramPtr {

        public abstract void handler(Object file, int read_or_write);
    };

    public static abstract interface WriteYmHandlerPtr {

        public abstract void handler(int linestate);
    }
}
