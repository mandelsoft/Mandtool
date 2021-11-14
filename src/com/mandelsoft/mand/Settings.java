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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class Settings {
 
  static public final String ENV_MANDEL_HOME="MANDEL_HOME";
  static public final String ENV_HOME="HOME";

  static public final String MANDEL_SETTINGS=".mandtool";


  static public final String MANDEL_HOME="home";
  static public final String MANDEL_DIR="dir";
  static public final String MANDEL_LISTS="standardlists.dir";
  static public final String BASE="base";
  static public final String PATH="path";
  static public final String ADDITIONAL_PATH="path.additional";

  static public final String AUTORESCAN="autorescan";
  static public final String PROXY="proxy";
  static public final String USER="username";
  static public final String SITE="sitename";
  static public final String HOMEPAGE="homepage";
  static public final String COPYRIGHT="copyright";
  
  static public final String BACKUP_PATH="path.backup";
  static public final String SAVE_PATH="path.save";

  static public final String BITMAP_SAVE_PATH="path.bitmap.save";
  static public final String INCOMPLETE_SAVE_PATH="path.incomplete.save";
  static public final String INCOMPLETE_BACKUP_PATH="path.incomplete.backup";

  static public final String INFO_PRIO_PATH="path.info.prio";
  static public final String INFO_SAVE_PATH="path.info.save";
  static public final String INFO_BACKUP_PATH="path.info.backup";

  static public final String RASTER_PATH="path.raster";
  static public final String RASTER_SEEN_PATH="path.raster.seen";
  static public final String VARIANT_SEEN_PATH="path.variant.seen";
  static public final String RASTER_SAVE_PATH="path.raster.save";
  
  static public final String AREACOLMAP_PATH="path.areacolormap";
  static public final String AREACOLMAP_SAVE_PATH="path.areacolormap.save";
  static public final String AREACOLMAP_BACKUP_PATH="path.areacolormap.backup";

  static public final String RASTER_BACKUP_PATH="path.raster.backup";

  static public final String VARIANT_SAVE_PATH="path.variant.save";
  static public final String RASTERIMAGE_SAVE_PATH="path.rasterimage.save";
  static public final String RASTERIMAGE_BACKUP_PATH="path.rasterimage.backup";

  static public final String IMAGE_SAVE_PATH="path.image.save";

  static public final String INFO_CLEANUP="cleanup.info";
  static public final String RASTER_CLEANUP="cleanup.raster";
  static public final String RASTERIMAGE_CLEANUP="cleanup.rasterimage";

  static public final String COLORMAP_SAVE_PATH="path.colormap.save";
  static public final String COLORMAP_PATH="path.colormap";

  static public final String FAVORITES="favorites";
  static public final String AREAS="areas";
  static public final String TODO="todo";
  static public final String REFINEMENTS="refinements";
  static public final String SEEN="seen";
  static public final String LINKS="links";
  static public final String COLORS="colors";
  static public final String TAGS="tags";
  static public final String ATTRS="attributes";

  static public final String USERLIST_PATH="path.lists";
  static public final String LIST_SHORTCUTS="shortcuts.lists";

  static public final String DEFCOLORMAP="colormap.default";

  static public final String NESTED="path.base";
  static public final String ALIASES="aliases";

  static public final String COLORMAP_CACHE_SIZE="cache.colormaps";
  
  ////////////////////////////////////////////////////////////////////////
  // factory
  ////////////////////////////////////////////////////////////////////////

  static public Settings getSettings() throws IOException
  {
    return getSettings(new File("."));
  }
  
  static public Settings getSettings(File dir) throws IOException
  {
    return getSettings(AbstractFile.Factory.create(dir));
  }

  static public Settings getSettings(AbstractFile dir) throws IOException
  { 
    Settings ms=new Settings(dir.isFile());
    String prop="current.dir";
    System.out.println("*** settings for "+dir);
    if (dir.isFile()) {
      File f=new File(dir.getFile(), MANDEL_SETTINGS);
      ms.setProperty(prop, dir.toString());
      if (!f.exists()||!f.isFile()) {
        ms=handle(System.getenv(ENV_MANDEL_HOME), ms, MANDEL_HOME, true);
        ms=handle(System.getenv(ENV_HOME), ms, "user.home", true);
      }
      else ms=handle(dir, ms, null, true);
    }
    else ms=handle(dir,ms,prop, true);

    String b=ms.getProperty(BASE);
    if (!Utils.isEmpty(b)) {
      System.out.println("base is "+b);
      Settings base=getSettings(AbstractFile.Factory.create(b, dir.getProxy(),
                                                               dir.isFile()));
      ms=handle(dir,base,null, false);
      ms.remove(BASE);
    }
    return ms;
  }

  //////////////////////////////////////////////////////////////////////////

  static private Settings handle(String dir, Settings parent,
                                       String prop, boolean mandel)
                 throws IOException
  { if (dir!=null) {
      return handle(new File(dir),parent,prop, mandel);
    }
    return parent;
  }

  static private Settings handle(File dir, Settings parent,
                                       String prop, boolean mandel)
                 throws IOException
  { if (dir!=null) {
      return handle(AbstractFile.Factory.create(dir),parent,prop, mandel);
    }
    return parent;
  }

  static private Settings handle(AbstractFile d, Settings parent,
                                 String prop, boolean mandel)
                 throws IOException
  {
    if (d!=null) {
      if (!d.isFile() || (d.getFile().exists() && d.getFile().isDirectory())) {
        if (prop!=null) parent.setProperty(prop, d.toString());
        AbstractFile m=d.getSub(MANDEL_SETTINGS);
        if (!m.isFile() || (m.getFile().exists() && m.getFile().isFile())) {
          if (mandel) {
            parent.setProperty(MANDEL_DIR, d.toString());
          }
          parent=new Settings(m, parent);
        }
      }
    }
    return parent;
  }

  //////////////////////////////////////////////////////////////////////////
  // property evaluation
  //////////////////////////////////////////////////////////////////////////

  private void process() throws IOException
  {

    if (parent!=null) {
      for (String n:parent.propertyNames()) {
        if (!raw.containsKey(n)) {
          raw.setProperty(n,parent.raw.getProperty(n));
        }
      }
    }

    for (String n:propertyNames()) {
      props.setProperty(n, process(n,new Stack<String>()));
      // System.out.println(n+": "+raw.getProperty(n)+" -> "+props.getProperty(n));
    }
  }
  
  private  String process(String n, Stack<String> stack)
                        throws IOException
  { String v=props.getProperty(n);
    if (!Utils.isEmpty(v)) return v;
    v=raw.getProperty(n);
    if (Utils.isEmpty(v)) return v;
    StringBuilder sb=new StringBuilder();
    if (stack.contains(n)) {
       throw new IOException("recursion found for "+n+": "+stack);
    }
    stack.push(n);
    try {
      int is=0;
      int ix;
      while ((ix=v.indexOf("${",is))>=0) {
        sb.append(v.substring(is, ix));
        int ie=v.indexOf("}", ix);
        if (ie>=0) {
          String name=v.substring(ix+2, ie);
          ix=ix+1;
          String subst;
          if (name.equals(n)) {
            if (parent==null) subst="";
            else subst=parent.getProperty(n);
          }
          else {
            subst=process(name, stack);
            if (!Utils.isEmpty(subst)) props.setProperty(name,subst);
          }
          if (!Utils.isEmpty(subst)) sb.append(subst);
          is=ie+1;
        }
        else {
          sb.append("${");
          is=ix+2;
        }
      }
      sb.append(v.substring(is));
    }
    finally {
      stack.pop();
    }
    return sb.toString();
  }

  ////////////////////////////////////////////////////////////////////////
  // Settings
  ////////////////////////////////////////////////////////////////////////

  private Settings   parent;
  private Properties raw=new Properties();
  private Properties props=new Properties();
  private boolean    local;

  public Settings(boolean local)
  { this.parent=null;
    this.local=local;

    System.out.println("local="+local);
    // set defaults
    raw.setProperty("mandtool.root",MandelName.ROOT_NAME);

    try {
      raw.setProperty(USER,System.getProperty("user.name"));
    }
    catch (AccessControlException ex) {
    }
    InetAddress addr;
    try {
      addr=InetAddress.getLocalHost();
      String host=addr==null?null:addr.getCanonicalHostName();
      if (host!=null) raw.setProperty(SITE,host);
    }
    catch (UnknownHostException ex) {
    }

    raw.setProperty(MANDEL_LISTS, "${dir}");

    raw.setProperty(FAVORITES, "${standardlists.dir}/favorites");
    raw.setProperty(COLORS, "${standardlists.dir}/colors");
    raw.setProperty(TAGS, "${standardlists.dir}/tags");
    raw.setProperty(AREAS, "${standardlists.dir}/areas");
    raw.setProperty(REFINEMENTS, "${standardlists.dir}/refine");

    if (local) {
      raw.setProperty(TODO, "${standardlists.dir}/todo");
      raw.setProperty(LINKS, "${standardlists.dir}/links");

      raw.setProperty(SEEN, "${standardlists.dir}/seen");
      raw.setProperty(BITMAP_SAVE_PATH, "${dir}/bitmaps");
      raw.setProperty(INCOMPLETE_SAVE_PATH, "${dir}/incomplete");

      raw.setProperty(RASTERIMAGE_BACKUP_PATH, "${"+BACKUP_PATH+"}");
      raw.setProperty(RASTER_BACKUP_PATH, "${"+BACKUP_PATH+"}");
      raw.setProperty(INFO_BACKUP_PATH, "${"+BACKUP_PATH+"}");
      raw.setProperty(AREACOLMAP_BACKUP_PATH, "${"+BACKUP_PATH+"}");
      raw.setProperty(INCOMPLETE_BACKUP_PATH, "${"+BACKUP_PATH+"}");
    }
    
    raw.setProperty(IMAGE_SAVE_PATH, "${"+SAVE_PATH+"}");
    raw.setProperty(RASTER_SAVE_PATH, "${"+SAVE_PATH+"}");
    raw.setProperty(AREACOLMAP_SAVE_PATH, "${"+SAVE_PATH+"}");
    raw.setProperty(RASTER_PATH, "${"+RASTER_SAVE_PATH+"};"+
            "${"+RASTER_SEEN_PATH+"};"+
            "${"+VARIANT_SEEN_PATH+"}");
    raw.setProperty(RASTERIMAGE_SAVE_PATH, "${"+SAVE_PATH+"}");
    raw.setProperty(INFO_SAVE_PATH, "${"+SAVE_PATH+"}");
    raw.setProperty(VARIANT_SEEN_PATH, "${"+VARIANT_SAVE_PATH+"}");

    raw.setProperty(PATH, "${"+IMAGE_SAVE_PATH+"};${"+
            RASTERIMAGE_SAVE_PATH+"};${"+
            RASTER_SAVE_PATH+"};${"+
            RASTER_SEEN_PATH+"};${"+
            VARIANT_SEEN_PATH+"};${"+
            RASTER_PATH+"};${"+
            ADDITIONAL_PATH+"};${"+
            AREACOLMAP_PATH+"};${"+
            AREACOLMAP_SAVE_PATH+"};${"+
            INFO_PRIO_PATH+"};${"+
            INFO_SAVE_PATH+"}");

    raw.setProperty(COLORMAP_SAVE_PATH, "${"+SAVE_PATH+"}");
    raw.setProperty(COLORMAP_PATH, "${"+COLORMAP_SAVE_PATH+"}");

    try {
      process();
    }
    catch (IOException ex) {
    }
  }

  public Settings(AbstractFile file, Settings parent) throws IOException
  {
    if (parent!=null) local=parent.local;
    else local=file.isFile();
    this.parent=parent;
    BufferedInputStream is=null;
    try {
      System.out.println("reading mandel settings URL "+file+"...");
      raw.load(is=new BufferedInputStream(file.getInputStream()));
    }
    finally {
      if (is!=null) is.close();
    }
    process();
  }

  public boolean isLocal()
  {
    return local;
  }
  
  public Set<String> propertyNames()
  {
    return raw.stringPropertyNames();
  }

  public synchronized Object setProperty(String key, String value)
  {
    raw.setProperty(key, value);
    String old=getProperty(key);
    try {
      process();
    }
    catch (IOException ex) {
      return null;
    }
    return old;
  }

  public String getProperty(String key, String defaultValue)
  {
    return props.getProperty(key, defaultValue);
  }

  public String getProperty(String key)
  {
    return props.getProperty(key);
  }

  public boolean getSwitch(String key, boolean def)
  {
    String p=getProperty(key);
    if (!Utils.isEmpty(p)) {
      p=p.toLowerCase();
      if (p.equals("true") || p.equals("on") || p.equals("yes")) {
        def=true;
      }
      else {
        if (p.equals("false") || p.equals("off") || p.equals("no"))
        def=false;
      }
    }
    return def;
  }

  public boolean contains(String p)
  {
    return raw.contains(p);
  }

  public boolean remove(String p)
  {
    if (raw.contains(p)) {
      raw.remove(p);
      props.remove(p);
      return true;
    }
    return false;
  }
  ////////////////////////////////////////////////////////////////////////
  // main
  ////////////////////////////////////////////////////////////////////////
  
  static private void Error(String msg)
  { System.err.println(msg);
    System.exit(2);
  }

  static public void main(String[] args)
  {
    try {
    Settings ms=Settings.getSettings();
    for (String s:ms.propertyNames()) {
      System.out.println(s+"="+ms.getProperty(s));
    }
    }
    catch (IOException ex) {
      Error("cannot read settings: "+ex);
    }
  }
}
