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
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Uwe Krueger
 */
public class ProxyColormapList implements ColormapList {
  private ColormapList list;

  public ProxyColormapList(ColormapList list)
  {
    this.list=list;
  }

  public boolean valid()
  {
    return list.valid();
  }

  public int size()
  {
    return list.size();
  }

  public void save() throws IOException
  {
    list.save();
  }

  public ColormapName remove(int index)
  {
    return list.remove(index);
  }

  public boolean remove(ColormapName name)
  {
    return list.remove(name);
  }

  public void refresh()
  {
    list.refresh();
  }

  public Iterator<ColormapName> iterator()
  {
    return list.iterator();
  }

  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  public int indexOf(Colormap cm)
  {
    return list.indexOf(cm);
  }

  public int indexOf(ColormapName name)
  {
    return list.indexOf(name);
  }

  public ColormapName getName(int index)
  {
    return list.getName(index);
  }

  public ColormapHandle getColormapHandle(int index) throws IOException
  {
    return list.getColormapHandle(index);
  }

  public ColormapHandle getColormapHandle(ColormapName name) throws IOException
  {
    return list.getColormapHandle(name);
  }

  public Colormap get(int index) throws IOException
  {
    return list.get(index);
  }

  public Colormap get(ColormapName name) throws IOException
  {
    return list.get(name);
  }

  public boolean contains(Colormap sm)
  {
    return list.contains(sm);
  }

  public boolean contains(ColormapName name)
  {
    return list.contains(name);
  }

  public void clear()
  {
    list.clear();
  }

  public boolean add(int index, ColormapName name, Colormap cm)
  {
    return list.add(index, name, cm);
  }

  public boolean add(ColormapName name, Colormap cm)
  {
    return list.add(name, cm);
  }

  public boolean add(ColormapName n, Colormap cm, ColormapHandle h)
  {
    return list.add(n, cm, h);
  }

  public boolean add(int index, ColormapName name, Colormap cm, ColormapHandle h)
  {
    return list.add(index, name, cm, h);
  }
}
