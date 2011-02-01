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

package com.mandelsoft.mand;

import com.mandelsoft.io.AbstractFile;
import java.io.File;

/**
 *
 * @author Uwe Krüger
 */
public class ColormapName extends DefaultElementName<ColormapName> {
  private String base;

  public ColormapName(String n)
  {
    super(n);
    base=base(n);
  }

  public ColormapName(ColormapName name, String label)
  {
    super(name.getBasename(),label);
    base=name.getBasename();
  }

  @Override
  public String getBasename()
  {
    return base;
  }

  //////////////////////////////////////////////////////////////////////
  
  @Override
  public int compareTo(ColormapName o)
  {
    int c=base.compareTo(o.getBasename());
    if (c==0) {
      if (label!=o.label) {
        if (label==null) c=-1;
        else if (o.label==null) c=1;
        else c=label.compareTo(o.label);
      }
    }
    return c;
  }

  //////////////////////////////////////////////////////////////////////////
  public static ColormapName create(File f)
  {
    try {
      String base=f.getName();
      if (base.endsWith(".cm")) base=base.substring(0,base.length()-3);
      return new ColormapName(base);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  public static ColormapName create(AbstractFile f)
  {
    try {
      String base=f.getName();
      if (base.endsWith(".cm")) base=base.substring(0,base.length()-3);
      return new ColormapName(base);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }
}
