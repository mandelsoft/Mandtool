/*
 *  Copyright 2021 Uwe Krueger.
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

import com.mandelsoft.mand.meth.PixelIterator;
import com.mandelsoft.mand.tools.Mand;
import java.math.BigDecimal;
import com.mandelsoft.mand.util.MandArith;
import com.mandelsoft.mand.meth.AbstractPixelIterator;
import com.mandelsoft.mand.meth.DefaultMandIteratorFactory;
import java.util.Map;

/**
 *
 * @author Uwe Krueger
 */
public class MandIter extends MandArith {

  static private BigDecimal br = new BigDecimal(5.e-16);
  
  static public PixelIterator createPixelIterator(MandelSpec mi)
  {
    return createPixelIterator(mi, null);
  }

  static public PixelIterator createPixelIterator(MandelInfo mi)
  {
    return createPixelIterator(mi, mi.getProperties());
  }

  static public PixelIterator createPixelIterator(MandelInfo mi,
                                                  PixelIterator.PropertySource.PropertyHandler h)
  {
    PixelIterator pi = createPixelIterator(mi, mi.getProperties());
    PixelIterator.setPropertyHandler(pi, h);
    return pi;
  }

  static public PixelIterator createPixelIterator(MandelSpec mi,
                                                  Map<String, String> properties)
  {
    
    return new DefaultMandIteratorFactory(optimized, useDLL).createPixelIterator(mi, properties);
  }
 


  /////////////////////////////////////////////////////////////////////////
  // big decimal iterator support
  /////////////////////////////////////////////////////////////////////////
  public static abstract class BigDecimalIterator extends AbstractPixelIterator {

    protected int x;
    protected int y;
    protected BigDecimal sx;
    protected BigDecimal sy;
    protected BigDecimal dx;
    protected BigDecimal dy;
    protected BigDecimal drx;
    protected BigDecimal dry;
    protected BigDecimal x0;
    protected BigDecimal y0;
    protected BigDecimal cx;
    protected BigDecimal cy;

    public BigDecimalIterator(MandelSpec spec)
    {
      this(new BigDecimal(0), new BigDecimal(0),
              spec.getXMin(), spec.getYMax(), spec.getDX(), spec.getDY(),
              spec.getRX(), spec.getRY(), spec.getLimitIt());
    }

    public BigDecimalIterator(BigDecimal sx,
                              BigDecimal sy,
                              BigDecimal x0,
                              BigDecimal y0,
                              BigDecimal dx,
                              BigDecimal dy,
                              int rx,
                              int ry,
                              int limit)
    {
      super(rx, ry, limit, dx, dy);

      this.sx = sx;
      this.sy = sy;

      this.dx = dx;
      this.dy = dy;
      this.drx = new BigDecimal(rx);
      this.dry = new BigDecimal(ry);

      this.x0 = x0;
      this.y0 = y0;
    }

    public boolean isFast()
    {
      return false;
    }

    public void setX(int x)
    {
      this.x = x;
      cx = add(x0, div(mul(dx, x), drx));
    }

    public void setY(int y)
    {
      this.y = y;
      cy = sub(y0, div(mul(dy, y), dry));
    }

    public BigDecimal getCX()
    {
      return cx;
    }

    public BigDecimal getCY()
    {
      return cy;
    }

    public double getX(BigDecimal x)
    {
      return div(mul(sub(x, x0), drx), dx).doubleValue();
    }

    public double getY(BigDecimal y)
    {
      return div(mul(sub(y0, y), dry), dy).doubleValue();
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // big decimal iterator
  /////////////////////////////////////////////////////////////////////////
  private static class BigDecimalMandIterator extends BigDecimalIterator {

    protected int cnt;
    protected BigDecimal bound;

    public BigDecimalMandIterator(BigDecimal sx,
                                  BigDecimal sy,
                                  BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit)
    {
      super(sx, sy, x0, y0, dx, dy, rx, ry, limit);
      bound = new BigDecimal(Mand.BOUND);
    }

    private BigDecimalMandIterator(
                                  BigDecimal dx,
                                  BigDecimal dy)
    {
      super(MandIter.b0, MandIter.b0, MandIter.b0, MandIter.b0, dx, dy, 1, 1, 1);
      bound = new BigDecimal(Mand.BOUND);
    }
     
    public int iter()
    {
      cnt++;
      if (cnt % 100 == 0) {
        System.out.print(".");
        System.out.flush();
      }
      return iter(this.sx, this.sy, cx, cy,
              bound, limit);
    }

    public int iter(BigDecimal sx, BigDecimal sy,
                           BigDecimal cx, BigDecimal cy,
                           BigDecimal bound, int limit)
    {
      BigDecimal x = sx;
      BigDecimal y = sy;

      BigDecimal x2 = mul(x, x);
      BigDecimal y2 = mul(y, y);
      int it = 0;

      while (add(x2, y2).compareTo(bound) < 0 && ++it <= limit) {
        BigDecimal xn = add(sub(x2, y2), cx);
        BigDecimal yn = add(mul(mul(b2, x), y), cy);
        x = xn;
        x2 = mul(x, x);
        y = yn;
        y2 = mul(y, y);
      }
      return it;
    }
  }

//////////////////////////////////////////////////////////////////////////////
// Optimized calculation using BigDecimals
//////////////////////////////////////////////////////////////////////////////  
  public interface IterationSink {

    public void setValue(int it, Coord c);
  }

  static public int buildref(IterationSink sink, Coord c, int limit,
                             BigDecimal bound)
  {
    BigDecimal x = c.X;
    BigDecimal y = c.Y;

    sink.setValue(0, c);

    BigDecimal x2 = mul(x, x);
    BigDecimal y2 = mul(y, y);
    int it = 0;

    while (add(x2, y2).compareTo(bound) < 0 && ++it <= limit) {
      BigDecimal xn = add(sub(x2, y2), c.X);
      BigDecimal yn = add(mul(mul(b2, x), y), c.Y);
      x = xn;
      x2 = mul(x, x);
      y = yn;
      y2 = mul(y, y);
      sink.setValue(it, new Coord(x, y));
    }

    return it;
  }

  /////////////////////////////////////////////////////////////////////////
  // native support
  /////////////////////////////////////////////////////////////////////////
  native static public int iter(String sx, String sy, String cx, String cy,
                                String bound, int limit);

  native static public int iterP(String sx, String sy, String cx, String cy,
                                 String bound, int limit, int prec);
  public static boolean useDLL;
  public static boolean optimized = false;

  static {
    try {
      System.loadLibrary("gmp");
      System.loadLibrary("Mandelbrot");
      useDLL = true;
      //useDLL=false;
    }
    catch (UnsatisfiedLinkError le) {
      System.out.println("failed:" + le);
      useDLL = false;
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
              bound.toString(), limit, 100);
    }
    else {
      System.out.println("using BigDecimal mode");
      return new BigDecimalMandIterator(cx,cy).iter(sx, sy, cx, cy, bound, limit);
    }
  }

  static public void main(String[] args)
  {
    int i;
    System.out.println("calling iter");
    i = iter(b0, b0, new BigDecimal(0.5), b0, b10, 128000);
    System.out.println("got " + i);
  }
}
