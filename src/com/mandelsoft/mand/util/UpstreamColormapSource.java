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
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class UpstreamColormapSource implements ColormapSource {

  protected static final boolean debug=true;
  private MandelName n;
  private MandelScanner scanner;
  private ColormapSource defaultSource;
  private QualifiedMandelName source;

  public UpstreamColormapSource(MandelName n, MandelScanner scanner,
                                ColormapSource defaultSource)
  {
    this.n=n;
    this.scanner=scanner;
    this.defaultSource=defaultSource;
  }

  public UpstreamColormapSource(MandelName n, MandelScanner scanner)
  {
    this(n,scanner,null);
  }

  public Colormap getColormap()
  {
    Colormap cm=null;
    MandelHandle h=MandUtils.lookupColormap(scanner, n);
    while (cm==null && h!=null) {
      cm=optimizedLoad(h);
      if (cm==null) try {
        if (debug) System.out.println("use upstream colormap of "+h.getName());
        cm=h.getData().getColormap();
      }
      catch (IOException ex) {
        h=MandUtils.lookupColormap(scanner,
                                   h.getName().getMandelName().getParentName());
      }
    }
    if (cm!=null) colormapFound(h,cm);
    else cm=defaultSource==null?null:defaultSource.getColormap();
    return cm;
  }

  public MandelName getBasename()
  {
    return n;
  }


  protected Colormap optimizedLoad(MandelHandle h)
  {
    return null;
  }

  protected void colormapFound(MandelHandle h, Colormap cm)
  {
    source=h.getName();
  }

  public QualifiedMandelName getSource()
  {
    return source;
  }
}
