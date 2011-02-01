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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 *
 * @author Uwe Krueger
 */
public class GBCPanel extends GBCSupportPanel {
  static boolean debug=false;

  private GridBagLayout layout;

  public GBCPanel()
  {
    setLayout(layout=new GridBagLayout());
  }

  public JComponent addBorder(int col, int row, int colspan, int rowspan)
  {
    return addBorder(col, row, colspan, rowspan, false);
  }

  protected void adjustBorderArea(Rectangle rect)
  {
  }

  public JComponent addBorder(int col, int row, int colspan, int rowspan,
          boolean raise)
  {
    return addBorder(col,row,colspan,rowspan,raise,null);
  }

  public JComponent addBorder(int col, int row, int colspan, int rowspan,
                        boolean raise, String title)
  {
    JComponent b=new JPanel();
    Border border=new BevelBorder(raise?BevelBorder.RAISED:BevelBorder.LOWERED);
    if (title!=null)
      border=BorderFactory.createTitledBorder(border,title);
    b.setBorder(border);
    b.setOpaque(true);
    int offset=10;
    // adjust
    Rectangle rect=new Rectangle(col,row,colspan,rowspan);
    adjustBorderArea(rect);
    int gcol=(int)rect.getX();
    int grow=(int)rect.getY();
    int gcs=(int)rect.getWidth();
    int grs=(int)rect.getHeight();
    double wx=0;
    double wy=0;
    if (debug) System.out.println("border "+gcol+"/"+grow+" "+gcs+"/"+grs);
    // update insets
    Component[] comps=getComponents();
    if (comps!=null)
      for (Component c:comps) {
        GridBagConstraints cs=layout.getConstraints(c);
        boolean mod=false;
        if (isLine(cs.gridx, cs.gridy, gcol, gcs, grow)) {
          if (debug) System.out.println("adjust top "+cs.gridx+"/"+cs.gridy);
          cs.insets.top+=offset;
          mod=true;
        }
        if (isLine(cs.gridx, cs.gridy+cs.gridheight-1, gcol, gcs, grow+grs-1)) {
          if (debug) System.out.println("adjust bottom "+cs.gridx+"/"+cs.gridy);
          cs.insets.bottom+=offset;
          mod=true;
        }
        if (isLine(cs.gridy, cs.gridx, grow, grs, gcol)) {
          if (debug) System.out.println("adjust left "+cs.gridx+"/"+cs.gridy);
          cs.insets.left+=offset;
          mod=true;
        }
        if (isLine(cs.gridy, cs.gridx+cs.gridwidth-1, grow, grs, gcol+gcs-1)) {
          if (debug) System.out.println("adjust right "+cs.gridx+"/"+cs.gridy);
          cs.insets.right+=offset;
          mod=true;
        }
        if (isInBox(cs.gridx, cs.gridy, gcol, gcs, grow, grs)) {
          if (wx<cs.weightx) {
            if (debug) System.out.println("adjust weightx "+cs.weightx);
            wx=cs.weightx;
          }
          if (wy<cs.weighty) {
            if (debug) System.out.println("adjust weighty "+cs.weighty);
            wy=cs.weighty;
          }
        }
        if (mod) layout.setConstraints(c, cs);
      }
    // add border
    add(b, new GBC(gcol, grow, gcs, grs).setFill(GBC.BOTH)
                                        .setWeight(wx, wy));
    return b;
  }

  private boolean isLine(int x, int y, int sx, int nx, int sy)
  {
    if (y!=sy) return false;
    if (x<sx||x>=sx+nx) return false;
    return true;
  }

  private boolean isInBox(int x, int y, int sx, int nx,
                                        int sy, int ny)
  {
    if (x<sx || x>sx+nx) return false;
    if (y<sy || y>sy+ny) return false;
    return true;
  }
}
