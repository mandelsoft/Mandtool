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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.TextAction;

/**
 *
 * @author Uwe Krüger
 */

public class TextField extends JTextField
                       implements DataField<String> {

  private class CommitAction extends AbstractAction {

    public CommitAction()
    {
      super(JTextField.notifyAction);
    }

    public void actionPerformed(ActionEvent e)
    {
      if (isEnabled()) {
        _setText(getText());
      }
    }

    @Override
    public boolean isEnabled()
    {
      return isEditable();
    }
  }

  private String old;
  private Action[] defaultActions;

  public TextField(String txt)
  { super(txt);
    old=txt;
  }

  public TextField()
  { super("");
  }

  @Override
  public Action[] getActions()
  {
    if (defaultActions==null) {
      defaultActions=new Action[] {
        new CommitAction()
      };
    }
    return TextAction.augmentList(super.getActions(), defaultActions);
  }

  public String getDataValue()
  {
    return getText();
  }

  public void setDataValue(String v)
  {
    setText(v);
  }

  @Override
  protected void processFocusEvent(FocusEvent e)
  {
    super.processFocusEvent(e);

    // ignore temporary focus event
    if (e.isTemporary()) {
      return;
    }

    if (e.getID()==FocusEvent.FOCUS_LOST) {
      _setText(getText());
    }
  }

  @Override
  public void setText(String v)
  {
    super.setText(v);
    _setText(v);
  }

  protected void _setText(String v)
  {
    if (v!=null && old !=null && old.equals(v))
      return;
    if (v==old) return;
    firePropertyChange("value", old, v);
    old=v;
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
    { PropertyChangeListener listener=new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt)
        {
          //TextField f=(TextField)evt.getSource();
          Object v=evt.getNewValue();
          if (v!=null)
            System.out.println("="+evt.getPropertyName()+"="+v);
        }
      };

      JTextField tf0=new JTextField("test");
      tf0.setColumns(10);
      //tf0.setHorizontalAlignment(JTextField.LEFT);
      tf0.setHorizontalAlignment(JTextField.TRAILING);
      tf0.addPropertyChangeListener(listener);
      TextField nf1=new TextField();
      nf1.setColumns(20);
      nf1.setDataValue("Test");
      //nf1.setHorizontalAlignment(JTextField.LEFT);
      nf1.setHorizontalAlignment(JTextField.TRAILING);
      nf1.addPropertyChangeListener(listener);

      JButton b=new JButton();
      b.setContentAreaFilled(true);
      b.setBackground(Color.BLUE);
      this.setLayout(new FlowLayout());
      add(tf0);
      add(nf1);
      add(b);
      pack();
    }
  }
}
