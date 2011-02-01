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
import com.mandelsoft.io.FileAbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.scan.FolderMandelScanner;
import com.mandelsoft.mand.scan.CompoundMandelScanner;
import com.mandelsoft.mand.scan.MandelHandle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScanner.Filter;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krüger
 */

public class Updater extends MandUtils {
  private int ccnt;
  private int ecnt;
  private int ucnt;

  public void printStat()
  {
    System.out.println("checked: "+ucnt);
    System.out.println("updated: "+ucnt);
    System.out.println("failed : "+ecnt);
  }

  //////////////////////////////////////////////////////////////////////////
  // version update
  //////////////////////////////////////////////////////////////////////////

  class UpdateInfoHandler implements Handler {
    Environment env;
    String creator;
    String site;

    public UpdateInfoHandler(Environment env)
    {
      this.env=env;
      this.creator=env.getProperty(Settings.USER);
      if (creator!=null && creator.equals("ttt Krueger")) {
        creator="Uwe Krüger";
      }
      this.site=env.getProperty(Settings.SITE);
    }

    public Environment getEnvironment()
    {
      return env;
    }


    public void handle(File f, File target)
    {
      //System.out.println("found "+f);
      try {
        MandelData md=new MandelData(f);
        MandelInfo mi=md.getInfo();
        MandelInfo old;
        if (mi==null) return;
        
        old=new MandelInfo().copyFrom(mi);
//        mi.equals(old);
//        System.out.println("  creat "+old.getCreationTime()+"->"+mi.getCreationTime());
        
        update(md);
        if (mi.needsVersionUpdate()) mi.updateData(md);
        if (mi.equals(old)) {
          System.out.println(f+" is up to date (mcnt="+mi.getMCnt()+")");
          return;
        }
        System.out.println("update info of "+f);
        System.out.println("  mcnt ("+mi.getVersion()+")"+old.getMCnt()+
                           "->"+mi.getMCnt());
        System.out.println("  cnt  ("+mi.getVersion()+")"+old.getNumIt()+
                           "->"+mi.getNumIt());
        long lm=f.lastModified();
        if (target!=null) {
          File t=new File(target,f.getName());
          try {
            md.write(t);
            t.setLastModified(lm);
          }
          catch (IOException io) {
            Command.Error("cannot write "+t);
          }
        }
        else {
          try {
            ucnt++;
            md.write();
            f.setLastModified(lm);
          }
          catch (IOException io) {
            ecnt++;
            Command.Error("cannot rewrite "+f);
          }
        }
      }
      catch (IOException ex) {
        Command.Error("cannot read "+f);
      }
    }

    public void update(MandelData data)
    {
      MandelRaster raster=data.getRaster();
      MandelInfo   info=data.getInfo();
      if (raster==null) return;
      int limit=info.getLimitIt();
      int mcnt=0;
      long cnt=0;
      int[][] r=raster.getRaster();
      int rx=raster.getRX();
      int ry=raster.getRY();
      for (int x=0; x<rx; x++) {
        for (int y=0; y<ry; y++) {
          if (r[y][x]==0) {
            mcnt++;
            cnt+=limit;
          }
          else cnt+=r[y][x];
        }
      }
      info.setMCnt(mcnt);
      if (getEnvironment()!=null) {
        if (site!=null && Utils.isEmpty(info.getSite()))
          info.setSite(site);
        //if (creator!=null && Utils.isEmpty(info.getCreator()))
          info.setCreator(creator);
      }
      //data.getInfo().setNumIt(cnt);
    }
  }

  void updateInfos(List<String> elems, MandelScanner scan, File target,
                          Environment env)
  {
    handle(elems,scan,target,new UpdateInfoHandler(env));
  }

  void updateInfos(MandelScanner scan, File target,
                          Environment env)
  {
    handle(scan,target,new UpdateInfoHandler(env));
  }

  //////////////////////////////////////////////////////////////////////////
  // version update
  //////////////////////////////////////////////////////////////////////////

  class UpdateVersionHandler implements Handler {

    public void handle(File f, File target)
    {
      //System.out.println("found "+f);
      try {
        MandelData md=new MandelData(f);
        if (!md.needsVersionUpdate()) {
          System.out.println("version of "+f+" is up to date");
          return;
        }
        System.out.println("update version of "+f);
        long lm=f.lastModified();
        if (target!=null) {
          File t=new File(target,f.getName());
          try {
            ucnt++;
            md.write(t);
            t.setLastModified(lm);
          }
          catch (IOException io) {
            ecnt++;
            Command.Error("cannot write "+t);
          }
        }
        else {
          try {
            md.write();
            f.setLastModified(lm);
          }
          catch (IOException io) {
            Command.Error("cannot rewrite "+f);
          }
        }
      }
      catch (IOException ex) {
        Command.Error("cannot read "+f);
      }
    }
  }

  void updateVersions(List<String> elems, MandelScanner scan, File target)
  {
    handle(elems,scan,target,new UpdateVersionHandler());
  }

  void updateVersions(MandelScanner scan, File target)
  {
    handle(scan,target,new UpdateVersionHandler());
  }


  //////////////////////////////////////////////////////////////////////////
  // renormalize
  //////////////////////////////////////////////////////////////////////////

  class NormalizeHandler implements Handler {
    private boolean nflag;
    private boolean rflag;

    public NormalizeHandler()
    {
    }

    public NormalizeHandler(boolean nflag, boolean rflag)
    {
      this.nflag=nflag;
      this.rflag=rflag;
    }

    public void handle(File f, File target)
    {
      // System.out.println("found "+f);
      try {
        MandelData md=new MandelData(true,f);
        MandelInfo info=md.getInfo();
        File t=MandUtils.mapToInfoFile(new FileAbstractFile(f),target);
        if (!t.exists()) {
          System.out.println("  "+f);
          if (!info.valid()) {
            System.out.println("  invalid: "+info.getMessage());
            return;
          }
          if (nflag) MandUtils.normalize(info);
          if (rflag) MandUtils.round(info);
          MandelData n=new MandelData(info);
          try {
            ucnt++;
            n.write(t);
          }
          catch (IOException io) {
            ecnt++;
            Command.Error("cannot write "+t);
          }
        }
      }
      catch (IOException ex) {
        Command.Error("cannot read "+f);
      }
    }
  }

  void normalize(boolean nflag, boolean rflag,
                        List<String> elems, MandelScanner scan, File target)
  {
    handle(elems,scan,target,new NormalizeHandler(nflag,rflag));
  }

  void normalize(boolean nflag, boolean rflag,
                        MandelScanner scan, File target)
  {
    handle(scan,target,new NormalizeHandler(nflag,rflag));
  }

  //////////////////////////////////////////////////////////////////////////
  // utilities
  //////////////////////////////////////////////////////////////////////////

  public interface Handler {
    void handle(File f, File target);
  }

  void handle(List<String> elems, MandelScanner scan, File target,
                      Handler handler)
  {
    for (String e:elems) {
      //System.out.println("handle "+e);
      File f=new File(e);
      if (f.exists()) handler.handle(f,target);
      else {
        MandelName name=MandelName.create(e);

        if (name!=null) {
          for (MandelHandle h:scan.getMandelHandles(name)) {
            if (h.getFile().isFile()) {
              ccnt++;
              handler.handle(h.getFile().getFile(),target);
            }
          }
        }

        ColormapName cn=new ColormapName(f.getName());
        for (ColormapHandle h:scan.getColormapHandles(cn)) {
          if (h.getFile().isFile()) {
            ccnt++;
            handler.handle(h.getFile().getFile(),target);
          }
        }
      }
    }
  }

  void handle(MandelScanner scan, File target,
                     Handler handler)
  {
    // System.out.println("handle scanner");
    for (MandelName name:scan.getMandelNames()) {
     // System.out.println("handle mandel "+name);
      for (MandelHandle h:scan.getMandelHandles(name)) {
        if (h.getFile().isFile()) {
          ccnt++;
          handler.handle(h.getFile().getFile(),target);
        }
      }
    }
    for (ColormapName name:scan.getColormapNames()) {
      // System.out.println("handle colormap "+name);
      for (ColormapHandle h:scan.getColormapHandles(name)) {
        if (h.getFile().isFile()) {
          ccnt++;
          handler.handle(h.getFile().getFile(),target);
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////////


  static public class UpdateFilter implements Filter {
    private boolean Aflag=false;  // all files
    private boolean Dflag=false;  // data files
    private boolean Rflag=false;  // raster files
    private boolean Iflag=false;  // image files
    private boolean Cflag=false;  // colormap files

    public boolean filter(MandelHeader h)
    {
      //System.out.println("   filter "+h.getFile());
      if (Aflag) return true;
      if (Cflag && h.isColormap()) return true;
      if (Iflag && h.isPlainImage()) return true;
      if (Rflag && h.hasRaster()) return true;
      if (Dflag && h.isInfo()) return true;
      return false;
    }

    public void setCflag(boolean Cflag)
    {
      this.Cflag=Cflag;
    }

    public void setDflag(boolean Dflag)
    {
      this.Dflag=Dflag;
    }

    public void setIflag(boolean Iflag)
    {
      this.Iflag=Iflag;
    }

    public void setRflag(boolean Rflag)
    {
      this.Rflag=Rflag;
    }

    public void setAflag(boolean Aflag)
    {
      this.Aflag=Aflag;
    }

  }

  static public void main(String[] args)
  { int c=0;
    File targetdir=null;
    Updater upd=new Updater();

    boolean vflag=false;  // update version
    boolean nflag=false;  // normalize
    boolean rflag=false;  // round
    boolean iflag=false;  // update info

    CompoundMandelScanner sourcedirs=new CompoundMandelScanner();
    UpdateFilter filter=new UpdateFilter();

    List<String> elems=new ArrayList<String>();

    while (c<args.length && args[c].startsWith("-")) {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        switch (arg.charAt(i)) {
          case 'n':
            nflag=true;
            break;
          case 'r':
            rflag=true;
            break;
          case 'v':
            vflag=true;
            break;
          case 'i':
            iflag=true;
            break;

          case 'D':
            filter.setDflag(true);
            break;
          case 'R':
            filter.setRflag(true);
            break;
          case 'I':
            filter.setIflag(true);
            break;
          case 'C':
            filter.setCflag(true);
            break;
          case 'M':
            filter.setDflag(true);
            filter.setIflag(true);
            filter.setRflag(true);
            break;
          case 'A':
            filter.setAflag(true);
            break;


          case 't':
            if (c<args.length) {
              targetdir=new File(args[c++]);
            }
            else Command.Error("target directory missing");
            break;
          case 'd':
            if (c<args.length) {
              try {
                sourcedirs.addScanner(new FolderMandelScanner(
                        new File(args[c++]),
                                                              null, false));
              }
              catch (IOException ex) {
                Command.Error("illegal source directory: "+ex);
              }
            }
            else Command.Error("source directory missing");
            break;
        }
      }
    }

    try {
      Environment env=null;
      if (!sourcedirs.hasScanners()) {
        env=new Environment(null);
        sourcedirs.addScanner(env.getAllScanner());
        sourcedirs.addScanner(env.getColormapScanner());
      }
      sourcedirs.setFilter(filter);

      while (c<args.length) {
        elems.add(args[c++]);
      }

      if (vflag&&nflag) {
        Command.Error("only -v or -n may be set");
      }
      if (iflag) {
        System.out.println("Update infos...");
        if (!elems.isEmpty()) upd.updateInfos(elems, sourcedirs, targetdir, env);
        else upd.updateInfos(sourcedirs, targetdir, env);
      }
      if (vflag) {
        System.out.println("Update Versions...");
        if (!elems.isEmpty()) upd.updateVersions(elems, sourcedirs, targetdir);
        else upd.updateVersions(sourcedirs, targetdir);
      }
      if (nflag||rflag) {
        System.out.println("Normalize Areas...");
        if (!elems.isEmpty()) upd.normalize(nflag, rflag, elems, sourcedirs,
                                        targetdir);
        else upd.normalize(nflag, rflag, sourcedirs, targetdir);
      }

      upd.printStat();
    }
    catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: "+ex);
    }
  }
}
