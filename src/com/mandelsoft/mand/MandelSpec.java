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

/**
 *
 * @author Uwe Krueger
 */
public class MandelSpec extends MandelAreaSpec {
  // basic image information
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
  { super(xm,ym,dx,dy);
    this.rx=rx;
    this.ry=ry;
    this.limitit=limitit;
  }


  public MandelSpec(MandelSpec mi)
  {
    this(mi.getXM(), mi.getYM(), mi.getDX(), mi.getDY(),
         mi.rx, mi.ry, mi.limitit);
  }

  public boolean valid()
  {
    return getMessage()==null;
  }

  @Override
  public String getMessage()
  {
    if (rx==0)
      return ("rx is zero");
    if (ry==0)
      return ("ry is zero");
    return super.getMessage();
  }

  public MandelSpec getSpec()
  {
    return this;
  }

  @Override
  public MandelAreaSpec getAreaSpec()
  {
    return new MandelAreaSpec(this);
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
    super.setSpec(i);
    setRX(i.getRX());
    setRY(i.getRY());
    setLimitIt(i.getLimitIt());
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
    return super.isSameArea(o) &&
           getRX()==o.getRX() &&
           getRY()==o.getRY() &&
           getLimitIt()==o.getLimitIt();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (!super.equals(obj)) return false;
    final MandelSpec other=(MandelSpec)obj;
    if (this.rx!=other.rx) return false;
    if (this.ry!=other.ry) return false;
    if (this.limitit!=other.limitit) return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=3;
    hash=29*hash+super.hashCode();
    hash=29*hash+this.rx;
    hash=29*hash+this.ry;
    hash=29*hash+this.limitit;
    return hash;
  }
}

