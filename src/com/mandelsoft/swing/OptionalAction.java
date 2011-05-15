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

import com.mandelsoft.util.upd.UpdateContext;
import javax.swing.Icon;

/**
 *
 * @author Uwe Krueger
 */
public abstract class OptionalAction extends UpdatableAction {

  public OptionalAction()
  {
    super();
  }

  public OptionalAction(String name)
  {
    super(name);
  }

  public OptionalAction(String name, Icon icon)
  {
    super(name,icon);
  }

  protected abstract boolean isPossible();

  protected boolean isPossible(UpdateContext ctx)
  {
    return isPossible();
  }

  @Override
  public void updateObject(UpdateContext ctx)
  {
    setEnabled(isPossible(ctx));
  }
}
