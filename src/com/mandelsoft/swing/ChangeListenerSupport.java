
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
package com.mandelsoft.swing;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Uwe Krüger
 */

public class ChangeListenerSupport {
  private Set<ChangeListener> listeners=new HashSet<ChangeListener>();
  private ChangeListener[] fire;

  public void addChangeListener(ChangeListener h)
  {
    listeners.add(h);
    fire=null;
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.remove(h);
    fire=null;
  }

  public void fireChangeEvent()
  { ChangeEvent e=new ChangeEvent(this);
    fireChangeEvent(e);
  }

  public void fireChangeEvent(Object src)
  { ChangeEvent e=new ChangeEvent(src);
    fireChangeEvent(e);
  }

  public void fireChangeEvent(ChangeEvent e)
  {
    if (fire==null) {
      fire=new ChangeListener[listeners.size()];
      fire=listeners.toArray(fire);
    }
    for (ChangeListener h:fire) {
      h.stateChanged(e);
    }
  }
}
