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

import java.awt.Rectangle;
import java.math.BigDecimal;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;

/**
 *
 * @author Uwe Krüger
 */
public class ToolUtils extends MandUtils {

  static private final boolean debug=false;
  static private final double D=-0.5;

  static public void updateInfo(MandelInfo info, Rectangle rect,
                                MandelInfo parent, double scale)
  {
    if (debug) {
      System.out.println("*** update info");
      System.out.println("parent:");
      System.out.println("   center: "+parent.getXM()+","+parent.getYM());
      System.out.println("   size  : "+parent.getDX()+","+parent.getDY());
    }

    BigDecimal x=sub(parent.getXM(), div(parent.getDX(), 2));
    BigDecimal y=add(parent.getYM(), div(parent.getDY(), 2));

    double rx=rect.getX()+(rect.getWidth()+D)/2;
    double ry=rect.getY()+(rect.getHeight()+D)/2;

    info.setXM(add(x, mul(parent.getDX(), rx/parent.getRX()/scale)));
    info.setYM(sub(y, mul(parent.getDY(), ry/parent.getRY()/scale)));

    info.setDX(mul(parent.getDX(), (rect.getWidth()+D)/parent.getRX()/scale));
    info.setDY(mul(parent.getDY(), (rect.getHeight()+D)/parent.getRY()/scale));

    if (debug) {
      System.out.println("child:");
      System.out.println("   center: "+info.getXM()+","+info.getYM());
      System.out.println("   size  : "+info.getDX()+","+info.getDY());
    }
  }

  static public void updateRect(MandelInfo info, VisibleRect rect,
                                MandelInfo parent, double scale)
  {
    //System.out.println("*** update rect");

    BigDecimal x=sub(parent.getXM(), div(parent.getDX(), 2));
    BigDecimal y=add(parent.getYM(), div(parent.getDY(), 2));

    if (debug) {
      System.out.println("child:");
      System.out.println("   center: "+info.getXM()+","+info.getYM());
      System.out.println("   size  : "+info.getDX()+","+info.getDY());
      System.out.println("old:");
      System.out.println("   start: "+rect.getX()+","+rect.getY());
      System.out.println("   size  : "+rect.getWidth()+","+rect.getHeight());
    }

    rect.setSize(toInt(parent.getRX()*scale*div(info.getDX(), parent.getDX()).
            doubleValue()-D),
                 toInt(parent.getRY()*scale*div(info.getDY(), parent.getDY()).
            doubleValue()-D));

    rect.setLocation(
            toInt(mul(div(sub(sub(info.getXM(), x), div(info.getDX(), 2)), parent.
            getDX()), parent.getRX()*scale).doubleValue()),
            toInt(mul(div(sub(sub(y, info.getYM()), div(info.getDY(), 2)), parent.
            getDY()), parent.getRY()*scale).doubleValue()));

    if (debug) {
      System.out.println("new:");
      System.out.println("   start: "+rect.getX()+","+rect.getY());
      System.out.println("   size  : "+rect.getWidth()+","+rect.getHeight());
      System.out.println("update rect done");
    }
  }
}
