/*
 * Copyright 2021 d021770.
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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.util.ChangeEvent;

/**
 *
 * @author d021770
 */
public class MandelListTableProxyModelForListModel extends AbstractMandelListTableModel {

  protected MandelListModel model;

  public MandelListTableProxyModelForListModel(MandelListModel m)
  {
    model = m;
    m.addMandelListListener(new Listener());
  }

  private class Listener implements MandelListListener {

    @Override
    public void listChanged(ChangeEvent evt)
    {
      fireTableDataChanged();
    }
  }
  
  @Override
  public void refresh(boolean soft)
  {
    model.refresh(soft);
  }

  @Override
  public void setDuplicates(boolean m)
  {
    model.setDuplicates(m);
  }

  @Override
  public void setList(MandelList list)
  {
    model.setList(list);
  }

  @Override
  public MandelScanner getMandelScanner()
  {
     return model.getMandelScanner();
  }

  @Override
  public boolean allowDuplicates()
  {
    return model.allowDuplicates();
  }

  @Override
  public void add(QualifiedMandelName name)
  {
    model.add(name);
  }

  @Override
  public void add(int index, QualifiedMandelName name)
  {
    model.add(index, name);
  }

  @Override
  public void addAll(QualifiedMandelName[] names)
  {
    model.addAll(names);
  }

  @Override
  public void addAll(int index, QualifiedMandelName[] names)
  {
    model.addAll(index, names);
  }

  @Override
  public void remove(QualifiedMandelName name)
  {
    model.remove(name);
  }

  @Override
  public MandelList getList()
  {
    return model.getList();
  }

  @Override
  public void refresh(Environment env)
  {
    model.refresh(env);
  }

  @Override
  public void clear()
  {
    model.clear();
  }

  @Override
  public void addMandelListListener(MandelListListener l)
  {
    model.addMandelListListener(l);
  }

  @Override
  public void removeMandelListListener(MandelListListener l)
  {
    model.removeMandelListListener(l);
  }
}
