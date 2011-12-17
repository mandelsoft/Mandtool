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

package com.mandelsoft.mand.tool.ctx;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.MandelImagePanel;
import com.mandelsoft.mand.tool.MandelListFolderGaleryDialog;
import com.mandelsoft.mand.tool.MandelListFolderTreeModel;
import com.mandelsoft.mand.tool.MandelListFolderTreeModelSource;
import com.mandelsoft.mand.tool.MandelListGaleryDialog;
import com.mandelsoft.mand.tool.MandelListModel;
import com.mandelsoft.mand.tool.MandelListSelector;
import com.mandelsoft.mand.tool.MandelListTableModel;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.mand.tool.PictureSaveDialog;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import javax.swing.AbstractAction;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListFolderContextMenuHandler
       extends MandelContextMenuHandler<MandelListFolder,
                                        TreePath,
                                        MandelListFolderTreeModel>
       implements MandelListSelector {

  //////////////////////////////////////////////////////////////////////////
  // util
  //////////////////////////////////////////////////////////////////////////

  protected boolean folderMetaModifiable(TreePath p)
  {
    return MandelListFolderTreeModel.Util.folderMetaModifiable(getModel(), p);
  }

  protected boolean folderModifiable(TreePath p)
  {
    return MandelListFolderTreeModel.Util.folderModifiable(getModel(), p);
  }

  protected boolean folderContentModifiable(TreePath p)
  {
    return MandelListFolderTreeModel.Util.folderContentModifiable(getModel(), p);
  }

  //////////////////////////////////////////////////////////////////////////
  // Environment Embedding
  //////////////////////////////////////////////////////////////////////////

  public MandelListFolderTreeModel getModel()
  {
     MandelListFolderTreeModelSource s=lookupInterface(MandelListFolderTreeModelSource.class);
     return s==null?null:s.getModel();
  }

  @Override
  public MandelListFolder getSelectedItem()
  {
    if (getSelectionSpec()==null) return null;
    return (MandelListFolder)getSelectionSpec().getLastPathComponent();
  }

  public MandelList getSelectedMandelList()
  {
    return getSelectedItem().getMandelList();
  }

  //////////////////////////////////////////////////////////////////////
  // Conext menu
  //////////////////////////////////////////////////////////////////////

  private class ShowGaleryAction extends ContextAction {
    public ShowGaleryAction()
    { super("Folder Galery");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      new MandelListFolderGaleryDialog(access,getModel().getEffectiveFolderTreeModel(),
                                              getSelectedItem());
    }
  }

  private class ShowImageGaleryAction extends ContextAction {
    public ShowImageGaleryAction()
    { super("Image Galery");
    }

    public void actionPerformed(ActionEvent e)
    {

      MandelWindowAccess access=getMandelWindowAccess();
      new MandelListGaleryDialog(access,getModel().getEffectiveFolderTreeModel(),
                                        getSelectedItem());
    }
  }

  private class AddListShortcutAction extends ContextAction {

    public AddListShortcutAction()
    {
      super("Add List Shortcut");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      MandelListModel m=getModel().getMandelListModel(getSelectedItem());
      if (m!=null) {
        access.getMandelImagePane().addListShortcut(getSelectedItem().getName(), m);
      }
    }
  }

  private class RemoveListShortcutAction extends ContextAction {

    public RemoveListShortcutAction()
    {
      super("Remove List Shortcut");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      MandelListModel m=getModel().getMandelListModel(getSelectedItem());
      if (m!=null) {
        access.getMandelImagePane().removeListShortcut(m);
      }
    }
  }

   private class AddCurrentImageAction extends ContextAction {
    public AddCurrentImageAction()
    { super("Add Current");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      QualifiedMandelName name=access.getQualifiedName();
      MandelListModel m=getModel().getMandelListModel(getSelectedItem());
      if (m!=null) {
        m.add(name);
      }
      else {
        JOptionPane.showMessageDialog(getWindow(),
                                        "Cannot add image", //text to display
                                        "Mandel List Folder", //title
                                        JOptionPane.ERROR_MESSAGE);
      }
    }
  }

    private class RemoveCurrentImageAction extends ContextAction {

    public RemoveCurrentImageAction()
    {
      super("Remove Current");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      QualifiedMandelName name=access.getQualifiedName();
      MandelListModel m=getModel().getMandelListModel(getSelectedItem());
      if (name!=null) m.remove(name);
    }
  }

  private class SetThumbnailAction extends ContextAction {
    public SetThumbnailAction()
    { super("Set Thumbnail");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      getModel().setThumbnailName(getSelectedItem(), access.getQualifiedName());
    }
  }

  private class ClearThumbnailAction extends ContextAction {
    public ClearThumbnailAction()
    { super("Clear Thumbnail");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      getModel().setThumbnailName((getSelectedItem()), null);
    }
  }

  private class LoadThumbnailAction extends LoadImageContextAction {
    protected LoadThumbnailAction(String name)
    {
      super(name);
    }

    public LoadThumbnailAction()
    { this("Load Thumbnail");
    }

    public void actionPerformed(ActionEvent e)
    {
      QualifiedMandelName n=getSelectedItem().getThumbnailName();
      if (n!=null) loadImage(n);
    }
  }

  private class SaveImagesAction extends AbstractAction {

    public SaveImagesAction()
    {
      super("Save Images");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelListFolder f=getSelectedItem();
      String title="Save Images for "+f.getPath();
      if (f.hasMandelList()) {
        PictureSaveDialog d=new PictureSaveDialog(getMandelWindowAccess(),
                                                  title,
                                                  f.getMandelList());
        d.setVisible(true);
      }
    }
  }

  private Action addImageAction=new AddCurrentImageAction();
  private Action removeImageAction=new RemoveCurrentImageAction();
  private Action newFolderAction=new NewFolderAction(this);
  private Action deleteAction=new DeleteFolderAction(this);
  private Action showGaleryAction=new ShowGaleryAction();
  private Action showImageGaleryAction=new ShowImageGaleryAction();
  private Action setThumbnailAction=new SetThumbnailAction();
  private Action loadThumbnailAction=new LoadThumbnailAction();
  private Action clearThumbnailAction=new ClearThumbnailAction();
  private Action addListShortcutAction=new AddListShortcutAction();
  private Action removeListShortcutAction=new RemoveListShortcutAction();
  private Action saveImagesAction=new SaveImagesAction();

  @Override
  protected JPopupMenu createContextMenu(TreePath p)
  {
    if (p==null) return null;
    MandelListFolder folder=getSelectedItem();
    MandelWindowAccess acc=getMandelWindowAccess();
    MandelImagePanel pane=acc!=null?acc.getMandelImagePane():null;
    MandelListFolderTreeModel m=getModel();
    boolean modcont=folderContentModifiable(p);
    boolean modmeta=folderMetaModifiable(p);
    boolean modfold=folderModifiable(p);

    JPopupMenu menu=new JPopupMenu();

    if (modcont) {
      if (folder.hasMandelList()) {
        menu.add(addImageAction);
        menu.add(removeImageAction);
        menu.addSeparator();
      }
    }
    if (!folder.isLeaf()) {
      menu.add(showGaleryAction);
    }
    if (folder.hasMandelList()) {
      menu.add(showImageGaleryAction);
      if (acc!=null && !acc.getEnvironment().isReadonly()) {
        menu.add(saveImagesAction);
      }
      if (pane!=null) {
        menu.add(pane.getSlideShowModel().createMenu(null,this));
        MandelListTableModel mt=m.getMandelListModel(folder);
        if (!pane.hasListShortcut(mt)) {
          if (mt!=null && mt.isModifiable()) {
            menu.add(addListShortcutAction);
          }
        }
        else {
          if (mt!=null) {
            menu.add(removeListShortcutAction);
          }
        }
      }
    }


    if (modmeta) {
      if (acc!=null&&acc.getQualifiedName()!=null) {
        menu.add(setThumbnailAction);
      }
      if (folder.getThumbnailName()!=null) {
        menu.add(loadThumbnailAction);
        menu.add(clearThumbnailAction);
      }
    }
    else {
      if (folder.getThumbnailName()!=null) {
        menu.add(loadThumbnailAction);
      }
    }

    if (modfold) {
      menu.addSeparator();
      menu.add(newFolderAction);
      if (modmeta) {
        menu.add(deleteAction);
      }
    }

    return menu;
  }


}
