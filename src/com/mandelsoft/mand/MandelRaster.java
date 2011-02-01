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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class MandelRaster implements MandelData.Part {
  private int rx;
  private int ry;
  private int[][] raster;

  public MandelRaster()
  {
  }

  public MandelRaster(int rx, int ry)
  { this.rx=rx;
    this.ry=ry;
    raster=new int[ry][rx];
  }

  public int[][] getRaster()
  {
    return raster;
  }

  public int getRX()
  {
    return rx;
  }

  public int getRY()
  {
    return ry;
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

    if (verbose) System.out.println("  writing raster ("+rx+","+ry+")...");
    dos.writeInt(rx);
    dos.writeInt(ry);

    for (y=0; y<ry; y++) {
      for (ix=0; ix<rx; ix++, x+=dx) {
        if (raster[y][x]==v && c<255) {
          c++;
        }
        else {
          write(dos,c,v);
          c=1;
          v=raster[y][x];
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

    rx=dis.readInt();
    ry=dis.readInt();
    if (verbose) System.out.println("  reading raster ("+rx+","+ry+")...");
    raster=new int[ry][rx];

    for (y=0; y<ry; y++) {
      for (ix=0; ix<rx; ix++, x+=dx) {
        while (c<=0) {
          c=dis.readUnsignedByte();
          v=dis.readInt();
        }
        raster[y][x]=v;
        c--;
      }
      dx=-dx;
      x+=dx;
    }
  }
}
