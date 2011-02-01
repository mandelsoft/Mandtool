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
package com.mandelsoft.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Uwe Krüger
 */

public class IJTable extends JTable {

  public IJTable(Object[][] rowData, Object[] columnNames)
  {
    super(rowData, columnNames);
    _setupIJTable();
  }

  public IJTable(Vector rowData, Vector columnNames)
  {
    super(rowData, columnNames);
    _setupIJTable();
  }

  public IJTable(int numRows, int numColumns)
  {
    super(numRows, numColumns);
    _setupIJTable();
  }

  public IJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
  {
    super(dm, cm, sm);
    _setupIJTable();
  }

  public IJTable(TableModel dm, TableColumnModel cm)
  {
    super(dm, cm);
    _setupIJTable();
  }

  public IJTable(TableModel dm)
  {
    super(dm);
    _setupIJTable();
  }

  public IJTable()
  {
    _setupIJTable();
  }

  private void _setupIJTable()
  {
    addPropertyChangeListener("ancestor",new ComponentPropertyListener());
  }



  private TableModel origModel;
  private TableColumnModel origCol;
  private ListSelectionModel origSel;

  protected void componentBound()
  {
    if (origModel!=null && dataModel==null) {
      dataModel=origModel;
      dataModel.addTableModelListener(this);
      origModel=null;
    }
    if (origCol!=null && columnModel==null) {
      columnModel=origCol;
      columnModel.addColumnModelListener(this);
      origCol=null;
    }
    if (origSel!=null && selectionModel==null) {
      selectionModel=origSel;
      selectionModel.addListSelectionListener(this);
      origSel=null;
    }
  }

  protected void componentUnbound()
  {
    //System.out.println("----- unbound");
    origModel=dataModel;
    origCol=columnModel;
    origSel=selectionModel;

    clearSelection();
    super.setModel(new DnDJTable.DefaultModel());
    if (origModel!=null) origModel.removeTableModelListener(this);

    columnModel=createDefaultColumnModel();
    if (origCol!=null) origCol.removeColumnModelListener(this);

    selectionModel=new DefaultListSelectionModel();
    if (origSel!=null) origSel.removeListSelectionListener(this);
  }

  private class ComponentPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getNewValue()==null) {
        componentUnbound();
      }
      else {
        componentBound();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // util
  /////////////////////////////////////////////////////////////////////////
  public void scrollToVisible(int rowIndex, int vColIndex) {
    if (!(getParent() instanceof JViewport)) {
        return;
    }
    JViewport viewport = (JViewport)getParent();

    // This rectangle is relative to the table where the
    // northwest corner of cell (0,0) is always (0,0).
    Rectangle rect = getCellRect(rowIndex, vColIndex, true);

    // The location of the viewport relative to the table
    Point pt = viewport.getViewPosition();

    // Translate the cell location so that it is relative
    // to the view, assuming the northwest corner of the
    // view is (0,0)
    rect.setLocation(rect.x-pt.x, rect.y-pt.y);

    // Scroll the area into view
    viewport.scrollRectToVisible(rect);
  }
}
