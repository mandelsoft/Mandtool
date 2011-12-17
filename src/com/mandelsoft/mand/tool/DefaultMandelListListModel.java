
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
import javax.swing.JOptionPane;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krüger
 */
public class DefaultMandelListListModel extends AbstractMandelListListModel {
  protected MandelList list;
  protected MandelScanner scanner;
  protected boolean duplicates;

  protected DefaultMandelListListModel()
  {
  }
  
  public DefaultMandelListListModel(MandelList list, MandelScanner scanner)
  {
    this.scanner=scanner;
    this.list=list;
  }

  public void setDuplicates(boolean m)
  { this.duplicates=m;
  }

  public boolean allowDuplicates()
  {
    return duplicates;
  }
  
  public MandelScanner getMandelScanner()
  {
    return scanner;
  }

  public void add(QualifiedMandelName name)
  {
    checkModifiable();
    if (duplicates || !list.contains(name)) {
      int max=list.size();
      list.add(name);
      if (list.size()!=max) {
        try {
          list.save();
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(null,
                                        "Cannot save list: "+ex,
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        int index=max;
        fireIntervalAdded(this, index, index);
      }
    }
  }

  public void add(int index, QualifiedMandelName name)
  {
    if (index>=list.size()) add(name);
    else {
      checkModifiable();
      if (duplicates||!list.contains(name)) {
        int max=list.size();
        list.add(index, name);
        if (list.size()!=max) {
          try {
            list.save();
          }
          catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                                          "Cannot save list: "+ex,
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
          }
          fireIntervalAdded(this, index, index);
        }
      }
    }
  }

  public void addAll(QualifiedMandelName[] names)
  { int cnt=0;

    checkModifiable();
    int max=list.size();
    for (QualifiedMandelName name:names) {
      if (duplicates||!list.contains(name)) {
        list.add(name);
        if (list.size()!=max+cnt) {
          cnt++;
        }
      }
    }
    if (cnt!=0) {
      try {
        list.save();
      }
      catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: "+ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      int index=list.size();
      fireIntervalAdded(this, index-cnt,index-1);
    }
  }

  public void addAll(int index, QualifiedMandelName[] names)
  { int cnt=0;

    if (index>=list.size()) addAll(names);
    checkModifiable();
    int max=list.size();
    for (QualifiedMandelName name:names) {
      if (duplicates||!list.contains(name)) {
        list.add(index+cnt,name);
        if (list.size()!=max+cnt) {
          cnt++;
        }
      }
    }
    if (cnt!=0) {
      try {
        list.save();
      }
      catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: "+ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      fireIntervalAdded(this,index,index+cnt-1);
    }
  }

  public void remove(QualifiedMandelName name)
  {
    remove(list.indexOf(name));
  }

  public void remove(int index)
  {
    checkModifiable();
    if (index>=0) {
      QualifiedMandelName name=list.remove(index);
      if (!list.contains(name))
        factory.remove(name);
      try {
        list.save();
      }
      catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: "+ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      fireIntervalRemoved(this, index, index);
    }
  }

  public MandelList getList()
  { return list;
  }

  public void setList(MandelList list)
  { MandelList old=this.list;
    this.list=list;
    if (old!=null && list!=null) {
      fireRefresh(old.size(),list.size());
    }
  }

  public void refresh(boolean soft)
  {
    int old=list.size();
    list.refresh(soft);
    fireRefresh(old,list.size());
  }

  public void refresh(Environment env)
  {
    int old=list.size();
    env.refresh(list);
    fireRefresh(old,list.size());
  }

  public void clear()
  {
    checkModifiable();
    int old=list.size();
    getList().clear();
    fireRefresh(old,list.size());
  }
}