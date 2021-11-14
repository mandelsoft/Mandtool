
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

import com.mandelsoft.mand.ColormapName;
import java.io.IOException;
import java.util.Iterator;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;


/**
 *
 * @author Uwe Krüger
 */

public interface ColormapList extends Iterable<ColormapName>{
  void refresh();
  void save() throws IOException;
  void clear();
  boolean valid();
  
  int size();
  boolean isEmpty();
  Colormap get(ColormapName name) throws IOException;
  Colormap get(int index) throws IOException;
  ColormapName   getName(int index);
  ColormapHandle getColormapHandle(ColormapName name) throws IOException;
  ColormapHandle getColormapHandle(int index) throws IOException;
  int indexOf(ColormapName name);
  int indexOf(Colormap cm);
  boolean contains(ColormapName name);
  boolean contains(Colormap sm);
  boolean add(ColormapName name, Colormap cm);
  boolean add(ColormapName name, Colormap cm, ColormapHandle h);
  boolean add(int index, ColormapName name, Colormap cm);
  boolean add(int index, ColormapName name, Colormap cm, ColormapHandle h);
  boolean remove(ColormapName name);
  ColormapName remove(int index);
}
