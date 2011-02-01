
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

/**
 *
 * @author Uwe Krüger
 */

public class UniqueArrayBaseList<T> extends ArrayBaseList<T> {

  @Override
  public boolean add(T e)
  {
    if (!contains(e)) return super.add(e);
    return false;
  }

  @Override
  public void add(int index, T element)
  {
    if (!contains(element)) super.add(index, element);
  }

  @Override
  public boolean addAll(Collection<? extends T> c)
  {
    boolean add=false;
    for (T n:c) add|=add(n);
    return add;
  }

  @Override
  public boolean addAll(int index,
                        Collection<? extends T> c)
  {
    boolean add=false;
    for (T n:c) {
      if (!contains(n)) {
        add(index++,n);
        add=true;
      }
    }
    return add;
  }
}
