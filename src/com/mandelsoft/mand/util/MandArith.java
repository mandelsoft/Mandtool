
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
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author Uwe Krüger
 */

public class MandArith implements MandelConstants {
  static public final BigDecimal b2=new BigDecimal(2);
  static public final BigDecimal b0=new BigDecimal(0);
  static public final BigDecimal b10=new BigDecimal(10);
  
  static public BigDecimal mul(BigDecimal a, BigDecimal b)
  { return a.multiply(b,MC);
  }

  static public BigDecimal mul(BigDecimal a, double b)
  { return a.multiply(BigDecimal.valueOf(b),MC);
  }

  static public BigDecimal div(BigDecimal a, BigDecimal b)
  { return a.divide(b,MC);
  }

  static public BigDecimal div(BigDecimal a, double b)
  { return a.divide(BigDecimal.valueOf(b),MC);
  }

  static public BigDecimal div(double a, BigDecimal b)
  { return BigDecimal.valueOf(a).divide(b,MC);
  }

  static public BigDecimal div(double a, double b)
  { return BigDecimal.valueOf(a/b);
  }
  
  static public BigDecimal add(BigDecimal a, BigDecimal b)
  { return a.add(b,MC);
  }

  static public BigDecimal add(BigDecimal a, double b)
  { return a.add(BigDecimal.valueOf(b),MC);
  }

  static public BigDecimal sub(BigDecimal a, BigDecimal b)
  { return a.subtract(b,MC);
  }

  static public BigDecimal sub(BigDecimal a, double b)
  { return a.subtract(BigDecimal.valueOf(b),MC);
  }

  static public BigDecimal sub(double a, BigDecimal b)
  { return BigDecimal.valueOf(a).subtract(b,MC);
  }

  static int minScale(BigDecimal d)
  { int s=d.scale();
    while (d.compareTo(BigDecimal.ZERO)!=0) {
      d=d.setScale(d.scale()-1,RoundingMode.HALF_EVEN);
      s--;
    }
    return s;
  }

  static public BigDecimal round(BigDecimal a, BigDecimal b, int res)
  { return round(a,b,res,1);
  }

  static public BigDecimal round(BigDecimal a, BigDecimal b, int res, int r)
  {
    // System.out.println("rounding "+a+": res="+res+", prec="+b);
    b=div(b,res);
    int m=minScale(b)+1+r;
    a=a.setScale(m,RoundingMode.HALF_EVEN);
    if (m<0 && m>-4) a=a.setScale(0);
    return a;
  }
}
