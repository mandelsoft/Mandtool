
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
package com.mandelsoft.mand.tool;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.util.MandArith;
import com.mandelsoft.swing.NumberField;

/**
 *
 * @author Uwe Krüger
 */

public class AltSpec extends MandelSpecDialog<AltSpec.AltPanel> {

  public AltSpec(Window parent, String name, boolean change)
  {
    super(parent, "Alternate Area Specification",name,change);
  }

  /////////////////////////////////////////////////////////////////////////
 
  @Override
  protected AltPanel createPanel(String name, boolean change)
  {
    return new AltPanel(name,change);
  }

  /////////////////////////////////////////////////////////////////////////

  public BigDecimal getYmin()
  {
    return getPanel().getYmin();
  }

  public BigDecimal getYmax()
  {
    return getPanel().getYmax();
  }

  public BigDecimal getXmin()
  {
    return getPanel().getXmin();
  }

  public BigDecimal getXmax()
  {
    return getPanel().getXmax();
  }

  public class AltPanel extends MandelSpecDialog.Panel {
    private NumberField xmin;
    private NumberField xmax;
    private NumberField ymin;
    private NumberField ymax;

    public AltPanel(String name, boolean change)
    {
      super(name,change);
      this.updateListener=new UpdateListener();
      xmin=createNumberField("xmin",1);
      xmax=createNumberField("xmax",2);
      ymin=createNumberField("ymin",3);
      ymax=createNumberField("ymax",4);
      addBorder(0,1,2,2);
      addBorder(0,3,2,2);
    }

    @Override
    protected void _setInfo(MandelInfo info)
    {
      xmin.setValue(MandArith.sub(info.getXM(),
                     MandArith.div(info.getDX(),2.0)));
      xmax.setValue(MandArith.add(info.getXM(),
                     MandArith.div(info.getDX(),2.0)));
      ymin.setValue(MandArith.sub(info.getYM(),
                     MandArith.div(info.getDY(),2.0)));
      ymax.setValue(MandArith.add(info.getYM(),
                     MandArith.div(info.getDY(),2.0)));
    }

    @Override
    public boolean updateInfo(MandelInfo info)
    { BigDecimal t;
      BigDecimal xa=getXmax();
      BigDecimal xi=getXmin();
      BigDecimal ya=getYmax();
      BigDecimal yi=getYmin();

      if (xa.compareTo(xi)<0) {
        t=xa;
        xa=xi;
        xi=t;
      }
      if (ya.compareTo(yi)<0) {
        t=ya;
        ya=yi;
        yi=t;
      }

      info.setXM(MandArith.div(MandArith.add(xi, xa),2.0));
      info.setYM(MandArith.div(MandArith.add(yi, ya),2.0));
      info.setDX(MandArith.sub(xa, xi));
      info.setDY(MandArith.sub(ya, yi));
      return true; // TODO: check for modification
    }

    @Override
    public void setEditable(boolean b)
    {
      super.setEditable(b);
      xmin.setEditable(b);
      xmax.setEditable(b);
      ymin.setEditable(b);
      ymax.setEditable(b);
    }

    ///////////////////////////////////////////////////////////////////////
    public BigDecimal getXmin()
    {
      return (BigDecimal)xmin.getValue();
    }

    public BigDecimal getXmax()
    {
      return (BigDecimal)xmax.getValue();
    }

    public BigDecimal getYmin()
    {
      return (BigDecimal)ymin.getValue();
    }

    public BigDecimal getYmax()
    {
      return (BigDecimal)ymax.getValue();
    }

    private class UpdateListener implements PropertyChangeListener {
      synchronized
      public void propertyChange(PropertyChangeEvent evt)
      {
        //System.out.println("spec changed (inup="+inupdate+")");
        if (!inupdate) {
          inupdate=true;
          BigDecimal xa=getXmax();
          BigDecimal xi=getXmin();
          BigDecimal ya=getYmax();
          BigDecimal yi=getYmin();

//          System.out.println("xmax="+xa+
//                             "xmin="+xi+
//                             "ymax="+ya+
//                             "ymin="+yi);
          if (xa.compareTo(xi)<0) {
            xmax.setValue(xi);
            xmin.setValue(xa);
          }
          if (ya.compareTo(yi)<0) {
            ymax.setValue(yi);
            ymin.setValue(ya);
          }
          fireChangeEvent();
          inupdate=false;
        }
      }
    }
  }
}
