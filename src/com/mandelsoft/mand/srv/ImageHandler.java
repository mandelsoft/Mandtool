
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
package com.mandelsoft.mand.srv;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.io.FolderLock;
import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.PixelIterator;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.ChangeListener;
import com.mandelsoft.util.StateChangeSupport;

/**
 *
 * @author Uwe Krüger
 */

public class ImageHandler implements Request {
  private AbstractFile mandelfile;
  private MandelFileName name;
  private Environment env;
  private boolean accepted;

  public ImageHandler(Environment env, AbstractFile f)
  {
    this.mandelfile=f;
    this.env=env;
  }

  public boolean isAccepted()
  {
    return accepted;
  }

  //////////////////////////////////////////////////////////////////
  // handling the request
  //////////////////////////////////////////////////////////////////

  private Exception err;
  private FolderLock lock;
  private Server server;
  private long starttime;

  public void send(Server server)
  {
    MandelScanner imagescan;

    this.server=server;

    accepted=false;
    name=MandelFileName.create(mandelfile);
    imagescan=env.getImageDataScanner();

    if (name==null) return;

    try {
      MandelData old=null;
      lock=new FolderLock(mandelfile.getFile());

      if (!lock.lock()) return;
      try {
        if (!mandelfile.tryLock()) return;
      }
      finally {
        lock.releaseLock();
      }

      System.out.println("got lock for "+mandelfile);
      MandelData req;
      try {
        req=new MandelData(mandelfile, false);
      }
      catch (IOException io) {
        mandelfile.releaseLock();
        if (mandelfile.getFile().length()==0) mandelfile.getFile().delete();
        return;
      }
      MandelInfo reqd=req.getInfo();

      Set<MandelHandle> set=imagescan.getMandelHandles(name.getQualifiedName());
      if (!set.isEmpty()) {
        for (MandelHandle h:set) {
          if (h.getHeader().hasRaster()) {
            try {
              MandelData md=h.getData();
              MandelInfo mi=md.getInfo();
              System.out.println("requested "+reqd.getLimitIt()+
                      " found "+h.getFile()+": "+mi.getLimitIt());
              if (reqd.getDX().equals(mi.getDX())&&
                      reqd.getDY().equals(mi.getDY())&&
                      reqd.getXM().equals(mi.getXM())&&
                      reqd.getYM().equals(mi.getYM())&&
                      reqd.getRX()==mi.getRX()&&
                      reqd.getRY()==mi.getRY()) {
                old=md;
                break;
              }
            }
            catch (IOException io) {
            }
          }
        }
        if (old==null||old.getInfo().getLimitIt()>=reqd.getLimitIt()) {
          System.out.println(mandelfile+" skipped");

          lock.lock();
          try {
            mandelfile.releaseLock();
            cleanupInfo(env, mandelfile);
          }
          finally {
            lock.releaseLock();
          }

          return;
        }
      }
      setup(req,old);
      start();
    }
    catch (IOException io) {
      System.err.println("*** "+mandelfile+": "+io);
    }
  }

  private void start()
  { boolean recalc=md.getRaster()!=null;
    System.out.println((recalc?"re":"")+
            "calculating "+(file==null?"":file)+"...");
    if (md.getRaster()!=null&&md.getInfo().getMaxIt()>limit) {
      System.out.println("nothing to be done");
      return;
    }

    starttime=System.currentTimeMillis();
    ImageData data=new ImageData(name,recalc,pi.getPrecision(),
                                             pi.getMagnification(),starttime);
    server.addImage(data);
    if (!recalc) mi.setTime(0);
    raster=md.createRaster().getRaster();
    AreaHandler area=new AreaHandler(server, recalc, true, md,
                                     0, 0, mi.getRX(), mi.getRY());
    area.setPixelIterator(pi);
    area.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e)
      {
        AreaHandler area=(AreaHandler)e.getSource();
        mi.setTime(mi.getTime()+(int)(area.getMTime()/1000));
        done();
      }
    });
    accepted=true;
    area.send(server);
    // mi.setTime(mi.getTime()+(int)((end-start)/1000));;
  }

  private void done()
  {
    long end=System.currentTimeMillis();
    int efftime=(int)((end-starttime)/1000);
    mi.setEffectiveTime(efftime);
    mi.setRasterCreationTime(end);
    server.removeImage(name);
    try {
      write(false);

      lock.lock();
      try {
        mandelfile.releaseLock();
        System.out.println("release lock for "+mandelfile);
        if (!mandelfile.getName().equals(getFile().getName())) {
          cleanupInfo(env, mandelfile);
        }
      }
      finally {
        lock.releaseLock();
      }
    }
    catch (IOException io) {
      err=io;
      System.err.println("*** "+mandelfile+": "+io);
    }
    listeners.fireChangeEvent(this);
  }

  public AbstractFile getMandelFile()
  {
    return mandelfile;
  }

  public Exception getError()
  {
    return err;
  }

  ///////////////////////////////////////////////////////////////////
  // calculation
  ///////////////////////////////////////////////////////////////////

  private File file;
  private MandelData md;
  private MandelInfo mi;
  private PixelIterator pi;
  private int limit;
  private int[][] raster;

  public void setPixelIterator(PixelIterator pi)
  {
    this.pi=pi;
  }

  private void setup(MandelData md)
  {
    this.md=md;
    this.mi=md.getInfo();
    this.limit=mi.getLimitIt();
    this.file=env.mapToRasterFile(md.getFile());
  }

  private void setup(MandelData md, MandelData old) throws IOException
  {
    setup(md);
    if (old!=null && old.getFile().isFile()) {
      md.setRaster(old.getRaster());
      md.setMapper(ResizeMode.RESIZE_LOCK_COLORS, old.getMapper());
      mi.setSite(old.getInfo().getSite());
      mi.setCreator(old.getInfo().getCreator());
      mi.setLocation(old.getInfo().getLocation());
      mi.setName(old.getInfo().getName());
      this.file=old.getFile().getFile();
    }
    else {
      this.file=env.mapToRasterFile(md.getFile());
    }
    pi=MandIter.createPixelIterator(mi);
    System.out.println("precision set to "+pi.getPrecision()+
                                               "("+pi.getMagnification()+")");
    mi.setMinIt(limit);
  }

  public File getFile()
  {
    return file;
  }

  private void write(boolean verbose) throws IOException
  {
    if (file==null) throw new IOException("no file specified");

    md.write(file,verbose);
    System.out.println(new Date()+": "+file+" done: "+MandUtils.time(md.getInfo().getTime()));
  }


  ////////////////////////////////////////////////////////////////////////
  // events
  ////////////////////////////////////////////////////////////////////////

  private StateChangeSupport listeners=new StateChangeSupport();

  public void addChangeListener(ChangeListener l)
  {
    listeners.addChangeListener(l);
  }

  public void removeChangeListener(ChangeListener l)
  {
    listeners.removeChangeListener(l);
  }

  static private void cleanupInfo(Environment env, AbstractFile f)
  {
    if (!env.backupInfoFile(f)) {
      if (env.isCleanupInfo() && f.isFile()) {
        System.out.println("deleting "+f);
        f.getFile().delete();
      }
    }
  }
}
