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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeListener;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.swing.ChangeListenerSupport;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krüger
 */
public class MandelVariantModel extends AbstractListModel
                                implements ComboBoxModel {

  private ChangeListenerSupport listeners=new ChangeListenerSupport();

  protected MandelName         name;
  protected MandelScanner      scanner;
  protected List<MandelHandle> list;
  protected String             selected;

  public MandelVariantModel(MandelScanner scanner)
  {
    this.scanner=scanner;
    this.list=new ArrayList<MandelHandle>();
  }

  public MandelName getName()
  {
    return name;
  }

  public void refresh(MandelHandle h)
  { if (h==null) return;
    this.name=h.getName().getMandelName();
    refresh(getLabel(h));
  }

  public void refresh(QualifiedMandelName n, MandelHeader h)
  {
    System.out.println("refresh variants for "+n+"/"+h);
    if (h==null || n==null) return;
    this.name=n.getMandelName();
    refresh(getLabel(n,h));
  }

  private int compare(String s1, String s2)
  {
    if (s1==null && s2==null) return 0;
    if (s1==null) return -1;
    if (s2==null) return 1;
    return s1.compareTo(s2);
  }

//  protected String getLabel(String q)
//  { if (Utils.isEmpty(q)) return "<default>";
//    return q;
//  }

  protected String getLabel(MandelHandle h)
  {
    return getLabel(h.getName(),h.getHeader());
  }

  protected String getLabel(QualifiedMandelName n, MandelHeader h)
  { String q=n.getQualifier();
    if (Utils.isEmpty(q)) q="<default>";
    return q+" ("+h.getTypeDesc()+")";
  }

  private void refresh(String sel)
  { int old=list.size();
    list.clear();
    Set<MandelHandle> n=scanner.getMandelHandles(name);
    System.out.println("*** update variants: "+n+" ("+sel+")");
    selected=null;
    if (n!=null) {
      for (MandelHandle h:n) {
        String v=getLabel(h);
        System.out.println("found "+v+": "+h.getFile());
        if (v.equals(sel)) selected=sel;
        for (int i=0; h!=null && i<list.size(); i++) {
          if (compare(list.get(i).getQualifier(),h.getQualifier())>=0) {
            list.add(i,h);
            h=null;
          }
        }
        if (h!=null) list.add(h);
      }
    }
    System.out.println("Selected: "+selected);
    this.fireContentsChanged(this, 0, Math.max(old, list.size())-1);
  }

//  public String getName(int index)
//  {
//    MandelHandle h= list.get(index);
//    return h==null?null:h.getName().toString();
//  }

  public Object getElementAt(int index)
  {
    MandelHandle h;
    if (index>=list.size()) return "<none>";
    h=list.get(index);
    if (h==null) {
      return "<unknown>";
    }
    else {
      return getLabel(h);
    }
  }

  public MandelHandle getElement(Object item)
  {
    for (MandelHandle h:list) {
      if (getLabel(h).equals(item)) {
        return h;
      }
    }
    return null;
  }

  public QualifiedMandelName getVariantName()
  {
    if (selected==null) return null;
    MandelHandle h=getElement(selected);
    if (h==null) return null;
    return h.getName();
  }

  public MandelHandle getVariantHandle()
  {
    if (selected==null) return null;
    return getElement(selected);
  }

  public int getSize()
  {
    return list.size();
  }

  public Object getSelectedItem()
  {
    return selected;
  }

  public void setSelectedItem(Object anItem)
  {
    this.selected=(String)anItem;
    this.fireContentsChanged(this, 0, list.size()-1);
    fireChangeEvent();
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }

  private void fireChangeEvent()
  {
    listeners.fireChangeEvent();
  }
}