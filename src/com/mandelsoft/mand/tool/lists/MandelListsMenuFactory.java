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

package com.mandelsoft.mand.tool.lists;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import com.mandelsoft.mand.tool.util.MandelContextMenuFactory;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListsMenuFactory extends MandelContextMenuFactory {
  private List<MandelListViewAction> lists=new ArrayList<MandelListViewAction>();
  private boolean readonly;

  public MandelListsMenuFactory(boolean readonly)
  {
    super("Derived Lists");
    setup();
  }

  protected void setup()
  {
    add(new FavoritesListAction());
    add(new TodoListAction());
    add(new SubAreaListAction());
    add(new MandelAreaListAction());
    add(new LeafImagesListAction());
    add(new DeadEndListAction());
    add(new PendingWorkListAction());
    add(new MandelLeafListAction());
    if (!readonly)
      add(new UnseenListAction());
    add(new RequestListAction());

    add(new DirectAreaMarkerListAction());
    add(new AreaMarkerListAction());
    
    add(new DirectForkListAction());
    add(new ForkListAction());
    add(new AreaColormapListAction());
  }

  protected void add(MandelListViewAction action)
  {
    lists.add(action);
  }

  @Override
  protected void addItems(JComponent menu, Component comp, boolean generic)
  {
    for (MandelListViewAction a:lists) {
      JMenuItem it=new JMenuItem(a);
      menu.add(it);
    }
  }

}
