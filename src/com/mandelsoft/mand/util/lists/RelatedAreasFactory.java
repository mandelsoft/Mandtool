/*
 *  Copyright 2021 Uwe Krueger.
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
import com.mandelsoft.mand.tool.LinkFolderTreeModel;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krueger
 */
public class RelatedAreasFactory implements MandelListFactory {
  private MandelName basename;
  private LinkFolderTreeModel links;

  public RelatedAreasFactory(MandelName basename, LinkFolderTreeModel links)
  {
    this.basename = basename;
    this.links = links;
  }

  @Override
  public MandelList getList()
  {
    return links.getLinkClosure(basename);
  }

  @Override
  public String getTitle()
  {
    return "Related Areas for "+basename.toString();
  }
}
