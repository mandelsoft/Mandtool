
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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

/**
 *
 * @author Uwe Krüger
 */
public abstract class ContextAction<E> extends AbstractAction {

  public ContextAction(String name)
  {
    super(name);
  }

  public String getName()
  {
    return (String)getValue(NAME);
  }

  protected abstract E getSelectedItem(ActionEvent e);

  ////////////////////////////////////////////////////////////////////
  
  public static <T> T getEnvironmentObject(ActionEvent e, Class<T> type)
  {
    return getEnvironmentObject((Component)e.getSource(),type);
  }

  public static <T> T getEnvironmentObject(Component c, Class<T> type)
  {
    Component s=c;

    while (s!=null&&!(type.isAssignableFrom(s.getClass()))) {
      if (s instanceof JPopupMenu) {
        s=((JPopupMenu)s).getInvoker();
      }
      else {
        s=s.getParent();
      }
    }
//    System.out.println("found object of type "+type+" for "+
//                 (c==null?null:c.getClass())+": "+s);
    return (T)s;
  }
}