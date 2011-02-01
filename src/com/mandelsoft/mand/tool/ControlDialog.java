
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
package com.mandelsoft.mand.tool;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;

/**
 *
 * @author Uwe Krüger
 */
public abstract class ControlDialog extends MandelDialog {

  private ControlPanel panel;

  public ControlDialog(MandelWindowAccess owner, String name)
  {
    super(owner,name);
    //System.out.println("create control dialog '"+name+"' for "+owner);
    panel=createControlPanel();
    setup();
    add(panel);
    pack();
  }

  protected ControlPanel createControlPanel()
  {
    return new ControlPanel();
  }

  protected void addTab(String name, JComponent c)
  {
    panel.addTab(name, c);
  }

  protected void addTab(String name, JComponent c, String tip)
  {
    panel.addTab(name, c, tip);
  }

  protected abstract void setup();

  ////////////////////////////////////////////////////////////////////////
  // Control Panel
  ////////////////////////////////////////////////////////////////////////
  protected class ControlPanel extends GBCPanel {

    protected JTabbedPane tp;

    ControlPanel()
    {
      tp=new JTabbedPane();
      setup();
      add(tp, GBC(0, 0, GBC.BOTH));
      addBorder(0, 0, 1, 1, true);

//      super(new GridLayout(1,1));
//      tp=new JTabbedPane();
//      setup();
//      add(tp);
//      Border border=new BevelBorder(BevelBorder.RAISED);
//      setBorder(border);
    }

    protected void addTab(String name, JComponent c)
    {
      tp.addTab(name, c);
    }

    protected void addTab(String name, JComponent c, String tip)
    {
      tp.addTab(name, null, c, tip);
    }

    protected void setup()
    {
    }
  }
}
