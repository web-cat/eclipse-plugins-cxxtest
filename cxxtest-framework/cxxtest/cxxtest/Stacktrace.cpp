extern "C" {
    void __cyg_profile_func_enter( void *, void * ) _CXXTEST_NO_INSTR;
    void __cyg_profile_func_exit( void *, void * ) _CXXTEST_NO_INSTR;
}

#ifndef CXXTEST_TRACE_STACK

extern "C" {
    void __cyg_profile_func_enter( void *, void * ) {}
    void __cyg_profile_func_exit( void *, void * ) {}
}

#else

#ifndef CXXTEST_STACK_TRACE_INITIAL_PREFIX
#   define CXXTEST_STACK_TRACE_INITIAL_PREFIX "  in: "
#endif
#ifndef CXXTEST_STACK_TRACE_INITIAL_SUFFIX
#   define CXXTEST_STACK_TRACE_INITIAL_SUFFIX "\n"
#endif
#ifndef CXXTEST_STACK_TRACE_OTHER_PREFIX
#   define CXXTEST_STACK_TRACE_OTHER_PREFIX "    called from: "
#endif
#ifndef CXXTEST_STACK_TRACE_OTHER_SUFFIX
#   define CXXTEST_STACK_TRACE_OTHER_SUFFIX "\n"
#endif
#ifndef CXXTEST_STACK_TRACE_ELLIDED_MESSAGE
#   define CXXTEST_STACK_TRACE_ELLIDED_MESSAGE "    ...\n"
#endif
#ifndef CXXTEST_STACK_TRACE_FILELINE_PREFIX
#   define CXXTEST_STACK_TRACE_FILELINE_PREFIX "("
#endif
#ifndef CXXTEST_STACK_TRACE_FILELINE_SUFFIX
#   define CXXTEST_STACK_TRACE_FILELINE_SUFFIX ")"
#endif
#ifndef CXXTEST_STACK_TRACE_MAX_FRAMES_TO_DUMP
#   define CXXTEST_STACK_TRACE_MAX_FRAMES_TO_DUMP 12
#endif

#include <string.h>
#include <bfd.h>

extern "C" {
    char* cplus_demangle( const char*, int );
}

#define _CXXTEST_MAXIMUM_STACK_TRACE_DEPTH 4096

namespace CxxTest {

struct StackElem
{
    void* func;
    void* callsite;
};

struct SymbolInfo
{
    bfd_vma      pc;
    const char*  fileName;
    const char*  funcName;
    unsigned int line;
    bool         found;
};

static StackElem __cxxtest_stack[_CXXTEST_MAXIMUM_STACK_TRACE_DEPTH];
unsigned int __cxxtest_stackTop = 0;
static bool __cxxtest_generatingTrace  = false;
static bool __cxxtest_traceNeedsInit   = true;
bool __cxxtest_handlingOverflow = false;

static bfd*          abfd     = 0;
static asymbol**     syms     = 0;
static unsigned long numSym   = 0;
static asymbol**     symTable = 0;


// Function prototypes to enforce no-instrument attributes
static std::string demangle( const char* ) _CXXTEST_NO_INSTR;
static void        initSymbolTranslator() _CXXTEST_NO_INSTR;
static int         verifyLoadedSymbolInformation() _CXXTEST_NO_INSTR;
static bool printStackTraceEntry(
    std::ostream&, void*, const char*, const char* ) _CXXTEST_NO_INSTR;
std::string getStackTrace(
    unsigned int top           = __cxxtest_stackTop,
    StackElem*   stackBase     = __cxxtest_stack,
    const char*  initialPrefix = CXXTEST_STACK_TRACE_INITIAL_PREFIX,
    const char*  otherPrefix   = CXXTEST_STACK_TRACE_OTHER_PREFIX
    ) _CXXTEST_NO_INSTR;
static void findBfdAddress( bfd*, asection*, void* ) _CXXTEST_NO_INSTR;
static void lookUpSymbol( void*, SymbolInfo& ) _CXXTEST_NO_INSTR;
static bool shouldPrintEntry( const std::string& ) _CXXTEST_NO_INSTR;
unsigned int stackTraceSize( unsigned int traceDepth ) _CXXTEST_NO_INSTR;
void saveStackTraceWindow( StackElem* dest, unsigned int traceDepth )
  _CXXTEST_NO_INSTR;


// -------------------------------------------------------------------------
std::string getStackTrace(
    unsigned int top,
    StackElem* stackBase,
    const char* initialPrefix,
    const char* otherPrefix )
{
    __cxxtest_generatingTrace = true;
    if ( __cxxtest_traceNeedsInit )
    {
        initSymbolTranslator();
    }
    std::ostringstream result;
    while ( top && !stackBase[top - 1].func ) top--;
    if ( top )
    {
        // result << " (" << top << ") ";
        printStackTraceEntry(
            result,
            stackBase[top - 1].func,
            initialPrefix,
            CXXTEST_STACK_TRACE_INITIAL_SUFFIX );
        unsigned int printedCount = 0;
        for ( unsigned int i = top; i > 0; )
        {
            i--;
            // result << " (" << i << ") ";
            if ( printStackTraceEntry(
                result,
                stackBase[i].callsite,
                otherPrefix,
                CXXTEST_STACK_TRACE_OTHER_SUFFIX ) ) printedCount++;
            if ( printedCount > CXXTEST_STACK_TRACE_MAX_FRAMES_TO_DUMP )
            {
                result << CXXTEST_STACK_TRACE_ELLIDED_MESSAGE;
		break;
            }
        }
    }
    __cxxtest_generatingTrace = false;
    return result.str();
}

// -------------------------------------------------------------------------
#ifndef CXXTEST_STACK_TRACE_ESCAPE_AS_XML
#   define escape
#else

static std::string escape(const char* str)
{
	std::string escStr;
	escStr.reserve(512);

	while(*str != 0)
	{
		char ch = *str++;
		switch(ch)
		{
			case '"':  escStr += "&quot;"; break;
			case '\'': escStr += "&apos;"; break;
			case '<':  escStr += "&lt;"; break;
			case '>':  escStr += "&gt;"; break;
			case '&':  escStr += "&amp;"; break;
			default:   escStr += ch; break;
		}
	}
	
	return escStr;
}

static std::string escape(const std::string& str)
{
	return escape(str.c_str());
}

#endif // CXXTEST_STACK_TRACE_ESCAPE_AS_XML
		
// -------------------------------------------------------------------------
static bool printStackTraceEntry(
    std::ostream& dest,
    void*         location,
    const char*   prefix,
    const char*   suffix
    )
{
    if ( !location ) return false;
    SymbolInfo info;
    lookUpSymbol( location, info );
    if ( info.found )
    {
        std::string demangled = demangle( info.funcName );
        if ( shouldPrintEntry( demangled ) )
        {
            if ( demangled == "__assert" )
            {
                // translate asserts
                demangled = "assert(bool) macro";
                info.fileName = "";
            }
            else if ( demangled.find( "operator new" ) == 0
                      || demangled.find( "operator delete" ) == 0 )
            {
                info.fileName = "";
            }
            dest << prefix << escape(demangled);
            if ( info.line )
            {
                // dest << "[" << info.funcName << "] ";
                const char* dir = "Debug/../";
                const char* fname = strstr( info.fileName, dir );
                if ( fname )
                {
                    fname += strlen( dir );
                }
                else
                {
                    fname = info.fileName;
                    const char* dir2 = "cxxtest/cxxtest/";
                    fname = strstr( fname, dir2 );
                    if ( fname )
                    {
                        fname += strlen( dir2 ) / 2;
                    }
                    else
                    {
                        fname = info.fileName;
                    }
                }
                if ( fname && fname[0] )
                {
#ifdef CXXTEST_STACK_TRACE_NO_ESCAPE_FILELINE_AFFIXES
                    dest << CXXTEST_STACK_TRACE_FILELINE_PREFIX << escape(fname);
#else
                    dest << escape(CXXTEST_STACK_TRACE_FILELINE_PREFIX) << escape(fname);
#endif
                    if ( info.line )
                    {
                        dest << ":" << info.line;
                    }
#ifdef CXXTEST_STACK_TRACE_NO_ESCAPE_FILELINE_AFFIXES
                    dest << CXXTEST_STACK_TRACE_FILELINE_SUFFIX;
#else
                    dest << escape(CXXTEST_STACK_TRACE_FILELINE_SUFFIX);
#endif
                }
            }
            dest << suffix;
            return true;
        }
    }
//    else
//    {
//        dest << prefix << location << suffix;
//    }
    return false;
}


// -------------------------------------------------------------------------
// This is a filtering function to trim unnecessary clutter from
// the stack trace
static bool shouldPrintEntry( const std::string& funcName )
{
    return funcName.length()
           && funcName.find( "CxxTest::" ) == std::string::npos
           && funcName.find( "TestDescription_" ) != 0
           && funcName.find( "Memwatch::" ) == std::string::npos
        ;

//    if ( strstr( funcName, "_GLOBAL_" ) )
//        return false;
//
//    if ( strstr( funcName, "global " ) )
//        return false;
//
//    if ( strstr( funcName, "_initialization" ) )
//        return false;
}


// -------------------------------------------------------------------------
static void initSymbolTranslator()
{
    __cxxtest_traceNeedsInit = false;
    bfd_init();
    // use "/proc/self/exe" ???
    abfd = bfd_openr( CXXTEST_STACK_TRACE_EXE, 0 );
    if ( !verifyLoadedSymbolInformation() )
    {
        if ( syms )
        {
            free( syms );
            syms = 0;
        }
        if ( symTable )
        {
            free( symTable );
            symTable = 0;
        }
        if ( abfd )
        {
            bfd_close( abfd );
            abfd = 0;
        }
    }
}


// -------------------------------------------------------------------------
static int verifyLoadedSymbolInformation()
{
    char** matching;

    if ( !abfd )
    {
        printf( "Error loading symbols from executable image: %s.\n",
                bfd_errmsg( bfd_get_error() ) );
        return 0;
    }
    if ( bfd_check_format( abfd, bfd_archive ) )
    {
        printf( "Format error loading symbols from executable image: %s.\n",
                bfd_errmsg( bfd_get_error() ) );
        return 0;
    }
    if ( !bfd_check_format_matches( abfd, bfd_object, &matching ) )
    {
        printf( "Error loading symbols from executable image: "
                "format does not match.\n" );
        free( matching );
        return 0;
    }

    if ( !( bfd_get_file_flags( abfd ) & HAS_SYMS ) )
    {
        return 0;
    }

    unsigned int size;
    numSym = bfd_read_minisymbols( abfd, 0, (void **)&syms, &size );

    if ( !numSym )
    {
        numSym = bfd_read_minisymbols( abfd, 1, (void **)&syms, &size );
    }

    // supporting dynamic symbols
    long storage = bfd_get_symtab_upper_bound( abfd );
    if ( storage < 1 )
    {
        printf( "Error loading symbols from executable image: "
                "no symbols found.\n" );
        return 0;
    }

    syms = (asymbol **)malloc( storage );
    numSym = bfd_canonicalize_symtab( abfd, syms );
    if ( numSym < 1 )
    {
        free( syms );
        printf( "Error loading symbols from executable image: "
                "no canonical symbols found.\n" );
        return 0;
    }
    return 1;
}


// -------------------------------------------------------------------------
static void lookUpSymbol( void* addr, SymbolInfo& info )
{
    info.pc = (bfd_vma)addr;
    info.found = false;
    if ( abfd )
        bfd_map_over_sections( abfd, findBfdAddress, &info );
}


// -------------------------------------------------------------------------
static void findBfdAddress( bfd* abfd, asection* section, void* data )
{
    SymbolInfo* info = (SymbolInfo*)data;
    if ( info->found ) return;
    if ( !( bfd_get_section_flags( abfd, section ) & SEC_ALLOC ) ) return;
    if ( info->pc < section->vma ) return;

    info->pc -= section->vma;
    if ( info->pc >= section->size ) return;

    info->found = bfd_find_nearest_line(
        abfd,
        section,
        syms,
        info->pc,
        &(info->fileName),
        &(info->funcName),
        &(info->line) );
}


// -------------------------------------------------------------------------
static std::string demangle( const char* name )
{
    if ( *name=='.' ) name += 6;

    char* demangle = (char*)cplus_demangle( name, 512+256+2+1 );
    return demangle
        ? std::string( demangle )
        : std::string( name );
}


// -------------------------------------------------------------------------
unsigned int stackTraceSize( unsigned int traceDepth )
{
    return traceDepth * sizeof( StackElem );
}


// -------------------------------------------------------------------------
void saveStackTraceWindow( StackElem* dest, unsigned int traceDepth )
{
    unsigned int offset = traceDepth;
    if ( offset > __cxxtest_stackTop ) offset = __cxxtest_stackTop;
    offset = __cxxtest_stackTop - offset;
    for ( unsigned int i = 0; i < traceDepth; i++ )
    {
        unsigned int pos = offset + i;
        if ( pos < __cxxtest_stackTop )
        {
            dest[i] = __cxxtest_stack[offset + i];
        }
        else
        {
            dest[i].func     = 0;
            dest[i].callsite = 0;
        }
    }
}


};  // End of namespace CxxTest


// -------------------------------------------------------------------------
void __cyg_profile_func_enter( void* func, void* callsite )
{
    if ( CxxTest::__cxxtest_generatingTrace ) return;
    if ( CxxTest::__cxxtest_handlingOverflow ) return;
    if ( CxxTest::__cxxtest_stackTop == _CXXTEST_MAXIMUM_STACK_TRACE_DEPTH )
    {
        CxxTest::__cxxtest_handlingOverflow = true;
        CxxTest::__cxxtest_assertmsg = HINT_PREFIX "call stack too deep";
        abort();
    }

    CxxTest::__cxxtest_stack[CxxTest::__cxxtest_stackTop].func = func;
    CxxTest::__cxxtest_stack[CxxTest::__cxxtest_stackTop].callsite = callsite;
    CxxTest::__cxxtest_stackTop++;
}


// -------------------------------------------------------------------------
void __cyg_profile_func_exit( void*, void* )
{
    if ( CxxTest::__cxxtest_generatingTrace ) return;
    if ( CxxTest::__cxxtest_handlingOverflow ) return;
    if ( CxxTest::__cxxtest_stackTop )
    {
        CxxTest::__cxxtest_stackTop--;
    }
}

#endif
