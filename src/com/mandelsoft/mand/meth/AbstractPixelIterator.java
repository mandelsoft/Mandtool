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
import com.mandelsoft.mand.PixelIterator;
import com.mandelsoft.mand.util.MandArith;
import java.math.BigDecimal;
import java.math.MathContext;
import static java.math.RoundingMode.HALF_EVEN;

/**
 *
 * @author Uwe Krueger
 */
abstract public class AbstractPixelIterator extends MandArith implements PixelIterator {
  protected int limit;
  protected int rx;
  protected int ry;
  protected MathContext ctx;
  protected int precision;
  protected int magnification;

  public AbstractPixelIterator(MandelSpec mi)
  {
    this(mi.getRX(), mi.getRY(), mi.getLimitIt(), mi.getDX(), mi.getDY());
  }

  public AbstractPixelIterator(int rx, int ry, int limit,
                           BigDecimal dx, BigDecimal dy)
  {
    this.limit=limit;
    this.rx=rx;
    this.ry=ry;
    setPrecision(dx, dy);
  }

  protected void setPrecision(BigDecimal dx, BigDecimal dy)
  {
    BigDecimal dX=div(dx, rx);
    BigDecimal dY=div(dy, ry);
    BigDecimal d=dX;
    if (dX.compareTo(dY)>0) d=dY;
    int p=0;
    while (d.compareTo(BigDecimal.ONE)<0) {
      p++;
      d=mul(d, BigDecimal.TEN);
    }
    ctx=new MathContext(p+2, HALF_EVEN);
    precision=(int)((p+2)/Math.log10(2));

    d=dx;
    if (dx.compareTo(dy)>0) d=dy;
    p=0;
    while (d.compareTo(BigDecimal.ONE)<0) {
      p++;
      d=mul(d, BigDecimal.TEN);
    }
    magnification=p;
  }

  public int getPrecision()
  {
    return precision;
  }

  public int getMagnification()
  {
    return magnification;
  }
}
