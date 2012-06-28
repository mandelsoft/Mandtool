
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

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.swing.GBCPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Uwe Krüger
 */
public class GotoDialog extends MandelDialog {

  private GBCPanel panel;

  public GotoDialog(MandelWindowAccess owner)
  {
    super(owner,"Goto Mandel Area");
    panel=new GotoPanel();
    add(panel);
    pack();
  }

  ////////////////////////////////////////////////////////////////////////
  // Control Panel
  ////////////////////////////////////////////////////////////////////////
  protected class GotoPanel extends GBCPanel {
    private JTextField text;

    GotoPanel()
    {
      JLabel label=new JLabel("Qualified area name");
      add(label, GBC(0, 0).setInsets(10));
      text=new JTextField(40);
      label.setLabelFor(text);
      add(text,GBC(1,0).setInsets(10));
      JPanel buttons=new JPanel();
      add(buttons,GBC(0,1,2,1).setInsets(10));

      buttons.add(new GotoButton());
      buttons.add(new MetaButton());
    }

    ////////////////////////////////////////////////////////////////////////
    // Common Area Button Base
    ////////////////////////////////////////////////////////////////////////
    private abstract class AreaButton extends JButton {
      public AreaButton(String label)
      {
        super(label);
        addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            String txt=text.getText();
            if (txt==null||txt.length()==0) {
              mandelError("no name specified");
            }
            else {
              QualifiedMandelName name=QualifiedMandelName.create(txt);
              if (name==null) {
                if (handleFile(txt))
                  GotoDialog.this.setVisible(false);
              }
              else {
                if (handleArea(name))
                  GotoDialog.this.setVisible(false);
              }
            }
          }
        });
      }

      protected abstract boolean handleArea(QualifiedMandelName name);
      protected boolean handleFile(String txt)
      {
        mandelError("'"+txt+"' is no valid area name");
        return false;
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // Goto Area Button
    ////////////////////////////////////////////////////////////////////////
    private class GotoButton extends AreaButton {

      public GotoButton()
      {
        super("Goto");
      }

      @Override
      protected boolean handleArea(QualifiedMandelName name)
      {
        getMandelWindowAccess().getMandelImagePane().setImage(name);
        return true;
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // Show Area Meta Data Button
    ////////////////////////////////////////////////////////////////////////
    private class MetaButton extends AreaButton {

      public MetaButton()
      {
        super("Meta Data");
      }

      protected void showMeta(MandelData md, QualifiedMandelName name)
      {
        MandelImageAreaDialog v=new MandelImageAreaDialog(
          getMandelWindowAccess(), "Mandel Image Meta Information",
                                   name, md);
        v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        v.setVisible(true);
      }

      @Override
      protected boolean handleArea(QualifiedMandelName name)
      {
        MandelHandle found=getEnvironment().getMetaScanner().getMandelData(
                                                                         name);
        if (found==null) {
          mandelError("no meta data found for "+name);
        }
        else {
          try {
            MandelData data=found.getInfo();
            showMeta(data,name);
            return true;
          }
          catch (IOException ex) {
            mandelError("cannot read meta data for "+name);
          }
        }
        return false;
      }

      @Override
      protected boolean handleFile(String txt)
      {
        File f=new File(txt);
        if (f.isFile()) {
          try {
            MandelFileName mfn=MandelFileName.create(f);
            if (mfn==null) {
              mandelError(txt+" is no mandel file");
            }
            else {
              MandelData md=new MandelData(f);
              showMeta(md,mfn.getQualifiedName());
              return true;
            }
          }
          catch (IOException ex) {
            mandelError("cannot read "+txt+": "+ex);
          }
        }
        else {
          mandelError(txt+" is no qualified mandel name or file");
        }
        return false;
      }


    }
  }
}
