#include <setjmp.h>	   // for siglongjmp()
#include <stdlib.h>	   // for exit()

void __cxxtest_sig_handler( int, siginfo_t*, void* ) _CXXTEST_NO_INSTR;

void __cxxtest_sig_handler( int signum, siginfo_t* info, void* /*arg*/ )
{
    const char* msg = "run-time exception";
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
#ifdef SIGEMT
        case SIGEMT:
            msg = "SIGEMT: EMT instruction";
            break;
#endif
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

#ifdef CXXTEST_CREATE_BINARY_LOG
	executionLog.setLastResult(signum, CxxTest::__cxxtest_assertmsg);
#endif

    if ( !CxxTest::__cxxtest_assertmsg.empty() )
    {
	CxxTest::__cxxtest_sigmsg = CxxTest::__cxxtest_assertmsg;
	CxxTest::__cxxtest_assertmsg = "";
    }
    else if ( CxxTest::__cxxtest_sigmsg.empty() )
    {
	CxxTest::__cxxtest_sigmsg = msg;
    }
    else
    {
	CxxTest::__cxxtest_sigmsg = std::string(msg)
	    + ", maybe related to " + CxxTest::__cxxtest_sigmsg;
    }

#ifdef CXXTEST_TRACE_STACK
    {
        std::string trace = CxxTest::getStackTrace(CxxTest::__cxxtest_jmppos < 0);
        if ( trace.length() )
        {
            CxxTest::__cxxtest_sigmsg += "\n";
            CxxTest::__cxxtest_sigmsg += trace;
        }
    }
#endif
    if ( CxxTest::__cxxtest_jmppos >= 0 )
    {
		siglongjmp( CxxTest::__cxxtest_jmpbuf[CxxTest::__cxxtest_jmppos], 1 );
    }
    else
    {
        std::cout << "\nError: untrapped signal:\n"
	        << CxxTest::__cxxtest_sigmsg
            << "\n"; // std::endl;
		exit(1);
    }
}
