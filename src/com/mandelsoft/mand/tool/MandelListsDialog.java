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
import java.io.IOException;
import javax.swing.JOptionPane;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.tool.ctx.MandelListContextMenuHandler;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.swing.TablePanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 *
 * @author Uwe Krüger
 */
public class MandelListsDialog extends ControlDialog {
  public MandelListsDialog(MandelWindowAccess owner)
  {
    super(owner, "Mandel Lists");
    Dimension d=new Dimension((int)getSize().getWidth()+60,
                              (int)getSize().getHeight());
    setSize(d);
    setMinimumSize(d);
    System.out.println("minimal size is "+this.getMinimumSize());
    setResizable(true);
  }

  protected void setup()
  {
    if (getEnvironment().getUnseenRastersModel()!=null) {
      addTab("Unseen Images", new UnseenRasterPanel(),
                            "Images not seen according to local seen file");
    }
    addTab("Variants", new VariantsPanel(),
                            "Mandel areas with variants");
    if (getEnvironment().getFavoritesModel()!=null)
      addTab("Favorites", new FavoritesPanel(),
                            "Images according local favorites file");
    if (getEnvironment().getTodosModel()!=null)
      addTab("Todos", new TodosPanel(),
                            "Images according local todo file");
    addTab("History", new HistoryPanel(),
                            "Display history of images");
    if (getEnvironment().getNewRastersModel()!=null) {
      addTab("New Images", new NewRasterPanel(),
                       "newly generated rasters not touched by anybody so far");
    }
    if (getEnvironment().getAreasModel()!=null) {
      addTab("Key Areas", new AreasPanel(),
                       "marked root areas for backward navigation");
    }
    if (getEnvironment().getRefinementsModel()!=null) {
      addTab("Refinements", new RefinementsPanel(),
                       "available refinements");
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // refreshing list panel
  /////////////////////////////////////////////////////////////////////////

  private class RefreshingPanel extends MandelListPanel {

    public RefreshingPanel(MandelListTableModel model)
    {
      super(null, model, null);
    }

    @Override
    protected void setup(ActionListener action)
    {
      super.setup(new RefreshLoadAction());
    }

    protected class RefreshLoadAction extends LoadAction {
      @Override
      protected void handleLoaded(QualifiedMandelName name)
      {
        // now handled via change listeners at model level!!!
        // getModel().refresh();
      }
    }

  }

  /////////////////////////////////////////////////////////////////////////
  // new tab
  /////////////////////////////////////////////////////////////////////////
  private class NewRasterPanel extends RefreshingPanel {

    public NewRasterPanel()
    {
      super(getEnvironment().getNewRastersModel());
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // unseen tab
  /////////////////////////////////////////////////////////////////////////
  private class UnseenRasterPanel extends RefreshingPanel {
    public UnseenRasterPanel()
    {
      super(getEnvironment().getUnseenRastersModel());
      //addButton("Sync New", new NewAction(),"Synchoronize with new images");
    }


    /////////////////////////////////////////////////////////////////////////
    private class NewAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        MandelList unseen=getModel().getList();
        MandelList n=getEnvironment().getNewRasters();
        n.refresh(false);

        getEnvironment().refresh(unseen);
        unseen.retainAll(n);
        try {
          unseen.save();
        }
        catch (IOException ex) {
          System.err.println("cannot write seen: "+ex);
        }
        getModel().fireTableDataChanged();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // variants tab
  /////////////////////////////////////////////////////////////////////////
  private class VariantsPanel extends MandelListPanel {
    public VariantsPanel()
    {
      super(null,getEnvironment().getVariantsModel(),null);
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // favorites tab
  /////////////////////////////////////////////////////////////////////////

  private class FavoritesPanel extends MandelListPanel {

    public FavoritesPanel()
    {
      super(null, getEnvironment().getFavoritesModel(), null,
            !getEnvironment().isReadonly());
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // todos tab
  /////////////////////////////////////////////////////////////////////////

  private class TodosPanel extends MandelListPanel {

    public TodosPanel()
    {
      super(null, getEnvironment().getTodosModel(), null,
            !getEnvironment().isReadonly());
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // history tab
  /////////////////////////////////////////////////////////////////////////
  private class HistoryPanel extends TablePanel<MandelListTableModel>
                             implements MandelListModelSource {
    private History             history;
    private QualifiedMandelName mandelname;

    public HistoryPanel()
    {
      super(null,MandelListsDialog.this.getMandelWindowAccess().getHistory(),
            null);
      LoadAction load=new LoadAction();
      addActionListener(load);
      addButton("Load", load);
      addButton("Clear", new ClearAction());
      history=MandelListsDialog.this.getMandelWindowAccess().getHistory();
      setContextMenuHandler(new ContextHandler());
    }

    @Override
    protected void setSelection(int index, int col)
    {
      mandelname=getModel().getQualifiedName(index);
      System.out.println("model index: "+index+
         "("+getTable().getSelectionModel().getMaxSelectionIndex()+"): "+
         mandelname);
    }

    /////////////////////////////////////////////////////////////////////////
    private class LoadAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {   
        System.out.println("*** load history entry "+mandelname);
        if (mandelname==null) return;
        setBusy(true);
        if (!getMandelWindowAccess().getMandelImagePane().setImage(mandelname)) {
          JOptionPane.showMessageDialog(getOwner(),
                                        "Cannot load image: "+mandelname, //text to display
                                        "Mandel IO", //title
                                        JOptionPane.WARNING_MESSAGE);
        }
        setBusy(false);
      }
    }
    
    /////////////////////////////////////////////////////////////////////////
    private class ClearAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        getModel().clear();
      }
    }

    //////////////////////////////////////////////////////////////////////////
    private class ContextHandler extends MandelListContextMenuHandler
      implements ContextMenuHandler {

      @Override
      public void handleContextMenu(JComponent comp, MouseEvent evt,
                                    int row, int col)
      {
        handleContextMenu(comp, evt, row);
      }

      @Override
      protected JPopupMenu createContextMenu(Integer index)
      {
        JPopupMenu menu;
        QualifiedMandelName sel=null;

        MandelWindowAccess access=getMandelWindowAccess();
        MandelImagePanel mp=getMandelWindowAccess().getMandelImagePane();
        MandelListModel model=getModel();

        if (index<0) return null;

        sel=model.getQualifiedName(index);
        menu=createLabeledMenu(sel.toString());

        MandelHandle h=model.getMandelHandle(index);
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
        menu.add(addMemoryAction);
        Component comp=getContextComponent();
        menu.add(
          access.getEnvironment().getListActions().createMenu(comp, sel));

        return menu;
      }
    }
  }

   /////////////////////////////////////////////////////////////////////////
  // areas tab
  /////////////////////////////////////////////////////////////////////////
  private class AreasPanel extends MandelListPanel {

    public AreasPanel()
    {
      super(null, getEnvironment().getAreasModel(), null,
               !getEnvironment().isReadonly());
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // refinements tab
  /////////////////////////////////////////////////////////////////////////
  private class RefinementsPanel extends MandelListPanel {
    public RefinementsPanel()
    {
      super(null,getEnvironment().getRefinementsModel(),null);
    }
  }
}
