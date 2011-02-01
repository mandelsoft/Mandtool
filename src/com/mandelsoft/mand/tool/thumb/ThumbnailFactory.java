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

package com.mandelsoft.mand.tool.thumb;

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.image.MandelImage.Factory;
import com.mandelsoft.mand.scan.MandelScanner;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 *
 * @author Uwe Krueger
 */
public interface ThumbnailFactory {

  public interface Client {
    boolean usesThumbnail(QualifiedMandelName name);
    MandelScanner getMandelScanner();
  }

  void cleanupThumbnails();

  Factory getFactory();

  BufferedImage getThumbnail(QualifiedMandelName n, Dimension max);

  void remove(QualifiedMandelName name);

  void setFactory(MandelImage.Factory factory);

  void setMaxcache(int maxcache);

  public void addImageChangeListener(ImageChangeListener<QualifiedMandelName> l);
  public void removeImageChangeListener(ImageChangeListener<QualifiedMandelName> l);

}
