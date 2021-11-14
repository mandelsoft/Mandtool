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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JViewport;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.tool.ctx.MandelListContextMenuHandler;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.swing.DnDJTable;
import com.mandelsoft.swing.MenuButton;
import com.mandelsoft.swing.Selection;
import com.mandelsoft.swing.TablePanel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

/**
 *
 * @author Uwe Krüger
 */
public class MandelListPanel extends TablePanel<MandelListTableModel>
                             implements MandelWindowAccessSource,
                                        MandelNameSelector,
                                        MandelListSelector,
                                        MandelListModelSource {
  private boolean modifiable;
  private MandelName rootName;

  //private MandelListTransferHandler transfer;
  private JButton removeButton;
  private JButton addButton;
  private JButton galeryButton;

  private GaleryAction galery;
  private Set<JButton> buttons;

  private boolean slideShow;
          
  public MandelListPanel()
  {
    super();
    setup(null);
    handleModifiable(modifiable);
  }

//  public MandelListPanel(String header, MandelList list,
//                                        MandelScanner scanner,
//                              ActionListener action)
//  {
//    super(header, new MandelListTableModel(list,scanner), action);
//    setup(action);
//    handleModifiable(modifiable);
//  }

  public MandelListPanel(String header, MandelListTableModel model,
                              ActionListener action)
  {
    super(header, model, null);
    setup(action);
    handleModifiable(modifiable=model.isModifiable());
  }

  public MandelListPanel(boolean modifiable)
  {
    super();
    setup(null);
    this.modifiable=modifiable;
    handleModifiable(modifiable);
  }


  public MandelListPanel(String header, MandelListTableModel model,
                         ActionListener action, boolean modifiable)
  {
    super(header, model, null);
    setup(action);
    this.modifiable=modifiable;
    handleModifiable(modifiable);
  }

  public MandelListPanel(String header, MandelListTableModel model,
                         boolean modifiable)
  {
    super(header, model);
    setup(null);
    this.modifiable=modifiable;
    handleModifiable(modifiable);
  }

  public void enableGalery(boolean active)
  {
    galeryButton.setEnabled(active);
  }
  
  public void enableSlideShow(boolean active)
  {
    slideShow=active;
  }

  public MandelWindowAccess getMandelWindowAccess()
  {
    return MandelWindowAccess.Access.getMandelWindowAccess(this);
  }

  @Override
  protected JTable createTable()
  {
    return new DnDJTable();
  }

  protected void setup(ActionListener action)
  {
    if (action==null) {
      action=new LoadAction();
    }
    slideShow=true;
    addActionListener(action);
    addButton("Refresh", new RefreshAction());
    addButton("Load", action);
    addButton("Prev", new PrevAction(action));
    addButton("Next", new NextAction(action));
    removeButton=addButton("Remove", new RemoveAction());
    addButton=addButton("Add", new AddAction());
    galeryButton=addButton("Galery", galery=new GaleryAction());
    setupActions();
    setContextMenuHandler(new ContextHandler());
  }

  @Override
  protected void panelBound()
  {
    //System.out.println("bound: "+this);
    super.panelBound();
    MandelImagePanel mp=getMandelWindowAccess().getMandelImagePane();
//    addButton(mp.getSlideShowModel().getJourneyAction());
//    addButton(mp.getSlideShowModel().getZoomInAction());
//    addButton(mp.getSlideShowModel().getStopAction());

    if (mp!=null && slideShow) {
      JPopupMenu showmenu=mp.getSlideShowModel().
              createPopupMenu(this, true);
      JButton show=new MenuButton(showmenu);
      addButton(show);
    }
  }

  ////////////////////////////////////////////////////////////////////////
  public MandelName getRootName()
  {
    return rootName;
  }

  public void setRootName(MandelName rootName)
  {
    this.rootName=rootName;
  }

  public QualifiedMandelName getSelectedMandelName()
  {
    //return mandelname;
    int index=getSelectedIndex();
    return index>=0?getModel().getQualifiedName(index):null;
  }

  public MandelList getSelectedMandelList()
  {
    return getModel().getList();
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public void setModifiable(boolean modifiable)
  {
    if (modifiable!=this.modifiable)
    {
      handleModifiable(this.modifiable=modifiable);
      firePropertyChange("modifiable", !modifiable, modifiable);
    }
  }

  protected void handleModifiable(boolean modifiable)
  {
    //System.out.println("set mod "+modifiable);
    if (removeButton!=null) {
      removeButton.setEnabled(modifiable);
      removeButton.setVisible(modifiable);
    }
    if (addButton!=null) {
      addButton.setEnabled(modifiable);
      addButton.setVisible(modifiable);
    }
    getModel().setModifiable(modifiable);
    //if (transfer!=null) transfer.setActive(modifiable);
    setFillsViewportHeight(modifiable);
  }

  @Override
  public void setModel(MandelListTableModel model)
  {
    super.setModel(model);
    setModifiable(getModel().isModifiable());
    setupActions();
    invalidate();
    validate();
  }

  private void setupActions()
  {
    if (buttons==null) buttons=new HashSet<JButton>();

    for (JButton b: buttons) {
      removeButton(b);
    }
    List<Action> actions=getModel().getActions();
    if (actions!=null) {
      for (Action a:actions) {
        JButton b=new JButton(a);
        addButton(b);
        buttons.add(b);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Actions
  /////////////////////////////////////////////////////////////////////////

  private class GaleryAction implements ActionListener {

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess w=getMandelWindowAccess();
      MandelListGaleryDialog d=new MandelListGaleryDialog(w,getModel(),getTitle());
      d.setRootName(getRootName());
    }
  }

  private class RefreshAction implements ActionListener {

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess w=getMandelWindowAccess();
      if (w==null) {
        getModel().refresh();
      }
      else {
        Environment env=w.getEnvironment();
        System.out.println("*** initiate refresh "+getModel());
        getModel().refresh(env);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  protected class LoadAction implements ActionListener {

    public void actionPerformed(ActionEvent e)
    {
      try {
        MandelAreaViewDialog v;
        QualifiedMandelName name=getSelectedMandelName();
        if (name!=null) {
          setBusy(true);
          MandelHandle found=getModel().getMandelData(getSelectedIndex());
          if (found==null) {
            //System.out.println("file="+found.getFile());
          }
          else {
            if (found.getHeader().hasImageData()) {
              // load mage
              if (getMandelWindowAccess().getMandelImagePane().setImage(found)) {
                handleLoaded(name);
              }
              else {
                JOptionPane.showMessageDialog(getWindow(),
                                              "Cannot load image: "+name,
                                              "Mandel IO",
                                              JOptionPane.WARNING_MESSAGE);
              }
            }
            else {
              MandelData data=found.getInfo();
              // show meta data
              v=new MandelImageAreaDialog(getMandelWindowAccess(),
                                          "Mandel Image Meta Information",
                                          name,
                                          data);
              v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
              v.setVisible(true);
            }
          }
        }
      }
      catch (IOException ex) {
        Error("Mandel Meta Data","cannot load mandel data");
      }
      finally {
        setBusy(false);
      }
    }

    protected void handleLoaded(QualifiedMandelName name)
    {
    }
  }

  /////////////////////////////////////////////////////////////////////////
  private class SelectListener implements ActionListener {
    private ActionListener slave;

    public SelectListener(ActionListener slave)
    {
      this.slave=slave;
    }

    protected int getIndex(int index)
    {
      return index;
    }

    public void actionPerformed(ActionEvent e)
    {
      int index=getTable().getSelectionModel().getLeadSelectionIndex();
      index=getIndex(index);
      if (index>=0&&index<getTable().getRowCount()) {
        System.out.println("select "+index);
        getTable().getSelectionModel().setSelectionInterval(index, index);
        scrollToVisible(index,0);
        slave.actionPerformed(e);
      }
    }
  }

  private class NextAction extends SelectListener {
    public NextAction(ActionListener load)
    {
      super(load);
    }

    @Override
    protected int getIndex(int index)
    {
      if (++index>=getTable().getRowCount()) index=0;
      return index;
    }
  }

  private class PrevAction extends SelectListener {
    public PrevAction(ActionListener load)
    {
      super(load);
    }

    @Override
    protected int getIndex(int index)
    {
      if (index<0) index=getTable().getRowCount();
      return index-1;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  private class RemoveAction implements ActionListener {

    public void actionPerformed(ActionEvent e)
    {
      if (getSelectedMandelName()==null) return;
      getModel().remove(getSelectedMandelName());
      try {
        getModel().getList().save();
      }
      catch (IOException io) {
        JOptionPane.showMessageDialog(getWindow(),
                                      "Cannot save list file: "+io, "Mandel IO",
                                      JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private class AddAction implements ActionListener {

    public void actionPerformed(ActionEvent e)
    {
      MandelWindowAccess w=getMandelWindowAccess();
      if (w!=null) {
        getModel().add(w.getQualifiedName());
      }
    }
  }

  public void scrollToVisible(int rowIndex, int vColIndex)
  {
    JTable table=getTable();
    if (!(table.getParent() instanceof JViewport)) {
        return;
    }
    JViewport viewport = (JViewport)table.getParent();

    // This rectangle is relative to the table where the
    // northwest corner of cell (0,0) is always (0,0).
    Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);

    // The location of the viewport relative to the table
    Point pt = viewport.getViewPosition();

    // Translate the cell location so that it is relative
    // to the view, assuming the northwest corner of the
    // view is (0,0)
    rect.setLocation(rect.x-pt.x, rect.y-pt.y);

    // Scroll the area into view
    viewport.scrollRectToVisible(rect);
  }

  //////////////////////////////////////////////////////////////////////////

  private class ContextHandler extends MandelListContextMenuHandler
                               implements ContextMenuHandler {

    @Override
    protected JPopupMenu createListContextMenu(JPopupMenu menu)
    {
      JMenuItem it;
      menu=super.createListContextMenu(menu);
      if (menu==null) menu=new JPopupMenu();
      it=new JMenuItem("Galery");
      it.addActionListener(galery);
      menu.add(it);
      
      if (rootName!=null) { 
        menu.add(new JMenuItem(rootAction));
      }
      return menu;
    }

    private Action rootAction=new RootAction();

    private class RootAction extends LoadImageAction {
      public RootAction()
      { super("Load Root Image");
      }

      @Override
      public QualifiedMandelName getSelectedItem()
      {
        return new QualifiedMandelName(rootName);
      }
    }
  }
}
