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

/**
 *
 * @author Uwe Krueger
 */
public class GBCSupportPanel extends IJPanel {
  static boolean debug=false;

  protected GBC GBC(int x, int y)
  {
    return new GBC(x, y).setLayout(GBC.NONE, GBC.CENTER);
  }

  protected GBC GBC(int x, int y, int w, int h)
  {
    return new GBC(x, y, w, h).setLayout(GBC.NONE, GBC.CENTER);
  }

  protected GBC GBC(int x, int y, int w, int h, int fill)
  {
    return GBC(x,y,fill).setSpan(w, h);
  }

  protected GBC GBC(int x, int y, int fill)
  {
    return GBC(x,y).setWeight(weightX(fill),weightY(fill)).setFill(fill);
  }

  protected double weightX(int fill)
  {
    if (fill==GBC.BOTH || fill==GBC.HORIZONTAL) return 10;
    return 0;
  }
  
  protected double weightY(int fill)
  {
    if (fill==GBC.BOTH || fill==GBC.VERTICAL) return 10;
    return 0;
  }
}
