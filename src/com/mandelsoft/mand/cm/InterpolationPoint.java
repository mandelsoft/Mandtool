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

/**
 *
 * @author Uwe Krüger
 */
public final class InterpolationPoint extends EventSourceSupport {
  public static boolean debug=false;

  private int index;
  private InterpolationPoint next;
  private InterpolationPoint prev;
  private boolean fixed;
  private ColormapModel model;
  private Color color;

  protected InterpolationPoint(int index, ColormapModel colormap)
  {
    this.model=colormap;
    this.index=index;
    this.color=model.getColor(index);
  }

  public ColormapModel getModel()
  { return model;
  }

  public Color getColor()
  {
    return color;
  }

  public boolean isFixed()
  {
    return fixed;
  }

  public int getIndex()
  {
    return index;
  }

  public double getRelativePosition()
  {
    if (getPrev()==null || getNext()==null) return -1;
    int p=getPrev().getIndex();
    int n=getNext().getIndex();
    double position=(getIndex()-p-1)/(double)(n-p-2);
    return position;
  }

  public void setRelativePosition(double position)
  {
    int p=getPrev().getIndex();
    int n=getNext().getIndex();
    int i=(int)(position*(double)(n-p-2)+p+1);
    setIndex(i, false);
  }

  public InterpolationPoint getPrev()
  {
    return prev;
  }

  public InterpolationPoint getNext()
  {
    return next;
  }


  public void setFixed(boolean fixed)
  {
    if (fixed!=this.fixed) {
      this.fixed=fixed;
      fireInterpolationPointEvent(InterpolationPointEvent.IPE_CHANGED);
    }
  }

  public void setColor(Color c)
  {
    if (c!=getColor()) setColor(c,true);
  }

  public void delete()
  {
    if (next==null||prev==null) return;
    if (!isFixed()) {
      model.setAdjusting(true);
      try {
        model.getColormap().startModification();
        if (debug) System.out.println("remove "+index+" prev="+prev.getIndex()+" next="+next.
                                       getIndex());
        next.setPrev(prev);
        prev.setNext(next);
        next.interpolateL(null);
        if (getPrev()!=null) getPrev().setPosition();
        if (getNext()!=null) getNext().setPosition();
        next=prev=null;
        model.getColormap().endModification();
      }
      finally {
        model.setAdjusting(false);
      }
      cleanup();
    }
  }


  ////////////////////////////////////////////////////////////////////////////
  // interaction with colormap model
  ////////////////////////////////////////////////////////////////////////////
  
  protected void cleanup()
  {
    fireInterpolationPointEvent(InterpolationPointEvent.IPE_DELETED);
  }

  protected void setPosition()
  {
    fireInterpolationPointEvent(InterpolationPointEvent.IPE_NEIGHBOR_CHANGED);
  }

  public void setIndex(int index, boolean adjust)
  {
    if (index!=this.index) {
      _prepareSetIndex(index);
      _finishSetIndex(adjust);
    }
  }

  protected void _prepareSetIndex(int index)
  {
    this.index=index;
  }

  protected void _finishSetIndex(boolean adjust)
  {
    if (getPrev()!=null) getPrev().setPosition();
    if (adjust) setPosition();
    if (getNext()!=null) getNext().setPosition();
    setColor(null,false);
    fireInterpolationPointEvent(InterpolationPointEvent.IPE_MOVED);
  }

  protected void setColor(Color c, boolean adjusted)
  { Color old=getColor();
    boolean locked=model.isAdjusting();
    model.setAdjusting(true);
    try {
      if (!locked) model.getColormap().startModification();
      interpolateR(c);
      interpolateL(c);
      if (!locked) model.getColormap().endModification();
    }
    finally {
      model.setAdjusting(locked);
    }
    if (adjusted && getColor()!=old) {
      fireInterpolationPointEvent(InterpolationPointEvent.IPE_COLOR_CHANGED);
    }
  }
  
  private void setNext(InterpolationPoint next)
  {
    this.next=next;
  }

  private void setPrev(InterpolationPoint prev)
  {
    this.prev=prev;
  }

  protected void add(InterpolationPoint ip)
  {
    if (ip.getIndex()<=index) throw new IllegalArgumentException("ip is lower");
    if (next!=null&&ip.getIndex()>=next.getIndex())
      throw new IllegalArgumentException("ip is too high");
    ip.setNext(next);
    ip.setPrev(this);
    if (next!=null) ip.next.setPrev(ip);
    next=ip;
    ip.fireInterpolationPointEvent(InterpolationPointEvent.IPE_ADDED);
    if (ip.getPrev()!=null) ip.getPrev().setPosition();
    if (ip.getNext()!=null) ip.getNext().setPosition();
  }

 
  protected void fireInterpolationPointEvent(int id)
  {
    fireInterpolationPointEvent(this, id);
    getModel().fireInterpolationPointEvent(this,id);
  }

  
  ////////////////////////////////////////////////////////////////////////////
  // interpolation
  ////////////////////////////////////////////////////////////////////////////
  
  private void interpolateL(Color n)
  {
    interpolate(n, prev);
  }

  private void interpolateR(Color n)
  {
    interpolate(n, next);
  }

  private void interpolate(Color n, InterpolationPoint ip)
  {
    if (ip==null) {
      if (n!=null) {
        model.getColormap().setColor(index, n);
        color=n;
      }
    }
    else {
      if (n==null) n=getColor();
      color=n;
      if (debug) System.out.println("interpolate "+index+" to "+ip.getIndex()+": "+
                                    color+"-"+ip.getColor());
      int rx=ip.getIndex()-index;
      int dx=Integer.signum(rx);
      Color start=n;
      Color end=ip.getColor();
      //System.out.println("rx="+rx+", dx="+dx);
      double sred=start.getRed();
      double sgreen=start.getGreen();
      double sblue=start.getBlue();
      double dred=end.getRed()-sred;
      double dgreen=end.getGreen()-sgreen;
      double dblue=end.getBlue()-sblue;
      //System.out.println("sred="+sred+", "+sgreen+", "+sblue);
      //System.out.println("dred="+dred+", "+dgreen+", "+dblue);
      /*
       */
      for (int i=0; i!=rx;
              i+=dx) {
        int cur=i+getIndex();
        /*
        System.out.println("  set "+cur+" ("+(int)(sred+i*dred/rx)+","+
        (int)(sgreen+i*dgreen/rx)+","+
        (int)(sblue+i*dblue/rx)+")");
         */
        model.getColormap().setColor(cur,
                new Color((int)(sred+i*dred/rx),
                (int)(sgreen+i*dgreen/rx),
                (int)(sblue+i*dblue/rx)));
      }
    }
  }
}
