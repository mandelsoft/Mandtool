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

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

/**
 *
 * @author Uwe Krueger
 */
public class AbstractTreeModelListener implements TreeModelListener {

  public void treeNodesChanged(TreeModelEvent e)
  {
    if (e.getChildren()!=null) {
      for (Object o:e.getChildren()) {
        TreePath p=e.getTreePath().pathByAddingChild(o);
        if (accept(p))
          treeNodesChanged(p);
      }
    }
    else {
      if (accept(e.getTreePath()))
        treeNodesChanged(e.getTreePath());
    }
  }

  public void treeNodesInserted(TreeModelEvent e)
  {
    if (e.getChildren()!=null) {
      for (Object o:e.getChildren()) {
        TreePath p=e.getTreePath().pathByAddingChild(o);
        if (accept(p))
          treeNodesInserted(p);
      }
    }
    else {
      if (accept(e.getTreePath()))
        treeNodesInserted(e.getTreePath());
    }
  }

  public void treeNodesRemoved(TreeModelEvent e)
  {
    if (e.getChildren()!=null) {
      for (Object o:e.getChildren()) {
        TreePath p=e.getTreePath().pathByAddingChild(o);
        if (accept(p))
          treeNodesRemoved(p);
      }
    }
    else {
      if (accept(e.getTreePath()))
        treeNodesRemoved(e.getTreePath());
    }
  }

  public void treeStructureChanged(TreeModelEvent e)
  {
    if (e.getChildren()!=null) {
      for (Object o:e.getChildren()) {
        TreePath p=e.getTreePath().pathByAddingChild(o);
        if (accept(p))
          treeStructureChanged(p);
      }
    }
    else {
      if (accept(e.getTreePath()))
        treeStructureChanged(e.getTreePath());
    }
  }

  protected boolean accept(TreePath p)
  {
    return true;
  }

  protected void treeNodesChanged(TreePath p)
  {
  }

  protected void treeNodesRemoved(TreePath p)
  {
  }

  protected void treeNodesInserted(TreePath p)
  {
  }

  protected void treeStructureChanged(TreePath p)
  {
  }
}
