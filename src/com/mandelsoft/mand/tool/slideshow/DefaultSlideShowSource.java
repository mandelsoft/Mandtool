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

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.MandelImagePanel;
import com.mandelsoft.mand.tool.MandelListSelector;
import com.mandelsoft.mand.tool.MandelNameSelector;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.mand.tool.util.MandelContextAction;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.util.Utils;
import java.awt.Component;
import javax.swing.JComponent;

/**
 *
 * @author Uwe Krueger
 */
public class DefaultSlideShowSource extends SlideShowSourceAdapter {
  
  protected JComponent menu;
  protected Component comp;
  
  public DefaultSlideShowSource(JComponent menu, Component comp)
  {
    this.menu=menu;
    this.comp=comp;
  }

  //////////////////////////////////////////////////////////////////////////
  // List Mode
  //////////////////////////////////////////////////////////////////////////

  protected class DefaultListMode extends ListModeAdapter {
    protected MandelListSelector selector;

    public DefaultListMode(MandelListSelector selector)
    {
      this.selector=selector;
    }

    @Override
    public MandelList getMandelList(SlideShowModel model)
    {
      return selector.getSelectedMandelList();
    }
  }

  @Override
  public ListMode getListMode(SlideShowModel model)
  {
    MandelListSelector ml;

    ml=MandelContextAction.getEnvironmentObject(menu, MandelListSelector.class);
    if (ml==null) {
      ml=MandelContextAction.getEnvironmentObject(comp,
                                                  MandelListSelector.class);
    }
    if (ml!=null) return new DefaultListMode(ml);
    return null;
  }

  //////////////////////////////////////////////////////////////////////////
  // One Mode
  //////////////////////////////////////////////////////////////////////////

  protected class DefaultOneMode extends OneModeAdapter {
    protected MandelNameSelector selector;

    public DefaultOneMode(MandelNameSelector selector)
    {
      this.selector=selector;
    }

    @Override
    public QualifiedMandelName getSingleName(SlideShowModel model)
    {
      return selector.getSelectedMandelName();
    }
  }

  @Override
  public OneMode getOneMode(SlideShowModel model)
  {
    MandelNameSelector mn=getMandelNameSelector();
    if (mn!=null) return new DefaultOneMode(mn);
    return null;
  }

  //////////////////////////////////////////////////////////////////////////
  // Two Mode
  //////////////////////////////////////////////////////////////////////////

  protected class DefaultTwoMode extends TwoModeAdapter {
    protected MandelNameSelector selector;

    public DefaultTwoMode(MandelNameSelector selector)
    {
      this.selector=selector;
    }

    @Override
    public QualifiedMandelName getFirstName(SlideShowModel model)
    {
      MandelWindowAccess acc;
      MandelImagePanel mp=null;
      QualifiedMandelName s=model.getCurrentQualifiedMandelName();
      if (s==null) {
        acc=MandelWindowAccess.Access.getMandelWindowAccess(comp);
        
        if (acc!=null) mp=acc.getMandelImagePane();
        if (mp!=null) s=mp.getQualifiedMandelName();
      }
      return s;
    }

    @Override
    public QualifiedMandelName getSecondName(SlideShowModel model)
    {
      QualifiedMandelName s=getFirstName(model);
      QualifiedMandelName n=selector.getSelectedMandelName();
      if (s==null || n==null) return null;
      if (Utils.equals(s.getMandelName(), n.getMandelName())) return null;
      return n;
    }
  }

  @Override
  public TwoMode getTwoMode(SlideShowModel model)
  {
    MandelWindowAccess acc;
    MandelImagePanel mp=null;
    QualifiedMandelName s=model.getCurrentQualifiedMandelName();
    
    if (s==null) {
      acc=MandelWindowAccess.Access.getMandelWindowAccess(comp);
      if (acc!=null) mp=acc.getMandelImagePane();
      if (mp==null) return null;
    }

    MandelNameSelector mn=getMandelNameSelector();
    if (mn!=null) return new DefaultTwoMode(mn);
    return null;
  }

  //////////////////////////////////////////////////////////////////////////
  // Utilities
  //////////////////////////////////////////////////////////////////////////

  protected MandelNameSelector getMandelNameSelector()
  {
    MandelNameSelector mn;
    mn=MandelContextAction.getEnvironmentObject(menu, MandelNameSelector.class);
    if (mn==null) {
      mn=MandelContextAction.getEnvironmentObject(comp, MandelNameSelector.class);
    }
    return mn;
  }
}
