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
import com.mandelsoft.util.upd.UpdateContext;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Uwe Krueger
 */
public class UpdatableJMenu extends JMenu implements UpdatableObject {

  public UpdatableJMenu(String s, boolean b)
  {
    super(s,b);
  }

  public UpdatableJMenu(Action a)
  {
    super(a);
  }

  public UpdatableJMenu(String s)
  {
    super(s);
  }

  public UpdatableJMenu()
  {
  }

  @Override
  protected JMenuItem createActionComponent(Action a)
  {
    JMenuItem mi=new UpdatableJMenuItem() {

      @Override
      protected PropertyChangeListener createActionPropertyChangeListener(
        Action a)
      {
        PropertyChangeListener pcl=createActionChangeListener(this);
        if (pcl==null) {
          pcl=super.createActionPropertyChangeListener(a);
        }
        return pcl;
      }
    };
    mi.setHorizontalTextPosition(JButton.TRAILING);
    mi.setVerticalTextPosition(JButton.CENTER);
    return mi;
  }

  public void updateObject(UpdateContext ctx)
  {
    Component[] comps=this.getMenuComponents();
    if (comps!=null) for (Component c:comps) {
      if (c instanceof UpdatableObject) {
        ((UpdatableObject)c).updateObject(ctx);
      }
    }
  }

}
