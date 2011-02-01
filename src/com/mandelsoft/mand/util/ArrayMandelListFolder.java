
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
import java.util.Iterator;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.util.IteratedIteratorSupport;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krüger
 */

public abstract class ArrayMandelListFolder
       extends ArrayBaseList<MandelListFolder> implements MandelListFolder {
  private MandelListFolder    parent;
  private String              name;
  private QualifiedMandelName thumb;
  private MandelList          list;
  private String              identifier;

  private static int cnt=0;

  public ArrayMandelListFolder(String name)
  {
    this.name=name;
    this.list=createMandelList();

    identifier=Utils.getObjectIdentifier(this,++cnt);
  }

  public boolean containsTransitively(MandelListFolder f)
  {
    
    if (f==this) return true;
    for (MandelListFolder sub :this) {
      if (sub.containsTransitively(f)) return true;
    }
    return false;
  }

  public boolean containsTransitively(MandelList l)
  {
    if (getMandelList()==l) return true;
    for (MandelListFolder f :this) {
      if (f.containsTransitively(l)) return true;
    }
    return false;
  }

  @Override
  public boolean add(MandelListFolder f)
  { boolean b=false;
    if (!contains(f)){
      b=super.add(f);
      if (b) {
        f.setParent(this);
      }
      else {
//        System.out.println(getName()+" ignored "+f.getName());
      }
    }
    else {
//      System.out.println(getName()+" already contains "+f.getName());
    }
    return b;
  }

  @Override
  public void add(int index, MandelListFolder f)
  {
    if (!contains(f)){
      super.add(index, f);
      f.setParent(this);
    }
  }

  @Override
  public boolean addAll(Collection<? extends MandelListFolder> c)
  {
    boolean add=false;
    for (MandelListFolder n:c) add|=add(n);
    return add;
  }

  @Override
  public boolean addAll(int index,
                        Collection<? extends MandelListFolder> c)
  {
    boolean add=false;
    for (MandelListFolder n:c) {
      if (!contains(n)) {
        add(index++,n);
        add=true;
      }
    }
    return add;
  }

  public boolean isLocalFolder(MandelListFolder f)
  {
    MandelListFolder folder=this;
    while (folder.getParent()!=null) folder=folder.getParent();
    while (f!=null && f!=folder) f=f.getParent();
    return f==folder;
  }

  @Override
  public boolean remove(Object o)
  { boolean b=false;
    if (contains(o)) {
      b=super.remove(o);
      MandelListFolder f=(MandelListFolder)o;
      f.setParent(null);
    }
    return b;
  }

  @Override
  public MandelListFolder remove(int index)
  {
    MandelListFolder f=super.remove(index);
    f.setParent(null);
    return f;
  }

  public void setName(String name)
  {
    this.name=name;
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

  public String getPath()
  {
    if (getParent()==null) return getName();
    StringBuffer sb=new StringBuffer();
    getPath(sb,getParent());
    sb.append("/");
    sb.append(getName());
    return sb.toString();
  }

  private void getPath(StringBuffer sb, MandelListFolder f)
  {
    if (f.getParent()!=null) {
      getPath(sb,f.getParent());
      sb.append("/");
    }
    sb.append(f.getName());
  }

  public MandelListFolder getSubFolder(String name)
  {
    for (MandelListFolder f:this) {
      if (f.getName().equals(name)) return f;
    }
    return null;
  }

  public boolean isLeaf()
  {
    return false;
  }
  
  public MandelListFolder getParent()
  {
    return parent;
  }

  abstract protected MandelList createMandelList();

  public void setParent(MandelListFolder f)
  {
    if (parent!=f) {
      if (parent!=null) parent.remove(this);
      parent=f;
      //if (parent!=null) parent.add(this);
    }
  }

  public MandelList getMandelList()
  {
    return list;
  }

  public boolean hasMandelList()
  {
    return getMandelList()!=null;
  }

  static private class MandelListFolderIterator extends IteratedIteratorSupport<MandelListFolder, MandelListFolder> {

    public MandelListFolderIterator(MandelListFolder folder)
    {
      super(folder, folder.iterator());
    }

    @Override
    protected Iterator<MandelListFolder> _getIterator(MandelListFolder src)
    {
      return src.allfolders().iterator();
    }
  }

  static private class AllIterator extends
              IteratedIteratorSupport<QualifiedMandelName,MandelListFolder> {

    public AllIterator(Iterator<MandelListFolder> folders)
    {
      super(null,folders);
      setup();
    }

    @Override
    protected Iterator<QualifiedMandelName> _getIterator(MandelListFolder src)
    {
      if (src.getMandelList()==null) return null;
      return src.getMandelList().iterator();
    }
  }

  public Iterable<QualifiedMandelName> allentries()
  {
    return new Iterable<QualifiedMandelName>() {
      public Iterator<QualifiedMandelName> iterator()
      { return  new AllIterator(new MandelListFolderIterator(ArrayMandelListFolder.this));
      }
    };
  }

  public Iterable<MandelListFolder> allfolders()
  {
    return new Iterable<MandelListFolder>() {
      public Iterator<MandelListFolder> iterator()
      {
        return new MandelListFolderIterator(ArrayMandelListFolder.this);
      }
    };
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
  

  ////////////////////////////////////////////////////////////////////////
  @Override
  public String toString()
  {
    return getName()+" ("+identifier+"): "+
            Utils.getObjectIdentifier(getMandelListFolderTree());
  }
}
