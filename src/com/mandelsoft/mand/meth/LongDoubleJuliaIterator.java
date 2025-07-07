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

import com.mandelsoft.mand.MandIter;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public class LongDoubleJuliaIterator extends LongDoubleMandIterator {
    protected String sjx;
    protected String sjy;

    public LongDoubleJuliaIterator(BigDecimal jx,
                                   BigDecimal jy,
                                   BigDecimal x0,
                                   BigDecimal y0,
                                   BigDecimal dx,
                                   BigDecimal dy,
                                   int rx,
                                   int ry,
                                   int limit)
    {
      super(x0,y0,dx,dy,rx,ry,limit);
      this.sjx=jx.toString();
      this.sjy=jy.toString();
    }

    @Override
    public int iter()
    {
      return MandIter.iterP(scx, scy, sjx, sjy, bound, limit, bits);
    }
  }
