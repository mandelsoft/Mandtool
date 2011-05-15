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

import java.awt.event.ActionEvent;
import java.util.Random;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */
public class ImageShow extends AbstractSlideShow  {

  private MandelList list;
  private Random random;

  ImageShow()
  {
    super("Image Show",5000);
    this.random=new Random();
    addAction(new ShowAction());
  }

  @Override
  public void startShow(MandelList list)
  {
    if (list.size()>0) {
      model.setActive(this);
      this.list=list;
      start();
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    int index=random.nextInt();
    index=(index*Integer.signum(index))%list.size();
    System.out.println("show index "+index);
    model.show(list.get(index));
    start();
  }

  /////////////////////////////////////////////////////////////////////////
  // supported actions
  /////////////////////////////////////////////////////////////////////////

  private class ShowAction extends SlideShowActionBase {

    protected ShowAction(String name)
    {
      super(name, LIST);
    }

    public ShowAction()
    {
      this("Image Show");
    }

    public void actionPerformed(ActionEvent e)
    {
      SlideShowSource.ListMode m=model.getSource(e).getListMode(model);
      if (m!=null) {
        MandelList l=m.getMandelList(model);
        if (l!=null) startShow(l);
      }
    }
  }
}
