/*
 * Copyright 2021 D021770.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author D021770
 */
public abstract class AbstractHashList<T> implements List<T> {
  private List<T> list = new ArrayList<>();
  
  public AbstractHashList()
  {
  }
   
  public AbstractHashList(List<T> list)
  {
    addAll(list);
  }
    
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public void clear()
  {
    list.clear();
    clearMap();
  }

  protected abstract void clearMap();
  protected abstract void addMap(T e);
  protected abstract void removeMap(T e);
  
  @Override
  public boolean add(T e)
  {
    if (e==null) {
      return false;
    }
    boolean b = list.add(e);
    if (b) {
      addMap(e);
    }
    return b;
  }

  @Override
  public boolean remove(Object o)
  {
    boolean b = list.remove(o);
    if (b) {
      T e=(T)o;
      removeMap(e);
    }
    return b;
  }

  @Override
  public boolean addAll(Collection<? extends T> c)
  {
    boolean added = false;
    for ( T e : c) {
      if (add(e)) {
        added=true;
      }
    }
    return added;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c)
  {
    boolean added = list.addAll(index, c);
    for ( T e : c) {
      addMap(e);
    }
    return added;
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    boolean removed = false;
    for ( Object o : c) {
      if (remove(o)) {
        removed=true;
      }
    }
    return removed;
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    if (list.retainAll(c)) {
      clearMap();
      for (T e : list) {
        addMap(e);
      }
      return true;
    }
    return false;
  }

  @Override
  public T get(int index)
  {
    return list.get(index);
  }

  @Override
  public T set(int index, T e)
  {
    T old =list.set(index, e);
    if (old!=null && !old.equals(e)) {
      removeMap(old);
      addMap(e);
    }
    return old;
  }

  @Override
  public void add(int index, T e)
  {
    list.add(index, e);
    addMap(e);
  }

  @Override
  public T remove(int index)
  {
    T old = list.remove(index);
    if (old!=null) removeMap(old);
    return old;
  }

  @Override
  public int size()
  {
    return list.size();
  }

  @Override
  public boolean isEmpty()
  {
    return list.isEmpty();
  }
  
  @Override
  public boolean containsAll(Collection<?> c)
  {
    for (Object o : c ) {
      if (!contains(o)) return false;
    }
    return true;
  }
  
  @Override
  public Iterator<T> iterator()
  {
    return list.iterator();
  }

  @Override
  public Object[] toArray()
  {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a)
  {
    return list.toArray(a);
  }

  @Override
  public int indexOf(Object o)
  {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o)
  {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator()
  {
    return list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index)
  {
     return list.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex)
  {
    return list.subList(fromIndex, toIndex);
  }
}
