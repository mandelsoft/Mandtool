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

import com.mandelsoft.mand.*;
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
public class CalcRequest extends MandelSpec implements Request {
  static private final int VERSION=1;

  private int version; // found version

  private long reqid;

  // basic request info
  private int sx;          // start x pixel
  private int sy;          // start y pixel
  private int nx;          // number x pixels
  private int ny;          // number y pixels
  

  // content relted image information
  private int minit;   // minimum iteration
  private int maxit;   // maximum iterations
  private long numit;  // number of total iteration steps
  private long mtime;  // calculation time in milli seconds

  // internal calculation related information
  private long ccnt;    // calculated number pixels
  private long mcnt;    // number of mandel set pixel
  
  private int[] data;   // iteration data

  // temporary
  private PixelIterator iter;

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
    this.sx=sx;
    this.sy=sy;
    this.nx=nx;
    this.ny=ny;
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

  public int getSX()
  {
    return sx;
  }

  public int getSY()
  {
    return sy;
  }

  public int getNX()
  {
    return nx;
  }

  public int getNY()
  {
    return ny;
  }

  

  public int getMinIt()
  {
    return minit;
  }

  public int getMaxIt()
  {
    return maxit;
  }

  // content related data

  public long getNumIt()
  {
    return numit;
  }

  public long getMTime()
  {
    return mtime;
  }

  public long getCCnt()
  {
    return ccnt;
  }

  public long getMCnt()
  {
    return mcnt;
  }

  public int[] getData()
  {
    return data;
  }

  public PixelIterator getPixelIterator()
  {
    if (iter==null) {
      iter=MandIter.createPixelIterator(getXMin(), getYMax(),
                                        getDX(), getDY(),
                                        getRX(), getRY(),
                                        getLimitIt());
    }
    return iter;
  }

  private void setReqId(long id)
  {
    reqid=id;
  }

  public void setSX(int sx)
  {
    this.sx=sx;
  }

  public void setSY(int sy)
  {
    this.sy=sy;
  }

  public void setNX(int nx)
  {
    this.nx=nx;
  }

  public void setNY(int ny)
  {
    this.ny=ny;
  }

  // content related data
  
  public void setMinIt(int minit)
  {
    this.minit=minit;
  }

  public void setMaxIt(int maxit)
  {
    this.maxit=maxit;
  }

  public void setNumIt(long numit)
  {
    this.numit=numit;
  }

  public void setMTime(long mtime)
  {
    this.mtime=mtime;
  }

  public void setCCnt(long ccnt)
  {
    this.ccnt=ccnt;
  }

  public void setMCnt(long mcnt)
  {
    this.mcnt=mcnt;
  }

  public void setData(int[] data)
  {
    this.data=data;
  }


  public void setPixelIterator(PixelIterator i)
  {
    this.iter=i;
  }

  public boolean isSameSpec(CalcRequest o)
  {
    return getSX()==o.getSX() &&
           getSY()==o.getSY() &&
           getNX()==o.getNX() &&
           getNY()==o.getNY() &&
           isSameSpec(getSpec());
  }

  ///////////////////////////////////////////////////////////////
  // calculation
  ///////////////////////////////////////////////////////////////

  public int getIndexAbs(int x, int y)
  {
    return (x-sx)+(y-sy)*nx;
  }

  public int getIndexRel(int x, int y)
  {
    return x+y*nx;
  }

  public int getDataAbs(int x, int y)
  {
    return data[getIndexAbs(x,y)];
  }

  public int getDataRel(int x, int y)
  {
    return data[getIndexRel(x,y)];
  }

  public int[] createData()
  {
    if (data==null) {
      data=new int[nx*ny];
    }
    return data;
  }
  
  public void calc()
  { PixelIterator pi=getPixelIterator();
    int x,y,it;
    long time=System.currentTimeMillis();
    createData();
    minit=getLimitIt();
    for (y=0; y<ny; y++) {
      pi.setY(y+sy);
      for (x=0; x<nx; x++) {
        pi.setX(x+sx);
        handle(pi,x,y);
      }
    }
    setMTime(System.currentTimeMillis()-time);
  }

  private int handle(PixelIterator pi, int x, int y)
  { int ix=getIndexRel(x,y);
    int it=data[ix];

    if (it==0) {
      int i=pi.iter();
      ccnt++;
      if (i>getLimitIt()) {
          data[ix]=it=0;
          mcnt++;
          i--;
      }
      else {
        data[ix]=it=i;
      }
      if (i<minit) minit=i;
      if (i>maxit) maxit=i;
      numit+=i;
    }
    else numit+=it;
    return it;
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

    dos.writeInt(sx);
    dos.writeInt(sy);
    dos.writeInt(nx);
    dos.writeInt(ny);

    dos.writeInt(minit);
    dos.writeInt(maxit);
    dos.writeLong(numit);
    dos.writeLong(mtime);
    dos.writeLong(mcnt);
    dos.writeLong(ccnt);

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

    sx=dis.readInt();
    sy=dis.readInt();
    nx=dis.readInt();
    ny=dis.readInt();

    minit=dis.readInt();
    maxit=dis.readInt();
    numit=dis.readLong();
    mtime=dis.readLong();
    mcnt=dis.readLong();
    ccnt=dis.readLong();

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

