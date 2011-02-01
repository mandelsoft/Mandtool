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

import java.awt.geom.Rectangle2D;

/**
 *
 * @author Uwe Krueger
 */
public interface IntervalAccess {

  double getMin(Rectangle2D r);
  double getMax(Rectangle2D r);
  Interval2D getInterval(Rectangle2D r);

  public static class acc {
    public abstract static class AccessBase implements IntervalAccess {
      public Interval2D getInterval(Rectangle2D r)
      {
        return new Interval2D(getMin(r),getMax(r));
      }
    }

    public static final IntervalAccess X=new AccessBase() {

      public double getMax(Rectangle2D r)
      {
        return r.getMaxX();
      }

      public double getMin(Rectangle2D r)
      {
        return r.getMinX();
      }
    };

    public static final IntervalAccess Y=new AccessBase() {

      public double getMax(Rectangle2D r)
      {
        return r.getMaxY();
      }

      public double getMin(Rectangle2D r)
      {
        return r.getMinY();
      }
    };
  }
}
