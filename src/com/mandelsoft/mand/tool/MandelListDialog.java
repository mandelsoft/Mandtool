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
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.Utils;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;

/**
 *
 * @author Uwe Krueger
 */
public class MandelListDialog extends MandelDialog {
  private MandelListPanel panel;
  private String title;
  private MandelListListener listener;

  public MandelListDialog(MandelWindowAccess owner, MandelListTableModel model)
  {
    super(owner);
    setup(model);
  }

  public MandelListDialog(MandelWindowAccess owner, String title,
                          MandelListTableModel model)
  {
    super(owner,title);
    this.title=title;
    setup(model);
    panel.setTitle(title);  // don't generate title line in panel !!!
    model.addMandelListListener(listener=new MandelListListener() {
      public void listChanged(ChangeEvent evt)
      {
        updateTitle();
      }
    });
    updateTitle();
  }

  protected void updateTitle()
  {
    int c=panel.getModel().getRowCount();
    setTitle(title+" ("+Utils.sizeString(c,"entry")+")");
  }

  @Override
  protected void cleanup()
  {
    super.cleanup();
    if (listener!=null) {
      panel.getModel().removeMandelListListener(listener);
    }
  }

  protected void setup(MandelListTableModel model)
  {
    panel=new MandelListPanel(null,model,model.isModifiable());
    if (!getEnvironment().isReadonly()) {
      panel.addButton(new SaveAction());
    }
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

  /////////////////////////////////////////////////////////////////////////

  private class SaveAction extends AbstractAction {

    public SaveAction()
    {
      super("Save Images");
    }

    public void actionPerformed(ActionEvent e)
    {
      PictureSaveDialog d=new PictureSaveDialog(getMandelWindowAccess(),
                                                getTitle(),
                                                panel.getModel().getList());
      d.setVisible(true);
    }

  }
}
