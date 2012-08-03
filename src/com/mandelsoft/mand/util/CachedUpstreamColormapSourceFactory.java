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

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krueger
 */
public class CachedUpstreamColormapSourceFactory extends UpstreamColormapSourceFactory {
  private MandelColormapCache cache;

  public CachedUpstreamColormapSourceFactory(MandelScanner scanner,
                                       ColormapSource defaultSource,
                                       MandelColormapCache cache)
  {
    super(scanner,defaultSource);
    this.cache=cache;
  }
  
  @Override
  public ColormapSource getColormapSource(QualifiedMandelName name)
  {
    return new CachedUpstreamColormapSource(name.getMandelName(),getMandelScanner(),
                                     getDefaultSource(),getCache());
  }

  protected MandelColormapCache getCache()
  {
    return cache;
  }
}
