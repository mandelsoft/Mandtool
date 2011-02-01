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

import javax.swing.JLabel;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.mapping.OptimalMapper;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.IntegerField;

/**
 *
 * @author Uwe Krüger
 */

public class OptimalMapperCreator extends MapperCreator {
  private IntegerField area;
  private IntegerField minimize;

  public OptimalMapperCreator()
  { super("Optimal");
    
    area=new IntegerField(15);
    area.setColumns(10);
    area.setMinimumNumber(1);

    area.setHorizontalAlignment(IntegerField.RIGHT);
    area.addPropertyChangeListener("value",listener);

    JLabel l=new JLabel("Color Area");
    l.setLabelFor(area);
    l.setHorizontalAlignment(JLabel.LEFT);
    add (l,GBC(0,0).setAnchor(GBC.WEST));
    add(area,GBC(1,0));

    ///////////////////////////////////////////////
    minimize=new IntegerField(0);
    minimize.setColumns(10);
    minimize.setMinimumNumber(0);

    minimize.setHorizontalAlignment(IntegerField.RIGHT);
    minimize.addPropertyChangeListener("value",listener);

    l=new JLabel("Minimize Colormap Size");
    l.setLabelFor(minimize);
    l.setHorizontalAlignment(JLabel.LEFT);
    add (l,GBC(0,1).setAnchor(GBC.WEST));
    add(minimize,GBC(1,1));
  }

  public Mapper createMapper()
  {
    return new OptimalMapper(area.getValue().intValue(),
                             minimize.getValue().intValue());
  }

  public boolean setup(Mapper m)
  {
    if (m.getClass()==OptimalMapper.class) {
      OptimalMapper sm=(OptimalMapper)m;
      area.setValue(sm.getMinArea());
      minimize.setValue(sm.getMinimize());
      return true;
    }
    return false;
  }
}
