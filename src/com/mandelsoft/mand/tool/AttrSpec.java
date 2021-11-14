
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
import com.mandelsoft.swing.TextField;
import com.mandelsoft.util.Utils;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Map;
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
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Uwe Krüger
 */

public class AttrSpec extends MandelSpecDialog<AttrSpec.AttrPanel> {

  public AttrSpec(Window parent, String name, boolean change)
  {
    super(parent,"Attribute Specification",name,change);
   
  }

  @Override
  protected AttrPanel createPanel(String name, boolean change)
  {
    return new AttrPanel(name,change);
  }

  public class AttrPanel extends MandelSpecDialog.Panel {
    private JList attrs;
    private DefaultListModel model;
    private JComboBox key;
    private TextField value;
    private JPanel buttons;
    private JButton add;
    private JButton remove;
    private JButton clear;

    public AttrPanel(String name, boolean change)
    {
      super(name,change);
      model=new DefaultListModel();
      //model.addElement("Initial");
      attrs=new JList(model);
      attrs.setPrototypeCellValue("InitialTagValueForMandelArea");
      attrs.setVisibleRowCount(10);
      attrs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      attrs.setLayoutOrientation(JList.VERTICAL);
      attrs.setFixedCellWidth(1000);
      attrs.addListSelectionListener(new Listener());
      JScrollPane sp=new JScrollPane(attrs);
      add(sp,new GBC(0,1,2,1));

      key=new JComboBox();
      key.setPrototypeDisplayValue("WWWWWWWWWWWWWWWWWWWWWW");
      key.setEditable(true);
      add(key,GBC(0,2,GBC.HORIZONTAL).setTopInset(10));
      value=new TextField();
      value.setColumns(60);
      value.setEditable(true);
      add(value,GBC(1,2,GBC.HORIZONTAL).setTopInset(10));

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
        String s=(String)attrs.getSelectedValue();
        if (s!=null) {
           String elems[]=s.split("=");
           key.setSelectedItem(elems[0]);
           value.setText(elems[1]);
        }
      }
    }
    
    @Override
    protected void panelBound()
    {
      super.panelBound();
      ComboBoxModel cbm=getMandelWindowAccess().getEnvironment().getAttributeModel();
      key.setModel(cbm);
    }

    @Override
    protected void panelUnbound()
    {
      super.panelUnbound();
      ComboBoxModel cbm=new DefaultComboBoxModel();
      key.setModel(cbm);
    }

    @Override
    protected void _setInfo(MandelInfo info)
    {
      model.clear();
      if (info.getProperties()!=null) {
        for (Map.Entry<String,String> e:info.getProperties().entrySet()) {
          model.addElement(e.getKey()+"="+e.getValue());
        }
      }
    }

    @Override
    public boolean updateInfo(MandelInfo info)
    {
      super.updateInfo(info);
      info.clearProperties();
      for (Object o: model.toArray()) {
        String s=o.toString();
        if (!Utils.isEmpty(s)) {
           String elems[]=s.split("=");
           info.setProperty(elems[0],elems[1]);
        }
      }
      return true;
    }

    @Override
    public void setEditable(boolean b)
    {
      super.setEnabled(b);
      key.setVisible(b);
      value.setVisible(b);
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
        String k=(String)key.getSelectedItem();
        String v=value.getDataValue();
        if (!Utils.isEmpty(k) && !Utils.isEmpty(v)) {
          String s=k+"=";
          String n=s+v;
          for (Object o: model.toArray()) {
            if (o.equals(n)) return;
            if (((String)o).startsWith(k)) {
              model.removeElement(o);
            }
          }
          
          model.addElement(n);
          fireChangeEvent();
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
        String k = (String)key.getSelectedItem();
        if (!Utils.isEmpty(k)) {
          String s = k + "=";
          for (Object o : model.toArray()) {
            if (((String) o).startsWith(k)) {
              model.removeElement(o);
              fireChangeEvent();
              return;
            }
          }
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
