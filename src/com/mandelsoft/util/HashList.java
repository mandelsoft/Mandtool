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

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author D021770
 * @param <T> list element type (objects must be immutable hashable)
 */
public class HashList<T> extends AbstractHashList<T> implements List<T> {
  private final HashMap<T,Integer> map = new HashMap<>();
  
  public HashList()
  {
  }
   
  public HashList(List<T> list)
  {
    super(list);
  }
    
  ///////////////////////////////////////////////////////////////////////////

  @Override
  protected void clearMap()
  {
    map.clear();
  }

  @Override
  protected void addMap(T e)
  {
    Integer names = map.get(e);
    if (names == null) {
      map.put(e,1);
    } else {
      map.put(e, names+1);
    }
  }
  
  @Override
  protected void removeMap(T e)
  {
    Integer names = map.get(e);
    if (names != null && names > 1) {
      map.put(e, names - 1);
    }
    else {
      map.remove(e);
    }
  }
  
  @Override
  public boolean contains(Object o)
  {
    Integer names = map.get(o);
    return names != null && names > 0;
  }
}
