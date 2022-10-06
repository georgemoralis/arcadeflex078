/**
 * Ported to 0.56
 */
package mame056;

import arcadeflex.v078.mame.cpuintrfH.cpu_interface;

//cpu imports
import mame056.cpu.dummy_cpu;
import mame056.cpu.z80.z80;

public class cpuintrf {

    /**
     * ***********************************
     *
     * The core list of CPU interfaces
     *
     ************************************
     */
    public static cpu_interface cpuintrf[]
            = {
                new dummy_cpu(),
                new z80(),
                new dummy_cpu(),/*TODO*///	CPU0(8080,	   i8080,	 4,255,1.00,I8080_INTR_LINE,8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(8085A,    i8085,	 4,255,1.00,I8085_INTR_LINE,8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),//CPU0(M6502,    m6502,	 1,  0,1.00,M6502_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M65C02,   m65c02,	 1,  0,1.00,M65C02_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M65SC02,  m65sc02,  1,  0,1.00,M65SC02_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M65CE02,  m65ce02,  1,  0,1.00,M65CE02_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M6509,    m6509,	 1,  0,1.00,M6509_IRQ_LINE, 8, 20,	  0,20,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M6510,    m6510,	 1,  0,1.00,M6510_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M6510T,   m6510t,	 1,  0,1.00,M6510T_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M7501,    m7501,	 1,  0,1.00,M7501_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M8502,    m8502,	 1,  0,1.00,M8502_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),//CPU0(N2A03,    n2a03,	 1,  0,1.00,N2A03_IRQ_LINE, 8, 16,	  0,16,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(M4510,    m4510,	 1,  0,1.00,M4510_IRQ_LINE, 8, 20,	  0,20,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(H6280,    h6280,	 3,  0,1.00,-1,			    8, 21,	  0,21,LE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(I86,	   i86, 	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(I88,	   i88, 	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(I186,	   i186,	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(I188,	   i188,	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(I286,	   i286,	 1,  0,1.00,-1000,		    8, 24,	  0,24,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(V20,	   v20, 	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(V30,	   v30, 	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(V33,	   v33, 	 1,  0,1.00,-1000,		    8, 20,	  0,20,LE,1, 5	),
                new dummy_cpu(),/*TODO*///	CPU0(V60,	   v60, 	 1,  0,1.00,-1000,		   16, 24lew, 0,24,LE,1, 11	),
                new dummy_cpu(),/*TODO*///	CPU0(I8035,    i8035,	 1,  0,1.00,0,              8, 16,	  0,16,LE,1, 2	),
                new dummy_cpu(),//CPU0(I8039,    i8039,	 1,  0,1.00,0,              8, 16,	  0,16,LE,1, 2	),
                new dummy_cpu(),/*TODO*///	CPU0(I8048,    i8048,	 1,  0,1.00,0,              8, 16,	  0,16,LE,1, 2	),
                new dummy_cpu(),/*TODO*///	CPU0(N7751,    n7751,	 1,  0,1.00,0,              8, 16,	  0,16,LE,1, 2	),
                new dummy_cpu(),/*TODO*///	CPU0(I8X41,    i8x41,	 1,  0,1.00,I8X41_INT_IBF,  8, 16,	  0,16,LE,1, 2	),
                new dummy_cpu(),//CPU0(M6800,    m6800,	 1,  0,1.00,M6800_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),/*TODO*///	CPU0(M6801,    m6801,	 1,  0,1.00,M6801_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(M6802,    m6802,	 1,  0,1.00,M6802_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(M6803,    m6803,	 1,  0,1.00,M6803_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(M6808,    m6808,	 1,  0,1.00,M6808_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(HD63701,  hd63701,  1,  0,1.00,HD63701_IRQ_LINE,8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),/*TODO*///	CPU0(NSC8105,  nsc8105,  1,  0,1.00,NSC8105_IRQ_LINE,8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(M6805,    m6805,	 1,  0,1.00,M6805_IRQ_LINE,  8, 16,	  0,11,BE,1, 3	),
                new dummy_cpu(),//	CPU0(M68705,   m68705,	 1,  0,1.00,M68705_IRQ_LINE, 8, 16,	  0,11,BE,1, 3	),
                new dummy_cpu(),//CPU0(HD63705,  hd63705,  8,  0,1.00,HD63705_INT_IRQ1,8, 16,	  0,16,BE,1, 3	),
                new dummy_cpu(),/*TODO*///	CPU0(HD6309,   hd6309,	 2,  0,1.00,HD6309_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(M6809,    m6809,	 2,  0,1.00,M6809_IRQ_LINE,  8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(KONAMI,   konami,	 2,  0,1.00,KONAMI_IRQ_LINE, 8, 16,	  0,16,BE,1, 4	),
                new dummy_cpu(),//CPU0(M68000,   m68000,	 8, -1,1.00,-1,			   16,24bew,  0,24,BE,2,10	),
                new dummy_cpu(),//CPU0(M68010,   m68010,	 8, -1,1.00,-1,			   16,24bew,  0,24,BE,2,10	),
                new dummy_cpu(),//CPU0(M68EC020, m68ec020, 8, -1,1.00,-1,			   32,24bedw, 0,24,BE,4,10	),
                new dummy_cpu(),//CPU0(M68020,   m68020,	 8, -1,1.00,-1, 		   32,32bedw, 0,32,BE,4,10	),
                new dummy_cpu(),//CPU0(T11,	   t11, 	 4,  0,1.00,-1,			   16,16lew,  0,16,LE,2, 6	),
                new dummy_cpu(),//CPU0(S2650,    s2650,	 2,  0,1.00,-1,			    8, 16,	  0,15,LE,1, 3	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS34010)
                new dummy_cpu(),/*TODO*///	CPU0(TMS34010, tms34010, 2,  0,1.00,0,             16,29lew,  3,29,LE,2,10	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS34020)
                new dummy_cpu(),/*TODO*///	CPU0(TMS34020, tms34020, 2,  0,1.00,0,             16,29lew,  3,29,LE,2,10	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS9900)
                new dummy_cpu(),/*TODO*///	CPU0(TMS9900,  tms9900,  1,  0,1.00,-1,			   16,16bew,  0,16,BE,2, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS9940)
                new dummy_cpu(),/*TODO*///	CPU0(TMS9940,  tms9940,  1,  0,1.00,-1,			   16,16bew,  0,16,BE,2, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS9980)
                new dummy_cpu(),/*TODO*///	CPU0(TMS9980,  tms9980a, 1,  0,1.00,-1,			    8, 16,	  0,16,BE,1, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS9985)
                new dummy_cpu(),/*TODO*///	CPU0(TMS9985,  tms9985,  1,  0,1.00,-1,			    8, 16,	  0,16,BE,1, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS9989)
                new dummy_cpu(),/*TODO*///	CPU0(TMS9989,  tms9989,  1,  0,1.00,-1,			    8, 16,	  0,16,BE,1, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS9995)
                new dummy_cpu(),/*TODO*///	CPU0(TMS9995,  tms9995,  1,  0,1.00,-1,			    8, 16,	  0,16,BE,1, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS99105A)
                new dummy_cpu(),/*TODO*///	CPU0(TMS99105A,tms99105a,1,  0,1.00,-1,			   16,16bew,  0,16,BE,2, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS99110A)
                new dummy_cpu(),/*TODO*///	CPU0(TMS99110A,tms99110a,1,  0,1.00,-1,			   16,16bew,  0,16,BE,2, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_Z8000)
                new dummy_cpu(),/*TODO*///	CPU0(Z8000,    z8000,	 2,  0,1.00,0,        	   16,16bew,  0,16,BE,2, 6	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_TMS320C10)
                new dummy_cpu(),/*TODO*///	CPU3(TMS320C10,tms320c10,2,  0,1.00,-1,			   16,16bew, -1,16,BE,2, 4	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_CCPU)
                new dummy_cpu(),/*TODO*///	CPU3(CCPU,	   ccpu,	 2,  0,1.00,-1,			   16,16bew,  0,15,BE,2, 3	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_ADSP2100)
                new dummy_cpu(),/*TODO*///	CPU3(ADSP2100, adsp2100, 4,  0,1.00,-1,			   16,17lew, -1,14,LE,2, 4	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_ADSP2105)
                new dummy_cpu(),/*TODO*///	CPU3(ADSP2105, adsp2105, 4,  0,1.00,-1,			   16,17lew, -1,14,LE,2, 4	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_PSXCPU)
                new dummy_cpu(),/*TODO*///	CPU0(PSXCPU,   mips,	 8, -1,1.00,0,             16,32lew,  0,32,LE,4, 4	),
                /*TODO*///#endif
                /*TODO*///#if (HAS_ASAP)
                /*TODO*///	#define asap_ICount asap_icount
                new dummy_cpu(),/*TODO*///	CPU0(ASAP,	   asap,	 1,  0,1.00,-1,			   32,32ledw, 0,32,LE,4, 12 ),
                /*TODO*///#endif
                /*TODO*///#if (HAS_UPD7810)
                /*TODO*///#define upd7810_ICount upd7810_icount
                new dummy_cpu(),/*TODO*///	CPU0(UPD7810,  upd7810,  2,  0,1.00,UPD7810_INTF1,  8, 16,	  0,16,LE,1, 4	),
            };

}
