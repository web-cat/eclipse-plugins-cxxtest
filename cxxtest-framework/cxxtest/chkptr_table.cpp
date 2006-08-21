/*
 *	This file is part of Web-CAT Eclipse Plugins.
 *
 *	Web-CAT is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	Web-CAT is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Web-CAT; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
 
/*
 * ACKNOWLEDGMENTS: 
 * This code is based on and extends the work done by Scott M. Pike and
 * Bruce W. Weide at The Ohio State University and Joseph E. Hollingsworth of
 * Indiana University SE in "Checkmate: Cornering C++ Dynamic Memory Errors
 * With Checked Pointers", Proc. of the 31st SIGCSE Technical Symposium on
 * CSE, ACM Press, March 2000.  
 */

#include <chkptr.h>
#include <stdarg.h>

#if defined(CXXTEST_TRACE_STACK) && !defined(CHKPTR_STACK_WINDOW_SIZE)
#  define CHKPTR_STACK_WINDOW_SIZE 8
#endif

#ifdef CHKPTR_BASIC_HEAP_CHECK
#	define SAFETY_SIZE 16
#else
#	define SAFETY_SIZE 0
#endif

#define SAFETY_CHAR '!'

namespace ChkPtr
{

static const char* __error_messages[] = {
	"Called \"delete\" on array pointer (should use \"delete[]\")",
	"Called \"delete[]\" on non-array pointer (should use \"delete\")",
	"Freed uninitialized pointer",
	"Freed memory that was already freed",
	"Dereferenced uninitialized pointer",
	"Dereferenced null pointer",
	"Dereferenced freed memory",
	"Checked pointers cannot be used with memory not allocated with \"new\" or \"new[]\"",
	"Memory leak caused by last valid pointer to memory block going out of scope",
	"Memory leak caused by last valid pointer to memory block being overwritten",
	"Comparison with a dead pointer may result in unpredictable behavior",
	"Indexed a non-array pointer",
	"Invalid array index (%d); valid indices are [0..%lu]",
	"Deleted pointer that was not dynamically allocated",
	"Memory %s block was corrupted; likely invalid array indexing or pointer arithmetic",
};

__checked_pointer_table __manager __attribute__((init_priority(101)));

void __stderr_error_handler(const char* msg,
	const char* filename, int line);

#define HASH(address) \
	(reinterpret_cast<unsigned long>(address) % CHECKED_HASHTABLE_SIZE)
	
#define HASH_UNCHECKED(address) \
	(reinterpret_cast<unsigned long>(address) % UNCHECKED_HASHTABLE_SIZE)

#define HASH_PROXY(address) \
	(reinterpret_cast<unsigned long>(address) % PROXY_HASHTABLE_SIZE)

__checked_pointer_table::__checked_pointer_table()
{
	reportAtEnd = false;
	nextTag = 0;
	numEntries = 0;
	numUnchecked = 0;

	totalBytesAllocated = 0;
	maxBytesInUse = 0;
	numCallsToNew = 0;
	numCallsToArrayNew = 0;
	numCallsToDelete = 0;
	numCallsToArrayDelete = 0;

	for(int i = 0; i < CHECKED_HASHTABLE_SIZE; i++)
		table[i] = 0;

	for(int i = 0; i < UNCHECKED_HASHTABLE_SIZE; i++)
		uncheckedTable[i] = 0;
		
	setErrorHandler(&__stderr_error_handler);
	setReporter(&__stderr_reporter_obj, false);
}

__checked_pointer_table::~__checked_pointer_table()
{
	if(reportAtEnd)
		reportAllocations();
		
	if(ownReporter)
		delete reporter;
}

unsigned long __checked_pointer_table::getTag()
{
	return nextTag++;
}

unsigned long __checked_pointer_table::moveToChecked(void* address)
{
	int index = HASH_UNCHECKED(address);
	__node* node = uncheckedTable[index];

	if(node->address == address)
	{
		uncheckedTable[index] = node->next;
	}
	else
	{
		__node* nextNode = node->next;
		while(nextNode->address != address)
		{
			node = nextNode;
			nextNode = nextNode->next;
		}
		
		node->next = nextNode->next;
		node = nextNode;
	}
	numUnchecked--;

	index = HASH(address);
	
	node->next = table[index];
	table[index] = node;
	numEntries++;
	
	return node->tag;	
}

void __checked_pointer_table::addUnchecked(void* address, bool isArray,
	size_t size, unsigned long tag, const char* filename, int line)
{
	int index = HASH_UNCHECKED(address);
	__node* node = (__node*)malloc(sizeof(__node));
	
	node->address = address;
	node->isArray = isArray;
	node->size = size;
	node->tag = tag;
	node->filename = filename;
	node->line = line;
	node->refCount = 0;

	node->next = uncheckedTable[index];
	uncheckedTable[index] = node;
	numUnchecked++;
}

void __checked_pointer_table::remove(void* address)
{
	int index = HASH(address);
	__node* node = table[index];
	
	if(node->address == address)
	{
		table[index] = node->next;
		free(node); 
	}
	else
	{
		__node* nextNode = node->next;
		while(nextNode->address != address)
		{
			node = nextNode;
			nextNode = nextNode->next;
		}
		
		node->next = nextNode->next;
		free(nextNode);
	}
	
	numEntries--;
}

void __checked_pointer_table::removeUnchecked(void* address)
{
	int index = HASH_UNCHECKED(address);
	__node* node = uncheckedTable[index];
	
	if(node->address == address)
	{
		uncheckedTable[index] = node->next;
		free(node);
	}
	else
	{
		__node* nextNode = node->next;
		while(nextNode->address != address)
		{
			node = nextNode;
			nextNode = nextNode->next;
		}
		
		node->next = nextNode->next;
		free(nextNode);
	}
	
	numUnchecked--;
}

bool __checked_pointer_table::contains(void* address, unsigned long tag)
{
	int index = HASH(address);
	__node* node = table[index];
	
	while((node != 0) && (node->address != address))
		node = node->next;
	
	if(node == 0)
		return false;
	else
		return (node->tag == tag);
}

bool __checked_pointer_table::containsUnchecked(void* address)
{
	int index = HASH_UNCHECKED(address);
	__node* node = uncheckedTable[index];

	while((node != 0) && (node->address != address))
		node = node->next;
	
	return (node != 0);
}

void __checked_pointer_table::retain(void* address)
{
	int index = HASH(address);
	__node* node = table[index];
	
	while(node->address != address)
		node = node->next;
		
	node->refCount++;
}

void __checked_pointer_table::release(void* address)
{
	int index = HASH(address);
	__node* node = table[index];
	
	while(node->address != address)
		node = node->next;
		
	node->refCount--;
}

unsigned long __checked_pointer_table::getRefCount(void* address)
{
	int index = HASH(address);
	__node* node = table[index];
	
	while(node->address != address)
		node = node->next;
		
	return node->refCount;
}

size_t __checked_pointer_table::getSize(void* address)
{
	if(containsUnchecked(address))
	{
		int index = HASH_UNCHECKED(address);
		__node* node = uncheckedTable[index];
	
		while((node != 0) && (node->address != address))
			node = node->next;
		
		if(node != 0)
			return node->size;
		else
			return (size_t)-1;
	}
	else
	{
		int index = HASH(address);
		__node* node = table[index];
	
		while((node != 0) && (node->address != address))
			node = node->next;

		if(node != 0)		
			return node->size;
		else
			return (size_t)-1;
	}
}

bool __checked_pointer_table::isArray(void* address)
{
	int index = HASH(address);
	__node* node = table[index];
	
	while(node->address != address)
		node = node->next;
		
	return node->isArray;
}

void __checked_pointer_table::logError(int code, ...)
{
	char msg[512] = { 0 };

	va_list args;
	va_start(args, code);
	vsprintf(msg, __error_messages[code], args);	
	va_end(args);

	(*errorHandler)(msg, "", 0);
	free(msg);
}

void __checked_pointer_table::setErrorHandler(
	void (*handler)(const char*, const char*, int))
{
	errorHandler = handler;
}

void __checked_pointer_table::reportAllocations()
{
	reporter->beginReport(numEntries + numUnchecked,
		totalBytesAllocated, maxBytesInUse, numCallsToNew, numCallsToArrayNew,
		numCallsToDelete, numCallsToArrayDelete);

	if(numEntries > 0)
		for(int i = 0; i < CHECKED_HASHTABLE_SIZE; i++)
			for(__node* p = table[i]; p != 0; p = p->next)
				reporter->report(p->address, p->size, p->filename, p->line);

	if(numUnchecked > 0)
		for(int i = 0; i < UNCHECKED_HASHTABLE_SIZE; i++)
			for(__node* p = uncheckedTable[i]; p != 0; p = p->next)
			{
				if(p->address != dynamic_cast<void*>(reporter))
					reporter->report(p->address, p->size, p->filename, p->line);
			}
	
	reporter->endReport();
}

void __checked_pointer_table::setReporter(chkptr_reporter* r, bool own)
{
	reporter = r;
	ownReporter = own;
}

void __checked_pointer_table::setReportAtEnd(bool value)
{
	reportAtEnd = value;
}

void __checked_pointer_table::getStatistics(int& totalBytes, int& maxBytes,
	int& numNew, int& numArrayNew, int& numDelete, int& numArrayDelete) const
{
	totalBytes = totalBytesAllocated;
	maxBytes = maxBytesInUse;
	numNew = numCallsToNew;
	numArrayNew = numCallsToArrayNew;
	numDelete = numCallsToDelete;
	numArrayDelete = numCallsToArrayDelete;
}

void __stderr_error_handler(const char* msg, const char* filename, int line)
{
	if(line != 0)
		fprintf(stderr, "Pointer error in %s:%d: %s\n", filename, line, msg);
	else
		fprintf(stderr, "Pointer error: %s\n", msg);
}

} // namespace ChkPtr


void* operator new(size_t size)
{
	ChkPtr::__manager.currentBytesAllocated += size;
	ChkPtr::__manager.totalBytesAllocated += size;
	if(ChkPtr::__manager.currentBytesAllocated > ChkPtr::__manager.maxBytesInUse)
		ChkPtr::__manager.maxBytesInUse = ChkPtr::__manager.currentBytesAllocated;

	int allocSize = size
#ifdef CXXTEST_TRACE_STACK
		+ CxxTest::stackTraceSize(CHKPTR_STACK_WINDOW_SIZE)
#endif
		+ (2 * SAFETY_SIZE)
	;

	void* allocPtr = malloc(allocSize);
	char* ptr = ((char*)allocPtr) + SAFETY_SIZE;

#ifdef CHKPTR_BASIC_HEAP_CHECK
	memset(allocPtr, SAFETY_CHAR, SAFETY_SIZE);
	memset(ptr + size, SAFETY_CHAR, SAFETY_SIZE);
#endif
	
#ifdef CXXTEST_TRACE_STACK
    CxxTest::saveStackTraceWindow(
		(CxxTest::StackElem*)((char*)ptr + size + SAFETY_SIZE),
		CHKPTR_STACK_WINDOW_SIZE);
#endif

	unsigned long tag = ChkPtr::__manager.getTag();
	ChkPtr::__manager.addUnchecked(ptr, false, size, tag, "", 0);

	return ptr;
}

void* operator new[](size_t size)
{
	ChkPtr::__manager.currentBytesAllocated += size;
	ChkPtr::__manager.totalBytesAllocated += size;
	if(ChkPtr::__manager.currentBytesAllocated > ChkPtr::__manager.maxBytesInUse)
		ChkPtr::__manager.maxBytesInUse = ChkPtr::__manager.currentBytesAllocated;

	int allocSize = size
#ifdef CXXTEST_TRACE_STACK
		+ CxxTest::stackTraceSize(CHKPTR_STACK_WINDOW_SIZE)
#endif
		+ (2 * SAFETY_SIZE)
	;

	void* allocPtr = malloc(allocSize);
	char* ptr = ((char*)allocPtr) + SAFETY_SIZE;

#ifdef CHKPTR_BASIC_HEAP_CHECK
	memset(allocPtr, SAFETY_CHAR, SAFETY_SIZE);
	memset(ptr + size, SAFETY_CHAR, SAFETY_SIZE);
#endif

#ifdef CXXTEST_TRACE_STACK
    CxxTest::saveStackTraceWindow(
		(CxxTest::StackElem*)((char*)ptr + size + SAFETY_SIZE),
		CHKPTR_STACK_WINDOW_SIZE);
#endif

	unsigned long tag = ChkPtr::__manager.getTag();
	ChkPtr::__manager.addUnchecked(ptr, true, size, tag, "", 0);

	return ptr;
}

void operator delete(void* address)
{
	if(address != 0)
	{
		size_t size = ChkPtr::__manager.getSize(address);
#ifdef CHKPTR_BASIC_HEAP_CHECK
		if(size == (size_t)-1)
		{
			fprintf(stderr,
				"*** MEMORY ERROR:\n"
				"Attempt to delete memory at address %p\n"
				"which was not dynamically allocated with\n"
				"\"new\" or \"new[]\".\n\n",
				address);
				
			CHKPTR_ERROR(ChkPtr::PTRERR_DELETE_NOT_DYNAMIC);
		}
		else
#endif
		{
			ChkPtr::__manager.currentBytesAllocated -= size;

			if(ChkPtr::__manager.containsUnchecked(address))
				ChkPtr::__manager.removeUnchecked(address);

			void* realAddr = (char*)address - SAFETY_SIZE;

#ifdef CHKPTR_BASIC_HEAP_CHECK
			char lowSafetyStr[SAFETY_SIZE], highSafetyStr[SAFETY_SIZE];
			bool lowDamage = false, highDamage = false;

			memset(lowSafetyStr, SAFETY_CHAR, SAFETY_SIZE);
			memset(highSafetyStr, SAFETY_CHAR, SAFETY_SIZE);

			if(memcmp((char*)realAddr, lowSafetyStr, SAFETY_SIZE))
				lowDamage = true;

			if(memcmp(((char*)address) + size, highSafetyStr, SAFETY_SIZE))
				highDamage = true;

			if(lowDamage || highDamage)
			{
				const char* damageStr;
				if(lowDamage && highDamage)
					damageStr = "before and after";
				else if(lowDamage)
					damageStr = "before";
				else
					damageStr = "after";

				fprintf(stderr,
					"*** MEMORY ERROR:\n"
					"The memory block at address %p is corrupt;\n"
					"the memory %s the block has been\n"
					"overwritten. This was likely caused by improper\n"
					"array indexing or faulty pointer arithmetic.\n\n",
					address, damageStr);
					
				CHKPTR_ERROR(ChkPtr::PTRERR_MEMORY_CORRUPTION, damageStr);
			}
#endif
			free(realAddr);
		}
	}
}

void operator delete[](void* address)
{
	if(address != 0)
	{
		size_t size = ChkPtr::__manager.getSize(address);
#ifdef CHKPTR_BASIC_HEAP_CHECK
		if(size == (size_t)-1)
		{
			fprintf(stderr,
				"*** MEMORY ERROR:\n"
				"Attempt to delete memory at address %p\n"
				"which was not dynamically allocated with\n"
				"\"new\" or \"new[]\".\n\n",
				address);
				
			CHKPTR_ERROR(ChkPtr::PTRERR_DELETE_NOT_DYNAMIC);
		}
		else
#endif
		{
			ChkPtr::__manager.currentBytesAllocated -= size;

			if(ChkPtr::__manager.containsUnchecked(address))
				ChkPtr::__manager.removeUnchecked(address);
		
			void* realAddr = (char*)address - SAFETY_SIZE;
			
#ifdef CHKPTR_BASIC_HEAP_CHECK
			char lowSafetyStr[SAFETY_SIZE], highSafetyStr[SAFETY_SIZE];
			bool lowDamage = false, highDamage = false;

			memset(lowSafetyStr, SAFETY_CHAR, SAFETY_SIZE);
			memset(highSafetyStr, SAFETY_CHAR, SAFETY_SIZE);

			if(memcmp((char*)realAddr, lowSafetyStr, SAFETY_SIZE))
				lowDamage = true;

			if(memcmp(((char*)address) + size, highSafetyStr, SAFETY_SIZE))
				highDamage = true;

			if(lowDamage || highDamage)
			{
				const char* damageStr;
				if(lowDamage && highDamage)
					damageStr = "before and after";
				else if(lowDamage)
					damageStr = "before";
				else
					damageStr = "after";

				fprintf(stderr,
					"*** MEMORY ERROR:\n"
					"The memory block at address %p is corrupt;\n"
					"the memory %s the block has been\n"
					"overwritten. This was likely caused by improper\n"
					"array indexing or faulty pointer arithmetic.\n\n",
					address, damageStr);
					
				CHKPTR_ERROR(ChkPtr::PTRERR_MEMORY_CORRUPTION, damageStr);
			}
#endif
			free(realAddr);
		}
	}
}

