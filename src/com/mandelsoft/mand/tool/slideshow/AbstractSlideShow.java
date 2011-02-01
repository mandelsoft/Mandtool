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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */

public abstract class AbstractSlideShow extends Timer
                                        implements ActionListener, SlideShow {
  private String id;
  protected SlideShowModel model;
  private List<SlideShowAction> supportedActions;

  public AbstractSlideShow(String id, int delay)
  {
    super(delay, null);
    addActionListener(this);
    this.supportedActions=new ArrayList<SlideShowAction>();
    this.id=id;
  }

  protected void addAction(SlideShowAction a)
  {
    supportedActions.add(a);
  }

  public void cancel()
  {
    if (debug) System.out.println("cancel "+getId());
    stop();
    model.setActive(null);
  }

  public SlideShowAction[] getActions()
  {
    return supportedActions.toArray(new SlideShowAction[supportedActions.size()]);
  }

  public String getId()
  {
    return id;
  }

  public void install(SlideShowModel model)
  {
    this.model=model;
  }

  public void startShow(MandelList list)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startShow(QualifiedMandelName start, QualifiedMandelName end)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
