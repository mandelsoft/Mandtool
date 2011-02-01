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
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class MandelInfo extends MandelSpec
                        implements MandelData.Part {
  static private final int VERSION=4;

  private int version; // found version
  private boolean hidden;  // hidden area: hint for ui

  // content relted image information
  private int minit;   // minimum iteration
  private int maxit;   // maximum iterations
  private long numit;  // number of total iteration steps
  private int time;    // calculation time in seconds
  private int efftime; // effective calculation time

  private long creattime; // creation time
  private long calctime;  // raster creation time
  private long imgtime;   // image creation time

  // internal calculation related information
  private int usedlimit; // limit used for last calculation
  private long mccnt;    // calculated number of mandel set pixels
  private long mcnt;     // number of mandel set pixel

  private String name;     // assigned name
  private String location; // assigned location spec
  private String creator;  // creator
  private String site;     // creation site

  private Set<String> keywords; // keywords for classification
  
  public MandelInfo()
  { super();
    setup();
  }

  public MandelInfo(double xm, double ym,
                    double dx, double dy,
                    int rx, int ry, int limitit, boolean hidden)
  {
    this(BigDecimal.valueOf(xm),BigDecimal.valueOf(ym),
         BigDecimal.valueOf(dx),BigDecimal.valueOf(dy),
         rx,ry,limitit,hidden);
  }

  public MandelInfo(BigDecimal xm, BigDecimal ym,
                    BigDecimal dx, BigDecimal dy,
                    int rx, int ry, int limitit, boolean hidden)
  { super(xm,ym,dx,dy,rx,ry,limitit);
    setup();
    this.hidden=hidden;
  }


  public MandelInfo(MandelInfo mi)
  {
    this(mi.getXM(), mi.getYM(),
         mi.getDX(), mi.getDY(),
         mi.getRX(), mi.getRY(),
         mi.getLimitIt(),mi.hidden);
    setName(mi.getName());
    setLocation(mi.getLocation());
    setCreator(mi.getCreator());
    setSite(mi.getSite());
    setKeywords(mi.getKeywords());
  }

  private void setup()
  {
    this.version=VERSION;
    this.name="";
    this.location="";
    this.creator=null;
    this.site=null;
  }

  public MandelInfo copyFrom(MandelInfo o)
  {
    setInfo(o);
    return this;
  }

  public int getVersion()
  {
    return version;
  }

  public boolean isHidden()
  {
    return hidden;
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

  public int getTime()
  {
    return time;
  }

  public int getEffectiveTime()
  {
    return efftime;
  }

  public long getCreationTime()
  {
    return creattime;
  }

  public long getRasterCreationTime()
  {
    return calctime;
  }

  public long getImageCreationTime()
  {
    return imgtime;
  }

  public String getLocation()
  {
    return location;
  }

  public String getName()
  {
    return name;
  }

  public String getCreator()
  {
    return creator;
  }

  public String getSite()
  {
    return site;
  }

  public Set<String> getKeywords()
  {
    if (keywords==null) keywords=new HashSet<String>();
    return new HashSet(keywords);
  }

  public long getMCCnt()
  {
    return mccnt;
  }

  public long getMCnt()
  {
    return mcnt;
  }

  public int getUsedLimit()
  {
    return usedlimit;
  }

  public int getTargetSize()
  { return getMaxIt()-getMinIt()+2;
  }
  
  // setter
  public void setInfo(MandelInfo i)
  { setInfo(i,true);
  }

  public void setInfo(MandelInfo i, boolean full)
  {
    setSpec(i);
    setHidden(i.isHidden());
    if (full) {
      setMinIt(i.getMinIt());
      setMaxIt(i.getMaxIt());
      setNumIt(i.getNumIt());
      setTime(i.getTime());
      setEffectiveTime(i.getEffectiveTime());
      setMCCnt(i.getMCCnt());
      setMCnt(i.getMCnt());
      setUsedLimit(i.getUsedLimit());
      setCreationTime(i.getCreationTime());
      setRasterCreationTime(i.getRasterCreationTime());
      setImageCreationTime(i.getImageCreationTime());
    }
    setName(i.getName());
    setLocation(i.getLocation());
    setCreator(i.getCreator());
    setSite(i.getSite());
    setKeywords(i.getKeywords());
    version=i.version;
  }

  public void setHidden(boolean hidden)
  {
    this.hidden=hidden;
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

  public void setTime(int time)
  {
    this.time=time;
  }

  public void setEffectiveTime(int efftime)
  {
    this.efftime=efftime;
  }

  public void setMCCnt(long mccnt)
  {
    this.mccnt=mccnt;
  }

  public void setMCnt(long mcnt)
  {
    this.mcnt=mcnt;
  }

  public void setUsedLimit(int usedlimit)
  {
    this.usedlimit=usedlimit;
  }

  public void setCreationTime(long t)
  {
    creattime=t;
  }

  public void setRasterCreationTime(long t)
  {
    calctime=t;
  }

  public void setImageCreationTime(long t)
  {
    imgtime=t;
  }

  public void setLocation(String location)
  {
    if (location==null) location="";
    //System.out.println("location is "+location);
    this.location=location;
  }

  public void setName(String name)
  {
    if (name==null) name="";
    this.name=name;
  }

  public void setCreator(String creator)
  {
    this.creator=creator;
  }

  public void setSite(String site)
  {
    this.site=site;
  }

  public void setKeywords(Set<String> k)
  {
    if (keywords==null) keywords=new HashSet<String>(k);
    else {
      keywords.clear();
      keywords.addAll(k);
    }
  }

  public boolean hasMandelCount()
  {
    if (version>=2) return true;
    return mcnt_set;
  }

  ///////////////////////////////////////////////////////////////
  // version update
  ///////////////////////////////////////////////////////////////

  private boolean mcnt_set=false;

  public void updateData(MandelData data)
  {
    if (version==1 && mcnt==0 && data.getRaster()!=null) {
      MandelRaster raster=data.getRaster();
      int[][] r=raster.getRaster();
      int rx=raster.getRX();
      int ry=raster.getRY();
      for (int x=0; x<rx; x++) {
        for (int y=0; y<ry; y++) {
          if (r[y][x]==0) mcnt++;
        }
      }
      mcnt_set=true;
    }
  }

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  public boolean needsVersionUpdate()
  {
    //System.out.println("version is "+version);
    return version!=VERSION;
  }

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
       case 2: dos.writeInt(v);
               writeV2(dos);
               break;
       case 3: dos.writeInt(v);
               writeV3(dos);
               break;
       case 4: dos.writeInt(v);
               writeV4(dos);
               break;
      default: throw new IOException("unknown mandel info version "+v);
    }
  }

  private void writeV1(DataOutputStream dos) throws IOException
  {
    // fixed format
    dos.writeDouble(getXM().doubleValue());
    dos.writeDouble(getYM().doubleValue());
    dos.writeDouble(getDX().doubleValue());
    dos.writeDouble(getDY().doubleValue());
    dos.writeInt(getRX());
    dos.writeInt(getRY());
    dos.writeInt(getLimitIt());
    dos.writeInt(minit);
    dos.writeInt(maxit);
    dos.writeLong(numit);
    dos.writeInt(time);

    // extension
    dos.writeUTF(name);
    dos.writeUTF(location);
  }

  private void writeV2(DataOutputStream dos) throws IOException
  {
    dos.writeUTF(getXM().toString());
    dos.writeUTF(getYM().toString());
    dos.writeUTF(getDX().toString());
    dos.writeUTF(getDY().toString());
    dos.writeInt(getRX());
    dos.writeInt(getRY());
    dos.writeInt(getLimitIt());
    dos.writeBoolean(hidden);

    dos.writeInt(minit);
    dos.writeInt(maxit);
    dos.writeLong(numit);
    dos.writeInt(time);

    dos.writeInt(usedlimit);
    dos.writeLong(mcnt);
    dos.writeLong(mccnt);
    

    // extension
    dos.writeUTF(name);
    dos.writeUTF(location);
  }

  private void writeV3(DataOutputStream dos) throws IOException
  {
    writeV2(dos);
    dos.writeUTF(mapWrite(creator));
    dos.writeUTF(mapWrite(site));
  }

  private void writeV4(DataOutputStream dos) throws IOException
  {
    writeV3(dos);
    dos.writeInt(efftime);
    dos.writeLong(creattime);
    dos.writeLong(calctime);
    dos.writeLong(imgtime);
    StringBuffer sb=new StringBuffer();
    if (keywords!=null) {
      String sep="";
      for (String s:keywords) {
        sb.append(sep);
        sb.append(s);
        sep=",";
      }
    }
    dos.writeUTF(sb.toString());
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
      case 2: readV2(dis);
              break;
      case 3: readV3(dis);
              break;
      case 4: readV4(dis);
              break;
      default: throw new IOException("unknown mandel info version "+version);
    }
  }

  private void readV1(DataInputStream dis) throws IOException
  {
    // fixed format
    setXM(BigDecimal.valueOf(dis.readDouble()));
    setYM(BigDecimal.valueOf(dis.readDouble()));
    setDX(BigDecimal.valueOf(dis.readDouble()));
    setDY(BigDecimal.valueOf(dis.readDouble()));
    setRX(dis.readInt());
    setRY(dis.readInt());
    setLimitIt(dis.readInt());

    minit=dis.readInt();
    maxit=dis.readInt();
    numit=dis.readLong();
    time=dis.readInt();

    // extension
    name=dis.readUTF();
    location=dis.readUTF();
  }

  private void readV2(DataInputStream dis) throws IOException
  {
    // fixed format
    setXM(new BigDecimal(dis.readUTF()));
    setYM(new BigDecimal(dis.readUTF()));
    setDX(new BigDecimal(dis.readUTF()));
    setDY(new BigDecimal(dis.readUTF()));
    setRX(dis.readInt());
    setRY(dis.readInt());
    setLimitIt(dis.readInt());
    hidden=dis.readBoolean();

    minit=dis.readInt();
    maxit=dis.readInt();
    numit=dis.readLong();
    time=dis.readInt();

    usedlimit=dis.readInt();
    mcnt=dis.readLong();
    mccnt=dis.readLong();

    // extension
    name=dis.readUTF();
    location=dis.readUTF();
  }

  private void readV3(DataInputStream dis) throws IOException
  {
    readV2(dis);
    creator=mapRead(dis.readUTF());
    site=mapRead(dis.readUTF());
    efftime=time;
  }

  private void readV4(DataInputStream dis) throws IOException
  {
    readV3(dis);
    efftime=dis.readInt();
    creattime=dis.readLong();
    calctime=dis.readLong();
    imgtime=dis.readLong();
    if (keywords==null) keywords=new HashSet<String>();
    else keywords.clear();
    StringTokenizer t=new StringTokenizer(dis.readUTF(),",");
    while (t.hasMoreTokens()) {
      keywords.add(t.nextToken());
    }
  }

  private String mapRead(String s)
  { return Utils.isEmpty(s)?null:s;
  }
  
  private String mapWrite(String s)
  { return s==null?"":s;
  }
  
  /////////////////////////////////////////////////////////////////////////

  private boolean _false(String reason)
  {
    System.out.println("mismatch: "+reason);
    return false;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return _false("class");
    final MandelInfo other=(MandelInfo)obj;
    if (!super.equals(obj)) return _false("spec");
    if (this.version!=other.version) return _false("version");
    if (this.minit!=other.minit) return _false("minit");
    if (this.maxit!=other.maxit) return _false("maxit");
    if (this.numit!=other.numit) return _false("numit");
    if (this.time!=other.time) return _false("time");
    if (this.efftime!=other.efftime) return _false("efftime");
    if (this.creattime!=other.creattime) return _false("creattime");
    if (this.calctime!=other.calctime) return _false("calctime");
    if (this.imgtime!=other.imgtime) return _false("imgtime");
    if (this.usedlimit!=other.usedlimit) return _false("usedlimit");
    if (this.mccnt!=other.mccnt) return _false("mccnt");
    if (this.mcnt!=other.mcnt) return _false("mcnt");
    if ((this.name==null)?(other.name!=null):!this.name.equals(other.name))
      return _false("name");
    if ((this.location==null)?(other.location!=null):!this.location.equals(other.location))
      return _false("location");
    if ((this.creator==null)?(other.creator!=null):!this.creator.equals(other.creator))
      return _false("creator");
    if ((this.site==null)?(other.site!=null):!this.site.equals(other.site))
      return _false("site");
    if (this.keywords!=other.keywords&&(this.keywords==null||!this.keywords.equals(other.keywords)))
      return _false("keywords");
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=super.hashCode();
    hash=89*hash+this.version;
    hash=89*hash+this.minit;
    hash=89*hash+this.maxit;
    hash=89*hash+(int)(this.numit^(this.numit>>>32));
    hash=89*hash+this.time;
    hash=89*hash+this.efftime;
    hash=89*hash+(int)(this.creattime^(this.creattime>>>32));
    hash=89*hash+(int)(this.calctime^(this.calctime>>>32));
    hash=89*hash+(int)(this.imgtime^(this.imgtime>>>32));
    hash=89*hash+this.usedlimit;
    hash=89*hash+(int)(this.mccnt^(this.mccnt>>>32));
    hash=89*hash+(int)(this.mcnt^(this.mcnt>>>32));
    hash=89*hash+(this.name!=null?this.name.hashCode():0);
    hash=89*hash+(this.location!=null?this.location.hashCode():0);
    hash=89*hash+(this.creator!=null?this.creator.hashCode():0);
    hash=89*hash+(this.site!=null?this.site.hashCode():0);
    hash=89*hash+(this.keywords!=null?this.keywords.hashCode():0);
    return hash;
  }


}

