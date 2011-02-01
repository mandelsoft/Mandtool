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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.mandelsoft.mand.MandelRaster;

/**
 *
 * @author Uwe Krueger
 */
public class StatisticMapper2 extends MapperSupport {
  static public boolean debug=false;
  static public final int VERSION=1;

  private double  factor;
  private double  limit;

  public StatisticMapper2()
  { this(0);
  }

  public StatisticMapper2(double factor)
  { this(factor,1.0);
  }

  public StatisticMapper2(double factor, double limit)
  { this.factor=factor;
    this.limit=limit;
  }

  public String getName()
  {
    return "Statistic";
  }

  public String getParamDesc()
  {
    return "f="+factor+",l="+limit;
  }

  public double getFactor()
  {
    return factor;
  }

  public double getLimit()
  {
    return limit;
  }

  ///////////////////////////////////////////////////////////////
  // statistic
  ///////////////////////////////////////////////////////////////

  protected class Histogram extends RasterInfo {
    int[] histogram;

    Histogram(MandelRaster r)
    { super(r);
    }

    @Override
    protected void analyseRaster(MandelRaster r)
    {
      super.analyseRaster(r);
      histogram=new int[getSize()];

      int[][] raster=r.getRaster();

      for (int y=0; y<r.getRY(); y++) {
        for (int x=0; x<r.getRX(); x++) {
          int i=raster[y][x];
          if (i>0) {
            histogram[i-minIt]++;
          }
        }
      }
    }

    int histMax()
    { int max=0;
      for (int i=0; i<histogram.length; i++) {
        if (histogram[i]>max) max=histogram[i];
      }
      return max;
    }

    /////////////////////////////////////////////////////////////////////////
    // joining method
    /////////////////////////////////////////////////////////////////////////

    int[] points;
    int[] accu;
    int   last;

    int setupColors()
    {
      points=new int[histogram.length];
      accu=new int[histogram.length];
      last=-1;
      int cnt=0;

      int cur=histogram.length-1;

      for (int i=histogram.length-1; i>=0; i--) {
        if ((accu[i]=histogram[i])>0) {
          if (last<0) {
            last=i;
          }
          else {
            points[cur]=i;
          }
          cur=i;
          cnt++;
        }
      }
      points[cur]=-1;

      return cnt;
    }


    private class Pointer {
      int cur;
      int prev;
    }

    Pointer minUsedColor()
    { Pointer p=new Pointer();
      int cur=points.length-1;
      int prev=-1;

      p.cur=cur;
      p.prev=prev;

      while (cur>=0) {
        if (accu[cur]<accu[p.cur]) {
          p.cur=cur;
          p.prev=prev;
        }
        prev=cur;
        cur=points[cur];
      }
      return p;
    }

    int joinNext(Pointer p)
    {
      if (p.cur==1 || p.prev==1)
        if (debug)  System.out.println("join next "+p.cur);
      accu[p.cur]+=accu[points[p.cur]];
      points[p.cur]=points[points[p.cur]];
      return p.cur;
    }

    int joinPrev(Pointer p)
    {
      if (p.cur==1 || p.prev==1)
        if (debug) System.out.println("join prev "+p.cur+" ("+p.prev+")");
      accu[p.prev]+=accu[p.cur];
      points[p.prev]=points[p.cur];
      return p.prev;
    }

    int joinColor()
    { Pointer p=minUsedColor();
      return joinColor(p);
    }

    int joinColor(Pointer p)
    {
      if (points[p.cur]<0) {
        return joinPrev(p);
      }
      else if (p.prev<0) {
        return  joinNext(p);
      }
      else if (accu[points[p.cur]]<accu[p.prev]) {
        return joinNext(p);
      }
      else {
        return joinPrev(p);
      }
    }

    int compressColors(int n, int[] mapping, BreakCondition c)
    {
      int num=setupColors();
      int max=histMax();
      int bound=(int)(limit*max);
      while (num>n) {
        Pointer p=minUsedColor();
        if (c!=null && c.done(num, accu[p.cur])) break;
        int cur=joinColor(p);
        if (bound-accu[cur]>0) {
          accu[cur]+=(bound-accu[cur])*factor;
        }
        num--;
      }
      if (debug) System.out.println("p(0)->"+points[0]);
      if (debug) System.out.println("p(1)->"+points[1]);
      if (debug) System.out.println("compressed");
      int cur=points.length-1;
      int it=cur;
      n=num;
      while (it>=0) {
        if (it==points[cur]) {
          cur=points[cur];
          n--;
        }
        if (n<=0) {
          throw new IllegalStateException(
                  "need more colors than expected (it="+it+")");
        }
        mapping[it--]=n;
      }
      return num;
    }
  }

  public interface BreakCondition {
    public boolean done(int num, int accu);
  }

  ///////////////////////////////////////////////////////////////
  // mapping
  ///////////////////////////////////////////////////////////////


  public Mapping createMapping(MandelRaster raster, int colmapsize)
  { Histogram info=new Histogram(raster);
    int[] mapping=new int[info.getSize()];
    int s=createMapping(info,colmapsize,mapping);
//    System.out.println("0->"+mapping[0]);
//    System.out.println("1->"+mapping[1]);
//    System.out.println("2->"+mapping[2]);
    return new Mapping(info.getMinIt(),info.getMaxIt(),s,mapping);
  }

  protected int createMapping(Histogram info, int colmapsize, int[] mapping)
  {
    return info.compressColors(colmapsize-1,mapping,null)+1;
//    return colmapsize;
  }

  ///////////////////////////////////////////////////////////////
  // io info
  ///////////////////////////////////////////////////////////////

  @Override
  protected int getDefaultVersion()
  {
    return VERSION;
  }

  @Override
  protected boolean validVersion(int v)
  {
    return 1<=v && v<=VERSION;
  }


  /////////////////////////////////////////////////////////////////////////
  // IO
  /////////////////////////////////////////////////////////////////////////

  protected void _write(DataOutputStream dos, int v) throws IOException
  {
    switch (v) {
      case 1:
        writeV1(dos);
        break;
      default:
        throw new IOException("unknown cyclic mapping version "+v);
    }
  }

  protected void writeV1(DataOutputStream dos) throws IOException
  { dos.writeDouble(factor);
    dos.writeDouble(limit);
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
    limit=dis.readDouble();
  }
}
