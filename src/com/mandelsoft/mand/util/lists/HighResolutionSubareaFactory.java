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

import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class HighResolutionSubareaFactory extends SubAreaFactory {
  private int min;
  
  public HighResolutionSubareaFactory(MandelScanner scanner, MandelName basename,
                           String title, int min)
  {
    super(scanner,basename,title, true, false);
    this.min=min;
  }

  @Override
  protected boolean accept(MandelHandle h)
  {
    if (!super.accept(h)) return false;

    if (!h.getHeader().hasImageData()) return false;
   
    try {
      MandelInfo info = h.getInfo().getInfo();
      if (info.getRX() >= min) {
        return true;
      }
      return false;
    }
    catch (IOException io) {
      return false;
    }
  }
  
  /*
  @Override
  protected void add(MandelList list, QualifiedMandelName name)
  {
    name = new QualifiedMandelName(name.getMandelName());
    if (!list.contains(name)) {
      list.add(name);
    }
  }
*/
}
