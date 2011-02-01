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

import java.math.BigDecimal;
import com.mandelsoft.mand.util.MandArith;

/**
 *
 * @author Uwe Krueger
 */
public class MandelSpec extends MandArith {
  // basic image information
  private BigDecimal xm;   // middle x coordinate
  private BigDecimal ym;   // middle y coordinate
  private BigDecimal dx;   // width
  private BigDecimal dy;   // height
  private int rx;          // resolution on x axis
  private int ry;          // resolution on y axis
  private int limitit;     // iteration limitation
  
  public MandelSpec()
  { 
  }

  public MandelSpec(double xm, double ym,
                    double dx, double dy,
                    int rx, int ry, int limitit)
  {
    this(BigDecimal.valueOf(xm),BigDecimal.valueOf(ym),
         BigDecimal.valueOf(dx),BigDecimal.valueOf(dy),
         rx,ry,limitit);
  }

  public MandelSpec(BigDecimal xm, BigDecimal ym,
                    BigDecimal dx, BigDecimal dy,
                    int rx, int ry, int limitit)
  { this();
    this.xm=xm;
    this.ym=ym;
    this.dx=dx;
    this.dy=dy;
    this.rx=rx;
    this.ry=ry;
    this.limitit=limitit;
  }


  public MandelSpec(MandelSpec mi)
  {
    this(mi.xm, mi.ym, mi.dx, mi.dy, mi.rx, mi.ry, mi.limitit);
  }

  public boolean valid()
  {
    return getMessage()==null;
  }

  public String getMessage()
  {
    if (dy.compareTo(BigDecimal.ZERO)==0)
      return ("dy is zero");
    if (dx.compareTo(BigDecimal.ZERO)==0)
      return ("dx is zero");
    if (rx==0)
      return ("rx is zero");
    if (ry==0)
      return ("ry is zero");
    return null;
  }

  public MandelSpec getSpec()
  {
    return this;
  }
  
  public BigDecimal getXM()
  {
    return xm;
  }

  public BigDecimal getYM()
  {
    return ym;
  }

  public BigDecimal getDX()
  {
    return dx;
  }

  public BigDecimal getDY()
  {
    return dy;
  }

  public int getRX()
  {
    return rx;
  }

  public int getRY()
  {
    return ry;
  }

  public int getLimitIt()
  {
    return limitit;
  }

  // setter
  public void setSpec(MandelSpec i)
  { 
    setXM(i.getXM());
    setYM(i.getYM());
    setDX(i.getDX());
    setDY(i.getDY());
    setRX(i.getRX());
    setRY(i.getRY());
    setLimitIt(i.getLimitIt());
  }

  public void setXM(BigDecimal xm)
  {
    this.xm=xm;
  }

  public void setYM(BigDecimal ym)
  {
    this.ym=ym;
  }

  public void setDX(BigDecimal dx)
  {
    this.dx=dx;
  }

  public void setDY(BigDecimal dy)
  {
    this.dy=dy;
  }

  public void setXM(double xm)
  {
    this.xm=BigDecimal.valueOf(xm);
  }

  public void setYM(double ym)
  {
    this.ym=BigDecimal.valueOf(ym);
  }

  public void setDX(double dx)
  {
    this.dx=BigDecimal.valueOf(dx);
  }

  public void setDY(double dy)
  {
    this.dy=BigDecimal.valueOf(dy);
  }

  public void setRX(int rx)
  {
    this.rx=rx;
  }

  public void setRY(int ry)
  {
    this.ry=ry;
  }

  public void setLimitIt(int limitit)
  {
    this.limitit=limitit;
  }

  public boolean isSameSpec(MandelSpec o)
  {
    return getXM().equals(o.getXM()) &&
           getYM().equals(o.getYM()) &&
           getDX().equals(o.getDX()) &&
           getDY().equals(o.getDY()) &&
           getRX()==o.getRX() &&
           getRY()==o.getRY() &&
           getLimitIt()==o.getLimitIt();
  }

  public boolean isSameArea(MandelSpec o)
  {
    return getXM().equals(o.getXM()) &&
           getYM().equals(o.getYM()) &&
           getDX().equals(o.getDX()) &&
           getDY().equals(o.getDY());
  }
  
  // derived

  public BigDecimal getXMin()
  {
    return sub(getXM(),div(getDX(),2));
  }

  public BigDecimal getXMax()
  {
    return add(getXM(),div(getDX(),2));
  }

  public BigDecimal getYMin()
  {
    return sub(getYM(),div(getDY(),2));
  }

  public BigDecimal getYMax()
  {
    return add(getYM(),div(getDY(),2));
  }

  public boolean contains(BigDecimal x, BigDecimal y)
  {
    BigDecimal x0=sub(getXM(),div(getDX(),2));
    BigDecimal y0=sub(getYM(),div(getDY(),2));
    BigDecimal x1=add(getXM(),div(getDX(),2));
    BigDecimal y1=add(getYM(),div(getDY(),2));
    if (x.compareTo(x0)<0 || x.compareTo(x1)>0) return false;
    if (y.compareTo(y0)<0 || y.compareTo(y1)>0) return false;
    return true;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final MandelSpec other=(MandelSpec)obj;
    if (this.xm!=other.xm&&(this.xm==null||!this.xm.equals(other.xm)))
      return false;
    if (this.ym!=other.ym&&(this.ym==null||!this.ym.equals(other.ym)))
      return false;
    if (this.dx!=other.dx&&(this.dx==null||!this.dx.equals(other.dx)))
      return false;
    if (this.dy!=other.dy&&(this.dy==null||!this.dy.equals(other.dy)))
      return false;
    if (this.rx!=other.rx) return false;
    if (this.ry!=other.ry) return false;
    if (this.limitit!=other.limitit) return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=3;
    hash=29*hash+(this.xm!=null?this.xm.hashCode():0);
    hash=29*hash+(this.ym!=null?this.ym.hashCode():0);
    hash=29*hash+(this.dx!=null?this.dx.hashCode():0);
    hash=29*hash+(this.dy!=null?this.dy.hashCode():0);
    hash=29*hash+this.rx;
    hash=29*hash+this.ry;
    hash=29*hash+this.limitit;
    return hash;
  }

  
}

