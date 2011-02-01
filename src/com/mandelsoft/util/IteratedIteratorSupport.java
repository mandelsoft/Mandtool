
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

public abstract class IteratedIteratorSupport<T,I> extends IteratorSupport<T> {
  private Iterator<I> sub;
  private Iterator<T> subs;

  public IteratedIteratorSupport(T cur, Iterator<I> sub)
  {
    super(cur);
    this.sub=sub;
    setup();
  }

  @Override
  protected T _next()
  {
    while ((subs==null || !subs.hasNext()) && sub!=null && sub.hasNext()) {
      subs=_getIterator(sub.next());
    }
    if (subs==null || !subs.hasNext()) return null;
    return subs.next();
  }

  abstract protected Iterator<T> _getIterator(I src);
}
