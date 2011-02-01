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
package com.mandelsoft.mand.tool;

import com.mandelsoft.util.ChangeEvent;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Uwe Krüger
 */

public class MandelListListenerSupport {
  private Set<MandelListListener> listeners=new HashSet<MandelListListener>();
  private MandelListListener[] fire;

  public void addMandelListListener(MandelListListener h)
  {
    listeners.add(h);
    fire=null;
  }

  public void removeMandelListListener(MandelListListener h)
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
      fire=new MandelListListener[listeners.size()];
      fire=listeners.toArray(fire);
    }
    for (MandelListListener h:fire) {
      h.listChanged(e);
    }
  }
}
