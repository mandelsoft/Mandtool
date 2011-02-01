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

package com.mandelsoft.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 *
 * @author Uwe Krüger
 */
public class FolderLock {
  static public boolean debug=false;

  private File folder;
  private File file;
  private FileLock lock;
  private int count=0;

  public FolderLock(File f)
  {
    super();
    while (!f.isDirectory()) f=f.getAbsoluteFile().getParentFile();
    try {
      folder=f.getCanonicalFile();
    }
    catch (IOException ex) {
      folder=f;
    }
    file=new File(f, ".lock");
    if (!file.exists()) {
      // System.out.println("creating lock file "+file);
      FileOutputStream fos=null;
      try {
        fos=new FileOutputStream(file);
      }
      catch (FileNotFoundException ex) {
        System.err.println("cannot create lock file "+file+": "+ex);
      }
      finally {
        try {
          fos.close();
        }
        catch (IOException ex) {
        }
      }
    }
  }

  public File getFolder()
  {
    return folder;
  }

  public synchronized boolean lock()
  {
    if (count>0) {
      count++;
      return true;
    }
    try {
      RandomAccessFile ra=new RandomAccessFile(file, "rw");
      lock=ra.getChannel().lock();
      count++;
      if (debug) System.out.println("%%% got folder lock "+file);
      return true;
    }
    catch (IOException ex) {
      return false;
    }
  }

  public synchronized void releaseLock()
  {
    if (count<=0) return;
    count--;
    if (count>0) return;
    if (lock!=null) {
      try {
        lock.release();
        lock.channel().close();
        lock=null;
        if (debug) System.out.println("%%% released folder lock "+file);
      }
      catch (IOException ex) {
        if (debug) System.out.println("%%% released folder lock "+file+" failed: "+ex);
      }
    }
  }
}
