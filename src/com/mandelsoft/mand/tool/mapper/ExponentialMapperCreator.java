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

import com.mandelsoft.mand.mapping.ExponentialMapper;
import javax.swing.JLabel;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.swing.DoubleField;
import com.mandelsoft.swing.GBC;

/**
 *
 * @author Uwe Krüger
 */

public class ExponentialMapperCreator extends MapperCreator {
  private DoubleField factor;
  private DoubleField linear;

  public ExponentialMapperCreator()
  { super("Exponential");
    
    factor=new DoubleField(1);
    factor.setColumns(10);
    factor.setMinimumNumber(0);
    factor.setMaximumNumber(1000);
    factor.setHorizontalAlignment(DoubleField.RIGHT);
    factor.addPropertyChangeListener("value",listener);
    linear=new DoubleField(10);
    linear.setColumns(10);
    linear.setMinimumNumber(0);
    linear.setMaximumNumber(100);
    linear.setHorizontalAlignment(DoubleField.RIGHT);
    linear.addPropertyChangeListener("value",listener);

    JLabel l=new JLabel("Slope Factor");
    l.setLabelFor(factor);
    l.setHorizontalAlignment(JLabel.LEFT);
    add (l,GBC(0,0).setAnchor(GBC.WEST));
    add(factor,GBC(1,0));

    l=new JLabel("Linear Part (%)");
    l.setLabelFor(linear);
    l.setHorizontalAlignment(JLabel.LEFT);
    add (l,GBC(0,1).setAnchor(GBC.WEST));
    add(linear,GBC(1,1));
  }

  public Mapper createMapper()
  { return new ExponentialMapper((Double)factor.getValue(),
                               (Double)linear.getValue());
  }

  public boolean setup(Mapper m)
  {
    if (m.getClass()==ExponentialMapper.class) {
      ExponentialMapper sm=(ExponentialMapper)m;
      factor.setValue(sm.getFactor());
      linear.setValue(sm.getLinear());
      return true;
    }
    return false;
  }
}
