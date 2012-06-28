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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Krueger
 */
public class VerticalListMovieDecoration extends FadedMovieDecoration {
  private List<Decoration> decos;
  private boolean prepared;
  private int gap;

  public VerticalListMovieDecoration(int gap, long start, long fadein,
                                              long show, long fadeout)
  {
    super(start,fadein,show,fadeout);
    decos=new ArrayList<Decoration>();
    this.gap=gap;
  }

  public void addDecoration(Decoration d)
  {
    decos.add(d);
  }

  protected void prepare()
  {
    if (!prepared) {
      double cnt=decos.size();
      double o=-(cnt-1)/2;
      for (Decoration d:decos) {
        int offset=(int)(o++*gap);
        d.setVInset(offset+d.getVInset());
        //System.out.println("offset "+(o-1)+": "+offset+":"+d.getVInset());
      }
      prepared=true;
    }
  }

  @Override
  public void paintDecoration(long time, Graphics g, int w, int h)
  {
    prepare();
    super.paintDecoration(time,g,w,h);
  }

  ///////////////////////////////////////////////////////////////
   protected boolean decoValid()
  {
    if (!decos.isEmpty()) {
      for (Decoration deco:decos) {
        deco.setAlpha(255);
        if (deco.showDecoration()) return true;
      }
    }
    System.out.println("list invalid");
    return false;
  }

  @Override
  protected void decoPaint(Graphics g, int w, int h)
  {
    for (Decoration deco:decos)
      deco.paintDecoration(g, w, h);
  }

  @Override
  protected void decoSetAlpha(int alpha)
  {
    System.out.println("list set alpha "+alpha);
    for (Decoration deco:decos)
      deco.setAlpha(alpha);
  }
}
