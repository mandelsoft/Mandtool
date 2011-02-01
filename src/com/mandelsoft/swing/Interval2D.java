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

/**
 *
 * @author Uwe Krüger
 */

public class Interval2D {
  private double a;
  private double b;

  public Interval2D()
  {
  }

  public Interval2D(double a, double b)
  {
    if (a>b) {
      this.b=a;
      this.a=b;
    }
    else {
      this.a=a;
      this.b=b;
    }
  }

  public double getMin()
  {
    return a;
  }

  public double getMax()
  {
    return b;
  }

  public double size()
  {
    return b-a;
  }
  
  public boolean isEmpty()
  {
    return a==b;
  }

  public void limitSize(double s)
  {
    if (size()>s) {
      b=a+s;
    }
  }
}
