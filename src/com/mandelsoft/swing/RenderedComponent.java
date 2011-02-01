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

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * A rendered component contains any content, that does not catch any
 * events.
 * 
 * @author Uwe Krueger
 */
public class RenderedComponent extends JComponent {

  private JComponent effective;
  private CellRendererPane renderer;
  private boolean redispatch;
  private Listener listener;

  public RenderedComponent()
  {
    super.setBorder(null);
    listener=new Listener();
    renderer=new CellRendererPane();
    redispatch=true;
    add(renderer);
  }

  public boolean isRedispatch()
  {
    return redispatch;
  }

  public void setRedispatch(boolean redispatch)
  {
    if (redispatch!=this.redispatch) {
      this.redispatch=redispatch;
      if (redispatch) {
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        this.addMouseWheelListener(listener);
//        this.addKeyListener(listener);
//        this.addFocusListener(listener);
      }
      else {
        this.removeMouseListener(listener);
        this.removeMouseMotionListener(listener);
        this.removeMouseWheelListener(listener);
//        this.removeKeyListener(listener);
//        this.removeFocusListener(listener);
      }
    }
  }

  public JComponent getEffective()
  {
    return effective;
  }

  public void setEffectiveComponent(JComponent effective)
  {
    this.effective=effective;
    effective.validate();
  }

  @Override
  public void setBorder(Border border)
  {
    effective.setBorder(border);
  }

  @Override
  public Dimension getPreferredSize()
  {
    return effective.getPreferredSize();
  }

  @Override
  public void setBounds(int x, int y, int width, int height)
  {
    effective.setBounds(0, 0, width, height);
    super.setBounds(x, y, width, height);
  }

  @Override
  public void paint(Graphics g)
  {
    Graphics2D g2=(Graphics2D)g.create();
    if (!this.isOpaque()) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    }
    renderer.paintComponent(g2, effective, this, 0, 0,
                            effective.getWidth(), effective.getHeight(), true);
    renderer.remove(effective);
    g2.dispose();
  }

  private void redispatchMouseEvent(MouseEvent e)
  {
    Point glassPanePoint=e.getPoint();
    Container container=effective;
    renderer.add(effective);
    Point containerPoint=SwingUtilities.convertPoint(
      this,
      glassPanePoint,
      effective);

    //The mouse event is probably over the content pane.
    //Find out exactly which component it's over.
    Component component=
      SwingUtilities.getDeepestComponentAt(
      container,
      containerPoint.x,
      containerPoint.y);

    if (component!=null) {
      //Forward events over the check box.
      Point componentPoint=SwingUtilities.convertPoint(
        this,
        glassPanePoint,
        component);
      component.dispatchEvent(new MouseEvent(component,
                                             e.getID(),
                                             e.getWhen(),
                                             e.getModifiers(),
                                             componentPoint.x,
                                             componentPoint.y,
                                             e.getClickCount(),
                                             e.isPopupTrigger()));
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // Dispatching
  ///////////////////////////////////////////////////////////////////////////

  private class Listener implements MouseListener,
                                    MouseMotionListener,
                                    MouseWheelListener,

                                    KeyListener,
                                    FocusListener {

    public void mouseClicked(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mousePressed(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mouseReleased(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mouseEntered(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mouseExited(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mouseDragged(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mouseMoved(MouseEvent e)
    {
      redispatchMouseEvent(e);
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
      redispatchMouseEvent(e);
    }

    ///////////////////

    public void keyPressed(KeyEvent e)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void keyReleased(KeyEvent e)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void keyTyped(KeyEvent e)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void focusGained(FocusEvent e)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void focusLost(FocusEvent e)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }


  }
}
