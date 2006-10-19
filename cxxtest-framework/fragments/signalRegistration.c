class SignalRegistrar
{
public:
	SignalRegistrar() {
#ifdef CXXTEST_TRACE_STACK
	symreader_initialize(CXXTEST_STACK_TRACE_EXE, SYMFLAGS_DEMANGLE);
#endif
	ChkPtr::__manager.setErrorHandler(&CxxTest::__cxxtest_chkptr_error_handler);

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
	}
};
SignalRegistrar __signal_registrar __attribute__((init_priority(101)));

