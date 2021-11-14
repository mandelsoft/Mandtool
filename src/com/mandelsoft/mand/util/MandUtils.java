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

package com.mandelsoft.mand.util;

import java.awt.Dimension;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelAreaSpec;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.ContextMandelScanner;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerUtils;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class MandUtils extends MandArith {
  static public final int PRECISION=3;

  static public int toInt(double d)
  {
    return (int)Math.round(d);
  }

  static public int toInt(long l)
  {
    return (int)l;
  }

  static public String time(int d)
  {
    int h=d/3600;
    d=d%3600;
    int m=d/60;
    d=d%60;
    if (h!=0) return h+":"+m+":"+d;
    return m+":"+d;
  }

  public static MandelName lookupRoot(MandelScanner scan, MandelAreaSpec spec)
  {
    return lookupRoot(scan,MandelName.ROOT,spec);
  }

  public static MandelName lookupRoot(MandelScanner scan,
                                      MandelName mn, MandelAreaSpec spec)
  {
    MandelName found=null;
    MandelData md;

    while (mn!=null) {
      found=mn;
      MandelName s=mn.sub();
      while (s!=null) {
        //System.out.println("checking "+s);
        try {
          MandelHandle handle=scan.getMandelInfo(s);
          if (handle!=null) {
            md=handle.getInfo();
            if (md!=null) {
              MandelInfo info=md.getInfo();
              if (info!=null) {
                if (info.contains(spec.getXM(), spec.getYM())) {
                  if (spec.getDX().compareTo(info.getDX())<0
                    ||spec.getDY().compareTo(info.getDY())<0) {
                    break;
                  }
                  if (info.isSameArea(spec)) return s;
                }
              }
            }
          }
        }
        catch (IOException ex) {
          // just try next
        }
        s=s.next();
      }
      mn=s;
    }
    return found;
  }

  public static MandelHandle lookupColormap(MandelScanner scan, MandelName n)
  {
    MandelHandle cm=null;
    MandelHandle mc=null; //always prefer explicit areacm
    MandelHandle mb=null; //then base (non-variant) areas

    System.out.println("lookup colormap for "+n);
    while (cm==null && n!=null) {
      Set<MandelHandle> set=scan.getMandelHandles(n);
      if (set!=null) {
        for (MandelHandle h:set) {
          if (h.getHeader().hasColormap()) {
            System.out.println("---- found cm "+h.getName());
            cm=h;
            if (h.getHeader().isAreaColormap()) {
              System.out.println("    ----> area cm");
              mc=h;
            }
            if (h.getName().getQualifier()==null) {
              System.out.println("    ----> base cm");
              mb=h;
              if (mc==h) break;
            }
          }
        } // for set
      }
      n=n.getParentName();
    }
    if (mc!=null) return mc;
    if (mb!=null) return mb;
    return cm;
  }

  public static File mapFile(AbstractFile f, String dstSuf, File d)
  {
    String n=f.getName();
    if (!n.endsWith(dstSuf)) {
      int ix=n.lastIndexOf('.');
      if (ix>=0) n=n.substring(0,ix)+dstSuf;
      else n=n+dstSuf;
    }
    if (d!=null) return new File(d,n);
    else {
      if (f.isFile())
        return new File(f.getFile().getParentFile(),n);
      return null;
    }
  }

  public static File mapToRasterName(AbstractFile f)
  {
    return mapFile(f,RASTER_SUFFIX,null);
  }

  public static File mapToInfoName(AbstractFile f)
  {
    return mapFile(f,INFO_SUFFIX,null);
  }

  public static File mapToRasterImageName(AbstractFile f)
  {
    return mapFile(f,RASTERIMAGE_SUFFIX,null);
  }

  public static File mapToImageName(AbstractFile f)
  {
    return mapFile(f,IMAGE_SUFFIX,null);
  }



  public static File mapToRasterFile(AbstractFile f, File target)
  { return mapFile(f,RASTER_SUFFIX, target);
  }

  public static File mapToInfoFile(AbstractFile f, File target)
  { return mapFile(f,INFO_SUFFIX, target);
  }

  public static File mapToRasterImageFile(AbstractFile f, File target)
  { return mapFile(f,RASTERIMAGE_SUFFIX, target);
  }

  public static File mapToImageFile(AbstractFile f, File target)
  { return mapFile(f,IMAGE_SUFFIX, target);
  }



  static public Set<MandelName> getSubNames(MandelName n, Set<MandelName> set)
  { Set<MandelName> sub=new HashSet<MandelName>();

    for (MandelName s:set) {
      if (n.equals(s.getParentName())) sub.add(s);
    }
    return set;
  }

  static public boolean hasSubNames(MandelName n, Set<MandelName> set)
  {
    for (MandelName s:set) {
      if (s.isChildOf(n)) return true;
      // if (n.equals(s.getParentName())) return true;
    }
    return false;
  }

  
  public static Set<MandelName> getSubNames(MandelName n, MandelScanner scan)
  {
    if (scan instanceof ContextMandelScanner) {
      return ((ContextMandelScanner)scan).getSubNames(n);
    }
    else {
      return MandelScannerUtils.getSubNames(n, null, scan);
    }
  }

  public static boolean hasSubNames(MandelName n, MandelScanner scan)
  {
    if (scan instanceof ContextMandelScanner) {
      return ((ContextMandelScanner)scan).hasSubNames(n);
    }
    else {
      return MandelScannerUtils.hasSubNames(n, null, scan);
    }
  }


  public static boolean hasSubNames(MandelName n, MandelScanner scan,
                                    MandelScanner.Filter f)
  {
    if (scan instanceof ContextMandelScanner) {
      return ((ContextMandelScanner)scan).hasSubNames(n,f);
    }
    else {
      return MandelScannerUtils.hasSubNames(n, null, scan, f);
    }
  }


  static public MandelName getNextSubName(MandelName n, Set<MandelName> set)
  { MandelName s=n.sub();
    while (s!=null && set.contains(s)) {
      s=s.next();
    }
    return s;
  }

  static public MandelName getNextSubName(MandelName n, MandelScanner scan)
  { return getNextSubName(n,scan,true);
  }

  static public MandelName getNextSubName(MandelName n, MandelScanner scan,
                                          boolean rescan)
  { MandelName s=n.sub();
    if (rescan) scan.rescan(false);
    while (s!=null && scan.getMandelInfo(s)!=null) {
      s=s.next();
    }
    return s;
  }

  static public MandelName getNextName(MandelName n, MandelScanner scan)
  { return getNextName(n,scan,true);
  }

  static public MandelName getNextName(MandelName s, MandelScanner scan,
                                          boolean rescan)
  {
    if (rescan) scan.rescan(false);
    s=s.next();
    while (s!=null && scan.getMandelInfo(s)!=null) {
      s=s.next();
    }
    return s;
  }

  static public MandelHandle better(MandelHandle a,
                                    MandelHandle b)
  {
    if (a==null) return b;
    if (b==null) return a;

    MandelHandle r=a;
    if (b.getHeader().hasRaster()&&!a.getHeader().hasRaster()) a=b;
    if (b.getHeader().hasModifiableImage()&&!a.getHeader().hasModifiableImage()) a=b;
   
    if (b.getQualifier()==null
      &&a.getQualifier()!=null) a=b;
    if (b.getHeader().hasAdditional(a.getHeader().getType())
      &&(b.getQualifier()==null)
      ==(a.getQualifier()==null)) a=b;

    if (a==r && a.getName().equals(b.getName())) {
      try {
        MandelData d1=a.getInfo();
        MandelData d2=b.getInfo();
        if (d2.getInfo().getLimitIt()>d1.getInfo().getLimitIt()) a=b;
      }
      catch (IOException ex) {
        System.out.println("error reading info: "+ex);
      }
    }
    return a;
  }
  
  static public void adjustMandelInfo(MandelInfo info, Dimension raster)
  {
    BigDecimal v;

    if (raster.getWidth()/raster.getHeight() >
        ((double)info.getRX())/info.getRY()) {
      v=mul(info.getDY(),info.getRX());
      v=div(v,info.getRY());
      v=mul(v,raster.getHeight());
      v=div(v,raster.getWidth());
      info.setDY(v);
    }
    else {
      v=mul(info.getDX(),info.getRY());
      v=div(v,info.getRX());
      v=mul(v,raster.getWidth());
      v=div(v,raster.getHeight());
      info.setDX(v);
    }
    info.setRX(toInt(raster.getWidth()));
    info.setRY(toInt(raster.getHeight()));
  }
  
  
  static public void normalizeImageSize(MandelInfo info)
  {
    if (div(info.getDX(),info.getDY()).doubleValue() >
            ((double)info.getRX())/info.getRY()) {
      adjustHeight(info);
    }
    else {
      adjustWidth(info);
    }
  }
  
  static public void adjustHeight(MandelInfo info)
  {
    info.setRY((int)(info.getRX()*
                       div(info.getDY(),info.getDX()).doubleValue()));
  }
  
  static public void adjustWidth(MandelInfo info)
  {
    info.setRX((int)(info.getRY()*
                       div(info.getDX(),info.getDY()).doubleValue()));
  }

  static public void normalize(MandelInfo info)
  {
    if (div(info.getDX(),info.getDY()).doubleValue() >
            ((double)info.getRX())/info.getRY()) {
      adjustDY(info);
    }
    else {
      adjustDX(info);
    }
  }

  static public void adjustDX(MandelInfo info)
  {
    info.setDX(mul(info.getDY(),div(info.getRX(),info.getRY())));
  }

  static public void adjustDY(MandelInfo info)
  {
    info.setDY(mul(info.getDX(),div(info.getRY(),info.getRX())));
  }

  static public void round(MandelInfo info)
  {
    round(info,PRECISION);
  }

  static public void round(MandelInfo info, int r)
  {
    info.setXM(round(info.getXM(),info.getDX(),info.getRX(),r));
    info.setYM(round(info.getYM(),info.getDY(),info.getRY(),r));

    info.setDX(round(info.getDX(),info.getDX(),info.getRX(),r));
    info.setDY(round(info.getDY(),info.getDY(),info.getRY(),r));
  }

  static public int getMagnification(MandelInfo i)
  {
    BigDecimal d=div(i.getDY(),3.0);  // initial size of typical root
    int m=0;
    while (d.compareTo(BigDecimal.ONE)<0) {
      d=d.scaleByPowerOfTen(1);
      m++;
    }
    return m;
  }

  static public double getProportion(int rx, int ry, MandelInfo cur)
  {
    return div(cur.getDY(), cur.getDX()).doubleValue()*
               cur.getRX()/cur.getRY()*rx/ry;
  }
  
  static public MandelInfo createRoot()
  {
    MandelInfo mi=new MandelInfo();
    mi.setXM(-0.55);
    mi.setYM(0);
    mi.setDX(4);
    mi.setDY(4);
    mi.setLimitIt(64000);
    mi.setRX(1000);
    mi.setRY(1000);
    return mi;
  }

  static public boolean isAbove(MandelName name, MandelList list)
  { 
    for (QualifiedMandelName n:list) {
      if (name.isAbove(n.getMandelName())) return true;
    }
    return false;
  }

  static public boolean isAbove(MandelName name, MandelListFolder list)
  { 
    for (QualifiedMandelName n:list.allentries()) {
      if (name.isAbove(n.getMandelName())) return true;
    }
    return false;
  }
}
