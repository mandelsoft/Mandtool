
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Uwe Krüger
 */

public class SyncFiles {
  static public void copyFile(File src, File dst) throws IOException
  {
    BufferedInputStream bis=new BufferedInputStream(new FileInputStream(src));
    try {
      BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(dst));
      try {
        byte[] buf=new byte[8192];
        int len;
        while ((len=bis.read(buf))>0) bos.write(buf, 0, len);
      }
      finally {
        bos.close();
        dst.setLastModified(src.lastModified());
      }
    }
    finally {
      bis.close();
    }
  }

  static public void sync(File dst, File src, boolean verbose)
                          throws IOException
  {
    if (dst.exists()) {
      if (dst.isDirectory()!=src.isDirectory())
        throw new IOException("file type mismatch: "+src+","+dst);
      if (src.isFile() && src.lastModified()!=dst.lastModified()) {
        System.out.println("copy "+src+" to "+dst);
        if (!verbose) {
          copyFile(src,dst);
        }
      }
      else {
        System.out.println("skipping "+src);
      }
    }
    else {
      if (src.isDirectory()) {
        System.out.println("creating "+dst);
        if (!verbose) {
          dst.mkdirs();
        }
      }
      else {
        System.out.println("copy "+src+" to "+dst);
        if (!verbose) {
          copyFile(src,dst);
        }
      }
    }
    if (src.isDirectory()) {
      File[] list=src.listFiles();
      if (list!=null) for (File n:list) {
        sync(new File(dst,n.getName()),n,verbose);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////////

  static public void main(String[] args)
  {
    boolean vflag=false;
    int c=0;
    while (c<args.length) {
      if (args[c].equals("-v")) {
        vflag=true;
        c++;
      }
      else break;
    }
    if (args.length-c!=2) {
      System.err.println("destination and source folder required.");
      System.exit(1);
    }
    try {
      File dst=new File(args[c+0]);
      File src=new File(args[c+1]);
      System.out.println("synching "+dst.getAbsolutePath()+" from "+
                                     src.getAbsolutePath());
      //throw new IOException("test");
      sync(dst,src,vflag);
    }
    catch (IOException io) {
      System.err.println(""+io);
    }
  }
}
