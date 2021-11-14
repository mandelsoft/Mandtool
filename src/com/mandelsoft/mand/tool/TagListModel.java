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

import com.mandelsoft.mand.util.TagList;
import java.io.IOException;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author Uwe Krueger
 */
public class TagListModel extends AbstractListModel
                          implements ComboBoxModel {

  private TagList list;
  private Object selectedObject;
  private boolean autosave;


  /**
   * Constructs a DefaultComboBoxModel object initialized with
   * a vector.
   *
   * @param v  a Vector object ...
   */
  public TagListModel(TagList tags)
  {
    list=tags;

    if (getSize()>0) {
      selectedObject=getElementAt(0);
    }
  }

  public boolean isAutosave()
  {
    return autosave;
  }

  public void setAutosave(boolean austosave)
  {
    this.autosave=austosave;
  }

  // implements javax.swing.ComboBoxModel
  /**
   * Set the value of the selected item. The selected item may be null.
   * <p>
   * @param anObject The combo box value or null for no selection.
   */
  public void setSelectedItem(Object anObject)
  {
    if ((selectedObject!=null&&!selectedObject.equals(anObject))
            ||selectedObject==null&&anObject!=null) {
      selectedObject=anObject;
      fireContentsChanged(this, -1, -1);
    }
  }

  // implements javax.swing.ComboBoxModel
  public Object getSelectedItem()
  {
    return selectedObject;
  }

  // implements javax.swing.ListModel
  public int getSize()
  {
    return list.size();
  }

  // implements javax.swing.ListModel
  public Object getElementAt(int index)
  {
    if (index>=0&&index<list.size())
      return list.get(index);
    else
      return null;
  }

  /**
   * Returns the index-position of the specified object in the list.
   *
   * @param anObject
   * @return an int representing the index position, where 0 is
   *         the first position
   */
  public int getIndexOf(Object anObject)
  {
    return list.indexOf(anObject);
  }


  public void addElement(String anObject)
  {
    if (!list.contains(anObject)) {
      list.add(anObject);
      fireIntervalAdded(this, list.size()-1, list.size()-1);
      if (list.size()==1&&selectedObject==null&&anObject!=null) {
        setSelectedItem(anObject);
      }
    }
  }

  public void insertElementAt(String anObject, int index)
  {
    if (!list.contains(anObject)) {
      list.add(index, anObject);
      fireIntervalAdded(this, index, index);
    }
  }

  public void removeElementAt(int index)
  {
    if (getElementAt(index)==selectedObject) {
      if (index==0) {
        setSelectedItem(getSize()==1?null:getElementAt(index+1));
      }
      else {
        setSelectedItem(getElementAt(index-1));
      }
    }

    list.remove(index);

    fireIntervalRemoved(this, index, index);
  }

  public void removeElement(String anObject)
  {
    int index=list.indexOf(anObject);
    if (index!=-1) {
      removeElementAt(index);
    }
  }

  /**
   * Empties the list.
   */
  public void removeAllElements()
  {
    if (list.size()>0) {
      int firstIndex=0;
      int lastIndex=list.size()-1;
      list.clear();
      selectedObject=null;
      fireIntervalRemoved(this, firstIndex, lastIndex);
    }
    else {
      selectedObject=null;
    }
  }

  @Override
  protected void fireContentsChanged(Object source, int index0, int index1)
  {
    save();
    super.fireContentsChanged(source, index0, index1);
  }

  @Override
  protected void fireIntervalAdded(Object source, int index0, int index1)
  {
    save();
    super.fireIntervalAdded(source, index0, index1);
  }

  @Override
  protected void fireIntervalRemoved(Object source, int index0, int index1)
  {
    save();
    super.fireIntervalRemoved(source, index0, index1);
  }

  ///////////////////////////////////////////////////////////////////////////

  protected void save()
  {
    if (autosave) try {
      list.save();
    }
    catch (IOException ex) {
      System.err.println("cannot save tag list "+ex);
    }
  }

  public boolean addUniqueElement(String a)
  {
    if (getIndexOf(a)<0) {
      addElement(a);
      return true;
    }
    return false;
  }
}
