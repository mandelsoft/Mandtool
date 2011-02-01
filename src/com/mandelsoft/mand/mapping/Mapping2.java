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

/**
 *
 * @author Uwe Krueger
 */
public class Mapping2 {
  private static final int VERSION=1;

  private int version=VERSION;
  private int minIt;
  private int maxIt;
  private int target;
  private int[] mapping;

  public Mapping2()
  {
  }

  public Mapping2(DataInputStream dis) throws IOException
  { read(dis);
  }
  
  public Mapping2(int minIt, int maxIt, int target, int[] mapping)
  {
    this.maxIt=maxIt;
    this.minIt=minIt;
    this.target=target;
    this.mapping=mapping;
    if (mapping.length!=maxIt-minIt+1)
      throw new IllegalArgumentException("array size doe no match");

    if (getTargetSize()>0 && (getSourceSize()/getTargetSize()>2)) {
      this.version=2;
    }
    else {
      this.version=1;
    }
  }

  public int getTargetSize()
  {
    return target;
  }
  
  public int getSourceSize()
  {
    return maxIt-minIt+1;
  }
  
  public int getMinIt()
  {
    return minIt;
  }
  
  public int getMaxIt()
  {
    return maxIt;
  }

  public int getColormapIndex(int iteration)
  {
    if (iteration==0) return 0;
    try {
      return mapping[iteration-minIt];
    }
    catch (ArrayIndexOutOfBoundsException oob) {
      if (iteration-minIt<0) {
        System.out.println("invalid interation value "+
                         iteration+" (min="+minIt);
        return 0; //!!!
      }
      else {
        System.out.println("invalid interation value "+
                         iteration+" (max="+minIt+mapping.length);
        return mapping[getSourceSize()-1];
      }
      
    }
  }

  public int getInteration(int index, int start, boolean skip)
  {
    if (start<minIt) start=minIt;
    if (skip) while (start<=maxIt && mapping[start-minIt]==index) start++;
    while (start<=maxIt && mapping[start-minIt]!=index) start++;
    if (start>maxIt) return 0;
    int end=start;
    while (end<=maxIt && mapping[end-minIt]==index) end++;
    int m=(end+start)/2;
    //System.out.println("index "+index+": "+start+"-"+(end-1)+": "+m);
    return m;
  }

  /////////////////////////////////////////////////////////////////////////////
  // IO
  /////////////////////////////////////////////////////////////////////////////

  public boolean needsVersionUpdate()
  { return false;
  }

  public void write(DataOutputStream dos) throws IOException
  {
    write(dos,true);
  }

  public void write(DataOutputStream dos, boolean verbose) throws IOException
  {
    if (verbose) System.out.println("  writing mapping (v"+version+")...");
    dos.writeInt(version);
    _write(dos,version);
  }

  protected void _write(DataOutputStream dos, int v) throws IOException
  {
    switch (v) {
      case 1:
        writeV1(dos);
        break;
      case 2:
        writeV2(dos);
        break;
      default:
        throw new IOException("unknown mapping version "+v);
    }
  }
  
  protected void writeV1(DataOutputStream dos) throws IOException
  {
    dos.writeInt(minIt);
    dos.writeInt(maxIt);
    dos.writeInt(target);
    for (int i=0; i<mapping.length; i++) {
      dos.writeInt(mapping[i]);
    }
  }

  protected void writeV2(DataOutputStream dos) throws IOException
  {
    dos.writeInt(minIt);
    dos.writeInt(maxIt);
    dos.writeInt(target);
    int count=0;
    int value=mapping[0];
    for (int i=0; i<mapping.length; i++) {
      if (mapping[i]==value) count++;
      else {
        dos.writeInt(count);
        dos.writeInt(value);
        count=1;
        value=mapping[i];
      }
    }
    if (count>0) {
      dos.writeInt(count);
      dos.writeInt(value);
    }
  }

  public void read(DataInputStream dis) throws IOException
  {
    read(dis,true);
  }

  public void read(DataInputStream dis, boolean verbose) throws IOException
  {
    if (verbose) System.out.println("  reading mapping...");
    version=dis.readInt();
    _read(dis,version);
  }

  protected void _read(DataInputStream dis, int v) throws IOException
  {
    switch (v) {
      case 1:
        readV1(dis);
        break;
      case 2:
        readV2(dis);
        break;
      default:
        throw new IOException("unknown  mapping version "+v);
    }
  }

  protected void readV1(DataInputStream dis) throws IOException
  {
    minIt=dis.readInt();
    maxIt=dis.readInt();
    target=dis.readInt();
    mapping=new int[maxIt-minIt+1];
    for (int i=0; i<mapping.length; i++) {
      mapping[i]=dis.readInt();
    }
  }

  protected void readV2(DataInputStream dis) throws IOException
  {
    minIt=dis.readInt();
    maxIt=dis.readInt();
    target=dis.readInt();
    mapping=new int[maxIt-minIt+1];
    int count=0;
    int value=0;
    for (int i=0; i<mapping.length; i++) {
      if (count<=0) {
        count=dis.readInt();
        value=dis.readInt();
      }
      mapping[i]=value;
      count--;
    }
  }
}
