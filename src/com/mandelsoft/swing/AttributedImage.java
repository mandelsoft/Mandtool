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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 *
 * @author Uwe Krueger
 */
public class AttributedImage implements WritableRenderedImage, Transparency {
  BufferedImage image;
  Properties    props;

  public AttributedImage(BufferedImage image)
  {
    this.image=image;
    props=new Properties();
  }

  private boolean isEmpty(Object o)
  {
    if (o==null) return true;
    if (o instanceof String) return ((String)o).equals("");
    return false;
  }
  
  public synchronized Object setProperty(String key, String value)
  {
    String[] names=image.getPropertyNames();
    if (names!=null) {
      for (String p:names) if (p.equals(key))
        throw new IllegalArgumentException("property "+key+" cannot be overwritten");
    }
    return props.setProperty(key, value);
  }

  public String[] getPropertyNames()
  {
    List<String> r=new ArrayList<String>();
    String[] names=image.getPropertyNames();
    if (names!=null) for (String p:names) {
      r.add(p);
    }
    for (String p:props.stringPropertyNames()) {
      if (!r.contains(p)) r.add(p);
    }
    return r.toArray(new String[r.size()]);
  }

  public Object getProperty(String name)
  {
    String v=props.getProperty(name);
    if (v!=null) return v;
    return image.getProperty(name);
  }

  public Object getProperty(String name, ImageObserver observer)
  {
    Object o=image.getProperty(name, observer);
    if (o!=null) return o;
    return props.getProperty(name);
  }

  /////////////////////////////////////////////////////////////////////////

  @Override
  public String toString()
  {
    return image.toString();
  }

  public void setRGB(int startX, int startY, int w, int h, int[] rgbArray,
                     int offset, int scansize)
  {
    image.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
  }

  public synchronized void setRGB(int x, int y, int rgb)
  {
    image.setRGB(x, y, rgb);
  }

  public void setData(Raster r)
  {
    image.setData(r);
  }

  public void removeTileObserver(TileObserver to)
  {
    image.removeTileObserver(to);
  }

  public void releaseWritableTile(int tileX, int tileY)
  {
    image.releaseWritableTile(tileX, tileY);
  }

  public boolean isTileWritable(int tileX, int tileY)
  {
    return image.isTileWritable(tileX, tileY);
  }

  public boolean isAlphaPremultiplied()
  {
    return image.isAlphaPremultiplied();
  }

  public boolean hasTileWriters()
  {
    return image.hasTileWriters();
  }

  public Point[] getWritableTileIndices()
  {
    return image.getWritableTileIndices();
  }

  public WritableRaster getWritableTile(int tileX, int tileY)
  {
    return image.getWritableTile(tileX, tileY);
  }

  public int getWidth(ImageObserver observer)
  {
    return image.getWidth(observer);
  }

  public int getWidth()
  {
    return image.getWidth();
  }

  public int getType()
  {
    return image.getType();
  }

  public int getTransparency()
  {
    return image.getTransparency();
  }

  public int getTileWidth()
  {
    return image.getTileWidth();
  }

  public int getTileHeight()
  {
    return image.getTileHeight();
  }

  public int getTileGridYOffset()
  {
    return image.getTileGridYOffset();
  }

  public int getTileGridXOffset()
  {
    return image.getTileGridXOffset();
  }

  public Raster getTile(int tileX, int tileY)
  {
    return image.getTile(tileX, tileY);
  }

  public BufferedImage getSubimage(int x, int y, int w, int h)
  {
    return image.getSubimage(x, y, w, h);
  }

  public Vector<RenderedImage> getSources()
  {
    return image.getSources();
  }

  public ImageProducer getSource()
  {
    return image.getSource();
  }

  public SampleModel getSampleModel()
  {
    return image.getSampleModel();
  }

  public WritableRaster getRaster()
  {
    return image.getRaster();
  }

  public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray,
                      int offset, int scansize)
  {
    return image.getRGB(startX, startY, w, h, rgbArray, offset, scansize);
  }

  public int getRGB(int x, int y)
  {
    return image.getRGB(x, y);
  }

  public int getNumYTiles()
  {
    return image.getNumYTiles();
  }

  public int getNumXTiles()
  {
    return image.getNumXTiles();
  }

  public int getMinY()
  {
    return image.getMinY();
  }

  public int getMinX()
  {
    return image.getMinX();
  }

  public int getMinTileY()
  {
    return image.getMinTileY();
  }

  public int getMinTileX()
  {
    return image.getMinTileX();
  }

  public int getHeight(ImageObserver observer)
  {
    return image.getHeight(observer);
  }

  public int getHeight()
  {
    return image.getHeight();
  }

  public Graphics getGraphics()
  {
    return image.getGraphics();
  }

  public Raster getData(Rectangle rect)
  {
    return image.getData(rect);
  }

  public Raster getData()
  {
    return image.getData();
  }

  public ColorModel getColorModel()
  {
    return image.getColorModel();
  }

  public WritableRaster getAlphaRaster()
  {
    return image.getAlphaRaster();
  }

  public Graphics2D createGraphics()
  {
    return image.createGraphics();
  }

  public WritableRaster copyData(WritableRaster outRaster)
  {
    return image.copyData(outRaster);
  }

  public void coerceData(boolean isAlphaPremultiplied)
  {
    image.coerceData(isAlphaPremultiplied);
  }

  public void addTileObserver(TileObserver to)
  {
    image.addTileObserver(to);
  }

  ///////////////////////////////////////////////////////////////////////////

  static void print(WritableRenderedImage img)
  {
    String[] names=img.getPropertyNames();
    if (names!=null) for (String p: names) {
      System.out.println("  "+p+": "+img.getProperty(p));
    }
  }

  static public void main(String[] args)
  {
    BufferedImage image;
    AttributedImage attr;
    String target;
    String suffix=null;
    if (args.length>0) {
      try {
        int ix=args[0].lastIndexOf('.');
        if (ix>0) suffix=args[0].substring(ix+1);
        System.out.println("handling "+args[0]);
        image=ImageIO.read(new File(args[0]));
        System.out.println("FOUND:");
        print(image);
        attr=new AttributedImage(image);
        attr.setProperty("Test", "Dies ist ein Test.");
        System.out.println("MOD:");
        print(attr);
        if (args.length>1) target=args[1];
        else target=args[0];
        ix=target.lastIndexOf('.');
        if (suffix==null) suffix="png";
        if (ix>0) suffix=target.substring(ix+1);
        else {
          target=target+"."+suffix;
        }
        try {
          if (!ImageIO.write(attr, suffix, new File(target))) {
            System.out.println("not written");
          }
        }
        catch (IOException io) {
          System.out.println("cannot write "+target+": "+io);
        }
      }
      catch (IOException ex) {
        System.out.println("cannot read "+args[0]+": "+ex);
      }
    }
  }
}
