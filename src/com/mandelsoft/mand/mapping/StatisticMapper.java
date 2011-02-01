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
public class StatisticMapper extends MapperSupport {
  static public boolean debug=false;
  static public boolean usebuilder=true;
  static public final int VERSION=1;

  private double  factor;
  private double  limit;

  public StatisticMapper()
  { this(0);
  }

  public StatisticMapper(double factor)
  { this(factor,1.0);
  }

  public StatisticMapper(double factor, double limit)
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

  private static boolean hdeb=false;

  protected static interface Histogram {
    void add(int i); // add iteration value for new pixel
    int  get(int i); // get number of pixels for given iteration value
    int  getSize();
    int  histMax();

    int mapIndexToValue(int i); // iteration value at index
    int mapIndexToCount(int i); // number of pixels at index
  }

  ///////////////////////////////////////////////////////////////
  // Tree Implementation

  protected static class TreeHistogram extends BalancedTreeSupport
                                       implements Histogram {
    class Node extends TreeNode<Node> {
      int index;
      int value;
      int cnt;

      Node(int i, int c)
      {
        value=i;
        cnt=c;
        if (hdeb) System.out.println("CREATE "+i);
        indexed=false;
      }

      Node(int i)
      {
        this(i,1);
      }

      int createIndex(int index)
      {
        if (right!=null) index=right.createIndex(index);
        this.index=index++;
        if (left!=null) index=left.createIndex(index);
        return index;
      }

      @Override
      public String toString()
      {
        return ""+value+"("+cnt+")";
      }
    }

    private int size;
    private int max;
    private boolean indexed;

    public TreeHistogram(int size)
    {
      this.size=size;
      this.root=new Node(size-1,0);
    }

    public void add(int i)
    {
       if (hdeb) System.out.println("add "+i);
      root=add((Node)root,i);
    }

    private Node add(Node n, int i)
    {
      if (n==null) {
        if (max==0) max=1;
        return new Node(i);
      }
      if (hdeb) System.out.println("  handle "+n.value);
      if (i==n.value) {
        n.cnt++;
        if (n.cnt>max) max=n.cnt;
      }
      else {
        if (i>n.value) {
          n.left=add((Node)n.left, i);
          n=n.balanceLeft();
        }
        else {
          n.right=add((Node)n.right, i);
          n=n.balanceRight();
        }
      }
      return n;
    }

    public int get(int i)
    {
      Node n=(Node)root;
      while (n!=null) {
        if (n.value==i) return n.cnt;
        if (i>n.value)  n=n.left;
        else            n=n.right;
      }
      return 0;
    }

    public int getSize()
    {
      return nodecount;
    }

    public int histMax()
    { return max;
    }
    
    public int mapIndexToValue(int i)
    {
      createIndex();
      Node n=(Node)root;
      while (n!=null) {
        if (n.index==i) return n.value;
        if (i>n.index)  n=n.left;
        else            n=n.right;
      }
      return -1;
    }
    
    public int mapIndexToCount(int i)
    {
      createIndex();
      Node n=(Node)root;
      while (n!=null) {
        if (n.index==i) return n.cnt;
        if (i>n.index)  n=n.left;
        else            n=n.right;
      }
      return -1;
    }

    private void createIndex()
    {
      if (!indexed) {
        if (root!=null) ((Node)root).createIndex(0);
        indexed=true;
      }
    }
  }

  ///////////////////////////////////////////////////////////////
  // Array Implementation

  protected static class ArrayHistogram implements Histogram {
    private int[] histogram;

    public ArrayHistogram(int size)
    {
      histogram=new int[size];
    }

    public void add(int i)
    {
      histogram[i]++;
    }

    public int get(int i)
    {
      return histogram[i];
    }

    public int  getSize()
    {
      return histogram.length;
    }

    public int histMax()
    { int max=0;
      for (int i=0; i<histogram.length; i++) {
        if (histogram[i]>max) max=histogram[i];
      }
      return max;
    }

    public int mapIndexToValue(int i)
    {
      if (i>=histogram.length) return -1;
      return i;
    }

    public int mapIndexToCount(int i)
    {
      if (i>=histogram.length) return -1;
      return histogram[i];
    }
  }

  protected class StatisticRasterInfo extends RasterInfo {
    Histogram histogram;

    StatisticRasterInfo(MandelRaster r)
    { super(r);
    }

    @Override
    protected void analyseRaster(MandelRaster r)
    {
      super.analyseRaster(r);
      histogram=new TreeHistogram(getSize());

      int[][] raster=r.getRaster();

      for (int y=0; y<r.getRY(); y++) {
        for (int x=0; x<r.getRX(); x++) {
          int i=raster[y][x];
          if (i>0) {
            histogram.add(i-minIt);
          }
        }
      }
    }

    int histMax()
    { return histogram.histMax();
    }

    /////////////////////////////////////////////////////////////////////////
    // joining method
    /////////////////////////////////////////////////////////////////////////

    int[] points;
    int[] accu;
    int   last;

    int setupColors()
    {
      points=new int[histogram.getSize()];
      accu=new int[histogram.getSize()];
      last=-1;
      int cnt=0;

      int cur=histogram.getSize()-1;

      for (int i=histogram.getSize()-1; i>=0; i--) {
        if ((accu[i]=histogram.mapIndexToCount(i))>0) {
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
      int cnt;
      int min;
    }

    Pointer minUsedColor(Pointer p)
    {
      if (p.cnt>0 && p.min>0) {
        // try next points first
        int cur=points[p.prev];
        int prev=p.prev;

        p.cur=cur;
        while (cur>=0) {
          if (accu[cur]==p.min) {
            p.cur=cur;
            p.prev=prev;
            p.cnt--;
            return p;
          }
          prev=cur;
          cur=points[cur];
        }
      }
      return _minUsedColor(p);
    }

    Pointer _minUsedColor(Pointer p)
    {
      int cur=points.length-1;
      int prev=-1;

      p.cur=cur;
      p.prev=prev;
      p.cnt=0;
      p.min=0;

      while (cur>=0) {
        if (accu[cur]<=accu[p.cur]) {
          if (accu[cur]<accu[p.cur]) {
            p.cur=cur;
            p.prev=prev;
            p.min=accu[cur];
            p.cnt=1;
          }
          else {
            p.cnt++;
          }
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

//    int joinColor()
//    { Pointer p=minUsedColor();
//      return joinColor(p);
//    }

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

    int compressColors(int n, int[] mapping, MappingBuilder mb, BreakCondition c)
    {
      int num=setupColors();
      int max=histMax();
      int bound=(int)(limit*max);
      Pointer p=new Pointer();
      while (num>n) {
        p=minUsedColor(p);
        if (c!=null && c.done(num, accu[p.cur])) break;
        int cur=joinColor(p);
        if (bound-accu[cur]>0) {
          accu[cur]+=(bound-accu[cur])*factor;
        }
        num--;
      }
      if (num>n) mb.setTarget(num+1); // change colormap size for mapping
      //System.out.println("compresed "+num);
      if (debug) System.out.println("p(0)->"+points[0]);
      if (debug) System.out.println("p(1)->"+points[1]);
      if (debug) System.out.println("compressed");
      // all iteration values between two indices
      // ( curidx and points[curidx]) get the same color value
      // therefore the indices must be mapped to iteration values
      // for the final mapping table to support sparse histograms
      // This is done by the mapIndexToValue method of the histogram.

      if (usebuilder) {
        int curidx=points.length-1;
        n=num;
        while (curidx>=0) {
          mb.add(histogram.mapIndexToValue(curidx), n--);
          curidx=points[curidx];
        }
      }
      else {
        int curidx=points.length-1;
        int idx=curidx;
        int it=histogram.mapIndexToValue(curidx);
        int nextit=histogram.mapIndexToValue(points[curidx]);
        n=num;
        while (it>=0) {
          if (it==nextit) {
            curidx=points[curidx];
            nextit=histogram.mapIndexToValue(points[curidx]);
            n--;
          }
          if (n<=0) {
            throw new IllegalStateException(
                    "need more colors than expected (it="+idx+")");
          }
          //System.out.println("  "+it+": "+n);
          mapping[it--]=n;
        }
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
  { StatisticRasterInfo info=new StatisticRasterInfo(raster);
    int[] mapping=usebuilder?null:new int[info.getSize()];
    colmapsize=adjustColmapSize(colmapsize);
    MappingBuilder mb=new MappingBuilder(info.getMinIt(),info.getMaxIt(),
                                         colmapsize);
    int s=info.compressColors(colmapsize-1,mapping,mb,createBreakCondition(info))+1;
    if (debug) System.out.println("MAPPING: "+s);
    MappingRepresentation mr;
    if (usebuilder) {
      if (mb.getSourceSize()<2000000) mr=mb.createArrayMapping();
      else mr=mb.createTreeMapping();
    }
    else {
     mr=new ArrayMapping(mapping);
    }
    return new Mapping(mb,mr);
  }

  protected BreakCondition createBreakCondition(StatisticRasterInfo info)
  {
    return null;
  }

  protected int adjustColmapSize(int colmapsize)
  {
    return colmapsize;
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

  @Override
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
