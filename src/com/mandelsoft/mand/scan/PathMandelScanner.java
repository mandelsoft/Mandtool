/*
 *  Copyright 2011 Uwe Krueger.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mandelsoft.mand.scan;

import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.Settings;

/**
 *
 * @author Uwe Krueger
 */
public class PathMandelScanner extends CompoundMandelScanner {
  public static boolean debug=true;
  
  public PathMandelScanner(String path, Filter filter, boolean local)
  {
    this(null,path,filter,local,null);
  }

  public PathMandelScanner(Proxy proxy, String path, Filter filter, boolean local)
  {
    this(proxy,path,filter,local,null);
  }

  public PathMandelScanner(String path, Filter filter, boolean local,
                           MandelScannerCache cache)
  {
    this(cache.getProxy(), path, filter, local, cache);
  }

  private PathMandelScanner(Proxy proxy, String path, Filter filter, boolean local,
                            MandelScannerCache cache)
  { 
    if (path!=null) {
      startSetup();
      StringTokenizer t=new StringTokenizer(path, ";");
      Set<Object> elements=new HashSet<Object>();
      if (debug) System.out.println("found path "+path);
      while (t.hasMoreTokens()) {
        String p=t.nextToken().trim();
        if (!p.equals("")) {
          if (p.startsWith("http:")||p.startsWith("file:")) {
            try {
              URL url=new URL(p);
              if (!elements.contains(url)) {
                elements.add(url);
                if (debug) System.out.println("add path "+url);
                MandelScanner s;
                if (cache!=null) {
                  s=cache.getScanner(url,filter);
                }
                else { // don't use cache
                  s=new URLMandelScanner(proxy, url, filter);
                }
                if (s!=null)
                  addScanner(s);
              }
            }
            catch (MalformedURLException ex) {
              System.out.println(p+": "+ex);
            }
          }
          else {
            if (!local)
              throw new RuntimeException(
                      "file paths not allowed for remote repositories");
            try {
              File f=new File(p).getCanonicalFile();

              if (f.exists()&&!elements.contains(f)) {
                elements.add(f);
                if (debug) System.out.println("add path "+f);
                MandelScanner s;
                if (cache!=null) {
                  s=cache.getScanner(f,filter);
                }
                else {
                  s=new FolderMandelScanner(f, filter);
                }
                if (s!=null)
                  addScanner(s);
              }
            }
            catch (IOException io) {
              if (debug) System.out.println("ignoring "+p+": "+io);
            }
          }
        }
      }
      if (debug) System.out.println("path done");
    }
    finishSetup();
  }

  /////////////////////////////////////////////////////////////////////////
  // main
  /////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  { String p=null;

    if (args.length>0) {
      for (int i=0; i<args.length; i++) {
        if (p==null) p=args[i];
        else p=p+";"+args[i];
      }
    }
    else {
      try {
        Settings s=Settings.getSettings();
        p=s.getProperty("mandtool.path");
      }
      catch (IOException ex) {
        System.out.println("cannot read mandel settings");
      }
    }
    if (p!=null) {
      MandelScanner ms=new PathMandelScanner(p,MandelScanner.HAS_IMAGEDATA,true);
      for (MandelName n:ms.getMandelNames()) {
        for (MandelHandle h:ms.getMandelHandles(n)) {
          System.out.println(n+": "+h.getHeader().getTypeDesc()+": "+h.getFile());
        }
      }
    }
    else System.out.println("no path found");
  }
}
