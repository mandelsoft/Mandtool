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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

/**
 *
 * @author Uwe Krueger
 */
public class ButtonPanel extends JPanel {
  private int lines=1;

  public ButtonPanel()
  {
    addComponentListener(new ComponentAdapter() {
      private boolean active;

      @Override
      public void componentResized(ComponentEvent e)
      {
        ButtonPanel p=(ButtonPanel)e.getComponent();
        if (!active) {
          active=true;
          p.updatePreferredSize();
          active=false;
        }
      }
    });
  }

  private void updatePreferredSize()
  {
    //System.out.println("update preferred size");
    int l=0;
    int y=-1;
    for (Component c:getComponents()) {
      if (y<c.getY()) {
        y=c.getY();
        l++;
      }
    }

    if (l!=getLines()) {
      //System.out.println("set lines: "+l);
      setLines(l);
    }
  }

  @Override
  public void validate()
  {
    updatePreferredSize();
    super.validate();
  }
  
  public void setLines(int lines)
  {
    if (lines!=this.lines) {
      this.lines=lines;
      revalidate();
      repaint();
    }
  }

  public int getLines()
  {
    return lines;
  }

  public Dimension getPreferredUISize()
  {
    return super.getPreferredSize();
  }

  @Override
  public Dimension getPreferredSize()
  {
    Dimension p=super.getPreferredSize();
    return new Dimension(p.width, p.height*lines);
  }
}
