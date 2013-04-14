/*
 *  Copyright 2013 Uwe Krueger.
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
package com.mandelsoft.mand.meth;

import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelSpec;
import com.mandelsoft.mand.PixelIterator;
import com.mandelsoft.mand.PixelIteratorFactory;
import com.mandelsoft.mand.tools.Mand;
import java.math.BigDecimal;
import com.mandelsoft.mand.util.MandArith;

/**
 *
 * @author Uwe Krueger
 */
public class MandelbrotPixelIteratorFactory extends MandArith
                                            implements PixelIteratorFactory {
  static private BigDecimal br=new BigDecimal(5.e-16);
  
  public PixelIterator createPixelIterator(MandelSpec mi)
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
    if (MandIter.useDLL) {
      System.out.println("long double iteration mode");
      return new LongDoubleMandIterator(x0,y0,dx,dy,rx,ry,limit);
    }
    System.out.println("big decimal iteration mode");
    return new BigDecimalMandIterator(x0,y0,dx,dy,rx,ry,limit);
  }

  /////////////////////////////////////////////////////////////////////////
  // double iterator
  /////////////////////////////////////////////////////////////////////////
  private static class DoubleMandIterator extends AbstractDoublePixelIterator {
   
    public DoubleMandIterator(BigDecimal x0,
                              BigDecimal y0,
                              BigDecimal dx,
                              BigDecimal dy,
                              int rx,
                              int ry,
                              int limit)
    {
      super(x0,y0,dx,dy,rx,ry,limit);
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
  // long double iterator
  /////////////////////////////////////////////////////////////////////////
  private static class LongDoubleMandIterator extends AbstractBigDecimalPixelIterator {

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

  private static class BigDecimalMandIterator extends AbstractBigDecimalPixelIterator {
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
}
