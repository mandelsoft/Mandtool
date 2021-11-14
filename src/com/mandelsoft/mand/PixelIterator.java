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

package com.mandelsoft.mand;

import java.math.BigDecimal;
import java.util.Map;

/**
 *
 * @author Uwe Krueger
 */
public interface PixelIterator {
  void setX(int x);
  void setY(int y);

  int getPrecision();
  int getMagnification();
  boolean isFast();
  int iter();

  BigDecimal getCX();
  BigDecimal getCY();

  double getX(BigDecimal x);
  double getY(BigDecimal y);
  
  public interface Setup {
    void setup();
  }

  public static void setup(PixelIterator pi)
  {
    if (pi instanceof PixelIterator.Setup) {
      ((PixelIterator.Setup)pi).setup();
    }
  }
  
  public interface PropertySource {
    void setPropertyHandler(PropertyHandler h);

    public interface PropertyHandler {
      void updateProperties(Map<String, String> props);
    }
  }
  
  public static void setPropertyHandler(PixelIterator pi, PropertySource.PropertyHandler h)
  {
    if (pi instanceof PixelIterator.PropertySource) {
      ((PixelIterator.PropertySource)pi).setPropertyHandler(h);
    }
  }
  
}
