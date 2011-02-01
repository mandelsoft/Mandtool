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
import com.mandelsoft.mand.ElementNameMapper;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.scan.ProxyColormapHandle;
import com.mandelsoft.util.MappedIterator;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Uwe Krueger
 */
public class MappedColormapList extends ProxyColormapList {
  private ElementNameMapper mapper;

  public MappedColormapList(ColormapList list, ElementNameMapper mapper)
  {
    super(list);
    this.mapper=mapper;
  }

  private ColormapHandle mapOut(ColormapHandle h)
  {
    ColormapName n=mapper.mapOut(h.getName());
    if (n==h.getName()) return h;
    return new ProxyColormapHandle(h,n);
  }

  private ColormapHandle mapIn(ColormapHandle h)
  {
    ColormapName n=mapper.mapIn(h.getName());
    if (n==h.getName()) return h;
    if (h instanceof ProxyColormapHandle) {
      ColormapHandle o=((ProxyColormapHandle)h).getOrig();
      if (o.getName().equals(n)) {
        return o;
      }
    }
    return new ProxyColormapHandle(h,n);
  }

  @Override
  public boolean add(int index, ColormapName name, Colormap cm)
  {
    return super.add(index, mapper.mapIn(name), cm);
  }

  @Override
  public boolean add(ColormapName name, Colormap cm)
  {
    return super.add(mapper.mapIn(name), cm);
  }

  @Override
  public boolean add(ColormapName n, Colormap cm, ColormapHandle h)
  {
    return super.add(mapper.mapIn(n), cm, mapIn(h));
  }

  @Override
  public boolean add(int index, ColormapName name, Colormap cm, ColormapHandle h)
  {
    return super.add(index, mapper.mapIn(name), cm, mapIn(h));
  }

  @Override
  public boolean contains(ColormapName name)
  {
    return super.contains(mapper.mapIn(name));
  }

  @Override
  public int indexOf(ColormapName name)
  {
    return super.indexOf(mapper.mapIn(name));
  }

  @Override
  public boolean remove(ColormapName name)
  {
    return super.remove(mapper.mapIn(name));
  }

  @Override
  public Colormap get(ColormapName name) throws IOException
  {
    return super.get(mapper.mapIn(name));
  }

  @Override
  public ColormapName getName(int index)
  {
    ColormapName n=super.getName(index);
    if (n==null) return null;
    return mapper.mapOut(n);
  }

  @Override
  public ColormapName remove(int index)
  {
    ColormapName n=super.remove(index);
    if (n==null) return null;
    return mapper.mapOut(n);
  }

  @Override
  public ColormapHandle getColormapHandle(ColormapName name) throws IOException
  {
    return mapOut(super.getColormapHandle(mapper.mapIn(name)));
  }

  @Override
  public Iterator<ColormapName> iterator()
  {
    return new MappedIterator<ColormapName,ColormapName>(super.iterator()) {
      @Override
      protected ColormapName map(ColormapName elem)
      {
        return mapper.mapOut(elem);
      }
    };
  }
}
