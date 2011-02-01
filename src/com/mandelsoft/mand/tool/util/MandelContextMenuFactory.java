
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
package com.mandelsoft.mand.tool.util;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.MandelListSelector;
import com.mandelsoft.mand.tool.MandelNameSelector;
import com.mandelsoft.mand.util.MandelList;
import javax.swing.JMenuItem;

/**
 *
 * @author Uwe Krüger
 */

public abstract class MandelContextMenuFactory {
  private String name;

  public MandelContextMenuFactory(String name)
  {
    this.name=name;
  }

  public void updateMenu(JComponent menu, Component comp)
  {
    Component[] comps;
    if (menu instanceof JMenu) comps=((JMenu)menu).getMenuComponents();
    else comps=menu.getComponents();
    for (Component c:comps) {
      if (c instanceof JMenuItem) updateItem((JMenuItem)c,comp);
    }
  }

  protected void updateItem(JMenuItem item, Component comp)
  {

  }

  public JPopupMenu createPopupMenu(Component comp, boolean generic)
  {
    JPopupMenu menu=new JPopupMenu(this.name);
    addItems(menu, comp, generic);
    return menu;
  }

  public JMenu createMenu(Component comp, boolean generic)
  {
    JMenu menu=new JMenu(this.name);
    addItems(menu, comp, generic);
    return menu;
  }

  public JPopupMenu createPopupMenu(Component comp, QualifiedMandelName name)
  {
    JPopupMenu menu=new MandelPopupMenu(this.name,name);
    addItems(menu, comp, false);
    return menu;
  }

  public JMenu createMenu(Component comp, QualifiedMandelName name)
  {
    JMenu menu=new MandelMenu(this.name,name);
    addItems(menu, comp, false);
    return menu;
  }

  public JPopupMenu createPopupMenu(Component comp, MandelNameSelector sel)
  {
    JPopupMenu menu=new MandelSelPopupMenu(this.name,sel);
    addItems(menu, comp, true);
    return menu;
  }

  public JMenu createMenu(Component comp, MandelNameSelector sel)
  {
    JMenu menu=new MandelSelMenu(this.name,sel);
    addItems(menu, comp, true);
    return menu;
  }


  public JPopupMenu createPopupMenu(Component comp, MandelListSelector sel)
  {
    JPopupMenu menu=new MandelListSelPopupMenu(this.name,sel);
    addItems(menu, comp, true);
    return menu;
  }

  public JMenu createMenu(Component comp, MandelListSelector sel)
  {
    JMenu menu=new MandelListSelMenu(this.name,sel);
    addItems(menu, comp, true);
    return menu;
  }



  protected abstract void addItems(JComponent menu, Component comp,
                                   boolean generic);


  ////////////////////////////////////////////////////////////////////////////
  // item menu
  ////////////////////////////////////////////////////////////////////////////

  private static class MandelPopupMenu extends JPopupMenu
                                       implements MandelNameSelector {
    private QualifiedMandelName selected;

    public MandelPopupMenu(String label, QualifiedMandelName name)
    {
      super(label);
      selected=name;
    }


    public QualifiedMandelName getSelectedMandelName()
    {
      return selected;
    }
  }

  private static class MandelMenu extends JMenu
                                  implements MandelNameSelector {
    private QualifiedMandelName selected;

    public MandelMenu(String label, QualifiedMandelName name)
    {
      super(label);
      selected=name;
    }

    public QualifiedMandelName getSelectedMandelName()
    {
      return selected;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  private static class MandelSelPopupMenu extends JPopupMenu
                                       implements MandelNameSelector {
    private MandelNameSelector selector;

    public MandelSelPopupMenu(String label, MandelNameSelector sel)
    {
      super(label);
      selector=sel;
    }


    public QualifiedMandelName getSelectedMandelName()
    {
      return selector.getSelectedMandelName();
    }
  }

  private static class MandelSelMenu extends JMenu
                                       implements MandelNameSelector {
    private MandelNameSelector selector;

    public MandelSelMenu(String label, MandelNameSelector sel)
    {
      super(label);
      selector=sel;
    }


    public QualifiedMandelName getSelectedMandelName()
    {
      return selector.getSelectedMandelName();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  private static class MandelListSelPopupMenu extends JPopupMenu
                                       implements MandelListSelector {
    private MandelListSelector selector;

    public MandelListSelPopupMenu(String label, MandelListSelector sel)
    {
      super(label);
      selector=sel;
    }


    public MandelList getSelectedMandelList()
    {
      return selector.getSelectedMandelList();
    }
  }

  private static class MandelListSelMenu extends JMenu
                                       implements MandelListSelector {
    private MandelListSelector selector;

    public MandelListSelMenu(String label, MandelListSelector sel)
    {
      super(label);
      selector=sel;
    }

    public MandelList getSelectedMandelList()
    {
      return selector.getSelectedMandelList();
    }
  }
}
