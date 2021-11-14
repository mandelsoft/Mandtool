
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
package com.mandelsoft.mand.applet;

import com.mandelsoft.mand.IllegalConfigurationException;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.tool.DefaultMandelListTableModel;
import com.mandelsoft.mand.tool.History;
import com.mandelsoft.mand.tool.MandelImagePanel;
import com.mandelsoft.mand.tool.MandelListGaleryPanel;
import com.mandelsoft.mand.tool.MandelListProxyListModelForTable;
import com.mandelsoft.mand.tool.MandelListTableModel;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.mand.tool.ToolEnvironment;
import com.mandelsoft.mand.tool.mapper.MapperModel;
import com.mandelsoft.mand.util.ArrayMandelList;
import com.mandelsoft.mand.util.CachedUpstreamColormapSourceFactory;
import com.mandelsoft.mand.util.DefaultMandelList;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.util.Utils;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author Uwe Krüger
 */
public class GaleryStarter extends JApplet implements MandelWindowAccess {

  @Override
  public String[][] getParameterInfo()
  {
    String[][] info = {
      // Parameter Name       Kind of Value   Description
        {"datasource",       "URL",          "a directory containing the config"},
        {"list",             "list path",    "a list to display"},
        {"itemwidth",        "int",          "width of galary items"},
        {"itemheight",       "int",          "height of galary items"},
        {"framewidth",       "int",          "image browser max size"},
        {"upstreamcolormap", "bool",         "use upstream colormap as default"},
        {"ticker",           "bool",         "start as ticker"}
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

  private void createGUI()
  {
    try {
      String p_datasource=getParameter("datasource");
      String p_listpath=getParameter("list");
      String p_maxs=getParameter("framewidth");
      String p_width=getParameter("itemwidth");
      String p_height=getParameter("itemheight");
      String p_upstream=getParameter("upstreamcolormap");
      String p_ticker=getParameter("ticker");
      URL base=getDocumentBase();
      boolean ticker=false;
      boolean upstream=false;

      System.out.println("document base is     "+base);
      System.out.println("data source is       "+p_datasource);
      System.out.println("list is              "+p_listpath);
      System.out.println("max is               "+p_maxs);
      System.out.println("width is             "+p_width);
      System.out.println("height is            "+p_height);
      System.out.println("upstream colormap is "+p_upstream);
      System.out.println("ticker is            "+p_ticker);
      if (p_datasource==null) p_datasource=".";
      if (p_listpath==null) p_listpath="favorites";

      ticker=Utils.parseBoolean(p_ticker, false);
      upstream=Utils.parseBoolean(p_upstream, false);

      maxx=400;
      if (p_maxs!=null) {
        try {
          maxx=Integer.parseInt(p_maxs);
        }
        catch (NumberFormatException ex) {
        }
      }
      if (p_width!=null) {
        try {
          width=Integer.parseInt(p_width);
        }
        catch (NumberFormatException ex) {
        }
      }
      if (p_height!=null) {
        try {
          height=Integer.parseInt(p_height);
        }
        catch (NumberFormatException ex) {
        }
      }
      if (width==0) width=6*height/5;
      if (height==0) height=5*width/6;
      URL dataURL=new URL(base, p_datasource);
      ToolEnvironment env=new ToolEnvironment(null, dataURL);
      MandelListFolder f=env.getMandelListFolderTreeModel().getRoot();
      MandelListTableModel model;
      String[] comps=p_listpath.split("/");
      for (String comp:comps) {
        System.out.println("lookup "+comp);
        if (!comp.isEmpty()) {
          f=f.getSubFolder(comp);
          if (f==null) break;
        }
      }
      if (f==null) {
        GaleryStarter.this.showStatus("path not found");
        System.out.println("path not found");
        MandelList l=new DefaultMandelList();
        model=new DefaultMandelListTableModel(l,env.getAllScanner());
      }
      else {
        model=env.getMandelListFolderTreeModel().getMandelListModel(f);
        GaleryStarter.this.showStatus("setup done");
        System.out.println("setup done");
      }
      setup(env,model,upstream);
      if (ticker) {
        timer=new Timer(10000,new ActionListener() {

          public void actionPerformed(ActionEvent e)
          {
             panel.startTicker();
          }

        });
        timer.setRepeats(false);
        timer.start();
      }
    }

    catch (MalformedURLException ex) {
      System.out.println("url failed: "+ex);
      GaleryStarter.this.showStatus("url failed: "+ex);
    }
    catch (IllegalConfigurationException ic) {
      System.out.println("url failed: "+ic);
      GaleryStarter.this.showStatus("config failed: "+ic);
    }
    catch (IOException io) {
      System.out.println("initial image failed: "+io);
      GaleryStarter.this.showStatus("initial image failed: "+io);
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
    if (panel!=null) panel.stopTicker();
    if (timer!=null) timer.stop();
  }

  /////////////////////////////////////////////////////////////////////
  // there we are
  ////////////////////////////////////////////////////////////////////

   MandelListGaleryPanel panel;
   ToolEnvironment env;
   Timer timer;
   int maxx;
   int width;
   int height;
   int rows=1;

  public void setup(ToolEnvironment env, MandelListTableModel model,
                    boolean upstream) throws IOException
  {
    MandelListProxyListModelForTable proxyModel;

    this.env=env;
    proxyModel=new MandelListProxyListModelForTable(model);

    if (width>0 && height>0) {
      Dimension d=new Dimension(width,height);
      panel=new MandelListGaleryPanel(proxyModel,rows,d);
    }
    else {
      panel=new MandelListGaleryPanel(proxyModel,rows);
    }
    panel.setMaxFrame(maxx);
    MandelImage.Factory factory=new MandelImage.Factory(env.getDefaultColormap());
    proxyModel.setFactory(factory);
    if (upstream) {
      proxyModel.setColormapSourceFactory(
        new CachedUpstreamColormapSourceFactory(model.getMandelScanner(),
                                                env.getDefaultColormap(),
                                                env.getColormapCache()));
    }
    add(panel);
  }

  public ColormapModel getColormapModel()
  {
    return null;
  }

  public ToolEnvironment getEnvironment()
  {
    return env;
  }

  public Window getMandelWindow()
  {
    return null;
  }

  public MandelData getMandelData()
  {
    return null;
  }

  public MandelImage getMandelImage()
  {
    return null;
  }

  public MandelName getMandelName()
  {
    return null;
  }

  public MapperModel getMapperModel()
  {
    return null;
  }

  public QualifiedMandelName getQualifiedName()
  {
    return null;
  }

  public MandelImagePanel getMandelImagePane()
  {
    return null;
  }

  public History getHistory()
  {
    return null;
  }
}
