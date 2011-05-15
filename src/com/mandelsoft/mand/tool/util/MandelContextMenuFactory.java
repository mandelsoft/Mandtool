
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

import com.mandelsoft.util.upd.UpdateSource;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.MandelListSelector;
import com.mandelsoft.mand.tool.MandelNameSelector;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.swing.UpdatableJMenu;
import com.mandelsoft.swing.UpdatableJPopupMenu;
import com.mandelsoft.util.upd.UpdatableObject;
import com.mandelsoft.util.upd.UpdateContext;
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

  public String getName()
  {
    return name;
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
    JPopupMenu menu=new PopupMenuBase(comp);
    _addItems(menu, comp, generic);
    return menu;
  }

  public JMenu createMenu(Component comp, boolean generic)
  {
    JMenu menu=new MenuBase(comp);
    _addItems(menu, comp, generic);
    return menu;
  }

  public JPopupMenu createPopupMenu(Component comp, QualifiedMandelName name)
  {
    JPopupMenu menu=new MandelPopupMenu(comp,name);
    _addItems(menu, comp, false);
    return menu;
  }

  public JMenu createMenu(Component comp, QualifiedMandelName name)
  {
    JMenu menu=new MandelMenu(comp,name);
    _addItems(menu, comp, false);
    return menu;
  }

  public JPopupMenu createPopupMenu(Component comp, MandelNameSelector sel)
  {
    JPopupMenu menu=new MandelSelPopupMenu(comp,sel);
    _addItems(menu, comp, true);
    return menu;
  }

  public JMenu createMenu(Component comp, MandelNameSelector sel)
  {
    JMenu menu=new MandelSelMenu(comp,sel);
    _addItems(menu, comp, true);
    return menu;
  }


  public JPopupMenu createPopupMenu(Component comp, MandelListSelector sel)
  {
    JPopupMenu menu=new MandelListSelPopupMenu(comp,sel);
    _addItems(menu, comp, true);
    return menu;
  }

  public JMenu createMenu(Component comp, MandelListSelector sel)
  {
    JMenu menu=new MandelListSelMenu(comp,sel);
    _addItems(menu, comp, true);
    return menu;
  }

  protected void _addItems(JComponent menu, // common parent for JMenu and JPopupMenu
                           Component comp, boolean generic)
  {
    addItems(menu,comp,generic);
    if (menu instanceof UpdatableObject) {
      ((UpdatableObject)menu).updateObject(null/*unused*/);
    }
  }

  protected abstract void addItems(JComponent menu, Component comp,
                                   boolean generic);


  ////////////////////////////////////////////////////////////////////////////
  // item menu
  ////////////////////////////////////////////////////////////////////////////

  protected class PopupMenuBase extends UpdatableJPopupMenu {
    private Component comp;

    public PopupMenuBase(Component comp)
    {
      super(MandelContextMenuFactory.this.getName());
      this.comp=comp;
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      MandelContextMenuFactory.this.updateMenu(this, comp);
    }
  }

  protected class MenuBase extends UpdatableJMenu {
    private Component comp;

    public MenuBase(Component comp)
    {
      super(MandelContextMenuFactory.this.getName());
      this.comp=comp;
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      MandelContextMenuFactory.this.updateMenu(this, comp);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // direct name flavors

  private class MandelPopupMenu extends PopupMenuBase
                                implements MandelNameSelector {
    private QualifiedMandelName selected;

    public MandelPopupMenu(Component comp, QualifiedMandelName name)
    {
      super(comp);
      this.selected=name;
    }

    public QualifiedMandelName getSelectedMandelName()
    {
      return selected;
    }
  }

  private class MandelMenu extends MenuBase
                           implements MandelNameSelector {
    private QualifiedMandelName selected;

    public MandelMenu(Component comp, QualifiedMandelName name)
    {
      super(comp);
      selected=name;
    }

    public QualifiedMandelName getSelectedMandelName()
    {
      return selected;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // name selector flavors

  private class MandelSelPopupMenu extends PopupMenuBase
                                   implements MandelNameSelector {
    private MandelNameSelector selector;

    public MandelSelPopupMenu(Component comp, MandelNameSelector sel)
    {
      super(comp);
      selector=sel;
    }

    public QualifiedMandelName getSelectedMandelName()
    {
      return selector.getSelectedMandelName();
    }
  }

  private class MandelSelMenu extends MenuBase
                              implements MandelNameSelector {
    private MandelNameSelector selector;

    public MandelSelMenu(Component comp, MandelNameSelector sel)
    {
      super(comp);
      selector=sel;
    }

    public QualifiedMandelName getSelectedMandelName()
    {
      return selector.getSelectedMandelName();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  private class MandelListSelPopupMenu extends PopupMenuBase
                                       implements MandelListSelector {
    private MandelListSelector selector;

    public MandelListSelPopupMenu(Component comp, MandelListSelector sel)
    {
      super(comp);
      selector=sel;
    }

    public MandelList getSelectedMandelList()
    {
      return selector.getSelectedMandelList();
    }
  }

  private class MandelListSelMenu extends MenuBase
                                       implements MandelListSelector {
    private MandelListSelector selector;

    public MandelListSelMenu(Component comp, MandelListSelector sel)
    {
      super(comp);
      selector=sel;
    }

    public MandelList getSelectedMandelList()
    {
      return selector.getSelectedMandelList();
    }
  }
}
