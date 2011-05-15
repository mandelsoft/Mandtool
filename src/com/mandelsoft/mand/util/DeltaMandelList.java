
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
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Uwe Krüger
 */
public class DeltaMandelList extends AbstractList<QualifiedMandelName>
        implements MandelList {

  private List<QualifiedMandelName> list=new ArrayList<QualifiedMandelName>();
  private MandelScanner scan;
  private MandelList base;

  public DeltaMandelList(MandelScanner scan, MandelList base)
  {
    this.scan=scan;
    this.base=base;
    _refresh(true);
  }

  ///////////////////////////////////////////////////////////////////
  // assure correct handling of still unknown entries
  ///////////////////////////////////////////////////////////////////

  @Override
  public boolean contains(Object o)
  {
    System.out.println("delta contains "+o+": "+!base.contains(o));
    return !base.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> c)
  {
    return !base.containsAll(c);
  }

  @Override
  public boolean remove(Object o)
  {
    super.remove(o);
    return base.add((QualifiedMandelName)o);
  }

  @Override
  public boolean add(QualifiedMandelName e)
  {
    super.add(e);
    return base.remove(e);
  }


  ///////////////////////////////////////////////////////////////////
  // minimal suuport abstract list
  ///////////////////////////////////////////////////////////////////

  @Override
  public QualifiedMandelName get(int index)
  {
    return list.get(index);
  }

  @Override
  public int size()
  {
    return list.size();
  }

  @Override
  public void add(int index, QualifiedMandelName element)
  {
    list.add(index, element);
    base.remove(element);
  }

  @Override
  public QualifiedMandelName remove(int index)
  {
    if (index<size()) base.add(get(index));
    return list.remove(index);
  }

  @Override
  public QualifiedMandelName set(int index, QualifiedMandelName element)
  {
    if (index<size()) base.add(get(index));
    base.remove(element);
    return list.set(index, element);
  }

  @Override
  public void clear()
  {
    list.clear();
    scan.rescan(false);
    base.refresh(false);
    base.addAll(scan.getQualifiedMandelNames());
  }

  public void refresh(boolean soft)
  {
    //new Throwable().printStackTrace();
    if (!soft) scan.rescan(soft);
    _refresh(soft);
  }

  private void _refresh(boolean soft)
  {
    //new Throwable().printStackTrace();
    list.clear();
    base.refresh(soft);
    for (QualifiedMandelName n:scan.getQualifiedMandelNames()) {
      if (!base.contains(n)) list.add(n);
    }
  }

  public void save() throws IOException
  {
    base.save();
  }

  public boolean valid()
  {
    return base.valid();
  }

   public void write(OutputStream os, String dst) throws IOException
  {
    IO.write(this,os,dst);
  }

  public void read(InputStream is, String src) throws IOException
  {
    IO.read(this,is,src);
  }

  public QualifiedMandelName get(MandelName n)
  {
    for (QualifiedMandelName q:this) {
      if (q.getMandelName().equals(n)) return q;
    }
    return null;
  }

}
