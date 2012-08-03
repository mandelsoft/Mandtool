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

import java.util.Set;
import com.mandelsoft.mand.MandelConstants;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;

/**
 *
 * @author Uwe Krueger
 */
public interface MandelScanner extends MandelConstants {
  public void                        setFilter(Filter f);
  public Filter                      getFilter();

  public Set<ElementHandle<?>>       getAllHandles();
  public Set<MandelHandle>           getMandelHandles();

  public Set<MandelName>             getMandelNames();
  public Set<MandelHandle>           getMandelHandles(MandelName name);
  public MandelHandle                getMandelInfo(MandelName name);
  public MandelHandle                getMandelData(MandelName name);

  public Set<QualifiedMandelName>    getQualifiedMandelNames();
  public Set<QualifiedMandelName>    getQualifiedMandelNames(MandelName name);
  public Set<MandelHandle>           getMandelHandles(QualifiedMandelName name);
  public MandelHandle                getMandelHandle(QualifiedMandelName name);
  //
  // assure accessibilty of requested type of data
  public MandelHandle                getMandelInfo(QualifiedMandelName name);
  public MandelHandle                getMandelData(QualifiedMandelName name);

  public Set<ColormapName>           getColormapNames();
  public Set<ColormapHandle>         getColormapHandles(ColormapName name);
  public boolean                     hasColormap(ColormapName name);
  public ColormapHandle              getColormap(ColormapName name);

  public void addMandelScannerListener (MandelScannerListener l);
  public void removeMandelScannerListener(MandelScannerListener l);
  public MandelScannerListener[] getMandelScannerListeners();

  public void rescan(boolean verbose);

  public interface Filter {
    boolean filter(MandelHeader h);
  }

  static public final Filter ALL=new Filter() {
    public boolean filter(MandelHeader h)
    { return true;
    }
  };

  static public final Filter COLORMAP=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isColormap();
    }
  };

  static public final Filter INFO=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isInfo();
    }
  };

  static public final Filter AREACOLMAP=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isAreaColormap();
    }
  };

  static public final Filter RASTER=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isRaster();
    }
  };

  static public final Filter INCOMPLETERASTER=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.has(C_RASTER&C_INCOMPLETE);
    }
  };

  static public final Filter IMAGE=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isImage();
    }
  };

  static public final Filter RASTERIMAGE=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.hasModifiableImage();
    }
  };
  static public final Filter PLAINIMAGE=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isPlainImage();
    }
  };

  static public final Filter HAS_INFO=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.hasInfo();
    }
  };

  static public final Filter HAS_RASTER=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.hasRaster();
    }
  };

  static public final Filter IS_RASTER=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.isRaster();
    }
  };

  static public final Filter HAS_IMAGE=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.hasImage();
    }
  };

  static public final Filter HAS_IMAGEDATA=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.hasImageData();
    }
  };

  static public final Filter HAS_AREACOLMAP=new Filter() {
    public boolean filter(MandelHeader h)
    { return h.hasMandelColormap();
    }
  };
}
