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

package com.mandelsoft.mand.mapping;

import com.mandelsoft.mand.MandelRaster;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Closure;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.System.out;

/**
 *
 * @author Uwe Krueger
 */
public class ExponentialMapper extends MapperSupport {
  static public final int VERSION=1;
  static public final boolean debug=false;
  
  private double linear;
  private double factor;

  public ExponentialMapper()
  { 
    this(1, 0);
  }

  public ExponentialMapper(double factor, double linear)
  { 
    this.linear=linear;
    this.factor=factor;
  }

  @Override
  public String getName()
  {
    return "Exponential";
  }

  @Override
  public String getParamDesc()
  {
    return "l="+linear+",f="+factor;
  }

  public double getLinear()
  {
    return linear;
  }

  public double getFactor()
  {
    return factor;
  }

  ///////////////////////////////////////////////////////////////
  // mapping
  ///////////////////////////////////////////////////////////////

  @Override
  public Mapping createMapping(MandelRaster raster, int colmapsize) {
    RasterInfo info = new RasterInfo(raster);

    MappingBuilder mb = new MappingBuilder(info.getMinIt(), info.getMaxIt(),
            colmapsize);
    compressColors(colmapsize - 1, mb);
    MappingRepresentation mr;
    if (mb.getSourceSize() < 2000000) {
      mr = mb.createArrayMapping();
    } else {
      mr = mb.createTreeMapping();
    }
    return new Mapping(mb, mr);
  }

  static public int map(int color, double a, double b) {
      return (int) (a * Math.exp(color) - a + color *(1.0-a));
  }
  
  static public class ColorMapper {
    double a;
    double b;
    double c;
    double d;
    
    public ColorMapper(double factor, double cm, double m)
    {
       double f1 = cm <=0 ? 1 : cm;
       double f2 = m<=0 ? 1 : m;
       b = (factor*f1)/f2;
       a = ((double)(m -cm))/(Math.exp(b*cm)-b*cm-1.0);
       c = 1.0-a*b;
       d=-a;
       
       System.out.printf("a:%g, b:%g, c:%g, d:%g\n", a,b,c,d);
    }
    
    public int map(int color)
    {
      return (int)(a*Math.exp(b*color)+color*c+d);
    }
  }
  
  void compressColors(int n, MappingBuilder mb) {
    int range = mb.getSourceSize();
    int linear = (int) (this.linear * n / 100.0);

    int exp = range - linear; // exp iteration values
    int cexp = n - linear;      // exp color values

    System.out.printf("minIt=%s, maxIt=%d\n", mb.getMinIt(), mb.getMaxIt());
    System.out.printf("range=%d\n", range);

    for (int i = 0; i < linear && i < range; i++) {
      int c = i + 1;
        mb.add(i, c);
        if (debug) System.out.printf("/%10d->%4d\n", i, c);
    }

    double cm = cexp-1;
    double m = exp -1;
    
    if (exp > 0) {
      ColorMapper mapper = new ColorMapper(factor, cm, m);

      System.out.printf("0: %d\n", mapper.map(0));
      System.out.printf("1: %d\n", mapper.map(1));
      System.out.printf("%d: %d\n", cexp - 1, mapper.map(cexp - 1));
      for (int i = 0; i < cexp; i++) {
        int c = i + linear + 1;
        int it = mapper.map(i) + linear;
        //System.out.printf("     %10d->%4d\n", i + linear, c);
        if (debug) System.out.printf("^%10d->%4d\n", it, c);
        mb.add(it, c);
      }
    }
  }

  ///////////////////////////////////////////////////////////////
  // io info
  ///////////////////////////////////////////////////////////////

  @Override
  protected int getDefaultVersion() {
    return VERSION;
  }

  @Override
  protected boolean validVersion(int v) {
    return 1 <= v && v <= VERSION;
  }

  /////////////////////////////////////////////////////////////////////////
  // IO
  /////////////////////////////////////////////////////////////////////////
   @Override
  protected void _write(DataOutputStream dos, int v) throws IOException
  {
    switch (v) {
      case 1:
        writeV1(dos);
        break;
      default:
        throw new IOException("unknown exponential mapping version "+v);
    }
  }

  protected void writeV1(DataOutputStream dos) throws IOException
  { dos.writeDouble(factor);
    dos.writeDouble(linear);
  }

  @Override
  protected void _read(DataInputStream dis, int v) throws IOException
  {
    switch (v) {
      case 1:
        readV1(dis);
        break;
      default:
        throw new IOException("unknown cyclic mapping version "+v);
    }
  }

  protected void readV1(DataInputStream dis) throws IOException
  { factor=dis.readDouble();
    linear=dis.readDouble();
  }
}
