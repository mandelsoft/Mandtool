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

import com.mandelsoft.util.IntMatrix;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class MandelRaster implements MandelData.Part, IntMatrix {
  private IntMatrix raster;
  private boolean modified;

  public MandelRaster()
  {
  }
  
  public MandelRaster(IntMatrix m)
  { this.raster = m;
  }

  public MandelRaster(int rx, int ry)
  { raster=new IntMatrix.Memory(rx, ry);
  }

  public IntMatrix getRaster()
  {
    return raster;
  }
  
  public int getRX()
  {
    return raster.getRX();
  }

  public int getRY()
  {
    return raster.getRY();
  }

  public void setModified(boolean m)
  {
    modified=m;
  }
  
  public boolean isModified()
  {
    return modified;
  }
  
  public int getData(int x, int y) {
    return raster.getData(x, y);
  }

  public void setData(int x, int y, int val) {
    raster.setData(x, y, val);
  }

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  // private int version; // shit no version available

  public boolean needsVersionUpdate()
  {
    return false;
  }

  public void write(DataOutputStream dos) throws IOException
  { write(dos, true);
  }

  public void write(DataOutputStream dos, boolean verbose) throws IOException
  { int x=0;
    int y;
    int ix;
    int dx=1;
    int c=0;
    int v=-1;
    int rx=getRX();
    int ry=getRY();
    
    if (verbose) System.out.println("  writing raster ("+rx+","+ry+")...");
    dos.writeInt(rx);
    dos.writeInt(ry);

    for (y=0; y<ry; y++) {
      for (ix=0; ix<rx; ix++, x+=dx) {
        int it = raster.getData(x, y);
        if (it==v && c<255) {
          c++;
        }
        else {
          write(dos,c,v);
          c=1;
          v=it;
        }
      }
      dx=-dx;
      x+=dx;
    }
    write(dos,c,v);
  }

  private void write(DataOutputStream dos, int c, int v) throws IOException
  {
    if (c>0) {
      dos.writeByte(c);
      dos.writeInt(v);
    }
  }

  void read(DataInputStream dis) throws IOException
  { read(dis,true);
  }

  void read(DataInputStream dis, boolean verbose) throws IOException
  { int x=0;
    int y;
    int ix;
    int dx=1;
    int c=0;
    int v=-1;

    int rx=dis.readInt();
    int ry=dis.readInt();
    if (verbose) System.out.println("  reading raster ("+rx+","+ry+")...");
    raster=new IntMatrix.Memory(rx, ry);

    for (y=0; y<ry; y++) {
      for (ix=0; ix<rx; ix++, x+=dx) {
        while (c<=0) {
          c=dis.readUnsignedByte();
          v=dis.readInt();
        }
        raster.setData(x, y, v);
        c--;
      }
      dx=-dx;
      x+=dx;
    }
  }
}
