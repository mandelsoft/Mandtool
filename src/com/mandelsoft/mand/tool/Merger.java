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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import static com.mandelsoft.mand.MandelInfo.ATTR_COMPOSED_REFS;
import static com.mandelsoft.mand.MandelInfo.ATTR_COMPOSED_TIME;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.MandelRasterAccess;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.meth.BigDecimalMandIterator;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.util.MandArith;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Supplier;


public abstract class Merger {
  
  public interface Progress {
    public void reportProgress(double p);
  } 
  
  public static class MergeBase {

    MandelData base;
    MandelInfo info;
    int rx;
    int ry;
    MandelRaster raster;
    HashSet<String> refs;
    QualifiedMandelName name;
    boolean modified;
    boolean cancelled;
    Supplier<Boolean> cancelCheck;
    
    Progress progress;

    public MergeBase(QualifiedMandelName name, MandelData data) {
      this.name=name;
      base = data;
      rx = data.getInfo().getRX();
      ry = data.getInfo().getRY();
      raster = base.getRaster();
      info = base.getInfo();
      modified=false;
      
      refs=new HashSet<String>();
      String s = info.getProperty(ATTR_COMPOSED_REFS);
      if (s != null) {
        for ( String ref : s.split(",")) {
          refs.add(ref.trim());
        }
      }
    }

    public void setProgressListener(Progress p)
    {
      this.progress=p;
    }
    
    public QualifiedMandelName getName()
    {
      return name;
    }
    
    public MandelInfo getInfo()
    {
      return info;
    }
    
    public MandelRaster getRaster()
    {
      return raster;
    }
    
    public boolean isModified()
    {
      return modified;
    }
    
    public void setCancelCheck(Supplier<Boolean> check) 
    {
      cancelCheck=check;
    }
    
    synchronized public void cancel()
    {
      cancelled=true;
    }
    
    synchronized public boolean isCancelled()
    {
      return cancelled || (cancelCheck != null && cancelCheck.get());
    }
    
    public String merge(Merger j) throws InterruptedException {
      if (!j.getBaseInfo().isSameSpec(info)) {
        return "non matching merger";
      }
      
      String err = j.check();
      if (err != null ) {
        return err;
      }
      
      if (progress != null) {
        progress.reportProgress(0.0);
      }
      System.out.printf("merging %s\n", j.getName());
      for (int y = 0; y < ry; y++) {
        for (int x = 0; x < rx; x++) {
          int d = raster.getData(x, y);
          int dj = j.join(x, y, d);

          if (d != dj) {
            modified=true;
            raster.setData(x, y, dj);
          }
        }
        if (progress != null) {
          progress.reportProgress((double)(y+1)/(double)(ry));
        }
        if (isCancelled()) {
          throw new InterruptedException("merge cancelled");
        }
      }
      refs.add(j.getName().getName());
      return null;
    }
    
    public void finish()
    {
      int min = base.getInfo().getLimitIt();
      int max = 0;
      int black = 0;
      int cnt = 0;

      for (int y = 0; y < ry; y++) {
        for (int x = 0; x < rx; x++) {
          int d = raster.getData(x, y);
          if (d == 0) {
            black++;
          } else {
            cnt += d;
            if (d < min) {
              min = d;
            }
            if (d > max) {
              max = d;
            }
          }
        }
      }
      
      if ( max > info.getLimitIt()) {
        info.setLimitIt(max+1);
      }

      info.setMaxIt(max);
      info.setMinIt(min);
      info.setNumIt(cnt);
      info.setMCnt(black);
      info.setMCCnt(0);
      System.out.printf("found %d mandel set pixels\n", black);
      info.setProperty(ATTR_COMPOSED_TIME, Long.toString(System.currentTimeMillis()));
      info.setProperty(ATTR_COMPOSED_REFS, String.join(",", refs));
    }

    public Merger createMerger(MandelHandle h) throws IllegalArgumentException {
      if (h.getHeader().hasRaster()) {
        try {
          if (h.getName().getMandelName().equals(name.getMandelName())) {
            if (Objects.equals(name.getVariant(), h.getName().getVariant()) && h.getName().getRefPrefix() != null) {
              System.out.printf("using variant merger for %s\n", h.getName());
              return new Merger.Variant(h, info);
            } else {
              throw new IllegalArgumentException(String.format("%s: skipped non-matching variant\n", h.getName()));
            }
          } else {
            System.out.printf("using sub area merger for %s\n", h.getName());
            return new Merger.SubArea(h, info);
          }
        } catch (IOException io) {
            throw new IllegalArgumentException(String.format("%s: %s", h.getName(), io.toString()));
        }
      }
      else {
        throw new IllegalArgumentException(String.format("%s: no raster available", h.getName()));
      }
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  protected MandelInfo getBaseInfo()
  { return base;
  }
  
  protected abstract String check();
  protected abstract int getData(int x, int y);
  
  protected int join(int x, int y, int o)
  {
    int d = getData(x,y);
    
    if (d < 0) {
      return o;
    }
    if (d >= baselimit) {
      return d;
    }
    if (o == 0 && d < baselimit) {
      return o;
    }
    if (d > 0 && d <= o) {
      return o;
    }
    return d;
  }
  
  protected QualifiedMandelName name;
  protected MandelInfo base;
  protected int baselimit;
  
  protected Merger(QualifiedMandelName name, MandelInfo base)
  {
    this.name=name;
    this.base=base;
    this.baselimit=base.getLimitIt();
  }
  
  public QualifiedMandelName getName()
  {
    return name;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  
  public static class Variant extends Merger {

    MandelData data;
    MandelRaster raster;

    public Variant(MandelHandle h, MandelInfo base) throws IOException {
      this(h.getName(), h.getData(), base);
    }
    
    public Variant(QualifiedMandelName name, MandelData data, MandelInfo base) {
      super(name, base);
      this.data = data;
      this.raster = data.getRaster();
    }

    @Override
    protected String check() {
      if (!data.getInfo().isSameVariant(base)) {
        return "variant mismatch";
      }
      if (base.getLimitIt() < data.getInfo().getLimitIt()) {
        base.setLimitIt( data.getInfo().getLimitIt());
      }
      return null;
    }
     
    @Override
    protected int getData(int x, int y)
    {
      return raster.getData(x, y);
    }
  }
  
   //////////////////////////////////////////////////////////////////////////////
  
  public static class SubArea extends Merger {
    MandelRasterAccess access;
    BigDecimalMandIterator it;
    boolean subst;
    MandelInfo info;
    
   
    public SubArea(MandelHandle h, MandelInfo base) throws IOException {
      this(h.getName(), h.getData(), base);
    }
     
    public SubArea(QualifiedMandelName name, MandelData data, MandelInfo base)
    {
      super(name, base);
      access = new MandelRasterAccess(data);
      this.info=data.getInfo();
      it = new BigDecimalMandIterator(base);
      
      if (MandArith.div(base.getDX(), (double) base.getRX()).compareTo(
              MandArith.div(info.getDX(), (double) info.getRX())) > 0
              && MandArith.div(base.getDY(), (double) base.getRY()).compareTo(
                      MandArith.div(info.getDY(), (double) info.getRY())) > 0) {

        System.out.printf("setting substitution mode for sub area %s\n", name);
        subst = true;
      }
      else {
         System.out.printf("setting merge mode for sub area %s\n", name);
      }
    }
    
    @Override
    protected String check() {
      if (!info.hasOverlapWith(base)) {
        return "no area overlap";
      }
      return null;
    }
     
    @Override
    protected int join(int x, int y, int o) {
      int d = getData(x, y);
      if (d < 0 || (!subst && d <= o)) {
        return o;
      }
      return d;
    }
     
    @Override
    protected int getData(int x, int y)
    {
      return access.getData(it.getCX(x), it.getCY(y));
    }
  }
}
