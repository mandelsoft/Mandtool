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
import java.math.BigDecimal;


/**
 *
 * @author Uwe Krueger
 */
abstract public class AbstractPixelIterator extends PixelArith implements PixelIterator {
  protected int limit;
  protected int rx;
  protected int ry;
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
    setPrecision(dx, dy, rx, ry);
  }

  @Override
  protected void setPrecision(BigDecimal dx, BigDecimal dy, int rx, int ry)
  {
    super.setPrecision(dx, dy, ry, ry);
    magnification=calcDigits(dx, dy, 1, 1);
  }

  @Override
  public int getPrecision()
  {
    return bits;
  }

  @Override
  public int getMagnification()
  {
    return magnification;
  }
}
