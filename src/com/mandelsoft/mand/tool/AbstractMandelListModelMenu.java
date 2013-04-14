/*
 *  Copyright 2013 Uwe Krueger.
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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.util.ChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Krüger
 */

public abstract class AbstractMandelListModelMenu extends AbstractMandelListMenu {
  private MandelListModel mlmodel;
  private MandelListListener listener;

  public AbstractMandelListModelMenu(String name,
                             MandelWindowAccess access, MandelListModel model)
  {
    super(name, access);
    setup(model);
  }

  private final void setup(MandelListModel model)
  {
    listener = new Listener();
    setMandelListModel(model);
  }

  public void setMandelListModel(MandelListModel model)
  {
    if (this.mlmodel!=null) this.mlmodel.removeMandelListListener(listener);
    this.mlmodel=model;
    if (model!=null) {
      model.addMandelListListener(listener);
      update();
    }
    else clear();
  }

  private void update()
  {
    List<Entry> entries=new ArrayList<Entry>();

    for (QualifiedMandelName n:mlmodel.getList()) {
      Entry e=lookup(n,entries);
      if (e==null) e=createEntry(n);
      entries.add(e);
    }

    clear();
    for (Entry e:entries) {
      add(e);
    }
  }

  private class Listener implements MandelListListener {

    public void listChanged(ChangeEvent evt)
    {
      update();
    }
  }
}
