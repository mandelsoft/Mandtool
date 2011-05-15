
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
package com.mandelsoft.mand.tool.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.MandelListSelector;
import com.mandelsoft.mand.tool.MandelNameSelector;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */
public abstract class MandelContextAction
                extends ContextAction<QualifiedMandelName> {

  public MandelContextAction(String name)
  {
    super(name);
  }

  protected QualifiedMandelName getSelectedItem(ActionEvent e)
  {
    MandelNameSelector s=getMandelNameSelector(e);
    if (s!=null) {
      QualifiedMandelName n=s.getSelectedMandelName();
      if (n!=null) {
        System.out.println("selected "+n);
        return n;
      }
    }
    return null;
  }

  protected MandelList getSelectedMandelList(ActionEvent e)
  {
    MandelListSelector s=getMandelListSelector(e);
    if (s!=null) {
      MandelList n=s.getSelectedMandelList();
      return n;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////

  public static MandelNameSelector getMandelNameSelector(ActionEvent e)
  {
    return getEnvironmentObject(e,MandelNameSelector.class);
  }

  public static MandelListSelector getMandelListSelector(ActionEvent e)
  {
    return getEnvironmentObject(e,MandelListSelector.class);
  }

//  protected MandelListPanel getListPanel(ActionEvent e)
//  {
//    return getEnvironmentObject(e,MandelListPanel.class);
//  }

  ////////////////////////////////////////////////////////////////////
  public static MandelWindowAccess getMandelWindowAccess(ActionEvent e)
  {
    return MandelWindowAccess.Access.getMandelWindowAccess((Component)e.getSource());
  }
}