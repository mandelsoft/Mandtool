
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
import java.beans.PropertyChangeEvent;
import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import com.mandelsoft.mand.MandelConstants;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.tool.cm.ColormapDialog;
import com.mandelsoft.mand.tool.mapper.MapperControl;
import com.mandelsoft.mand.tool.mapper.MapperPanel;
import com.mandelsoft.swing.ActionPanel;
import com.mandelsoft.swing.BooleanAttribute;
import com.mandelsoft.swing.BufferedComponent.ProportionalRectangleSelector;
import com.mandelsoft.swing.BufferedComponent.RectangleSelector;
import com.mandelsoft.swing.BufferedFrame;
import com.mandelsoft.swing.FilePanel;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import com.mandelsoft.swing.IntegerField;
import com.mandelsoft.swing.ScaleAdapter;
import com.mandelsoft.swing.ScaleEvent;
import com.mandelsoft.swing.ScaleEventListener;
import com.mandelsoft.swing.TablePanel;
import com.mandelsoft.util.Utils;
import java.awt.Graphics;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author Uwe Krüger
 */
public class ImageControl extends ControlDialog {
  private static final boolean debug=false;
  
  private javax.swing.event.ChangeListener listener;                                          
  private ImageControlPanel image;
  private MappingPanel      mapping;
  private ColormapsPanel    colormaps;
  private DisplayPanel      display;
  private PicturePanel      picture;

  public ImageControl(MandelWindowAccess owner)
  {
    super(owner, "Image Control");
  }
  
  protected void setup()
  {
    listener=new ImageListener();
    getMandelWindowAccess().getMandelImagePane().addChangeListener(listener);

    // tab image
    addTab("Image", image=new ImageControlPanel());

    // tab colormaps
    colormaps=new ColormapsPanel();
    addTab("Colormaps", colormaps);

    // tab mapper
    addTab("Mapping", mapping=new MappingPanel());

    if (!getEnvironment().isReadonly()) {
      // image
      addTab("Picture", picture=new PicturePanel());

      // data
      addTab("Data", new DataPanel());
    }

    // tab mapper
    addTab("Display", display=new DisplayPanel());
  }


  ////////////////////////////////////////////////////////////////////////
  private void updateState(MandelData md)
  {
    image.updateState();
    mapping.updateState();
    if (picture!=null) {
      picture.updateState();
    }
  }

  private class ImageListener implements javax.swing.event.ChangeListener {

    public void stateChanged(javax.swing.event.ChangeEvent e)
    {
      updateState(((MandelImagePanel)e.getSource()).getMandelData());
    }
  }  

  /////////////////////////////////////////////////////////////////////////
  // image tab
  /////////////////////////////////////////////////////////////////////////

  private class ImageControlPanel extends ActionPanel {

    private MandelVariantModel vmodel;
    private JComboBox variants;
    private IntegerField colmapsize;
    private IntegerField scale;
    private MapperControl mapper;
    private JTextField    mappingtype;
    private boolean adjusting;

    public ImageControlPanel()
    {
      ScaleListener sl;
      int row=0;

      JLabel c=new JLabel("Variants");
      vmodel=new MandelVariantModel(getEnvironment().getImageDataScanner());
      addContent(c, GBC(0, row).setAnchor(GBC.WEST));
      variants=new JComboBox(vmodel);
      variants.setEditable(false);
      addContent(variants, GBC(1, row++).setFill(GBC.HORIZONTAL).setTopInset(5));
      vmodel.addChangeListener(new VariantListener());

      c=new JLabel("Current colormap size");
      addContent(c, GBC(0, row).setAnchor(GBC.WEST));
      colmapsize=new IntegerField();
      colmapsize.setColumns(10);
      colmapsize.setMinimumNumber(100);
      colmapsize.setEditable(false);
      colmapsize.addPropertyChangeListener("value", new ResizeListener());
      addContent(colmapsize, GBC(1, row++).setInsets(5));

      c=new JLabel("Scaling (%)");
      addContent(c, GBC(0, row).setAnchor(GBC.WEST).setInsets(0,0,5,0));
      scale=new IntegerField();
      scale.setColumns(10);
      scale.setEditable(false);
      scale.addPropertyChangeListener("value", sl=new ScaleListener());
      addContent(scale, GBC(1, row++).setInsets(0,5,5,5));
      getMandelWindowAccess().getMandelImagePane().getImagePane().addScaleEventListener(sl);

      c=new JLabel("Mapping type");
      addContent(c, GBC(0, row).setAnchor(GBC.WEST).setInsets(0,0,5,0));
      mappingtype=new JTextField();
      mappingtype.setColumns(10);
      mappingtype.setEditable(false);
      mappingtype.setHorizontalAlignment(JTextField.RIGHT);
      addContent(mappingtype, GBC(1, row++).setInsets(5));

      mapper=new MapperControl((GBCPanel)getContentPane(), 0, row++);
      //addC(mapper,GBC(0,1).setSpanW(2));
      mapper.addChangeListener(new RemapListener());
    }

    public void updateState()
    {
      boolean editable=false;
      adjusting=true;
      MandelImage image=getMandelWindowAccess().getMandelImage();
      if (image!=null) {
        editable=(image.getRasterData()!=null&&image.getMapper()!=null);
      }
      int s=getMandelWindowAccess().getColormapModel().getSize();
      if (debug) System.out.println("UPDATE image panel: size="+s);
      colmapsize.setValue(s);
      colmapsize.setEditable(editable);
      MandelImage img=getMandelWindowAccess().getMandelImage();
      if (img!=null) {
        //System.out.println("  mapper is "+img.getMapper());
        mapper.setMapper(img.getMapper());
        mapper.setEditable(editable);
        if (img.getMapping()!=null) {
          mappingtype.setText(img.getMapping().getType());
        }
        else {
          mappingtype.setText("none");
        }
      }
      else {
        mappingtype.setText("none");
      }
      scale.setValue(Math.round(getMandelWindowAccess().
                                 getMandelImagePane().
                                   getImagePane().getScale()*100));
      scale.setEditable(editable);
      if (vmodel.getName()==null ||
          !vmodel.getName().equals(getMandelWindowAccess().getMandelName())) {
        vmodel.refresh(getMandelWindowAccess().getQualifiedName());
      }
      adjusting=false;
    }

    private class ResizeListener implements PropertyChangeListener {

      public void propertyChange(PropertyChangeEvent evt)
      {
        if (!adjusting) {
          if (debug) System.out.println("RESIZE: "+colmapsize.getValue());
//          getFrame().getColormapModel().resizeC(
//                  colmapsize.getValue().intValue());
            getMandelWindowAccess().getMandelImage().resizeColormap(
                    getMandelWindowAccess().getColormapModel().getResizeMode(),
                    colmapsize.getValue().intValue());
        }
        else {
          if (debug) System.out.println("ignore resize update");
        }
      }
    }

    private class ScaleListener extends ScaleAdapter
                                implements PropertyChangeListener,
                                           ScaleEventListener {

      public void propertyChange(PropertyChangeEvent evt)
      {
        if (!adjusting) {
          if (debug) System.out.println("SCALE: "+evt.getNewValue());
          double s=(Integer)evt.getNewValue();
          if (s<=0) s=1;
          ((BufferedFrame)getMandelWindowAccess()).getImagePane().setScale(s/100);
        }
        else {
          if (debug) System.out.println("ignore scale update");
        }
      }

      @Override
      public void componentScaled(ScaleEvent e)
      {
        adjusting=true;
        scale.setValue(Math.round(e.getScaleX()*100));
        adjusting=false;
      }
    }

    private class RemapListener implements ChangeListener {

      public void stateChanged(ChangeEvent e)
      {
        if (!adjusting) {
          if (debug) System.out.println("REMAP");
          adjusting=true;
          getMandelWindowAccess().getMandelImage().setMapper(
                  getMandelWindowAccess().getColormapModel().getResizeMode(),
                  mapper.getMapperModel().getMapper());
          adjusting=false;
        }
        else {
          if (debug) System.out.println("ignore remap update");
        }
      }
    }

    private class VariantListener implements ChangeListener {

      public void stateChanged(ChangeEvent e)
      {
        if (!adjusting) {
          if (debug) System.out.println("VARIANT");
          adjusting=true;
          getMandelWindowAccess().getMandelImagePane().setImage(vmodel.getVariantName());
          adjusting=false;
        }
        else {
          if (debug) System.out.println("ignore remap update");
        }
      }
    }

  }

  /////////////////////////////////////////////////////////////////////////
  // image save tab
  /////////////////////////////////////////////////////////////////////////
  private class PicturePanel extends ActionPanel {

    private JComboBox formats;
    private JCheckBox decoration;
    private IntegerField width;
    private FilePanel imagefile;
    private FilePanel datafile;

    public PicturePanel()
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

      imagefile=new FilePanel("Image file", "", (JFrame)getOwner());
      addContent(imagefile, GBC(0, 3, 2, 1).setLayout(GBC.BOTH, GBC.NORTH).setWeight(
              100, 100).
              setInsets(10, 10, 10, 10));

      addButton("Save", new SaveImageAction(),"Save picture file");
      addButton("Clear", new ClearAction(),"Clear file name");
      addButton("Path", new PathAction(),"Generate picture path name");
      addButton("Name", new NameAction(),"Generate picture file name");
    }

    void updateState()
    {
      AbstractFile file=getMandelWindowAccess().getMandelData().getFile();
      if (file!=null) {
        if (!file.isFile()) decoration.setSelected(true);
        decoration.setEnabled(file.isFile());
      }
    }
    /////////////////////////////////////////////////////////////////////////
    private class SaveImageAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        BufferedImage tmp=null;
        Graphics g=null;
        if (Utils.isEmpty(imagefile.getFilename())) return;

        BufferedImage im=getMandelWindowAccess().getMandelImage().getImage();
        if (im!=null) {
          String name=imagefile.getFilename();
          String fmt=(String)formats.getSelectedItem();
          int w;
          int h;

          try {
            w=width.getValue().intValue();
          }
          catch (Exception ex) {
            w=0;
          }
          if (w!=0) {
            h=w*im.getHeight()/im.getWidth();
            tmp=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            (g=tmp.getGraphics()).drawImage(im, 0, 0, w, h, null);
            im=tmp;
          }
          if (decoration.isSelected()) {
            if (tmp==null) {
              tmp=new BufferedImage(im.getWidth(),im.getHeight(),BufferedImage.TYPE_INT_RGB);
              (g=tmp.getGraphics()).drawImage(im, 0, 0, null);
              im=tmp;
            }
            if (g==null) g=im.getGraphics();
            getMandelWindowAccess().getMandelImagePane().
                    paintDecoration(g,im.getWidth(),im.getHeight());
          }
          if (!name.endsWith("."+fmt)) {
            name+="."+fmt;
          }
          File f=new File(name);

          if (f.exists()) {
            if (!overwriteFileDialog(f)) return;
          }
          if (!f.getParentFile().exists()) {
            JOptionPane.showMessageDialog(getOwner(),
                                            "Cannot write image: directory not found", //text to display
                                            "Image IO", //title
                                            JOptionPane.WARNING_MESSAGE);
          }
          else {
            try {
              ImageIO.write(im, (String)formats.getSelectedItem(), f);
            }
            catch (Exception ex) {
              JOptionPane.showMessageDialog(getOwner(),
                                            "Cannot write image: "+ex.toString(), //text to display
                                            "Image IO", //title
                                            JOptionPane.WARNING_MESSAGE);
            }
          }
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class ClearAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        imagefile.setFilename("");
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class PathAction implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        ToolEnvironment env=getEnvironment();
        QualifiedMandelName mn=getMandelWindowAccess().getQualifiedName();
        String n=env.getProperty(Settings.BITMAP_SAVE_PATH);
        if (n!=null&&n.length()>0) n+="/"+mn.toString();
        else n=mn.toString();
        imagefile.setFilename(n);
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class NameAction extends PathAction {
      public void actionPerformed(ActionEvent e)
      {
        String old=imagefile.getFilename();
        if (Utils.isEmpty(old)) super.actionPerformed(e);
        else {
        ToolEnvironment env=getEnvironment();
          QualifiedMandelName mn=getMandelWindowAccess().getQualifiedName();
          String n=new File(old).getParent();
          if (n!=null&&n.length()>0) n+="/"+mn.toString();
          else n=mn.toString();
          imagefile.setFilename(n);
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////

  }

  /////////////////////////////////////////////////////////////////////////
  // data tab
  /////////////////////////////////////////////////////////////////////////
  private class DataPanel extends ActionPanel {
    private FilePanel datafile;

    public DataPanel()
    {
      ToolEnvironment env=getEnvironment();
      String n=env.getProperty(Settings.BITMAP_SAVE_PATH);
      
      datafile=new FilePanel("Data file", "", (JFrame)getOwner());
      if (n!=null) {
        n=n+"/mandeldata";
        datafile.setFilename(n);
      }
      addContent(datafile, GBC(0, 0, 2, 1).setLayout(GBC.BOTH, GBC.NORTH).setWeight(
              100, 100).
              setInsets(10, 10, 10, 10));
      addButton("Add", new AddAction(),"Add mandel coordinates to data file");
    }

    /////////////////////////////////////////////////////////////////////////
    private class AddAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(datafile.getFilename())) return;

        BufferedImage im=getMandelWindowAccess().getMandelImage().getImage();
        if (im!=null) {
          String name=datafile.getFilename();

          File f=new File(name);

          if (f.exists()) {
            if (!overwriteFileDialog(f)) return;
          }

          try {
            boolean h=!f.exists();
            PrintWriter pw=new PrintWriter(new BufferedWriter(
                                           new FileWriter(f,true)));
            try {
              MandelInfo mi=getMandelWindowAccess().getMandelData().getInfo();
              if (h) {
                pw.printf("%-20s  %30s %30s %20s %20s %7s %7s\n",
                        "Name", "XM", "YM", "DX", "DY", "MinIt", "MaxIt");
              }
              pw.printf("%-20s: %30s %30s %20s %20s %7s %7s\n",
                                           getMandelWindowAccess().getQualifiedName(),
                                           mi.getXM(),mi.getYM(),
                                           mi.getDX(),mi.getDY(),
                                           mi.getMinIt(),mi.getMaxIt());
            }
            finally {
              pw.close();
            }
          }
          catch (IOException ex) {
            JOptionPane.showMessageDialog(getOwner(),
                                          "Cannot write data: "+ex.toString(), //text to display
                                          "Data IO", //title
                                          JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    }
  }
  
  /////////////////////////////////////////////////////////////////////////
  // colormaps tab
  /////////////////////////////////////////////////////////////////////////
  private class ColormapsPanel extends TablePanel<ColormapListModel> {
    private FilePanel filename;
    private ColormapListModel model;

    public ColormapsPanel()
    {
      setModel(model=getEnvironment().getColormapListModel());
      addActionListener(new ActivateAction());
      JComponent c=new JLabel("s");
      FontMetrics m=c.getFontMetrics(c.getFont());

      TableColumn col=getTable().getColumnModel().getColumn(1);
      col.setPreferredWidth(m.charWidth('W')*10);
      col.setMaxWidth(m.charWidth('W')*10);

      col=getTable().getColumnModel().getColumn(2);
      col.setPreferredWidth(m.charWidth('W')*6);
      col.setMaxWidth(m.charWidth('W')*6);
      DefaultTableCellRenderer r=new DefaultTableCellRenderer();
      r.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
      col.setCellRenderer(r);
              
      filename=new FilePanel("Colormap File", "",
                             new FileNameExtensionFilter("Colormaps", "cm"),
                             getWindow());
      filename.setEnableChooser(!getEnvironment().isReadonly());
      addContent(filename, GBC(0, 2));

      addButton("Refresh", new RefreshAction());
      addButton("Memorize", new MemorizeAction());
      addButton("Remove", new RemoveAction());

      if (!getEnvironment().isReadonly())
        addButton("Save", new SaveAction());
      addButton("Load", new LoadAction());
      if (!getEnvironment().isReadonly())
        addButton("Delete", new DeleteAction());
      addButton("Show", new ShowAction());
    }

    @Override
    protected void setSelection(int index, int col)
    {
      try {
        AbstractFile file=getModel().getFile(index);
        if (debug) System.out.println("model index: "+index+": "+file);
        if (file==null) {
          filename.setFilename(getModel().getName(index).getName());
        }
        else {
          filename.setFilename(file.toString());
        }
      }
      catch (IOException ex) {
        if (debug) System.out.println("cannot get header: "+ex);
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class RefreshAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        getModel().refresh();
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class ActivateAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        int index=getSelectedIndex();
        if (index<0) return;

        try {
          Colormap cm=getModel().getColormap(index);
          if (cm!=null) {
            getMandelWindowAccess().getColormapModel().setColormap(new Colormap(cm));
          }
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(getOwner(),
                                        "Cannot get colormap: "+ex.toString(), //text to display
                                        "Colormap IO", //title
                                        JOptionPane.WARNING_MESSAGE);
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class SaveAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getFilename())) return;

        Colormap cm=getMandelWindowAccess().getMandelData().getColormap();
        if (cm!=null) {
          MandelData d=new MandelData(cm);

          File f=new File(filename.getFilename());
          if (f.exists()) {
            if (!overwriteFileDialog(f)) return;
          }

          try {
            d.write(f);
            MandelFolder.Util.add(f);
            getModel().refresh();
          }
          catch (IOException ex) {
            JOptionPane.showMessageDialog(getOwner(),
                                          "Cannot write colormap: "+ex.toString(), //text to display
                                          "Colormap IO", //title
                                          JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class LoadAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getFilename())) return;

        try {
          AbstractFile mf=getMandelWindowAccess().
                  getEnvironment().createMandelFile(filename.getFilename());
          MandelData md=new MandelData(mf);
          if (md.getColormap()!=null) {
            getMandelWindowAccess().getColormapModel().setColormap(md.getColormap());
          }
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(getOwner(),
                                        "Cannot read colormap: "+ex.toString(), //text to display
                                        "Colormap IO", //title
                                        JOptionPane.WARNING_MESSAGE);
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class DeleteAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getFilename())) return;

        File f=new File(filename.getFilename());
        if (!f.exists()) {
          mandelError("File does not exist.");
          return;
        }

        if (!deleteFileDialog(f)) return;
        try {
          MandelFolder.Util.delete(f);
          if (f.exists()) {
            mandelError("Cannot delete "+f+".");
            return;
          }
        }
        catch (IOException ex) {
          mandelError("Cannot delete "+f+": "+ex);
          return;
        }
        getModel().refresh();
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class MemorizeAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getFilename())) return;

        Colormap cm=getMandelWindowAccess().getMandelData().getColormap();
        if (cm!=null) {
          cm=new Colormap(cm);
          add(extract(filename), cm);
        }
      }

      private void add(String name, Colormap cm)
      {
        ColormapListModel m=getModel();
        int suf=0;
        String eff=name;
        while (!m.add(new ColormapName(eff),cm)) {
          eff=name+"."+(++suf);
        }
      }

      private String extract(FilePanel filename)
      {
        String n=new File(filename.getFilename()).getName();
        if (n.endsWith(MandelConstants.COLORMAP_SUFFIX)) {
          n=n.substring(0,n.length()-MandelConstants.COLORMAP_SUFFIX.length());
        }
        return n;
      }
    }

    private class RemoveAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        int index=getSelectedIndex();
        if (index<0) return;
        if (!getModel().remove(index)) {
          JOptionPane.showMessageDialog(getOwner(),
                                        "colormap cannot be removed from list", //text to display
                                        "Colormap List", //title
                                        JOptionPane.WARNING_MESSAGE);
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class ShowAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (Utils.isEmpty(filename.getFilename())) return;

        try {
          AbstractFile mf=getMandelWindowAccess().
                  getEnvironment().createMandelFile(filename.getFilename());
          MandelData md=new MandelData(mf);
          if (md.getColormap()!=null) {
            show(md.getColormap());
          }
        }
        catch (IOException ex) {
          if (getSelectedIndex()>=0) {
            try {
              show(getModel().getColormap(getSelectedIndex()));
              ex=null;
            }
            catch (IOException ex1) {
              ex=ex1;
            }
          }
          if (ex!=null) {
            JOptionPane.showMessageDialog(getOwner(),
                                          "Cannot read colormap: "+ex.toString(), //text to display
                                          "Colormap IO", //title
                                          JOptionPane.WARNING_MESSAGE);
          }
        }
      }

      private void show(Colormap cm)
      {
        ColormapDialog d=new ColormapDialog(getMandelWindowAccess(),
                                                filename.getFilename(),
                                                cm, false);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setVisible(true);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Mappings
  /////////////////////////////////////////////////////////////////////////
  
  private class MappingPanel extends ActionPanel {
    private JComboBox resizemode;
    private MapperPanel mapper;

    public MappingPanel()
    { 
      JLabel c;
      int row=0;

      c=new JLabel("Colormap resize mode");
      addContent(c, GBC(0, row).setAnchor(GBC.WEST));
      resizemode=new JComboBox(ResizeMode.values());
      resizemode.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
          super.getListCellRendererComponent(list, value, index, isSelected,
                                             cellHasFocus);
          setText(((ResizeMode)value).getName());
          return this;
        }

      });
      resizemode.setEditable(false);
      resizemode.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
          JComboBox cb=(JComboBox)e.getSource();
          if (debug) System.out.println("resize mode: "+cb.getSelectedItem());
          getMandelWindowAccess().getColormapModel().
                   setResizeMode(((ResizeMode)cb.getSelectedItem()));
        }
      });
      updateState();
      addContent(resizemode, GBC(1, row++).setFill(GBC.HORIZONTAL).setInsets(5));

      mapper=new MapperPanel(getMandelWindowAccess().getMapperModel());
      addContent(mapper,
           GBC(0, row++, 2,1).setLayout(GBC.BOTH, GBC.NORTH).setWeight(100, 100));
      addButton("Remap", new RemapAction());
    }

    public void updateState()
    {
      if (getMandelWindowAccess().getColormapModel().getResizeMode()!=null) {
        resizemode.setSelectedItem(getMandelWindowAccess().getColormapModel().getResizeMode());
      }
    }

    /////////////////////////////////////////////////////////////////////////
    private class RemapAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (debug) System.out.println("*** remap image");
        getMandelWindowAccess().getMandelImage().setMapper(
                getMandelWindowAccess().getColormapModel().getResizeMode(),
                mapper.getMapperModel().getMapper());
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Display tab
  /////////////////////////////////////////////////////////////////////////
  private class DisplayPanel extends ActionPanel {
    private JCheckBox decoration;
    private JCheckBox fullareanames;
    private JCheckBox tooltip;
    private JComboBox tooltipmode;
    private JComboBox selection;
    private JComboBox proportion;

    private JCheckBox automark_keyarea;
    private JCheckBox automark_fork;
    private JCheckBox automark_parent;

    DisplayPanel()
    { JLabel c;
      int row=0;
      MandelImagePanel mp=getMandelWindowAccess().getMandelImagePane();
      ////////////
      decoration=createCheckbox(row++,mp.getDecorationModel());
      ////////////
      fullareanames=createCheckbox(row++,mp.getFullAreaNamesModel());
      ////////////
      automark_keyarea=createCheckbox(row++,mp.getAutoMarkKeyAreaModel());
      ////////////
      automark_fork=createCheckbox(row++,mp.getAutoMarkForkModel());
      ////////////
      automark_parent=createCheckbox(row++,mp.getAutoMarkParentModel());
      ////////////
      tooltip=createCheckbox(row++,mp.getPixelToolTipModel());

      ////////////
      tooltipmode=createCombobox(row++,"Tooltip Mode",
                                  mp.getToolTipSelectionModel());
      ////////////
      if (!getMandelWindowAccess().getEnvironment().isReadonly()) {
        selection=createCombobox(row++,"Sub Area Selection",
                                  mp.getAreaSelectorModel());
        selection.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent e)
          {
            RectangleSelector s=getMandelWindowAccess().getMandelImagePane().
                                getImagePane().getRectangleSelector();
            if (debug) System.out.println("SELECTOR: "+s);
            if (s!=null) {
              proportion.setEnabled(s instanceof ProportionalRectangleSelector);
            }
          }
        });

        ////////////
        proportion=createCombobox(row++,"Selection Proportion",
                                  mp.getProportionSelectionModel());
      }
    }

    private JCheckBox createCheckbox(int row, BooleanAttribute attr)
    {
      return createCheckbox(row,attr.getLabel(),attr);
    }
    
    private JCheckBox createCheckbox(int row, String label, ButtonModel model)
    {
      JLabel c=new JLabel(label);
      addContent(c, GBC(0, row).setAnchor(GBC.WEST));

      JCheckBox b=new JCheckBox();
      b.setModel(model);
      c.setLabelFor(b);
      addContent(b, GBC(1, row).setAnchor(GBC.CENTER));
      return b;
    }

    private JComboBox createCombobox(int row, String label, ComboBoxModel model)
    {
      JLabel c=new JLabel(label);
      addContent(c, GBC(0, row).setAnchor(GBC.WEST).setRightInset(5));
      JComboBox b=new JComboBox();
      b.setModel(model);
      c.setLabelFor(b);
      addContent(b, GBC(1, row).setFill(GBC.HORIZONTAL));
      return b;
    }
  }
}
