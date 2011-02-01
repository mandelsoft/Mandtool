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

package com.mandelsoft.util;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class BigDecimalFormat extends Format {

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo,
                             FieldPosition pos)
  {
    BigDecimal d=(BigDecimal)obj;
    d.stripTrailingZeros();
    toAppendTo.append(d.toString());
    return toAppendTo;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos)
  {
    int ix=pos.getIndex();
    int len=source.length();
    int sign=1;
    int mod=0;
    int scale=0;
    int exp=0;
    boolean match=false;
    char c=0;
    BigDecimal n=BigDecimal.ZERO;
    if (ix<len) {
      c=source.charAt(ix);
      if (c=='+'||c=='-') {
        sign=c=='-'?-1:1;
        ix++;
      }
    }
    ix--;
    while (++ix<len) {
      c=source.charAt(ix);
      // System.out.println("c="+c+"; n="+n);
      if (Character.isDigit(c)) {
        int d=Character.digit(c, 10);
        n=n.multiply(BigDecimal.TEN);
        n=n.add(BigDecimal.valueOf(d));
        scale+=mod;
        match=true;
        // System.out.println("->"+d+"; n="+n);
      }
      else if (c=='.'&&mod==0) {
        mod=1;
      }
      else {
        if (!match) {
          pos.setErrorIndex(ix);
          return null;
        }
        break;
      }
    }
    if (match&&ix<len&&(c=='E'||c=='e')) {
      match=false;
      if (++ix<len) {
        int esign=1;
        c=source.charAt(ix);
        if (c=='+'||c=='-') {
          esign=c=='-'?-1:1;
        }
        else ix--;
        while (++ix<len) {
          c=source.charAt(ix);
          if (Character.isDigit(c)) {
            int d=Character.digit(c, 10);
            exp=exp*10+d;
            match=true;
          }
        }
        exp*=esign;
      }
    }
    if (!match) {
      pos.setErrorIndex(ix);
      return null;
    }
    scale-=exp;
    n=n.movePointLeft(scale);
    if (sign<0) n=n.negate();
    pos.setIndex(ix);
    return n;
  }
}
