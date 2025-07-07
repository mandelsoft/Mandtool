/*
 * Copyright 2025 uwekr.
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
package com.mandelsoft.mand;

import com.mandelsoft.mand.util.MandArith;
import java.math.BigDecimal;

/**
 *
 * @author uwekr
 */
public class MandelRasterAccess extends MandArith {
  MandelRaster r;
  
  BigDecimal x0;
  BigDecimal x1;
  BigDecimal y0;
  BigDecimal y1;
  
  BigDecimal dx;
  BigDecimal dy;
  
  BigDecimal rx;
  BigDecimal ry;
  
  public MandelRasterAccess(MandelData data) {
    r = data.getRaster();
    x0 = data.getInfo().getXMin();
    x1 = data.getInfo().getXMax();
    y0 = data.getInfo().getYMin();
    y1 = data.getInfo().getYMax();

    dx = data.getInfo().getDX();
    dy = data.getInfo().getDY();

    rx = new BigDecimal(data.getInfo().getRX());
    ry = new BigDecimal(data.getInfo().getRY());
  }
  
  public int getData(int x, int y)
  {
    return r.getData(x, y);
  }
  
  public int getData(BigDecimal x, BigDecimal y) {
    if (x.compareTo(x0) >= 0 && x.compareTo(x1) < 0
            && y.compareTo(y0) >= 0 && y.compareTo(y1) < 0) {
      int cx = mul(div(sub(x, x0), dx), rx).intValue();
      int cy = mul(div(sub(y1, y), dy), ry).intValue();

      if (cx < r.getRX() && cy < r.getRY()) {
        return r.getData(cx, cy);
      }
    }
    return -1;
  }
}
