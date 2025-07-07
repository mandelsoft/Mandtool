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
package com.mandelsoft.mand.tool.util;

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;

/**
 *
 * @author uwekr
 */
public class RasterCleaner {
    private MandelInfo info;
    private MandelRaster raster;
    private int limit;
    private int rx;
    private int ry;
    
    public RasterCleaner(MandelData data)
    {
        info = data.getInfo();
        raster = data.getRaster();
        limit = data.getInfo().getLimitIt();
        rx = info.getRX();
        ry = info.getRY();

    }
    
    public int clean(int max, int r)
    {
      int found = 0;
      for (int y = 0; y < ry; y++) {
        for (int x = 0; x < rx; x++) {
          if (raster.getData(x, y) == 0) {
            int c = countBlack(x, y, r);
            if (c < max) {
              raster.setData(x, y, limit - 1);
              found++;
            }
          }
        }
      }
      return found;
    }
    
    private int countBlack(int x, int y, int r) {
        int c = 0;
        int n = 0;
        for (int j = y - r; j <= y + r; j++) {
            if (j >= 0 && j < ry) {
                for (int i = x - r; i <= x + r; i++) {
                    if (i >= 0 && i < rx) {
                      n++;
                        if (raster.getData(i, j) == 0)  {
                            c++;
                        }
                    }
                }
            }
        }
        return c*100/n;
    }
}
  