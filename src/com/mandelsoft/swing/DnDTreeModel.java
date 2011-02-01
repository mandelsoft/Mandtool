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
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

/**
 *
 * @author Uwe Krueger
 */
public interface DnDTreeModel extends ITreeModel  {
  static public class DropLocation extends TransferHandler.DropLocation {
    private JTree.DropLocation location;
    private IJTree             tree;

    public DropLocation(TransferHandler.TransferSupport trans)
    {
      super(trans.getDropLocation().getDropPoint());
      this.location=(JTree.DropLocation)trans.getDropLocation();
      this.tree=(IJTree)trans.getComponent();
    }

    public int getChildIndex()
    {
      return location.getChildIndex();
    }

    public TreePath getPath()
    {
      return location.getPath();
    }

    public IJTree getComponent()
    {
      return tree;
    }

    @Override
    public String toString()
    {
      return location.toString();
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

    public IJTree getComponent()
    {
      return location.getComponent();
    }
  }

  static public class DragLocation {
    private IJTree tree;

    public DragLocation(IJTree t)
    {
      tree=t;
    }

    public IJTree getComponent()
    {
      return tree;
    }

    public TreePath[] getSelectionPaths()
    {
      return tree.getSelectionPaths();
    }

    public TreePath getSelectionPath()
    {
      return tree.getSelectionPath();
    }

    public int getSelectionCount()
    {
      return tree.getSelectionCount();
    }

    public TreePath getLeadSelectionPath()
    {
      return tree.getLeadSelectionPath();
    }

    public Object getLastSelectedPathComponent()
    {
      return tree.getLastSelectedPathComponent();
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
