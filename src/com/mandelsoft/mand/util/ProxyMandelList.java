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

import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelList.IO;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Uwe Krueger
 */
public class ProxyMandelList implements MandelList {
  private MandelList list;

  public ProxyMandelList(MandelList list)
  {
    this.list=list;
  }

  ///////////////////////////////////////////////////////////////////////////

  public void write(OutputStream os, String dst) throws IOException
  {
    IO.write(this, os, dst);
  }

  public void read(InputStream is, String src) throws IOException
  {
    IO.read(this, is, src);
  }

  public QualifiedMandelName get(MandelName n)
  {
    return list.get(n);
  }

  ///////////////////////////////////////////////////////////////////////////

  public <T> T[] toArray(T[] a)
  {
    return list.toArray(a);
  }

  public Object[] toArray()
  {
    return list.toArray();
  }

  public List<QualifiedMandelName> subList(int fromIndex, int toIndex)
  {
    return list.subList(fromIndex, toIndex);
  }

  public int size()
  {
    return list.size();
  }

  public QualifiedMandelName set(int index, QualifiedMandelName element)
  {
    return list.set(index, element);
  }

  public boolean retainAll(Collection<?> c)
  {
    return list.retainAll(c);
  }

  public boolean removeAll(Collection<?> c)
  {
    return list.removeAll(c);
  }

  public QualifiedMandelName remove(int index)
  {
    return list.remove(index);
  }

  public boolean remove(Object o)
  {
    return list.remove(o);
  }

  public ListIterator<QualifiedMandelName> listIterator(int index)
  {
    return list.listIterator(index);
  }

  public ListIterator<QualifiedMandelName> listIterator()
  {
    return list.listIterator();
  }

  public int lastIndexOf(Object o)
  {
    return list.lastIndexOf(o);
  }

  public Iterator<QualifiedMandelName> iterator()
  {
    return list.iterator();
  }

  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  public int indexOf(Object o)
  {
    return list.indexOf(o);
  }

  public QualifiedMandelName get(int index)
  {
    return list.get(index);
  }

  public boolean containsAll(Collection<?> c)
  {
    return list.containsAll(c);
  }

  public boolean contains(Object o)
  {
    return list.contains(o);
  }

  public boolean addAll(int index,
                        Collection<? extends QualifiedMandelName> c)
  {
    return list.addAll(index, c);
  }

  public boolean addAll(Collection<? extends QualifiedMandelName> c)
  {
    return list.addAll(c);
  }

  public void add(int index, QualifiedMandelName element)
  {
    list.add(index, element);
  }

  public boolean add(QualifiedMandelName e)
  {
    return list.add(e);
  }

  public boolean valid()
  {
    return list.valid();
  }

  public void save() throws IOException
  {
    list.save();
  }

  public void refresh(boolean soft)
  {
    list.refresh(soft);
  }

  public void clear()
  {
    list.clear();
  }

}
