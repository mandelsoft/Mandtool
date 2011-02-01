
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

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import java.awt.Font;

/**
 *
 * @author Uwe Krüger
 */

public class Help extends JDialog {
  private JButton     closeButton;
  private JEditorPane text;

  public Help(Window parent)
  {
    super(parent);
    initComponents();
    getRootPane().setDefaultButton(closeButton);
  }

  public void closeNews()
  {
    dispose();
  }


  private void initComponents()
  {
    closeButton = new javax.swing.JButton("Close");
    text=new JEditorPane();
    URL u=getClass().getResource("resc/help");
    System.out.println("url="+u);
    try {
      Font f=Font.decode("Courier New-12");
      if (f!=null) text.setFont(f);
      else System.out.println("font not found -> use default");
      text.setPage(u);
    }
    catch (IOException ex) {
      System.out.println("cannot find help file");
    }
    text.setEditable(false);

    JScrollPane scroll=new JScrollPane(text);
    scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setPreferredSize(new Dimension(580,400));

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    setTitle("Mandtool Help");
    setModal(false);
    setName("help");
    setResizable(false);

    closeButton.setAction(new AbstractAction("close") {
      public void actionPerformed(ActionEvent e)
      {
        closeNews();
      }
    });

    JPanel panel=new GBCPanel();
    add(panel);
    panel.add(scroll, new GBC(0,0).setFill(GBC.BOTH));
    panel.add(closeButton,new GBC(0,1).setAnchor(GBC.EAST));
    this.pack();
  }
}
