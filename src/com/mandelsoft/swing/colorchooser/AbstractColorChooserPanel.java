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
package com.mandelsoft.swing.colorchooser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JLabel;

/**
 *
 * @author uwekr
 */
public abstract class AbstractColorChooserPanel extends javax.swing.colorchooser.AbstractColorChooserPanel {

  protected JLabel paletteLabel;
  protected Point paletteSelection=new Point();
  
  abstract protected void wheel(int notches);
  abstract protected void getForLocation(int x, int y, float[] rgb);
  abstract protected void update(float red, float green, float blue);

  protected class MouseListener extends MouseAdapter {

    long last;
    int delta = 1;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      long t = System.currentTimeMillis();

      if (t - last < 50) {
        delta += 3;
      } else {
        delta = 1;
      }
      last = t;
      int notches = e.getWheelRotation();
      System.out.printf("wheel %d\n", notches * delta);
      wheel(notches * delta);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      float[] v = new float[3];
      getForLocation(e.getX(), e.getY(), v);
      update(v[0], v[1], v[2]);
    }

    public void mouseDragged(MouseEvent e) {
      int labelWidth = paletteLabel.getWidth();

      int labelHeight = paletteLabel.getHeight();
      int x = e.getX();
      int y = e.getY();

      if (x >= labelWidth) {
        x = labelWidth - 1;
      }

      if (y >= labelHeight) {
        y = labelHeight - 1;
      }

      if (x < 0) {
        x = 0;
      }

      if (y < 0) {
        y = 0;
      }

      float[] v = new float[3];
      System.out.println("" + x + "," + y);
      getForLocation(x, y, v);
      update(v[0], v[1], v[2]);
    }
  }

  ///////////////////////////////////////////////////////////

  protected JLabel createPaletteLabel() {
    return new JLabel() {

      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.drawOval(paletteSelection.x - 4, paletteSelection.y - 4, 8, 8);
      }
    };
  }
}
