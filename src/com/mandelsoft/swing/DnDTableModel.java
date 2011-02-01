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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

/**
 *
 * @author Uwe Krueger
 */
public interface DnDTableModel extends TableModel  {
  static public class DropLocation extends TransferHandler.DropLocation {
    private JTable.DropLocation location;
    private JTable              table;

    public DropLocation(TransferHandler.TransferSupport trans)
    {
      super(trans.getDropLocation().getDropPoint());
      this.location=(JTable.DropLocation)trans.getDropLocation();
      this.table=(JTable)trans.getComponent();
    }

    public boolean isInsertRow()
    {
      return location.isInsertRow();
    }

    public boolean isInsertColumn()
    {
      return location.isInsertColumn();
    }

    public int getRow()
    {
      return table.convertRowIndexToModel(location.getRow());
    }

    public int getColumn()
    {
      return table.convertColumnIndexToModel(location.getColumn());
    }

    public JTable getComponent()
    {
      return table;
    }

    @Override
    public String toString()
    {
      return getClass().getName()
             + "[dropPoint=" + getDropPoint() + ","
             + "row=" + getRow() + ","
             + "column=" + getColumn() + ","
             + "insertRow=" + isInsertRow() + ","
             + "insertColumn=" + isInsertColumn() + "]";
    }
  }

  static public class TransferSupport {
    TransferHandler.TransferSupport orig;
    DropLocation location;

    TransferSupport(TransferHandler.TransferSupport orig)
    {
      this.orig=orig;
      this.location=new DropLocation(orig);
    }

    public void setShowDropLocation(boolean showDropLocation)
    {
      orig.setShowDropLocation(showDropLocation);
    }

    public void setDropAction(int dropAction)
    {
      orig.setDropAction(dropAction);
    }

    public boolean isDrop()
    {
      return orig.isDrop();
    }

    public boolean isDataFlavorSupported(DataFlavor df)
    {
      return orig.isDataFlavorSupported(df);
    }

    public int getUserDropAction()
    {
      return orig.getUserDropAction();
    }

    public Transferable getTransferable()
    {
      return orig.getTransferable();
    }

    public int getSourceDropActions()
    {
      return orig.getSourceDropActions();
    }

    public DropLocation getDropLocation()
    {
      return location;
    }

    public int getDropAction()
    {
      return orig.getDropAction();
    }

    public DataFlavor[] getDataFlavors()
    {
      return orig.getDataFlavors();
    }

    public JTable getComponent()
    {
      return location.getComponent();
    }
  }

  static public class DragLocation {
    private JTable table;

    public DragLocation(JTable t)
    {
      table=t;
    }

    public JTable getComponent()
    {
      return table;
    }
    
    public int[] getSelectedRows()
    {
      int n[]=null;
      int[] t=table.getSelectedRows();
      if (t!=null) {
        n=new int[t.length];
        for (int i=0; i<t.length; i++)
          n[i]=table.convertRowIndexToModel(t[i]);
      }
      return n;
    }

    public int[] getSelectedColums()
    {
      int n[]=null;
      int[] t=table.getSelectedColumns();
      if (t!=null) {
        n=new int[t.length];
        for (int i=0; i<t.length; i++)
          n[i]=table.convertColumnIndexToModel(t[i]);
      }
      return n;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // addtional model interface to support drag and drop
  /////////////////////////////////////////////////////////////////////////

  DropMode getDropMode();

  /**
   * return supported frag source actions.
   * @return
   */
  int getSourceActions();

  /**
   * create a transferable containing the selected list items
   * @param loc the selected items
   * @return the transferable containing the items
   */
  Transferable createTransferable(DragLocation loc);

  /**
   * finally handle the export after a successful import
   * @param data the originally created transferable
   * @param action
   */
  void exportDone(Transferable data, int action);

  /**
   * check whether a drop is possible
   * @param info
   * @return
   */
  boolean canImport(TransferSupport info);

  /**
   * finally do the drop
   * @param index the selected index in terms of the model
   * @param info
   * @return
   */
  boolean importData(TransferSupport info);
}
