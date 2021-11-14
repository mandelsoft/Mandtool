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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class MandelHeader implements MandelConstants {

  private int flags;

  public MandelHeader(int flags)
  {
    super();
    this.flags=flags;
  }

  public int getType()
  {
    return flags;
  }

  public String getTypeDesc()
  {
    String incomplete="";
    if (has(C_INCOMPLETE)) {
      incomplete="Incomplete ";
    }
    if (has(C_RASTERIMAGE)) {
      if (!has(C_INFO))
        return "Mandel Image without Info";
      return incomplete+"Modifiable Mandel Image";
    }
    if (has(C_RASTER)) {
      return incomplete+"Mandel Raster";
    }
    if (has(C_IMAGE)) {
      return "Mandel Image";
    }
    if (is(C_COLMAP)) {
      return "Colormap";
    }
    if (is(C_AREACOLMAP)) {
      return "Area Colormap";
    }
    if (has(C_INFO)) {
      return "Mandel Info";
    }
    return "unknown";
  }

  public boolean has(int flags)
  {
    return (this.flags&flags)==flags;
  }

  public boolean is(int flags)
  {
    return this.flags==flags;
  }

  public boolean hasAtLeast(int flags)
  {
    return (flags & this.flags)==flags;
  }

  public boolean hasAdditional(int flags)
  {
    return this.flags!=flags && hasAtLeast(flags);
  }

  public boolean isColormap()
  {
    return is(C_COLMAP);
  }

  public boolean isInfo()
  {
    return is(C_INFO);
  }

  public boolean isAreaColormap()
  {
    return is(C_AREACOLMAP);
  }

  public boolean isRaster()
  {
    return is(C_RASTER|C_INFO);
  }

  public boolean isModifiableImage()
  {
    return is(C_RASTERIMAGE|C_INFO) || is(C_RASTERIMAGE|C_MAPPER|C_INFO);
  }

  public boolean hasModifiableImage()
  {
    return has(C_RASTERIMAGE);
  }

  public boolean isPlainImage()
  {
    return (flags&C_IMAGEDATA)==C_IMAGE;
  }

  public boolean isImage()
  {
    return isModifiableImage()||isPlainImage();
  }

  public boolean hasPlainImage()
  {
    return has(C_IMAGE);
  }

  public boolean hasImage()
  {
    return (flags&C_IMAGE)!=0;
  }

  public boolean hasInfo()
  {
    return has(C_INFO);
  }

  public boolean hasRaster()
  {
    return has(C_RASTER);
  }

  public boolean hasColormap()
  {
    return has(C_COLMAP);
  }

  public boolean hasMapping()
  {
    return has(C_MAPPING);
  }

  public boolean hasMapper()
  {
    return has(C_MAPPER);
  }

  public boolean hasMandelColormap()
  {
    return has(C_AREACOLMAP);
  }

  public boolean isMandelColormap()
  {
    return is(C_AREACOLMAP);
  }
  
  public boolean hasImageData()
  {
    return (flags&C_IMAGEDATA)!=0;
  }

  public boolean isIncomplete()
  {
    return has(C_INCOMPLETE);
  }

  @Override
  public String toString()
  {
    return getTypeDesc();
  }

  //////////////////////////////////////////////////////////////////////////
  // factory
  //////////////////////////////////////////////////////////////////////////

  public static MandelHeader getHeader(File f) throws IOException
  { 
    DataInputStream dis=new DataInputStream(
            new BufferedInputStream(
            new FileInputStream(f)));
    try {
      int magic=dis.readInt();
      if (magic!=MandelData.MAGIC) {
        throw new IOException("illegal format");
      }
      int flags=dis.readInt();
      return new MandelHeader(flags);
    }
    finally {
      dis.close();
    }
  }
}
