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
import com.mandelsoft.mand.meth.AbstractPixelIterator;
import com.mandelsoft.mand.util.MandUtils;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Uwe Krueger
 */
public class MandIter extends MandArith {
  static private BigDecimal br=new BigDecimal(5.e-16);  
  
  static public PixelIterator createPixelIterator(MandelSpec mi)
  {
    return createPixelIterator(mi, null);
  }
  
  static public PixelIterator createPixelIterator(MandelInfo mi)
  {
    return createPixelIterator(mi, mi.getProperties());
  }
  
  static public PixelIterator createPixelIterator(MandelInfo mi, PixelIterator.PropertySource.PropertyHandler h)
  {
    PixelIterator pi= createPixelIterator(mi, mi.getProperties());
    PixelIterator.setPropertyHandler(pi, h);
    return pi;
  }
  
  static public PixelIterator createPixelIterator(MandelSpec mi, Map<String,String> properties)
  {
    BigDecimal sx=new BigDecimal(0);
    BigDecimal sy=new BigDecimal(0);
    
    BigDecimal x0=mi.getXMin();
    BigDecimal y0=mi.getYMax();
    BigDecimal dx=mi.getDX();
    BigDecimal dy=mi.getDY();
    int rx=mi.getRX();
    int ry=mi.getRY();
    int limit=mi.getLimitIt();
    
    if (MandArith.div(dx,rx).compareTo(br)>0 && MandArith.div(dy,ry).compareTo(br)>0) {
      System.out.println("double iteration mode");
      return new DoubleMandIterator(sx,sy, x0,y0, dx,dy,rx,ry,limit);
    }
    if (optimized) {
      System.out.println("optimized iteration mode");
      return new OptimizedBigDecimalMandIterator(x0,y0,dx,dy,rx,ry,limit,properties);
    }
    if (useDLL) {
      System.out.println("long double iteration mode");
      return new LongDoubleMandIterator(sy,sy, x0,y0,dx,dy,rx,ry,limit);
    }
    System.out.println("big decimal iteration mode");
    return new BigDecimalMandIterator(sx,sy, x0,y0,dx,dy,rx,ry,limit);
  }

 
  /////////////////////////////////////////////////////////////////////////
  // double iterator
  /////////////////////////////////////////////////////////////////////////
  private static class DoubleMandIterator extends AbstractPixelIterator {
    //parameters
    private double bound;
    private double sx;
    private double sy;
    private double dx;
    private double dy;
    private double x0;
    private double y0;
    private double drx;
    private double dry;
    // iteration point
    private double cx;
    private double cy;

    public DoubleMandIterator(BigDecimal sx,
                              BigDecimal sy,
                              BigDecimal x0,
                              BigDecimal y0,
                              BigDecimal dx,
                              BigDecimal dy,
                              int rx,
                              int ry,
                              int limit)
    {
      super(rx,ry,limit,dx,dy);
      bound=Mand.BOUND;

      this.sx=sx.doubleValue();
      this.sy=sy.doubleValue();
      
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

     public BigDecimal getCX()
    {
      return new BigDecimal(cx);
    }

    public BigDecimal getCY()
    {
      return new BigDecimal(cy);
    }
    
    public double getX(BigDecimal x)
    {
      double xd=x.doubleValue();
      return (xd-x0)*drx/dx;
    }

    public double getY(BigDecimal y)
    {
      double yd=y.doubleValue();
      return (y0-yd)*dry/dy;
    }

    public int iter()
    {
      return iter(this.sx, this.sy, cx, cy, bound, limit);
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
           spec.getXMin(),spec.getYMax(),spec.getDX(),spec.getDY(),
           spec.getRX(),spec.getRY(),spec.getLimitIt());
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
      super(rx,ry,limit,dx,dy);

      this.sx=sx;
      this.sy=sy;
      
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
      this.x=x;
      cx=add(x0, div(mul(dx, x), drx));
    }

    public void setY(int y)
    {
      this.y=y;
      cy=sub(y0, div(mul(dy, y), dry));
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
      return div(mul(sub(x,x0),drx),dx).doubleValue();
    }

    public double getY(BigDecimal y)
    {
       return div(mul(sub(y0,y),dry),dy).doubleValue();
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // long double iterator
  /////////////////////////////////////////////////////////////////////////
  private static class LongDoubleMandIterator extends BigDecimalIterator {

    protected String bound;
    protected String ssx;
    protected String ssy;
    protected String scx;
    protected String scy;

    public LongDoubleMandIterator(BigDecimal sx,
                                  BigDecimal sy,
                                  BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit)
    {
      super(sx,sy,x0,y0,dx,dy,rx,ry,limit);
      bound=new BigDecimal(Mand.BOUND).toString();
      System.out.println("prec: "+precision);
      this.ssx=sx.toString();
      this.ssy=sy.toString();
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
      return MandIter.iterP(this.ssx, this.ssy,
              scx, scy, bound, limit, precision);
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
      super(sx,sy, x0,y0,dx,dy,rx,ry,limit);
      bound=new BigDecimal(Mand.BOUND);
    }

    public int iter()
    {
      cnt++;
      if (cnt%100==0) {
        System.out.print(".");
        System.out.flush();
      }
      return iter(this.sx, this.sy, cx, cy,
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
  
  //////////////////////////////////////////////////////////////////////////////
  // Optimized calculation using BigDecimals
  //////////////////////////////////////////////////////////////////////////////
  
  public static class Coord {
    BigDecimal X;
    BigDecimal Y;
    double x;
    double y;
    double sqs;
    
    public Coord(double cx, double cy)
    {
      X=new BigDecimal(cx);
      Y=new BigDecimal(cy);
      x=cx;
      y=cy;
      sqs=x*x+y*y;
    }
     
    public Coord(BigDecimal cx, BigDecimal cy)
    {
      X=cx;
      Y=cy;
      x=cx.doubleValue();
      y=cy.doubleValue();
      sqs=x*x+y*y;
    }
    
    @Override
    public String toString()
    {
      return String.format("(%s,%s)", X.toString(), Y.toString());
    }
    
    public double getX()
    { return x;
    }
    public double getY()
    { return y;
    }
    
    static public Coord parse(String s) throws NumberFormatException{
      if (!s.startsWith("(") || !s.endsWith(")")) throw new NumberFormatException("missing brackets");
      s=s.substring(1, s.length()-1);
      int i=s.indexOf(',');
      if (i<=0) throw new NumberFormatException("missing comma");
      BigDecimal X=new BigDecimal(s.substring(0,i));
      BigDecimal Y=new BigDecimal(s.substring(i+1));
      return new Coord(X,Y);
    }
  }
  
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
  
  
  public static final String ATTR_REFCOORD = MandelInfo.ATTR_REFCOORD;
  public static final String ATTR_REFPIXEL = MandelInfo.ATTR_REFPIXEL;
  public static final String ATTR_ITERATONMETHOD = MandelInfo.ATTR_ITERATONMETHOD;
  
  private static class OptimizedBigDecimalMandIterator extends BigDecimalMandIterator
          implements PixelIterator.PropertySource, PixelIterator.Setup {
    private Coord ref;
    private int pixelX;
    private int pixelY;
    private Coord[] iterations;
    private PropertyHandler handler;
    
    public OptimizedBigDecimalMandIterator(
                                  BigDecimal x0,
                                  BigDecimal y0,
                                  BigDecimal dx,
                                  BigDecimal dy,
                                  int rx,
                                  int ry,
                                  int limit,
                                  Map<String,String> properties)
    {
      super(new BigDecimal(0), new BigDecimal(0), x0,y0,dx,dy,rx,ry,limit);
      System.out.println("prec: "+precision+" ("+Math.ceil(precision*Math.log10(2))+")");
      iterations=new Coord[limit+1];
      if (properties != null) {
        if (properties.containsKey(ATTR_REFCOORD)) {
          try {
            ref = Coord.parse(properties.get(ATTR_REFCOORD));
          }
          catch (NumberFormatException ex) {
          }
        }
        if (properties.containsKey(ATTR_REFPIXEL)) {
          try {
            Coord p = Coord.parse(properties.get(ATTR_REFCOORD));
            pixelX=(int)p.getX();
            pixelY=(int)p.getY();
          }
          catch (NumberFormatException ex) {

          }
        }
      }
    
      if (ref==null) {
        ref=new Coord(add(x0, div(dx, 2)), sub(y0, div(dy, 2)));
      }
    }
    
    public void setup()
    {
      buildref(iterations,ref);
    }
    
    int buildref(Coord[] iterations, Coord c)
    {
      BigDecimal x=c.X;
      BigDecimal y=c.Y;
      
      System.out.printf("calculating ref with limit %d for %s\n", limit, c);
      long stime = System.currentTimeMillis();
      iterations[0]=ref;

      BigDecimal x2=mul(x, x);
      BigDecimal y2=mul(y, y);
      int it=0;

      while (add(x2, y2).compareTo(bound)<0&&++it<=limit) {
        BigDecimal xn=add(sub(x2, y2), c.X);
        BigDecimal yn=add(mul(mul(b2, x), y), c.Y);
        x=xn;
        x2=mul(x, x);
        y=yn;
        y2=mul(y, y);
        iterations[it]=new Coord(x,y);
      }
      long etime = System.currentTimeMillis();
      System.out.printf("ref done: it=%d, %s\n", it, MandUtils.time((int)((etime-stime)/1000)));
      
      if (it<limit) {
        iterations[it+1]=null;
      }
      return it;
    }
    
    private void setProperties(int x, int y, Coord c)
    {
      if (handler!=null) {
        Coord p = new Coord(x,y);
        Map<String,String> map=new HashMap<>();
        map.put(ATTR_REFCOORD, c.toString());
        if (pixelX!=0 || pixelY!=0) {
          map.put(ATTR_REFPIXEL, p.toString());
        } else {
          map.put(ATTR_REFPIXEL, null);
        }
        handler.updateProperties(map);
      }
    }
    
    @Override
    public int iter()
    {
      cnt++;
      /*
      if (cnt%100==0) {
        System.out.print(".");
        System.out.flush();
      }
      */
      
      double ox0;
      double oy0;
      double ox;
      double oy;
      
      restart:
      while (true) {
        ox0 = sub(cx, ref.X).doubleValue();
        oy0 = sub(cy, ref.Y).doubleValue();

        ox = ox0;
        oy = oy0;

        for (int i = 0; i <= limit; i++) {
          if (iterations[i] == null) {
            ref = new Coord(cx, cy);
            System.out.printf("%d exeeded ref limit %d\n", cnt, i);
            buildref(iterations, ref);
            pixelX=x;
            pixelY=y;
            setProperties(x,y,ref);
            continue restart;
          }
          // dist = (Xn+On)^2
          double ox2 = ox * ox;
          double oy2 = oy * oy;
          double mx = 2 * iterations[i].x * ox;
          double my = 2 * iterations[i].y * oy;
          double dist = iterations[i].sqs + mx + my + ox2 + oy2;

          if (dist >= Mand.BOUND) {
            return i;
          }

          // Dn = (2 Xn-1 Dn-1) + (Dn-1^2) + D0
          double fx = mx - my;
          double fy = 2 * (iterations[i].x * oy + iterations[i].y * ox);

          double qx = ox2 - oy2;
          double qy = 2 * ox * oy;

          ox = fx + qx + ox0;
          oy = fy + qy + oy0;
        }
        return limit+1;
      }
    }

    @Override
    public void setPropertyHandler(PropertyHandler h)
    {
      handler=h;
      if (h!=null) {
        Map<String,String> map = new HashMap<>();
        map.put(ATTR_ITERATONMETHOD,this.getClass().getName());
        h.updateProperties(map);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // native support
  /////////////////////////////////////////////////////////////////////////

  native static public int iter(String sx, String sy, String cx, String cy,
          String bound, int limit);
  native static public int iterP(String sx, String sy, String cx, String cy,
          String bound, int limit, int prec);
  public static boolean useDLL;
  public static boolean optimized=false;

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
