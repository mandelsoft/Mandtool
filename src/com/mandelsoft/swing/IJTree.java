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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Uwe Krüger
 */

public class IJTree extends JTree {

  public IJTree(TreeModel newModel)
  {
    super(newModel);
    _setupIJTree();
  }

  public IJTree(TreeNode root, boolean asksAllowsChildren)
  {
    super(root, asksAllowsChildren);
    _setupIJTree();
  }

  public IJTree(TreeNode root)
  {
    super(root);
    _setupIJTree();
  }

  public IJTree(Hashtable<?, ?> value)
  {
    super(value);
    _setupIJTree();
  }

  public IJTree(Vector<?> value)
  {
    super(value);
    _setupIJTree();
  }

  public IJTree(Object[] value)
  {
    super(value);
    _setupIJTree();
  }

  public IJTree()
  {
    _setupIJTree();
  }

  private void _setupIJTree()
  {
    addPropertyChangeListener("ancestor",new ComponentPropertyListener());
  }

  private TreeModel origModel;

  protected void componentBound()
  {
    if (origModel!=null && treeModel==null) {
      treeModel=origModel;
      if (treeModelListener!=null) 
        treeModel.addTreeModelListener(treeModelListener);
      if (accessibleContext!=null)
        treeModel.addTreeModelListener((TreeModelListener)accessibleContext);
      origModel=null;
    }
  }

  protected void componentUnbound()
  {
    //System.out.println("----- unbound");
    origModel=treeModel;
    treeModel=null;
    if (origModel!=null) {
      if (treeModelListener!=null)
        origModel.removeTreeModelListener(treeModelListener);
      if (accessibleContext!=null)
        treeModel.removeTreeModelListener((TreeModelListener)accessibleContext);
    }
  }

  private class ComponentPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getNewValue()==null) {
        componentUnbound();
      }
      else {
        componentBound();
      }
    }
  }


  /////////////////////////////////////////////////////////////////////
  // extended model
  /////////////////////////////////////////////////////////////////////

  @Override
  public ITreeModel getModel()
  {
    return (ITreeModel)super.getModel();
  }

  @Override
  public void setModel(TreeModel newModel)
  {
    setModel((ITreeModel)newModel);
  }

  public void setModel(ITreeModel newModel)
  {
    super.setModel(newModel);
  }

  @Override
  public String convertValueToText(Object value, boolean selected,
                                   boolean expanded, boolean leaf, int row,
                                   boolean hasFocus)
  {
    ITreeModel m=(ITreeModel)getModel();
    if (m==null) return "";

    return m.convertValueToText(value, selected, expanded, leaf, row,
                                                            hasFocus);
  }

  @Override
  public boolean isPathEditable(TreePath path)
  {
    return ((ITreeModel)getModel()).isPathEditable(path);
  }

  /////////////////////////////////////////////////////////////////////
  // default model
  /////////////////////////////////////////////////////////////////////

  public static class DefaultModel extends DefaultTreeModel
                             implements ITreeModel {
    private boolean modifiable=false;

    public DefaultModel(TreeNode root, boolean asksAllowsChildren)
    {
      super(root, asksAllowsChildren);
    }

    public DefaultModel(TreeNode root)
    {
      super(root);
    }

    public boolean isModifiable()
    {
      return modifiable;
    }

    public void setModifiable(boolean modifiable)
    {
      this.modifiable=modifiable;
    }



    public String convertValueToText(Object value, boolean selected,
                                     boolean expanded, boolean leaf, int row,
                                     boolean hasFocus)
    {
      return value.toString();
    }

    public boolean isPathEditable(TreePath path)
    {
      return isModifiable();
    }
  }
}
