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
public abstract class AreaCalculator {
  int kept=0;
  
  ///////////////////////////////////////////////////////////////
  // calculation
  ///////////////////////////////////////////////////////////////

  public final void calc(CalculationContext c)
  { PixelIterator pi=c.getPixelIterator();
    
    if (c.getMinIt()==0) c.setMinIt(c.getLimitIt()); // not yet calculated
    c.createData();
    long time=System.currentTimeMillis();
    calc(pi,c);
    c.setMTime(System.currentTimeMillis()-time);
  }

  protected abstract void calc(PixelIterator pi, CalculationContext c);
  
  protected int handle(PixelIterator pi, CalculationContext c, int x, int y)
  { int it=c.getDataRel(x,y);

    if (it==0) {
      if (kept>0) {
        System.out.printf("kept %d points\n", kept);
        kept=0;
      }
      it=c.incorporateIteration(x, y, pi.iter());
    }
    else { // keep old iteration value for refinement mode
      kept++;
      c.addNumIt(it);
    }
    return it;
  }
}

