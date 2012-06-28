package com.mandelsoft.mand.movie;

import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.PixelIterator;
import java.math.BigDecimal;

public class MandelAccess {

  private MandelData data;
  private MandelInfo info;
  private PixelIterator pi;
  private int[][] raster;
  private int rx;
  private int ry;

  public MandelAccess(MandelData data)
  {
    this.data=data;
    info=data.getInfo();
    rx=info.getRX();
    ry=info.getRY();
    pi=MandIter.createPixelIterator(info);
    raster=data.getRaster().getRaster();
  }

  public MandelData getMandelData()
  {
    return data;
  }

  public MandelInfo getInfo()
  {
    return data.getInfo();
  }

  public boolean contains(BigDecimal x, BigDecimal y)
  {
    return info.contains(x, y);
  }

  public boolean containsY(BigDecimal y)
  {
    return info.containsY(y);
  }

  public boolean containsX(BigDecimal x)
  {
    return info.containsX(x);
  }

  public double getX(BigDecimal x)
  {
    return pi.getX(x);
  }

  public double getY(BigDecimal y)
  {
    return pi.getY(y);
  }

  public int getIter(int x, int y)
  {
    int i=raster[y][x];
    if (i==0) return info.getLimitIt()+1;
    return i;
  }

  /////////////////////////////////////////////////////////////////////////
  // preset
  /////////////////////////////////////////////////////////////////////////
  private boolean xset;
  private BigDecimal cx;
  private double ix;
 
  private boolean yset;
  private BigDecimal cy;
  private double iy;

  public void setX(BigDecimal x)
  {
    cx=x;
    xset=true;
  }

  public void setY(BigDecimal y)
  {
    cy=y;
    yset=true;
  }

  public int getIter()
  {
    if (xset) {
      ix=getX(cx);
      xset=false;
    }
    if (yset) {
      iy=getY(cy);
      yset=false;
    }
    return getIter(ix,iy);
  }

  public int getIter(BigDecimal x, BigDecimal y)
  {
    return getIter(getX(x),getY(y));
  }

  private int getIter(double ix, double iy)
  {
    if (ix<0) ix=0;
    if (ix>=rx) iy=rx-1;
    if (iy<0) iy=0;
    if (iy>=ry) iy=ry-1;

    int xmin=(int)Math.floor(ix);
    int xmax=(int)Math.ceil(ix);
    int ymin=(int)Math.floor(iy);
    int ymax=(int)Math.ceil(iy);

    if (xmin<0) xmin=xmax;
    if (xmax>=rx) xmax=xmin;
    if (ymin<0) ymin=ymax;
    if (ymax>=ry) ymax=ymin;

    int a00=getIter(xmin, ymin);
    int a01=getIter(xmin, ymax);
    int a10=getIter(xmax, ymin);
    int a11=getIter(xmax, ymax);

    double fx=ix-xmin;
    double fy=iy-ymin;
    int i= (int)((a11-a10-a01+a00)*fx*fy+(a01-a00)*fy+(a10-a00)*fx+a00);
    return i;
  }
}
