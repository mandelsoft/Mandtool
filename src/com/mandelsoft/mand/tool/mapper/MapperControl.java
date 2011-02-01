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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;

/**
 *
 * @author Uwe Krüger
 */

public class MapperControl {

  private MapperModel model;
  private JComboBox   selection;
  private JPanel      creatorPanel;
  private boolean     editable=true;
  private JPanel      panel;

  public MapperControl(GBCPanel p, int x, int y)
  { this(p,x,y,null);
  }

  public MapperControl(GBCPanel p, int x, int y, MapperModel m)
  {
    JLabel l=new JLabel("Mapping Method");
    if (m==null) model=new MapperModel();
    else         model=m;
    selection=new JComboBox(model);
    //selection.setEditable(false);
    //l.setHorizontalAlignment(JLabel.RIGHT);
    l.setLabelFor(selection);
    p.add(l,new GBC(x,y).setAnchor(GBC.WEST));
    p.add(selection, new GBC(x+1,y));
    creatorPanel=new JPanel();
    creatorPanel.setPreferredSize(new Dimension(300, 100));
    p.add(creatorPanel,new GBC(x,y+1,2,0).setWeight(100, 100));
    selection.addItemListener(new CreatorListener());
    if (model.getSize()>0) selection.setSelectedIndex(1);
    this.panel=p;
  }

  public int getSizeX()
  { return 2;
  }

  public int getSizeY()
  { return 2;
  }

  public MapperModel getMapperModel()
  { return model;
  }

  public void setMapper(Mapper m)
  {
    int n=model.getSize();
    for (int i=0; i<n; i++) {
      MapperCreator c=model.getCreator(i);
      if (c.setup(m)) {
        //System.out.println("  select "+c.getName());
        //model.setSelectedItem(c.getName());
        selection.setSelectedItem(c.getName());
        selection.repaint();
        creatorPanel.repaint();
        panel.repaint();
      }
    }
  }

  public boolean isEditable()
  {
    return editable;
  }

  public void setEditable(boolean editable)
  {
    this.editable=editable;
    //selection.setEditable(editable);
  }

  public void removeChangeListener(ChangeListener h)
  {
    model.removeChangeListener(h);
  }

  public void addChangeListener(ChangeListener h)
  {
    model.addChangeListener(h);
  }

  //////////////////////////////////////////////////////////////////////
  // creator handling
  //////////////////////////////////////////////////////////////////////

  private class CreatorListener implements ItemListener {

    public void itemStateChanged(ItemEvent e)
    { MapperModel m=(MapperModel)((JComboBox)e.getSource()).getModel();
      MapperCreator cur=model.getElement(e.getItem());
      if (e.getStateChange()==ItemEvent.DESELECTED) {
        cur.setVisible(false);
        creatorPanel.remove(cur);
      }
      else {
        creatorPanel.add(cur);
        cur.setEditable(editable);
        cur.setVisible(true);
      }
    }
  }
}
