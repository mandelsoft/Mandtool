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
import java.math.MathContext;
import java.util.Formatter;

/**
 *
 * @author Uwe Krüger
 */

public class DecimalTest {
  BigDecimal d;

  static public void main(String[] args)
  {
    MathContext c=MathContext.DECIMAL64;
    BigDecimal b10=new BigDecimal(10);
    BigDecimal b1=new BigDecimal(1);
    BigDecimal b0=new BigDecimal(0);

    BigDecimal a=b1;
    BigDecimal s=b10;
    int i=0;
    while (s.compareTo(b1)!=0) {
      i++;
      a=a.divide(b10, c);
      s=b1.add(a,c);
      Formatter f=new Formatter();
      f.format("% .6g", a);
      System.out.println(i+": "+a+"; "+s.stripTrailingZeros()+"; "+f);
    }

  }
}
