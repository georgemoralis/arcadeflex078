/**
 * Ported to 0.56
 */
package mame056;

import static arcadeflex.v078.mame.cpuintrf.activecpu_get_pc_byte;
import static arcadeflex.v078.mame.cpuintrf.activecpu_get_reg;
import static arcadeflex.v078.mame.cpuintrf.activecpu_set_op_base;
import static arcadeflex.v078.mame.cpuintrfH.REG_PC;
import static arcadeflex.v078.mame.cpuintrfH.REG_PREVIOUSPC;
import static arcadeflex.v078.mame.cpuintrfH.REG_SP_CONTENTS;

public class cpuintrfH {

    /**
     * ***********************************
     *
     * Enum listing all the CPUs
     *
     ************************************
     */
    public static final int CPU_DUMMY = 0;
    public static final int CPU_Z80 = 1;
    public static final int CPU_8080 = 2;
    public static final int CPU_8085A = 3;
    public static final int CPU_M6502 = 4;
    public static final int CPU_M65C02 = 5;
    public static final int CPU_M65SC02 = 6;
    public static final int CPU_M65CE02 = 7;
    public static final int CPU_M6509 = 8;
    public static final int CPU_M6510 = 9;
    public static final int CPU_M6510T = 10;
    public static final int CPU_M7501 = 11;
    public static final int CPU_M8502 = 12;
    public static final int CPU_N2A03 = 13;
    public static final int CPU_M4510 = 14;
    public static final int CPU_H6280 = 15;
    public static final int CPU_I86 = 16;
    public static final int CPU_I88 = 17;
    public static final int CPU_I186 = 18;
    public static final int CPU_I188 = 19;
    public static final int CPU_I286 = 20;
    public static final int CPU_V20 = 21;
    public static final int CPU_V30 = 22;
    public static final int CPU_V33 = 23;
    public static final int CPU_V60 = 24;
    public static final int CPU_I8035 = 25;
    public static final int CPU_I8039 = 26;
    public static final int CPU_I8048 = 27;
    public static final int CPU_N7751 = 28;
    public static final int CPU_I8X41 = 29;
    public static final int CPU_M6800 = 30;
    public static final int CPU_M6801 = 31;
    public static final int CPU_M6802 = 32;
    public static final int CPU_M6803 = 33;
    public static final int CPU_M6808 = 34;
    public static final int CPU_HD63701 = 35;
    public static final int CPU_NSC8105 = 36;
    public static final int CPU_M6805 = 37;
    public static final int CPU_M68705 = 38;
    public static final int CPU_HD63705 = 39;
    public static final int CPU_HD6309 = 40;
    public static final int CPU_M6809 = 41;
    public static final int CPU_KONAMI = 42;
    public static final int CPU_M68000 = 43;
    public static final int CPU_M68010 = 44;
    public static final int CPU_M68EC020 = 45;
    public static final int CPU_M68020 = 46;
    public static final int CPU_T11 = 47;
    public static final int CPU_S2650 = 48;
    public static final int CPU_TMS34010 = 49;
    public static final int CPU_TMS34020 = 50;
    public static final int CPU_TMS9900 = 51;
    public static final int CPU_TMS9940 = 52;
    public static final int CPU_TMS9980 = 53;
    public static final int CPU_TMS9985 = 54;
    public static final int CPU_TMS9989 = 55;
    public static final int CPU_TMS9995 = 56;
    public static final int CPU_TMS99105A = 57;
    public static final int CPU_TMS99110A = 58;
    public static final int CPU_Z8000 = 59;
    public static final int CPU_TMS320C10 = 60;
    public static final int CPU_CCPU = 61;
    public static final int CPU_ADSP2100 = 62;
    public static final int CPU_ADSP2105 = 63;
    public static final int CPU_PSXCPU = 64;
    public static final int CPU_ASAP = 65;
    public static final int CPU_UPD7810 = 66;

    public static final int CPU_COUNT = 67;





 

    /**
     * ***********************************
     *
     * Core CPU interface structure
     *
     ************************************
     */
    public static abstract interface burnPtr {

        public abstract void handler(int cycles);
    }

    public static abstract interface IrqcallbackPtr {

        public abstract int handler(int irqline);
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
    public static int cpu_geturnpc(){
        return activecpu_get_reg(REG_SP_CONTENTS);
    }

    /* map older cpu_* functions to activecpu_* */
    public static int cpu_get_pc() {
        return activecpu_get_pc();
    }

    /*TODO*///#define		cpu_get_sp					activecpu_get_sp
    public static int cpu_get_reg(int regnum) {
        return activecpu_get_reg(regnum);
    }

    /*TODO*///#define		cpu_set_reg					activecpu_set_reg
    public static int cpu_getpreviouspc() {
        return activecpu_get_previouspc();
    }

    public static void cpu_set_op_base(int val) {
        activecpu_set_op_base(val);
    }

    public static int cpu_get_pc_byte() {
        return activecpu_get_pc_byte();
    }


}
