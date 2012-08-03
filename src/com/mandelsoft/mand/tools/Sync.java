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
import java.io.File;
import java.io.IOException;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.MandelConstants;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.scan.ElementHandle;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.mand.tools.Sync.SyncHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Uwe Krüger
 */

public class Sync extends Copy<SyncHandler> {
  static public final int IMAGES = 1;
  static public final int REQUESTS = 2;
  static public final int VARIANTS = 4;
  static public final int AREACOLMAPS = 8;

  static public final int COLORMAPS = 16;

  static public final int MANDEL = IMAGES|REQUESTS|VARIANTS|AREACOLMAPS;
  static public final int ALL = MANDEL|COLORMAPS;

  //////////////////////////////////////////////////////////////////////

  public interface SyncHandler extends ExecutionHandler {
    void syncList(MandelList ml, MandelList dl, String name);
    Set<QualifiedMandelName> syncTree(MandelListFolderTree ml, MandelListFolderTree dl, String name);
  }

  public class DefaultSyncHandler extends DefaultHandler
                                  implements SyncHandler {
    public void syncList(MandelList ml, MandelList dl, String msg)
    {
      System.out.println("syncing list "+msg+"...");
      if (dl!=null&&ml!=null) {
        if (listCopyMode) dl.clear();
        dl.addAll(ml);
        try {
          dl.save();
        }
        catch (IOException ex) {
          Warning("cannot write "+msg+": "+ex);
        }
      }
    }

    public Set<QualifiedMandelName> syncTree(MandelListFolderTree ml,
                                             MandelListFolderTree dl,
                                             String msg)
    {
      Set<QualifiedMandelName> set=new HashSet<QualifiedMandelName>();
      Set<QualifiedMandelName> old=new HashSet<QualifiedMandelName>();
      System.out.println("syncing tree "+msg+"...");
      for (QualifiedMandelName q:dl.getRoot().allentries()) {
        old.add(q);
      }
      if (dl!=null&&ml!=null) {
        sync(" ", ml.getRoot(), dl.getRoot(), set, old);
        try {
          dl.save();
        }
        catch (IOException ex) {
          Warning("cannot write "+msg+": "+ex);
        }
      }
      return set;
    }

    private void sync(String gap, MandelListFolder s, MandelListFolder d,
                                  Set<QualifiedMandelName> set,
                                  Set<QualifiedMandelName> old)
    { String ngap=gap+"  ";
      System.out.println(gap+"syncing folder "+s.getPath());
      List<QualifiedMandelName> list=new ArrayList<QualifiedMandelName>();
      MandelList l=s.getMandelList();
      if (!listCopyMode && l!=null) for (QualifiedMandelName n:l) {
        list.add(n);
        if (!old.contains(n)) {
          set.add(n);
          System.out.println(ngap+"adding "+n);
        }
        else {
          //System.out.println(ngap+"keeping "+n);
        }
      }
      l=d.getMandelList();
      if (l!=null) {
        for (QualifiedMandelName n:d.getMandelList()) {
          if (!list.contains(n)) {
            list.add(n);
          }
        }
        l.clear();
        l.addAll(list);
      }

      // sync sub folders
      for (MandelListFolder f :s) {
        MandelListFolder n=d.getSubFolder(f.getName());
        if (n==null) n=d.createSubFolder(f.getName());
        sync(ngap,f, n, set, old);
      }
      if (listCopyMode) {
        for (MandelListFolder f: d) {
           MandelListFolder n=s.getSubFolder(f.getName());
           if (n==null) d.remove(f);
        }
      }
    }
  }

  public class VerboseSyncHandler extends VerboseHandler
                                  implements SyncHandler {

    public void syncList(MandelList ml, MandelList dl, String name)
    {
      System.out.println("syncing list "+name);
    }

    public Set<QualifiedMandelName> syncTree(MandelListFolderTree ml,
                                             MandelListFolderTree dl,
                                             String name)
    {
      System.out.println("syncing tree "+name);
      return new HashSet<QualifiedMandelName>();
    }

  }
    
  //////////////////////////////////////////////////////////////////////
  // additional file types

  private class ColormapType extends FileType {
    ColormapType()
    {
      super("colormaps", MandelScanner.COLORMAP,
            src==null?null:src.getColormapScanner(), dst.getColormapScanner());
      handlePath(Settings.COLORMAP_SAVE_PATH);
    }

    @Override
    protected boolean has(ElementHandle h)
    {
      String n=h.getFile().getName();
      if (n.endsWith(MandelConstants.COLORMAP_SUFFIX)) n=n.substring(0,n.length()-3);
      return !dstscan.getColormapHandles(new ColormapName(n)).isEmpty();
    }

    @Override
    protected boolean use(ElementHandle h)
    {
      return true;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // Sync class

  protected int typecodes;
  protected boolean listCopyMode=false;

  public Sync(Environment src, Environment dst)
  {
    this(src,dst,ALL);
  }

  public Sync(Environment src, Environment dst, int types)
  {
    super(src,dst);
    this.typecodes=types;
    this.types.clear();
    if ((types&REQUESTS)!=0) {
      this.types.add(new InfoType());
    }
    if ((types&IMAGES)!=0) {
      this.types.add(new AreaColmapType());
      this.types.add(new RasterType());
      this.types.add(new RasterImageType());
    }
    if ((types&COLORMAPS)!=0) {
      this.types.add(new ColormapType());
    }
  }

  public void setListCopyMode(boolean listCopyMode)
  {
    this.listCopyMode=listCopyMode;
  }

  //////////////////////////////////////////////////////////////////////
  @Override
  protected String map(AbstractFile s)
  {
    return s.getName();
  }

  @Override
  protected boolean handleFile(FileType ft, ElementHandle h)
  {
    boolean v;
    AbstractFile mf=h.getFile();
    if (mf.getName().startsWith("x")) {
      System.out.println("  "+mf);
      v=true;
    }
    else v=false;
    //v=true;
    //System.out.println("checking "+mf);
    if ((typecodes&VARIANTS)==0 && h.getHeader().hasInfo()) {
      if (((MandelHandle)h).getQualifier()!=null) return false;
    }

    if (!ft.has(h)) {
      if (ft.use(h)) {
        return ft.handle(h);
      }
      else {
        if (v) System.out.println("    skipped");
      }
    }
    else {
      if (v) {
        System.out.println("    already exists "+mf);
      }
    }
    return false;
  }

  ///////////////////////////////////////////////////////////////////////////

  @Override
  protected void additionalSteps()
  { 
    if (exec.getClass()!=VerboseSyncHandler.class) {
      exec.syncTree(src.getFavorites(),dst.getFavorites(),"favorites");
      exec.syncTree(src.getTodos(),dst.getTodos(),"todos");
      exec.syncTree(src.getLinks(),dst.getLinks(),"links");
      exec.syncList(src.getSeenRasters(),dst.getSeenRasters(),"seen");
      exec.syncList(src.getAreas(),dst.getAreas(),"areas");

      for (MandelListFolderTree t:src.getUserLists()) {
        if (t.getRoot().getName().equals("news")) continue;
        for (MandelListFolderTree d:dst.getUserLists()) {
          if (t.getRoot().getName().equals(d.getRoot().getName())) {
            exec.syncTree(t,d,t.getRoot().getName());
          }
        }
      }
    }
  }
  
  ///////////////////////////////////////////////////////////////////////////

  protected static int parseTypes(int c, String[] args)
  { int types=ALL;

    if (args.length>c) {
      if (args[c].startsWith("-")) {
        types=ALL;
      }
      else {
        types = 0;
      }
      while (args.length>c) {
        String t=args[c++];
        boolean sub=false;
        int type=0;

        if (t.startsWith("-")) {
          sub=true;
          t=t.substring(1);
        }
        else {
          if (t.startsWith("+")) {
            sub=false;
            t=t.substring(1);
          }
        }
        if (t.equals("requests")) {
          type=REQUESTS;
        }
        else if(t.equals("images")) {
          type=IMAGES;
        }
        else if (t.equals("mandel")) {
          type=MANDEL;
        }
        else if(t.equals("variants")) {
          type=VARIANTS;
        }
        else if (t.equals("colormaps")) {
          type=COLORMAPS;
        }
        else if (t.equals("all")) {
          type=ALL;
        }
        else if (t.equals("none")) {
          type=ALL;
          sub=true;
        }
        else {
          Error("illegal mode '"+t+"'");
        }
        if (sub) types&=~type;
        else     types|=type;
      }
    }
    return types;
  }

  public static void main(String[] args)
  { try {
      int c=0;
      boolean vflag=false;
      boolean cflag=false;
      File src=new File("F://Mandel2");
      File dst=new File("F://Mandel");
      int types;
      while (args.length>c&&args[c].charAt(0)=='-') {
        for (int i=1; i<args[c].length();
             i++) {
          char opt;
          switch (opt=args[c].charAt(i)) {
            case 'v':
              vflag=true;
              break;
            case 'c':
              cflag=true;
              break;
            default:
              Error("illegal option '"+opt+"'");
          }
        }
        c++;
      }
      if (args.length>c) src=new File(args[c++]);
      if (args.length>c) dst=new File(args[c++]);
      types=parseTypes(c, args);
      System.out.println("syncing from "+src+" to "+dst+" (types="+types+")");
      if (!src.isDirectory()) Error(src+" is no directory");
      if (!dst.isDirectory()) Error(dst+" is no directory");
      Environment env_src=new Environment("mandtool", null, src);
      Environment env_dst=new Environment("mandtool", null, dst);
      Sync a=new Sync(env_src, env_dst, types);
      a.setListCopyMode(cflag);
      if (vflag) {
        a.setExecutionHandler(a.new VerboseSyncHandler());
      }
      else {
        a.setExecutionHandler(a.new DefaultSyncHandler());
      }
      a.execute();
      System.out.println(""+a.count+" copied");
    }
    catch (IllegalConfigurationException ex) {
      Error("illegal config: "+ex);
    }
  }
}
