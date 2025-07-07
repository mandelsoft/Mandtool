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
package com.mandelsoft.mand.meth;

import static com.mandelsoft.mand.MandelInfo.ATTR_FRAGMENTSCALE;
import static com.mandelsoft.mand.MandelInfo.ATTR_ITERATONMETHOD;
import com.mandelsoft.mand.tools.Mand;
import com.mandelsoft.mand.util.MandArith;
import static com.mandelsoft.mand.util.MandArith.b2;
import com.mandelsoft.mand.util.MandUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Uwe Krueger
 */
public class OptimizedBigDecimalMandIteratorExp extends BigDecimalMandIterator
        implements PixelIterator.PropertySource, PixelIterator.Setup, PixelIterator.Cleanup {

  int scale;
  ReferenceCoord temp;
  boolean modified;
  
  public static class Coord extends com.mandelsoft.mand.Coord {

    double x;
    double y;
    double sqs;

    public Coord(com.mandelsoft.mand.Coord c) {
      this(c.getX(), c.getY());
    }

    public Coord(double cx, double cy) {
      super(cx, cy);
      x = cx;
      y = cy;
      sqs = MandArith.add(MandArith.mul(getX(), getY()), MandArith.mul(getY(), getY())).doubleValue();
    }

    public Coord(BigDecimal cx, BigDecimal cy) {
      super(cx, cy);
      x = cx.doubleValue();
      y = cy.doubleValue();
      sqs = MandArith.add(MandArith.mul(cx, cx), MandArith.mul(cy, cy)).doubleValue();
    }

    public double getXasDouble() {
      return x;
    }

    public double getYasDouble() {
      return y;
    }

    public static Coord parse(String s) throws NumberFormatException {
      return new Coord(com.mandelsoft.mand.Coord.parse(s));
    }

    static public void writeCoords(File file, Coord[] list) throws IOException {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
      for (Coord c : list) {
        if (c == null) {
          break;
        }
        out.write(c.toString() + "\n");
      }
      out.close();
    }

    static public void readCoords(File file, Coord[] list) throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      int i = 0;

      String line;
      while ((line = in.readLine()) != null) {
        try {
          Coord c = Coord.parse(line);
          if (i >= list.length) {
            throw new IOException("read more than " + list.length + " entries");
          }
          list[i++] = c;
        } catch (NumberFormatException ex) {
          throw new IOException(ex);
        }
      }
      if (i < list.length) {
        list[i] = null;
      }
      in.close();
    }
  }


  private PropertyHandler handler;

  public OptimizedBigDecimalMandIteratorExp(
          BigDecimal x0,
          BigDecimal y0,
          BigDecimal dx,
          BigDecimal dy,
          int rx,
          int ry,
          int limit,
          Map<String, String> properties) {
    super(x0, y0, dx, dy, rx, ry, limit);
  
    System.out.println("prec: " + bits + " (" + Math.ceil(bits * Math.log10(2)) + ")");

    if (properties != null) {
       if (properties.containsKey(ATTR_FRAGMENTSCALE)) {
        try {
          scale = Integer.parseInt(properties.get(ATTR_FRAGMENTSCALE));
          System.out.printf("found fragment scale %s\n", scale);
        } catch (NumberFormatException ex) {
          System.out.printf("malformed fragment scale '%s': %s\n", properties.get(ATTR_FRAGMENTSCALE), ex.getMessage());
        }
      }
    }
    
    if (scale <= 1) {
      scale=Math.max(rx, ry);
    }
    temp=new ReferenceCoord(null, null);
  }

  private RefCache cache;
  public void setup() {
    try {
      File f = File.createTempFile("mand", ".refs");
      f.delete();
      f.mkdir();
      System.out.printf("refcache is %s\n", f);
      cache = new RefCache(null, scale);
    } catch (IOException ex) {

    }
  }

  public void cleanup() {
    cache.cleanup();
  }

  static int buildref(String msg, ReferenceCoord ref, OptimizedBigDecimalMandIteratorExp iter) {
    BigDecimal X = ref.coord.getX();
    BigDecimal Y = ref.coord.getY();

    BigDecimal x = X;
    BigDecimal y = Y;

    if (msg == null || msg.equals("")) {
      msg = "";
    } else {
      msg = msg + " ";
    }
    System.out.printf("calculating %sref with limit %d for %s\n", msg, iter.limit, ref.coord);
    long stime = System.currentTimeMillis();
    if (ref.iterations==null) {
      ref.iterations=new Coord[iter.limit+1];
    }
    ref.iterations[0] = ref.coord;

    BigDecimal x2 = iter.mul(x, x);
    BigDecimal y2 = iter.mul(y, y);
    int it = 0;

    while (iter.add(x2, y2).compareTo(iter.bound) < 0 && ++it <= iter.limit) {
      BigDecimal xn = iter.add(iter.sub(x2, y2), X);
      BigDecimal yn = iter.add(iter.mul(iter.mul(b2, x), y), Y);
      x = xn;
      x2 = iter.mul(x, x);
      y = yn;
      y2 = iter.mul(y, y);
      ref.iterations[it] = new Coord(x, y);
    }
    long etime = System.currentTimeMillis();
    System.out.printf("ref done: it=%d, %s\n", it, MandUtils.time((int) ((etime - stime) / 1000)));

    if (it < iter.limit) {
      ref.iterations[it + 1] = null;
    }
    ref.depth=it;
    return it;
  }
  
  public int iter(ReferenceCoord ref) {
    double ox0;
    double oy0;
    double ox;
    double oy;

    while (true) {
      ox0 = sub(cx, ref.getX()).doubleValue();
      oy0 = sub(cy, ref.getY()).doubleValue();

      ox = ox0;
      oy = oy0;

      for (int i = 0; i <= limit; i++) {
        if (ref.iterations[i] == null) {
          return -1;
        }
        // dist = (Xn+On)^2
        double ox2 = ox * ox;
        double oy2 = oy * oy;
        double mx = 2 * ref.iterations[i].x * ox;
        double my = 2 * ref.iterations[i].y * oy;
        
        double t =  mx + my + ox2 + oy2;
        double dist = ref.iterations[i].sqs + t;

        if (dist >= Mand.BOUND) {
          return i;
        }

        // Dn = (2 Xn-1 Dn-1) + (Dn-1^2) + D0
        double fx = mx - my;
        double fy = 2 * (ref.iterations[i].x * oy + ref.iterations[i].y * ox);

        double qx = ox2 - oy2;
        //double qx = csub(ox2, oy2);
        double qy = 2 * ox * oy;
        ox = fx + qx + ox0;
        oy = fy + qy + oy0;
      }
      return limit + 1;
    }
  }

  @Override
  public int iter() {
    cnt++;

    ReferenceCoord ref = cache.get(x, y);
    int depth = iter(ref);

    if (depth >= 0) {
      return depth;
    }

    temp.coord = new Coord(cx, cy);
    System.out.printf("%d exceeded ref limit\n", cnt);
    
    depth = buildref(null, temp, this);
    try {
      if (temp.depth > ref.depth) {
        cache.set(x, y, temp);
        temp=ref;
        modified=true;
      }
    }
    catch(IOException ex) {
    }
    return depth;
  }

  @Override
  public void setPropertyHandler(PropertyHandler h) {
    handler = h;
    if (h != null) {
      Map<String, String> map = new HashMap<>();
      map.put(ATTR_ITERATONMETHOD, this.getClass().getName());
      h.updateProperties(map);
    }
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  
  public static class ReferenceCoord {
    Coord []iterations;
    Coord coord;
    int depth;
    
    public ReferenceCoord(Coord []iterations, Coord ref)
    {
      this.iterations=iterations;
      this.coord=ref;
    }
    
    public BigDecimal getX()
    {
      return coord.getX();
    }
    
    public BigDecimal getY()
    {
      return coord.getY();
    }
    
    public Coord getRef() {
      return coord;
    }
    
    public Coord[] getIterations() {
      return iterations;
    }
  }
  
  
  public class RefCache {
    File base;
    int scale;
    ReferenceCoord [][]cache;
    
    int curX, curY;
    ReferenceCoord cur;
    
    
    public RefCache(File base, int scale)
    {
      this.base=base;
      this.scale=scale;
      
      curX = curY = -1;
      
      if (base != null) {
        cur = new ReferenceCoord(null, null);
      }
      else {
        cache=new ReferenceCoord[ry/scale+1][rx/scale+1];
      }
      
      System.out.printf("fragment cache %d\n", scale);
    }
    
    public void cleanup()
    {
      if (base!=null) {
        for (File f : base.listFiles()) {
          f.delete();
        }
        base.delete();
      }
    }
    
    public void set(int x, int y, ReferenceCoord ref) throws IOException {
      int cx = x / scale;
      int cy = y / scale;
      
      if (cx == curX && cy == curY) {
        cur = ref;
      } else {
        System.out.printf("shit\n");
        System.exit(1);
      }

      if (base != null) {
        File f = new File(Integer.toString(cx) + "x" + Integer.toString(cy));
        Coord.writeCoords(f, ref.iterations);
      } else {
        cache[cy][cx] = ref;
      }     
    }
    
    public ReferenceCoord get(int x, int y) {
      int cx = x / scale;
      int cy = y / scale;
      
      if (curX == cx && curY == cy) {
        return cur;
      }
      
      curX = cx;
      curY = cy;
      String key = Integer.toString(cx) + "x" + Integer.toString(cy);
      //System.out.printf("(%d,%d):%s\n", x,y,key);

      if (base != null) {
        try {
          File f = new File(base, key);
          if (!f.exists()) {
            System.out.printf("initial ref for %s\n", key);
            cur.coord = new Coord(getCX(cx * scale + scale / 2), getCY(cy * scale + scale / 2));
            buildref(key, cur, OptimizedBigDecimalMandIteratorExp.this  );
            Coord.writeCoords(f, cur.iterations);
          } else {
            Coord.readCoords(f, cur.iterations);
          }
        } catch (IOException ex) {
          System.out.printf("error: %s\n", ex);
        }
      }
      else {
        cur = cache[cy][cx];
        if (cur == null) {
          System.out.printf("initial ref for %s\n", key);
          cur = new ReferenceCoord(new Coord[limit + 1], new Coord(getCX(cx * scale + scale / 2), getCY(cy * scale + scale / 2)));
          buildref(key, cur, OptimizedBigDecimalMandIteratorExp.this);
          cache[cy][cx] = cur;
        }
      }
      return cur;
    }
  }
}
