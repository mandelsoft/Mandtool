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
package com.mandelsoft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Uwe Krüger
 */

public class Queue<T> {
  private List<T> list;

  public Queue()
  {
    list=new ArrayList<T>();
  }

  synchronized
  public T pull() throws InterruptedException
  {
    while (list.isEmpty()) wait();
    T o=list.get(0);
    list.remove(0);
    return o;
  }

  synchronized
  public T testAndPull()
  {
    if (list.isEmpty()) return null;
    T o=list.get(0);
    list.remove(0);
    return o;
  }

  synchronized
  public <T> T[] toArray(T[] a)
  {
    return list.toArray(a);
  }

  synchronized
  public Object[] toArray()
  {
    return list.toArray();
  }

  synchronized
  public int size()
  {
    return list.size();
  }

  synchronized
  public boolean removeAll(Collection<?> c)
  {
    return list.removeAll(c);
  }

  synchronized
  public boolean remove(Object o)
  {
    return list.remove(o);
  }

  synchronized
  public int lastIndexOf(Object o)
  {
    return list.lastIndexOf(o);
  }

  synchronized
  public Iterator<T> iterator()
  {
    return list.iterator();
  }

  synchronized
  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  synchronized
  public int indexOf(Object o)
  {
    return list.indexOf(o);
  }

  synchronized
  public boolean containsAll(Collection<?> c)
  {
    return list.containsAll(c);
  }

  synchronized
  public boolean contains(Object o)
  {
    return list.contains(o);
  }

  synchronized
  public void clear()
  {
    list.clear();
  }

  synchronized
  public boolean putAll(Collection<? extends T> c)
  {
    boolean b= list.addAll(c);
    if (!list.isEmpty()) notify();
    return b;
  }

  synchronized
  public void put(T e)
  {
    list.add(e);
    if (!list.isEmpty()) notify();
  }

  synchronized
  public void putTop(T e)
  {
    list.add(0, e);
    if (!list.isEmpty()) notify();
  }
}
