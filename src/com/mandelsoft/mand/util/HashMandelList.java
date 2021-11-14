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
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.util.HashList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author D021770
 */
public class HashMandelList extends HashList<QualifiedMandelName> implements MandelList {
  private final Map<MandelName,List<QualifiedMandelName>> map = new HashMap<>();
  
  public HashMandelList()
  {
  }
   
  public HashMandelList(List<QualifiedMandelName> list)
  {
    addAll(list);
  }
    
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public void write(OutputStream os, String dst) throws IOException
  {
    IO.write(this, os, dst);
  }

  @Override
  public void read(InputStream is, String src) throws IOException
  {
    IO.read(this, is, src);
  }
  
  @Override
  public void refresh(boolean soft)
  {
  }

  @Override
  public void save() throws IOException
  {
  }

  @Override
  public boolean valid()
  {
    return true;
  }
  
  ///////////////////////////////////////////////////////////////////////////
  
  @Override
  public QualifiedMandelName get(MandelName n)
  {
    List<QualifiedMandelName> names=map.get(n);
    if (names==null || names.isEmpty()) return null;
    return names.get(0);
  }
  
  public List<QualifiedMandelName> getAll(MandelName n)
  {
    return map.get(n);  
  }

  ///////////////////////////////////////////////////////////////////////////
  @Override
  protected void clearMap()
  {
    map.clear();
  }
  
  @Override
  protected void addMap(QualifiedMandelName e)
  {
    MandelName name = e.getMandelName();
    List<QualifiedMandelName> names = map.get(name);
    if (names == null) {
      names = new ArrayList<>();
      map.put(name, names);
    }
    names.add(e);
  }
  
  @Override
  protected void removeMap(QualifiedMandelName e)
  {
    MandelName name = e.getMandelName();
    List<QualifiedMandelName> names = map.get(e.getMandelName());
    names.remove(e);
    if (names.isEmpty()) {
      map.remove(name);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////
  
  @Override
  public boolean contains(Object o)
  {
    MandelName name;
    List<QualifiedMandelName> names; 
    
    if (o instanceof QualifiedMandelName) {
      name=((QualifiedMandelName)o).getMandelName();
      names = map.get(name);
      return names !=null && names.contains(o);
    }
    if (o instanceof MandelName) {
      names = map.get((MandelName)o);
      return names !=null && !names.isEmpty();
    }
    return false;
  }
}
