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
package com.mandelsoft.mand.cm;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.mapping.Mapping;
import com.mandelsoft.swing.ChangeListenerSupport;

/**
 *
 * @author Uwe Krueger
 */
public class ColormapModel extends EventSourceSupport
                           implements ColormapSource {
  public static boolean debug=false;

  public static final int RESIZE_PROPORTIONAL=0;
  public static final int RESIZE_LOCK_IPS=1;
  public static final int RESIZE_LOCK_COLORS=2;


   public enum ResizeMode {
     RESIZE_PROPORTIONAL("proportional"),
     RESIZE_LOCK_IPS("lock interpolation points"),
     RESIZE_LOCK_COLORS("lock iteration colors");

    private String name;

    ResizeMode(String name)
    {
      this.name=name;
    }

    public String getName()
    {
      return name;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private boolean modifiable;
  private ResizeMode resizemode=ResizeMode.RESIZE_LOCK_IPS;
  private Colormap colormap;
  private InterpolationPoint ips;
  private boolean adjusting;

  public ColormapModel()
  {
  }

  public ColormapModel(Colormap cm)
  { if (cm!=null) setColormap(cm);
  }

  public Colormap getColormap()
  { return colormap;
  }

  public int getSize()
  { return colormap==null?0:colormap.getSize();
  }
  
  public void setModifiable(boolean b)
  { boolean old=modifiable;
    modifiable=b;
    if (old!=b) change.fireChangeEvent(this);
  }

  public void setResizeMode(ResizeMode resizemode)
  {
    if (debug) System.out.println("setting resize mode "+resizemode);
    this.resizemode=resizemode;
  }

  public void setAdjusting(boolean adjusting)
  {
    this.adjusting=adjusting;
  }

  public boolean isAdjusting()
  {
    return adjusting;
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public ResizeMode getResizeMode()
  {
    return resizemode;
  }

  public Set<InterpolationPoint> getInterpolationPoints()
  {
    Set<InterpolationPoint> set=new HashSet<InterpolationPoint>();
    InterpolationPoint ip=ips;
    while (ip!=null) {
      set.add(ip);
      ip=ip.getNext();
    }
    return set;
  }

  public Iterator<InterpolationPoint> interpolationPoints()
  {
     return new IPIterator();
  }

  private class IPIterator implements Iterator<InterpolationPoint> {
    private InterpolationPoint ip=ips;

    public boolean hasNext()
    {
      return ip!=null;
    }

    public InterpolationPoint next()
    {
      try {
        return ip;
      }
      finally {
        ip=ip.getNext();
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  public void setColormap(Colormap map)
  {
    if (_setColormap(map)) {
      if (map!=null) {
        setupInterpolationPoints();
      }
      change.fireChangeEvent(this);
    }
  }

  private boolean _setColormap(Colormap map)
  {
    if (this.colormap==map) {
      if (debug) System.out.println("colormap in colormap model unchanged");
      return false;
    }
    if (map==null) {
      if (debug) System.out.println("clear colormap in colormap model");
    }
    else {
      if (debug) System.out.println("setting colormap in colormap model "+map.getSize());
    }
    if (this.colormap!=null) this.colormap.removeChangeListener(listener);
    this.colormap=map;
    
    if (map!=null) {
      this.colormap.addChangeListener(listener);
    }
    return true;
  }
  
  private void cleanupInterpolationPoints()
  {
    InterpolationPoint ip=ips;
    while (ip!=null) {
      ip.cleanup();
      ip=ip.getNext();
    }
  }
  
  private void setupInterpolationPoints()
  { InterpolationPoint ip;
    cleanupInterpolationPoints();
    //System.out.println("setup new ips "+colormap.getSize());
    ips=new InterpolationPoint(1, this);
    ips.fireInterpolationPointEvent(InterpolationPointEvent.IPE_ADDED);
    ips.setFixed(true);

    ip=new InterpolationPoint(colormap.getSize()-1, this);
    ip.setFixed(true);
    ips.add(ip);

    determineInterpolationPoints();
  }

  protected void determineInterpolationPoints()
  {
//    handleComponent(ColorComponentAccess.red);
//    handleComponent(ColorComponentAccess.green);
//    handleComponent(ColorComponentAccess.blue);
    handleComponents();
  }

  protected int dir(int diff, int dir)
  { if (diff==0) return dir;
    return Integer.signum(diff);
  }

  protected boolean sameDir(int diff, int dir)
  {
    return diff==0||dir==0||Integer.signum(diff)==dir;
  }

  protected void handleComponent(ColorComponentAccess comp)
  {
    int index=1;
    while (index<colormap.getSize()-1) {
      int diff=comp.getComponentValue(colormap.getColor(index+1))-
              comp.getComponentValue(colormap.getColor(index));
      int cur=diff;
      int dir=dir(diff,0);
      while (diff-1<=cur&&cur<=diff+1&&sameDir(cur,dir)
             &&++index<colormap.getSize()-1) {
        dir=dir(cur,dir);
        if (debug) System.out.println("dir="+dir+" diff="+diff);
        cur=comp.getComponentValue(colormap.getColor(index+1))-
                comp.getComponentValue(colormap.getColor(index));
      }
      createInterpolationPoint(index);
    }
  }

  private class State {
    ColorComponentAccess comp;

    State(ColorComponentAccess comp)
    { this.comp=comp;
    }

    int rx;
    double start;
    double end;
    double dc;

    void setup(int idx1, int idx2)
    {
      rx = idx2 - idx1;
      start = comp.getComponentValue(getColor(idx1));
      end = comp.getComponentValue(getColor(idx2));
      dc=end-start;
    }

    boolean check(int cur, int i)
    { int c = (int) (start + i * dc / rx);
      int f=comp.getComponentValue(getColor(cur));
      if (c-1>f || f>c+1) {
        return true;
      }
      return false;
    }
  }

  private class CheckState {
    State red=new State(ColorComponentAccess.red);
    State green=new State(ColorComponentAccess.green);
    State blue=new State(ColorComponentAccess.blue);

    void setup(int idx1, int idx2)
    {
      red.setup(idx1, idx2);
      green.setup(idx1, idx2);
      blue.setup(idx1, idx2);
    }

    boolean check(int cur, int i)
    {
      return red.check(cur, i) || green.check(cur, i) || blue.check(cur, i);
    }
  }

  protected void handleComponents()
  { CheckState state=new CheckState();
    int index=1;
    while (index<colormap.getSize()) {
      int found=index;
      for (int i=index; i<colormap.getSize(); i++) {
        if (checkInterpolation(index, i, state)) {
          found=i;
        }
      }
      createInterpolationPoint(found);
      if (found==index) index=found+1;
      else index=found;
    }
  }

  private boolean checkInterpolation(int idx1, int idx2,
                                     CheckState state)
  {
    return  _checkInterpolation(idx1,idx2,state);
            //||_checkInterpolation(idx2,idx1,comp);
  }


  private boolean _checkInterpolation(int idx1, int idx2,
                                      CheckState state)
  {
    int rx = idx2 - idx1;
    int dx = Integer.signum(rx);
    state.setup(idx1,idx2);

    //System.out.println("check "+idx1+"-"+idx2);
    for (int i = 0; i != rx; i += dx) {
        int cur = i + idx1;
        if (state.check(cur, i)) {
            //System.out.println("break "+cur);
            return false;
        }
    }
    return true;
  }

  public Color getColor(int i)
  {
    return colormap.getColor(i);
  }

  public InterpolationPoint createInterpolationPoint(int index)
  {
    //System.out.println("  create ip at "+index+": "+getColor(index));
    InterpolationPoint ip=ips;
    InterpolationPoint n;
    while (ip.getIndex()<index) {
      ip=ip.getNext();
    }
    if (ip==null)
      throw new IllegalArgumentException("index beyond colormap");

    if (ip.getIndex()==index) return ip;
    n=new InterpolationPoint(index,this);
    ip.getPrev().add(n);
    return ip;
  }

  public InterpolationPoint getInterpolationPoint(int index)
  {
    InterpolationPoint ip=ips;
    while (ip!=null&&ip.getIndex()<index) {
      ip=ip.getNext();
    }
    if (ip==null||ip.getIndex()!=index) return null;
    return ip;
  }

  ////////////////////////////////////////////////////////////////////////////
  // resizing
  ////////////////////////////////////////////////////////////////////////////
  
  public void resize(int size)
  {
    resizeI(size,new FactorResize(colormap.getSize(),size));
  }

  public void resize(int size, Mapping src, Mapping dst)
  {
    resize(resizemode, size, src, dst);
  }

  public void resize(ResizeMode mode, int size, Mapping src, Mapping dst)
  {
    if (debug) System.out.println("resizemode is "+mode);
    switch (mode) {
      case RESIZE_PROPORTIONAL:
        resizeI(size);
        break;
      case RESIZE_LOCK_IPS:
        resizeI(size,src,dst);
        break;
      case RESIZE_LOCK_COLORS:
        resizeC(size,src,dst);
        break;
    }
  }

  public void resizeI(int size)
  {
    resizeI(size,new FactorResize(colormap.getSize(),size));
  }

  public void resizeI(int size, Mapping src, Mapping dst)
  {
    resizeI(size,new MappingBasedResize(src,dst));
  }

  private void resizeI(int size, ResizeHandler h)
  {
    if (colormap==null || size==getSize()) return;
    if (debug) System.out.println("resize from "+getSize()+" to "+size);
    Colormap old=colormap;
    double factor=((double)size-2)/(old.getSize()-2);
    if (debug) System.out.println("resize factor "+factor);

    Colormap cm=new Colormap(size);
    cm.startModification();
    cm.setColor(0, old.getColor(0));

    setAdjusting(true);
    _setColormap(cm);

    InterpolationPoint ip=ips;
    int last=0;
    while (ip!=null) {
      int n;

      if (ip.getPrev()==null) n=1;
      else n=h.getRelocatedIndex(ip.getIndex());

      if (ip.getNext()==null) {
        if (n!=size-1) {
          if (debug) System.out.println("^correcting last index "+n+" to "+(size-1));
          n=size-1;
        }
      }
      if (n==0) {
        n=(int)Math.round((ip.getIndex()-1)*factor+1);
        if (n<=last) n=last+1;
        if (debug) System.out.println("  no mapping found for "+ip.getIndex());
      }
      if (debug) System.out.println("  moving "+ip.getIndex()+" to "+n);
      ip._prepareSetIndex(last=n);
      ip=ip.getNext();
    }
    ip=ips;
    while (ip!=null) {
      ip._finishSetIndex(true);
      ip=ip.getNext();
    }
    colormap.endModification();
    change.fireChangeEvent(this);
    setAdjusting(false);
  }

  public void resizeC(int size, Mapping src, Mapping dst)
  {
    resizeC(size,new MappingBasedResize(src,dst));
  }

  private void resizeC(int size, ResizeHandler h)
  {
    if (colormap==null || size==getSize()) return;
    if (debug) System.out.println("resize new from "+getSize()+" to "+size);
    Colormap old=colormap;
    double factor=((double)size-2)/(old.getSize()-2);
    if (debug) System.out.println("resize factor "+factor);

    cleanupInterpolationPoints();
    Colormap cm=new Colormap(size);
    cm.startModification();
    cm.setColor(0, old.getColor(0));
    setAdjusting(true);
    _setColormap(cm);

    // first create temp ips to setup colormap
    cm.setColor(1, old.getColor(1));
    ips=new InterpolationPoint(1, this);
    ips.setFixed(true);

    cm.setColor(cm.getSize()-1, old.getColor(old.getSize()-1));
    InterpolationPoint ip=new InterpolationPoint(cm.getSize()-1, this);
    ip.setFixed(true);
    ips.add(ip);

    int n;
    int last=1;
    for (int c=2; c<old.getSize()-1; c++) {
      n=h.getRelocatedIndex(c);
      if (n==0) {
        n=(int)Math.round((c-1)*factor+1);
        if (n<=last) n=last+1;
        if (debug) System.out.println("  no mapping found for "+c);
      }
      last=n;
      if (debug) System.out.println("  moving index "+c+" to "+n);
      cm.setColor(n, old.getColor(c));
      createInterpolationPoint(n);
    }

    // second fill colormap
    ip=ips;
    while (ip!=null) {
      ip._finishSetIndex(true);
      ip=ip.getNext();
    }

    // third officially setup ips according new colormap
    setupInterpolationPoints();
    cm.endModification();
  }

  ///////////////////////////////////////////////////////////////////////////

  public interface ResizeHandler {
    int getRelocatedIndex(int index);
  }

  public static class FactorResize implements ResizeHandler {
    private double factor;

    public FactorResize(int src, int dst)
    {
      factor=((double)dst-2)/(src-2);
    }

    public int getRelocatedIndex(int index)
    {
      return (int)Math.round((index-1)*factor+1);
    }
  }

  

  public static class MappingBasedResize implements ResizeHandler {
    private Mapping dst;
    private Mapping src;

    public MappingBasedResize(Mapping src, Mapping dst)
    {
      this.dst=dst;
      this.src=src;
    }
  
    public int getRelocatedIndex(int index)
    {
      return dst.getColormapIndex(src.getInteration(index, 0, false));
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  // colormap changes
  ////////////////////////////////////////////////////////////////////////////
  
  private ChangeListenerSupport change=new ChangeListenerSupport();
  private com.mandelsoft.util.ChangeListener listener=new com.mandelsoft.util.ChangeListener() {
    public void stateChanged(com.mandelsoft.util.ChangeEvent e)
    { if (!isAdjusting()) {
        setupInterpolationPoints();
      }
      change.fireChangeEvent(ColormapModel.this);
    }
  };

  public void removeChangeListener(ChangeListener h)
  {
    change.removeChangeListener(h);
  }

  public void addChangeListener(ChangeListener h)
  {
    change.addChangeListener(h);
  }
}
