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


  // temporary cache values
  private BigDecimal x0;
  private BigDecimal x1;
  private BigDecimal y0;
  private BigDecimal y1;

  private void clearAlt()
  {
    x0=x1=y0=y1=null;
  }

  private void setAlt()
  {
    if (x0==null) {
      x0=sub(getXM(), div(getDX(), 2));
      x1=add(getXM(), div(getDX(), 2));
      y0=sub(getYM(), div(getDY(), 2));
      y1=add(getYM(), div(getDY(), 2));
    }
  }
  public boolean containsY(BigDecimal y)
  {
    setAlt();
    if (y.compareTo(y0)<0||y.compareTo(y1)>=0) return false;
    return true;
  }

  public boolean containsX(BigDecimal x)
  {
    setAlt();
    if (x.compareTo(x0)<0||x.compareTo(x1)>=0) return false;
    return true;
  }

  public boolean contains(BigDecimal x, BigDecimal y)
  {
    return containsX(x) && containsY(y);
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
    setAlt();
    return x1;
  }

  public BigDecimal getXMin()
  {
    setAlt();
    return x0;
  }

  public BigDecimal getYM()
  {
    return ym;
  }

  public BigDecimal getYMax()
  {
    setAlt();
    return y1;
  }

  public BigDecimal getYMin()
  {
    setAlt();
    return y0;
  }

  public void setDX(BigDecimal dx)
  {
    clearAlt();
    this.dx=dx;
  }

  public void setDX(double dx)
  {
    setDX(BigDecimal.valueOf(dx));
  }

  public void setDY(BigDecimal dy)
  {
    clearAlt();
    this.dy=dy;
  }

  public void setDY(double dy)
  {
    setDY(BigDecimal.valueOf(dy));
  }

  public void setXM(BigDecimal xm)
  {
    clearAlt();
    this.xm=xm;
  }

  public void setXM(double xm)
  {
    setXM(BigDecimal.valueOf(xm));
  }

  public void setYM(BigDecimal ym)
  {
    clearAlt();
    this.ym=ym;
  }

  public void setYM(double ym)
  {
    setYM(BigDecimal.valueOf(ym));
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
