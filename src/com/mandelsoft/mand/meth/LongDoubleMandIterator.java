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
import com.mandelsoft.mand.tools.Mand;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public class LongDoubleMandIterator extends AbstractBigDecimalPixelIterator {

    protected String bound;
    protected String scx;
    protected String scy;

    public LongDoubleMandIterator(BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit)
    {
      super(x0,y0,dx,dy,rx,ry,limit);
      bound=new BigDecimal(Mand.BOUND).toString();
      System.out.println("prec: "+bits);
    }

    @Override
    public void setX(int x)
    {
      super.setX(x);
      scx=cx.toString();
    }

    @Override
    public void setY(int y)
    {
      super.setY(y);
      scy=cy.toString();
    }

    public int iter()
    {
      return MandIter.iterP("0.0", "0.0", scx, scy, bound, limit, bits);
    }
  }