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
import java.awt.Dialog;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Uwe Krüger
 */

public class IJPanel extends JPanel {

  public IJPanel()
  {
    this.addPropertyChangeListener("ancestor",new PanelPropertyListener());
  }

  protected void Error(String title, String msg)
  {
    JOptionPane.showMessageDialog(getOwner(), msg, title,
                                       JOptionPane.WARNING_MESSAGE);
  }

  protected void Info(String title, String msg)
  {
    JOptionPane.showMessageDialog(getOwner(), msg, title,
                                       JOptionPane.INFORMATION_MESSAGE);
  }

  protected Window getOwner()
  {
    Window w=getWindow();
    if (w==null || !(w instanceof Dialog)) {
      return w;
    }
    return ((Dialog)w).getOwner();
  }

  protected Window getWindow()
  {
    Component c=this;
    while (c!=null && !(c instanceof Window)) {
      c=c.getParent();
    }
    return (Window)c;
  }

  protected void panelBound()
  {
  }

  protected void panelUnbound()
  {
  }

  private class PanelPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getNewValue()==null) {
        panelUnbound();
      }
      else {
        panelBound();
      }
    }
  }
}
