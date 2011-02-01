
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

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Stack;
import javax.swing.DropMode;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.UniqueArrayMandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.swing.DnDTreeModel.DragLocation;
import com.mandelsoft.swing.DnDTreeModel.TransferSupport;
import com.mandelsoft.swing.TreeModelListenerSupportBase;

/**
 *
 * @author Uwe Krüger
 */

public class MandelListFolderBrowserModel extends TreeModelListenerSupportBase
                                          implements MandelListFolderTreeModel {
  static private boolean debug=false;

  static public final String PROP_MODIFIABLE = "modifiable";
  static public final String PROP_MODEL = "model";
  static public final String PROP_ACTIVE_NAME = "activeName";
  static public final String PROP_ACTIVE_PATH = "activePath";
  static public final String PROP_ACTIVE_FOLDER = "activeFolder";

  private MandelListFolderTreeModel fmodel;
  
  //
  // active list display
  //
  private TreePath              activepath;
  private MandelListFolder      activefolder;
  private String                activename;

  private MandelListTableModel  lmodel;

  private MandelListTableModel  lempty;
  private MandelListFolderTreeModel fempty;
  private ForwardingListener    listener;

  private boolean               modifiable;

  public MandelListFolderBrowserModel(MandelListFolderTreeModel fmodel)
  {
    lempty=new DefaultMandelListTableModel(new UniqueArrayMandelList(),null);
    fempty=new DefaultMandelListFolderTreeModel("empty",lempty);
    listener=new ForwardingListener();
    setFolderTreeModel(fmodel);
  }

  public void clear()
  {
    setFolderTreeModel(null);
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public void setModifiable(boolean modifiable)
  {
    System.out.println("browser model modifiable: "+modifiable);
    //new Throwable().printStackTrace();
    if (this.modifiable!=modifiable) {
      this.modifiable=modifiable;
      firePropertyChange(PROP_MODIFIABLE,!modifiable,modifiable);
    }
  }

  public void setFolderTreeModel(MandelListFolderTreeModel model)
  {
    if (model==null) model=fempty;
    if (model!=fmodel) {
      MandelListFolderTreeModel old=fmodel;
      if (old!=null) {
        old.removeTreeModelListener(listener);
      }
      this.fmodel=model;
      if (fmodel!=null) {
        fmodel.addTreeModelListener(listener);
      }
      firePropertyChange(PROP_MODEL,old,fmodel);
      setModifiable(fmodel.isModifiable());
      setActivePath(new TreePath(fmodel.getRoot()));
      fireTreeStructureChanged(new TreePath(fmodel.getRoot()));
    }
  }

  public MandelListFolderTreeModel getFolderTreeModel()
  {
    if (fmodel==fempty) return null;
    return fmodel;
  }
  
  public void setActiveFolder(MandelListFolder f)
  {
//    System.out.println("*** fmodel: "+getFolderTreeModel());
//    System.out.println("folder: "+f.getMandelListFolderTree());
//    System.out.println("model:  "+getFolderTreeModel().getFolderTree());

    if (f.getMandelListFolderTree()!=getFolderTreeModel().getFolderTree()) {
      throw new IllegalArgumentException("non matching folder tree");
    }
    setActivePath(MandelListFolderTreeModelSupport.getPathToRoot(f));
  }
   
  public void setActivePath(TreePath path)
  {
    setActivePath(path,false);
  }

  private void setActivePath(TreePath path, boolean obsolete)
  {
    if (path==null) path=new TreePath(fmodel.getRoot());
    TreePath oldpath=activepath;
    MandelListFolder folder=(MandelListFolder)path.getLastPathComponent();
    if (folder!=activefolder) {
      if (folder!=null && !fmodel.isLocalFolder(folder)) {
        throw new IllegalArgumentException("setting non local folder");
      }
      MandelListFolder oldfolder=activefolder;
      activefolder=folder;
      activepath=path;
      this.lmodel=folder==null?null:fmodel.getMandelListModel(activefolder);
      if (lmodel==null) {
        System.out.println("set empty list");
        lmodel=lempty;
      }
      firePropertyChange(PROP_ACTIVE_FOLDER, oldfolder, activefolder);
      if (!obsolete && oldfolder!=null) fireTreeNodesChanged(oldpath,null,null);
      if (activefolder!=null) fireTreeNodesChanged(activepath,null,null);
    }
    updateActiveName();
    if (!activepath.equals(oldpath)) {
      firePropertyChange(PROP_ACTIVE_PATH, oldpath, activepath);
    }
  }

  public TreePath getActivePath()
  {
    return activepath;
  }

  public MandelListFolder getActiveFolder()
  {
    return activefolder;
  }

  public MandelListTableModel getActiveListModel()
  {
    return lmodel;
  }

  public String getActivePathName()
  {
    return activename;
  }

  protected void updateActiveName()
  {
    String name=determineActivePathName();
    if (activename==null || !activename.equals(name)) {
      String old=activename;
      activename=name;
      this.firePropertyChange(PROP_ACTIVE_NAME, old, name);
    }
  }

  protected String determineActivePathName()
  {
    StringBuilder b=new StringBuilder();
    String sep="";
    for (Object o:getActivePath().getPath()) {
      b.append(sep);
      sep="/";
      b.append(((MandelListFolder)o).getName());
    }
    return b.toString();
  }

  private static void dump(String msg, TreePath p)
  {
    if (debug) {
      System.out.println(msg+":");
      Object[] oa=p.getPath();
      for (Object o:oa) {
        System.out.println("  "+o);
      }
      System.out.println("--");
    }
  }
  
  private static void dump(String msg, TreeModelEvent e)
  {
    if (debug) {
      System.out.println(msg+":");
      Object[] oa=e.getPath();
      for (Object o:oa) {
        System.out.println("  "+o);
      }
      oa=e.getChildren();
      if (oa!=null&&oa.length>0) {
        System.out.println("  children:");
        for (Object o:oa) {
          System.out.println("  "+o);
        }
      }
    }
  }

  static public TreePath getParentPath(TreeModelEvent e, TreePath path)
  {
    TreePath parent=null;
    dump("check parent",e);
    dump("for",path);
    if (e.getTreePath().isDescendant(path)) {
      Object[] children=e.getChildren();
      int length=e.getTreePath().getPathCount();

      if (children!=null&&children.length>0) {
        if (length!=path.getPathCount()) {
          Object n=path.getPathComponent(length);
          for (int i=0; i<children.length; i++) {
            if (children[i]==n) {
              parent=e.getTreePath().pathByAddingChild(n);
              break;
            }
          }
        }
      }
      else parent=e.getTreePath();
    }
    return parent;
  }


  /////////////////////////////////////////////////////////////////////
  // util
  /////////////////////////////////////////////////////////////////////

  private void fireTreeNodesChanged(MandelListFolder f)
  {
    fireTreeNodesChanged(MandelListFolderTreeModelSupport.getPathToRoot(f),null,null);
  }

  private boolean contains(TreePath p, MandelListFolder f)
  {
    while (p!=null) {
      if (p.getLastPathComponent()==f) return true;
      p=p.getParentPath();
    }
    return false;
  }

  private TreePath subPathTo(TreePath p, MandelListFolder f)
  {
    while (p!=null) {
      if (p.getLastPathComponent()==f) return p;
      p=p.getParentPath();
    }
    return null;
  }

  private TreePath findFolder(TreePath root, MandelListFolder f)
  { Stack<MandelListFolder> path=new Stack<MandelListFolder>();
    MandelListFolder r=(MandelListFolder)root.getLastPathComponent();
    if (r==f) return root;
    for (MandelListFolder c:r) {
      if (findFolder(path,c,f)) {
        for (MandelListFolder s:path) {
          root=root.pathByAddingChild(s);
        }
        return root;
      }
    }
    return null;
  }

  private boolean findFolder(Stack<MandelListFolder> path,
                             MandelListFolder r, MandelListFolder f)
  {
    path.push(r);
    if (r==f) {
      return true;
    }
    for (MandelListFolder c:r) {
      if (findFolder(path,c,f))
        return true;
    }
    path.pop();
    return false;
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

  /////////////////////////////////////////////////////////////////////
  // event forwarding
  /////////////////////////////////////////////////////////////////////

  private class ForwardingListener implements PropertyChangeListener,
                                              TreeModelListener,
                                              MandelListFolderTreeModelListener {

    public void propertyChange(PropertyChangeEvent evt)
    {
      firePropertyChange(evt.getPropertyName(),evt.getOldValue(),
                                                    evt.getNewValue());
    }

    public void foldersDeleted(TreeModelEvent e)
    {
      fireFoldersDeleted(e.getTreePath(),e.getChildIndices(),e.getChildren());
    }

    public void treeNodesChanged(TreeModelEvent e)
    {
      System.out.println("forward TNC ");
      fireTreeNodesChanged(e.getTreePath(),e.getChildIndices(),e.getChildren());

      MandelListFolder f;
      Object[] children=e.getChildren();
      f=(MandelListFolder)e.getTreePath().getLastPathComponent();

      if (MandelListFolderListModel.getParentPath(e, getActivePath())!=null) {
        updateActiveName();
      }
    }

    public void treeNodesInserted(TreeModelEvent e)
    { Object[] children=e.getChildren();
      TreePath a=null;
      fireTreeNodesInserted(e.getTreePath(),e.getChildIndices(),children);
      
      if (children==null || children.length==0) {
        a=findFolder(e.getTreePath(),activefolder);
      }
      else {
         for (int i=0; i<children.length && a==null; i++) {
           a=findFolder(e.getTreePath().pathByAddingChild(children[i]),
                        activefolder);
         }
      }
      if (a!=null) {
        // switch active path to new path
        // now the model is prepared for a later remove
        // !!! Moves must FIRST raise the insert event and
        // !!! afterwards the remove event.
        setActivePath(a);
      }
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
      fireTreeNodesRemoved(e.getTreePath(),e.getChildIndices(),e.getChildren());
      if (!isMoving()) handleReorg(e,true);
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
      fireTreeStructureChanged(e.getPath(),e.getChildIndices(),e.getChildren());
      handleReorg(e,false);
    }

    private void handleReorg(TreeModelEvent e, boolean remove)
    {
      TreePath parent=getParentPath(e, activepath);
      if (parent!=null) {
        // this path has been removed and current folder is
        // descendant of this element.
        setActivePath(remove?parent.getParentPath():parent,true);
      }
    }
  }


  /////////////////////////////////////////////////////////////////////
  // regular folder model
  /////////////////////////////////////////////////////////////////////

  public MandelListFolderTreeModel getEffectiveFolderTreeModel()
  {
    return getFolderTreeModel();
  }

  public void valueForPathChanged(TreePath path, Object newValue)
  {
    fmodel.valueForPathChanged(path, newValue);
  }

  public void save()
  {
    fmodel.save();
  }

  public MandelListFolder getRoot()
  {
    return fmodel.getRoot();
  }

  public MandelListTableModel getRootModel()
  {
    return fmodel.getRootModel();
  }
  
  public MandelListTableModel getMandelListModel(Object folder)
  {
    return fmodel.getMandelListModel(folder);
  }

  public boolean isPathTransferable(TreePath path)
  {
    return fmodel.isPathTransferable(path);
  }

  public boolean isPathModifiable(TreePath path)
  {
    return fmodel.isPathModifiable(path);
  }

  public boolean isPathListModifiable(TreePath path)
  {
    return fmodel.isPathListModifiable(path);
  }

  public boolean isPathEditable(TreePath path)
  {
    return fmodel.isPathEditable(path);
  }

  public boolean isLocalFolder(MandelListFolder f)
  {
    return fmodel.isLocalFolder(f);
  }

  public boolean isLeaf(Object node)
  {
    return fmodel.isLeaf(node);
  }

  public boolean insertFolders(int index, MandelListFolder[] folders,
                               MandelListFolder parent)
  {
    return fmodel.insertFolders(index, folders, parent);
  }

  public MandelListFolder insertFolder(String name, MandelListFolder parent)
  {
    return fmodel.insertFolder(name, parent);
  }

  public void removeFolder(MandelListFolder node)
  {
    fmodel.removeFolder(node);
  }

  public void moveFolders(int index, MandelListFolder[] folders,
                          MandelListFolder parent)
  {
    fmodel.moveFolders(index, folders, parent);
  }

  public void setThumbnailName(MandelListFolder folder, QualifiedMandelName name)
  {
    fmodel.setThumbnailName(folder, name);
  }

  public boolean isMoving()
  {
    return fmodel.isMoving();
  }

  public int getIndexOfChild(Object parent, Object child)
  {
    return fmodel.getIndexOfChild(parent, child);
  }

  public MandelListFolderTree getFolderTree()
  {
    return fmodel.getFolderTree();
  }

  public int getChildCount(Object parent)
  {
    return fmodel.getChildCount(parent);
  }

  public MandelListFolder getChild(MandelListFolder parent, String name)
  {
    return fmodel.getChild(parent, name);
  }

  public Object getChild(Object parent, int index)
  {
    return fmodel.getChild(parent, index);
  }

  public String convertValueToText(Object value, boolean selected,
                                   boolean expanded, boolean leaf, int row,
                                   boolean hasFocus)
  {
    return fmodel.convertValueToText(value, selected, expanded, leaf, row,
                                     hasFocus);
  }

  public void addAll(MandelListFolder f, QualifiedMandelName[] list)
  {
    fmodel.addAll(f, list);
  }

  public void add(MandelListFolder f, QualifiedMandelName name)
  {
    fmodel.add(f, name);
  }

  public void remove(MandelListFolder f, QualifiedMandelName name)
  {
    fmodel.remove(f, name);
  }

  public boolean importData(TransferSupport info)
  {
    return fmodel.importData(info);
  }

  public int getSourceActions()
  {
    return fmodel.getSourceActions();
  }

  public DropMode getDropMode()
  {
    return fmodel.getDropMode();
  }

  public void exportDone(Transferable data, int action)
  {
    fmodel.exportDone(data, action);
  }

  public Transferable createTransferable(DragLocation loc)
  {
    return fmodel.createTransferable(loc);
  }

  public boolean canImport(TransferSupport info)
  {
    return fmodel.canImport(info);
  }
}
