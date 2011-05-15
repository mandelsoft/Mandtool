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

import com.mandelsoft.util.upd.UpdatableObject;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;


/**
 *
 * @author Uwe Krüger
 */

public class MenuButton extends JButton {
  private JPopupMenu menu;

  public MenuButton(String text, Icon icon)
  {
    super(text,icon);
    setup(null);
  }

  public MenuButton(String text)
  {
    super(text);
    setup(null);
  }

  public MenuButton(Icon icon)
  {
    super(icon);
    setup(null);
  }

  public MenuButton(String text, Icon icon, JPopupMenu menu)
  {
    super(text,icon);
    setup(menu);
  }

  public MenuButton(String text, JPopupMenu menu)
  {
    super(text);
    setup(menu);
  }

  public MenuButton(Icon icon, JPopupMenu menu)
  {
    super(icon);
    setup(menu);
  }

  public MenuButton(JPopupMenu menu)
  {
    super(menu.getLabel());
    setup(menu);
  }

  private void setup(JPopupMenu menu)
  {
    this.menu=menu;
    PopupListener l=new PopupListener();
    addActionListener(l);
    addMouseListener(l);
  }

  protected JPopupMenu getPopupMenu(Component c)
  {
    if (menu instanceof UpdatableObject) {
      ((UpdatableObject)menu).updateObject(null);
    }
    return menu;
  }

  private class PopupListener extends MouseAdapter
                      implements ActionListener {
    private int x,y;
    private Component c;

    @Override
    public void actionPerformed(ActionEvent e)
    {
      getPopupMenu(c).show(c, x, y);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      c=e.getComponent();
      x=e.getX();
      y=e.getY();
    }
  }
}
