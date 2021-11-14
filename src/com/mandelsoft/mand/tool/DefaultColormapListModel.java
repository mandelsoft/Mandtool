
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.DropMode;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.tool.ColormapTransferable.ColormapInfo;
import com.mandelsoft.mand.util.ColormapList;
import com.mandelsoft.swing.DnDTableModel.DragLocation;
import com.mandelsoft.swing.DnDTableModel.DropLocation;
import com.mandelsoft.swing.DnDTableModel.TransferSupport;

/**
 *
 * @author Uwe Krüger
 */

public class DefaultColormapListModel extends AbstractTableModel
                                      implements ColormapListModel {
  static public boolean debug=false;
  
  private ColormapList maps;
  private boolean modifiable;

  public DefaultColormapListModel(ColormapList list)
  {
    maps=list;
  }

  public void setModifiable(boolean m)
  { this.modifiable=m;
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public ColormapList getList()
  {
    return maps;
  }

  public void refresh()
  {
    maps.refresh();
    this.fireTableDataChanged();
  }

  public boolean add(ColormapName name, Colormap cm)
  {
    if (maps.add(name, cm)) {
      this.fireTableDataChanged();
      return true;
    }
    return false;
  }

  public boolean add(ColormapName name, Colormap cm, ColormapHandle h)
  {
    if (maps.add(name, cm, h)) {
      this.fireTableDataChanged();
      return true;
    }
    return false;
  }

  public boolean remove(int index)
  {
    if (maps.remove(index)!=null) {
      this.fireTableDataChanged();
      return true;
    }
    return false;
  }

  public boolean remove(ColormapName name)
  {
    if (maps.remove(name)) {
      this.fireTableDataChanged();
      return true;
    }
    return false;
  }

  public AbstractFile getFile(int index) throws IOException
  {
    return getColormapHandle(index).getFile();
  }

  public ColormapHandle getColormapHandle(int index) throws IOException
  {
    return maps.getColormapHandle(index);
  }

  public ColormapName getName(int index)
  {
    return maps.getName(index);
  }

  public Colormap getColormap(int index) throws IOException
  {
    return maps.get(index);
  }

  public int getRowCount()
  {
    return maps.size();
  }

  public int getColumnCount()
  {
    return 3;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    Colormap cm;

    switch (columnIndex) {
      case 0:
        return getName(rowIndex);
      case 1:
        try {
          cm=getColormap(rowIndex);
          if (cm==null) return 0;
          return cm.getSize();
        }
        catch (IOException ex) {
        }
      case 2:
        try {
          ColormapHandle h=getColormapHandle(rowIndex);
          if (h==null) return "<none>";
          if (h.getFile()==null) return "memory";
          if (h.getHeader().isColormap())
            return "file";
          return h.getHeader().getTypeDesc();
        }
        catch (IOException ex) {
        }
    }
    return null;
  }

  @Override
  public String getColumnName(int column)
  {
    switch (column) {
      case 0:
        return "Name";
      case 1:
        return "Size";
      case 2:
        return "Type";
    }
    return null;
  }

  @Override
  public Class getColumnClass(int column)
  {
    switch (column) {
      case 0:
        return ColormapName.class;
      case 1:
        return Integer.class;
      case 2:
        return String.class;
    }
    return null;
  }

  @Override
  public void addTableModelListener(TableModelListener l)
  {
    //System.out.println("########## add colmap list listener");
    super.addTableModelListener(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l)
  {
    //System.out.println("########## remove colmap list listener");
    super.removeTableModelListener(l);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Drag & Drop support by model
  ///////////////////////////////////////////////////////////////////////////

  public DropMode getDropMode()
  {
    return DropMode.INSERT_ROWS;
  }

  public int getSourceActions()
  {
    return TransferHandler.COPY_OR_MOVE;
  }

  public Transferable createTransferable(DragLocation loc)
  {
    int[] indices=loc.getSelectedRows();
    ColormapInfo[] names=new ColormapInfo[indices.length];
    for (int i=0; i<indices.length; i++) {
      try {
      names[i]=new ColormapInfo(getName(indices[i]),
                                getColormap(indices[i]),
                                getColormapHandle(indices[i]));
      }
      catch (IOException io) {

      }
    }
    return new ColormapTransferable(getList(), names);
  }

  public void exportDone(Transferable data, int action)
  {
    if (debug) System.out.println("action = "+action+"/"+TransferHandler.MOVE);

    ColormapTransferable trans=(ColormapTransferable)data;
    if (trans.getSource()!=getList()) {
      ColormapInfo[] names=trans.getColormaps();
      if (action==TransferHandler.MOVE) {
        for (int i=names.length-1; i>=0; i--) {
          remove(names[i].getName());
        }
      }
    }
  }

  public boolean canImport(TransferSupport info)
  {
    if (isModifiable()) {
      if (info.isDataFlavorSupported(MandelTransferable.mandelFlavor)) {
        return true;
      }
      if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        try {
          String data=(String)info.getTransferable().getTransferData(
                  DataFlavor.stringFlavor);
          if (QualifiedMandelName.create(data)!=null) return true;
        }
        catch (Exception ex) {
        }
      }
    }
    return false;
  }

  public boolean importData(TransferSupport info)
  {
    if (!info.isDrop()) {
      return false;
    }

    DropLocation dl=info.getDropLocation();
    boolean insert=dl.isInsertRow()||dl.isInsertColumn();

    // Get the string that is being dropped.
    Transferable t=info.getTransferable();
    ColormapInfo[] data;
    ColormapTransferable trans;

    // Perform the actual import.
    try {
      trans=(ColormapTransferable)t.getTransferData(
              ColormapTransferable.colormapFlavor);

      if (trans.getSource()==getList()) {
        if (debug) System.out.println("drop to self");
        return false;
      }
      data=trans.getColormaps();

      for (int i=0; i<data.length; i++) {
        add(data[i].getName(),data[i].getColormap(),data[i].getColormapHandle());
      }
    }
    catch (Exception e) {
      return false;
    }
    return true;
  }
}