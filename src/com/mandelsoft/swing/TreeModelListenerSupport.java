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

import javax.swing.tree.TreePath;

/**
 *
 * @author Uwe Krüger
 */

public class TreeModelListenerSupport extends TreeModelListenerSupportBase {

  protected TreeModelListenerSupport()
  {
  }

  public TreeModelListenerSupport(Object source)
  {
    super(source);
  }

  @Override
  public void fireTreeNodesChanged(TreePath path, int[] childIndices,
                                      Object[] children)
  {
    super.fireTreeNodesChanged(path, childIndices, children);
  }

  @Override
  public void fireTreeNodesInserted(TreePath path, int[] childIndices,
                                       Object[] children)
  {
    super.fireTreeNodesInserted(path, childIndices, children);
  }

  @Override
  public void fireTreeNodesRemoved(TreePath path, int[] childIndices,
                                      Object[] children)
  {
    super.fireTreeNodesRemoved(path, childIndices, children);
  }

  @Override
  public void fireTreeStructureChanged(Object[] path, int[] childIndices,
                                          Object[] children)
  {
    super.fireTreeStructureChanged(path, childIndices, children);
  }

  @Override
  public void fireTreeStructureChanged(TreePath path)
  {
    super.fireTreeStructureChanged(path);
  }
}
