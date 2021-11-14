
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

import com.mandelsoft.swing.worker.UIFunction;
import com.mandelsoft.swing.worker.UIExecution;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.cm.ColormapSourceFactory;
import com.mandelsoft.mand.image.MandelAreaImage;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.CachedUpstreamColormapSourceFactory;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.SimpleColormapSourceFactory;
import com.mandelsoft.swing.worker.CallbackWorker;
import com.mandelsoft.swing.worker.ErrorNotification;
import com.mandelsoft.swing.worker.WorkerProgressMonitor;
import com.mandelsoft.util.Utils;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Uwe Krüger
 */
public class PictureSaveDialog extends MandelDialog {

  private PictureSavePanel panel;
  private List<MandelHandle> list;

  private PictureSaveDialog(MandelWindowAccess owner, String name)
  {
    super(owner,name);
    //System.out.println("create control dialog '"+name+"' for "+owner);
    panel=createPictureSavePanel();
    panel.updateState();
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setup();
    add(panel);
    pack();
  }

  public PictureSaveDialog(MandelWindowAccess owner, String name,
                          List<MandelHandle> list)
  {
    this(owner,name);
    this.list=list;
  }

  public PictureSaveDialog(MandelWindowAccess owner, String name,
                           MandelList list)
  {
    this(owner,name);
    this.list=new ArrayList<MandelHandle>();

    MandelScanner s=getEnvironment().getImageDataScanner();
    for (QualifiedMandelName n:list) {
      MandelHandle h=s.getMandelHandle(n);
      if (h!=null) this.list.add(h);
    }
  }

  protected PictureSavePanel createPictureSavePanel()
  {
    return new PicturePanel();
  }

  protected void setup()
  {
  }

  //////////////////////////////////////////////////////////////////////////

  public class PicturePanel extends PictureSavePanel {
    private JButton save;

    public PicturePanel()
    {
      super("Image directory", JFileChooser.DIRECTORIES_ONLY);

      ResetAction reset;
      save=addButton("Save", new SaveImageAction(),"Save picture file");
      addButton("Clear", new ClearAction(),"Clear file name");
      addButton("Reset", reset=new ResetAction(),"Generate picture path name");
      reset.actionPerformed(null);
    }

    public MandelWindowAccess getMandelWindowAccess()
    {
      return PictureSaveDialog.this.getMandelWindowAccess();
    }

    /////////////////////////////////////////////////////////////////////////
    private class ResetAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        ToolEnvironment env=getEnvironment();
        QualifiedMandelName mn=getMandelWindowAccess().getQualifiedName();
        String n=env.getProperty(Settings.BITMAP_SAVE_PATH);
        if (n==null) n=".";
        imagefile.setFilename(n);
      }
    }

   /////////////////////////////////////////////////////////////////////////
    private class SaveImageAction implements ActionListener {
      private Worker task;

      public void actionPerformed(ActionEvent e)
      {
        int width;

        String path=imagefile.getFilename();
        File file=new File(path);
        if (!file.exists()) {
          int o=JOptionPane.showOptionDialog(getOwner(), "Create Directory", path,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE, null,
                                       null, null);
          if (o!=0) return;
          if (!file.mkdirs()) {
            JOptionPane.showMessageDialog(getOwner(),
                                        "cannot create directoty", //text to display
                                        "Image IO", //title
                                        JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
        if (!file.isDirectory()) {
          JOptionPane.showMessageDialog(getOwner(),
                                        path+" is no directory", //text to display
                                        "Image IO", //title
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }
       
        save.setEnabled(false);
        try {
          width=PicturePanel.this.width.getValue().intValue();
        }
        catch (Exception ex) {
          width=0;
        }
        task=new Worker(list, getMandelWindowAccess(),
                            imagefile.getFilename(),
                            (String)formats.getSelectedItem(), width,
                            decorationButton.isSelected());
        new WorkerProgressMonitor(PictureSaveDialog.this,
                                                  "Writing Images", task);
      }
    }
    
    /////////////////////////////////////////////////////////////////////////
    private class PathAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        ToolEnvironment env=getEnvironment();
        QualifiedMandelName mn=getMandelWindowAccess().getQualifiedName();
        String n=env.getProperty(Settings.BITMAP_SAVE_PATH);
        if (n==null) n="";
        imagefile.setFilename(n);
      }
    }
  }

  private static class OverwriteQuestion
                 extends UIFunction<PictureSaveDialog,Integer> {
    private File file;

    public OverwriteQuestion(File file)
    {
      this.file=file;
    }

    synchronized
    public void execute(PictureSaveDialog d)
    {
      Object[] options={"Replace", "Ignore", "Replace all", "Ignore all", "Cancel"};
      int o=JOptionPane.showOptionDialog(d,
            file.getName()+" already exists.\n"+
            "Do you want to replace it?",
            "Warning",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            null, options, options[1]);
      setResult(o);
    }
  }

  //////////////////////////////////////////////////////////////////////
  // Worker
  //////////////////////////////////////////////////////////////////////
  private class Worker extends CallbackWorker<Void, UIExecution, PictureSaveDialog> {

    private List<MandelHandle> list;
    private ColormapSourceFactory cmfac;
    private Mapper mapper;
    private ResizeMode mode;
    private ToolEnvironment env;
    private String path;
    private String fmt;
    private int width;
    private boolean showDecoration;

    public Worker(List<MandelHandle> list, MandelWindowAccess acc,
                  String path, String fmt, int width, boolean showDecoration)
    {
      super(PictureSaveDialog.this);

      ColormapSource cm;
      ColormapModel cmm;
      this.list=list;
      this.env=acc.getEnvironment();

      cmm=acc.getColormapModel();
      cm=cmm.getColormap();
      this.mode=cmm.getResizeMode();
      this.mapper=acc.getMapperModel().getMapper();

      MandelImagePanel mp=acc.getMandelImagePane();
      if (mp!=null&&mp.getParentColormapModel().isSet()) {
        cmfac=new CachedUpstreamColormapSourceFactory(env.getImageDataScanner(),
                                                      mp.getColormapModel(),
                                                      env.getColormapCache());
        System.out.println("-> save with upstream colormap");
      }
      else {
        System.out.println("-> save with main colormap");
        cmfac=new SimpleColormapSourceFactory(cm);
      }
      
      this.path=path;
      this.fmt=fmt;
      this.width=width;
      this.showDecoration=showDecoration;
    }

    public Decoration getDecoration(MandelInfo info)
    {
      String deco = info.getProperty(MandelInfo.ATTR_TITLE);
      String copyright = env.getCopyright(info);
      if (!Utils.isEmpty(copyright)) {
        if (Utils.isEmpty(deco)) {
          deco = copyright;
        }
        else {
          deco += " " + copyright;
        }
      }

      Decoration decoration = new Decoration();
      decoration.setShowDecoration(showDecoration);
      decoration.setDecoration(deco);
      System.out.println("*** decoration is " + decoration);
      return decoration;
    }

    public MandelAreaImage getMandelImage(MandelHandle h) throws IOException
    {
      ColormapSource cm=cmfac.getColormapSource(h.getName());
      return env.getMandelImage(h, mode, cm, mapper, null);
    }

    @Override
    protected void done()
    {
      setProgress(100);
      PictureSaveDialog.this.dispose();
    }

    @Override
    protected Void doInBackground() throws Exception
    {
      int c=0;
      boolean overwrite=false;
      boolean oset=false;

      for (MandelHandle handle:list) {
        String name=path+"/"+handle.getName()+"."+fmt;
        if (this.isCancelled()) {
          break;
        }
        try {
          File f=new File(name);

          if (f.exists()) {
            if (!oset) {
              int r=call(new OverwriteQuestion(f));
              if (r==4) {
                break;
              }
              overwrite=(r%2)==0;
              oset=r>1;
            }
            if (!overwrite) {
              System.out.println("cancel "+name);
              continue;
            }
          }

          MandelAreaImage mai=getMandelImage(handle);
          BufferedImage im=mai.getImage();
          BufferedImage tmp=null;
          Graphics g=null;
          int w=width;
          int h;

          if (w!=0) {
            h=w*im.getHeight()/im.getWidth();
            tmp=new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            (g=tmp.getGraphics()).drawImage(im, 0, 0, w, h, null);
            im=tmp;
          }
          Decoration decoration=getDecoration(mai.getInfo());
          if (decoration!=null && decoration.showDecoration()) {
            if (tmp==null) {
              tmp=new BufferedImage(im.getWidth(), im.getHeight(),
                                    BufferedImage.TYPE_INT_RGB);
              (g=tmp.getGraphics()).drawImage(im, 0, 0, null);
              im=tmp;
            }
            decoration.setColorHandler(new DynamicColor(new DynamicColor.StaticImage(im)));
            if (g==null) g=im.getGraphics();
            decoration.paintDecoration(g, im.getWidth(), im.getHeight());
          }

          try {
            ImageIO.write(im, fmt, f);
          }
          catch (Exception ex) {
            call(new ErrorNotification("Image IO", "Cannot write image: "+ex.
              toString()));
          }
        }
        catch (IOException ex) {
          call(new ErrorNotification("Image IO", "Cannot find area image: "+ex.
            toString()));
        }
        finally {
          setProgress(++c*100/list.size());
          System.out.println("written "+name+" ("+c+"/"+list.size()+")");
        }
      }
      setProgress(100);
      return null;
    }
  }

}
