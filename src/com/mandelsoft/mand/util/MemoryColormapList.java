
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
import java.util.HashMap;
import java.util.Map;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.scan.DefaultColormapHandle;

/**
 *
 * @author Uwe Krüger
 */

public class MemoryColormapList extends AbstractColormapList  {

  private Map<ColormapName,Colormap> maps;
  private Map<ColormapName,ColormapHandle> files;

  public MemoryColormapList()
  {
    maps=new HashMap<ColormapName,Colormap>();
    files=new HashMap<ColormapName,ColormapHandle>();
  }

  public void refresh()
  {
  }

  public void save() throws IOException
  {
  }

  @Override
  public void clear()
  {
    super.clear();
    maps.clear();
    files.clear();
  }

  ////////////////////
  // abstract list

  public Colormap get(ColormapName name) throws IOException
  {
    return maps.get(name);
  }

  public ColormapHandle getColormapHandle(ColormapName name) throws IOException
  {
    if (contains(name)) {
      ColormapHandle h=files.get(name);
      if (h==null) {
        h=new DefaultColormapHandle(null,name,new MandelHeader(MandelHeader.C_COLMAP));
        files.put(name,h);
      }
      return h;
    }
    return null;
  }

  @Override
  protected boolean _add(ColormapName name, Colormap cm, ColormapHandle h)
  {
    maps.put(name,cm);
    if (h!=null) files.put(name,h);
    return true;
  }

  @Override
  protected boolean _remove(ColormapName name)
  {
    maps.remove(name);
    files.remove(name);
    return true;
  }
}
