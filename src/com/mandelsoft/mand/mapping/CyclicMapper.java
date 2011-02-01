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
public class CyclicMapper extends MapperSupport {
  static public final int VERSION=1;

  public CyclicMapper()
  { 
  }

  public String getName()
  {
    return "Cyclic";
  }

  public String getParamDesc()
  {
    return "";
  }

  ///////////////////////////////////////////////////////////////
  // mapping
  ///////////////////////////////////////////////////////////////

  public Mapping createMapping(MandelRaster raster, int colmapsize)
  { RasterInfo info=new RasterInfo(raster);

    int[] mapping=new int[info.getSize()];
    for (int i=0; i<info.getSize(); i++)
      mapping[i]=1+(i%(colmapsize-1));
    return new Mapping(info.getMinIt(),info.getMaxIt(),colmapsize,mapping);
  }

  ///////////////////////////////////////////////////////////////
  // io info
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

  /////////////////////////////////////////////////////////////////////////
  // IO
  /////////////////////////////////////////////////////////////////////////
  /*
  protected void _write(DataOutputStream dos, int v) throws IOException
  {
    switch (v) {
      case 1:
        writeV1(dos);
        break;
      default:
        throw new IOException("unknown cyclic mapping VERSION "+v);
    }
  }
  
  protected void writeV1(DataOutputStream dos) throws IOException
  { 
  }

  @Override
  protected void _read(DataInputStream dis, int v) throws IOException
  {
    switch (v) {
      case 1:
        readV1(dis);
        break;
      default:
        throw new IOException("unknown cyclic mapping VERSION "+v);
    }
  }

  protected void readV1(DataInputStream dis) throws IOException
  { 
  }
  */
}
