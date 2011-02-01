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

import java.awt.GridBagConstraints;
import java.awt.Insets;


/**
 * This class simplifies the use of the GridBagConstraints class.
 * @version 1.01 2004-05-06
 * @author Cay Horstmann
 */
public class GBC extends GridBagConstraints
{
   /**
    * Constructs a GBC with a given gridx and gridy position and all other grid
    * bag constraint values set to the default.
    * @param gridx the gridx position
    * @param gridy the gridy position
    */
   public GBC(int gridx, int gridy)
   {
      this.gridx = gridx;
      this.gridy = gridy;
   }

   /**
    * Constructs a GBC with given gridx, gridy, gridwidth, gridheight and all
    * other grid bag constraint values set to the default.
    * @param gridx the gridx position
    * @param gridy the gridy position
    * @param gridwidth the cell span in x-direction
    * @param gridheight the cell span in y-direction
    */
   public GBC(int gridx, int gridy, int gridwidth, int gridheight)
   {
      this.gridx = gridx;
      this.gridy = gridy;
      setSpan(gridwidth, gridheight);
   }

   /**
    * Sets the Grid span.
    * @param gridwidth the width span 
    * @param gridheight the height span 
    * @return this object for further modification
    */
   public GBC setSpan(int gridwidth, int gridheight)
   {
     this.gridwidth=gridwidth;
     this.gridheight = gridheight;
     return this;
   }

   /**
    * Sets the Grid span for width.
    * @param gridwidth the width span
    * @return this object for further modification
    */
   public GBC setSpanW(int gridwidth)
   {
     this.gridwidth=gridwidth;
     return this;
   }

   /**
    * Sets the Grid span for height.
    * @param gridheight the height span
    * @return this object for further modification
    */
   public GBC setSpanH(int gridheight)
   {
     this.gridheight = gridheight;
     return this;
   }

   public GBC setLayout(int fill, int anchor)
   {
     this.fill = fill;
     this.anchor = anchor;
     return this;
   }
   /**
    * Sets the anchor.
    * @param anchor the anchor value
    * @return this object for further modification
    */
   public GBC setAnchor(int anchor)
   {
      this.anchor = anchor;
      return this;
   }

   /**
    * Sets the fill direction.
    * @param fill the fill direction
    * @return this object for further modification
    */
   public GBC setFill(int fill)
   {
      this.fill = fill;
      return this;
   }

   /**
    * Sets the cell weights.
    * @param weightx the cell weight in x-direction
    * @param weighty the cell weight in y-direction
    * @return this object for further modification
    */
   public GBC setWeight(double weightx, double weighty)
   {
      this.weightx = weightx;
      this.weighty = weighty;
      return this;
   }

   /**
    * Sets the insets of this cell.
    * @param distance the spacing to use in all directions
    * @return this object for further modification
    */
   public GBC setInsets(int distance)
   {
      this.insets = new Insets(distance, distance, distance, distance);
      return this;
   }

   /**
    * Sets the top inset of this cell.
    * @param distance the spacing to use for top
    * @return this object for further modification
    */
   public GBC setTopInset(int distance)
   {
      if (this.insets==null) this.insets = new Insets(0,0,0,0);
      this.insets.top=distance;
      return this;
   }

   /**
    * Sets the bottom inset of this cell.
    * @param distance the spacing to use for bottom
    * @return this object for further modification
    */
   public GBC setBottomInset(int distance)
   {
      if (this.insets==null) this.insets = new Insets(0,0,0,0);
      this.insets.bottom=distance;
      return this;
   }

     /**
    * Sets the right inset of this cell.
    * @param distance the spacing to use for right
    * @return this object for further modification
    */
   public GBC setRightInset(int distance)
   {
      if (this.insets==null) this.insets = new Insets(0,0,0,0);
      this.insets.right=distance;
      return this;
   }
   
   /**
    * Sets the left inset of this cell.
    * @param distance the spacing to use for left
    * @return this object for further modification
    */
   public GBC setLeftInset(int distance)
   {
      if (this.insets==null) this.insets = new Insets(0,0,0,0);
      this.insets.left=distance;
      return this;
   }

  /**
    * Sets the insets of this cell.
    * @param top the spacing to use on top
    * @param left the spacing to use to the left
    * @param bottom the spacing to use on the bottom
    * @param right the spacing to use to the right
    * @return this object for further modification
    */
   public GBC setInsets(int top, int left, int bottom, int right)
   {
      this.insets = new Insets(top, left, bottom, right);
      return this;
   }

   /**
    * Sets the internal padding
    * @param ipadx the internal padding in x-direction
    * @param ipady the internal padding in y-direction
    * @return this object for further modification
    */
   public GBC setIpad(int ipadx, int ipady)
   {
      this.ipadx = ipadx;
      this.ipady = ipady;
      return this;
   }
}
