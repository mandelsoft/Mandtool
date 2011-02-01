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
package com.mandelsoft.mand.tool.mapper;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.swing.GBCPanel;

/**
 *
 * @author Uwe Krüger
 */

public class MapperPanel extends GBCPanel {

  private  MapperControl mapper;

  public MapperPanel()
  { this(null);
  }

  public MapperPanel(MapperModel m)
  {
    mapper=new MapperControl(this,0,0,m);
  }

  public void setMapper(Mapper m)
  {
    mapper.setMapper(m);
  }

  public void removeChangeListener(ChangeListener h)
  {
    mapper.removeChangeListener(h);
  }

  public MapperModel getMapperModel()
  {
    return mapper.getMapperModel();
  }

  public void addChangeListener(ChangeListener h)
  {
    mapper.addChangeListener(h);
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
    MapperPanel mp;

    TestFrame()
    {
      setup();
    }

    void setup()
    { mp=new MapperPanel();
      mp.setBorder(new BevelBorder(BevelBorder.RAISED));
      add(mp);
      pack();
      mp.addChangeListener(new ChangeListener() {

        public void stateChanged(ChangeEvent e)
        {
          //System.out.println("Mapper changed");
        }

      });
      //setResizable(false);
    }
  }

}
