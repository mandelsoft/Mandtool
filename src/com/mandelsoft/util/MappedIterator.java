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

public class MappedIterator<S,T> extends IteratorSupport<T> {

  private Iterator<S> iterator;

  public MappedIterator(Iterator<S> iterator)
  {
    this.iterator=iterator;
    setup();
  }

  @Override
  protected T _next()
  {
    return iterator.hasNext()?map(iterator.next()):null;
  }

  protected T map(S elem)
  {
    return (T)elem;
  }
}
