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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.ctx.MandelListContextMenuHandler;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.swing.DnDJList;
import com.mandelsoft.swing.Selection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListGaleryPanel
             extends MandelGaleryPanel<QualifiedMandelName,MandelListListModel>
             implements MandelNameSelector, MandelListSelector,
                        MandelListModelSource {
  private boolean navToFolder=true;
  private MandelName rootName;

  public MandelListGaleryPanel(MandelListListModel model)
  {
   this(model,1);
  }

  public MandelListGaleryPanel(MandelListListModel model, int rows, Dimension d)
  {
    super(model,rows, d);
    list.addMouseListener(new Listener());

    setContextMenuHandler(new ContextHandler());
  }
  
  public MandelListGaleryPanel(MandelListListModel model, int rows)
  {
    this(model,rows, new Dimension(300,250));
  }

  @Override
  protected void panelUnbound()
  {
    if (ticker!=null) ticker.stop();
    super.panelUnbound();
  }

  public boolean isNavToFolder()
  {
    return navToFolder;
  }

  public MandelName getRootName()
  {
    return rootName;
  }

  public void setRootName(MandelName rootName)
  {
    this.rootName=rootName;
  }

  public void setNavToFolder(boolean navToFolder)
  {
    this.navToFolder=navToFolder;
  }

  @Override
  protected String getLabel(QualifiedMandelName elem)
  {
    return elem.toString();
  }

  public QualifiedMandelName getSelectedMandelName()
  {
    int index=getSelectedIndex();
    return index>=0?getModel().getQualifiedName(index):null;
  }

  public MandelList getSelectedMandelList()
  {
    return getModel().getList();
  }

  public int getSelectedIndex()
  {
    return list.getSelectedIndex();
  }
  
  private class Listener extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e)
    {
      if (e.getClickCount()<2) return;
      QualifiedMandelName sel=(QualifiedMandelName)list.getSelectedValue();
      if (sel==null) return;
      setBusy(true);
      MandelWindowAccess access=getMandelWindowAccess();
      if (access.getMandelImagePane()!=null) {
        if (!access.getMandelImagePane().setImage(sel)) {
          JOptionPane.showMessageDialog(getWindow(),
                                        "Cannot load image: "+sel,
                                        "Mandel IO", JOptionPane.WARNING_MESSAGE);
        }
      }
      else {
        MandelListContextMenuHandler.createMandelImageFrame(getWindow(),
                access, getMaxFrame(), sel);
      }
      setBusy(false);
    }
  }

  private Ticker ticker=new Ticker();

  private int last;
  
  private void requestThumbnail(int index)
  {
    if (index>=0 && index<list.getModel().getSize()) {
      System.out.println("request index "+(index));
      getModel().requestThumbnail(index, getMaxImage());
    }
  }
  
  @Override
  protected void requestListIndex(int index)
  {
    if (ticker.isRunning()) {
      if (last!=index) {
        last=index;
        requestThumbnail(index);
        requestThumbnail(index+1);
      }
    }
  }
  
  public void startTicker()
  {
    if (list.getVisibleRowCount()==1 &&
          scrollPane.getHorizontalScrollBar().isVisible()) {
      ticker.start();
    }
  }

  public void startTicker(int delay)
  {
    if (list.getVisibleRowCount()==1 &&
          scrollPane.getHorizontalScrollBar().isVisible()) {
      System.out.println("start ticker");
      ticker.start(delay);
    }
    else {
      System.out.println("ticker not possible");
    }
  }

  public void stopTicker()
  {
    ticker.stop();
  }

  private class ContextHandler extends MandelListContextMenuHandler
                               implements DnDJList.ContextMenuHandler {

    @Override
    protected int getMaxFrame()
    {
      return MandelListGaleryPanel.this.getMaxFrame();
    }

    @Override
    protected JPopupMenu createItemContextMenu(Selection select)
    {
      JMenuItem it;
      JPopupMenu menu=super.createItemContextMenu(select);
      if (list.getVisibleRowCount()==1 &&
          scrollPane.getHorizontalScrollBar().isVisible()) {
        if (menu==null) {
          menu=new JPopupMenu();
        }
        else {
          menu.addSeparator();
        }
        it=new JMenuItem(tickerStart);
        it.getAction().setEnabled(!ticker.isRunning());
        menu.add(it);

        it=new JMenuItem(tickerStop);
        it.getAction().setEnabled(ticker.isRunning());
        menu.add(it);
      }
      return menu;
    }

    @Override
    protected JPopupMenu createListContextMenu(JPopupMenu menu)
    {
      menu=super.createListContextMenu(menu);
      if (rootName!=null) {
        if (menu==null) menu=new JPopupMenu();
        menu.add(new JMenuItem(rootAction));
      }
      return menu;
    }


    /////////////////////////////////////////////////////////////////
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

    /////////////////////////////////////////////////////////////////
    private Action tickerStart=new TickerStartAction();

    private class TickerStartAction extends AbstractAction {

      public TickerStartAction()
      {
        super("Ticker");
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        startTicker();
      }
    }

    /////////////////////////////////////////////////////////////////
    private Action tickerStop=new TickerStopAction();

    private class TickerStopAction extends AbstractAction {

      public TickerStopAction()
      {
        super("Stop Ticker");
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        stopTicker();
      }
    }
  }

    
}
