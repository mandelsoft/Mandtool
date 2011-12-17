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

import com.mandelsoft.mand.util.MandArith;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public class MandelAreaSpec extends MandArith {
  // basic area information
  private BigDecimal xm;   // middle x coordinate
  private BigDecimal ym;   // middle y coordinate
  private BigDecimal dx;   // width
  private BigDecimal dy;   // height

  public MandelAreaSpec()
  {
  }

  public MandelAreaSpec(BigDecimal xm, BigDecimal ym,
                        BigDecimal dx, BigDecimal dy)
  {
    this.xm = xm;
    this.ym = ym;
    this.dx = dx;
    this.dy = dy;
  }

  public MandelAreaSpec(MandelAreaSpec spec)
  {
    this(spec.xm, spec.ym, spec.dx, spec.dy);
  }

  public String getMessage()
  {
    if (dy.compareTo(BigDecimal.ZERO)==0)
      return ("dy is zero");
    if (dx.compareTo(BigDecimal.ZERO)==0)
      return ("dx is zero");
    return null;
  }

  public MandelAreaSpec getAreaSpec()
  {
    return this;
  }

  public void setSpec(MandelAreaSpec i)
  {
    setXM(i.getXM());
    setYM(i.getYM());
    setDX(i.getDX());
    setDY(i.getDY());
  }

  public boolean contains(BigDecimal x, BigDecimal y)
  {
    BigDecimal x0=sub(getXM(), div(getDX(), 2));
    BigDecimal y0=sub(getYM(), div(getDY(), 2));
    BigDecimal x1=add(getXM(), div(getDX(), 2));
    BigDecimal y1=add(getYM(), div(getDY(), 2));
    if (x.compareTo(x0)<0||x.compareTo(x1)>0) return false;
    if (y.compareTo(y0)<0||y.compareTo(y1)>0) return false;
    return true;
  }

  public BigDecimal getDX()
  {
    return dx;
  }

  public BigDecimal getDY()
  {
    return dy;
  }

  public BigDecimal getXM()
  {
    return xm;
  }

  public BigDecimal getXMax()
  {
    return add(getXM(), div(getDX(), 2));
  }

  public BigDecimal getXMin()
  {
    return sub(getXM(), div(getDX(), 2));
  }

  public BigDecimal getYM()
  {
    return ym;
  }

  public BigDecimal getYMax()
  {
    return add(getYM(), div(getDY(), 2));
  }

  public BigDecimal getYMin()
  {
    return sub(getYM(), div(getDY(), 2));
  }

  public void setDX(BigDecimal dx)
  {
    this.dx=dx;
  }

  public void setDX(double dx)
  {
    this.dx=BigDecimal.valueOf(dx);
  }

  public void setDY(BigDecimal dy)
  {
    this.dy=dy;
  }

  public void setDY(double dy)
  {
    this.dy=BigDecimal.valueOf(dy);
  }

  public void setXM(BigDecimal xm)
  {
    this.xm=xm;
  }

  public void setXM(double xm)
  {
    this.xm=BigDecimal.valueOf(xm);
  }

  public void setYM(BigDecimal ym)
  {
    this.ym=ym;
  }

  public void setYM(double ym)
  {
    this.ym=BigDecimal.valueOf(ym);
  }

  public boolean isSameArea(MandelAreaSpec o)
  {
    return getXM().equals(o.getXM()) &&
           getYM().equals(o.getYM()) &&
           getDX().equals(o.getDX()) &&
           getDY().equals(o.getDY());
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final MandelAreaSpec other=(MandelAreaSpec)obj;
    if (this.xm!=other.xm&&(this.xm==null||!this.xm.equals(other.xm)))
      return false;
    if (this.ym!=other.ym&&(this.ym==null||!this.ym.equals(other.ym)))
      return false;
    if (this.dx!=other.dx&&(this.dx==null||!this.dx.equals(other.dx)))
      return false;
    if (this.dy!=other.dy&&(this.dy==null||!this.dy.equals(other.dy)))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=7;
    hash=97*hash+(this.xm!=null?this.xm.hashCode():0);
    hash=97*hash+(this.ym!=null?this.ym.hashCode():0);
    hash=97*hash+(this.dx!=null?this.dx.hashCode():0);
    hash=97*hash+(this.dy!=null?this.dy.hashCode():0);
    return hash;
  }
}
