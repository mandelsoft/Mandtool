
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Uwe Krüger
 */

public class ArrayMandelList extends ArrayBaseList<QualifiedMandelName>
                             implements MandelList {          
  public ArrayMandelList(int initialCapacity)
  { super(initialCapacity);
  }

  public ArrayMandelList()
  {
  }

  public ArrayMandelList(Collection<? extends QualifiedMandelName> c)
  { super(c);
  }

 
  ///////////////////////////////////////////////////////////////////////////

  public void write(OutputStream os, String dst) throws IOException
  {
    IO.write(this, os, dst);
  }

  public void read(InputStream is, String src) throws IOException
  {
    IO.read(this, is, src);
  }

  public QualifiedMandelName get(MandelName n)
  {
    for (QualifiedMandelName q:this) {
      if (q.getMandelName().equals(n)) return q;
    }
    return null;
  }
}
