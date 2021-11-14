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

import com.mandelsoft.io.AbstractFile;
import static com.mandelsoft.mand.Settings.ENV_MANDEL_HOME;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.FileMandelList;
import com.mandelsoft.mand.util.UniqueArrayMandelList;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import com.mandelsoft.mand.scan.FilteredMandelScanner;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.PathMandelScanner;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.image.MandelAreaImage;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScannerCache;
import com.mandelsoft.mand.scan.MandelScannerProxy;
import com.mandelsoft.mand.scan.MandelScannerUtils;
import com.mandelsoft.mand.util.ColorList;
import com.mandelsoft.mand.util.ColormapList;
import com.mandelsoft.mand.util.DeltaMandelList;
import com.mandelsoft.mand.util.FileColorList;
import com.mandelsoft.mand.util.FileMandelListFolderTree;
import com.mandelsoft.mand.util.FileTagList;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.mand.util.MemoryMandelListFolderTree;
import com.mandelsoft.mand.util.ScannerColormapList;
import com.mandelsoft.mand.util.TagList;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class Environment0 implements MandelConstants  {
  static public boolean debug=false;

  static File basedir() 
  {
    String base=System.getenv(ENV_MANDEL_HOME);
    if (base!=null) {
      return new File(base);
    }
    return new File(".");
  }
  
  public static class FileInfo {
    private Colormap cm;

    public Colormap getColormap()
    {
      return cm;
    }

    public void setColormap(Colormap cm)
    {
      this.cm=cm;
    }
  }

  private boolean readonly;

  private MandelImage.Factory fac;
  private Settings settings;
  private String copyright;
  private PathMandelScanner all;

  private MandelScanner imagedata;
  private MandelScanner raster;
  private MandelScanner info;
  private MandelScanner prioinfo;
  private MandelScanner meta;
  private MandelScanner rasterimage;

  private MandelScanner autoimagedata;
  private MandelScanner autometa;

  private MandelScanner colormaps;

  private MandelScanner newraster;

  private QualifiedMandelName initialName;
  private File                initialFile;

  private ColorList  colors;
  private TagList    tags;
  private Colormap   defcolormap;
  private MandelListFolderTree memory;
  private MandelList favorites;
  private MandelListFolderTree tfavorites;
  private MandelList todos;
  private MandelListFolderTree ttodos;
  private MandelList newrasters;
  private MandelList areas;
  private MandelList seenrasters;
  private MandelList unseenrasters;
  private MandelList variants;
  private MandelList leafs;
  private MandelList pending;
  private ColormapList colormaplist;
 
 

  private List<MandelListFolderTree> userlists;

  private boolean autoRescan=true;

  private Proxy proxy=null;
  private MandelScannerCache scannercache;

  public Environment0(String tool, String[] args)
  { this(tool,args,basedir());
  }

  public Environment0(String[] args)
  { this(args,basedir());
  }

  public Environment0(String[] args, File dir)
  {
    this(null,args,dir);
  }

  public Environment0(String tool, String[] args, File dir)
  { String path=null;
    String cpath=null;
    String npath=null;
    
    if (args==null || args.length<1) {
      initialName=new QualifiedMandelName(MandelName.ROOT);
    }
    else {
      File f=new File(args[0]);
      if (f.exists() && !f.isDirectory()) initialFile=f;
      else initialName=QualifiedMandelName.create(f);
    }

    fac=new MandelImage.Factory();
    try {
      settings=Settings.getSettings(dir);
      autoRescan=settings.getSwitch(Settings.AUTORESCAN,true);

      String p=settings.getProperty(Settings.PROXY);
      if (p!=null) {
        int ix=p.indexOf(":");
        if (ix>0) {
          String host=p.substring(0,ix);
          int port=Integer.parseInt(p.substring(ix+1));
          proxy=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(host,port));
        }
      }
      path=settings.getProperty(Settings.PATH);
      cpath=settings.getProperty(Settings.COLORMAP_PATH);
      npath=settings.getProperty(Settings.RASTER_SAVE_PATH);

      if (tool!=null) {
        p=settings.getProperty(tool+"."+Settings.PATH);
        if (p!=null) path+=";"+p;
        p=settings.getProperty(tool+"."+Settings.COLORMAP_PATH);
        if (p!=null) cpath+=";"+p;
        p=settings.getProperty(tool+"."+Settings.RASTER_SAVE_PATH);
        if (p!=null) npath=p;
      }
    }
    catch (IOException ex) {
      System.err.println("cannot read mandel settings");
    }

    scannercache=new MandelScannerCache(proxy);

    all=new PathMandelScanner(path, MandelScanner.ALL, settings.isLocal(),
                              scannercache);
    imagedata=new FilteredMandelScanner(all, MandelScanner.HAS_IMAGEDATA);
    autoimagedata=new AutoScanner(imagedata);
    raster=new FilteredMandelScanner(all, MandelScanner.RASTER);
    info=new FilteredMandelScanner(all, MandelScanner.INFO);
    meta=new FilteredMandelScanner(all, MandelScanner.HAS_INFO);
    autometa=new AutoScanner(meta);
    rasterimage=new FilteredMandelScanner(all, MandelScanner.RASTERIMAGE);

    colormaps=new PathMandelScanner(cpath, MandelScanner.COLORMAP,
                                           settings.isLocal(), scannercache);

    prioinfo=new PathMandelScanner(
                       settings.getProperty(Settings.INFO_PRIO_PATH),
                       MandelScanner.INFO, settings.isLocal(), scannercache);

    ///////////////////////////////////////////////////////////////
    newraster=new PathMandelScanner(npath, MandelScanner.RASTER,
                                           settings.isLocal(), scannercache);

    ttodos=createMandelListFolderTree(Settings.TODO);
    todos=ttodos.getRoot().getMandelList();
    System.out.println("seen areas");
    seenrasters=createMandelList(Settings.SEEN);

    //System.out.println("**** todo rasters "+todos);
    System.out.println("new");
    newrasters=new NewRasterList();
    if (seenrasters!=null) {
      System.out.println("unseen");
      unseenrasters=new DeltaMandelList(autoimagedata,seenrasters);
    }
    System.out.println("common");
    commonSetup();
  }

  public Environment0(String tool, String[] args, URL dir)
  { String path=null;
    String cpath=null;

    this.readonly=true;

    if (args==null || args.length<1) {
      initialName=new QualifiedMandelName(MandelName.ROOT);
    }
    else {
      initialName=QualifiedMandelName.create(dir.getPath());
    }

    fac=new MandelImage.Factory();
    try {
      settings=Settings.getSettings(AbstractFile.Factory.create(null, dir));
      autoRescan=settings.getSwitch(Settings.AUTORESCAN,true);

      String p=settings.getProperty(Settings.PROXY);
      if (!Utils.isEmpty(p)) {
        int ix=p.indexOf(":");
        if (ix>0) {
          String host=p.substring(0,ix);
          int port=Integer.parseInt(p.substring(ix+1));
          proxy=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(host,port));
        }
      }

      path=settings.getProperty(Settings.PATH);
      cpath=settings.getProperty(Settings.COLORMAP_PATH);

      if (tool!=null) {
        p=settings.getProperty(tool+"."+Settings.PATH);
        if (p!=null) path+=";"+p;
        p=settings.getProperty(tool+"."+Settings.COLORMAP_PATH);
        if (p!=null) cpath+=";"+p;
      }
    }
    catch (IOException ex) {
      System.err.println("cannot read mandel settings");
    }

    scannercache=new MandelScannerCache(proxy);

    all=new PathMandelScanner(path, MandelScanner.ALL, settings.isLocal(),
                              scannercache);
    imagedata=new FilteredMandelScanner(all, MandelScanner.HAS_IMAGEDATA);
    autoimagedata=new AutoScanner(imagedata);
    raster=new FilteredMandelScanner(all, MandelScanner.RASTER);
    info=new FilteredMandelScanner(all, MandelScanner.INFO);
    meta=new FilteredMandelScanner(all, MandelScanner.HAS_INFO);
    autometa=new AutoScanner(meta);
    rasterimage=new FilteredMandelScanner(all, MandelScanner.RASTERIMAGE);
    
    colormaps=new PathMandelScanner(cpath, MandelScanner.COLORMAP,
                                    settings.isLocal(),scannercache);

    prioinfo=new PathMandelScanner(
                       settings.getProperty(Settings.INFO_PRIO_PATH),
                       MandelScanner.INFO, settings.isLocal(), scannercache);

    commonSetup();
  }

  public String getCopyright(MandelInfo info)
  {
    return getCopyright(info.getCreator(), info.getSite());
  }

  public String getCopyright(String creator, String site)
  {
    String c=creator;
    if (Utils.isEmpty(c)) c=site;
    if (Utils.isEmpty(c)) c=this.copyright;
    else c="by "+c;
    return c;
  }

  protected final void commonSetup()
  {
    copyright=settings.getProperty(Settings.COPYRIGHT);
    if (copyright!=null) {
      if (copyright.equals("")) {
        copyright=getCopyright(settings.getProperty(Settings.USER),
                               settings.getProperty(Settings.SITE));
      }
    }
    System.out.println("variants");
    variants=new VariantImageList();
    System.out.println("leafs");
    leafs=new LeafImageList();
    System.out.println("pending");
    pending=new PendingImageList();
    areas=createMandelList(Settings.AREAS);

    colors=createColorList(Settings.COLORS);
    tags=createTagList(Settings.TAGS);
   
    tfavorites=createMandelListFolderTree(Settings.FAVORITES);
    favorites=tfavorites.getRoot().getMandelList();
    memory=new MemoryMandelListFolderTree("memory");

    colormaplist=new ScannerColormapList(getColormapScanner());

    String cmname=settings.getProperty(Settings.DEFCOLORMAP);
    System.out.println("default colormap property: "+cmname);
    if (cmname!=null && colormaplist!=null) {
      try {
        defcolormap=colormaplist.get(new ColormapName(cmname));
        if (defcolormap==null) {
          System.err.println("colormap "+cmname+" not found");
        }
      }
      catch (IOException io) {
        System.err.println("cannot read colormap "+cmname);
      }
    }
    else {
      System.out.println("no default colormap");
    }

    userlists=new ArrayList<MandelListFolderTree>();
    addUserLists(settings.getProperty(Settings.USERLIST_PATH));
  }

  private void addUserLists(String path)
  {
    if (path!=null) {
      Set<Object> elements=new HashSet<Object>();
      System.out.println("found userlists: "+path);
      StringTokenizer t=new StringTokenizer(path, ";");
      while (t.hasMoreTokens()) {
        String p=t.nextToken().trim();
        if (!p.equals("")) {
          if (!elements.contains(p)) {
            elements.add(p);
            userlists.add(createMandelListFolderTreeFor(p));
          }
        }
      }
    }
    else {
      System.out.println("no userlists found");
    }
  }

  public AbstractFile createMandelFile(String path)
  {
    return AbstractFile.Factory.create(path, proxy, settings.isLocal());
  }
  
  public final MandelList createMandelList(String prop)
  { String path=settings.getProperty(prop);
    if (Utils.isEmpty(path)) return null;
    AbstractFile mf=createMandelFile(path);
    return new FileMandelList(mf);
  }

  public final MandelListFolderTree createMandelListFolderTree(String prop)
  { String path=settings.getProperty(prop);
    if (Utils.isEmpty(path)) return null;
    return createMandelListFolderTreeFor(path);
  }

  public final MandelListFolderTree createMandelListFolderTreeFor(String path)
  {
    try {
      AbstractFile mf=createMandelFile(path);
      return new FileMandelListFolderTree(mf);
    }
    catch (Exception ex) {
      System.err.println("cannot get folder tree "+path+": "+ex);
      return null;
    }
  }

  public final ColorList createColorList(String prop)
  { String path=settings.getProperty(prop);
    ColorList list=null;
    if (Utils.isEmpty(path)) return null;
  
    AbstractFile f=AbstractFile.Factory.create(path, proxy, settings.isLocal());
    return new FileColorList(f);
  }

  public final TagList createTagList(String prop)
  { String path=settings.getProperty(prop);
    ColorList list=null;
    if (Utils.isEmpty(path)) return null;

    AbstractFile f=AbstractFile.Factory.create(path, proxy, settings.isLocal());
    return new FileTagList(f);
  }
 

  public boolean isReadonly(String label)
  {
    if (label==null) return isReadonly();
    return true;
  }

  public boolean isReadonly()
  {
    return readonly;
  }

  public boolean isAutoRescan()
  { return autoRescan;
  }

  public void setAutoRescan(boolean b)
  { autoRescan=b;
  }

  public void refresh(MandelList list)
  { boolean save=autoRescan;
    autoRescan=true;
    list.refresh(false);
    autoRescan=save;
  }

  public void autoRescan()
  {
    if (isAutoRescan()) rescan();
  }

  public void autoRescan(boolean verbose)
  {
    if (isAutoRescan()) rescan(verbose);
  }

  public void rescan()
  { rescan(false);
  }

  public void rescan(boolean verbose)
  { 
    System.out.println("RESCAN FileSystem");
    all.rescan(verbose);
  }

  public String getCopyright()
  {
    return copyright;
  }

  public QualifiedMandelName getInitialName()
  {
    return initialName;
  }

  public File getInitialFile()
  {
    return initialFile;
  }

  public ColorList getColors()
  {
    return colors;
  }

  public TagList getTags()
  {
    return tags;
  }

  public Colormap getDefaultColormap()
  {
    return defcolormap;
  }

  public MandelListFolderTree getFavorites()
  { return tfavorites;
  }

  public MandelListFolderTree getMemory()
  { return memory;
  }

  public MandelList getMainFavorites()
  { return favorites;
  }

  public MandelListFolderTree getTodos()
  { return ttodos;
  }

  public MandelList getMainTodos()
  { return todos;
  }

  public MandelList getNewRasters()
  { return newrasters;
  }

  public MandelList getAreas()
  { return areas;
  }

  public MandelList getSeenRasters()
  { return seenrasters;
  }

  public MandelList getUnseenRasters()
  { return unseenrasters;
  }
  
  public MandelList getVariants()
  { return variants;
  }

  public MandelList getLeafs()
  { return leafs;
  }

  public MandelList getPending()
  { return pending;
  }

  public List<MandelListFolderTree> getUserLists()
  {
    return Collections.unmodifiableList(userlists);
  }

  public ColormapList getColormaps()
  { return colormaplist;
  }
  
  public MandelScanner getAllScanner()
  {
    return all;
  }

  public MandelScanner getColormapScanner()
  {
    return colormaps;
  }

  public MandelScanner getNewRasterScanner()
  {
    return newraster;
  }

  public MandelScanner getInfoScanner()
  {
    return info;
  }

  public MandelScanner getPrioInfoScanner()
  {
    return prioinfo;
  }

  public MandelScanner getMetaScanner()
  {
    return meta;
  }

  public MandelScanner getAutoMetaScanner()
  {
    return autometa;
  }

  public MandelScanner getImageDataScanner()
  {
    return imagedata;
  }

  public MandelScanner getAutoImageDataScanner()
  {
    return autoimagedata;
  }

  public MandelScanner getRasterImageScanner()
  {
    return rasterimage;
  }

  public MandelScanner getRasterScanner()
  {
    return raster;
  }

  public Settings getSettings()
  {
    return settings;
  }

  public String getProperty(String name)
  {
    return settings.getProperty(name);
  }

  public MandelImage.Factory getFactory()
  {
    return fac;
  }

  /////////////////////////////////////////////////////////////////////////

  public Set<MandelName> getSubNames(MandelName n, MandelScanner scan)
  { MandelName s=n.sub();
    Set<MandelName> set=new HashSet<MandelName>();

    while (s!=null) {
       //System.out.println("checking "+s);
       if (!scan.getMandelHandles(s).isEmpty()) {
         //System.out.println("  found "+s);
         set.add(s);
       }
       s=s.next();
    }
    return set;
  }

  public boolean hasSubNames(MandelName n, MandelScanner scan)
  { MandelName s=n.sub();

    while (s!=null) {
       //System.out.println("checking "+s);
       if (!scan.getMandelHandles(s).isEmpty()) {
         //System.out.println("  found "+s);
         return true;
       }
       s=s.next();
    }
    return false;
  }


  public boolean hasSubNames(MandelName n, MandelScanner scan,
                                    MandelScanner.Filter f)
  { MandelName s=n.sub();

    while (s!=null) {
       //System.out.println("checking "+s);
       Set<MandelHandle> set=scan.getMandelHandles(s);
       if (MandelScannerUtils.hasAtLeastOne(set, f)) return true;
       s=s.next();
    }
    return false;
  }

  //////////////////////////////////////////////////////////////////////////

  public MandelHandle getMandelImageData(MandelName name)
  {
    QualifiedMandelName qn=new QualifiedMandelName(name);
    MandelHandle h=getImageDataScanner().getMandelData(qn);
    if (h==null)  h=getImageDataScanner().getMandelData(name);
    return h;
  }

  public MandelHandle getMandelImageData(QualifiedMandelName name)
  { 
    return getImageDataScanner().getMandelData(name);
  }

  /////////////////////////////////////////////////////////////////////////
  public MandelAreaImage getMandelImage(MandelName name) throws IOException
  { return getMandelImage(name, ResizeMode.RESIZE_PROPORTIONAL,null);
  }

  public MandelAreaImage getMandelImage(MandelName name, ColormapModel cm)
                     throws IOException
  { return getMandelImage(name,cm,null);
  }

  public MandelAreaImage getMandelImage(MandelName name, ResizeMode mode,
                                    Colormap cm)
                     throws IOException
  { return getMandelImage(name,mode,cm,null);
  }

  public MandelAreaImage getMandelImage(MandelName name,
                                    ColormapModel cm, Mapper m)
                     throws IOException
  {
    return getMandelImage(name,cm.getResizeMode(),cm.getColormap(),m);
  }

  public MandelAreaImage getMandelImage(MandelName name, ResizeMode mode,
                                    Colormap cm, Mapper m)
                     throws IOException
  {
    return getMandelImage(name,mode,cm,m,null);
  }

  public MandelAreaImage getMandelImage(MandelName name, ResizeMode mode,
                                    Colormap cm, Mapper m, FileInfo info)
                     throws IOException
  { MandelHandle h=getMandelImageData(name);
    if (h==null) {
      System.err.println("no image data found for "+name);
      return null;
    }
    return getMandelImage(h,mode,cm,m,info);
  }

  /////////////////////////////////////////////////////////////////////////
  public MandelAreaImage getMandelImage(QualifiedMandelName name) throws IOException
  { return getMandelImage(name, ResizeMode.RESIZE_PROPORTIONAL,null);
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name, ColormapModel cm)
                     throws IOException
  { return getMandelImage(name, cm, null);
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name, ResizeMode mode,
                                    Colormap cm)
                     throws IOException
  { return getMandelImage(name,mode, cm,null);
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name, ColormapModel cm,
                                    Mapper m)
                     throws IOException
  { return getMandelImage(name,cm==null?ResizeMode.RESIZE_PROPORTIONAL:
                                        cm.getResizeMode(),
                               cm==null?null:cm.getColormap(), m);
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name, ResizeMode mode,
                                    Colormap cm, Mapper m)
                     throws IOException
  {
    return getMandelImage(name,mode,cm,m,null);
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name, ResizeMode mode,
                                    Colormap cm, Mapper m, FileInfo info)
                     throws IOException
  { MandelHandle h=getMandelImageData(name);
    if (h==null) {
      System.err.println("no image data found for "+name);
      return null;
    }
    return getMandelImage(h, mode, cm, m, info);
  }

  /////////////////////////////////////////////////////////////////////////
//  public MandelImage getMandelImage(MandelFileName name,
//                                    ColormapModel cm, Mapper m)
//                     throws IOException
//  {
//    return getMandelImage(name,cm.getResizeMode(),cm.getColormap(),m);
//  }
//
//  public MandelImage getMandelImage(MandelFileName name, ResizeMode mode,
//                                    Colormap cm, Mapper m)
//                     throws IOException
//  {
//    return getMandelImage(name,mode,cm,m,null);
//  }
//
//  public MandelImage getMandelImage(MandelFileName name, ResizeMode mode,
//                                    Colormap cm, Mapper m, FileInfo info)
//                     throws IOException
//  { MandelData md=getMandelImageData(name);
//    if (md==null) {
//      System.err.println("no image data found for "+name);
//      return null;
//    }
//    return getMandelImage(md,mode,cm,m,info);
//  }
    
  public MandelAreaImage getMandelImage(MandelHandle h, ResizeMode mode,
                                    Colormap cm, Mapper m, FileInfo info)
                     throws IOException
  {
    MandelData md=h.getData();
    return getMandelImage(h.getName(),md,mode,cm,m,info);
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name, MandelData md,
                                    ResizeMode mode,
                                    Colormap cm, Mapper m, FileInfo info)
                     throws IOException
  {
    MandelImage img;
    if (info!=null) {
      info.setColormap(md.getColormap());
    }
    img=getFactory().getImage(md,mode,cm,m);
    if (img!=null) return new MandelAreaImage(name,img);
    return null;
  }

  ///////////////////////////////////////////////////////////////////////////

  public boolean handleRasterSeen(AbstractFile f)
  { String n=getProperty(Settings.RASTER_SAVE_PATH);
    String s=getProperty(Settings.RASTER_SEEN_PATH);
    String v=getProperty(Settings.VARIANT_SEEN_PATH);

    if (f==null || isReadonly()) return false;
    
    QualifiedMandelName mn=QualifiedMandelName.create(f);
    if (unseenrasters!=null && unseenrasters.contains(mn)) {
      if (debug) System.out.println(mn+" set to seen: "+f);
      unseenrasters.remove(mn);
      try {
        unseenrasters.save();
      }
      catch (IOException ex) {
        System.err.println("cannot write seen: "+ex);
      }
      seenModified();
    }
    if (!Utils.isEmpty(mn.getQualifier()) && !Utils.isEmpty(v)) s=v;

    //System.out.println("seen path: "+s);
    if (Utils.isEmpty(s)) return false;
    if (Utils.isEmpty(n)) return false;

    File root=null;
    if (f.isFile()) {
      try {
        root=f.getFile().getParentFile().getCanonicalFile();
      }
      catch (IOException ex) {
        System.err.println("cannot eval "+f);
        return false;
      }

      StringTokenizer t=new StringTokenizer(n, ";:");
      boolean found=false;
      while (t.hasMoreTokens()) {
        try {
          File save=new File(t.nextToken()).getCanonicalFile();
          if (save.equals(root)) {
            found=true;
            break;
          }
        }
        catch (IOException ex) {
         // ignore illegal path
        }
      }
      if (!found) {
        System.err.println("not in save path");
        return false;
      }
      if (debug) System.out.println("relocation candidate");

      File store;
      try {
        store=new File(s).getCanonicalFile();
      }
      catch (IOException ex) {
        return false;
      }
      if (!store.equals(root)&&f.isFile()) {
        File nf=new File(store, f.getName());
        File of=f.getFile();
        try {
          System.out.println("relocate file "+of+" to "+store);
          MandelFolder mf=MandelFolder.getMandelFolder(store);
          if (mf.renameTo(of, nf)) {
            if (of.exists()&&nf.exists()) {
              System.out.println("*** delete "+of);
              of.delete();
            }
            return true;
          }
        }
        catch (IOException ex) {
        }
        //if (isAutoRescan()) newraster.rescan(false);
        //else addLogicalFile(nf);
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  // backup
  ////////////////////////////////////////////////////////////////////////////
  protected File _getBackup(String prop)
  {
    File backup=null;
    String p=getProperty(prop);
    if (Utils.isEmpty(p)) p=getProperty(Settings.BACKUP_PATH);
    if (!Utils.isEmpty(p)) {
      backup=new File(p);
      backup.mkdirs();
      if (debug) System.out.println("using backup folder "+backup);
    }
    return backup;
  }

  public File getInfoBackup()
  { return _getBackup(Settings.INFO_BACKUP_PATH);
  }

  public File getRasterBackup()
  { return _getBackup(Settings.RASTER_BACKUP_PATH);
  }

  public File getRasterImageBackup()
  { return _getBackup(Settings.RASTERIMAGE_BACKUP_PATH);
  }

  public boolean backupFile(AbstractFile f, File backup)
  {
    if (backup!=null && f.isFile()) {
      File n=new File(backup,f.getName());
      if (debug) System.out.println("saving "+f);
      try {
        if (!MandelFolder.Util.renameTo(f.getFile(), n)) {
          MandelFolder.Util.delete(n);
          return MandelFolder.Util.renameTo(f.getFile(), n);
        }
      }
      catch (IOException io) {
        System.err.println("cannot save "+f+": "+io);
      }
      return true;
    }
    return false;
  }

  public boolean backupInfoFile(AbstractFile f)
  { return backupFile(f,getInfoBackup());
  }

  public boolean backupRasterFile(AbstractFile f)
  { return backupFile(f,getRasterBackup());
  }

  public boolean backupRasterImageFile(AbstractFile f)
  { return backupFile(f,getRasterImageBackup());
  }

  public boolean isCleanupInfo()
  {
    return Utils.parseBoolean(getProperty(Settings.INFO_CLEANUP),true);
  }

  public boolean isCleanupRaster()
  {
    return Utils.parseBoolean(getProperty(Settings.RASTER_CLEANUP),true);
  }

  public boolean isCleanupRasterImage()
  {
    return Utils.parseBoolean(getProperty(Settings.RASTERIMAGE_CLEANUP),false);
  }

  protected File getFolder(AbstractFile mf, String prop)
  { File d=(mf!=null&&mf.isFile())?mf.getFile():null;
    if (d!=null && d.exists() && d.isFile()) d=d.getParentFile();
    String v=getProperty(prop);
    if (Utils.isEmpty(v)) v=getProperty(Settings.SAVE_PATH);
    if (Utils.isEmpty(v)) return d;
    d=new File(v);
    d.mkdirs();
    return d;
  }

  public File getInfoFolder(AbstractFile d)
  {
    return getFolder(d, Settings.INFO_SAVE_PATH);
  }

  public File getRasterFolder(AbstractFile d)
  {
    return getFolder(d,Settings.RASTER_SAVE_PATH);
  }

  public File getRasterImageFolder(AbstractFile d)
  {
    QualifiedMandelName mn=QualifiedMandelName.create(d);
    if (!Utils.isEmpty(mn.getQualifier()) &&
        !Utils.isEmpty(settings.getProperty(Settings.VARIANT_SAVE_PATH))) {
      return getFolder(d,Settings.VARIANT_SAVE_PATH);
    }
    return getFolder(d,Settings.RASTERIMAGE_SAVE_PATH);
  }

  public File getImageFolder(AbstractFile d)
  {
    return getFolder(d,Settings.IMAGE_SAVE_PATH);
  }

  
  ///////////

  public File mapToRasterFile(AbstractFile f)
  { return MandUtils.mapFile(f,RASTER_SUFFIX,getRasterFolder(f));
  }

  public File mapToInfoFile(AbstractFile f)
  { return MandUtils.mapFile(f,INFO_SUFFIX,getInfoFolder(f));
  }

  public File mapToRasterImageFile(AbstractFile f)
  { return MandUtils.mapFile(f,RASTERIMAGE_SUFFIX,getRasterImageFolder(f));
  }

  public File mapToImageFile(AbstractFile f)
  { return MandUtils.mapFile(f,IMAGE_SUFFIX,getImageFolder(f));
  }

  protected void seenModified()
  {
  }

  ///////////////////////////////////////////////////////////////////////////

  private class NewRasterList extends ScannerBasedList {
    protected MandelScanner getScanner()
    {
      return getNewRasterScanner();
    }
  }

  
  ///////////////////////////////////////////////////////////////////////////

  private class VariantImageList extends ScannerBasedList {
    protected MandelScanner getScanner()
    {
      return getImageDataScanner();
    }

    @Override
    protected void _addAll(Set<QualifiedMandelName> set)
    {
      for (QualifiedMandelName n: set) {
        if (n.getQualifier()!=null) add(n);
      }
    }

    
  }

  private class LeafImageList extends ScannerBasedList {
    protected MandelScanner getScanner()
    {
      return getImageDataScanner();
    }

    @Override
    protected void _addAll(Set<QualifiedMandelName> set)
    {
      if (!set.isEmpty()) {
        MandelName mn=set.iterator().next().getMandelName();
        MandelScanner all=getAllScanner();
       
        if (!MandUtils.hasSubNames(mn, all)) {
          //  System.out.println("- "+mn+" has no sub areas");
          for (QualifiedMandelName n :set) {
            add(n);
          }
        }
        else {
          // System.out.println("+ "+mn+" has subareas");
        }
      }
    }
  }

  private class PendingImageList extends ScannerBasedList {
    protected MandelScanner getScanner()
    {
      return getImageDataScanner();
    }

    @Override
    protected void _addAll(Set<QualifiedMandelName> set)
    {
      if (!set.isEmpty()) {
        //System.out.println("_addAll");
        boolean add=false;
        QualifiedMandelName qn=set.iterator().next();
        MandelName mn=qn.getMandelName();
        MandelScanner all=getAllScanner();
        Set<MandelName> sub=MandUtils.getSubNames(mn, all);

        for (MandelName s :sub) {
          Set<MandelHandle> subs=all.getMandelHandles(s);
          if (!MandelScannerUtils.hasImageData(subs)) {
            add=true;
            break;
          }
        }
        if (add) { // add image (select generic name if possible
          for (QualifiedMandelName n :set) {
            if (n.getQualifier()==null) {
              add(n);
              add=false;
              break;
            }
          }
          if (add) add(qn);
        }
        //System.out.println("end");
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  public abstract class ScannerBasedList extends UniqueArrayMandelList {

    ScannerBasedList()
    {
      _refresh();
    }

    abstract protected MandelScanner getScanner();
    
    @Override
    public void refresh(boolean soft)
    {
      this.clear();
      if (!soft) getScanner().rescan(false);
      _refresh();
    }

    protected void _addAll(Set<QualifiedMandelName> set)
    {
      addAll(set);
    }
   
    private void _refresh()
    {
      for (MandelName n:getScanner().getMandelNames()) {
        _addAll(getScanner().getQualifiedMandelNames(n));
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // Auto
  ///////////////////////////////////////////////////////////////////////////

  private class AutoScanner extends MandelScannerProxy {
    public AutoScanner(MandelScanner s)
    {
      super(s);
    }

    @Override
    public void rescan(boolean verbose)
    {
      if (isAutoRescan())
        super.rescan(verbose);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    Environment0 env=new Environment0(null);
//    MandelScanner s=env.getColormapScanner();
//    System.out.println(s.getColormapNames());
  }
}
