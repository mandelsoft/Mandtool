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
import com.mandelsoft.mand.mapping.StatisticMapper;
import com.mandelsoft.swing.DoubleField;
import com.mandelsoft.swing.GBC;

/**
 *
 * @author Uwe Krüger
 */

public class StatisticMapperCreator extends MapperCreator {
  private DoubleField factor;
  private DoubleField limit;

  public StatisticMapperCreator()
  { super("Statistic");
    
    factor=new DoubleField(0);
    factor.setColumns(10);
    factor.setMinimumNumber(0);
    factor.setMaximumNumber(1);
    factor.setHorizontalAlignment(DoubleField.RIGHT);
    factor.addPropertyChangeListener("value",listener);
    limit=new DoubleField(1);
    limit.setColumns(10);
    limit.setMinimumNumber(0);
    limit.setMaximumNumber(1);
    limit.setHorizontalAlignment(DoubleField.RIGHT);
    limit.addPropertyChangeListener("value",listener);

    JLabel l=new JLabel("Adjustment Factor");
    l.setLabelFor(factor);
    l.setHorizontalAlignment(JLabel.LEFT);
    add (l,GBC(0,0).setAnchor(GBC.WEST));
    add(factor,GBC(1,0));

    l=new JLabel("Adjustment Limit");
    l.setLabelFor(limit);
    l.setHorizontalAlignment(JLabel.LEFT);
    add (l,GBC(0,1).setAnchor(GBC.WEST));
    add(limit,GBC(1,1));
  }

  public Mapper createMapper()
  {
    return new StatisticMapper((Double)factor.getValue(),
                               (Double)limit.getValue());
  }

  public boolean setup(Mapper m)
  {
    if (m.getClass()==StatisticMapper.class) {
      StatisticMapper sm=(StatisticMapper)m;
      factor.setValue(sm.getFactor());
      limit.setValue(sm.getLimit());
      return true;
    }
    return false;
  }
}
