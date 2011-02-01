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

import java.util.Iterator;

/**
 *
 * @author Uwe Krüger
 */

public class FilteredIteratorSource<T> extends IteratorSource<T> {

  public FilteredIteratorSource(Iterable<T> source)
  {
    super(source);
  }

  @Override
  public Iterator<T> iterator()
  {
    return new Filter(super.iterator());
  }

  protected boolean accept(T n)
  {
    return true;
  }

  private class Filter extends IteratorSupport<T> {
    private Iterator<T> it;

    public Filter(Iterator<T> it)
    {
      if (it==null) throw new IllegalArgumentException("no iterator passed");
      this.it=it;
      setup();
    }

    @Override
    protected T _next()
    {
      while (it.hasNext()) {
        T n=it.next();
        if (accept(n)) return n;
      }
      return null;
    }
    
  }
}
