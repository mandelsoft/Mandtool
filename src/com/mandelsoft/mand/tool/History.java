
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

import com.mandelsoft.mand.QualifiedMandelName;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.ArrayMandelList;

/**
 *
 * @author Uwe Krüger
 */

public class History extends IndexedMandelListModel {
  private ListSelectionModel selmodel;
  private int                current;

  public History(MandelScanner scanner)
  {
    super(new ArrayMandelList(), scanner);
    setModifiable(true);
    setDuplicates(true);
    selmodel=new DefaultListSelectionModel();
    addAction(new ClearAction());
  }

  public ListSelectionModel getSelectionModel()
  {
    return selmodel;
  }

  public int getCurrent()
  {
    return current;
  }
  
  public int setCurrent(int index)
  {
    try {
      return current;
    }
    finally {
      current=index;
    }
  }
  
  public void add(QualifiedMandelName name)
  {
    try {
      QualifiedMandelName cur=this.getList().get(current);
      if (debug) {
        System.out.printf("*************** add %d %s from (%d %s)\n", 
                this.getList().size(), name, current, cur );
      }
      if (this.getList().get(current).equals(name)) {
        super.add(name);
        return;
      }
    }
    catch (IndexOutOfBoundsException ex) {
    }
    current = this.getList().size();
    super.add(name);
  }
  
  @Override
  public void clear()
  {
    QualifiedMandelName cur=null;
    try {
      cur=getList().get(current);
    }
    catch (IndexOutOfBoundsException ex) {
    }
    getList().clear();
    if (cur!=null) getList().add(cur);
    current=0;
    fireTableDataChanged();
  }
}
