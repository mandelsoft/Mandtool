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

import com.mandelsoft.swing.ProxyModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krüger
 */
public class MandelListProxyListModel extends AbstractMandelListListModel
                                   implements ProxyModel<MandelListListModel> {
  
  /////////////////////////////////////////////////////
  // Event forwarding
  private class Listener implements ListDataListener {

    public void intervalAdded(ListDataEvent e)
    {
      fireIntervalAdded(MandelListProxyListModel.this,e.getIndex0(),e.getIndex1());
    }

    public void intervalRemoved(ListDataEvent e)
    {
      fireIntervalRemoved(MandelListProxyListModel.this,e.getIndex0(),e.getIndex1());
      cleanupThumbnails();
    }

    public void contentsChanged(ListDataEvent e)
    {
      fireContentsChanged(MandelListProxyListModel.this,e.getIndex0(),e.getIndex1());
      cleanupThumbnails();
    }
  }

  /////////////////////////////////////////////////////
  // main class
  protected MandelListListModel model;
  protected boolean bound;
  private ListDataListener listener;
  
  protected MandelListProxyListModel()
  {
    listener=new Listener();
  }

  protected MandelListProxyListModel(MandelListListModel model)
  {
    this();
    setModel(model);
    bind();
  }

  public MandelListListModel getModel()
  {
    return model;
  }

  public void setModel(MandelListListModel model)
  {
    if (model!=this.model) {
      if (bound && this.model!=null) {
        this.model.removeListDataListener(listener);
      }
      this.model=model;
      if (bound) {
        if (model!=null) {
          model.addListDataListener(listener);
        }
      }
      fireContentsChanged(this,0,Integer.MAX_VALUE);
    }
  }

  public void bind()
  {
    if (!bound) {
      if (model!=null) model.addListDataListener(listener);
      bound=true;
    }
  }

  public void unbind()
  {
    if (bound) {
      if (model!=null) model.removeListDataListener(listener);
      bound=false;
    }
  }

  public boolean isBound()
  {
    return bound;
  }

  public MandelScanner getMandelScanner()
  {
    return model.getMandelScanner();
  }

  @Override
  public boolean isModifiable()
  {
    return super.isModifiable() && model.isModifiable();
  }

  @Override
  public List<Action> getActions()
  {
    List<Action> actions1=model.getActions();
    List<Action> actions2=super.getActions();
    if (actions1!=null) {
      if (actions2!=null) {
        List<Action> r=new ArrayList<Action>();
        if (actions1!=null) r.addAll(actions1);
        if (actions2!=null) r.addAll(actions2);
        return r;
      }
      return actions1;
    }
    return actions2;
  }

  public void setList(MandelList list)
  {
    throw new UnsupportedOperationException("list cannot be changed via proxy");
  }

  public void setDuplicates(boolean m)
  {
     throw new UnsupportedOperationException("duplicates cannot be changed via proxy");
  }

  public boolean allowDuplicates()
  {
    return model.allowDuplicates();
  }

  public void remove(QualifiedMandelName name)
  {
    model.remove(name);
  }

  public void refresh(Environment env)
  {
    model.refresh(env);
  }

  @Override
  public void refresh()
  {
    model.refresh();
  }

  public void refresh(boolean soft)
  {
    model.refresh(soft);
  }

  public MandelList getList()
  {
    return model.getList();
  }

  public void clear()
  {
    checkModifiable();
    model.clear();
  }

  public void addAll(QualifiedMandelName[] names)
  {
    checkModifiable();
    model.addAll(names);
  }

  public void add(QualifiedMandelName name)
  {
    checkModifiable();
    model.add(name);
  }

  public void addAll(int index, QualifiedMandelName[] names)
  {
    checkModifiable();
    model.addAll(index,names);
  }

  public void add(int index, QualifiedMandelName name)
  {
    checkModifiable();
    model.add(index,name);
  }
}