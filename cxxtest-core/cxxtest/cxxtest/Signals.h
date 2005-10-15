#ifndef __CXXTEST__SIGNALS_H
#define __CXXTEST__SIGNALS_H

#ifdef CXXTEST_TRAP_SIGNALS

#include <iostream>
#include <setjmp.h>

// This file holds the declarations for the support
// features used for trapping and returning from signal-
// based failures.

namespace CxxTest
{
    const int __cxxtest_jmpmax = 10;
    extern sigjmp_buf            __cxxtest_jmpbuf[__cxxtest_jmpmax];
    extern volatile sig_atomic_t __cxxtest_jmppos;
    extern const char*           __cxxtest_sigmsg;

#define _TS_TRY_WITH_SIGNAL_PROTECTION					\
    if ( ++CxxTest::__cxxtest_jmppos >= CxxTest::__cxxtest_jmpmax ) {	\
	std::cout << "Too many nested signal handler levels.\n";	\
	exit( 1 ); }							\
    if ( !sigsetjmp(CxxTest::__cxxtest_jmpbuf[CxxTest::__cxxtest_jmppos], 1) )
#define _TS_CATCH_SIGNAL( action )					\
    else { action } CxxTest::__cxxtest_jmppos--;
#define _TS_THROWS_NO_SIGNAL( msg, action )          	   		\
    _TS_TRY_WITH_SIGNAL_PROTECTION action				\
    _TS_CATCH_SIGNAL( { CxxTest::doFailTest( __FILE__, __LINE__, msg ); } )
}

#else
#define _TS_TRY_WITH_SIGNAL_PROTECTION
#define _TS_CATCH_SIGNAL( action )
#define _TS_THROWS_NO_SIGNAL( msg, action ) action
#endif

#endif
