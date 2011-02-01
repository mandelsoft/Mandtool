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

public class Side {

  public static final Side TOP=new Side("Top");
  public static final Side RIGHT=new Side("Right");
  public static final Side BOTTOM=new Side("Bottom");
  public static final Side LEFT=new Side("Left");
  static {
    CornerSideCrossLink.setup();
  }

  static void setup()
  {
    TOP.setup(BOTTOM, Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.TOP_LEFT,
            Cursor.N_RESIZE_CURSOR);
    RIGHT.setup(LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT, Corner.TOP_RIGHT,
            Cursor.E_RESIZE_CURSOR);
    BOTTOM.setup(TOP, Corner.BOTTOM_RIGHT, Corner.BOTTOM_LEFT,
                 Corner.BOTTOM_LEFT, Cursor.S_RESIZE_CURSOR);
    LEFT.setup(RIGHT, Corner.BOTTOM_LEFT, Corner.TOP_LEFT, Corner.TOP_LEFT,
                 Cursor.W_RESIZE_CURSOR);
  }

  private String name;
  private Side opposite;
  // corner are taken clockwise arround the rectange
  private Corner leftCorner;
  private Corner rightCorner;
  private Corner lowerCorner;
  private Corner higherCorner;
  private int cursor;

  private Side(String name)
  {
    super();
    this.name=name;
  }

  private void setup(Side c, Corner l, Corner r, Corner lc, int cursor)
  {
    this.opposite=c;
    this.leftCorner=l;
    this.rightCorner=r;
    this.lowerCorner=lc;
    this.higherCorner=(lc==l?r:l);
    this.cursor=cursor;
    if (l==null) {
      throw new IllegalStateException(name+" l");
    }
    if (r==null) {
      throw new IllegalStateException(name+" r");
    }
  }

  public String getName()
  {
    return name;
  }

  public Side getOppositeSide()
  {
    return opposite;
  }

  public Corner getLeftCorner()
  {
    return leftCorner;
  }

  public Corner getRightCorner()
  {
    return rightCorner;
  }

  public Corner getLowerCorner()
  {
    return lowerCorner;
  }

  public Corner getHigherCorner()
  {
    return higherCorner;
  }

  public Cursor getCursor()
  {
    return Cursor.getPredefinedCursor(cursor);
  }

  @Override
  public String toString()
  {
    return "Side "+name;
  }
}
