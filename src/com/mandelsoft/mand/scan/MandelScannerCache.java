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
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.mandelsoft.mand.scan.MandelScanner.Filter;

/**
 *
 * @author Uwe Krueger
 */
public class MandelScannerCache {
   static private boolean debug=false;

   private Map<Object,Map<Filter,MandelScanner>> cache;
   private Proxy proxy;

  public MandelScannerCache(Proxy proxy)
  {
    this.cache=new HashMap<Object,Map<Filter,MandelScanner>>();
    this.proxy=proxy;
  }

  public void setProxy(Proxy proxy)
  {
    this.proxy=proxy;
  }

  public Proxy getProxy()
  {
    return proxy;
  }

  public MandelScanner getScanner(URL url, Filter filter)
                           throws MalformedURLException
  {
    MandelScanner scanner;
    Map<Filter,MandelScanner> scanners=cache.get(url);

    if (scanners==null) {
      if (debug) System.out.println("*** create all scanner for "+url);
      scanner=new URLMandelScanner(proxy, url, MandelScanner.ALL);
      scanners=new HashMap<Filter,MandelScanner>();
      cache.put(url, scanners);
      scanners.put(MandelScanner.ALL, scanner);
    }
    return getFilter(url,scanners,filter);
  }

  public MandelScanner getScanner(File d, Filter filter) throws IOException
  {
    MandelScanner scanner;
    d=d.getCanonicalFile();
    Map<Filter,MandelScanner> scanners=cache.get(d);

    if (scanners==null) {
      if (debug) System.out.println("*** create all scanner for "+d);
      scanner=new FolderMandelScanner(d, MandelScanner.ALL);
      scanners=new HashMap<Filter,MandelScanner>();
      cache.put(d, scanners);
      scanners.put(MandelScanner.ALL, scanner);
    }
    return getFilter(d,scanners,filter);
  }

  private MandelScanner getFilter(Object src,
                                  Map<Filter,MandelScanner> scanners,
                                  Filter filter)
  {
    MandelScanner scanner=scanners.get(filter);
    if (scanner==null) {
      if (debug) System.out.println("*** create filtered scanner for "+src);
      scanner=new FilteredMandelScanner(scanners.get(MandelScanner.ALL),filter);
      scanners.put(filter, scanner);
    }
    else {
      if (debug) System.out.println("*** found filtered scanner for "+src);
    }
    return scanner;
  }
}
