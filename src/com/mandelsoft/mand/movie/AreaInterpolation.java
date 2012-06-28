/*
 *  Copyright 2012 Uwe Krueger.
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

package com.mandelsoft.mand.movie;

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.MandelSpec;
import com.mandelsoft.mand.PixelIterator;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.calc.AreaCalculator;
import com.mandelsoft.mand.calc.MandelRasterCalculationContext;
import com.mandelsoft.mand.calc.OptimizedAreaCalculator;
import com.mandelsoft.mand.calc.SimpleAreaCalculator;
import com.mandelsoft.mand.scan.FolderMandelScanner;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tools.Command;
import com.mandelsoft.mand.util.MandArith;
import com.mandelsoft.mand.util.MandUtils;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Uwe Krueger
 */
public class AreaInterpolation extends MandArith {
  static public final double zoombase=0.1;
  
  public static class ZoomHandler {
    private boolean simulate;
    private MandelScanner scanner;
    private MandelName next;
    private boolean matched;

    public ZoomHandler(boolean simulate, MandelScanner scanner)
    {
      this.simulate=simulate;
      this.scanner=scanner;
    }

    public boolean hasMatched()
    {
      return matched;
    }

    public boolean isSimulate()
    {
      return simulate;
    }

    public void setNext(MandelName next)
    {
      this.next=next;
      matched=false;
    }

    public MandelName getNext()
    {
      return next;
    }

    public MandelScanner getScanner()
    {
      return scanner;
    }

    public MandelData getNext(AreaInterpolation i)
    {
      if (next==null || scanner==null) return null;
      MandelHandle h=scanner.getMandelData(next);
      if (h==null) return null;
      try {
        MandelData md=h.getData();
        if (md!=null) {
          if (i.getCurrent().getInfo().getDX().equals(md.getInfo().getDX())) {
            System.out.println("found "+getNext());
            matched=true;
          }
          else {
            System.out.println("found "+getNext()+" does not match current zoom");
            md=null;
          }
        }
        return md;
      }
      catch (IOException ex) {
        return null;
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  
  static public class SlowDown {
    public double sdt;
    public double sd;

    public double z1;
    public double v1;
    public double ve;
    public double a;
    public double t;
    public double c;
    public double orig;
    public double sda;
    
    public SlowDown(double zpf, double sdt, double sd)
    {
      this.sdt=sdt;
      this.sd=sd;

      z1=Math.log(sdt)/Math.log(zoombase);
      v1=Math.log(zpf)/Math.log(zoombase);
      ve=sd*v1;
      a=v1*v1*(1-sd*sd)/2/z1;
      t=2*z1/v1/(1+sd);
      orig=Math.log(sdt)/Math.log(zpf);
      sda=Math.pow(zoombase, a);
      c=Math.pow(zoombase, Math.sqrt(a));
    }

    public void print(String msg)
    {
      System.out.println("*** "+msg);
      System.out.println("slow down threashold:                "+sdt+" ("+z1+")");
      System.out.println("zoom speed:                          "+v1);
      System.out.println("final speed:                         "+ve);
      System.out.println("a:                                   "+a);
      System.out.println("slow down factor:                    "+(1/sda));
      System.out.println("correction factor:                   "+c);
      System.out.println("number of frames:                    "+t);
      System.out.println("number of original frames:           "+orig);
      System.out.println("additional frames:                   "+(t-orig));
    }
  }

  /////////////////////////////////////////////////////////////////////////

  public static class Area {
    private QualifiedMandelName name;
    private MandelAccess access;

    public Area(QualifiedMandelName name, MandelAccess access)
    {
      this.name=name;
      this.access=access;
    }

    public MandelAccess getAccess()
    {
      return access;
    }

    public QualifiedMandelName getName()
    {
      return name;
    }

    public MandelName getMandelName()
    {
      return name.getMandelName();
    }

    public MandelData getMandelData()
    {
      return access.getMandelData();
    }

    public MandelInfo getInfo()
    {
      return access.getInfo();
    }

    public int getIter()
    {
      return access.getIter();
    }

    public void setY(BigDecimal y)
    {
      access.setY(y);
    }

    public void setX(BigDecimal x)
    {
      access.setX(x);
    }

    public double getY(BigDecimal y)
    {
      return access.getY(y);
    }

    public double getX(BigDecimal x)
    {
      return access.getX(x);
    }

    public boolean containsY(BigDecimal y)
    {
      return access.containsY(y);
    }

    public boolean containsX(BigDecimal x)
    {
      return access.containsX(x);
    }

    public boolean contains(BigDecimal x, BigDecimal y)
    {
      return access.contains(x, y);
    }
  }

  /////////////////////////////////////////////////////////////////////////
  static boolean debug=false;

  static final AreaCalculator calc_opt=new OptimizedAreaCalculator();
  static final AreaCalculator calc_simple=new SimpleAreaCalculator();

  private BigDecimal dx;

  private Area top;
  private MandelInfo info;
  private Area bottom;
  private int intercnt;
  private MandelData current;

  private File intermediate_dir;
  private MandelScanner intermediate_scanner;
  private ZoomHandler handler;

  private Area    intermediate_start;
  private Area    intermediate_end;
  private double intermediate_factor;
  private int    intermediate_count;
  private int    intermediate_current;

  // slow down data
  private BigDecimal end_zoom;
  private BigDecimal threshold;
  private double sdt;
  private double sd;
  private double sda; // slow down correction

  private double limit;
  private MandelName target;
  private MandelScanner scanner;
  private BigDecimal zoom;
  private int count=0;

  
  public AreaInterpolation(MandelName target, Double zoom, 
                           MandelScanner scanner, ZoomHandler handler)
                           throws IOException
  {
    setup(target,zoom,scanner,handler);
  }

  private  void setup(MandelName target, Double zoom, 
                      MandelScanner scanner, ZoomHandler handler)
  {
    this.target=target;
    this.scanner=scanner;
    this.handler=handler;
    this.zoom=new BigDecimal(zoom);
  }

  public AreaInterpolation(MandelName target, Double zoom, 
                           MandelScanner scanner, ZoomHandler handler,
                           double limit, File intermediate)
                           throws IOException
  {
    this.limit=limit;
    this.intermediate_dir=intermediate;
    if (intermediate!=null) {
      intermediate_scanner=new FolderMandelScanner(intermediate);
    }
    System.out.println("intermediate "+limit+" ("+intermediate_dir+")");
    setup(target,zoom,scanner, handler);
  }

  private double total_zoom;
  private double total_frames;

  public void check() throws IOException
  {
    MandelHandle h=scanner.getMandelInfo(MandelName.ROOT);
    if (h==null) throw new IOException("no root area found");
    MandelData start=h.getInfo();
    h=scanner.getMandelInfo(target);
    if (h==null) throw new IOException("target area not found");
    MandelData end=h.getInfo();
    end_zoom=end.getInfo().getDX();
    if (sdt>0) {
      threshold=div(end_zoom,sdt);
    }
    else {
      threshold=BigDecimal.ZERO;
    }
    total_zoom=div(end.getInfo().getDX(),start.getInfo().getDX()).doubleValue();
    total_frames=Math.log(total_zoom)/Math.log(zoom.doubleValue());
  }

  public void setSlowDownThreshold(double sdt)
  {
    this.sdt=sdt;
  }

  public void setFinalSpeedFactor(double sd)
  {
    this.sd=sd;
  }

  public double getTotalFrames()
  {
    return total_frames;
  }

  public double getTotalZoom()
  {
    return total_zoom;
  }

  private boolean isIntermediateRoot()
  {
    return intermediate_end!=null;
  }

  private void finishIntermediate()
  {
    intermediate_end=null;
  }

  private boolean continueWithNextSubArea(Area next) throws IOException
  {
    System.out.println("start next interpolation root "+next.getName());
    top=next;
    intercnt=0;
    info=new MandelInfo(top.getInfo());
    bottom=createIntermediate();
    if (bottom!=null) {
      // futher intermediate bottom areas to use
      System.out.println("next sub is "+bottom.getName());
      return true;
    }
    if (isIntermediateRoot()) {
      // no more intermediate subs
      // so finally continue with original intermediate target area
      
      System.out.println("reuse intermediate target area "+
                          intermediate_end.getName()+
                          " as next sub area for "+
                          next.getName());
      bottom=intermediate_end;
      finishIntermediate();
      return true;
    }

    System.out.println("next original main area is "+next.getName());
    if (next.getMandelName().equals(target)) {
      bottom=null;
      System.out.println("target reached");
      return false;
    }
    else {
      // try next area in tree path down to target
      MandelName n=next.getMandelName().sub(target);
      QualifiedMandelName qn=new QualifiedMandelName(n,"adjust");
      MandelHandle h=scanner.getMandelData(qn);
      if (h==null) {
        qn=new QualifiedMandelName(n,"zoom");
        h=scanner.getMandelData(qn);
      }
      if (h==null) {
        qn=new QualifiedMandelName(n);
        h=scanner.getMandelData(n);
      }
      if (h==null) throw new IOException("cannot find "+n);
      bottom=new Area(qn,new MandelAccess(h.getData()));

      // check for interpolation limit
      // if zoom too large inject explicitly calculated intermediate area
      double z=div(bottom.getInfo().getDX(), info.getDX()).doubleValue();
      if (z<limit) {
        // zpf to next bottom rea too large calculate intermediate zpf variations
        // save official bottom area
        next=bottom;
        // calculate number of required intermediate area

        double num=Math.ceil(Math.log(z)/Math.log(limit));
        System.out.println("requires "+((int)(num-1))
          +" intermedite zooms to meet limit of "+limit);

        double t=Math.pow(z, 1/num);
        intermediate_start=top;
        intermediate_end=bottom;
        intermediate_factor=t;
        intermediate_count=(int)num-1;
        intermediate_current=0;
        bottom=createIntermediate();
      }
      return true;
    }
  }

  private Area createIntermediate()
  {
     if (intermediate_current == intermediate_count) {
       return null;
     }
     intermediate_current++;
     String label="zoom-"+intermediate_start.getMandelName().sub(target).getSubAreaName()+
                  "-"+intermediate_current;
     QualifiedMandelName iname=new QualifiedMandelName(intermediate_start.getMandelName(),label);
     BigDecimal dx=mul(info.getDX(),intermediate_factor);
     MandelData md=createZoom(intermediate_start.getInfo(),
                              intermediate_end.getInfo(),dx);

     if (intermediate_scanner!=null) {
       MandelHandle h=intermediate_scanner.getMandelData(iname);
       if (h!=null) {
        try {
          MandelData me=h.getData();
          if (me.getInfo().getDX().equals(md.getInfo().getDX())) {
            System.out.println("reusing intermediate "+iname+"...");
            return new Area(iname,new MandelAccess(me));
          }
          System.out.println("existing intermediate "+iname+" does not match: dx "+
             me.getInfo().getDX()+" differs from expected "+md.getInfo().getDX());
        }
        catch (IOException ex) {
          // continue calculating
         }
       }
     }
     System.out.println("calculating intermediate "+iname+"...");
     calc(md,null,calc_opt);
     if (intermediate_dir!=null) {
       System.out.println("saving "+iname);
       md.getInfo().setLocation("intermediate zoom "+iname+
                                " for interpolation limit "+limit);
       save(intermediate_dir,iname,md);
     }
     return new Area(iname,new MandelAccess(md));
  }

  private void calc(MandelData md,
                    PixelIterator pi, AreaCalculator calc)
  {
    MandelRasterCalculationContext ctx=new MandelRasterCalculationContext(md.getInfo());
    if (pi!=null) ctx.setPixelIterator(pi);
    calc.calc(ctx);
    ctx.setInfoTo(md.getInfo());
    md.getInfo().setRasterCreationTime(System.currentTimeMillis());
    md.setRaster(ctx.getRaster());
  }

  private MandelData createZoom(MandelInfo start, MandelInfo end,
                                BigDecimal dx)
  {
    MandelSpec spec=new MandelSpec(start.getSpec());

    BigDecimal f=div(sub(dx,start.getDX()),
                     sub(end.getDX(),start.getDX()));

    spec.setXM(approach(start.getXM(), end.getXM(),f));
    spec.setYM(approach(start.getYM(), end.getYM(),f));
    spec.setDX(dx);
    spec.setDY(div(mul(dx,start.getRY()),start.getRX()));
    spec.setLimitIt(Math.max(start.getLimitIt(), end.getLimitIt()));
    MandelInfo next=new MandelInfo(spec,start);
    MandUtils.round(next);
    return new MandelData(next);
  }

  private boolean prepareNext() throws IOException
  {
    System.out.println("using zoom factor "+zoom);
    dx=mul(dx,zoom);
    if (dx.compareTo(threshold)<0) {
      // in slow down mode
      if (sda==0) {
        SlowDown s=new SlowDown(zoom.doubleValue(),div(end_zoom,dx).doubleValue(),sd);
        s.print("starting slow down");
        sda=s.sda;
        // zoom=mul(zoom,s.c); // correction factor for first slow down
      }
      BigDecimal next=div(zoom,sda);
      if (next.compareTo(BigDecimal.ONE)<0) {
        zoom=next;
      }
      else {
        System.out.println("stop slow down");
      }
    }
    while (dx.compareTo(bottom.getInfo().getDX())<=0) {
      if (!continueWithNextSubArea(bottom)) {
        return false;
      }
    }

    current=createZoom(info,bottom.getInfo(),dx);
    intercnt++;
    return true;
  }

  private BigDecimal approach(BigDecimal s, BigDecimal d, BigDecimal f)
  {
    return add(s, mul(sub(d,s),f) );
  }

  public MandelName getTarget()
  {
    return target;
  }

  public MandelName getTopAreaName()
  {
    return top.getMandelName();
  }

  public QualifiedMandelName getTopName()
  {
    return top.getName();
  }

  public MandelAccess getTopAccess()
  {
    return top.getAccess();
  }

  public MandelAccess getBottomAccess()
  {
    if (bottom==null) return null;
    return bottom.getAccess();
  }

  public int getInterpolationCount()
  {
    return intercnt;
  }

  public MandelData getCurrent()
  {
    return current;
  }

  public int getCount()
  {
    return count;
  }

  public boolean hasNext()
  {
    return top==null || !top.getMandelName().equals(target);
  }
  
  public MandelData getNext() throws IOException
  {
    if (!hasNext()) return null;

    if (top==null) {
      MandelHandle h=scanner.getMandelData(MandelName.ROOT);
      Area root=new Area(QualifiedMandelName.ROOT,new MandelAccess(h.getData()));
      continueWithNextSubArea(root);
      dx=info.getDX();
      current=top.getMandelData();
    }
    else {
      if (!prepareNext()) {
        current=top.getMandelData();
      }
      else {
        Interpolator i=new Interpolator(current.getInfo());
        if (handler!=null) {
          if (handler.isSimulate()) {
            return current;
          }
          MandelData md=handler.getNext(this);
          if (md!=null) {
            current=md;
          }
        }
        if (current.getRaster()==null) {
          System.out.println("interpolating "+top.getName()+
                     "("+intercnt+") ...");
          calc(current, i, calc_simple);
          current.getInfo().setLocation("interpolation "+intercnt+" for "+
                                        top.getName());
        }
//      MandelRasterCalculator c=new SimpleMandelRasterCalculator(current.getInfo());
//      c.setPixelIterator(i);
//      c.calc();
//      c.setInfoTo(current.getInfo());
//      current.getInfo().setRasterCreationTime(System.currentTimeMillis());
//      current.setRaster(c.getRaster());
      }
    }
    count++;
    return current;
  }

  /////////////////////////////////////////////////////////////////////////

  private class Interpolator implements PixelIterator {
    private PixelIterator pi;
    private int limit;

    public Interpolator(MandelSpec spec)
    {
      pi=MandIter.createPixelIterator(spec);
      limit=spec.getLimitIt()+1;
      if (debug) {
        System.out.println(" start: x="+spec.getXMin()+"<->"+spec.getXMax());
        System.out.println("        y="+spec.getYMin()+"<->"+spec.getYMax());
        System.out.println("  main: x="+info.getXMin()+"<->"+info.getXMax());
        System.out.println("        y="+info.getYMin()+"<->"+info.getYMax());
        System.out.println("   sub: x="+bottom.getInfo().getXMin()+"<->"+bottom.
          getInfo().getXMax());
        System.out.println("        y="+bottom.getInfo().getYMin()+"<->"+bottom.
          getInfo().getYMax());
      }
    }

    private BigDecimal cx;
    private BigDecimal cy;
    private boolean subx;
    private boolean suby;
    private int x,y;

    public int iter()
    { int it;

      if (subx && suby) {
        //System.out.println("  iter bottom "+cx+", "+cy);
        it=bottom.getIter();
        if (it>bottom.getInfo().getLimitIt()) it=limit;
      }
      else {
        //System.out.println("  iter top "+cx+", "+cy);
        it=top.getIter();
        if (it>top.getInfo().getLimitIt()) it=limit;
      }
      return it;
    }

    public void setY(int y)
    {
      pi.setY(y);
      cy=pi.getCY();
      top.setY(cy);
      bottom.setY(cy);
      suby=bottom.containsY(cy);
      this.x=x;
      this.y=y;
    }

    public void setX(int x)
    {
      pi.setX(x);
      cx=pi.getCX();
      top.setX(cx);
      bottom.setX(cx);
      subx=bottom.containsX(cx);
    }

    public BigDecimal getCY()
    {
      return cy;
    }

    public BigDecimal getCX()
    {
      return cx;
    }

    public boolean isFast()
    {
      return pi.isFast();
    }

    public double getY(BigDecimal y)
    {
      return pi.getY(y);
    }

    public double getX(BigDecimal x)
    {
      return pi.getX(x);
    }

    public int getPrecision()
    {
      return pi.getPrecision();
    }

    public int getMagnification()
    {
      return pi.getMagnification();
    }
  }

  /////////////////////////////////////////////////////////////////////////

  public static void print(int cnt, AreaInterpolation i)
  {
    MandelInfo info=i.getCurrent().getInfo();
    System.out.println(""+cnt+": "+i.getTopName()+
                        "("+i.getInterpolationCount()+"); "+
                        i.getTopAccess().getInfo().getXM()+" -> "+
                        (i.getBottomAccess()==null?"end":i.getBottomAccess().getInfo().getXM())+" = "+
                        info.getXM()+" ("+info.getDX()+")");
  }

  public static void save(File dir, MandelName n, MandelData md)
  {
    save(dir,new QualifiedMandelName(n),md);
  }

  public static void save(File dir, QualifiedMandelName n, MandelData md)
  {
    File file=new File(dir, n.toString()+".mr");
    try {
      System.out.println("writing "+file);
      md.write(file);
    }
    catch (IOException ex) {
      System.out.println("cannot write "+file+": "+ex);
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private static class Handler extends ZoomHandler {
    private MandelName name=MandelName.ROOT;
    private MandelName last;
    private File dir;

    public Handler(boolean simulate, File dir) throws IOException
    {
      super(simulate,new FolderMandelScanner(dir));
      setNext(name);
      this.dir=dir;
    }

    public void prepareNext()
    {
      last=name;
      name=name.sub('z');
      setNext(name);
    }

    public MandelName getLast()
    {
      return last;
    }
    
    public void save(AreaInterpolation i)
    {
      if (!isSimulate() && !hasMatched()) {
        AreaInterpolation.save(dir,getNext(),i.getCurrent());
      }
    }
  }

  public static double getDoubleArg(String arg, String name)
  {
    double d=0;
    try {
      d=Double.valueOf(arg);
    }
    catch (NumberFormatException nfe) {
      Command.Error("double value as "+name+" expected");
    }
    return d;
  }


  public static void main(String[] args)
  {
    int c=0;

    double fps=25;    // frames per secons
    double speed=0;   // time per zoombase factor 0.1
    double limit=0.5;
    double zpf=0;     // zoombase per frame
    double sd=0.1;      // final speed slow down factor
    double sdt=0.1;   // slow down threshold
    MandelName target=null;
    File dbroot=new File(".");
    File dir=new File("movies");
    boolean vflag=false;
    boolean iflag=false;

    if (args.length==0) {
      System.out.println("interpolation sequence [<options>] <target area>");
      System.out.println("  -i           show info only");
      System.out.println("  -v           verbose/simulate only");
      System.out.println("  -s <speed>   time per zoom factor "+zoombase);
      System.out.println("     -f <fps>  frames per second");
      System.out.println("  -z <zpf>     zoom per frame");
      System.out.println("  -n           no intermediate area calculation");
      System.out.println("  -l <limit>   interpolation limit (default "+limit+")");
      System.out.println("  -r <dbroot>  image database (default "+dbroot+")");
      System.out.println("  -d <dir>     target root folder (default "+dir+")");
      System.exit(0);
    }
    while (args.length>c&&args[c].charAt(0)=='-') {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt;
        switch (opt=arg.charAt(i)) {
          case 'v':
            vflag=true; // simulation
            break;
          case 'i':
            iflag=true;
            break;
          case 'n':
            limit=0; // only linear interpolation among existing areas
            break;

          case 'f':
            if (args.length>c) {
                fps=getDoubleArg(args[c++], "frames per seconds");
            }
            else Command.Error("frames per second missing");
            break;
          case 's':
            if (zpf!=0) {
              Command.Error("only zoom or speed");
            }
            if (args.length>c) {
              speed=getDoubleArg(args[c++], "seconds per zoom");
            }
            else Command.Error("seconds per zoom missing");
            break;
          case 'z':
            if (speed!=0) {
              Command.Error("only zoom or speed");
            }
            if (args.length>c) {
                zpf=getDoubleArg(args[c++], "zoom factor");
            }
            else Command.Error("zoom factor missing");
            break;

          case 'l':
            if (args.length>c) {
                limit=getDoubleArg(args[c++], "limit");
            }
            else Command.Error("interpolation limit missing");
            break;

          case 'r':
            if (args.length>c) {
              dbroot=new File(args[c++]);
              if (!dbroot.isDirectory()) {
                Command.Error("root does not exist");
              }
            }
            else Command.Error("root folder missing");
            break;

          case 'd':
            if (args.length>c) {
              dir=new File(args[c++]);
            }
            else Command.Error("target folder missing");
            break;
          default:
            Command.Error("illegal option '"+opt+"'");
        }
      }
    }

    if (!(args.length>c)) {
      Command.Error("target area missing");
    }
    try {
      target=MandelName.create(args[c++]);
    }
    catch (IllegalArgumentException iae) {
      Command.Error("illegal area name "+args[c-1]);
    }

    double fpz; // frames per zoombase;
    if (speed!=0) { // speed selecte
      fpz=speed*fps;
      zpf=Math.pow(zoombase, 1/fpz);
    }
    else { // zpf selected
      if (zpf==0) zpf=0.98;
      fpz=Math.log(zoombase)/Math.log(zpf);
      speed=fpz/fps;
    }
    try {
      Environment env=new Environment("interpol",args, dbroot);
      dir=new File(dir,target.getName());
      File intermediate=new File(dir,"intermediate");
      intermediate.mkdirs();

      Handler handler=new Handler(vflag,dir);

      AreaInterpolation i=new AreaInterpolation(target,zpf,
                                                env.getImageDataScanner(),
                                                handler,
                                                limit,intermediate);
      i.setFinalSpeedFactor(sd);
      i.setSlowDownThreshold(sdt);
      i.check();
      double time=i.getTotalFrames()/fps;
      System.out.println("target area:                         "+target);
      System.out.println("target folder:                       "+dir);
      System.out.println("interpolation limit:                 "+limit);
      System.out.println("frame rate (frames per second):      "+fps);
      System.out.println("zoom per frame:                      "+zpf+" ("+(1/fpz)+")");
      System.out.println("speed (seconds per zoom factor "+zoombase+"): "+speed);
      System.out.println("speed (frames per zoom factor "+zoombase+"):  "+fpz);
      System.out.println("total zoom:                          "+i.getTotalZoom());
      System.out.println("total frames:                        "+i.getTotalFrames());
      System.out.println("total movie length (seconds):        "+time);

      if (sdt>0) {
        SlowDown s=new SlowDown(zpf,sdt,sd);
//        double z1=Math.log(sdt)/Math.log(zoombase);
//        double v1=Math.log(zpf)/Math.log(zoombase);
//        double ve=sd*v1;
//        double a=v1*v1*(1-sd*sd)/2/z1;
//        double t=2*z1/v1/(1+sd);
//        double orig=Math.log(sdt)/Math.log(zpf);
//        double sda=Math.pow(zoombase, a);
        s.print("slow down mode info");
      }
      if (!iflag) {
        System.out.println("starting interpolation...............");
        int cnt=0;
        while (i.getNext()!=null) {
          print(cnt++, i);
          handler.save(i);
          handler.prepareNext();
        }
      }
    }
    catch (IOException io) {
      System.out.println("illegal intrpolation: "+io);
    }
    catch (IllegalConfigurationException ex) {
      System.out.println("illegal configuration: "+ex);
    }
  }
}
