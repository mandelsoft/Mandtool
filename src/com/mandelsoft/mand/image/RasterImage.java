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
import java.awt.image.WritableRaster;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelException;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.mapping.Mapping;
import com.mandelsoft.util.ChangeListener;

/**
 *
 * @author Uwe Krueger
 */
public class RasterImage extends BufferedImage implements MandelImage {
  public static boolean debug=false;

  private WritableRaster     raster;

  private ImageEventSupport  listeners;
  private MandelData         data;
  private ColorMapper        colormapper;
  private RasterColorMapper  rastermapper;

  public RasterImage(MandelData data)
  { super(data.getInfo().getRX(),data.getInfo().getRY(),
          BufferedImage.TYPE_INT_RGB);
    if (data.getRaster()==null) throw new MandelException("no raster set");
    this.raster=getRaster();
    this.listeners=new ImageEventSupport();

    this.data=new MandelData(data);
    this.data.setModified(data.isModified());
    this.colormapper=new ColorMapper(getColorModel(),
                                     this.data.getColormap(),
                                     this.data.getMapping());
    this.rastermapper=new RasterColorMapper(colormapper,this.data.getRaster());
    
    colormapper.addChangeListener(new ColorMapper.ChangeHandler() {
      public void handle(ColorMapper cm)
      { updateImage();
      }
    });
    updateImage();
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }

  public void removeImageListener(ImageListener h)
  {
    listeners.removeImageListener(h);
  }

  public void addImageListener(ImageListener h)
  {
    listeners.addImageListener(h);
  }

  public int getColormapIndex(int x, int y)
  {
    return rastermapper.getColormapIndex(x, y);
  }

  public BufferedImage getImage()
  { return this;
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

  public MandelInfo getInfo()
  { return data.getInfo();
  }

  public Colormap getColormap()
  { return data.getColormap();
  }

  public Mapping getMapping()
  {
    return data.getMapping();
  }

  public Mapper getMapper()
  {
    return data.getMapper();
  }

  public void setColormap(ResizeMode mode, Colormap map)
  { 
    if (map!=null && data.getColormap()!=null &&
        data.getColormap().getSize()!=map.getSize()) {
      data.setColormap(mode, map);
      colormapper.setData(data.getColormap(),data.getMapping());
    }
    else {
      data.setColormap(mode,map);
      colormapper.setColormap(map);
    }
  }

  public Colormap resizeColormap(ResizeMode mode, int size)
  {
    Colormap cm=data.resizeColormap(mode,size);
    if (cm!=null) colormapper.setData(data.getColormap(),data.getMapping());
    return cm;
  }

  public void setMapping(Mapping m)
  {
    data.setMapping(m);
    colormapper.setMapping(m);
  }

  public void setMapper(ResizeMode mode, Mapper m)
  {
    if (debug) System.out.println("Setting mapper "+m);
    data.setMapper(mode,m);
    colormapper.setData(data.getColormap(),data.getMapping());
  }

  private void updateImage()
  {
    if (data.getColormap()!=null && data.getMapping()!=null) {
      if (debug) System.out.println("updating image ...");
      
        //new Throwable().printStackTrace(System.out);
      listeners.firePrepareEvent();
      for (int y=0; y<getHeight(); y++) {
        for (int x=0; x<getWidth(); x++) {
          raster.setDataElements(x, y, rastermapper.getPixelDataElements(x, y));
        }
      }
      listeners.fireChangeEvent();
    }
  }

}
