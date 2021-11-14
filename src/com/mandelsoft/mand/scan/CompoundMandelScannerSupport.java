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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandUtils;
import java.util.Collection;

/**
 *
 * @author Uwe Krueger
 */
public abstract class CompoundMandelScannerSupport extends MandelScannerSupport {
  protected List<MandelScanner> list=new ArrayList<MandelScanner>();
  private Listener              listener=new Listener();

  public CompoundMandelScannerSupport()
  {
  }

  synchronized
  public void startUpdate()
  {
    super.startUpdate();
    for (MandelScanner s : list) {
      if ( s instanceof MandelScannerTree) {
        ((MandelScannerTree)s).startUpdate();
      }
    }
  }
  
  synchronized
  public void finishUpdate()
  {
    for (MandelScanner s : list) {
      if ( s instanceof MandelScannerTree) {
        ((MandelScannerTree)s).finishUpdate();
      }
    }
    super.finishUpdate();
  }
  
  protected void addScanner(MandelScanner s)
  { 
    if (!list.contains(s)) {
      startUpdate(true);
      //System.out.println("   add scanner "+s);
      list.add(0,s);
      s.addMandelScannerListener(listener);
      finishUpdate();
    }
  }

  protected void removeScanner(MandelScanner s)
  { 
    if (list.contains(s)) {
      startUpdate(true);
      list.remove(s);
      s.removeMandelScannerListener(listener);
    }
  }

  public boolean hasScanners()
  { return !list.isEmpty();
  }

  ///////////////////////////////////////////////////////////////////////////
  // implementation support
  ///////////////////////////////////////////////////////////////////////////

  protected Set<MandelName> _getMandelNames(Collection<MandelScanner> scanners)
  {
    Set<MandelName> set=new HashSet<MandelName>();
    if (scanners==null) return set;
    if (!isFiltered()) {
      //System.out.println("unfiltered mandel names");
      for (MandelScanner s:scanners) {
        set.addAll(s.getMandelNames());
      }
    }
    else {
      //System.out.println("filtered mandel names");
      for (MandelScanner s:scanners) {
        for (MandelName n:s.getMandelNames()) {
          if (!set.contains(n)) {
            Set<MandelHandle> hs=s.getMandelHandles(n);
            if (hs!=null) for (MandelHandle h:hs) {
              if (filter(h.getHeader())) {
                set.add(n);
                break;
              }
            }
          }
        }
      }
    }
    return set;
  }

  protected Set<MandelHandle> _getMandelHandles(Collection<MandelScanner> scanners,
                                                MandelName name)
  { Set<MandelHandle> set=new HashSet<MandelHandle>();

    if (scanners!=null) for (MandelScanner s:scanners) {
      Set<MandelHandle> sub=s.getMandelHandles(name);
      //System.out.println("sub: "+s+": "+name+": "+set);
       add(set,sub);
    }
    //System.out.println("CS: "+name+": "+set);
    return set;
  }
  
  ///////////////////////////////////////////////////////////////////////////

  protected Set<ColormapName> _getColormapNames(Collection<MandelScanner> scanners)
  {
    Set<ColormapName> set=new HashSet<ColormapName>();
    if (scanners!=null && providesColormaps()) for (MandelScanner s:scanners) {
      set.addAll(s.getColormapNames());
    }
   return set;
  }

  protected Set<ColormapHandle> _getColormapHandles(Collection<MandelScanner> scanners,
                                                    ColormapName name)
  {
    Set<ColormapHandle> set=new HashSet<ColormapHandle>();
    if (scanners!=null && providesColormaps()) for (MandelScanner s:scanners) {
      Set<ColormapHandle> cm=s.getColormapHandles(name);
      set.addAll(cm);
    }
    return set;
  }

  ///////////////////////////////////////////////////////////////////////////
  
  protected MandelHandle _getMandelInfo(Collection<MandelScanner> scanners,
                                       MandelName name)
  {
    MandelHandle best=null;
    if (scanners!=null) for (MandelScanner s:scanners) {
      MandelHandle md=s.getMandelInfo(name);
      best=MandUtils.better(best, md);
    }
    return best;
  }

  protected MandelHandle _getMandelData(Collection<MandelScanner> scanners,
                                    MandelName name)
  {
    MandelHandle best=null;
    if (scanners!=null) for (MandelScanner s:scanners) {
      MandelHandle md=s.getMandelData(name);
      best=MandUtils.better(best, md);
    }
    return best;
  }

  ////////////////////////////////////////////////////////////////////////

  protected MandelHandle _getMandelHandle(Collection<MandelScanner> scanners,
                                      QualifiedMandelName name)
  {
    MandelHandle best=null;
    if (scanners!=null) for (MandelScanner s:scanners) {
      MandelHandle md=s.getMandelHandle(name);
      best=MandUtils.better(best, md);
    }
    return best;
  }

  protected MandelHandle _getMandelInfo(Collection<MandelScanner> scanners,
                                    QualifiedMandelName name)
  {
    MandelHandle best=null;
    if (scanners!=null) for (MandelScanner s:scanners) {
      MandelHandle md=s.getMandelInfo(name);
      best=MandUtils.better(best, md);
    }
    return best;
  }

  protected MandelHandle _getMandelData(Collection<MandelScanner> scanners,
                                    QualifiedMandelName name)
  {
    MandelHandle best=null;
    if (scanners!=null) for (MandelScanner s:scanners) {
      MandelHandle md=s.getMandelData(name);
      best=MandUtils.better(best, md);
    }
    return best;
  }

  protected boolean _hasColormap(Collection<MandelScanner> scanners,
                             ColormapName name)
  {
    if (scanners!=null && providesColormaps()) {
      for (MandelScanner s:scanners) {
        if (s.hasColormap(name)) return true;
      }
    }
    return false;
  }

  ///////////////////////////////////////////////////////////////////////////
  // mandel scanner interface 
  ///////////////////////////////////////////////////////////////////////////
  
  
  //////////////////////////////////////////////////////////////////////////
  // basic methods to be implemented for support class

  protected Set<MandelName> _getMandelNames()
  {
    return _getMandelNames(list);
  }

  protected Set<MandelHandle> _getMandelHandles(MandelName name)
  {
    return _getMandelHandles(list,name);
  }

  //////////////////////////////////////////////////////////////////////////

  protected Set<ColormapName> _getColormapNames()
  {
   return _getColormapNames(list);
  }

  protected Set<ColormapHandle> _getColormapHandles(ColormapName name)
  {
    return _getColormapHandles(list,name);
  }

  //////////////////////////////////////////////////////////////////////////
  // optimized access
  //////////////////////////////////////////////////////////////////////////

  //
  // avoid unneccessary copy of set
  //
  @Override
  synchronized
  public Set<MandelName> getMandelNames()
  {
    return _getMandelNames();
  }

  @Override
  synchronized
  public Set<MandelHandle> getMandelHandles(MandelName name)
  {
    return _getMandelHandles(name);
  }

  @Override
  synchronized
  public Set<ColormapName> getColormapNames()
  {
    return _getColormapNames();
  }

  @Override
  synchronized
  public Set<ColormapHandle> getColormapHandles(ColormapName name)
  {
    return _getColormapHandles(name);
  }

  //////////////////////////////////////////////////////////////////////////
  // optimized access

  //
  // better implementation
  //
  
  @Override
  public MandelHandle getMandelInfo(MandelName name)
  {
    return _getMandelInfo(list,name);
  }

  @Override
  public MandelHandle getMandelData(MandelName name)
  {
    return _getMandelData(list,name);
  }

  ////////////////////////////////////////////////////////////////////////
  
  @Override
  public MandelHandle getMandelHandle(QualifiedMandelName name)
  {
    return _getMandelHandle(list,name);
  }

  @Override
  public MandelHandle getMandelInfo(QualifiedMandelName name)
  {
    return _getMandelInfo(list,name);
  }

  @Override
  public MandelHandle getMandelData(QualifiedMandelName name)
  {
    return _getMandelData(list,name);
  }

  //
  // required for interface
  //

  @Override
  public boolean hasColormap(ColormapName name)
  {
    return _hasColormap(list,name);
  }

  /////////////////////////////////////////////////////////////////////////
  // general
  /////////////////////////////////////////////////////////////////////////

  synchronized
  public void rescan(boolean verbose)
  {

    if (debug) System.out.println("*** rescan compound scanner");
    //new Throwable().printStackTrace();
    startUpdate();
    for (MandelScanner s:list) {
      s.rescan(verbose);
    }
    finishUpdate();
  }

  /////////////////////////////////////////////////////////////////////////
  // utilitiies
  /////////////////////////////////////////////////////////////////////////

  protected void add(Set<MandelHandle> set, Set<MandelHandle> a)
  {
    if (isFiltered()) {
      for (MandelHandle h:a) {
        if (filter(h.getHeader())) set.add(h);
      }
    }
    else set.addAll(a);
  }
}
