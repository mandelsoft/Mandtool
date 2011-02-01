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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;

/**
 *
 * @author Uwe Krüger
 */

public class WindowControlAction<W extends Window> extends AbstractAction
                                 implements RenewStateListener {
  
  //////////////////////////////////////////////////////////////////////
  
  public interface WindowCreator<W extends Window> {
    public W createWindow(Window owner);
  }
  
  //////////////////////////////////////////////////////////////////////
  
  private class Listener extends WindowAdapter
                         implements PropertyChangeListener {
    @Override
    public void windowClosed(WindowEvent e)
    {
      //System.out.println("CLOSE");
      setEnabled(true);
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
      windowClosed(e);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
      if (((Boolean)evt.getNewValue())==false) {
        //System.out.println("CLOSE2");
        setEnabled(true);
      }
    }
  }

  public void renewState(Window w)
  {
    disconnectWindow();
  }
  
  //////////////////////////////////////////////////////////////////////
  
  private W window;
  private Window owner;
  private Listener listener;
  private WindowCreator<W> creator;
  private boolean cleanupWindow;
  
  public WindowControlAction(Window owner)
  {
    this.owner=owner;
    this.listener=new Listener();
    
  }

  public WindowControlAction(Window owner, String label)
  { super(label);
    this.owner=owner;
    this.listener=new Listener();

  }

  public WindowControlAction(Window owner, String label,
                             WindowCreator<W> creator)
  { this(owner,label);
    this.creator=creator;
  }

  public WindowControlAction(Window owner, String label,  W window)
  {
    this(owner,label);
    setWindow(window);
  }

  @Override
  public void setEnabled(boolean b)
  {
    super.setEnabled(b);
    if (!b) {
      Window w=getOrCreateWindow();
      w.setEnabled(true);
      w.setVisible(true);
    }
    else {
      if (window!=null) window.setVisible(false);
    }
  }

  private void setWindow(W window)
  {
    if (this.window!=null) {
      this.window.removeWindowListener(listener);
      this.window.removePropertyChangeListener(listener);
    }
    this.window=window;
    if (window!=null) {
      window.addWindowListener(listener);
      window.addPropertyChangeListener("visible",listener);
    }
  }

  public boolean isCleanupWindow()
  {
    return cleanupWindow;
  }

  public void setCleanupWindow(boolean cleanupWindow)
  {
    this.cleanupWindow=cleanupWindow;
  }

  public void disconnectWindow()
  {
    System.out.println("DISCONNECT");
    if (window!=null) {
      if (cleanupWindow) {
        window.setVisible(false);
      }
      setWindow(null);
    }
    setEnabled(true);
  }

  public W getWindow()
  { return window;
  }
  
  protected W createWindow(Window owner)
  {
    return creator.createWindow(owner);
  }
  
  private W getOrCreateWindow()
  {
    if (window==null) {
      setWindow(createWindow(owner));
    }
    return window;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    setEnabled(false);
  }
}
