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

import java.awt.image.BufferedImage;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.mapping.Mapping;

/**
 *
 * @author Uwe Krueger
 */
public class PlainImage extends ImageEventSupport
                        implements MandelImage {
  private MandelData data;

  public PlainImage(MandelData data)
  { this.data=data;
    if (data.getImage()==null) {
      throw new IllegalArgumentException("no image found");
    }
  }

  public MandelData getMandelData()
  {
    return data;
  }

  public boolean isModified()
  {
    return data.isModified();
  }

  public MandelRaster getRasterData()
  {
    return data.getRaster();
  }

  public Mapping getMapping()
  {
    return data.getMapping();
  }

  public Mapper getMapper()
  {
    return data.getMapper();
  }

  public MandelInfo getInfo()
  {
    return data.getInfo();
  }

  public BufferedImage getImage()
  {
    return data.getImage();
  }

  public Colormap getColormap()
  {
    return data.getColormap();
  }

  public void setColormap(ResizeMode mode, Colormap map)
  {
    throw new UnsupportedOperationException("Not supported for this image.");
  }

  public Colormap resizeColormap(ResizeMode mode, int size)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setMapper(ResizeMode mode, Mapper map)
  {
    throw new UnsupportedOperationException("Not supported for this image.");
  }

  public int getColormapIndex(int x, int y)
  {
    throw new UnsupportedOperationException("Not supported for this image.");
  }
}
