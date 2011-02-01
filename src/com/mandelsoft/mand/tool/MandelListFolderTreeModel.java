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

import javax.swing.tree.TreePath;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.swing.DnDTreeModel;

/**
 *
 * @author Uwe Krueger
 */
public interface MandelListFolderTreeModel extends DnDTreeModel {

  void clear();
  
  MandelListFolderTree getFolderTree();
  MandelListTableModel getMandelListModel(Object folder);
  MandelListTableModel getRootModel();
  boolean isLocalFolder(MandelListFolder f);
  boolean isModifiable();
  void setModifiable(boolean modifiable);

  MandelListFolder getChild(MandelListFolder parent, String name);
  MandelListFolderTreeModel getEffectiveFolderTreeModel();

  ////////////////////////////////////////////////////////////////
  // tree interface
  ////////////////////////////////////////////////////////////////
  MandelListFolder getRoot();
  boolean isLeaf(Object node);
  Object getChild(Object parent, int index);
  int getChildCount(Object parent);
  int getIndexOfChild(Object parent, Object child);
  void valueForPathChanged(TreePath path, Object newValue);

  ////////////////////////////////////////////////////////////////
  // extended tree interface
  ////////////////////////////////////////////////////////////////
  boolean isPathModifiable(TreePath path);
  boolean isPathListModifiable(TreePath path);
  boolean isPathTransferable(TreePath path);

  ////////////////////////////////////////////////////////////////
  // list ops
  ////////////////////////////////////////////////////////////////
  void add(MandelListFolder f, QualifiedMandelName name);
  void addAll(MandelListFolder f, QualifiedMandelName[] list);
  void remove(MandelListFolder f, QualifiedMandelName name);
  
  ////////////////////////////////////////////////////////////////
  // folder ops
  ////////////////////////////////////////////////////////////////
  MandelListFolder insertFolder(String name, MandelListFolder parent);
  boolean insertFolders(int index, MandelListFolder[] folders,
                        MandelListFolder parent);
  void moveFolders(int index, MandelListFolder[] folders,
                   MandelListFolder parent);
  void removeFolder(MandelListFolder node);

  void setThumbnailName(MandelListFolder folder, QualifiedMandelName name);
  
  boolean isMoving();

  void save();

  void addMandelListFolderTreeModelListener(MandelListFolderTreeModelListener l);
  void removeMandelListFolderTreeModelListener(MandelListFolderTreeModelListener l);
  MandelListFolderTreeModelListener[] getMandelListFolderTreeModelListeners();

  static public class Util {

    public static boolean folderMetaModifiable(MandelListFolderTreeModel m,
                                               TreePath p)
    {
      if (p==null) return false;
      return m.isPathModifiable(p) && m.isPathEditable(p);
    }

    public static boolean folderModifiable(MandelListFolderTreeModel m,
                                               TreePath p)
    {
      if (p==null) return false;
      return m.isPathModifiable(p);
    }

    public static boolean folderContentModifiable(MandelListFolderTreeModel m,
                                                  TreePath p)
    {
      if (p==null) return false;

      return m.isPathListModifiable(p);
              //&&!((MandelListFolder)p.getLastPathComponent()).isLeaf();
    }
  }
}
