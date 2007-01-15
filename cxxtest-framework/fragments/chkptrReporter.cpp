
namespace CxxTest
{
	
class xml_chkptr_reporter : public ChkPtr::chkptr_reporter
{
private:
	FILE* xmlFile;
	int totalBytesAllocated;
	int maxBytesInUse;
	int numCallsToNew;
	int numCallsToArrayNew;
	int numCallsToDelete;
	int numCallsToArrayDelete;
	int numCallsToDeleteNull;

public:
	xml_chkptr_reporter(const char* path)
	{
		xmlFile = fopen(path, "w");
	}
	
	virtual void beginReport(int* tagList)
	{
		int numLeaks = 0;

		while(*tagList != CHKPTR_REPORT_END)
		{
			int tag = *tagList++;
			int value = *tagList++;
			
			switch(tag)
			{
				case CHKPTR_REPORT_NUM_LEAKS:
					numLeaks = value;
					break;
				
				case CHKPTR_REPORT_TOTAL_BYTES_ALLOCATED:
					totalBytesAllocated = value;
					break;
					
				case CHKPTR_REPORT_MAX_BYTES_IN_USE:
					maxBytesInUse = value;
					break;
					
				case CHKPTR_REPORT_NUM_CALLS_NEW:
					numCallsToNew = value;
					break;
	
				case CHKPTR_REPORT_NUM_CALLS_ARRAY_NEW:
					numCallsToArrayNew = value;
					break;
	
				case CHKPTR_REPORT_NUM_CALLS_DELETE:
					numCallsToDelete = value;
					break;
	
				case CHKPTR_REPORT_NUM_CALLS_ARRAY_DELETE:
					numCallsToArrayDelete = value;
					break;
	
				case CHKPTR_REPORT_NUM_CALLS_DELETE_NULL:
					numCallsToDeleteNull = value;
					break;
			}
		}

		fprintf(xmlFile, "<?xml version='1.0'?>\n");
		fprintf(xmlFile, "<memwatch actual-leak-count=\"%d\">\n", numLeaks);
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
			"total-bytes-allocated=\"%d\" max-bytes-in-use=\"%d\" "
			"calls-to-new=\"%d\" calls-to-array-new=\"%d\" "
			"calls-to-delete=\"%d\" calls-to-array-delete=\"%d\" "
			"calls-to-delete-null=\"%d\"" 
			"/>\n",
			totalBytesAllocated, maxBytesInUse, numCallsToNew,
			numCallsToArrayNew, numCallsToDelete, numCallsToArrayDelete,
			numCallsToDeleteNull);

		fprintf(xmlFile, "</memwatch>\n");
	}
};

}
