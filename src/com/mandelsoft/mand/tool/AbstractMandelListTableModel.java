
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

import com.mandelsoft.mand.cm.ColormapSourceFactory;
import com.mandelsoft.mand.tool.thumb.ImageChangeListener;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import com.mandelsoft.mand.image.MandelImage.Factory;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.thumb.AsyncThumbnailFactory;
import com.mandelsoft.util.DateTime;

/**
 *
 * @author Uwe Krüger
 */
public abstract class AbstractMandelListTableModel extends AbstractTableModel
                                                implements MandelListTableModel {
  public static boolean debug=false;

  protected List<Action> actions;
  private AsyncThumbnailFactory factory;
  private boolean modifiable;
  private boolean showlocation;

  ////////////////////////////////////////////////////////////////////
  private int[] columnMapping;

  static public final int NAME      = 0;
  static public final int QUALIFIER = 1;
  static public final int TYPE      = 2;
  static public final int LOCATION  = 3;
  static public final int TIME      = 4;

  static private final int[] m_local= new int[]{NAME,QUALIFIER,TYPE,TIME};
  static private final int[] m_located= new int[]{NAME,QUALIFIER,LOCATION,TYPE,TIME};

  ////////////////////////////////////////////////////////////////////
  protected AbstractMandelListTableModel()
  {
    columnMapping=m_local;
    factory=new AsyncThumbnailFactory(new AsyncThumbnailFactory.Client() {

      public boolean usesThumbnail(QualifiedMandelName name)
      {
        return AbstractMandelListTableModel.this.getList().contains(name);
      }

      public MandelScanner getMandelScanner()
      {
        return AbstractMandelListTableModel.this.getMandelScanner();
      }

    });
  }

  public boolean isShowLocation()
  {
    return showlocation;
  }

  public void setShowLocation(boolean showlocation)
  {
    boolean old=this.showlocation;
    this.showlocation=showlocation;
    if (showlocation) columnMapping=m_local;
    else columnMapping=m_located;
    if (old!=showlocation) fireTableStructureChanged();
  }

  public void removeImageChangeListener(ImageChangeListener l)
  {
    factory.removeImageChangeListener(l);
  }

  public void addImageChangeListener(ImageChangeListener l)
  {
    factory.addImageChangeListener(l);
  }

  public void setModifiable(boolean m)
  { this.modifiable=m;
  }

  public void setColormapSourceFactory(ColormapSourceFactory colmapfac)
  {
    factory.setColormapSourceFactory(colmapfac);
  }

  public ColormapSourceFactory getColormapSourceFactory()
  {
    return factory.getColormapSourceFactory();
  }

  public void setFactory(Factory factory)
  {
    this.factory.setFactory(factory);
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public void addAction(Action a)
  {
    if (actions==null) actions=new ArrayList<Action>();
    actions.add(a);
  }

  public void removeAction(Action a)
  {
    if (actions!=null) actions.remove(a);
  }

  public List<Action> getActions()
  {
    return actions;
  }

  protected void checkModifiable()
  {
    if (!isModifiable())
      throw new UnsupportedOperationException();
  }

  public void refresh()
  {
    refresh(false);
  }
  
  //////////////////////////////////////////////////////////////////////////


  public BufferedImage getThumbnail(int index, Dimension max)
  {
    QualifiedMandelName n=getQualifiedName(index);
    return factory.getThumbnail(n,max);
  }

  public BufferedImage getThumbnail(QualifiedMandelName n, Dimension max)
  {
    return factory.getThumbnail(n, max);
  }

  protected void cleanupThumbnail(QualifiedMandelName name)
  {
    factory.remove(name);
  }
  
  public MandelName getName(int index)
  {
    QualifiedMandelName n=getQualifiedName(index);
    return n==null?null:n.getMandelName();
  }

  public String getQualifier(int index)
  {
    QualifiedMandelName n=getQualifiedName(index);
    return n==null?null:n.getQualifier();
  }

  public String getLocation(int index)
  {
    QualifiedMandelName n=getQualifiedName(index);
    return n==null?null:n.getLabel();
  }

  public QualifiedMandelName getQualifiedName(int index)
  {
    return getList().get(index);
  }

  public MandelHandle getMandelHandle(int index)
  {
    return getMandelScanner().getMandelHandle(getList().get(index));
  }

  public MandelHandle getMandelData(int index) throws IOException
  {
    return getMandelScanner().getMandelData(getList().get(index));
  }

  public int getRowCount()
  {
    return getList().size();
  }

  public int getColumnCount()
  {
    return columnMapping.length;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    MandelHandle h;
    String q;
    long t;

    switch (columnMapping[columnIndex]) {
      case NAME:
        return getName(rowIndex);
      case QUALIFIER:
        q=getQualifier(rowIndex);
        if (q==null) return "";
        return q;
      case LOCATION:
        q=getLocation(rowIndex);
        if (q==null) return "";
        return q;
      case TYPE:
        h=getMandelScanner()==null?
          null:getMandelScanner().getMandelHandle(getList().get(rowIndex));
        return h==null?"Not found":h.getHeader().getTypeDesc();
      case TIME:
        h=getMandelScanner()==null?
          null:getMandelScanner().getMandelHandle(getList().get(rowIndex));
        t=h==null?0:h.getFile().getLastModified();
        if (t==0) return null;
        return new DateTime(t);
        //return date.format(new Date(t));
    }
    return null;
  }

  @Override
  public String getColumnName(int column)
  {
    switch (columnMapping[column]) {
      case NAME:
        return "Name";
      case QUALIFIER:
        return "Variant";
      case TYPE:
        return "Description";
      case LOCATION:
        return "Location";
      case TIME:
        return "Time Modified";
    }
    return null;
  }

  @Override
  public Class getColumnClass(int column)
  {
    switch (columnMapping[column]) {
      case NAME:
        return MandelName.class;
      case QUALIFIER:
        return String.class;
      case LOCATION:
        return String.class;
      case TYPE:
        return String.class;
      case TIME:
        return DateTime.class;
    }
    return null;
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
    QualifiedMandelName[] names=new QualifiedMandelName[indices.length];
    for (int i=0; i<indices.length; i++) {
      names[i]=getQualifiedName(indices[i]);
    }
    return new MandelTransferable(getList(), names);
  }

  public void exportDone(Transferable data, int action)
  {
    if (debug) System.out.println("action = "+action+"/"+TransferHandler.MOVE);

    MandelTransferable trans=(MandelTransferable)data;
    /*if (trans.getSource()!=getList())*/ {
      QualifiedMandelName[] names=trans.getNames();
      if (action==TransferHandler.MOVE) {
        if (debug) System.out.println("  remove moved items");
        for (int i=names.length-1; i>=0; i--) {
          remove(names[i]);
        }
      }
    }
    //else System.out.println("ignore own list");
  }

  public boolean canImport(TransferSupport info)
  {
    if (isModifiable()) {
      Transferable t=info.getTransferable();
      if (info.isDataFlavorSupported(MandelTransferable.mandelFlavor)) {
        try {
          if (t.isDataFlavorSupported(MandelFolderTransferable.folderFlavor)) {
            // if folders are dropped, use the content but keep the original folder
            if (debug) System.out.println("enforce copy");
            info.setDropAction(TransferHandler.COPY);
          }
          
          MandelTransferable trans=(MandelTransferable)t.getTransferData(
                MandelTransferable.mandelFlavor);
          if (trans.getSource()==getList()) {
            if (debug) System.out.println("drop to self");
            info.setDropAction(TransferHandler.COPY);
          }
          return true;
        }
        catch (UnsupportedFlavorException ex) {
          return false;
        }
        catch (IOException ex) {
          Logger.getLogger(AbstractMandelListTableModel.class.getName()).
                  log(Level.SEVERE, null, ex);
        }
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
    QualifiedMandelName[] data;
    MandelTransferable trans;

    // Perform the actual import.
    try {
      trans=(MandelTransferable)t.getTransferData(
              MandelTransferable.mandelFlavor);

      if (t.isDataFlavorSupported(MandelFolderTransferable.folderFlavor)) {
        // if folders are dropped, use the content but keep the original folder
        if (debug) System.out.println("enforce copy");
        info.setDropAction(TransferHandler.COPY);
      }

      if (trans.getSource()==getList()) {
        if (debug) System.out.println("drop to self");
        return false;
      }
      data=trans.getNames();

      for (int i=0; i<data.length; i++) {
        add(data[i]);
      }
    }
    catch (Exception e) {
      try {
        String name=(String)t.getTransferData(DataFlavor.stringFlavor);
        QualifiedMandelName mn=QualifiedMandelName.create(name);
        if (mn==null) return false;
        add(mn);
      }
      catch (Exception ex) {
        return false;
      }
    }
    return true;
  }

  ///////////////////////////////////////////////////////////////////////////
  // additional actions
  ///////////////////////////////////////////////////////////////////////////

  protected class ClearAction extends AbstractAction {

    public ClearAction()
    {
      super("Clear");
    }

    public void actionPerformed(ActionEvent e)
    {
      clear();
    }
  }


}