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

/**
 *
 * @author Uwe Krueger
 */

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.util.IteratorSource;
import com.mandelsoft.util.IteratorSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class LeafMandelListFolder implements MandelListFolder {

  private MandelList list;
  private String name;
  private QualifiedMandelName thumb;
  private MandelListFolderTree tree;

  public LeafMandelListFolder(MandelListFolderTree tree, String name, MandelList list)
  {
    this.tree=tree;
    this.list=list;
    this.name=name;
  }

  public boolean containsTransitively(MandelListFolder f)
  {
    return f==this;
  }

  public boolean containsTransitively(MandelList l)
  {
    return getMandelList()==l;
  }

  public MandelListFolder createSubFolder(String name)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public MandelListFolder createSubFolder(int index, String name)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public MandelList getMandelList()
  {
    return list;
  }

  public boolean hasMandelList()
  {
    return getMandelList()!=null;
  }

  public MandelListFolderTree getMandelListFolderTree()
  {
    return tree;
  }

  public boolean isLeaf()
  {
    return true;
  }

  public String getName()
  {
    return name;
  }

  public QualifiedMandelName getThumbnailName()
  {
    return thumb;
  }

  public void setThumbnailName(QualifiedMandelName thumb)
  {
    this.thumb=thumb;
  }

  public MandelListFolder getParent()
  {
    return null;
  }

  public MandelListFolder getSubFolder(String name)
  {
    return null;
  }

  public String getPath()
  {
    return getName();
  }

  public void setName(String name)
  {
    this.name=name;
  }

  public void setParent(MandelListFolder f)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public void clear()
  {
  }

  public void refresh(boolean soft)
  {
    list.refresh(soft);
  }

  public void save() throws IOException
  {
    list.save();
  }

  public boolean valid()
  {
    return list.valid();
  }

  public Iterable<QualifiedMandelName> allentries()
  {
    return new IteratorSource<QualifiedMandelName>(getMandelList());
  }

  public Iterable<MandelListFolder> allfolders()
  {
    return new Iterable<MandelListFolder>() {

      public Iterator<MandelListFolder> iterator()
      {
        return new IteratorSupport<MandelListFolder>(LeafMandelListFolder.this);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////
  // list methods.
  ////////////////////////////////////////////////////////////////////
  public boolean add(MandelListFolder e)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public void add(int index, MandelListFolder element)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public boolean addAll(Collection<? extends MandelListFolder> c)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public boolean addAll(int index,
                        Collection<? extends MandelListFolder> c)
  {
    return false;
  }

  public boolean contains(Object o)
  {
    return false;
  }

  public boolean containsAll(Collection<?> c)
  {
    return false;
  }

  public MandelListFolder get(int index)
  {
    throw new IndexOutOfBoundsException("no sub folders supported");
  }

  public int indexOf(Object o)
  {
    return -1;
  }

  public boolean isEmpty()
  {
    return true;
  }

  public Iterator<MandelListFolder> iterator()
  {
    return (Iterator<MandelListFolder>)(Object)Collections.emptyList().
      iterator();
  }

  public int lastIndexOf(Object o)
  {
    return -1;
  }

  public ListIterator<MandelListFolder> listIterator()
  {
    return (ListIterator<MandelListFolder>)(Object)Collections.emptyList().
      listIterator();
  }

  public ListIterator<MandelListFolder> listIterator(int index)
  {
    return (ListIterator<MandelListFolder>)(Object)Collections.emptyList().
      listIterator(index);
  }

  public boolean remove(Object o)
  {
    return false;
  }

  public MandelListFolder remove(int index)
  {
    throw new IndexOutOfBoundsException("no sub folders supported");
  }

  public boolean removeAll(Collection<?> c)
  {
    return false;
  }

  public boolean retainAll(Collection<?> c)
  {
    return false;
  }

  public MandelListFolder set(int index, MandelListFolder element)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public int size()
  {
    return 0;
  }

  public List<MandelListFolder> subList(int fromIndex, int toIndex)
  {
    throw new IndexOutOfBoundsException("no sub folders supported");
  }

  public Object[] toArray()
  {
    return Collections.emptyList().toArray();
  }

  public <T> T[] toArray(T[] a)
  {
    return Collections.emptyList().toArray(a);
  }
  ////////////////////////////////////////////////////////////////////////
  private PropertyBag properties=new PropertyBag();

  public void setProperty(String name, String value)
  {
    properties.setProperty(name, value);
  }

  public Iterable<String> propertyNames()
  {
    return properties.propertyNames();
  }

  public String getProperty(String name)
  {
    return properties.getProperty(name);
  }
}
