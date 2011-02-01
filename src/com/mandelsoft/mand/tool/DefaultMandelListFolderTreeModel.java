
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

import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.LeafMandelListFolderTree;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;

/**
 *
 * @author Uwe Krüger
 */
public class DefaultMandelListFolderTreeModel extends MandelListFolderTreeModelSupport
                                          implements MandelListFolderTreeModel {

  protected MandelListFolderTree tree;
  protected MandelScanner scanner;
  private boolean modifiable;

  public DefaultMandelListFolderTreeModel(MandelListFolderTree tree, MandelScanner scanner)
  {
    super();
    this.tree=tree;
    this.scanner=scanner;
  }

  public DefaultMandelListFolderTreeModel(String name, MandelList list, MandelScanner scanner)
  {
    super();
    this.tree=new LeafMandelListFolderTree(name,list);
    this.scanner=scanner;
  }

  public DefaultMandelListFolderTreeModel(String name, MandelListTableModel model)
  {
    super();
    this.tree=new LeafMandelListFolderTree(name,model.getList());
    this.scanner=model.getMandelScanner();
    this.listmodels.put(tree.getRoot(), model);
    setModifiable(model.isModifiable());
  }

  public DefaultMandelListFolderTreeModel(String name, MandelListTableModel model,
                                      String desc)
  {
    super();
    this.tree=new LeafMandelListFolderTree(name,model.getList());
    this.scanner=model.getMandelScanner();
    this.listmodels.put(tree.getRoot(), model);
    tree.getRoot().setProperty(MandelListFolder.DESCRIPTION, desc);
    setModifiable(model.isModifiable());
  }

  public void setModifiable(boolean modifiable)
  {
    this.modifiable=modifiable;
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public MandelListFolderTree getFolderTree()
  {
    return tree;
  }

  protected MandelScanner getMandelScanner()
  {
    return scanner;
  }

  ////////////////////////////////////////////////////////////////
  // tree model
  ////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////
  // extended interface
  ////////////////////////////////////////////////////////////////

  @Override
  protected boolean isModifiable(MandelListFolder f)
  {
    if (f.getName().equals("TEST")) return false;
    return isModifiable();
  }

  
}
  
