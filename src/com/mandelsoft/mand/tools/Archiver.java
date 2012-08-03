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
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.DefaultColormapHandle;
import com.mandelsoft.mand.scan.DefaultMandelHandle;
import com.mandelsoft.mand.scan.ElementHandle;
import com.mandelsoft.mand.scan.MandelInventory;
import com.mandelsoft.mand.util.DefaultMandelListFolderTree;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.util.UnclosableInputStream;
import com.mandelsoft.util.UnclosableOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Uwe Krueger
 */
public class Archiver extends Sync {

  public Archiver(Environment src, Environment dst, int types)
  {
    super(src, dst, types);
  }

  public Archiver(Environment src, Environment dst)
  {
    super(src, dst);
  }

  ////////////////////////////////////////////////////////////////////////
  // Writer
  ////////////////////////////////////////////////////////////////////////
  public static class ZipOS extends UnclosableOutputStream {

    public ZipOS(ZipOutputStream zos)
    {
      super(zos);
    }
  }

  public class ArchiveWriter implements SyncHandler {

    private File zipfile;
    private MandelInventory inv=new MandelInventory();
    private List<ElementHandle> list=new ArrayList<ElementHandle>();
    private Map<String, MandelList> lists=new HashMap<String, MandelList>();
    private Map<String, MandelListFolderTree> trees=new HashMap<String, MandelListFolderTree>();
    private ZipOutputStream zip;

    public ArchiveWriter(File zipfile)
    {
      this.zipfile=zipfile;
    }

    public void copy(ElementHandle src, AbstractFile dst)
    {
      AbstractFile mf=src.getFile();
      String name=mf.getName();
      list.add(src);
      inv.add(name, src.getHeader().getType()|MandelData.M_INFOOMITTED, null, mf.
              getLastModified());
    }

    public void backupInfoFile(AbstractFile mf)
    {
    }

    public void backupRasterFile(AbstractFile mf)
    {
    }

    public void backupAreaColormapFile(AbstractFile mf)
    {
    }

    public void syncList(MandelList ml, MandelList dl, String name)
    {
      lists.put(name, ml);
    }

    public Set<QualifiedMandelName> syncTree(MandelListFolderTree ml,
                                             MandelListFolderTree dl,
                                             String name)
    {
      trees.put(name, ml);
      return new HashSet<QualifiedMandelName>();
    }

    public void finish()
    {
      try {
        System.out.println("creating archive "+zipfile);
        zip=new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(zipfile)));
        zip.setMethod(ZipOutputStream.DEFLATED);

        try {
          ZipEntry e=new ZipEntry("inventory");
          zip.putNextEntry(e);
          inv.write(new ZipOS(zip), "inventory");
          zip.closeEntry();

          for (ElementHandle h :list) {
            System.out.println("writing "+h.getFile().getName());
            e=new ZipEntry("mandel/"+h.getFile().getName());
            e.setTime(h.getFile().getLastModified());
            zip.putNextEntry(e);
            putData(h.getFile().getInputStream());
            zip.closeEntry();
          }
          for (String n :lists.keySet()) {
            System.out.println("writing list "+n);
            e=new ZipEntry("lists/"+n);
            zip.putNextEntry(e);
            MandelList ml=lists.get(n);
            ml.write(new ZipOS(zip),n);
            zip.closeEntry();
          }
          for (String n :trees.keySet()) {
            if (n.equals("news")) continue;
            System.out.println("writing tree "+n);
            e=new ZipEntry("trees/"+n);
            zip.putNextEntry(e);
            MandelListFolderTree mt=trees.get(n);
            mt.write(new ZipOS(zip),n);
            zip.closeEntry();
          }
        }
        catch (IOException io) {
          System.err.println(""+io);
          zip.close();
          System.exit(1);
        }
        finally {
          zip.close();
        }
      }
      catch (IOException io) {
        System.err.println(""+io);
        System.exit(1);
      }

    }

    private void putData(InputStream inp) throws IOException
    {
      int n;
      BufferedInputStream is=new BufferedInputStream(inp);

      try {
        while ((n=is.read(buffer))>0) {
          zip.write(buffer, 0, n);
        }
      }
      finally {
        is.close();
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Reader
  protected void read(File arch) throws IOException
  {
    ZipEntry e;
    int ix;
    String name;
    String folder;
    QualifiedMandelName qmn;
    List<QualifiedMandelName> updates=new ArrayList<QualifiedMandelName>();
    Set<QualifiedMandelName>  favorites=new HashSet<QualifiedMandelName>();
    MandelInventory inv=new MandelInventory();
    ZipInputStream zis=new ZipInputStream(new FileInputStream(arch));
    try {
      e=zis.getNextEntry();
      if (e.getName().equals("inventory")) {
        inv.read(new UnclosableInputStream(zis), "inventory");
        while ((e=zis.getNextEntry())!=null) {
          if (e.isDirectory()) continue;
          name=e.getName();
          ix=name.indexOf('/');
          if (ix>0) {
            folder=name.substring(0, ix);
            name=name.substring(ix+1);
            if (folder.equals("mandel")) {
              MandelInventory.Entry me=inv.get(name);
              if (me!=null) {
                AbstractFile file=new ZipMandelFile(e, zis);
                MandelHeader h=new MandelHeader(me.getType());
                ElementHandle eh;
                if (h.isColormap()) {
                  eh=new DefaultColormapHandle(file,ColormapName.create(file),h);
                }
                else {
                  eh=new DefaultMandelHandle(file,QualifiedMandelName.create(file),h);
                }
                boolean found=false;
                for (FileType t :types) {
                  if (t.match(h)) {
                    found=true;
                    if (handleFile(t, eh)) {
                      if (h.hasInfo()) {
                        qmn=(QualifiedMandelName)eh.getName();
                        if (qmn!=null) updates.add(qmn);
                      }
                    }
                  }
                }
                if (!found) {
                  System.err.println("ignoring "+name);
                }
              }
              else {
                System.out.println("unexpected file "+e.getName());
              }
            }
            else {
              // handle lists
              if (folder.equals("lists")||folder.equals("trees")) {
                MandelListFolderTree mt=new DefaultMandelListFolderTree(name);
                try {
                  mt.read(new UnclosableInputStream(zis), e.getName());
                  MandelListFolderTree dt=findTree(name);
                  if (dt!=null) {
                    if (name.equals("favorites")) {
                      favorites=exec.syncTree(mt, dt, e.getName());
                      if (favorites!=null) {
                        System.out.println("found "+favorites.size()+" favorites");
                      }
                      else {
                        System.out.println("no new favorites");
                      }
                    }
                    else {
                      exec.syncTree(mt, dt, e.getName());
                    }
                  }
                  else {
                    MandelList dl=findList(name);
                    if (dl!=null) {
                      exec.syncList(mt.getRoot().getMandelList(), dl,
                                    e.getName());
                    }
                    else
                      System.out.println("ignoring ("+folder+") "+e.getName());
                  }
                }
                catch (IOException io) {
                  System.out.println("cannot read list "+e.getName()+": "+io);
                }
              }
              else {
                System.out.println("ignoring unexpected entry "+e.getName());
              }
            }
          }
        }
      }
      else {
        throw new IOException("inventory not found");
      }

      if (!updates.isEmpty()) {
        for (MandelListFolderTree n :dst.getUserLists()) {
          if (n.getRoot().getName().equals("news")) {
            DateFormat fmt=new SimpleDateFormat("yyyyMMdd-HHmmss");
            MandelListFolder uf=n.getRoot().createSubFolder(fmt.format(
                    new Date()));
            MandelListFolder af=uf.createSubFolder("new");
            af.getMandelList().addAll(updates);
            if (favorites!=null&&!favorites.isEmpty()) {
              MandelListFolder ff=uf.createSubFolder("favorites");
              ff.getMandelList().addAll(favorites);
            }
            try {
              n.save();
            }
            catch (IOException w) {
              System.out.println("cannot write news: "+w);
            }
          }
        }
      }

      exec.finish();
    }
    finally {
      zis.close();
    }
  }

  ////////////////////////////////////////////////////////////////////////
  protected MandelListFolderTree findTree(String name)
  {
    if (name.equals("favorites")) return dst.getFavorites();
    if (name.equals("todos")) return dst.getTodos();
    for (MandelListFolderTree t :dst.getUserLists()) {
      if (t.getRoot().getName().equals(name))  return t;
    }
    return null;
  }

  protected MandelList findList(String name)
  {
    if (name.equals("seen"))  return dst.getSeenRasters();
    if (name.equals("areas")) return dst.getAreas();
    return null;
  }

  ////////////////////////////////////////////////////////////////////////
  static public class ZipMandelFile implements AbstractFile {

    ZipEntry e;
    ZipInputStream zis;

    public ZipMandelFile(ZipEntry e, ZipInputStream zis)
    {
      this.e=e;
      this.zis=zis;
    }

    public boolean isFile()
    {
      return false;
    }

    public Proxy getProxy()
    {
      return null;
    }

    public InputStream getInputStream() throws IOException
    {
      try {
        return new UnclosableInputStream(zis);
      }
      finally {
        zis=null;
      }
    }

    public long getLastModified()
    {
      return e.getTime();
    }

    public URL getURL()
    {
      return null;
    }

    public File getFile()
    {
      return null;
    }

    public String getPath()
    {
      return e.getName();
    }

    public String getName()
    {
      int ix=e.getName().lastIndexOf('/');
      if (ix>=0) return e.getName().substring(ix+1);
      return e.getName();
    }

    public boolean lock() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean tryLock() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void releaseLock() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public AbstractFile getParent()
    {
      return null;
    }

    public AbstractFile getSub(String name)
    {
      return null;
    }

    @Override
    public String toString()
    {
      return getPath();
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Main
  //////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
    int c=0;
    String arg;
    boolean vflag=false;
    boolean wflag=false;
    boolean rflag=false;
    boolean cflag=false;
    File src=new File("C:/work/AccuRev/test/Mandel");
    File dst=new File("C:/Tomcat/apache-tomcat-6.0.29/webapps/mandel/mandel");
    File arch=new File("mandel.zip");
    String sn=null;
    String dn=null;

    if (args.length>0 && args[0].equals("help")) {
      System.out.println("MandelDB Delta Archiver");
      System.out.println("  <cmd> <options> <args>");
      System.out.println("    -a <achivename>  set the name of the used archive");
      System.out.println("    -v               show wht would be done");
      System.out.println("  Archive Writer:  <cmd> -w <options> <src> <dst>");
      System.out.println("    -w               write an archive");
      System.out.println("  Archive Reader:  <cmd> -r <options> <dst>");
      System.out.println("    -r               read an archive");
      System.out.println("    -c               copy mandel list instead of add content");
      return;
    }
    while (args.length>c&&args[c].charAt(0)=='-') {
      //System.out.println("arg: "+args[arg]);
      arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt=arg.charAt(i);
        //System.out.println("option: "+opt);
        switch (opt) {
          case 'v':
            vflag=true;
            break;
          case 'w':
            if (rflag) Error("only read or write allowed");
            wflag=true;
            break;
          case 'r':
            if (wflag) Error("only read or write allowed");
            rflag=true;
            break;
          case 'c':
            cflag=true;
            break;
          case 'a':
            if (args.length>c) {
              arch=new File(args[c++]);
            }
            else Error("archive name missing");
            break;
          default:
            Error("illegal option '"+opt+"'");
        }
      }
      //System.out.println("done");
    }

    if (wflag==rflag) {
      rflag=vflag=true;
    }

    Archiver a;
    int types;

    if (wflag) {
      try {
        if (args.length>c) src=new File(args[c++]);
        if (args.length>c) dst=new File(args[c++]);
        else Error("mar: [-v] [-a <archive>] -w <srcroot> <dstroot>");
        types=parseTypes(c, args);
        System.out.println("create update archive from "+src+" to "+dst);
        if (!src.isDirectory()) Error(src+" is no directory");
        if (!dst.isDirectory()) Error(dst+" is no directory");
        Environment env_src=new Environment("mandtool", null, src);
        Environment env_dst=new Environment("mandtool", null, dst);
        a=new Archiver(env_src, env_dst, types);
        if (vflag) {
          a.setExecutionHandler(a.new VerboseSyncHandler());
        }
        else {
          a.setExecutionHandler(a.new ArchiveWriter(arch));
        }
        a.execute();
        System.out.println(""+a.count+" copied");
      }
      catch (IllegalConfigurationException ex) {
        ex.printStackTrace(System.err);
        Error("error reading config: "+ex);
      }
    }
    else if (rflag) {
      try {
        if (args.length>c) dst=new File(args[c++]);
        else Error("mar: [-v] [-a <archive>] -r <dstroot>");
        types=parseTypes(c, args);
        System.out.println("extract update archive to "+dst);
        if (!dst.isDirectory()) Error(dst+" is no directory");
        Environment env_dst=new Environment("mandtool", null, dst);
        a=new Archiver(null, env_dst, types);
        a.setListCopyMode(cflag);
        if (vflag) {
          a.setExecutionHandler(a.new VerboseSyncHandler());
        }
        else {
          a.setExecutionHandler(a.new DefaultSyncHandler());
        }
        try {
          a.read(arch);
          System.out.println(""+a.count+" copied");
        }
        catch (IOException ex) {
          ex.printStackTrace(System.err);
          Error("error reading archive: "+ex);
        }
      }
      catch (IllegalConfigurationException ex) {
        ex.printStackTrace(System.err);
        Error("error reading config: "+ex);
      }
    }
  }
}
