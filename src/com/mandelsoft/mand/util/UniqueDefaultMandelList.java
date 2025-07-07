
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author Uwe Krüger
 */

public class UniqueDefaultMandelList extends DefaultMandelList
                             implements MandelList {
  private boolean projectMandelName=false;
  
  public UniqueDefaultMandelList()
  {
    this(false);
  }
  
  public UniqueDefaultMandelList(boolean mand)
  {
    projectMandelName=mand;
  }
  
  private QualifiedMandelName map(QualifiedMandelName e)
  {
    if (!projectMandelName || e.getQualifier() == null) {
      return e;
    }
    return new QualifiedMandelName(e.getMandelName());
  }

  ////////////////////////////////////////////////////////////////////////////
  
  @Override
  public boolean add(QualifiedMandelName e)
  {
    if (!contains(e)) return super.add(map(e));
    return false;
  }

  @Override
  public void add(int index, QualifiedMandelName element)
  {
    if (!contains(element)) super.add(index, map(element));
  }

  @Override
  public boolean addAll(Collection<? extends QualifiedMandelName> c)
  {
    boolean add=false;
    for (QualifiedMandelName n:c) add|=add(n);
    return add;
  }

  @Override
  public boolean addAll(int index,
                        Collection<? extends QualifiedMandelName> c)
  {
    boolean add=false;
    for (QualifiedMandelName n:c) {
      if (!contains(n)) {
        add(index++,n);
        add=true;
      }
    }
    return add;
  }

  @Override
  public boolean remove(Object o)
  {
    if (o==null) {
      return super.remove(o);
    }
    if (o instanceof QualifiedMandelName) {
      return super.remove(map((QualifiedMandelName)o));
    }
    if (o instanceof MandelName) {
      return super.remove(new QualifiedMandelName((MandelName)o));
    }
    return false;
  }

  @Override
  public boolean removeAll(
          Collection<?> c)
  {
    if (!projectMandelName) {
      return super.removeAll(c);
    }
    boolean del=false;
    for (Object o:c) del|=remove(o);
    return del;
  }

  @Override
  public boolean retainAll(
          Collection<?> c)
  {
    if (!projectMandelName) {
      return super.retainAll(c);
    }
    Set<QualifiedMandelName> set = new HashSet<>();
    for (Object o : c) {
      if (o == null) {
        continue;
      }
      if (o instanceof QualifiedMandelName) {
        set.add(map((QualifiedMandelName) o));
        continue;
      }
      if (o instanceof MandelName) {
        set.add(new QualifiedMandelName((MandelName) o));
      }
    }
    return super.retainAll(set);
  }
}
