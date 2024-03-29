/***************************************************************************

1942

driver by Paul Leaman


MAIN CPU:

0000-bfff ROM (8000-bfff banked)
cc00-cc7f Sprites
d000-d3ff Video RAM
d400-d7ff Color RAM
d800-dbff Background RAM (groups of 32 bytes, 16 code, 16 color/attribute)
e000-efff RAM

read:
c000      IN0
c001      IN1
c002      IN2
c003      DSW0
c004      DSW1

write:
c800      command for the audio CPU
c802-c803 background scroll
c804      bit 7: flip screen
          bit 4: cpu B reset
		  bit 0: coin counter
c805      background palette bank selector
c806      bit 0-1 ROM bank selector 00=1-N5.BIN
                                    01=1-N6.BIN
                                    10=1-N7.BIN



SOUND CPU:

0000-3fff ROM
4000-47ff RAM
6000      command from the main CPU
8000      8910 #1 control
8001      8910 #1 write
c000      8910 #2 control
c001      8910 #2 write



Game runs in interrupt mode 0 (the devices supply the interrupt number).

Two interrupts must be triggered per refresh for the game to function
correctly.

0x10 is the video retrace. This controls the speed of the game and generally
     drives the code. This must be triggerd for each video retrace.
0x08 is the sound card service interupt. The game uses this to throw sounds
     at the sound CPU.

***************************************************************************/

/*
 * ported to v0.56
 * using automatic conversion tool v0.01
 */ 
package mame056.drivers;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
import static arcadeflex.v078.mame.commonH.*;
import static mame056.commonH.*;
//mame imports
import static arcadeflex.v078.mame.cpuexec.*;
import static arcadeflex.v078.mame.cpuexecH.*;
import static arcadeflex.v078.mame.cpuint.*;
import static arcadeflex.v078.mame.cpuintrfH.CPU_Z80;
import static arcadeflex.v078.mame.cpuintrfH.HOLD_LINE;
import static arcadeflex.v078.mame.driverH.*;
import static arcadeflex.v078.mame.inptportH.IPT_BUTTON1;
import static arcadeflex.v078.mame.inptportH.IPT_BUTTON2;
import static arcadeflex.v078.mame.inptportH.IPT_COIN1;
import static arcadeflex.v078.mame.inptportH.IPT_COIN2;
import static arcadeflex.v078.mame.inptportH.IPT_JOYSTICK_DOWN;
import static arcadeflex.v078.mame.inptportH.IPT_JOYSTICK_LEFT;
import static arcadeflex.v078.mame.inptportH.IPT_JOYSTICK_RIGHT;
import static arcadeflex.v078.mame.inptportH.IPT_JOYSTICK_UP;
import static arcadeflex.v078.mame.inptportH.IPT_SERVICE1;
import static arcadeflex.v078.mame.inptportH.IPT_START1;
import static arcadeflex.v078.mame.inptportH.IPT_START2;
import static arcadeflex.v078.mame.inptportH.IPT_UNKNOWN;
import static arcadeflex.v078.mame.memoryH.*;
import static arcadeflex.v078.vidhrdw.generic.spriteram;
import static arcadeflex.v078.vidhrdw.generic.spriteram_size;

import arcadeflex056.fucPtr.InputPortPtr;
import arcadeflex056.fucPtr.RomLoadPtr;

import common.ptr.UBytePtr;
import static mame056.common.*;
import static mame056.drawgfxH.*;
import static mame056.driverH.DEFAULT_60HZ_VBLANK_DURATION;
import mame056.driverH.GameDriver;
import static mame056.driverH.ROT270;
import static mame056.driverH.VIDEO_TYPE_RASTER;
import static mame056.inptport.*;
import static mame056.inptportH.*;
import static mame056.sndintrf.*;
import static mame056.vidhrdw._1942.*;

public class _1942
{
	
	public static WriteHandlerPtr c1942_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1));
	
	
		bankaddress = 0x10000 + (data & 0x03) * 0x4000;
		cpu_setbank(1,new UBytePtr(RAM, bankaddress));
	} };
	
	
	
	public static InterruptHandlerPtr c1942_interrupt = new InterruptHandlerPtr() {public void handler()
	{
		if (cpu_getiloops() != 0)
			cpu_set_irq_line_and_vector(0, 0, HOLD_LINE, 0xcf);/* RST 08h */
		else
			cpu_set_irq_line_and_vector(0, 0, HOLD_LINE, 0xd7);	/* RST 10h - vblank */
	} };
	
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xc000, 0xc000, input_port_0_r ),	/* IN0 */
		new Memory_ReadAddress( 0xc001, 0xc001, input_port_1_r ),	/* IN1 */
		new Memory_ReadAddress( 0xc002, 0xc002, input_port_2_r ),	/* IN2 */
		new Memory_ReadAddress( 0xc003, 0xc003, input_port_3_r ),	/* DSW0 */
		new Memory_ReadAddress( 0xc004, 0xc004, input_port_4_r ),	/* DSW1 */
		new Memory_ReadAddress( 0xd000, 0xdbff, MRA_RAM ),
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc800, 0xc800, soundlatch_w ),
		new Memory_WriteAddress( 0xc802, 0xc803, c1942_scroll_w ),
		new Memory_WriteAddress( 0xc804, 0xc804, c1942_c804_w ),
		new Memory_WriteAddress( 0xc805, 0xc805, c1942_palette_bank_w ),
		new Memory_WriteAddress( 0xc806, 0xc806, c1942_bankswitch_w ),
		new Memory_WriteAddress( 0xcc00, 0xcc7f, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0xd000, 0xd7ff, c1942_fgvideoram_w, c1942_fgvideoram ),
		new Memory_WriteAddress( 0xd800, 0xdbff, c1942_bgvideoram_w, c1942_bgvideoram ),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x47ff, MRA_RAM ),
		new Memory_ReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x4000, 0x47ff, MWA_RAM ),
/*TODO*///		new Memory_WriteAddress( 0x8000, 0x8000, AY8910_control_port_0_w ),
/*TODO*///		new Memory_WriteAddress( 0x8001, 0x8001, AY8910_write_port_0_w ),
/*TODO*///		new Memory_WriteAddress( 0xc000, 0xc000, AY8910_control_port_1_w ),
/*TODO*///		new Memory_WriteAddress( 0xc001, 0xc001, AY8910_write_port_1_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_1942 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x30, "20000 80000" );
		PORT_DIPSETTING(    0x20, "20000 100000" );
		PORT_DIPSETTING(    0x10, "30000 80000" );
		PORT_DIPSETTING(    0x00, "30000 100000" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x80, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x00, "5" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_SERVICE( 0x08, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x40, "Easy" );
		PORT_DIPSETTING(    0x60, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x80, "Freeze" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,3),
		3,
		new int[] { RGN_FRAC(0,3), RGN_FRAC(1,3), RGN_FRAC(2,3) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+4, RGN_FRAC(1,2)+0, 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3,
				16*16+0, 16*16+1, 16*16+2, 16*16+3, 16*16+8+0, 16*16+8+1, 16*16+8+2, 16*16+8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		64*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,             0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,          64*4, 4*32 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 64*4+4*32*8, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
/*TODO*///	static AY8910interface ay8910_interface = new AY8910interface
/*TODO*///	(
/*TODO*///		2,	/* 2 chips */
/*TODO*///		1500000,	/* 1.5 MHz ? */
/*TODO*///		new int[] { 25, 25 },
/*TODO*///		new ReadHandlerPtr[] { null, null },
/*TODO*///		new ReadHandlerPtr[] { null, null },
/*TODO*///		new WriteHandlerPtr[] { null, null },
/*TODO*///		new WriteHandlerPtr[] { null, null }
/*TODO*///	);
	
	
	
/*TODO*///	static MachineDriver machine_driver_1942 = new MachineDriver
/*TODO*///	(
/*TODO*///		/* basic machine hardware */
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80,
/*TODO*///				4000000,	/* 4 MHz (?) */
/*TODO*///				readmem,writemem,null,null,
/*TODO*///				c1942_interrupt,2
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///				3000000,	/* 3 MHz ??? */
/*TODO*///				sound_readmem,sound_writemem,null,null,
/*TODO*///				interrupt,4
/*TODO*///			)
/*TODO*///		},
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
/*TODO*///		null,
/*TODO*///	
/*TODO*///		/* video hardware */
/*TODO*///		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
/*TODO*///		gfxdecodeinfo,
/*TODO*///		256, 64*4+4*32*8+16*16,
/*TODO*///		c1942_vh_convert_color_prom,
/*TODO*///	
/*TODO*///		VIDEO_TYPE_RASTER,
/*TODO*///		null,
/*TODO*///		c1942_vh_start,
/*TODO*///		null,
/*TODO*///		c1942_vh_screenrefresh,
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///		0,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_AY8910,
/*TODO*///				ay8910_interface
/*TODO*///			)
/*TODO*///		}
/*TODO*///	);
	
	
    /* basic machine hardware */
    public static MachineHandlerPtr machine_driver_1942 = new MachineHandlerPtr() {/* basic machine hardware */
        public void handler(InternalMachineDriver machine) {
                MACHINE_DRIVER_START(machine);
		MDRV_CPU_ADD(CPU_Z80, 4000000);	/* 4 MHz (?) */
		MDRV_CPU_MEMORY(readmem,writemem);
		MDRV_CPU_VBLANK_INT(c1942_interrupt,2);
	
		MDRV_CPU_ADD(CPU_Z80, 3000000);
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU);	/* 3 MHz ??? */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem);
		MDRV_CPU_VBLANK_INT(irq0_line_hold,4);
	
		MDRV_FRAMES_PER_SECOND(60);
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION);
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER);
		MDRV_SCREEN_SIZE(32*8, 32*8);
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1);
		MDRV_GFXDECODE(gfxdecodeinfo);
		MDRV_PALETTE_LENGTH(256);
		MDRV_COLORTABLE_LENGTH(64*4+4*32*8+16*16);
	
		MDRV_PALETTE_INIT(c1942_vh_convert_color_prom);
		MDRV_VIDEO_START(c1942_vh_start);
		MDRV_VIDEO_UPDATE(c1942_vh_screenrefresh);
	
		/* sound hardware */
/*TODO*///		MDRV_SOUND_ADD(AY8910, ay8910_interface);
                MACHINE_DRIVER_END();
        }
    };
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_1942 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1c000, REGION_CPU1, 0 );/* 64k for code + 3*16k for the banked ROMs images */
		ROM_LOAD( "1-n3a.bin",    0x00000, 0x4000, 0x40201bab );
		ROM_LOAD( "1-n4.bin",     0x04000, 0x4000, 0xa60ac644 );
		ROM_LOAD( "1-n5.bin",     0x10000, 0x4000, 0x835f7b24 );
		ROM_LOAD( "1-n6.bin",     0x14000, 0x2000, 0x821c6481 );
		ROM_LOAD( "1-n7.bin",     0x18000, 0x4000, 0x5df525e1 );
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "1-c11.bin",    0x0000, 0x4000, 0xbd87f06b );
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1-f2.bin",     0x0000, 0x2000, 0x6ebca191 );/* characters */
	
		ROM_REGION( 0xc000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "2-a1.bin",     0x0000, 0x2000, 0x3884d9eb );/* tiles */
		ROM_LOAD( "2-a2.bin",     0x2000, 0x2000, 0x999cf6e0 );
		ROM_LOAD( "2-a3.bin",     0x4000, 0x2000, 0x8edb273a );
		ROM_LOAD( "2-a4.bin",     0x6000, 0x2000, 0x3a2726c3 );
		ROM_LOAD( "2-a5.bin",     0x8000, 0x2000, 0x1bd3d8bb );
		ROM_LOAD( "2-a6.bin",     0xa000, 0x2000, 0x658f02c4 );
	
		ROM_REGION( 0x10000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "2-l1.bin",     0x00000, 0x4000, 0x2528bec6 );/* sprites */
		ROM_LOAD( "2-l2.bin",     0x04000, 0x4000, 0xf89287aa );
		ROM_LOAD( "2-n1.bin",     0x08000, 0x4000, 0x024418f8 );
		ROM_LOAD( "2-n2.bin",     0x0c000, 0x4000, 0xe2c7e489 );
	
		ROM_REGION( 0x0a00, REGION_PROMS, 0 );
		ROM_LOAD( "08e_sb-5.bin", 0x0000, 0x0100, 0x93ab8153 );/* red component */
		ROM_LOAD( "09e_sb-6.bin", 0x0100, 0x0100, 0x8ab44f7d );/* green component */
		ROM_LOAD( "10e_sb-7.bin", 0x0200, 0x0100, 0xf4ade9a4 );/* blue component */
		ROM_LOAD( "f01_sb-0.bin", 0x0300, 0x0100, 0x6047d91b );/* char lookup table */
		ROM_LOAD( "06d_sb-4.bin", 0x0400, 0x0100, 0x4858968d );/* tile lookup table */
		ROM_LOAD( "03k_sb-8.bin", 0x0500, 0x0100, 0xf6fad943 );/* sprite lookup table */
		ROM_LOAD( "01d_sb-2.bin", 0x0600, 0x0100, 0x8bb8b3df );/* tile palette selector? (not used) */
		ROM_LOAD( "02d_sb-3.bin", 0x0700, 0x0100, 0x3b0c99af );/* tile palette selector? (not used) */
		ROM_LOAD( "k06_sb-1.bin", 0x0800, 0x0100, 0x712ac508 );/* interrupt timing (not used) */
		ROM_LOAD( "01m_sb-9.bin", 0x0900, 0x0100, 0x4921635c );/* video timing? (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_1942a = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1c000, REGION_CPU1, 0 );/* 64k for code + 3*16k for the banked ROMs images */
		ROM_LOAD( "1-n3.bin",     0x00000, 0x4000, 0x612975f2 );
		ROM_LOAD( "1-n4.bin",     0x04000, 0x4000, 0xa60ac644 );
		ROM_LOAD( "1-n5.bin",     0x10000, 0x4000, 0x835f7b24 );
		ROM_LOAD( "1-n6.bin",     0x14000, 0x2000, 0x821c6481 );
		ROM_LOAD( "1-n7.bin",     0x18000, 0x4000, 0x5df525e1 );
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "1-c11.bin",    0x0000, 0x4000, 0xbd87f06b );
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1-f2.bin",     0x0000, 0x2000, 0x6ebca191 );/* characters */
	
		ROM_REGION( 0xc000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "2-a1.bin",     0x0000, 0x2000, 0x3884d9eb );/* tiles */
		ROM_LOAD( "2-a2.bin",     0x2000, 0x2000, 0x999cf6e0 );
		ROM_LOAD( "2-a3.bin",     0x4000, 0x2000, 0x8edb273a );
		ROM_LOAD( "2-a4.bin",     0x6000, 0x2000, 0x3a2726c3 );
		ROM_LOAD( "2-a5.bin",     0x8000, 0x2000, 0x1bd3d8bb );
		ROM_LOAD( "2-a6.bin",     0xa000, 0x2000, 0x658f02c4 );
	
		ROM_REGION( 0x10000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "2-l1.bin",     0x00000, 0x4000, 0x2528bec6 );/* sprites */
		ROM_LOAD( "2-l2.bin",     0x04000, 0x4000, 0xf89287aa );
		ROM_LOAD( "2-n1.bin",     0x08000, 0x4000, 0x024418f8 );
		ROM_LOAD( "2-n2.bin",     0x0c000, 0x4000, 0xe2c7e489 );
	
		ROM_REGION( 0x0a00, REGION_PROMS, 0 );
		ROM_LOAD( "08e_sb-5.bin", 0x0000, 0x0100, 0x93ab8153 );/* red component */
		ROM_LOAD( "09e_sb-6.bin", 0x0100, 0x0100, 0x8ab44f7d );/* green component */
		ROM_LOAD( "10e_sb-7.bin", 0x0200, 0x0100, 0xf4ade9a4 );/* blue component */
		ROM_LOAD( "f01_sb-0.bin", 0x0300, 0x0100, 0x6047d91b );/* char lookup table */
		ROM_LOAD( "06d_sb-4.bin", 0x0400, 0x0100, 0x4858968d );/* tile lookup table */
		ROM_LOAD( "03k_sb-8.bin", 0x0500, 0x0100, 0xf6fad943 );/* sprite lookup table */
		ROM_LOAD( "01d_sb-2.bin", 0x0600, 0x0100, 0x8bb8b3df );/* tile palette selector? (not used) */
		ROM_LOAD( "02d_sb-3.bin", 0x0700, 0x0100, 0x3b0c99af );/* tile palette selector? (not used) */
		ROM_LOAD( "k06_sb-1.bin", 0x0800, 0x0100, 0x712ac508 );/* interrupt timing (not used) */
		ROM_LOAD( "01m_sb-9.bin", 0x0900, 0x0100, 0x4921635c );/* video timing? (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_1942b = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1c000, REGION_CPU1, 0 );/* 64k for code + 3*16k for the banked ROMs images */
		ROM_LOAD( "srb-03.n3",    0x00000, 0x4000, 0xd9dafcc3 );
		ROM_LOAD( "srb-04.n4",    0x04000, 0x4000, 0xda0cf924 );
		ROM_LOAD( "srb-05.n5",    0x10000, 0x4000, 0xd102911c );
		ROM_LOAD( "srb-06.n6",    0x14000, 0x2000, 0x466f8248 );
		ROM_LOAD( "srb-07.n7",    0x18000, 0x4000, 0x0d31038c );
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "1-c11.bin",    0x0000, 0x4000, 0xbd87f06b );
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1-f2.bin",     0x0000, 0x2000, 0x6ebca191 );/* characters */
	
		ROM_REGION( 0xc000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "2-a1.bin",     0x0000, 0x2000, 0x3884d9eb );/* tiles */
		ROM_LOAD( "2-a2.bin",     0x2000, 0x2000, 0x999cf6e0 );
		ROM_LOAD( "2-a3.bin",     0x4000, 0x2000, 0x8edb273a );
		ROM_LOAD( "2-a4.bin",     0x6000, 0x2000, 0x3a2726c3 );
		ROM_LOAD( "2-a5.bin",     0x8000, 0x2000, 0x1bd3d8bb );
		ROM_LOAD( "2-a6.bin",     0xa000, 0x2000, 0x658f02c4 );
	
		ROM_REGION( 0x10000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "2-l1.bin",     0x00000, 0x4000, 0x2528bec6 );/* sprites */
		ROM_LOAD( "2-l2.bin",     0x04000, 0x4000, 0xf89287aa );
		ROM_LOAD( "2-n1.bin",     0x08000, 0x4000, 0x024418f8 );
		ROM_LOAD( "2-n2.bin",     0x0c000, 0x4000, 0xe2c7e489 );
	
		ROM_REGION( 0x0a00, REGION_PROMS, 0 );
		ROM_LOAD( "08e_sb-5.bin", 0x0000, 0x0100, 0x93ab8153 );/* red component */
		ROM_LOAD( "09e_sb-6.bin", 0x0100, 0x0100, 0x8ab44f7d );/* green component */
		ROM_LOAD( "10e_sb-7.bin", 0x0200, 0x0100, 0xf4ade9a4 );/* blue component */
		ROM_LOAD( "f01_sb-0.bin", 0x0300, 0x0100, 0x6047d91b );/* char lookup table */
		ROM_LOAD( "06d_sb-4.bin", 0x0400, 0x0100, 0x4858968d );/* tile lookup table */
		ROM_LOAD( "03k_sb-8.bin", 0x0500, 0x0100, 0xf6fad943 );/* sprite lookup table */
		ROM_LOAD( "01d_sb-2.bin", 0x0600, 0x0100, 0x8bb8b3df );/* tile palette selector? (not used) */
		ROM_LOAD( "02d_sb-3.bin", 0x0700, 0x0100, 0x3b0c99af );/* tile palette selector? (not used) */
		ROM_LOAD( "k06_sb-1.bin", 0x0800, 0x0100, 0x712ac508 );/* interrupt timing (not used) */
		ROM_LOAD( "01m_sb-9.bin", 0x0900, 0x0100, 0x4921635c );/* video timing? (not used) */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_1942	   = new GameDriver("1984"	,"1942"	,"_1942.java"	,rom_1942,null	,machine_driver_1942	,input_ports_1942	,null	,ROT270	,	"Capcom", "1942 (set 1)" );
	public static GameDriver driver_1942a	   = new GameDriver("1984"	,"1942a"	,"_1942.java"	,rom_1942a,driver_1942	,machine_driver_1942	,input_ports_1942	,null	,ROT270	,	"Capcom", "1942 (set 2)" );
	public static GameDriver driver_1942b	   = new GameDriver("1984"	,"1942b"	,"_1942.java"	,rom_1942b,driver_1942	,machine_driver_1942	,input_ports_1942	,null	,ROT270	,	"Capcom", "1942 (set 3)" );
}