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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Uwe Krüger
 */

public class StateChangeSupport {
  private Set<ChangeListener> listeners=new HashSet<ChangeListener>();
  private Object owner;

  public StateChangeSupport(Object owner)
  {
    this.owner=owner==null?this:owner;
  }

  public StateChangeSupport()
  {
    this.owner=this;
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.add(h);
  }

  protected int noOfListeners()
  {
    return listeners.size();
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.remove(h);
  }

  public void fireChangeEvent()
  { ChangeEvent e=new ChangeEvent(owner);
    fireChangeEvent(e);
  }

  public void fireChangeEvent(Object src)
  { ChangeEvent e=new ChangeEvent(src);
    fireChangeEvent(e);
  }

  public void fireChangeEvent(ChangeEvent e)
  {
    for (ChangeListener h:listeners) {
      h.stateChanged(e);
    }
  }
}
