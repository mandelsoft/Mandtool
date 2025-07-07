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

import static com.mandelsoft.mand.MandelInfo.ATTR_ITERATONMETHOD;
import static com.mandelsoft.mand.MandelInfo.ATTR_REFCOORD;
import static com.mandelsoft.mand.MandelInfo.ATTR_STDREFCOORD;
import static com.mandelsoft.mand.MandelInfo.ATTR_REFDEPTH;
import static com.mandelsoft.mand.MandelInfo.ATTR_REFPIXEL;

import com.mandelsoft.mand.MandelInfo;
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
public class OptimizedBigDecimalMandIterator extends BigDecimalMandIterator
        implements PixelIterator.PropertySource, PixelIterator.Setup, PixelIterator.Cleanup {

  public static class Coord extends com.mandelsoft.mand.Coord {

    double x;
    double y;
    double sqs;

    public Coord(com.mandelsoft.mand.Coord c)
    {
      this(c.getX(), c.getY());
    }

    public Coord(double cx, double cy)
    {
      super(cx, cy);
      x = cx;
      y = cy;
      sqs = MandArith.add(MandArith.mul(getX(), getY()), MandArith.mul(getY(), getY())).doubleValue();
    }

    public Coord(BigDecimal cx, BigDecimal cy)
    {
      super(cx, cy);
      x = cx.doubleValue();
      y = cy.doubleValue();
      sqs = MandArith.add(MandArith.mul(cx, cx), MandArith.mul(cy, cy)).doubleValue();
    }

    public double getXasDouble()
    {
      return x;
    }

    public double getYasDouble()
    {
      return y;
    }

    public static Coord parse(String s) throws NumberFormatException
    {
      return new Coord(com.mandelsoft.mand.Coord.parse(s));
    }
    
    static public void writeCoords(File file, Coord[] list) throws IOException
    {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
      for (Coord c : list) {
        if (c == null) {
          break;
        }
        out.write(c.toString() + "\n");
      }
      out.close();
    }

    static public void readCoords(File file, Coord[] list) throws IOException
    {
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
        }
        catch (NumberFormatException ex) {
          throw new IOException(ex);
        }
      }
      if (i < list.length) {
        list[i] = null;
      }
      in.close();
    }
  }

  private static boolean accept_all_depths = false;

  private Coord stdref;
  private int stdrefDepth;
  private Coord[] stdIterations;

  private Coord ref;
  private int refDepth;
  private Coord[] iterations;


  private int refPixelX;
  private int refPixelY;
  private File refFile;
  private boolean refSaved;

  private Coord[] saved;
  private PropertyHandler handler;

  public OptimizedBigDecimalMandIterator(
          BigDecimal x0,
          BigDecimal y0,
          BigDecimal dx,
          BigDecimal dy,
          int rx,
          int ry,
          int limit,
          Map<String, String> properties)
  {
    super(x0, y0, dx, dy, rx, ry, limit);
    refSaved = false;
    refFile = null;
    try {
      if (!accept_all_depths) {
        refFile = File.createTempFile("mandelref", "ref");
      }
    }
    catch (IOException ex) {
    }
    System.out.println("prec: " + bits + " (" + Math.ceil(bits * Math.log10(2)) + ")");
    iterations = new Coord[limit + 1];
    saved = null;
    if (!accept_all_depths && limit <= 2000000) {
      saved = new Coord[limit + 1];
    }

    if (properties != null) {
       if (properties.containsKey(ATTR_STDREFCOORD)) {
        try {
          stdref = Coord.parse(properties.get(ATTR_STDREFCOORD));
          System.out.printf("found standard ref %s\n", stdref);
          stdIterations = new Coord[limit + 1];
        } catch (NumberFormatException ex) {
          System.out.printf("malformed standard reference coordinates '%s': %s\n", properties.get(ATTR_STDREFCOORD), ex.getMessage());
        }
        ref = stdref;
      }
      if (properties.containsKey(ATTR_REFCOORD)) {
        try {
          ref = new Coord(Coord.parse(properties.get(ATTR_REFCOORD)));
         System.out.printf("found ref %s\n", ref);
        }
        catch (NumberFormatException ex) {
          System.out.printf("malformed reference coordinates '%s': %s\n", properties.get(ATTR_REFCOORD), ex.getMessage());
        }
      }
      if (properties.containsKey(ATTR_REFPIXEL)) {
        try {
          Coord p = Coord.parse(properties.get(ATTR_REFPIXEL));
          System.out.printf("found ref pixel %s\n", p);
          refPixelX = (int) p.getXasDouble();
          refPixelY = (int) p.getYasDouble();
        }
        catch (NumberFormatException ex) {
          System.out.printf("malformed reference pixel '%s': %s\n", properties.get(ATTR_REFPIXEL), ex.getMessage());
        }
      }
    }

    if (ref == null) {
      ref = new Coord(add(x0, div(dx, 2)), sub(y0, div(dy, 2)));
    }
  }

  public void setup()
  {
    if (stdref != null) {
      stdrefDepth = buildref("standard", stdIterations, ref);
    }
    refDepth = buildref(null, iterations, ref);
    setProperties(-1, -1, ref, refDepth);
  }

  public void cleanup()
  {
    if (refFile != null) {
      refFile.delete();
    }
  }

  int buildref(String msg, Coord[] iterations, Coord ref)
  {
    BigDecimal X = ref.getX();
    BigDecimal Y = ref.getY();

    BigDecimal x = X;
    BigDecimal y = Y;

    if (msg==null || msg.equals("")) {
      msg="";
    } else {
      msg = msg + " ";
    }
    System.out.printf("calculating %sref with limit %d for %s\n", msg, limit, ref);
    long stime = System.currentTimeMillis();
    iterations[0] = ref;

    BigDecimal x2 = mul(x, x);
    BigDecimal y2 = mul(y, y);
    int it = 0;

    while (add(x2, y2).compareTo(bound) < 0 && ++it <= limit) {
      BigDecimal xn = add(sub(x2, y2), X);
      BigDecimal yn = add(mul(mul(b2, x), y), Y);
      x = xn;
      x2 = mul(x, x);
      y = yn;
      y2 = mul(y, y);
      iterations[it] = new Coord(x, y);
    }
    long etime = System.currentTimeMillis();
    System.out.printf("ref done: it=%d, %s\n", it, MandUtils.time((int) ((etime - stime) / 1000)));

    if (it < limit) {
      iterations[it + 1] = null;
    }
    return it;
  }

  private void setProperties(int x, int y, Coord c, int depth)
  {
    if (handler != null) {
      Map<String, String> map = new HashMap<>();
      map.put(ATTR_REFCOORD, c.toString());
      if (x >= 0 || y >= 0) {
        Coord p = new Coord(x, y);
        map.put(ATTR_REFPIXEL, p.toString());
      }
      else {
        map.put(ATTR_REFPIXEL, null);
      }
      if (depth > 0) {
        map.put(ATTR_REFDEPTH, Integer.toString(depth));
      }
      else {
        map.put(ATTR_REFDEPTH, null);
      }
      handler.updateProperties(map);
    }
  }

  public int iter(Coord[] iterations, Coord ref)
  {
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
        if (iterations[i] == null) {
          return -1;
        }
        // dist = (Xn+On)^2
        double ox2 = ox * ox;
        double oy2 = oy * oy;
        double mx = 2 * iterations[i].x * ox;
        double my = 2 * iterations[i].y * oy;
        double dist = iterations[i].sqs + mx + my + ox2 + oy2;

        if (dist >= Mand.BOUND) {
          return i;
        }

        // Dn = (2 Xn-1 Dn-1) + (Dn-1^2) + D0
        double fx = mx - my;
        double fy = 2 * (iterations[i].x * oy + iterations[i].y * ox);

        double qx = ox2 - oy2;
        double qy = 2 * ox * oy;
        ox = fx + qx + ox0;
        oy = fy + qy + oy0;
      }
      return limit + 1;
    }
  }
  
  @Override
  public int iter()
  {
    cnt++;

    int depth = -1;

    if (stdref != null) {
      depth = iter(stdIterations, stdref);
    }
    if (depth < 0) {
      depth = iter(iterations, ref);
    }

    if (depth >= 0) {
      return depth;
    }

    Coord oldref = ref;
    ref = new Coord(cx, cy);
    System.out.printf("%d exceeded ref limit %d\n", cnt, refDepth);
    if (saved != null) {
      Coord[] tmp = iterations;
      iterations = saved;
      saved = tmp;
      refSaved = true;
    } else {
      if (!refSaved && refFile != null) {
        System.out.printf("saving old ref to %s\n", refFile);
        try {
          Coord.writeCoords(refFile, iterations);
          refSaved = true;
        } catch (IOException ex) {
          System.out.printf("saving failed: %s\n", ex);
          refSaved = false;
        }
      }
    }

    depth = buildref(null, iterations, ref);
    if (refSaved && depth < refDepth) {
      System.out.printf("---------- depth not increased -> keep old ref\n");
      if (saved != null) {
        Coord[] tmp = iterations;
        iterations = saved;
        saved = tmp;
        refSaved = false;
        ref = oldref;
        if (handler != null) {
          Map<String, String> map = new HashMap<>();
          map.put(MandelInfo.ATTR_REFCORRUPTED, "true");
          handler.updateProperties(map);
        }
      } else {
        try {
          Coord.readCoords(refFile, iterations);
          ref = oldref;
          if (handler != null) {
            Map<String, String> map = new HashMap<>();
            map.put(MandelInfo.ATTR_REFCORRUPTED, "true");
            handler.updateProperties(map);
          }
        } catch (IOException ex) {
          System.out.printf("restore failed: %s\n", ex);
          refPixelX = x;
          refPixelY = y;
          refDepth = depth;
          refSaved = false;
          setProperties(x, y, ref, depth);
        }
      }
    } else {
      if (refSaved) {
        System.out.printf("old depth %d -> new depth %d\n", refDepth, depth);
      } else {
        System.out.printf("-> new depth %d\n", depth);
      }
      refPixelX = x;
      refPixelY = y;
      refDepth = depth;
      refSaved = false;
      setProperties(x, y, ref, depth);
    }
    return depth;

  }

  @Override
  public void setPropertyHandler(PropertyHandler h)
  {
    handler = h;
    if (h != null) {
      Map<String, String> map = new HashMap<>();
      map.put(ATTR_ITERATONMETHOD, this.getClass().getName());
      h.updateProperties(map);
    }
  }
}
