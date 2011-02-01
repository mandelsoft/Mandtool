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
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Uwe Krüger
 */

public class Rectangles {

  private static class Unmodifiable extends Rectangle2D.Double {

    public Unmodifiable(double x, double y, double w, double h)
    {
      super.setRect(x, y, w, h);
    }

    @Override
    public void setRect(double x, double y, double w, double h)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setRect(Rectangle2D r)
    {
      throw new UnsupportedOperationException();
    }

  }

  public static String toString(Rectangle2D lo)
  {
    if (lo==null) return "[null]";
    return "["+lo.getX()+","+lo.getY()+","+lo.getWidth()+","+lo.getHeight()+"]";
  }

  public static Rectangle2D unmodifiable(Rectangle2D r)
  {
    if (r==null || r instanceof Unmodifiable) return r;
    return new Unmodifiable(r.getX(),r.getY(),r.getWidth(),r.getHeight());
  }

  public static Rectangle2D create(Rectangle2D r)
  {
    if (r==null) return null;
    return new Rectangle2D.Double(r.getX(),r.getY(),r.getWidth(),r.getHeight());
  }

  public static Rectangle2D create(Point2D p1, Point2D p2)
  {
    double x1 = Math.min(p1.getX(), p2.getX());
	  double x2 = Math.max(p1.getX(), p2.getX());
	  double y1 = Math.min(p1.getY(), p2.getY());
	  double y2 = Math.max(p1.getY(), p2.getY());
    return new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
  }

  public static Rectangle2D create(Interval2D x, Interval2D y)
  {
    return new Rectangle2D.Double(x.getMin(), y.getMin(), x.size(), y.size());
  }

  ///////////////////////////////////////////////////////////////////////////

  public static Point2D location(Rectangle2D r)
  {
    return new Point2D.Double(r.getX(), r.getY());
  }

  public static Dimension2D size(Rectangle2D r)
  {
    return new Dimensions.Dimension(r.getWidth(),r.getHeight());
  }

  public static Point2D center(Rectangle2D r)
  {
    return new Point2D.Double(r.getCenterX(), r.getCenterY());
  }

  ///////////////////////////////////////////////////////////////////////////
  static public class mod {

    public static void setSize(Rectangle2D r, double w, double h)
    {
      r.setFrame(r.getX(), r.getY(), w, h);
    }

    public static void setSize(Rectangle2D r, Dimension2D d)
    {
      r.setFrame(r.getX(), r.getY(), d.getWidth(), d.getHeight());
    }

    public static void setLocation(Rectangle2D r, double x, double y)
    {
      r.setFrame(x, y, r.getWidth(), r.getHeight());
    }

    public static void setLocation(Rectangle2D r, Point2D p)
    {
      r.setFrame(p.getX(), p.getY(), r.getWidth(), r.getHeight());
    }


    public static void translate(Rectangle2D r, Dimension2D d)
    {
      translate(r, d.getWidth(), d.getHeight());
    }

    public static void translate(Rectangle2D r, double dx, double dy)
    {
      r.setFrame(r.getX()+dx,
                 r.getY()+dy,
                 r.getWidth(),
                 r.getHeight());
    }

    
    public static void grow(Rectangle2D r, Dimension2D d)
    {
      grow(r, d.getWidth(), d.getHeight());
    }

    public static void  grow(Rectangle2D r, double dx, double dy)
    {
      r.setFrame(r.getX(), r.getY(),
                 r.getWidth()+dx, r.getHeight()+dy);
    }


    public static void shrink(Rectangle2D r, Dimension2D d)
    {
      shrink(r, d.getWidth(), d.getHeight());
    }

    public static void shrink(Rectangle2D r, double dx, double dy)
    {
      r.setFrame(r.getX(), r.getY(), r.getWidth()-dx, r.getHeight()-dy);
    }

    public static void limit(Rectangle2D r, Dimension2D d)
    {
      limit(r, d.getWidth(), d.getHeight());
    }

    public static void limit(Rectangle2D r, double w, double h)
    {
      r.setFrame(r.getX(), r.getY(), Math.min(r.getWidth(), w),
                                     Math.min(r.getHeight(), h));
    }
  }

  ///////////////////////////////////////////////////////////////////////////


  static public class op {

    public static Rectangle2D translate(Rectangle2D r, Dimension2D d)
    {
      return translate(r, d.getWidth(), d.getHeight());
    }

    public static Rectangle2D translate(Rectangle2D r, double dx, double dy)
    {
      return new Rectangle2D.Double(r.getX()+dx,
                                    r.getY()+dy,
                                    r.getWidth(),
                                    r.getHeight());
    }

    public static Rectangle2D grow(Rectangle2D r, Dimension2D d)
    {
      return grow(r, d.getWidth(), d.getHeight());
    }

    public static Rectangle2D grow(Rectangle2D r, double dx, double dy)
    {
      return new Rectangle2D.Double(r.getX(), r.getY(),
                                    r.getWidth()+dx, r.getHeight()+dy);
    }

    public static Rectangle2D shrink(Rectangle2D r, Dimension2D d)
    {
      return shrink(r, d.getWidth(), d.getHeight());
    }

    public static Rectangle2D shrink(Rectangle2D r, double dx, double dy)
    {
      return new Rectangle2D.Double(r.getX(), r.getY(),
                                    r.getWidth()-dx, r.getHeight()-dy);
    }
  }
}
