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

import javax.swing.JDialog;

/**
 *
 * @author Uwe Krüger
 */

public class MandelBrowserDialog extends MandelDialog {

  public MandelBrowserDialog(MandelWindowAccess owner, MandelListFolderTreeModel model)
  {
    super(owner,"Mandel List Browser");
    MandelListFolderBrowserPanel panel=new MandelListFolderBrowserPanel(
                  "Lists", model, null);
    //panel.setRootVisible(false);
    add(panel);
    pack();
    this.setMinimumSize(this.getSize());
    this.setResizable(true);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }
}
