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

import com.mandelsoft.mand.QualifiedMandelName;
import javax.swing.JOptionPane;

/**
 *
 * @author Uwe Krüger
 */

public class MandelListModelMenu extends AbstractMandelListModelMenu {
  // copied from MandelListMenu : found no better way
  
  public MandelListModelMenu(MandelWindowAccess access, MandelListModel model)
  {
    this("Load", access, model);
  }
  
  public MandelListModelMenu(String name, MandelWindowAccess access, MandelListModel model)
  {
    super(name, access, model);
  }

  @Override
  protected void selectArea(QualifiedMandelName sel)
  {
    access.getMandelImagePane().setBusy(true);
    if (access.getMandelImagePane().setImage(sel)) {
      handleLoaded(sel);
    }
    else {
      JOptionPane.showMessageDialog(access.getMandelWindow(),
                                    "Cannot load image: "+sel,
                                    "Mandel IO", JOptionPane.WARNING_MESSAGE);
    }
    access.getMandelImagePane().setBusy(false);
  }

  protected void handleLoaded(QualifiedMandelName name)
  {
  }
}
