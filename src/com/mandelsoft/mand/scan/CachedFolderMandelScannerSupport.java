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

import com.mandelsoft.mand.QualifiedMandelName;
import java.io.IOException;
import java.util.HashMap;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.MandelScanner.Filter;

/**
 *
 * @author Uwe Krueger
 */
public abstract class CachedFolderMandelScannerSupport extends FolderMandelScannerSupport {
  static public boolean debug=false;
  
  protected HashMap<AbstractFile,MandelInfo> infos=
          new HashMap<AbstractFile,MandelInfo>();
  protected HashMap<AbstractFile,MandelData> colormaps=
          new HashMap<AbstractFile,MandelData>();
  protected boolean initial=true;

  public CachedFolderMandelScannerSupport(Filter filter, boolean setup)
  {
    super(filter, setup);
  }

  public CachedFolderMandelScannerSupport(Filter filter)
  {
    super(filter);
  }

  public CachedFolderMandelScannerSupport()
  {
  }

  abstract protected MandelFolderCache getCache();

  //////////////////////////////////////////////////////////////////////////
  private class MyMandelHandle extends DefaultMandelHandle {
    public MyMandelHandle(AbstractFile file, QualifiedMandelName name,
                          MandelHeader header)
    {
      super(file,name,header);
    }

    @Override
    public MandelData getInfo() throws IOException
    {
      MandelInfo info=infos.get(getFile());
      if (info==null) {
        // info data omitted in cache; read directly

        try {
          MandelData md=super.getInfo();
          if (md!=null) infos.put(getFile(), info);
          return md;
        }
        catch (IOException io) {
          System.err.println("cannot read "+getFile()+": "+io);
          throw io;
        }
      }
      else {
        return new MandelData(info,getHeader(),getFile());
      }
    }
  }

  @Override
  protected MandelHandle create(AbstractFile f, QualifiedMandelName n,
                                MandelHeader h)
  {
    return new MyMandelHandle(f, n, h);
  }

  //////////////////////////////////////////////////////////////////////////

  private class MyColormapHandle extends DefaultColormapHandle {
    public MyColormapHandle(AbstractFile file, ColormapName name,
                          MandelHeader header)
    {
      super(file,name,header);
    }

    @Override
    public MandelData getData() throws IOException
    {
      MandelData data=colormaps.get(getFile());
      if (data==null) {
        // info data omitted in cache; read directly

        try {
          MandelData md=super.getData();
          if (md!=null) colormaps.put(getFile(), md);
          return md;
        }
        catch (IOException io) {
          System.err.println("cannot read "+getFile()+": "+io);
          throw io;
        }
      }
      else {
        return data;
      }
    }
  }

  @Override
  protected ColormapHandle create(AbstractFile f, ColormapName n,
                                MandelHeader h)
  {
    return new MyColormapHandle(f, n, h);
  }
  //////////////////////////////////////////////////////////////////////////
  
  @Override
  protected void clear()
  {
    if (debug) System.out.println("clear folder scanner");
    initial=true;
    infos.clear();
    colormaps.clear();
    super.clear();
  }

  @Override
  protected void add(MandelHandle h)
  {
    infos.remove(h.getFile());
    super.add(h);
  }

  @Override
  protected void add(ColormapHandle h)
  {
    colormaps.remove(h.getFile());
    super.add(h);
  }

  @Override
  protected void remove(AbstractFile f)
  {
    infos.remove(f);
    colormaps.remove(f);
    super.remove(f);
  }

  //
  // cache handling
  //

  private MandelHeader createMandelHeader(MandelFolderCache.Entry e)
                         throws IOException
  {
    return new MandelHeader(e.getType());
  }

  abstract protected AbstractFile createAbstractFile(MandelFolderCache.Entry e)
                                      throws IOException;

  protected void add(MandelFolderCache.Entry e)
  {
    //System.out.println("add "+e);
    try {
      String base=e.getFilename();
      int ix=base.lastIndexOf('.');
      if (ix>0) base=base.substring(0,ix);
      MandelFileName n=null;
      
      try {
        MandelHeader h=createMandelHeader(e);
        AbstractFile af;
        if (h!=null && filter(h)) {
          if (h.isColormap()) {
            add(create(createAbstractFile(e),new ColormapName(base), h));
          }
          else {
            n=MandelFileName.create(e.getFilename());
            if (n==null) return;
            add(create(af=createAbstractFile(e),n.getQualifiedName(), h));
            if (h.hasInfo()) infos.put(af,e.getInfo());
          }
          //System.out.println("      used");
        }
      }
      catch (IOException io) {
        // System.out.println("    no mandel: "+f);
      }
    }
    catch (IllegalArgumentException ex) {
      //ignore
    }
  }

  //
  // file handling
  //
  
  public void rescan(boolean verbose)
  {
    rescan(verbose,true);
  }

  protected abstract boolean rescanNonCached(boolean verbose, boolean read);
  protected boolean rescanCached(MandelFolderCache cache,
                                 boolean verbose, boolean read)
  {
    boolean updated=true;
    // System.out.println("using cache");
    if (read) try {
        updated=cache.update();
      }
      catch (IOException ex) {
        System.err.println("cannot read cache: "+ex);
      }
    if (updated || initial) {
      clear();
      for (MandelFolderCache.Entry e:cache) {
        add(e);
      }
    }
    return updated;
  }

  protected void rescan(boolean verbose, boolean read)
  { boolean updated=true;

    lock();
    startUpdate();
    try {
      MandelFolderCache mcache=getCache();
      if (mcache!=null) {
        updated=rescanCached(mcache,verbose,read);
      }
      else {
        updated=rescanNonCached(verbose, read);
      }
    }
    finally {
      releaseLock();
      initial=false;
      finishUpdate();
      //if (debug) dump();
    }
  }

  protected abstract void lock();
  protected abstract void releaseLock();
}
