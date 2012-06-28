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

import java.util.ArrayList;
import java.util.List;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Uwe Krueger
 */
public abstract class Selection {
  private int lead=-1;

  protected abstract ListSelectionModel getSelectionModel();

  void setLeadSelection(int lead)
  {
    this.lead=lead<0?-1:convertIndexToModel(lead);
  }

  public int getLeadSelection()
  {
    return lead;
  }

  public boolean isEmpty()
  {
    return getSelectionModel().isSelectionEmpty();
  }

  public List<Integer> getSelectedIndices()
  {
    List<Integer> list=new ArrayList<Integer>();
    ListSelectionModel selmodel=getSelectionModel();
    if (!selmodel.isSelectionEmpty()) {
      for (int index=selmodel.getMinSelectionIndex();
           index<=selmodel.getMaxSelectionIndex();
           index++) {
        if (selmodel.isSelectedIndex(index)) {
          list.add(convertIndexToModel(index));
        }
      }
    }
    return list;
  }

  protected int convertIndexToModel(int index)
  {
    return index;
  }
  
  @Override
  public String toString()
  {
    return "index "+lead+", "+getSelectedIndices();
  }
}
