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

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;

/**
 *
 * @author Uwe Krüger
 */

public class ListComboBoxModel extends AbstractListModel
                               implements ComboBoxModel {

  private ListModel model;
  private Object    selectedObject;

  public ListComboBoxModel(ListModel model)
  {
    this.model=model;
    if (getSize()>0)
      setSelectedItem(model.getElementAt(0));
  }

  public int getIndexOf(Object anObject)
  {
    int m=model.getSize();
    for (int i=0; i<m; i++)
      if (model.getElementAt(i)==anObject) return i;
    return -1;
  }

  public int getSize()
  {
    return model.getSize();
  }

  public Object getElementAt(int index)
  {
    return model.getElementAt(index);
  }

  public void setSelectedItem(Object anObject)
  {
    if ((selectedObject!=null&&!selectedObject.equals(anObject))||
            selectedObject==null&&anObject!=null) {
      selectedObject=anObject;
      fireContentsChanged(this, -1, -1);
    }
  }

  public Object getSelectedItem()
  {
    return selectedObject;
  }
}
