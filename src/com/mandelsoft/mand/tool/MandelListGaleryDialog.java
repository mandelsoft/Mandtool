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

import com.mandelsoft.mand.MandelName;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.event.TreeModelEvent;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.util.CachedUpstreamColormapSourceFactory;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.swing.AbstractTreeModelListener;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListGaleryDialog extends MandelDialog {
  private MandelListProxyListModelForTable pmodel;
  private AbstractMandelListListModel model;
  private Listener listener;
  private MandelListFolderTreeModel fmodel;
  private MandelListFolder folder;
  private FListener flistener;
  private MandelListGaleryPanel galery;

  public MandelListGaleryDialog(MandelWindowAccess owner,
                            MandelListFolderTreeModel fmodel, MandelListFolder f)
  {
    this(owner,fmodel.getMandelListModel(f),f.getPath());
    this.fmodel=fmodel;
    this.folder=f;
    flistener=new FListener();
    fmodel.addTreeModelListener(flistener);
  }

  public MandelListGaleryDialog(MandelWindowAccess owner,
                            MandelListTableModel tmodel, String name)
  {
    this(owner,new MandelListProxyListModelForTable(tmodel),name);
    pmodel=(MandelListProxyListModelForTable)model; // remember local wrapper
    pmodel.setModifiable(!owner.getEnvironment().isReadonly());
  }

  public MandelListGaleryDialog(MandelWindowAccess owner,
                            MandelList tmplist, String name)
  {
    this(owner,new DefaultMandelListListModel(tmplist,
                          owner.getEnvironment().getImageDataScanner()),name);
  }

  public MandelListGaleryDialog(MandelWindowAccess owner,
                            AbstractMandelListListModel lmodel, String name)
  {
    super(owner,name);
    model=lmodel;
    MandelImage.Factory factory=new MandelImage.Factory(owner.getColormapModel());
    model.setFactory(factory);
    MandelImagePanel mp=owner.getMandelImagePane();
    if (mp!=null && mp.getParentColormapModel().isSet()) {
      model.setColormapSourceFactory(
        new CachedUpstreamColormapSourceFactory(model.getMandelScanner(),
                                                mp.getColormapModel(),
                                owner.getEnvironment().getColormapCache()));
      System.out.println("-> galery with upstream colormap");
    }
    else {
      System.out.println("-> galery with main colormap");
    }

    galery=new MandelListGaleryPanel(model);
    if (owner.getMandelImagePane()!=null) {
      galery.setMaxFrame(owner.getMandelImagePane().getMaxFrame());
    }
    add(galery);
    pack();
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
    int w=this.getWidth();
    int h=this.getHeight();
    if (w>d.getWidth()-20)  {
      w=(int)d.getWidth()-20;
      h=h+30;
    }
    else {
      if (w<100) w=100;
      else w+=30;
    }
    if (h>d.getHeight()-20) {
      h=(int)d.getHeight()-20;
    }
    else {
      if (h<100) h=100;
      else h+=30;
    }

    this.setSize(w, h);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.setResizable(true);
    this.setVisible(true);
    owner.getEnvironment().addEnvironmentListener(listener=new Listener());
    owner.getEnvironment().getMandelListFolderTreeModel().addMandelListFolderTreeModelListener(
                                           listener);
  }

  @Override
  public void dispose()
  {
    System.out.println("close galery");
    if (pmodel!=null) pmodel.setModel(null); // cleanup local wrapper model
    getEnvironment().getMandelListFolderTreeModel().removeMandelListFolderTreeModelListener(
                                           listener);
    getEnvironment().removeEnvironmentListener(listener);
    if (fmodel!=null) fmodel.removeTreeModelListener(flistener);
    super.dispose();
  }

  private class Listener extends ToolEnvironment.ListenerAdapter
                         implements MandelListFolderTreeModelListener {

    @Override
    public void mandelListDeleted(MandelList list)
    {
      if (list==model.getList()) {
        dispose();
      }
    }

    public void foldersDeleted(TreeModelEvent e)
    {
      Object[] children=e.getChildren();
      if (children==null) {
        handle((MandelListFolder)e.getTreePath().getLastPathComponent());
      }
      else {
        for (Object o:children) {
          handle((MandelListFolder)o);
        }
      }
    }

    protected void handle(MandelListFolder f)
    {
      if (model!=null&&model.getList()!=null) {
        if (f!=null&&f.containsTransitively(model.getList())) {
          dispose();
        }
      }
    }
  }

  private class FListener extends AbstractTreeModelListener {

    @Override
    public void treeNodesChanged(TreeModelEvent e)
    {
      if (MandelListFolderListModel.getParentPath(e,
                   MandelListFolderTreeModelSupport.getPathToRoot(folder))!=null) {
        setTitle(folder.getPath());
      }
    }
  }

  /////////////////////////////////////////////////////////////
  
  public void setRootName(MandelName rootName)
  {
    galery.setRootName(rootName);
  }

  public MandelName getRootName()
  {
    return galery.getRootName();
  }


}
