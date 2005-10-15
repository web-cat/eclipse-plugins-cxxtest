#ifndef __CXXTEST__ROOT_CPP
#define __CXXTEST__ROOT_CPP

//
// This file holds the "root" of CxxTest, i.e.
// the parts that must be in a source file file.
//

#include <cxxtest/TestTracker.h>
#include <cxxtest/RealDescriptions.h>
#include <cxxtest/GlobalFixture.h>
#include <cxxtest/ValueTraits.h>
#include <cxxtest/Signals.h>

namespace CxxTest
{
    //
    // Class statics
    //
    bool TestTracker::_created = false;
    List GlobalFixture::_list = { 0, 0 };
    List RealSuiteDescription::_suites = { 0, 0 };
#ifdef CXXTEST_TRAP_SIGNALS
    sigjmp_buf __cxxtest_jmpbuf[__cxxtest_jmpmax];
    volatile sig_atomic_t __cxxtest_jmppos = -1;
    const char* __cxxtest_sigmsg = 0;
#endif

    //
    // Some compilers get confused by these functions if they're inline
    //
    bool RealTestDescription::setUp()
    {
        if ( !suite() )
            return false;

        for ( GlobalFixture *gf = GlobalFixture::firstGlobalFixture(); gf != 0; gf = gf->nextGlobalFixture() ) {
            bool ok;
            _TS_TRY
	    {
	        _TS_TRY_WITH_SIGNAL_PROTECTION
		{
		  ok = gf->setUp();
		}
	        _TS_CATCH_SIGNAL({
		  doFailTest( file(), line(), __cxxtest_sigmsg );
		  ok = false;
		});
	    }
            _TS_LAST_CATCH( { ok = false; } );

            if ( !ok ) {
                doFailTest( file(), line(), "Error in GlobalFixture::setUp()" );
                return false;
            }
        }

        _TS_TRY {
            _TSM_ASSERT_THROWS_NOTHING( file(), line(), "Exception thrown from setUp()", suite()->setUp() );
        }
        _TS_CATCH_ABORT( { return false; } );

        return true;
    }

    bool RealTestDescription::tearDown()
    {
        if ( !suite() )
            return false;

        _TS_TRY {
            _TSM_ASSERT_THROWS_NOTHING( file(), line(), "Exception thrown from tearDown()", suite()->tearDown() );
        }
        _TS_CATCH_ABORT( { return false; } );

        for ( GlobalFixture *gf = GlobalFixture::lastGlobalFixture(); gf != 0; gf = gf->prevGlobalFixture() ) {
            bool ok;
            _TS_TRY
	    {
		_TS_TRY_WITH_SIGNAL_PROTECTION
		{
		  ok = gf->tearDown();
		}
	        _TS_CATCH_SIGNAL({
		  doFailTest( file(), line(), __cxxtest_sigmsg );
		  ok = false;
		});
	    }
            _TS_LAST_CATCH( { ok = false; } );

            if ( !ok ) {
                doFailTest( file(), line(), "Error in GlobalFixture::tearDown()" );
                return false;
            }
        }

        return true;
    }

    bool RealWorldDescription::setUp()
    {
        for ( GlobalFixture *gf = GlobalFixture::firstGlobalFixture(); gf != 0; gf = gf->nextGlobalFixture() ) {
            bool ok;
            _TS_TRY {
		_TS_TRY_WITH_SIGNAL_PROTECTION
		{
		  ok = gf->setUpWorld();
		}
	        _TS_CATCH_SIGNAL({
		  doWarn( __FILE__, 1, __cxxtest_sigmsg );
		  ok = false;
		});
	    }
            _TS_LAST_CATCH( { ok = false; } );

            if ( !ok ) {
                doWarn( __FILE__, 1, "Error setting up world" );
                return false;
            }
        }

        return true;
    }

    bool RealWorldDescription::tearDown()
    {
        for ( GlobalFixture *gf = GlobalFixture::lastGlobalFixture(); gf != 0; gf = gf->prevGlobalFixture() ) {
            bool ok;
            _TS_TRY {
		_TS_TRY_WITH_SIGNAL_PROTECTION
		{
		  ok = gf->tearDownWorld();
		}
	        _TS_CATCH_SIGNAL({
		  doWarn( __FILE__, 1, __cxxtest_sigmsg );
		  ok = false;
		});
	    }
            _TS_LAST_CATCH( { ok = false; } );

            if ( !ok ) {
                doWarn( __FILE__, 1, "Error tearing down world" );
                return false;
            }
        }

        return true;
    }

    //
    // These are just nicer here
    //
    Link *List::head()
    {
        Link *l = _head;
        while ( l && !l->active() )
            l = l->next();
        return l;
    }

    const Link *List::head() const
    {
        Link *l = _head;
        while ( l && !l->active() )
            l = l->next();
        return l;
    }

    Link *List::tail()
    {
        Link *l = _tail;
        while ( l && !l->active() )
            l = l->prev();
        return l;
    }

    const Link *List::tail() const
    {
        Link *l = _tail;
        while ( l && !l->active() )
            l = l->prev();
        return l;
    }

    unsigned List::size() const
    {
        unsigned count = 0;
        for ( const Link *l = head(); l != 0; l = l->next() )
            ++ count;
        return count;
    }

    Link *List::nth( unsigned n )
    {
        Link *l = head();
        while ( n -- )
            l = l->next();
        return l;
    }

    void List::activateAll()
    {
        for ( Link *l = _head; l != 0; l = l->justNext() )
            l->setActive( true );
    }

    void List::leaveOnly( const Link &link )
    {
        for ( Link *l = head(); l != 0; l = l->next() )
            if ( l != &link )
                l->setActive( false );
    }

    //
    // Convert total tests to string
    //
#ifndef _CXXTEST_FACTOR
    char *WorldDescription::strTotalTests( char *s ) const
    {
        numberToString( numTotalTests(), s );
        return s;
    }
#else // _CXXTEST_FACTOR
    char *WorldDescription::strTotalTests( char *s ) const
    {
        char *p = numberToString( numTotalTests(), s );

        if ( numTotalTests() <= 1 )
            return s;

        unsigned n = numTotalTests();
        unsigned numFactors = 0;

        for ( unsigned factor = 2; (factor * factor) <= n; factor += (factor == 2) ? 1 : 2 ) {
            unsigned power;

            for ( power = 0; (n % factor) == 0; n /= factor )
                ++ power;

            if ( !power )
                continue;

            p = numberToString( factor, copyString( p, (numFactors == 0) ? " = " : " * " ) );
            if ( power > 1 )
                p = numberToString( power, copyString( p, "^" ) );
            ++ numFactors;
        }

        if ( n > 1 ) {
            if ( !numFactors )
                copyString( p, tracker().failedTests() ? " :(" : tracker().warnings() ? " :|" : " :)" );
            else
                numberToString( n, copyString( p, " * " ) );
        }
        return s;
    }
#endif // _CXXTEST_FACTOR

#   if defined(_CXXTEST_HAVE_EH)
    //
    // Test-aborting stuff
    //
    static bool currentAbortTestOnFail = false;

    bool abortTestOnFail()
    {
        return currentAbortTestOnFail;
    }

    void setAbortTestOnFail( bool value )
    {
        currentAbortTestOnFail = value;
    }
    
    void doAbortTest()
    {
        if ( currentAbortTestOnFail )
            throw AbortTest();
    }
#   endif // _CXXTEST_HAVE_EH

    //
    // Max dump size
    //
    static unsigned currentMaxDumpSize = CXXTEST_MAX_DUMP_SIZE;

    unsigned maxDumpSize()
    {
        return currentMaxDumpSize;
    }
    
    void setMaxDumpSize( unsigned value )
    {
        currentMaxDumpSize = value;
    }
};

#endif // __CXXTEST__ROOT_CPP
