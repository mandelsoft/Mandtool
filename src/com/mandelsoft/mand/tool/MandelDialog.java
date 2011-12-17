
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

import com.mandelsoft.swing.IJDialog;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Uwe Krüger
 */

public class MandelDialog extends IJDialog implements MandelWindowAccessSource {
  private MandelWindowAccess owner;

  public MandelDialog(MandelWindowAccess owner, String title)
  { this(owner);
    setTitle(title);
  }

  public MandelDialog(MandelWindowAccess owner)
  { super(owner==null?null:owner.getMandelWindow());

    this.owner=owner;
    this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    setResizable(false);
  }

  public MandelWindowAccess getMandelWindowAccess()
  { return owner;
  }

  @Override
  public void setVisible(boolean b)
  {
    boolean old=isVisible();
    if (old!=b) {
      super.setVisible(b);
      this.firePropertyChange("visible", old, b);
    }
  }


  public ToolEnvironment getEnvironment()
  { return owner.getEnvironment();
  }

  public boolean overwriteFileDialog(File f)
  {
    Object[] options={"Replace", "Cancel"};
    int o=JOptionPane.showOptionDialog(getOwner(),
            f.getName()+" already exists.\n"+
            "Do you want to replace it?",
            "Warning",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            null, options, options[1]);
    return o==0;
  }

  public boolean deleteFileDialog(File f)
  {
    Object[] options={"Delete", "Cancel"};
    int o=JOptionPane.showOptionDialog(getOwner(),
            "Do you really want to delete "+f.getPath()+"?",
            "Warning",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            null, options, options[1]);
    return o==0;
  }

  public void mandelInfo(String msg)
  {
    JOptionPane.showMessageDialog(getOwner(),
                msg,
                "Mandel Area",
                JOptionPane.INFORMATION_MESSAGE);
  }

  public void mandelError(String msg)
  {
    JOptionPane.showMessageDialog(getOwner(),
                msg,
                "Mandel Area",
                JOptionPane.ERROR_MESSAGE);
  }

  public void mandelError(String msg, Exception ex)
  {
    JOptionPane.showMessageDialog(getOwner(),
                msg+": "+ex,
                "Mandel Area",
                JOptionPane.ERROR_MESSAGE);
  }
}
