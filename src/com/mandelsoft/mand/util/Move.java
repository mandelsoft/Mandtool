
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
package com.mandelsoft.mand.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.scan.FolderMandelScanner;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krüger
 */

public class Move {
  private MandelScanner scan;
  private File target;
  private boolean move;
  private boolean verbose;
  
  public Move(MandelScanner scan, File target, boolean move)
  {
    this.scan=scan;
    this.target=target;
    this.move=move;
  }

  public void setVerbose(boolean verbose)
  {
    this.verbose=verbose;
  }

  public void move(MandelName src, MandelName dst)
  {
    if (src.isLocalName() || dst.isRemoteName()) {
      throw new IllegalArgumentException("remote name not supported");
    }
    String sp=src.getEffective();
    String dp=dst.getEffective();
    handle(sp,dp,src);
  }

  private void copy(File src, File dst)
  {
    int n;
    byte[] b=new byte[1024];
    try {
      BufferedInputStream is=new BufferedInputStream(new FileInputStream(src));
      try {
        BufferedOutputStream os=new BufferedOutputStream(new FileOutputStream(
                dst));
        try {
          while ((n=is.read(b))>0) {
            os.write(b, 0, n);
          }
        }
        finally {
          os.close();
        }
        dst.setLastModified(src.lastModified());
      }
      finally {
        is.close();
      }
    }
    catch (IOException io) {
      System.out.println("cannot copy "+src+": "+io);
    }
  }

  private void handle(String sp, String dp, MandelName src)
  {
    Set<MandelHandle> set=scan.getMandelHandles(src);
    if (set.isEmpty()) return;
    String eff=src.getEffective();
    eff=dp+eff.substring(sp.length());
    MandelName dst=MandelName.create(eff);
    for (MandelHandle h:set) {
      if (h.getFile().isFile()) {
        MandelFileName n=MandelFileName.create(h.getFile());
        n=n.get(dst,true);
        System.out.println((move?"move":"copy")+" "+h.getFile()+" to "+n);
        if (!verbose) {
          if (target==null) {
            if (move) {
              h.getFile().getFile().
                 renameTo(new File(h.getFile().getFile().getParentFile(),
                          n.toString()));
            }
            else {
              copy(h.getFile().getFile(),
                 new File(h.getFile().getFile().getParentFile(), n.toString()));

            }
          }
          else {
            copy(h.getFile().getFile(), new File(target, n.toString()));
            if (move) h.getFile().getFile().delete();
          }
        }
      }
    }
    src=src.sub();
    while (src!=null) {
      handle(sp,dp,src);
      src=src.next();
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    try {
      String src="F://Mandel2/raster";
      FolderMandelScanner scan=new FolderMandelScanner(new File(src));
      Move m=new Move(scan, new File("."),false);
      m.setVerbose(true);
      m.move(MandelName.create("babc"), MandelName.create("babx"));
    }
    catch (IOException ex) {
      System.out.println("failed; "+ex);
    }
  }
}
