
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

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

/**
 *
 * @author Uwe Krüger
 */

public class DnDJList extends JList {
  static public boolean debug=false;

  private ListTransferHandler transfer;

  public DnDJList()
  {
    super();
    setup();
  }

  public DnDJList(Vector<?> listData)
  {
    super(listData);
    setup();
  }

  public DnDJList(Object[] listData)
  {
    super(listData);
    setup();
  }

  public DnDJList(ListModel dataModel)
  {
    super(dataModel);
    setup();
  }

  private void setup()
  {
    setTransferHandler(transfer=createListTransferHandler());
    setDropMode();
  }

  @Override
  public DnDListModel getModel()
  {
    return (DnDListModel)super.getModel();
  }

  @Override
  public void setModel(ListModel dataModel)
  {
    setModel((DnDListModel)dataModel);
  }

  public void setModel(DnDListModel dataModel)
  {
    super.setModel(dataModel);
    setDropMode();
  }

  //////////////////////////////////////////////////////////////////////////
  // Drag'n drop
  //////////////////////////////////////////////////////////////////////////

  private void setDropMode()
  {
    DropMode mode=null;
    if (getModel()!=null) mode=getModel().getDropMode();
    if (mode!=null) {
      if (debug) System.out.println("list: drop mode "+mode);
      setDragEnabled(true);
      setDropMode(mode);
    }
    else {
      if (debug) System.out.println("list: disable drop mode");
      setDragEnabled(false);
    }
  }

  protected ListTransferHandler createListTransferHandler()
  {
    return new ListTransferHandler();
  }

  /////////////////////////////////////////////////////////////////////////
  // Default Model

  public static class DefaultModel extends DefaultListModel
                                implements DnDListModel {

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
  // transfer
  
  protected class ListTransferHandler extends TransferHandler {
    private Transferable trans=null;

    private DnDJList getList(Component comp)
    {
      if (comp instanceof JList) return (DnDJList)comp;
      Container c=(Container)comp;
      return (DnDJList)c.getComponent(0);
    }
    
    @Override
    public boolean canImport(TransferHandler.TransferSupport info)
    {
      return getList(info.getComponent()).getModel().
                 canImport(new DnDListModel.TransferSupport(info));
    }
 
    @Override
    protected Transferable createTransferable(JComponent c)
    {
      return trans=getList(c).getModel().
              createTransferable(new DnDListModel.DragLocation((DnDJList)c));
    }

    @Override
    public int getSourceActions(JComponent c)
    {
       return getList(c).getModel().getSourceActions();
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

      if (getList(info.getComponent()).getModel().
              importData(new DnDListModel.TransferSupport(info))) {
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
        getList(c).getModel().exportDone(trans, action);
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
        // find item
        ListSelection sel=new ListSelection(DnDJList.this, e);
        if (debug) System.out.println("CTX POPUP at "+sel);
        ctxmenu.handleContextMenu(DnDJList.this, e, sel);
      }
    }
  }

  public interface ContextMenuHandler {
    void handleContextMenu(JComponent comp, MouseEvent evt, Selection sel);
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

  synchronized
  public ContextMenuHandler getContextMenuHandler()
  {
    return ctxmenu;
  }
}
