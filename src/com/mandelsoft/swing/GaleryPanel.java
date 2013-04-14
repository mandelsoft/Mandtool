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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import com.mandelsoft.swing.DnDJList.ContextMenuHandler;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Uwe Krueger
 */
public abstract class GaleryPanel<E,M extends ThumbnailListModel<E>> extends GBCPanel {
  static Color backColor = Color.lightGray;

  private int maxframe=0;
  private Dimension maximage=new Dimension(180,180);
  private int gap=40;
  private Dimension item;
  private Renderer renderer;
  protected DnDJList  list;
  protected JScrollPane scrollPane;
  private GBCPanel panel;
  private M model;
  private ThumbnailChangeListener thumblistener;

  public GaleryPanel(M model)
  {
    this(model,1,null);
  }
  
  public GaleryPanel(M model, int rows, Dimension d)
  {
    if (d!=null) maximage=d;
    System.out.println("Galery panel for items of size "+Dimensions.toString(d));
    thumblistener=new ThumbnailChangeListener();
    panel=new GBCPanel(){

    };
    panel.setBackground(backColor);
    this.model=model;
    renderer=new Renderer();
    list=new DnDJList(model);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setVisibleRowCount(rows);
    list.setCellRenderer(renderer);
    list.setBackground(backColor);
    //list.setBorder(new BevelBorder(BevelBorder.LOWERED));
    panel.add(list,GBC(0,0).setAnchor(GBC.CENTER));
    // panel.setBorder(new BevelBorder(BevelBorder.RAISED));
    panel.setTransferHandler(list.getTransferHandler());
    scrollPane=new JScrollPane(panel);
    setBorder(new BevelBorder(BevelBorder.LOWERED));
    add(scrollPane,GBC(0,0,GBC.BOTH).setAnchor(GBC.CENTER));
    item=renderer.getPreferredSize();

    ResizeListener l=new ResizeListener();
    addComponentListener(l);
  }

  @Override
  protected void panelBound()
  {
    System.out.println("galery item size "+Dimensions.toString(item));
    super.panelBound();
    if (model!=null) {
      model.addThumbnailListener(thumblistener);
      if (model instanceof ProxyModel) {
        ((ProxyModel)model).bind();
      }
    }
  }

  @Override
  protected void panelUnbound()
  {
    System.out.println("cleanup galery");
    super.panelUnbound();
    if (model!=null) {
      if (model instanceof ProxyModel) {
        ((ProxyModel)model).unbind();
      }
      model.removeThumbnailListener(thumblistener);
    }
  }

  protected abstract String getLabel(E elem);
  protected abstract Icon getIcon(E elem);
  
  public int getMaxFrame()
  {
    return maxframe;
  }

  public void setMaxFrame(int maxframe)
  {
    this.maxframe=maxframe;
  }

  public Dimension getMaxImage()
  {
    return maximage;
  }
  
  public M getModel()
  {
    return model;
  }
  
  public class TestRenderer extends JPanel
                            implements ListCellRenderer {
    private JLabel label;

    TestRenderer()
    {
      super(false);
       add(label=new JLabel());
       validate();
    }
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus)
    {
      label.setText(getLabel((E)value));
      validate();
      return this;
    }

  }

  public class ItemPanel extends GBCPanel {

    protected JLabel label;
    protected ImagePanel panel;

    ItemPanel()
    {
      add(panel=new ImagePanel(), GBC(0, 0).setInsets(10, 10, 10, 10));
      add(label=new JLabel(), GBC(0, 1).setInsets(0, 10, 10, 10));
    }

    public void setData(E elem)
    {
      label.setText(getLabel(elem));
      label.setIcon(getIcon(elem));
      panel.setData(elem);
    }
  }

  public class ImagePanel extends JComponent {

    private E      elem;
    private Border border;

    public ImagePanel()
    {
      border=(new BevelBorder(BevelBorder.LOWERED));
      //setBorder(null);
      setBorder(new BevelBorder(BevelBorder.RAISED,4));

      Insets insets=getInsets();
      Dimension s=new Dimension((int)maximage.getWidth()+insets.right+insets.left+gap,
                                (int)maximage.getHeight()+insets.top+insets.bottom+gap);
      setPreferredSize(s);
      setBackground(Color.GRAY);
      //setSize(s);
      //addMouseListener(new ML(elem));
    }

    void setData(E elem)
    {
      this.elem=elem;
    }

    @Override
    public void paintComponent(Graphics g)
    {
      Rectangle r=g.getClipBounds();
//      System.out.println(elem+" local insets: "+getInsets()
//              +" rect: "+Rectangles.toString(r)+": "+this.getBackground());
      if (!r.isEmpty()) {
        int x,y;

        Insets insets=getInsets();
        g.setColor(getBackground());
        g.fillRect(insets.left, insets.top,
                   (int)maximage.getWidth()+gap, (int)maximage.getHeight()+gap);


        BufferedImage image=model.getThumbnail(elem, maximage);
        x=(int)(maximage.getWidth()-image.getWidth()+gap)/2+insets.left;
        y=(int)(maximage.getHeight()-image.getHeight()+gap)/2+insets.top;
        g.drawImage(image, x, y, null);
        Insets binsets=border.getBorderInsets(this);
        border.paintBorder(this, g, x-binsets.left, y-binsets.top,
                                 image.getWidth()+binsets.left+binsets.right,
                                 image.getHeight()+binsets.top+binsets.bottom);
      }
    }
  }

  protected void requestListIndex(int index)
  {
  }
  
  public class Renderer extends ItemPanel implements ListCellRenderer {

    Renderer()
    {
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus)
    {
      setData((E)value);
      requestListIndex(index);
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
        //panel.setBackground(list.getSelectionBackground());
      }
      else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        panel.setBackground(Color.GRAY);
      }
      //revalidate();
      return this;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  private class ResizeListener extends ComponentAdapter
                               implements PropertyChangeListener {

    @Override
    public void componentResized(ComponentEvent e)
    {
      adjustList();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
      System.out.println("panel prop changed: "+evt.getPropertyName());
      if (evt.getPropertyName().equals("size")) {
        adjustList();
      }
    }

    private void adjustList()
    {
      Dimension n=getSize();
      //System.out.println("panel size: "+Dimensions.toString(n));
      int h=(int)(n.getHeight()/item.getHeight());
      if (h<1) h=1;
      if (list.getVisibleRowCount()!=h) {
        list.setVisibleRowCount(h);
        list.revalidate();
      }
    }
  }

  private class ThumbnailChangeListener implements ThumbnailListener<E> {
    public void thumbnailChanged(ThumbnailEvent<E> event)
    {
      repaint();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
 
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

 

  
  //////////////////////////////////////////////////////////////////////////
  // Context Menu outside of list
  //////////////////////////////////////////////////////////////////////////

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
        ctxmenu.handleContextMenu(panel, e, new ListSelection(list));
      }
    }
  }
  
  private ContextMenuHandler ctxmenu;
  private Listener           listener;

  synchronized
  public void setContextMenuHandler(DnDJList.ContextMenuHandler h)
  {
    if (h==null) {
      if (ctxmenu!=null) panel.removeMouseListener(listener);
    }
    else {
      if (ctxmenu==null) {
        if (listener==null) listener=new Listener();
        panel.addMouseListener(listener);
      }
    }
    ctxmenu=h;
    list.setContextMenuHandler(h);
  }

  synchronized
  public ContextMenuHandler getContextMenuHandler()
  {
    return ctxmenu;
  }

  //////////////////////////////////////////////////////////////////////////

  public class Ticker extends Timer implements ActionListener,
                                               ChangeListener {
    private Rectangle rect;
    private boolean inupdate;

    public Ticker()
    {
      super(100,null);
      rect=new Rectangle(0,0,1,1);
      this.addActionListener(this);
      scrollPane.getViewport().addChangeListener(this);
    }

    public void actionPerformed(ActionEvent e)
    {
//      System.out.println("tick...");
      if (!scrollPane.getHorizontalScrollBar().isVisible()) {
        this.stop();
      }
      Rectangle r=scrollPane.getViewport().getViewRect();
      int w=(int)panel.getPreferredSize().getWidth();
//      System.out.println("     vx="+r.getX()+", vw="+r.getWidth()+
//                         ", w="+w+", cur="+rect.getX());
      if (r.getX()+r.getWidth()+1<w) {
        setX((int)rect.getX()+1);
        inupdate=true;
        try {
          list.scrollRectToVisible(rect);
        }
        finally {
          inupdate=false;
        }
      }
      else {
        this.stop();
      }
    }

    @Override
    public void restart()
    {
      setX(1);
      super.restart();
    }

    @Override
    public void start()
    {
      Rectangle r=scrollPane.getViewport().getViewRect();
//      setX(0);
//      list.scrollRectToVisible(rect);
      setX((int)(r.getWidth()+r.getX()));
      this.setInitialDelay(this.getDelay());
      super.start();
    }

     public void start(int delay)
    {
      Rectangle r=scrollPane.getViewport().getViewRect();
//      setX(0);
//      list.scrollRectToVisible(rect);
      setX((int)(r.getWidth()+r.getX()));
      this.setInitialDelay(delay);
      super.start();
    }

    @Override
    public void stop()
    {
      System.out.println("stop ticker");
      super.stop();
    }

    private void setX(int x)
    {
      rect.setRect(x, 0, 1, 1);
    }

    public void stateChanged(ChangeEvent e)
    {
      if (!inupdate) stop();
    }
  }
}
