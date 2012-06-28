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
package com.mandelsoft.mand.calc;

import com.mandelsoft.mand.*;

/**
 *
 * @author Uwe Krueger
 */
public class SimpleAreaCalculator extends AreaCalculator {

  ///////////////////////////////////////////////////////////////
  // calculation
  ///////////////////////////////////////////////////////////////

  protected void calc(PixelIterator pi, CalculationContext c)
  {
    int x,y,it;
    int sy=c.getSY();
    int sx=c.getSX();
    int ny=c.getNY();
    int nx=c.getNX();
    for (y=0; y<ny; y++) {
      // System.out.println("  line "+y);
      pi.setY(y+sy);
      for (x=0; x<nx; x++) {
        pi.setX(x+sx);
        handle(pi,c,x,y);
      }
    }
  }
}

