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
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krueger
 */
public class SlideShowSourceAdapter implements SlideShowSource {

  public static class OneModeAdapter implements OneMode {
    public QualifiedMandelName getSingleName(SlideShowModel model)
    {
      return null;
    }
  }

  public static class TwoModeAdapter implements TwoMode {
    public QualifiedMandelName getFirstName(SlideShowModel model)
    {
      return null;
    }

    public QualifiedMandelName getSecondName(SlideShowModel model)
    {
      return null;
    }
  }

  public static class ListModeAdapter implements ListMode {
    public MandelList getMandelList(SlideShowModel model)
    {
      return null;
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  protected OneMode oneMode;
  protected TwoMode twoMode;
  protected ListMode listMode;

  public SlideShowSourceAdapter(OneMode o, TwoMode t, ListMode l)
  {
    this.oneMode=o;
    this.twoMode=t;
    this.listMode=l;
  }

  public SlideShowSourceAdapter(OneMode o, TwoMode t)
  {
    this.oneMode=o;
    this.twoMode=t;
  }

  public SlideShowSourceAdapter(ListMode l)
  {
    this.listMode=l;
  }

  public SlideShowSourceAdapter()
  {
  }

  public int getSourceMode(SlideShowModel model, boolean generic)
  {
    QualifiedMandelName n1,n2;
    int mode=SlideShowAction.NONE;

    if (supportsListMode(model, generic)) {
      mode|=SlideShowAction.LIST;
    }
    if (supportsOneMode(model, generic)) {
      mode|=SlideShowAction.ONE;
    }
    if (supportsTwoMode(model, generic)) {
      mode|=SlideShowAction.TWO;
    }
    //System.out.println((generic?"generic ":"")+"slideshow mode "+mode);
    return mode;
  }

  protected boolean supportsListMode(SlideShowModel model, boolean generic)
  {
    ListMode lm=getListMode(model);
    return lm!=null && (generic || lm.getMandelList(model)!=null);
  }

  protected boolean supportsOneMode(SlideShowModel model, boolean generic)
  {
    QualifiedMandelName n1;
    OneMode om=getOneMode(model);
    return om!=null && (generic || ((n1=om.getSingleName(model))!=null &&
                                    !n1.isRoot()));
  }

  protected boolean supportsTwoMode(SlideShowModel model, boolean generic)
  {
    QualifiedMandelName n1,n2;
    TwoMode tm=getTwoMode(model);
    return (tm!=null && (generic || ((n1=tm.getFirstName(model))!=null &&
                                     (n2=tm.getSecondName(model))!=null &&
                                      !n1.equals(n2))));
  }

  public ListMode getListMode(SlideShowModel model)
  {
    return listMode;
  }

  public OneMode getOneMode(SlideShowModel model)
  {
    return oneMode;
  }

  public TwoMode getTwoMode(SlideShowModel model)
  {
    return twoMode;
  }
}
