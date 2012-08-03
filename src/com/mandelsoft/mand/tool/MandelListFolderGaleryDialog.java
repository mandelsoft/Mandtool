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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.util.CachedUpstreamColormapSourceFactory;
import com.mandelsoft.mand.util.MandelListFolder;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListFolderGaleryDialog extends MandelDialog {
  private MandelListFolderListModel model;
  private Listener listener;

  public MandelListFolderGaleryDialog(MandelWindowAccess owner,
                            MandelListFolderTreeModel tmodel,
                            MandelListFolder folder)
  {
    super(owner);
    model=new MandelListFolderListModel(tmodel,owner.getEnvironment().getImageDataScanner());
    if (folder!=null) model.setActiveFolder(folder);
    setTitle(model.getActivePathName());
    listener=new Listener();
    model.addPropertyChangeListener(MandelListFolderBrowserModel.PROP_ACTIVE_NAME,listener);
    //model.addTreeModelListener(listener);
    model.setModifiable(!owner.getEnvironment().isReadonly());
    MandelImage.Factory factory=new MandelImage.Factory(owner.getColormapModel());
    model.setFactory(factory);
    MandelImagePanel mp=owner.getMandelImagePane();
    if (mp!=null && mp.getParentColormapModel().isSet()) {
      model.setColormapSourceFactory(
        new CachedUpstreamColormapSourceFactory(model.getMandelScanner(),
                                                owner.getColormapModel(),
                                 owner.getEnvironment().getColormapCache()));
    }

    MandelListFolderGaleryPanel p=new MandelListFolderGaleryPanel(model);
    add(p);
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
  }

  @Override
  public void dispose()
  {
    System.out.println("close galery");
    model.setModel(null);
    super.dispose();
  }

  ///////////////////////////////////////////////////////////////////////

  private class Listener // extends AbstractTreeModelListener
                         implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt)
    {
      System.out.println("path name changed: "+model.getActivePathName());
      setTitle(model.getActivePathName());
    }

//    @Override
//    public void treeNodesChanged(TreeModelEvent e)
//    {
//      MandelListFolder f;
//      Object[] children=e.getChildren();
//      f=(MandelListFolder)e.getTreePath().getLastPathComponent();
//
//      if (MandelListFolderListModel.getParentPath(e, model.getActivePath())!=null) {
//        System.out.println("path name changred: "+model.getActivePathName());
//        setTitle(model.getActivePathName());
//      }
//    }
  }
 
}
