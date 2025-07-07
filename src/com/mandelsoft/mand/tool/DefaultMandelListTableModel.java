
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
import javax.swing.event.ListDataListener;

/**
 *
 * @author Uwe Krüger
 */
public class DefaultMandelListTableModel extends AbstractMandelListTableModel
        implements MandelListTableModel {

  protected MandelListListModel model;
  private MandelListListenerSupport listeners;
  private boolean inupdate;

  protected DefaultMandelListTableModel() {
    listeners = new MandelListListenerSupport();
  }

  public DefaultMandelListTableModel(MandelList list, MandelScanner scanner) {
    this();
    this.model = new DefaultMandelListListModel(list, scanner);
    if (scanner instanceof ContextMandelScanner) {
      this.setShowLocation(((ContextMandelScanner) scanner).getContext().hasNested());
    }
  }

  @Override
  public void removeMandelListListener(MandelListListener h) {
    listeners.removeMandelListListener(h);
  }

  @Override
  public void addMandelListListener(MandelListListener h) {
    listeners.addMandelListListener(h);
  }

  @Override
  public void addListDataListener(ListDataListener l) {
    model.addListDataListener(l);
  }

  @Override
  public void removeListDataListener(ListDataListener l) {
    model.removeListDataListener(l);
  }

  @Override
  public void fireTableDataChanged() {
    super.fireTableDataChanged();
    fireListChanged();
  }

  protected void fireListChanged() {
    listeners.fireChangeEvent(this);
  }

  @Override
  public void setModifiable(boolean m) {
    super.setModifiable(m);
    model.setModifiable(m);
  }
  
  public void setDuplicates(boolean m) {
    model.setDuplicates(m);
  }

  public boolean allowDuplicates() {
    return model.allowDuplicates();
  }

  public MandelScanner getMandelScanner() {
    return model.getMandelScanner();
  }

  protected boolean isInUpdate() {
    return inupdate;
  }

  public void add(QualifiedMandelName name) {
    checkModifiable();
    if (model.allowDuplicates() || !model.getList().contains(name)) {
      int max = model.getSize();
      model.add(name);
      if (model.getSize() != max) {
        try {
          model.getList().save();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(null,
                  "Cannot save list: " + ex,
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
        }
        int index = max;
        fireTableRowsInserted(index, index);
        fireListChanged();
        //fireTableDataChanged();
      }
    }
  }

  public void add(int index, QualifiedMandelName name) {
    if (index >= model.getSize()) {
      add(name);
    }
    checkModifiable();
    if (model.allowDuplicates() || !model.getList().contains(name)) {
      int max = model.getSize();
      model.add(index, name);
      if (model.getSize() != max) {
        try {
          model.getList().save();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(null,
                  "Cannot save list: " + ex,
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
        }
        fireTableRowsInserted(index, index);
        fireListChanged();
      }
    }
  }

  public void addAll(QualifiedMandelName[] names) {
    int cnt = 0;

    checkModifiable();
    int max = model.getSize();
    for (QualifiedMandelName name : names) {
      if (model.allowDuplicates() || !model.getList().contains(name)) {
        model.add(name);
        if (model.getSize() != max + cnt) {
          cnt++;
        }
      }
    }
    if (cnt != 0) {
      try {
        model.getList().save();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: " + ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      int index = model.getSize();
      fireTableRowsInserted(index - cnt, index - 1);
      fireListChanged();
      //fireTableDataChanged();
    }
  }

  public void addAll(int index, QualifiedMandelName[] names) {
    int cnt = 0;

    if (index >= model.getSize()) {
      addAll(names);
    }
    checkModifiable();
    int max = model.getSize();
    for (QualifiedMandelName name : names) {
      if (model.allowDuplicates() || !model.getList().contains(name)) {
        model.add(index + cnt, name);
        if (model.getSize() != max + cnt) {
          cnt++;
        }
      }
    }
    if (cnt != 0) {
      try {
        model.getList().save();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: " + ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      fireTableRowsInserted(index, index + cnt - 1);
      fireListChanged();
    }
  }

  public void remove(QualifiedMandelName name) {
    int index;

    checkModifiable();
    index = model.getList().indexOf(name);
    if (index >= 0) {
      model.remove(name);
      if (!model.getList().contains(name)) {
        cleanupThumbnail(name);
      }
      try {
        model.getList().save();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null,
                "Cannot save list: " + ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
      }
      this.fireTableRowsDeleted(index, index);
      fireListChanged();
      //fireTableDataChanged();
    }
  }

  public MandelList getList() {
    return model.getList();
  }

  public void setList(MandelList list) {
    this.model.setList(list);
    fireTableDataChanged();
  }

  public void refresh(boolean soft) {
    if (!inupdate) {
      inupdate = true;
      try {
        if (debug) {
          System.out.println("refresh " + this + " soft=" + soft);
        }
        model.refresh(soft);
        fireTableDataChanged();
      } finally {
        inupdate = false;
      }
    }
  }

  public void refresh(Environment env) {
    if (!inupdate) {
      inupdate = true;
      try {
        env.refresh(model);
        fireTableDataChanged();
      } finally {
        inupdate = false;
      }
    }
  }

  public void clear() {
    getList().clear();
    fireTableDataChanged();
  }

  @Override
  public int getSize() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public Object getElementAt(int index) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }
}
