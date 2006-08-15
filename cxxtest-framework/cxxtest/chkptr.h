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
#ifndef __CHKPTR_H__
#define __CHKPTR_H__

#include <chkptr_table.h>

/**
 * Ptr<T> is a checked pointer class that should be used in lieu of T* in
 * student programs. The checked pointer does not automatically manage memory
 * as a smart pointer does; rather, its operations are tracked and errors in
 * usage that would manifest themselves as crashes or unpredictable behavior
 * at runtime are instead logged to an error mechanism (stderr by default,
 * but this can be overridden if desired).
 *
 * As stated above, Ptr<T> totally replaces T* syntactically. Through the
 * use of operator overloading and some macro magic, no other syntactic
 * constructs need to be modified (notably, standard 'new' and 'delete'
 * work as expected).
 */
template <typename T>
class Ptr
{
private:
	/**
	 * The actual memory address referenced by this pointer.
	 */
	T* pointer;
	
	/**
	 * A unique (up to 2^32 - 1) tag that indicates "when" the memory was
	 * allocated. Since memory addresses can be reused after a deallocation/
	 * allocation cycle, the tag provides a way to identify separate uses
	 * of the same address.
	 */
	unsigned long tag;
	
	/**
	 * Is initially false, when a pointer object is declared but not yet
	 * assigned an address. This is used to customize some error messages
	 * regarding use of uninitialized pointers.
	 */
	bool isInit;

	/**
	 * The ptr_proxy class is used to cajole the delete and delete[]
	 * statements to work with the checked pointers. A checked pointer can
	 * be implicitly cast to a pointer to one of these objects, and since
	 * ptr_proxy* is the only available pointer cast, it will be invoked
	 * when delete or delete[] is called. When this temporary ptr_proxy
	 * object is freed, the actual delete logic for the checked pointer is
	 * then executed.
	 */
	class ptr_proxy
	{
		enum {
			PROXY_HEADER_SIZE = sizeof(unsigned long),
			PROXY_ARRAY_TAG = 0xFAFBFCFD,
			PROXY_NONARRAY_TAG = 0xFDFCFBFA
		};

	public:
		unsigned long tag;
		Ptr<T>* parent;
		bool doNothing;

		ptr_proxy() : doNothing(false) { }

		void set(Ptr<T>* p, bool a)
		{
			parent = p;
			tag = (a ? PROXY_ARRAY_TAG : PROXY_NONARRAY_TAG);
		}

		~ptr_proxy()
		{
			if(doNothing)
				return;

			parent->deallocate(tag == PROXY_ARRAY_TAG);
		}
	
		/**
		 * This class uses custom new/delete operators so that the allocation
		 * and deallocation of these objects does not pass through the
		 * overloaded global operators used for tracking.
		 */
		void* operator new[](size_t size, bool isArray)
		{
			void* ptr = malloc(size + PROXY_HEADER_SIZE);
			*((unsigned long*)ptr) = isArray ? PROXY_ARRAY_TAG : PROXY_NONARRAY_TAG;
			return (char*)ptr + PROXY_HEADER_SIZE;
		}

		void operator delete(void* ptr)
		{
			unsigned long tag = *((unsigned long*)ptr);
			if(tag != PROXY_NONARRAY_TAG)
			{
				CHKPTR_ERROR(ChkPtr::PTRERR_DELETE_ARRAY);
//					"Called \"delete\" on array pointer (should use \"delete[]\")",
//					"", 0);
			}

			char* backtrack = (char*)ptr - 1;
			while(*((unsigned long*)backtrack) != PROXY_NONARRAY_TAG)
				backtrack--;

			free(backtrack);
		}
		
		void operator delete[](void* ptr)
		{
			unsigned long tag = *((unsigned long*)ptr - 1);						
			if(tag != PROXY_ARRAY_TAG)
			{
				CHKPTR_ERROR(ChkPtr::PTRERR_DELETE_NONARRAY);
//					"Called \"delete[]\" on non-array pointer (should use \"delete\")",
//					"", 0);
			}
	
			free((char*)ptr - PROXY_HEADER_SIZE);
		}
	};

	ptr_proxy* proxy;

	/**
	 * A shorthand method that returns a value indicating whether the pointer
	 * is dead; that is, it either is uninitialized or is non-NULL but no
	 * longer in the checked pointer table.
	 * 
	 * @returns true if the pointer is dead; otherwise, false.
	 */
	bool isDead() const
	{
		return !isInit || ((pointer != 0) && (!ChkPtr::__manager.contains(pointer, tag)));
	}

	T* dereference()
	{	
		if(!isInit)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_UNINITIALIZED);
//				"Dereferenced uninitialized pointer",
//				"", 0);
		}
		else if(pointer == 0)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_NULL);
//				"Dereferenced null pointer",
//				"", 0);
		}
		else if(!ChkPtr::__manager.contains(pointer, tag))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_FREED);
//				"Dereferenced pointer to freed memory",
//				"", 0);
		}

		return pointer;
	}
	
	const T* constDereference() const
	{	
		if(!isInit)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_UNINITIALIZED);
//				"Dereferenced uninitialized pointer",
//				"", 0);
		}
		else if(pointer == 0)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_NULL);
//				"Dereferenced null pointer",
//				"", 0);
		}
		else if(!ChkPtr::__manager.contains(pointer, tag))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_FREED);
//				"Dereferenced pointer to freed memory",
//				"", 0);
		}

		return pointer;
	}

	/**
	 * Deallocates the memory at the address indicated by this pointer.
	 * 
	 * @param useArrayDelete true if the delete[] operator should be called
	 *     on the memory address indicated by this pointer; false if delete
	 *     should be called.
	 */
	void deallocate(bool useArrayDelete)
	{
		if(!isInit)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DELETE_UNINITIALIZED);
//				"Attempted to delete uninitialized pointer",
//				"", 0);
		}
		else if(pointer != 0)
		{
			if(!ChkPtr::__manager.contains(pointer, tag))
			{
				CHKPTR_ERROR(ChkPtr::PTRERR_DELETE_FREED);
//					"Attempted to free memory that has already been freed",
//					"", 0);
			}
			else
			{				
				if(useArrayDelete)
					delete[] pointer;
				else
					delete pointer;
	
				ChkPtr::__manager.remove(pointer);
				createProxy();
			}
		}
	}

	void createProxy()
	{
		bool isArray = false;
		if(ChkPtr::__manager.contains(pointer, tag))
			isArray = ChkPtr::__manager.isArray(pointer);
		
		proxy = new(isArray) ptr_proxy[1];
		proxy[0].set(this, isArray);
	}
	
public:
	/**
	 * Creates a checked pointer that represents an uninitialized pointer.
	 */
	Ptr() : tag((unsigned long)~0), isInit(false), proxy(0) { }
	
	/**
	 * Creates a checked pointer that aliases an existing checked pointer
	 * (the result of either a direct aliasing or of passing the pointer to
	 * a function). This causes the reference count of the memory address to
	 * be incremented.
	 * 
	 * @param rhs The checked pointer being aliased.
	 */
	Ptr(const Ptr<T>& rhs)
	{
		pointer = rhs.pointer;
		tag = rhs.tag;
		isInit = rhs.isInit;
		proxy = 0;

		// When a checked pointer object is copied (due to aliasing or
		// passing to a function), we increment the pointer's reference
		// count if it is live.
		if(ChkPtr::__manager.contains(pointer, tag))
		{
			ChkPtr::__manager.retain(pointer);
			createProxy();
		}
	}
	
	/**
	 * Creates a checked pointer that encapsulates the specified memory
	 * address. This is typically only used on the left-hand side of a call
	 * to new, in order to complete the assignment of the newly allocated
	 * memory to the checked pointer.
	 * 
	 * @param ptr A pointer to an object of type T, to be assigned to this
	 *     checked pointer object.
	 */
	Ptr(T* ptr)
	{
		if(ptr != 0 && !ChkPtr::__manager.containsUnchecked(ptr))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_POINT_TO_NONNEW);
//				"Checked pointers cannot point to memory that was not allocated with \"new\" or \"new[]\"",
//				"", 0);
		}

		pointer = ptr;
		isInit = true;
		proxy = 0;

		if(ptr != 0)
		{
			tag = ChkPtr::__manager.moveToChecked(ptr);
			ChkPtr::__manager.retain(pointer);
			createProxy();
		}
	}

	/**
	 * Destroys the checked pointer object. Since this is called when a
	 * pointer goes out of scope, if this is the last remaining reference to
	 * the memory address identified by this pointer, an error is generated
	 * that a leak will occur.
	 */
	~Ptr()
	{
		// If the pointer table contains the pointer, then it is still
		// alive and we decrement its reference count. If this causes the
		// count to reach zero, then we have a live pointer going out of
		// scope, which will result in a memory leak.
		if(ChkPtr::__manager.contains(pointer, tag))
		{
			ChkPtr::__manager.release(pointer);
			if(ChkPtr::__manager.getRefCount(pointer) == 0)
			{
				CHKPTR_ERROR(ChkPtr::PTRERR_LIVE_OUT_OF_SCOPE);
//					"Memory leak caused by live pointer going out of scope without any other references",
//					"", 0);
			}
		}
		
		if(proxy)
		{
			proxy->doNothing = true;
			delete proxy;
		}
	}

	/**
	 * 
	 */
	Ptr<T>& operator=(const Ptr<T>& rhs)
	{
		if(this != &rhs)
		{
			// If the pointer to which assignment is being made is alive,
			// decrement its reference count since we are writing over its
			// value. If this causes the count to reach zero, then we have
			// a memory leak because no references to the memory remain.
			if(ChkPtr::__manager.contains(pointer, tag))
			{
				ChkPtr::__manager.release(pointer);
				
				if(ChkPtr::__manager.getRefCount(pointer) == 0)
				{
					CHKPTR_ERROR(ChkPtr::PTRERR_LIVE_OVERWRITTEN);
//						"Memory leak caused by live pointer being overwritten without any other references",
//						"", 0);
				}
			}
			
			pointer = rhs.pointer;
			tag = rhs.tag;
			isInit = rhs.isInit;
			proxy = 0;
			
			// Increment the reference count of the pointer that was used
			// on the right-hand side of the assignment.
			if(ChkPtr::__manager.contains(pointer, tag))
			{
				ChkPtr::__manager.retain(pointer);
				createProxy();
			}
		}

		return *this;
	}

	/**
	 * 
	 */
	Ptr<T>& operator=(T* ptr)
	{
		// If the pointer to which assignment is being made is alive,
		// decrement its reference count since we are writing over its
		// value. If this causes the count to reach zero, then we have
		// a memory leak because no references to the memory remain.
		if(ChkPtr::__manager.contains(pointer, tag))
		{
			ChkPtr::__manager.release(pointer);
			
			if(ChkPtr::__manager.getRefCount(pointer) == 0)
			{
				CHKPTR_ERROR(ChkPtr::PTRERR_LIVE_OVERWRITTEN);
//					"Memory leak caused by live pointer being overwritten without any other references",
//					"", 0);
			}
		}
		else if(ptr != 0 && !ChkPtr::__manager.containsUnchecked(ptr))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_POINT_TO_NONNEW);
//				"Checked pointers cannot point to memory that was not allocated with \"new\" or \"new[]\"",
//				"", 0);
		}
		
		pointer = ptr;
		isInit = true;
		proxy = 0;
		
		if(ptr != 0)
		{
			tag = ChkPtr::__manager.moveToChecked(ptr);
			ChkPtr::__manager.retain(pointer);
			createProxy();	
		}
		
		return *this;
	}

	/**
	 * 
	 */
	bool operator==(const Ptr<T>& rhs) const
	{
		// If either of the pointers is non-NULL and is not in the pointer
		// table, then it must be a dead pointer. Performing a comparison
		// with a dead pointer can yield to unpredictable results.
		if(isDead() || rhs.isDead())
		{
			// equality checking with dead pointer; depending on the value of a
			// pointer that is no longer alive is unpredictable
			CHKPTR_ERROR(ChkPtr::PTRERR_DEAD_COMPARISON);
//				"Performing comparison against dead pointer can yield unpredictable results",
//				"", 0);
		}
		
		return (pointer == rhs.pointer);
	}
	
	/**
	 * 
	 */
	bool operator==(const T* rhs) const
	{
		// If either of the pointers is non-NULL and is not in the pointer
		// table, then it must be a dead pointer. Performing a comparison
		// with a dead pointer can yield to unpredictable results.
		if(isDead())
		{
			// equality checking with dead pointer; depending on the value of a
			// pointer that is no longer alive is unpredictable
			CHKPTR_ERROR(ChkPtr::PTRERR_DEAD_COMPARISON);
//				"Performing comparison against dead pointer can yield unpredictable results",
//				"", 0);
		}
		
		return (pointer == rhs);
	}

	/**
	 * 
	 */
	bool operator!=(const Ptr<T>& rhs) const
	{
		return !(*this == rhs);
	}

	/**
	 * 
	 */
	bool operator!=(const T* rhs) const
	{
		return !(*this == rhs);
	}

	/**
	 * Dereferences the memory address indicated by this pointer.
	 * 
	 * @returns a reference to the object at the memory address indicated
	 *     by this pointer.
	 */
	T& operator*() { return *dereference(); }

	/**
	 * Dereferences the memory address indicated by this pointer.
	 * 
	 * @returns a reference to the object at the memory address indicated
	 *     by this pointer.
	 */
	T* operator->() { return dereference(); }

	/**
	 * Dereferences the memory address indicated by this pointer (const).
	 * 
	 * @returns a constant reference to the object at the memory address
	 *     indicated by this pointer.
	 */
	const T& operator*() const { return *constDereference(); }

	/**
	 * Dereferences the memory address indicated by this pointer (const).
	 * 
	 * @returns a constant reference to the object at the memory address
	 *     indicated by this pointer.
	 */
	const T* operator->() const { return constDereference(); }

	/**
	 * Performs an implicit cast of the checked pointer to a pointer to a
	 * ptr_proxy object.
	 * 
	 * This is the only pointer-cast available in this class, so this is the
	 * operator that will be called when a delete or delete[] statement is
	 * used. This allows us to perform some tasks at deletion-time, by
	 * implementing these in the destructor of the ptr_proxy class.
	 * 
	 * This method actually returns different values depending on which state
	 * the delete state-machine loop is in. The first time through the loop,
	 * the ptr_proxy object is set up but NULL is returned so that the
	 * actual delete call will be a no-op. The second invocation of this
	 * operator in the loop will return the actual proxy object for deletion.
	 *  
	 * @returns A pointer to a ptr_proxy object.
	 */
	operator ptr_proxy*()
	{
		return proxy;
	}
	
	/**
	 * Accesses an element of the array pointed to by this pointer, if it is
	 * an array pointer.
	 * 
	 * @param index the integer index of the array element to be accessed.
	 * @returns a reference to the element at the specified index in the
	 *     array.
	 */ 
	T& operator[](int index)
	{
		if(!isInit)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_UNINITIALIZED);
//				"Dereferenced uninitialized pointer",
//				"", 0);
		}
		else if(pointer == 0)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_NULL);
//				"Dereferenced null pointer",
//				"", 0);
		}
		else if(!ChkPtr::__manager.contains(pointer, tag))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_FREED);
//				"Dereferenced pointer to freed memory",
//				"", 0);
		}

		if(!ChkPtr::__manager.isArray(pointer))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_INDEX_NONARRAY);
//				"Attempted to index non-array pointer",
//				"", 0);
		}

		size_t size = ChkPtr::__manager.getSize(pointer) / sizeof(T);
		if(index < 0 || index >= (int)size)
		{
//			char msg[256] = { 0 };
//			sprintf(msg, "Invalid array index (%d); "
//				"valid indices are [0..%lu]", index, size - 1);

			CHKPTR_ERROR(ChkPtr::PTRERR_INDEX_INVALID, index, size - 1);
		}
		
		return *(pointer + index);
	}

	/**
	 * Accesses an element of the array pointed to by this pointer, if it is
	 * an array pointer (const).
	 * 
	 * @param index the integer index of the array element to be accessed.
	 * @returns a constant reference to the element at the specified index
	 *     in the array.
	 */ 
	const T& operator[](int index) const
	{
		if(!isInit)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_UNINITIALIZED);
//				"Dereferenced uninitialized pointer",
//				"", 0);
		}
		else if(pointer == 0)
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_NULL);
//				"Dereferenced null pointer",
//				"", 0);
		}
		else if(!ChkPtr::__manager.contains(pointer, tag))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_DEREF_FREED);
//				"Dereferenced pointer to freed memory",
//				"", 0);
		}

		if(!ChkPtr::__manager.isArray(pointer))
		{
			CHKPTR_ERROR(ChkPtr::PTRERR_INDEX_NONARRAY);
//				"Attempted to index non-array pointer",
//				"", 0);
		}

		size_t size = ChkPtr::__manager.getSize(pointer) / sizeof(T);
		if(index < 0 || index >= (int)size)
		{
//			char msg[256] = { 0 };
//			sprintf(msg, "Invalid array index (%d); "
//				"valid indices are [0..%lu]", index, size - 1);

			CHKPTR_ERROR(ChkPtr::PTRERR_INDEX_INVALID, index, size - 1);
		}
		
		return *(pointer + index);
	}
};

#endif // __CHKPTR_H__
