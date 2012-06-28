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

package com.mandelsoft.mand.mapping;

import com.mandelsoft.mand.MandelRaster;

/**
 *
 * @author Uwe Krueger
 */
public class IdentityMapper extends MapperSupport {
  static public final int VERSION=1;

  public String getName()
  {
    return "Identity";
  }

  public String getParamDesc()
  {
    return "";
  }

  public Mapping createMapping(MandelRaster raster, int colmapsize)
  { RasterInfo info=new RasterInfo(raster);

    if (colmapsize<info.getSize()+1)
      throw new IllegalArgumentException("colormap size does not match: "+
                   "col: "+colmapsize+ " rsater: "+info.getSize());
    int[] mapping=new int[info.getSize()];
    for (int i=0; i<info.getSize(); i++)
      mapping[i]=i+1;
    return new Mapping(info.getMinIt(),info.getMaxIt(),info.getSize()+1,mapping);
  }

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  @Override
  protected int getDefaultVersion()
  {
    return VERSION;
  }

  @Override
  protected boolean validVersion(int v)
  {
    return 1<=v && v<=VERSION;
  }
}
