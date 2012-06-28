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

package com.mandelsoft.mand.calc;

import com.mandelsoft.mand.*;

/**
 *
 * @author Uwe Krueger
 */
public class OptimizedAreaCalculator extends AreaCalculator {

  protected void calc(PixelIterator pi, CalculationContext c)
  { int u;
    int sy=c.getSY();
    int sx=c.getSX();
    int ny=c.getNY();
    int nx=c.getNX();

      calcHLine(pi,c,sx,      sy,      nx);
      calcHLine(pi,c,sx,      sy+ny-1, nx);
      calcVLine(pi,c,sx,      sy+1,    ny-2);
    u=calcVLine(pi,c,sx+nx-1, sy+1,    ny-2);

    calcBox(pi,c, u, sx,sy,nx,ny);
  }

  private int calcHLine(PixelIterator pi, CalculationContext c,
                        int sx, int sy, int n)
  {
    pi.setX(sx);
    pi.setY(sy);
    int u=handle(pi,c, sx,sy);

    for (int x=sx+1; x<sx+n; x++) {
      pi.setX(x);
      int it=handle(pi,c, x,sy);
      if (it!=u) u=-1;
    }
    return u;
  }

  private int calcVLine(PixelIterator pi, CalculationContext c, int sx, int sy, int n)
  {
    pi.setX(sx);
    pi.setY(sy);
    int u=handle(pi,c, sx,sy);

    for (int y=sy+1; y<sy+n; y++) {
      pi.setY(y);
      int it=handle(pi,c, sx,y);
      if (it!=u) u=-1;
    }
    return u;
  }

  private void calcBox(PixelIterator pi, CalculationContext c,
                       int u, int sx, int sy, int nx, int ny)
  {
    //System.out.println("calcBox "+sx+","+sy+"("+nx+"x"+ny+")");
    if (nx<=2 || ny<=2)  return;

    if (u>=0) {
      u=checkHLine(c, u, sx, sy, nx);
      u=checkHLine(c, u, sx, sy+ny-1, nx);
      u=checkVLine(c, u, sx, sy+1, ny-2);
      u=checkVLine(c, u, sx+nx-1, sy+1, ny-2);
      if (u>=0) {
        fillBox(c, sx+1,sy+1,nx-2,ny-2,u);
        return;
      }
    }
    if (nx>ny) {
      // divide horizontally
      int s=(nx-1)/2;
      if (s!=0) {
        //System.out.println("s="+s);
        u=calcVLine(pi,c, sx+s,sy+1,ny-2);
        calcBox(pi,c, u,sx,sy,s+1,ny);
        calcBox(pi,c, u,sx+s,sy,nx-s,ny);
      }
    }
    else {
      // divide vertically
      int s=(ny-1)/2;
      if (s!=0) {
        u=calcHLine(pi,c, sx+1,sy+s,nx-2);
        calcBox(pi,c, u,sx,sy,nx,s+1);
        calcBox(pi,c, u,sx,sy+s,nx,ny-s);
      }
    }
  }

  private int checkHLine(CalculationContext c, int u, int sx, int sy, int n)
  {
    if (u>=0) for (int x=sx; x<sx+n; x++) {
      if (c.getDataRel(x,sy)!=u) return -1;
    }
    return u;
  }

  private int checkVLine(CalculationContext c, int u, int sx, int sy, int n)
  {
    if (u>=0) for (int y=sy; y<sy+n; y++) {
      if (c.getDataRel(sx,y)!=u) return -1;
    }
    return u;
  }

  private void fillBox(CalculationContext c, int sx, int sy, int nx, int ny, int u)
  {
    //System.out.println("fill "+sx+","+sy+"("+nx+"x"+ny+") with "+u);
    for (int y=sy; y<sy+ny; y++) {
      for (int x=sx; x<sx+nx; x++) {
        c.setDataRel(x,y,u);
      }
    }
    if (u==0) c.addMCnt(nx*ny);
  }
}
