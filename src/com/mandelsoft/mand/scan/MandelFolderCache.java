
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
package com.mandelsoft.mand.scan;

import com.mandelsoft.io.FileAbstractFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelName;

/**
 *
 * @author Uwe Krüger
 */
public class MandelFolderCache extends MandelInventory {
  static public final String cachefile=".mcache";

  static public boolean isCached(File f)
  {
    File c=new File(f,cachefile);
    return !c.isDirectory() && c.exists();
  }

  //////////////////////////////////////////////////////////////////////////
  private AbstractFile file;
  private boolean valid;
  private long timestamp;

  public MandelFolderCache(File f) throws IOException
  {
    if (f.isDirectory()) {
      f=new File(f,cachefile);
    }

    file=new FileAbstractFile(f);
    read();
  }

  public MandelFolderCache(AbstractFile f) throws IOException
  {
    this.file=f;
    read();
  }

  public boolean isValid()
  {
    return valid;
  }

  final public void read()
  {
    valid=false;
    try {
      read(file);
      valid=true;
    }
    catch (IOException io) {
      System.out.println("cannot read cache "+file+": "+io);
    }
  }

  private void read(AbstractFile f) throws IOException
  {
    long ts=f.getLastModified();
    read(f.getInputStream(), f.toString());
    timestamp = ts;
  }

  public void write() throws IOException
  {
    if (file.isFile()) {
      File cachefile=new File(file.getFile().getPath());
      try {
        write(new FileOutputStream(cachefile),cachefile.toString());
        valid=true;
      }
      finally {
        timestamp=cachefile.lastModified();
      }
    }
    else {
      throw new UnsupportedOperationException("cache update only for files");
    }
  }

  public boolean removeFile(File f)
  {
    if (file.isFile()) {
      File folder=new File(file.getFile().getPath()).getParentFile();
      if (!f.getParentFile().equals(folder)) {
        throw new IllegalArgumentException(f+" not in directory");
      }
      else {
        return remove(f.getName());
      }
    }
    else {
      throw new UnsupportedOperationException("cache update only for files");
    }
  }

  public Entry addFile(File f)
  {
    if (file.isFile()) {
      File folder=new File(file.getFile().getPath()).getParentFile();
      if (!f.getParentFile().equals(folder)) {
        throw new IllegalArgumentException(f+" not in directory");
      }
      try {
        MandelData md=new MandelData(true, f, false);
        return add(f.getName(),md,f.lastModified());
      }
      catch (IOException ex) {
        System.out.println("ignoring "+f+": "+ex);
        return null;
      }
    }
    else {
      throw new UnsupportedOperationException("cache update only for files");
    }
  }

  public boolean update() throws IOException
  {
    long cur = file.getLastModified();
    if (cur!=timestamp) {
      read(file);
      return true;
    }
    return false;
  }

  private static class UncachedScanner extends FolderMandelScanner {
    public UncachedScanner(File d) throws IOException
    {
      super(d, MandelScanner.ALL);
    }

    @Override
    protected MandelFolderCache getCache()
    {
      return null;
    }

    @Override
    protected void lock()
    {
    }

    @Override
    protected void releaseLock()
    {
    }
  }

  public void recreate() throws IOException
  {
    int cnt=0;

    if (file.isFile()) {
      File folder=new File(file.getFile().getPath()).getParentFile();
      FolderMandelScanner scan=new UncachedScanner(folder);
      cache.clear();
      System.out.println("setting up cache data...");
      for (MandelName n:scan.getMandelNames()) {
        //System.out.println("found "+n);
        for (MandelHandle h:scan.getMandelHandles(n)) {
          try {
            MandelData md=new MandelData(true, h.getFile(), false);
            add(h.getFile().getName(), h.getHeader().getType(), md.getInfo(),
                     h.getFile().getLastModified());
            if (++cnt%500==0) {
              System.out.println("  "+cnt+" mem="+Runtime.getRuntime().freeMemory());
              Runtime.getRuntime().gc();
              Runtime.getRuntime().runFinalization();
            }
          }
          catch (IOException ex) {
            System.out.println("ignoring "+h.getFile()+": "+ex);
            throw ex;
          }
        }
      }
      for (ColormapName c:scan.getColormapNames()) {
        //System.out.println("found "+c);
        for (ColormapHandle h:scan.getColormapHandles(c)) {
          try {
            MandelData md=new MandelData(true, h.getFile(), false);
            add(h.getFile().getName(), h.getHeader().getType(), md.getInfo(),
                     h.getFile().getLastModified());
          }
          catch (IOException ex) {
            System.out.println("ignoring "+h.getFile()+": "+ex);
            throw ex;
          }
        }
      }
      write();
    }
    else {
      throw new UnsupportedOperationException("cache update only for files");
    }
  }


  /////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    if (args.length>0) {
      boolean read=false;
      for (String arg:args) {
        if (arg.equals("-r")) read=true;
        else if (arg.equals("-w")) read=false;
        else {
          try {
            MandelFolderCache c=new MandelFolderCache(new File(arg));
            if (read) {
              for (Entry e:c) {
                System.out.println(e.getFilename()+": "+e.getType());
              }
            }
            else {
              c.recreate();
            }
          }
          catch (IOException ex) {
            System.out.println("cannot read "+arg);
          }
        }
      }     
    }
  }
}
