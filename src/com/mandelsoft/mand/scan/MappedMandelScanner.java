
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
package com.mandelsoft.mand.scan;

import java.util.Set;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.ElementNameMapper;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import java.util.HashSet;

/**
 *
 * @author Uwe Krüger
 */

public class MappedMandelScanner extends MandelScannerProxy {
  private ElementNameMapper mapper;

  public MappedMandelScanner(MandelScanner s, ElementNameMapper mapper)
  {
    super(s);
    this.mapper=mapper;
  }

  @Override
  protected MandelHandle mapOut(MandelHandle h)
  {
    QualifiedMandelName n=mapper.mapOut(h.getName());
    if (n==h.getName()) return h;
    return new ProxyMandelHandle(h,n);
  }

  @Override
  protected ColormapHandle mapOut(ColormapHandle h)
  {
    ColormapName n=mapper.mapOut(h.getName());
    if (n==h.getName()) return h;
    return new ProxyColormapHandle(h,n);
  }

  private Set<MandelHandle> mapOutMH(Set<MandelHandle> set)
  {
    Set<MandelHandle> nset=new HashSet<MandelHandle>();
    boolean mapped=false;
    for (MandelHandle h:set) {
      MandelHandle n=mapOut(h);
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  private Set<QualifiedMandelName> mapOutQN(Set<QualifiedMandelName> set)
  {
    Set<QualifiedMandelName> nset=new HashSet<QualifiedMandelName>();
    boolean mapped=false;
    for (QualifiedMandelName h:set) {
      QualifiedMandelName n=mapper.mapOut(h);
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  private Set<MandelName> mapOutMN(Set<MandelName> set)
  {
    Set<MandelName> nset=new HashSet<MandelName>();
    boolean mapped=false;
    for (MandelName h:set) {
      MandelName n=mapper.mapOut(h);
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  private Set<ColormapHandle> mapOutC(Set<ColormapHandle> set)
  {
    Set<ColormapHandle> nset=new HashSet<ColormapHandle>();
    boolean mapped=false;
    for (ColormapHandle h:set) {
      ColormapHandle n=mapOut(h);
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  private Set<ColormapName> mapOutCN(Set<ColormapName> set)
  {
    Set<ColormapName> nset=new HashSet<ColormapName>();
    boolean mapped=false;
    for (ColormapName h:set) {
      ColormapName n=mapper.mapOut(h);
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  //////////////////////////////////////////////////////////////////////////
  // mapping scanner methods
  //////////////////////////////////////////////////////////////////////////

  @Override
  public Set<ElementHandle<?>> getAllHandles()
  {
    Set<ElementHandle<?>> nset=new HashSet<ElementHandle<?>>();
    Set<ElementHandle<?>> set=super.getAllHandles();
    boolean mapped=false;
    for (ElementHandle<?> h:set) {
      ElementHandle<?> n;
      if (h instanceof MandelHandle) {
        n=mapOut((MandelHandle)h);
      }
      else {
        n=mapOut((ColormapHandle)h);
      }
      nset.add(n);
      mapped|=(n!=h);
    }
    return mapped?nset:set;
  }

  @Override
  public Set<MandelHandle> getMandelHandles()
  {
    return mapOutMH(super.getMandelHandles());
  }

  @Override
  public Set<QualifiedMandelName> getQualifiedMandelNames(MandelName name)
  {
    return mapOutQN(super.getQualifiedMandelNames(mapper.mapIn(name)));
  }

  @Override
  public Set<QualifiedMandelName> getQualifiedMandelNames()
  {
    return mapOutQN(super.getQualifiedMandelNames());
  }

  @Override
  public Set<MandelName> getMandelNames()
  {
    return mapOutMN(super.getMandelNames());
  }

  @Override
  public Set<MandelHandle> getMandelHandles(QualifiedMandelName name)
  {
    return mapOutMH(super.getMandelHandles(mapper.mapIn(name)));
  }

  @Override
  public Set<MandelHandle> getMandelHandles(MandelName name)
  {
    return mapOutMH(super.getMandelHandles(mapper.mapIn(name)));
  }

  @Override
  public MandelHandle getMandelHandle(QualifiedMandelName name)
  {
    return mapOut(super.getMandelHandle(mapper.mapIn(name)));
  }

  @Override
  public MandelHandle getMandelInfo(QualifiedMandelName name)
  {
    return mapOut(super.getMandelInfo(mapper.mapIn(name)));
  }

  @Override
  public MandelHandle getMandelInfo(MandelName name)
  {
    return mapOut(super.getMandelInfo(mapper.mapIn(name)));
  }

  @Override
  public MandelHandle getMandelData(QualifiedMandelName name)
  {
    return mapOut(super.getMandelData(mapper.mapIn(name)));
  }

  @Override
  public MandelHandle getMandelData(MandelName name)
  {
    return mapOut(super.getMandelData(mapper.mapIn(name)));
  }

  //////////////////////////////////////////////////////////////////////////

  @Override
  synchronized
  public Set<ColormapName> getColormapNames()
  {
   return mapOutCN(super.getColormapNames());
  }

  @Override
  public boolean hasColormap(ColormapName name)
  {
    return super.hasColormap(mapper.mapIn(name));
  }

  @Override
  public Set<ColormapHandle> getColormapHandles(ColormapName name)
  {
    return mapOutC(super.getColormapHandles(mapper.mapIn(name)));
  }

  @Override
  public ColormapHandle getColormap(ColormapName name)
  {
    return mapOut(super.getColormap(mapper.mapIn(name)));
  }
}
