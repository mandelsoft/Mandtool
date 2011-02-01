
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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.TransferHandler;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;

/**
 *
 * @author Uwe Krüger
 */
public abstract class AbstractMandelListListModel
                extends AbstractMandelListModel<QualifiedMandelName>
                implements MandelListListModel {
  static public boolean debug=false;

  protected List<Action> actions;
  private MandelListListenerSupport listeners;

  protected AbstractMandelListListModel()
  {
    listeners=new MandelListListenerSupport();
  }

  public void removeMandelListListener(MandelListListener h)
  {
    listeners.removeMandelListListener(h);
  }

  public void addMandelListListener(MandelListListener h)
  {
    listeners.addMandelListListener(h);
  }

  protected void fireListChangeEvent()
  {
    listeners.fireChangeEvent(this);
  }

  @Override
  protected void fireContentsChanged(Object source, int index0, int index1)
  {
    super.fireContentsChanged(source, index0, index1);
    fireListChangeEvent();
  }

  @Override
  protected void fireIntervalAdded(Object source, int index0, int index1)
  {
    super.fireIntervalAdded(source, index0, index1);
    fireListChangeEvent();
  }

  @Override
  protected void fireIntervalRemoved(Object source, int index0, int index1)
  {
    super.fireIntervalRemoved(source, index0, index1);
    fireListChangeEvent();
  }


  /////////////////////////////////////////////////////////////////////////

  @Override
  protected QualifiedMandelName getQualifiedName(QualifiedMandelName elem)
  {
    return elem;
  }

  @Override
  protected boolean usesThumbnail(QualifiedMandelName name)
  {
    return getList().contains(name);
  }

  @Override
  protected QualifiedMandelName lookupElement(QualifiedMandelName name)
  {
    return name;
  }
  
  
  /////////////////////////////////////////////////////////////////////////

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
  
  
  /////////////////////////////////////////////////////////////////////////

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

  public Object getElementAt(int index)
  {
    return getList().get(index);
  }

  public int getSize()
  {
    if (getList()==null) return 0;
    return getList().size();
  }

  ///////////////////////////////////////////////////////////////////////////
  // Drag & Drop support by model
  ///////////////////////////////////////////////////////////////////////////

  public DropMode getDropMode()
  {
    return DropMode.INSERT;
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
    if (debug) System.out.println("action = "+action+"/MOVE="+TransferHandler.MOVE);

    MandelTransferable trans=(MandelTransferable)data;
    /*if (trans.getSource()!=getList()) { */
      QualifiedMandelName[] names=trans.getNames();
      if (action==TransferHandler.MOVE) {
        if (debug) System.out.println("  remove moved items");
        for (int i=names.length-1; i>=0; i--) {
          remove(names[i]);
        }
      }
//    }
//    else {
//      if (debug) System.out.println("ignore own list");
//    }
  }

  public boolean canImport(TransferSupport info)
  {
    if (isModifiable()) {
//      System.out.println("can import to list: "+
//                         Arrays.toString(info.getDataFlavors()));
      Transferable t=info.getTransferable();
      
      if (info.isDataFlavorSupported(MandelTransferable.mandelFlavor)) {
        try {
//          MandelTransferable trans=(MandelTransferable)(info.getTransferable().getTransferData(
//              MandelTransferable.mandelFlavor));
//          System.out.println("drop action: "+info.getDropAction()+
//                              "/COPY="+TransferHandler.COPY+" MOVE="+TransferHandler.MOVE);
//          if (trans.getSource()==getList() && info.getDropAction()==TransferHandler.COPY) {
//            return allowDuplicates();
//          }
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
        catch (Exception ex) {
          return false;
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
    boolean insert=dl.isInsert();
    int index=dl.getIndex();

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
        info.setDropAction(TransferHandler.COPY);
      }
      data=trans.getNames();

      for (int i=0; i<data.length; i++) {
        if (trans.getSource()==getList()) {
          int si=getList().indexOf(data[i]);
          remove(data[i]);
          if (si<=index) index--;
        }
        add(index++,data[i]);
      }
    }
    catch (Exception e) {
      try {
        String name=(String)t.getTransferData(DataFlavor.stringFlavor);
        QualifiedMandelName mn=QualifiedMandelName.create(name);
        if (mn==null) return false;
        add(index++,mn);
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