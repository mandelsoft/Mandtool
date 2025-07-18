/*
 * Copyright 2021 Uwe Krueger.
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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import java.util.List;

/**
 * TODO: not yet implemented
 * @author Uwe Krueger
 */
public class LinkClosureModel extends DefaultMandelListTableModel {
  private MandelListFolderTreeModel links;
  private MandelName name;
  
  public LinkClosureModel(MandelListFolderTreeModel links, MandelName name) {
    this.links=links;
    this.name = name;
    
    setup();
  }
  
  private void setup() {
    MandelListFolder f = links.getChild(links.getRoot(), name.getName());
  }
}
