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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides functions used to generate a relative path from two
 * absolute paths.
 */
public class PathUtils
{
    /**
     * get relative path of File 'f' with respect to 'home' directory example :
     * home = /a/b/c f = /a/d/e/x.txt s = getRelativePath(home,f) =
     * ../../d/e/x.txt
     * 
     * @param home
     *            base path, should be a directory, not a file, or it doesn't
     *            make sense
     * @param f
     *            file to generate path for
     * @return path from home to f as a string
     */
    public static String relativePathTo(String home, String f)
    {
        return relativePathTo(new File(home), new File(f));
    }


    public static String relativePathTo(File home, File f)
    {
        List<String> homelist;
        List<String> filelist;
        String s;

        if (!home.isDirectory())
            home = home.getParentFile();
        
        if (home == null)
            home = new File(".");

        homelist = getPathList(home);
        filelist = getPathList(f);
        s = matchPathLists(homelist, filelist);

        return s;
    }


    /**
     * break a path down into individual elements and add to a list. example :
     * if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
     * 
     * @param f
     *            input file
     * @return a List collection with the individual elements of the path in
     *         reverse order
     */
    private static List<String> getPathList(File f)
    {
        List<String> l = new ArrayList<String>();
        File r;

        try
        {
            r = f.getCanonicalFile();
            while (r != null)
            {
                l.add(r.getName());
                r = r.getParentFile();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            l = null;
        }
        return l;
    }


    /**
     * figure out a string representing the relative path of 'f' with respect to
     * 'r'
     * 
     * @param r
     *            home path
     * @param f
     *            path of file
     */
    private static String matchPathLists(List<String> r, List<String> f)
    {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size() - 1;
        j = f.size() - 1;

        // first eliminate common root
        while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j))))
        {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for (; i >= 0; i--)
        {
            s += ".." + File.separator;
        }

        // for each level in the file path, add the path
        for (; j >= 1; j--)
        {
            s += f.get(j) + File.separator;
        }

        // file name
        s += f.get(j);
        return s;
    }
}
