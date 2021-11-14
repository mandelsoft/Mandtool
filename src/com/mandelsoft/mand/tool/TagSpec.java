
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

import java.awt.Window;
import java.awt.event.ActionEvent;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.util.Utils;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Uwe Krüger
 */

public class TagSpec extends MandelSpecDialog<TagSpec.TagPanel> {

  public TagSpec(Window parent, String name, boolean change)
  {
    super(parent,"Tag Specification",name,change);
   
  }

  @Override
  protected TagPanel createPanel(String name, boolean change)
  {
    return new TagPanel(name,change);
  }

  public class TagPanel extends MandelSpecDialog.Panel {
    private JList tags;
    private DefaultListModel model;
    private JComboBox input;
    private JPanel buttons;
    private JButton add;
    private JButton remove;
    private JButton clear;

    public TagPanel(String name, boolean change)
    {
      super(name,change);
      model=new DefaultListModel();
      //model.addElement("Initial");
      tags=new JList(model);
      tags.setPrototypeCellValue("InitialTagValueForMandelArea");
      tags.setVisibleRowCount(10);
      tags.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tags.setLayoutOrientation(JList.VERTICAL);
      tags.setFixedCellWidth(200);
      tags.addListSelectionListener(new Listener());
      JScrollPane sp=new JScrollPane(tags);
      add(sp,new GBC(0,1));

      input=new JComboBox();
      input.setEditable(true);
      add(input,GBC(0,2,GBC.HORIZONTAL).setTopInset(10));

      buttons=new JPanel();
      buttons.setLayout(new FlowLayout());
      add(buttons, GBC(0, 3).setWeight(100, 0));

      add=new JButton(new AddAction());
      buttons.add(add);
      remove=new JButton(new RemoveAction());
      buttons.add(remove);
      clear=new JButton(new ClearAction());
      buttons.add(clear);

      setEditable(change);
    }

    private class Listener implements ListSelectionListener {
      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        String s=(String)tags.getSelectedValue();
        if (s!=null) {
           input.setSelectedItem(s);
        }
      }
    }
    
    @Override
    protected void panelBound()
    {
      super.panelBound();
      ComboBoxModel cbm=getMandelWindowAccess().getEnvironment().getTagsModel();
      input.setModel(cbm);
    }

    @Override
    protected void panelUnbound()
    {
      super.panelUnbound();
      ComboBoxModel cbm=new DefaultComboBoxModel();
      input.setModel(cbm);
    }

    @Override
    protected void _setInfo(MandelInfo info)
    {
      model.clear();
      for (String tag:info.getKeywords()) {
        model.addElement(tag);
      }
    }

    @Override
    public boolean updateInfo(MandelInfo info)
    {
      super.updateInfo(info);
      Set<String> tmp=new HashSet<String>();
      for (Object o: model.toArray()) {
        String s=o.toString();
        if (!Utils.isEmpty(s))
           tmp.add(s);
      }
      Set<String> old=info.getKeywords();
      if (Utils.equals(old, tmp)) return false;
      info.setKeywords(tmp);
      return true;
    }

    @Override
    public void setEditable(boolean b)
    {
      super.setEnabled(b);
      input.setVisible(b);
      add.setVisible(b);
      remove.setVisible(b);
      clear.setVisible(b);
    }

    ///////////////////////////////////////////////////////////////////////

    private class AddAction extends AbstractAction {

      public AddAction()
      {
        super("Add");
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        String s=(String)input.getSelectedItem();
        if (!Utils.isEmpty(s)) {
          if (!model.contains(s)) {
            model.addElement(s);
            getMandelWindowAccess().getEnvironment().getTagsModel().addElement(s);
            fireChangeEvent();
          }
        }
      }

    }

    ///////////////////////////////////////////////////////////////////////
    
    private class RemoveAction extends AbstractAction {

      public RemoveAction()
      {
        super("Remove");
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        Object[] values=tags.getSelectedValues();
        if (values!=null && values.length>0) {
          for (Object v :values) {
            model.removeElement(v);
          }
          fireChangeEvent();
        }
      }
    }

    ///////////////////////////////////////////////////////////////////////

    private class ClearAction extends AbstractAction {

      public ClearAction()
      {
        super("Clear");
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        model.clear();
        fireChangeEvent();
      }
    }
  }
}
