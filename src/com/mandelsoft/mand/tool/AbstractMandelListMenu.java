
/*
 *  Copyright 2013 Uwe Krueger.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import com.mandelsoft.mand.QualifiedMandelName;
import java.util.Collection;

/**
 *
 * @author Uwe Krüger
 */

public abstract class AbstractMandelListMenu extends JMenu {

  public interface SelectAction {
    public void selectArea(MandelWindowAccess access, QualifiedMandelName name);
  }

  ////////////////////////////////////////////////////////////
  protected MandelWindowAccess access;
  
  private List<Entry> list;
  private boolean shortnames;
  private boolean sorted;
  private List<SelectAction> actions;
  
  public AbstractMandelListMenu(String name, MandelWindowAccess access)
  {
    super(name);
    this.access=access;
    list=new ArrayList<Entry>();
    actions=new ArrayList<SelectAction>();
    setEnabled(false);
  }

  public void addSelectAction(SelectAction a)
  {
    if (!actions.contains(a)) actions.add(a);
  }

  public boolean removeSelectAction(SelectAction a)
  {
    return actions.remove(a);
  }
  
  public void setUseShortnames(boolean shortnames)
  {
    this.shortnames=shortnames;
  }

  public void setSorted(boolean sorted)
  {
    this.sorted=sorted;
  }

  protected Entry lookup(QualifiedMandelName n)
  {
    for (Entry e:list) {
      if (e.name.equals(n)) return e;
    }
    return null;
  }

  protected Entry lookup(QualifiedMandelName n, Collection<Entry> skip)
  {
    for (Entry e:list) {
      if (e.name.equals(n) && (skip==null || !skip.contains(e))) return e;
    }
    return null;
  }

  protected Entry createEntry(QualifiedMandelName n)
  {
    return new Entry(n);
  }

  public void clear()
  {
    ListIterator<Entry> i=entries();
    while (i.hasNext()) {
      remove(i.next(),i);
    }
    this.setEnabled(false);
  }

  protected void add(Entry e)
  {
    int idx=0;
    if (sorted) {
      ListIterator<Entry> i=entries();
      while (i.hasNext()) {
        Entry s=i.next();

        if (s.name.compareTo(e.name)>=0) {
          // System.out.println("add "+n+" at "+idx);
          list.add(idx, e);
          super.add(e, idx);
          idx=-1;
          break;
        }
        idx++;
      }
    }
    if (idx>=0) {
      // System.out.println("append "+n);
      list.add(e);
      super.add(e);
    }
    this.setEnabled(true);
  }

  public void add(QualifiedMandelName n)
  {
    Entry e=lookup(n);
    if (e==null) {
      e=createEntry(n);
      add(e);
    }
  }

  public void remove(QualifiedMandelName n)
  {
    Entry entry=lookup(n);
    if (entry!=null) {
       remove(entry, null);
       if (list.isEmpty()) setEnabled(false);
    }
  }

  protected void remove(Entry e, ListIterator<Entry> i)
  {
    if (i!=null) i.remove();
    else list.remove(e);
    super.remove(e);
    if (list.isEmpty()) setEnabled(false);
  }

  protected ListIterator<Entry> entries()
  {
    return list.listIterator();
  }
  
  protected class Entry extends JMenuItem
                     implements ActionListener {
    QualifiedMandelName name;

    public Entry(QualifiedMandelName name)
    {
      this.name=name;
      String txt;
      if (shortnames) {
        txt=name.getMandelName().getSubAreaName();
        if (name.getQualifier()!=null) {
          txt+="-"+name.getQualifier();
        }
      }
      else {
        txt=name.toString();
      }
      this.setText(txt);
      this.setName(txt);
      this.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e)
    {
      QualifiedMandelName sel=name;

      if (sel==null) return;
      for (SelectAction a:actions) {
        a.selectArea(access, sel);
      }
      selectArea(sel);
    }

   }
  
   protected abstract void selectArea(QualifiedMandelName name);
}
