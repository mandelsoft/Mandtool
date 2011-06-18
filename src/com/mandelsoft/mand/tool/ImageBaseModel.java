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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerListener;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.util.StateChangeSupport;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class ImageBaseModel extends StateChangeSupport {

  private ToolEnvironment env;
  private int all;
  private int available;
  private int rasters;
  private int modifiableImages;
  private int requests;
  private int variants;
  private int others;
  private int colormaps;
  private int unseen;
  private int unseenRefine;
  private int refineRequests;
  private boolean changed=false;
  private EnvUpdateHandler listener;

  public ImageBaseModel(ToolEnvironment env)
  {
    this.env=env;
    listener=new EnvUpdateHandler();

    env.getAllScanner().addMandelScannerListener(listener);
    add(env.getUnseenRefinementsModel());
    add(env.getRefinementRequestsModel());
    add(env.getUnseenRastersModel());
    updateData();
  }

  public ToolEnvironment getEnvironment()
  {
    return env;
  }

  public int getAllAreas()
  {
    return all;
  }

  public int getAvailableImages()
  {
    return available;
  }

  public int getColormaps()
  {
    return colormaps;
  }

  public int getModifiableImages()
  {
    return modifiableImages;
  }

  public int getOthers()
  {
    return others;
  }

  public int getRasters()
  {
    return rasters;
  }

  public int getRefinementRequests()
  {
    return refineRequests;
  }

  public int getRequests()
  {
    return requests;
  }

  public int getUnseenAreas()
  {
    return unseen;
  }

  public int getUnseenRefinements()
  {
    return unseenRefine;
  }

  public int getVariants()
  {
    return variants;
  }



  //////////////////////////////////////////////////////////////////////////
  // implementation

  private void add(MandelListModel m)
  {
    if (m!=null) m.addMandelListListener(listener);
  }

  private void remove(MandelListModel m)
  {
    if (m!=null) m.removeMandelListListener(listener);
  }

  private void updateListSizes()
  {
    unseen=set(unseen, env.getUnseenRasters());
    unseenRefine=set(unseenRefine, env.getUnseenRefinements());
    refineRequests=set(refineRequests, env.getRefinementRequests());
  }

  private int set(int value, MandelList list)
  {
    if (list!=null) {
      value=set(value, list.size());
    }
    return value;
  }

  private int set(int value, int n)
  {
    if (n!=value) {
      changed=true;
      value=n;
    }
    return value;
  }

  private void updateData()
  {
    int c_all=0;
    int c_mod=0;
    int c_req=0;
    int c_ras=0;
    int c_var=0;
    int c_oth=0;

    System.out.println("update statistic");
    MandelScanner scan=env.getAllScanner();
    Set<MandelName> names=scan.getMandelNames();
    c_all=names.size();
    for (MandelName n:names) {
      boolean b_mod=false;
      boolean b_ras=false;
      boolean b_req=false;
      boolean b_oth=false;
      Set<MandelHandle> handles=scan.getMandelHandles(n);
      for (MandelHandle h:handles) {
        MandelHeader header=h.getHeader();
        if (h.getQualifier()!=null) c_var++;
        if (header.isModifiableImage()) b_mod=true;
        else {
          if (header.isRaster()) b_ras=true;
          else {
            if (header.isInfo()) b_req=true;
            else {
              b_oth=true;
              System.out.println(""+h.getName()+": "+header.getType());
            }
          }
        }
      }
      if (b_mod) c_mod++;
      else if (b_ras) c_ras++;
      else if (b_req) c_req++;
      else if (b_oth) c_oth++;
    }

    colormaps=set(colormaps, env.getColormapScanner().getColormapNames().size());
    all=set(all, c_all);
    available=set(available, c_all-c_req);
    modifiableImages=set(modifiableImages, c_mod);
    rasters=set(rasters, c_ras);
    requests=set(requests, c_req);
    variants=set(variants, c_var);
    others=set(others, c_oth);

    updateListSizes();
    System.out.println("done");
  }

  private void startUpdate()
  {
    changed=false;
  }

  private void finishUpdate()
  {
    if (changed) this.fireChangeEvent();
    changed=false;
  }

  private class EnvUpdateHandler implements MandelScannerListener,
                                            MandelListListener {

    public void addMandelFile(MandelScanner s, MandelHandle h)
    {
      scannerChanged(s);
    }

    public void removeMandelFile(MandelScanner s, MandelHandle h)
    {
      scannerChanged(s);
    }

    public void addColormap(MandelScanner s, ColormapHandle h)
    {
    }

    public void removeColormap(MandelScanner s, ColormapHandle h)
    {
    }

    public void scannerChanged(MandelScanner s)
    {
      startUpdate();
      updateData();
      finishUpdate();
    }

    public void listChanged(com.mandelsoft.util.ChangeEvent evt)
    {
      startUpdate();
      updateListSizes();
      finishUpdate();
    }
  }
}
