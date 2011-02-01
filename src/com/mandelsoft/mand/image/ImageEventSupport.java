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

package com.mandelsoft.mand.image;

import java.util.HashSet;
import java.util.Set;
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.StateChangeSupport;

/**
 *
 * @author Uwe Krueger
 */
class ImageEventSupport extends StateChangeSupport {

  private Set<ImageListener> listeners=new HashSet<ImageListener>();

  public void addImageListener(ImageListener h)
  {
    listeners.add(h);
  }

  public void removeImageListener(ImageListener h)
  {
    listeners.remove(h);
  }

  public void firePrepareEvent()
  {
    ChangeEvent e=new ChangeEvent(this);
    firePrepareEvent(e);
  }

  public void firePrepareEvent(Object src)
  {
    ChangeEvent e=new ChangeEvent(src);
    firePrepareEvent(e);
  }

  public void firePrepareEvent(ChangeEvent e)
  {
    for (ImageListener h:listeners) {
      h.stateToBeChanged(e);
    }
  }

  @Override
  public void fireChangeEvent()
  {
    ChangeEvent e=new ChangeEvent(this);
    fireChangeEvent(e);
  }

  @Override
  public void fireChangeEvent(Object src)
  {
    ChangeEvent e=new ChangeEvent(src);
    fireChangeEvent(e);
  }

  @Override
  public void fireChangeEvent(ChangeEvent e)
  {
    for (ImageListener h:listeners) {
      h.stateChanged(e);
    }
    super.fireChangeEvent(e);
  }
}
