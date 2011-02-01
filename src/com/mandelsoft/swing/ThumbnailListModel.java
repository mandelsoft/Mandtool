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

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 *
 * @author Uwe Krueger
 */
public interface ThumbnailListModel<E> extends DnDListModel  {
  public BufferedImage getThumbnail(int index, Dimension max);
  public BufferedImage getThumbnail(E element, Dimension max);
  public void addThumbnailListener(ThumbnailListener<E> l);
  public void removeThumbnailListener(ThumbnailListener<E> l);
}
