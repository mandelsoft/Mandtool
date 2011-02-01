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

package com.mandelsoft.mand.tool.ctx;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import com.mandelsoft.mand.tool.MandelListFolderTreeModel;
import com.mandelsoft.mand.util.MandelListFolder;

/**
 *
 * @author Uwe Krueger
 */
public class DeleteFolderAction
       extends ContextAction<MandelListFolder, TreePath, MandelListFolderTreeModel> {

  public DeleteFolderAction(
          ContextProvider<MandelListFolder, TreePath, MandelListFolderTreeModel> p)
  {
    super("Delete Folder", p);
  }

  public void actionPerformed(ActionEvent e)
  {
    MandelListFolder sel=getSelectedItem();
    if (sel!=null) {
      if (sel.getParent()==null) {
        JOptionPane.showMessageDialog(getWindow(),
                                      "Cannot delete root folder", //text to display
                                      "Mandel List Folder", //title
                                      JOptionPane.ERROR_MESSAGE);
      }
      else {
        if (deleteFolderDialog(sel.getName()))
          getModel().removeFolder(sel);
      }
    }
  }

  public boolean deleteFolderDialog(String n)
  {
    Object[] options={"Delete", "Cancel"};
    Object o=JOptionPane.showOptionDialog(getWindow(),
                                          "Do you really want to delete "+n+"?",
                                          "Warning",
                                          JOptionPane.DEFAULT_OPTION,
                                          JOptionPane.WARNING_MESSAGE,
                                          null, options, options[1]);
    return (o.equals(0));
  }
}
