
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

import java.util.Set;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;

/**
 *
 * @author Uwe Krüger
 */

public class MandelScannerProxy extends MandelScannerListenerSupport {
  private MandelScanner scanner;
  private Listener      listener;

  public MandelScannerProxy(MandelScanner s)
  {
    listener=new Listener();
    scanner=s;
    s.addMandelScannerListener(listener);
  }

  public MandelScanner getScanner()
  {
    return scanner;
  }

  synchronized
  public void setFilter(Filter f)
  {
    scanner.setFilter(f);
  }

  synchronized
  public void rescan(boolean verbose)
  {
    startUpdate();
    scanner.rescan(verbose);
    finishUpdate();
  }

  public Set<ElementHandle<?>> getAllHandles()
  {
    return scanner.getAllHandles();
  }

  public Set<MandelHandle> getMandelHandles()
  {
    return scanner.getMandelHandles();
  }

  public Set<QualifiedMandelName> getQualifiedMandelNames(MandelName name)
  {
    return scanner.getQualifiedMandelNames(name);
  }

  public Set<QualifiedMandelName> getQualifiedMandelNames()
  {
    return scanner.getQualifiedMandelNames();
  }

  public Set<MandelName> getMandelNames()
  {
    return scanner.getMandelNames();
  }

  public Set<MandelHandle> getMandelHandles(QualifiedMandelName name)
  {
    return scanner.getMandelHandles(name);
  }

  public Set<MandelHandle> getMandelHandles(MandelName name)
  {
    return scanner.getMandelHandles(name);
  }

  public MandelHandle getMandelHandle(QualifiedMandelName name)
  {
    return scanner.getMandelHandle(name);
  }

  public MandelHandle getMandelInfo(QualifiedMandelName name)
  {
    return scanner.getMandelInfo(name);
  }

  public MandelHandle getMandelInfo(MandelName name)
  {
    return scanner.getMandelInfo(name);
  }

  public MandelHandle getMandelData(QualifiedMandelName name)
  {
    return scanner.getMandelData(name);
  }

  public MandelHandle getMandelData(MandelName name)
  {
    return scanner.getMandelData(name);
  }

  /////////////////////////////////////////////////////////////////////////

  public Filter getFilter()
  {
    return scanner.getFilter();
  }

  //////////////////////////////////////////////////////////////////////////

  public Set<ColormapName> getColormapNames()
  {
    return scanner.getColormapNames();
  }

  public boolean hasColormap(ColormapName name)
  {
    return scanner.hasColormap(name);
  }

  public Set<ColormapHandle> getColormapHandles(ColormapName name)
  {
    return scanner.getColormapHandles(name);
  }

  public ColormapHandle getColormap(ColormapName name)
  {
    return scanner.getColormap(name);
  }
}
