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
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krueger
 */
public class ListSubsetFactory extends SubAreaFactory {
  protected MandelList baselist;

  public ListSubsetFactory(MandelScanner scanner, MandelName basename,
                           MandelList baselist, String title)
  {
    super(scanner,basename,title);
    this.baselist=baselist;
  }

  @Override
  protected boolean accept(QualifiedMandelName name)
  {
    if (baselist==null) return false;
    if (!super.accept(name)) return false;
    if (baselist.contains(name)) return true;
    QualifiedMandelName base=name.getBaseName();
    return baselist.contains(base);
  }
}
