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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DropMode;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.swing.TreeModelListenerSupport;

/**
 *
 * @author Uwe Krüger
 */

public abstract class MandelListFolderTreeModelSupport
                extends TreeModelListenerSupport
                implements MandelListFolderTreeModel {

  static private final boolean debug=false;
  
  protected Map<MandelListFolder,MandelListTableModel> listmodels;

  //
  // temporary internal states
  // for event interaction
  //
  protected boolean moving;

  protected MandelListFolderTreeModelSupport()
  {
    super();
    listmodels=new HashMap<MandelListFolder,MandelListTableModel>();
  }

  public void clear()
  {
    listmodels.clear();
  }
  
  ////////////////////////////////////////////////////////////////
  //folder tree
  ////////////////////////////////////////////////////////////////

  public void save()
  {
    try {
      System.out.println("save "+getFolderTree());
      getFolderTree().save();
    }
    catch (IOException ex) {

    }
  }

  public MandelListFolder getChild(MandelListFolder parent, String name)
  {
    for (MandelListFolder f:parent) {
      if (f.getName().equals(name)) return f;
    }
    return null;
  }


  /////////////////////////////////////////////////////////////////////////
  // tree model
  /////////////////////////////////////////////////////////////////////////

  public Object getChild(Object parent, int index)
  {
    Object c=((MandelListFolder)parent).get(index);
    if (debug) System.out.println("get child for "+parent+": "+c);
    return c;
  }

  public int getChildCount(Object parent)
  {
    return ((MandelListFolder)parent).size();
  }

  public int getIndexOfChild(Object parent, Object child)
  {
    return ((MandelListFolder)parent).indexOf(child);
  }

  public boolean isLeaf(Object node)
  {
    MandelListFolder f=(MandelListFolder)node;
    return f.isLeaf() || f.size()==0;
  }

  public void valueForPathChanged(TreePath path, Object newValue)
  {
    if (newValue==null || newValue.toString().isEmpty()) return;
    MandelListFolder f=(MandelListFolder)path.getLastPathComponent();

    if (debug) System.out.println(f.getClass()+" value changed: "+path(path)+": "+newValue);
    //new Throwable().printStackTrace();
    f.setName(newValue.toString());

    fireTreeNodesChanged(path,null,null);
    try {
      f.save();
    }
    catch (IOException ex) {
      System.out.println("cannot save: "+ex);
    }
  }

  public MandelListFolder getRoot()
  {
    return getFolderTree().getRoot();
  }

  public MandelListTableModel getRootModel()
  {
    return getMandelListModel(getRoot());
  }

  public MandelListFolderTreeModel getEffectiveFolderTreeModel()
  {
    return this;
  }

  ////////////////////////////////////////////////////////////////
  // extended interface
  ////////////////////////////////////////////////////////////////

   public String convertValueToText(Object value, boolean selected,
                                   boolean expanded, boolean leaf, int row,
                                   boolean hasFocus)
  {
    return ((MandelListFolder)value).getName();
  }

  public boolean isPathEditable(TreePath path)
  {
    if (!isModifiable()) return false;
    if (path.getPathCount()==1) return false;
    return true;
  }

  public boolean isPathModifiable(TreePath path)
  {
    MandelListFolder f=(MandelListFolder)path.getLastPathComponent();
    return !f.isLeaf() && isModifiable(f);
  }

  public boolean isPathListModifiable(TreePath path)
  {
    MandelListFolder f=(MandelListFolder)path.getLastPathComponent();
    if (!f.hasMandelList()) return false;
    if (!this.getMandelListModel(f).isModifiable()) return false;
    return isListModifiable(f); // disallow modification via folder
  }

  protected boolean isModifiable(MandelListFolder f)
  {
    return isModifiable();
  }

  public boolean isPathTransferable(TreePath path)
  {
    return true;
  }

  ////////////////////////////////////////////////////////////////
  // mandel list model handling
  ////////////////////////////////////////////////////////////////

  public MandelListTableModel getMandelListModel(Object folder)
  { MandelListFolder f=(MandelListFolder)folder;
    MandelListTableModel m=listmodels.get(f);
    if (m==null) {
      checkFolder(f);
      if (f.getMandelList()==null) return null;
      m=createMandelListModel(f);
      m.setModifiable(isListModifiable(f));
      listmodels.put(f, m);
    }
    return m;
  }

  protected boolean isListModifiable(MandelListFolder f)
  {
    return isModifiable(f);
  }

  protected void cleanup(MandelListFolder f)
  {
    listmodels.remove(f);
    for (MandelListFolder s:f) {
      cleanup(s);
    }
  }

  ///////////////////////////////////////////////////////////////////////
  // list ops
  ///////////////////////////////////////////////////////////////////////

  public void addAll(MandelListFolder f, QualifiedMandelName[] list)
  {
    MandelListTableModel m=getMandelListModel(f);
    if (m==null)
      throw new IllegalArgumentException(" no list supported for folder "+f.getPath());
    m.addAll(list);
  }

  public void add(MandelListFolder f, QualifiedMandelName name)
  {
    MandelListTableModel m=getMandelListModel(f);
    if (m==null)
      throw new IllegalArgumentException(" no list supported for folder "+f.getPath());
    m.add(name);
  }

  public void remove(MandelListFolder f, QualifiedMandelName name)
  {
    MandelListTableModel m=getMandelListModel(f);
    if (m==null)
      throw new IllegalArgumentException(" no list supported for folder "+f.getPath());
    m.remove(name);
  }

  ////////////////////////////////////////////////////////////////
  // folder ops
  ////////////////////////////////////////////////////////////////

  public MandelListFolder insertFolder(String name, MandelListFolder parent)
  {
    MandelListFolder child=parent.createSubFolder(name);
    if (child!=null) {
      if (debug) System.out.println("created sub folder of "+parent.getName()+
              ": "+child.getName());
      int[] newIndexs=new int[1];
      newIndexs[0]=parent.indexOf(child);
      foldersWereInserted(parent, newIndexs);
    }
    return child;
  }

  public void moveFolders(int index, MandelListFolder[] folders,
                          MandelListFolder parent)
  {
    try {
      moving=true;

      int[] newIndexs=new int[folders.length];
      MandelListFolder s;

      for (int i=0; i<folders.length; i++) {
        checkFolder(folders[i]);
        if (folders[i].getParent()==parent) {
          //local order changed -> adjust insertion index
          if (index>parent.indexOf(folders[i])) index--;
        }
        _removeFolder(folders[i],true);
      }
      for (int i=0; i<folders.length; i++) {
        if (index<0||index>=parent.size()) {
          newIndexs[i]=parent.size();
        }
        else {
          newIndexs[i]=index++;
        }

        parent.add(newIndexs[i], folders[i]);
      }
      foldersWereInserted(parent, newIndexs);
    }
    finally {
      moving=false;
    }
  }

  public boolean insertFolders(int index, MandelListFolder[] folders,
                                          MandelListFolder parent)
  {
    int[] newIndexs=new int[folders.length];
    MandelListFolder s;
    boolean add=false;
    for (int i=0; i<folders.length; i++) {
      if (index<0 || index>=parent.size()) {
        newIndexs[i]=parent.size();
      }
      else {
        newIndexs[i]=index++;
      }
      add|=_insertFolder(newIndexs[i],folders[i],parent);
    }
    if (add) foldersWereInserted(parent, newIndexs);
    return add;
  }

  private boolean _insertFolder(int index, MandelListFolder folder,
                                        MandelListFolder parent)
  {
    if (debug) System.out.println("insert folder "+folder.getName()+" into "+
                                          parent.getName()+" at "+index);
    boolean add=false;

     add=true;
    MandelListFolder dst=parent.createSubFolder(index, folder.getName());
    dst.getMandelList().addAll(folder.getMandelList());
    for (MandelListFolder s:folder) {
      _insertFolder(dst.size(), s, dst);
    }

    return add;
  }

  protected void _removeFolder(MandelListFolder node, boolean moved)
  {
    MandelListFolder parent=node.getParent();

    if (parent==null)
      throw new IllegalArgumentException("folder does not have a parent.");

    int[] childIndices=new int[1];
    Object[] removedChildren=new Object[1];

    childIndices[0]=parent.indexOf(node);
    removedChildren[0]=node;
    parent.remove(childIndices[0]);
    foldersWereRemoved(parent,childIndices,removedChildren);
    if (!moved) foldersWereDeleted(parent,childIndices,removedChildren);
  }

  public void removeFolder(MandelListFolder node)
  {
    _removeFolder(node,false);
  }

  public void foldersWereInserted(MandelListFolder node, int[] childIndices)
  {
    if (listenerList!=null&&node!=null&&childIndices!=null&&childIndices.length>0) {
      int cCount=childIndices.length;
      Object[] newChildren=new Object[cCount];

      for (int counter=0; counter<cCount; counter++)
        newChildren[counter]=node.get(childIndices[counter]);
      fireTreeNodesInserted(getPathToRoot(node), childIndices,
                            newChildren);
      save();
    }
  }

  public void foldersWereRemoved(MandelListFolder node, int[] childIndices,
                                                 Object[] removedChildren)
  {
    for (Object o:removedChildren) {
      MandelListFolder f=(MandelListFolder)o;
      cleanup(f);
    }
    if (listenerList!=null&&node!=null&&childIndices!=null&&childIndices.length>0) {

      fireTreeNodesRemoved(getPathToRoot(node), childIndices,
                            removedChildren);
    }
    save();
  }

  protected void foldersWereDeleted(MandelListFolder node, int[] childIndices,
                                                 Object[] removedChildren)
  {
    if (listenerList!=null&&node!=null&&childIndices!=null&&childIndices.length>0) {

      fireFoldersDeleted(getPathToRoot(node), childIndices,
                            removedChildren);
    }
  }

  public void setThumbnailName(MandelListFolder folder, QualifiedMandelName name)
  {
    checkFolder(folder);
    folder.setThumbnailName(name);
    System.out.println("set thumbnail of "+folder.getPath()+" to "+name);
    try {
      folder.save();
    }
    catch (IOException ex) {
      System.out.println("cannot save: "+ex);
    }
    this.fireTreeNodesChanged(getPathToRoot(folder), null, null);
  }


  public boolean isMoving()
  {
    return moving;
  }

  ////////////////////////////////////////////////////////////////
  // support
  ////////////////////////////////////////////////////////////////

//  public boolean isLocalFolder(MandelListFolder f)
//  {
//    while (f!=null&&f!=getRoot()) f=f.getParent();
//    return f==getRoot();
//  }

  public boolean isLocalFolder(MandelListFolder f)
  {
    dump(f);
    return f.getMandelListFolderTree()==getFolderTree();
  }

  protected void checkFolder(MandelListFolder f)
  {
    if (!isLocalFolder(f))
      throw new IllegalArgumentException("none local folder");
  }

  protected void dump(MandelListFolder f)
  {
    if (debug) {
      while (f!=null) {
        System.out.println("* "+f);
        f=f.getParent();
      }
      System.out.println("--");
    }
  }

  ////////////////////////////////////////////////////////////////
  // list model
  protected MandelListTableModel createMandelListModel(MandelListFolder f)
  {
    return new TreeMandelListModel(f.getMandelList(), getMandelScanner());
  }

  protected abstract MandelScanner getMandelScanner();

  protected class TreeMandelListModel extends DefaultMandelListTableModel {

    public TreeMandelListModel(MandelList list, MandelScanner scanner)
    {
      super(list, scanner);
    }

    @Override
    public void refresh()
    {
      super.refresh();
      fireTreeStructureChanged();
    }

    @Override
    public void refresh(Environment env)
    {
      super.refresh(env);
      fireTreeStructureChanged();
    }

    private void fireTreeStructureChanged()
    {
      TreePath path=new TreePath(getRoot());
      MandelListFolderTreeModelSupport.this.fireTreeStructureChanged(path);
    }
  }

  ////////////////////////////////////////////////////////////////
  // drag'n drop support
  ////////////////////////////////////////////////////////////////
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
    if (isModifiable()) {
      MandelFolderTransferable t;
      DropLocation dl=info.getDropLocation();
      MandelListFolder folder=(MandelListFolder)(dl.getPath().
              getLastPathComponent());
      boolean insert=dl.getChildIndex()>=0;

      // both drop on and in between
      if (info.isDataFlavorSupported(MandelFolderTransferable.folderFlavor)) {
        //System.out.println("support folder");
        if (!isPathModifiable(dl.getPath())) return false;
        if (!insert && folder.isLeaf()) return false;
        try {
          t=(MandelFolderTransferable)info.getTransferable().
                  getTransferData(MandelFolderTransferable.folderFlavor);
        }
        catch (Exception e) {
          return false;
        }

        while (folder!=null) {
          for (MandelListFolder f:t.getFolders()) {
            if (f==folder) return false;
          }
          folder=folder.getParent();
        }
        if (info.getDropAction()==TransferHandler.LINK) {
          info.setDropAction(TransferHandler.COPY);
        }
        return true;

      }

      // drop on only
      if (!insert) {
        if (folder.getMandelList()==null) return false;
        if (!isPathListModifiable(dl.getPath())) return false;
        if (info.isDataFlavorSupported(MandelTransferable.mandelFlavor)) {
          return true;
        }
        if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          try {
            String data=(String)info.getTransferable().getTransferData(
                    DataFlavor.stringFlavor);
            if (QualifiedMandelName.create(data)!=null) {
              if (info.getDropAction()==TransferHandler.LINK) {
                info.setDropAction(TransferHandler.COPY);
              }
              return true;
            }
          }
          catch (Exception ex) {
          }
        }
      }
    }
    else {
      //System.out.println("not active");
    }
    return false;
  }

  /**
   * Bundle up the selected items in a single list for export.
   * Each line is separated by a newline.
   */
  public Transferable createTransferable(DragLocation loc)
  {
    TreePath[] paths=loc.getSelectionPaths();
    int cnt=0;
    for (int i=0; i<paths.length; i++) {
      if (isPathTransferable(paths[i])) cnt++;
    }
    if (cnt==0) return null;
    MandelListFolder[] folders=new MandelListFolder[cnt];
    cnt=0;
    for (int i=0; i<paths.length; i++) {
      if (isPathTransferable(paths[i])) {
        folders[cnt++]=(MandelListFolder)paths[i].getLastPathComponent();
      }
    }
    if (debug) System.out.println("create transferable folder");
    return new MandelFolderTransferable(getFolderTree(), folders);
  }

  /**
   * Perform the actual import.  This demo only supports drag and drop.
   */
  public boolean importData(TransferSupport info)
  {
    DropLocation dl=info.getDropLocation();
    TreePath path=dl.getPath();
    boolean insert=dl.getChildIndex()>=0;
    boolean done=true;

    MandelListFolder folder=((MandelListFolder)path.getLastPathComponent());
    Transferable t=info.getTransferable();
    QualifiedMandelName[] data;
    MandelListFolder[] dropfolders;

//    if (info.getDropAction()==TransferHandler.LINK) {
//      info.setDropAction(TransferHandler.COPY);
//    }
    // Perform the actual import.
    try {
      MandelFolderTransferable trans=(MandelFolderTransferable)t.getTransferData(
              MandelFolderTransferable.folderFlavor);

      dropfolders=trans.getFolders();

      if (debug) System.out.println("import folder");
      if (trans.getSource()!=getFolderTree()||
              info.getDropAction()!=TransferHandler.MOVE) {
        // handle foreign folders
        insertFolders(dl.getChildIndex(), dropfolders, folder);
      }
      else {
        // handle own folders
        moveFolders(dl.getChildIndex(), dropfolders, folder);
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
        addAll(folder, data);
      }
      catch (Exception ey) {
        check(ey);
        try {
          String name=(String)t.getTransferData(DataFlavor.stringFlavor);
          if (debug) System.out.println("import string");
          QualifiedMandelName mn=QualifiedMandelName.create(name);
          if (mn==null) return false;
          add(folder, mn);
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
    if (action==TransferHandler.MOVE && trans.getSource()!=getFolderTree()) {
      for (int i=folders.length-1; i>=0; i--) {
        removeFolder(folders[i]);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // util
  /////////////////////////////////////////////////////////////////////////

  static public String path(TreePath path)
  {
    String s=((MandelListFolder)path.getLastPathComponent()).getName();
    if (path.getParentPath()!=null)
      return path(path.getParentPath())+"/"+s;
    return s;
  }

  static public TreePath getPathToRoot(MandelListFolder node)
  {
    if (node.getParent()==null) return new TreePath(node);
    return getPathToRoot(node.getParent()).pathByAddingChild(node);
  }

  /////////////////////////////////////////////////////////////////////////
  //MandelListFolderTreeModel
  /////////////////////////////////////////////////////////////////////////

  public void addMandelListFolderTreeModelListener(MandelListFolderTreeModelListener l)
  {
    listenerList.add(MandelListFolderTreeModelListener.class, l);
  }

  public void removeMandelListFolderTreeModelListener(MandelListFolderTreeModelListener l)
  {
    listenerList.remove(MandelListFolderTreeModelListener.class, l);
  }

  public MandelListFolderTreeModelListener[] getMandelListFolderTreeModelListeners()
  {
    return getListeners(MandelListFolderTreeModelListener.class);
  }

  protected void fireFoldersDeleted(TreePath path,
                                    int[] childIndices,
                                    Object[] children)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    TreeModelEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new TreeModelEvent(this, path,
                               childIndices, children);
        ((MandelListFolderTreeModelListener)listeners[i+1]).foldersDeleted(e);
      }
    }
  }

  protected void fireFoldersDeleted(TreeModelEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==MandelListFolderTreeModelListener.class) {
        // Lazily create the event:
        ((MandelListFolderTreeModelListener)listeners[i+1]).foldersDeleted(e);
      }
    }
  }
}
