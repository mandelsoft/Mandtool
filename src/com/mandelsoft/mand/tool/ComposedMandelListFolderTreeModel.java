
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.ArrayMandelListFolder;
import com.mandelsoft.mand.util.DefaultMandelListFolderTree;
import com.mandelsoft.mand.util.FileMandelList;
import com.mandelsoft.mand.util.FileMandelListFolderTree;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krüger
 */

public class ComposedMandelListFolderTreeModel
        extends MandelListFolderTreeModelSupport
        implements MandelListFolderTreeModel {
  static public boolean debug=false;

  private MandelListFolderTree tree;
  private MandelScanner scanner;
  private boolean modifiable;
  private Map<MandelListFolder, FolderWrapper> map;
  private Map<MandelListFolderTree, FolderLink> links;
  private List<ComposedTreeModelListener> listeners;

  public ComposedMandelListFolderTreeModel(String name, MandelScanner scanner)
  {
    this.scanner=scanner;
    this.tree=new FolderTree(name);
    this.map=new HashMap<MandelListFolder,FolderWrapper>();
    this.links=new HashMap<MandelListFolderTree,FolderLink>();
    this.listmodels=new HashMap<MandelListFolder,MandelListTableModel>();
    this.listeners=new ArrayList<ComposedTreeModelListener>();
  }

  @Override
  public  void clear()
  {
    if (debug) System.out.println("clear "+tree.getRoot().getName()+": "+this);
    for (ComposedTreeModelListener l:listeners) l.clear();
    listeners.clear();
    map.clear();
    links.clear();
    super.clear();
  }


  ////////////////////////////////////////////////////////////////////////
  // tree model
  ////////////////////////////////////////////////////////////////////////

  @Override
  protected boolean isModifiable(MandelListFolder f)
  {
    return super.isModifiable(f);
  }

  @Override
  public boolean isPathEditable(TreePath path)
  {
    ComposedFolder c=(ComposedFolder)path.getLastPathComponent();
    if (isLogical(c)) {
      return false;
    }
    else {
      return mapToEffectiveModel(c).isPathEditable(getEffectiveTreePath(path));
    }
  }

  @Override
  public boolean isPathModifiable(TreePath path)
  {
    ComposedFolder c=(ComposedFolder)path.getLastPathComponent();
    if (isLogical(c)) {
      return false;
    }
    else {
      return mapToEffectiveModel(c).isPathModifiable(getEffectiveTreePath(path));
    }
  }

  @Override
  public boolean isPathListModifiable(TreePath path)
  {
    ComposedFolder c=(ComposedFolder)path.getLastPathComponent();
    if (isLogical(c)) {
      return false;
    }
    else {
      return mapToEffectiveModel(c).isPathListModifiable(getEffectiveTreePath(path));
    }
  }

  @Override
  public boolean isPathTransferable(TreePath path)
  {
    ComposedFolder c=(ComposedFolder)path.getLastPathComponent();
    if (isLogical(c)) {
      return false;
    }
    else {
      return mapToEffectiveModel(c).isPathTransferable(getEffectiveTreePath(path));
    }
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue)
  {
    ComposedFolder c=(ComposedFolder)path.getLastPathComponent();
    if (isLogical(c)) {
      super.valueForPathChanged(path, newValue);
    }
    else {
      mapToEffectiveModel(c).valueForPathChanged(getEffectiveTreePath(path),
                                              newValue);
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // folder model
  ////////////////////////////////////////////////////////////////////////

  public MandelListFolderTree getFolderTree()
  {
    return tree;
  }

  @Override
  protected MandelScanner getMandelScanner()
  {
    return scanner;
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public void setModifiable(boolean modifiable)
  {
    this.modifiable=modifiable;
  }

  ////////////////////////////////////////////////////////////////
  // mandel list model handling
  ////////////////////////////////////////////////////////////////

  @Override
  public MandelListTableModel getMandelListModel(Object folder)
  {
    MandelListTableModel m;
    ComposedFolder f=(ComposedFolder)folder;

    if (isLogical(f)) {
      m=super.getMandelListModel(folder);
    }
    else {
      m=f.getEffectiveFolderTreeModel().getMandelListModel(f.getEffectiveFolder());
    }
    return m;
  }

  ///////////////////////////////////////////////////////////////////////
  // folder ops
  ///////////////////////////////////////////////////////////////////////

  @Override
  public MandelListFolder insertFolder(String name, MandelListFolder parent)
  {
    ComposedFolder c=(ComposedFolder)parent;
    if (isLogical(c)) {
      return super.insertFolder(name, parent);
    }
    return ((FolderWrapper)c).mapToComposedChild(c.getEffectiveFolderTreeModel().
                                   insertFolder(name, c.getEffectiveFolder()));
  }

  @Override
  public boolean insertFolders(int index, MandelListFolder[] folders,
                               MandelListFolder parent)
  {
    ComposedFolder c=(ComposedFolder)parent;
    if (isLogical(c)) {
      return super.insertFolders(index, folders, parent);
    }
    else {
      boolean b=c.getEffectiveFolderTreeModel().insertFolders(index,
                                    mapToEffectiveFolder(folders),
                                    c.getEffectiveFolder());
      if (b) {
        ((FolderWrapper)c).assureChildren(folders);
      }
      return b;
    }
  }

  @Override
  public void moveFolders(int index, MandelListFolder[] folders,
                          MandelListFolder parent)
  {
    try {
      ComposedFolder c=(ComposedFolder)parent;
      moving=true;
      if (isLogical(c)) {
        super.moveFolders(index, folders, parent);
      }
      else {
        MandelListFolderTree pt=((ComposedFolder)c).getEffectiveFolderTree();
        MandelListFolderTree mt=((ComposedFolder)folders[0]).getEffectiveFolderTree();
        if (mt==pt) {
          // forward move to enclosing nested model
          c.getEffectiveFolderTreeModel().moveFolders(index,
                                                  mapToEffectiveFolder(folders),
                                                  c.getEffectiveFolder());
          ((FolderWrapper)c).assureChildren(folders);
        }
        else {
          // source and target have different nested models
          // -> map to insert remove instead of move
          // insert first, to assert validity of source for inserting(copying)
          insertFolders(index,folders,parent);
          for (int i=0; i<folders.length; i++) {
           removeFolder(folders[i]);
          }
        }
      }
    }
    finally {
      moving=false;
    }
  }

  @Override
  public void removeFolder(MandelListFolder node)
  {
    ComposedFolder c=(ComposedFolder)node;
    if (isLogical(c)) {
      super.removeFolder(node);
    }
    else {
      c.getEffectiveFolderTreeModel().removeFolder(c.getEffectiveFolder());
      ((FolderWrapper)c).removeEffectiveChild(c.getEffectiveFolder());
    }
  }
  
  ///////////////////////////////////////////////////////////////////////
  // composition
  ///////////////////////////////////////////////////////////////////////

  protected boolean isLogical(Object f)
  {
    if (((MandelListFolder)f).getMandelListFolderTree()!=tree) {
      throw new IllegalArgumentException("non local folder");
    }
    return ((ComposedFolder)f).getEffectiveFolderTree()==tree;
  }

  public void addListModel(MandelListTableModel model, String name)
  {
    MandelListFolderTreeModel m;
    
    m=new DefaultMandelListFolderTreeModel(new File(name).getName(),model);
    addFolderTreeModel(m,name);
  }

  public void addListModel(MandelListTableModel model, String name, boolean modifiable)
  {
    MandelListFolderTreeModel m;

    m=new DefaultMandelListFolderTreeModel(new File(name).getName(),model);
    if (!modifiable) m.setModifiable(modifiable);
    addFolderTreeModel(m,name);
  }

  public void addListModel(MandelListTableModel model, String name, String desc)
  {
    MandelListFolderTreeModel m;

    m=new DefaultMandelListFolderTreeModel(new File(name).getName(),model, desc);
    addFolderTreeModel(m,name);
  }

  public void addFolderTreeModel(MandelListFolderTreeModel model, String name)
  {
    MandelListFolder f=tree.getRoot();
    StringTokenizer t=new StringTokenizer(name,"/");
    String n=null;
    while (t.hasMoreTokens()) {
      n=t.nextToken();
      if (t.hasMoreTokens()) {
        MandelListFolder p=null;
        for (MandelListFolder s:f) {
          if (s.getName().equals(n)) {
            p=s;
            break;
          }
        }
        if (p==null) {
          p=new LogicalFolder(n);
          f.add(p);
        }
        f=p;
      }
    }
    if (n==null) throw new IllegalArgumentException("illegal path name");
    FolderLink p=new FolderLink(n,model);
    f.add(p);
    listeners.add(new ComposedTreeModelListener(model,getPathToRoot(p).getPath()));
  }
  
  protected MandelListFolderTreeModel mapToEffectiveModel(MandelListFolder f)
  {
    MandelListFolderTree t=f.getMandelListFolderTree();
    if (t==tree) {
      return ((ComposedFolder)f).getEffectiveFolderTreeModel();
    }
    if (t!=null) {
      //System.out.println("eff tree is "+t);
      FolderLink l=mapToComposedLink(t);
      if (l!=null) return l.getEffectiveFolderTreeModel();
    }
    throw new IllegalArgumentException("illegal folder "+f.getClass()+
                                     ": "+f.getMandelListFolderTree());
  }

  private FolderLink mapToComposedLink(MandelListFolderTree tree)
  {
    return links.get(tree);
  }

  private FolderWrapper _mapToComposedFolder(MandelListFolder f)
  {
    FolderWrapper m=map.get(f);
    if (m==null && f!=null) {
      m=new FolderWrapper(mapToEffectiveModel(f),f);
    }
    return m;
  }

  protected MandelListFolder mapToEffectiveFolder(Object o)
  {
    return ((ComposedFolder)o).getEffectiveFolder();
  }

  protected <T> T[] mapToEffectiveFolder(T[] oa)
  {
    T[] na =(T[])Array.newInstance(oa.getClass().getComponentType(), oa.length);
    for (int i=0; i<oa.length; i++) {
      na[i]=(T)mapToEffectiveFolder(oa[i]);
    }
    return na;
  }

  protected TreePath getEffectiveTreePath(TreePath p)
  {
    // System.out.println("effective path for "+path(p));
    ComposedFolder c=(ComposedFolder)p.getLastPathComponent();
    if (isLogical(p.getParentPath().getLastPathComponent())) {
      return new TreePath(c.getEffectiveFolder());
    }
    return getEffectiveTreePath(p.getParentPath()).pathByAddingChild(
                              c.getEffectiveFolder());
  }

  protected void cleanup(MandelListFolder f, boolean incl)
  {
    for (MandelListFolder s:f) {
      if (map.get(s)!=null) cleanup(s,true);
    }
    if (incl) map.remove(f);
  }

  ////////////////////////////////////////////////////////////////////////
  // event mapper
  ////////////////////////////////////////////////////////////////////////
  private class ComposedTreeModelListener implements TreeModelListener,
                                               MandelListFolderTreeModelListener {
    private Object[] basepath;
    MandelListFolderTreeModel model;

    public ComposedTreeModelListener(MandelListFolderTreeModel model,
                                     Object[] basepath)
    {
      this.basepath=basepath;
      if (debug) {
        System.out.println("composed listener for ");
        for (Object o:basepath) {
          System.out.println("  "+o);
        }
      }
      this.model=model;
      model.addTreeModelListener(this);
      model.addMandelListFolderTreeModelListener(this);
    }

    public void clear()
    {
      model.removeTreeModelListener(this);
      model.removeMandelListFolderTreeModelListener(this);
    }

    private void dump(String msg, TreeModelEvent e)
    {
      if (debug) {
        System.out.println(msg+":");
        Object[] oa=e.getPath();
        for (Object o:oa) {
          System.out.println("    "+o);
        }
        oa=e.getChildren();
        if (oa!=null&&oa.length>0) {
          System.out.println("  children:");
          for (Object o:oa) {
            System.out.println("    "+o);
          }
        }
        System.out.println("--");
      }
    }

    private Object map(Object last, boolean cont,
                       int sindex, int eindex, Object[] oa,
                     int index, Object[] na)
    {
      for (int i=sindex; i<eindex; i++) {
        FolderWrapper w=(FolderWrapper)last;
        FolderWrapper next=w.mapToComposedChild((MandelListFolder)oa[i]);
        //if (debug) System.out.println("mapped to "+next);
        na[i-sindex+index]=next;
        if (cont) last=next;
      }
      return last;
    }

    private TreeModelEvent mapToComposedEvent(String msg, TreeModelEvent e)
    {
      Object[] o=e.getPath();
      Object[] no=new Object[o.length+basepath.length-1];

      dump("got "+msg,e);
      for (int i=0; i<basepath.length; i++) no[i]=basepath[i];
      Object last=basepath[basepath.length-1];
      last=map(last,true,1,o.length,o,basepath.length,no);
      Object[] c=e.getChildren();
      Object[] nc=null;
      if (c!=null) {
        nc=new Object[c.length];
        map(last,false,0,c.length,c,0,nc);
      }
      e=new TreeModelEvent(ComposedMandelListFolderTreeModel.this,
                                no,e.getChildIndices(),nc);
      dump("mapped for "+Utils.getObjectIdentifier(tree),e);
      return e;
    }

    public void foldersDeleted(TreeModelEvent e)
    {
      ComposedMandelListFolderTreeModel.this.fireFoldersDeleted(mapToComposedEvent("FD",e));
    }

    public void treeNodesChanged(TreeModelEvent e)
    {
      ComposedMandelListFolderTreeModel.this.fireTreeNodesChanged(mapToComposedEvent("TNC",e));
    }

    public void treeNodesInserted(TreeModelEvent e)
    {
      ComposedMandelListFolderTreeModel.this.fireTreeNodesInserted(mapToComposedEvent("TNI",e));
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
      ComposedMandelListFolderTreeModel.this.fireTreeNodesRemoved(mapToComposedEvent("TNR",e));
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
      TreeModelEvent m=mapToComposedEvent("TSC",e);

      // discard all wrappers below changed node
      Object[] p=m.getPath();
      Object[] c=m.getChildren();
      if (c==null||c.length==0) {
        ((FolderWrapper)p[p.length-1]).reset();
      }
      else {
        for (Object o:c) {
          ((FolderWrapper)o).reset();
        }
      }

      ComposedMandelListFolderTreeModel.this.fireTreeStructureChanged(m);
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // folder tree
  ////////////////////////////////////////////////////////////////////////
  
  private class FolderTree extends DefaultMandelListFolderTree {
    
    FolderTree(String name)
    {
      super(new LogicalFolder(name));
    }

    @Override
    public void read(InputStream is, String src) throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // common folder features
  ////////////////////////////////////////////////////////////////////////

  private interface ComposedFolder extends MandelListFolder {
    MandelListFolderTree  getEffectiveFolderTree();
    MandelListFolderTreeModel getEffectiveFolderTreeModel();
    MandelListFolder      getEffectiveFolder();
  }

  ////////////////////////////////////////////////////////////////////////
  // folder link
  ////////////////////////////////////////////////////////////////////////

  private class FolderLink extends FolderWrapper {
    private String name;
    private MandelListFolder parent;

    public FolderLink(String name, MandelListFolderTreeModel link)
    {
      super(link,link.getRoot());
      this.name=name;
      // System.out.println("add link "+link.getFolderTree());
      links.put(link.getFolderTree(), this);
    }

    protected void reset()
    {
      setEffectiveFolder(getEffectiveFolderTreeModel().getRoot());
      super.reset();
    }

    @Override
    public String getName()
    {
      return name;
    }

    @Override
    public void setName(String name)
    {
      this.name=name;
    }

    @Override
    public MandelListFolder getParent()
    {
      return parent;
    }

    @Override
    public void setParent(MandelListFolder parent)
    {
      this.parent=parent;
    }
  }
  
  ////////////////////////////////////////////////////////////////////////
  // logical folder
  ////////////////////////////////////////////////////////////////////////

  private class LogicalFolder extends ArrayMandelListFolder
                              implements ComposedFolder {
    public LogicalFolder(String name)
    {
      super(name);
    }

    /////////////////////////////////////////////////////////////////
    // composed folder
    /////////////////////////////////////////////////////////////////
    
    public MandelListFolderTreeModel getEffectiveFolderTreeModel()
    {
      return ComposedMandelListFolderTreeModel.this;
    }

    public MandelListFolderTree getEffectiveFolderTree()
    {
      return tree;
    }

    public MandelListFolder getEffectiveFolder()
    {
      return this;
    }

    /////////////////////////////////////////////////////////////////
    // mandel list folder
    /////////////////////////////////////////////////////////////////

    @Override
    protected MandelList createMandelList()
    {
      return null;
    }

    public MandelListFolderTree getMandelListFolderTree()
    {
      return tree;
    }

    public LogicalFolder createSubFolder(String name)
    {
      LogicalFolder f=new LogicalFolder(name);
      super.add(f);
      return f;
    }

    public LogicalFolder createSubFolder(int index, String name)
    {
      LogicalFolder f=new LogicalFolder(name);
      if (index<0) index=this.size();
      super.add(index, f);
      return f;
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // folder wrapper
  ////////////////////////////////////////////////////////////////////////

  private class FolderWrapper implements ComposedFolder {
    private MandelListFolder folder;
    private MandelListFolderTreeModel model;
    private HashMap<MandelListFolder, FolderWrapper> mappedchildren;

    public FolderWrapper(MandelListFolderTreeModel model,
                         MandelListFolder folder)
    {
      this.model=model;
      this.mappedchildren=new HashMap<MandelListFolder, FolderWrapper>();
      setEffectiveFolder(folder);
      if (debug) System.out.println("create wrapper "+this);
    }

    @Override
    public String toString()
    {
      String s=getName()+" ("+Utils.getObjectIdentifier(super.toString());
      s=s+"): "+Utils.getObjectIdentifier(tree)+"-> "+folder;
      return s;
    }
    
    protected void setEffectiveFolder(MandelListFolder f)
    {
      if (folder==f) return;
      if (folder!=null) map.remove(folder);
      this.folder=f;
      map.put(f,this);
    }

    protected void reset()
    {
      if (debug) System.out.println("resetting "+this);
      for (FolderWrapper w:mappedchildren.values()) {
        w.reset();
      }
      for (MandelListFolder f:mappedchildren.keySet()) {
        map.remove(f);
      }
      mappedchildren.clear();
    }

    FolderWrapper mapToComposedChild(MandelListFolder f)
    {
      FolderWrapper w=mappedchildren.get(f);
      if (w==null) {
        w=_mapToComposedFolder(f);
        mappedchildren.put(f, w);
      }
      return w;
    }

    void removeEffectiveChild(MandelListFolder f)
    {
      if (mappedchildren.containsKey(f)) {
        mappedchildren.remove(f);
        map.remove(f);
      }
    }

    void assureChildren(MandelListFolder folders[])
    {
      for (MandelListFolder f:folders) {
        FolderWrapper w=(FolderWrapper)f;
        MandelListFolder e=w.getEffectiveFolder();
        if (folder.contains(e)) {
          if (!mappedchildren.containsKey(e)) mappedchildren.put(e, w);
        }
      }
    }

    /////////////////////////////////////////////////////////////////
    // composed folder
    /////////////////////////////////////////////////////////////////

    public MandelListFolderTreeModel getEffectiveFolderTreeModel()
    {
      return model;
    }

    public MandelListFolderTree getEffectiveFolderTree()
    {
      return folder.getMandelListFolderTree();
    }

    public MandelListFolder getEffectiveFolder()
    {
      return folder;
    }

    /////////////////////////////////////////////////////////////////
    // mandel list folder
    /////////////////////////////////////////////////////////////////

    public MandelListFolderTree getMandelListFolderTree()
    {
      return tree;
    }

    public void setParent(MandelListFolder f)
    {
      throw new UnsupportedOperationException("Not supported.");
    }

    public void setName(String name)
    {
      folder.setName(name);
    }

    public void setThumbnailName(QualifiedMandelName thumb)
    {
      folder.setThumbnailName(thumb);
    }
    
    public String getPath()
    {
      if (getParent()==null) return getName();
      StringBuffer sb=new StringBuffer();
      getPath(sb, getParent());
      sb.append("/");
      sb.append(getName());
      return sb.toString();
    }

    private void getPath(StringBuffer sb, MandelListFolder f)
    {
      if (f.getParent()!=null) {
        getPath(sb, f.getParent());
        sb.append("/");
      }
      sb.append(f.getName());
    }

    public MandelListFolder getParent()
    {
      MandelListFolder p=folder.getParent();
      FolderWrapper w=_mapToComposedFolder(p);
      if (w!=null) w.mappedchildren.put(folder, this);
      return w;
    }

    public MandelListFolder getSubFolder(String name)
    {
      MandelListFolder f=folder.getSubFolder(name);
      return mapToComposedChild(f);
    }

    public String getName()
    {
      return folder.getName();
    }

    public QualifiedMandelName getThumbnailName()
    {
      return folder.getThumbnailName();
    }

    public boolean isLeaf()
    {
      return folder.isLeaf();
    }

    public MandelList getMandelList()
    {
      return folder.getMandelList();
    }

    public boolean hasMandelList()
    {
      return folder.hasMandelList();
    }

    public MandelListFolder createSubFolder(int index, String name)
    {
      MandelListFolder f=folder.createSubFolder(index, name);
      return mapToComposedChild(f);
    }

    public MandelListFolder createSubFolder(String name)
    {
      MandelListFolder f=folder.createSubFolder(name);
      return mapToComposedChild(f);
    }

    public boolean containsTransitively(MandelListFolder f)
    {
      while (f!=null&&f!=this) f=f.getParent();
      return f==this;
    }

    public boolean containsTransitively(MandelList l)
    {
      if (getMandelList()==l) return true;
      for (MandelListFolder f:this) {
        if (f.containsTransitively(l)) return true;
      }
      return false;
    }

    public void setProperty(String name, String value)
    {
      folder.setProperty(name, value);
    }

    public String getProperty(String name)
    {
      return folder.getProperty(name);
    }

    public Iterable<String> propertyNames()
    {
      return folder.propertyNames();
    }
    
    //////////////////////////////////////////////////////////////////////
    // Base List

    public boolean valid()
    {
      return folder.valid();
    }

    public void save() throws IOException
    {
      folder.save();
    }

    public void refresh(boolean soft)
    {
      cleanup(folder.getMandelListFolderTree().getRoot(),false);
      folder.refresh(soft);
    }

    public void clear()
    {
      cleanup(folder,false);
      folder.clear();
    }

    //////////////////////////////////////////////////////////////////////
    // list

    public <T> T[] toArray(T[] a)
    {
      a=folder.toArray(a);
      for (int i=0; i<a.length; i++) {
        a[i]=(T)mapToComposedChild((MandelListFolder)a[i]);
      }
      return a;
    }

    public Object[] toArray()
    {
      Object[] a=folder.toArray();
      for (int i=0; i<a.length; i++) {
        a[i]=mapToComposedChild((MandelListFolder)a[i]);
      }
      return a;
    }

    public List<MandelListFolder> subList(int fromIndex, int toIndex)
    {
      ArrayList<MandelListFolder> a=new ArrayList<MandelListFolder>();
      for (MandelListFolder f:folder.subList(fromIndex, toIndex)) {
        a.add(mapToComposedChild(f));
      }
      return a;
    }

    public int size()
    {
      return folder.size();
    }

    public MandelListFolder set(int index, MandelListFolder element)
    {
      throw new UnsupportedOperationException("Not supported.");
    }

    protected Collection<?> mapC(Collection<?> c)
    {
      Collection<Object> r=new ArrayList<Object>();

      for (Object e:c) {
        r.add(map((MandelListFolder)e));
      }
      return r;
    }

    protected Object map(Object e)
    {
      if (e instanceof ComposedFolder) {
        ComposedFolder w=((ComposedFolder)e);
        e=w.getEffectiveFolder();
      }
      return e;
    }

    public boolean retainAll(Collection<?> c)
    {
      return folder.retainAll(mapC(c));
    }

    public boolean removeAll(Collection<?> c)
    {
      return folder.removeAll(mapC(c));
    }

    public MandelListFolder remove(int index)
    {
      return folder.remove(index);
    }

    public boolean remove(Object o)
    {
      return folder.remove(map(o));
    }

    public int lastIndexOf(Object o)
    {
      return folder.lastIndexOf(map(o));
    }

    public boolean isEmpty()
    {
      return folder.isEmpty();
    }

    public int indexOf(Object o)
    {
      return folder.indexOf(map(o));
    }

    public MandelListFolder get(int index)
    {
      return mapToComposedChild(folder.get(index));
    }

    public boolean containsAll(Collection<?> c)
    {
      return folder.containsAll(mapC(c));
    }

    public boolean contains(Object o)
    {
      return folder.contains(map(o));
    }

    public boolean addAll(int index,
                          Collection<? extends MandelListFolder> c)
    {
      throw new UnsupportedOperationException("Not supported.");
    }

    public boolean addAll(Collection<? extends MandelListFolder> c)
    {
      throw new UnsupportedOperationException("Not supported.");
    }

    public void add(int index, MandelListFolder element)
    {
      throw new UnsupportedOperationException("Not supported.");
    }

    public boolean add(MandelListFolder e)
    {
      throw new UnsupportedOperationException("Not supported.");
    }

    public ListIterator<MandelListFolder> listIterator(int index)
    {
      return new MappedIterator(folder.listIterator(index));
    }

    public ListIterator<MandelListFolder> listIterator()
    {
      return new MappedIterator(folder.listIterator());
    }

    public Iterator<MandelListFolder> iterator()
    {
      return listIterator();
    }

    public Iterable<QualifiedMandelName> allentries()
    {
      return folder.allentries();
    }

    public Iterable<MandelListFolder> allfolders()
    {
      return new Iterable<MandelListFolder>() {
        public Iterator<MandelListFolder> iterator()
        {
          return new MappedIteratorBase(folder.allfolders().iterator());
        }
      };
    }

    private class MappedIteratorBase<I extends Iterator<MandelListFolder>>
               implements Iterator<MandelListFolder> {
      protected I it;

      public MappedIteratorBase(I it)
      {
        this.it=it;
      }

      public void remove()
      {
        it.remove();
      }

      public MandelListFolder next()
      {
        MandelListFolder f=it.next();
        return (MandelListFolder)mapToComposedChild(f);
      }

      public boolean hasNext()
      {
        return it.hasNext();
      }
    }

    private class MappedIterator
             extends MappedIteratorBase<ListIterator<MandelListFolder>>
             implements ListIterator<MandelListFolder> {

      public MappedIterator(ListIterator<MandelListFolder> it)
      {
        super(it);
      }

      public void set(MandelListFolder e)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

      public int previousIndex()
      {
        return it.previousIndex();
      }

      public MandelListFolder previous()
      {
        return it.previous();
      }

      public int nextIndex()
      {
        return it.nextIndex();
      }

      public boolean hasPrevious()
      {
        return it.hasPrevious();
      }

      public void add(MandelListFolder e)
      {
        throw new UnsupportedOperationException("Not supported.");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // main
  ////////////////////////////////////////////////////////////////////////

  static public String path(MandelListFolder f)
  {
    if (f==null) return "/";
    return path(f.getParent())+"/"+f.getName();
  }

  static public String path(TreePath p)
  {
    if (p.getPathCount()==1)
      return ((MandelListFolder)p.getLastPathComponent()).getName();
    return path(p.getParentPath())+"/"+((MandelListFolder)p.getLastPathComponent()).getName();
  }

  static public String event(TreeModelEvent e)
  {
    String r=path(e.getTreePath());
    if (e.getChildren()!=null) {
      r+=": children ";
      char c='[';
      if (e.getChildren().length==0) r+=c;
      else for (Object o:e.getChildren()) {
        r+=c+((MandelListFolder)o).getName();
        c=',';
      }
      r+=']';

    }
    return r;
  }

  static public void print(String gap,
                           MandelListFolder f)
  {
    System.out.println(path(f));
    for (MandelListFolder s:f) {
      print(gap+"  ",s);
    }
  }

  static public void dump(MandelListFolderTreeModel m)
  {
    print("",m.getRoot());
  }

  private static class Listener implements TreeModelListener {

    public void treeNodesChanged(TreeModelEvent e)
    {
      if (debug) System.out.println("CHD: "+event(e));
    }

    public void treeNodesInserted(TreeModelEvent e)
    {
      if (debug) System.out.println("INS: "+event(e));
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
      if (debug) System.out.println("REM: "+event(e));
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
      if (debug) System.out.println("STR: "+event(e));
    }
  }

  public static MandelListFolderTreeModel createDemo(MandelListFolderTree t)
  {
    DefaultMandelListFolderTreeModel m=new DefaultMandelListFolderTreeModel(t,null);
    m.setModifiable(true);
    ComposedMandelListFolderTreeModel c=new ComposedMandelListFolderTreeModel("test",null);
    c.addFolderTreeModel(m, "laber/bla");
    File file = new File("list");
    if (file.exists()) {
      FileMandelList l=new FileMandelList(file);
      MandelListTableModel lm=new DefaultMandelListTableModel(l,null);
      lm.addAction(new AbstractAction("TEST") {
        public void actionPerformed(ActionEvent e)
        {
          System.out.println("hurra!");
        }
      });
      lm.setModifiable(true);
      DefaultMandelListFolderTreeModel fm=new DefaultMandelListFolderTreeModel("leaf",lm);
      c.addFolderTreeModel(fm, "laber/dummy");
      MandelListProxyTableModel pm=new MandelListProxyTableModel(lm);
      pm.setModifiable(false);
      fm=new DefaultMandelListFolderTreeModel("readonly", pm);
      c.addFolderTreeModel(fm, "laber/readonly");
    }
    c.setModifiable(true);
    c.addTreeModelListener(new Listener());
    ComposedMandelListFolderTreeModel r=new ComposedMandelListFolderTreeModel("root",null);
    r.addFolderTreeModel(m, "test");
    return r;
  }
  
  public static void main(String[] args)
  {
    File file = new File(args[0]);
    FileMandelListFolderTree t=new FileMandelListFolderTree(file);
    DefaultMandelListFolderTreeModel m=new DefaultMandelListFolderTreeModel(t,null);
    ComposedMandelListFolderTreeModel c=new ComposedMandelListFolderTreeModel("test",null);
    c.addFolderTreeModel(m, "laber/bla");
    c.addTreeModelListener(new Listener());
    dump(c);
    MandelListFolder s=t.getRoot().get(1).get(0);
    m.insertFolder("Test", s);
  }
}
