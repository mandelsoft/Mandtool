
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
package com.mandelsoft.mand.srv;

import java.util.HashSet;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.PixelIterator;
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.ChangeListener;
import com.mandelsoft.util.StateChangeSupport;

/**
 *
 * @author Uwe Krüger
 */

public class AreaHandler implements Request {

  private MandelData data;
  private int sx;
  private int sy;
  private int nx;
  private int ny;

  private boolean full;
  private boolean recalc;
  private Server  server;
  private PixelIterator pi;

  private long mtime;

  private boolean log;


  public AreaHandler(Server server, boolean recalc, boolean full,
                    MandelData data, int sx, int sy, int nx, int ny)
  {
    this.full=full;
    this.recalc=recalc;
    this.server=server;
    this.data=data;
    this.sx=sx;
    this.sy=sy;
    this.nx=nx;
    this.ny=ny;

    this.mtime=0;
  }

  private void log(String m)
  {
    if (log) System.out.println(m);
  }

  public void setPixelIterator(PixelIterator pi)
  {
    this.pi=pi;
  }

  public long getMTime()
  {
    return mtime;
  }

  synchronized
  public void initiate()
  {
    startStateSetup();
    if (!full) {
      startFrameState();
      // frame already processed, just regularily continue
      // with next state
    }
    else if (nx*ny<500) {
      startCalcRequestState();
      calc("full", sx+(full?0:1), sy+(full?0:1),
                       nx-(full?0:2), ny-(full?0:2));
    }
    else {
      startFrameState();
      if (full) {
        //up
        calc("top   ", sx+1,    sy, nx-2, 1);
        //down
        calc("bottom", sx+1,    sy+ny-1, nx-2, 1);
        // left
        calc("left  ", sx,      sy, 1, ny);
        // right
        calc("right ", sx+nx-1, sy, 1, ny);
      }
    }
    full=false;
    finishStateSetup();
  }

  private void calc(String msg, int x0, int y0, int dx, int dy)
  {
    if (!full) {
      // correct area not to contain frame that is already processed
      if (x0==sx) {
        x0++;
        dx--;
      }
      else if (x0+dx==sx+nx) {
        dx--;
      }

      if (y0==sy) {
        y0++;
        dy--;
      }
      else if (y0+dy==sy+ny) {
        dy--;
      }
    }
    if (dx>0 && dy>0) {
      if (dx*dy>500) {
        int d;
        if (dx>dy) {
          d=dx/2;
          calc(msg,x0,y0,d,dy);
          calc(msg,x0+d,y0,dx-d,dy);
        }
        else {
          d=dy/2;
          calc(msg,x0,y0,dx,d);
          calc(msg,x0,y0+d,dx,dy-d);
        }
      }
      else initiate(msg,x0,y0,dx,dy);
    }
  }

  private void initiate(String msg, int x0, int y0, int dx, int dy)
  {
    CalcRequest req=new CalcRequest(data.getInfo(),x0,y0,dx,dy);
    if (recalc && !put(req)) {
      log("skipped calc "+msg+": "+x0+", "+y0+", "+dx+", "+dy);
      return;
    }
    log("calc "+msg+": "+x0+", "+y0+", "+dx+", "+dy);
    addRequest(req);
  }
  
  private void initiateSubArea(int x0, int y0, int dx, int dy)
  {
    if (dx>0 && dy>0) {
      log("sub: "+x0+", "+y0+", "+dx+", "+dy);
      AreaHandler req=new AreaHandler(server, recalc, false, data,
                                      x0, y0, dx, dy);
      addRequest(req);
    }
  }

  private boolean constantFrame()
  { int[][] raster=getRaster();

    int it=raster[sy][sx];
    if (!equals(raster,it,sx+1,   sy,      nx-1,1)) return false;
    if (!equals(raster,it,sx+1,   sy+ny-1, nx-1,1)) return false;
    if (!equals(raster,it,sx,     sy+1,    1,ny-1)) return false;
    if (!equals(raster,it,sx+nx-1,sy+1,    1,ny-1)) return false;
    return true;
  }

  private boolean equals(int[][] raster, int it, int x0, int y0,
                                                 int dx, int dy)
  {
    for (int y=y0; y<y0+dy; y++) {
      for (int x=x0; x<x0+dx; x++) {
        if (raster[y][x]!=it) return false;
      }
    }
    return true;
  }

  private void fillFrame()
  {
    int[][] raster=getRaster();
    fillFrame(raster,raster[sy][sx],sx+1,sy+1,nx-2,ny-2);
  }

  private void fillFrame(int[][] raster, int it, int x0, int y0,
                                                 int dx, int dy)
  { int m=it==0?1:0;
    int cnt=0;
    int mcnt=0;

    log("fill "+it+": "+x0+", "+y0+", "+dx+", "+dy);
    MandelInfo info=data.getInfo();
    for (int y=y0; y<y0+dy; y++) {
      for (int x=x0; x<x0+dx; x++) {
        raster[y][x]=it;
        cnt+=it;
        mcnt+=m;
      }
    }
    info.setMCnt(info.getMCnt()+mcnt);
    info.setNumIt(info.getNumIt()+cnt);
  }

  private int[][] getRaster()
  {
    return data.getRaster().getRaster();
  }

  /////////////////
  // take over of calc request results

  private void transfer(CalcRequest req)
  { int nx=req.getNX();
    int ny=req.getNY();
    int sx=req.getSX();
    int sy=req.getSY();
    int[][] raster=getRaster();
    MandelInfo info=data.getInfo();

    for (int x=0; x<nx; x++) {
      for (int y=0; y<ny; y++) {
        int ax=sx+x;
        int ay=sy+y;
        raster[ay][ax]=req.getDataRel(x, y);
      }
    }

    if (req.getMaxIt()>info.getMaxIt()) info.setMaxIt(req.getMaxIt());
    if (req.getMinIt()<info.getMinIt()) info.setMinIt(req.getMinIt());
    info.setMCnt(info.getMCnt()+req.getMCnt());
    info.setMCCnt(info.getMCCnt()+req.getCCnt());
    info.setNumIt(info.getNumIt()+req.getNumIt());
    mtime+=req.getMTime();
  }

  private boolean put(CalcRequest req)
  { int nx=req.getNX();
    int ny=req.getNY();
    int sx=req.getSX();
    int sy=req.getSY();
    MandelInfo info=data.getInfo();
    int[][] raster=getRaster();
    boolean found=false;
    int minit=info.getMinIt();
    int maxit=info.getMinIt();
    long mcnt=info.getMCnt();
    long ccnt=info.getMCCnt();
    long numit=info.getNumIt();
  //int[] buffer=
    req.createData();
    
    for (int x=0; x<nx; x++) {
      for (int y=0; y<ny; y++) {
        int ax=sx+x;
        int ay=sy+y;
        int it=raster[ay][ax];
        req.setDataRel(x,y,it);
        //buffer[req.getIndexRel(x, y)]=it;
        if (it==0) {
          found=true;
        }
        numit+=it;
        if (it>maxit) maxit=it;
        if (it<minit) minit=it;
      }
    }
    if (!found) {
      info.setMaxIt(maxit);
      info.setMinIt(minit);
      info.setMCnt(mcnt);
      info.setMCCnt(ccnt);
      info.setNumIt(numit);
    }
    return found;
  }

  ///////////////////////////////////////////////////////////////
  // state handling
  ///////////////////////////////////////////////////////////////

  ///////////////////
  // state diagramm
  //           +--------calcrequest-----------+
  //          /        /                       \
  //  initial -> frame -> divide -> subarea -> done
  //

  private boolean instatesetup=false;

  synchronized private void startStateSetup()
  {
    instatesetup=true;
  }

  synchronized private void finishStateSetup()
  {
    instatesetup=false;
    checkFinishState();
  }

  synchronized private void checkFinishState()
  {
    if (!instatesetup && requests.isEmpty() && listener!=null)
      listener.nextState();
  }

  // state change
  private void startFrameState()
  {
    listener=new FrameChangeListener();
  }

  private void startDivideState()
  {
    listener=new DivideChangeListener();
  }

  private void startCalcRequestState()
  {
    listener=new CalcRequestChangeListener();
  }

  private void startSubAreaState()
  {
    listener=new SubAreaChangeListener();
  }

  private void startDoneState()
  {
    listener=null;
    fireChangeEvent();
  }

  ///////////////////////////////////////////////////////////////
  // sub requests
  ///////////////////////////////////////////////////////////////

  private HashSet<Request>   requests=new HashSet<Request>();
  private HandlerChangeListener listener;

  private void addRequest(Request req)
  {
    if (listener==null) throw new IllegalStateException("illegal area state");
    req.addChangeListener(listener);
    req.setPixelIterator(pi);
    requests.add(req);
    req.send(server);
  }

  ///////////////////
  // base
  private abstract class HandlerChangeListener implements ChangeListener {

    final public void stateChanged(ChangeEvent e)
    {
      synchronized (AreaHandler.this) {
        Request req=(Request)e.getSource();
        requests.remove(req);
        requestProcessed(req);
        checkFinishState();
      }
    }
    
    abstract protected void requestProcessed(Request req);

    protected void nextState()
    {
      startDoneState();
    }
  }

  ///////////////////
  // calc request
  private class CalcRequestChangeListener extends HandlerChangeListener {
    protected void requestProcessed(Request req)
    {
      transfer((CalcRequest)req);
    }
  }

  ///////////////////
  // outer frame
  private class FrameChangeListener extends CalcRequestChangeListener {
    @Override
    protected void nextState()
    { int d;

      startStateSetup();
      if (constantFrame()) {
        startDoneState();
        fillFrame();
      }
      else {
        if (nx*ny<500) {
          startCalcRequestState();
          calc("full", sx+1, sy+1, nx-2, ny-2);
        }
        else {
          startDivideState();
          if (nx>ny) { // divide x
            d=nx/2;
            calc("div x", sx+d, sy+1, 1, ny-2);
          }
          else { // divide y
            d=ny/2;
            calc("div y", sx+1, sy+d, nx-2, 1);
          }
        }
      }
      finishStateSetup();
    }
  }

  ///////////////////
  // divide line
  private class DivideChangeListener extends CalcRequestChangeListener {
    @Override
    protected void nextState()
    { int d;

      startStateSetup();
      startSubAreaState();
      if (nx>ny) { // divide x
        d=nx/2;
        initiateSubArea(sx,   sy, d+1,  ny);
        initiateSubArea(sx+d, sy, nx-d, ny);
      }
      else { // divide y
        d=ny/2;
        initiateSubArea(sx, sy,   nx, d+1);
        initiateSubArea(sx, sy+d, nx, ny-d);
      }
      finishStateSetup();
    }
  }

  ///////////////////
  // nested areas
  private class SubAreaChangeListener extends HandlerChangeListener {
    protected void requestProcessed(Request req)
    {
      AreaHandler area=(AreaHandler)req;
      mtime+=area.getMTime();
    }
  }

  ///////////////////////////////////////////////////////////////
  // Request
  ///////////////////////////////////////////////////////////////

  public void send(Server server)
  {
    initiate(); // handled locally
  }

  private StateChangeSupport listeners=new StateChangeSupport();

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  private void fireChangeEvent()
  {
    listeners.fireChangeEvent(this);
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }
}
