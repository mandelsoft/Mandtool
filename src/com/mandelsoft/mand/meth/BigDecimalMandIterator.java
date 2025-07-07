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

import com.mandelsoft.mand.tools.Mand;
import static com.mandelsoft.mand.util.MandArith.b0;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public class BigDecimalMandIterator extends AbstractBigDecimalPixelIterator {
    protected int cnt;
    protected BigDecimal bound;

    public BigDecimalMandIterator(BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit)
    {
      super(x0,y0,dx,dy,rx,ry,limit);
      bound=new BigDecimal(Mand.BOUND);
    }

    public int iter()
    {
      cnt++;
      if (cnt%100==0) {
        System.out.print(".");
        System.out.flush();
      }
      return _iter();
    }
    
    protected int _iter()
    {
      return iter(b0, b0, cx, cy, bound, limit);
    }
    
  }
