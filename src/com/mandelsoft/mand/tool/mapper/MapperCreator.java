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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.swing.ChangeListenerSupport;
import com.mandelsoft.swing.GBCPanel;

/**
 *
 * @author Uwe Krüger
 */

public abstract class MapperCreator extends GBCPanel {
  private String name;
  private ChangeListenerSupport listeners=new ChangeListenerSupport();
  protected Listener listener=new Listener();
  protected boolean editable=true;

  public MapperCreator(String name)
  { this.name=name;
  }

  @Override
  public String getName()
  { return name;
  }

  public boolean isEditable()
  {
    return editable;
  }

  public void setEditable(boolean editable)
  {
    this.editable=editable;
  }

  abstract Mapper createMapper();
  abstract boolean setup(Mapper m);

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
    listeners.fireChangeEvent();
  }

  protected class Listener implements ActionListener, ChangeListener,
                                      PropertyChangeListener {
    protected void fire()
    {
      //System.out.println("mapper value changed");
      fireChangeEvent();
    }

    public void actionPerformed(ActionEvent e)
    {
      fire();
    }

    public void stateChanged(ChangeEvent e)
    {
      fire();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
      fire();
    }
  }
}
