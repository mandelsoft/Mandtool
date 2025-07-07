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
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.DefaultMandelList;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krueger
 */
public class ParentAreasFactory implements MandelListFactory {
  private String title;
  private MandelName basename;

  public ParentAreasFactory(MandelName basename)
  {
    this("Parent Areas", basename);
  }
  
  public ParentAreasFactory(String title, MandelName basename)
  {
    this.title=title;
    this.basename = basename;
  }

  @Override
  public MandelList getList()
  {
    MandelList list = new DefaultMandelList();
    
    MandelName n = basename;
    
    while (n != null) {
      if (accept(n)) {
        list.add(new QualifiedMandelName(n));
      }
      n = n.getParentName();
    }
    return list;
  }

  protected boolean accept(MandelName n)
  {
    return true;
  }
  
  @Override
  public String getTitle()
  {
    return title+" for "+basename.toString();
  }
}
