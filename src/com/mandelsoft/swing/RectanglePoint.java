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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Uwe Krueger
 */
public class RectanglePoint {
  public static RectanglePoint MIDDLE_LEFT=new RectanglePoint("MiddleLeft",
                                                0, 0.5, Cursor.E_RESIZE_CURSOR);
  public static RectanglePoint MIDDLE_TOP=new RectanglePoint("MiddleTop",
                                                0.5, 0, Cursor.N_RESIZE_CURSOR);
  public static RectanglePoint MIDDLE_RIGHT=new RectanglePoint("MiddleRight",
                                                1, 0.5, Cursor.W_RESIZE_CURSOR);
  public static RectanglePoint MIDDLE_BOTTOM=new RectanglePoint("MiddleBottom",
                                                0.5, 1, Cursor.S_RESIZE_CURSOR);

  private String name;
  private double factorX;
  private double factorY;
  private int    cursor;

  protected RectanglePoint(String name, double factorX, double factorY,
                           int cursor)
  {
    this.name=name;
    this.factorX=factorX;
    this.factorY=factorY;
    this.cursor=cursor;
  }

  public String getName()
  {
    return name;
  }

  public double getFactorX()
  {
    return factorX;
  }

  public double getFactorY()
  {
    return factorY;
  }

  public Cursor getCursor()
  {
    return Cursor.getPredefinedCursor(cursor);
  }

  public Point getPoint(Rectangle2D rect)
  {
    return new Point((int)(rect.getX()+getFactorX()*(rect.getWidth()-1)),
                     (int)(rect.getY()+getFactorY()*(rect.getHeight()-1)));
  }

  @Override
  public String toString()
  {
    return "Point "+getName();
  }
}
