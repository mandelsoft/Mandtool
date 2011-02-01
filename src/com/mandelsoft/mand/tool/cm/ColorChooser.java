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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Locale;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.tool.ColorListModel;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.colorchooser.ColorImageModel;
import com.mandelsoft.swing.colorchooser.HSBChooserPanel;
import com.mandelsoft.swing.colorchooser.ImageChooserPanel;
import com.mandelsoft.swing.colorchooser.RGBChooserPanel;
import com.mandelsoft.swing.colorchooser.SwatchChooserPanel;
import com.mandelsoft.swing.colorchooser.SwatchChooserPanel.UserColorListener;
import com.mandelsoft.swing.colorchooser.SwatchChooserPanel.UserColorSource;

/**
 *
 * @author Uwe Krueger
 */
public class ColorChooser extends JDialog {
  private boolean preview=false;
  private GBC colorSampleGBC;
  private SampleComponent colorSample;
  private ImageChooserPanel imagePanel;
  private SwatchChooserPanel swatchPanel;
  private JColorChooser cc;
  private ColorListModel colors;
  private ChangeListener changeListener;

  public ColorChooser(Window c)
  {
    super(c);
    setup();
  }

  public ColorChooser()
  {
    setup();
  }

  private void setup()
  {
    if (getOwner() instanceof MandelWindowAccess) {
      colors=((MandelWindowAccess)getOwner()).getEnvironment().getColorsModel();
    }
    cc=new JColorChooser();
    
    if (!preview) {
      setLayout(new GridBagLayout());
      colorSampleGBC = new GBC(0,0).setInsets(10,10,10,10)
                                             .setFill(GBC.HORIZONTAL);
      cc.setPreviewPanel(new JPanel());
    }
    
    cc.setLocale(Locale.UK);
    cc.getSelectionModel().addChangeListener(new ColorListener());

    AbstractColorChooserPanel[] old=cc.getChooserPanels();
    AbstractColorChooserPanel keep=null;
    for (int i=0; i<old.length; i++) {
      //System.out.println("CHOOSER: "+old[i]);
      if (old[i].getClass().getName().equals("javax.swing.colorchooser.DefaultRGBChooserPanel")) {
        keep=old[i];
      }
      cc.removeChooserPanel(old[i]);
    }
    //cc.setChooserPanels(new AbstractColorChooserPanel[]{});
    cc.addChooserPanel(new RGBChooserPanel());
    cc.addChooserPanel(new HSBChooserPanel());
    cc.addChooserPanel(swatchPanel=new SwatchChooserPanel(new UserColorSource() {
        @Override
        public Color getUserColor(int i)
        {
          if (colors!=null) {
            if (i<colors.getList().size()) {
              return colors.getList().get(i);
            }
          }
          return null;
        }
      }));
    cc.addChooserPanel(imagePanel=new ImageChooserPanel());
    if (keep!=null) {
      cc.addChooserPanel(keep);
    }

    if (colors!=null) {
//      for (Color c:colors.getList()) {
//        swatchPanel.addUserColor(c);
//      }
      swatchPanel.addColorListener(new UserColorListener() {
        public void colorAdded(Color c)
        {
          colors.addColor(0,c);
        }
        public void colorRemoved(Color c)
        {
          colors.removeColor(c);
        }
      });
      //System.out.println("connect chooser to colors");
      colors.addChangeListener(changeListener=new ChangeListener() {
        public void stateChanged(ChangeEvent e)
        {
          ColorListModel.ListEvent l=(ColorListModel.ListEvent)e;
          if (l.getMode()==ColorListModel.ListEvent.ADD) {
            swatchPanel.addUserColor(l.getColor());
          }
          else if (l.getMode()==ColorListModel.ListEvent.ADD) {
            swatchPanel.removeUserColor(l.getColor());
          }
        }
      });
    }

    if (!preview) add(cc, new GBC(0,1));
    else add(cc);
    setSampleComponent(new ColorSample(200,20));

    pack();
  }

  @Override
  public void dispose()
  {
    super.dispose();
    if (colors!=null&&changeListener!=null) {
      //System.out.println("disconnect chooser to colors");
      colors.removeChangeListener(changeListener);
    }
  }

  public ColorImageModel getColorImageModel()
  {
    return imagePanel.getColorImageModel();
  }

  public void setColorImageModel(ColorImageModel m)
  {
    imagePanel.setColorImageModel(m);
  }

  public void setSampleComponent(SampleComponent sample)
  {
      if (colorSample!=null) {
        if (!preview) remove(colorSample);
      }
      this.colorSample=sample;
      sample.setColor(getColor());

      if (preview) cc.setPreviewPanel(sample);
      else add(colorSample,colorSampleGBC);
  }

  public SampleComponent getSampleComponent(SampleComponent c)
  {
    return colorSample;
  }

  public void setColor(Color color)
  {
    cc.setColor(color);
  }

  public ColorSelectionModel getSelectionModel()
  {
    return cc.getSelectionModel();
  }

  public Color getColor()
  {
    return cc.getColor();
  }

  private class ColorListener implements ChangeListener {
    public void stateChanged(ChangeEvent e)
    {
      ColorSelectionModel m=(ColorSelectionModel)e.getSource();
      colorSample.setColor(m.getSelectedColor());
    }
  }

  //////////////////////////////////////////////////////////////////////
  // color sample
  //////////////////////////////////////////////////////////////////////

  public static abstract class SampleComponent extends JComponent {
    public abstract void setColor(Color c);
  }

  public static class ColorSample extends SampleComponent {
    private Color color;
    private int width;
    private int height;

    public ColorSample(int width, int height)
    {
      setBorder(new BevelBorder(BevelBorder.RAISED));
      setSize(width, height);
      setPreferredSize(new Dimension(width, height));
      this.width=width;
      this.height=height;
    }

    public void setColor(Color c)
    {
      this.color=c;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
      g.setColor(color);
      Insets o=getInsets();
      g.fillRect(o.left, o.top, getWidth()-o.left-o.right,
                 getHeight()-o.bottom-o.top);
    }
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
        JDialog frame=new ColorChooser();


        frame.setVisible(true);
      }
    });
  }
}
