
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

import java.awt.Color;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import com.mandelsoft.mand.util.ColorList;
import com.mandelsoft.swing.ChangeListenerSupport;

/**
 *
 * @author Uwe Krüger
 */

public class ColorListModel extends ChangeListenerSupport {

  public class ListEvent extends ChangeEvent {
    static public final int ADD=1;
    static public final int REMOVE=2;

    private Color color;
    private int mode;

    protected ListEvent(Color c, int mode)
    {
      super(ColorListModel.this);
      this.color=c;
      this.mode=mode;
    }

    public Color getColor()
    {
      return color;
    }

    public int getMode()
    {
      return mode;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  private ColorList list;
  private boolean autosave;

  public ColorListModel(ColorList list)
  {
    this.list=list;
  }

  public boolean isAutosave()
  {
    return autosave;
  }

  public void setAutosave(boolean autosave)
  {
    this.autosave=autosave;
  }

  public ColorList getList()
  {
    return list;
  }

  public boolean addColor(Color c)
  {
    boolean a=list.add(c);
    if (a) fire(c,ListEvent.ADD);
    return a;
  }

  public boolean addColor(int index, Color c)
  {
    if (!list.contains(c)) {
      list.add(index, c);
      fire(c, ListEvent.ADD);
      return true;
    }
    return false;
  }

  public boolean removeColor(Color c)
  {
    boolean a=list.remove(c);
    if (a) fire(c,ListEvent.REMOVE);
    return a;
  }

  ///////////////////////////////////////////////////////////////////////////
  protected void fire(Color c, int mode)
  {
    ListEvent e=new ListEvent(c,mode);
    if (autosave) try {
      list.save();
    }
    catch (IOException ex) {
      System.err.println("cannot save color list "+ex);
    }
    this.fireChangeEvent(e);
  }
}
