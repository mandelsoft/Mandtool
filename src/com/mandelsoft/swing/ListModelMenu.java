/*
 * Copyright 2025 uwekr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author uwekr
 */
public class ListModelMenu<E> extends JMenu {
  
  private List<Entry> list;
  private ListModel<E> model;
  private ListDataListener listener;
  private boolean sorted;
  private int minSize;
  
  public ListModelMenu(String name, ListModel<E> model) {
    this(model);
    setText(name);
    minSize=1;
  }

  public ListModelMenu(ListModel<E> model)
  {
    super();
    this.model=model;
    this.list=new ArrayList();
    this.listener = new Listener();
    model.addListDataListener(this.listener);
  }
  
  public void setMinSize(int s)
  {
    if (s>=0) {
      minSize=s;
    }
  }
  
  protected String getText(E e)
  {
    return e.toString();
  }
  
  protected int compare(E e1, E e2)
  {
    return e1.toString().compareTo(e2.toString());
  }
    
  protected E getElementAt(int index)
  {
    return model.getElementAt(index);
  }
  
  protected String getActionCommand(E e) {
    return getText(e);
  }
  
  protected boolean isEnabled(Object elem) {
    return true;
  }
  
  protected void update() {
    List<Entry> entries = new ArrayList<Entry>();
    int size = model.getSize();

    for (int i = 0; i < size; i++) {
      E n = getElementAt(i);
      Entry e = lookup(n, entries);
      if (e == null) {
        e = createEntry(n);
      } else {
        e.update();
      }
      entries.add(e);
    }

    clear();
    for (Entry e : entries) {
      add(e);
    }
  }
  
  protected Entry lookup(E n)
  {
    for (Entry e:list) {
      if (e.elem.equals(n)) return e;
    }
    return null;
  }

  protected Entry lookup(E n, Collection<Entry> skip)
  {
    for (Entry e:list) {
      if (e.elem.equals(n) && (skip==null || !skip.contains(e))) return e;
    }
    return null;
  }
        
  protected Entry createEntry(E n)
  {
    return new Entry(n);
  }

  protected ListIterator<Entry> entries()
  {
    return list.listIterator();
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

        if (compare(s.elem, e.elem)>=0) {
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
    if (list.size()>=minSize) {
      this.setEnabled(true);
    }
  }

  public void add(E n)
  {
    Entry e=lookup(n);
    if (e==null) {
      e=createEntry(n);
      add(e);
    }
  }

  public void remove(E n)
  {
    Entry entry=lookup(n);
    if (entry!=null) {
       remove(entry, null);
       if (list.size() < minSize) setEnabled(false);
    }
  }

  protected void remove(Entry e, ListIterator<Entry> i)
  {
    if (i!=null) i.remove();
    else list.remove(e);
    super.remove(e);
    if (list.isEmpty()) setEnabled(false);
  }

  private class Listener implements ListDataListener {

    @Override
    public void intervalAdded(ListDataEvent e) {
      update();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
      update();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
      update();
    }
  }
  
  protected class Entry extends JMenuItem
                     implements ActionListener {
    E elem;

    public Entry(E elem)
    {
      this.elem=elem;
      this.update();
      this.addActionListener(this);
    }
    
    public void update()
    {
      String txt=ListModelMenu.this.getText(elem);
       this.setText(txt);
      this.setName(txt);
      this.setActionCommand(txt);
      setEnabled(ListModelMenu.this.isEnabled(elem));
    }
    
    public void actionPerformed(ActionEvent e)
    {
      if (elem==null) return;
      ListModelMenu.this.fireActionPerformed(new ActionEvent(ListModelMenu.this, e.getID(), ListModelMenu.this.getActionCommand(elem)));
    }
  }
}
