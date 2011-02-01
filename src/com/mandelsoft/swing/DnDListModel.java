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

import java.awt.Container;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

/**
 *
 * @author Uwe Krueger
 */
public interface DnDListModel extends ListModel  {
  static public class DropLocation extends TransferHandler.DropLocation {
    private JList              list;
    private int                index;
    private boolean            insert;

    public DropLocation(TransferHandler.TransferSupport trans)
    {
      super(trans.getDropLocation().getDropPoint());
      if (trans.getDropLocation() instanceof JList.DropLocation) {
        JList.DropLocation location=(JList.DropLocation)trans.getDropLocation();
        this.list=(JList)trans.getComponent();
        this.index=location.getIndex();
        this.insert=location.isInsert();
      }
      else {
        Container c=(Container)trans.getComponent();
        this.list=(JList)c.getComponent(0);
        this.index=1;
        this.insert=true;
      }

    }

    public DropLocation(Point dropPoint, JList list, int index, boolean insert)
    {
      super(dropPoint);
      this.list=list;
      this.index=index;
      this.insert=insert;
    }

    public boolean isInsert()
    {
      return insert;
    }

    public int getIndex()
    {
      return index;
    }

    public JList getComponent()
    {
      return list;
    }

    @Override
    public String toString()
    {
      return getClass().getName()
             + "[dropPoint=" + getDropPoint() + ","
             + "index=" + getIndex() + ","
             + "insert=" + isInsert() + "]";
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

    public JList getComponent()
    {
      return location.getComponent();
    }
  }

  static public class DragLocation {
    private JList list;

    public DragLocation(JList t)
    {
      list=t;
    }

    public JList getComponent()
    {
      return list;
    }
    
    public int[] getSelectedRows()
    {
      int n[]=null;
      int[] t=list.getSelectedIndices();
      if (t!=null) {
        n=new int[t.length];
        for (int i=0; i<t.length; i++)
          n[i]=t[i];
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
