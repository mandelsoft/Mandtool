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

import com.mandelsoft.util.BigDecimalFormat;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.NumberFormatter;

/**
 * FormatterFactoryDemo.java requires no other files.
 */
public class FormattedField extends JFormattedTextField {

  static boolean debug=false;
  
  ///////////////////////////////////////////////////////////////////////
  // own formal objects to control input
  ///////////////////////////////////////////////////////////////////////
  /*
   * unfortunately there is no general way to adjust the format behaviour
   * because the JFormattedTextField class uses dedicated formmatter
   * wrapperes for the different formats accepting ONLY those formats.
   * Try to fake this by calling setFormat instead of using constructor
   */
  private class FieldFormat extends Format {

    private Format fmt;
    private boolean trim;

    public FieldFormat(Format fmt)
    {
      this.fmt=fmt;
    }

    public FieldFormat setTrim(boolean b)
    {
      this.trim=b;
      return this;
    }

    @Override
    public StringBuffer format(Object value, StringBuffer toAppendTo,
            FieldPosition pos)
    {
      if (debug) {
        System.out.println("format "+value);
      }
      return fmt.format(value, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String source, ParsePosition parsePosition)
    {
      if (debug) {
        System.out.println("parse '"+parsePosition+" '"+source+"'");
      }
      try {
        int tmp=parsePosition.getIndex();
        if (trim) {
          skipBlanks(source, parsePosition);
        }
        Object n=fmt.parseObject(source, parsePosition);
        if (n==null) {
          if (debug) {
            System.out.println("basic fail");
          }
        }
        else {
          if (debug) {
            System.out.println(n+" up to "+parsePosition);
          }
          if (parsePosition.getErrorIndex()<0) {
            if (trim) {
              skipBlanks(source, parsePosition);
            }
            if (parsePosition.getIndex()<source.length()) {
              parsePosition.setErrorIndex(parsePosition.getIndex());
              parsePosition.setIndex(tmp);
              if (debug) {
                System.out.println("fail length"+parsePosition);
              }
              return null;
            }
          }
        }
        return n;
      }
      catch (RuntimeException e) {
        System.err.println(e);
        throw e;
      }
    }

    private void skipBlanks(String source, ParsePosition p)
    {
      int i=p.getIndex();
      int l=source.length();
      while (i<l&&Character.isWhitespace(source.charAt(i))) {
        i++;
      }
      p.setIndex(i);
    }
  }

  ///////////////////////////////////////////////////////////////////////
  // Input verification
  ///////////////////////////////////////////////////////////////////////
  private static class FieldVerifier extends InputVerifier {

    @Override
    public boolean verify(JComponent input)
    {
      FormattedField f=(FormattedField)input;
      Object fs;

      if (!f.isEnforceCorrectValue()) {
        return true;
      }
      try {
        fs=f.getFormatter().stringToValue(f.getText());
        return fs!=null;
      }
      catch (ParseException ex) {
        return false;
      }
    }

    @Override
    public boolean shouldYieldFocus(JComponent input)
    {
      FormattedField f=(FormattedField)input;

      if (verify(input)) {
        return true;
      }

      if (f.isShowValueWarning()) {
        String message=f.getValueHint();
        if (message!=null) {
          message="\n"+message;
        }
        else {
          message="";
        }
        JOptionPane.showMessageDialog(null, //no owner frame
                "Invalid field value, please try again."+
                message, //text to display
                "Invalid Value", //title
                JOptionPane.WARNING_MESSAGE);
      }
      //Reinstall the input verifier.
      //input.setInputVerifier(this);
      return false;
    }
  }

  static FieldVerifier verifier=new FieldVerifier();

  ///////////////////////////////////////////////////////////////////////
  // Number Field
  ///////////////////////////////////////////////////////////////////////
  private DefaultFormatter fmt;
  private String valueHint;
  private boolean enforceCorrectValue;
  private boolean showValueWarning;

  public FormattedField()
  { super();
    setInputVerifier(verifier);
  }

  public FormattedField(Object v)
  { this();
    setValue(v);
  }

  public FormattedField(Class<?> vclass, Format format)
  { this();
    setFormatterFactory(getDefaultFormatterFactory(format,vclass));
  }

  public FormattedField(Class<? extends Number> vclass)
  {
    this(vclass, createDefaultNumberFormat());
  }

  static NumberFormat createDefaultNumberFormat()
  {
    //NumberFormat fmt=NumberFormat.getNumberInstance(Locale.UK);
    NumberFormat fmt=new NumberFormat() {
      NumberFormat fmt=NumberFormat.getNumberInstance(Locale.UK);

      @Override
      public StringBuffer format(double number, StringBuffer toAppendTo,
                                 FieldPosition pos)
      {
        toAppendTo.append(Double.toString(number));
        return toAppendTo;
      }

      @Override
      public StringBuffer format(long number, StringBuffer toAppendTo,
                                 FieldPosition pos)
      {
        return fmt.format(number, toAppendTo, pos);
      }

      @Override
      public Number parse(String source, ParsePosition parsePosition)
      {
        return fmt.parse(source, parsePosition);
      }

    };
    fmt.setGroupingUsed(false);
    return fmt;
  }

  private AbstractFormatterFactory getDefaultFormatterFactory(Object type)
  {
    return getDefaultFormatterFactory(type,null);
  }

  private AbstractFormatterFactory getDefaultFormatterFactory(
                            Object type, Class<?> gvalueclass)
  {
    InternationalFormatter ifmt;
    DefaultFormatter orig=null;
    Class<?> valueclass=null;

    // first check for dedicated formatters
    if (type instanceof DateFormat) {
      valueclass=Date.class;
      fmt=ifmt=new DateFormatter();
      ifmt.setFormat(new FieldFormat((Format)type).setTrim(true));
    }
    else if (type instanceof NumberFormat) {
      fmt=ifmt=new NumberFormatter();
      ifmt.setFormat(new FieldFormat((Format)type).setTrim(true));
    }
    else if (type instanceof Format) {
      fmt=ifmt=new InternationalFormatter();
      ifmt.setFormat(new FieldFormat((Format)type));
    }
    // then check for dedicated value types
    else if (type instanceof Date) {
      fmt=ifmt=new DateFormatter();
      ifmt.setFormat(new FieldFormat(DateFormat.getDateInstance()).setTrim(true));
      valueclass=gvalueclass;
    }
    else if (type instanceof Number) {
      Format f;
      if (type instanceof BigDecimal) {
        f=new BigDecimalFormat();
      }
      else {
        NumberFormat nf=createDefaultNumberFormat();
        if ((type instanceof Double)||(type instanceof Float)) {
          // no adjust
        }
        else {
          nf.setParseIntegerOnly(true);
        }
        f=nf;
      }
      fmt=ifmt=new NumberFormatter();
      ifmt.setFormat(new FieldFormat(f).setTrim(true));
      valueclass=type.getClass();
    }

    // check for given class
    else if (isOfType(gvalueclass,Number.class)) {
      Format f;
      if (gvalueclass==BigDecimal.class) {
        f=new BigDecimalFormat();
      }
      else {
        NumberFormat nf=createDefaultNumberFormat();
        if (isOfType(gvalueclass, Double.class)||
                isOfType(gvalueclass, Float.class)) {
          // no adjust
        }
        else {
          nf.setParseIntegerOnly(true);
        }
        f=nf;
      }
      fmt=ifmt=new NumberFormatter();
      ifmt.setFormat(new FieldFormat(f).setTrim(true));
      valueclass=gvalueclass;
    }
    else if (isOfType(gvalueclass,Date.class)) {
      fmt=ifmt=new DateFormatter();
      ifmt.setFormat(new FieldFormat((Format)type));
      valueclass=gvalueclass;
    }
    // use default
    else {
      fmt=new DefaultFormatter();
      valueclass=type.getClass();
    }

    if (valueclass!=null) {
      if (gvalueclass!=null) {
        if (!valueclass.isAssignableFrom(gvalueclass))
          throw new IllegalArgumentException("given "+gvalueclass+
                            "does not match derived "+valueclass);
      }
      fmt.setValueClass(valueclass);
      if (orig!=null) {
        orig.setValueClass(valueclass);
      }
    }
    if (orig==null) {
      orig=fmt;
    }
    return new DefaultFormatterFactory(orig, orig, fmt);
  }
  
  private boolean isOfType(Class<?> v, Class<?>m)
  { return v!=null && m.isAssignableFrom(v);
  }

  private InternationalFormatter getInternationalFormatter()
  {
    if (fmt instanceof InternationalFormatter) {
      return (InternationalFormatter)fmt;
    }
    throw new UnsupportedOperationException("min/max not supported");
  }

  public void setMinimum(Comparable min)
  {
    Comparable old=getMinimum();
    if (min!=null) {
      getInternationalFormatter().setMinimum(
              (Comparable)Utils.convertValueToValueClass(min, getValueClass()));
    }
    else {
      getInternationalFormatter().setMinimum(null);
    }
    firePropertyChange("minimum", old, min);
  }

  public void setMaximum(Comparable max)
  {
    Comparable old=getMaximum();
    if (max!=null) {
      getInternationalFormatter().setMaximum(
              (Comparable)Utils.convertValueToValueClass(max, getValueClass()));
    }
    else {
      getInternationalFormatter().setMaximum(null);
    }
    firePropertyChange("maximum", old, max);
  }

  public Comparable getMinimum()
  {
    return getInternationalFormatter().getMinimum();
  }

  public Comparable getMaximum()
  {
    return getInternationalFormatter().getMaximum();
  }

  @Override
  public void setValue(Object v)
  {
    // copied from base class
    if (v!=null&&getFormatterFactory()==null) {
      setFormatterFactory(getDefaultFormatterFactory(v));
    }
    // keep consistent value type
    if (v!=null) {
      v=Utils.convertValueToValueClass(v, getValueClass());
    }
    super.setValue(v);
  }

  public String getValueHint()
  {
    return valueHint;
  }

  public void setValueHint(String hint)
  {
    this.valueHint=hint;
  }

  public boolean isEnforceCorrectValue()
  {
    return enforceCorrectValue;
  }

  public void setEnforceCorrectValue(boolean enforceCorrectValue)
  {
    this.enforceCorrectValue=enforceCorrectValue;
  }

  public boolean isShowValueWarning()
  {
    return showValueWarning;
  }

  public void setShowValueWarning(boolean showValueWarning)
  {
    this.showValueWarning=showValueWarning;
  }

  public Class<?> getValueClass()
  {
    return fmt.getValueClass();
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
    { 
      setLayout(new GridBagLayout());
      
      JLabel l0=new JLabel("string");
      FormattedField ff0=new FormattedField("test");
      System.out.println(ff0.getValueClass());
      ff0.setColumns(10);
      add(l0,new GBC(0,0));
      add(ff0,new GBC(1,0));
      
      JLabel l1=new JLabel("double>=10");
      FormattedField ff1=new FormattedField(10.0);
      System.out.println(ff1.getValueClass());
      ff1.setColumns(10);
      ff1.setMinimum(10);
      ff1.setValueHint("Value must at leat be 10.");
      ff1.setHorizontalAlignment(JTextField.TRAILING);
      add(l1,new GBC(0,1));
      add(ff1,new GBC(1,1));

      JLabel l2=new JLabel("int<=10");
      FormattedField ff2=new FormattedField(10);
      System.out.println(ff2.getValueClass());
      ff2.setColumns(10);
      ff2.setMaximum(10);
      ff2.setValueHint("Value may not be greater than 10.");
      ff2.setHorizontalAlignment(JTextField.LEADING);
      ff2.setEnforceCorrectValue(true);
      add(l2,new GBC(0,2));
      add(ff2,new GBC(1,2));

      JLabel l3=new JLabel("bigdec");
      FormattedField ff3=new FormattedField(BigDecimal.TEN);
      System.out.println(ff3.getValueClass());
      ff3.setColumns(10);
      ff3.setMaximum(10);
      ff3.setValueHint("Value may not be greater than 10.");
      ff3.setHorizontalAlignment(JTextField.LEADING);
      ff3.setEnforceCorrectValue(true);
      add(l3,new GBC(0,3));
      add(ff3,new GBC(1,3));

      pack();
    }
  }

}
    
    
