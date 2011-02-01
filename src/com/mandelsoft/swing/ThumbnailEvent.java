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
import java.util.EventObject;

public class ThumbnailEvent<E> extends EventObject {

  private E elem;
  private BufferedImage image;

  public ThumbnailEvent(ThumbnailListModel<E> source, E elem,
                        BufferedImage image)
  {
    super(source);
    this.elem=elem;
    this.image=image;
  }

  public E getElem()
  {
    return elem;
  }

  public BufferedImage getImage()
  {
    return image;
  }

  @Override
  public ThumbnailListModel<E> getSource()
  {
    return (ThumbnailListModel<E>)super.getSource();
  }
}
