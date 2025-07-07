/*
 * Copyright 2022 Uwe Krueger.
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
package com.mandelsoft.mand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;

public class Coord {

  BigDecimal X;
  BigDecimal Y;

  public Coord(double cx, double cy)
  {
    X = new BigDecimal(cx);
    Y = new BigDecimal(cy);
  }

  public Coord(BigDecimal cx, BigDecimal cy)
  {
    X = cx;
    Y = cy;
  }

  @Override
  public String toString()
  {
    return String.format("(%s,%s)", X.toString(), Y.toString());
  }

  public BigDecimal getX()
  {
    return X;
  }

  public BigDecimal getY()
  {
    return Y;
  }

  public static Coord parse(String s) throws NumberFormatException
  {
    if (!s.startsWith("(") || !s.endsWith(")")) {
      throw new NumberFormatException("missing brackets");
    }
    s = s.substring(1, s.length() - 1);
    int i = s.indexOf(',');
    if (i <= 0) {
      throw new NumberFormatException("missing comma");
    }
    BigDecimal X = new BigDecimal(s.substring(0, i));
    BigDecimal Y = new BigDecimal(s.substring(i + 1));
    return new Coord(X, Y);
  }
}
