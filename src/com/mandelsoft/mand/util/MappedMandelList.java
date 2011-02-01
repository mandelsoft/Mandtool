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

import com.mandelsoft.mand.ElementNameMapper;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.util.MappedIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Uwe Krueger
 */
public class MappedMandelList extends ProxyMandelList {
  private ElementNameMapper mapper;

  public MappedMandelList(MandelList list, ElementNameMapper mapper)
  {
    super(list);
    this.mapper=mapper;
  }

  ///////////////////////////////////////////////////////////////////////////
  // limited functionality for add
  ///////////////////////////////////////////////////////////////////////////

  /*
   * adding an element not avalable in sub context leads to exception!
   */

  @Override
  public void add(int index, QualifiedMandelName element)
  {
    super.add(index, mapper.mapIn(element));
  }

  @Override
  public boolean add(QualifiedMandelName e)
  {
    return super.add(mapper.mapIn(e));
  }

  @Override
  public QualifiedMandelName set(int index, QualifiedMandelName element)
  {
    return super.set(index, mapper.mapIn(element));
  }

  private <T> Collection<T> mapIn(Collection<T> set)
  {
    List<T> nset=new ArrayList<T>();
    boolean mapped=false;
    for (T h:set) {
      QualifiedMandelName n=mapper.mapIn((QualifiedMandelName)h);
      nset.add((T)n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  @Override
  public boolean addAll(int index,
                        Collection<? extends QualifiedMandelName> c)
  {
    return super.addAll(index, mapIn(c));
  }

  @Override
  public boolean addAll(Collection<? extends QualifiedMandelName> c)
  {
    return super.addAll(mapIn(c));
  }

  //////////////////////////////////////////////////////////////////////////
  // regular
  //////////////////////////////////////////////////////////////////////////

  private Collection<?> mapFilteredIn(Collection<?> set)
  {
    List<QualifiedMandelName> nset=new ArrayList<QualifiedMandelName>();
    boolean mapped=false;
    for (Object o:set) {
      if (o instanceof QualifiedMandelName) {
        try {
          QualifiedMandelName n=mapper.mapIn((QualifiedMandelName)o);
          nset.add(n);
          mapped|=(n!=o);
        }
        catch (IllegalArgumentException ex) {
          mapped=true;
        }
      }
      else {
        mapped=true;
      }
    }
    return mapped?nset:set;
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    return super.removeAll(mapFilteredIn(c));
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    return super.retainAll(mapFilteredIn(c));
  }

  /////////////////////////////////////////////////////////////////////////

  private List<QualifiedMandelName>
          mapOut(List<QualifiedMandelName> set)
  {
    List<QualifiedMandelName> nset=new ArrayList<QualifiedMandelName>();
    boolean mapped=false;
    for (QualifiedMandelName h:set) {
      QualifiedMandelName n=mapper.mapOut(h);
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  private <T> T[] mapOut(T[] array)
  {
    for (int i=0; i<array.length; i++) {
      array[i]=(T)mapper.mapOut((QualifiedMandelName)array[i]);
    }
    return array;
  }

  @Override
  public boolean contains(Object o)
  {
    if (o instanceof QualifiedMandelName) {
      return super.contains(mapper.mapIn((QualifiedMandelName)o));
    }
    return false;
  }

  @Override
  public QualifiedMandelName get(int index)
  {
    QualifiedMandelName n=super.get(index);
    if (n==null) return null;
    return mapper.mapOut(n);
  }

  @Override
  public int indexOf(Object o)
  {
    if (o instanceof QualifiedMandelName) {
      return super.indexOf(mapper.mapIn((QualifiedMandelName)o));
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o)
  {
    if (o instanceof QualifiedMandelName) {
      return super.lastIndexOf(mapper.mapIn((QualifiedMandelName)o));
    }
    return -1;
  }

  @Override
  public boolean remove(Object o)
  {
    if (o instanceof QualifiedMandelName) {
      return super.remove(mapper.mapIn((QualifiedMandelName)o));
    }
    return false;
  }

  @Override
  public List<QualifiedMandelName> subList(int fromIndex, int toIndex)
  {
    return mapOut(super.subList(fromIndex, toIndex));
  }

  @Override
  public <T> T[] toArray(T[] a)
  {
    return mapOut(super.toArray(a));
  }

  @Override
  public Object[] toArray()
  {
    return mapOut(super.toArray());
  }

  @Override
  public boolean containsAll(Collection<?> c)
  {
    try {
      c=mapIn(c);
    }
    catch (IllegalArgumentException ex) {
      // not mappable name -> cannot be contained
      return false;
    }
    catch (ClassCastException ex) {
      // non-QualifiedMandelName entry -> cannot be contained
      return false;
    }
    return super.containsAll(c);
  }

  ////////////////////////////////////////////////////////////////////////////

  private class MappedListIterator implements ListIterator<QualifiedMandelName> {
    private ListIterator<QualifiedMandelName> it;

    public MappedListIterator(ListIterator<QualifiedMandelName> it)
    {
      this.it=it;
    }

    public boolean hasNext()
    {
      return it.hasNext();
    }

    public QualifiedMandelName next()
    {
      QualifiedMandelName n=it.next();
      if (n==null) return n;
      return mapper.mapOut(n);
    }

    public boolean hasPrevious()
    {
      return it.hasPrevious();
    }

    public QualifiedMandelName previous()
    {
      QualifiedMandelName n=it.previous();
      if (n==null) return n;
      return mapper.mapOut(n);
    }

    public int nextIndex()
    {
      return it.nextIndex();
    }

    public int previousIndex()
    {
      return it.previousIndex();
    }

    public void remove()
    {
      it.remove();
    }

    public void set(QualifiedMandelName e)
    {
      it.set(mapper.mapIn(e));
    }

    public void add(QualifiedMandelName e)
    {
      it.add(mapper.mapIn(e));
    }
  }

  @Override
  public Iterator<QualifiedMandelName> iterator()
  {
    return new MappedIterator<QualifiedMandelName,QualifiedMandelName>(super.iterator()) {
      @Override
      protected QualifiedMandelName map(QualifiedMandelName elem)
      {
        return mapper.mapOut(elem);
      }
    };
  }

  @Override
  public ListIterator<QualifiedMandelName> listIterator(int index)
  {
    return new MappedListIterator(super.listIterator(index));
  }

  @Override
  public ListIterator<QualifiedMandelName> listIterator()
  {
    return new MappedListIterator(super.listIterator());
  }
}
