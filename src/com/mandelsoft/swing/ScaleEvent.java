
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

import java.util.EventObject;

/**
 *
 * @author Uwe Krüger
 */

public class ScaleEvent extends EventObject {
  private Scale newScale;
  private Scale oldScale;

  public ScaleEvent(Object source, Scale newScale, Scale oldScale)
  {
    super(source);
    this.newScale=newScale;
    this.oldScale=oldScale;
  }
   
  public ScaleEvent(Object source, double scalex, double scaley,
                                   double oldx, double oldy)
  {
    this(source, new Scale(scalex,scaley), new Scale(oldx,oldy));
  }

  public Scale getOld()
  {
    return oldScale;
  }
  
  public Scale getNew()
  {
    return newScale;
  }
  
  public double getOldX()
  {
    return oldScale.getX();
  }

  public double getOldY()
  {
    return oldScale.getY();
  }

  public double getScaleX()
  {
    return newScale.getX();
  }

  public double getScaleY()
  {
    return newScale.getY();
  }
}
