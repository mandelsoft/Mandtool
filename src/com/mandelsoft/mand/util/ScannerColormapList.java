
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
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.ColormapName;
import java.io.IOException;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerUtils;
import java.util.Set;

/**
 *
 * @author Uwe Krüger
 */

public class ScannerColormapList extends AbstractColormapList {

  protected MandelScanner scanner;

  public ScannerColormapList(MandelScanner scanner)
  {
    this.scanner=scanner;
    _setColormaps();
  }

  private void _setColormaps()
  {
    System.out.println("setting colormaps...");
    list.clear();
    list.addAll(getColormapNames());
    System.out.println("found "+size()+" colormaps");
  }

  protected Set<ColormapName> getColormapNames()
  {
    return scanner.getColormapNames();
  }
  
  public void refresh()
  {
    scanner.rescan(false);
    _setColormaps();
  }

  public void save() throws IOException
  {
  }

  public Colormap get(ColormapName name) throws IOException
  {
    return MandelScannerUtils.getColormap(getColormapHandle(name));
  }

  public ColormapHandle getColormapHandle(ColormapName name) throws IOException
  {
    return scanner.getColormap(name);
  }

  ///////////////////
  // abstract list

  @Override
  protected boolean _add(ColormapName name, Colormap cm, ColormapHandle h)
  {
    return true;
  }

  @Override
  protected boolean _remove(ColormapName name)
  {
    return true;
  }
}
