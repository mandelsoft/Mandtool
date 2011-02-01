
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

import java.util.Collection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Uwe Krüger
 */

abstract public class ArrayBaseList<T> implements BaseList<T> {

  private ArrayList<T> list;

  public ArrayBaseList(Collection<? extends T> c)
  {
    list=new ArrayList(c);
  }

  public ArrayBaseList()
  {
    list=new ArrayList();
  }

  public ArrayBaseList(int initialCapacity)
  {
    list=new ArrayList(initialCapacity);
  }

  public void refresh(boolean soft)
  {
  }

  public void save() throws IOException
  {
  }

  public boolean valid()
  { return true;
  }

  ////////////////////////////////////////////////////////////////////////
  // delegate list interface

  // the identity of such a list is independent of its content
  // therefore we cannor derive from ArrayList!!!!

  @Override
  public String toString()
  {
    return list.toString();
  }

  public boolean retainAll(Collection<?> c)
  {
    return list.retainAll(c);
  }

  public boolean removeAll(Collection<?> c)
  {
    return list.removeAll(c);
  }

  public boolean containsAll(Collection<?> c)
  {
    return list.containsAll(c);
  }

  public List<T> subList(int fromIndex, int toIndex)
  {
    return list.subList(fromIndex, toIndex);
  }

  public ListIterator<T> listIterator(int index)
  {
    return list.listIterator(index);
  }

  public ListIterator<T> listIterator()
  {
    return list.listIterator();
  }

  public Iterator<T> iterator()
  {
    return list.iterator();
  }

  public void trimToSize()
  {
    list.trimToSize();
  }

  public <T> T[] toArray(T[] a)
  {
    return list.toArray(a);
  }

  public Object[] toArray()
  {
    return list.toArray();
  }

  public int size()
  {
    return list.size();
  }

  public T set(int index, T element)
  {
    return list.set(index, element);
  }

  public boolean remove(Object o)
  {
    return list.remove(o);
  }

  public T remove(int index)
  {
    return list.remove(index);
  }

  public int lastIndexOf(Object o)
  {
    return list.lastIndexOf(o);
  }

  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  public int indexOf(Object o)
  {
    return list.indexOf(o);
  }

  public T get(int index)
  {
    return list.get(index);
  }

  public void ensureCapacity(int minCapacity)
  {
    list.ensureCapacity(minCapacity);
  }

  public boolean contains(Object o)
  {
    return list.contains(o);
  }

  public void clear()
  {
    list.clear();
  }

  public boolean addAll(int index,
                        Collection<? extends T> c)
  {
    return list.addAll(index, c);
  }

  public boolean addAll(Collection<? extends T> c)
  {
    return list.addAll(c);
  }

  public void add(int index, T element)
  {
    list.add(index, element);
  }

  public boolean add(T e)
  {
    return list.add(e);
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    ArrayBaseList<T> c=(ArrayBaseList<T>)super.clone();
    c.list=(ArrayList<T>)list.clone();
    return c;
  }
}
