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

import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.swing.GBCPanel;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.tool.mapper.MapperModel;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.IntegerField;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krueger
 */
public class InitialStartupFrame extends JFrame
                          implements MandelWindowAccess {

  private ToolEnvironment env;

  private MandelAreaCreationDialog create;


  private JLabel screensize;
  private JButton setimagesize;
  private IntegerField sizex;
  private IntegerField sizey;
  private JTextField rootfolder;

  public InitialStartupFrame(ToolEnvironment env)
  {
    this.env=env;
    setTitle("Control Panel");
    JPanel p=new ControlPanel();
    add(p);
    pack();
    setResizable(false);

    create=new MandelAreaCreationDialog(this,"Mandel Area Creation");
  }

  public JFrame getMandelWindow()
  { return this;
  }

  public ToolEnvironment getEnvironment()
  { return env;
  }

  public MandelName getMandelName()
  { QualifiedMandelName n=getQualifiedName();
    return n==null?null:n.getMandelName();
  }

  public QualifiedMandelName getQualifiedName()
  { return getEnvironment().getInitialName();
  }

  public MandelData getMandelData()
  {
    MandelName name=getMandelName();
    if (name!=null) {
      MandelHandle h=getEnvironment().getMetaScanner().getMandelData(name);
      try {
        return h.getData();
      }
      catch (IOException ex) {
         // not found
      }
    }
    return null;
  }

  public MandelImage getMandelImage()
  {
    return null;
  }

  public MapperModel getMapperModel()
  {
    return null;
  }

  public ColormapModel getColormapModel()
  {
    return null;
  }

  public MandelImagePanel getMandelImagePane()
  {
    return null;
  }

  public History getHistory()
  {
    return null;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Panel
  ///////////////////////////////////////////////////////////////////////////

  private class ControlPanel extends GBCPanel {

    ControlPanel()
    { JButton b;

      int row=0;

      add(new JLabel("Default Root Image Size"),
            GBC(3, row).setSpanW(3).setInsets(0, 10, 10, 10));
      row++;

      add(new JLabel("Screen size"),
              GBC(0, row).setSpanH(2).setRightInset(10));
      add(screensize=new JLabel(""), GBC(1, row).setSpanH(2));
      setScreenSize();

      add(setimagesize=new JButton(createImageIcon(
              "/com/mandelsoft/mand/resc/rightarrow.gif")),
              GBC(2, row).setSpanH(2).setInsets(10));
      setimagesize.setFocusable(false);
      setimagesize.setIconTextGap(0);
      setimagesize.setHorizontalAlignment(JButton.CENTER);
      setimagesize.setMargin(new Insets(0,0,0,0));
      //setimagesize.setPreferredSize(new Dimension(20,30));
      setimagesize.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        { setDefaultSize();
        }
      });

      add(new JLabel("Width"), GBC(3, row).setRightInset(10));
      add(new JLabel("Height"), GBC(3, row+1).setRightInset(10));
      add(sizex=new IntegerField(), GBC(4, row).setSpanW(2));
      add(sizey=new IntegerField(), GBC(4, row+1).setSpanW(2));
      //add(new JLabel("s"),GBC(5,1));
      sizex.setColumns(10);
      sizey.setColumns(10);
      // set dummy cell for sett weight
      add(new JLabel(""),GBC(4,row).setWeight(200, 0));setDefaultSize();
      row+=2;
      addBorder(3,row-3,3,3,false);
      
      add(rootfolder=new JTextField(),GBC(0,row).setSpanW(5).
                                               setFill(GBC.HORIZONTAL).
                                               setInsets(10,0,0,0).
                                               setWeight(200, 0));
      rootfolder.setText(env.getProperty(Settings.INFO_SAVE_PATH));
      add(b=new JButton("select"),GBC(5,row).setInsets(10,0,0,0));
      b.setMargin(new Insets(0,0,0,0));
      b.addActionListener(new FileChooserAction());
      row++;

      JPanel buttons=new JPanel();
      add(buttons,GBC(0,row).setSpanW(6));

      b=new JButton("Rescan");
      b.setToolTipText("Rescan the file system");
      b.addActionListener(new RescanAction());
      buttons.add(b);

      b=new JButton("Load");
      b.addActionListener(new LoadAction());
      buttons.add(b);
      
      b=new JButton("Create root");
      b.addActionListener(new CreateRootAction());
      buttons.add(b);

      b=new JButton("AdjustX");
      b.addActionListener(new AdjustXAction());
      b.setToolTipText("Adjust image size according to proportion of the screen");
      buttons.add(b);
      b=new JButton("AdjustY");
      b.setToolTipText("Adjust image size according to proportion of the screen");
      b.addActionListener(new AdjustYAction());
      buttons.add(b);

      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void setDefaultSize()
    { Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
      sizex.setValue(d.getWidth());
      sizey.setValue(d.getHeight());
    }
    
    /////////////////////////////////////////////////////////////////////////
    // commands
    /////////////////////////////////////////////////////////////////////////
    
    private class FileChooserAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      { JFileChooser c=env.getFileChooser();
        String cur=rootfolder.getText();
        if (cur==null || cur.equals("")) cur=".";
        File f=new File(cur);
        if (f.isDirectory()) c.setCurrentDirectory(f);
        else {
          //c.setCurrentDirectory(f.getParentFile());
          c.setSelectedFile(f);
        }
        c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result=c.showDialog(InitialStartupFrame.this, "select");
        if (result==JFileChooser.APPROVE_OPTION) {
          rootfolder.setText(c.getSelectedFile().getPath());
        }
      }
    }

    private class CreateRootAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      { if (Utils.isEmpty(rootfolder.getText())) return;

        File f=new File(rootfolder.getText());

        if (f.isDirectory()) {
          f=new File(f,MandelName.ROOT_NAME+MandelData.INFO_SUFFIX);
        }
        else {
          if (!f.getParentFile().isDirectory()) {
            Error("Root Creation",f.getParentFile()+" is no directory.");
            return;
          }
          String n=f.getName();
          if (MandelName.isMandelName(n)) {
            n+=MandelData.INFO_SUFFIX;
            f=new File(f.getParentFile(),n);
          }
        }
        
        MandelInfo info=MandUtils.createRoot();
        Dimension d=new Dimension(sizex.getValue().intValue(),
                                  sizey.getValue().intValue());
        System.out.println("root dimension "+d);
        MandUtils.adjustMandelInfo(info, d);
        System.out.println("root dimension "+info.getRX()+"x"+info.getRY());
        create.setInfo("Mandel Root", info);
        create.setFilename(f.getPath());
        create.setVisible(true);
      }
    }

    private class AdjustXAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      { Dimension d=getScreenSize();
        sizex.setValue(d.getWidth()/d.getHeight()*sizey.getValue().doubleValue());
      }
    }

    private class AdjustYAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      { Dimension d=getScreenSize();
        sizey.setValue(d.getHeight()/d.getWidth()*sizex.getValue().doubleValue());
      }
    }

    private class RescanAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        env.rescan();
      }
    }
    private class LoadAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      { String n=rootfolder.getText();
        MandelName mn;
        MandelData md;

        try {
          if (Utils.isEmpty(n)) {
            mn=MandelName.ROOT;
            env.createMandelImageFrame(mn);
          }
          else {
            env.createMandelImageFrame(n);
          }
        }
        catch (IOException ex) {
          Error("Cannot show image",ex.toString());
        }
      }
    }
  }

  public Dimension getScreenSize()
  { return Toolkit.getDefaultToolkit().getScreenSize();
  }

  public void setScreenSize()
  {
    Dimension d=getScreenSize();
    screensize.setText(""+(int)d.getWidth()+"x"+(int)d.getHeight());
  }

  protected ImageIcon createImageIcon(String path)
  {
    java.net.URL imgURL=this.getClass().getResource(path);
    ImageIcon im= new ImageIcon(imgURL);
    return im;
  }


  ///////////////////////////////////////////////////////////////////////
  // test
  ///////////////////////////////////////////////////////////////////////
  public static void main(final String[] args)
  {
    try {
      ToolEnvironment env=new ToolEnvironment(args);
      env.startup();
    }
    catch (IllegalConfigurationException ex) {
      System.out.println("failed: "+ex);
    }
  }
}
