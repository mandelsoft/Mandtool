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

import javax.swing.event.ChangeListener;

/**
 *
 * @author Uwe Krüger
 */
public interface PositionSelectionModel {

  /**
   * Returns the selected <code>Position</code> which should be
   * between <code>0.0</code> and <code>1.0</code>.
   *
   * @return  the selected position
   * @see     #setRelativeColor
   */
  double getSelectedPosition();

  /**
   * Sets the relative position to <code>rp</code>.
   * This method fires a state changed event if it sets the
   * current position to a new value.
   *
   * @param color the new relative position
   *              between <code>0.0</code> and <code>1.0</code>.
   * @see   #getRelativePosition
   * @see   #addChangeListener
   */
  void setSelectedPosition(double rp);

  /**
   * Adds <code>listener</code> as a listener to changes in the model.
   * @param listener the <code>ChangeListener</code> to be added
   */
  void addChangeListener(ChangeListener listener);

  /**
   * Removes <code>listener</code> as a listener to changes in the model.
   * @param listener the <code>ChangeListener</code> to be removed
   */
  void removeChangeListener(ChangeListener listener);
}
