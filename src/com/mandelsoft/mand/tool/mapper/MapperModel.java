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

package com.mandelsoft.mand.tool.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.swing.ChangeListenerSupport;

/**
 *
 * @author Uwe Krüger
 */
public class MapperModel extends AbstractListModel implements ComboBoxModel {
  public static boolean debug=false;

  private List<MapperCreator> mappers;
  private MapperCreator selected;
  private ChangeListenerSupport listeners=new ChangeListenerSupport();
  private MapperListener listener=new MapperListener();


  public MapperModel()
  {
    super();
    mappers=new ArrayList<MapperCreator>();
    setup();
  }

  private void setup()
  {
    addCreator(new CyclicMapperCreator());
    addCreator(new StatisticMapperCreator());
    addCreator(new OptimalMapperCreator());
  }

  public MapperCreator getCreator(int index)
  {
    return mappers.get(index);
  }

  public int getSize()
  {
    return mappers.size();
  }

  public Object getElementAt(int index)
  {
    return mappers.get(index).getName();
  }

  public MapperCreator getElement(Object item)
  {
    for (MapperCreator c:mappers) {
      if (item.equals(c.getName())) {
        return c;
      }
    }
    return null;
  }

  public void setSelectedItem(Object anItem)
  {
    if (selected!=null) {
      selected.removeChangeListener(listener);
    }
    if (anItem==null) {
      selected=null;
    }
    else
      if (debug) System.out.println("current select: "+selected);
      for (MapperCreator c:mappers) {
        if (anItem.equals(c.getName())) {
          if (debug) System.out.println("found select: "+c);
          MapperCreator old=selected;
          selected=c;
          selected.addChangeListener(listener);
          if (selected!=old) {
            if (debug) System.out.println("fire change event: "+selected.getName());
            //this.fireContentsChanged(this, 0, mappers.size()-1);
            fireChangeEvent();
          }
        }
      }
  }

  public Object getSelectedItem()
  {
    if (selected==null) return null;
    return selected.getName();
  }

  public Mapper getMapper()
  {
    return selected.createMapper();
  }

  /////////////////////////////////////////////////////////////////////
  
  public void addCreator(MapperCreator c)
  {
    if (!mappers.contains(c)) {
      mappers.add(c);
      c.setVisible(false);
      if (selected==null) selected=c;
      this.fireContentsChanged(this, mappers.size()-1, mappers.size()-1);
    }
  }

  public void removeCreator(MapperCreator c)
  {
    for (int i=0; i<mappers.size();
         i++) {
      if (mappers.get(i)==c) {
        mappers.remove(c);
        if (selected==c) selected=null;
        this.fireContentsChanged(this, i, mappers.size());
      }
    }
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }

  private void fireChangeEvent()
  {
    listeners.fireChangeEvent();
  }

  //////////////////////////////////////////////////////////////////////
  // change handling
  //////////////////////////////////////////////////////////////////////

  private class MapperListener implements ChangeListener {

    public void stateChanged(ChangeEvent e)
    {
      if (debug) System.out.println("mapper data changed");
      fireChangeEvent();
    }

  }
}
