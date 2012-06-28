
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
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Uwe Krüger
 */

public class DnDJTable extends IJTable {
  static public boolean debug=false;

  private TableTransferHandler transfer;

  public DnDJTable()
  {
    super(new DefaultModel());
    setup();
  }

  public DnDJTable(DnDTableModel dm, TableColumnModel cm, ListSelectionModel sm)
  {
    super(dm, cm, sm);
    setup();
  }

  public DnDJTable(DnDTableModel dm, TableColumnModel cm)
  {
    super(dm, cm);
    setup();
  }

  public DnDJTable(DnDTableModel dm)
  {
    super(dm);
    setup();
  }

  private void setup()
  {
    setTransferHandler(transfer=createTableTransferHandler());
    setDropMode();
  }

  @Override
  public DnDTableModel getModel()
  {
    return (DnDTableModel)super.getModel();
  }

  @Override
  public void setModel(TableModel dataModel)
  {
    setModel((DnDTableModel)dataModel);
  }

  public void setModel(DnDTableModel dataModel)
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

  protected TableTransferHandler createTableTransferHandler()
  {
    return new TableTransferHandler();
  }

  /////////////////////////////////////////////////////////////////////////
  // Default Model
  /////////////////////////////////////////////////////////////////////////

  public static class DefaultModel extends DefaultTableModel
                             implements DnDTableModel {

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

  protected class TableTransferHandler extends TransferHandler {
    private Transferable trans=null;

    /**
     * We only support importing strings.
     */
    @Override
    public boolean canImport(TransferHandler.TransferSupport info)
    {
      return ((DnDJTable)info.getComponent()).getModel().
                 canImport(new DnDTableModel.TransferSupport(info));
    }

    /**
     * Bundle up the selected items in a single list for export.
     * Each line is separated by a newline.
     */
    @Override
    protected Transferable createTransferable(JComponent c)
    {
      return trans=((DnDJTable)c).getModel().
              createTransferable(new DnDTableModel.DragLocation((DnDJTable)c));
    }

    /**
     * We support both copy and move actions.
     */
    @Override
    public int getSourceActions(JComponent c)
    {
       return ((DnDJTable)c).getModel().getSourceActions();
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

      if (((DnDJTable)info.getComponent()).getModel().
              importData(new DnDTableModel.TransferSupport(info))) {
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
        ((DnDJTable)c).getModel().exportDone(trans, action);
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
        // find row of click and
        int row=rowAtPoint(e.getPoint());
        // find column of click and
        int col=columnAtPoint(e.getPoint());

        TableSelection sel=new TableSelection(DnDJTable.this,row,col);
        if (debug) System.out.println("CTX POPUP at "+sel);
        ctxmenu.handleContextMenu(DnDJTable.this, e, sel);
      }
    }
  }

  public interface ContextMenuHandler {
    void handleContextMenu(JComponent comp, MouseEvent evt,
                           Selection sel);
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
