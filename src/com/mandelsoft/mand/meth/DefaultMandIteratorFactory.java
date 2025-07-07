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

import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelSpec;
import com.mandelsoft.mand.PixelIteratorFactory;
import java.math.BigDecimal;
import com.mandelsoft.mand.util.MandArith;
import java.util.Map;

/**
 *
 * @author Uwe Krueger
 */
public class DefaultMandIteratorFactory extends MandArith
        implements PixelIteratorFactory {

  static private BigDecimal br = new BigDecimal(5.e-16, MC);
  static private BigDecimal br2 = new BigDecimal(1e-310, MC);

  private boolean optimized;
  private boolean useDLL;

  public DefaultMandIteratorFactory(boolean optimized, boolean useDLL)
  {
    this.optimized = optimized;
    this.useDLL = useDLL;
  }

  public PixelIterator createPixelIterator(MandelInfo mi)
  {
    return createPixelIterator(mi, mi.getProperties());
  }

  public PixelIterator createPixelIterator(MandelInfo mi,
                                           PixelIterator.PropertySource.PropertyHandler h)
  {
    PixelIterator pi = createPixelIterator(mi, mi.getProperties());
    PixelIterator.setPropertyHandler(pi, h);
    return pi;
  }

  public PixelIterator createPixelIterator(MandelSpec mi,
                                           Map<String, String> properties)
  {
    BigDecimal x0 = mi.getXMin();
    BigDecimal y0 = mi.getYMax();
    BigDecimal dx = mi.getDX();
    BigDecimal dy = mi.getDY();
    int rx = mi.getRX();
    int ry = mi.getRY();
    int limit = mi.getLimitIt();

    if (MandArith.div(dx, rx).compareTo(br) > 0 && MandArith.div(dy, ry).compareTo(br) > 0) {
      System.out.println("double iteration mode");
      return new DoubleMandIterator(x0, y0, dx, dy, rx, ry, limit);
    }
    if (optimized) {
      if (MandArith.div(dx, rx).compareTo(br2) > 0 && MandArith.div(dy, ry).compareTo(br2) > 0) {
        System.out.println("optimized iteration mode");
        return new OptimizedBigDecimalMandIterator(x0, y0, dx, dy, rx, ry, limit, properties);
      }
       System.out.println("long optimized iteration mode");
       return new OptimizedLongBigDecimalMandIterator(x0, y0, dx, dy, rx, ry, limit, properties);
    }
    if (useDLL) {
      System.out.println("long double iteration mode");
      return new LongDoubleMandIterator(x0, y0, dx, dy, rx, ry, limit);
    }
    System.out.println("big decimal iteration mode");
    return new BigDecimalMandIterator(x0, y0, dx, dy, rx, ry, limit);
  }
}
