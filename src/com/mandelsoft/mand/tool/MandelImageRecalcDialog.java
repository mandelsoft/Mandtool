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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.plaf.basic.BasicBorders;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class MandelImageRecalcDialog extends MandelImageAreaDialog {

  public MandelImageRecalcDialog(MandelWindowAccess owner, String title,
                                 QualifiedMandelName name,
                                 MandelData data)
  { super(owner, title,
          owner.getEnvironment().mapToInfoFile(data.getFile()).getPath(),
          name,new MandelInfo().copyFrom(data.getInfo()),true);
  }
  
  @Override
  protected MandelAreaView createView(QualifiedMandelName name, Object info,
                                      boolean change, boolean readonly)
  {
    return new RecalcView(name,(MandelInfo)info, readonly);
  }


  ///////////////////////////////////////////////////////////////////////
  // view
  ///////////////////////////////////////////////////////////////////////

  protected class RecalcView extends View {
   
    public RecalcView(QualifiedMandelName name, MandelInfo info, boolean readonly)
    {
      super(name, info, readonly);
      filename.setEditable(true);
      filename.setBorder(BasicBorders.getTextFieldBorder());
      limitfield.setEditable(true);
      limitfield.setBorder(BasicBorders.getTextFieldBorder());
    }

    @Override
    protected void setupButtons()
    {
      createButton("Save",null,new SaveAction());
      createButton("Delete",null,new DeleteAction());
      super.setupButtons();
    }

    private class SaveAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getText())) return;
        MandelInfo info=getInfo();
        MandelData md=new MandelData(info);
        MandelFileName name;

        File f=new File(filename.getText());
        name=MandelFileName.create(f);
        if (name==null) {
          mandelError(f.getName()+" is no valid mandel area name.");
          return;
        }
        if (f.exists()) {
          if (!overwriteFileDialog(f)) return;
        }
        
        try {
          md.write(f);
          if (name.getQualifiedName().equals(qname)) {
            System.out.println("add to refinement list");
            getEnvironment().getUnseenRefinements().addRefinement(qname);
          }
          System.out.println("close refinement");
          getDialog().setVisible(false);
        }
        catch (IOException ex) {
          mandelError("Cannot write mandel info file",ex);
        }
      }
    }

    private class DeleteAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getText())) return;
        File f=new File(filename.getText());
        MandelName name=MandelName.create(f);
        if (name==null) {
          mandelError(f.getName()+" is no valid mandel area name.");
          return;
        }
        if (!f.exists()) {
          mandelError("File does not exist.");
          return;
        }
        if (!deleteFileDialog(f)) return;

        f.delete();
        if (f.exists()) {
          mandelError("Cannot delete "+f+".");
          return;
        }
      }
    }
  }

}
