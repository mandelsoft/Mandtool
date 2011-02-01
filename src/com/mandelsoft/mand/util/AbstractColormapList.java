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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;

/**
 *
 * @author Uwe Krüger
 */

public abstract class AbstractColormapList implements ColormapList {
  protected List<ColormapName> list;

  public AbstractColormapList()
  {
    list=new ArrayList<ColormapName>();
  }

  public void clear()
  {
    list.clear();
  }

  public int size()
  {
    return list.size();
  }

  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  public boolean valid()
  {
    return true;
  }

  public boolean remove(ColormapName name)
  {
    if (_remove(name)) {
      return list.remove(name);
    }
    return false;
  }

  public ColormapName remove(int index)
  {
    ColormapName name=getName(index);
    if (name!=null) {
      if (_remove(name)) {
        return list.remove(index);
      }
    }
    return null;
  }

  abstract protected boolean _remove(ColormapName name);

  public boolean add(ColormapName name, Colormap cm)
  {
    if (list.contains(name)) return false;
    if (_add(name,cm,null)) {
      list.add(name);
      return true;
    }
    return false;
  }

  public boolean add(ColormapName name, Colormap cm, ColormapHandle h)
  {
    if (list.contains(name)) return false;
    if (_add(name,cm,h)) {
      list.add(name);
      return true;
    }
    return false;
  }

  public boolean add(int index, ColormapName name, Colormap cm)
  {
    if (list.contains(name)) return false;
    if (_add(name,cm,null)) {
      list.add(index,name);
      return true;
    }
    return false;
  }

  public boolean add(int index, ColormapName name, Colormap cm, ColormapHandle h)
  {
    if (list.contains(name)) return false;
    if (_add(name,cm,h)) {
      list.add(index,name);
      return true;
    }
    return false;
  }
  
  abstract protected boolean _add(ColormapName name, Colormap cm, ColormapHandle h);

  public ColormapName getName(int index)
  {
    return list.get(index);
  }

  public Colormap get(int index) throws IOException
  {
    ColormapName name=list.get(index);
    if (name==null) return null;
    return get(name);
  }

  public ColormapHandle getColormapHandle(int index) throws IOException
  {
    ColormapName name=list.get(index);
    if (name==null) return null;
    return getColormapHandle(name);
  }


  public int indexOf(ColormapName name)
  {
    return list.indexOf(name);
  }

  public int indexOf(Colormap cm)
  {
    int i=0;
    for (ColormapName n:list) {
      if (cm.equals(cm)) return i;
      i++;
    }
    return -1;
  }

  public boolean contains(ColormapName name)
  {
    return list.contains(name);
  }

  public boolean contains(Colormap cm)
  {
    for (ColormapName n:list) {
      if (cm.equals(cm)) return true;
    }
    return false;
  }

  public Iterator<ColormapName> iterator()
  {
    return list.iterator();
  }



}
