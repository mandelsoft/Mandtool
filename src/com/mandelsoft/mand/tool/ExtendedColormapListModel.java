
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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.ColormapName;
import java.io.IOException;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.util.ColormapList;
import com.mandelsoft.mand.util.MemoryColormapList;

/**
 *
 * @author Uwe Krüger
 */

public class ExtendedColormapListModel extends DefaultColormapListModel {
  private ColormapList maps;

  public ExtendedColormapListModel(ColormapList list)
  {
    super(new MemoryColormapList());
    maps=list;
  }

  @Override
  public void refresh()
  {
    maps.refresh();
    this.fireTableDataChanged();
  }

  @Override
  public boolean remove(int index)
  {
    if (index>=maps.size()) {
      return super.remove(index-maps.size());
    }
    return false;
  }

  @Override
  public ColormapHandle getColormapHandle(int index) throws IOException
  {
    if (index>=maps.size()) {
      return super.getColormapHandle(index-maps.size());
    }
    return maps.getColormapHandle(index);
  }

  @Override
  public ColormapName getName(int index)
  {
    if (index>=maps.size()) {
      return super.getName(index-maps.size());
    }
    return maps.getName(index);
  }

  @Override
  public Colormap getColormap(int index) throws IOException
  {
    if (index>=maps.size()) {
      return super.getColormap(index-maps.size());
    }
    return maps.get(index);
  }

  @Override
  public int getRowCount()
  {
    return maps.size()+super.getRowCount();
  }
}