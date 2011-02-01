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

import java.awt.Color;

/**
 *
 * @author Uwe Krueger
 */
public class Colormaps {
  static public class Simple extends Colormap {
    private Color start;
    private Color end;
    
    public Simple(int size, Color start, Color end)
    { super(size,false);
      this.start=start;
      this.end=end;
      setup();
    }

    @Override
    public void setup()
    { int n=getSize()-2;
      
      int r=start.getRed();
      int dr=end.getRed()-r;
      int g=start.getGreen();
      int dg=end.getGreen()-g;
      int b=start.getBlue();
      int db=end.getBlue()-b;
      
      startModification();
      setColor(0,Color.BLACK);
      for (int i=0; i<=n; i++) {
        setColor(i+1,new Color(r+(dr*i/n),g+(dg*i/n),b+(db*i/n)));
      }
      endModification();
    }
    
  }
}
