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

package com.mandelsoft.mand.util.lists;

import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelListFolder;

/**
 *
 * @author Uwe Krueger
 */
public class FolderSubsetFactory extends SubAreaFactory {
  private MandelListFolder folder;

  public FolderSubsetFactory(MandelScanner scanner, MandelName basename,
                       MandelListFolder folder, String title)
  {
    super(scanner,basename,title);
    this.folder=folder;
  }

  @Override
  protected boolean accept(QualifiedMandelName name)
  {
    if (!super.accept(name)) return false;
    for (QualifiedMandelName n:folder.allentries()) {
      if (n.equals(name) || n.getMandelName().equals(name.getMandelName())) return true;
    }
    return false;
  }
}
