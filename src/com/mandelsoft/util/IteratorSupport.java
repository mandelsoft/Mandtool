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

public class IteratorSupport<T> implements Iterator<T> {
  protected T next;

  public IteratorSupport(T next)
  {
    this(next,false);
  }

  public IteratorSupport()
  { this(false);
  }

  protected IteratorSupport(boolean setup)
  { this(null,setup);
  }

  protected IteratorSupport(T next, boolean setup)
  {
    this.next=next;
    if (setup) setup();
  }

  public boolean hasNext()
  {
    return next!=null;
  }

  public T next()
  {
    try {
      return next;
    }
    finally {
      next=_next();
    }
  }

  public void remove()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  protected void setup()
  {
    if (next==null) next=_next();
  }

  protected T _next()
  {
    return null;
  }
}
