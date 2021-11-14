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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author Uwe Krueger
 */
public interface ProportionProvider {

  double getProportion();

  static public class Proportion implements ProportionProvider {

    private double proportion;

    public Proportion(Dimension d)
    {
      proportion=d.getWidth()/d.getHeight();
    }

    public Proportion(double d)
    {
      proportion=d;
    }

    public Proportion(BufferedImage d)
    {
      proportion=d.getWidth()/d.getHeight();
    }

    public double getProportion()
    {
      return proportion;
    }
  }
  
  static public class Screen implements ProportionProvider {

    public Screen()
    {
    }

    public double getProportion()
    {
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      return d.getWidth()/d.getHeight();
    }
  }
}
