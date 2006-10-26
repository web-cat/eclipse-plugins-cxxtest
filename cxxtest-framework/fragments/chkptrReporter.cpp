
namespace CxxTest
{
	
class xml_chkptr_reporter : public ChkPtr::chkptr_reporter
{
private:
	FILE* xmlFile;
	int totalBytesAllocated;
	int maxBytesInUse;

public:
	xml_chkptr_reporter(const char* path)
	{
		xmlFile = fopen(path, "w");
	}
	
	virtual void beginReport(int numEntries, int totalBytes, int maxBytes,
		int numNew, int numArrayNew, int numDelete, int numArrayDelete)
	{
		totalBytesAllocated = totalBytes;
		maxBytesInUse = maxBytes;

		fprintf(xmlFile, "<?xml version='1.0'?>\n");
		fprintf(xmlFile, "<memwatch actual-leak-count=\"%d\">\n", numEntries);
	}
		
	virtual void report(const void* address, size_t size,
		const char* filename, int line)
	{
		fprintf(xmlFile, "    <leak address=\"%p\" size=\"%lu\">\n",
			address, (unsigned long)size);

#ifdef CXXTEST_TRACE_STACK
		fprintf(xmlFile,
			getStackTrace(false, CHKPTR_STACK_WINDOW_SIZE,
				(CxxTest::StackElem*)(((char*)address) + size)).c_str() );
#endif

		fprintf(xmlFile, "    </leak>\n");
	}
	
	virtual void reportsTruncated(int numReports, int actualCount)
	{
	}

	virtual void endReport()
	{
		fprintf(xmlFile, "    <summary "
			"total-bytes-allocated=\"%d\" max-bytes-in-use=\"%d\"/>\n",
			totalBytesAllocated, maxBytesInUse);

		fprintf(xmlFile, "</memwatch>\n");
	}
};

}
