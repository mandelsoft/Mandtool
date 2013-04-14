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

import com.mandelsoft.mand.Settings;
import com.mandelsoft.swing.GBC;
import javax.swing.event.ChangeEvent;
import com.mandelsoft.swing.GBCPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Uwe Krueger
 */
public class ToolControlFrame extends JFrame {

  private ToolEnvironment env;
  private ToolControlPanel panel;

  public ToolControlFrame(ToolEnvironment env)
  {
    this.env=env;
    setTitle("Tool Control Panel");
    panel=new ToolControlPanel();
    add(panel);
    pack();
    setResizable(false);
  }

  public JFrame getFrame()
  { return this;
  }

  public ToolEnvironment getEnvironment()
  { return env;
  }

  @Override
  public void setVisible(boolean b)
  {
    super.setVisible(b);
    panel.update();
  }

  ///////////////////////////////////////////////////////////////////////////
  // Panel
  ///////////////////////////////////////////////////////////////////////////

  private class ToolControlPanel extends GBCPanel {
    private JCheckBox autoRescan;
    private JCheckBox shutdownMode;
    
    ToolControlPanel()
    { JButton b;
      JTextField tf;
      String s;
      int row=0;

      s=env.getProperty(Settings.SITE);
      if (s==null) s="";
      addTextField(row++,"Site name",s);

      s=env.getProperty(Settings.USER);
      if (s==null) s="";
      addTextField(row++,"Site owner", s);
   
      autoRescan=new JCheckBox((Icon)null, env.isAutoRescan());
      autoRescan.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e)
        {
          if (env.isAutoRescan()!=autoRescan.isSelected()) {
            System.out.println("auto rescan is "+autoRescan.isSelected());
            env.setAutoRescan(autoRescan.isSelected());
          }
        }
      });
      addField(row++, "Auto rescan", autoRescan);

      if (!env.isReadonly()) {
        shutdownMode=new JCheckBox((Icon)null, env.isShutdown());
        shutdownMode.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e)
          {
            System.out.println("shutdown mode is "+shutdownMode.isSelected());
            env.setShutdown(shutdownMode.isSelected());
            shutdownMode.setSelected(env.isShutdown());
          }
        });
        addField(row++, "Shutdown Mode", shutdownMode);
      }

      JPanel buttons=new JPanel();
      add(buttons,GBC(0,row).setSpanW(2));

      b=new JButton("Rescan");
      b.setToolTipText("Rescan the file system");
      b.addActionListener(new RescanAction());
      buttons.add(b);
    }

    private void update()
    {
      if (shutdownMode!=null) shutdownMode.setSelected(env.isShutdown());
    }
    
    private JTextField addTextField(int row, String name, String value)
    {
      JTextField tf=new JTextField();
      tf.setEditable(false);
      tf.setColumns(30);
      tf.setHorizontalAlignment(JTextField.TRAILING);
      tf.setText(value);
      addField(row,name,tf);
      return tf;
    }

    private void addField(int row, String name, JComponent c)
    {
      JLabel label=new JLabel(name);
      label.setLabelFor(c);
      label.setHorizontalAlignment(JLabel.LEFT);
      add(label, GBC(0, row).setAnchor(GBC.WEST));
      add(c,GBC(1,row).setLeftInset(10));
    }
    
    /////////////////////////////////////////////////////////////////////////
    // commands
    /////////////////////////////////////////////////////////////////////////
    
    

    private class RescanAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        env.rescan();
      }
    }
  }
}
