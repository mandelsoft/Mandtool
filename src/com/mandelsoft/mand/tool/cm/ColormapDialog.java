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
package com.mandelsoft.mand.tool.cm;

import com.mandelsoft.mand.cm.ColormapModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.Colormaps;
import com.mandelsoft.mand.tool.MandelDialog;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.swing.Dimensions;
import com.mandelsoft.swing.ScaleAdapter;
import com.mandelsoft.swing.ScaleEvent;


/**
 *
 * @author Uwe Krüger
 */

public class ColormapDialog extends MandelDialog {
  public static boolean debug=false;

  private ColormapComponent component;
  private JScrollPane       scrollpane;
  private String            name;

  private boolean           adjusting;

  public ColormapDialog(MandelWindowAccess owner)
  { this(owner,"");
  }

  public ColormapDialog(MandelWindowAccess owner, String name)
  { this(owner,name,null);
  }

  public ColormapDialog(MandelWindowAccess owner, String name, ColormapModel model)
  { super(owner);
    this.name=name;
    setup(owner,model);
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e)
      {
        if (debug) System.out.println("DRS: "+e);
        if (!adjusting) setInitialSize();
      }
    });
  }

  public ColormapDialog(MandelWindowAccess owner, String name, Colormap map, boolean modifiable)
  { this(owner, name);
    setColormap(map, modifiable);
  }

  private void setup(MandelWindowAccess owner, ColormapModel model)
  {
    component=new ColormapComponent(owner==null?null:owner.getMandelWindow(),model);
    component.addScaleEventListener(new ScaleAdapter() {
      @Override
      public void componentScaled(ScaleEvent e)
      {
        setTitle();
      }
    });
    scrollpane=new JScrollPane(component);
    Dimension d=getLimit();
    if (debug) System.out.println("MAXIMUM="+d);
    setMaximumSize(d);
    scrollpane.setMaximumSize(d);
    scrollpane.getViewport().setMaximumSize(d);
    scrollpane.setBorder(null);
    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    p.add(scrollpane);
    p.setMaximumSize(d);
    add(p);
    //component.setLimitWindowSize(true);
    setInitialSize();
    setTitle();
  }

  private void setTitle()
  { String scale="";
    if (Math.round(component.getScaleX()/ColormapComponent.COL_X*100)!=100.0) {
      scale=" ["+Math.round(component.getScaleX()/ColormapComponent.COL_X*100)+"%]";
    }
    setTitle("Colormap "+name+scale);
  }

  public void hightLight(int ix)
  {
    component.highLight(ix);
  }

  public void setColormap(Colormap colormap, boolean modifiable)
  {
    component.getColormapModel().setColormap(colormap);
    component.getColormapModel().setModifiable(modifiable);
  }

  @Override
  public void setVisible(boolean b)
  {
    if (b) {
      setInitialSize();
    }
    super.setVisible(b);
  }

  @Override
  public void pack()
  {
    super.pack();
    setInitialSize();
  }

  public Dimension getLimit()
  {
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
    Insets insets=getInsets();
    Dimensions.mod.sub(d,insets.left+insets.right,insets.top+insets.bottom);
    Dimensions.mod.sub(d,10,10);
    return d;
  }

  public void setInitialSize()
  {
    Dimension id=component.getPreferredSize();
    if (debug) System.out.println("preferred: "+id);
    Dimension sd=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension d=new Dimension(id);
    Insets insets=getInsets();
    Dimensions.mod.add(d,insets.left+insets.right,insets.top+insets.bottom);
    Dimensions.mod.sub(sd,10,10);
    Dimension od=new Dimension(d);
    Dimensions.mod.limit(d,sd);
    adjusting=true;
    if (d.getWidth()<od.getWidth()) {
      int h=(int)scrollpane.getHorizontalScrollBar().getMaximumSize().getHeight();
      if (debug) System.out.println("bar="+h);
      d.setSize(d.getWidth(), d.getHeight()+h);
    }
    if (debug) System.out.println("*set size "+d);
    setSize(d);
    adjusting=false;
//    revalidate();
  }

  public void revalidate()
  {
    component.revalidate();
  }

  //////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    //Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
    SwingUtilities.invokeLater(new Runnable() {

      public void run()
      {
        JFrame frame=new TestFrame();

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });
  }

  static class TestFrame extends JFrame {
    ColormapComponent cc;

    TestFrame()
    {
      setup();
    }

    void setup()
    { cc=new ColormapComponent(null);
      cc.getColormapModel().setColormap(new Colormaps.Simple(256,Color.BLUE,Color.WHITE));
      cc.getColormapModel().setModifiable(true);
      add(cc);
      pack();
      setResizable(false);
    }
  }
}
