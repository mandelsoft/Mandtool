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
 * @author Uwe Krüger
 */

public class Corner extends RectanglePoint {

  public static final Corner TOP_LEFT=new Corner("TopLeft", 0, 0,
                                                 Cursor.NW_RESIZE_CURSOR);
  public static final Corner TOP_RIGHT=new Corner("TopRight", 1, 0,
                                                 Cursor.NE_RESIZE_CURSOR);
  public static final Corner BOTTOM_RIGHT=new Corner("BottomRight", 1, 1,
                                                 Cursor.SE_RESIZE_CURSOR);
  public static final Corner BOTTOM_LEFT=new Corner("BottomLeft", 0, 1,
                                                 Cursor.SW_RESIZE_CURSOR);

  static {
    CornerSideCrossLink.setup();
  }

  static void setup()
  {
    TOP_LEFT.setup(BOTTOM_RIGHT, Side.LEFT,
                   Side.TOP);
    TOP_RIGHT.setup(BOTTOM_LEFT, Side.TOP,
                    Side.RIGHT);
    BOTTOM_RIGHT.setup(TOP_LEFT, Side.RIGHT,
                       Side.BOTTOM);
    BOTTOM_LEFT.setup(TOP_RIGHT, Side.BOTTOM,
                      Side.LEFT);
  }

  private Corner opposite;
  private Side leftSide;
  private Side rightSide;
  private int cursor;

  private Corner(String name, int x, int y, int cursor)
  {
    super(name,x,y, cursor);
  }

  private void setup(Corner c, Side l,
                     Side r)
  {
    this.opposite=c;
    this.leftSide=l;
    this.rightSide=r;
  }

  public Corner getOppositeCorner()
  {
    return opposite;
  }

  public Side getLeftSide()
  {
    return leftSide;
  }

  public Side getRightSide()
  {
    return rightSide;
  }

  @Override
  public String toString()
  {
    return "Corner "+getName();
  }
}
