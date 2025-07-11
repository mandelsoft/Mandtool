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

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.DefaultMandelList;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.UniqueDefaultMandelList;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public abstract class ScannerBasedMandelListFactory implements MandelListFactory {
  private MandelScanner scanner;
  private boolean uniqueNames;
  private boolean projectMandelNames;
  
  protected ScannerBasedMandelListFactory(MandelScanner scanner)
  {
    this.scanner=scanner;
  }
  
  protected ScannerBasedMandelListFactory(MandelScanner scanner, boolean uniqueNames, boolean projectMandelNames)
  {
    this.scanner=scanner;
    this.uniqueNames=uniqueNames;
    this.projectMandelNames=projectMandelNames;
  }
  

  public MandelScanner getScanner()
  {
    return scanner;
  }

  public void setScanner(MandelScanner scanner)
  {
    this.scanner=scanner;
  }

  protected void add(MandelList list, QualifiedMandelName name)
  {
    list.add(name);
  }
  
  public MandelList getList()
  {
     MandelList list;
     if (uniqueNames) {
       list=new UniqueDefaultMandelList(projectMandelNames);
     }
     else {
       list=new DefaultMandelList();
     
     }
     for (MandelHandle h:scanner.getMandelHandles()) {
       if (acceptType(h) && accept(h)) add(list, h.getName());
     }
     return list;
  }

  protected boolean accept(MandelHandle h)
  {
    return accept(h.getName());
  }
  
  protected boolean acceptType(MandelHandle h)
  {
    return acceptType(h.getName());
  }
  
  protected boolean accept(QualifiedMandelName n)
  {
    return true;
  }

  protected boolean acceptType(QualifiedMandelName n)
  {
    return true;
  }

  protected boolean hasMandel(QualifiedMandelName n)
  {
    MandelHandle h=getScanner().getMandelInfo(n);
    if (h!=null) {
      try {
        MandelData md=h.getInfo();
        MandelInfo mi=md.getInfo();
        long nm=mi.getMCnt();
        long np=mi.getRX()*mi.getRY();
        if (nm*100/(double)np>0.1) return true;
      }
      catch (IOException ex) {
        // not found
      }
    }
    return false;
  }
}
