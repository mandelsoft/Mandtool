
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 *
 * @author Uwe Krüger
 */
public class BooleanAttribute extends JCheckBox.ToggleButtonModel {

  private JComponent component;
  private boolean state;
  private String property;
  private String label;

  private class ToggleListener implements ItemListener {

    public void itemStateChanged(ItemEvent e)
    {
      //System.out.println("^toggle "+property);
      if (isSelected()!=state) {
        stateChanged();
        setState();
        if (component!=null)
          component.firePropertyChange(property, !state, state);
      }
    }
  }

  public BooleanAttribute(JComponent component, String property)
  {
    this.component=component;
    this.property=property;
    addItemListener(new ToggleListener());
  }

  public BooleanAttribute(JComponent component, String property, String label)
  {
    this.component=component;
    this.property=property;
    addItemListener(new ToggleListener());
    setLabel(label);
  }

  public BooleanAttribute(JComponent component, String property, boolean b)
  {
    this.component=component;
    this.property=property;
    addItemListener(new ToggleListener());
    setState(b);
  }

  public BooleanAttribute(JComponent component, String property, String label,
                          boolean b)
  {
    this.component=component;
    this.property=property;
    addItemListener(new ToggleListener());
    setLabel(label);
    setState(b);
  }

  /**
   * Effectively set the actual state. This reflects the state
   * returned by isSet. The selction state is invisible for the attribute
   * as long as this method is not used to transfer the selection state.
   * @param b
   */
  protected void setState()
  {
    state=isSelected();
    //System.out.println("set "+property+" to "+state);
  }

  /**
   * process a state change. The effective state when calling this
   * method is the state prior to change. It is automatically changed after
   * this method has been called. But it is also possible to execute actions
   * based on the new state by calling setState() in between.
   * Alternatively instead of overriding this method the methods
   * beforeStateChange and afterStateChange can be used, which are called by
   * the default implementation of this method.
   */
  protected void stateChanged()
  {
    beforeStateChange();
    setState();
    afterStateChange();
  }

  protected void beforeStateChange()
  {
  }

  protected void afterStateChange()
  {
  }

  public boolean isSet()
  {
    return state;
  }

  public void setState(boolean b)
  {
    this.setSelected(b);
  }

  public String getLabel()
  {
    return label;
  }

  /**
   * The label is intended to be used as label for a maintenance dialog.
   *
   * @param label default label text for a meintenance dialog
   */
  public void setLabel(String label)
  {
    this.label=label;
  }

  public String getPropertyName()
  {
    return property;
  }
}
