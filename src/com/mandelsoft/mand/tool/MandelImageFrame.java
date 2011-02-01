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

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import java.awt.EventQueue;
import java.awt.Window;
import java.io.IOException;
import javax.swing.JFrame;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.image.MandelAreaImage;
import com.mandelsoft.mand.tool.mapper.MapperModel;
import java.awt.Image;
import java.awt.SplashScreen;
import java.net.URL;
import javax.swing.ImageIcon;


/**
 *
 * @author Uwe Krueger
 */
public class MandelImageFrame  extends JFrame
                               implements MandelWindowAccess {
  static public boolean debug=false;

  private MandelImagePanel panel;

  public MandelImageFrame(ToolEnvironment env, MandelAreaImage img) throws IOException
  {
    setup(new MandelImagePanel(env, img));
  }

  public MandelImageFrame(ToolEnvironment env, MandelAreaImage img, int maxx)
                          throws IOException
  {
    setup(new MandelImagePanel(env, img, maxx));
  }

  private void setup(MandelImagePanel panel)
  {
    this.panel=panel;
//    JLayeredPane l=new JLayeredPane();
//    l.setLayout(new GridBagLayout());
//    l.add(panel,0);
//    add(l);
    add(panel);
    pack();
    setVisible(true);
    loadFrameIcon();
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
    return this;
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

  private void loadFrameIcon()
  {
    URL imgUrl=null;
    ImageIcon imgIcon=null;

    imgUrl=MandelImageFrame.class.getResource("resc/mand.gif");
    imgIcon=new ImageIcon(imgUrl);
    Image img=imgIcon.getImage();
    this.setIconImage(img);
  }

  ///////////////////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////////////////

  public static void main(final String[] args)
  {
    try {
      ToolEnvironment env;
      SplashScreen sp=SplashScreen.getSplashScreen();
      try {
        sp.setImageURL(Environment.class.getResource("resc/splash.png"));
      }
      catch (Exception ex) {

      }

      env=new ToolEnvironment(args);
      synchronized (env) {
        /*
        try {
        System.out.println("all:"+
        env.getAllScanner().getMandelHeaders(env.getInitialName()));
        env.getImageDataScanner().getMandelData(env.getInitialName());
        MandelImage img=env.getMandelImage(env.getInitialName());
        }
        catch (IOException ex) {
        System.out.println("cannot read "+env.getInitialName());
        }
         * */
        try {
          createWindow(env);
        }
        catch (IllegalArgumentException ia) {
          System.out.println("illegal mandel name");
        }
      }
    }
    catch (IllegalConfigurationException ex) {
      System.out.println("illegal config: "+ex);
    }
  }

  static void createWindow(final ToolEnvironment env)
  {

    EventQueue.invokeLater(new Runnable() {
      public void run()
      {
        synchronized (env) {
          try {
            MandelAreaImage img=env.getMandelImage(env.getInitialName());
            JFrame frame=new MandelImageFrame(env, img);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
          }
          catch (IOException io) {
            System.out.println("cannot read image");
          }
        }
      }
    });
  }
}
