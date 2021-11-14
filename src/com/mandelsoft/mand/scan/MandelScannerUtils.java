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

import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelImageDBContext;
import com.mandelsoft.mand.MandelImageDBContext.ContextMapping;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.MandelScanner.Filter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class MandelScannerUtils {
  static public MandelData getMandelData(MandelScanner s, MandelName n)
    {
      return getMandelData(s.getMandelData(n));
    }

    static public MandelData getMandelData(MandelHandle h)
    {
      if (h!=null) {
        try {
          return h.getData();
        }
        catch (IOException ex) {
          // just ignore and return null;
        }
      }
      return null;
    }

    static public MandelData getMandelInfo(MandelScanner s, MandelName n)
    {
      return getMandelInfo(s.getMandelInfo(n));
    }

    static public MandelData getMandelInfo(MandelHandle h)
    {
      if (h!=null) {
        try {
          return h.getInfo();
        }
        catch (IOException ex) {
          // just ignore and return null;
        }
      }
      return null;
    }

    static public boolean isInfo(MandelHandle h)
    {
      return h!=null && h.getHeader().isInfo();
    }

    static public boolean isInfo(Set<MandelHandle> set)
    {
      if (set.isEmpty()) return false;
      for (MandelHandle h:set) if (!h.getHeader().isInfo()) return false;
      return true;
    }

    static public boolean hasImageData(Set<MandelHandle> set)
    {
      for (MandelHandle h:set) if (h.getHeader().hasImageData()) return true;
      return false;
    }

    static public boolean isAreaColormap(Set<MandelHandle> set)
    {
      for (MandelHandle h:set) if (h.getHeader().isAreaColormap()) return true;
      return false;
    }
    
    static public boolean hasNone(Set<MandelHandle> set, Filter f)
    {
      for (MandelHandle h:set) if (f.filter(h.getHeader())) return false;
      return true;
    }

    static public boolean hasAtLeastOne(Set<MandelHandle> set, Filter f)
    {
      for (MandelHandle h:set) if (f.filter(h.getHeader())) return true;
      return false;
    }


    ///////////////////////////////////////////////////////////////////////

    static public Colormap getColormap(MandelScanner s, ColormapName n)
    {
      return getColormap(s.getColormap(n));
    }

    static public Colormap getColormap(ColormapHandle h)
    {
      if (h!=null) {
        try {
          return h.getColormap();
        }
        catch (IOException ex) {
          // just ignore and return null;
        }
      }
      return null;
    }

    ////////////////////////////////////////////////////////////////////////

  private static void addSubNames(Set<MandelName> set,
                                  String label, MandelName n,
                                  MandelScanner scan, MandelScanner.Filter f)
  {
   
    Set<MandelHandle> subs;
    MandelName s=n.subAt(label);
    while (s!=null) {
       //System.out.println("checking "+s);
       if (!(subs=scan.getMandelHandles(s)).isEmpty()) {
          if (f==null || hasAtLeastOne(subs, f)) {
            set.add(s);
          }
        }
        s=s.next();
      }
    
    /*
       Set<MandelName> subs;
       if (!(subs=scan.getMandelNames()).isEmpty()) {
         for (MandelName s: subs) {
           if (s.isChildOf(n)) {
              set.add(s);
           }
         }
       }
     */
  }

  static public Set<MandelName> getSubNames(MandelName n,
                                     MandelImageDBContext ctx, MandelScanner scan)
  {
    return getSubNames(n,ctx,scan,null);
  }

  static public Set<MandelName> getSubNames(MandelName n,
                                     MandelImageDBContext ctx, MandelScanner scan,
                                     MandelScanner.Filter f)
  {
    Set<MandelName> set=new HashSet<MandelName>();

    addSubNames(set, n.getLabel(),n,scan,f);
    if (ctx!=null) {
      if (n.isRoot()) {
        for (MandelImageDBContext.ContextMapping m:ctx.mappings()) {
          addSubNames(set, m.getLabel(), n, scan, f);
        }
      }
      else if(n.isRemoteName()) {
        ContextMapping sub=ctx.getContextMapping(n.getLabel());
        for (MandelImageDBContext subctx:sub.getContext().containers()) {
          addSubNames(set, ctx.getLabel(subctx), n, scan, f);
        }
      }
    }
    return set;
  }

  private static boolean hasSubNames(String label, MandelName n,
                              MandelScanner scan, MandelScanner.Filter f)
  {
    Set<MandelHandle> set;
    MandelName s=n.subAt(label);
    while (s!=null) {
       if (!(set=scan.getMandelHandles(s)).isEmpty()) {
         if (f==null || hasAtLeastOne(set, f)) {
           return true;
         }
       }
       s=s.next();
    }
    return false;
  }

  static public boolean hasSubNames(MandelName n,
                             MandelImageDBContext ctx, MandelScanner scan)
  {
    return hasSubNames(n,ctx,scan,null);
  }

  static public boolean hasSubNames(MandelName n,
                             MandelImageDBContext ctx, MandelScanner scan,
                             MandelScanner.Filter f)
  {
    if (hasSubNames(n.getLabel(),n,scan,f)) return true;
    if (ctx!=null) {
      if (n.isRoot()) {
        for (MandelImageDBContext.ContextMapping m:ctx.mappings()) {
          if (hasSubNames(m.getLabel(), n, scan, f)) return true;
        }
      }
      else if (n.isRemoteName()) {
        ContextMapping sub=ctx.getContextMapping(n.getLabel());
        for (MandelImageDBContext subctx:sub.getContext().containers()) {
          if (hasSubNames(ctx.getLabel(subctx), n, scan, f)) return true;
        }
      }
    }
    return false;
  }
}
