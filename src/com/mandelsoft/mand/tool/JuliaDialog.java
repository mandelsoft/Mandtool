
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.swing.BufferedComponent;

/**
 *
 * @author Uwe Krüger
 */

public class JuliaDialog extends MandelDialog {
  private BufferedComponent bc;
  private BufferedImage image;
  private JPanel panel;
  private Colormap colormap;

  public JuliaDialog(MandelWindowAccess frame, int w, int h)
  { super(frame);
    setTitle("Julia Test");
    panel=new JuliaPanel();
    image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
    panel.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
    add(panel);
    pack();
    setResizable(true);
  }

  private class JuliaPanel extends JPanel {

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

  public void update(double x, double y)
  {
    colormap=getMandelWindowAccess().getMandelImage().getColormap();
    jx=x;
    jy=y;
    updateImage();
  }

  private void updateImage()
  {
    setupContext();
    calc();
    repaint();
  }

   ////////////////////////////////////////////////////////////////////////
  // calculation
  ////////////////////////////////////////////////////////////////////////

  private double BOUND=10;

  private double jx;
  private double jy;

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
    dx=4.5;
   
    limit=30000;
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
    for (int y=0; y<ry; y++) {
      double cy=y0-(y*dy)/dry;
      for (int x=0; x<rx; x++) {
        double cx=x0+(x*dx)/drx;
          int i=iter(cx, cy,jx,jy);
          image.setRGB(x, y, colormap.getColor(map(i)).getRGB());
      }
    }
  }

  private int map(int i)
  { if (i>limit) return 0;
    return 1+i%(colormap.getSize()-1);
  }

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
    }
    return it;
  }
}
