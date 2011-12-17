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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import com.mandelsoft.swing.BufferedComponent.RectPointEventListener;
import com.mandelsoft.swing.BufferedComponent.RectEventListener;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEventListener;
import com.mandelsoft.swing.BufferedComponent.RectangleSelector;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;

/**
 *
 * @author Uwe Krueger
 */
public class BufferedFrame extends JFrame {
  static public boolean debug=false;
  static public final int FRAME_INSETS= 5;

  private BufferedComponent buffer;
  private JScrollPane       scrollpane;
  private boolean           partial=false;

  public BufferedFrame()
  {
    this(new BufferedComponent());
  }

  public BufferedFrame(int width, int height)
  {
    this(new BufferedComponent(width, height));
  }

  public BufferedFrame(BufferedComponent c)
  { buffer=c;
    scrollpane=new JScrollPane(c);
    scrollpane.setBorder(null);
    add(scrollpane);
    pack();
    c.setLimitWindowSize(true);
    scrollpane.getViewport().addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e)
      {
        //System.out.println("RS: "+e);

        JViewport vp=scrollpane.getViewport();
        if (vp!=null) {
          if (debug) System.out.println("PREF:"+buffer.getPreferredSize());
          if (debug) System.out.println("VP: "+vp.getViewPosition());
          if (debug) System.out.println("VP: "+vp.getExtentSize());
          partial=!buffer.getPreferredSize().equals(vp.getExtentSize());
        }
        else partial=false;
      }
    });
  }

  public BufferedFrame(BufferedImage image)
  { this(new BufferedComponent(image));
  }

  public boolean isPartial()
  {
    return partial;
  }

  public BufferedComponent getImagePane()
  { return buffer;
  }

  public JScrollPane getScrollPane()
  { return scrollpane;
  }

  public void setInitialSize()
  {
    Dimension id=buffer.getPreferredSize();
    Dimension sd=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension d=new Dimension(id);
    Insets insets=getInsets();
    Dimensions.mod.add(d,insets.left+insets.right,insets.top+insets.bottom);
    Dimensions.mod.sub(sd,FRAME_INSETS,FRAME_INSETS);
    Dimensions.mod.limit(d,sd);
    setSize(d);
  }

  public void setImage(BufferedImage image)
  {
    if (debug) System.out.println("image to buffer");
    buffer.setImage(image);
    buffer.setScale(1);
    if (debug) System.out.println("image buffer set");
  }

  public void setComponentPopupMenu(JPopupMenu menu)
  {
    buffer.setComponentPopupMenu(menu);
  }

  public void showAllRects()
  {
    buffer.showAllRects();
  }

  public VisibleRect createRect(String name, Object o)
  {
    return buffer.createRect(name, o);
  }

  public void setSelectInvisible(boolean b)
  {
    buffer.setSelectInvisible(b);
  }

  public void setRectangleSelector(RectangleSelector s)
  {
    buffer.setRectangleSelector(s);
  }

  public void setLimitWindowSize(boolean b)
  {
    buffer.setLimitWindowSize(b);
  }

  public void removeRectModifiedEventListener(RectModifiedEventListener l)
  {
    buffer.removeRectModifiedEventListener(l);
  }

  public void removeRectEventListener(RectEventListener l)
  {
    buffer.removeRectEventListener(l);
  }

  public void removeCornerEventListener(RectPointEventListener l)
  {
    buffer.removeRectPointEventListener(l);
  }

  public void removeActionListener(ActionListener l, Corner c)
  {
    buffer.removeActionListener(l, c);
  }

  public void discardAllRects()
  {
    buffer.discardAllRects();
  }

  public void addRectModifiedEventListener(RectModifiedEventListener l)
  {
    buffer.addRectModifiedEventListener(l);
  }

  public void addRectEventListener(RectEventListener l)
  {
    buffer.addRectEventListener(l);
  }

  public void addCornerEventListener(RectPointEventListener l)
  {
    buffer.addRectPointEventListener(l);
  }

  public void addActionListener(ActionListener l, Corner c)
  {
    buffer.addActionListener(l, c);
  }

  public Graphics2D createGraphics()
  {
    return buffer.createGraphics();
  }

  public void revalidate()
  { buffer.revalidate();
  }

  public int translateY(MouseEvent e)
  {
    return buffer.translateY(e);
  }

  public int translateY(int y)
  {
    return buffer.translateY(y);
  }

  public int translateX(MouseEvent e)
  {
    return buffer.translateX(e);
  }

  public int translateX(int x)
  {
    return buffer.translateX(x);
  }

  public MouseEvent translate(MouseEvent e)
  {
    return buffer.translate(e);
  }

  private List<RenewStateListener> listeners=new ArrayList<RenewStateListener>();

  public void addRenewStateListener(RenewStateListener l)
  {
    listeners.add(l);
  }
  public void removeRenewStateListener(RenewStateListener l)
  {
    listeners.remove(l);
  }

  protected void fireRenewState()
  {
    for (RenewStateListener l:listeners) {
      try {
       l.renewState(this);
      }
      catch (Exception e) {
        e.printStackTrace(System.err);
      }
    }
  }
}
