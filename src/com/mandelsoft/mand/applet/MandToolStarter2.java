
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
package com.mandelsoft.mand.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import com.mandelsoft.mand.tools.MandTool;

/**
 *
 * @author Uwe Krüger
 */
public class MandToolStarter2 extends JApplet {

  @Override
  public String[][] getParameterInfo()
  {
    String[][] info = {
      // Parameter Name     Kind of Value   Description
        {"datasource",     "URL",          "a directory containing the config"}
    };
    return info;
  }

  @Override
  public void init()
  {
    
      //Execute a job on the event-dispatching thread:
      //creating this applet's GUI.
      //    this.getImage(getCodeBase(), "rightarrow.gif");
   
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run()
        {
          createGUI();
        }
      });
    }
    catch (Exception e) {
      System.err.println("createGUI didn't successfully complete");
    }
  }

  private void createGUI()
  {
    JButton button=new JButton();
    button.setAction(new AbstractAction("Mandel Browser") {
      public void actionPerformed(ActionEvent e)
      {
        try {
          MandToolStarter2.this.showStatus("starting browsing tool...");
          
//          InputStream inp=new URL(getDocumentBase(), "favorites").openConnection().getInputStream();
//          inp.close();
//          MandToolStarter.this.showStatus("favorites");
          MandTool.startApplet(getParameter("datasource"), getDocumentBase());
          MandToolStarter2.this.showStatus("done");
        }
        catch (MalformedURLException ex) {
          MandToolStarter2.this.showStatus("url failed: "+ex);
        }
//        catch (Exception ex) {
//          MandToolStarter.this.showStatus("failed: "+ex);
//          System.out.println("failed: "+ex);
//          ex.printStackTrace();
//          throw new RuntimeException("failed",ex);
//        }
        finally {

        }
      }
    });
    button.setHorizontalAlignment(JLabel.CENTER);
    button.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
    getContentPane().add(button, BorderLayout.CENTER);
    setSize(getContentPane().getPreferredSize());
  }
}
