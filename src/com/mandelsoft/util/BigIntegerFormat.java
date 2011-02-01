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

import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class BigIntegerFormat extends Format {

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo,
                             FieldPosition pos)
  {
    BigInteger d=(BigInteger)obj;
    toAppendTo.append(d.toString());
    return toAppendTo;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos)
  {
    int ix=pos.getIndex();
    int len=source.length();
    int sign=1;
    boolean match=false;
    char c=0;
    BigInteger n=BigInteger.ZERO;
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
        n=n.multiply(BigInteger.TEN);
        n=n.add(BigInteger.valueOf(d));
        match=true;
        // System.out.println("->"+d+"; n="+n);
      }
      else {
        if (!match) {
          pos.setErrorIndex(ix);
          return null;
        }
        break;
      }
    }
    if (!match) {
      pos.setErrorIndex(ix);
      return null;
    }
    if (sign<0) n=n.negate();
    pos.setIndex(ix);
    return n;
  }
}
