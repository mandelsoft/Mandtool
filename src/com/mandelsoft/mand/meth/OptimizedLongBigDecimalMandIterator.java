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
import static com.mandelsoft.mand.MandelInfo.ATTR_REFDEPTH;
import static com.mandelsoft.mand.MandelInfo.ATTR_REFPIXEL;

import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.tools.Mand;
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

public class OptimizedLongBigDecimalMandIterator extends BigDecimalMandIterator
        implements PixelIterator.PropertySource, PixelIterator.Setup, PixelIterator.Cleanup {

  static public BigExpDouble BOUND = new BigExpDouble(Mand.BOUND);
  
  public static class Coord extends com.mandelsoft.mand.Coord {

    BigExpDouble x;
    BigExpDouble y;
    BigExpDouble sqs;

    public Coord(com.mandelsoft.mand.Coord c)
    {
      this(c.getX(), c.getY());
    }

    public Coord(double cx, double cy)
    {
      super(cx, cy);
      x = new BigExpDouble(cx);
      y = new BigExpDouble(cy);
      sqs = x.square().add(y.square());
    }

    public Coord(BigDecimal cx, BigDecimal cy)
    {
      super(cx, cy);
      
      x = new BigExpDouble(cx);
      y = new BigExpDouble(cy);
      sqs = x.square().add(y.square());
    }

    public BigExpDouble getXasDouble()
    {
      return x;
    }

    public BigExpDouble getYasDouble()
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

  private static boolean accept_all_depths = true;
  private static int memory_depth_limit = 800000;


  private Coord ref;
  private int refPixelX;
  private int refPixelY;
  private File refFile;
  private boolean refSaved;
  private int refDepth;

  private Coord[] iterations;
  private Coord[] saved;
  private PropertyHandler handler;

  public OptimizedLongBigDecimalMandIterator(
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
      if (!accept_all_depths && limit <= memory_depth_limit) {
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
      String msg = null;
      if (properties.containsKey(ATTR_REFCOORD)) {
        try {
          ref = new Coord(Coord.parse(properties.get(ATTR_REFCOORD)));
          msg = String.format("found ref %s", ref);
        }
        catch (NumberFormatException ex) {
          System.out.printf("malformed reference coordinates '%s': %s\n", properties.get(ATTR_REFCOORD), ex.getMessage());
        }
      }
      if (properties.containsKey(ATTR_REFPIXEL)) {
        try {
          Coord p = Coord.parse(properties.get(ATTR_REFPIXEL));
          if (msg == null) {
            msg = String.format("found ref pixel %s", p);
          }
          else {
            msg += String.format(" at pixel %s", p);
          }
          refPixelX =  p.getX().intValue();
          refPixelY =  p.getY().intValue();
        }
        catch (NumberFormatException ex) {
          System.out.printf("malformed reference pixel '%s': %s\n", properties.get(ATTR_REFPIXEL), ex.getMessage());
        }
      }
      if (msg != null) {
        System.out.println(msg);
      }
    }

    if (ref == null) {
      ref = new Coord(add(x0, div(dx, 2)), sub(y0, div(dy, 2)));
    }
  }

  public void setup()
  {
    refDepth = buildref(iterations, ref);
    setProperties(-1, -1, ref, refDepth);
  }

  public void cleanup()
  {
    if (refFile != null) {
      refFile.delete();
    }
  }

  int buildref(Coord[] iterations, Coord ref)
  {
    BigDecimal X = ref.getX();
    BigDecimal Y = ref.getY();

    BigDecimal x = X;
    BigDecimal y = Y;

    System.out.printf("calculating ref with limit %d for %s\n", limit, ref);
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

  @Override
  public int iter()
  {
    cnt++;

    BigExpDouble ox0;
    BigExpDouble oy0;
    BigExpDouble ox;
    BigExpDouble oy;

    restart:
    while (true) {
      ox0 = new BigExpDouble(sub(cx, ref.getX()));
      oy0 = new BigExpDouble(sub(cy, ref.getY()));

      ox = ox0;
      oy = oy0;

      for (int i = 0; i <= limit; i++) {
        if (iterations[i] == null) {
          Coord oldref = ref;
          ref = new Coord(cx, cy);
          System.out.printf("%d exceeded ref limit %d\n", cnt, i - 1);
          if (saved != null) {
            Coord[] tmp = iterations;
            iterations = saved;
            saved = tmp;
            refSaved = true;
          }
          else {
            if (!refSaved && refFile != null) {
              System.out.printf("saving old ref to %s\n", refFile);
              try {
                Coord.writeCoords(refFile, iterations);
                refSaved = true;
              }
              catch (IOException ex) {
                System.out.printf("saving failed: %s\n", ex);
                refSaved = false;
              }
            }
          }
          int depth = buildref(iterations, ref);
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
            }
            else {
              try {
                Coord.readCoords(refFile, iterations);
                ref = oldref;
                if (handler != null) {
                  Map<String, String> map = new HashMap<>();
                  map.put(MandelInfo.ATTR_REFCORRUPTED, "true");
                  handler.updateProperties(map);
                }
              }
              catch (IOException ex) {
                System.out.printf("restore failed: %s\n", ex);
                refPixelX = x;
                refPixelY = y;
                refDepth = depth;
                refSaved = false;
                setProperties(x, y, ref, depth);
              }
            }
          }
          else {
            refPixelX = x;
            refPixelY = y;
            refDepth = depth;
            refSaved = false;
            setProperties(x, y, ref, depth);
          }
          return depth;
        }
        // dist = (Xn+On)^2
        BigExpDouble ox2 = ox.square();
        BigExpDouble oy2 = oy.square();
        BigExpDouble mx = iterations[i].x.mul(ox).mul2();
        BigExpDouble my = iterations[i].y.mul(oy).mul2();
        BigExpDouble dist = iterations[i].sqs.add(mx).add(my).add(ox2).add(oy2);

        if (dist.gt(BOUND)) {
          return i;
        }

        // Dn = (2 Xn-1 Dn-1) + (Dn-1^2) + D0
        BigExpDouble fx = mx.sub(my);
        BigExpDouble fy = iterations[i].x.mul(oy).add(iterations[i].y.mul(ox)).mul2();

        BigExpDouble qx = ox2.sub(oy2);
        BigExpDouble qy = ox.mul(oy).mul2();

        ox = fx.add(qx).add(ox0);
        oy = fy.add(qy).add(oy0);
      }
      return limit + 1;
    }
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
