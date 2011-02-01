
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
  private double scalex;
  private double scaley;
  private double oldx;
  private double oldy;

  public ScaleEvent(Object source, double scalex, double scaley,
                                   double oldx, double oldy)
  {
    super(source);
    this.scalex=scalex;
    this.scaley=scaley;
    this.oldx=oldx;
    this.oldy=oldy;
  }

  public double getOldX()
  {
    return oldx;
  }

  public double getOldY()
  {
    return oldy;
  }

  public double getScaleX()
  {
    return scalex;
  }

  public double getScaleY()
  {
    return scaley;
  }
}
