/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

public class fileioH {
/*TODO*///
/*TODO*////* file types */
/*TODO*///enum
/*TODO*///{
/*TODO*///	FILETYPE_RAW = 0,
/*TODO*///	FILETYPE_ROM,
/*TODO*///	FILETYPE_IMAGE,
/*TODO*///	FILETYPE_IMAGE_DIFF,
/*TODO*///	FILETYPE_SAMPLE,
/*TODO*///	FILETYPE_ARTWORK,
/*TODO*///	FILETYPE_NVRAM,
/*TODO*///	FILETYPE_HIGHSCORE,
/*TODO*///	FILETYPE_HIGHSCORE_DB,
/*TODO*///	FILETYPE_CONFIG,
/*TODO*///	FILETYPE_INPUTLOG,
/*TODO*///	FILETYPE_STATE,
/*TODO*///	FILETYPE_MEMCARD,
/*TODO*///	FILETYPE_SCREENSHOT,
/*TODO*///	FILETYPE_HISTORY,
/*TODO*///	FILETYPE_CHEAT,
/*TODO*///	FILETYPE_LANGUAGE,
/*TODO*///	FILETYPE_CTRLR,
/*TODO*///	FILETYPE_INI,
/*TODO*///#ifdef MESS
/*TODO*///	FILETYPE_CRC,
/*TODO*///#endif
/*TODO*///	FILETYPE_end /* dummy last entry */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*////* gamename holds the driver name, filename is only used for ROMs and    */
/*TODO*////* samples. If 'write' is not 0, the file is opened for write. Otherwise */
/*TODO*////* it is opened for read. */
/*TODO*///
/*TODO*///typedef struct _mame_file mame_file;
/*TODO*///
/*TODO*///int mame_faccess(const char *filename, int filetype);
/*TODO*///mame_file *mame_fopen(const char *gamename, const char *filename, int filetype, int openforwrite);
/*TODO*///mame_file *mame_fopen_rom(const char *gamename, const char *filename, const char* exphash);
/*TODO*///UINT32 mame_fread(mame_file *file, void *buffer, UINT32 length);
/*TODO*///UINT32 mame_fwrite(mame_file *file, const void *buffer, UINT32 length);
/*TODO*///UINT32 mame_fread_swap(mame_file *file, void *buffer, UINT32 length);
/*TODO*///UINT32 mame_fwrite_swap(mame_file *file, const void *buffer, UINT32 length);
/*TODO*///#ifdef LSB_FIRST
/*TODO*///#define mame_fread_msbfirst mame_fread_swap
/*TODO*///#define mame_fwrite_msbfirst mame_fwrite_swap
/*TODO*///#define mame_fread_lsbfirst mame_fread
/*TODO*///#define mame_fwrite_lsbfirst mame_fwrite
/*TODO*///#else
/*TODO*///#define mame_fread_msbfirst mame_fread
/*TODO*///#define mame_fwrite_msbfirst mame_fwrite
/*TODO*///#define mame_fread_lsbfirst mame_fread_swap
/*TODO*///#define mame_fwrite_lsbfirst mame_fwrite_swap
/*TODO*///#endif
/*TODO*///int mame_fseek(mame_file *file, INT64 offset, int whence);
/*TODO*///void mame_fclose(mame_file *file);
/*TODO*///int mame_fchecksum(const char *gamename, const char *filename, unsigned int *length, char* hash);
/*TODO*///UINT64 mame_fsize(mame_file *file);
/*TODO*///const char *mame_fhash(mame_file *file);
/*TODO*///int mame_fgetc(mame_file *file);
/*TODO*///int mame_ungetc(int c, mame_file *file);
/*TODO*///char *mame_fgets(char *s, int n, mame_file *file);
/*TODO*///int mame_feof(mame_file *file);
/*TODO*///UINT64 mame_ftell(mame_file *file);
/*TODO*///
/*TODO*///int mame_fputs(mame_file *f, const char *s);
/*TODO*///int mame_vfprintf(mame_file *f, const char *fmt, va_list va);
/*TODO*///
/*TODO*///#ifdef __GNUC__
/*TODO*///int CLIB_DECL mame_fprintf(mame_file *f, const char *fmt, ...)
/*TODO*///      __attribute__ ((format (printf, 2, 3)));
/*TODO*///#else
/*TODO*///int CLIB_DECL mame_fprintf(mame_file *f, const char *fmt, ...);
/*TODO*///#endif /* __GNUC__ */
/*TODO*///
/*TODO*///#ifdef __cplusplus
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
