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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTextField;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.util.Utils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Uwe Krueger
 */
public class MandelAreaCreationDialog extends MandelAreaViewDialog {

  public MandelAreaCreationDialog(MandelWindowAccess owner, String title, String file,
                                 MandelName name,
                                 MandelInfo info)
  {
    super(owner, title, new QualifiedMandelName(name), info, true, false);
    setFilename(file);
  }

  public MandelAreaCreationDialog(MandelWindowAccess owner, String title)
  {
    super(owner, title, true, false);
  }

  @Override
  protected MandelAreaView createView(QualifiedMandelName name, Object info,
                                      boolean change, boolean readonly)
  {
    return new CreationView(name,(MandelInfo)info);
  }

  public void setFilename(String file)
  {
    ((CreationView)getView()).setFilename(file);
  }

  public void setFilename(String file, boolean modifiable)
  {
    ((CreationView)getView()).setFilename(file,modifiable);
  }

  public void setAutoMode(boolean b)
  {
    ((CreationView)getView()).setAutoMode(b);
  }

  public MandelWindowAccess getMandelFrame()
  { return getMandelWindowAccess();
  }

  protected void handleClose()
  {
    ((CreationView)getView()).handleClose();
  }
  
  ///////////////////////////////////////////////////////////////////////
  // view
  ///////////////////////////////////////////////////////////////////////

  protected class CreationView extends MandelAreaView {
    protected JTextField filename;

    protected boolean automode;

    public CreationView(QualifiedMandelName name, MandelInfo info)
    {
      super(name, info, true, false);
      automode=true;
      getDialog().addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e)
        {
          handleClose();
        }

        @Override
        public void windowClosed(WindowEvent e)
        {
          System.out.println("closed creation area");
        }
      });
    }

    protected void handleClose()
    {
      System.out.println("closing creation dialog "+getClass().getSimpleName());
      if (rect!=null) {
        System.out.println(" discard old rect");
        rect.discard();
        if (rect.getName()!=null) {
          fireMandelAreaEvent(
                  new MandelAreaEvent(MandelAreaCreationDialog.this,
                                      MandelAreaEvent.MA_UPDATE));
        }
        rect.setProportionProvider(null);
        rect=null;
      }
    }

    public void setFilename(String n)
    {
      setFilename(n,true);
    }

    public void setAutoMode(boolean b)
    {
      automode=b;
    }

    public void setFilename(String n, boolean modifiable)
    {
      filename.setText(n);
      filename.setEditable(modifiable);
    }

    public String getFilename()
    {
      return filename.getText();
    }

    @Override
    protected void setupFields()
    {
      super.setupFields();
      int row=getMaxGridRow()+1;

      setMaxGridRow(row);
      add(filename=new JTextField(),GBC(0,row).setSpanW(getMaxGridCol()+1).
                                               setFill(GBC.HORIZONTAL).
                                               setInsets(10,0,0,0).
                                               setWeight(200, 0));
    }

    @Override
    protected void setupButtons()
    {
      super.setupButtons();
      createButton("AdjustX","Adjust area width to preserve image propotion.",
                   new AdjustXAction());
      createButton("AdjustY","Adjust area height to preserve image propotion.",
                   new AdjustYAction());
      createButton("Adjust Width","Adjust image width to preserve image propotion.",
                   new AdjustWidthAction());
      createButton("Adjust Height","Adjust image height to preserve image propotion.",
                   new AdjustHeightAction());
      createButton("Normalize","Normalize coordinates preserving visible area.",
                   new NormAction());
      createButton("Round","Round image area specifical to useful precision.",
                   new RoundAction());

      newButtonPanel();
      createButton("Save",null,new SaveAction());
      createButton("Load",null,new LoadAction());
      createButton("Delete",null,new DeleteAction());
    }

    protected VisibleRect getSelectedRect()
    { return null;
    }

    private class AdjustXAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        MandelInfo info=new MandelInfo(getInfo());
        MandUtils.adjustDX(info);
        setInfo(info);
      }
    }

    private class AdjustYAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        MandelInfo info=new MandelInfo(getInfo());
        MandUtils.adjustDY(info);
        setInfo(info);
      }
    }

    private class AdjustWidthAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        MandelInfo info=new MandelInfo(getInfo());
        MandUtils.adjustWidth(info);
        setInfo(info);
      }
    }

    private class AdjustHeightAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        MandelInfo info=new MandelInfo(getInfo());
        MandUtils.adjustHeight(info);
        setInfo(info);
      }
    }

    private class NormAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        MandelInfo info=new MandelInfo(getInfo());
        MandUtils.normalize(info);
        setInfo(info);
      }
    }

    private class RoundAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        MandelInfo info=new MandelInfo(getInfo());
        MandUtils.round(info);
        setInfo(info);
      }
    }

    private class SaveAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(getFilename())) return;
        MandelInfo info=getInfo();
        info.setSite(getEnvironment().getProperty(Settings.SITE));
        info.setCreator(getEnvironment().getProperty(Settings.USER));
        info.setCreationTime(System.currentTimeMillis());
        MandelData md=new MandelData(info);
        MandelFileName name;
        
        File f=new File(getFilename());
        name=MandelFileName.create(f);
        if (name==null) {
          mandelError(f.getName()+" is no valid mandel area name.");
          return;
        }
        if (f.exists()) {
          if (!overwriteFileDialog(f)) return;
        }
        getMandelWindowAccess().getEnvironment().autoRescan();
        if (getMandelWindowAccess().getEnvironment().getImageDataScanner().
                                 getMandelHandle(name.getQualifiedName())!=null) {
          mandelError("Image for "+name+" already exists.");
          return;
        }
        try {
          md.write(f);
          //getFrame().getEnvironment().addLogicalFile(f);
          fireCreationEvent(name.getName(),md.getInfo(),getSelectedRect());
          getDialog().setVisible(false);
        }
        catch (IOException ex) {
          mandelError("Cannot write mandel info file",ex);
        }
      }
    }

    private class LoadAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(getFilename())) return;
        File  file=new File(getFilename());
        MandelFileName mfn=MandelFileName.create(file.getName());
        if (mfn==null) {
          mandelError(getFilename()+" is no valid mandel area name.");
          return;
        }

        try {
          AbstractFile f=getMandelWindowAccess().getEnvironment().
                                    createMandelFile(getFilename());
          MandelData tmp=new MandelData(f);
          if (!tmp.getHeader().isInfo()) {
            mandelError("this is not a mandel parameter file.");
            return;
          }
          getInfo().copyFrom(tmp.getInfo());
          updateFields();
        }
        catch (IOException ex) {
          mandelError("Cannot read mandel info file",ex);
        }
      }
    }

    private class DeleteAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(getFilename())) return;
        File f=new File(getFilename());
        MandelName name=MandelName.create(f);
        if (name==null) {
          mandelError(f.getName()+" is no valid mandel area name.");
          return;
        }
        if (!f.exists()) {
          mandelError("File does not exist.");
          return;
        }
        if (!deleteFileDialog(f)) return;
        try {
          MandelFolder.Util.delete(f);
        }
        catch (IOException ex) {
          mandelError("Delete Error: "+ex);
        }
        if (f.exists()) {
          mandelError("Cannot delete "+f+".");
          return;
        }
        fireDeletionEvent(name,f);
        System.out.println("are editable "+filename.isEditable());
        if (!filename.isEditable()) getDialog().setVisible(false);
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////
  // Own Events
  //////////////////////////////////////////////////////////////////////////
  
  static public class MandelAreaEvent extends EventObject {
    static public final int MA_CREATED = 1;
    static public final int MA_DELETED = 2;
    static public final int MA_UPDATE  = 3;

    private int        id;
    private MandelInfo info;
    private MandelName name;
    private VisibleRect rect;
    private File        file;

    public MandelAreaEvent(Component c, MandelName name, MandelInfo info,
                         VisibleRect r)
    {
      this(c,MA_CREATED);
      this.name=name;
      this.info=info;
      this.rect=r;
    }

    public MandelAreaEvent(Component c, MandelName name, File file)
    {
      this(c,MA_DELETED);
      this.name=name;
      this.file=file;
    }

    public MandelAreaEvent(Component c, int id)
    {
      super(c);
      this.id=id;
    }

    public int getId()
    {
      return id;
    }

    public MandelInfo getInfo()
    {
      return info;
    }

    public MandelName getName()
    {
      return name;
    }

    public VisibleRect getRect()
    {
      return rect;
    }

    public File getFile()
    {
      return file;
    }

    public Object getOwner()
    {
      return rect.getOwner();
    }
  }

  public interface MandelAreaListener {

    void areaActionPerformed(MandelAreaEvent e);
  }

  //////////////////////////////////////////////////////////////////////////
  private Set<MandelAreaListener> listeners=new HashSet<MandelAreaListener>();

  public void addCreationListener(MandelAreaListener l)
  {
    listeners.add(l);
  }

  public void removeCreationListener(MandelAreaListener l)
  {
    listeners.remove(l);
  }

  protected void fireCreationEvent(MandelName name, MandelInfo info,
                                   VisibleRect r)
  {
    fireMandelAreaEvent(new MandelAreaEvent(this, name, info, r));
  }

  protected void fireDeletionEvent(MandelName name, File file)
  {
    fireMandelAreaEvent(new MandelAreaEvent(this, name, file));
  }

  protected void fireMandelAreaEvent(MandelAreaEvent e)
  {
    for (MandelAreaListener l:listeners) {
      l.areaActionPerformed(e);
    }
  }
}
