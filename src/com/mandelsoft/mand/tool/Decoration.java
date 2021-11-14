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
  protected static final int DEFAULT_INSET=10;

  public static final int ALIGN_CENTER=0;
  public static final int ALIGN_LEFT=-1;
  public static final int ALIGN_RIGHT=1;
  public static final int ALIGN_TOP=-1;
  public static final int ALIGN_BOTTOM=1;

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
  
  public interface ColorHandler {
    public Color getColor(int x, int y, int w, int h);
  }
  
  public static class StaticColor implements ColorHandler {
    private Color color;
    
    public StaticColor(Color color)
    {
      this.color=color;
    }
    
    public Color getColor(int x, int y, int w, int h)
    {
      return color;
    }
  }

  //////////////////////////////////////////////////////////////////////////
  protected int h_inset=DEFAULT_INSET;
  protected int v_inset=DEFAULT_INSET;
  protected int h_align=ALIGN_RIGHT;
  protected int v_align=ALIGN_BOTTOM;


  protected String decoration="by Uwe Krüger";
  protected  int dw, dh;
  private Font font;
  private float size;
  private ColorHandler color=new StaticColor(Color.WHITE);
  private int alpha=255;
  private boolean showDecoration;

  public Decoration()
  {
  }

  public Decoration(String txt)
  {
    decoration=txt;
  }

  public void reset()
  {
    dw=dh=0;
  }

  public int getHAlign()
  {
    return h_align;
  }

  public void setHAlign(int h_align)
  {
    this.h_align=h_align;
  }

  public int getHInset()
  {
    return h_inset;
  }

  public void setHInset(int h_inset)
  {
    this.h_inset=h_inset;
  }

  public int getVAlign()
  {
    return v_align;
  }

  public void setVAlign(int v_align)
  {
    this.v_align=v_align;
  }

  public int getVInset()
  {
    return v_inset;
  }

  public void setVInset(int v_inset)
  {
    this.v_inset=v_inset;
  }

  public void setDecoration(String s)
  {
    if (s==null||decoration==null||!s.equals(decoration)) {
      decoration=s;
      System.out.println("decoration is "+decoration);
      reset();
    }
  }

  public String getDecoration()
  {
    return decoration;
  }

  public void setShowDecoration(boolean showDecoration)
  {
    System.out.println("decoration enabled "+showDecoration);
    this.showDecoration=showDecoration;
  }

  public boolean showDecoration()
  {
    boolean enabled=showDecoration && decoration!="" && alpha!=0;
    System.out.println("decoration is "+(enabled?"enabled":"disabled"));
    return enabled;
  }

  public void setAlpha(int a)
  {
    alpha=a;
  }

  public void setFontSize(float s)
  {
    if (font!=null) {
      font=font.deriveFont(s);
    }
    size=s;
    reset();
  }

  public void setColorHandler(ColorHandler color)
  {
    this.color=color;
  }
  
  public void setColor(Color color)
  {
    this.color=new StaticColor(color);
  }

  protected Color getColor(int w, int h)
  {
    int dx=getX(w,h,dw,dh);
    int dy=getY(w,h,dw,dh);
    System.out.printf("decoration dimension (%d,%d)@(%d,%d) in (%d,%d)\n", dw, dh, dx,dy, w,h);
    Color c=color.getColor(dx,dy,dw,dh);
    return new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
  }

  public void setFont(Font f)
  {
    font=f;
    size=f.getSize();
  }

  public void setFont(String f)
  {
    font=Font.decode(f);
  }
  
  public Font getFont()
  {
    return font;
  }

  protected int getX(int w, int h, int dw, int dh)
  {
    switch (h_align) {
      case ALIGN_LEFT:   return h_inset;
      case ALIGN_RIGHT:  return w-h_inset-dw;
      case ALIGN_CENTER: return (w-dw)/2+h_inset;
    }
    throw new IllegalArgumentException("illegal horizontal alignment");
  }

  protected int getY(int w, int h, int dw, int dh)
  {
    switch (v_align) {
      case ALIGN_TOP:    return v_inset+dh;
      case ALIGN_BOTTOM: return h-v_inset;
      case ALIGN_CENTER:
        System.out.println("h="+h+",dh="+dh+", inset="+v_inset);
        return (h+dh)/2+v_inset;
    }
    throw new IllegalArgumentException("illegal vertical alignment");
  }

  protected void draw(Graphics g, int w, int h, int dw, int dh)
  {
    int dx=getX(w,h,dw,dh);
    int dy=getY(w,h,dw,dh);

    //System.out.println("decoration: "+dx+","+dy+" "+decoration);

    g.drawString(decoration, dx, dy);
    //g.drawLine(dx, dy, dx+dw, dy);
    //g.drawLine(dx, dy-dh, dx+dw, dy-dh);
  }

  public void paintDecoration(Graphics g, int w, int h)
  {
    if (decoration!=null) {
      setup(g, w, h);
      draw(g,w,h, dw,dh);
//        System.out.println("decoration bounds "+g.getClipBounds().getWidth()
//                                           +","+g.getClipBounds().getHeight());
    }
  }

  public void setup(Graphics g, int w, int h)
  {
    prepare(g);
    System.out.printf("****** deco for area (%d,%d)\n", w, h);
    g.setColor(getColor(w,h));
    g.setFont(font);
  }

  public void prepare(Graphics g)
  {
    if (dw==0) {
      if (font==null) {
        font=getAnnotationFont(g);
        if (size!=0) font=font.deriveFont(size);
        else size=font.getSize();
      }
//          String[] f=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//          if (f!=null) for (String s:f) {
//            System.out.println("  "+s);
//          }
      FontMetrics metrics=g.getFontMetrics(font);
      // get the height of a line of text in this font and render context
      dh=metrics.getAscent();
      // get the advance of my text in this font and render context
      dw=metrics.stringWidth(decoration);
    }
  }

  @Override
  public String toString()
  {
    return ""+decoration+"("+showDecoration()+")";
  }
}
