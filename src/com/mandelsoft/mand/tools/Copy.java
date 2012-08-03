
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

import com.mandelsoft.mand.IllegalConfigurationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.scan.ElementHandle;
import com.mandelsoft.mand.scan.MandelFolderCache;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerUtils;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.util.Utils;


/**
 *
 * @author Uwe Krüger
 */

public class Copy<E extends ExecutionHandler> extends Command {
  static protected byte[] buffer=new byte[1024];

  protected int count=0;
  protected E exec;


  /////////////////////////////////////////////////////////////////////
  // Execution Handler
  /////////////////////////////////////////////////////////////////////
  public class VerboseHandler implements ExecutionHandler {

    public void copy(ElementHandle src, AbstractFile dst)
    {
      System.out.println("copying "+src.getFile()+" to "+dst.getFile());
    }

    public void backupInfoFile(AbstractFile mf)
    {
      System.out.println("backup request "+mf);
    }

    public void backupRasterFile(AbstractFile mf)
    {
      System.out.println("backup raster "+mf);
    }

    public void backupAreaColormapFile(AbstractFile mf)
    {
      System.out.println("backup area colormap "+mf);
    }

    public void finish()
    {
    }
  }

  /////////////////////////////////////////////////////////////////////
  public class DefaultHandler extends Copy.VerboseHandler {
    protected Set<File> destinations=new HashSet<File>();

    public void addDestination(File d)
    {
      if (!destinations.contains(d)) {
        destinations.add(d);
      }
    }

    @Override
    public void copy(ElementHandle src, AbstractFile dst)
    {
      int n;
      File d=dst.getFile();
      AbstractFile s=src.getFile();
      if (d==null) {
        System.out.println("copy to URL not possible: "+dst);
        return;
      }
      super.copy(src,dst);
      addDestination(d.getParentFile());
      try {
        BufferedInputStream is=new BufferedInputStream(s.getInputStream());
        try {
          BufferedOutputStream os=new BufferedOutputStream(new FileOutputStream(
                  d));
          try {
            while ((n=is.read(buffer))>0) {
              os.write(buffer, 0, n);
            }
          }
          finally {
            os.close();
          }
          d.setLastModified(s.getLastModified());
        }
        finally {
          is.close();
        }
      }
      catch (IOException io) {
        System.out.println("cannot copy "+s+": "+io);
        io.printStackTrace(System.err);
      }
    }

    @Override
    public void backupInfoFile(AbstractFile mf)
    {
      super.backupInfoFile(mf);
      Copy.this.dst.backupInfoFile(mf);
    }

    @Override
    public void backupRasterFile(AbstractFile mf)
    {
      super.backupRasterFile(mf);
      Copy.this.dst.backupRasterFile(mf);
    }

    @Override
    public void backupAreaColormapFile(AbstractFile mf)
    {
      super.backupAreaColormapFile(mf);
      Copy.this.dst.backupAreaColormapFile(mf);
    }

    public void updateCaches()
    {
      for (File f :destinations) {
        System.out.println("checking cache "+f);
        if (MandelFolderCache.isCached(f)) {
          try {
            System.out.println("updating cache in "+f);
            MandelFolder mf=new MandelFolder(f);
            mf.recreate();
          }
          catch (IOException io) {
            System.out.println("  cannot recreate cache for "+f);
          }
        }
      }
    }

    @Override
    public void finish()
    {
      updateCaches();
    }
  }

  //////////////////////////////////////////////////////////////////////
  // exits for file type handler
  //////////////////////////////////////////////////////////////////////

  protected boolean handleFile(FileType ft, ElementHandle h)
  {
    return ft.handle(h);
  }

  protected String map(AbstractFile s)
  {
    MandelFileName n=MandelFileName.create(s);
    MandelName sn=n.getName();
    String eff=sn.getEffective();
    if (!eff.startsWith(sp)) return null;
    eff=dp+eff.substring(sp.length());
    MandelName dn=MandelName.create(eff);
    String fn=n.get(dn,true).getFileName();
    return fn;
  }

  /////////////////////////////////////////////////////////////////////
  // File Type Handler
  /////////////////////////////////////////////////////////////////////
  protected abstract class FileType {
    private String name;
    private Map<AbstractFile,AbstractFile> pathmapping=new HashMap<AbstractFile,AbstractFile>();
    private AbstractFile def;
    private MandelScanner.Filter filter;
    protected MandelScanner srcscan;
    protected MandelScanner dstscan;
    

    FileType(String name, MandelScanner.Filter filter,
            MandelScanner srcscan, MandelScanner dstscan)
    {
      this.name=name;
      this.filter=filter;
      this.srcscan=srcscan;
      this.dstscan=dstscan;
    }

    public boolean match(MandelHeader h)
    {
      return filter.filter(h);
    }

    public boolean match(ElementHandle<?> h)
    {
      return match(h.getHeader());
    }

    protected void handlePath(String prop)
    {
      String d=dst.getProperty(prop);
      if (!Utils.isEmpty(d)) {
        AbstractFile fd=dst.createMandelFile(d);
        if (src!=null) {
          String s=src.getProperty(prop);
          if (!Utils.isEmpty(s)) {
            AbstractFile fs=src.createMandelFile(s);
            pathmapping.put(fs, fd);
          }
        }
        def=fd;
      }
    }

    public String getName()
    {
      return name;
    }

    public void execute()
    { boolean v;

      System.out.println("copying "+getName()+"...");
      for (ElementHandle<?> h:srcscan.getAllHandles()) {
        if (match(h)) {
          //System.out.println("  found "+h.getName()+" DOIT "+h.getHeader());
          handleFile(this,h);
        }
      }
    }

    protected boolean handle(ElementHandle<?> h)
    {
      AbstractFile mf=h.getFile();
      AbstractFile p=mf.getParent();
      AbstractFile d=pathmapping.get(p);

      if (d==null) d=def;
      if (d==null) {
        System.out.println("no target for "+mf);
        return false;
      }
      else {
        String fn=map(mf);
        if (fn==null) return false;
        
        exec.copy(h, d.getSub(fn));
        cleanup(h);
        count++;
        return true;
      }
    }

    public void cleanup(ElementHandle<?> h)
    {
    }

    protected boolean has(ElementHandle<?> h)
    {
      MandelHandle mh=((MandelHandle)h);
      Set<MandelHandle> set=dstscan.getMandelHandles(mh.getName());
      for (MandelHandle dh:set) {
        if (match(dh)) return true;
      }
      return false;
    }

    protected boolean use(ElementHandle<?> h)
    {
      return use((MandelHandle)h,dst.getImageDataScanner());
//      MandelHandle mh=((MandelHandle)h);
//      return dst.getImageDataScanner().getMandelHandles(mh.getName()).isEmpty();
    }

    protected boolean use(MandelHandle mh, MandelScanner ds)
    {
      Set<MandelHandle> set=ds.getMandelHandles(mh.getName());
      if (set.isEmpty()) return true;
      long lm=mh.getFile().getLastModified();
      for (MandelHandle dh:set) {
        if (dh.getFile().getLastModified()>=lm) return false;
      }
      return true;
    }
  }

  /////////////////////////////////////////////////////////////////////
  protected class InfoType extends FileType {
    InfoType()
    {
      super("requests", MandelScanner.INFO,
            src==null?null:src.getInfoScanner(), dst.getInfoScanner());
      handlePath(Settings.INFO_SAVE_PATH);
    }
  }

  protected class AreaColmapType extends FileType {
    AreaColmapType()
    {
      super("areacolmaps", MandelScanner.AREACOLMAP,
            src==null?null:src.getAreaColormapScanner(), dst.getAreaColormapScanner());
      handlePath(Settings.AREACOLMAP_PATH);
      handlePath(Settings.AREACOLMAP_SAVE_PATH);
    }

    @Override
    protected boolean use(ElementHandle h)
    {
      return use((MandelHandle)h,dst.getAreaColormapScanner());
    }
  }
  
  /////////////////////////////////////////////////////////////////////
  protected class RasterType extends FileType {
    RasterType()
    {
      super("rasters", MandelScanner.RASTER,
            src==null?null:src.getRasterScanner(), dst.getRasterScanner());
      handlePath(Settings.VARIANT_SEEN_PATH);
      handlePath(Settings.VARIANT_SAVE_PATH);
      handlePath(Settings.RASTER_SEEN_PATH);
      handlePath(Settings.RASTER_SAVE_PATH);
    }

    @Override
    public void cleanup(ElementHandle h)
    {
      QualifiedMandelName mn=((MandelHandle)h).getName();

      Set<MandelHandle> set=dst.getInfoScanner().getMandelHandles(mn);
      for (MandelHandle mh:set) {
        System.out.println("  backup request "+mh.getFile());
        exec.backupInfoFile(mh.getFile());
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  protected class RasterImageType extends FileType {
    RasterImageType()
    {
      super("raster images", MandelScanner.RASTERIMAGE,
            src==null?null:src.getRasterImageScanner(), dst.getRasterImageScanner());
      handlePath(Settings.VARIANT_SEEN_PATH);
      handlePath(Settings.VARIANT_SAVE_PATH);
      handlePath(Settings.RASTERIMAGE_SAVE_PATH);
    }

    @Override
    protected boolean use(ElementHandle h)
    {
      return use((MandelHandle)h,dst.getRasterImageScanner());
//      MandelHandle mh=((MandelHandle)h);
//      return dst.getRasterImageScanner().
//                 getMandelHandles(mh.getName()).isEmpty();
    }
    
    @Override
    public void cleanup(ElementHandle h)
    {
      MandelHandle mh=((MandelHandle)h);
      QualifiedMandelName mn=mh.getName();

      for (MandelHandle m:dst.getInfoScanner().getMandelHandles(mn)) {
        System.out.println("  backup request "+m.getFile());
        dst.backupInfoFile(m.getFile());
      }

      for (MandelHandle m:dst.getRasterScanner().getMandelHandles(mn)) {
        System.out.println("  backup raster "+m.getFile());
        exec.backupRasterFile(m.getFile());
      }

      for (MandelHandle m:dst.getAreaColormapScanner().getMandelHandles(mn)) {
        System.out.println("  backup area colormap "+m.getFile());
        exec.backupAreaColormapFile(m.getFile());
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  // class
  /////////////////////////////////////////////////////////////////////
  protected Environment src;
  protected Environment dst;
  protected List<FileType> types=new ArrayList<FileType>();
  
  protected String sp;
  protected String dp;

  public Copy(Environment src, Environment dst)
  {
    this.src=src;
    this.dst=dst;
    
    this.types.add(new InfoType());
    this.types.add(new AreaColmapType());
    this.types.add(new RasterType());
    this.types.add(new RasterImageType());
  }

  protected void additionalSteps()
  {
  }

  protected void execute()
  {
    for (FileType t:types) {
      t.execute();
    }
    additionalSteps();
    exec.finish();
  }

  ///////////////////////////////////////////////////////////////////////////

  public void copy(MandelName s, MandelName d)
  {
    if (s.isLocalName() || d.isRemoteName()) {
      throw new IllegalArgumentException("remote name not supported");
    }
    sp=s.getEffective();
    dp=d.getEffective();

    execute();
  }

  public void setExecutionHandler(E h)
  {
    exec=h;
  }

  ///////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  { try {
      int c=0;
      boolean vflag=false;
      File src=new File("F://Mandel2");
      File dst=new File("F://Mandel");
      String sn=null;
      String dn=null;
      while (args.length>c&&args[c].charAt(0)=='-') {
        for (int i=1; i<args[c].length();
             i++) {
          char opt;
          switch (opt=args[c].charAt(i)) {
            case 'v':
              vflag=true;
              break;
            default:
              Error("illegal option '"+opt+"'");
          }
        }
        c++;
      }
      if (args.length>c) src=new File(args[c++]);
      if (args.length>c) dst=new File(args[c++]);
      if (args.length>c) sn=args[c++];
      else Error("copy: [-v] <srcroot> <dstroot> <srcname>");
      System.out.println("copying from "+src+" to "+dst);
      if (!src.isDirectory()) Error(src+" is no directory");
      if (!dst.isDirectory()) Error(dst+" is no directory");
      Environment env_src=new Environment("mandtool", null, src);
      Environment env_dst=new Environment("mandtool", null, dst);
      MandelName s=MandelName.create(sn);
      MandelData sd=MandelScannerUtils.getMandelInfo(env_src.getAllScanner(), s);
      if (sd==null) Error("source area not found");
      MandelName n=MandUtils.lookupRoot(env_dst.getAllScanner(), sd.getInfo());
      MandelData dd=MandelScannerUtils.getMandelInfo(env_dst.getAllScanner(), n);
      if (dd.getInfo().isSameArea(sd.getInfo())) Error("already there");
      n=MandUtils.getNextSubName(n, env_dst.getAllScanner());
      System.out.println("target area: "+n);
      Copy<ExecutionHandler> a=new Copy<ExecutionHandler>(env_src, env_dst);
      if (vflag) {
        a.setExecutionHandler(a.new VerboseHandler());
      }
      else {
        a.setExecutionHandler(a.new DefaultHandler());
      }
      a.copy(s, n);
      System.out.println(""+a.count+" copied");
    }
    catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: "+ex);
    }
  }
}
