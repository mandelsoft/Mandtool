
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

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Uwe Krüger
 */

public class DnDJTree extends IJTree {
  private TreeTransferHandler transfer;

  public DnDJTree()
  {
    _setupDnDJTree();
  }

  public DnDJTree(Object[] value)
  {
    super(value);
    _setupDnDJTree();
  }

  public DnDJTree(Vector<?> value)
  {
    super(value);
    _setupDnDJTree();
  }

  public DnDJTree(Hashtable<?, ?> value)
  {
    super(value);
    _setupDnDJTree();
  }

  public DnDJTree(TreeNode root)
  {
    super(root);
    _setupDnDJTree();
  }

  public DnDJTree(TreeNode root, boolean asksAllowsChildren)
  {
    super(root, asksAllowsChildren);
    _setupDnDJTree();
  }

  public DnDJTree(TreeModel newModel)
  {
    super(newModel);
    _setupDnDJTree();
  }

  private void _setupDnDJTree()
  {
    setTransferHandler(transfer=createTreeTransferHandler());
    setDropMode();
  }

  @Override
  public DnDTreeModel getModel()
  {
    return (DnDTreeModel)super.getModel();
  }

  @Override
  public void setModel(TreeModel dataModel)
  {
    setModel((DnDTreeModel)dataModel);
  }

  @Override
  public void setModel(ITreeModel dataModel)
  {
    setModel((DnDTreeModel)dataModel);
  }

  public void setModel(DnDTreeModel dataModel)
  {
    super.setModel(dataModel);
    setDropMode();
  }

  private void setDropMode()
  {
    DropMode mode=null;
    if (getModel()!=null) mode=getModel().getDropMode();
    if (mode!=null) {
      setDragEnabled(true);
      setDropMode(mode);
    }
    else {
      setDragEnabled(false);
    }
  }

  protected TreeTransferHandler createTreeTransferHandler()
  {
    return new TreeTransferHandler();
  }

  /////////////////////////////////////////////////////////////////////////
  // Default Model
  /////////////////////////////////////////////////////////////////////////

  public static class DefaultModel extends IJTree.DefaultModel
                                   implements DnDTreeModel {

    public DefaultModel(TreeNode root)
    {
      super(root);
    }

    public DefaultModel(TreeNode root, boolean asksAllowsChildren)
    {
      super(root, asksAllowsChildren);
    }


    public DropMode getDropMode()
    {
      return null;
    }

    public int getSourceActions()
    {
      return 0;
    }

    public Transferable createTransferable(DragLocation loc)
    {
      return null;
    }

    public void exportDone(Transferable data, int action)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean canImport(TransferSupport info)
    {
      return false;
    }

    public boolean importData(TransferSupport info)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // drag'n drop
  /////////////////////////////////////////////////////////////////////////

  protected class TreeTransferHandler extends TransferHandler {
    private Transferable trans=null;

    @Override
    public boolean canImport(TransferHandler.TransferSupport info)
    {
      return ((DnDJTree)info.getComponent()).getModel().
                 canImport(new DnDTreeModel.TransferSupport(info));
    }

    /**
     * Bundle up the selected items in a single list for export.
     * Each line is separated by a newline.
     */
    @Override
    protected Transferable createTransferable(JComponent c)
    {
      return trans=((DnDJTree)c).getModel().
              createTransferable(new DnDTreeModel.DragLocation((DnDJTree)c));
    }

    @Override
    public int getSourceActions(JComponent c)
    {
       return ((DnDJTree)c).getModel().getSourceActions();
    }

    /**
     * Perform the actual import.  This demo only supports drag and drop.
     */
    @Override
    public boolean importData(TransferHandler.TransferSupport info)
    {
      if (!info.isDrop()) {
        return false;
      }

      if (((DnDJTree)info.getComponent()).getModel().
              importData(new DnDTreeModel.TransferSupport(info))) {
        return true;
      }
      else {
        //trans=null;
        return false;
      }
    }

    /**
     * Remove the items moved from the list.
     */
    @Override
    protected void exportDone(JComponent c, Transferable data, int action)
    {
      if (trans!=null)
        ((DnDJTree)c).getModel().exportDone(trans, action);
      trans=null;
    }
  }

  ///////////////////////////////////////////////////////////////////////
  // Context Menu
  ///////////////////////////////////////////////////////////////////////

  private class Listener extends MouseAdapter {
    @Override
    public void mouseReleased(MouseEvent e)
    {
      handlePopup(e);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      handlePopup(e);
    }

    public void handlePopup(MouseEvent e)
    {
      if (e.isPopupTrigger()&&ctxmenu!=null) {
        TreePath p=getPathForLocation(e.getX(), e.getY());
        ctxmenu.handleContextMenu(DnDJTree.this, e,p);
      }
    }
  }

  public interface ContextMenuHandler {
    void handleContextMenu(DnDJTree table, MouseEvent evt,
                           TreePath path);
  }

  private ContextMenuHandler ctxmenu;
  private Listener           listener;

  synchronized
  public void setContextMenuHandler(ContextMenuHandler h)
  {
    if (h==null) {
      if (ctxmenu!=null) removeMouseListener(listener);
    }
    else {
      if (ctxmenu==null) {
        if (listener==null) listener=new Listener();
        addMouseListener(listener);
      }
    }
    ctxmenu=h;
  }
}
