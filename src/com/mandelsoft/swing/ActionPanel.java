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

package com.mandelsoft.swing;

import com.mandelsoft.util.upd.UpdatableObject;
import com.mandelsoft.util.upd.UpdateContext;
import com.mandelsoft.util.upd.UpdateSource;
import com.mandelsoft.util.upd.UpdateSourceSupport;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 *
 * @author Uwe Krueger
 */
public class ActionPanel extends GBCSupportPanel implements UpdatableObject  {
  private JPanel content;
  private JPanel buttons;
  private GBCPanel container;
  private UpdateSourceSupport update;

  public ActionPanel()
  {
    super();
    update = new UpdateSourceSupport();
    
    BorderLayout lo;
    setLayout(lo=new BorderLayout());
    container=new GBCPanel();
   
    buttons=new ButtonPanel();

    super.add(container, BorderLayout.CENTER);
    super.add(buttons,BorderLayout.SOUTH);
    //super.add(buttons, GBC(0, 1, GBC.BOTH).setWeight(10, 0));

    setContentPane(new GBCPanel());
  }

  public JPanel getContentPane()
  {
    return content;
  }

  public void setContentPane(JPanel c)
  {
    if (content!=null) container.remove(content);
    content=c;
    if (content!=null) container.add(c, GBC(0, 0,GBC.BOTH).setInsets(5));
  }

  public JComponent addBorder(int col, int row, int colspan, int rowspan) {
    if (content instanceof GBCPanel) {
      return ((GBCPanel) content).addBorder(col, row, colspan, rowspan);
    }
    return null;
  }

  public JComponent addBorder(int col, int row, int colspan, int rowspan, boolean raise) {
    if (content instanceof GBCPanel) {
      return ((GBCPanel) content).addBorder(col, row, colspan, rowspan, raise);
    }
    return null;
  }

  public JComponent addBorder(int col, int row, int colspan, int rowspan, boolean raise, String title) {
    if (content instanceof GBCPanel) {
      return ((GBCPanel) content).addBorder(col, row, colspan, rowspan, raise, title);
    }
    return null;
  }

  public void setPreferredContentSize(Dimension d)
  {
    content.setPreferredSize(d);
  }
  
  public final JButton addButton(String txt, ActionListener l, String tooltip)
  {
    JButton b=addButton(txt,l);
    b.setToolTipText(tooltip);
    return b;
  }

  public final JButton addButton(String txt, ActionListener l)
  {
    JButton b=new JButton(txt);
    b.addActionListener(l);
    return addButton(b);
  }

  public final JButton addButton(Action action)
  {
    JButton b=new JButton(action);
    return addButton(b);
  }

  public final JButton addButton(JButton b) {
    buttons.add(b);
    update.tryAddUpdatableObject(b);
    update.tryAddUpdatableObject(b.getAction());
    revalidate();
    return b;
  }

  public final void addButton(JComponent m) {
    buttons.add(m);
    update.tryAddUpdatableObject(m);
    revalidate();
  }

  protected void removeButton(JButton b) {
    buttons.remove(b);
    update.tryRemoveUpdatableObject( b);
    update.tryRemoveUpdatableObject( b.getAction());
    revalidate();
  }

  public void addContent(JComponent c, Object o)
  {
    content.add(c, o);
    update.tryAddUpdatableObject(c);
    content.revalidate();
  }

   @Override
  public void updateObject(UpdateContext c) {
    update.updateObjects(c);
  }
  
  ///////////////////////////////////////////////////////////////////////
  // test
  ///////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
    //Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
    SwingUtilities.invokeLater(new Runnable() {

      public void run()
      {
        JFrame frame=new TestFrame();

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });
  }

  ///////////////////////////////////////////////////////////////
  // Test
  ///////////////////////////////////////////////////////////////

  static class TestFrame extends JFrame {
    TestFrame()
    { ActionListener l=new ActionListener() {

        public void actionPerformed(ActionEvent e)
        {
          System.out.println("button clicked: "+e.getActionCommand());
        }

      };

      ActionPanel p=new ActionPanel();
      // p.setContentPane(new JPanel(new GridLayout(1,1)));
      JLabel c=new JLabel("label laber blubber bla");
      c.setBorder(new BevelBorder(BevelBorder.RAISED));
      p.addContent(c, new GBC(0,0).setWeight(10,10).setFill(GBC.BOTH));
      p.addButton("Test", l);
      add(p);
      pack();
      setMinimumSize(getSize());
    }
  }
}
