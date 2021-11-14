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

package com.mandelsoft.mand.tool;

import java.io.IOException;
import javax.swing.table.TableModel;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;

/**
 *
 * @author Uwe Krueger
 */
public interface ColormapListModel extends TableModel {

  void refresh();

  boolean add(ColormapName name, Colormap cm);
  boolean remove(int index);
  boolean remove(ColormapName name);

  Colormap getColormap(int index) throws IOException;
  AbstractFile getFile(int index) throws IOException;
  ColormapHandle getColormapHandle(int index) throws IOException;
  ColormapName getName(int index);
}
