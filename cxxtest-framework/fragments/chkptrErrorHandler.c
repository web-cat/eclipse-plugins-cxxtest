namespace CxxTest
{
	
void __cxxtest_chkptr_error_handler(const char* msg,
	const char* filename, int line)
{
	char text[256];

	if(line != 0)
	{
		sprintf(text, "Pointer error in %s:%d: %s", filename, line, msg);
	}
	else
	{
		sprintf(text, "Pointer error: %s", msg);
	}

#ifdef CXXTEST_TRACE_STACK
	__cxxtest_assertmsg = text;
#else
	fprintf(stderr, "%s\n", text);
#endif
	abort();
}

} // namespace CxxTest
