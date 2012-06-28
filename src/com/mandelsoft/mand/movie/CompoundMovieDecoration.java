/*
 *  Copyright 2012 Uwe Krueger.
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

package com.mandelsoft.mand.movie;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Krueger
 */
public class CompoundMovieDecoration implements MovieDecoration {
  private List<MovieDecoration> decos;

  public CompoundMovieDecoration()
  {
    decos=new ArrayList<MovieDecoration>();
  }

  public void addMovieDecoration(MovieDecoration d)
  {
    decos.add(d);
  }

  public boolean isActive(long time)
  {
    for (MovieDecoration d:decos) if (d.isActive(time)) return true;
    return false;
  }

  public void paintDecoration(long time, Graphics g, int w, int h)
  {
    for (MovieDecoration d:decos) {
      d.paintDecoration(time, g, w, h);
    }
  }
}
