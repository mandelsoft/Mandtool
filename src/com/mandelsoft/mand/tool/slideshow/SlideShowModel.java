
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
package com.mandelsoft.mand.tool.slideshow;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.util.MandelContextAction;
import com.mandelsoft.mand.tool.util.MandelContextMenuFactory;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/**
 *
 * @author Uwe Krüger
 */

public class SlideShowModel extends MandelContextMenuFactory {
  private SlideShowDestination destination;
  private List<SlideShowAction> actions;
  private List<SlideShow> slideshows;
  private Action stop;
  private SlideShow active;

  public SlideShowModel(SlideShowDestination dest)
  {
    super("Slide Show");
    destination=dest;
    actions=new ArrayList<SlideShowAction>();
    slideshows=new ArrayList<SlideShow>();

    stop=new StopAction();
    setActive(null);
  }

  public void addSlideShow(SlideShow s)
  {
    if (!slideshows.contains(s)) {
      s.install(this);
      slideshows.add(s);
      for (SlideShowAction a:s.getActions()) {
        actions.add(a);
        a.setEnabled(active==null);
      }
    }
  }
  public SlideShow getActive()
  {
    return active;
  }

  public void setActive(SlideShow s)
  {
    if (s!=null && active!=null) {
      throw new IllegalStateException(active.getId()+" still active");
    }
    active=s;
    for (SlideShowAction a:actions) a.setEnabled(s==null);
    stop.setEnabled(s!=null);
  }

  public Action getStopAction()
  {
    return stop;
  }

  public void cancel()
  {
    for (SlideShow s:slideshows) s.cancel();
  }

  ////////////////////////////////////////////////////////////////////////////

  public JMenu createMenu(Component comp, SlideShowSource src)
  {
    JMenu menu=new MandelSourceMenu(comp,src);
    _addItems(menu, comp, true);
    return menu;
  }


  public JPopupMenu createPopupMenu(Component comp, SlideShowSource src)
  {
    JPopupMenu menu=new MandelSourcePopupMenu(comp,src);
    _addItems(menu, comp, true);
    return menu;
  }

  ////////////////////////////////////////////////////////////////////////////
  // direct name flavors

  private class MandelSourcePopupMenu extends PopupMenuBase
                                implements SlideShowSource {
    private SlideShowSource src;

    public MandelSourcePopupMenu(Component comp, SlideShowSource src)
    {
      super(comp);
      this.src=src;
    }

    public int getSourceMode(SlideShowModel model, boolean generic)
    {
      return src.getSourceMode(model,generic);
    }

    public TwoMode getTwoMode(SlideShowModel model)
    {
      return src.getTwoMode(model);
    }

    public OneMode getOneMode(SlideShowModel model)
    {
      return src.getOneMode(model);
    }

    public ListMode getListMode(SlideShowModel model)
    {
      return src.getListMode(model);
    }
  }

  private class MandelSourceMenu extends MenuBase
                           implements SlideShowSource {
    private SlideShowSource src;

    public MandelSourceMenu(Component comp, SlideShowSource src)
    {
      super(comp);
      this.src=src;
    }

    public int getSourceMode(SlideShowModel model, boolean generic)
    {
      return src.getSourceMode(model,generic);
    }

     public TwoMode getTwoMode(SlideShowModel model)
    {
      return src.getTwoMode(model);
    }

    public OneMode getOneMode(SlideShowModel model)
    {
      return src.getOneMode(model);
    }

    public ListMode getListMode(SlideShowModel model)
    {
      return src.getListMode(model);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // update

  private static SlideShowSource getSource(JComponent menu, Component comp)
  {
    SlideShowSource s=MandelContextAction.getEnvironmentObject(menu,
                                                       SlideShowSource.class);
    if (s==null) {
      s=MandelContextAction.getEnvironmentObject(comp, SlideShowSource.class);
    }
    if (s!=null) return s;

    return new DefaultSlideShowSource(menu,comp);
  }

  private int getMode(JComponent menu, Component comp, boolean generic)
  {
    return getSource(menu,comp).getSourceMode(this,generic);
  }

  @Override
  protected void updateItem(JMenuItem item, Component comp)
  {
    if (item.getAction() instanceof SlideShowAction) {
      SlideShowAction a=(SlideShowAction)item.getAction();
      SlideShowActionMenuItem mi=(SlideShowActionMenuItem)item;
      int mode=getMode(item, comp, false);
      if ((a.getMode()&mode)!=0) {
        mi.setPossible(true);
      }
      else {
        mi.setPossible(false);
      }
    }
  }

  protected void addItems(JComponent menu, Component comp, boolean generic)
  {
    JMenuItem it;
    int mode=getMode(menu,comp,generic);

    for (SlideShowAction a:actions) {
      if ((a.getMode()&mode)!=0) {
        it=new SlideShowActionMenuItem(a);
        menu.add(it);
      }
    }
    it=new JMenuItem(stop);
    menu.add(it);
  }

  public SlideShowSource getSource(ActionEvent e)
  {
    SlideShowSource s=MandelContextAction.getEnvironmentObject(e, SlideShowSource.class);
    if (s!=null) return s;

    return new DefaultSlideShowSource(null,(Component)e.getSource());
  }

  public boolean show(QualifiedMandelName name)
  {
    if (!destination.show(name)) {
      JOptionPane.showMessageDialog(destination.getWindow(),
                                    "Cannot load image: "+name,
                                    "Mandel IO", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }

  public boolean show(MandelName name)
  {
    if (!destination.show(name)) {
      JOptionPane.showMessageDialog(destination.getWindow(),
                                    "Cannot load image: "+name,
                                    "Mandel IO", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }

  public QualifiedMandelName getCurrentQualifiedMandelName()
  {
    return destination.getCurrentQualifiedMandelName();
  }

  public void setHighLight(QualifiedMandelName name)
  {
    destination.setHighLight(name);
  }

  private class StopAction extends AbstractAction {

    public StopAction()
    {
      super("Stop Show");
    }

    public void actionPerformed(ActionEvent e)
    {
      if (isEnabled()) {
        cancel();
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////

  /**
   * Keep state info whether the action is possible in the actual
   * mandel context.
   * This state is independent of the action state. The action state
   * described whether the action is basically possible for the
   * current slide show state. If a slide show is running the
   * actions are disabled.
   * If the slide show stops the action will be enabled again,
   * this must reset the item state to the possible state. This
   * propgataion from the action state to the item state is done
   * in the actionPropertyChanged event handler overridden from
   * the JMenuItem class.
   */
  private static class SlideShowActionMenuItem extends JMenuItem {
    private boolean possible;

    public SlideShowActionMenuItem(Action a)
    {
      super(a);
      possible=a.isEnabled();
    }

    public boolean isPossible()
    {
      return possible;
    }

    public void setPossible(boolean possible)
    {
      this.possible=possible;
      setEnabled(possible&getAction().isEnabled());
    }

    @Override
    protected void actionPropertyChanged(Action action, String propertyName)
    {
      if (propertyName!=null && propertyName.equals("enabled")) {
        if (action.isEnabled()) {
          setEnabled(isPossible());
          return;
        }
      }
      super.actionPropertyChanged(action, propertyName);
    }
  }
}
