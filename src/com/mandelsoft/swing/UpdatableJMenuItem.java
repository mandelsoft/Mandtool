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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 *
 * @author Uwe Krueger
 */
public class UpdatableJMenuItem extends JMenuItem implements UpdatableObject {

  public UpdatableJMenuItem(String text, int mnemonic)
  {
    super(text,mnemonic);
  }

  public UpdatableJMenuItem(String text, Icon icon)
  {
    super(text,icon);
  }

  public UpdatableJMenuItem(Action a)
  {
    super(a);
  }

  public UpdatableJMenuItem(String text)
  {
    super(text);
  }

  public UpdatableJMenuItem(Icon icon)
  {
    super(icon);
  }

  public UpdatableJMenuItem()
  {
  }

  public void updateObject(UpdateContext ctx)
  {
    Action a=getAction();
    if (a!=null && (a instanceof UpdatableObject)) {
      ((UpdatableObject)a).updateObject(ctx);
    }
  }
}
