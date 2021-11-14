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

import com.mandelsoft.mand.MandIter;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.tools.Mand;
import com.mandelsoft.swing.BufferedComponent;
import java.awt.Color;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krüger
 */

public class IterationPathDialog extends MandelDialog implements MandIter.IterationSink {
  private BufferedComponent bc;
  private BufferedImage image;
  private JPanel panel;
  private Colormap colormap;

  public IterationPathDialog(MandelWindowAccess frame, int w, int h)
  { super(frame);
    setTitle("Iteration Path");
    panel=new IterationPathPanel();
    image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
    panel.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
    add(panel);
    pack();
    setResizable(true);
  }

  private class IterationPathPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g)
    {
      Rectangle r=getVisibleRect();
      //System.out.println(r);
      if (r.getWidth()!=image.getWidth() || r.getHeight()!=image.getHeight()) {
        image=new BufferedImage((int)r.getWidth(),(int)r.getHeight(),
                                 BufferedImage.TYPE_INT_RGB);
        updateImage();
      }
      g.drawImage(image,0,0,null);
    }

  }

  public void setValue(int it, MandIter.Coord c)
  {
    int x = (int) ((c.getX() - x0) * rx / dx);
    int y = (int) ((y0 - c.getY()) * ry / dy);

    if (x >= 0 && x < rx && y >= 0 && y < ry) {
      image.setRGB(x, y, colormap.getColor(map(it)).getRGB());
    }
  }
  
  public void update(int limit, BigDecimal x, BigDecimal y)
  {
    colormap=getMandelWindowAccess().getMandelImage().getColormap();
    this.jx=x;
    this.jy=y;
    this.limit=limit;
    updateImage();
  }

  private void updateImage()
  {
    System.out.println("update path image x="+jx+", y="+jy);
    setupContext();
    Graphics g=image.createGraphics();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, image.getWidth(), image.getHeight());
    calc();
    repaint();
  }

  ////////////////////////////////////////////////////////////////////////
  // calculation
  ////////////////////////////////////////////////////////////////////////

  private BigDecimal BOUND=new BigDecimal(Mand.BOUND);

  private BigDecimal jx;
  private BigDecimal jy;

  private double xm;
  private double ym;
  private double dx;
  private double dy;
  private int rx;
  private int ry;
  private double x0;
  private double y0;
  private int limit;

  private double drx;
  private double dry;

  private void setupContext()
  {
    xm=0;
    ym=0;
    dx=2;
    
    rx=image.getWidth();
    ry=image.getHeight();
    dy=dx*ry/rx;
    drx=rx;
    dry=ry;

    x0=xm-dx/2;
    y0=ym+dy/2;
  }

  private void calc()
  {
    int i=MandIter.buildref(this, new MandIter.Coord(jx,jy), limit, BOUND);
    System.out.println("it="+i);
    // int i=iter(0, 0, jx, jy);
  }

  private int map(int i)
  { if (i>limit) return 0;
    return 1+i%(colormap.getSize()-1);
  }

 /*
  private int iter(double x, double y, double px, double py)
  {
    double x2=x*x;
    double y2=y*y;
    int it=0;

    while (x2+y2<BOUND&&++it<=limit) {
      double xn=x2-y2+px;
      double yn=2*x*y+py;
      x=xn;
      x2=x*x;
      y=yn;
      y2=y*y;

      int ix=(int)((x-x0)*drx/dx);
      int iy=(int)((y0-y)*dry/dy);
      //System.out.println("ix="+ix+", iy="+iy);
      if (ix>=0 && ix<image.getWidth() &&
          iy>=0 && iy<image.getHeight()) {
        image.setRGB(ix, iy, colormap.getColor(map(it)).getRGB());
      }
    }
    System.out.println("it="+it);
    return it;
  }
*/
}
