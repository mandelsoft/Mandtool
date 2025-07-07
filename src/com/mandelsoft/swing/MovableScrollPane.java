/*
 * Copyright 2025 uwekr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.plaf.LayerUI;

/**
 *
 * @author uwekr
 */
public class MovableScrollPane extends JScrollPane {

  public MovableScrollPane(JComponent c) {
    super();
    JLayer<JScrollPane> layers = new JLayer(c, new ScrollLayer<JComponent>());
    this.getViewport().setView(layers);
  }

  protected boolean isModifierPressed(MouseEvent e) {
    int mods = e.getModifiersEx();
    return (mods & MouseEvent.CTRL_DOWN_MASK) != 0 || (mods & MouseEvent.ALT_DOWN_MASK) != 0;
  }

  protected boolean isButton(MouseEvent e) {
    return e.getButton() == MouseEvent.BUTTON1;
  }

  class ScrollLayer<T extends Component> extends LayerUI<T> {

    private Point lastDragPoint = null;

    @Override
    public void installUI(JComponent c) {
      super.installUI(c);
      ((JLayer<?>) c).setLayerEventMask(
              AWTEvent.MOUSE_EVENT_MASK
              | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
      ((JLayer<?>) c).setLayerEventMask(0);
      super.uninstallUI(c);
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends T> l) {
      if (isButton(e)) {
        if (isModifierPressed(e)) {
          if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            lastDragPoint = e.getPoint();
            e.consume(); // prevent inner components from responding

          }
          if (e.getID() == MouseEvent.MOUSE_RELEASED) {
            lastDragPoint = null;
            e.consume(); // prevent inner components from responding
          }
        }
      }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends T> l) {
      if (e.getID() == MouseEvent.MOUSE_DRAGGED && lastDragPoint != null) {
        if (isModifierPressed(e)) {
          Point current = e.getPoint();
          JViewport viewport = getViewport();

          Point viewPos = viewport.getViewPosition();
          Dimension vd = viewport.getSize();
          int dx = lastDragPoint.x - current.x;
          int dy = lastDragPoint.y - current.y;

          Dimension d = getViewport().getView().getSize();
          viewPos.translate(dx, dy);
          viewPos.x = Math.max(0, viewPos.x);
          viewPos.y = Math.max(0, viewPos.y);
          viewPos.x = Math.min(d.width - vd.width, viewPos.x);
          viewPos.y = Math.min(d.height - vd.height, viewPos.y);

          getViewport().setViewPosition(viewPos);
          lastDragPoint = current;
          e.consume();
        }
      }
    }
  }
}
