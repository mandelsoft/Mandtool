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
  private static boolean debug=true;
  
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
  private boolean updatePending=false;
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

  synchronized
  public void handleUpdate()
  {
    if (updatePending) {
      startUpdate();
      updateData();
      updatePending=false;
      finishUpdate();
    }
    else {
      if (debug) System.out.println("no update pending for image base statistic");
    }
  }

  private void updateListSizes()
  {
    if (debug) System.out.println("  update list statistic");
    unseen=set("unseen",unseen, env.getUnseenRastersModel());
    unseenRefine=set("unseen refinemnts",unseenRefine, env.getUnseenRefinementsModel());
    refineRequests=set("refinements",refineRequests, env.getRefinementRequestsModel());
  }

  private int set(String key, int value, MandelListTableModel list)
  {
    if (list!=null) {
      //list.refresh(true);
      value=set(key, value, list.getRowCount());
    }
    return value;
  }

  private int set(String key, int value, int n)
  {
    String attr="";

    if (n!=value) {
      changed=true;
      attr=" (changed)";
      value=n;
    }
    if (debug) System.out.println("  "+key+"="+value+attr);
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

    if (debug) System.out.println("update statistic");
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

    colormaps=set("colormaps",colormaps, env.getColormapScanner().getColormapNames().size());
    all=set("areas",all, c_all);
    available=set("available",available, c_all-c_req);
    modifiableImages=set("images",modifiableImages, c_mod);
    rasters=set("rasters",rasters, c_ras);
    requests=set("requests",requests, c_req);
    variants=set("variants",variants, c_var);
    others=set("others",others, c_oth);

    updateListSizes();
    if (debug) System.out.println("done");
  }

  private void startUpdate()
  {
    changed=false;
  }

  private void finishUpdate()
  {
    if (changed) {
      if (debug) System.out.println("notify changes statistic");
      this.fireChangeEvent();
    }
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

    private void cacheUpdate(String reason)
    {
      if (debug) System.out.println("statistic update pending ("+reason+")");
      updatePending=true;
    }

    public void scannerChanged(MandelScanner s)
    {
      if (env.isInUpdate()) cacheUpdate("scanner");
      else {
        startUpdate();
        updateData();
        finishUpdate();
      }
    }

    public void listChanged(com.mandelsoft.util.ChangeEvent evt)
    {
      if (env.isInUpdate()) cacheUpdate("list "+evt.getSource());
      else {
        startUpdate();
        updateListSizes();
        finishUpdate();
      }
    }
  }
}
