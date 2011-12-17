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

import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.swing.ActionPanel;
import com.mandelsoft.swing.FilePanel;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.IntegerField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Uwe Krueger
 */
public abstract class PictureSavePanel extends ActionPanel
                                    implements MandelWindowAccessSource {

    protected JComboBox formats;
    protected JCheckBox decoration;
    protected IntegerField width;
    protected FilePanel imagefile;

    public PictureSavePanel()
    {
      this("Image file");
    }

    public PictureSavePanel(String fileLabel, int fileChooserMode)
    {
      this(fileLabel);
      setFileChooserMode(fileChooserMode);
    }

    public PictureSavePanel(String fileLabel)
    {
      JComponent c=new JLabel("Image Format");
      addContent(c, GBC(0, 0).setRightInset(10).setAnchor(GBC.EAST).setWeight(
              100, 100));
      String[] fmts=ImageIO.getReaderFileSuffixes();
      formats=new JComboBox(fmts);
      addContent(formats, GBC(1, 0).setAnchor(GBC.WEST).setWeight(
              100, 100));

      c=new JLabel("Modified Width");
      addContent(c, GBC(0, 1).setRightInset(10).setAnchor(GBC.EAST).setWeight(
              100, 100));
      width=new IntegerField(0);
      width.setColumns(10);
      addContent(width, GBC(1, 1).setAnchor(GBC.WEST).setWeight(
              100, 100));

      c=new JLabel("Show Creator");
      addContent(c, GBC(0, 2).setRightInset(10).setAnchor(GBC.EAST).setWeight(
              100, 100));
      decoration=new JCheckBox();
      decoration.setSelected(getEnvironment().getCopyright()!=null);
      decoration.setEnabled(!decoration.isSelected());
      addContent(decoration, GBC(1, 2).setAnchor(GBC.WEST).setWeight(
              100, 100));

      imagefile=new FilePanel(fileLabel, "", (JFrame)getOwner());
      addContent(imagefile, GBC(0, 3, 2, 1).setLayout(GBC.BOTH, GBC.NORTH).setWeight(
              100, 100).
              setInsets(10, 10, 10, 10));
    }

    void updateState()
    {
      AbstractFile file=getMandelWindowAccess().getMandelData().getFile();
      if (file!=null) {
        if (!file.isFile()) decoration.setSelected(true);
        decoration.setEnabled(file.isFile());
      }
    }

    protected ToolEnvironment getEnvironment()
    {
      return getMandelWindowAccess().getEnvironment();
    }

    public void setFileChooserMode(int m)
    {
      imagefile.setFileChooserMode(m);
    }

    /////////////////////////////////////////////////////////////////////////
    public class ClearAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        imagefile.setFilename("");
      }
    }

    /////////////////////////////////////////////////////////////////////////

  }
