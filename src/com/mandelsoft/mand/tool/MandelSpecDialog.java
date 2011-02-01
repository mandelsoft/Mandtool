
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
import java.awt.Font;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.swing.ChangeListenerSupport;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import com.mandelsoft.swing.NumberField;
import javax.swing.JFormattedTextField;

/**
 *
 * @author Uwe Krüger
 */

public abstract class MandelSpecDialog<P extends MandelSpecDialog.Panel> extends JDialog {
  private ChangeListenerSupport listeners;
  protected P panel;

  public MandelSpecDialog(Window parent, String title, String name,
                          boolean change)
  {
    super(parent);
    this.listeners=new ChangeListenerSupport();

    setName(name);
    setTitle(title);
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    
    this.panel=createPanel(name,change);
    add(panel);
    pack();
    this.setResizable(false);
  }

  protected abstract P createPanel(String name, boolean change);

  public void setEditable(boolean b)
  {
    panel.setEditable(b);
  }

  protected P getPanel()
  {
    return panel;
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }

  protected void fireChangeEvent()
  {
    listeners.fireChangeEvent(this);
  }

  protected MandelWindowAccess getMandelWindowAccess()
  {
    return MandelWindowAccess.Access.getMandelWindowAccess(this);
  }

  public void updateInfo(MandelInfo info)
  {
    panel.updateInfo(info);
  }

  public void setInfo(MandelInfo info)
  {
    panel.setInfo(info);
  }

  public void setData(MandelData data)
  {
    panel.setData(data);
  }

  @Override
  public void setName(String name)
  {
    super.setName(name);
    if (panel!=null) panel.setName(name);
  }


  public abstract class Panel extends GBCPanel {
    private boolean change;
    private JLabel label;
    protected boolean inupdate;
    protected PropertyChangeListener updateListener;

    protected Panel(String name, boolean change)
    {
      this.change=change;
      setName(name);
      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      add(label=new JLabel(getName()),
          new GBC(0, 0, 2, 1).setLayout(GBC.HORIZONTAL,GBC.CENTER).
                              setBottomInset(10));
      label.setHorizontalAlignment(JLabel.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD,
                                               label.getFont().getSize()+2));
    }

    @Override
    public void setName(String name)
    {
      super.setName(name);
      if (label!=null) label.setText(name);
    }

    public void setInfo(MandelInfo info)
    {
      inupdate=true;
      _setInfo(info);
      inupdate=false;
    }

    public void setData(MandelData data)
    {
      setInfo(data.getInfo());
    }
    
    protected void _setInfo(MandelInfo info)
    {
    }

    public boolean updateInfo(MandelInfo info)
    {
      return false;
    }

    public void setEditable(boolean b)
    {
      change=b;
    }

    protected NumberField createNumberField(String txt,int y)
    { JLabel label=new JLabel(txt);
      label.setHorizontalAlignment(JTextField.LEFT);
      add(label, new GBC(0, y, 1, 1).setWeight(0, 0).setAnchor(GBC.WEST));
      NumberField field=new NumberField(BigDecimal.class);
      field.setEditable(change);
      if (!change) {
        field.setBorder(null);
      }
      field.setHorizontalAlignment(JTextField.TRAILING);
      field.setColumns(50);
      if (updateListener!=null) field.addPropertyChangeListener("value", updateListener);
      label.setLabelFor(field);
      add(field,new GBC(1,y,1,1).setLeftInset(10));
      return field;
    }

    protected JFormattedTextField createTextField(String txt,int y)
    {
      JLabel fieldlabel=new JLabel(txt);
      fieldlabel.setHorizontalAlignment(JTextField.LEFT);
      add(fieldlabel, new GBC(0, y, 1, 1).setWeight(0, 0).setAnchor(GBC.WEST));
      JFormattedTextField field=new JFormattedTextField();
      field.setBorder(null);
      field.setHorizontalAlignment(JTextField.TRAILING);
      field.setColumns(20);
      field.setEditable(false);
      fieldlabel.setLabelFor(field);
      add(field,new GBC(1,y,1,1).setLeftInset(10));
      return field;
    }
  }
}
