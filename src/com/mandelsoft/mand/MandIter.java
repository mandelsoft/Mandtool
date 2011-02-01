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

import com.mandelsoft.mand.tools.Mand;
import java.math.BigDecimal;
import com.mandelsoft.mand.util.MandArith;

/**
 *
 * @author Uwe Krueger
 */
public class MandIter extends MandArith {
  static private BigDecimal br=new BigDecimal(5.e-16);
  
  static public PixelIterator createPixelIterator(MandelInfo mi)
  {
    BigDecimal x0=mi.getXMin();
    BigDecimal y0=mi.getYMax();
    BigDecimal dx=mi.getDX();
    BigDecimal dy=mi.getDY();
    int rx=mi.getRX();
    int ry=mi.getRY();
    return createPixelIterator(x0,y0,dx,dy,rx,ry,
                               mi.getLimitIt());
  }

  static public PixelIterator createPixelIterator(BigDecimal x0,
                                                  BigDecimal y0,
                                                  BigDecimal dx,
                                                  BigDecimal dy,
                                                  int rx,
                                                  int ry,
                                                  int limit)
  {
    if (div(dx,rx).compareTo(br)>0 && div(dy,ry).compareTo(br)>0) {
      System.out.println("double iteration mode");
      return new DoubleMandIterator(x0,y0,dx,dy,rx,ry,limit);
    }
    if (useDLL) {
      System.out.println("long double iteration mode");
      return new LongDoubleMandIterator(x0,y0,dx,dy,rx,ry,limit);
    }
    System.out.println("big decimal iteration mode");
    return new BigDecimalMandIterator(x0,y0,dx,dy,rx,ry,limit);
  }

  private static abstract class MandPixelIterator implements PixelIterator {
    protected int limit;
    protected int rx;
    protected int ry;
    protected int precision;
    protected int magnification;

    public MandPixelIterator(MandelInfo mi)
    {
      this(mi.getRX(),mi.getRY(),mi.getLimitIt(),
           mi.getDX(),mi.getDY());
    }

    public MandPixelIterator(int rx, int ry, int limit,
                             BigDecimal dx, BigDecimal dy)
    {
      this.limit=limit;
      this.rx=rx;
      this.ry=ry;
      setPrecision(dx,dy);
    }

    protected void setPrecision(BigDecimal dx, BigDecimal dy)
    {
      BigDecimal dX=div(dx,rx);
      BigDecimal dY=div(dy,ry);
      BigDecimal d=dX;
      if (dX.compareTo(dY)>0) d=dY;
      int p=0;
      while (d.compareTo(BigDecimal.ONE)<0) {
        p++;
        d=mul(d,BigDecimal.TEN);
      }
      int prec=((p+2)/3)*10;
      precision=(int)((p+2)/Math.log10(2));

      d=dx;
      if (dx.compareTo(dy)>0) d=dy;
      p=0;
      while (d.compareTo(BigDecimal.ONE)<0) {
        p++;
        d=mul(d,BigDecimal.TEN);
      }
      magnification=p;
    }

    public int getPrecision()
    {
      return precision;
    }

    public int getMagnification()
    {
      return magnification;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // double iterator
  /////////////////////////////////////////////////////////////////////////
  private static class DoubleMandIterator extends MandPixelIterator {
    //parameters
    private double bound;
    private double dx;
    private double dy;
    private double x0;
    private double y0;
    private double drx;
    private double dry;
    // iteration point
    private double cx;
    private double cy;

    public DoubleMandIterator(BigDecimal x0,
                              BigDecimal y0,
                              BigDecimal dx,
                              BigDecimal dy,
                              int rx,
                              int ry,
                              int limit)
    {
      super(rx,ry,limit,dx,dy);
      bound=Mand.BOUND;

      this.dx=dx.doubleValue();
      this.dy=dy.doubleValue();
      this.x0=x0.doubleValue();
      this.y0=y0.doubleValue();
      this.drx=rx;
      this.dry=ry;
    }

    public boolean isFast()
    { return true;
    }

    public void setX(int x)
    {
      cx=x0+(x*dx)/drx;
    }

    public void setY(int y)
    {
      cy=y0-(y*dy)/dry;
    }

    public int iter()
    {
      return iter(0.0, 0.0, cx, cy, bound, limit);
    }

    private static int iter(double x, double y, double px, double py,
            double bound, int limit)
    {
      double x2=x*x;
      double y2=y*y;
      int it=0;

      while (x2+y2<bound&&++it<=limit) {
        double xn=x2-y2+px;
        double yn=2*x*y+py;
        x=xn;
        x2=x*x;
        y=yn;
        y2=y*y;
      }
      return it;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // big decimal iterator support
  /////////////////////////////////////////////////////////////////////////

  private static abstract class BigDecimalIterator extends MandPixelIterator {
    protected BigDecimal dx;
    protected BigDecimal dy;
    protected BigDecimal drx;
    protected BigDecimal dry;
    protected BigDecimal x0;
    protected BigDecimal y0;
    protected BigDecimal cx;
    protected BigDecimal cy;

    public BigDecimalIterator(BigDecimal x0,
                              BigDecimal y0,
                              BigDecimal dx,
                              BigDecimal dy,
                              int rx,
                              int ry,
                              int limit)
    {
      super(rx,ry,limit,dx,dy);

      this.dx=dx;
      this.dy=dy;
      this.drx=new BigDecimal(rx);
      this.dry=new BigDecimal(ry);

      this.x0=x0;
      this.y0=y0;
    }

    public boolean isFast()
    { return false;
    }

    public void setX(int x)
    {
      cx=add(x0, div(mul(dx, x), drx));
    }

    public void setY(int y)
    {
      cy=sub(y0, div(mul(dy, y), dry));
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // long double iterator
  /////////////////////////////////////////////////////////////////////////
  private static class LongDoubleMandIterator extends BigDecimalIterator {

    protected String bound;
    protected String scx;
    protected String scy;

    public LongDoubleMandIterator(BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit)
    {
      super(x0,y0,dx,dy,rx,ry,limit);
      bound=new BigDecimal(Mand.BOUND).toString();
      System.out.println("prec: "+precision);
    }

    @Override
    public void setX(int x)
    {
      super.setX(x);
      scx=cx.toString();
    }

    @Override
    public void setY(int y)
    {
      super.setY(y);
      scy=cy.toString();
    }

    public int iter()
    {
      return MandIter.iterP("0.0", "0.0", scx, scy, bound, limit, precision);
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // big decimal iterator
  /////////////////////////////////////////////////////////////////////////

  private static class BigDecimalMandIterator extends BigDecimalIterator {
    private int cnt;
    private BigDecimal bound;

    public BigDecimalMandIterator(BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit)
    {
      super(x0,y0,dx,dy,rx,ry,limit);
      bound=new BigDecimal(Mand.BOUND);
    }

    public int iter()
    {
      cnt++;
      if (cnt%100==0) {
        System.out.print(".");
        System.out.flush();
      }
      return iter(BigDecimal.ZERO, BigDecimal.ZERO, cx, cy,
                  bound, limit);
    }

    static public int iter(BigDecimal sx, BigDecimal sy,
            BigDecimal cx, BigDecimal cy,
            BigDecimal bound, int limit)
    {
      BigDecimal x=sx;
      BigDecimal y=sy;

      BigDecimal x2=mul(x, x);
      BigDecimal y2=mul(y, y);
      int it=0;

      while (add(x2, y2).compareTo(bound)<0&&++it<=limit) {
        BigDecimal xn=add(sub(x2, y2), cx);
        BigDecimal yn=add(mul(mul(b2, x), y), cy);
        x=xn;
        x2=mul(x, x);
        y=yn;
        y2=mul(y, y);
      }
      return it;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // native support
  /////////////////////////////////////////////////////////////////////////

  native static private int iter(String sx, String sy, String cx, String cy,
          String bound, int limit);
  native static private int iterP(String sx, String sy, String cx, String cy,
          String bound, int limit, int prec);
  private static boolean useDLL;


  static {
    try {
      System.loadLibrary("gmp");
      System.loadLibrary("Mandelbrot");
      useDLL=true;
      //useDLL=false;
    }
    catch (UnsatisfiedLinkError le) {
      System.out.println("failed:"+le);
      useDLL=false;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // test
  /////////////////////////////////////////////////////////////////////////
  
  static private int iter(BigDecimal sx, BigDecimal sy,
          BigDecimal cx, BigDecimal cy,
          BigDecimal bound, int limit)
  {
    if (useDLL) {
      System.out.println("using native(dll) mode");
      return iterP(sx.toString(), sy.toString(),
              cx.toString(), cy.toString(),
              bound.toString(), limit,100);
    }
    else {
      System.out.println("using BigDecimal mode");
      return BigDecimalMandIterator.iter(sx, sy, cx, cy, bound, limit);
    }
  }

  static public void main(String[] args)
  {
    int i;
    System.out.println("calling iter");
    i=iter(b0, b0, new BigDecimal(0.5), b0, b10, 128000);
    System.out.println("got "+i);
  }
}
