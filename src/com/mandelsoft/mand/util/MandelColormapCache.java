package com.mandelsoft.mand.util;

import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.cm.Colormap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MandelColormapCache {

  private int max;
  private List<QualifiedMandelName> list;
  private Map<QualifiedMandelName, Colormap> map;
  private Map<QualifiedMandelName, Colormap> locked;

  public MandelColormapCache(int max)
  {
    this.max=max;
    list=new ArrayList<QualifiedMandelName>();
    map=new HashMap<QualifiedMandelName, Colormap>();
    locked=new HashMap<QualifiedMandelName, Colormap>();
  }

  synchronized
  public Colormap get(QualifiedMandelName n)
  {
    Colormap cm=map.get(n);
    if (cm==null) cm=locked.get(n);
    return cm;
  }

  synchronized
  public void remove(QualifiedMandelName n)
  {
    list.remove(n);
    map.remove(n);
    locked.remove(n);
  }

  synchronized
  public void remove(MandelName n)
  {
    List<QualifiedMandelName> del=new ArrayList<QualifiedMandelName>();
    for (QualifiedMandelName q:list) {
      if (q.getMandelName().equals(n)) del.add(q);
    }
    for (QualifiedMandelName q:del) {
      remove(q);
    }
  }

  synchronized
  public void unlock(QualifiedMandelName n)
  {
    if (locked.containsKey(n)) {
      Colormap cm=locked.get(n);
      remove(n);
      add(n,cm);
    }
  }

  synchronized
  public void lock(QualifiedMandelName n, Colormap cm)
  {
    remove(n);
    locked.put(n,cm);
  }

  synchronized
  public void add(QualifiedMandelName n, Colormap cm)
  {
    if (locked.containsKey(n)) {
      locked.put(n, cm);
    }
    else {
      if (!map.containsKey(n)&&list.size()>=max) {
        map.remove(list.get(0));
        list.remove(0);
      }
      list.remove(n);
      list.add(n);
      map.put(n, cm);
    }
  }
}
