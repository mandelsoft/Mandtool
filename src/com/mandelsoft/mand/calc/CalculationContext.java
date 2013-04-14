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
package com.mandelsoft.mand.calc;

import com.mandelsoft.mand.*;

/**
 *
 * @author Uwe Krueger
 */
public abstract class CalculationContext extends MandelSpec {

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

  private PixelIterator iter;

  public CalculationContext()
  {
  }

  public CalculationContext(MandelSpec spec)
  {
    this(spec,0,0,spec.getRX(),spec.getRY());
  }

  public CalculationContext(MandelSpec spec, int sx, int sy,
                                             int nx, int ny)
  {
    this();
    setSpec(spec);
    this.sx=sx;
    this.sy=sy;
    this.nx=nx;
    this.ny=ny;
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

  public PixelIterator getPixelIterator()
  {
    if (iter==null) {
      iter=MandIter.createPixelIterator(this);
    }
    return iter;
  }

  public void setInitialInfo(MandelInfo mi)
  {
    minit=mi.getMinIt();
    maxit=mi.getMaxIt();
  }

  public void setSX(int sx)
  {
    resetData();
    this.sx=sx;
  }

  public void setSY(int sy)
  {
    resetData();
    this.sy=sy;
  }

  public void setNX(int nx)
  {
    resetData();
    this.nx=nx;
  }

  public void setNY(int ny)
  {
    resetData();
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

  public void addMCnt(int n)
  {
    mcnt+=n;
  }

  public void incMCnt()
  {
    mcnt++;
  }

  public void addNumIt(int n)
  {
    numit+=n;
  }

  public void incNumIt()
  {
    numit++;
  }

  public int incorporateIteration(int x, int y, int i)
  {
    int it;

    ccnt++;
    if (i>getLimitIt()) {
      setDataRel(x, y, it=0);
      mcnt++;
      i--;
    }
    else {
      setDataRel(x, y, it=i);
    }
    if (i<minit) minit=i;
    if (i>maxit) maxit=i;
    numit+=i;
    return it;
  }
  
  public void setPixelIterator(PixelIterator i)
  {
    this.iter=i;
  }

  public boolean isSameSpec(CalculationContext o)
  {
    return getSX()==o.getSX() &&
           getSY()==o.getSY() &&
           getNX()==o.getNX() &&
           getNY()==o.getNY() &&
           isSameSpec(getSpec());
  }

  public int getDataAbs(int x, int y)
  {
    return getDataRel(x-sx, y-sy);
  }

  public void setInfoTo(MandelInfo mi)
  {
    mi.setMinIt(minit);
    mi.setMaxIt(maxit);
    mi.setNumIt(numit);

    mi.setMCnt(mcnt);
    mi.setMCCnt(ccnt);
    mi.setTime((int)(mtime/1000));
  }

  ///////////////////////////////////////////////////////////////
  // impl
  ///////////////////////////////////////////////////////////////

  abstract public int getDataRel(int x, int y);
  abstract public void setDataRel(int x, int y, int it);

  abstract protected void resetData();
  abstract public void createData();
}

