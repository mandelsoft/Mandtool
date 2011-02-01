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
import java.util.HashSet;
import java.util.Set;
import com.mandelsoft.mand.MandelName;

/**
 *
 * @author Uwe Krueger
 */
public class FilteredMandelScanner extends MandelScannerSupport {
  private MandelScanner scanner;
  protected Listener    listener;
  
  public FilteredMandelScanner(MandelScanner scanner, Filter filter)
  { super(filter);
    this.listener=new Listener();
    this.scanner=scanner;
    scanner.addMandelScannerListener(listener);
  }

  //////////////////////////////////////////////////////////////////////////
  // basic methods to be implemented
  //////////////////////////////////////////////////////////////////////////

  @Override
  protected Set<MandelName> _getMandelNames()
  {
    Set<MandelName> set=new HashSet<MandelName>();

    for (MandelName n:scanner.getMandelNames()) {
      for (MandelHandle h:scanner.getMandelHandles(n)) {
        if (filter(h.getHeader())) {
          set.add(n);
          break;
        }
      }
    }
    return set;
  }

  protected Set<MandelHandle> _getMandelHandles(MandelName name)
  { Set<MandelHandle> set=new HashSet<MandelHandle>();

    //System.out.println("get headers for "+name);
    for (MandelHandle h:scanner.getMandelHandles(name)) {
      if (filter(h.getHeader())) set.add(h);
    }
    return set;
  }

  protected Set<ColormapName> _getColormapNames()
  {
    if (providesColormaps()) return scanner.getColormapNames();
    return new HashSet<ColormapName>();
  }

  protected Set<ColormapHandle> _getColormapHandles(ColormapName name)
  {
    if (providesColormaps()) return scanner.getColormapHandles(name);
    return new HashSet<ColormapHandle>();
  }

  ////////////////////////////////////////////////////////////////////////
  // optimized implementation for support class
  ////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////
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

  ////////////////////////////////////////////////////////////////////////
  // general
  ////////////////////////////////////////////////////////////////////////

  synchronized
  public void rescan(boolean verbose)
  {
    startUpdate();
    scanner.rescan(verbose);
    finishUpdate();
  }

  ////////////////////////////////////////////////////////////////////////

}
