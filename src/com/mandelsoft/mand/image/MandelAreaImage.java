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

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.mapping.Mapping;
import com.mandelsoft.util.ChangeListener;
import java.awt.image.BufferedImage;

/**
 *
 * @author Uwe Krueger
 */
public class MandelAreaImage implements MandelImage {
  private QualifiedMandelName name;
  private MandelImage image;

  public MandelAreaImage(QualifiedMandelName name, MandelImage image)
  {
    this.name=name;
    this.image=image;
  }

  public MandelImage getMandelImage()
  {
    return image;
  }

  public QualifiedMandelName getName()
  {
    return name;
  }

  ///////////////////////////////////////////////////////////////////////////
  // MandelImage API
  ///////////////////////////////////////////////////////////////////////////
  
  public void setMapper(ResizeMode mode, Mapper map)
  {
    image.setMapper(mode, map);
  }

  public void setColormap(ResizeMode mode, Colormap map)
  {
    image.setColormap(mode, map);
  }

  public Colormap resizeColormap(ResizeMode mode, int size)
  {
    return image.resizeColormap(mode, size);
  }

  public void removeImageListener(ImageListener l)
  {
    image.removeImageListener(l);
  }

  public void removeChangeListener(ChangeListener l)
  {
    image.removeChangeListener(l);
  }

  public boolean isModified()
  {
    return image.isModified();
  }

  public MandelRaster getRasterData()
  {
    return image.getRasterData();
  }

  public Mapping getMapping()
  {
    return image.getMapping();
  }

  public Mapper getMapper()
  {
    return image.getMapper();
  }

  public MandelData getMandelData()
  {
    return image.getMandelData();
  }

  public MandelInfo getInfo()
  {
    return image.getInfo();
  }

  public BufferedImage getImage()
  {
    return image.getImage();
  }

  public int getColormapIndex(int x, int y)
  {
    return image.getColormapIndex(x, y);
  }

  public Colormap getColormap()
  {
    return image.getColormap();
  }

  public void addImageListener(ImageListener l)
  {
    image.addImageListener(l);
  }

  public void addChangeListener(ChangeListener l)
  {
    image.addChangeListener(l);
  }

}
