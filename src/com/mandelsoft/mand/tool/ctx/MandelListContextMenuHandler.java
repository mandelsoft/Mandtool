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

import com.mandelsoft.mand.Environment;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerUtils;
import com.mandelsoft.mand.tool.MandelAreaViewDialog;
import com.mandelsoft.mand.tool.MandelImageAreaDialog;
import com.mandelsoft.mand.tool.MandelImagePanel;
import com.mandelsoft.mand.tool.MandelListModel;
import com.mandelsoft.mand.tool.MandelListModelSource;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelListContextMenuHandler
                extends MandelContextMenuHandler<QualifiedMandelName,
                                                 Integer,
                                                 MandelListModel> {

  protected boolean isNavToFolder()
  {
    return true;
  }
  
  //////////////////////////////////////////////////////////////////////////
  // Environment Embedding
  //////////////////////////////////////////////////////////////////////////

  public MandelListModel getModel()
  {
     MandelListModelSource s=lookupInterface(MandelListModelSource.class);
     return s==null?null:s.getModel();
  }

  public QualifiedMandelName getSelectedItem()
  {
    QualifiedMandelName sel;
    if (getSelectionSpec()<0) {
      sel=null;
    }
    else {
      sel=getModel().getQualifiedName(getSelectionSpec());
    }
    return sel;
  }

  //////////////////////////////////////////////////////////////////////////
  // Context Menu Entries
  //////////////////////////////////////////////////////////////////////////

  private class AddCurrentImageAction extends ContextAction {

    public AddCurrentImageAction()
    {
      super("Add Current");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess access=getMandelWindowAccess();
      QualifiedMandelName name=access.getQualifiedName();
      if (name!=null) getModel().add(name);
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
      if (name!=null) getModel().remove(name);
    }
  }

  private class RemoveImageAction extends ContextAction {

    public RemoveImageAction()
    {
      super("Remove");
    }

    public void actionPerformed(ActionEvent e)
    {
      QualifiedMandelName sel=getSelectedItem();
      if (sel==null) return;
      getModel().remove(sel);
    }
  }

  private class AddMemoryAction extends ContextAction {

    public AddMemoryAction()
    {
      super("Add to memory");
    }

    public void actionPerformed(ActionEvent e)
    {
      QualifiedMandelName sel=getSelectedItem();
      if (sel==null) return;
      MandelWindowAccess access=getMandelWindowAccess();
      access.getEnvironment().getMemoryModel().add(sel);
    }
  }

  private class ShowImageAction extends ContextAction {

    public ShowImageAction()
    {
      super("Show image");
    }

    public void actionPerformed(ActionEvent e)
    {
      QualifiedMandelName sel=getSelectedItem();
      if (sel==null) return;
      setBusy(true);
      MandelWindowAccess access=getMandelWindowAccess();
      System.out.println("access is "+access);
      createMandelImageFrame(getWindow(),access,getMaxFrame(),sel);
      setBusy(false);
    }
  }

  protected class LoadImageAction extends LoadImageContextAction {
    protected LoadImageAction(String name)
    {
      super(name);
    }

    public LoadImageAction()
    { this("Load image");
    }

    public void actionPerformed(ActionEvent e)
    {
      loadImage(getSelectedItem());
    }
  }

  private class LoadParentAction extends LoadImageAction {
    public LoadParentAction()
    {
      super("Load Parent");
    }

    @Override
    public QualifiedMandelName getSelectedItem()
    {
      QualifiedMandelName n=super.getSelectedItem();
      MandelWindowAccess access=getMandelWindowAccess();
      Environment env=access.getEnvironment();
      MandelScanner scanner=env.getImageDataScanner();
      Set<MandelHandle> set=scanner.getMandelHandles(n);;
      while (!MandelScannerUtils.hasImageData(set)) {
        n=new QualifiedMandelName(n.getMandelName().getParentName());
        set=scanner.getMandelHandles(n);
      }
      return n;
    }
  }

  private class LoadRegImageAction extends ContextAction {
    public LoadRegImageAction()
    { super("Load Standard Variant");
    }

    public void actionPerformed(ActionEvent e)
    {
      QualifiedMandelName sel=getSelectedItem();
      if (sel==null) return;
      setBusy(true);
      MandelWindowAccess access=getMandelWindowAccess();
      if (!access.getMandelImagePane().setImage(sel.getMandelName())) {
        JOptionPane.showMessageDialog(getWindow(),
                                      "Cannot load image: "+sel.getMandelName(),
                                      "Mandel IO", JOptionPane.WARNING_MESSAGE);
      }
      setBusy(false);
    }
  }

  private class ShowMetaAction extends ContextAction {
    public ShowMetaAction()
    { super("Show Meta Data");
    }

    public void actionPerformed(ActionEvent e)
    {
      try {
        MandelAreaViewDialog v;
        QualifiedMandelName name=getSelectedItem();
        if (name!=null) {
          MandelHandle found=getModel().getMandelData(getSelectionSpec());
          if (found==null) {
            //System.out.println("file="+found.getFile());
          }
          else {
            MandelData data=found.getInfo();
            v=new MandelImageAreaDialog(getMandelWindowAccess(),
                                        "Mandel Image Meta Information",
                                         name, data);
            v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            v.setVisible(true);
          }
        }
      }
      catch (IOException ex) {
        Error("Mandel Meta Data","cannot load mandel data");
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////

  protected Action addCurrentImageAction=new AddCurrentImageAction();
  protected Action removeCurrentImageAction=new RemoveCurrentImageAction();
  protected Action removeImageAction=new RemoveImageAction();
  protected Action addMemoryAction=new AddMemoryAction();
  protected Action showMetaAction=new ShowMetaAction();
  protected Action showImageAction=new ShowImageAction();
  protected Action loadImageAction=new LoadImageAction();
  protected Action loadParentAction=new LoadParentAction();
  protected Action loadRegImageAction=new LoadRegImageAction();

  protected JPopupMenu createLabeledMenu(String text)
  {
    JPopupMenu menu=new JPopupMenu(text);
    JPanel panel=new JPanel();
    JLabel label=new JLabel(text);
    label.setHorizontalTextPosition(JLabel.CENTER);
    label.setFont(label.getFont().deriveFont(
      label.getFont().getStyle()|Font.BOLD));
    panel.add(label);
    menu.add(panel);
    return menu;
  }

  protected JPopupMenu createContextMenu(Integer index)// </editor-fold>
  {
    JPopupMenu menu;
    QualifiedMandelName sel=null;

    //System.out.println("create list ctx menu for "+index);

    MandelWindowAccess access=getMandelWindowAccess();
    MandelImagePanel mp=getMandelWindowAccess().getMandelImagePane();
    MandelListModel model=getModel();

    if (index<0&&mp==null&&(getMandelWindowAccess().getQualifiedName()==null
                            ||!getModel().isModifiable())) return null;
    if (index>=0) {
      sel=model.getQualifiedName(index);
      menu=createLabeledMenu(sel.toString());
    }
    else {
      menu=new JPopupMenu();
    }

    boolean sep=false;

    if (model.isModifiable()) {
      if (getMandelWindowAccess().getQualifiedName()!=null) {
        sep=true;
        menu.add(addCurrentImageAction);
        menu.add(removeCurrentImageAction);
      }
      if (index>=0) {
        if (sep)  menu.addSeparator();
        sep=false;
        menu.add(removeImageAction);
      }
    }

    Component comp=getContextComponent();

    System.out.println("contextComponent is "+comp);
    if (index>=0) {
      MandelHandle h=model.getMandelHandle(index);
      if (sep) menu.addSeparator();
      sep=false;
      menu.add(showMetaAction);
      if (h!=null&&h.getHeader().hasImageData()) {
        menu.add(showImageAction);
        if (mp!=null) {
          menu.add(loadImageAction);
          if (sel.getQualifier()!=null) {
            menu.add(loadRegImageAction);
          }
        }
      }
      else {
        menu.add(loadParentAction);
      }
      menu.add(addMemoryAction);
      menu.add(access.getEnvironment().getListActions().createMenu(comp, sel));
    }
    
    if (mp!=null) {
      if (sep) menu.addSeparator();
      menu.add(mp.getSlideShowModel().
              createMenu(comp, sel));
    }
    
    return menu;
  }

  ///////////////////////////////////////////////////////////////////////////
  static public void createMandelImageFrame(Component requester,
                                            MandelWindowAccess access,
                                            int maxx,
                                            QualifiedMandelName sel)
  {
    try {
      ColormapModel cm=access.getColormapModel();
      if (cm==null) {
        Colormap c=access.getEnvironment().getDefaultColormap();
        if (c!=null) {
          System.out.println("found environment default colormap");
          cm=new ColormapModel(c);
        }
      }
      else {
        System.out.println("found access colormap");
      }
      if (maxx==0) {
        access.getEnvironment().createMandelImageFrame(sel, cm);
      }
      else {
        access.getEnvironment().createMandelImageFrame(sel, cm, maxx);
      }
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(requester,
                                    "Cannot load image: "+sel,
                                    "Mandel IO", JOptionPane.WARNING_MESSAGE);
    }
  }
}
