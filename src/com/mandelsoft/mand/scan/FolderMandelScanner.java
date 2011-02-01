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

import com.mandelsoft.io.AbstractFile;
import java.io.File;
import java.io.IOException;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.scan.MandelInventory.Entry;

/**
 *
 * @author Uwe Krueger
 */
public class FolderMandelScanner extends CachedFolderMandelScannerSupport {
  static public boolean debug=false;
  
  private MandelFolder folder;

  public FolderMandelScanner(File d)
                            throws IOException
  {
    this(d,MandelScanner.HAS_IMAGEDATA);
  }

  public FolderMandelScanner(File d, Filter filter)
                            throws IOException
  { this(d,filter,true);
  }

  public FolderMandelScanner(File d, Filter filter, boolean setup)
                            throws IOException
  { super(filter,false);
    folder=MandelFolder.getMandelFolder(d);
    folder.addMandelFolderListener(new FolderListener());
    if (setup) rescan(false);
  }

  @Override
  public String toString()
  {
    return "FolderMandelScanner:"+folder+"("+super.toString()+")";
  }

  //
  // cache handling
  //

  @Override
  protected MandelFolderCache getCache()
  {
    return folder.getCache();
  }

  @Override
  protected AbstractFile createAbstractFile(Entry e) throws IOException
  {
    return AbstractFile.Factory.create(folder.getFolder(), e.getFilename());
  }

  @Override
  protected void lock()
  {
    folder.lock();
  }

  @Override
  protected void releaseLock()
  {
    folder.releaseLock();
  }

  @Override
  protected boolean rescanNonCached(boolean verbose, boolean read)
  {
    int cnt=0;
    clear();
    System.out.println("scanning folder "+folder.getFolder());
    if (debug) System.out.println("  reading directory....");
    File list[]=folder.getFolder().listFiles();
    if (debug) System.out.println("  scanning content...");
    if (list!=null) for (File f :list) {
        //System.out.println("  checking "+f);
        add(f);
        if (++cnt%1000==0) {
          if (debug) System.out.println("    "+cnt);
        }
    }
    //System.out.println("folder done");
    return true;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Event Listener
  ///////////////////////////////////////////////////////////////////////////

  private class FolderListener implements MandelFolderListener {

    public void addMandelFile(File f, Entry e)
    {
      if (debug) System.out.println("--- add mandel file "+f);
      if (e!=null) {
        add(e);
      }
      else {
        add(f);
      }
    }

    public void removeMandelFile(File f)
    {
      if (debug) System.out.println("--- remove mandel file "+f);
      MandelFileName mfn=MandelFileName.create(f);
      AbstractFile af=AbstractFile.Factory.create(f);
      if (mfn!=null) {
        if (folder.hasCache()) {
          if (infos.remove(af)!=null) {
            if (debug) System.out.println("    scanner updated");
          }
          else {
            if (debug) System.out.println("    not found");
          }
        }
      }
      remove(af);
    }

    public void folderUpdated(File f)
    {
      if (!isInUpdate()) rescan(false,false);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  { 
    //File f=new File("C:/work/AccuRev/test/Mandel/data/images");
    File f=new File("C:/work/AccuRev/test/Mandel/data/new");
    try {
      MandelScanner s=new FolderMandelScanner(f);
      //s=new FilteredMandelScanner(s,MandelScanner.RASTERIMAGE);
      for (MandelName n:s.getMandelNames()) {
        System.out.println(n);
        for (MandelHandle mh:s.getMandelHandles(n)) {
          System.out.println("   "+mh.getFile()
              +": "+mh.getHeader()+": "+mh.getHeader().getType());
        }
      }
    }
    catch (IOException ex) {
      System.err.println("cannot handle folder "+f+": "+ex);
    }
  }
}
