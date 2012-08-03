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

import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krueger
 */
public class CachedUpstreamColormapSource extends UpstreamColormapSource {

  private MandelColormapCache cache;

  public CachedUpstreamColormapSource(MandelName n, MandelScanner scanner,
                                ColormapSource defaultSource,
                                MandelColormapCache cache)
  {
    super(n,scanner,defaultSource);
    this.cache=cache;
  }

  public CachedUpstreamColormapSource(MandelName n, MandelScanner scanner,
                                MandelColormapCache cache)
  {
    this(n,scanner,null,cache);
  }

  public CachedUpstreamColormapSource(MandelName n, MandelScanner scanner,
                                ColormapSource defaultSource)
  {
    this(n,scanner,defaultSource,null);
  }

  @Override
  protected Colormap optimizedLoad(MandelHandle h)
  {
    if (h!=null && cache!=null) {
      Colormap cm=cache.get(h.getName());
      if (cm!=null && debug) {
        System.out.println("found cached colormap for "+h.getName());
      }
      return cm;
    }
    return null;
  }

  @Override
  protected void colormapFound(MandelHandle h, Colormap cm)
  {
    super.colormapFound(h, cm);
    if (cache!=null) cache.add(h.getName(), cm);
  }
}
