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
 *  limitations under the License..
 */
package com.mandelsoft.mand.applet;

import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.ColormapModel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import com.mandelsoft.mand.image.MandelAreaImage;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.tool.History;
import com.mandelsoft.mand.tool.MandelImagePanel;
import com.mandelsoft.mand.tool.ToolEnvironment;
import com.mandelsoft.mand.tool.mapper.MapperModel;
import com.mandelsoft.swing.BufferedComponent;
import com.mandelsoft.util.Utils;
import java.awt.Window;

/**
 *
 * @author Uwe Krüger
 */
public class MandToolStarter extends JApplet
                                      {

  @Override
  public String[][] getParameterInfo()
  {
    String[][] info = {
      // Parameter Name       Kind of Value   Description
        {"datasource",       "URL",        "a directory containing the config"},
        {"showsub",          "boolean",    "initially show sub areas"},
        {"upstreamcolormap", "boolean",    "use upstream colormap as default"},
        {"tooltipmode",      "String",     "initial tool tip mode"}
    };
    return info;
  }

  @Override
  public void init()
  {
    
      //Execute a job on the event-dispatching thread:
      //creating this applet's GUI.
      //    this.getImage(getCodeBase(), "rightarrow.gif");
   
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run()
        {
          createGUI();
        }
      });
    }
    catch (Exception e) {
      System.err.println("createGUI didn't successfully complete: "+e);
      e.printStackTrace(System.err);
    }
  }

  private boolean getBooleanParameter(String name, boolean def)
  {
    String arg=getParameter(name);
    if (!Utils.isEmpty(arg)) return arg.toLowerCase().equals("true");
    return def;
  }

  private void createGUI()
  {
    try {
      String data=getParameter("datasource");
      String tooltipmode=getParameter("tooltipmode");
      URL base=getDocumentBase();
      boolean showsub=getBooleanParameter("showsub",false);
      boolean showtooltip=getBooleanParameter("showtooltip",false);
      boolean upstream=getBooleanParameter("upstreamcolormap",false);
      
      System.out.println("document base is "+base);
      System.out.println("data source is   "+data);
      System.out.println("show sub is      "+showsub);
      System.out.println("show tooltip     "+showtooltip);
      System.out.println("upstream is      "+upstream);
      System.out.println("tooltipmode is   "+tooltipmode);
      if (data==null) data=".";
      URL dataURL=new URL(base, data);
      ToolEnvironment env=new ToolEnvironment(null, dataURL);
      MandelAreaImage img=env.getMandelImage(env.getInitialName());
      MandToolStarter.this.showStatus("setup done");
      System.out.println("setup done");
      setup(env,img,showsub,showtooltip,upstream,tooltipmode);
    }
    catch (MalformedURLException ex) {
      System.out.println("url failed: "+ex);
      MandToolStarter.this.showStatus("url failed: "+ex);
    }
    catch (IllegalConfigurationException ic) {
      System.out.println("url failed: "+ic);
      MandToolStarter.this.showStatus("config failed: "+ic);
    }
    catch (IOException io) {
      System.out.println("initial image failed: "+io);
      MandToolStarter.this.showStatus("initial image failed: "+io);
    }
//    catch (Exception e) {
//      MandToolStarter.this.showStatus("failed: "+e);
//    }
    finally {
    }
  }

  @Override
  public void stop()
  {
    if (panel!=null) panel.cancel();
  }

  /////////////////////////////////////////////////////////////////////
  // there we are
  ////////////////////////////////////////////////////////////////////

  MandelImagePanel panel;

  public void setup(ToolEnvironment env, MandelAreaImage img,
                    boolean showsub,
                    boolean showtooltip,
                    boolean upstream,
                    String tooltipmode) throws IOException
  {
    BufferedComponent bc;
    panel=new MandelImagePanel(env, img, getWidth());
    add(panel);
    bc=panel.getImagePane();
    bc.setScaleMode(false);
    bc.setLimitWindowSize(false);
    if (showtooltip) panel.getPixelToolTipModel().setState(showtooltip);
    if (tooltipmode!=null) {
      if (!panel.getToolTipSelectionModel().setSelectedItemId(tooltipmode)) {
        showStatus("unknown tooltip mode "+tooltipmode);
        System.out.println("unknown tooltip mode "+tooltipmode);
      }
    }
    if (showsub) panel.showSubRects();
    panel.getParentColormapModel().setState(upstream);
  }

  public ColormapModel getColormapModel()
  {
    return panel.getColormapModel();
  }

  public ToolEnvironment getEnvironment()
  {
    return panel.getEnvironment();
  }

  public Window getMandelWindow()
  {
    return null;
  }

  public MandelData getMandelData()
  {
    return panel.getMandelData();
  }

  public MandelImage getMandelImage()
  {
    return panel.getMandelImage();
  }

  public MandelName getMandelName()
  {
    return panel.getMandelName();
  }

  public MapperModel getMapperModel()
  {
    return panel.getMapperModel();
  }

  public QualifiedMandelName getQualifiedName()
  {
    return panel.getQualifiedName();
  }

  public MandelImagePanel getMandelImagePane()
  {
    return panel;
  }

  public History getHistory()
  {
    return panel.getHistory();
  }
}
