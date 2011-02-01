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
    this.name=MandelName.ROOT;
    this.list=new ArrayList<MandelHandle>();
  }

  public MandelName getName()
  {
    return name;
  }

  public void refresh(QualifiedMandelName name)
  { if (name==null) return;
    this.name=name.getMandelName();
    refresh(name.getQualifier());
  }
  
  private int compare(String s1, String s2)
  {
    if (s1==null && s2==null) return 0;
    if (s1==null) return -1;
    if (s2==null) return 1;
    return s1.compareTo(s2);
  }

  protected String getLabel(String q)
  { if (Utils.isEmpty(q)) return "<default>";
    return q;
  }

  public void refresh(String sel)
  { int old=list.size();
    list.clear();
    sel=getLabel(sel);
    Set<MandelHandle> n=scanner.getMandelHandles(name);
    System.out.println("*** update variants: "+n);
    selected=null;
    if (n!=null) {
      for (MandelHandle h:n) {
        String v=h.getQualifier();
        System.out.println("found "+v+": "+h.getFile());
        if (getLabel(v).equals(sel)) selected=sel;
        for (int i=0; h!=null && i<list.size(); i++) {
          if (compare(list.get(i).getQualifier(),v)>=0) {
            list.add(i,h);
            h=null;
          }
        }
        if (h!=null) list.add(h);
      }
    }
    this.fireContentsChanged(this, 0, Math.max(old, list.size())-1);
  }

  public String getName(int index)
  {
    MandelHandle h= list.get(index);
    return h==null?null:h.getName().toString();
  }

  public Object getElementAt(int index)
  {
    MandelHandle h;
    if (index>=list.size()) return "<none>";
    h=list.get(index);
    if (h==null) {
      return "";
    }
    else {
      return getLabel(h.getQualifier());
    }
  }

  public QualifiedMandelName getElement(Object item)
  {
    for (MandelHandle h:list) {
      if (getLabel(h.getQualifier()).equals(item)) {
        return h.getName();
      }
    }
    return null;
  }

  public QualifiedMandelName getVariantName()
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