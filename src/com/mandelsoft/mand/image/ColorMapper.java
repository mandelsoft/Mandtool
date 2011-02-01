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

import com.mandelsoft.mand.*;
import java.awt.Color;
import java.awt.image.ColorModel;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.mapping.Mapping;
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.ChangeListener;
import com.mandelsoft.util.StateChangeSupport;

/**
 *
 * @author Uwe Krueger
 */
public class ColorMapper extends StateChangeSupport {
  public static boolean debug=false;

  public static abstract class ChangeHandler implements ChangeListener {
    public abstract void handle(ColorMapper cm);

    public void stateChanged(ChangeEvent e)
    { handle((ColorMapper)e.getSource());
    }
  }

  private ColorModel colormodel;
  private Colormap   colormap;
  private Mapping    mapping;

  private Colormap.ChangeHandler handler;
  private Object[]               colormapping;

  public ColorMapper(ColorModel colmod, Colormap colmap, Mapping m)
  { 
    this.handler=new Colormap.ChangeHandler() {
        public void handle(Colormap cm)
        { updateColorMapping();
        }
      };

    this.colormodel=colmod;
    setColormap(colmap);
    setMapping(m);
  }

  public void setColormap(Colormap colmap)
  {
    if (this.colormap==colmap) return;
    if (mapping!=null && colmap!=null &&
        colmap.getSize()!=mapping.getTargetSize()) {
      throw new MandelException("illegal colormap size: "+
                   colmap.getSize()+"!="+mapping.getTargetSize());
    }

    if (colormap!=null) {
      colormap.removeChangeListener(handler);
    }
    
    colormap=colmap;
    if (debug) System.out.println("setting colormap for image "+colmap);
    if (colormodel!=null && colormap!=null) {
      updateColorMapping();
      colormap.addChangeListener(handler);
    }
  }

  public void setMapping(Mapping map)
  {
    if (map!=null && colormap!=null &&
        map.getTargetSize()>colormap.getSize()) {
      throw new MandelException("illegal target size: "+
              map.getTargetSize()+">"+colormap.getSize());
    }
    if (!equals(this.mapping,map)) {
      this.mapping=map;
      fireChangeEvent();
    }
    
  }

  public void setData(Colormap colmap, Mapping map)
  { boolean mod=false;

    if (map!=null && colmap!=null &&
        map.getTargetSize()>colmap.getSize()) {
      throw new MandelException("illegal target size: "+
              map.getTargetSize()+">"+colmap.getSize());
    }

    if (!equals(this.mapping,map)) {
      mod=true;
      this.mapping=map;
    }
    if (this.colormap!=colmap) {
      mod=true;
      if (colormap!=null) {
        colormap.removeChangeListener(handler);
      }
      colormap=colmap;
      if (colormap!=null) {
        if (colormodel!=null) {
          updateColorMapping();
        }
        colormap.addChangeListener(handler);
      }
    }
    if (mod) {
      fireChangeEvent();
    }
  }
  
  private boolean equals(Mapping a, Mapping b)
  {
    if (a==b) return true;
    if (a==null || b==null) return false;
    return a.equals(b);
  }

  private void updateColorMapping()
  {
    if (colormapping==null || colormapping.length!=colormap.getSize()) {
      colormapping=new Object[colormap.getSize()];
    }
    for (int i=0; i<colormap.getSize(); i++) {
      int rgb=colormap.getColor(i).getRGB();
      colormapping[i]=colormodel.getDataElements(rgb, colormapping[i]);
      //System.out.println("color["+i+"]="+rgb+"->"+((int[])colormapping[i])[0]);
    }
    fireChangeEvent();
  }

  @Override
  public void fireChangeEvent()
  {
    if (mapping!=null && colormap!=null) {
      super.fireChangeEvent();
    }
  }

  public int mapColormapIndex(int it)
  {
    return mapping.getColormapIndex(it);
  }

  public Color mapIterationValue(int it)
  { try {
      int ci=mapColormapIndex(it);
      return colormap.getColor(ci);
    }
    catch (NullPointerException npe) {
      if (mapping==null)
        throw new MandelException("no mapping set");
      else
        throw new MandelException("no colormap set");
    }
  }

  public int mapIterationValueToRGB(int it)
  { return mapIterationValue(it).getRGB();
  }

  public Object mapIterationValueToDataElements(int it)
  { int ci;
    try {
      ci=mapping.getColormapIndex(it);
    }
    catch (NullPointerException npe) {
      throw new MandelException("no mapping set");
    }
    try {
      return colormapping[ci];
    }
    catch (NullPointerException npe) {
      if (colormap!=null)
        throw new MandelException("no color model set");
      else
        throw new MandelException("no colormap set");
    }
  }

  @Override
  protected void finalize() throws Throwable
  {
    super.finalize();
    if (colormap!=null) colormap.removeChangeListener(handler);
  }
}
