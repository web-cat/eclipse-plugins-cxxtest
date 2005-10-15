#ifndef __GUI_WAIT_H
#define __GUI_WAIT_H

#ifdef _WIN32
#   include <windows.h>
#   define CXXTEST_SAMPLE_GUI_WAIT() Sleep( 1000 )
#else // !_WIN32
    extern "C" unsigned sleep( unsigned seconds );
#   define CXXTEST_SAMPLE_GUI_WAIT() sleep( 1 )
#endif // _WIN32

#endif // __GUI_WAIT_H
