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
    public static final int FILETYPE_RAW            = 0;
    public static final int FILETYPE_ROM            = 1;
    public static final int FILETYPE_IMAGE          = 2;
    public static final int FILETYPE_IMAGE_DIFF     = 3;
    public static final int FILETYPE_SAMPLE         = 4;
    public static final int FILETYPE_ARTWORK        = 5;
    public static final int FILETYPE_NVRAM          = 6;
    public static final int FILETYPE_HIGHSCORE      = 7;
    public static final int FILETYPE_HIGHSCORE_DB   = 8;
    public static final int FILETYPE_CONFIG         = 9;
    public static final int FILETYPE_INPUTLOG       = 10;
    public static final int FILETYPE_STATE          = 11;
    public static final int FILETYPE_MEMCARD        = 12;
    public static final int FILETYPE_SCREENSHOT     = 13;
    public static final int FILETYPE_HISTORY        = 14;
    public static final int FILETYPE_CHEAT          = 15;
    public static final int FILETYPE_LANGUAGE       = 16;
    public static final int FILETYPE_CTRLR          = 17;
    public static final int FILETYPE_INI            = 18;
/*TODO*///#ifdef MESS
    public static final int FILETYPE_CRC            = 19;
/*TODO*///#endif
    public static final int FILETYPE_end            = 20; /* dummy last entry */
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
