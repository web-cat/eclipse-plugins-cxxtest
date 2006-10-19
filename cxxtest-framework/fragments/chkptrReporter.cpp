
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
		fprintf(xmlFile, "<memwatch>\n");
	}
		
	virtual void report(void* address, size_t size, const char* filename,
		int line)
	{
		fprintf(xmlFile, "    <leak address=\"%p\" size=\"%lu\">\n",
			address, (unsigned long)size);

		fprintf(xmlFile,
			getStackTrace(false, CHKPTR_STACK_WINDOW_SIZE,
				(CxxTest::StackElem*)(((char*)address) + size)).c_str() );

		fprintf(xmlFile, "    </leak>\n");
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
