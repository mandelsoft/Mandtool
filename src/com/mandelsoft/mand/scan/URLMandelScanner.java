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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.io.URLAbstractFile;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.scan.MandelInventory.Entry;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class URLMandelScanner extends CachedFolderMandelScannerSupport {
  private AbstractFile folder;
  private AbstractFile cache;
  private MandelFolderCache mcache;

  public URLMandelScanner(Proxy proxy, URL d) throws MalformedURLException
  {
    this(new URLAbstractFile(proxy,d), MandelScanner.HAS_IMAGEDATA);
  }

  public URLMandelScanner(Proxy proxy, URL d, Filter filter) throws MalformedURLException
  {
    this(new URLAbstractFile(proxy,d), filter);
  }

  public URLMandelScanner(AbstractFile d) throws MalformedURLException
  {
    this(d, MandelScanner.HAS_IMAGEDATA);
  }

  public URLMandelScanner(AbstractFile d, Filter filter)
          throws MalformedURLException
  { this(d,filter,true);
  }

  public URLMandelScanner(AbstractFile d, Filter filter, boolean setup)
          throws MalformedURLException
  {
    super(filter,false);
    this.folder=d;
    this.cache=d.getSub(MandelFolderCache.cachefile);
    if (setup) rescan(false);
  }

  public URL getFolder()
  {
    return folder.getURL();
  }

  private URL sub(String name) throws MalformedURLException

  {
    return Utils.subURL(getFolder(), name);
  }


  //
  // file handling
  //

  @Override
  protected MandelFolderCache getCache()
  {
    if (mcache==null) {
      try {
        mcache=new MandelFolderCache(this.cache);
      }
      catch (IOException ex) {
        System.out.println("cannot read cache: "+ex);
      }
    }
    return mcache;
  }

  @Override
  protected AbstractFile createAbstractFile(Entry e) throws IOException
  {
    return AbstractFile.Factory.create(folder.getProxy(),sub(e.getFilename()),
                                      e.getLastModified());
  }

  @Override
  protected void lock()
  {
  }

  @Override
  protected void releaseLock()
  {
  }

  @Override
  protected boolean rescanNonCached(boolean verbose, boolean read)
  {
    throw new UnsupportedOperationException("Uncached not supported.");
  }

  @Override
  public String toString()
  {
    return getFolder()+"("+super.toString()+")";
  }

  ///////////////////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    try {
      
      URL url=new URL(args[0]);
      System.out.println("setting up "+url);
      MandelScanner scan=new URLMandelScanner(null,url);
      scan.rescan(true);
      System.out.println("files:");
      for (ElementHandle<?> h:scan.getAllHandles()) {
        System.out.println("  "+h.getFile());
      }
      System.out.println("header:");
      for (MandelName n:scan.getMandelNames()) {
        MandelData info=scan.getMandelInfo(n).getInfo();
        String p=info.getInfo().getXM()+","+info.getInfo().getYM();
        System.out.println(n+": "+info.getHeader().getTypeDesc()+": "+
                                  p+": "+info.getFile());
      }
    }
    catch (Exception ex) {
      System.out.println(ex);
      ex.printStackTrace();
    }
  }
}
