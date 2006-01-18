    struct sigaction act;
    // act.sa_handler = __cxxtest_sig_handler;
    // act.sa_flags = 0;
    act.sa_sigaction = __cxxtest_sig_handler;
    act.sa_flags = SA_SIGINFO;
    sigaction( SIGSEGV, &act, 0 );
    sigaction( SIGFPE,  &act, 0 );
    sigaction( SIGILL,  &act, 0 );
    sigaction( SIGBUS,  &act, 0 );
    sigaction( SIGABRT, &act, 0 );
    sigaction( SIGTRAP, &act, 0 );
#ifdef SIGEMT
    sigaction( SIGEMT,  &act, 0 );
#endif
    sigaction( SIGSYS,  &act, 0 );