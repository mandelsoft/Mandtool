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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelException;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.cm.Colormaps;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.mapping.Mapping;
import com.mandelsoft.mand.mapping.StatisticMapper;
import com.mandelsoft.util.ChangeListener;

/**
 *
 * @author Uwe Krueger
 */
public interface MandelImage {
  public static boolean debug=false;

  BufferedImage getImage();
  MandelInfo    getInfo();
  Colormap      getColormap();
  Mapping       getMapping();
  Mapper        getMapper();
  MandelRaster  getRasterData();
  MandelData    getMandelData();

  void addChangeListener(ChangeListener l);
  void removeChangeListener(ChangeListener l);

  void addImageListener(ImageListener l);
  void removeImageListener(ImageListener l);

  void setColormap(ResizeMode mode, Colormap map);
  void setMapper(ResizeMode mode, Mapper map);

  int getColormapIndex(int x, int y);

  Colormap resizeColormap(ResizeMode mode, int size);

  boolean isModified();
  
  static public class Factory implements ColormapSource {
    private ColormapSource colormapSource;
    
    public Factory(ColormapSource cms)
    { this.colormapSource=cms;
    }
    
    public Factory()
    { this(new Colormaps.Simple(255, Color.BLUE, Color.WHITE));
    }

    public Colormap getColormap()
    {
      Colormap cm=null;
      if (colormapSource!=null) cm=colormapSource.getColormap();
      if (cm==null) cm=new Colormaps.Simple(255, Color.BLUE, Color.WHITE);
      return cm;
    }

    public void setColormapSource(ColormapSource colormap)
    {
      this.colormapSource=colormap;
    }

    final protected Colormap getDefaultColormap(MandelData data, Colormap defcolmap)
    {
      if (defcolmap!=null) return defcolmap;
      return getColormap();
    }

    protected Mapper getDefaultMapper(MandelData data)
    { 
      return new StatisticMapper(0.00);
    }

    public MandelImage getImage(File f) throws IOException
    {
      return getImage(new MandelData(f));
    }

    public MandelImage getImage(File f, ColormapModel defcolmap)
                          throws IOException
    { return getImage(f,defcolmap.getResizeMode(),defcolmap.getColormap());
    }

    public MandelImage getImage(File f, ResizeMode mode, ColormapSource defcolmap)
                          throws IOException
    { return getImage(new MandelData(f), mode, defcolmap);
    }

    public MandelImage getImage(MandelData data) throws IOException
    { return getImage(data,ResizeMode.RESIZE_PROPORTIONAL,null);
    }

    public MandelImage getImage(MandelData data, ColormapSource defcolmap)
                          throws IOException
    { return getImage(data,ResizeMode.RESIZE_PROPORTIONAL,defcolmap);
    }

    public MandelImage getImage(MandelData data, ColormapModel defcolmap)
                       throws IOException
    { return getImage(data,defcolmap.getResizeMode(),defcolmap.getColormap());
    }

    public MandelImage getImage(MandelData data, ResizeMode mode,
                                ColormapSource defcolmap)
                       throws IOException
    { return getImage(data,mode,defcolmap,null);
    }

    public MandelImage getImage(MandelData data, ColormapModel defcolmap,
                                                 Mapper defmapper)
                       throws IOException
    { return getImage(data, defcolmap.getResizeMode(),
                            defcolmap.getColormap(),defmapper);
    }

    public MandelImage getImage(MandelData data, ResizeMode mode,
                                                 ColormapSource defcolmap,
                                                 Mapper defmapper)
                       throws IOException
    {
      MandelHeader h=data.getHeader();
      if (debug) System.out.println("found "+h+" file "+data.getFile());
      if (h.hasRaster()) {
        try {
          // first setup default image settings, if not present
          MandelData d=new MandelData(data);
          if (!h.hasColormap()) {
            Colormap cm=defcolmap==null?null:defcolmap.getColormap();
            d.setColormap(mode,getDefaultColormap(d, cm));
            data=d;
          }
          if (!h.hasMapping()) {
            if (!h.hasMapper()) {
              if (defmapper==null) {
                d.setMapper(mode,getDefaultMapper(d));
                data=d;
              }
              else {
                d.setMapper(mode,defmapper);
                data=d;
              }
            }
            else {
              d.createMapping(mode);
              data=d;
            }
          }
          return new RasterImage(data);
        }
        catch (MandelException ex) {
          System.out.println("corrupted image "+data.getFile()+": "+ex);
          ex.printStackTrace(System.out);
          throw new IOException("corrupted image "+data.getFile()+": "+ex,ex);
        }
      }
      if (data.getImage()!=null) {
        return new PlainImage(data);
      }
      throw new IOException("no image available");
    }
  }
}
