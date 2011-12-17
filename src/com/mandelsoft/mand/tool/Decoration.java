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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 *
 * @author Uwe Krueger
 */
public class Decoration {

  static private String[] fontnames=new String[] {
    "Brush Script MT-18",
    "Forte-16",
    //"Mistral",
    "Rage Italic-16",
    //"Viner Hand ITC",
    "Chiller-18",
    "Script MT Bold-18",
     "Times New Roman-ITALIC-16"
  };

  static public Font getAnnotationFont(Graphics g)
  {
    Font font=null;
    for (String name:fontnames) {
      font=Font.decode(name);
      if (font!=null) {
        System.out.println("found font "+name);
        break;
      }
    }
    if (font==null) {
      font=g.getFont().deriveFont(Font.ITALIC, 16);
    }
    return font;
  }

  //////////////////////////////////////////////////////////////////////////
  private int INSET=10;
  private String decoration="by Uwe Krüger";
  private int dw, dh;
  private Font font;
  private boolean showDecoration;

  public void setDecoration(String s)
  {
    decoration=s;
    dw=dh=0;
  }

  public String getDecoration()
  {
    return decoration;
  }

  public void setShowDecoration(boolean showDecoration)
  {
    this.showDecoration=showDecoration;
  }

  public boolean showDecoration()
  {
    return showDecoration;
  }

  public void paintDecoration(Graphics g, int w, int h)
  {
    if (decoration!=null) {
      if (dw==0) {
        if (font==null) {
          font=getAnnotationFont(g);
        }
//          String[] f=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//          if (f!=null) for (String s:f) {
//            System.out.println("  "+s);
//          }
        g.setFont(font);
        FontMetrics metrics=g.getFontMetrics();
        // get the height of a line of text in this font and render context
        dh=metrics.getHeight();
        // get the advance of my text in this font and render context
        dw=metrics.stringWidth(decoration);
      }
      else g.setFont(font);
      int dx=w-INSET-dw;
      int dy=h-INSET;

      //System.out.println("decoration: "+dx+","+dy+" "+decoration);
      g.setColor(Color.WHITE);
      g.drawString(decoration, dx, dy);

//        System.out.println("decoration bounds "+g.getClipBounds().getWidth()
//                                           +","+g.getClipBounds().getHeight());
    }
    }
}
