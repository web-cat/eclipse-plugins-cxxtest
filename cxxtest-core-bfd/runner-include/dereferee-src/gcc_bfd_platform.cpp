/*
 *	This file is part of Dereferee, the diagnostic checked pointer library.
 *
 *	Dereferee is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	Dereferee is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Dereferee; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

#include <cstdlib>
#include <cstdio>
#include <cstdarg>
#include <cstring>
#include <bfd.h>
#include <dereferee/platform.h>

#include <cxxtest/Signals.h>

// ===========================================================================
/**
 * The gcc_bfd_platform class is an implementation of the Dereferee::platform
 * class that is intended for systems that support libbfd for reading the
 * symbols from an executable, and the /proc filesystem for accessing the
 * current executable reliably at runtime (to my knowledge, this includes
 * Cygwin and most BSD and Linux distributions).
 *
 * OTHER REQUIREMENTS
 * ------------------
 * To support backtrace collection, you must set the -finstrument-functions
 * flag when compiling. Symbol table access requires that you link to the
 * following libraries: bfd, iberty, intl.
 */

// ===========================================================================

#define NO_INSTR __attribute__((no_instrument_function))

extern "C"
{
char *__cxa_demangle(const char *mangled_name, char *output_buffer,
	size_t *length, int *status);
void __cyg_profile_func_enter(void *this_fn, void *call_site) NO_INSTR;
void __cyg_profile_func_exit(void *this_fn, void *call_site) NO_INSTR;
}

// ==========================================================================

namespace DerefereeSupport
{

extern "C"
{
static void find_bfd_address(bfd* abfd, asection* section, void* data) NO_INSTR;
}

void try_demangle_symbol(const char* mangled, char* demangled, size_t size);

struct backtrace_frame
{
	void *function;
	void *call_site;
};

static const size_t MAX_BACKTRACE_SIZE = 256;

static backtrace_frame back_trace[MAX_BACKTRACE_SIZE];

// If CxxTest is in use, use its stack-top variable since the
// signal handlers need to unwind the stack trace upon failure.
#ifdef CXXTEST_TRAP_SIGNALS
#	define MAX_BACKTRACE_SIZE CxxTest::__cxxtest_jmpmax
#	define back_trace_index CxxTest::__cxxtest_stackTop
#else
	static const size_t MAX_BACKTRACE_SIZE = 256;
	static uint32_t back_trace_index = 0;
#endif

struct platform_symbol_info
{
	bfd_vma pc;
	const char* filename;
	const char* funcName;
	int line;
	int found;
};

// ===========================================================================
class symbol_table
{
public:
	~symbol_table() NO_INSTR;

	static symbol_table *instance() NO_INSTR;

	const char *symbol_name_at_address(uint32_t address) NO_INSTR;
	char *demangled_name_at_address(uint32_t address) NO_INSTR;
	uint32_t source_location_at_address(uint32_t address, const char **path,
		uint32_t *line) NO_INSTR;

	void *operator new(size_t size) NO_INSTR;
	void operator delete(void* ptr) NO_INSTR;

private:
	symbol_table() NO_INSTR;

	void load_symbol_info();

	static symbol_table *the_instance;

	bool symbols_loaded;
};

symbol_table *symbol_table::the_instance = NULL;

static bfd* abfd;
static asymbol** syms;
static unsigned long num_symbols;
static asymbol** sym_table;

// ===========================================================================
/**
 * Interface and implementation of the gcc_bfd_platform class.
 */
class gcc_bfd_platform : public Dereferee::platform
{
public:
	gcc_bfd_platform(const Dereferee::option* options);

	~gcc_bfd_platform();

	void** get_backtrace(void* instr_ptr, void* frame_ptr);

	void free_backtrace(void** backtrace);

	bool get_backtrace_frame_info(void* frame, char* function,
		char* filename, int* line_number);
};

// ---------------------------------------------------------------------------
gcc_bfd_platform::gcc_bfd_platform(const Dereferee::option* options)
{
	// Force the symbol table to be created at the start of execution.
	symbol_table::instance();
}

// ---------------------------------------------------------------------------
gcc_bfd_platform::~gcc_bfd_platform()
{
}

// ------------------------------------------------------------------
void** gcc_bfd_platform::get_backtrace(void* /* instr_ptr */,
		void* /* frame_ptr */)
{
	if(back_trace_index == 0)
		return NULL;

	void** bt = (void**)calloc(back_trace_index + 1, sizeof(void*));
	int bt_index = 0;

	bt[bt_index++] = back_trace[back_trace_index - 1].function;

	for(int i = (int)back_trace_index - 1; i >= 1; i--)
	{
		bt[bt_index++] = back_trace[i].call_site;
	}

	bt[bt_index++] = NULL;
	return bt;
}

// ------------------------------------------------------------------
void gcc_bfd_platform::free_backtrace(void** backtrace)
{
	if(backtrace)
		free(backtrace);
}

// ------------------------------------------------------------------
bool gcc_bfd_platform::get_backtrace_frame_info(void* frame, char* function,
	char* filename, int* line_number)
{
	char *name = symbol_table::instance()->demangled_name_at_address(
		(uint32_t)frame);
	const char *path = "";
	uint32_t line = 0;

	if (name)
	{
		strncpy(function, name, DEREFEREE_MAX_FUNCTION_LEN - 1);

		uint32_t true_address =
			symbol_table::instance()->source_location_at_address(
			(uint32_t)frame, &path, &line);

		if (true_address)
		{
			strncpy(filename, path, DEREFEREE_MAX_FILENAME_LEN - 1);
			*line_number = line;
		}
		else
		{
			filename[0] = '\0';
			*line_number = 0;
		}

		free(name);

		return true;
	}

	return false;
}

// ===========================================================================

static void find_bfd_address(bfd* abfd, asection* section, void* data)
{
	platform_symbol_info* info = (platform_symbol_info*)data;
	if(info->found)
		return;

	if(!(bfd_get_section_flags(abfd, section) & SEC_ALLOC))
		return;

	bfd_vma pc = info->pc;
	if(pc < section->vma)
		return;

	pc -= section->vma;
	if(pc >= section->size)
		return;

	info->found = bfd_find_nearest_line(abfd, section, syms, pc,
		&info->filename, &info->funcName, (unsigned int*)(&info->line));
}

// ===========================================================================

void destroy_symbol_table()
{
	delete symbol_table::instance();
}

void *symbol_table::operator new(size_t size) { return malloc(size); }

void symbol_table::operator delete(void* ptr) { free(ptr); }

symbol_table *symbol_table::instance()
{
	if(the_instance == NULL)
	{
		the_instance = new symbol_table();
		atexit(&destroy_symbol_table);
	}

	return the_instance;
}

symbol_table::symbol_table()
{
	symbols_loaded = false;

	bfd_init();

	pid_t pid = getpid();
	char proc_path[512];
	snprintf(proc_path, sizeof(proc_path), "/proc/%lu/exe", (unsigned long)pid);
	abfd = bfd_openr(proc_path, 0);

	load_symbol_info();
}

symbol_table::~symbol_table()
{
	if(syms)
		free(syms);

	if(sym_table)
		free(sym_table);

	if(abfd)
		bfd_close(abfd);
}

void symbol_table::load_symbol_info()
{
	char** matching;

	if(!abfd)
		return;

	if(bfd_check_format(abfd, bfd_archive))
		return;

	if(!bfd_check_format_matches(abfd, bfd_object, &matching))
	{
		free(matching);
		return;
	}

	if(!(bfd_get_file_flags(abfd) & HAS_SYMS))
		return;

	unsigned int size;
	num_symbols = bfd_read_minisymbols(abfd, 0, (void**)&syms, &size);

	if(!num_symbols)
		num_symbols = bfd_read_minisymbols(abfd, 1, (void**)&syms, &size);

	// supporting dynamic symbols
	long storage = bfd_get_symtab_upper_bound(abfd);
	if(storage < 1)
		return;

	syms = (asymbol**)malloc(storage);
	num_symbols = bfd_canonicalize_symtab(abfd, syms);
	if(num_symbols < 1)
	{
		free(syms);
		return;
	}

	symbols_loaded = true;
}

const char *symbol_table::symbol_name_at_address(uint32_t address)
{
	if(!symbols_loaded)
		return NULL;

	platform_symbol_info info;
	info.pc = (bfd_vma)address;
	info.found = 0;

	if(abfd)
		bfd_map_over_sections(abfd, &find_bfd_address, &info);

	if(info.found)
		return info.funcName;
	else
		return NULL;
}

char *symbol_table::demangled_name_at_address(uint32_t address)
{
	const char *name = symbol_name_at_address(address);
	if(!name)
		return NULL;

	int status;
	char *demangled = __cxa_demangle(name, NULL, NULL, &status);

	if(status != 0)
	{
		demangled = (char*)malloc(strlen(name) + 1);
		strcpy(demangled, name + ((name[0] == '_')? 1 : 0));
	}

	return demangled;
}

uint32_t symbol_table::source_location_at_address(uint32_t address,
	const char **path, uint32_t *line)
{
	if(!symbols_loaded)
		return 0;

	platform_symbol_info info;
	info.pc = (bfd_vma)address;
	info.found = 0;

	if(abfd)
		bfd_map_over_sections(abfd, &find_bfd_address, &info);

	if(info.found)
	{
		*path = info.filename;
		*line = info.line;
		return (uint32_t)info.pc;
	}
	else
		return 0;
}

void try_demangle_symbol(const char* mangled, char* demangled, size_t size)
{
	unsigned skip_first = 0;
	if(mangled[0] == '.' || mangled[0] == '$')
		++skip_first;

	char* ptr = __cxa_demangle(mangled + skip_first, 0, 0, 0);
	strncpy(demangled, ptr, size);
	free(ptr);
}

} // end namespace DerefereeSupport

// ===========================================================================
/*
 * Implementation of the functions called by the Dereferee memory manager to
 * create and destroy the listener object.
 */

Dereferee::platform* Dereferee::create_platform(
		const Dereferee::option* options)
{
	return new DerefereeSupport::gcc_bfd_platform(options);
}

void Dereferee::destroy_platform(Dereferee::platform* platform)
{
	delete platform;
}

// ===========================================================================

void __cyg_profile_func_enter(void *this_fn, void *call_site)
{
	using namespace DerefereeSupport;

    if (CxxTest::__cxxtest_handlingOverflow)
		return;

    if ((int)back_trace_index == (int)MAX_BACKTRACE_SIZE)
    {
		// Assume the student has gone into infinite recursion and abort.
        CxxTest::__cxxtest_handlingOverflow = true;

#ifdef __CYGWIN__
		// Can't use abort() here because it hard-kills the process on Windows,
		// rather than raising a signal that would be caught so execution could
		// continue with the next test case. Instead, cause an access violation
		// that will be caught by the structured exception handler.
		int* x = 0;
		*x = 0xBADBEEF;
#else
		abort();
#endif
    }
	else
	{
		back_trace[back_trace_index].function = this_fn;
		back_trace[back_trace_index].call_site = call_site;
		back_trace_index++;
	}
}

void __cyg_profile_func_exit(void *this_fn, void *call_site)
{
	using namespace DerefereeSupport;

    if (CxxTest::__cxxtest_handlingOverflow)
		return;

    if (back_trace_index)
    	back_trace_index--;
}
