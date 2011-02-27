
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.io.FileAbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner.Filter;
import com.mandelsoft.mand.util.MandUtils;

/**
 *
 * @author Uwe Krüger
 */

/*
 * generally caching the relevant folder contents in memory
 */
public abstract class FolderMandelScannerSupport extends MandelScannerSupport {
  public static boolean debug=false;

  private Map<MandelName, Map<AbstractFile,MandelHandle>>    mandels;
  private Map<ColormapName,Map<AbstractFile,ColormapHandle>> colmaps;

  private Map<AbstractFile,ElementHandle<?>> files;
  

  protected FolderMandelScannerSupport()
  {
    this(MandelScanner.HAS_IMAGEDATA);
  }

  protected FolderMandelScannerSupport(Filter filter)
  { this(filter,true);
  }

  protected FolderMandelScannerSupport(Filter filter, boolean setup)
  { super(filter);
    mandels=new HashMap<MandelName,Map<AbstractFile,MandelHandle>>();
    colmaps=new HashMap<ColormapName,Map<AbstractFile,ColormapHandle>>();
    files=new HashMap<AbstractFile,ElementHandle<?>>();
  }

  //////////////////////////////////////////////////////////////////////////
  // basic methods to be implemented for MandelScannerSupport
  //////////////////////////////////////////////////////////////////////////

  protected Set<MandelName> _getMandelNames()
  {
    if (debug) System.out.println(this+" lookup mandel names ");
    return (mandels.keySet());
  }

  protected Set<MandelHandle> _getMandelHandles(MandelName name)
  {
    if (debug) System.out.println(this+" lookup headers "+name);
    return valueSet(mandels, name);
  }

  protected Set<ColormapName> _getColormapNames()
  {
    if (debug) System.out.println(this+" lookup colmaps");
    return (colmaps.keySet());
  }

  protected Set<ColormapHandle> _getColormapHandles(ColormapName name)
  {
    if (debug) System.out.println(this+" lookup "+name);
    return valueSet(colmaps, name);
  }

  ////////////////////////////////////////////////////////////////////////
  // optimized implementations
  ////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////
  // avoid unneccessary copy of set, beacuse it is already copied
  //
  @Override
  synchronized
  public Set<MandelHandle> getMandelHandles(MandelName name)
  {
    return _getMandelHandles(name);
  }

  @Override
  synchronized
  public Set<ColormapHandle> getColormapHandles(ColormapName name)
  {
    return _getColormapHandles(name);
  }

  //////////////////////////////////////////////////////////////////////////


  ////////////////////////////////////////////////////////////////////////
  // optimized implementation for support class

  @Override
  synchronized
  public MandelHandle getMandelInfo(MandelName name)
  {
    if (debug) System.out.println(this+" lookup info "+name);
    Map<AbstractFile, MandelHandle> sub=mandels.get(name);
    if (sub==null) return null;
    Set<AbstractFile> set=new HashSet<AbstractFile>(sub.keySet());
    while (!set.isEmpty()) {
      MandelHandle best=null;
      for (AbstractFile f:set) {
        MandelHandle h=sub.get(f);
        if (h.getHeader().hasInfo()) {
          best=MandUtils.better(best, h);
        }
      }
      if (best!=null) {
        try {
          return new CachedMandelHandle(best).assertInfo();
        }
        catch (IOException ex) {
          set.remove(best.getFile());
        }
      }
      else break;
    }
    return null;
  }

  @Override
  synchronized
  public MandelHandle getMandelData(MandelName name)
  {
    if (debug) System.out.println(this+" lookup data "+name);
    Map<AbstractFile, MandelHandle> sub=mandels.get(name);
    if (sub==null) return null;
    Set<AbstractFile> set=new HashSet<AbstractFile>(sub.keySet());
    while (!set.isEmpty()) {
      MandelHandle best=null;
      for (AbstractFile f:set) {
        MandelHandle h=sub.get(f);
        if (h.getHeader().hasInfo()) {
          best=MandUtils.better(best, h);
        }
      }
      if (best!=null) {
        try {
          return new CachedMandelHandle(best).assertData();
        }
        catch (IOException ex) {
          // just try next
          set.remove(best.getFile());
        }
      }
      else break;
    }
    return null;
  }

  //
  // utilities
  //
  protected void clear()
  {
    if (debug) System.out.println("clearing "+this);
    startUpdate(true);
    mandels.clear();
    files.clear();
    colmaps.clear();
    finishUpdate();
  }
  
  protected <K,H> Set<H> valueSet(Map<K, Map<AbstractFile,H>> map, K name)
  {
    Set<H> set;

    Map<AbstractFile,H> sub=map.get(name);
    if (sub==null) set=new HashSet<H>();
    else set=new HashSet<H>(sub.values());
    return set;
  }

  @Override
  synchronized
  public String toString()
  {
    String id=super.toString();
    int ix=id.lastIndexOf('@');
    if (ix>=0) id=id.substring(ix);
    else id="";

    return files.size()+"/"+mandels.size()+"/"+colmaps.size()+id;
  }

  protected void dump()
  {
    System.out.println(toString()+":");
    dump(mandels);
  }

  ////////////////////////////////////////////////////////////////////////////
  // generic map util

  protected <K,H> void dump(Map<K,Map<AbstractFile,H>> map)
  {
    for (K n: map.keySet()) {
      System.out.println("  "+n);
    }
  }

  protected <K,H extends ElementHandle<?>>
            void add(Map<K,Map<AbstractFile,H>> map, K name, H h)
  {
    Map<AbstractFile,H> sub=map.get(name);
    if (sub==null) {
      sub=new HashMap<AbstractFile,H>();
      map.put(name, sub);
    }
    sub.put(h.getFile(), h);
  }

  protected <K,H> H remove(Map<K,Map<AbstractFile,H>> map,
                           K name, AbstractFile f)
  {
    H h=null;
    Map<AbstractFile,H> sub=map.get(name);
    if (sub!=null) {
      h=sub.remove(f);
      if (sub.isEmpty()) {
        map.remove(name);
      }
      if (debug) {
        if (h==null) {
          if (debug) {
            System.out.println("    no sub entry found for "+f);
            for (AbstractFile n:sub.keySet()) {
              System.out.println("      "+n);
            }
          }
        }
      }
    }
    else {
      if (debug) {
        System.out.println("    no entry found for "+name);
        dump(map);
      }
    }
    return h;
  }


  ////////////////////////////////////////////////////////////////////////////
  // type specific  handling

  protected void add(MandelHandle h)
  {
    //System.out.println("  adding mandel "+h.getFile());
    add(mandels,h.getName().getMandelName(),h);
    files.put(h.getFile(), h);
    notifyAddMandelFile(h);
  }

  protected void add(ColormapHandle h)
  {
    //System.out.println("  adding colormap "+h.getFile());
    add(colmaps,h.getName(),h);
    notifyAddColormap(h);
  }

  protected MandelHandle create(AbstractFile f, QualifiedMandelName n,
                                MandelHeader h)
  {
    return new DefaultMandelHandle(f,n,h);
  }

  protected ColormapHandle create(AbstractFile f, ColormapName n,
                                MandelHeader h)
  {
    return new DefaultColormapHandle(f,n,h);
  }

  ////////////////////////////////////////////////////////////////////////////
  // file handling

  protected void add(File f)
  {
    try {
      String base=f.getName();
      int ix=base.lastIndexOf('.');
      if (ix>0) base=base.substring(0,ix);
      MandelFileName n=null;
      boolean namedone=false;
      if (!providesColormaps()) {
        namedone=true;
        n=MandelFileName.create(f);
      }
      try {
        MandelHeader h=MandelHeader.getHeader(f);
        AbstractFile af=new FileAbstractFile(f);

        //System.out.println("    "+f.getMandelName()+" is mandel "+h+"("+h.getType()+")");
        if (filter(h)) {
          if (h.isColormap()) {
            add(create(af,new ColormapName(base),h));
          }
          else {
            if (n==null) {
              if (namedone) return;
              n=MandelFileName.create(f);
            }
            add(create(af,n.getQualifiedName(),h));
          }
          //System.out.println("      used");

        }
      }
      catch (IOException io) {
        // System.out.println("    no mandel: "+f);
      }
    }
    catch (IllegalArgumentException ex) {
      //ignore
    }
  }

  protected void remove(AbstractFile f)
  { Map<AbstractFile,MandelHeader> m;
    MandelHandle mh;
    ColormapHandle ch;
    
    try {
      String base=f.getName();
      int ix=base.lastIndexOf('.');
      if (ix>0) base=base.substring(0,ix);
      MandelFileName n=MandelFileName.create(f);
      if (debug) System.out.println(toString()+": remove mandel file "+f+": "+n);
      if (files.remove(f)==null) {
        if (debug) System.out.println("  file not registered");
      }
      if (n!=null) {
        mh=remove(mandels,n.getQualifiedName().getMandelName(),f);
        if (mh!=null) {
          notifyRemoveMandelFile(mh);
        }
        else {
          if (debug) System.out.println("  header not registered");
        }
      }
      ColormapName cn=new ColormapName(base);
      ch=remove(colmaps, cn, f);
      if (ch!=null) {
        notifyRemoveColormap(ch);
      }
      else {
        if (debug) System.out.println("  colormap not registerd");
      }
    }
    catch (IllegalArgumentException ex) {
      //ignore
    }
  }
}
