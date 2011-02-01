
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
import com.mandelsoft.mand.tool.MandelImagePanel;
import com.mandelsoft.mand.tool.MandelListSelector;
import com.mandelsoft.mand.tool.MandelNameSelector;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.mand.tool.util.MandelContextMenuFactory;
import com.mandelsoft.util.Utils;

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

  private boolean checkTwo(MandelImagePanel mp, MandelNameSelector mn)
  {
    if (mn==null || mp==null) return false;
    QualifiedMandelName n=mn.getSelectedMandelName();
    if (n==null) return false;
    return !Utils.equals(mp.getQualifiedMandelName().getMandelName(), n.getMandelName());
  }

  private int getMode(JComponent menu, Component comp, boolean generic)
  {
    int mode=0;
    MandelWindowAccess acc=MandelWindowAccess.Access.getMandelWindowAccess(comp);
    MandelListSelector ml;
    MandelNameSelector mn;
    MandelImagePanel mp=null;

    if (acc!=null) mp=acc.getMandelImagePane();
    ml=SlideShowActionBase.getEnvironmentObject(menu, MandelListSelector.class);
    if (ml==null) {
      ml=SlideShowActionBase.getEnvironmentObject(comp, MandelListSelector.class);
    }
    if (ml!=null && (generic || ml.getSelectedMandelList()!=null)) {
      mode|=SlideShowAction.LIST;
    }

    mn=SlideShowActionBase.getEnvironmentObject(menu, MandelNameSelector.class);
    if (mn==null) {
      mn=SlideShowActionBase.getEnvironmentObject(comp, MandelNameSelector.class);
    }

    if (mn!=null && (generic || mn.getSelectedMandelName()!=null)) {
      mode|=SlideShowAction.ONE;
      if (mp!=null) {
        if (generic || checkTwo(mp,mn)) {
          mode|=SlideShowAction.TWO;
        }
      }
    }
    if (mp!=null && mp==comp) {
      mode|=SlideShowAction.ONE;
    }
    return mode;
  }

  @Override
  protected void updateItem(JMenuItem item, Component comp)
  {
    if (item.getAction() instanceof SlideShowAction) {
      int mode=getMode(item,comp,false);
      SlideShowAction a=(SlideShowAction)item.getAction();
      if ((a.getMode()&mode)!=0) {
        item.setEnabled(true);
      }
      else {
        item.setEnabled(false);
      }
    }
  }

  protected void addItems(JComponent menu, Component comp, boolean generic)
  {
    JMenuItem it;
    int mode=getMode(menu,comp,generic);

    for (SlideShowAction a:actions) {
      if ((a.getMode()&mode)!=0) {
        it=new JMenuItem(a);
        menu.add(it);
      }
    }
    it=new JMenuItem(stop);
    menu.add(it);
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

  
}
