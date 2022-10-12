/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

public class input {
/*TODO*////******************************************************************************
/*TODO*///
/*TODO*///  input.c
/*TODO*///
/*TODO*///  Handle input from the user - keyboard, joystick, etc.
/*TODO*///
/*TODO*///******************************************************************************/
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*///
/*TODO*///#include <time.h>
/*TODO*///#include <assert.h>
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Codes */
/*TODO*///
/*TODO*////* Subtype of codes */
/*TODO*///#define CODE_TYPE_NONE 0U /* code not assigned */
/*TODO*///#define CODE_TYPE_KEYBOARD 1U /* keyboard code */
/*TODO*///#define CODE_TYPE_JOYSTICK 2U /* joystick code */
/*TODO*///
/*TODO*////* Informations for every input code */
/*TODO*///struct code_info {
/*TODO*///	int memory; /* boolean memory */
/*TODO*///	unsigned oscode; /* os dependant code */
/*TODO*///	unsigned type; /* subtype: CODE_TYPE_KEYBOARD or CODE_TYPE_JOYSTICK */
/*TODO*///};
/*TODO*///
/*TODO*////* Main code table, generic KEYCODE_*, JOYCODE_* are indexes in this table */
/*TODO*///static struct code_info* code_map;
/*TODO*///
/*TODO*////* Size of the table */
/*TODO*///static unsigned code_mac;
/*TODO*///
/*TODO*////* Create the code table */
/*TODO*///int code_init(void)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	assert(	__code_key_first == 0
/*TODO*///		&& __code_key_last + 1 == __code_joy_first
/*TODO*///		&& __code_joy_last + 1 == __code_max );
/*TODO*///
/*TODO*///	/* allocate */
/*TODO*///	code_map = (struct code_info*)malloc( __code_max * sizeof(struct code_info) );
/*TODO*///	if (!code_map)
/*TODO*///		return -1;
/*TODO*///
/*TODO*///	code_mac = 0;
/*TODO*///
/*TODO*///	/* insert all known codes */
/*TODO*///	for(i=0;i<__code_max;++i)
/*TODO*///	{
/*TODO*///		code_map[code_mac].memory = 0;
/*TODO*///		code_map[code_mac].oscode = 0; /* not used */
/*TODO*///
/*TODO*///		if (__code_key_first <= i && i <= __code_key_last)
/*TODO*///			code_map[code_mac].type = CODE_TYPE_KEYBOARD;
/*TODO*///		else if (__code_joy_first <= i && i <= __code_joy_last)
/*TODO*///			code_map[code_mac].type = CODE_TYPE_JOYSTICK;
/*TODO*///		else {
/*TODO*///			/* never happen */
/*TODO*///			assert(0);
/*TODO*///			code_map[code_mac].type = CODE_TYPE_NONE;
/*TODO*///		}
/*TODO*///		++code_mac;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Find the osd record of an oscode */
/*TODO*///INLINE const struct KeyboardInfo* internal_oscode_find_keyboard(unsigned oscode)
/*TODO*///{
/*TODO*///	const struct KeyboardInfo *keyinfo;
/*TODO*///	keyinfo = osd_get_key_list();
/*TODO*///	while (keyinfo->name)
/*TODO*///	{
/*TODO*///		if (keyinfo->code == oscode)
/*TODO*///			return keyinfo;
/*TODO*///		++keyinfo;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///INLINE const struct JoystickInfo* internal_oscode_find_joystick(unsigned oscode)
/*TODO*///{
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///	joyinfo = osd_get_joy_list();
/*TODO*///	while (joyinfo->name)
/*TODO*///	{
/*TODO*///		if (joyinfo->code == oscode)
/*TODO*///			return joyinfo;
/*TODO*///		++joyinfo;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Find a oscode in the table */
/*TODO*///static int internal_oscode_find(unsigned oscode, unsigned type)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///	const struct KeyboardInfo *keyinfo;
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///
/*TODO*///	/* Search in the main table for an oscode */
/*TODO*///	for(i=__code_max;i<code_mac;++i)
/*TODO*///		if (code_map[i].type == type && code_map[i].oscode == oscode)
/*TODO*///			return i;
/*TODO*///
/*TODO*///	/* Search in the osd table for a standard code */
/*TODO*///	switch (type)
/*TODO*///	{
/*TODO*///		case CODE_TYPE_KEYBOARD :
/*TODO*///			keyinfo = internal_oscode_find_keyboard(oscode);
/*TODO*///			if (keyinfo && keyinfo->standardcode != CODE_OTHER)
/*TODO*///				return keyinfo->standardcode;
/*TODO*///			break;
/*TODO*///		case CODE_TYPE_JOYSTICK :
/*TODO*///			joyinfo = internal_oscode_find_joystick(oscode);
/*TODO*///			if (joyinfo && joyinfo->standardcode != CODE_OTHER)
/*TODO*///				return joyinfo->standardcode;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* oscode not found */
/*TODO*///	return CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*////* Add a new oscode in the table */
/*TODO*///static int internal_oscode_add(unsigned oscode, unsigned type)
/*TODO*///{
/*TODO*///	struct code_info* new_code_map;
/*TODO*///	new_code_map = realloc( code_map, (code_mac+1) * sizeof(struct code_info) );
/*TODO*///	if (new_code_map)
/*TODO*///	{
/*TODO*///		code_map = new_code_map;
/*TODO*///		code_map[code_mac].memory = 0;
/*TODO*///		code_map[code_mac].oscode = oscode;
/*TODO*///		code_map[code_mac].type = type;
/*TODO*///		return code_mac++;
/*TODO*///	} else {
/*TODO*///		return CODE_NONE;
/*TODO*///        }
/*TODO*///}
/*TODO*///
/*TODO*////* Find the osd record of a standard code */
/*TODO*///INLINE const struct KeyboardInfo* internal_code_find_keyboard(InputCode code)
/*TODO*///{
/*TODO*///	const struct KeyboardInfo *keyinfo;
/*TODO*///	keyinfo = osd_get_key_list();
/*TODO*///
/*TODO*///	assert( code < code_mac );
/*TODO*///
/*TODO*///	if (code < __code_max)
/*TODO*///	{
/*TODO*///		while (keyinfo->name)
/*TODO*///		{
/*TODO*///			if (keyinfo->standardcode == code)
/*TODO*///				return keyinfo;
/*TODO*///			++keyinfo;
/*TODO*///		}
/*TODO*///	} else {
/*TODO*///		while (keyinfo->name)
/*TODO*///		{
/*TODO*///			if (keyinfo->standardcode == CODE_OTHER && keyinfo->code == code_map[code].oscode)
/*TODO*///				return keyinfo;
/*TODO*///	      		++keyinfo;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///INLINE const struct JoystickInfo* internal_code_find_joystick(InputCode code)
/*TODO*///{
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///	joyinfo = osd_get_joy_list();
/*TODO*///
/*TODO*///	assert( code < code_mac );
/*TODO*///
/*TODO*///	if (code < __code_max)
/*TODO*///	{
/*TODO*///		while (joyinfo->name)
/*TODO*///		{
/*TODO*///			if (joyinfo->standardcode == code)
/*TODO*///				return joyinfo;
/*TODO*///			++joyinfo;
/*TODO*///		}
/*TODO*///	} else {
/*TODO*///		while (joyinfo->name)
/*TODO*///		{
/*TODO*///			if (joyinfo->standardcode == CODE_OTHER && joyinfo->code == code_map[code].oscode)
/*TODO*///				return joyinfo;
/*TODO*///			++joyinfo;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Check if a code is pressed */
/*TODO*///static int internal_code_pressed(InputCode code)
/*TODO*///{
/*TODO*///	const struct KeyboardInfo *keyinfo;
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///
/*TODO*///	assert( code < code_mac );
/*TODO*///
/*TODO*///	if (code < __code_max)
/*TODO*///	{
/*TODO*///		switch (code_map[code].type)
/*TODO*///		{
/*TODO*///			case CODE_TYPE_KEYBOARD :
/*TODO*///				keyinfo = internal_code_find_keyboard(code);
/*TODO*///				if (keyinfo)
/*TODO*///					return osd_is_key_pressed(keyinfo->code);
/*TODO*///				break;
/*TODO*///			case CODE_TYPE_JOYSTICK :
/*TODO*///				joyinfo = internal_code_find_joystick(code);
/*TODO*///				if (joyinfo)
/*TODO*///					return osd_is_joy_pressed(joyinfo->code);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} else {
/*TODO*///		switch (code_map[code].type)
/*TODO*///		{
/*TODO*///			case CODE_TYPE_KEYBOARD :
/*TODO*///				return osd_is_key_pressed(code_map[code].oscode);
/*TODO*///			case CODE_TYPE_JOYSTICK :
/*TODO*///				return osd_is_joy_pressed(code_map[code].oscode);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Return the name of the code */
/*TODO*///static const char* internal_code_name(InputCode code)
/*TODO*///{
/*TODO*///	const struct KeyboardInfo *keyinfo;
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///
/*TODO*///	assert( code < code_mac );
/*TODO*///
/*TODO*///	switch (code_map[code].type)
/*TODO*///	{
/*TODO*///		case CODE_TYPE_KEYBOARD :
/*TODO*///			keyinfo = internal_code_find_keyboard(code);
/*TODO*///			if (keyinfo)
/*TODO*///				return keyinfo->name;
/*TODO*///			break;
/*TODO*///		case CODE_TYPE_JOYSTICK :
/*TODO*///			joyinfo = internal_code_find_joystick(code);
/*TODO*///			if (joyinfo)
/*TODO*///				return joyinfo->name;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	return "n/a";
/*TODO*///}
/*TODO*///
/*TODO*////* Update the code table */
/*TODO*///static void internal_code_update(void)
/*TODO*///{
/*TODO*///	const struct KeyboardInfo *keyinfo;
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///
/*TODO*///	/* add only oscode because all standard codes are already present */
/*TODO*///
/*TODO*///	keyinfo = osd_get_key_list();
/*TODO*///	while (keyinfo->name)
/*TODO*///	{
/*TODO*///		if (keyinfo->standardcode == CODE_OTHER)
/*TODO*///			if (internal_oscode_find(keyinfo->code,CODE_TYPE_KEYBOARD) == CODE_NONE)
/*TODO*///				internal_oscode_add(keyinfo->code,CODE_TYPE_KEYBOARD);
/*TODO*///		++keyinfo;
/*TODO*///	}
/*TODO*///
/*TODO*///	joyinfo = osd_get_joy_list();
/*TODO*///	while (joyinfo->name)
/*TODO*///	{
/*TODO*///		if (joyinfo->standardcode == CODE_OTHER)
/*TODO*///                        if (internal_oscode_find(joyinfo->code,CODE_TYPE_JOYSTICK)==CODE_NONE)
/*TODO*///				internal_oscode_add(joyinfo->code,CODE_TYPE_JOYSTICK);
/*TODO*///		++joyinfo;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* Delete the code table */
/*TODO*///void code_close(void)
/*TODO*///{
/*TODO*///#if 0
/*TODO*///	int i;
/*TODO*///	logerror("List of OS dependant input codes:\n");
/*TODO*///	for(i=__code_max;i<code_mac;++i)
/*TODO*///		logerror("\tcode %d, oscode %d, %s, %s\n",i,code_map[i].oscode,code_map[i].type == CODE_TYPE_KEYBOARD ? "keyboard" : "joystick", internal_code_name(i));
/*TODO*///#endif
/*TODO*///
/*TODO*///	code_mac = 0;
/*TODO*///	free(code_map);
/*TODO*///	code_map = 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Save support */
/*TODO*///
/*TODO*////* Flags used for saving codes to file */
/*TODO*///#define SAVECODE_FLAGS_TYPE_STANDARD 0x10000000 /* code */
/*TODO*///#define SAVECODE_FLAGS_TYPE_KEYBOARD 0x20000000 /* keyboard oscode */
/*TODO*///#define SAVECODE_FLAGS_TYPE_JOYSTICK 0x30000000 /* joystick oscode */
/*TODO*///#define SAVECODE_FLAGS_TYPE_MASK     0xF0000000
/*TODO*///
/*TODO*////* Convert one key oscode to one standard code */
/*TODO*///InputCode keyoscode_to_code(unsigned oscode)
/*TODO*///{
/*TODO*///	InputCode code;
/*TODO*///
/*TODO*///	code = internal_oscode_find(oscode,CODE_TYPE_KEYBOARD);
/*TODO*///
/*TODO*///	/* insert if missing */
/*TODO*///	if (code == CODE_NONE)
/*TODO*///		code = internal_oscode_add(oscode,CODE_TYPE_KEYBOARD);
/*TODO*///
/*TODO*///	return code;
/*TODO*///}
/*TODO*///
/*TODO*////* Convert one joystick oscode to one code */
/*TODO*///InputCode joyoscode_to_code(unsigned oscode)
/*TODO*///{
/*TODO*///	InputCode code = internal_oscode_find(oscode,CODE_TYPE_JOYSTICK);
/*TODO*///
/*TODO*///	/* insert if missing */
/*TODO*///	if (code == CODE_NONE)
/*TODO*///		code = internal_oscode_add(oscode,CODE_TYPE_JOYSTICK);
/*TODO*///
/*TODO*///	return code;
/*TODO*///}
/*TODO*///
/*TODO*////* Convert one saved code to one code */
/*TODO*///InputCode savecode_to_code(unsigned savecode)
/*TODO*///{
/*TODO*///	unsigned type = savecode & SAVECODE_FLAGS_TYPE_MASK;
/*TODO*///	InputCode code = savecode & ~SAVECODE_FLAGS_TYPE_MASK;
/*TODO*///
/*TODO*///	switch (type)
/*TODO*///	{
/*TODO*///		case SAVECODE_FLAGS_TYPE_STANDARD :
/*TODO*///			return code;
/*TODO*///		case SAVECODE_FLAGS_TYPE_KEYBOARD :
/*TODO*///			return keyoscode_to_code(code);
/*TODO*///		case SAVECODE_FLAGS_TYPE_JOYSTICK :
/*TODO*///			return joyoscode_to_code(code);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* never happen */
/*TODO*///	assert(0);
/*TODO*///	return CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*////* Convert one code to one saved code */
/*TODO*///unsigned code_to_savecode(InputCode code)
/*TODO*///{
/*TODO*///	if (code < __code_max || code >= code_mac)
/*TODO*///               	/* if greather than code_mac is a special CODE like CODE_OR */
/*TODO*///		return code | SAVECODE_FLAGS_TYPE_STANDARD;
/*TODO*///
/*TODO*///	switch (code_map[code].type)
/*TODO*///	{
/*TODO*///		case CODE_TYPE_KEYBOARD : return code_map[code].oscode | SAVECODE_FLAGS_TYPE_KEYBOARD;
/*TODO*///		case CODE_TYPE_JOYSTICK : return code_map[code].oscode | SAVECODE_FLAGS_TYPE_JOYSTICK;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* never happen */
/*TODO*///	assert(0);
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Interface */
/*TODO*///
/*TODO*///const char *code_name(InputCode code)
/*TODO*///{
/*TODO*///	if (code < code_mac)
/*TODO*///		return internal_code_name(code);
/*TODO*///
/*TODO*///	switch (code)
/*TODO*///	{
/*TODO*///		case CODE_NONE : return "None";
/*TODO*///		case CODE_NOT : return "not";
/*TODO*///		case CODE_OR : return "or";
/*TODO*///	}
/*TODO*///
/*TODO*///	return "n/a";
/*TODO*///}
/*TODO*///
/*TODO*///int code_pressed(InputCode code)
/*TODO*///{
/*TODO*///	int pressed;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	pressed = internal_code_pressed(code);
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return pressed;
/*TODO*///}
/*TODO*///
/*TODO*///int code_pressed_memory(InputCode code)
/*TODO*///{
/*TODO*///	int pressed;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	pressed = internal_code_pressed(code);
/*TODO*///
/*TODO*///	if (pressed)
/*TODO*///	{
/*TODO*///		if (code_map[code].memory == 0)
/*TODO*///			code_map[code].memory = 1;
/*TODO*///		else
/*TODO*///			pressed = 0;
/*TODO*///	} else
/*TODO*///		code_map[code].memory = 0;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return pressed;
/*TODO*///}
/*TODO*///
/*TODO*////* Report the pressure only if isn't already signaled with one of the */
/*TODO*////* functions code_memory and code_memory_repeat */
/*TODO*///static int code_pressed_not_memorized(InputCode code)
/*TODO*///{
/*TODO*///	int pressed;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	pressed = internal_code_pressed(code);
/*TODO*///
/*TODO*///	if (pressed)
/*TODO*///	{
/*TODO*///		if (code_map[code].memory != 0)
/*TODO*///			pressed = 0;
/*TODO*///	} else
/*TODO*///		code_map[code].memory = 0;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return pressed;
/*TODO*///}
/*TODO*///
/*TODO*///int code_pressed_memory_repeat(InputCode code, int speed)
/*TODO*///{
/*TODO*///	static int counter;
/*TODO*///	static int keydelay;
/*TODO*///	int pressed;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	pressed = internal_code_pressed(code);
/*TODO*///
/*TODO*///	if (pressed)
/*TODO*///	{
/*TODO*///		if (code_map[code].memory == 0)
/*TODO*///		{
/*TODO*///			code_map[code].memory = 1;
/*TODO*///			keydelay = 3;
/*TODO*///			counter = 0;
/*TODO*///		}
/*TODO*///		else if (++counter > keydelay * speed * Machine->drv->frames_per_second / 60)
/*TODO*///		{
/*TODO*///			keydelay = 1;
/*TODO*///			counter = 0;
/*TODO*///		} else
/*TODO*///			pressed = 0;
/*TODO*///	} else
/*TODO*///		code_map[code].memory = 0;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return pressed;
/*TODO*///}
/*TODO*///
/*TODO*///InputCode code_read_async(void)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	/* update the table */
/*TODO*///	internal_code_update();
/*TODO*///
/*TODO*///	for(i=0;i<code_mac;++i)
/*TODO*///		if (code_pressed_memory(i))
/*TODO*///			return i;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*////* returns the numerical value of a typed hex digit, or -1 if none */
/*TODO*///INT8 code_read_hex_async(void)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	/* update the table */
/*TODO*///	internal_code_update();
/*TODO*///
/*TODO*///	for(i=0;i<code_mac;++i)
/*TODO*///		if (code_pressed_memory(i))
/*TODO*///		{
/*TODO*///			if ((i >= KEYCODE_A) && (i <= KEYCODE_F))
/*TODO*///				return i - KEYCODE_A + 10;
/*TODO*///			else if ((i >= KEYCODE_0) && (i <= KEYCODE_9))
/*TODO*///				return i - KEYCODE_0;
/*TODO*///			else
/*TODO*///				return -1;
/*TODO*///		}
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return -1;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Sequences */
/*TODO*///
/*TODO*///void seq_set_0(InputSeq* a)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	for(j=0;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_set_1(InputSeq* a, InputCode code)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	(*a)[0] = code;
/*TODO*///	for(j=1;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_set_2(InputSeq* a, InputCode code1, InputCode code2)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	(*a)[0] = code1;
/*TODO*///	(*a)[1] = code2;
/*TODO*///	for(j=2;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_set_3(InputSeq* a, InputCode code1, InputCode code2, InputCode code3)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	(*a)[0] = code1;
/*TODO*///	(*a)[1] = code2;
/*TODO*///	(*a)[2] = code3;
/*TODO*///	for(j=3;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_set_4(InputSeq* a, InputCode code1, InputCode code2, InputCode code3, InputCode code4)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	(*a)[0] = code1;
/*TODO*///	(*a)[1] = code2;
/*TODO*///	(*a)[2] = code3;
/*TODO*///	(*a)[3] = code4;
/*TODO*///	for(j=4;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_set_5(InputSeq* a, InputCode code1, InputCode code2, InputCode code3, InputCode code4, InputCode code5)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	(*a)[0] = code1;
/*TODO*///	(*a)[1] = code2;
/*TODO*///	(*a)[2] = code3;
/*TODO*///	(*a)[3] = code4;
/*TODO*///	(*a)[4] = code5;
/*TODO*///	for(j=5;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = CODE_NONE;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_copy(InputSeq* a, InputSeq* b)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	for(j=0;j<SEQ_MAX;++j)
/*TODO*///		(*a)[j] = (*b)[j];
/*TODO*///}
/*TODO*///
/*TODO*///int seq_cmp(InputSeq* a, InputSeq* b)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	for(j=0;j<SEQ_MAX;++j)
/*TODO*///		if ((*a)[j] != (*b)[j])
/*TODO*///			return -1;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void seq_name(InputSeq* code, char* buffer, unsigned max)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	char* dest = buffer;
/*TODO*///	for(j=0;j<SEQ_MAX;++j)
/*TODO*///	{
/*TODO*///		const char* name;
/*TODO*///
/*TODO*///		/* this reduces those pesky "blah or n/a" constructs */
/*TODO*///		if (((*code)[j]==CODE_OR) && (j+1>=SEQ_MAX || !strcmp(code_name((*code)[j+1]), "n/a")))
/*TODO*///		{
/*TODO*///			++j;
/*TODO*///			continue;
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((*code)[j]==CODE_NONE)
/*TODO*///			break;
/*TODO*///
/*TODO*///		if (j && 1 + 1 <= max)
/*TODO*///		{
/*TODO*///			*dest = ' ';
/*TODO*///			dest += 1;
/*TODO*///			max -= 1;
/*TODO*///		}
/*TODO*///
/*TODO*///		name = code_name((*code)[j]);
/*TODO*///		if (!name)
/*TODO*///			break;
/*TODO*///
/*TODO*///		if (strlen(name) + 1 <= max)
/*TODO*///		{
/*TODO*///			strcpy(dest,name);
/*TODO*///			dest += strlen(name);
/*TODO*///			max -= strlen(name);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (dest == buffer && 4 + 1 <= max)
/*TODO*///		strcpy(dest,"None");
/*TODO*///	else
/*TODO*///		*dest = 0;
/*TODO*///}
/*TODO*///
/*TODO*///int seq_pressed(InputSeq* code)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	int res = 1;
/*TODO*///	int invert = 0;
/*TODO*///	int count = 0;
/*TODO*///
/*TODO*///	for(j=0;j<SEQ_MAX;++j)
/*TODO*///	{
/*TODO*///		switch ((*code)[j])
/*TODO*///		{
/*TODO*///			case CODE_NONE :
/*TODO*///				return res && count;
/*TODO*///			case CODE_OR :
/*TODO*///				if (res && count)
/*TODO*///					return 1;
/*TODO*///				res = 1;
/*TODO*///				count = 0;
/*TODO*///				break;
/*TODO*///			case CODE_NOT :
/*TODO*///				invert = !invert;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				if (res)
/*TODO*///				{
/*TODO*///					int pressed = code_pressed_not_memorized((*code)[j]);
/*TODO*///					if ((pressed != 0) == invert)
/*TODO*///						res = 0;
/*TODO*///				}
/*TODO*///				invert = 0;
/*TODO*///				++count;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return res && count;
/*TODO*///}
/*TODO*///
/*TODO*////* Static informations used in key/joy recording */
/*TODO*///static InputCode record_seq[SEQ_MAX]; /* buffer for key recording */
/*TODO*///static int record_count; /* number of key/joy press recorded */
/*TODO*///static clock_t record_last; /* time of last key/joy press */
/*TODO*///
/*TODO*///#define RECORD_TIME (CLOCKS_PER_SEC*2/3) /* max time between key press */
/*TODO*///
/*TODO*////* Start a sequence recording */
/*TODO*///void seq_read_async_start(void)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	record_count = 0;
/*TODO*///	record_last = clock();
/*TODO*///
/*TODO*///	/* reset code memory, otherwise this memory may interferes with the input memory */
/*TODO*///	for(i=0;i<code_mac;++i)
/*TODO*///		code_map[i].memory = 1;
/*TODO*///}
/*TODO*///
/*TODO*////* Check that almost one key/joy must be pressed */
/*TODO*///static int seq_valid(InputSeq* seq)
/*TODO*///{
/*TODO*///	int j;
/*TODO*///	int positive = 0;
/*TODO*///	int pred_not = 0;
/*TODO*///	int operand = 0;
/*TODO*///	for(j=0;j<SEQ_MAX;++j)
/*TODO*///	{
/*TODO*///		switch ((*seq)[j])
/*TODO*///		{
/*TODO*///			case CODE_NONE :
/*TODO*///				break;
/*TODO*///			case CODE_OR :
/*TODO*///				if (!operand || !positive)
/*TODO*///					return 0;
/*TODO*///				pred_not = 0;
/*TODO*///				positive = 0;
/*TODO*///				operand = 0;
/*TODO*///				break;
/*TODO*///			case CODE_NOT :
/*TODO*///				if (pred_not)
/*TODO*///					return 0;
/*TODO*///				pred_not = !pred_not;
/*TODO*///				operand = 0;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				if (!pred_not)
/*TODO*///					positive = 1;
/*TODO*///				pred_not = 0;
/*TODO*///				operand = 1;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return positive && operand;
/*TODO*///}
/*TODO*///
/*TODO*////* Record a key/joy sequence
/*TODO*///	return <0 if more input is needed
/*TODO*///	return ==0 if sequence succesfully recorded
/*TODO*///	return >0 if aborted
/*TODO*///*/
/*TODO*///int seq_read_async(InputSeq* seq, int first)
/*TODO*///{
/*TODO*///	InputCode newkey;
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (record_count == SEQ_MAX
/*TODO*///		|| (record_count > 0 && clock() > record_last + RECORD_TIME))	{
/*TODO*///		int k = 0;
/*TODO*///		if (!first)
/*TODO*///		{
/*TODO*///			/* search the first space free */
/*TODO*///			while (k < SEQ_MAX && (*seq)[k] != CODE_NONE)
/*TODO*///				++k;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* if no space restart */
/*TODO*///		if (k + record_count + (k!=0) > SEQ_MAX)
/*TODO*///			k = 0;
/*TODO*///
/*TODO*///		/* insert */
/*TODO*///		if (k + record_count + (k!=0) <= SEQ_MAX)
/*TODO*///		{
/*TODO*///			int j;
/*TODO*///			if (k!=0)
/*TODO*///				(*seq)[k++] = CODE_OR;
/*TODO*///			for(j=0;j<record_count;++j,++k)
/*TODO*///				(*seq)[k] = record_seq[j];
/*TODO*///		}
/*TODO*///		/* fill to end */
/*TODO*///		while (k < SEQ_MAX)
/*TODO*///		{
/*TODO*///			(*seq)[k] = CODE_NONE;
/*TODO*///			++k;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!seq_valid(seq))
/*TODO*///			seq_set_1(seq,CODE_NONE);
/*TODO*///
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	newkey = code_read_async();
/*TODO*///
/*TODO*///	if (newkey != CODE_NONE)
/*TODO*///	{
/*TODO*///		/* if code is duplicate negate the code */
/*TODO*///		if (record_count && newkey == record_seq[record_count-1])
/*TODO*///			record_seq[record_count-1] = CODE_NOT;
/*TODO*///
/*TODO*///		record_seq[record_count++] = newkey;
/*TODO*///		record_last = clock();
/*TODO*///	}
/*TODO*///
/*TODO*///	return -1;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* input ui */
/*TODO*///
/*TODO*////* Static buffer for memory input */
/*TODO*///struct ui_info {
/*TODO*///	int memory;
/*TODO*///};
/*TODO*///
/*TODO*///static struct ui_info ui_map[__ipt_max];
/*TODO*///
/*TODO*///int input_ui_pressed(int code)
/*TODO*///{
/*TODO*///	int pressed;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	pressed = seq_pressed(input_port_type_seq(code));
/*TODO*///
/*TODO*///	if (pressed)
/*TODO*///	{
/*TODO*///		if (ui_map[code].memory == 0)
/*TODO*///		{
/*TODO*///			ui_map[code].memory = 1;
/*TODO*///		} else
/*TODO*///			pressed = 0;
/*TODO*///	} else
/*TODO*///		ui_map[code].memory = 0;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return pressed;
/*TODO*///}
/*TODO*///
/*TODO*///int input_ui_pressed_repeat(int code,int speed)
/*TODO*///{
/*TODO*///	static int counter,inputdelay;
/*TODO*///	int pressed;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	pressed = seq_pressed(input_port_type_seq(code));
/*TODO*///
/*TODO*///	if (pressed)
/*TODO*///	{
/*TODO*///		if (ui_map[code].memory == 0)
/*TODO*///		{
/*TODO*///			ui_map[code].memory = 1;
/*TODO*///			inputdelay = 3;
/*TODO*///			counter = 0;
/*TODO*///		}
/*TODO*///		else if (++counter > inputdelay * speed * Machine->drv->frames_per_second / 60)
/*TODO*///		{
/*TODO*///			inputdelay = 1;
/*TODO*///			counter = 0;
/*TODO*///		} else
/*TODO*///			pressed = 0;
/*TODO*///	} else
/*TODO*///		ui_map[code].memory = 0;
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	return pressed;
/*TODO*///}
/*TODO*///
/*TODO*///int is_joystick_axis_code(unsigned code)
/*TODO*///{
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///
/*TODO*///	assert( code < code_mac );
/*TODO*///
/*TODO*///	if (code_map[code].type == CODE_TYPE_JOYSTICK)
/*TODO*///	{
/*TODO*///		if (code < __code_max)
/*TODO*///		{
/*TODO*///			joyinfo = internal_code_find_joystick(code);
/*TODO*///			if (joyinfo)
/*TODO*///				return osd_is_joystick_axis_code(joyinfo->code);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			return osd_is_joystick_axis_code(code_map[code].oscode);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int return_os_joycode(InputCode code)
/*TODO*///{
/*TODO*///	const struct JoystickInfo *joyinfo;
/*TODO*///
/*TODO*///	assert( code < code_mac );
/*TODO*///
/*TODO*///	if (code < __code_max)
/*TODO*///	{
/*TODO*///		if (code_map[code].type == CODE_TYPE_JOYSTICK)
/*TODO*///		{
/*TODO*///			joyinfo = internal_code_find_joystick(code);
/*TODO*///			if (joyinfo)
/*TODO*///				return joyinfo->code;
/*TODO*///		}
/*TODO*///	} else {
/*TODO*///		if (code_map[code].type == CODE_TYPE_JOYSTICK)
/*TODO*///		{
/*TODO*///			return code_map[code].oscode;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///    
}
