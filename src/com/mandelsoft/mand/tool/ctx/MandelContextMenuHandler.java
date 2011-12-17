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

package com.mandelsoft.mand.tool.ctx;

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelContextMenuHandler<E,S,M>
                      implements ContextProvider<E,S,M> {

  //////////////////////////////////////////////////////////////////////////
  // Environment Embedding
  //////////////////////////////////////////////////////////////////////////

  public abstract M getModel();

  public JWindow getWindow()
  {
    return _lookupInterface(contextComponent, JWindow.class);
  }

  public JDialog getDialog()
  {
    return _lookupInterface(contextComponent, JDialog.class);
  }

  public MandelWindowAccess getMandelWindowAccess()
  {
    return MandelWindowAccess.Access.getMandelWindowAccess(contextComponent);
  }

  protected int getMaxFrame()
  {
    MandelWindowAccess access=getMandelWindowAccess();
    return access.getMandelImagePane()==null?0:access.getMandelImagePane().getMaxFrame();
  }

  protected void Error(String title, String msg)
  {
    JOptionPane.showMessageDialog(getWindow(), msg, title,
                                       JOptionPane.WARNING_MESSAGE);
  }

  protected void Info(String title, String msg)
  {
    JOptionPane.showMessageDialog(getWindow(), msg, title,
                                       JOptionPane.INFORMATION_MESSAGE);
  }

  public <T> T lookupInterface(Class<T> clazz)
  {
    return lookupInterface(getContextComponent(),clazz);
  }
  
  public <T> T lookupInterface(Component leaf, Class<T> clazz)
  {
    MandelWindowAccess access;
    T t=_lookupInterface(leaf,clazz);

    if (t==null) {
      access=MandelWindowAccess.Access.getMandelWindowAccess(leaf);
      if (access!=null) {
        if (access.getMandelImagePane()!=null) {
          t=_lookupInterface(access.getMandelImagePane(),clazz);
        }
        if (t==null) {
          if (access.getMandelWindow()!=null) {
            t=_lookupInterface(access.getMandelWindow(),clazz);
          }
        }
      }
    }
    return t;
  }

  ///////////////////////////////////////////////////////////////////////////

  private boolean busy;
  private Cursor origcursor;

  protected void setBusy(boolean b)
  {
    if (b!=busy) {
      if (b) {
        System.out.println("-------------------------------------------------");
        System.out.println("set busy");
        origcursor=contextComponent.getCursor();
        contextComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
      else {
        System.out.println("orig cursor");
        contextComponent.setCursor(origcursor);
      }
    }
    busy=b;
  }

  protected <T> T _lookupInterface(Component leaf, Class<T> clazz)
  {
    Component c=leaf;
    while (c!=null && !clazz.isAssignableFrom(c.getClass())) {
      c=c.getParent();
    }
    return (T)c;
  }

  
  private Component contextComponent;
  private S         contextSpec;

  public Component getContextComponent()
  {
    return contextComponent;
  }

  public S getSelectionSpec()
  {
    return contextSpec;
  }
  
  abstract public E getSelectedItem();


  public void handleContextMenu(JComponent comp, MouseEvent evt, S spec)
  {
    JPopupMenu menu;

    contextComponent=comp;
    contextSpec=spec;
    menu=createContextMenu(spec);
    if (menu!=null) {
      menu.show(comp, evt.getX(), evt.getY());
    }
  }

   protected abstract JPopupMenu createContextMenu(S spec);

   public abstract class ContextAction
                   extends com.mandelsoft.mand.tool.ctx.ContextAction<E,S,M> {

    public ContextAction(String name)
    {
      super(name,MandelContextMenuHandler.this);
    }
  }

  public abstract class LoadImageContextAction extends ContextAction {
    protected LoadImageContextAction(String name)
    {
      super(name);
    }

    protected void loadImage(QualifiedMandelName sel)
    {
      if (sel==null) return;
      setBusy(true);
      MandelWindowAccess access=getMandelWindowAccess();
      if (!access.getMandelImagePane().setImage(sel)) {
        JOptionPane.showMessageDialog(getWindow(),
                                      "Cannot load image: "+sel,
                                      "Mandel IO", JOptionPane.WARNING_MESSAGE);
      }
      setBusy(false);
    }
  }
}
