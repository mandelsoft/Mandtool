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

import com.mandelsoft.mand.tool.Decoration;
import java.awt.Graphics;

/**
 *
 * @author Uwe Krueger
 */
public class SimpleMovieDecoration extends FadedMovieDecoration {
  private Decoration deco;
 

  public SimpleMovieDecoration(Decoration deco, long start, long fadein,
                                          long show, long fadeout)
  {
    super(start,fadein,show,fadeout);
    this.deco=deco;
  }

  protected boolean decoValid()
  {
    if (deco!=null) {
      deco.setAlpha(255);
      return deco.showDecoration();
    }
    return false;
  }


  @Override
  protected void decoPaint(Graphics g, int w, int h)
  {
    deco.paintDecoration(g, w, h);
  }

  @Override
  protected void decoSetAlpha(int alpha)
  {
    deco.setAlpha(alpha);
  }
}
