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

package com.mandelsoft.mand;

import java.math.MathContext;

/**
 *
 * @author Uwe Krueger
 */
public interface MandelConstants {
  static public final MathContext MC=new MathContext(200);

  public static final int MAGIC=0x55555555;

  public static final int C_ALL         =0x3F;

  public static final int C_INFO        =0x01;
  public static final int C_RASTER      =0x02;
  public static final int C_COLMAP      =0x04;
  public static final int C_MAPPING     =0x08;
  public static final int C_MAPPER      =0x10;
  public static final int C_IMAGE       =0x20;

  public static final int M_META=0x300;
  public static final int M_INFOOMITTED=0x100;  // for mandel cache
  public static final int C_INCOMPLETE =0x200;  // incomplete raster
  
  public static final int C_IMAGEDATA  =C_RASTER|C_IMAGE;
  public static final int C_RASTERIMAGE=C_RASTER|C_MAPPING|C_COLMAP;
  public static final int C_AREACOLMAP=C_INFO|C_COLMAP;

  public static final String COLORMAP_SUFFIX=".cm";
  public static final String AREACOLMAP_SUFFIX=".mc";
  public static final String INFO_SUFFIX=".md";
  public static final String RASTER_SUFFIX=".mr";
  public static final String RASTERIMAGE_SUFFIX=".mi";
  public static final String IMAGE_SUFFIX=".mpng";
  public static final String INCOMPLETE_SUFFIX=".ms";
}
