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
package com.mandelsoft.mand.cm;

import java.util.EventObject;

/**
 *
 * @author Uwe Krüger
 */

public class InterpolationPointEvent extends EventObject {
  static public final int IPE_ADDED            = 1;
  static public final int IPE_DELETED          = 2;
  static public final int IPE_MOVED            = 3;
  static public final int IPE_COLOR_CHANGED    = 4;
  static public final int IPE_NEIGHBOR_CHANGED = 5;
  static public final int IPE_CHANGED          = 6;

  private int id;

  public InterpolationPointEvent(InterpolationPoint ip, int id)
  { super(ip);
    this.id=id;
  }

  @Override
  public InterpolationPoint getSource()
  { return (InterpolationPoint)super.getSource();
  }

  public ColormapModel getColormapModel()
  { return getSource().getModel();
  }

  public int getId()
  { return id;
  }
}
