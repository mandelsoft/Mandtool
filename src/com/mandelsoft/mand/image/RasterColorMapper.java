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

package com.mandelsoft.mand.image;

import com.mandelsoft.mand.*;
import java.awt.Color;

/**
 *
 * @author Uwe Krueger
 */
public class RasterColorMapper {
  private ColorMapper mapper;
  private MandelRaster mraster;
  private int[][] raster;

  public RasterColorMapper(ColorMapper mapper, MandelRaster mr)
  { if (mr==null) throw new MandelException("no raster set");

    this.mapper=mapper;
    this.mraster=mr;
    this.raster=mr.getRaster();
  }

  public int getColormapIndex(int x, int y)
  {
    return mapper.mapColormapIndex(raster[y][x]);
  }

  public Color getPixelColor(int x, int y)
  { 
    return mapper.mapIterationValue(raster[y][x]);
  }

  public int getPixelRGB(int x, int y)
  { return getPixelColor(x,y).getRGB();
  }

  public Object getPixelDataElements(int x, int y)
  { int it=raster[y][x];
    Object de=mapper.mapIterationValueToDataElements(raster[y][x]);
    //System.out.println("raster["+x+","+y+"]="+it+"->"+((int[])de)[0]);
    return de;
  }
}
