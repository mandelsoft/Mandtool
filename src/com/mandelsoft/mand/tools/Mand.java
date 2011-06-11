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
package com.mandelsoft.mand.tools;


import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelException;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.PixelIterator;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.io.FolderLock;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 *
 * @author Uwe Krueger
 */
public class Mand extends Command {

  public static class ShutdownException extends RuntimeException {

  }

  static public final double BOUND=10;
  private File file;
  private File save;
  private QualifiedMandelName name;
  private MandelData md;
  private MandelInfo mi;
  private PixelIterator pi;
  private int limit;
  private int[][] raster;
  private Filter filter;

  public Mand(MandelData md, QualifiedMandelName n)
  {
    this.md=md;
    this.name=n;
    this.mi=md.getInfo();
    this.limit=mi.getLimitIt();
  }

  public Mand(MandelData md,  QualifiedMandelName n, Environment env)
         throws IOException
  {
    this(md,n);
    this.file=env.mapToRasterFile(md.getFile());
    this.save=env.mapToIncompleteFile(md.getFile());
  }

  public Mand(File f, Environment env) throws IOException
  {
    this(new MandelData(f),QualifiedMandelName.create(f), env);
  }

  public Mand(MandelData md, MandelData old, QualifiedMandelName n,
              Environment env) throws IOException
  {
    this(md, n, env);
    if (old!=null && old.getFile().isFile()) {
      md.setRaster(old.getRaster());
      md.setMapper(ResizeMode.RESIZE_LOCK_COLORS, old.getMapper());
      md.getInfo().setSite(old.getInfo().getSite());
      md.getInfo().setCreator(old.getInfo().getCreator());
      md.getInfo().setLocation(old.getInfo().getLocation());
      md.getInfo().setName(old.getInfo().getName());
      md.getInfo().setTime(old.getInfo().getTime());
      if (!old.isIncomplete()) { // keep old file name
        this.file=old.getFile().getFile();
      }
    }
  }

  void setFilter(Filter filter)
  {
    this.filter=filter;
  }

  public File getFile()
  {
    return file;
  }

  public boolean isAborted()
  {
    return aborted;
  }

  public void check() throws IOException
  {
    int rx=md.getRaster().getRX();
    int ry=md.getRaster().getRY();

    MandelData tmp=new MandelData(file);
    if (rx!=tmp.getRaster().getRX()) {
      throw new MandelException("rx mismatch: "+
              tmp.getRaster().getRX()+"!="+
              rx);
    }
    if (ry!=tmp.getRaster().getRY()) {
      throw new MandelException("ry mismatch: "+
              tmp.getRaster().getRY()+"!="+
              ry);
    }

    int[][] tmpraster=tmp.getRaster().getRaster();
    for (int y=0; y<ry; y++) {
      for (int x=0; x<rx; x++) {
        if (raster[y][x]!=tmpraster[y][x])
          throw new MandelException("content mismatch");
      }
    }
  }

  public boolean calculate()
  {
    if (filter!=null) {
      if (filter.prefix!=null
              &&!filter.prefix.isAbove(name.getMandelName()))
        return false;
      if (filter.variants&&name.getQualifier()==null) return false;
      setupContext();
      if (filter.fast&&!pi.isFast()) return false;
    }
    else {
      setupContext();
    }

    System.out.println((md.getRaster()==null?"":"re")+
            "calculating "+(file==null?"":file)+"... ("+name+")");
    if (md.getRaster()!=null&&md.getInfo().getMaxIt()>limit) {
      System.out.println("nothing to be done");
      return true;
    }

    raster=md.createRaster().getRaster();
    long start=System.currentTimeMillis();
    try {
      calc2();
    }
    catch (ShutdownException ex) {
      aborted=true;
    }
    finally {
      long end=System.currentTimeMillis();
      mi.setTime(mi.getTime()+(int)((end-start)/1000));
      mi.setRasterCreationTime(end);
      saveContext();
    }
    return true;
  } 
  
  ////////////////////////////////////////////////////////////////////////
  // calculation
  ////////////////////////////////////////////////////////////////////////
  
  
  
  private int rx;
  private int ry;
  
  private int min;
  private int max;
  private long cnt;

  private int mccnt;
  private int mcnt;

  private void setupContext()
  {
    pi=MandIter.createPixelIterator(mi);

    rx=mi.getRX();
    ry=mi.getRY();
    min=mi.getMinIt();
    if (min==0) min=limit; // not yet calculated
    max=mi.getMaxIt();
    cnt=0;
    
    mccnt=0;
    mcnt=0;
  }

  private void saveContext()
  {
    mi.setMinIt(min);
    mi.setMaxIt(max);
    mi.setNumIt(cnt);

    mi.setMCnt(mcnt);
    mi.setMCCnt(mccnt);
  }
  
  private void calc1()
  {
    for (int y=0; y<ry; y++) {
      pi.setY(y);
      for (int x=0; x<rx; x++) {
        pi.setX(x);
        handle(x,y);
      }
    }
  }

  private boolean aborted;
  private long lastcheck=0;
  static private final long TIMEOUT = 1000 * 60 * 10; // 10 min
  static private final File shutdown = new File("shutdown");
  
  int handle(int x, int y)
  { int it=raster[y][x];

    if (it==0) {
      int i=pi.iter();
      if (i>limit) {
          raster[y][x]=it=0;
          mccnt++;
          mcnt++;
          i--;
      }
      else {
        raster[y][x]=it=i;
      }
      if (i<min) min=i;
      if (i>max) max=i;
      cnt+=i;
    }
    else cnt+=it;
    long cur=System.currentTimeMillis();
    if (cur>lastcheck+TIMEOUT) {
      lastcheck=cur;
      if (shutdown.exists()) throw new ShutdownException();
    }
    return it;
  }

  private void calc2()
  { int u;
    calcHLine(0,0,rx);
    calcHLine(0,ry-1,rx);
    calcVLine(0,1,ry-2);
    u=calcVLine(rx-1,1,ry-2);

    calcBox(u, 0,0,rx,ry);
  }

  private int calcHLine(int sx, int sy, int n)
  {
    pi.setX(sx);
    pi.setY(sy);
    int u=handle(sx,sy);

    for (int x=sx+1; x<sx+n; x++) {
      pi.setX(x);
      int it=handle(x,sy);
      if (it!=u) u=-1;
    }
    return u;
  }

  private int calcVLine(int sx, int sy, int n)
  {
    pi.setX(sx);
    pi.setY(sy);
    int u=handle(sx,sy);

    for (int y=sy+1; y<sy+n; y++) {
      pi.setY(y);
      int it=handle(sx,y);
      if (it!=u) u=-1;
    }
    return u;
  }

  private void calcBox(int u, int sx, int sy, int nx, int ny)
  {
    //System.out.println("calcBox "+sx+","+sy+"("+nx+"x"+ny+")");
    if (nx<=2 || ny<=2)  return;

    if (u>=0) {
      u=checkHLine(u, sx, sy, nx);
      u=checkHLine(u, sx, sy+ny-1, nx);
      u=checkVLine(u, sx, sy+1, ny-2);
      u=checkVLine(u, sx+nx-1, sy+1, ny-2);
      if (u>=0) {
        fillBox(sx+1,sy+1,nx-2,ny-2,u);
        return;
      }
    }
    if (nx>ny) {
      // divide horizontally
      int s=(nx-1)/2;
      if (s!=0) {
        //System.out.println("s="+s);
        u=calcVLine(sx+s,sy+1,ny-2);
        calcBox(u,sx,sy,s+1,ny);
        calcBox(u,sx+s,sy,nx-s,ny);
      }
    }
    else {
      // divide vertically
      int s=(ny-1)/2;
      if (s!=0) {
        u=calcHLine(sx+1,sy+s,nx-2);
        calcBox(u,sx,sy,nx,s+1);
        calcBox(u,sx,sy+s,nx,ny-s);
      }
    }
  }

  private int checkHLine(int u, int sx, int sy, int n)
  {
    if (u>=0) for (int x=sx; x<sx+n; x++) {
      if (raster[sy][x]!=u) return -1;
    }
    return u;
  }

  private int checkVLine(int u, int sx, int sy, int n)
  {
    if (u>=0) for (int y=sy; y<sy+n; y++) {
      if (raster[y][sx]!=u) return -1;
    }
    return u;
  }

  private void fillBox(int sx, int sy, int nx, int ny, int u)
  {
    //System.out.println("fill "+sx+","+sy+"("+nx+"x"+ny+") with "+u);
    for (int y=sy; y<sy+ny; y++) {
      for (int x=sx; x<sx+nx; x++) {
        raster[y][x]=u;
      }
    }
    if (u==0) mcnt+=nx*ny;
  }

  ////////////////////////////////////////////////////////////////////////
  // iteration for pixel
  ////////////////////////////////////////////////////////////////////////

  private int iter(double x, double y, double px, double py)
  {
    double x2=x*x;
    double y2=y*y;
    int it=0;

    while (x2+y2<BOUND&&++it<=limit) {
      double xn=x2-y2+px;
      double yn=2*x*y+py;
      x=xn;
      x2=x*x;
      y=yn;
      y2=y*y;
    }
    return it;
  }

  public void write() throws IOException
  {
    write(true);
  }

  public void write(boolean verbose) throws IOException
  {
    if (file==null) throw new IOException("no file specified");
    if (aborted) {
      md.setIncomplete(aborted);
      write(save,verbose);
    }
    else {
      write(file, verbose);
      save.delete();
    }
  }

  public void write(File f) throws IOException
  {
    write(f,true);
  }

  public void write(File f, boolean verbose) throws IOException
  {
    md.write(f,verbose);
    System.out.println(new Date()+": "+f+" done: "+MandUtils.time(md.getInfo().getTime()));
  }

  private static class Filter {
    MandelName prefix;
    boolean fast;
    boolean variants;
  }

  static public void main(String[] args)
  {
    int c=0;
    boolean sflag=false; // server mode
    boolean cflag=false;
    boolean dflag=false; // delete obsolete
    Filter filter=new Filter();
    
    Set<File> files=new HashSet<File>();

    while (args.length>c&&args[c].charAt(0)=='-') {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt;
        switch (opt=arg.charAt(i)) {
          case 's':
            sflag=true;
            break;
          case 'd':
            dflag=true;
            break;
          case 'c':
            cflag=true;
            break;
          case 'f':
            filter.fast=true;
            break;
           case 'v':
            filter.variants=true;
            break;
          case 'p':
            if (args.length>c) {
              filter.prefix=MandelName.create(args[c++]);
              if (filter.prefix==null) Error("illegal mandel name '"+args[c-1]+"'");
            }
            else Error("name prefix missing");
            break;
          default:
            Error("illegal option '"+opt+"'");
        }
      }
      c++;
    }

    while (args.length>c) {
      files.add(new File(args[c++]));
    }

    if (sflag) {
      service(dflag,filter);
    }
    else {
      try {
        Environment env=new Environment(null);
        for (File f:files) {
          try {
            Mand m=new Mand(f, env);
            m.calculate();
            m.write();
            if (cflag) {
              try {
                m.check();
              }
              catch (Exception e) {
                e.printStackTrace(System.err);
                Error("check failed: "+e);
              }
            }
          }
          catch (IOException ex) {
            Error("cannot handle "+f+": "+ex);
          }
        }
      }
      catch (IllegalConfigurationException ex) {
        Error("illegal config: "+ex);
      }
    }
  }
  
  static void cleanupInfo(Environment env, AbstractFile f)
  {
    if (!env.backupInfoFile(f)) {
      if (env.isCleanupInfo() && f.isFile()) {
        System.out.println("deleting "+f);
        try {
          MandelFolder.Util.delete(f.getFile());
        }
        catch (IOException ex) {
          System.out.println("deletion of "+f+" failed: "+ex);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  private static class Service {
    Environment env;
    Set<AbstractFile> ignored;
    MandelScanner imagescan;
    MandelScanner incompletescan;
    MandelScanner infoscan;
    MandelScanner prioscan;
    boolean dflag;
    Filter filter;

    public Service(boolean dflag, Filter filter) throws IllegalConfigurationException
    {
      this.dflag=dflag;
      this.filter=filter;
      env=new Environment(null);
      ignored=new HashSet<AbstractFile>();
      imagescan=env.getImageDataScanner();
      incompletescan=env.getIncompleteScanner();
      infoscan=env.getInfoScanner();
      prioscan=env.getPrioInfoScanner();
    }

    public Environment getEnvironment()
    { return env;
    }

    public void service()
    { 
      Iterator<MandelHandle> fallback=infoscan.getMandelHandles().iterator();

      while (true) {
        int found=0;
        for (MandelHandle h:prioscan.getMandelHandles()) {
          found+=handle(h,false);
        }
        if (found==0) {
          if (!fallback.hasNext()) {
            System.out.println("rescan standard scanner");
            infoscan.rescan(false);
            fallback=infoscan.getMandelHandles().iterator();
          }
          while (found==0 && fallback.hasNext()) {
            found+=handle(fallback.next(),true);
          }
        }

        if (found==0) {
          //System.out.println("nothing found");
          try {
            Thread.sleep(1000*20);
          }
          catch (InterruptedException ie) {
            System.exit(1);
          }
        }
        else System.out.println(""+found+" files processed");

        //System.out.println("rescan prio scanner");
        prioscan.rescan(false);
      }
    }

    MandelData checkOld(MandelScanner scan,
                        QualifiedMandelName name, MandelInfo reqd,
                        String msg)
    {
      MandelData old=null;

      Set<MandelHandle> set=scan.getMandelHandles(name);
      if (!set.isEmpty()) {
        for (MandelHandle h:set) {
          if (h.getHeader().hasRaster()) {
            try {
              MandelData md=h.getData();
              MandelInfo mi=md.getInfo();
              if (reqd.getDX().equals(mi.getDX())
                &&reqd.getDY().equals(mi.getDY())
                &&reqd.getXM().equals(mi.getXM())
                &&reqd.getYM().equals(mi.getYM())
                &&reqd.getRX()==mi.getRX()
                &&reqd.getRY()==mi.getRY()) {
                System.out.println(msg+" "+h.getFile()+": "+mi.getLimitIt());
                old=md;
                break;
              }
            }
            catch (IOException io) {
            }
          }
        }
      }
      return old;
    }

    int handle(MandelHandle mh, boolean fallback)
    { int found=0;
      AbstractFile f=mh.getFile();
      QualifiedMandelName name=mh.getName();
      if (ignored.contains(f)) return found;
      if (name.getLabel()!=null) return found;
      //System.out.println("found "+f);
      if (!f.getFile().exists()) {
        System.out.println("already processed or deleted: "+f);
        return found;
      }
      String n=f.getName();
     
      //System.out.println("checking for image data "+f);

      try {
        MandelData old=null;
        FolderLock lock=MandelFolder.getMandelFolder(f.getFile().getParentFile());
        if (!lock.lock()) return found;

        try {
          if (!f.tryLock()) return found;
        }
        finally {
          lock.releaseLock();
        }
        System.out.println("got lock for "+f);
        MandelData req;
        try {
          req=new MandelData(f, false);
        }
        catch (IOException io) {
          f.releaseLock();
          if (f.getFile().length()==0) f.getFile().delete();
          return found;
        }
        MandelInfo reqd=req.getInfo();

        old=checkOld(imagescan,name,reqd,
                     "requested "+reqd.getLimitIt()+" found");
        // TODO: what happen if name is reused for other coordinates???
        if (old!=null&&old.getInfo().getLimitIt()>=reqd.getLimitIt()) {
          System.out.println(f+" skipped");
          lock.lock();
          try {
            f.releaseLock();
            if (dflag) {
              cleanupInfo(env, f);
            }
            else {
              ignored.add(f);
            }
          }
          finally {
            lock.releaseLock();
          }
          return found;
        }
        
        //////////////
        old=checkOld(incompletescan,name,reqd,"resuming");

        ///////////////
        Mand m=new Mand(req, old, name, env);
        m.setFilter(filter);
        if (!m.calculate()) {
          ignored.add(f);
          f.releaseLock();
          System.out.println(f+" skipped for filter mode");
          System.out.println("release lock for "+f);
          return found;
        }
        found++;
        m.write(false);
        lock.lock();
        try {
          f.releaseLock();
          System.out.println("release lock for "+f);
          if (!m.isAborted()) {
            if (!f.getName().equals(m.getFile().getName())) {
              cleanupInfo(env, f);
            }
          }
          else throw new ShutdownException();
        }
        finally {
          lock.releaseLock();
        }
      }
      catch (IOException io) {
        System.err.println("*** "+f+": "+io);
      }
      return found;
    }
  }

  static private void service(boolean dflag, Filter filter)
  {
    try {
      if (filter!=null) {
        if (filter.prefix!=null)
          System.out.println("prefix filter is "+filter.prefix);
        if (filter.variants) System.out.println("variants filter is on");
        if (filter.fast) System.out.println("fast filter is on");
      }
      Service srv=new Service(dflag, filter);
      srv.service();
    }
    catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: "+ex);
    }
    catch (ShutdownException sd) {
      Command.Warning("calculation service aborted!");
    }
  }
}
