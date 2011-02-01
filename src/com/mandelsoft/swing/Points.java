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

import java.awt.geom.Point2D;

/**
 *
 * @author Uwe Krüger
 */

public class Points {

  public static String toString(Point2D lo)
  {
    return "("+lo.getX()+","+lo.getY()+")";
  }

  public static class mod {
    public static void translate(Point2D p, double x, double y)
    {
      p.setLocation(p.getX()+x, p.getY()+y);
    }
  }

  public static class op {
    public static Point2D translate(Point2D p, double x, double y)
    {
      return new Point2D.Double(p.getX()+x, p.getY()+y);
    }
  }
}
