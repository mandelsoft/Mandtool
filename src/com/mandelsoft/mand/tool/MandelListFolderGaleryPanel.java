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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.tool.ctx.MandelListFolderContextMenuHandler;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.swing.DnDJList;
import com.mandelsoft.swing.Selection;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListFolderGaleryPanel
        extends MandelGaleryPanel<MandelListFolder, MandelListFolderListModel>
         {

  private ContextHandler contextHandler;

  public MandelListFolderGaleryPanel(MandelListFolderListModel model)
  {
    this(model, 1, new Dimension(100, 100));
  }

  public MandelListFolderGaleryPanel(MandelListFolderListModel model, int rows,
                                     Dimension d)
  {
    super(model, rows, d);
    list.addMouseListener(new Listener());
    setContextMenuHandler(contextHandler=new ContextHandler());
  }

  @Override
  protected void panelUnbound()
  {
    super.panelUnbound();
    System.out.println("cleanup folder galery");
    getModel().setModel(null);
  }

  @Override
  protected String getLabel(MandelListFolder elem)
  {
    return elem.getName();
  }

  @Override
  protected Icon getIcon(MandelListFolder elem)
  {
    if (elem.getMandelList()==null) {
      return MandelListFolderPanel.folderIcon;
    }
    if (elem.isLeaf()) return MandelListFolderPanel.listIcon;
    return MandelListFolderPanel.folderlistIcon;
  }

  private class Listener extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e)
    {
      if (e.getClickCount()<2) return;
      MandelListFolder sel=(MandelListFolder)list.getSelectedValue();
      if (sel==null||sel.isLeaf()) return;
      getModel().setActiveFolder(sel);
    }
  }

  //////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModifiable()
  {
    return getModel().isModifiable();
  }

  @Override
  public void setModifiable(boolean modifiable)
  {
    handleModifiable(modifiable);
    getModel().setModifiable(modifiable);

  }

  public MandelListFolderTreeModel getFolderTreeModel()
  {
    return getModel().getFolderTreeModel();
  }

 
  private class ContextHandler extends MandelListFolderContextMenuHandler
                                implements DnDJList.ContextMenuHandler {

    @Override
    public MandelListFolderTreeModel getModel()
    {
      return getFolderTreeModel();
    }

    @Override
    public MandelList getSelectedMandelList()
    {
      return getSelectedItem().getMandelList();
    }

    private class BackAction extends ContextAction {

      public BackAction()
      {
        super("Parent");
      }

      public void actionPerformed(ActionEvent e)
      {
        MandelListFolderListModel model=MandelListFolderGaleryPanel.this.getModel();
        MandelListFolder parent=model.getActiveFolder().getParent();
        if (parent!=null) {
          model.setActiveFolder(parent);
        }
      }
    }

    private Action backAction = new BackAction();

    public void handleContextMenu(JComponent comp, MouseEvent evt, Selection sel)
    {
      MandelListFolder folder;
      MandelListFolderListModel model;
      TreePath p;
      int index=sel.getLeadSelection();

      model=MandelListFolderGaleryPanel.this.getModel();

      if (index>=0) {
        folder=(MandelListFolder)model.getElementAt(index);
      }
      else {
        folder=model.getActiveFolder();
      }
      p=MandelListFolderTreeModelSupport.getPathToRoot(folder);
      handleContextMenu(comp,evt,p);
    }

    @Override
    protected JPopupMenu createContextMenu(TreePath p)
    {
      MandelListFolderListModel model;
      JPopupMenu menu=super.createContextMenu(p);

      model=MandelListFolderGaleryPanel.this.getModel();
      if (model.getActiveFolder().getParent()!=null) {
        if (menu!=null) {
          menu.addSeparator();
        }
        else {
          menu=new JPopupMenu();
        }
        menu.add(backAction);
      }
      return menu;
    }
  }
}
