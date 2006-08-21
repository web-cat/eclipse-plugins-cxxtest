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
 
#ifndef __CHKPTR_TABLE_H__
#define __CHKPTR_TABLE_H__

#define CHKPTR_ERROR ChkPtr::__manager.logError

namespace ChkPtr
{

enum error_codes {
	PTRERR_DELETE_ARRAY = 0,
	PTRERR_DELETE_NONARRAY,
	PTRERR_DELETE_UNINITIALIZED,
	PTRERR_DELETE_FREED,
	PTRERR_DEREF_UNINITIALIZED,
	PTRERR_DEREF_NULL,
	PTRERR_DEREF_FREED,
	PTRERR_POINT_TO_NONNEW,
	PTRERR_LIVE_OUT_OF_SCOPE,
	PTRERR_LIVE_OVERWRITTEN,
	PTRERR_DEAD_COMPARISON,
	PTRERR_INDEX_NONARRAY,
	PTRERR_INDEX_INVALID,
	PTRERR_DELETE_NOT_DYNAMIC,
	PTRERR_MEMORY_CORRUPTION,
};

class chkptr_reporter
{
public:
	virtual ~chkptr_reporter() { }

	virtual void beginReport(int numEntries, int totalBytes, int maxBytes,
		int numNew, int numArrayNew, int numDelete, int numArrayDelete) = 0;
	virtual void report(void* address, size_t size, const char* filename,
		int line) = 0;
	virtual void endReport() = 0;
};

/**
 * The __checked_pointer_table class is not intended to be used directly
 * by client code. Its methos are only called by the Ptr<T> checked pointer
 * class and by the auto-generated code in the CxxTest test driver.
 */
class __checked_pointer_table
{
private:
	class __stderr_reporter : public chkptr_reporter
	{
	private:
		int totalBytesAllocated;
		int maxBytesInUse;
		int numCallsToNew;
		int numCallsToArrayNew;
		int numCallsToDelete;
		int numCallsToArrayDelete;

	public:
		void beginReport(int numEntries, int totalBytes, int maxBytes,
			int numNew, int numArrayNew, int numDelete, int numArrayDelete)
		{
			if(numEntries > 0)
			{
				printf("%d memory leaks were detected:\n", numEntries);
				printf("--------\n");
			}
			else
			{
				printf("No memory leaks detected.\n");
			}
			
			totalBytesAllocated = totalBytes;
			maxBytesInUse = maxBytes;
			numCallsToNew = numNew;
			numCallsToArrayNew = numArrayNew;
			numCallsToDelete = numDelete;
			numCallsToArrayDelete = numArrayDelete;
		}
	
		void report(void* address, size_t size, const char* filename, int line)
		{
			printf("%10p (%lu bytes), allocated at %s:%d\n",
				address, (unsigned long)size, filename, line);
		}
		
		void endReport()
		{
			printf("\nMemory usage statistics:\n--------\n");
			printf("Total memory allocated during execution:  %d bytes\n", totalBytesAllocated);
			printf("Maximum memory in use during execution:   %d bytes\n", maxBytesInUse);
		}
	} __stderr_reporter_obj;

	/**
	 * The number of buckets in the checked and unchecked pointer hash tables.
	 */
	const static int CHECKED_HASHTABLE_SIZE = 4001;
	const static int UNCHECKED_HASHTABLE_SIZE = 101;
	const static int PROXY_HASHTABLE_SIZE = 31;
	
	/**
	 * Represents a node in the checked and unchecked pointer hash tables.
	 */
	struct __node
	{
		__node* next;
		void* address;
		bool isArray;
		size_t size;
		unsigned long tag;
		const char* filename;
		int line;
		unsigned long refCount;
	};

	/**
	 * Keeps track of the next unique pointer tag.
	 */
	unsigned long nextTag;
	
	/**
	 * A hash table that keeps track of the memory addresses that have been
	 * assigned or were once assigned to checked pointer objects.
	 */
	__node* table[CHECKED_HASHTABLE_SIZE];
	
	/**
	 * A hash table that keeps track of the memory addresses that have not
	 * yet been assigned to checked pointer objects (essentially the step
	 * between the return of a call to "new" and its assignment to a Ptr<T>).
	 */
	__node* uncheckedTable[UNCHECKED_HASHTABLE_SIZE];
	
	/**
	 * The number of currently allocated blocks of memory that are assigned
	 * to checked pointers.
	 */
	unsigned long numEntries;

	/**
	 * The number of currently allocated blocks of memory that have not yet
	 * been assigned to checked pointers.
	 */
	unsigned long numUnchecked;
	
	/**
	 * A pointer to a function that is called when a pointer error occurs
	 * at runtime. 
	 */
	void (*errorHandler)(const char*, const char*, int);

	/**
	 * A pointer to a chkptr_reporter object that is used to report the
	 * current memory allocations.
	 */
	chkptr_reporter* reporter;

	bool ownReporter;

	/**
	 * Indicates whether reportAllocations should be called when the checked
	 * pointer table is destroyed.
	 */
	bool reportAtEnd;

public:
	/**
	 * Indicates the total amount of memory allocated by the program through
	 * its entire execution.
	 */
	int totalBytesAllocated;
	
	/**
	 * Indicates the upper bound on memory used by the program at any time
	 * during its execution.
	 */ 
	int maxBytesInUse;
	
	/**
	 * Indicates the current amount of memory currently in use by the program.
	 */
	int currentBytesAllocated;
	
	/**
	 * Indicates the number of calls made to "new" during execution.
	 */
	int numCallsToNew;

	/**
	 * Indicates the number of calls made to "new[]" during execution.
	 */
	int numCallsToArrayNew;
	
	/**
	 * Indicates the number of calls made to "delete" during execution.
	 */
	int numCallsToDelete;
	
	/**
	 * Indicates the number of calls made to "delete[]" during execution.
	 */
	int numCallsToArrayDelete;

	/**
	 * Initializes a new checked pointer table.
	 */
	__checked_pointer_table();
	
	/**
	 * Destroys the checked pointer table.
	 */
	~__checked_pointer_table();

	/**
	 * Returns the next unique tag (up to 2^32 - 1) for memory address
	 * reuse tracking.
	 */
	unsigned long getTag();
	
	/**
	 * Returns a value indicating whether the assigned pointer table contains
	 * the specified address and tag; that is, whether the memory address is
	 * live.
	 * 
	 * @param address the memory address to check
	 * @param tag the unique tag associated with the pointer
	 * @returns true if the address is live; otherwise, false.
	 */
	bool contains(void* address, unsigned long tag);
	
	/**
	 * Returns a value indicating whether the unchecked pointer table contains
	 * the specified address; that is, the memory at the address has been
	 * allocated by the overloaded new operator but has not yet assigned to a
	 * checked pointer.
	 * 
	 * @param address the memory address to check
	 * @returns true if the address is allocated but unassigned; otherwise,
	 *     false. 
	 */
	bool containsUnchecked(void* address);
	
	/**
	 * Increments the reference count of the specified memory address to
	 * indicate that another checked pointer has been created that points to
	 * it.
	 * 
	 * @param address the memory address being referenced
	 */
	void retain(void* address);
	
	/**
	 * Decrements the reference count of the specified memory address to
	 * indicate that a checked pointer that points to it has gone out of
	 * scope or been overwritten.
	 * 
	 * @param address the memory address being unreferenced
	 */
	void release(void* address);
	
	/**
	 * Returns the number of live references to the specified memory address.
	 * 
	 * @param address the memory address being checked
	 * @returns the number of live references to the address
	 */
	unsigned long getRefCount(void* address);
	
	/**
	 * Returns the size of the memory block allocated at the specified
	 * address.
	 * 
	 * @param address the memory address being checked
	 * @returns a size_t value indicating the number of bytes allocated at
	 *     the memory address
	 */
	size_t getSize(void* address);
	
	/**
	 * Returns a value indicating whether the memory block allocated at the
	 * specified address was allocated using the array version of the "new"
	 * operator.
	 * 
	 * @param address the memory address being checked
	 * @returns true if the memory was allocated using new[]; false if it was
	 *     allocated with new.
	 */
	bool isArray(void* address);
	
	/**
	 * Adds a newly allocated block of memory to the unchecked pointer list.
	 * The overloaded new operators call this to register the new block of
	 * memory before it is assigned to a checked pointer object.
	 * 
	 * @param address the address of the memory that was allocated
	 * @param isArray true if the memory was allocated using new[]; false if
	 *     it was allocated with new.
	 * @param size the number of bytes allocated
	 * @param tag the unique tag that is to be associated with the memory
	 *     address when it is assigned to a checked pointer
	 * @param filename the name of the source file in which the "new"
	 *     statement was invoked
	 * @param line the line number in the source file at which the "new"
	 *     statement was invoked
	 */
	void addUnchecked(void* address, bool isArray, size_t size,
		unsigned long tag, const char* filename, int line);
		
	/**
	 * Moves a currently unchecked memory address to the checked pointer list.
	 * This method is called by the checked pointer class when a pointer is
	 * assigned.
	 * 
	 * @param address the memory address to move
	 * @returns the unique tag associated with this address
	 */
	unsigned long moveToChecked(void* address);

	/**
	 * Removes a memory address from the unchecked pointer table. This is
	 * called by the overloaded delete operators in order to remove any
	 * references to memory addresses that may have been allocated but never
	 * assigned to a checked pointer object.
	 * 
	 * @param address the memory address to remove
	 */	
	void removeUnchecked(void* address);

	/**
	 * Removes a memory address from the checked pointer table. This is called
	 * by the checked pointer class when the final reference to a memory
	 * address is released.
	 * 
	 * @param address the memory address to remove
	 */
	void remove(void* address);

	/**
	 * Logs a pointer error to the error handler.
	 */
	void logError(int code, ...);
	
	/**
	 * Sets the error handler that will be called by the logError method when
	 * a pointer error occurs.
	 * 
	 * @param handler a pointer to a function that is called when a pointer
	 *     error occurs.
	 */
	void setErrorHandler(void (*handler)(const char*, const char*, int));
	
	/**
	 * Prints a report of any memory that is still currently allocated but
	 * not freed at runtime. 
	 */
	void reportAllocations();

	/**
	 * Sets the reporter object used by the reportAllocations method to report
	 * on currently allocated memory.
	 * 
	 * @param reporter the reporter to use
	 */
	void setReporter(chkptr_reporter* reporter, bool own);

	/**
	 * Sets a value indicating whether the reportAllocations method should be
	 * called automatically when the checked pointer table is destroyed.
	 * 
	 * @param value true if reportAllocations should be called; otherwise,
	 *     false.
	 */
	void setReportAtEnd(bool value);

	void getStatistics(int& totalBytes, int& maxBytes, int& numNew,
		int& numArrayNew, int& numDelete, int& numArrayDelete) const;
};

/**
 * The __checked_pointer_table object is defined in <chkptr_table.cpp>.
 */
extern __checked_pointer_table __manager;

}

#endif // __CHKPTR_TABLE_H__
