#include <signal.h>	// for siginfo_t and signal constants
#include <setjmp.h>	// for siglongjmp()
#include <stdlib.h>	// for exit()

void __cxxtest_sig_handler( int signum, siginfo_t* info, void* arg )
{
    const char*& msg = CxxTest::__cxxtest_sigmsg;
    msg = "run-time exception";
    switch ( signum )
    {
        case SIGFPE:
			msg = "SIGFPE: floating point exception (div by zero?)";
			// Currently, can't get cygwin g++ to pass in info,
			// so we can't be more specific.
			break;
        case SIGSEGV:
            msg = "SIGSEGV: segmentation fault (null pointer dereference?)";
            break;
        case SIGILL:
            msg = "SIGILL: illegal instruction "
				 "(dereference uninitialized or deleted pointer?)";
            break;
        case SIGTRAP:
            msg = "SIGTRAP: trace trap";
            break;
        case SIGEMT:
            msg = "SIGEMT: EMT instruction";
            break;
        case SIGBUS:
            msg = "SIGBUS: bus error "
				 "(dereference uninitialized or deleted pointer?)";
            break;
        case SIGSYS:
            msg = "SIGSYS: bad argument to system call";
            break;
        case SIGABRT:
            msg = "SIGABRT: execution aborted "
		"(failed assertion, corrupted heap, or other problem?)";
            break;
    }
    if ( CxxTest::__cxxtest_jmppos >= 0 )
    {
		siglongjmp( CxxTest::__cxxtest_jmpbuf[CxxTest::__cxxtest_jmppos], 1 );
    }
    else
    {
		std::cout << "\\nError: untrapped signal:\\n" << msg << std::endl;
		exit(1);
    }
}
