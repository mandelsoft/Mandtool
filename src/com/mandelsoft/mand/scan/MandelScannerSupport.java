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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelScannerSupport extends MandelScannerListenerSupport {
  static public boolean debug=false;
  static final public MandelHeader colmapHeader=
          new MandelHeader(MandelData.C_COLMAP);

  private Filter  filter;
  private boolean colmaps=true;

  protected MandelScannerSupport()
  {
  }

  protected MandelScannerSupport(Filter filter)
  { 
    this.filter=filter;
    colmaps=filter(colmapHeader);
  }

  synchronized
  public void setFilter(Filter filter)
  {
    this.filter=filter;
    colmaps=filter(colmapHeader);
    rescan(false);
  }

  synchronized
  public Filter getFilter()
  { return filter;
  }

  //////////////////////////////////////////////////////////////////////////
  // utilities for basic methods
  //////////////////////////////////////////////////////////////////////////

  @Override
  protected boolean filter(MandelHeader h)
  {
    if (filter==null) return true;
    return filter.filter(h);
  }

  protected boolean isFiltered()
  {
    return filter!=null;
  }

  protected boolean providesColormaps()
  {
    return colmaps;
  }

  //////////////////////////////////////////////////////////////////////////
  // basic methods to be implemented
  //////////////////////////////////////////////////////////////////////////

  // may return internal and/or unsynchronized sets
  // will be copied for external versions

  abstract protected Set<MandelName> _getMandelNames();
  abstract protected Set<MandelHandle> _getMandelHandles(MandelName name);
  abstract protected Set<ColormapName> _getColormapNames();
  abstract protected Set<ColormapHandle> _getColormapHandles(ColormapName name);
  abstract public void rescan(boolean verbose);

  //////////////////////////////////////////////////////////////////////////
  // basic methods extenal view

  public Set<MandelName> getMandelNames()
  {
    return new HashSet<MandelName>(_getMandelNames());
  }

  public Set<MandelHandle> getMandelHandles(MandelName name)
  {
    return new HashSet<MandelHandle>(_getMandelHandles(name));
  }

  public Set<ColormapName> getColormapNames()
  {
    return new HashSet<ColormapName>(_getColormapNames());
  }

  public Set<ColormapHandle> getColormapHandles(ColormapName name)
  {
    return new HashSet<ColormapHandle>(_getColormapHandles(name));
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////
  // default implementations of the rest
  //////////////////////////////////////////////////////////////////////////

  synchronized
  public Set<ElementHandle<?>> getAllHandles()
  { Set<ElementHandle<?>> set=new HashSet<ElementHandle<?>>();

    for (MandelName n:getMandelNames()) {
      set.addAll(getMandelHandles(n));
    }
    for (ColormapName n:getColormapNames()) {
      set.addAll(getColormapHandles(n));
    }
    return set;
  }

  synchronized
  public Set<MandelHandle> getMandelHandles()
  { Set<MandelHandle> set=new HashSet<MandelHandle>();

    for (MandelName n:getMandelNames()) {
      set.addAll(getMandelHandles(n));
    }
    return set;
  }

  //////////////////////////////////////////////////////////////////////////
  // mandel versions
  //////////////////////////////////////////////////////////////////////////

  synchronized
  public Set<QualifiedMandelName> getQualifiedMandelNames()
  { Set<QualifiedMandelName> set=new HashSet<QualifiedMandelName>();

    for (MandelName n:getMandelNames()) {
      for (MandelHandle h:getMandelHandles(n)) {
        set.add(h.getName());
      }
    }
    return set;
  }


  //////////////////////////////////////////////////////////////////////////
  // mandel name versions

  synchronized
  public MandelHandle getMandelInfo(MandelName name)
  {
    MandelHandle best=null;
    for (MandelHandle h:getMandelHandles(name)) {
      if (h.getHeader().hasInfo()&&h==MandUtils.better(best, h)) {
        try {
          best=new CachedMandelHandle(h).assertInfo();
        }
        catch (IOException ex) {
          System.out.println("cannot read "+h.getFile()+": "+ex);
        }
      }
    }
    return best;
  }

  synchronized
  public MandelHandle getMandelData(MandelName name)
  {
    MandelHandle best=null;
    for (MandelHandle h:getMandelHandles(name)) {
      if (h==MandUtils.better(best, h)) {
        try {
          best=new CachedMandelHandle(h).assertData();
        }
        catch (IOException ex) {
          System.out.println("cannot read "+h.getFile()+": "+ex);
        }
      }
    }
    return best;
  }

  /////////////////////////////////////////////////////////////////////////
  // qualified mandel name versions

  public Set<QualifiedMandelName> getQualifiedMandelNames(MandelName name)
  {
    Set<QualifiedMandelName> set=new HashSet<QualifiedMandelName>();
    Set<MandelHandle> orig = _getMandelHandles(name);
    if (orig!=null) {
      for (MandelHandle h : orig) {
        set.add(h.getName());
      }
    }
    return set;
  }

  public Set<MandelHandle> getMandelHandles(QualifiedMandelName name)
  {
    Set<MandelHandle> set=new HashSet<MandelHandle>();
    Set<MandelHandle> orig = _getMandelHandles(name.getMandelName());
    if (orig != null) {
      for (MandelHandle h : orig) {
        if (Utils.equals(h.getQualifier(), name.getQualifier())) {
          set.add(h);
        }
      }
    }
    return set;
  }

  synchronized
  public MandelHandle getMandelHandle(QualifiedMandelName name)
  {
    MandelHandle best=null;
    for (MandelHandle h:getMandelHandles(name)) {
      if (h.getHeader().hasInfo()) {
        best=MandUtils.better(best, h);
      }
    }
    return best;
  }

  synchronized
  public MandelHandle getMandelInfo(QualifiedMandelName name)
  {
    Set<MandelHandle> set=getMandelHandles(name);
    while (!set.isEmpty()) {
      MandelHandle best=null;
      for (MandelHandle h:set) {
        if (h.getHeader().hasInfo()) {
          best=MandUtils.better(best, h);
        }
      }

      if (best!=null) {
        try {
          return new CachedMandelHandle(best).assertInfo();
        }
        catch (IOException ex) {
          // just try next
        }
        set.remove(best);
      }
      else break;
    }
    return null;
  }

  synchronized
  public MandelHandle getMandelData(QualifiedMandelName name)
  {
    Set<MandelHandle> set=getMandelHandles(name);
    while (!set.isEmpty()) {
      MandelHandle best=null;
      for (MandelHandle h:set) {
        best=MandUtils.better(best, h);
      }

      if (best!=null) {
        try {
          return new CachedMandelHandle(best).assertData();
        }
        catch (IOException ex) {
          // just try next
        }
        set.remove(best);
      }
      else break;
    }
    return null;
  }

  /////////////////////////////////////////////////////////////////////////
  // colormap versions
  /////////////////////////////////////////////////////////////////////////

  public ColormapHandle getColormap(ColormapName name)
  {
    Set<ColormapHandle> set=getColormapHandles(name);
    if (!set.isEmpty()) {
      for (ColormapHandle h:set) {
        try {
          return new CachedColormapHandle(h).assertData();
        }
        catch (IOException ex) {
          // just try next
        }
      }
    }
    return null;
  }

  public boolean hasColormap(ColormapName name)
  {
    return !_getColormapHandles(name).isEmpty();
  }
}
