/*
 *  Copyright 2013 Uwe Krueger.
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

package com.mandelsoft.mand.meth;

import com.mandelsoft.mand.tools.Mand;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public abstract class AbstractDoublePixelIterator extends AbstractPixelIterator {
  //parameters

  protected double bound;
  protected double dx;
  protected double dy;
  protected double x0;
  protected double y0;
  protected double drx;
  protected double dry;
  // iteration point
  protected double cx;
  protected double cy;

  public AbstractDoublePixelIterator(BigDecimal x0,
                                     BigDecimal y0,
                                     BigDecimal dx,
                                     BigDecimal dy,
                                     int rx,
                                     int ry,
                                     int limit)
  {
    super(rx, ry, limit, dx, dy);
    bound=Mand.BOUND;

    this.dx=dx.doubleValue();
    this.dy=dy.doubleValue();
    this.x0=x0.doubleValue();
    this.y0=y0.doubleValue();
    this.drx=rx;
    this.dry=ry;
  }

  public boolean isFast()
  {
    return true;
  }

  public void setX(int x)
  {
    cx=x0+(x*dx)/drx;
  }

  public void setY(int y)
  {
    cy=y0-(y*dy)/dry;
  }

  public BigDecimal getCX()
  {
    return new BigDecimal(cx);
  }

  public BigDecimal getCY()
  {
    return new BigDecimal(cy);
  }

  public double getX(BigDecimal x)
  {
    double xd=x.doubleValue();
    return (xd-x0)*drx/dx;
  }

  public double getY(BigDecimal y)
  {
    double yd=y.doubleValue();
    return (y0-yd)*dry/dy;
  }
}
