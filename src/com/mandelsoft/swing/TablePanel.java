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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Uwe Krüger
 */

public class TablePanel<T extends TableModel>
        extends ActionPanel {

  private JLabel label;
  private JTable table;
  private JScrollPane scrollpane;
  private T model;
  private String title;
  private ModelListener modellistener;
  private boolean showsize;
  private TableSelection selection;

  protected TablePanel()
  {
    setup(null,null,null);
  }

  public TablePanel(String header, T model)
  {
    setup(header, model, null);
  }

  public TablePanel(String header, T model, ActionListener action)
  {
    setup(header, model, action);
  }

  @Override
  protected void panelUnbound()
  {
    super.panelUnbound();
    if (modellistener!=null && model!=null && label!=null) {
      model.removeTableModelListener(modellistener);
    }
  }

  public void setModel(T model)
  {
    if (this.model==model) return;
    if (this.model!=null && modellistener!=null) {
      this.model.removeTableModelListener(modellistener);
    }
    this.model=model;
    if (this.model!=null) {
      if (modellistener==null) modellistener=new ModelListener();
      this.model.addTableModelListener(modellistener);
    }
    this.table.setModel(model);
    table.getRowSorter().toggleSortOrder(0);
  }

  public void setSortOrder(int column, SortOrder order)
  {
    List<RowSorter.SortKey> sortKeys = new ArrayList<>(1);
    sortKeys.add(new RowSorter.SortKey(column, order));
    table.getRowSorter().setSortKeys(sortKeys);
  }
   
  public void setTitle(String name)
  {
    title=name;
    modelUpdated();
  }

  public String getTitle()
  {
    return title;
  }

  protected void modelUpdated()
  {
    if (label!=null) {
      if (showsize) {
        int c=getModel().getRowCount();
        label.setText(title+"("+com.mandelsoft.util.Utils.sizeString(c,"entry")+")");
      }
      else {
        label.setText(title);
      }
    }
  }

  public boolean isShowSize()
  {
    return showsize;
  }

  public void setShowSize(boolean showsize)
  {
    if (showsize==this.showsize) return;
    this.showsize=showsize;
    if (showsize) {
      if (label!=null&&model!=null) {
        if (modellistener==null) modellistener=new ModelListener();
        model.addTableModelListener(modellistener);
      }
    }
    else {
      if (modellistener!=null&&model!=null) {
        model.removeTableModelListener(modellistener);
      }
      modellistener=null;
    }
    modelUpdated();
  }

  public void setFillsViewportHeight(boolean fillsViewportHeight)
  {
    table.setFillsViewportHeight(fillsViewportHeight);
  }

  private boolean busy;
  private Cursor origcursor;

  protected void setBusy(boolean b)
  {
    if (b!=busy) {
      if (b) {
        System.out.println("-------------------------------------------------");
        System.out.println("set busy");
        origcursor=getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
      else {
        System.out.println("orig cursor");
        setCursor(origcursor);
      }
      firePropertyChange("busy",!b,b);
    }
    busy=b;
  }

  private void setup(String header, T model, ActionListener action)
  {
    JLabel c;

    showsize=true;
    if (debug) System.out.println("*** creating "+this);
    if (action!=null) addActionListener(action);
    if (!com.mandelsoft.util.Utils.isEmpty(header)) {
      label=c=new JLabel(header);
      label.setHorizontalAlignment(JLabel.CENTER);
      addContent(label, GBC(0, 0, GBC.HORIZONTAL).setAnchor(GBC.CENTER));
    }
    else {
      c=new JLabel("height");
    }
    table=createTable();
    selection=new TableSelection(table);
    setupTable(table);
    if (model!=null) setModel(model);
    table.getSelectionModel().addListSelectionListener(
            new TableListener());
    table.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent event)
      { // check for double click
        //System.out.println("mouse clicked");
        if (event.getClickCount()<2)
          return;

//          // find column of click and
//          int row=table.rowAtPoint(event.getPoint());
//
//          // translate to table model index and sort
//          int modelRow=table.convertRowIndexToModel(row);
//          System.out.println("row index: "+row+"; model index: "+modelRow);
        fireActionPerformed(ActionEvent.ACTION_PERFORMED, null);
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        handlePopup(e);
      }

      @Override
      public void mousePressed(MouseEvent e)
      {
        handlePopup(e);
      }

      public void handlePopup(MouseEvent e)
      {
        //System.out.println("check table ctx popup event");
        if (e.isPopupTrigger() && ctxmenu!=null) {
          selection.setLeadSelection(e);
          System.out.println("CTX POPUP at "+e.getPoint().getX()+","+
                                             e.getPoint().getY()+" ("
                                             +selection+")");
          ctxmenu.handleContextMenu(table, e, selection);
        }
      }
    });

    FontMetrics m=c.getFontMetrics(c.getFont());
    getTable().setPreferredScrollableViewportSize(new Dimension(
            (int)(m.charWidth('W')*40), (int)(m.getHeight()*6)));

    addContent(scrollpane=new JScrollPane(table), GBC(0, 1, GBC.BOTH));
  }

  protected JTable createTable()
  {
    return new IJTable();
  }

  protected void setupTable(JTable table)
  {
    table.setAutoCreateRowSorter(true);
    //rastertable.setFillsViewportHeight(true);
    
    table.setShowGrid(false);
    //rastertable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }

  protected void setSelection(TableSelection s)
  {
//    setSelection(row);
  }

//  protected void setSelection(int row)
//  {
//  }

  public T getModel()
  {
    return model;
  }

  public JTable getTable()
  {
    return table;
  }

  public JScrollPane getScrollPane()
  {
    return scrollpane;
  }

  public int convertRowIndexToModel(int index)
  {
    return table.convertRowIndexToModel(index);
  }

  public int convertColumnIndexToModel(int index)
  {
    return table.convertColumnIndexToModel(index);
  }

  public void setSelectedRow(int index)
  {
    table.clearSelection();
    index=table.convertRowIndexToView(index);
    table.addRowSelectionInterval(index, index);
  }
  
  public int getSelectedIndex()
  {
    int index=table.getSelectedRow();
    int ix2=table.getSelectionModel().getLeadSelectionIndex();

    //System.out.println("sel idx="+index+", lead="+ix2);
    index=ix2;
    if (index>=0) {
      index=convertRowIndexToModel(index);
    }
    return index;
  }

  public int getSelectedColumn()
  {
    int index=table.getSelectedColumn();

    if (index>=0) {
      index=convertColumnIndexToModel(index);
    }
    return index;
  }

  private class TableListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel lsm=(ListSelectionModel)e.getSource();
      if (e.getValueIsAdjusting()) return;
      int col=table.getSelectedColumn();
      int index=table.getSelectedRow();
      int ix2=table.getSelectionModel().getLeadSelectionIndex();
//      System.out.println("selected: "+index+"/"+ix2+" "+lsm.getMaxSelectionIndex()+
//                                    "@"+col);
      index=ix2;
      if (index<0) return;
      selection.setLeadSelection(index,col);
      System.out.println("SELECTION: "+selection);
      setSelection(selection);
    }
  }

  private class ModelListener implements TableModelListener {
    public void tableChanged(TableModelEvent e)
    {
      modelUpdated();
    }
  }

  ////////////////////////////////////////////////////////////////
  // mouselistener support
  ////////////////////////////////////////////////////////////////

  public void addActionListener(ActionListener l)
  {
    listenerList.add(ActionListener.class, l);
  }

  public void removeActionListener(ActionListener l)
  {
    listenerList.remove(ActionListener.class, l);
  }

  public ActionListener[] getActionListeners()
  {
    return getListeners(ActionListener.class);
  }

  protected void fireActionPerformed(int id, String cmd)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    ActionEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==ActionListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new ActionEvent(this, id, cmd);
        ((ActionListener)listeners[i+1]).actionPerformed(e);
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////
  // Context Menu
  ///////////////////////////////////////////////////////////////////////

  public interface ContextMenuHandler extends DnDJTable.ContextMenuHandler  {
  }

  //////////////////////////////////////////////////////////////////////////
  // Context Menu outside of list

  private class Listener extends MouseAdapter {
    @Override
    public void mouseReleased(MouseEvent e)
    {
      handlePopup(e);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      handlePopup(e);
    }

    public void handlePopup(MouseEvent e)
    {
      if (e.isPopupTrigger()&&ctxmenu!=null) {
        System.out.println("CTX POPUP at panel");
        ctxmenu.handleContextMenu(TablePanel.this, e, 
                                  new TableSelection(table,-1, -1));
      }
    }
  }

  private DnDJTable.ContextMenuHandler ctxmenu;
  private Listener                     mouselistener;

  synchronized
  public void setContextMenuHandler(DnDJTable.ContextMenuHandler h)
  {
    if (h==null) {
      if (ctxmenu!=null) removeMouseListener(mouselistener);
    }
    else {
      if (ctxmenu==null) {
        if (mouselistener==null) mouselistener=new Listener();
        addMouseListener(mouselistener);
        scrollpane.getViewport().addMouseListener(mouselistener);
      }
    }
    ctxmenu=h;

  }

  synchronized
  public DnDJTable.ContextMenuHandler getContextMenuHandler()
  {
    return ctxmenu;
  }
}