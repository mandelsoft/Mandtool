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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.mandelsoft.mand.MandelRaster;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MapperSupport implements Mapper {
 
  static protected class RasterInfo {
    protected MandelRaster raster;
    protected int minIt;
    protected int maxIt;

    public RasterInfo(MandelRaster r)
    { this.raster=r;
      analyseRaster(r);
    }

    public int getMinIt()
    { return minIt;
    }
    
    public int getMaxIt()
    { return maxIt;
    }
    
    public int getSize()
    { return maxIt-minIt+1;
    }

    public MandelRaster getRaster()
    { return raster;
    }

    protected void analyseRaster(MandelRaster r)
    { int[][] raster=r.getRaster();
      minIt=0;
      maxIt=0;
      for (int y=0; y<r.getRY(); y++) {
        for (int x=0; x<r.getRX(); x++) {
          if (raster[y][x]>maxIt) {
            maxIt=raster[y][x];
            if (minIt==0) minIt=maxIt;
          }
          if (raster[y][x]<minIt && raster[y][x]>0) {
            minIt=raster[y][x];
          }
        }
      }
    }
  }
  
  public MapperSupport()
  { version=getDefaultVersion();
  }

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  private int version;

  public boolean needsVersionUpdate()
  { return version!=getDefaultVersion();
  }

  public void write(DataOutputStream dos) throws IOException
  {
    write(dos, getDefaultVersion());
  }

  public void write(DataOutputStream dos, int v) throws IOException
  { if (!validVersion(v)) {
      throw new IOException("unknown mapper version "+v);
    }
    dos.writeInt(v);
    _write(dos,v);
  }

  protected abstract int getDefaultVersion();
  protected abstract boolean validVersion(int v);

  protected void _write(DataOutputStream dos, int v) throws IOException
  {
    // do nothing
  }

  public void read(DataInputStream dis) throws IOException
  {
    version=dis.readInt();
    if (!validVersion(version)) {
      throw new IOException("unknown mapper version "+version+" for "+
                             getClass().getName());
    }
    _read(dis,version);
  }

  protected void _read(DataInputStream dis, int v) throws IOException
  {
    // do nothing
  }
}
