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
import com.mandelsoft.mand.scan.MandelScannerUtils;
import com.mandelsoft.mand.util.ArrayMandelList;
import com.mandelsoft.mand.util.DefaultMandelList;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.mand.util.MandelList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class SubAreaFactory extends ScannerBasedMandelListFactory
                            implements MandelBasenameSource {
  protected MandelName basename;
  protected String title;

  public SubAreaFactory(MandelScanner scanner, MandelName basename, String title)
  {
    super(scanner);
    this.basename=basename;
    this.title=title;
  }

  public MandelName getBasename()
  {
    return basename;
  }

  public void setBasename(MandelName basename)
  {
    this.basename=basename;
  }

  public String getTitle()
  {
    return title+" for "+getBasename();
  }

  @Override
  protected boolean accept(QualifiedMandelName n)
  {
    return getBasename().isAbove(n.getMandelName());
  }

  @Override
  protected boolean acceptType(QualifiedMandelName n)
  {
    return MandelScannerUtils.hasImageData(getScanner().getMandelHandles(n));
  }

  ////////////////////////////////////////////////////////////////////////////
  // utility for tree filtering

  protected boolean accept(QualifiedMandelName name, Set<MandelName> subareas)
  {
    return false;
  }

  protected boolean acceptTree(QualifiedMandelName name)
  {
    return true;
  }

  protected MandelList filterTree()
  {
    MandelList list=new DefaultMandelList();
    List<QualifiedMandelName> dive=new ArrayList<QualifiedMandelName>();
    List<QualifiedMandelName> next=new ArrayList<QualifiedMandelName>();
    List<QualifiedMandelName> tmp;
    dive.add(new QualifiedMandelName(getBasename()));
    boolean check=false;

     while (!dive.isEmpty()) {
       for (QualifiedMandelName name:dive) {
         if (acceptTree(name)) {
           Set<MandelName> set=MandUtils.getSubNames(name.getMandelName(), this.getScanner());

           if (check) {
             if (accept(name,set)) {
               list.add(name);
               continue;
             }
           }
           for (MandelName mn:set) {
             next.addAll(getScanner().getQualifiedMandelNames(mn));
           }
         }
       }
       tmp=dive;
       dive=next;
       next=tmp;
       tmp.clear();
       check=true;
     }
     return list;
  }
}
