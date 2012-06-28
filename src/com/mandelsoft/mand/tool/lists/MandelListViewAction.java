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

package com.mandelsoft.mand.tool.lists;

import java.awt.event.ActionEvent;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.DefaultMandelListTableModel;
import com.mandelsoft.mand.tool.MandelListDialog;
import com.mandelsoft.mand.tool.MandelListTableModel;
import com.mandelsoft.mand.tool.MandelWindowAccess;
import com.mandelsoft.mand.tool.ToolEnvironment;
import com.mandelsoft.mand.tool.util.MandelContextAction;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.lists.MandelBasenameSource;
import com.mandelsoft.mand.util.lists.MandelListFactory;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelListViewAction extends MandelContextAction {
  private MandelScanner scanner;
  private ActionEvent event;

  public MandelListViewAction(String name)
  {
    super(name);
  }

  public MandelListViewAction(String name, MandelScanner scanner)
  {
    this(name);
    this.scanner=scanner;
  }

  protected MandelWindowAccess getMandelWindowAccess()
  {
    return getMandelWindowAccess(event);
  }

  protected ToolEnvironment getEnvironment()
  {
    return getMandelWindowAccess().getEnvironment();
  }

  protected MandelScanner getMandelScanner(ToolEnvironment env)
  {
    return env.getAllScanner();
  }

  protected MandelScanner getMandelScanner()
  {
    if (scanner==null) {
      if (event!=null) {
        MandelWindowAccess acc=getMandelWindowAccess(event);
        if (acc!=null) {
          ToolEnvironment env=acc.getEnvironment();
          if (env!=null) {
            scanner=getMandelScanner(env);
          }
        }
      }
    }
    return scanner;
  }

  protected ActionEvent getEvent()
  {
    return event;
  }

  public void actionPerformed(ActionEvent e)
  {
    QualifiedMandelName n=getSelectedItem(e);
    if (n!=null) {
      event=e;
      MandelListFactory factory=createFactory(n);
      MandelListTableModel model=new Model(scanner,factory);
      MandelListDialog dia=new MandelListDialog(getMandelWindowAccess(e),factory.getTitle(),model);
      if (factory instanceof MandelBasenameSource) {
        dia.setRootName(((MandelBasenameSource)factory).getBasename());
      }
      event=null;
    }
  }

  protected abstract MandelListFactory createFactory(QualifiedMandelName n);

  protected static class Model extends DefaultMandelListTableModel {
    private MandelListFactory factory;

    public Model(MandelScanner scanner, MandelListFactory factory)
    {
      super(factory.getList(),scanner);
      this.factory=factory;
      this.setModifiable(false);
    }

    @Override
    public void refresh()
    {
      MandelList list=factory.getList();
      if (!getList().equals(list)) {
        setList(list);
      }
    }

    @Override
    public void refresh(Environment env)
    {
      refresh();
    }


  }
}
