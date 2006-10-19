
namespace CxxTest
{

void __cxxtest_chkptr_error_handler(bool fatal, const char* msg) _CXXTEST_NO_INSTR;

void __cxxtest_chkptr_error_handler(bool fatal, const char* msg)
{
	char text[256];

	if(fatal)
		sprintf(text, "Pointer error: %s", msg);
	else
		strncpy(text, msg, 256);

	if(fatal)
	{
#ifdef CXXTEST_TRAP_SIGNALS
		__cxxtest_assertmsg = text;
#else
		printf("%s\n", text);
#endif
		abort();
	}
	else
	{
		std::string finalMsg = text;

#ifdef CXXTEST_TRACE_STACK
    {
        std::string trace = CxxTest::getStackTrace(__cxxtest_runCompleted);
        if ( trace.length() )
        {
            finalMsg += "\n";
            finalMsg += trace;
        }
    }
#endif

		if(!__cxxtest_runCompleted)
		{
			CxxTest::doWarn("", 0, finalMsg.c_str());
		}
		else
		{
			printf("Warning: %s\n", finalMsg.c_str());
		}
	}
}

} // namespace CxxTest
