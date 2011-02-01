
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
import com.mandelsoft.mand.scan.ContextMandelScanner;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krüger
 */
public class DefaultMandelListTableModel extends AbstractMandelListTableModel
                                      implements MandelListTableModel {
  protected MandelList list;
  protected MandelScanner scanner;
  private MandelListListenerSupport listeners;
  private boolean duplicates;
  private boolean inupdate;

  protected DefaultMandelListTableModel()
  {
    listeners=new MandelListListenerSupport();
  }
  
  public DefaultMandelListTableModel(MandelList list, MandelScanner scanner)
  {
    this();
    this.scanner=scanner;
    this.list=list;
    if (scanner instanceof ContextMandelScanner) {
      this.setShowLocation(((ContextMandelScanner)scanner).getContext().hasNested());
    }
  }

  public void removeMandelListListener(MandelListListener h)
  {
    listeners.removeMandelListListener(h);
  }

  public void addMandelListListener(MandelListListener h)
  {
    listeners.addMandelListListener(h);
  }

  @Override
  public void fireTableDataChanged()
  {
    super.fireTableDataChanged();
    fireListChanged();
  }

  protected void fireListChanged()
  {
    listeners.fireChangeEvent(this);
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

  protected boolean isInUpdate()
  {
    return inupdate;
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
        fireTableRowsInserted(index,index);
        fireListChanged();
        //fireTableDataChanged();
      }
    }
  }

  public void add(int index, QualifiedMandelName name)
  {
    if (index>=list.size()) add(name);
    checkModifiable();
    if (duplicates || !list.contains(name)) {
      int max=list.size();
      list.add(index,name);
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
        fireTableRowsInserted(index, index);
        fireListChanged();
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
      fireTableRowsInserted(index-cnt,index-1);
      fireListChanged();
      //fireTableDataChanged();
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
      fireTableRowsInserted(index,index+cnt-1);
      fireListChanged();
    }
  }

  public void remove(QualifiedMandelName name)
  {
    int index;

    checkModifiable();
    index=list.indexOf(name);
    if (index>=0) {
      list.remove(index);
      if (!list.contains(name))
        cleanupThumbnail(name);
      try {
        list.save();
      }
      catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: "+ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      this.fireTableRowsDeleted(index, index);
      fireListChanged();
      //fireTableDataChanged();
    }
  }

  public MandelList getList()
  { return list;
  }

  public void setList(MandelList list)
  {
    this.list=list;
    fireTableDataChanged();
  }

  public void refresh(boolean soft)
  {
    if (!inupdate) {
      inupdate=true;
      try {
        if (debug) {
            System.out.println("refresh "+this+" soft="+soft);
         }
        list.refresh(soft);
        fireTableDataChanged();
      }
      finally {
        inupdate=false;
      }
    }
  }

  public void refresh(Environment env)
  {
    if (!inupdate) {
      inupdate=true;
      try {
        env.refresh(list);
        fireTableDataChanged();
      }
      finally {
        inupdate=false;
      }
    }
  }

  public void clear()
  {
    getList().clear();
    fireTableDataChanged();
  }
}