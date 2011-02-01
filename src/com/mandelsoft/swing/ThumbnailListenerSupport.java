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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Krueger
 */
public class ThumbnailListenerSupport<E> {
   private List<ThumbnailListener<E>> list;

  public void fireThumbnailChanged(ThumbnailListModel<E> model, E elem,
                                   BufferedImage image)
  {
    if (list!=null && list.size()>0) {
      ThumbnailEvent<E> event=new ThumbnailEvent<E>(model,elem,image);
      for (ThumbnailListener<E> l:list) l.thumbnailChanged(event);
    }
  }

  public void addThumbnailListener(ThumbnailListener<E> l)
  {
    if (list==null) list=new ArrayList<ThumbnailListener<E>>();
    else {
      if (list.contains(l)) return;
    }
    list.add(l);
  }

  public void removeThumbnailListener(ThumbnailListener<E> l)
  {
    if (list!=null) {
      list.remove(l);
      if (list.isEmpty()) list=null;
    }
  }
}
