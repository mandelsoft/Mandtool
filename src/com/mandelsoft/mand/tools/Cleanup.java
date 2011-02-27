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
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.FilteredMandelScanner;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class Cleanup extends Cmd {
  List<String> log=new ArrayList<String>();
  Environment env;

  public Cleanup(Environment env)
  {
    this.env=env;
  }

  void log(String msg)
  {
    log.add(msg);
    System.out.println("*** "+msg);
  }

  void print()
  {
    System.out.println("");
    System.out.println("*** Action summary ***");
    for (String msg:log) {
      System.out.println("  "+msg);
    }
  }

  void doit(boolean doit)
  {
    MandelScanner scanner=env.getInfoScanner();
    MandelScanner ref=env.getImageDataScanner();
    Set<QualifiedMandelName> refset=ref.getQualifiedMandelNames();

    // check for obsolete requests
    for (MandelHandle h:scanner.getMandelHandles()) {
      QualifiedMandelName n=h.getName();
      if (refset.contains(n)) {
        boolean found=false;
        try {
          MandelData info=h.getInfo();
          Set<MandelHandle> set=ref.getMandelHandles(n);
          for (MandelHandle i:set) {
            try {
              MandelData data=i.getInfo();
              if (data.getInfo().isSameSpec(info.getInfo())) {
                log("found data for request "
                  +h.getFile()+": "+i.getFile());
                found=true;
              }
            }
            catch (IOException ex) {
              System.out.println("cannot read "+i.getFile()+": "+ex);
            }
          }
        }
        catch (IOException ex) {
          System.out.println("cannot read "+h.getFile()+": "+ex);
        }
        if (found) {
          log("obsolete request for "+h.getFile());
          if (doit) env.backupInfoFile(h.getFile());
        }
        else {
          log("refinement request for "+h.getFile());
        }
      }
    }

    // check for avaialble refinements
    for (QualifiedMandelName n:refset) {
      MandelData best=null;
      Set<MandelHandle> set=ref.getMandelHandles(n);
      if (set.size()>1) for (MandelHandle i:set) {
        try {
          MandelData data=i.getInfo();
          log("found refinement: "+data.getInfo().getLimitIt()+": "+i.getFile());
          if (best==null) best=data;
          else {
            if (data.getInfo().getLimitIt()>best.getInfo().getLimitIt()) {
              if (doit) {
                File file=best.getFile().getFile();
                MandelFolder folder=MandelFolder.getMandelFolder(file);
                folder.remove(file);
              }
              best=data;
            }
          }
        }
        catch (IOException ex) {
          System.out.println("cannot read "+i.getFile()+": "+ex);
        }
      }
      if (best!=null) {
        log("found refined replacement "+best.getFile());
      }
      // TODO: handle seen of best if seen?
    }

    // check for obsolete raster data
    scanner=new FilteredMandelScanner(env.getImageDataScanner(),
                                      MandelScanner.IS_RASTER);
    ref=env.getRasterImageScanner();
    refset=ref.getQualifiedMandelNames();
    for (MandelHandle h:scanner.getMandelHandles()) {
      QualifiedMandelName n=h.getName();
      if (refset.contains(n)) {
        log("obsolete raster data"+h.getFile());
        if (doit) env.backupRasterFile(h.getFile());
      }
    }
  }

  static public void main(String[] args)
  {
    boolean doit=false;
    String root=".";
    if (args.length>0) {
      root=args[0];
    }
    try {
      Environment env=new Environment("mandtool", null, new File(root));
      Cleanup c=new Cleanup(env);
      c.doit(doit);
      c.print();
    }
    catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: "+ex);
    }
  }
}
