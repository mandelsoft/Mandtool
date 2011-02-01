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

package com.mandelsoft.swing;

import java.text.NumberFormat;

/**
 *
 * @author Uwe Krueger
 */
public class IntegerField extends NumberField {

  public IntegerField()
  { super(Integer.class);
  }

  public IntegerField(int value)
  {
    super(value);
  }

  public IntegerField(NumberFormat format)
  {
    super(Integer.class,format);
  }

  @Override
  public Integer getMaximumNumber()
  { Number n=super.getMaximumNumber();
    return n==null?null:n.intValue();
  }

  @Override
  public Integer getMinimumNumber()
  { Number n=super.getMinimumNumber();
    return n==null?null:n.intValue();
  }

  public void setMaximumNumber(Integer max)
  {
    super.setMaximum(max);
  }

  public void setMinimumNumber(Integer min)
  {
    super.setMinimum(min);
  }
}
