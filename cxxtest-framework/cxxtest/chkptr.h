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

#include <cstdio>
#include <memory>
#include "chkptr_table.h"

/**
 * Ptr<T> is a checked pointer class that should be used in lieu of T* in
 * student programs. The checked pointer does not automatically manage memory
 * as a smart pointer does; rather, its operations are tracked and errors in
 * usage that would manifest themselves as crashes or unpredictable behavior
 * at runtime are instead logged to an error mechanism (stderr by default,
 * but this can be overridden if desired).
 *
 * As stated above, Ptr<T> is a drop-in replacement for T*, syntactically.
 * Through the use of tricky operator overloading, no other syntactic
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

		ptr_proxy();
		void set(Ptr<T>* p, bool a);
		~ptr_proxy();
			
		/**
		 * This class uses custom new/delete operators so that the allocation
		 * and deallocation of these objects does not pass through the
		 * overloaded global operators used for tracking.
		 */
		void* operator new[](size_t size, bool isArray);
		void operator delete(void* ptr);
		void operator delete[](void* ptr);
	};

	mutable ptr_proxy* proxy;

	/**
	 * A shorthand method that returns a value indicating whether the pointer
	 * is dead; that is, it either is uninitialized or is non-NULL but no
	 * longer in the checked pointer table.
	 * 
	 * @returns true if the pointer is dead; otherwise, false.
	 */
	bool isDead() const;

	/**
	 * Used by the dereferencing operators to return the pointer owned by
	 * this checked pointer object. This method also handles error checking,
	 * for dead/uninitialized pointers.
	 */
	T* dereference();
	
	/**
	 * Used by the dereferencing operators to return a const pointer to the
	 * memory owned by this checked pointer object. This method also handles
	 * error checking, for dead/uninitialized pointers.
	 */
	const T* constDereference() const;

	/**
	 * Deallocates the memory at the address indicated by this pointer.
	 * 
	 * @param useArrayDelete true if the delete[] operator should be called
	 *     on the memory address indicated by this pointer; false if delete
	 *     should be called.
	 */
	void deallocate(bool useArrayDelete);

	void createProxy();
	
public:
	/**
	 * Creates a checked pointer that represents an uninitialized pointer.
	 */
	Ptr();
	
	/**
	 * Creates a checked pointer that aliases an existing checked pointer
	 * (the result of either a direct aliasing or of passing the pointer to
	 * a function). This causes the reference count of the memory address to
	 * be incremented.
	 * 
	 * @param rhs The checked pointer being aliased.
	 */
	Ptr(const Ptr<T>& rhs);	

	/**
	 * Creates a checked pointer that encapsulates the specified memory
	 * address. This is typically only used on the left-hand side of a call
	 * to new, in order to complete the assignment of the newly allocated
	 * memory to the checked pointer.
	 * 
	 * @param ptr A pointer to an object of type T, to be assigned to this
	 *     checked pointer object.
	 */
	Ptr(T* ptr);

	/**
	 * Destroys the checked pointer object. Since this is called when a
	 * pointer goes out of scope, if this is the last remaining reference to
	 * the memory address identified by this pointer, an error is generated
	 * that a leak will occur.
	 */
	~Ptr();

	/**
	 * Sets the current checked pointer to alias an existing checked pointer.
	 * This causes the reference count of the memory address to be
	 * incremented. If the current pointer is not dead, a memory leak warning
	 * will be generated if it is the last reference to a particular memory
	 * block.
	 * 
	 * @param rhs The checked pointer being aliased.
	 * @returns a reference to this pointer
	 */
	Ptr<T>& operator=(const Ptr<T>& rhs);

	/**
	 * Sets the current checked pointer to alias a memory address, which may
	 * be checked or unchecked. This causes the reference count of the memory
	 * address to be incremented (if checked), or moved into the checked table
	 * (if unchecked). If the current pointer is not dead, a memory leak
	 * warning will be generated if it is the last reference to a particular
	 * memory block.
	 * 
	 * @param ptr A pointer to an object of type T, to be assigned to this
	 *     checked pointer object.
	 * @returns a reference to this pointer
	 */
	Ptr<T>& operator=(T* ptr);

	/**
	 * Returns a value indicating whether the memory addresses of two checked
	 * pointers are equal. If either pointer is dead, a warning is generated
	 * to notify the user that this is likely a logical error.
	 * 
	 * @param rhs the pointer to compare to this pointer
	 * @returns true if the pointers point to the same address; otherwise,
	 *     false.
	 */
	bool operator==(const Ptr<T>& rhs) const;
	
	/**
	 * Returns a value indicating whether the memory address of a checked
	 * pointer is equal to that of a raw pointer. If the checked pointer is
	 * dead, a warning is generated to notify the user that this is likely a
	 * logical error.
	 * 
	 * @param rhs the pointer to compare to this pointer
	 * @returns true if the pointers point to the same address; otherwise,
	 *     false.
	 */
	bool operator==(const T* rhs) const;

	/**
	 * Returns a value indicating whether the memory addresses of two checked
	 * pointers are not equal. If either pointer is dead, a warning is
	 * generated to notify the user that this is likely a logical error.
	 * 
	 * @param rhs the pointer to compare to this pointer
	 * @returns true if the pointers point to the same address; otherwise,
	 *     false.
	 */
	bool operator!=(const Ptr<T>& rhs) const;

	/**
	 * Returns a value indicating whether the memory address of a checked
	 * pointer is not equal to that of a raw pointer. If the checked pointer
	 * is dead, a warning is generated to notify the user that this is likely
	 * a logical error.
	 * 
	 * @param rhs the pointer to compare to this pointer
	 * @returns true if the pointers point to the same address; otherwise,
	 *     false.
	 */
	bool operator!=(const T* rhs) const;

	/**
	 * Dereferences the memory address indicated by this pointer.
	 * 
	 * @returns a reference to the object at the memory address indicated
	 *     by this pointer.
	 */
	T& operator*();

	/**
	 * Dereferences the memory address indicated by this pointer.
	 * 
	 * @returns a reference to the object at the memory address indicated
	 *     by this pointer.
	 */
	T* operator->();

	/**
	 * Dereferences the memory address indicated by this pointer (const).
	 * 
	 * @returns a constant reference to the object at the memory address
	 *     indicated by this pointer.
	 */
	const T& operator*() const;

	/**
	 * Dereferences the memory address indicated by this pointer (const).
	 * 
	 * @returns a constant reference to the object at the memory address
	 *     indicated by this pointer.
	 */
	const T* operator->() const;

	/**
	 * Performs an implicit cast of the checked pointer to a pointer to a
	 * ptr_proxy object.
	 * 
	 * This is the only pointer-cast available in this class, so this is the
	 * operator that will be called when a delete or delete[] statement is
	 * used. This allows us to perform some tasks at deletion-time, by
	 * implementing these in the destructor of the ptr_proxy class.
	 *  
	 * @returns A pointer to a ptr_proxy object.
	 */
	operator ptr_proxy*() const;
	
	/**
	 * Accesses an element of the array pointed to by this pointer, if it is
	 * an array pointer.
	 * 
	 * @param index the integer index of the array element to be accessed.
	 * @returns a reference to the element at the specified index in the
	 *     array.
	 */ 
	T& operator[](int index);

	/**
	 * Accesses an element of the array pointed to by this pointer, if it is
	 * an array pointer (const).
	 * 
	 * @param index the integer index of the array element to be accessed.
	 * @returns a constant reference to the element at the specified index
	 *     in the array.
	 */ 
	const T& operator[](int index) const;
};

#include "chkptr_impl.h"

#endif // __CHKPTR_H__
