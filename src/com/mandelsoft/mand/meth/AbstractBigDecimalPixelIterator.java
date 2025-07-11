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

  protected int x;
  protected int y;

  protected BigDecimal sx;
  protected BigDecimal sy;
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

    this.dx = dx;
    this.dy = dy;
    this.drx = new BigDecimal(rx);
    this.dry = new BigDecimal(ry);

    this.x0 = x0;
    this.y0 = y0;
  }

  public boolean isFast()
  {
    return false;
  }

  public BigDecimal getCX(int x)
  {
    return MandArith.add(x0, MandArith.div(MandArith.mul(dx, x), drx));
  }

  public BigDecimal getCY(int y)
  {
    return MandArith.sub(y0, MandArith.div(MandArith.mul(dy, y), dry));
  }
  
  public void setX(int x)
  {
    this.x = x;
    cx = getCX(x);
  }

  public void setY(int y)
  {
    this.y = y;
    cy = getCY(y);
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

  public int iter(BigDecimal sx, BigDecimal sy,
                         BigDecimal cx, BigDecimal cy,
                         BigDecimal bound, int limit)
  {
    BigDecimal x = sx;
    BigDecimal y = sy;

    BigDecimal x2 = mul(x, x);
    BigDecimal y2 = mul(y, y);
    int it = 0;

    while (add(x2, y2).compareTo(bound) < 0 && ++it <= limit) {
      BigDecimal xn = add(sub(x2, y2), cx);
      BigDecimal yn = add(mul(mul(MandArith.b2, x), y), cy);
      x = xn;
      x2 = mul(x, x);
      y = yn;
      y2 = mul(y, y);
    }
    return it;
  }
}
