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
package com.mandelsoft.mand.tools;

import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.tool.Decoration;
import java.net.MalformedURLException;
import java.net.URL;
import com.mandelsoft.mand.tool.ToolEnvironment;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.util.ResourceBundle;

/**
 *
 * @author Uwe Krueger
 */
public class MandTool extends Command{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    try {
      SplashScreen sp=SplashScreen.getSplashScreen();
      if (sp!=null) {
        ResourceBundle bundle=ResourceBundle.getBundle("com.mandelsoft.mand.tool.resc.MandTool");
        Graphics2D g=sp.createGraphics();
        Font ofont=g.getFont();
        Font font=Decoration.getAnnotationFont(g);
        Font tfont=font.deriveFont(font.getSize2D()*1.7F);
        g.setFont(tfont);
        g.setColor(new Color(200,200,0));
        g.drawString("Mandelbrot", 240, 170);
        g.drawString("Explorer", 260, 220);
        font=Font.decode("Times New Roman-16");
        g.setFont(font==null?ofont:font);
        g.setColor(new Color(200,200,200));
        g.drawString("Mandtool, Version "+
                     bundle.getString("Application.version"), 300, 360);
        g.drawString("by Uwe Krüger, 2009-2021",40,360);
        sp.update();
//        try {
//          Thread.sleep(10000);
//        }
//        catch (InterruptedException ex) {
//        }
      }
      ToolEnvironment env=new ToolEnvironment(args);
      env.startup();
    }
    catch (IllegalConfigurationException ex) {
      Error("illegal config: "+ex);
    }
  }

  public static void startApplet(String data, URL base)
          throws MalformedURLException
  {
    try {
      System.out.println("document base is "+base);
      System.out.println("data source is   "+data);
      if (data==null) data=".";
      URL dataURL=new URL(base, data);
      ToolEnvironment env=new ToolEnvironment(null, dataURL);
      env.startup();
    }
    catch (IllegalConfigurationException ex) {
      Error("illegal config: "+ex);
    }
  }
}
