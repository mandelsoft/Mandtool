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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.Coord;
import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelData;
import java.awt.Rectangle;
import java.math.BigDecimal;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;

/**
 *
 * @author Uwe Krüger
 */
public class ToolUtils extends MandUtils {

  static private final boolean debug=false;
  static private final double D=-0.5;

  static public void updateInfo(MandelInfo info, Rectangle rect,
                                MandelInfo parent, double scale)
  {
    if (debug) {
      System.out.println("*** update info");
      System.out.println("parent:");
      System.out.println("   center: "+parent.getXM()+","+parent.getYM());
      System.out.println("   size  : "+parent.getDX()+","+parent.getDY());
    }

    BigDecimal x=sub(parent.getXM(), div(parent.getDX(), 2));
    BigDecimal y=add(parent.getYM(), div(parent.getDY(), 2));

    double rx=rect.getX()+(rect.getWidth()+D)/2;
    double ry=rect.getY()+(rect.getHeight()+D)/2;

    info.setXM(add(x, mul(parent.getDX(), rx/parent.getRX()/scale)));
    info.setYM(sub(y, mul(parent.getDY(), ry/parent.getRY()/scale)));

    info.setDX(mul(parent.getDX(), (rect.getWidth()+D)/parent.getRX()/scale));
    info.setDY(mul(parent.getDY(), (rect.getHeight()+D)/parent.getRY()/scale));

    if (debug) {
      System.out.println("child:");
      System.out.println("   center: "+info.getXM()+","+info.getYM());
      System.out.println("   size  : "+info.getDX()+","+info.getDY());
    }
  }
  
  static public boolean hasRefCoord(MandelData data)
  {
    MandelInfo info=data.getInfo();
    return hasRefCoord(data, 0, 0, info.getRX(), info.getRY());
  }
  
  static public Coord getRefCoord(MandelData data)
  {
    MandelInfo info=data.getInfo();
    return getRefCoord(data, 0, 0, info.getRX(), info.getRY());
  }
  
  static public boolean hasRefCoord(MandelData data, int x0, int y0, int rx, int ry)
  {
    MandelInfo info=data.getInfo();
    MandelRaster raster = data.getRaster();
    
    if (raster != null) {
      if (x0 < 0) {
        rx -= x0;
        if (rx <= 0) {
          return false;
        }
        x0 = 0;
      }
      if (y0 < 0) {
        ry -= y0;
        if (ry <= 0) {
          return false;
        }
        y0 = 0;
      }

      if (x0 + rx > info.getRX()) {
        rx -= (x0 + rx - info.getRX());
        if (rx <= 0) {
          return false;
        }
      }
      if (y0 + ry > info.getRY()) {
        ry -= (y0 + ry - info.getRY());
        if (ry <= 0) {
          return false;
        }
      }
      System.out.printf("available sub range: (%d,%d)[%d,%d]\n", x0, y0, rx, ry);

      for (int y = y0; y < y0 + ry; y++) {
        for (int x = x0; x < x0 + rx; x++) {
          if (raster.getData(x, y) == 0) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  static public Coord getRefCoord(MandelData data, int x0, int y0, int rx, int ry)
  {
    MandelInfo info=data.getInfo();
    MandelRaster raster = data.getRaster();
    Coord found=null;
    Coord pixel=null;
    int weight=-1;
    
    if (raster != null) {
       if (x0<0) {
         rx-=x0;
         if (rx<=0) return null;
         x0=0;
       }
       if (y0<0) {
         ry-=y0;
          if (ry<=0) return null;
         y0=0;
       }
      
       if (x0+rx > info.getRX()) {
         rx-=(x0+rx-info.getRX());
         if (rx<=0) return null;
       }
       if (y0+ry > info.getRY()) {
         ry-=(y0+ry-info.getRY());
         if (ry<=0) return null;
       }
       System.out.printf("available sub range: (%d,%d)[%d,%d]\n", x0,y0, rx, ry);
       
      for (int y = y0; y < y0 + ry; y++) {
        int startx = -1;
        int endx = -1;
        for (int x = x0; x < x0 + rx; x++) {
          if (raster.getData(x, y) == 0) {
            if (startx < 0) {
              startx = x;
              endx=x0 + rx - 1;
              for (int cx=x; cx < x0 + rx; cx++) {
                if (raster.getData(cx, y) != 0) {
                  endx=cx-1;
                  break;
                }
              }
            }
         
            if (startx >= 0) {
              // lookup range
             
              int starty = 0;
              int endy = y0 + ry - 1;
              for (int cy=y; cy >=0; cy--) {
                if (raster.getData(x, cy) != 0) {
                  starty = cy + 1;
                  break;
                }
              }
              for (int cy=y; cy < y0 + ry; cy++) {
                if (raster.getData(x, cy) != 0) {
                  endy = cy - 1;
                  break;
                }
              }
              
              int wx=Math.min(x-startx, endx-x);
              int wy=Math.min(y-starty, endy-y);

              int w = Math.min(wx,wy);
              if (w > weight) {
                found = new Coord(
                        add(info.getXMin(), div(mul(info.getDX(), x), info.getRX())),
                        sub(info.getYMax(), div(mul(info.getDY(), y), info.getRY())));
                pixel = new Coord(x, y);
                weight = w;
              }
              ///////////////
            }
          }
          else {
            startx=-1;
            endx=-1;
          }
        }
      }
    }
    if (found!=null) {
      System.out.printf("ref coord (%d) at %s=%s\n", weight, pixel, found);
    }
    return found;
  }
  
  static public void updateInfo(MandelInfo info, Rectangle rect,
                                MandelData parent, double scale)
  {
    updateInfo(info, rect, parent.getInfo(), scale);
    MandelRaster raster =parent.getRaster();
    if (raster!=null) {
       int x0=(int)(rect.getX()/scale);
       int y0=(int)(rect.getY()/scale);
       int rx=(int)(rect.getWidth()/scale);
       int ry=(int)(rect.getHeight()/scale);
       System.out.printf("sub range: (%d,%d)[%d,%d]\n", x0,y0, rx, ry);
       Coord ref = getRefCoord(parent, x0, y0, rx, ry);
       if (ref!=null) {
         info.setProperty(MandelInfo.ATTR_REFCOORD, ref.toString());
       }
    }
  }

  static public void updateRect(MandelInfo info, VisibleRect rect,
                                MandelInfo parent, double scale)
  {
    //System.out.println("*** update rect");

    BigDecimal x=sub(parent.getXM(), div(parent.getDX(), 2));
    BigDecimal y=add(parent.getYM(), div(parent.getDY(), 2));

    if (debug) {
      System.out.println("child:");
      System.out.println("   center: "+info.getXM()+","+info.getYM());
      System.out.println("   size  : "+info.getDX()+","+info.getDY());
      System.out.println("old:");
      System.out.println("   start: "+rect.getX()+","+rect.getY());
      System.out.println("   size  : "+rect.getWidth()+","+rect.getHeight());
    }

    rect.setSize(toInt(parent.getRX()*scale*div(info.getDX(), parent.getDX()).
            doubleValue()-D),
                 toInt(parent.getRY()*scale*div(info.getDY(), parent.getDY()).
            doubleValue()-D));

    rect.setLocation(
            toInt(mul(div(sub(sub(info.getXM(), x), div(info.getDX(), 2)), parent.
            getDX()), parent.getRX()*scale).doubleValue()),
            toInt(mul(div(sub(sub(y, info.getYM()), div(info.getDY(), 2)), parent.
            getDY()), parent.getRY()*scale).doubleValue()));

    if (debug) {
      System.out.println("new:");
      System.out.println("   start: "+rect.getX()+","+rect.getY());
      System.out.println("   size  : "+rect.getWidth()+","+rect.getHeight());
      System.out.println("update rect done");
    }
  }
}
