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
package com.mandelsoft.mand.scan;

import java.util.ArrayList;
import java.util.List;
import com.mandelsoft.mand.MandelHeader;

/**
 *
 * @author Uwe Krüger
 */
public abstract class MandelScannerListenerSupport implements MandelScanner, MandelScannerTree {

  static public boolean debug=false;

  protected boolean filter(MandelHeader h)
  {
    return true;
  }
  
  //////////////////////////////////////////////////////////////////////////
  // event generation
  //////////////////////////////////////////////////////////////////////////

  private List<MandelScannerListener> listenerList=new ArrayList<MandelScannerListener>();

  synchronized
  public void addMandelScannerListener(MandelScannerListener l)
  {
    listenerList.add(l);
  }

  synchronized
  public void removeMandelScannerListener(MandelScannerListener l)
  {
    listenerList.remove(l);
  }

  synchronized
  public MandelScannerListener[] getMandelScannerListeners()
  {
    return listenerList.toArray(new MandelScannerListener[listenerList.size()]);
  }

  protected void notifyAddMandelFile(MandelHandle h)
  {
    if (insetup>0) return;
    if (inupdate!=0) {
      updatePending=true;
    }
    else {
      for (MandelScannerListener l :listenerList) {
        l.addMandelFile(this, h);
      }
    }
  }

  protected void notifyRemoveMandelFile(MandelHandle h)
  {
    if (insetup>0) return;
    if (inupdate!=0) {
      updatePending=true;
    }
    else {
      for (MandelScannerListener l :listenerList) {
        l.removeMandelFile(this, h);
      }
    }
  }

  protected void notifyAddColormap(ColormapHandle h)
  {
    if (insetup>0) return;
    if (inupdate!=0) {
      updatePending=true;
    }
    else {
      for (MandelScannerListener l :listenerList) {
        l.addColormap(this, h);
      }
    }
  }

  protected void notifyRemoveColormap(ColormapHandle h)
  {
    if (insetup>0) return;
    if (inupdate!=0) {
      updatePending=true;
    }
    else {
      for (MandelScannerListener l :listenerList) {
        l.removeColormap(this, h);
      }
    }
  }

  protected void notifyScannerChanged()
  {
    if (insetup>0) return;
    if (inupdate!=0) {
      updatePending=true;
    }
    else {
      int cnt=0;
      for (MandelScannerListener l :listenerList) {
        if (debug) System.out.println("- fire scanner changed "
                  +MandelScannerListenerSupport.this+" for "+(++cnt)+" "+l);
        l.scannerChanged(this);
      }
    }
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // update accumulation support
  ///////////////////////////////////////////////////////////////////////////

  private int insetup=0;
  private int inupdate=0;
  private boolean updatePending;

  public void startUpdate()
  {
    startUpdate(false);
  }

  protected void startSetup()
  {
    insetup++;
  }

  protected void finishSetup()
  {
    if (insetup>0)
      insetup--;
  }

  protected void startUpdate(boolean force)
  {
    inupdate++;
    if (force && !updatePending) updatePending=force;
  }

  public boolean isInUpdate()
  {
    return inupdate!=0;
  }

  public void finishUpdate()
  {
    if (inupdate==0) return;
    if (--inupdate==0) {
      if (debug) System.out.println("update finished ("+updatePending+")"+this);
      if (updatePending) {
        notifyScannerChanged();
      }
      updatePending=false;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // event forwarding support
  ///////////////////////////////////////////////////////////////////////////

  protected MandelHandle mapOut(MandelHandle h)
  {
    return h;
  }

  protected ColormapHandle mapOut(ColormapHandle h)
  {
    return h;
  }

  protected class Listener implements MandelScannerListener {

    public void addMandelFile(MandelScanner s, MandelHandle h)
    {
      synchronized (MandelScannerListenerSupport.this) {
        if (!updatePending&&filter(h.getHeader())) {
          if (debug) System.out.println(
                    "forward add to "+MandelScannerListenerSupport.this);
          notifyAddMandelFile(mapOut(h));
        }
      }
    }

    public void removeMandelFile(MandelScanner s, MandelHandle h)
    {
      synchronized (MandelScannerListenerSupport.this) {
        if (!updatePending&&filter(h.getHeader())) {
          if (debug) System.out.println(
                    "forward rem to "+MandelScannerListenerSupport.this);
          notifyRemoveMandelFile(mapOut(h));
        }
      }
    }

    public void addColormap(MandelScanner s, ColormapHandle h)
    {
      synchronized (MandelScannerListenerSupport.this) {
        if (!updatePending&&filter(h.getHeader())) {
          notifyAddColormap(mapOut(h));
        }
      }
    }

    public void removeColormap(MandelScanner s, ColormapHandle h)
    {
      synchronized (MandelScannerListenerSupport.this) {
        if (!updatePending&&filter(h.getHeader())) {
          if (!hasColormap(h.getName())) {
            notifyRemoveColormap(mapOut(h));
          }
        }
      }
    }

    public void scannerChanged(MandelScanner s)
    {
      synchronized (MandelScannerListenerSupport.this) {
        if (debug) System.out.println(
                  "forward all to "+MandelScannerListenerSupport.this);
        notifyScannerChanged();
      }
    }
  }

}
