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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.FileMandelListFolderTree;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;

/**
 *
 * @author Uwe Krüger
 */

public class MandelListFolderBrowserPanel extends GBCPanel {
  static public boolean debug=false;

  private ModelChangedListener         listener;
  private MandelListFolderBrowserModel model;
  private MandelListFolderPanel        fpanel;
  private MandelListPanel              lpanel;
  private boolean                      extmodel;

  public MandelListFolderBrowserPanel(String header,
                                      MandelListFolderTree ftree,
                                      MandelScanner scanner)
  {
    this(header,new DefaultMandelListFolderTreeModel(ftree,scanner),scanner);
  }

  public MandelListFolderBrowserPanel(String header,
                                      MandelListFolderTreeModel model,
                                      MandelScanner scanner)
  {
    this(header, new MandelListFolderBrowserModel(model),scanner);
    extmodel=false;
  }

  public MandelListFolderBrowserPanel(String header,
                                      MandelListFolderBrowserModel model,
                                      MandelScanner scanner)
  {
    this.model=model;
    extmodel=true;
    fpanel=new MandelListFolderPanel(model,scanner);
    fpanel.addActionListener(new ListSelectionListener());

    listener=new ModelChangedListener();

    lpanel=new MandelListPanel(model.getRoot().getName(),
                               model.getActiveListModel(),null);
   
    //Add the scroll panes to a split pane.
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.5);
    splitPane.setLeftComponent(fpanel);
    splitPane.setRightComponent(lpanel);
    add(splitPane,GBC(0,0,GBC.BOTH));

    handleModifiable(model.isModifiable());
    //add(folders);
  }

  @Override
  protected void panelBound()
  {
    super.panelBound();
    System.out.println("setup tree listener");
    model.addTreeModelListener(listener);
    model.addPropertyChangeListener(listener);
  }

  @Override
  protected void panelUnbound()
  {
    super.panelUnbound();
    System.out.println("cleanup tree listener");
    model.removeTreeModelListener(listener);
    model.removePropertyChangeListener(listener);
    if (!extmodel) model.setFolderTreeModel(null);
  }

  public boolean isModifiable()
  {
    return model.isModifiable();
  }

  public void setModifiable(boolean modifiable)
  {
    handleModifiable(modifiable);
    model.setModifiable(modifiable);

  }

  protected void handleModifiable(boolean b)
  {
    System.out.println("handle browser panel modifiable "+b);
    fpanel.setModifiable(b);
    if (b) {
      lpanel.setModifiable(model.getActiveListModel().isModifiable());
    }
    else {
      lpanel.setModifiable(b);
    }
  }

  public void setRootVisible(boolean b)
  {
    fpanel.setRootVisible(b);
  }

  /////////////////////////////////////////////////////////////////////////

  private String folderSpec(Object o)
  {
    MandelListFolder f=(MandelListFolder)o;
    return f.toString();
  }

  private class ListSelectionListener implements ActionListener {

    private void dump(String msg, TreePath p)
    {
      if (debug) {
        System.out.println(msg+":");
        while (p!=null) {
          System.out.println("  "+folderSpec(p.getLastPathComponent()));
          p=p.getParentPath();
        }
      }
    }

    public void actionPerformed(ActionEvent e)
    {
      System.out.println("selection event");
      TreePath p=fpanel.getSelectedPath();
      if (p!=null &&
          ((MandelListFolder)p.getLastPathComponent()).getMandelList()!=null) {
        dump("active path",p);
        model.setActivePath(p);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
 
  private class ModelChangedListener implements TreeModelListener,
                                                PropertyChangeListener {

    private void dump(String msg, TreeModelEvent e)
    {
      if (debug) {
        System.out.println(msg+":");
        Object[] oa=e.getPath();
        for (Object o:oa) {
          System.out.println("  "+folderSpec(o));
        }
        oa=e.getChildren();
        if (oa!=null&&oa.length>0) {
          System.out.println("  children:");
          for (Object o:oa) {
            System.out.println("  "+folderSpec(o));
          }
        }
      }
    }

    public void treeNodesChanged(TreeModelEvent e)
    {
//      MandelListFolder f;
//      Object[] children=e.getChildren();
//      f=(MandelListFolder)e.getTreePath().getLastPathComponent();
//
        dump("tree node changed",e);
//      System.out.println("tree nodes changed");
//      if (model.getParentPath(e, model.getActivePath())!=null) {
//        lpanel.setTitle(model.getActivePathName());
//      }
    }

    public void treeNodesInserted(TreeModelEvent e)
    {
      dump("tree node inserted",e);
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
      MandelListFolder l,f;
      dump("tree node removed",e);
      f=model.getActiveFolder();
      if (f!=null) {
        l=(MandelListFolder)e.getTreePath().getLastPathComponent();
        while (f!=null) {
          if (f==l) {
            // update active path
            model.setActivePath(
             MandelListFolderTreeModelSupport.getPathToRoot(model.getActiveFolder()));
            break;
          }
          else {
            f=f.getParent();
          }
        }
      }
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
      dump("tree structure",e);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
      String prop=evt.getPropertyName();
      if (prop.equals(MandelListFolderBrowserModel.PROP_MODIFIABLE)) {
        handleModifiable((Boolean)evt.getNewValue());
      }
      else if (prop.equals(MandelListFolderBrowserModel.PROP_ACTIVE_NAME)) {
        System.out.println("set active name "+model.getActivePathName());
        lpanel.setTitle(model.getActivePathName());
      }
      else if (prop.equals(MandelListFolderBrowserModel.PROP_ACTIVE_FOLDER)) {
        System.out.println("set active list model");
        lpanel.setModel(model.getActiveListModel());
        fpanel.setActiveFolder(model.getActiveFolder());
        revalidate();
      }
    }
  }
  
  /////////////////////////////////////////////////////////////
  // test
  /////////////////////////////////////////////////////////////

  static class TestFrame extends JFrame {
    TestFrame(MandelListFolderTree folder)
    {
      MandelListFolderBrowserPanel panel=new MandelListFolderBrowserPanel(
                                          "Test",
                                          folder,
                                          null);
      add(panel);
      panel.setModifiable(true);
      pack();
      this.setMinimumSize(this.getSize());
    }

    TestFrame(MandelListFolderTreeModel model)
    {
      MandelListFolderBrowserPanel panel=new MandelListFolderBrowserPanel(
                                          "Test",
                                          model,
                                          null);
      add(panel);
      pack();
      this.setMinimumSize(this.getSize());
    }
  }

  public static void main(String[] args)
  {
    File file = new File(args[0]);
    FileMandelListFolderTree f=new FileMandelListFolderTree(file);
    final MandelListFolderTreeModel m1=new DefaultMandelListFolderTreeModel(f,null);
    m1.setModifiable(true);
    final MandelListFolderTreeModel m2=ComposedMandelListFolderTreeModel.createDemo(f);
    m2.setModifiable(true);
    System.out.println("folder setup done");

    SwingUtilities.invokeLater(new Runnable() {

      public void run()
      {
        create(JFrame.DISPOSE_ON_CLOSE);
        create(JFrame.EXIT_ON_CLOSE);
      }

      private void create(int mode)
      {
        JFrame frame=new TestFrame(m2);

        frame.setDefaultCloseOperation(
                mode);
        frame.setVisible(true);
      }
    });
  }
}
