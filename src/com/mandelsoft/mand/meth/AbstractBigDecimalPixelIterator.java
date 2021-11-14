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

import com.mandelsoft.mand.MandelSpec;
import com.mandelsoft.mand.util.MandArith;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public abstract class AbstractBigDecimalPixelIterator extends AbstractPixelIterator {

  protected BigDecimal dx;
  protected BigDecimal dy;
  protected BigDecimal drx;
  protected BigDecimal dry;
  protected BigDecimal x0;
  protected BigDecimal y0;
  protected BigDecimal cx;
  protected BigDecimal cy;

  public AbstractBigDecimalPixelIterator(MandelSpec spec)
  {
    this(spec.getXMin(), spec.getYMax(), spec.getDX(), spec.getDY(),
         spec.getRX(), spec.getRY(), spec.getLimitIt());
  }

  public AbstractBigDecimalPixelIterator(BigDecimal x0,
                                         BigDecimal y0,
                                         BigDecimal dx,
                                         BigDecimal dy,
                                         int rx,
                                         int ry,
                                         int limit)
  {
    super(rx, ry, limit, dx, dy);

    this.dx=dx;
    this.dy=dy;
    this.drx=new BigDecimal(rx);
    this.dry=new BigDecimal(ry);

    this.x0=x0;
    this.y0=y0;
  }

  public boolean isFast()
  {
    return false;
  }

  public void setX(int x)
  {
    cx=MandArith.add(x0, MandArith.div(MandArith.mul(dx, x), drx));
  }

  public void setY(int y)
  {
    cy=MandArith.sub(y0, MandArith.div(MandArith.mul(dy, y), dry));
  }

  public BigDecimal getCX()
  {
    return cx;
  }

  public BigDecimal getCY()
  {
    return cy;
  }

  public double getX(BigDecimal x)
  {
    return MandArith.div(MandArith.mul(MandArith.sub(x, x0), drx), dx).doubleValue();
  }

  public double getY(BigDecimal y)
  {
    return MandArith.div(MandArith.mul(MandArith.sub(y0, y), dry), dy).doubleValue();
  }
}
