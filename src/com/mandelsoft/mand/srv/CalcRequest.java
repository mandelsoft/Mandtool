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

import com.mandelsoft.mand.MandelSpec;
import com.mandelsoft.mand.calc.AreaCalculator;
import com.mandelsoft.mand.calc.CalculationContext;
import com.mandelsoft.mand.calc.SimpleAreaCalculator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import com.mandelsoft.util.ChangeListener;
import com.mandelsoft.util.StateChangeSupport;

/**
 *
 * @author Uwe Krueger
 */
public class CalcRequest extends CalculationContext implements Request {
  static private AreaCalculator calc=new SimpleAreaCalculator();

  static private final int VERSION=1;

  private int version; // found version
  private long reqid;
  private int[] data;   // iteration data

  static volatile long lastid=0;

  public CalcRequest()
  { 
    this.version=VERSION;
    //reqid=System.currentTimeMillis();
    reqid=0;
    if (reqid<=lastid) reqid=lastid+1;
    lastid=reqid;
  }

  public CalcRequest(MandelSpec spec, int sx, int sy,
                                      int nx, int ny)
  {
    this();
    setSpec(spec);
    setSX(sx);
    setSY(sy);
    setNX(nx);
    setNY(ny);
  }

  public CalcRequest(MandelSpec spec, int sx, int sy,
                                      int nx, int ny,
                                      int[] data)
  {
    this(spec,sx,sy,nx,ny);
    this.data=data;
  }

  public long getReqId()
  {
    return reqid;
  }


  public int[] getData()
  {
    return data;
  }

  private void setReqId(long id)
  {
    reqid=id;
  }

  public void setData(int[] data)
  {
    this.data=data;
  }

  ///////////////////////////////////////////////////////////////
  // calculation
  ///////////////////////////////////////////////////////////////

  public int getIndexAbs(int x, int y)
  {
    return (x-getSX())+(y-getSY())*getNX();
  }

  public int getIndexRel(int x, int y)
  {
    return x+y*getNX();
  }

  public void createData()
  {
    if (data==null) {
      data=new int[getNX()*getNY()];
    }
  }

  @Override
  protected void resetData()
  {
    data=null;
  }

  public int getDataRel(int x, int y)
  {
    return data[getIndexRel(x,y)];
  }

  @Override
  public void setDataRel(int x, int y, int it)
  {
    data[getIndexRel(x,y)]=it;
  }

  public void calc()
  {
    calc.calc(this);
  }

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  public void write(DataOutputStream dos) throws IOException
  {
    write(dos,VERSION);
  }

  public void write(DataOutputStream dos, boolean verbose) throws IOException
  {
    write(dos,VERSION,verbose);
  }

  public void write(DataOutputStream dos, int v) throws IOException
  {
    write(dos,v,true);
  }

  public void write(DataOutputStream dos, int v, boolean verbose)
              throws IOException
  {
    if (verbose) System.out.println("  writing info ("+v+") ...");
    switch (v) {
       case 1: dos.writeInt(v);
               writeV1(dos);
               break;
      default: throw new IOException("unknown calc req version "+v);
    }
  }

  private void writeV1(DataOutputStream dos) throws IOException
  {
    dos.writeLong(reqid);

    dos.writeUTF(getXM().toString());
    dos.writeUTF(getYM().toString());
    dos.writeUTF(getDX().toString());
    dos.writeUTF(getDY().toString());
    dos.writeInt(getRX());
    dos.writeInt(getRY());
    dos.writeInt(getLimitIt());

    dos.writeInt(getSX());
    dos.writeInt(getSY());
    dos.writeInt(getNX());
    dos.writeInt(getNY());

    dos.writeInt(getMinIt());
    dos.writeInt(getMaxIt());
    dos.writeLong(getNumIt());
    dos.writeLong(getMTime());
    dos.writeLong(getMCnt());
    dos.writeLong(getCCnt());

    if (data==null) dos.writeInt(0);
    else {
      dos.writeInt(data.length);
      for (int i=0; i<data.length; i++)
        dos.writeInt(data[i]);
    }
  }

  public void read(DataInputStream dis) throws IOException
  {
    read(dis,true);
  }

  public void read(DataInputStream dis, boolean verbose) throws IOException
  { 
    if (verbose) System.out.println("  reading info ...");
    version=dis.readInt();
    switch (version) {
      case 1: readV1(dis);
              break;
      default: throw new IOException("unknown calc req version "+version);
    }
  }

  private void readV1(DataInputStream dis) throws IOException
  {
    setReqId(dis.readLong());
    
    // fixed format
    setXM(new BigDecimal(dis.readUTF()));
    setYM(new BigDecimal(dis.readUTF()));
    setDX(new BigDecimal(dis.readUTF()));
    setDY(new BigDecimal(dis.readUTF()));
    setRX(dis.readInt());
    setRY(dis.readInt());
    setLimitIt(dis.readInt());

    setSX(dis.readInt());
    setSY(dis.readInt());
    setNX(dis.readInt());
    setNY(dis.readInt());

    setMinIt(dis.readInt());
    setMaxIt(dis.readInt());
    setNumIt(dis.readLong());
    setMTime(dis.readLong());
    setMCnt(dis.readLong());
    setCCnt(dis.readLong());

    int len=dis.readInt();

    if (len==0) data=null;
    else {
      data=new int[len];
      for (int i=0; i<len; i++)
        data[i]=dis.readInt();
    }
  }

  /////////////////////////////////////////////////////////////
  // State Change
  /////////////////////////////////////////////////////////////

  public void send(Server server)
  {
    server.sendRequest(this);
  }

  private StateChangeSupport listeners=new StateChangeSupport();

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  public void fireChangeEvent()
  {
    listeners.fireChangeEvent(this);
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }

  /////////////////////////////////////////////////////////////
  // hash
  /////////////////////////////////////////////////////////////

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final CalcRequest other=(CalcRequest)obj;
    if (this.reqid!=other.reqid) return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=5;
    hash=31*hash+(int)(this.reqid^(this.reqid>>>32));
    return hash;
  }

}

