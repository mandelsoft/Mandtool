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

import com.mandelsoft.mand.MandelName;
import javax.swing.JDialog;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListDialog extends MandelDialog {
  MandelListPanel panel;

  public MandelListDialog(MandelWindowAccess owner, MandelListTableModel model)
  {
    super(owner);
    setup(model);
  }

  public MandelListDialog(MandelWindowAccess owner, String title,
                          MandelListTableModel model)
  {
    super(owner,title);
    setup(model);
    panel.setTitle(title);  // don't generate title line in panel !!!
  }

  protected void setup(MandelListTableModel model)
  {
    panel=new MandelListPanel(null,model,model.isModifiable());
    add(panel);
    pack();
    setVisible(true);
    setResizable(true);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }

  public void setRootName(MandelName rootName)
  {
    panel.setRootName(rootName);
  }

  public MandelName getRootName()
  {
    return panel.getRootName();
  }
}
