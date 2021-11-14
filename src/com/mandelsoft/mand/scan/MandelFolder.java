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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.mandelsoft.io.FolderLock;
import java.util.HashMap;

/**
 *
 * @author Uwe Krüger
 */

public class MandelFolder extends FolderLock  {
  static public boolean debug=false;
  static private Map<File,MandelFolder> map=new HashMap<File,MandelFolder>();

  synchronized
  public static MandelFolder getMandelFolder(File f) throws IOException
  {
    f=f.getCanonicalFile();
    if (!f.isDirectory()) f=f.getParentFile();
    if (debug) System.out.println("%%% get mandel folder "+f);
    MandelFolder m=map.get(f);
    if (m==null) {
      if (debug) System.out.println("      create new mandel folder");
      m=new MandelFolder(f);
      map.put(f,m);
    }
    return m;
  }

  private MandelFolderCache cache;

  public MandelFolder(File f) throws IOException
  {
    super(f);
    if (!f.isDirectory()) throw new IOException(f+" is no directory");
    lock();
    try {
      if (MandelFolderCache.isCached(f)) {
        cache=new MandelFolderCache(f);
      }
    }
    finally {
      releaseLock();
    }
  }

  synchronized
  public MandelFolderCache getCache()
  {
    return cache;
  }

  synchronized
  public boolean hasCache()
  {
    return cache!=null;
  }

  synchronized void createCache() throws IOException
  {
    if (cache==null) {
      cache=new MandelFolderCache(this.getFolder());
      cache.recreate();
    }
  }

  synchronized
  public void recreate() throws IOException
  {
    if (cache!=null) {
      lock();
      try {
        cache.recreate();
      }
      finally {
        releaseLock();
      }
    }
  }
  
  public boolean remove(String name) throws IOException
  {
    return remove(new File(getFolder(),name));
  }

  synchronized
  public boolean remove(File f) throws IOException
  {
    f=f.getCanonicalFile();
    if (!f.getParentFile().equals(getFolder())) {
      throw new IllegalArgumentException(f+" not in directory");
    }
    lock();
    try {
      if (f.delete()) {
        _handleRemoved(f);
        return true;
      }
      return false;
    }
    finally {
      releaseLock();
    }
  }

  synchronized
  protected void handleRemoved(File f) throws IOException
  {
    lock();
    try {
      _handleRemoved(f);
    }
    finally {
      releaseLock();
    }
  }

  protected void _handleRemoved(File f) throws IOException
  {
    boolean full=false;
    if (cache!=null) {
      full=cache.update();
      if (cache.removeFile(f))
        cache.write();
    }
    if (full) {
      folderUpdated(getFolder());
    }
    else {
      removeMandelFile(f);
    }
  }

  public boolean moveToLocal(File f) throws IOException
  {
    return renameTo(f,new File(getFolder(),f.getName()));
  }

  public boolean renameTo(File f, String name) throws IOException
  {
    return renameTo(f,new File(getFolder(),name));
  }

  public boolean renameTo(File f, File dst) throws IOException
  { MandelFolder src=null;

    dst=dst.getCanonicalFile();
    if (!dst.getParentFile().equals(getFolder())) {
      throw new IllegalArgumentException(dst+" not in directory");
    }
    File old=f.getCanonicalFile();
    try {
      synchronized (this) {
        lock();
        try {
          if (f.renameTo(dst)) {
            if (debug) System.out.println("rename successful "+dst);
            if (old.getParentFile().equals(getFolder())) {
              if (debug) System.out.println("remove from current");
              _handleRemoved(old);
            }
            else {
              if (debug) System.out.println("remove from other "+old.getParentFile());
              src=getMandelFolder(old.getParentFile());
              //src._handleRemoved(old);
            }
            _handleAdded(dst);
            return true;
          }
          return false;
        }
        finally {
          releaseLock();
        }
      }
    }
    finally {
      if (debug) System.out.println("propagate remove from "+src);
      if (src!=null) {
        src.handleRemoved(old);
      }
    }
  }

  protected void _handleAdded(File f) throws IOException
  {
    boolean full=false;
    MandelFolderCache.Entry e=null;

    if (cache!=null) {
      full=cache.update();
      e=cache.addFile(f);
      if (e!=null) {
        cache.write();
      }
    }
    if (full) {
      folderUpdated(getFolder());
    }
    else {
      addMandelFile(f, e);
    }
  }

  synchronized
  public void add(File f) throws IOException
  {
    boolean full=false;
    f=f.getCanonicalFile();
    if (!f.getParentFile().equals(getFolder())) {
      throw new IllegalArgumentException(f+" not in directory");
    }
    lock();
    try {
      _handleAdded(f);
    }
    finally {
      releaseLock();
    }
  }

  @Override
  public String toString()
  {
    return getFolder().toString();
  }
  //////////////////////////////////////////////////////////////////////////
  // events

  private List<MandelFolderListener> listenerList=new ArrayList<MandelFolderListener>();

  synchronized
  public void addMandelFolderListener(MandelFolderListener l)
  {
    listenerList.add(l);
  }

  synchronized
  public void removeMandelFolderListener(MandelFolderListener l)
  {
    listenerList.remove(l);
  }

  synchronized
  public MandelFolderListener[] getMandelFolderListeners()
  {
    return listenerList.toArray(new MandelFolderListener[listenerList.size()]);
  }

  protected void addMandelFile(File f, MandelFolderCache.Entry e)
  {
    for (MandelFolderListener l:listenerList) {
      l.addMandelFile(f,e);
    }
  }

  protected void removeMandelFile(File f)
  {
    //System.out.println("propagate file remove "+listenerList.size());
    for (MandelFolderListener l:listenerList) {
      l.removeMandelFile(f);
    }
  }

  protected void folderUpdated(File f)
  {
    for (MandelFolderListener l:listenerList) {
      l.folderUpdated(f);
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // util

  static public class Util {
    public static void add(File f) throws IOException
    {
      MandelFolder mf=getMandelFolder(f.getParentFile());
      mf.add(f);
    }

    public static boolean delete(File f) throws IOException
    {
      MandelFolder mf=getMandelFolder(f.getParentFile());
      return mf.remove(f);
    }

    public static boolean renameTo(File s, File d) throws IOException
    {
      MandelFolder mf=getMandelFolder(d.getParentFile());
      return mf.renameTo(s,d);
    }
  }

  ////////////////////////////////////////////////////////////////////

  static public void main(String[] args)
  {
    File n=new File(".");
    if (args.length>0) n=new File(args[0]);
    try {
      MandelFolder f=getMandelFolder(n);
      f.createCache();
    }
    catch (IOException ex) {
      System.out.println("cannot handle folder "+n+": "+ex);
    }

  }
}
