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
package net.sf.webcat.eclipse.cxxtest.internal.generator;

import org.antlr.stringtemplate.AttributeRenderer;

public class TestRunnerStringRenderer implements AttributeRenderer
{
    public TestRunnerStringRenderer(String runnerPath)
    {
        this.runnerPath = runnerPath;
    }

    public String toString(Object o)
    {
        return o.toString();
    }

    public String toString(Object o, String formatName)
    {
    	if (formatName.equals("asCString"))
            return toCString(o.toString());
        else if (formatName.equals("runnerRelativePath"))
            return PathUtils.relativizePath(runnerPath, o.toString());
        else if (formatName.equals("runnerRelativePathAsCString"))
            return toCString(PathUtils.relativizePath(runnerPath, o.toString()));
        else
            return o.toString();
    }

    private String toCString(String str)
    {
        String res = "";
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);

            if (ch == '\\')
                res += "\\\\";
            else
                res += ch;
        }

        return "\"" + res + "\"";
    }

    private String runnerPath;
}
