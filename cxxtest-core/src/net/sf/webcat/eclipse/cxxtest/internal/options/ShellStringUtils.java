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
package net.sf.webcat.eclipse.cxxtest.internal.options;

import java.util.ArrayList;

/**
 * Utility functions that split a string into an array of components and join an
 * array into a string using logic similar to a shell. Single and double quotes
 * are handled properly, rather than simply splitting across any delimiting
 * whitespace.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class ShellStringUtils
{
	// === Methods ============================================================

	// ------------------------------------------------------------------------
	/**
	 * Splits a string into an array of components. Whitespace is used as the
	 * delimiter, but single and double quotes are handled properly so that a
	 * component in quotes is extracted as a single component, regardless of
	 * whether it contains whitespace or not.
	 * 
	 * @param string
	 *            the String that should be split
	 * 
	 * @return an array of String components extracted from the string
	 */
	public static String[] split(String string)
	{
		ArrayList<String> parts = new ArrayList<String>();
		StringBuffer currentPart = new StringBuffer();

		int state = STATE_BEGIN;

		for(int i = 0; i < string.length(); i++)
		{
			char ch = string.charAt(i);

			switch(state)
			{
			case STATE_BEGIN:
				if(ch == '"')
				{
					currentPart.append(ch);
					state = STATE_DQUOTE;
				}
				else if(ch == '\'')
				{
					currentPart.append(ch);
					state = STATE_SQUOTE;
				}
				else if(ch == '\\')
				{
					state = STATE_ESCAPE_IN_PART;
				}
				else if(!Character.isWhitespace(ch))
				{
					currentPart.append(ch);
					state = STATE_PART;
				}
				break;

			case STATE_PART:
				if(Character.isWhitespace(ch))
				{
					parts.add(currentPart.toString());
					currentPart = new StringBuffer();
					state = STATE_BEGIN;
				}
				else if(ch == '"')
				{
					currentPart.append(ch);
					state = STATE_DQUOTE;
				}
				else if(ch == '\'')
				{
					currentPart.append(ch);
					state = STATE_SQUOTE;
				}
				else if(ch == '\\')
				{
					currentPart.append(ch);
					state = STATE_ESCAPE_IN_PART;
				}
				else
				{
					currentPart.append(ch);
				}
				break;

			case STATE_ESCAPE_IN_PART:
				currentPart.append(ch);
				state = STATE_PART;
				break;

			case STATE_DQUOTE:
				currentPart.append(ch);

				if(ch == '"')
				{
					state = STATE_PART;
				}
				else if(ch == '\\')
				{
					state = STATE_ESCAPE_IN_DQUOTE;
				}
				break;

			case STATE_ESCAPE_IN_DQUOTE:
				currentPart.append(ch);
				state = STATE_DQUOTE;
				break;

			case STATE_SQUOTE:
				currentPart.append(ch);

				if(ch == '\'')
				{
					state = STATE_PART;
				}
				else if(ch == '\\')
				{
					state = STATE_ESCAPE_IN_SQUOTE;
				}
				break;

			case STATE_ESCAPE_IN_SQUOTE:
				currentPart.append(ch);
				state = STATE_SQUOTE;
				break;
			}
		}

		if(currentPart.length() > 0)
			parts.add(currentPart.toString());

		String[] array = new String[parts.size()];
		parts.toArray(array);
		return array;
	}


	// ------------------------------------------------------------------------
	/**
	 * Joins an array of String components into a single whitespace-delimited
	 * string.
	 * 
	 * @param array
	 *            an array of Strings containing the components to be joined
	 * 
	 * @return a String containing the joined components
	 */
	public static String join(String[] array)
	{
		StringBuffer buffer = new StringBuffer();

		if(array.length > 0)
		{
			buffer.append(array[0]);

			for(int i = 1; i < array.length; i++)
			{
				buffer.append(' ');
				buffer.append(array[i]);
			}
		}

		return buffer.toString();
	}


	// === Static Variables ===================================================

	/**
	 * State identifiers used by the DFA in the split method.
	 */
	private static final int STATE_BEGIN = 0;

	private static final int STATE_PART = 1;

	private static final int STATE_ESCAPE_IN_PART = 2;

	private static final int STATE_DQUOTE = 3;

	private static final int STATE_ESCAPE_IN_DQUOTE = 4;

	private static final int STATE_SQUOTE = 5;

	private static final int STATE_ESCAPE_IN_SQUOTE = 6;
}
