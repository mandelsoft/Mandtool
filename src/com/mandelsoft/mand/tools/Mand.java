/*
 *  Copyright 2021 Uwe Krueger.
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
package com.mandelsoft.mand.tools;

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandIter;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelException;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.io.FileAbstractFile;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.meth.PixelIterator;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.io.FolderLock;
import com.mandelsoft.io.MappedIntMatrix;
import com.mandelsoft.mand.Coord;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.util.IntMatrix;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class Mand extends Command implements PixelIterator.PropertySource.PropertyHandler {

  public static final String ATTR_REFREDO = MandelInfo.ATTR_REFREDO;

  public static class ShutdownException extends RuntimeException {

  }

  static public final double BOUND = 10;
  private MandelData old;
  private File mapFile;
  private File file;
  private File save;
  private QualifiedMandelName name;
  private MandelData md;
  private MandelInfo mi;
  private PixelIterator pi;
  private int limit;
  private MandelRaster raster;
  private Filter filter;
  private Environment env;
  private long start;

  public Mand(MandelData md, QualifiedMandelName n) {
    this.md = md;
    this.name = n;
    this.mi = md.getInfo();
    this.limit = mi.getLimitIt();
  }

  public Mand(MandelData md, QualifiedMandelName n, Environment env)
          throws IOException {
    this(md, n);
    this.env = env;
    this.file = env.mapToRasterFile(md.getFile());
    this.save = env.mapToIncompleteFile(md.getFile());
  }

  public Mand(File f, Environment env) throws IOException {
    this(new MandelData(f), QualifiedMandelName.create(f), env);
  }

  public Mand(MandelData md, MandelData old, File out, QualifiedMandelName n,
          Environment env) throws IOException {
    this(md, n, env);
    if (out != null) {
      this.file = out;
    }
    setOld(old);
}

  public final void setMapFile(File file)
  { mapFile=file;
  }
  
  public final void setOld(MandelData old) throws IOException
  {
    this.old=old;
    if (old != null && old.getFile().isFile()) {
      md.setRaster(old.getRaster());
      md.setMapper(ResizeMode.RESIZE_LOCK_COLORS, old.getMapper());
      md.getInfo().setSite(old.getInfo().getSite());
      md.getInfo().setCreator(old.getInfo().getCreator());
      md.getInfo().setLocation(old.getInfo().getLocation());
      md.getInfo().setName(old.getInfo().getName());
      md.getInfo().setTime(old.getInfo().getTime());
      md.getInfo().setProperties(old.getInfo().getProperties());
      if (!old.isIncomplete()) { // keep old file name
        this.file = old.getFile().getFile();
      }
    }
  }

  void prepareMapFile() throws IOException
  {
    File f = mapFile;
    if (f != null) {
      if (!f.exists()) {
        new FileOutputStream(mapFile).close();
      }
    } else {
      f = mapFileName(name);
    }

    if (f.exists()) {
      boolean initial = f.length() == 0;
      if (!initial && f.length() != mi.getRX() * mi.getRY() * Integer.BYTES) {
        throw new IOException("invalid size of map file");
      }
      System.out.printf("using map file %s\n", f.getName());
      IntMatrix  m = new MappedIntMatrix(f, mi.getRX(), mi.getRY());
      md.setRaster(new MandelRaster(m));
      if (old!=null && old.getRaster() != null && initial) {
        IntMatrix.copy(old.getRaster().getRaster(), m);
      }
      mapFile = f;
    } else {
      mapFile=null;
    }
  }
  
  void cleanupMapFile()
  {
     if (mapFile != null) {
      md.setRaster(null);
      System.gc();
      System.runFinalization();

      System.out.printf("deleting map file %s\n", mapFile.getName());
      mapFile.delete();
    }
  }
  void setFilter(Filter filter) {
    this.filter = filter;
  }

  public File getFile() {
    return file;
  }

  public boolean isAborted() {
    return aborted;
  }

  public void check() throws IOException {
    int rx = md.getRaster().getRX();
    int ry = md.getRaster().getRY();

    MandelData tmp = new MandelData(file);
    if (rx != tmp.getRaster().getRX()) {
      throw new MandelException("rx mismatch: "
              + tmp.getRaster().getRX() + "!="
              + rx);
    }
    if (ry != tmp.getRaster().getRY()) {
      throw new MandelException("ry mismatch: "
              + tmp.getRaster().getRY() + "!="
              + ry);
    }

    MandelRaster tmpraster = tmp.getRaster();
    for (int y = 0; y < ry; y++) {
      for (int x = 0; x < rx; x++) {
        if (raster.getData(x, y) != tmpraster.getData(x, y)) {
          throw new MandelException("content mismatch");
        }
      }
    }
  }

  public boolean calculate() throws IOException {
    setupContext();
    if (!filterName(filter, name, pi, env)) {
      return false;
    }
    
    System.out.println(new Date() + ": " + (md.getRaster() == null ? "" : "re")
            + "calculating " + (file == null ? "" : file) + "... (" + name + ")");
    if (md.getRaster() != null && md.getInfo().getMaxIt() > limit) {
      System.out.println("nothing to be done");
      return true;
    }

    prepareMapFile();
    raster = md.createRaster();
    start = System.currentTimeMillis();
    PixelIterator.setup(pi);
    addTime();
    start = 0;
    try {
      calc2();
      finalizeBlack();
    } catch (ShutdownException ex) {
      aborted = true;
    } finally {
      saveContext();
      PixelIterator.cleanup(pi);
    }
    return true;
  }

  public Redo createRedo() {
    if (mi.hasProperty(ATTR_REFREDO)) {
      try {
        Coord.parse(mi.getProperty(ATTR_REFREDO));
        return new Redo(false);
      } catch (NumberFormatException ex) {
        System.out.println("complete redo requested");
      }
    }
    return null;
  }

  private class Redo {

    private boolean redoStarted;
    private boolean redoStopped;
    private int redoX;
    private int redoY;
    private long redoCount;

    public Redo(boolean local) {
      redoStopped = redoStarted = false;
      redoX = redoY = 0;
      redoCount = 0;

      if (mi.hasProperty(MandelInfo.ATTR_REFCNT)) {
        String c = mi.getProperty(MandelInfo.ATTR_REFCNT);
        try {
          redoCount = Long.parseUnsignedLong(c) - 1;
        } catch (NumberFormatException ex) {
        }
      }

      if (mi.hasProperty(ATTR_REFREDO)) {
        try {
          Coord c = Coord.parse(mi.getProperty(ATTR_REFREDO));
          redoX = c.getX().intValue();
          redoY = c.getY().intValue();
          System.out.printf("found last successful redo at (%d,%d) (done %d/%d)\n", redoX, redoY, ready, redoCount);
        } catch (NumberFormatException ex) {
          if (!local) {
            System.out.printf("start complete redo with first pixel\n");
            pixelX = pixelY = -1;
            redoStarted = true;
          }
        }
      } else {
        System.out.printf("start redo with first pixel (%d pixels)\n", redoCount);
        redoStarted = true;
      }

      if (pixelX > 0) {
        System.out.printf("redo will be stopped at ref pixel (%d,%d)\n", pixelX, pixelY);
      }
    }

    public boolean isStarted() {
      return redoStarted;
    }

    public boolean isStopped() {
      return redoStopped;
    }

    public boolean isActive() {
      return redoStarted && !redoStopped;
    }

    public boolean process(int x, int y) {
      boolean active = isActive();
      if (!redoStarted && x == redoX && y == redoY) {
        System.out.printf("start redo after (%d,%d)\n", x, y);
        redoStarted = true;
        // don't repeat last successful redo pixel
      }
      if (x == pixelX && y == pixelY) {
        System.out.printf("stop redo at (%d,%d)\n", x, y);
        redoStopped = true;
        return false; // don't repeat ref pixel
      }
      if (active) {
        redoX = x;
        redoY = y;
      }
      return active;
    }

    public void saveContext() {
      if (redoX > 0 || redoY > 0) {
        mi.setProperty(ATTR_REFREDO, new Coord(redoX, redoY).toString());
      } else {
        cleanup();
      }
    }

    public void cleanup() {
      mi.removeProperty(ATTR_REFREDO);
      mi.removeProperty(MandelInfo.ATTR_REFCNT);
    }

    public double estimate() {
      long done;
      long max = mi.getRY() * mi.getRX() + redoCount;
      if (pixelX == 0 && pixelY == 0) {
        done = ready;
      } else {
        done = mi.getRY() * mi.getRX() + ready;
      }
      return (double) (done * 100) / max;
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // calculation
  ////////////////////////////////////////////////////////////////////////
  private int rx;
  private int ry;

  private int min;
  private int max;
  private long cnt;

  private int mccnt;
  private int mcnt;
  private long ccnt;

  private int kept = 0;
  private int pixelX;
  private int pixelY;
  private Redo redo;

  private boolean refmod;
  private int refdepth;
  private int redolastdepth;
  private int redonumber; // number of redos done

  private void setupContext() {
    pi = MandIter.createPixelIterator(mi, this);

    rx = mi.getRX();
    ry = mi.getRY();
    min = mi.getMinIt();
    if (min == 0) {
      min = limit; // not yet calculated
    }
    max = mi.getMaxIt();
    resetContext();
    redo = createRedo();
    if (redo == null && mi.hasProperty(MandelInfo.ATTR_REFPIXEL)) {
      System.out.println("remember ref modified");
      refmod = true; // enable required redo
    }
    if (mi.hasProperty(MandelInfo.ATTR_REDONUMBER)) {
      try {
        redonumber = Integer.parseInt(mi.getProperty(MandelInfo.ATTR_REDONUMBER));
        if (redonumber > 0) {
          System.out.printf("redo number %d\n", redonumber);
        }
      } catch (NumberFormatException ex) {
        redonumber = 0;
      }
    }
    if (mi.hasProperty(MandelInfo.ATTR_REDOLASTDEPTH)) {
      try {
        redolastdepth = Integer.parseInt(mi.getProperty(MandelInfo.ATTR_REDOLASTDEPTH));
        if (redolastdepth > 0) {
          System.out.printf("last redo depth %d\n", redolastdepth);
        }
      } catch (NumberFormatException ex) {
        redolastdepth = 0;
      }
    }
  }

  private void resetContext() {
    cnt = 0;
    ccnt = 0;
    mccnt = 0;
    mcnt = 0;
    ready = 0;
    refmod = false;

    if (mi.hasProperty(MandelInfo.ATTR_REFPIXEL)) {
      try {
        Coord p = Coord.parse(mi.getProperty(MandelInfo.ATTR_REFPIXEL));
        pixelX = p.getX().intValue();
        pixelY = p.getY().intValue();
      } catch (NumberFormatException ex) {
        pixelX = pixelY = 0;
      }
    }
  }

  private void addTime() {
    if (start > 0) {
      long end = System.currentTimeMillis();
      mi.setTime(mi.getTime() + (int) ((end - start) / 1000));
      mi.setRasterCreationTime(end);
      start = end;
    }
  }

  private void saveContext() {
    addTime();

    mi.setMinIt(min);
    mi.setMaxIt(max);
    mi.setNumIt(cnt);

    mi.setMCnt(mcnt);
    mi.setMCCnt(mccnt);
    if (redolastdepth > 0) {
      mi.setProperty(MandelInfo.ATTR_REDOLASTDEPTH, Integer.toString(redolastdepth));
    }
    if (redonumber > 0) {
      mi.setProperty(MandelInfo.ATTR_REDONUMBER, Integer.toString(redonumber));
    }
  }

  private void calc1() {
    for (int y = 0; y < ry; y++) {
      pi.setY(y);
      for (int x = 0; x < rx; x++) {
        pi.setX(x);
        handle(x, y);
      }
    }
  }

  private void finalizeBlack() {
    int mcnt = 0;
    for (int y = 0; y < ry; y++) {
      for (int x = 0; x < rx; x++) {
        int it = raster.getData(x, y);
        if (it > limit) {
          raster.setData(x, y, it = 0);
        }
        if (it == 0) {
          mcnt++;
        }
      }
    }
    System.out.printf(mcnt + " black pixels\n");
    this.mcnt = mcnt;
  }

  private boolean aborted;
  private long lastbackup = System.currentTimeMillis();
  private long lastcheck = System.currentTimeMillis();

  private long ready = 0;
  static private final long TIMEOUT = 1000 * 60 * 20; // 20 min
  static private final File shutdown = new File("shutdown");

  int handle(int x, int y) {
    int it = raster.getData(x, y);
    boolean force = redo != null && redo.process(x, y);
    if (it == 0 || force) {
      if (kept > 0) {
        // System.out.printf("kept %d points\n", kept);
        kept = 0;
      }
      if (start == 0) {
        start = System.currentTimeMillis();
        System.out.printf("start calculation (kept %d points)\n", ready);
      }
      ccnt++;
      int i = pi.iter();
      raster.setData(x, y, it = i);
      if (i > limit) {
        mccnt++;
        mcnt++;
        i--;
      }
      if (i < min) {
        min = i;
      }
      if (i > max) {
        max = i;
      }
    } else {
      kept++;
      if (it > limit) {
        mccnt++;
        mcnt++;
      }
    }
    cnt += it;
    ready++;
    long cur = System.currentTimeMillis();
    if (cur > lastcheck + TIMEOUT / 2) {
      lastcheck = cur;
      if (shutdown.exists()) {
        throw new ShutdownException();
      }
    }
    if (cur > lastbackup + TIMEOUT) {
      lastbackup = cur;
      if (start > 0) {
        if (redo != null) {
          redo.saveContext();
        }
        saveContext();
        try {
          write(false, true);
        } catch (IOException io) {
          System.out.println("cannot save intermediate state: " + io);
        }
        start = System.currentTimeMillis();
      }
    }
    /*
    if (redo) {
      System.out.printf("(%d,%d)=%d\n", x,y, it);
    }
     */
    return it;
  }

  private void calc2() {
    _calc();

    while (refmod) {
      mi.setProperty(MandelInfo.ATTR_REFCORRUPTED, null);
      resetContext();
      redonumber++;;
      redolastdepth = refdepth;
      redo = new Redo(true);
      _calc();
      System.out.printf("recalculated %d{%d} pixels\n", ccnt, ready);

      if (!redo.isStarted()) {
        System.out.printf("redo start pixel (%d,%d) not met\n", redo.redoX, redo.redoY);
      }
      if (redo.isActive()) {
        System.out.printf("redo ref pixel (%d,%d) not met\n", pixelX, pixelY);
      }
      if (refmod) {
        if (redolastdepth >= refdepth) {
          System.out.printf("redo caused re-ref with lower depth %d <= %d\n", refdepth, redolastdepth);
          break;
        }
        System.out.printf("redo caused re-ref with higher depth %d > %d -> repeat redo\n", refdepth, redolastdepth);
      }
      redo.cleanup();
      redo = null;
    }
    mi.removeProperty(MandelInfo.ATTR_REDOLASTDEPTH);
    if (redonumber > 0) {
      System.out.printf("required %d redo(s)\n", redonumber);
    }
  }

  private void _calc() {
    int u = calcHLine(0, 0, rx);
    calcHLine(0, ry - 1, rx);
    calcVLine(0, 1, ry - 2);
    calcVLine(rx - 1, 1, ry - 2);
    calcBox(u, 0, 0, rx, ry);
  }

  private int calcHLine(int sx, int sy, int n) {
    pi.setX(sx);
    pi.setY(sy);
    int u = handle(sx, sy);

    for (int x = sx + 1; x < sx + n; x++) {
      pi.setX(x);
      int it = handle(x, sy);
      if (it != u) {
        u = -1;
      }
    }
    return u;
  }

  private int calcVLine(int sx, int sy, int n) {
    pi.setX(sx);
    pi.setY(sy);
    int u = handle(sx, sy);

    for (int y = sy + 1; y < sy + n; y++) {
      pi.setY(y);
      int it = handle(sx, y);
      if (it != u) {
        u = -1;
      }
    }
    return u;
  }

  private void calcBox(int u, int sx, int sy, int nx, int ny) {
    //System.out.println("calcBox "+sx+","+sy+"("+nx+"x"+ny+")");
    if (nx <= 2 || ny <= 2) {
      return;
    }

    boolean done = redo == null || redo.isStopped();
    for (int y = sy + ny - 1; y >= sy; y--) {
      for (int x = sx + nx - 1; x >= sx; x--) {
        if (raster.getData(x, y) == 0) {
          done = false;
          break;
        }
      }
    }
    if (done) {
      ready += (nx - 2) * (ny - 2);
      return;
    }

    if (u >= 0) {
      u = checkHLine(u, sx, sy, nx);
      if (u >= 0) {
        u = checkHLine(u, sx, sy + ny - 1, nx);
        if (u >= 0) {
          u = checkVLine(u, sx, sy + 1, ny - 2);
          if (u >= 0) {
            u = checkVLine(u, sx + nx - 1, sy + 1, ny - 2);
            if (u >= 0) {
              fillBox(sx + 1, sy + 1, nx - 2, ny - 2, u);
              return;
            }
          }
        }
      }
    }
    if (nx > ny) {
      // divide horizontally
      int s = (nx - 1) / 2;
      if (s != 0) {
        //System.out.println("s="+s);
        u = calcVLine(sx + s, sy + 1, ny - 2);
        calcBox(u, sx, sy, s + 1, ny);
        calcBox(u, sx + s, sy, nx - s, ny);
      }
    } else {
      // divide vertically
      int s = (ny - 1) / 2;
      if (s != 0) {
        u = calcHLine(sx + 1, sy + s, nx - 2);
        calcBox(u, sx, sy, nx, s + 1);
        calcBox(u, sx, sy + s, nx, ny - s);
      }
    }
  }

  private int checkHLine(int u, int sx, int sy, int n) {
    if (u >= 0) {
      for (int x = sx; x < sx + n; x++) {
        if (raster.getData(x, sy) != u) {
          return -1;
        }
      }
    }
    return u;
  }

  private int checkVLine(int u, int sx, int sy, int n) {
    if (u >= 0) {
      for (int y = sy; y < sy + n; y++) {
        if (raster.getData(sx, y) != u) {
          return -1;
        }
      }
    }
    return u;
  }

  private void fillBox(int sx, int sy, int nx, int ny, int u) {
    //System.out.println("fill "+sx+","+sy+"("+nx+"x"+ny+") with "+u);
    for (int y = sy; y < sy + ny; y++) {
      for (int x = sx; x < sx + nx; x++) {
        raster.setData(x, y, u);
      }
    }
    if (u == 0) {
      mcnt += nx * ny;
    }
    ready += nx * ny;
  }

  ////////////////////////////////////////////////////////////////////////
  // iteration for pixel
  ////////////////////////////////////////////////////////////////////////
  private int iter(double x, double y, double px, double py) {
    double x2 = x * x;
    double y2 = y * y;
    int it = 0;

    while (x2 + y2 < BOUND && ++it <= limit) {
      double xn = x2 - y2 + px;
      double yn = 2 * x * y + py;
      x = xn;
      x2 = x * x;
      y = yn;
      y2 = y * y;
    }
    return it;
  }

  @Override
  public void updateProperties(Map<String, String> props) {
    if (props != null) {
      String d = props.get(MandelInfo.ATTR_REFDEPTH);
      if (d != null) {
        try {
          refdepth = Integer.parseInt(d);
        } catch (NumberFormatException e) {
        }
      }
      if (redo == null || redolastdepth < refdepth) {
        if (redo != null) {
          System.out.printf("found deeper pixel than current ref (%d > %d) -> skip stop pixel\n", refdepth, redolastdepth);
          pixelX = pixelY = 0;
        }
        for (Map.Entry<String, String> e : props.entrySet()) {
          mi.setProperty(e.getKey(), e.getValue());
        }
        if (props.containsKey(MandelInfo.ATTR_REFCOORD)) {
          String point = props.get(MandelInfo.ATTR_REFPIXEL);
          if (point == null) {
            point = "";
          } else {
            point = " " + point;
          }
          System.out.printf("update attributes for calculation %d{%d}%s\n", ccnt, ready, point);
          if (ccnt != 0) {
            refmod = true;
          }
          mi.setProperty(MandelInfo.ATTR_REFCNT, String.format("%d", ready + 1));
        }
      } else {
        mi.setProperty(MandelInfo.ATTR_REFCORRUPTED, "true");
        System.out.println("skip attribute update in redo mode");
      }
    }
  }

  public void write() throws IOException {
    write(true);
  }

  public void write(boolean verbose) throws IOException {
    write(verbose, aborted);
  }

  public void write(boolean verbose, boolean incomplete) throws IOException {
    if (file == null) {
      throw new IOException("no file specified");
    }
    md.setIncomplete(incomplete);
    if (incomplete) {
      write(save, verbose);
    } else {
      write(file, verbose);
      save.delete();
    }
  }

  public void write(File f) throws IOException {
    write(f, true);
  }

  public void write(File f, boolean verbose) throws IOException {
    MandelInfo info = md.getInfo();

    md.write(f, verbose);

    double p;
    String msg;
    if (redo == null) {
      p = (double) (ready * 100) / info.getRY() / info.getRX();
      msg = "";
    } else {
      p = redo.estimate();
      msg = " redo";
    }
    if ((int) p >= 100 || p == 0) {
      System.out.println(new Date() + ": " + f + msg + " done: " + (int) p + "% " + MandUtils.time(info.getTime()));
    } else {
      int r = (int) (info.getTime() * (100 - p) / p);
      System.out.println(new Date() + ": " + f + msg + " done: " + (int) p + "% " + MandUtils.time(info.getTime()) + " estimated: total: " + MandUtils.time(r + info.getTime()) + " => rest: " + MandUtils.time(r));
    }
  }

  private static class Filter {

    Set<MandelName> prefix;
    boolean fast;
    boolean variants;
    int limit;
    boolean isOptimized;

    public void addPrefix(MandelName name) {
      if (prefix == null) {
        prefix = new HashSet<MandelName>();
      }
      prefix.add(name);
    }

    public void setLimit(int limit) {
      this.limit = limit;
    }

    private boolean filterPrefix(MandelName name) {
      if (prefix == null) {
        return true;
      }
      for (MandelName p : prefix) {
        if (p.isAbove(name)) {
          return true;
        }
      }
      return false;
    }

    public boolean filter(QualifiedMandelName name, PixelIterator pi,
            MandelInfo parent) {
      if (!filterPrefix(name.getMandelName())) {
        return false;
      }
      if (variants && name.getQualifier() == null) {
        return false;
      }
      
      if (fast) {
        if (!isOptimized && !pi.isFast()) {
          return false;
        }
        int tlimit = (limit > 0) ? limit : 300;
        if (!pi.isFast() && (parent == null || parent.getTime() > tlimit) || name.getQualifier() != null) {
          return false;
        }
      }
      if (limit > 0) {
        if (parent == null) {
          System.out.printf("no parent for %s\n", name);
          return false;
        }
        System.out.printf("parent %s time %s\n", name.getMandelName().getParentName(), MandUtils.time(parent.getTime()));
        if (limit > 0 && (parent.getTime() > limit || name.getQualifier() != null)) {
          return false;
        }
      }
      return true;
    }
  }

  static public void main(String[] args) {
    int c = 0;
    boolean sflag = false; // server mode
    boolean cflag = false;
    boolean dflag = false; // delete obsolete
    boolean mflag = false; // use map file
    Filter filter = new Filter();

    Set<File> files = new HashSet<File>();

    while (args.length > c && args[c].charAt(0) == '-') {
      String arg = args[c++];
      for (int i = 1; i < arg.length(); i++) {
        char opt;
        switch (opt = arg.charAt(i)) {
          case 'o':
            MandIter.optimized = true;
            filter.isOptimized = true;
            break;
          case 's':
            sflag = true;
            break;
          case 'm':
            mflag = true;
            break;  
          case 'd':
            dflag = true;
            break;
          case 'c':
            cflag = true;
            break;
          case 'f':
            filter.fast = true;
            break;
          case 'v':
            filter.variants = true;
            break;
          case 'p':
            if (args.length > c) {
              MandelName mn = MandelName.create(args[c++]);
              if (mn == null) {
                Error("illegal mandel name '" + args[c - 1] + "'");
              }
              filter.addPrefix(mn);
            } else {
              Error("name prefix missing");
            }
            break;
          case 'l':
            if (args.length > c) {
              try {
                int t = Integer.parseInt(args[c++]);
                filter.setLimit(t * 60);
              } catch (NumberFormatException e) {
                Error("invalid time limt");
              }
            } else {
              Error("name prefix missing");
            }
            break;
          default:
            Error("illegal option '" + opt + "'");
        }
      }
    }

    while (args.length > c) {
      files.add(new File(args[c++]));
    }

    if (sflag) {
      service(dflag, mflag, filter);
    } else {
      try {
        Environment env = new Environment(null);
        for (File fi : files) {
          System.out.printf("handle file %s\n", fi);
          FolderLock lock;
          AbstractFile f = new FileAbstractFile(fi);
          try {
            lock = MandelFolder.getMandelFolder(f.getFile().getParentFile());
            if (!lock.lock()) {
              continue;
            }
            try {
              if (!f.tryLock()) {
                continue;
              }
            } finally {
              lock.releaseLock();
            }
          } catch (IOException ex) {
            System.out.printf("error getting locks for %s: %s\n", fi, ex);
            return;
          }

          try {
            System.out.println("got lock for " + f);
            QualifiedMandelName name = QualifiedMandelName.create(f);
            MandelData req = new MandelData(f);
            Mand m;
            if (req.getHeader().hasImageData()) {
              System.out.printf("  found image data\n");
              m = new Mand(fi, env);
            } else {
              System.out.printf("  checking old\n");
              MandelInfo reqd = req.getInfo();
              MandelData old = checkOld(env.getImageDataScanner(), name, reqd,
                      "requested " + reqd.getLimitIt() + " found");
              File file = null;
              if (old != null) {
                old.getInfo().removeProperty(MandelInfo.ATTR_REFPIXEL);
                old.getInfo().removeProperty(MandelInfo.ATTR_REFCNT);
                if (reqd.hasProperty(ATTR_REFREDO)) {
                  System.out.println(f + " redo already existing image");
                  file = old.getFile().getFile();
                  reqd.setCreationTime(old.getInfo().getCreationTime());
                  old = null;
                } else {
                  if (old.getInfo().getLimitIt() >= reqd.getLimitIt()) {
                    System.out.println(f + " skipped");
                    lock.lock();
                    try {
                      f.releaseLock();
                      if (dflag) {
                        cleanupInfo(env, f);
                      }
                    } finally {
                      lock.releaseLock();
                    }
                    continue;
                  }
                }
              }

              MandelData incomplete = checkOld(env.getIncompleteScanner(), name, reqd, "resuming");
              if (incomplete != null) {
                old = incomplete;
              }
              if (mflag) {
                File mm = mapFileName(name);
                if (!mm.exists()) {
                  new FileOutputStream(mm).close();
                }
              }
              m = new Mand(req, old, file, name, env);
            }

            if (!m.calculate()) {
              continue;
            }
            m.write();
            if (!m.aborted) {
              File file = mapFileName(name);
              if (file.exists()) {
                System.out.printf("deleting map file %s\n", file.getName());
                file.delete();
              }
            }
            if (cflag) {
              try {
                m.check();
              } catch (Exception e) {
                e.printStackTrace(System.err);
                Error("check failed: " + e);
              }
            }
          } catch (IOException ex) {
            Error("cannot handle " + f + ": " + ex);
          } finally {
            try {
              f.releaseLock();
            } catch (IOException ex) {
            }
          }
        }
      } catch (IllegalConfigurationException ex) {
        Error("illegal config: " + ex);
      }
    }
  }

  static boolean filterName(Filter filter, QualifiedMandelName name, PixelIterator pi, Environment env) 
  {
    if (filter != null) {
      MandelInfo parent = null;
      if (env != null) {
        Set<MandelHandle> handles = env.getImageDataScanner().getMandelHandles(name.getMandelName().getParentName());
        System.out.println("found " + handles.size() + " parent handles");
        MandelHandle ph = env.getImageDataScanner().getMandelInfo(name.getMandelName().getParentName());
        if (ph != null) {
          try {
            parent = ph.getInfo().getInfo();
          } catch (IOException e) {
          }
        }
      }
      if (!filter.filter(name, pi, parent)) {
        return false;
      }
    }
    return true;
  }
  
  
  static void cleanupInfo(Environment env, AbstractFile f) {
    if (!env.backupInfoFile(f)) {
      if (env.isCleanupInfo() && f.isFile()) {
        System.out.println("deleting " + f);
        try {
          MandelFolder.Util.delete(f.getFile());
        } catch (IOException ex) {
          System.out.println("deletion of " + f + " failed: " + ex);
        }
      }
    }
  }

  static MandelData checkOld(MandelScanner scan,
          QualifiedMandelName name, MandelInfo reqd,
          String msg) {
    MandelData old = null;

    Set<MandelHandle> set = scan.getMandelHandles(name);
    if (!set.isEmpty()) {
      for (MandelHandle h : set) {
        if (h.getHeader().hasRaster()) {
          try {
            MandelData md = h.getData();
            MandelInfo mi = md.getInfo();
            if (reqd.getDX().equals(mi.getDX())
                    && reqd.getDY().equals(mi.getDY())
                    && reqd.getXM().equals(mi.getXM())
                    && reqd.getYM().equals(mi.getYM())
                    && reqd.getRX() == mi.getRX()
                    && reqd.getRY() == mi.getRY()) {
              System.out.println(msg + " " + h.getFile() + ": " + mi.getLimitIt());
              old = md;
              break;
            } else {
              System.out.printf("found: %s\n", h.getFile());
              print(mi);
              System.out.printf("requested:\n");
              print(reqd);
            }
          } catch (IOException io) {
          }
        }
      }
    }
    return old;
  }

  static public File mapFileName(QualifiedMandelName name) {
    return new File(name.getName()+".mm");
  }
  
  static public void print(MandelInfo info) {
    System.out.printf("rx:      %d\n", info.getRX());
    System.out.printf("ry:      %d\n", info.getRY());
    System.out.printf("limit:   %d\n", info.getLimitIt());
    System.out.printf("dx:      %s\n", info.getDX());
    System.out.printf("dy:      %s\n", info.getDY());
    System.out.printf("xm:      %s\n", info.getXM());
    System.out.printf("ym:      %s\n", info.getYM());
  }

  ////////////////////////////////////////////////////////////////////////////
  private static class Service {

    Environment env;
    Set<AbstractFile> ignored;
    MandelScanner imagescan;
    MandelScanner incompletescan;
    MandelScanner infoscan;
    MandelScanner prioscan;
    MandelScanner allscan;

    boolean dflag;
    boolean mflag;
    Filter filter;

    public Service(boolean dflag, boolean mflag, Filter filter) throws IllegalConfigurationException {
      this.dflag = dflag;
      this.mflag = mflag;
      this.filter = filter;
      env = new Environment(null);
      ignored = new HashSet<AbstractFile>();
      imagescan = env.getImageDataScanner();
      incompletescan = env.getIncompleteScanner();
      infoscan = env.getInfoScanner();
      prioscan = env.getPrioInfoScanner();
      allscan = env.getAllScanner();

    }

    public Environment getEnvironment() {
      return env;
    }

    public void service() {
      Iterator<MandelHandle> fallback = infoscan.getMandelHandles().iterator();
      int found = 0;
      while (true) {
        int prio = 0;
        System.out.println("start loop");
        for (MandelHandle h : prioscan.getMandelHandles()) {
          prio += handle(h, false);
        }
        found += prio;
        if (prio == 0) {
          if (!fallback.hasNext()) {
            if (found > 0) {
              System.out.println("" + found + " files processed");
              found = 0;
            }
            System.out.println("rescan standard scanner");
            if (filter.limit > 0) {
              allscan.rescan(true);
            } else {
              infoscan.rescan(true);
            }
            fallback = infoscan.getMandelHandles().iterator();
          }
          while (fallback.hasNext()) {
            int f = handle(fallback.next(), true);
            found += f;
            if (f > 0) {
              break;
            }
          }
        }

        if (found == 0) {
          System.out.println("nothing found");
          try {
            Thread.sleep(1000 * 20);
          } catch (InterruptedException ie) {
            System.exit(1);
          }
        }

        System.out.println("rescan prio scanner");
        prioscan.rescan(false);
      }
    }

    int handle(MandelHandle mh, boolean fallback) {
      int found = 0;
      AbstractFile f = mh.getFile();
      QualifiedMandelName name = mh.getName();
      if (ignored.contains(f)) {
        return found;
      }
      if (name.getLabel() != null) {
        return found;
      }
      //System.out.println("found "+f);
      if (!f.getFile().exists()) {
        System.out.println("already processed or deleted: " + f);
        return found;
      }
      String n = f.getName();

      //System.out.println("checking for image data "+f);
      try {
        MandelData old = null;
        File file = null;
        FolderLock lock = MandelFolder.getMandelFolder(f.getFile().getParentFile());
        if (!lock.lock()) {
          return found;
        }

        try {
          if (!f.tryLock()) {
            return found;
          }
        } finally {
          lock.releaseLock();
        }
        System.out.println("got lock for " + f);
        MandelData req;
        try {
          req = new MandelData(f, false);
        } catch (IOException io) {
          f.releaseLock();
          if (f.getFile().length() == 0) {
            f.getFile().delete();
          }
          return found;
        }
        MandelInfo reqd = req.getInfo();
        reqd.removeProperty(MandelInfo.ATTR_REDONUMBER);
        old = checkOld(imagescan, name, reqd,
                "requested " + reqd.getLimitIt() + " found");
        // TODO: what happens if name is reused for other coordinates???
        if (old != null) {
          if (reqd.hasProperty(ATTR_REFREDO)) {
            System.out.println(f + " redo already existing image");
            file = old.getFile().getFile();
            old = null;
          } else if (old.getInfo().getLimitIt() >= reqd.getLimitIt()) {
            System.out.println(f + " skipped");
            lock.lock();
            try {
              f.releaseLock();
              if (dflag) {
                cleanupInfo(env, f);
              } else {
                ignored.add(f);
              }
            } finally {
              lock.releaseLock();
            }
            return found;
          }
        }

        //////////////
        MandelData incomplete = checkOld(incompletescan, name, reqd, "resuming");
        if (incomplete != null) {
          old = incomplete;
        }
        
        /////////////// 
        Mand m = new Mand(req, old, file, name, env);
        m.setFilter(filter);
        if (this.mflag) {
          m.setMapFile(mapFileName(name));
        }
        if (!m.calculate()) {
          ignored.add(f);
          f.releaseLock();
          System.out.println(f + " skipped for filter mode");
          System.out.println("release lock for " + f);
          return found;
        }
        found++;
        m.write(false);
  
        System.out.println("-------------------");
        lock.lock();
        try {
          f.releaseLock();
          System.out.println("release lock for " + f);
          if (!m.isAborted()) {
            if (!f.getName().equals(m.getFile().getName())) {
              cleanupInfo(env, f);
            }
          } else {
            throw new ShutdownException();
          }
        } finally {
          lock.releaseLock();
        }
      } catch (IOException io) {
        System.err.println("*** " + f + ": " + io);
      }
      return found;
    }
  }

  static private void service(boolean dflag, boolean mflag, Filter filter) {
    try {
      if (filter != null) {
        if (filter.prefix != null) {
          System.out.println("prefix filter is " + filter.prefix);
        }
        if (filter.variants) {
          System.out.println("variants filter is on");
        }
        if (filter.fast) {
          System.out.println("fast filter is on");
        }
        if (filter.limit > 0) {
          System.out.printf("limit filter is %d minutes\n", filter.limit / 60);
        }
      }
      Service srv = new Service(dflag, mflag, filter);
      srv.service();
    } catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: " + ex);
    } catch (ShutdownException sd) {
      Command.Warning("calculation service aborted!");
    }
  }
}
