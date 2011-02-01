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

/**
 * A text field representing a number.
 */
package com.mandelsoft.swing;


import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class NumberField extends FormattedField
                         implements DataField<Number> {
  
  ///////////////////////////////////////////////////////////////////////
  // Number Field
  ///////////////////////////////////////////////////////////////////////
  
  public NumberField(Number v)
  { super(v);
    setHorizontalAlignment(JTextField.TRAILING);
  }

  public NumberField(Class<? extends Number> vclass, NumberFormat format)
  { super(vclass,format);
    setHorizontalAlignment(JTextField.TRAILING);
  }

  public NumberField(Class<? extends Number> vclass)
  { this(vclass, null);
  }

  @Override
  public Number getValue()
  { return (Number)super.getValue();
  }

  public void setMinimumNumber(Number min)
  { 
    if (min!=null)
      setMinimum((Comparable)Utils.convertValueToValueClass(min,getValueClass()));
    else 
      super.setMinimum(null);
  }

  public void setMaximumNumber(Number max)
  { 
    if (max!=null)
      setMaximum((Comparable)Utils.convertValueToValueClass(max,getValueClass()));
    else 
      super.setMaximum(null);
  }

  public Number getMinimumNumber()
  {
    return (Number)getMinimum();
  }

  public Number getMaximumNumber()
  {
    return (Number)getMaximum();
  }
  
  public Number getDataValue()
  { return getValue();
  }

  public void setDataValue(Number v)
  {
    setValue(v);
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

  static class TestFrame extends JFrame {
    TestFrame()
    { PropertyChangeListener listener=new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt)
        {
          NumberField f=(NumberField)evt.getSource();
          Object v=evt.getNewValue();
          if (v!=null)
            System.out.println("="+v.getClass()+"="+v);
        }
      };

      JFormattedTextField tf0=new JFormattedTextField("test");
      tf0.setColumns(10);
      NumberField nf1=new DoubleField();
      nf1.setValue(10.0);
      NumberField nf2=new NumberField(10);
      System.out.println(nf1.getValueClass());
      System.out.println(nf2.getValueClass());
      nf1.setColumns(10);
      nf1.setMinimum(10);
      nf1.setValueHint("Value must at leat be 10.");
      nf1.setHorizontalAlignment(JTextField.TRAILING);
      nf2.setColumns(10);
      nf2.setMaximum(10);
      nf2.setValueHint("Value may not be greater than 10.");
      nf2.setHorizontalAlignment(JTextField.LEADING);
      
      NumberField nf3=new NumberField(BigDecimal.class);
      nf3.setColumns(10);
      nf3.addPropertyChangeListener("value", listener);

      this.setLayout(new FlowLayout());
      add(tf0);
      add(nf1);
      add(nf2);
      add(nf3);
      pack();
    }
  }

}
    
    
