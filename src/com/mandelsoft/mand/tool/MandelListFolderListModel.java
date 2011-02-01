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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeListener;
import javax.swing.DropMode;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.swing.DnDListModel.DragLocation;
import com.mandelsoft.swing.DnDListModel.TransferSupport;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListFolderListModel
             extends AbstractMandelListModel<MandelListFolder> {
  private TreeListEventAdapter adapter;
  private MandelListFolderBrowserModel model;
  private MandelScanner scanner;

  public MandelListFolderListModel(MandelListFolderTreeModel model,
                                   MandelScanner scanner )
  {
    adapter=new TreeListEventAdapter();
    this.scanner=scanner;
    this.model=new MandelListFolderBrowserModel(model);
    this.model.addTreeModelListener(adapter);
  }

  @Override
  public void refresh(boolean soft)
  {
    getFolderTreeModel().getFolderTree().refresh();
  }

  public MandelListFolderTreeModel getFolderTreeModel()
  {
    return model.getFolderTreeModel();
  }
  
  public void setModel(MandelListFolderTreeModel model)
  {
    this.model.setFolderTreeModel(model);
  }

  public void setActiveFolder(MandelListFolder f)
  {
    model.setActiveFolder(f);
  }

  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    model.addPropertyChangeListener(l);
  }

  public void addPropertyChangeListener(String propertyName,
                                        PropertyChangeListener listener)
  {
    model.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    model.removePropertyChangeListener(l);
  }

  public void removeTreeModelListener(TreeModelListener l)
  {
    model.removeTreeModelListener(l);
  }

  public void addTreeModelListener(TreeModelListener l)
  {
    model.addTreeModelListener(l);
  }

  public String getActivePathName()
  {
    return model.getActivePathName();
  }

  public TreePath getActivePath()
  {
    return model.getActivePath();
  }

  public MandelListFolder getActiveFolder()
  {
    return model.getActiveFolder();
  }

  public static TreePath getParentPath(TreeModelEvent e, TreePath path)
  {
    return MandelListFolderBrowserModel.getParentPath(e, path);
  }

  @Override
  public boolean isModifiable()
  {
    return model.isPathModifiable(getActivePath());
  }

  public void removeFolder(MandelListFolder node)
  {
    model.removeFolder(node);
  }

  public MandelListFolder insertFolder(String name, MandelListFolder parent)
  {
    return model.insertFolder(name, parent);
  }
  
  /////////////////////////////////////////////////////////////////////////
  @Override
  protected MandelScanner getMandelScanner()
  {
    return scanner;
  }

  @Override
  protected QualifiedMandelName getQualifiedName(MandelListFolder elem)
  {
    return elem.getThumbnailName();
  }

  @Override
  protected boolean usesThumbnail(QualifiedMandelName name)
  {
    return lookupElement(name)!=null;
  }

  @Override
  protected MandelListFolder lookupElement(QualifiedMandelName name)
  {
    for (MandelListFolder f:model.getActiveFolder().allfolders()) {
      QualifiedMandelName n=f.getThumbnailName();
      if (n!=null && n.equals(name)) return f;
    }
    return null;
  }

  /////////////////////////////////////////////////////////////////////////
  public Object getElementAt(int index)
  {
    return model.getActiveFolder().get(index);
  }

  public int getSize()
  {
    return model.getActiveFolder().size();
  }

  ////////////////////////////////////////////////////////////////
  // drag'n drop support
  ////////////////////////////////////////////////////////////////
  private static boolean debug=false;

  public DropMode getDropMode()
  {
    return DropMode.ON_OR_INSERT;
  }

  /**
   * We support both copy and move actions.
   */
  public int getSourceActions()
  {
    return TransferHandler.COPY_OR_MOVE;
  }


  public boolean canImport(TransferSupport info)
  {
    MandelFolderTransferable t;
    TreePath subpath;
    
    DropLocation dl=info.getDropLocation();
    boolean insert=dl.isInsert();
    int     index =dl.getIndex();
    //System.out.println("index = "+index);
    MandelListFolder folder=index<getSize()?(MandelListFolder)getElementAt(index)
                                        :null;
    
    if (insert) {
      if (!isModifiable()) return false;
    }
    else {
      subpath=getActivePath().pathByAddingChild(folder);
      if (!model.isPathModifiable(subpath)) return false;
    }

    // both drop on and in between
    if (info.isDataFlavorSupported(MandelFolderTransferable.folderFlavor)) {
      //System.out.println("support folder");
      if (!insert && folder.isLeaf()) return false;
      try {
        t=(MandelFolderTransferable)info.getTransferable().
                getTransferData(MandelFolderTransferable.folderFlavor);
      }
      catch (Exception e) {
        return false;
      }

      while (folder!=null) {
        for (MandelListFolder f :t.getFolders()) {
          if (f==folder) return false;
        }
        folder=folder.getParent();
      }
      return true;
    }

   
    // drop on only
    if (!insert) {
      if (folder.getMandelList()==null) return false;
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

  

  /**
   * Bundle up the selected items in a single list for export.
   * Each line is separated by a newline.
   */
  public Transferable createTransferable(DragLocation loc)
  {
    int[] indices=loc.getSelectedRows();
    TreePath[] paths=new TreePath[indices.length];
    for (int i=0; i<indices.length; i++) {
      paths[i]=getActivePath().pathByAddingChild(getElementAt(indices[i]));
    }

    int cnt=0;
    for (int i=0; i<paths.length; i++) {
      if (model.isPathTransferable(paths[i])) cnt++;
    }
    if (cnt==0) return null;
    MandelListFolder[] folders=new MandelListFolder[cnt];
    cnt=0;
    for (int i=0; i<paths.length; i++) {
      if (model.isPathTransferable(paths[i])) {
        folders[cnt++]=(MandelListFolder)paths[i].getLastPathComponent();
      }
    }
    return new MandelFolderTransferable(model.getFolderTree(), folders);
  }

  /**
   * Perform the actual import.  This demo only supports drag and drop.
   */
  public boolean importData(TransferSupport info)
  {
    DropLocation dl=info.getDropLocation();
    boolean insert=dl.isInsert();
    int     index =dl.getIndex();
    MandelListFolder folder;

    if (insert) {
      folder=getActiveFolder();
    }
    else {
      folder=(MandelListFolder)getElementAt(index);
      index=-1;
    }

    boolean done=true;

    Transferable t=info.getTransferable();
    QualifiedMandelName[] data;
    MandelListFolder[] dropfolders;

    if (info.getDropAction()==TransferHandler.LINK) {
      info.setDropAction(TransferHandler.COPY);
    }
    // Perform the actual import.
    try {
      MandelFolderTransferable trans=(MandelFolderTransferable)t.getTransferData(
              MandelFolderTransferable.folderFlavor);

      dropfolders=trans.getFolders();

      if (debug) System.out.println("import folder");
      if (trans.getSource()!=model.getFolderTree()||
              info.getDropAction()!=TransferHandler.MOVE) {
        // handle foreign folders
        model.insertFolders(index, dropfolders, folder);
      }
      else {
        // handle own folders
        model.moveFolders(index, dropfolders, folder);
        done=false;
      }
    }
    catch (Exception e) {
      check(e);
      try {
        MandelTransferable trans;
        trans=(MandelTransferable)t.getTransferData(
                MandelTransferable.mandelFlavor);
        if (trans.getSource()==folder.getMandelList()) {
          if (debug) System.out.println("self drop");
          return false;
        }
        data=trans.getNames();
        if (debug) System.out.println("import mandel");
        model.addAll(folder, data);
      }
      catch (Exception ey) {
        check(ey);
        try {
          String name=(String)t.getTransferData(DataFlavor.stringFlavor);
          if (debug) System.out.println("import string");
          QualifiedMandelName mn=QualifiedMandelName.create(name);
          if (mn==null) return false;
          model.add(folder, mn);
        }
        catch (Exception ex) {
          check(ex);
          return false;
        }
      }
    }
    return done;
  }

  private void check(Exception e)
  {
    if (!(e instanceof UnsupportedFlavorException)) {
      e.printStackTrace(System.out);
    }
  }

  /**
   * Remove the items moved from the list.
   */
  public void exportDone(Transferable data, int action)
  {
    if (debug) System.out.println("action = "+action+"/"+TransferHandler.MOVE);
    // skip for internal move
    MandelFolderTransferable trans=(MandelFolderTransferable)data;
    MandelListFolder[] folders=trans.getFolders();
    if (action==TransferHandler.MOVE && trans.getSource()!=model.getFolderTree()) {
      for (int i=folders.length-1; i>=0; i--) {
        model.removeFolder(folders[i]);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // List Event Adaptation
  /////////////////////////////////////////////////////////////////////////

  private class TreeListEventAdapter implements TreeModelListener {

    private int getIndex(Object o)
    { int ix=0;
      
      for (MandelListFolder f:model.getActiveFolder().allfolders()) {
        if (f==o) return ix;
        ix++;
      }
      return -1;
    }

    public void treeNodesChanged(TreeModelEvent e)
    {
      TreePath p=e.getTreePath();

      if (e.getChildren()==null) {
        if (p.getLastPathComponent()==model.getActiveFolder()) {
          fireContentsChanged(MandelListFolderListModel.this,0,Integer.MAX_VALUE);
        }
        if (p.getPathCount()>1 && p.getParentPath().getLastPathComponent()==model.getActiveFolder()) {
          int ix=getIndex(e.getTreePath().getLastPathComponent());
          if (ix>=0) fireContentsChanged(MandelListFolderListModel.this,ix,ix);
        }
      }
      else {
        if (p.getLastPathComponent()==model.getActiveFolder()) {
          for (int ix:e.getChildIndices()) {
            fireContentsChanged(MandelListFolderListModel.this,ix,ix);
          }
        }
      }
    }

    public void treeNodesInserted(TreeModelEvent e)
    {
      TreePath p=e.getTreePath();

      System.out.println("inserted: "+p.getLastPathComponent());
      if (p.getLastPathComponent()==model.getActiveFolder()) {
        for (int ix:e.getChildIndices()) {
          fireIntervalAdded(MandelListFolderListModel.this, ix, ix);
        }
      }
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
      TreePath p=e.getTreePath();

      if (p.getLastPathComponent()==model.getActiveFolder()) {
        for (int ix:e.getChildIndices()) {
          fireIntervalRemoved(MandelListFolderListModel.this, ix, ix);
        }
      }
    }

  }
}
