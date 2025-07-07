/*
 * Copyright 2025 uwekr.
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
package com.mandelsoft.util;

/**
 *
 * @author uwekr
 */
public interface IntMatrix {
  public int getData(int x, int y);
  public void setData(int x, int y, int val);
  
  public int getRX();
  public int getRY();
  
  public static int[][] asArray(IntMatrix m) {
    int rx = m.getRX();
    int ry = m.getRY();
    int[][] matrix = new int[ry][rx];

    for (int y = 0; y < ry; y++) {
      for (int x = 0; x < rx; x++) {
        matrix[y][x] = m.getData(x, y);
      }
    }
    return matrix;
  }
  
  public static void copy(IntMatrix from, IntMatrix to) {
    int rx = from.getRX();
    int ry = from.getRY();
    
    for (int y = 0; y < ry; y++) {
      for (int x = 0; x < rx; x++) {
        to.setData(x, y, from.getData(x, y));
      }
    }
  }
  
  public static class Memory implements IntMatrix {

    private int[][] buffer;
    private int rx, ry;

    public Memory(int rx, int ry) {
      buffer = new int[ry][rx];
      this.rx = rx;
      this.ry = ry;
    }

    public Memory(IntMatrix m) {
      rx = m.getRX();
      ry = m.getRY();
      buffer = asArray(m);
    }

    @Override
    public int getData(int x, int y) {
      return buffer[y][x];
    }

    @Override
    public void setData(int x, int y, int val) {
      buffer[y][x] = val;
    }

    @Override
    public int getRX() {
      return rx;
    }

    @Override
    public int getRY() {
      return ry;
    }
  }
}
