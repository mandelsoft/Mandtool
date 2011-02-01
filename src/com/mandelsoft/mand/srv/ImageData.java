
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
package com.mandelsoft.mand.srv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.mandelsoft.mand.MandelFileName;

/**
 *
 * @author Uwe Krüger
 */

public class ImageData {
  public static final int VERSION=1;

  private MandelFileName name;
  private boolean recalc;
  private int     resolution;
  private int     magnification;
  private long    starttime;

  public ImageData(DataInputStream dis) throws IOException
  {
    read(dis);
  }

  public ImageData(MandelFileName name, boolean recalc, int res, int mag,
                   long starttime)
  {
    this.name=name;
    this.recalc=recalc;
    this.resolution=res;
    this.magnification=mag;
    this.starttime=starttime;
  }

  public MandelFileName getName()
  {
    return name;
  }

  public int getPrecision()
  {
    return resolution;
  }

  public int getMagnification()
  {
    return magnification;
  }

  public boolean isRecalc()
  {
    return recalc;
  }

  public long getStartTime()
  {
    return starttime;
  }

  

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  public void write(DataOutputStream dos) throws IOException
  {
    write(dos,VERSION);
  }

  public void write(DataOutputStream dos, int v)
              throws IOException
  {
    switch (v) {
       case 1: dos.writeInt(v);
               writeV1(dos);
               break;
      default: throw new IOException("unknown host info version "+v);
    }
  }

  private void writeV1(DataOutputStream dos) throws IOException
  {
    dos.writeUTF(name.toString());
    dos.writeBoolean(recalc);
    dos.writeInt(resolution);
    dos.writeInt(magnification);
    dos.writeLong(starttime);
  }

  public void read(DataInputStream dis) throws IOException
  {
    int version=dis.readInt();
    switch (version) {
      case 1: readV1(dis);
              break;
      default: throw new IOException("unknown host data version "+version);
    }
  }

  private void readV1(DataInputStream dis) throws IOException
  {
    name=MandelFileName.create(dis.readUTF());
    recalc=dis.readBoolean();
    resolution=dis.readInt();
    magnification=dis.readInt();
    starttime=dis.readLong();
  }
}
