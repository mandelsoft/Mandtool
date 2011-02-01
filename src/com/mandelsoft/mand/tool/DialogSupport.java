
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

import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Uwe Krüger
 */
public class DialogSupport extends JDialog {

  public DialogSupport()
  {
  }

  public DialogSupport(JFrame owner)
  { super(owner);
  }

  public DialogSupport(JFrame owner, String title, boolean modal)
  { super(owner,title,modal);
  }

  public boolean overwriteFileDialog(File f)
  {
    Object[] options={"Replace", "Cancel"};
    Object o=JOptionPane.showOptionDialog(getOwner(),
            f.getName()+" already exists.\n"+
            "Do you want to replace it?",
            "Warning",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            null, options, options[1]);
    return (o.equals(0));
  }
}
