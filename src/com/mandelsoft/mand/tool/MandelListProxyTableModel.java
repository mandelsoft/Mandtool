
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.TableModelListener;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */

public class MandelListProxyTableModel extends DefaultMandelListTableModel {
  protected MandelListTableModel model;

  public MandelListProxyTableModel(MandelListTableModel model)
  {
    this.model=model;
  }

  @Override
  public MandelScanner getMandelScanner()
  {
    return model.getMandelScanner();
  }

  @Override
  public List<Action> getActions()
  {
    List<Action> actions1=model.getActions();
    List<Action> actions2=super.getActions();
    if (actions1!=null) {
      if (actions2!=null) {
        List<Action> r=new ArrayList<Action>();
        if (actions1!=null) r.addAll(actions1);
        if (actions2!=null) r.addAll(actions2);
        return r;
      }
      return actions1;
    }
    return actions2;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    model.setValueAt(aValue, rowIndex, columnIndex);
  }

  @Override
  public void removeTableModelListener(TableModelListener l)
  {
    model.removeTableModelListener(l);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return model.isCellEditable(rowIndex, columnIndex);
  }

//  @Override
//  public TableModelListener[] getTableModelListeners()
//  {
//    return model.getTableModelListeners();
//  }
//
//  @Override
//  public <T extends EventListener> T[] getListeners(Class<T> listenerType)
//  {
//    return model.getListeners(listenerType);
//  }
//
//  @Override
//  public void fireTableStructureChanged()
//  {
//    model.fireTableStructureChanged();
//  }
//
//  @Override
//  public void fireTableRowsUpdated(int firstRow, int lastRow)
//  {
//    model.fireTableRowsUpdated(firstRow, lastRow);
//  }
//
//  @Override
//  public void fireTableRowsInserted(int firstRow, int lastRow)
//  {
//    model.fireTableRowsInserted(firstRow, lastRow);
//  }
//
//  @Override
//  public void fireTableRowsDeleted(int firstRow, int lastRow)
//  {
//    model.fireTableRowsDeleted(firstRow, lastRow);
//  }
//

  @Override
  public void fireTableDataChanged()
  {
    model.fireTableDataChanged();
  }

//  @Override
//  public void fireTableChanged(TableModelEvent e)
//  {
//    model.fireTableChanged(e);
//  }
//
//  @Override
//  public void fireTableCellUpdated(int row, int column)
//  {
//    model.fireTableCellUpdated(row, column);
//  }
//
//  @Override
//  public int findColumn(String columnName)
//  {
//    return model.findColumn(columnName);
//  }

  @Override
  public void addTableModelListener(TableModelListener l)
  {
    model.addTableModelListener(l);
  }

  @Override
  public void setList(MandelList list)
  {
    model.setList(list);
  }

  @Override
  public void setDuplicates(boolean m)
  {
    model.setDuplicates(m);
  }

  @Override
  public void remove(QualifiedMandelName name)
  {
    model.remove(name);
  }

  @Override
  public void refresh(Environment env)
  {
    model.refresh(env);
  }

  @Override
  public void refresh()
  {
    model.refresh();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    return model.getValueAt(rowIndex, columnIndex);
  }

  @Override
  public int getRowCount()
  {
    return model.getRowCount();
  }

  @Override
  public String getQualifier(int index)
  {
    return model.getQualifier(index);
  }

  @Override
  public QualifiedMandelName getQualifiedName(int index)
  {
    return model.getQualifiedName(index);
  }

  @Override
  public MandelName getName(int index)
  {
    return model.getName(index);
  }

  @Override
  public MandelHandle getMandelHandle(int index)
  {
    return model.getMandelHandle(index);
  }

  @Override
  public MandelHandle getMandelData(int index) throws IOException
  {
    return model.getMandelData(index);
  }

  @Override
  public MandelList getList()
  {
    return model.getList();
  }

  @Override
  public String getColumnName(int column)
  {
    return model.getColumnName(column);
  }

  @Override
  public int getColumnCount()
  {
    return model.getColumnCount();
  }

  @Override
  public Class getColumnClass(int column)
  {
    return model.getColumnClass(column);
  }

  @Override
  public void clear()
  {
    model.clear();
  }

  @Override
  public void addAll(QualifiedMandelName[] names)
  {
    model.addAll(names);
  }

  @Override
  public void add(QualifiedMandelName name)
  {
    model.add(name);
  }

   @Override
  public void addAll(int index, QualifiedMandelName[] names)
  {
    model.addAll(index, names);
  }

  @Override
  public void add(int index, QualifiedMandelName name)
  {
    model.add(index, name);
  }
}
