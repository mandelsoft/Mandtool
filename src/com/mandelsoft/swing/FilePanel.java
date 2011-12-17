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

package com.mandelsoft.swing;

import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class FilePanel extends GBCPanel {
  public static final String PROP_FILENAME="filename";

  private String name;
  private FileFilter filter;
  private JTextField field;
  private JButton select;
  private Window frame;
  private boolean editable;
  private int mode=JFileChooser.FILES_ONLY;

  public FilePanel(String name, String def, Window frame)
  {
    this(name,def,null,frame);
  }

  public FilePanel(String name, String def, FileFilter filter, Window frame)
  {
    super();
    this.frame=frame;
    this.filter=filter;
    add(new JLabel(name), GBC(0, 0).setWeight(0, 0).setRightInset(10));
    add(field=new JTextField(),
        GBC(1, 0).setFill(com.mandelsoft.swing.GBC.HORIZONTAL).setWeight(200, 0));
    field.setText(def);
    field.setColumns(30); 
    field.getDocument().addDocumentListener(new FilenameListener());

    select=new JButton("select");
    add(select, GBC(2, 0).setWeight(0, 0));
    select.setMargin(new Insets(0, 0, 0, 0));
    select.addActionListener(new FileChooserAction());
  }

  public boolean isEditable()
  {
    return editable;
  }

  public void setEnableChooser(boolean editable)
  {
    this.editable=editable;
    select.setVisible(editable);
    //field.setEditable(editable);
  }

  public String getFilename()
  {
    return field.getText();
  }

  public void setFilename(String name)
  {
    field.setText(name);
  }

  public void setFileChooserMode(int m)
  {
    mode=m;
  }

  private class FilenameListener implements DocumentListener {
    private String old=field.getText();

    private void change()
    {
      String f=field.getText();
      if (f.equals(old)) return;
      firePropertyChange(PROP_FILENAME, old, field.getText());
      old=field.getText();
    }

    public void insertUpdate(DocumentEvent e)
    {
      change();
    }

    public void removeUpdate(DocumentEvent e)
    {
      change();
    }

    public void changedUpdate(DocumentEvent e)
    {
      change();
    }
  }

  private class FileChooserAction implements ActionListener {

    public void actionPerformed(ActionEvent e)
    {
      JFileChooser c=new JFileChooser();
      c.setLocale(Locale.UK);
      if (filter!=null) c.setFileFilter(filter);
      String cur=field.getText();
      if (Utils.isEmpty(cur)) cur=".";
      File f=new File(cur);
      if (f.isDirectory()) c.setCurrentDirectory(f);
      else {
        c.setSelectedFile(f);
      }
      c.setFileSelectionMode(mode);
      int result=c.showDialog(frame, "select");
      if (result==JFileChooser.APPROVE_OPTION) {
        field.setText(c.getSelectedFile().getPath());
      }
    }
  }
}
