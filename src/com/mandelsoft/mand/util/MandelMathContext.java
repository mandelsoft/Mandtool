/*
 * Copyright 2021 D021770.
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
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.MandelSpec;
import java.math.BigDecimal;
import java.math.MathContext;
import static java.math.RoundingMode.HALF_EVEN;

/**
 *
 * @author D021770
 */
public class MandelMathContext {
  protected MathContext ctx;
  protected int precision;
  
  public MandelMathContext(MandelSpec mi)
  {
    setPrecision(mi);
  }
  
  public MandelMathContext(int p)
  {
    setPrecision(p);
  }
  
  public MandelMathContext(int rx, int ry, BigDecimal dx, BigDecimal dy)
  {
    setPrecision(rx, ry, dx, dy);
  }
  
  public final void setPrecision(MandelSpec mi)
  {
    setPrecision(mi.getRX(), mi.getRY(), mi.getDX(), mi.getDY());
  }
  
  public final void setPrecision(int rx, int ry, BigDecimal dx, BigDecimal dy)
  {
    BigDecimal dX=MandArith.div(dx, rx);
    BigDecimal dY=MandArith.div(dy, ry);
    BigDecimal d=dX;
    if (dX.compareTo(dY)>0) d=dY;
    setPrecision(d);
  }
          
  public final void setPrecision(int p)
  {
    ctx = new MathContext(p, HALF_EVEN);
    precision = (int) (p / Math.log10(2));
  }
  
  static int precisionFor(BigDecimal d)
  {
    int p = 0;
    while (d.compareTo(BigDecimal.ONE) < 0) {
      p++;
      d = MandArith.mul(d, BigDecimal.TEN);
    }
    return p + 2;
  }
  
  public final void setPrecision(BigDecimal d)
  {
    setPrecision(precisionFor(d));
  }
  
  //////////////////////////////////////////////////////////////////////////////
  
  public BigDecimal mul(BigDecimal a, BigDecimal b)
  { return a.multiply(b,ctx);
  }

  public BigDecimal mul(BigDecimal a, double b)
  { return a.multiply(BigDecimal.valueOf(b),ctx);
  }

  public BigDecimal div(BigDecimal a, BigDecimal b)
  { return a.divide(b,ctx);
  }

  public BigDecimal div(BigDecimal a, double b)
  { return a.divide(BigDecimal.valueOf(b),ctx);
  }

  public BigDecimal div(double a, BigDecimal b)
  { return BigDecimal.valueOf(a).divide(b,ctx);
  }

  public BigDecimal div(double a, double b)
  { return BigDecimal.valueOf(a/b);
  }
  
  public BigDecimal add(BigDecimal a, BigDecimal b)
  { return a.add(b,ctx);
  }

  public BigDecimal add(BigDecimal a, double b)
  { return a.add(BigDecimal.valueOf(b),ctx);
  }

  public BigDecimal sub(BigDecimal a, BigDecimal b)
  { return a.subtract(b,ctx);
  }

  public BigDecimal sub(BigDecimal a, double b)
  { return a.subtract(BigDecimal.valueOf(b),ctx);
  }

  public BigDecimal sub(double a, BigDecimal b)
  { return BigDecimal.valueOf(a).subtract(b,ctx);
  }
}
