
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krüger
 */
public class MandelListProxyListModelForTable
             extends AbstractMandelListListModel
             implements ProxyModel<MandelListTableModel> {
  
  /////////////////////////////////////////////////////
  // Event forwarding
  private class Listener implements TableModelListener {

    public void tableChanged(TableModelEvent e)
    {
      switch (e.getType()) {
        case TableModelEvent.UPDATE:
          fireContentsChanged(MandelListProxyListModelForTable.this,
                              e.getFirstRow(),e.getLastRow());
          cleanupThumbnails();
          break;
        case TableModelEvent.INSERT:
          fireIntervalAdded(MandelListProxyListModelForTable.this,
                              e.getFirstRow(),e.getLastRow());
          break;
        case TableModelEvent.DELETE:
          fireIntervalRemoved(MandelListProxyListModelForTable.this,
                              e.getFirstRow(),e.getLastRow());
          cleanupThumbnails();
          break;
      }
    }
  }

  /////////////////////////////////////////////////////
  // main class
  protected MandelListTableModel model;
  protected boolean bound;
  private TableModelListener listener;
  
  protected MandelListProxyListModelForTable()
  {
    listener=new Listener();
  }

  public MandelListProxyListModelForTable(MandelListTableModel model)
  {
    this();
    setModel(model);
    if (model!=null) setModifiable(model.isModifiable());
    bind();
  }

  public MandelListTableModel getModel()
  {
    return model;
  }

  public void setModel(MandelListTableModel model)
  {
    if (model!=this.model) {
      if (bound && this.model!=null) {
        this.model.removeTableModelListener(listener);
      }
      this.model=model;
      if (bound) {
        if (model!=null) {
          model.addTableModelListener(listener);
        }
      }
      fireContentsChanged(this,0,Integer.MAX_VALUE);
    }
  }

  public void bind()
  {
    if (!bound) {
      if (model!=null) model.addTableModelListener(listener);
      bound=true;
    }
  }

  public void unbind()
  {
    if (bound) {
      if (model!=null) model.removeTableModelListener(listener);
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
    if (model==null) return null;
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