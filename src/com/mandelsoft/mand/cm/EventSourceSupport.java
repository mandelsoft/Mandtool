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
package com.mandelsoft.mand.cm;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Uwe Krüger
 */

public class EventSourceSupport {
  private Set<InterpolationPointEventListener> listeners=new HashSet<InterpolationPointEventListener>();

  public void addInterpolationPointEventListener(InterpolationPointEventListener h)
  {
    listeners.add(h);
  }

  public void removeInterpolationPointEventListener(InterpolationPointEventListener h)
  {
    listeners.remove(h);
  }

  protected void fireInterpolationPointEvent(InterpolationPoint ip, int id)
  { InterpolationPointEvent e=new InterpolationPointEvent(ip,id);
    for (InterpolationPointEventListener h:listeners) {
      h.stateChanged(e);
    }
  }
}
