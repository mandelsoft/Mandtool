
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Uwe Krüger
 */
public class DefaultPositionSelectionModel implements PositionSelectionModel {

  /**
   * Only one <code>ChangeEvent</code> is needed per model instance
   * since the event's only (read-only) state is the source property.
   * The source of events generated here is always "this".
   */
  protected transient ChangeEvent changeEvent=null;
  protected EventListenerList listenerList=new EventListenerList();
  private double selectedPosition;

  /**
   * Creates a <code>DefaultColorSelectionModel</code> with the
   * current color set to <code>Color.white</code>.  This is
   * the default constructor.
   */
  public DefaultPositionSelectionModel()
  {
    selectedPosition=0.0;
  }

  /**
   * Creates a <code>DefaultPositionSelectionModel</code> with the
   * current relative position set to <code>position</code>, which should be
   * between <code>0.0</code> and <code>1.0</code>.
   *
   * @param position the new relative position
   */
  public DefaultPositionSelectionModel(double position)
  {
    selectedPosition=position;
  }

  /**
   * Returns the selected <code>Color</code> which should be
   * non-<code>null</code>.
   *
   * @return the selected <code>Color</code>
   */
  public double getSelectedPosition()
  {
    return selectedPosition;
  }

  /**
   * Sets the selected relative position to <code>position</code>.
   * This method fires a state changed event if it sets the
   * current position to a new  value;
   * if the new position is the same as the current position,
   * no event is fired.
   *
   * @param position the new relative position
   */
  public void setSelectedPosition(double position)
  {
    if (position>1||position<0) {
      throw new IllegalArgumentException("illegal relative Position "+
              position);
    }
    if (selectedPosition!=position) {
      selectedPosition=position;
      fireStateChanged();
    }
  }

  /**
   * Adds a <code>ChangeListener</code> to the model.
   *
   * @param l the <code>ChangeListener</code> to be added
   */
  public void addChangeListener(ChangeListener l)
  {
    listenerList.add(ChangeListener.class, l);
  }

  /**
   * Removes a <code>ChangeListener</code> from the model.
   * @param l the <code>ChangeListener</code> to be removed
   */
  public void removeChangeListener(ChangeListener l)
  {
    listenerList.remove(ChangeListener.class, l);
  }

  /**
   * Returns an array of all the <code>ChangeListener</code>s added
   * to this <code>DefaultColorSelectionModel</code> with
   * <code>addChangeListener</code>.
   *
   * @return all of the <code>ChangeListener</code>s added, or an empty
   *         array if no listeners have been added
   * @since 1.4
   */
  public ChangeListener[] getChangeListeners()
  {
    return (ChangeListener[])listenerList.getListeners(
            ChangeListener.class);
  }

  /**
   * Runs each <code>ChangeListener</code>'s
   * <code>stateChanged</code> method.
   *
   * <!-- @see #setRangeProperties    //bad link-->
   * @see EventListenerList
   */
  protected void fireStateChanged()
  {
    Object[] listeners=listenerList.getListenerList();
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==ChangeListener.class) {
        if (changeEvent==null) {
          changeEvent=new ChangeEvent(this);
        }
        ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
      }
    }
  }
}
