/*
 * Copyright 2021 D021770.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.swing;

/**
 *
 * @author D021770
 */
public class Scale {
  private final double scaleX;
  private final double scaleY;
  
  public static final Scale One = new Scale(1,1);
  
  public Scale(double x, double y)
  {
    if (x<=0) throw new IllegalArgumentException("scale "+x+" must be larger than 0);");
    if (y<=0) throw new IllegalArgumentException("scale "+y+" must be larger than 0);");
    
    scaleX=x;
    scaleY=y;
  }
  
  public double getX()
  {
    return scaleX;
  }

  public double getY()
  {
    return scaleY;
  }
  
  public Scale scale(double f)
  {
    return new Scale(scaleX*f, scaleY*f);
  }
  public Scale scaleX(double f)
  {
    return new Scale(scaleX*f, scaleY);
  }
  public Scale scaleY(double f)
  {
    return new Scale(scaleX, scaleY*f);
  }
  
  public boolean isOne()
  {
    return scaleX==1 && scaleY==1;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 67 * hash + (int) (Double.doubleToLongBits(this.scaleX) ^ (Double.doubleToLongBits(this.scaleX) >>> 32));
    hash = 67 * hash + (int) (Double.doubleToLongBits(this.scaleY) ^ (Double.doubleToLongBits(this.scaleY) >>> 32));
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Scale other = (Scale) obj;
    if (Double.doubleToLongBits(this.scaleX) != Double.doubleToLongBits(other.scaleX)) {
      return false;
    }
    if (Double.doubleToLongBits(this.scaleY) != Double.doubleToLongBits(other.scaleY)) {
      return false;
    }
    return true;
  }
  
  @Override
  public String toString()
  {
    return String.format("(%g,%g)", scaleX, scaleY);
  }
}
