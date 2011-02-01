
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

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Uwe Krüger
 */

public class Dimensions {

  public static class Dimension extends Dimension2D {
    private double width;
    private double height;

    public Dimension()
    {
    }

    public Dimension(Dimension2D h)
    {
      this(h.getWidth(),h.getHeight());
    }

    public Dimension(double width, double height)
    {
      this.width=width;
      this.height=height;
    }

    @Override
    public double getWidth()
    {
      return width;
    }

    @Override
    public double getHeight()
    {
      return height;
    }

    @Override
    public void setSize(double width, double height)
    {
      this.width=width;
      this.height=height;
    }

    public void setHeight(double height)
    {
      this.height=height;
    }

    public void setWidth(double width)
    {
      this.width=width;
    }
  }

  /////////////////////////////////////////////////////////////////////////

  public static String toString(Dimension2D lo)
  {
    return "["+lo.getWidth()+","+lo.getHeight()+"]";
  }

  static public boolean empty(Dimension2D d)
  {
    return d.getWidth()==0&&d.getHeight()==0;
  }

  static public boolean isNegative(Dimension2D d)
  {
    return d.getHeight()<0 || d.getWidth()<0;
  }
  
  /////////////////////////////////////////////////////////////////////////

  public static class mod {

    /**
     * adds the second argument to the first one.
     * /
     * @param d dimenasion to add to
     * @param d2 dimension to add
     */
    static public void add(Dimension2D d, Dimension2D d2)
    {
      d.setSize(d.getWidth()+d2.getWidth(), d.getHeight()+d2.getHeight());
    }

    static public void add(Dimension2D d, double w, double h)
    {
      d.setSize(d.getWidth()+w, d.getHeight()+h);
    }

    static public void addX(Dimension2D d, Interval2D w)
    {
      d.setSize(d.getWidth()+w.size(), d.getHeight());
    }

    static public void addX(Dimension2D d, double w)
    {
      d.setSize(d.getWidth()+w, d.getHeight());
    }

    static public void addY(Dimension2D d, Interval2D h)
    {
      d.setSize(d.getWidth(), d.getHeight()+h.size());
    }

    static public void addY(Dimension2D d, double h)
    {
      d.setSize(d.getWidth(), d.getHeight()+h);
    }

    public static void neg(Dimension2D d)
    {
      d.setSize(-d.getWidth(), -d.getHeight());
    }

    /**
     * subtract the second argument from the first one.
     * /
     * @param d dimenasion to sub to
     * @param d2 dimension to sub
     */
    static public void sub(Dimension2D d, Dimension2D d2)
    {
      d.setSize(d.getWidth()-d2.getWidth(), d.getHeight()-d2.getHeight());
    }

    static public void sub(Dimension2D d, double w, double h)
    {
      d.setSize(d.getWidth()-w, d.getHeight()-h);
    }

    static public void subX(Dimension2D d, Interval2D w)
    {
      d.setSize(d.getWidth()-w.size(), d.getHeight());
    }

    static public void subX(Dimension2D d, double w)
    {
      d.setSize(d.getWidth()-w, d.getHeight());
    }

    static public void subY(Dimension2D d, Interval2D h)
    {
      d.setSize(d.getWidth(), d.getHeight()-h.size());
    }

    static public void subY(Dimension2D d, double h)
    {
      d.setSize(d.getWidth(), d.getHeight()-h);
    }

    static public void extend(Dimension2D d1, Dimension2D d2)
    {
      if (d2.getWidth()>d1.getWidth())
        d1.setSize(d2.getWidth(), d1.getHeight());
      if (d2.getHeight()>d1.getHeight()) d1.setSize(d1.getWidth(),
                                                    d2.getHeight());
    }

    static public void limit(Dimension2D d, Dimension2D d2)
    {
      limit(d, d2.getWidth(), d2.getHeight());
    }

    static public void limit(Dimension2D d, double w, double h)
    {
      if (d.getWidth()<w) w=d.getWidth();
      if (d.getHeight()<h) h=d.getHeight();
      d.setSize(w, h);
    }

    static public void limitMin(Dimension2D d, double w, double h)
    {
      if (d.getWidth()>w) w=d.getWidth();
      if (d.getHeight()>h) h=d.getHeight();
      d.setSize(w, h);
    }

    static public void translate(Point2D p, Dimension2D d)
    {
      p.setLocation(p.getX()+d.getWidth(), p.getY()+d.getHeight());
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  public static class op {
    public static Dimension2D add(Dimension2D a, Dimension2D b)
    {
      return new Dimension(a.getWidth()+b.getWidth(),
                           a.getHeight()+b.getHeight());
    }

    public static Dimension2D sub(Dimension2D a, Dimension2D b)
    {
      return new Dimension(a.getWidth()-b.getWidth(),
                           a.getHeight()-b.getHeight());
    }

    public static Dimension2D neg(Dimension2D d)
    {
      return new Dimension(-d.getWidth(), -d.getHeight());
    }
  }

}
