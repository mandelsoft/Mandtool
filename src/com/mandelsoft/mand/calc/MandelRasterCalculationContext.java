/*
 *  Copyright 2012 Uwe Krueger.
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

package com.mandelsoft.mand.calc;

import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.MandelSpec;

/**
 *
 * @author Uwe Krueger
 */
public class MandelRasterCalculationContext extends CalculationContext {

  private MandelRaster raster;

  public MandelRasterCalculationContext(MandelSpec spec)
  { super(spec);
  }

  @Override
  public int getDataRel(int x, int y)
  {
    return raster.getData(x, y);
  }

  @Override
  public void setDataRel(int x, int y, int it)
  {
    raster.setData(x, y, it);
  }

  @Override
  protected void resetData()
  {
    raster=null;
  }

  @Override
  public void createData()
  {
    raster=new MandelRaster(getNX(),getNY());
  }

  public MandelRaster getRaster()
  {
    return raster;
  }
}
