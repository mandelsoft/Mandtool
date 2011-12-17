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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import com.mandelsoft.swing.GBC;

/**
 *
 * @author Uwe Krüger
 */

public class News extends JDialog {
  private JButton     closeButton;
  private JEditorPane text;

  public News(JDialog parent)
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
    URL u=getClass().getResource("resc/history");
    System.out.println("url="+u);
    try {
      text.setPage(u);
    }
    catch (IOException ex) {
      System.out.println("cannot find history");
    }
    
    text.setEditable(false);

    JScrollPane scroll=new JScrollPane(text);
    scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setPreferredSize(new Dimension(500,500));
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    setTitle("News");
    setModal(true);
    setName("news");
    setResizable(false);

    closeButton.setAction(new AbstractAction("close") {
      public void actionPerformed(ActionEvent e)
      {
        closeNews();
      }
    });

    setLayout(new GridBagLayout());
    add(scroll, new GBC(0,0));
    add(closeButton,new GBC(0,1).setAnchor(GBC.EAST));
    this.pack();
  }
}
