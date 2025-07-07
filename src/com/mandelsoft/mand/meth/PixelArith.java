/*
 * Copyright 2022 Uwe Krueger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.mand.meth;

import com.mandelsoft.mand.MandelConstants;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import static java.math.RoundingMode.HALF_EVEN;

/**
 *
 * @author Uwe Krueger
 */
public class PixelArith implements MandelConstants {

  protected MathContext ctx;
  protected int bits;

  public PixelArith()
  {
    this.setPrecision(MaxPrecision);
  }

  public PixelArith(int precision)
  {
    this.setPrecision(precision);
  }

  protected final void setPrecision(int precision)
  {
    this.bits = (int) ((precision) / Math.log10(2));
    this.ctx = new MathContext(precision, HALF_EVEN);
  }

  protected void setPrecision(BigDecimal dx, BigDecimal dy, int rx, int ry)
  {
    setPrecision(calcDigits(dx, dy, rx, ry));
  }

  public int calcDigits(BigDecimal dx, BigDecimal dy, int rx, int ry)
  {
    BigDecimal dX = dx.divide(BigDecimal.valueOf(rx), MC);
    BigDecimal dY = dy.divide(BigDecimal.valueOf(ry), MC);
    BigDecimal d = dX;
    if (dX.compareTo(dY) > 0) {
      d = dY;
    }
    int p = 0;
    while (d.compareTo(BigDecimal.ONE) < 0) {
      p++;
      d = mul(d, BigDecimal.TEN);
    }
    return p + 2;
  }

  public BigDecimal mul(BigDecimal a, BigDecimal b)
  {
    return a.multiply(b, ctx);
  }

  public BigDecimal mul(BigDecimal a, double b)
  {
    return a.multiply(BigDecimal.valueOf(b), ctx);
  }

  public BigDecimal div(BigDecimal a, BigDecimal b)
  {
    return a.divide(b, ctx);
  }

  public BigDecimal div(BigDecimal a, double b)
  {
    return a.divide(BigDecimal.valueOf(b), ctx);
  }

  public BigDecimal div(double a, BigDecimal b)
  {
    return BigDecimal.valueOf(a).divide(b, ctx);
  }

  public BigDecimal div(double a, double b)
  {
    return BigDecimal.valueOf(a / b);
  }

  public BigDecimal add(BigDecimal a, BigDecimal b)
  {
    return a.add(b, ctx);
  }

  public BigDecimal add(BigDecimal a, double b)
  {
    return a.add(BigDecimal.valueOf(b), ctx);
  }

  public BigDecimal sub(BigDecimal a, BigDecimal b)
  {
    return a.subtract(b, ctx);
  }

  public BigDecimal sub(BigDecimal a, double b)
  {
    return a.subtract(BigDecimal.valueOf(b), ctx);
  }

  public BigDecimal sub(double a, BigDecimal b)
  {
    return BigDecimal.valueOf(a).subtract(b, ctx);
  }

  int minScale(BigDecimal d)
  {
    int s = d.scale();
    while (d.compareTo(BigDecimal.ZERO) != 0) {
      d = d.setScale(d.scale() - 1, RoundingMode.HALF_EVEN);
      s--;
    }
    return s;
  }

  public BigDecimal round(BigDecimal a, BigDecimal b, int res)
  {
    return round(a, b, res, 1);
  }

  public BigDecimal round(BigDecimal a, BigDecimal b, int res, int r)
  {
    // System.out.println("rounding "+a+": res="+res+", prec="+b);
    b = div(b, res);
    int m = minScale(b) + 1 + r;
    a = a.setScale(m, RoundingMode.HALF_EVEN);
    if (m < 0 && m > -4) {
      a = a.setScale(0);
    }
    return a;
  }
}
