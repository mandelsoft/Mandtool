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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.scan.FilteredMandelScanner;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.PathMandelScanner;
import com.mandelsoft.mand.scan.MandelScannerCache;
import com.mandelsoft.mand.util.ArrayTagList;
import com.mandelsoft.mand.util.ColorList;
import com.mandelsoft.mand.util.FileColorList;
import com.mandelsoft.mand.util.FileMandelList;
import com.mandelsoft.mand.util.FileMandelListFolderTree;
import com.mandelsoft.mand.util.FileTagList;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.mand.util.TagList;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class MandelImageDB implements MandelConstants  {
  static public boolean debug=false;
  
  private boolean readonly;

  private Settings settings;
  private String copyright;
  private PathMandelScanner all;

  private MandelScanner imagedata;
  private MandelScanner raster;
  private MandelScanner info;
  private MandelScanner prioinfo;
  private MandelScanner meta;
  private MandelScanner rasterimage;
  private MandelScanner areacolmap;
  private MandelScanner colormaps;
  private MandelScanner newraster;
  private MandelScanner incomplete;

  private ColorList  colors;
  private TagList    tags;
  private TagList    attrs;
  private MandelListFolderTree tfavorites;
  private MandelListFolderTree ttodos;
  private MandelListFolderTree tlinks;
  private MandelList favorites;
  private MandelList todos;
  private MandelList areas;
  private MandelList seenrasters;
  private MandelList refinements;
 
  private List<MandelListFolderTree> userlists;

  private Proxy proxy=null;
  private MandelScannerCache scannercache;

  public MandelImageDB(MandelImageDBFactory fac)
  { this(fac,new File("."));
  }

  public MandelImageDB(MandelImageDBFactory fac, File dir)
  {
    this(fac,AbstractFile.Factory.create(dir));
  }

  public MandelImageDB(MandelImageDBFactory fac, AbstractFile dir)
  { 
    String p;
    
    try {
      settings=Settings.getSettings(dir);
    }
    catch (IOException ex) {
      System.err.println("cannot read mandel settings: "+ex);
      // TODO: exception
    }

    if (dir.isFile()) {
      p=settings.getProperty(Settings.PROXY);
      if (p!=null) {
        int ix=p.indexOf(":");
        if (ix>0) {
          String host=p.substring(0, ix);
          int port=Integer.parseInt(p.substring(ix+1));
          proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }
      }
    }
    else {
      this.readonly=true;
      this.proxy=dir.getProxy();
    }

    commonSetup(fac.getTool());

    if (!this.readonly) {
      ttodos=createMandelListFolderTree(Settings.TODO);
      todos=ttodos.getRoot().getMandelList();
      seenrasters=createMandelList(Settings.SEEN);
    }
  }

  
  protected final void commonSetup(String tool)
  {
    String path=null;
    String cpath=null;
    String npath=null;
    String ipath=null;
    String p;

    path=settings.getProperty(Settings.PATH);
    cpath=settings.getProperty(Settings.COLORMAP_PATH);
    npath=settings.getProperty(Settings.RASTER_SAVE_PATH);
    ipath=settings.getProperty(Settings.INCOMPLETE_SAVE_PATH);

    if (tool!=null) {
      p=settings.getProperty(tool+"."+Settings.PATH);
      if (p!=null) path+=";"+p;
      p=settings.getProperty(tool+"."+Settings.COLORMAP_PATH);
      if (p!=null) cpath+=";"+p;
      p=settings.getProperty(tool+"."+Settings.RASTER_SAVE_PATH);
      if (p!=null) npath=p;
      p=settings.getProperty(tool+"."+Settings.INCOMPLETE_SAVE_PATH);
      if (p!=null) ipath=p;
    }

    scannercache=new MandelScannerCache(proxy);

    all=new PathMandelScanner(path, MandelScanner.ALL, settings.isLocal(),
                              scannercache);
    areacolmap=new FilteredMandelScanner(all, MandelScanner.HAS_AREACOLMAP);
    imagedata=new FilteredMandelScanner(all, MandelScanner.HAS_IMAGEDATA);
    raster=new FilteredMandelScanner(all, MandelScanner.RASTER);
    info=new FilteredMandelScanner(all, MandelScanner.INFO);
    meta=new FilteredMandelScanner(all, MandelScanner.HAS_INFO);
    rasterimage=new FilteredMandelScanner(all, MandelScanner.RASTERIMAGE);

    colormaps=new PathMandelScanner(cpath, MandelScanner.COLORMAP,
                                    settings.isLocal(),scannercache);

    prioinfo=new PathMandelScanner(
                       settings.getProperty(Settings.INFO_PRIO_PATH),
                       MandelScanner.INFO, settings.isLocal(), scannercache);

    if (!this.readonly) {
      newraster=new PathMandelScanner(npath, MandelScanner.RASTER,
                                      settings.isLocal(), scannercache);
      incomplete=new PathMandelScanner(ipath, MandelScanner.INCOMPLETERASTER,
                                      settings.isLocal(), scannercache);
    }

    copyright=settings.getProperty(Settings.COPYRIGHT);
    if (copyright!=null) {
      if (copyright.equals("")) {
        copyright=getCopyright(settings.getProperty(Settings.USER),
                               settings.getProperty(Settings.SITE));
      }
    }
   
    areas=createMandelList(Settings.AREAS);
    refinements=createMandelList(Settings.REFINEMENTS);

    colors=createColorList(Settings.COLORS);
    tags=createTagList(Settings.TAGS);
    attrs=createTagList(Settings.ATTRS);
   
    tfavorites=createMandelListFolderTree(Settings.FAVORITES);
    favorites=tfavorites.getRoot().getMandelList();

    try {
      String fp=settings.getProperty(Settings.LINKS);
      if (!Utils.isEmpty(fp)) {
        AbstractFile mf=createAbstractFile(fp);
        //tlinks=new FileMandelListListMandelListFolderTree(mf);
        tlinks=new LinkTree(mf);
      }
    }
    catch (Exception ex) {
      System.err.println("cannot get folder tree "+path+": "+ex);
    }

    userlists=new ArrayList<MandelListFolderTree>();
    addUserLists(settings.getProperty(Settings.USERLIST_PATH));

    if (debug) {
      System.out.println("**** lookup area colormaps: "+path);
      for (MandelHandle h:areacolmap.getMandelHandles()) {
        if (h.getHeader().isAreaColormap()) {
          System.out.println("areacm: "+h.getFile());
        } else {
          System.out.println("image:  "+h.getFile());
        }
      }
    }
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

  public AbstractFile createAbstractFile(String path)
  {
    return AbstractFile.Factory.create(path, proxy, settings.isLocal());
  }
  
  public final MandelList createMandelList(String prop)
  { String path=settings.getProperty(prop);
    if (Utils.isEmpty(path)) return null;
    AbstractFile mf=createAbstractFile(path);
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
      AbstractFile mf=createAbstractFile(path);
      return new FileMandelListFolderTree(mf);
    }
    catch (Exception ex) {
      System.err.println("cannot get folder tree "+path+": "+ex);
      return null;
    }
  }

  public final ColorList createColorList(String prop)
  { String path=settings.getProperty(prop);
    if (Utils.isEmpty(path)) return null;
  
    AbstractFile f=AbstractFile.Factory.create(path, proxy, settings.isLocal());
    return new FileColorList(f);
  }

  public final TagList createTagList(String prop)
  { String path=settings.getProperty(prop);
    if (Utils.isEmpty(path)) return new ArrayTagList();

    AbstractFile f=AbstractFile.Factory.create(path, proxy, settings.isLocal());
    return new FileTagList(f);
  }
 

  public boolean isReadonly()
  {
    return readonly;
  }

  public Proxy getProxy()
  {
    return proxy;
  }

  public void rescan()
  { rescan(false);
  }

  public void rescan(boolean verbose)
  { 
    System.out.println("RESCAN FileSystem");
    all.rescan(verbose);
    if (incomplete!=null) incomplete.rescan(verbose);
  }

  public String getCopyright(MandelInfo info)
  {
    return getCopyright(info.getCreator(), info.getSite());
  }

  public String getCopyright(String creator, String site)
  {
    String copyright=creator;
    if (Utils.isEmpty(copyright)) {
      copyright=site;
    }
    if (Utils.isEmpty(copyright)) copyright=this.copyright;
    else copyright="by "+copyright;
    return copyright;
  }

  public String getCopyright()
  {
    return copyright;
  }

  public ColorList getColors()
  {
    return colors;
  }

  public TagList getTags()
  {
    return tags;
  }
  
  public TagList getAttrs()
  {
    return attrs;
  }
  
  //////////////////////////////////////////////////////////////////////////

  public MandelListFolderTree getFavorites()
  { return tfavorites;
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

  public MandelListFolderTree getLinks()
  { return tlinks;
  }

  public MandelList getAreas()
  { return areas;
  }

  public MandelList getSeenRasters()
  { return seenrasters;
  }

  public MandelList getRefinements()
  { return refinements;
  }

  public List<MandelListFolderTree> getUserLists()
  {
    return Collections.unmodifiableList(userlists);
  }

  //////////////////////////////////////////////////////////////////////////

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

  public MandelScanner getAreaColormapScanner()
  {
    return areacolmap;
  }

  public MandelScanner getImageDataScanner()
  {
    return imagedata;
  }

  public MandelScanner getRasterImageScanner()
  {
    return rasterimage;
  }

  public MandelScanner getRasterScanner()
  {
    return raster;
  }

  public MandelScanner getIncompleteScanner()
  {
    return incomplete;
  }

  //////////////////////////////////////////////////////////////////////////

  public Settings getSettings()
  {
    return settings;
  }

  public String getProperty(String name)
  {
    return settings.getProperty(name);
  }

  public boolean getSwitch(String name, boolean def)
  {
    return settings.getSwitch(name,def);
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

  public File getAreaColormapFolder(AbstractFile d)
  {
    return getFolder(d, Settings.AREACOLMAP_SAVE_PATH);
  }

  public File getRasterFolder(AbstractFile d)
  {
    return getFolder(d,Settings.RASTER_SAVE_PATH);
  }

  public File getIncompleteFolder(AbstractFile d)
  {
    return getFolder(d,Settings.INCOMPLETE_SAVE_PATH);
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

  
  //////////////////////////////////////////////////////////////////////////

  public File mapToIncompleteFile(AbstractFile f)
  { return MandUtils.mapFile(f,INCOMPLETE_SUFFIX,getIncompleteFolder(f));
  }

  public File mapToRasterFile(AbstractFile f)
  { return MandUtils.mapFile(f,RASTER_SUFFIX,getRasterFolder(f));
  }

  public File mapToAreaColormapFile(AbstractFile f)
  { return MandUtils.mapFile(f,AREACOLMAP_SUFFIX,getRasterFolder(f));
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
}
