/*
 * Copyright 2021 d021770.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.ProxyColormapHandleForMandelHandle;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author d021770
 */
public class ScannerMandelColormapList extends ScannerColormapList {

  public ScannerMandelColormapList(MandelScanner scanner)
  {
    super(scanner);
  }

  protected Set<ColormapName> getColormapNames()
  {
    Set<ColormapName> set = new HashSet<>();
    for (MandelHandle h : scanner.getMandelHandles()) {
      if (h.getHeader().hasMandelColormap()) {
        set.add(new ColormapName(h.getName().toString()));
      }
    }
    return set;
  }

  public ColormapHandle getColormapHandle(ColormapName name) throws IOException
  {
    MandelHandle h = scanner.getMandelHandle(QualifiedMandelName.create(name.getName()));
    if (h == null) {
      return null;
    }
    return new ProxyColormapHandleForMandelHandle(h, name);
  }
}
