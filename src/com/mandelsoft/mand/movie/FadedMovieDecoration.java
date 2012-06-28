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

/**
 *
 * @author Uwe Krueger
 */
public abstract class FadedMovieDecoration implements MovieDecoration {
  static public final boolean debug=false;
  
  private long start;
  private long fadein;
  private long show;
  private long fadeout;

  private long end;

  public FadedMovieDecoration(long start, long fadein,
                              long show, long fadeout)
  {
    this.start=start;
    this.fadein=fadein;
    this.show=show;
    this.fadeout=fadeout;

    setup();
  }

  protected void setup()
  {
    if (show==Long.MAX_VALUE) end=Long.MAX_VALUE;
    else end=start+fadein+show+fadeout;
  }

  public long getEnd()
  {
    return end;
  }

  public long getStart()
  {
    return start;
  }

  public long getFadeInTime()
  {
    return fadein;
  }

  public long getFadeOutTime()
  {
    return fadeout;
  }

  public long getShowTime()
  {
    return show;
  }

  public void setFadeInTime(long fadein)
  {
    this.fadein=fadein;
    setup();
  }

  public void setFadeOutTime(long fadeout)
  {
    this.fadeout=fadeout;
    setup();
  }

  public void setShowTime(long show)
  {
    this.show=show;
    setup();
  }

  public void setStart(long start)
  {
    this.start=start;
    setup();
  }

  protected abstract boolean decoValid();

  public boolean isActive(long time)
  {
    if (time>=start && time<end) {
      return decoValid();
    }
    return false;
  }

  public void paintDecoration(long time, Graphics g, int w, int h)
  {
    if (isActive(time)) {
      prepareDecoration(time);
      decoPaint(g, w, h);
    }
  }

  protected abstract void decoPaint(Graphics g, int w, int h);

  private void prepareDecoration(long time)
  {  int alpha;

     if (time<start+fadein) {
       alpha=(int)((time-start)*256/fadein);
       if (debug) System.out.println(""+time+" fade in "+alpha);
     }
     else if(end==Long.MAX_VALUE || time<start+fadein+show) {
       alpha=255;
       if (debug) System.out.println(""+time+" show "+alpha);
     }
     else {
       alpha=255-(int)((time-(start+fadein+show))*256/fadeout);
       if (debug) System.out.println(""+time+" fade out "+alpha);
     }
     decoSetAlpha(alpha);
  }

  protected abstract void decoSetAlpha(int alpha);

}
