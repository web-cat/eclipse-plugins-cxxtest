#include <cxxtest/TestSuite.h>
#include <GuiWait.h>

class GreenYellowRed : public CxxTest::TestSuite
{
public:
    void wait()
    {
        CXXTEST_SAMPLE_GUI_WAIT();
    }

    void test_Start_green()
    {
        wait();
    }

    void test_Green_again()
    {
        TS_TRACE( "Still green" );
        wait();
    }

    void test_Now_yellow()
    {
        TS_WARN( "Yellow" );
        wait();
    }

    void test_Cannot_go_back()
    {
        wait();
    }

    void test_Finally_red()
    {
        TS_FAIL( "Red" );
        wait();
    }

    void test_Cannot_go_back_to_yellow()
    {
        TS_WARN( "Yellow?" );
        wait();
    }

    void test_Cannot_go_back_to_green()
    {
        wait();
    }
};
