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

import com.mandelsoft.mand.util.MandelColormapCache;
import com.mandelsoft.util.upd.UpdatableObject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import com.mandelsoft.io.FileAbstractFile;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.ImageListener;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.tool.MandelAreaCreationDialog.MandelAreaEvent;
import com.mandelsoft.mand.tool.MandelAreaCreationDialog.MandelAreaListener;
import com.mandelsoft.mand.tool.cm.ColormapDialog;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.tool.mapper.MapperModel;
import com.mandelsoft.mand.util.MandArith;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent;
import com.mandelsoft.swing.BufferedComponent.RectEvent;
import com.mandelsoft.swing.BufferedComponent.RectEventListener;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEvent;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEventListener;
import com.mandelsoft.swing.BufferedComponent.RectangleSelector;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;
import com.mandelsoft.swing.ProportionProvider;
import com.mandelsoft.swing.WindowControlAction;
import com.mandelsoft.util.ChangeEvent;

import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.image.MandelAreaImage;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerListener;
import com.mandelsoft.mand.scan.MandelScannerListenerAdapter;
import com.mandelsoft.mand.tool.slideshow.DefaultSlideShowModel;
import com.mandelsoft.mand.tool.slideshow.SlideShowDestination;
import com.mandelsoft.mand.tool.slideshow.SlideShowModel;
import com.mandelsoft.mand.tool.slideshow.SlideShowSourceAdapter;
import com.mandelsoft.mand.util.ArrayMandelList;
import com.mandelsoft.mand.util.CachedUpstreamColormapSource;
import com.mandelsoft.mand.util.DefaultMandelList;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MemoryMandelListFolderTree;
import com.mandelsoft.swing.AbstractListDataListener;
import com.mandelsoft.swing.BooleanAttribute;
import com.mandelsoft.swing.BufferedComponent.ProportionalRectangleSelector;
import com.mandelsoft.swing.BufferedComponent.RectPointEvent;
import com.mandelsoft.swing.BufferedComponent.RectangleActionEvent;
import com.mandelsoft.swing.BufferedComponent.VisibleRectFilter;
import com.mandelsoft.swing.BufferedFrame;
import com.mandelsoft.swing.ChangeListenerSupport;
import com.mandelsoft.swing.Corner;
import com.mandelsoft.swing.Dimensions;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.GBCPanel;
import com.mandelsoft.swing.OptionalAction;
import com.mandelsoft.swing.RectanglePoint;
import com.mandelsoft.swing.RenderedComponent;
import com.mandelsoft.swing.RenewStateListener;
import com.mandelsoft.swing.ScaleAdapter;
import com.mandelsoft.swing.ScaleEvent;
import com.mandelsoft.swing.UpdatableJMenu;
import com.mandelsoft.swing.UpdatableJMenuItem;
import com.mandelsoft.swing.UpdatableJPopupMenu;
import com.mandelsoft.swing.UpdatableAction;
import com.mandelsoft.util.upd.UpdateSource;
import com.mandelsoft.util.upd.UpdateSourceSupport;
import com.mandelsoft.util.Utils;
import com.mandelsoft.util.upd.BooleanUpdateCondition;
import com.mandelsoft.util.upd.UpdateContext;
import com.mandelsoft.util.upd.UpdateReason;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.image.ColorModel;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author Uwe Krueger
 */
public class MandelImagePanel  extends GBCPanel
                               implements MandelWindowAccess,
                                          UpdateSource {
  static public boolean debug=false;
  static public boolean debugHL=false;
  static public boolean iterPathActive=true;
  
  static private BasicStroke dashed=new BasicStroke(1, BasicStroke.CAP_ROUND,
                             BasicStroke.JOIN_ROUND, 10, new float[]{4, 4}, 0);
  static private BasicStroke favorite=new BasicStroke(2, BasicStroke.CAP_ROUND,
                             BasicStroke.JOIN_ROUND);
      
  /////////////////////////////////////////////////////////////////////////
  // Area Selection Model
  /////////////////////////////////////////////////////////////////////////

  private class AreaSelectionModel extends DefaultComboBoxModel {
    private class Selector {
      private String name;
      private BufferedComponent.RectangleSelector sel;

      Selector(String name, BufferedComponent.RectangleSelector sel)
      {
        this.name=name;
        this.sel=sel;
        sel.setStroke(dashed);
      }

      public String getName()
      {
        return name;
      }

      public RectangleSelector getSelector()
      {
        return sel;
      }

      @Override
      public String toString()
      {
        return getName();
      }
    }

    public AreaSelectionModel()
    {
      addElement(new Selector("Centered Proportional",
              new BufferedComponent.CenteredProportionalRectangleSelector(imagepropprov)));
      addElement(new Selector("Proportional",
              new BufferedComponent.ProportionalRectangleSelector(imagepropprov)));

      setSelectedItem(null);
      this.addListDataListener(new AbstractListDataListener() {
        @Override
        public void contentsChanged(ListDataEvent e)
        {
          if (debug) System.out.println("select SELECTOR "+getSelectedItem());
          if (!isReadonly()) {
            buffer.setRectangleSelector(getRectangleSelector());
            setProportionProvider(proportion.getProportionProvider());
          }
        }
      });
    }

    public RectangleSelector getRectangleSelector()
    {
      if (getSelectedItem()==null) return null;
      return ((Selector)getSelectedItem()).getSelector();
    }

    public void setProportionProvider(ProportionProvider d)
    {
      if (d!=null) {
        RectangleSelector s=getRectangleSelector();
        if (s instanceof ProportionalRectangleSelector) {
          ProportionalRectangleSelector p=(ProportionalRectangleSelector)s;
          p.setProportionProvider(d);
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Proportion Selection Model
  /////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////
  // Image Proportion

  private class AreaProportionProvider implements ProportionProvider {

    public double getProportion()
    {
      MandelInfo info=getMandelInfo();
      //double d=MandUtils.div(info.getDX(),info.getDY()).doubleValue();
      double d=MandUtils.div(info.getDY(), info.getDX()).doubleValue()*
              info.getRX()/info.getRY()*info.getRX()/info.getRY();
      if (debug) System.out.println("proportion is "+d);
      return d;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Creation Dialog Proportion

  private class CreationDialogProportionProvider extends AreaProportionProvider  {

    @Override
    public double getProportion()
    {
      double d=0;
      if (create!=null && create.isVisible()) {
        MandelInfo info=create.getInfo();
        if (info!=null) {
          d=MandUtils.getProportion(info.getRX(),info.getRY(), getMandelInfo());
        }
      }
      if (d==0.0) d=super.getProportion();
      return d;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // attrributes

  private AreaProportionProvider imagepropprov=new AreaProportionProvider();
  private AreaProportionProvider creatpropprov=new CreationDialogProportionProvider();

  /////////////////////////////////////////////////////////////////////////
  // Selection Model

  public class ProportionSelectionModel extends DefaultComboBoxModel {
    private class Proportion {
      private String name;
      private ProportionProvider provider;

      Proportion(String name, ProportionProvider provider)
      {
        this.name=name;
        this.provider=provider;
      }

      public String getName()
      {
        return name;
      }

      public ProportionProvider getProportionProvider()
      {
        return provider;
      }

      @Override
      public String toString()
      {
        return getName();
      }
    }

    public ProportionSelectionModel()
    {
      addElement(new Proportion("Image Proportion",
              imagepropprov));
      addElement(new Proportion("3:2",
              new ProportionProvider.Proportion(3.0/2.0)));
      addElement(new Proportion("10:7",
              new ProportionProvider.Proportion(10.0/7.0)));
      addElement(new Proportion("4:3",
              new ProportionProvider.Proportion(4.0/3.0)));
      addElement(new Proportion("1:1",
              new ProportionProvider.Proportion(1.0/1.0)));
      addElement(new Proportion("Screen",
              new ProportionProvider.Screen()));
      addElement(new Proportion("Creation Dialog Proportion",
              creatpropprov));

      setSelectedItem(null);
      this.addListDataListener(new AbstractListDataListener() {
        @Override
        public void contentsChanged(ListDataEvent e)
        {
          if (debug) System.out.println("select PROPORTION "+getSelectedItem());
          if (!isReadonly()) {
            selector.setProportionProvider(getProportionProvider());
          }
        }
      });
    }

    public ProportionProvider getProportionProvider()
    {
      if (getSelectedItem()==null) return null;
      return ((Proportion)getSelectedItem()).getProportionProvider();
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  // Tooltip handling
  ////////////////////////////////////////////////////////////////////////////

  /////////////////
  // Pixel Info
  public class PixelInfoToolTipHandler implements
          BufferedComponent.ToolTipHandler {

    public String getToolTipText(MouseEvent event)
    {
      MandelRaster r;
      ColorModel m;
      Point p=new Point((int)(event.getX()/filterscale),
                        (int)(event.getY()/filterscale));
      r=image.getRasterData();
      m=image.getImage().getColorModel();
      int rgb=image.getImage().getRGB(event.getX(), event.getY());
      int d=(r.getRaster())[(int)p.getY()][(int)p.getX()];
      return "("+event.getX()+","+event.getY()+"): "+d+
              " ("+m.getRed(rgb)+","+m.getGreen(rgb)+","+m.getBlue(rgb)+")";
    }
  }

  /////////////////
  // Sub Area Names
  public class SubAreaToolTipHandler implements BufferedComponent.ToolTipHandler {
    private MandelName last;
    private String text;

    public String getToolTipText(MouseEvent event)
    {
      String sub=findSubName(event.getPoint());
      if (sub!=null) {
        MandelName mn=QualifiedMandelName.create(sub).getMandelName();
        text=sub;
        if (mn!=null && !mn.equals(last)) {
          int ic=count(mn,getEnvironment().getImageDataScanner());
          if (getEnvironment().isReadonly()) {
            if (ic==1) text=sub+" ("+ic+" sub area)";
            else       text=sub+" ("+ic+" sub areas)";
          }
          else {
            int ir=count(mn,getEnvironment().getInfoScanner());
            if (ic==1) text=sub+" ("+ic+" sub area, "+ir+" pending)";
            else       text=sub+" ("+ic+" sub areas, "+ir+" pending)";
          }
        }
      }
      else {
        last=null;
        text=null;
      }
      return text;
    }
  }

  private int count(MandelName mn, MandelScanner s)
  {
    int cnt=0;
    for (MandelName n:s.getMandelNames()) {
      if (mn.isHigher(n)) cnt++;
    }
    return cnt;
  }
  
  /////////////////////////////////////////////////////////////////////////
  // Tool Tip Selection Model

  public class ToolTipSelectionModel extends DefaultComboBoxModel {
    
    //////////////////////////////
    // ToolTip Flavor
    private class ToolTipFlavor {
      private String name;
      private String id;
      private BufferedComponent.ToolTipHandler handler;

      ToolTipFlavor(String id, String name,
                    BufferedComponent.ToolTipHandler handler)
      {
        this.id=id;
        this.name=name;
        this.handler=handler;
      }

      public String getName()
      {
        return name;
      }

      public String getId()
      {
        return id;
      }

      public BufferedComponent.ToolTipHandler getToolTipHandler()
      {
        return handler;
      }

      @Override
      public String toString()
      {
        return getName();
      }
    }

    //////////////////////////////
    // ToolTip handler Wrapper
    private class MandelToolTipHandlerWrapper implements
            BufferedComponent.ToolTipHandler {

      private BufferedComponent.ToolTipHandler actual;

      public void setToolTipHandler(BufferedComponent.ToolTipHandler h)
      {
        actual=h;
      }

      public String getToolTipText(MouseEvent event)
      {
        return actual==null?null:actual.getToolTipText(event);
      }
    }

    //////////////////////////////
    private MandelToolTipHandlerWrapper wrapper;

    public ToolTipSelectionModel()
    { ToolTipFlavor def;

      wrapper = new MandelToolTipHandlerWrapper();
      this.addListDataListener(new AbstractListDataListener() {
        @Override
        public void contentsChanged(ListDataEvent e)
        {
          if (debug) System.out.println("select ToolTip "+getSelectedItem());
          wrapper.setToolTipHandler(getActualToolTipHandler());
        }
      });

      addElement(def=new ToolTipFlavor("PIXELINFO", "Pixel Info",
                                       new PixelInfoToolTipHandler()));
      addElement(new ToolTipFlavor("SUBAREANAME", "SubArea Name",
                                       new SubAreaToolTipHandler()));

//      setSelectedItem(def);
//      wrapper.setToolTipHandler(def.getToolTipHandler());
    }

    private BufferedComponent.ToolTipHandler getActualToolTipHandler()
    {
      if (getSelectedItem()==null) return null;
      return ((ToolTipFlavor)getSelectedItem()).getToolTipHandler();
    }

    public String getSelectedItemId()
    {
      if (getSelectedItem()==null) return null;
      return ((ToolTipFlavor)getSelectedItem()).getId();
    }

    public boolean setSelectedItemId(String id)
    {
      int size=getSize();
      for (int i=0; i<size; i++) {
        ToolTipFlavor item=(ToolTipFlavor)getElementAt(i);
        if (item.getId().equals(id)) {
          System.out.println("select tool tip mode "+id);
          setSelectedItem(item);
          return true;
        }
      }
      return false;
    }

    public BufferedComponent.ToolTipHandler getToolTipHandler()
    {
      return wrapper;
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Direct List Modification

  public class ListModifierMenu extends JMenu {
    private MandelListModel model;

    public ListModifierMenu(String name, MandelListModel model)
    {
      super(name);
      this.model=model;
      if (isValid()) {
        addItems();
      }
    }

    public void cleanupModel()
    {
    }

    public MandelListModel getMandelModel()
    {
      return model;
    }

    protected void addItems()
    {
      if ((model)!=null) {
        if (model.isModifiable()) {
          MandelListModel m=getMandelModel();
          JMenuItem it=new JMenuItem(new AddListEntryAction(m));
          add(it);
          it=new JMenuItem(new RemoveListEntryAction(m));
          add(it);
          if (m instanceof MandelListTableModel) {
            add(new OpenListShortcutAction(
              (MandelListTableModel)m,getText()));
          }
        }
      }
    }

    @Override
    public boolean isValid()
    {
      return model!=null && model.isModifiable();
    }
  }

  ///////////////////////////////////////////
  public class DynListModifierMenu extends ListModifierMenu {
    public DynListModifierMenu(String name, MandelListModel model)
    {
      super(name,model);
    }

    @Override
    protected void addItems()
    {
      if (!isValid()) return;
      super.addItems();
      add(new RemoveListShortcutAction(getMandelModel()));
    }
  }

  ///////////////////////////////////////////
  public class LoadableListModifier extends ListModifierMenu {
    private MandelListModelMenu load;

    public LoadableListModifier(String name, MandelListTableModel model)
    { super(name,model);
    }

    @Override
    public void cleanupModel()
    {
      if (load!=null) load.setMandelListModel(null);
    }

    @Override
    protected void addItems()
    {
      if (!isValid()) return;
      super.addItems();
      load=new MandelListModelMenu(MandelImagePanel.this,getMandelModel());
      add(load);
    }
  }

  ///////////////////////////////////////////
  public class EnvListener implements LinkListener {
    private void update(MandelName n)
    {
      if (getMandelName().equals(n)) updateLinks();
    }

    public void linkAdded(MandelName src, MandelName dst)
    {
      update(src);
      update(dst);
    }

    public void linkRemoved(MandelName src, MandelName dst)
    {
      update(src);
      update(dst);
    }
  }

  ///////////////////////////////////////////
  public class Mark extends ExplicitHighlight {
    private DefaultMandelListListModel previous;
     private DefaultMandelListListModel all;
    private MandelInfo info;
    private VisibleRect rect;
            
    public Mark(Environment env)
    {
      super("marked",true);
      previous=new DefaultMandelListListModel(new ArrayMandelList(),
                                              env.getAllScanner());
      previous.setModifiable(true);
      previous.setDuplicates(true);
      all=new DefaultMandelListListModel(new ArrayMandelList(),
                                         env.getAllScanner());
      all.setModifiable(true);
    }

    @Override
    public void setQualifiedName(QualifiedMandelName name)
    {
      QualifiedMandelName mn=name.getBaseName();
      int size=previous.getSize();
      super.setQualifiedName(name);
      all.add(name);
      if (size==0 || !previous.getElementAt(0).equals(mn)) {
        previous.add(0, mn);
        if (size>=20) {
          previous.remove(20);
        }
      }
      info=null;
      try {
        MandelHandle h=env.getAllScanner().getMandelInfo(getQualifiedName());
        if (h!=null) info=h.getInfo().getInfo();
      }
      catch (IOException ex) {
      }
      update();
    }
    
    @Override
    protected boolean _isActive()
    {
      if (super._isActive()) {
        return true;
      }
      if (info == null) {
        return false;
      }
      MandelInfo cur = MandelImagePanel.this.getMandelData().getInfo();
      BigDecimal x0 = cur.getXMin();
      BigDecimal y0 = cur.getYMin();
      BigDecimal x = info.getXMin();
      BigDecimal y = info.getYMin();
      return (x.compareTo(x0) >= 0
              && y.compareTo(y0) >= 0
              && x.add(info.getDX()).compareTo(x0.add(cur.getDX())) <= 0
              && y.add(info.getDY()).compareTo(y0.add(cur.getDY())) <= 0);

    }

    public void show()
    {
      if (getQualifiedName()!=null) {
        if (rect==null) {
          String label=marked.getMandelName().getName();
       
          rect=getImagePane().createRect(label,label);
          rect.setFixed(true);
        }
        rect.activate(!rect.isActive());
        rect.setVisible(rect.isActive());
        update();
      }
    }
    
    @Override
    public void clear()
    {
      super.clear();
      if (rect!=null) {
        rect.discard();
        rect=null;
      }
      info=null;
    }
    
    public void update()
    {
      if (rect!=null && isActive()) {
        if (info!=null) {
          rect.activate();
          updateRect(rect,info);
        }
        else {
          rect.setVisible(false);
        }
      }
    }
  }

  private class ConfiguredBooleanAttribute extends BooleanAttribute {

    public ConfiguredBooleanAttribute(JComponent component, String property,
                                      String label, boolean b)
    { super(component,property,label,b);
      configure();
    }

    public ConfiguredBooleanAttribute(JComponent component, String property,
                                      boolean b)
    { super(component,property,b);
      configure();
    }

    public ConfiguredBooleanAttribute(JComponent component, String property,
                                      String label)
    { super(component,property,label);
      configure();
    }

    public ConfiguredBooleanAttribute(JComponent component, String property)
    { super(component,property);
      configure();
    }

    private void configure()
    {
      setState(getEnvironment().getToolSwitch("config."+getPropertyName(),isSet()));
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // MandelImagePanel
  /////////////////////////////////////////////////////////////////////////

  private Buffer            buffer;
  private double            filterscale=1.0;
  private double            maxx=0;
  private ToolEnvironment   env;
  private boolean           showSubMode;
  
  private ChangeListenerSupport listeners=new ChangeListenerSupport();

  private ExplicitHighlight   lastname;
  private Mark                marked;
  private ExplicitHighlights  explicitHighlights;
  
  private QualifiedMandelName name;
  private MandelListMenu      subareas;

  private MandelListModelMenu gotolinks;
  private AbstractMandelListModelMenu showlinks;
  private AbstractMandelListModelMenu removelinks;
  private JMenu               linkhist;
  private JPopupMenu          linkhistpopup;
  private LinkListener        linkListener;

  private MandelListModel     keyareas;
  private MandelListModelMenu keyareasmenu;
  private MandelListModelMenu keyareaspopupmenu;

  private MandelImage         image;
  private MapperModel         mappermodel;
  private ColormapModel       colormapmodel;
  private Colormap            defcolormap;
  private Environment.FileInfo fileinfo;
  private History             history;

  private MandelSubAreaCreationDialog create;
  private ColormapDialog              colormapDialog;
  private ImageControl                imageDialog;
  private MandelListsDialog           listsDialog;
  private JuliaDialog                 juliaDialog;
  private IterationPathDialog         iterDialog;

  private ImageListener          imageListener;
  private List<ListModifierMenu> listModifiers;

  private Action parentAction;
  private Action areaUpAction;
  private Action forkAction;
  private Action showSubAction;
  private Action hideSubAction;
  private Action refreshSubAction;
  private Action showColormapAction;
  private Action showMetaAction;
  private Action variationAction;
  private Action linksGaleryAction;
  private Action keyareasGaleryAction;
  private Action zoomGaleryAction;
  private Action cloneAction;
  private Action gotoAction;
  private Action saveAction;
  private Action areaCMSaveAction;
  private Action areaCMDeleteAction;
  private Action areaCMAction;
  private Action showListsControlAction;
  private Action showJuliaAction;
  private Action showIterationPathAction;

  private Action areaDownAction;
 
  private int highlight_mode=H_FAVORITES;

  private static final int H_NONE=0;
  private static final int H_FAVORITES=1;
  private static final int H_UNSEEN=2;
  private static final int H_LAST=3;

  private JScrollPane scrollpane;
  private MandelAreaImage initialImage;
  private boolean partial;
  private SlideShowModel slideshowmodel;
  private AreaSelectionModel selector;
  private ProportionSelectionModel proportion;
  private ToolTipSelectionModel tooltip;
  private BooleanAttribute parentcolormap;
  private BooleanAttribute fullareanames;
  private BooleanAttribute automark_parent;
  private BooleanAttribute automark_keyarea;
  private BooleanAttribute automark_fork;
  private BooleanAttribute autoshow_info;
  private BooleanAttribute show_copyright;
  private BooleanAttribute show_title;
  private BooleanAttribute enable_refcoords;


  private MandelWindowsMenuListener mandelwindowslistener;
  
  private ExplicitHighlight   highlighted;
  private Timer               highlight_timer;
  private boolean             highlight_active;

  private Rotator    rotator;
  private MandelScannerListener msl;

  public MandelImagePanel(ToolEnvironment env,
                          MandelAreaImage img) throws IOException
  {
    this(env,img,00);
  }

  public MandelImagePanel(ToolEnvironment env,
                          MandelAreaImage img, int maxx) throws IOException
  {
    this.env=env;
    this.maxx=maxx;
    this.subareas=new MandelListMenu(this);
    this.subareas.setSorted(true);
    this.subareas.setUseShortnames(true);

    this.gotolinks=new MandelListModelMenu("Load Image",this,null);
    this.gotolinks.setSorted(true);
    
    this.showlinks=new AbstractMandelListModelMenu("Show Image",this,null) {
      @Override
      protected void selectArea(QualifiedMandelName name)
      {
        try {
          getEnvironment().createMandelImageFrame(name);
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(getWindow(),
                                    "Cannot load image: "+name,
                                    "Mandel IO", JOptionPane.WARNING_MESSAGE);
        }
      }
    };
    this.showlinks.setSorted(true);
    
    this.linkhist=new JMenu("Parent Links");
    this.linkhistpopup=new JPopupMenu("Parent Links");
    this.linkListener=new EnvListener();

    this.removelinks=new AbstractMandelListModelMenu("Remove Link",this,null){
      @Override
      protected void selectArea(QualifiedMandelName name)
      {
        getEnvironment().removeLink(getMandelName(), name.getMandelName());
      }
    };
    this.removelinks.setSorted(true);
    
    this.keyareas=new DefaultMandelListListModel(new DefaultMandelList(),
                                                 env.getAllScanner());
    this.keyareas.setModifiable(true);
    this.highlighted=new ExplicitHighlight("highlighted", true);
    this.lastname=new ExplicitHighlight("lastshown");
    this.marked=new Mark(env);
    this.explicitHighlights=new ExplicitHighlights();
    this.explicitHighlights.add(highlighted);
    this.explicitHighlights.add(marked);
    this.explicitHighlights.add(lastname);

    this.slideshowmodel=new DefaultSlideShowModel(new SlideShowDestination() {

      public QualifiedMandelName getCurrentQualifiedMandelName()
      {
        return MandelImagePanel.this.getQualifiedMandelName();
      }

      public boolean show(QualifiedMandelName name)
      {
        return MandelImagePanel.this.setImage(name);
      }

      public boolean show(MandelName name)
      {
        return MandelImagePanel.this.setImage(name);
      }

      public void setHighLight(QualifiedMandelName name)
      {
        MandelImagePanel.this.setHighLight(name);
      }

      public Window getWindow()
      {
        return MandelImagePanel.this.getWindow();
      }
    });

    this.highlight_timer=new Timer(5000,new HighlightHandler());
    this.selector=new AreaSelectionModel();
    this.proportion=new ProportionSelectionModel();
    this.tooltip=new ToolTipSelectionModel();
    this.parentcolormap=new ConfiguredBooleanAttribute(this,"upstream-colormap",
                           "Use upstream colormap as default",true);
    this.autoshow_info=new ConfiguredBooleanAttribute(this,"show-info-popup",
                           "Show info popup at image switch");
    this.automark_fork=new ConfiguredBooleanAttribute(this,"automark-fork",
                           "Auto Mark for Fork Navigation");
    this.automark_keyarea=new ConfiguredBooleanAttribute(this,"automark-keyarea",
                           "Auto Mark for Key Area Navigation",true);
    this.automark_parent=new ConfiguredBooleanAttribute(this,"automark-parent",
                           "Auto Mark for Parent Navigation");
    this.show_copyright=new ConfiguredBooleanAttribute(this,"show-copyright",
                           "Show Copyright");
    this.show_copyright.setState(env.getCopyright()!=null);
    this.show_title=new ConfiguredBooleanAttribute(this,"show-title",
                           "Show Title");
    this.show_title.setState(env.getCopyright()!=null);
    this.enable_refcoords=new ConfiguredBooleanAttribute(this,"enable-refcoords",
                           "Enable Reference Coordinates");
    this.fullareanames=new ConfiguredBooleanAttribute(this,"full-areanames",
                           "Show Full Sub Area Names") {
      @Override
      protected void afterStateChange()
      {
        System.out.println("full "+isSet());
        Iterator<VisibleRect> i=buffer.getRects();
        while(i.hasNext()) {
          VisibleRect r=i.next();
          String l=MandelImagePanel.this.getLabel(MandelName.create(r.getName()));
          if (l!=null) r.setLabel(l);
        }
      }
    };

    this.buffer=new Buffer(filter(img.getImage()));
    this.buffer.setShowDecoration(show_copyright.isSet() || show_title.isSet());
    this.setImageDecoration(img.getInfo()); 
    //this.buffer.setBorder(BorderFactory.createLineBorder(Color.blue,5));
    this.buffer.setBorder(null);
    this.buffer.getDecorationModel().setLabel("Show Subarea Names");
    this.buffer.getPixelToolTipModel().setLabel("Pixel Tooltip");
    this.scrollpane=new JScrollPane(buffer);
    //scrollpane.setBorder(BorderFactory.createLineBorder(Color.red,5));
    scrollpane.setBorder(null);
    scrollpane.getViewport().addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e)
      {
        //System.out.println("RS: "+e);

        JViewport vp=scrollpane.getViewport();
        if (vp!=null) {
          if (debug) System.out.println("PREF:"+buffer.getPreferredSize());
          if (debug) System.out.println("VP: "+vp.getViewPosition());
          if (debug) System.out.println("VP: "+vp.getExtentSize());
          partial=!buffer.getPreferredSize().equals(vp.getExtentSize());
        }
        else partial=false;
        buffer.setLimitWindowSize(true);
      }
    });
    add(this.scrollpane,GBC(0,0,GBC.BOTH));
    //this.setBorder(BorderFactory.createLineBorder(Color.black,5));
    this.setBorder(null);
    this.initialImage=img;
    this.buffer.setLimitWindowSize(true);    
  }

  private class Buffer extends BufferedComponent {
    private Decoration decoration;

    public Buffer(BufferedImage image)
    {
      super(image);
      decoration=new Decoration();
      decoration.setColorHandler(getColorHandler());
    }

    public void setDecoration(String s)
    {
      decoration.setDecoration(s);
    }

    public Decoration getDecoration()
    {
      return decoration;
    }
    
    @Override
    public void setShowDecoration(boolean showDecoration)
    {
      decoration.setShowDecoration(showDecoration);
    }

    @Override
    protected void paintChildren(Graphics g)
    {
      super.paintChildren(g);
      if (decoration.showDecoration())
        decoration.paintDecoration(g,getWidth(),getHeight());
    }
  }

  
  //////////////////////////////////////////////////////////////////////
  
  @Override
  protected void panelBound()
  {
    MandelAreaImage img=initialImage;

    initialImage=null;

    super.panelBound();
    MandelWindowAccess frame=this;
    Colormap cm=env.getDefaultColormap();
    this.history=new History(env.getImageDataScanner());
    this.mappermodel=new MapperModel();
    this.colormapmodel=new ColormapModel(cm);
    this.colormapmodel.addChangeListener(new ColModelChangeListener());

    rotator=new Rotator(colormapmodel);
    imageListener=new MandelFrameImageListner();

    create=new MandelSubAreaCreationDialog(frame,"Mandel Sub Area Creation");
    colormapDialog=new ColormapDialog(frame,"Main Colormap",this.colormapmodel);
    imageDialog=new ImageControl(frame);
    
    listsDialog=new MandelListsDialog(frame);
    listsDialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e)
      {
        showListsControlAction.setEnabled(true);
      }
    });
    juliaDialog=new JuliaDialog(frame,300,300);
    juliaDialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e)
      {
        showJuliaAction.setEnabled(true);
      }
    });
    iterDialog=new IterationPathDialog(frame,300,300);
    iterDialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e)
      {
        showIterationPathAction.setEnabled(true);
      }
    });
    
    SelectAreaListener arealistener=new SelectAreaListener();
    buffer.setSelectInvisible(true);
    buffer.addRectEventListener(arealistener);
    buffer.addRectModifiedEventListener(arealistener);
    if (!isReadonly()) {
      selector.setSelectedItem(selector.getElementAt(0));
      proportion.setSelectedItem(proportion.getElementAt(0));
    }

    buffer.addScaleEventListener(new ScaleAdapter() {
      @Override
      public void componentScaled(ScaleEvent e)
      {
        setTitle();
        if (debug) System.out.println("buf: "+getImagePane().getPreferredSize());
        if (debug) System.out.println("VP: "+getScrollPane().getViewport().getExtentSize());
        //if (!isPartial()) setInitialSize();
      }
    });

    create.addCreationListener(new SubAreaListener());
    parentAction=new ParentAction();
    areaUpAction=new AreaUpAction();
    forkAction=new ForkAction();
    showSubAction=new ShowSubAction();
    hideSubAction=new HideSubAction();
    refreshSubAction=new RefreshSubAction();
    showColormapAction=new ShowColormapAction(colormapDialog);
    if (!isReadonly()) variationAction=new VariationAction();
    showMetaAction=new ShowMetaAction();
    linksGaleryAction=new LinksGaleryAction();
    keyareasGaleryAction=new KeyAreasGaleryAction();
    zoomGaleryAction=new ZoomGaleryAction();
    cloneAction=new CloneAction();
    gotoAction=new GotoAction();
    if (!isReadonly()) {
      saveAction=new SaveAction();
      areaCMSaveAction=new AreaCMSaveAction();
      areaCMDeleteAction=new AreaCMDeleteAction();
    }
    areaCMAction=new AreaCMAction();

    showListsControlAction=new ShowListsControlAction();
    showJuliaAction=new ShowJuliaAction();
    showIterationPathAction=new ShowIterationPathAction();

    areaDownAction=new AreaDownAction();

    // pane.addRectPointEventListener(new CornerListener());
    buffer.addActionListener(parentAction, Corner.BOTTOM_LEFT);
    buffer.addActionListener(showSubAction, Corner.BOTTOM_RIGHT);
    buffer.addActionListener(hideSubAction, Corner.BOTTOM_RIGHT);
    buffer.addActionListener(showColormapAction, Corner.TOP_RIGHT);
    buffer.addActionListener(showMetaAction, Corner.TOP_LEFT);
    buffer.addActionListener(areaDownAction, RectanglePoint.MIDDLE_BOTTOM);
    buffer.addActionListener(new LinksAction(), RectanglePoint.MIDDLE_LEFT);
    buffer.addActionListener(new KeyAreasAction(), RectanglePoint.MIDDLE_RIGHT);
    buffer.getContentPane().addMouseMotionListener(new PixelListener());
    buffer.setInheritsPopupMenu(true);
    scrollpane.setInheritsPopupMenu(true);
    hideSubAction.setEnabled(false);
    refreshSubAction.setEnabled(false);
    setupMenu();

    getWindow().addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosed(WindowEvent e)
      {
        if (debug) System.out.println("FRAME closed");
        clear();
      }
    });

    buffer.setScaleMode(true);
    // finally set the image
    setImageData(img, false);

    msl=new MandelScannerListenerAdapter() {
      @Override
      public void addMandelFile(MandelScanner s, MandelHandle h)
      {
        handle(h);
      }

      @Override
      public void removeMandelFile(MandelScanner s, MandelHandle h)
      {
        handle(h);
      }

      @Override
      public void scannerChanged(MandelScanner s)
      {
        handle(null);
      }

      private void handle(MandelHandle h)
      {
        boolean doit=false;
        if (h==null) doit=true;
        else {
          QualifiedMandelName qn=h.getName();
          if (qn!=null && qn.getMandelName().getParentName()!=null) {
            if (qn.getMandelName().getParentName().equals(getMandelName())) {
              doit=true;
            }
          }
        }
        if (doit) {
          System.out.println("*** do update sub rects");
          _updateSubRects(false);
        }
      }
    };
    getEnvironment().getAllScanner().addMandelScannerListener(msl);
    getEnvironment().addLinkListener(linkListener);
  }

  @Override
  protected void panelUnbound()
  {
    super.panelUnbound();
    for (ListModifierMenu m:listModifiers) m.cleanupModel();
    if (msl!=null) {
      getEnvironment().getAllScanner().removeMandelScannerListener(msl);
    }
    getEnvironment().removeLinkListener(linkListener);
    getEnvironment().getMandelWindowsModel().removeListDataListener(
      mandelwindowslistener);
    gotolinks.setMandelListModel(null);
    showlinks.setMandelListModel(null);
    removelinks.setMandelListModel(null);
    cancel();
  }

  //////////////////////////////////////////////////////////////////////

  private UpdateSourceSupport uss=new UpdateSourceSupport();

  public void addUpdatableObject(UpdatableObject o)
  {
    uss.addUpdatableObject(o);
  }

  public void removeUpdatableObject(UpdatableObject o)
  {
    uss.removeUpdatableObject(o);
  }

  protected void updateObjects()
  {
    UpdateContext ctx=new UpdateContext(this);
    uss.updateObjects(ctx);
  }

  protected void updateObjects(String prop, Object value)
  {
    UpdateContext ctx=new UpdateContext(this,
                      new UpdateReason.Factory(this).setProperty(prop, value));
    uss.updateObjects(ctx);
  }

  //////////////////////////////////////////////////////////////////////
  public void cancel()
  {
    slideshowmodel.cancel();
    if (rotator!=null) rotator.cancel();
  }

  public String getDecorationText(MandelInfo info)
  {
    String deco=null;
    
    if (show_title.isSet()) {
      deco=info.getProperty(MandelInfo.ATTR_TITLE);
    }
    if (show_copyright.isSet()) {
      String copyright=getCopyright(info);
      if (!Utils.isEmpty(copyright)) {
        if (Utils.isEmpty(deco)) deco=copyright;
        else deco=deco+" "+copyright;
      }
    }
    return deco;
  }
          
  public String getCopyright(MandelInfo info)
  {
    String c = this.env.getCopyright(info);
    if (c == null || c.isEmpty()) {
      c = env.getCopyright(this.env.getProperty(Settings.USER),
              this.env.getProperty(Settings.SITE));
    }
    if (c != null && !c.isEmpty()) {
      if (info.getCreationTime() != 0) {
        Date date = new Date(info.getCreationTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        c += ", " + year;
      }
      return c;
    }
    return null;
  }
      
  public void setImageDecoration(String s)
  {
    buffer.setDecoration(s);
  }

  public void setImageDecoration(MandelInfo info)
  {
    String c = getDecorationText(info);
    if (!Utils.isEmpty(c)) {
      this.buffer.setDecoration(c);
      this.buffer.setShowDecoration(true);
    }
    else {
      this.buffer.setDecoration("");
      this.buffer.setShowDecoration(false);
    }
  }

  public BooleanAttribute getSelectInvisibleModel()
  {
    return buffer.getSelectInvisibleModel();
  }

  public BooleanAttribute getDecorationModel()
  {
    return buffer.getDecorationModel();
  }

  public BooleanAttribute getPixelToolTipModel()
  {
    return buffer.getPixelToolTipModel();
  }

  public BooleanAttribute getParentColormapModel()
  {
    return parentcolormap;
  }
  
  public BooleanAttribute getFullAreaNamesModel()
  {
    return fullareanames;
  }

  public BooleanAttribute getAutoShowInfoModel()
  {
    return autoshow_info;
  }

  public BooleanAttribute getAutoMarkForkModel()
  {
    return automark_fork;
  }

  public BooleanAttribute getAutoMarkKeyAreaModel()
  {
    return automark_keyarea;
  }

  public BooleanAttribute getAutoMarkParentModel()
  {
    return automark_parent;
  }
  
  public BooleanAttribute getShowCopyrightModel()
  {
    return show_copyright;
  }
  
  public BooleanAttribute getShowTitleModel()
  {
    return show_title;
  }
  
  
  public BooleanAttribute getEnableRefCoordsModel()
  {
    return enable_refcoords;
  }


  public ComboBoxModel getAreaSelectorModel()
  {
    return selector;
  }

  public ProportionSelectionModel getProportionSelectionModel()
  {
    return proportion;
  }

  public ToolTipSelectionModel getToolTipSelectionModel()
  {
    return tooltip;
  }

  public SlideShowModel getSlideShowModel()
  {
    return slideshowmodel;
  }

  public BufferedComponent getImagePane()
  {
    return buffer;
  }

  public JScrollPane getScrollPane()
  {
    return scrollpane;
  }

  public boolean isPartial()
  {
    return partial;
  }

  public void setInitialSize()
  {
    Dimension id=buffer.getPreferredSize();
    Dimension sd=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension d=new Dimension(id);
    Insets insets=getInsets();
    if (debug) System.out.println("bufsize:"+id);
    if (debug) System.out.println("scrsize:"+sd);
    Dimensions.mod.add(d,insets.left+insets.right,insets.top+insets.bottom);
    Dimensions.mod.sub(sd,BufferedFrame.FRAME_INSETS,BufferedFrame.FRAME_INSETS+20);
    Dimensions.mod.limit(d,sd);
    if (debug) System.out.println("ressize:"+d);
    setPreferredSize(d);
  }

  public MandelImagePanel getMandelImagePane()
  {
    return this;
  }

  public void paintDecoration(Graphics g, int w, int h)
  {
    getDecoration().paintDecoration(g, w, h);
  }

  public Decoration getDecoration()
  {
    return buffer.getDecoration();
  }

  public Window getMandelWindow()
  {
    return getWindow();
  }

  private class ColModelChangeListener implements ChangeListener {
    public void stateChanged(javax.swing.event.ChangeEvent e)
    {
      boolean mod=getMandelData().isModified();
      if (colormapmodel.getColormap()!=image.getColormap()) {
        image.setColormap(colormapmodel.getResizeMode(),
                          colormapmodel.getColormap());
      }
      if (debug) System.out.println("setting default colormap to actual colormap");
      defcolormap=colormapmodel.getColormap();
      if (!mod) { // so far there is no content related update handler
        //image.getMandelData().setModified(true);
        updateObjects("colormap",colormapmodel.getColormap());
      }
    }
  }

  private class MandelFrameImageListner implements ImageListener {
    public void stateToBeChanged(ChangeEvent e)
    {
      if (debug) System.out.println("image to be changed...");
      getImagePane().hideAllRects();
    }

    public void stateChanged(ChangeEvent e)
    {
      if (debug) System.out.println("image changed...");
      if (image.getColormap()!=null) {
        if (debug) System.out.println("  setting image and default colormap");
        colormapmodel.setColormap(defcolormap=image.getColormap());
      }
      colormapmodel.setModifiable(image.getRasterData()!=null);

      if (image.getImage()!=buffer.getImage()) {
        if (debug) System.out.println("  set changed image to buffer");
        BufferedImage tmp=filter(image.getImage());
        BufferedImage cur=buffer.getImage();
        if (cur.getWidth()==tmp.getWidth() && cur.getHeight()==tmp.getHeight()) {
          cur.getGraphics().drawImage(tmp, 0, 0, null);
        }
        else {
          buffer.setImage(tmp);
          MandelImagePanel.this._updateSubRects(false,getMandelName(),subAreasShown());
        }
      }
      
      if (subAreasShown()) {
        if (debug) System.out.println("  show areas");
        getImagePane().showAllRects();
      }
      
      fireChangeEvent();
      getImagePane().repaint();
    }
  }

  private void clear()
  {
    if (this.image!=null) {
      this.image.removeImageListener(imageListener);
      this.image=null;
    }
    if (browserModel!=null) {
      browserModel.clear();
      browserModel=null;
    }
    cancel();
  }

  public boolean isReadonly()
  { return getEnvironment().isReadonly();
  }

  public int getMaxFrame()
  {
    return (int)maxx;
  }

  private JMenu listmenu=new JMenu("List Shortcuts");

  private void addListModifierMenu(ListModifierMenu m)
  {
    if (m.isValid()) {
      listModifiers.add(m);
      listmenu.add(m);
    }
  }

  public void addListShortcut(String name, MandelListModel model)
  {
    addListModifierMenu(new DynListModifierMenu(name,model));
  }

  public boolean hasListShortcut(MandelListModel model)
  {
    return hasListShortcut(model.getList());
  }
  
  public boolean hasListShortcut(MandelList l)
  {
    for (ListModifierMenu m:listModifiers) {
      if (m.getMandelModel().getList()==l) {
        return true;
      }
    }
    return false;
  }

  public void removeListShortcut(MandelListModel model)
  {
    removeListShortcut(model.getList());
  }

  public void removeListShortcut(MandelList l)
  {
    for (ListModifierMenu m:listModifiers) {
      if (m.getMandelModel().getList()==l) {
        listModifiers.remove(m);
        listmenu.remove(m);
        break; // avoid concurrent modification exception
      }
    }
  }

  //
  // Provide menu for foreign mandel window frames
  //
  private class MandelWindowsMenuListener implements MenuListener,
                                                     ActionListener,
                                                     ListDataListener {
    private JMenu menu;
    
    public MandelWindowsMenuListener(JMenu menu)
    {
      this.menu=menu;
      menu.setEnabled(getEnvironment().getMandelWindowsModel().getSize()>1);
    }
    
    protected Action createAction(final QualifiedMandelName n)
    {
      Action a=new AbstractAction(n.toString()) {
        public void actionPerformed(ActionEvent e)
        {
          getEnvironment().addLink(getMandelName(), n.getMandelName());
        }
      };
      MandelListModel m=getEnvironment().getLinkModel(getMandelName());
      // avoid self links and duplicates
      a.setEnabled((m==null || m.getList().get(n.getMandelName())==null) &&
                   (!n.getMandelName().equals(getMandelName())));
      return a;
    }
    
    public void menuSelected(MenuEvent e)
    {
      menu.removeAll();
      ListModel m=getEnvironment().getMandelWindowsModel();
      for (int i=0; i<m.getSize(); i++) {
        MandelImageFrame f=(MandelImageFrame) m.getElementAt(i);
        if (f!=MandelImagePanel.this.getMandelWindow()) {
          menu.add(createAction(f.getQualifiedName()));
        }
      }
    }

    public void menuDeselected(MenuEvent e)
    {
      menuCanceled(e);
    }

    public void menuCanceled(MenuEvent e)
    {
      menu.removeAll();
    }

    private void windowModelChanged(ListDataEvent e)
    {
      menu.setEnabled(((ListModel)e.getSource()).getSize()>1);
    }
    
    public void intervalAdded(ListDataEvent e)
    {
      windowModelChanged(e);
    }

    public void intervalRemoved(ListDataEvent e)
    {
      windowModelChanged(e);
    }

    public void contentsChanged(ListDataEvent e)
    {
      windowModelChanged(e);
    }

    public void actionPerformed(ActionEvent e)
    {
      System.out.println("action on windows menu");
    }
  }
  
  
  private void setupMenu()
  {    
//    JMenu favorites=null;
//    JMenu todos=null;
//    JMenu memory=null;
    JMenuItem it;
    UpdatableJPopupMenu menu=new UpdatableJPopupMenu();
    addUpdatableObject(menu);
    menu.setBorder(new BevelBorder(BevelBorder.RAISED));

    listModifiers=new ArrayList<ListModifierMenu>();
    addListModifierMenu(new ListModifierMenu("Favorites",getEnvironment().getFavoritesModel()));
    addListModifierMenu(new ListModifierMenu("Todos",getEnvironment().getTodosModel()));
    addListModifierMenu(new ListModifierMenu("Key Areas",getEnvironment().getAreasModel()));
    addListModifierMenu(new LoadableListModifier("Memory",getEnvironment().getMemoryModel()));

    String shortcuts=env.getProperty(Settings.LIST_SHORTCUTS);
    System.out.println("default shortcuts: "+shortcuts);
    if (!Utils.isEmpty(shortcuts)) {
      StringTokenizer t=new StringTokenizer(shortcuts,";");
      String p;
      while ((t.hasMoreTokens())) {
        p=t.nextToken();
        MandelListModel m=env.getMandelListModel(p);
        if (m!=null) {
          int ix=p.lastIndexOf('/');
          if (ix>=0) p=p.substring(ix+1);
          addListShortcut(p,m);
        }
      }
    }
    
    ///////////////////////////////////////////////////
    JMenu mark=new UpdatableJMenu("Mark");
    
    addMarkAction(mark,new GotoMarkAction());
    mark.add(new JMenuItem(new SetMarkAction()));
    addMarkAction(mark,new ShowMarkAction());
    addMarkAction(mark,new CloneAction("Show Image") {
      @Override
      protected QualifiedMandelName getQualifiedMandelName()
      {
        return marked.getQualifiedName();
      }
    });
    addMarkAction(mark,new CommonAncestorAction());
    addMarkAction(mark,areaDownAction);
    addMarkAction(mark,new KeyAreaDownAction());
    addMarkAction(mark,new ForkAreaDownAction());
    addMarkAction(mark,new MemorizeMarkAction());
    if (!isReadonly()) {
      addMarkAction(mark,new LinkFromMarkAction());
      addMarkAction(mark,new UnlinkFromMarkAction());
    }
    addMarkAction(mark,new SwapMarkAction());
    addMarkAction(mark,new ClearMarkAction());
    mark.add(new MandelListModelMenu("Previous",this,marked.previous));
    
    ///////////////////////////////////////////////////
    JMenu highlight=new JMenu("Sub area highlight");
    ButtonGroup group = new ButtonGroup();
    it = new JRadioButtonMenuItem(new NoHighlightAction());
    // it.setMnemonic(KeyEvent.VK_R);
    group.add(it);
    highlight.add(it);

    it = new JRadioButtonMenuItem(new FavoritesHighlightAction());
    it.setSelected(true);
    //it.setMnemonic(KeyEvent.VK_O);
    group.add(it);
    highlight.add(it);

    if (!isReadonly()) {
      it=new JRadioButtonMenuItem(new UnseenHighlightAction());
      //it.setMnemonic(KeyEvent.VK_O);
      group.add(it);
      highlight.add(it);
    }

    it = new JRadioButtonMenuItem(new LastHighlightAction());
    //it.setMnemonic(KeyEvent.VK_O);
    group.add(it);
    highlight.add(it);

    ///////////////////////////////////////////////////

    JMenu subarea=new UpdatableJMenu("Sub areas");
    
    it=new JMenuItem(showSubAction);
    subarea.add(it);
    it=new JMenuItem(refreshSubAction);
    subarea.add(it);
    it=new JMenuItem(hideSubAction);
    subarea.add(it);
    subarea.add(subareas);

    //////////////////////

    menu.add(listmenu);
    menu.add(mark);

    menu.add(highlight);
    menu.add(subarea);
    
    menu.addSeparator();
    it=new JMenuItem(showColormapAction);
    menu.add(it);
    menu.add(rotator.createMenu());
    it=new JMenuItem(new WindowControlAction(getWindow(),"Image Control",
                                                  imageDialog));
    menu.add(it);

    menu.addSeparator();
    it=new JMenuItem(showMetaAction);
    menu.add(it);

    JMenu galerymenu=new JMenu("Galeries");
    galerymenu.add(linksGaleryAction);
    galerymenu.add(keyareasGaleryAction);
    galerymenu.add(zoomGaleryAction);

    ///////////////////////////////////////////////////
    JMenu windowLink=new JMenu("Link To Other Display Area");
    mandelwindowslistener=new MandelWindowsMenuListener(windowLink);
    windowLink.addActionListener(mandelwindowslistener);
    windowLink.addMenuListener(mandelwindowslistener);
    getEnvironment().getMandelWindowsModel().addListDataListener(mandelwindowslistener);
    JMenu linkmenu=new JMenu("Links");
    linkmenu.add(gotolinks);
    linkmenu.add(showlinks);
    linkmenu.add(windowLink);
    linkmenu.add(removelinks);
    linkmenu.add(linkhist);
    menu.add(galerymenu);
    menu.add(linkmenu);

    MandelListMenu.SelectAction selectAction;
    menu.add(keyareasmenu=new MandelListModelMenu("Key Areas",this,keyareas));
    keyareasmenu.addSelectAction(selectAction=new MandelListMenu.SelectAction() {
      public void selectArea(MandelWindowAccess access, QualifiedMandelName name)
      {
        if (automark_keyarea.isSet()) {
          setAutoMark();
        }
      }
    });
    keyareaspopupmenu=new MandelListModelMenu("Key Areas",this,keyareas);
    keyareaspopupmenu.addSelectAction(selectAction);

    it=new UpdatableJMenuItem(areaUpAction);
    menu.add(it);
    it=new UpdatableJMenuItem(forkAction);
    menu.add(it);
    it=new UpdatableJMenuItem(parentAction);
    menu.add(it);
    if (!isReadonly()) {
      it=new JMenuItem(variationAction);
      menu.add(it);
      RecalcAction ra=new RecalcAction();
      ra.setCleanupWindow(true);
      this.addRenewStateListener(ra);
      it=new JMenuItem(ra);
      menu.add(it);
    }

    menu.add(getEnvironment().getListActions().createMenu(this,
                                                   new MandelNameSelector() {
      public QualifiedMandelName getSelectedMandelName()
      { return getQualifiedMandelName(); }
    }));

    menu.addSeparator();
    it=new JMenuItem(showListsControlAction);
    menu.add(it);
    it=new JMenuItem(new ShowBrowserAction());
    menu.add(it);
    menu.add(gotoAction);
    
    menu.addSeparator();
    menu.add(showJuliaAction);
    if (iterPathActive) menu.add(showIterationPathAction);

    menu.add(slideshowmodel.createMenu(this,
                    new SlideShowSourceAdapter(new MainSlideShowOneMode(),
                                               new MainSlideShowTwoMode())));
    menu.add(cloneAction);

    JMenu submenu=new UpdatableJMenu("Area Colormap");
    menu.add(submenu);
    if (!isReadonly()) {
      menu.add(saveAction);

      submenu.add(new UpdatableJMenuItem(areaCMSaveAction));
      submenu.add(new UpdatableJMenuItem(areaCMDeleteAction));
    }
    submenu.add(new UpdatableJMenuItem(areaCMAction));

    it=new UpdatableJMenuItem(new HomeAction());
    menu.add(it);
    it=new JMenuItem(env.getToolControlAction());
    menu.add(it);
    
    menu.add(new JPopupMenu.Separator());
    it=new JMenuItem(new AboutAction());
    menu.add(it);
    if (getWindow() instanceof JFrame) {
      it=new JMenuItem("Close");
      it.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e)
        {
          getWindow().dispose();
        }
      });
      menu.add(it);
    }
    setComponentPopupMenu(menu);
  }

  private class MainSlideShowOneMode extends SlideShowSourceAdapter.OneModeAdapter {
    @Override
    public QualifiedMandelName getSingleName(SlideShowModel model)
    {
      return getQualifiedMandelName();
    }
  }

  private class MainSlideShowTwoMode extends SlideShowSourceAdapter.TwoModeAdapter {
    @Override
    public QualifiedMandelName getFirstName(SlideShowModel model)
    {
      return getQualifiedMandelName();
    }

    @Override
    public QualifiedMandelName getSecondName(SlideShowModel model)
    {
      return getMark();
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  private void addMarkAction(JMenu menu, Action action)
  {
    action.setEnabled(false);
    JMenuItem it=new UpdatableJMenuItem(action);
    menu.add(it);
  }

  private MandelListFolderTreeModel browserModel;
  private MandelListFolderTreeModel memoryModel;

  public MandelListFolderTreeModel getMandelBrowserModel()
  {
    if (browserModel==null) {
      ComposedMandelListFolderTreeModel local;
      memoryModel=new DefaultMandelListFolderTreeModel(new MemoryMandelListFolderTree("memory"),
                                                   env.getAllScanner());
      memoryModel.setModifiable(true);
      browserModel=getEnvironment().getMandelListFolderTreeModel();
      local=new ComposedMandelListFolderTreeModel("list", null);
      local.setModifiable(!getEnvironment().isReadonly());
      local.addFolderTreeModel(browserModel, "general");
      local.addListModel(getHistory(), "history", false);
      local.addFolderTreeModel(memoryModel, "window memory");
      MandelListTableModel marks = new MandelListTableProxyModelForListModel(marked.all);
      marks.setModifiable(false);
      local.addListModel(marks, "marks");
      
      browserModel=local;
    }
    return browserModel;
  }

  public History getHistory()
  { return history;
  }

  public MandelWindowAccess getFrameAccess()
  { return this;
  }

  public ToolEnvironment getEnvironment()
  { return env;
  }

  public MandelName getMandelName()
  { return name.getMandelName();
  }

  public QualifiedMandelName getQualifiedMandelName()
  { return name;
  }

  public QualifiedMandelName getQualifiedName()
  { return name;
  }

  public MandelData getMandelData()
  { return image==null?null:image.getMandelData();
  }

  public MandelImage getMandelImage()
  { return image;
  }

  public MandelInfo getMandelInfo()
  { 
    return image==null?null:image.getMandelData().getInfo();
  }

  public MapperModel getMapperModel()
  { return mappermodel;
  }

  public ColormapModel getColormapModel()
  { return colormapmodel;
  }

  public boolean hasColormap()
  {
    return image.getColormap()!=null;
  }

  private void setTitle()
  { String scale="";
    //System.out.println("Image scale="+getImagePane().getScale());
    if (Math.round(getImagePane().getScaleX()*100)!=100.0) {
      scale=" ["+Math.round(getImagePane().getScaleX()*100)+"%]";
    }
    Window w=getWindow();
    if (w instanceof Frame) {
      ((Frame)w).setTitle("Mandel Image "+name+scale);
    }
  }

  private void setHighLight(QualifiedMandelName name)
  {
    this.highlighted.setQualifiedName(name);
    if (name!=null) showSubRect(name, true);
  }

  private void setImage(BufferedImage image)
  {
    if (debug) System.out.println("image to buffer");
    image=filter(image);
    buffer.setImage(image);
    buffer.setScale(1);
    if (debug) System.out.println("image buffer set");
  }

  private void updateKeyAreas()
  {
    System.out.println("update upstream key areas");
    MandelList list=getEnvironment().getAreas();
    keyareas.clear();
    if (!name.isRoot()) {
      MandelName loop=name.getMandelName().getParentName();
      while (!loop.isRoot()) {
        QualifiedMandelName n=new QualifiedMandelName(loop);
        if (list.contains(n)) {
          keyareas.add(n);
          System.out.println("  found "+n);
        }
        loop=loop.getParentName();
      }
    }
    keyareasGaleryAction.setEnabled(!keyareas.getList().isEmpty());
  }

  private void updateLinks()
  {
    MandelListModel m=getEnvironment().getLinkModel(name.getMandelName());
    linksGaleryAction.setEnabled(m!=null && !m.getList().isEmpty());
    gotolinks.setMandelListModel(m);
    showlinks.setMandelListModel(m);
    removelinks.setMandelListModel(m);

    MandelName loop=name.getMandelName();
    boolean found=false;
    boolean parent=false;
    linkhist.removeAll();
    linkhist.setEnabled(false);
    linkhistpopup.removeAll();
    linkhistpopup.setEnabled(false);
    while (!loop.isRoot()) {
      m=getEnvironment().getLinkModel(loop);
      if (m!=null && m.getList().size()>0) {
        if (parent) {
          System.out.println("found parent links for "+loop);
          linkhist.add(new MandelListModelMenu(loop.getName(),this,m));
          linkhist.setEnabled(found=true);
        }

        linkhistpopup.add(new MandelListModelMenu(loop.getName(),this,m));
        linkhistpopup.setEnabled(true);
      }
      loop=loop.getParentName();
      parent=true;
    }
    if (!found) System.out.println("no parent links found");
  }
  
  private void setImageData(MandelAreaImage img, boolean showlast)
  {
    QualifiedMandelName name=img.getName();
    System.out.println("setup image for file ="+img.getMandelData().getFile());
    stopHighLight();
    if (this.image!=null) {
      img.removeImageListener(imageListener);
      this.buffer.setToolTipHandler(null);
    }
    this.lastname.setEnabled(showlast);
    this.lastname.setQualifiedName(this.name);
    if (highlight_mode==H_LAST) highlighted.setName(lastname);
    this.name=name;
    this.image=img;

    if (img.getRasterData()!=null) {
      this.buffer.setToolTipHandler(tooltip.getToolTipHandler());
    }
    if (!hasColormap()) {
      showColormapAction.setEnabled(false);
    }
    else {
      showColormapAction.setEnabled(true);
    }
    img.addImageListener(imageListener);
    imageListener.stateChanged(null);

    if (!isPartial()) setInitialSize();
    history.add(name);
    updateLinks();
    updateKeyAreas();
    infoPopup.setData(img.getInfo(), name);
    updateSubRects();
    updateObjects("area",name);

    if (autoshow_info.isSet()) showInfoPopup();
    
    if (env.handleRasterSeen(img.getMandelData().getFile())) {
      MandelHandle h=env.getAutoImageDataScanner().getMandelHandle(name);
      if (h!=null) img.getMandelData().setFile(h.getFile());
      else {
        System.out.println("*** no raster file found for "+name+" after relocate");
      }
    }
    setImageDecoration(img.getInfo());
    setTitle();
    fireRenewState();
    fireChangeEvent();
    if (debug) System.out.println("image setup done");
  }

  protected BufferedImage filter(BufferedImage image)
  {
    if (maxx>0) {
      filterscale=maxx/image.getWidth();
      double y=maxx*image.getHeight()/image.getWidth();
      if (image.getWidth()>maxx) {
        BufferedImage n=new BufferedImage((int)maxx, (int)y,
                                          BufferedImage.TYPE_INT_RGB);
        Graphics2D g=n.createGraphics();
        g.drawImage(image, 0, 0, (int)maxx, (int)y, null);
        image=n;
      }
    }
    else filterscale=1;
    return image;
  }

  private Colormap getDefColormap()
  {
    if (defcolormap==null) {
      return colormapmodel.getColormap();
    }
    return defcolormap;
  }

  /////////////////////////////////////////////////////////////////////////
  // Image Factories
  /////////////////////////////////////////////////////////////////////////

  public MandelAreaImage getMandelImage(MandelName name) throws IOException
  { 
    fileinfo=new Environment.FileInfo();
    MandelAreaImage im=env.getMandelImage(name,colormapmodel.getResizeMode(),
            createColormapSource(name, fileinfo),
            mappermodel.getMapper(),fileinfo);
    return im;
  }

  public MandelAreaImage getMandelImage(QualifiedMandelName name) throws IOException
   {
    fileinfo=new Environment.FileInfo();
    MandelAreaImage im=env.getMandelImage(name,colormapmodel.getResizeMode(),
            createColormapSource(name.getMandelName(), fileinfo),
            mappermodel.getMapper(),fileinfo);
    return im;
  } 

  public MandelAreaImage getMandelImage(MandelHandle h) throws IOException
   {
    fileinfo=new Environment.FileInfo();
    MandelAreaImage im=env.getMandelImage(h, colormapmodel.getResizeMode(),
            createColormapSource(h.getName().getMandelName(), fileinfo),
            mappermodel.getMapper(),fileinfo);
    return im;
  }

  synchronized
  public MandelColormapCache getColormapCache()
  {
    return getEnvironment().getColormapCache();
  }

  private ColormapSource createColormapSource(MandelName n,
                                              Environment.FileInfo fileinfo)
  {
    if (parentcolormap.isSet()) {
      return new PanelColormapSource(n, fileinfo);
    }
    else {
      return getDefColormap();
    }
  }

  private class PanelColormapSource extends CachedUpstreamColormapSource {
    private Environment.FileInfo fileinfo;

    public PanelColormapSource(MandelName n, Environment.FileInfo fileinfo)
    {
      super(n,getEnvironment().getAreaColormapScanner(),getColormapCache());
      this.fileinfo=fileinfo;
    }

    @Override
    protected Colormap optimizedLoad(MandelHandle h)
    {
      if (!h.getHeader().isAreaColormap() &&
          getMandelName()!=null&&getMandelName().isAbove(getBasename())) {
        if (h.getName().getMandelName().isAbove(getMandelName())) {
          if (debug)
            System.out.println("new area is downstream");
          return getDefColormap();
        }
      }
      return super.optimizedLoad(h);
    }

    @Override
    protected void colormapFound(MandelHandle h, Colormap cm)
    {
      if (cm!=getDefColormap()) {
        super.colormapFound(h, cm);
        if (fileinfo!=null) fileinfo.setColormap(cm);
      }
    }

    @Override
    public Colormap getColormap()
    {
      Colormap cm=super.getColormap();
      if (cm==null) cm=getDefColormap();
      return cm;
    }
  }

  ////////////////////////
  private abstract class ImageFactory<T> {
    protected T arg;

    public ImageFactory(T arg)
    {
      this.arg=arg;
    }

    abstract MandelAreaImage getMandelImage() throws IOException;

    @Override
    public String toString()
    {
      return arg.toString();
    }
  }

  private class MandelNameImageFactory extends ImageFactory<MandelName> {
    public MandelNameImageFactory(MandelName arg)
    {
      super(arg);
    }

    public MandelAreaImage getMandelImage() throws IOException
    {
      return MandelImagePanel.this.getMandelImage(arg);
    }
  }

  private class QualifiedMandelNameImageFactory
          extends ImageFactory<QualifiedMandelName> {
    public QualifiedMandelNameImageFactory(QualifiedMandelName arg)
    {
      super(arg);
    }

    public MandelAreaImage getMandelImage() throws IOException
    {
      return MandelImagePanel.this.getMandelImage(arg);
    }
  }

  private class MandelHandleImageFactory extends ImageFactory<MandelHandle> {
    public MandelHandleImageFactory(MandelHandle arg)
    {
      super(arg);
    }

    public MandelAreaImage getMandelImage() throws IOException
    {
      return MandelImagePanel.this.getMandelImage(arg);
    }
  }

  public MandelHeader getOrigMandelHeader()
  {
    return image.getMandelData().getOrigHeader();
  }
  
  //////////////////////////////////////////////////////////////////////////
  // Image setter
  //////////////////////////////////////////////////////////////////////////
  private boolean setImage(ImageFactory fac, boolean showlast)
  {
    try {
      setBusy(true);
      Colormap def=defcolormap;
      MandelAreaImage img=fac.getMandelImage();
      if (img==null) {
        if (debug) System.out.println(fac+" not found -> rescan");
        env.rescan();
        img=fac.getMandelImage();
      }
      if (img!=null) {
        if (debug) System.out.println("got image for "+fac);
        setImage(img.getImage());
        setImageData(img, showlast);
        if (fileinfo.getColormap()==null) {
          if (debug) {
            System.out.println("-> finally preserving default colormap "+
                                   def.getSize());
          }
          defcolormap=def;
        }
        else {
          if (debug) System.out.println("-> finally keeping current colormap");
        }
        return true;
      }
      else mandelInfo("No image for "+fac+" found.");
    }
    catch (IOException ex) {
      mandelError("cannot load "+name,ex);
    }
    finally {
      setBusy(false);
    }
    return false;
  }


  public boolean setImage(MandelName name)
  { 
    return setImage(name,false);
  }

  public boolean setImage(MandelName name, boolean showlast)
  {
    return setImage(new MandelNameImageFactory(name),showlast);
  }

  public boolean setImage(QualifiedMandelName name)
  {
    return setImage(name,false);
  }

  public boolean setImage(QualifiedMandelName name, boolean showlast)
  {
    return setImage(new QualifiedMandelNameImageFactory(name),showlast);
  }

  public boolean setImage(MandelHandle h)
  {
    return setImage(h,false);
  }

  public boolean setImage(MandelHandle h, boolean showlast)
  {
    return setImage(new MandelHandleImageFactory(h),showlast);
  }

  ////////////////////////////////////////////////////////////////////////////

  private boolean busy;
  private Cursor  origcursor;

  private JComponent getCC()
  { return getImagePane().getContentPane();
  }

  protected void setBusy(boolean b)
  {
    
    if (b!=busy) {
      if (b) {
        if (debug) System.out.println("-------------------------------------------------");
        if (debug) System.out.println("set busy");
        origcursor=getImagePane().getCursor();
        getImagePane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
      else {
        if (debug) System.out.println("orig cursor");
        getImagePane().setCursor(origcursor);
      }
    }
    busy=b;
  }

  public void addChangeListener(ChangeListener h)
  {
    listeners.addChangeListener(h);
  }

  public void removeChangeListener(ChangeListener h)
  {
    listeners.removeChangeListener(h);
  }

  protected void fireChangeEvent()
  {
    listeners.fireChangeEvent(this);
  }

  public void showColormap()
  {   
    showColormapAction.setEnabled(false);
  }

  public void hideColormap()
  {
    showColormapAction.setEnabled(true);
  }

  //////////////////////////////////////////////////////////////////////////
  // Visible Rect handling
  //////////////////////////////////////////////////////////////////////////

  private VisibleRectFilter subareafilter=new VisibleRectFilter() {
    public boolean match(VisibleRect r)
    {
      return r.getName()!=null;
    }
  };

  public boolean subAreasShown()
  {
    return hideSubAction.isEnabled();
  }

  private String getLabel(MandelName n)
  {
    if (n==null) return null;
    if (fullareanames.isSet() || !n.getParentName().equals(getMandelName())) {
      return n.getName();
    }
    return n.getSubAreaName();
  }

  ////////////////////////////////////////////////////////////////////////////
  // sub area highlight

  public class ExplicitHighlight {
    private String id;
    private QualifiedMandelName name;
    private boolean enabled;

    public ExplicitHighlight(String id, boolean enabled)
    {
      this.id=id;
      this.enabled=enabled;
    }

    public ExplicitHighlight(String id)
    {
      this(id,false);
    }

    public boolean isActive()
    {
      return enabled && name!=null && _isActive();
    }

    protected boolean _isActive()
    {
      return MandelImagePanel.this.getMandelName().isHigher(getMandelName());
    }
    
    public void setEnabled(boolean enabled)
    {
      this.enabled=enabled;
    }

    public boolean isEnabled()
    {
      return enabled;
    }

    public MandelName getMandelName()
    {
      if (name==null) return null;
      return name.getMandelName();
    }
    
    public QualifiedMandelName getQualifiedName()
    {
      return name;
    }

    public void setQualifiedName(QualifiedMandelName name)
    {
      this.name=name;
    }

    public void setName(ExplicitHighlight hl)
    {
      setQualifiedName(hl.getQualifiedName());
    }

    public boolean isSet()
    {
      return name!=null;
    }

    public void clear()
    {
      name=null;
    }

    @Override
    public String toString()
    {
      return "ExplicitHighlight{"+"id="+id+"; name="+name+"; enabled="+enabled+
                                           "; active="+isActive()+'}';
    }
  }

  public final class ExplicitHighlights {
    private List<ExplicitHighlight> list;

    public ExplicitHighlights()
    {
      list=new ArrayList<ExplicitHighlight>();
    }

    public void add(ExplicitHighlight hl)
    {
      if (!list.contains(hl)) list.add(hl);
    }

    public void remove(ExplicitHighlight hl)
    {
       list.remove(hl);
    }

    public Handler createHandler()
    {
      return new Handler();
    }

    public final class Handler {
      private List<ExplicitHighlight> list;

      private Handler()
      {
        list=new ArrayList<ExplicitHighlight>();
        for (ExplicitHighlight h:ExplicitHighlights.this.list) {
          if (h.isActive() && !contains(h.getQualifiedName())) {
            if (debugHL) System.out.println("-- add "+h);
            list.add(h);
          }
          else {
            if (debugHL) System.out.println("-- ignore "+h);
          }
        }
      }

      public boolean contains(QualifiedMandelName n)
      {
        if (n==null) return false;
        return contains(n.getMandelName());
      }

      public boolean contains(MandelName n)
      {
        if (n!=null) for (ExplicitHighlight h:list) {
          if (h.getQualifiedName().getMandelName().equals(n)) return true;
        }
        return false;
      }

      public void handleAlreadyShown(QualifiedMandelName n)
      {
        if (n==null) return;
        handleAlreadyShown(n.getMandelName());
      }
      
      public void handleAlreadyShown(MandelName n)
      {
        if (n==null) return;
        ListIterator<ExplicitHighlight> i=list.listIterator();
        while (i.hasNext()) {
          ExplicitHighlight h=i.next();
          if (h.getQualifiedName().getMandelName().equals(n)) {
            if (debugHL) System.out.println("-- remove already seen "+h);
            i.remove();
          }
        }
      }

      public void show()
      {
        for (ExplicitHighlight h:list) {
          if (debugHL) System.out.println("-- show "+h);
          showSubRect(h.getQualifiedName(),true);
        }
      }
    }
  }

  private void updateSubRects()
  {
    _updateSubRects(true);
  }

  public void updateSubRects(MandelName name, boolean vis)
  {
    _updateSubRects(true,name,vis);
  }

  private void _updateSubRects(boolean rescan)
  {
    Set<MandelName> subs;
    getImagePane().discardAllRects(subareafilter);
    subareas.clear();
    _updateSubRects(rescan, getMandelName(),subAreasShown());
//
//    if (subAreasShown()) {
//      //System.out.println("Show all rects");
//      getImagePane().showAllRects();
//    }
  }

  private void _updateSubRects(boolean rescan, MandelName name, boolean vis)
  { Set<MandelName> subs;
    MandelScanner scanner=env.getAutoMetaScanner();
    ExplicitHighlights.Handler highlights=explicitHighlights.createHandler();
    highlights.handleAlreadyShown(name);
    if (rescan) scanner.rescan(false);
    subs=MandUtils.getSubNames(name, scanner);
    if (debug) System.out.println("**** found "+subs.size()+" sub areas");
    for (MandelName n:subs) {
      if (showSubRect(n,vis)) highlights.handleAlreadyShown(n);
      subareas.add(new QualifiedMandelName(n));
    }
    highlights.show();
  }

  private boolean showSubRect(MandelName n, boolean vis)
  {
    MandelHandle h=env.getMetaScanner().getMandelInfo(n);
    return showSubRect(h,vis);
  }

  private boolean showSubRect(QualifiedMandelName n, boolean vis)
  {
    MandelHandle h=env.getMetaScanner().getMandelInfo(n);
    return showSubRect(h,vis);
  }

  private boolean showSubRect(MandelHandle h, boolean vis)
  {
    if (h==null) return false;
    MandelName n=h.getName().getMandelName();
    MandelData data;
    try {
      data=h.getInfo();
    }
    catch (IOException ex) {
      data=null;
    }
    if (debug) System.out.println("  found "+n+": "+h.getHeader().getTypeDesc()+": "+data);
    VisibleRect rect=getImagePane().getRect(n.getName());
    if (data==null) {
      if (rect!=null) rect.discard();
    }
    else {
      MandelInfo info=data.getInfo();
      boolean ht=(highlighted.isActive() && highlight(n));
      if (rect==null) rect=getImagePane().createRect(n.getName(),
                                                     getLabel(n), info);
      updateRect(rect, info);
      rect.setVisible(ht||vis);
      if (ht) {
        highlight_active=true;
        highlight_timer.restart();
      }
      if (!h.getHeader().hasImageData()) rect.setStroke(dashed);
      else {
        if (highlight(n)) {
          rect.setStroke(favorite);
        }
        else {
          rect.setStroke(null);
        }
      }
      rect.setFixed(true);
      return ht||vis;
    }
    return false;
  }

  public void showSubRects()
  {
    showSubAction.setEnabled(false);
    hideSubAction.setEnabled(true);
    refreshSubAction.setEnabled(true);
    showSubMode=true;
    updateSubRects(); 
  }

  public void hideSubRects()
  {
    showSubAction.setEnabled(true);
    hideSubAction.setEnabled(false);
    refreshSubAction.setEnabled(false);
    showSubMode=false;
    getImagePane().hideAllRects();
  }

  public String findSubName(Point p)
  {
    VisibleRect r=getImagePane().findRect(p, true, true);
    if (r!=null) return r.getName();
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  // Actions
  ////////////////////////////////////////////////////////////////////////////
 
  private boolean highlight(MandelName n)
  {
    if (highlighted.isActive() && highlighted.getMandelName().equals(n)) return true;

    switch (highlight_mode) {
      case H_FAVORITES:
        return MandUtils.isAbove(n,getEnvironment().getFavorites().getRoot());
      case H_UNSEEN:
        return MandUtils.isAbove(n,getEnvironment().getUnseenRastersModel().getList());
    }
    return false;
  }
  
  private class NoHighlightAction extends AbstractAction {
    public NoHighlightAction()
    { super("None");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      highlight_mode=H_NONE;
      updateSubRects();
      if (debug) System.out.println("No highlight");
    }
  }
  
  private class FavoritesHighlightAction extends AbstractAction {
    public FavoritesHighlightAction()
    { super("Favorites");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      highlight_mode=H_FAVORITES;
      updateSubRects();
      if (debug) System.out.println("Favorites highlight");
    }
  }
  
  private class UnseenHighlightAction extends AbstractAction {
    public UnseenHighlightAction()
    { super("Unseen");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      highlight_mode=H_UNSEEN;
      updateSubRects();
      if (debug) System.out.println("Unseen highlight");
    }
  }

  private class LastHighlightAction extends AbstractAction {
    public LastHighlightAction()
    { super("Last Area");
    }

    public void actionPerformed(ActionEvent e)
    {
      highlight_mode=H_LAST;
      updateSubRects();
      if (debug) System.out.println("Last highlight");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  private class RecalcAction extends WindowControlAction {
    public RecalcAction()
    { super(MandelImagePanel.this.getWindow(),"Refine", new Creator());
    }
  }

  private class Creator implements WindowControlAction.WindowCreator {
    public Window createWindow(Window owner)
    {
      return new MandelImageRecalcDialog(MandelImagePanel.this.getFrameAccess(),
                  "Refinement Request",
                  getQualifiedMandelName(),getMandelData());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  private class ParentAction extends UpdatableAction {
    public ParentAction()
    { super("Parent");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      if (!getMandelName().isRoot()) {
        if (automark_parent.isSet()) {
          setAutoMark();
        }
        setImage(getMandelName().getParentName());
      }
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      setEnabled(!getMandelName().isRoot());
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  public void setMark()
  {
    setMark(getQualifiedMandelName());
  }

  public void setMark(QualifiedMandelName n)
  {
    if (n==null) clearMark();
    else {
      marked.setQualifiedName(n);
      if (marked.isActive()) {
        updateSubRects();
      }
      updateObjects("mark",marked.getQualifiedName());
    }
  }

  public void setAutoMark()
  {
    if (!marked.isSet() || !getMandelName().isAbove(marked.getMandelName())) {
      setMark();
    }
  }

  public QualifiedMandelName getMark()
  {
    return marked.getQualifiedName();
  }

  public void clearMark()
  {
    if (!marked.isSet()) return;
    boolean was_active=marked.isActive();
    marked.clear();
    if (was_active) {
      updateSubRects();
    }
    updateObjects("mark",marked.getQualifiedName());
  }

  /////////////////////////////

  private abstract class MarkActionBase extends OptionalAction {
    public MarkActionBase(String name)
    { super(name);
    }

    protected boolean isPossible()
    {
      return getMark()!=null;
    }

    public void actionPerformed(ActionEvent e)
    {
      if (isPossible()) {
        markActionPerformed(e);
      }
    }

    protected abstract void markActionPerformed(ActionEvent e);
  }

  private class SetMarkAction extends AbstractAction {
    public SetMarkAction()
    { super("Set");
    }

    public void actionPerformed(ActionEvent e)
    {
      setMark(getQualifiedMandelName());
    }
  }

  private class SwapMarkAction extends MarkActionBase {
    public SwapMarkAction()
    { super("Swap");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      QualifiedMandelName old=getQualifiedMandelName();
      setImage(marked.getQualifiedName());
      setMark(old);
    }
  }

  private class ClearMarkAction extends MarkActionBase {
    public ClearMarkAction()
    { super("Clear");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      clearMark();
    }
  }

   private class GotoMarkAction extends MarkActionBase {
    public GotoMarkAction()
    { super("Jump");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      setImage(marked.getQualifiedName());
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      super.updateObject(ctx);
      putValue(Action.NAME, marked.isSet()?marked.getQualifiedName().toString()
                                          :"Jump");
    }
  }

  private class MemorizeMarkAction extends MarkActionBase {

    public MemorizeMarkAction()
    { super("Memorize");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      getEnvironment().getMemoryModel().add(marked.getQualifiedName());
    }
  }

  private class LinkFromMarkAction extends MarkActionBase {

    public LinkFromMarkAction()
    { super("Link from");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      getEnvironment().addLink(marked.getMandelName(), getMandelName());
    }
  }


  private class UnlinkFromMarkAction extends MarkActionBase {

    public UnlinkFromMarkAction()
    { super("Unlink from");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      getEnvironment().removeLink(marked.getMandelName(), getMandelName());
    }
  }

  private class ShowMarkAction extends MarkActionBase {

    public ShowMarkAction()
    {
      super("Toggle Show Area");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      if (marked != null) {
        marked.show();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class CommonAncestorAction extends MarkActionBase {
    public CommonAncestorAction()
    { super("Common Ancestor");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      MandelName n=getMandelName();
      while (!n.isAbove(marked.getMandelName())) n=n.getParentName();
      setImage(n, true);
    }

    @Override
    protected boolean isPossible()
    {
      return super.isPossible() &&
             !getMandelName().isAbove(marked.getMandelName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  public MandelName getAreaMarker(MandelName n)
  {
    MandelList list=getEnvironment().getAreas();
    if (list!=null) {
      while (!n.isRoot()) {
        n=n.getParentName();
        if (list.get(n)!=null) return n;
      }
    }
    return null;
  }

  private class AreaUpAction extends OptionalAction {
    public AreaUpAction()
    { super("Key Area");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelList list=getEnvironment().getAreas();
      if (list!=null) {
        MandelName n=getAreaMarker(getMandelName());
        if (n!=null) {
          if (automark_keyarea.isSet()) {
            setAutoMark();
          }
          setImage(n);
        }
      }
    }

    @Override
    protected boolean isPossible()
    {
      return getAreaMarker(getMandelName())!=null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  public MandelName getNextAreaMarker(QualifiedMandelName n)
  {
    if (n==null) return null;
    return getNextAreaMarker(n.getMandelName());
  }
  
  public MandelName getNextAreaMarker(MandelName n)
  {
    if (n==null) return null;
    if (!getMandelName().isHigher(n)) return null;
    MandelName next=getMandelName();
    MandelList list=getEnvironment().getAreas();
    do {
      next=next.sub(n);
    }
    while (!next.equals(n) && list.get(next)==null);
    return next;
  }

  private abstract class AboveMarkAction extends MarkActionBase {

    public AboveMarkAction(String name)
    {
      super(name);
    }

    @Override
    protected boolean isPossible()
    {
      return super.isPossible() &&
             getMandelName().isHigher(getMark().getMandelName());
    }

    /**
     * prototype condition evaluation for all instances of AboveMarkAction
     * not overriding isPossible methods. Must be derived class to
     * provide prototype identity.
     */
    private class UpdateCondition extends BooleanUpdateCondition {
      public UpdateCondition()
      {
        super(AboveMarkAction.this.isPossible());
      }
    }

    /**
     * must be overridden, also, if a derived class redefines isPossible().
     * keep expensive condition evaluation in update context to reuse
     * the evaluation result for other derived classes sharing the
     * same condition.
     * @param ctx update context used to cache condition evaluation
     * @return per update cached result of isPossible();
     */
    @Override
    protected boolean isPossible(UpdateContext ctx)
    {
      UpdateCondition c=ctx.getEntry(UpdateCondition.class);
      if (c==null) {
        c=ctx.setEntry(UpdateCondition.class, new UpdateCondition());
      }
      return c.isTrue();
    }
  }

  private MandelName getNextForkMarker(MandelName n)
  {
    if (getMark()==null) return null;
    MandelName name=getMark().getMandelName();

    while (!n.equals(name) && (n=n.sub(name))!=null) {
      Set<MandelName> set=MandUtils.getSubNames(n, env.getAllScanner());
      if (set!=null&&set.size()>1) return n;
    }
    return null;
  }

  private class ForkAreaDownAction extends AboveMarkAction {
    public ForkAreaDownAction()
    { super("Fork Area Down");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      MandelName n=getNextForkMarker(getMandelName());
      if (n!=null) {
        setImage(n);
      }
    }
  }

  private class KeyAreaDownAction extends AboveMarkAction {
    public KeyAreaDownAction()
    { super("Key Area Down");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      MandelList list=getEnvironment().getAreas();
      QualifiedMandelName name=getMark();

      if (list!=null && name!=null) {
        MandelName n=getNextAreaMarker(name.getMandelName());
        if (n!=null) {
          setImage(n);
        }
      }
    }
  }

  private class AreaDownAction extends AboveMarkAction {
    public AreaDownAction()
    { super("Down");
    }

    protected void markActionPerformed(ActionEvent e)
    {
      QualifiedMandelName name=getMark();
      MandelName n=getMandelName();
      n=n.sub(name.getMandelName());
      if (n!=null) {
        setImage(n);
      }
    }
  }

  private class LinksGaleryAction extends OptionalAction {

    public LinksGaleryAction()
    {
      super("Links");
    }

    private MandelListModel getListModel()
    {
      return getEnvironment().getLinkModel(getMandelName());
    }
    
    @Override
    protected boolean isPossible()
    {
      MandelListModel m=getListModel();
      return (m!=null && !m.getList().isEmpty());
    }
    
    public void actionPerformed(ActionEvent e)
    {
      if (isPossible()) {
        MandelListModel m=getListModel();
        MandelList ml=new DefaultMandelList(m.getList());
        String title="Links Galery for "+getMandelName()+
                     " ("+Utils.sizeString(ml.size(), "entry")+")";
        System.out.println(title);
        new MandelListGaleryDialog(MandelImagePanel.this.getFrameAccess(), ml, title);
      }
    }
  }

  private class KeyAreasGaleryAction extends AbstractAction {

    public KeyAreasGaleryAction()
    {
      super("Key areas");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelListModel m=keyareas;
      if (m!=null && !m.getList().isEmpty()) {
        MandelList ml=new DefaultMandelList(m.getList());
        String title="Key Areas Galery for "+getMandelName()+
                     " ("+Utils.sizeString(ml.size(), "entry")+")";
        System.out.println(title);
        new MandelListGaleryDialog(MandelImagePanel.this.getFrameAccess(), ml, title);
      }
    }
  }

  private class ZoomGaleryAction extends AbstractAction {

    public ZoomGaleryAction()
    {
      super("Zoom");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelList ml=new DefaultMandelList();
      MandelName n=getMandelName();
      ml.add(getQualifiedName());
      while (!n.isRoot()) {
        n=n.getParentName();
        ml.add(0, new QualifiedMandelName(n));
      }
      String title="Zoom Galery for "+getMandelName()
        +" ("+Utils.sizeString(ml.size(), "entry")+")";
      System.out.println(title);
      new MandelListGaleryDialog(MandelImagePanel.this.getFrameAccess(), ml,
                                 title);
    }
  }

  private class LinksAction extends AbstractAction {
    public LinksAction()
    { super("Links");
    }

    public void actionPerformed(ActionEvent e)
    {
      JPopupMenu m=linkhistpopup;

      if (m.isEnabled()) {
        RectPointEvent re=((RectangleActionEvent)e).getRectanglePointEvent();
        m.show(MandelImagePanel.this,re.getX(), re.getY());
      }
    }
  }

  private class KeyAreasAction extends AbstractAction {
    public KeyAreasAction()
    { super("KeyAreas");
    }

    public void actionPerformed(ActionEvent e)
    {
      if (keyareaspopupmenu.isEnabled()) {
        RectPointEvent re=((RectangleActionEvent)e).getRectanglePointEvent();
        JPopupMenu m=keyareaspopupmenu.getPopupMenu();
        m.show(MandelImagePanel.this,re.getX(), re.getY());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  public MandelName getFork(MandelName n)
  {
    MandelList list=getEnvironment().getAreas();
    if (list!=null) {
      while (!n.isRoot()) {
        n=n.getParentName();
        Set<MandelName> set=MandUtils.getSubNames(n, env.getAllScanner());
        if (set!=null && set.size()>1) return n;
      }
    }
    return null;
  }

  private class ForkAction extends UpdatableAction {
    public ForkAction()
    { super("Last Fork");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelList list=getEnvironment().getAreas();
      if (list!=null) {
        MandelName n=getFork(getMandelName());
        if (n!=null) {
          if (automark_fork.isSet()) {
            setAutoMark();
          }
          setImage(n);
        }
      }
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      setEnabled(getFork(getMandelName())!=null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class ShowSubAction extends AbstractAction {
    public ShowSubAction()
    { super("Show Sub Areas");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      showSubRects();
      //System.out.println("shown "+this);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////

  private class RefreshSubAction extends AbstractAction {
    public RefreshSubAction()
    { super("Refresh Sub Areas");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      updateSubRects();
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////

  private class HideSubAction extends AbstractAction {
    public HideSubAction()
    { super("Hide Sub Areas");
    }
    
    public void actionPerformed(ActionEvent e)
    { 
      hideSubRects();
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////

  private class ShowMetaAction extends AbstractAction {
    public ShowMetaAction()
    { super("Image Meta Data");
    }
    
    public void actionPerformed(ActionEvent e)
    {
      MandelAreaViewDialog v;
      QualifiedMandelName name=getQualifiedMandelName();
      MandelData data=getMandelData();
      
//      MandelHandle found=env.getImageDataScanner().getMandelData(name);
//      if (found==null) {
//        env.rescan();
//        found=env.getImageDataScanner().getMandelData(name);
//      }
//      if (found!=null) {
//        //System.out.println("file="+found.getFile());
//      }
      v=new MandelImageAreaDialog(
              MandelImagePanel.this.getFrameAccess(),
              "Mandel Image Meta Information",
              name, data);
      v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      v.setVisible(true);
    }
  }

 
  ////////////////////////////////////////////////////////////////////////////

  private class ShowColormapAction extends WindowControlAction<ColormapDialog> {
    public ShowColormapAction(ColormapDialog cm)
    { super(MandelImagePanel.this.getWindow(),"Colormap", cm);
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class ShowListsControlAction extends AbstractAction {
    public ShowListsControlAction()
    { super("Lists");
    }

    public void actionPerformed(ActionEvent e)
    {
      if (!listsDialog.isVisible()) {
       listsDialog.setVisible(true);
       setEnabled(false);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class ShowJuliaAction extends AbstractAction {
    public ShowJuliaAction()
    { super("Show Julia");
    }

    public void actionPerformed(ActionEvent e)
    {
      if (!juliaDialog.isVisible()) {
       juliaDialog.setVisible(true);
       juliaDialog.update(1, 1);
       setEnabled(false);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class ShowIterationPathAction extends AbstractAction {
    public ShowIterationPathAction()
    { super("Show Iteration Path");
    }

    public void actionPerformed(ActionEvent e)
    {
      if (!iterDialog.isVisible()) {
       iterDialog.setVisible(true);
       MandelInfo info=getMandelData().getInfo();
       iterDialog.update(getMandelInfo().getLimitIt(),info.getXM(), info.getYM());
       setEnabled(false);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class ShowBrowserAction extends AbstractAction {
    public ShowBrowserAction()
    { super("List Browser");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelBrowserDialog b=new MandelBrowserDialog(
                                         MandelImagePanel.this.getFrameAccess(),
                                         getMandelBrowserModel());
      b.setVisible(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class CloneAction extends OptionalAction {

    public CloneAction()
    {
      this("New Window");
    }
    
    public CloneAction(String name)
    {
      super(name);
    }

    protected boolean isPossible()
    {
      return getQualifiedMandelName()!=null;
    }

    protected QualifiedMandelName getQualifiedMandelName()
    {
      return MandelImagePanel.this.getQualifiedMandelName();
    }
    
    public void actionPerformed(ActionEvent e)
    {
      if (isPossible()) try {
        QualifiedMandelName name=getQualifiedMandelName();
        MandelAreaImage img=getMandelImage(name);
        if (img!=null) {
          getEnvironment().createMandelImageFrame(img,
                  (int)MandelImagePanel.this.maxx);
        }
        else {
          mandelError("Image "+name+" not found");
        }
      }
      catch (IOException ex) {
        mandelError("Image cannot be loaded",ex);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class SaveAction extends AbstractAction {

    public SaveAction()
    {
      super("Save");
    }

    public void actionPerformed(ActionEvent e)
    {
      ToolEnvironment env=getEnvironment();
      MandelImage img=getMandelImage();
      MandelData d=img.getMandelData();
      AbstractFile f=d.getFile();
      String n=f.getName();
      int ix=n.lastIndexOf('.');
      long lm=f.getLastModified();
      File save=env.mapToRasterImageFile(f);
      d.setFile(new FileAbstractFile(save));

      env.startUpdate();
      try {
        d.setTemporary(false);
        d.getInfo().setImageCreationTime(System.currentTimeMillis());
        d.write();
        if (!d.getFile().equals(f)) {
          if (!getEnvironment().backupRasterFile(f)) {
            if (getEnvironment().isCleanupRaster()) {
              //System.out.println("delete "+f);
              if (f.isFile()) {
                MandelFolder.Util.delete(f.getFile());
              }
            }
          }
        }
        getEnvironment().getColormapCache().add(getQualifiedName(), 
                              new Colormap(d.getColormap()));
      }
      catch (IOException io) {
        mandelError("Image cannot be saved",io);
      }
      finally {
        env.finishUpdate();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class AreaCMSaveAction extends UpdatableAction {

    public AreaCMSaveAction()
    {
      super("Save");
    }

    public void actionPerformed(ActionEvent e)
    {
      ToolEnvironment env=getEnvironment();
      MandelImage img=getMandelImage();
      MandelData d=img.getMandelData();
      AbstractFile f=d.getFile();
      MandelData mc=new MandelData(d.getInfo());
      mc.setColormap(ColormapModel.ResizeMode.RESIZE_LOCK_COLORS, d.getColormap());
      String n=f.getName();
      int ix=n.lastIndexOf('.');
      long lm=f.getLastModified();
      File save=env.mapToAreaColormapFile(f);
      mc.setFile(new FileAbstractFile(save));

      env.startUpdate();
      try {
        mc.setTemporary(false);
        mc.write();
        MandelFolder.Util.add(save);
        Colormap cm = new Colormap(mc.getColormap());
        getEnvironment().getColormapCache().add(getQualifiedName(), cm);
        getEnvironment().getAreaColormapListModel().add(new ColormapName(getQualifiedName().toString()), cm);
        updateObjects("areacm",getMandelName());
      }
      catch (IOException io) {
        mandelError("Area colormap cannot be saved",io);
      }
      finally {
        env.finishUpdate();
      }
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      System.out.println(ctx.getReason().getReason()+": "+getMandelData().isModified());
      setEnabled(getMandelData().isModified());
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class AreaCMDeleteAction extends UpdatableAction {

    public AreaCMDeleteAction()
    {
      super("Delete");
    }

    public void actionPerformed(ActionEvent e)
    {
      ToolEnvironment env=getEnvironment();
      MandelHandle h=null;
      Set<MandelHandle> set=env.getAreaColormapScanner().getMandelHandles(getMandelName());
      if (set!=null)  {
        for (MandelHandle mh:set) {
          if (mh.getHeader().isAreaColormap()) {
            h=mh;
            break;
          }
        }
      }
      if (h!=null) {
        env.startUpdate();
        try {
          if (h.getFile().isFile()) {
             MandelFolder.Util.delete(h.getFile().getFile());
             getEnvironment().getColormapCache().remove(getMandelName());
             
             if (getOrigMandelHeader().hasColormap()) {
               Colormap cm = new Colormap(getMandelData().getColormap());
               getEnvironment().getColormapCache().add(getQualifiedName(), cm);
               getEnvironment().getAreaColormapListModel().add(new ColormapName(getQualifiedName().toString()), cm);
             } else {
                getEnvironment().getAreaColormapListModel().remove(new ColormapName(getQualifiedName().toString()));
             }
             updateObjects("areacm",getMandelName());
          }
        }
        catch (IOException io) {
          mandelError("Area colormap "+h.getFile()+"cannot be deleted", io);
        }
        finally {
          env.finishUpdate();
        }
      }
    }

    @Override
    public void updateObject(UpdateContext ctx)
    { boolean b=false;
      Set<MandelHandle> set=env.getAreaColormapScanner().getMandelHandles(getMandelName());
      if (set!=null)  {
        for (MandelHandle mh:set) {
          if (mh.getHeader().isAreaColormap()) {
            b=true;
            break;
          }
        }
      }
      setEnabled(b);
    }
  }

 ////////////////////////////////////////////////////////////////////////////

  private class AreaCMAction extends UpdatableAction {
    private MandelName name;
    
    public AreaCMAction()
    {
      super("Upstream");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelImagePanel.this.setImage(name);
    }

    @Override
    public void updateObject(UpdateContext ctx)
    { boolean b=false;
      MandelName n=getMandelName();
      do {
        Set<MandelHandle> set=env.getAreaColormapScanner().
          getMandelHandles(n);
        if (set!=null) {
          for (MandelHandle mh:set) {
            if (mh.getHeader().isAreaColormap()) {
              b=true;
              break;
            }
          }
        }
        if (!b) n=n.getParentName();
      }
      while (!b && n!=null);
      setEnabled(b);
      if (b) {
        name=n;
        putValue(Action.NAME, n.getName());
      }
      else {
        putValue(Action.NAME,"upstream");
      }
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////

  private class HomeAction extends UpdatableAction {

    public HomeAction()
    {
      super("Home");
    }

    public void actionPerformed(ActionEvent e)
    {
      setImage(MandelName.ROOT);
    }

    @Override
    public void updateObject(UpdateContext ctx)
    {
      setEnabled(!getMandelName().isRoot());
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private abstract class AbstractListAction extends AbstractAction {
    private MandelListModel model;

    public AbstractListAction(MandelListModel model, String cmd)
    { super(cmd);
      this.model=model;
    }

    public MandelListModel getModel()
    { return model;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class AddListEntryAction extends AbstractListAction {

    public AddListEntryAction(MandelListModel model)
    { super(model,"Add");
    }

    public void actionPerformed(ActionEvent e)
    { getModel().add(name);
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class RemoveListEntryAction extends AbstractListAction {

    public RemoveListEntryAction(MandelListModel model)
    { super(model,"Remove");
    }

    public void actionPerformed(ActionEvent e)
    { getModel().remove(name);
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class RemoveListShortcutAction extends AbstractListAction {
    public RemoveListShortcutAction(MandelListModel model)
    {
      super(model,"Remove Shortcut");
    }

    public void actionPerformed(ActionEvent e)
    {
      removeListShortcut(getModel());
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class OpenListShortcutAction extends AbstractListAction {
    private String name;

    public OpenListShortcutAction(MandelListTableModel model, String name)
    {
      super(model,"Open List");
      this.name=name;
    }

    public void actionPerformed(ActionEvent e)
    {
      new MandelListDialog(getFrameAccess(),name,(MandelListTableModel)getModel());
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private class VariationAction extends AbstractAction {
    public VariationAction()
    { super("Variation");
    }

    public void actionPerformed(ActionEvent e)
    {
      MandelVariationCreationDialog f=
              new MandelVariationCreationDialog(
                                        MandelImagePanel.this.getFrameAccess(),
                                        "Variation for "+getMandelName());
      f.setVisible(true);
    }
  }

  private class AboutAction extends AbstractAction {

    public AboutAction()
    {
      super("About");
    }

    public void actionPerformed(ActionEvent e)
    {
       new AboutBox(MandelImagePanel.this.getWindow()).setVisible(true);
    }
  }


  private class GotoAction extends WindowControlAction<GotoDialog> {

    public GotoAction()
    {
      super(MandelImagePanel.this.getMandelWindow(),
           "Goto",new GotoDialog(MandelImagePanel.this));
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  private void mandelError(String msg)
  {
    JOptionPane.showMessageDialog(MandelImagePanel.this,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE);
  }

  private void mandelError(String msg, Exception ex)
  {
    JOptionPane.showMessageDialog(MandelImagePanel.this,
                msg+": "+ex,
                "Error",
                JOptionPane.ERROR_MESSAGE);
  }

  private void mandelInfo(String msg)
  {
    JOptionPane.showMessageDialog(MandelImagePanel.this,
                msg,
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
  }
  
  public class SelectAreaListener implements RectEventListener,
                                             RectModifiedEventListener {

    private MandelHandle getMandelInfo(String n)
    { MandelHandle data=null;
      MandelName name=MandelName.create(n);

      if (name!=null) {
        data=env.getImageDataScanner().getMandelInfo(name);
        if (data==null) {
          //System.out.println("no image found for "+name);
          data=env.getMetaScanner().getMandelInfo(name);
        }
        if (data==null) {
          env.rescan();
          data=env.getMetaScanner().getMandelInfo(name);
        }
        if (data==null) {
          mandelError("cannot find mandel data for area "+name);
        }
      }
      return data;
    }

    private void updateDialog(VisibleRect rect, MandelHandle data,
                              boolean created)
    {
      
      //System.out.println("update info");
      
      if (created) {
        // start new area
        create.setRect(null);
        if (data!=null) {
          create.setAutoMode(false);
          create.setFilename(data.getFile().getPath(), false);
        }
        else {
          create.setAutoMode(true);
          create.setFilename(null, true);
        }
      }
      inupdate=true;
      rect.setVisible(true);
      create.setRect(rect);
      create.setVisible(true);
      inupdate=false;
      //System.out.println("update info done");
    }

    public void buttonClicked(RectEvent e)
    { VisibleRect rect=e.getRect();
      String n=rect.getName();

      //System.out.println("clicked at area "+n);
      if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2) {

        // edit are meta data
        if (e.isControlDown() && !isReadonly()) {
          if (!com.mandelsoft.util.Utils.isEmpty(n)&&rect.getOwner()!=null) {
            MandelHandle data=getMandelInfo(n);
            if (data!=null) {
              if (!data.getHeader().isInfo()) {
                mandelError("area "+n+" already proccesed");
                return;
              }
              rect.setFixed(false);
              updateDialog(rect,data,true);
            }
          }
        }

        // show area meta data
        else if (e.isShiftDown()) {
          MandelAreaViewDialog v;

          if (!com.mandelsoft.util.Utils.isEmpty(n)&&rect.getOwner()!=null) {
            MandelHandle data=getMandelInfo(n);
            if (data!=null) {
              try {
                v=new MandelImageAreaDialog(MandelImagePanel.this.getFrameAccess(),
                                            "Mandel Image Meta Information",
                                            data.getName(), data.getInfo());
                v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                v.setVisible(true);
              }
              catch (IOException ex) {
                System.err.println("*** cannot read "+data.getFile()+": "+ex);
              }
            }
          }
        }

        // show area as image
        else {
          //System.out.println("rect selected for image "+e.getRect());
          if (!com.mandelsoft.util.Utils.isEmpty(n)&&rect.getOwner()!=null) {
            MandelName name=MandelName.create(n);
            if (name!=null) {
              setImage(name);
            }
          }
        }
        // end chain
      }

      if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==1) {
        // show sub sub areas
        if (e.isAltDown()) {
          if (debug) System.out.println("show sub sub "+n);
          if (!com.mandelsoft.util.Utils.isEmpty(n)&&rect.getOwner()!=null) {
            MandelHandle data=getMandelInfo(n);
            if (data!=null) {
              showSubRect(data.getName(),true);
              updateSubRects(data.getName().getMandelName(),true);
            }
          }
          repaint();
        }
      }
    }

    synchronized
    public void rectModified(RectModifiedEvent e)
    { VisibleRect rect=e.getRect();
      MandelInfo info;
      boolean created;

      if (e.getAction()==RectModifiedEvent.RECT_CREATED) {
        if (debug) System.out.println("rect created "+rect+
                          " prop="+(((double)rect.getWidth())/rect.getHeight()));
        info=new MandelInfo(MandelImagePanel.this.getMandelInfo());
        info.setKeywords(new HashSet<String>());
        rect.setOwner(info);
        created=true;
      }
      else {
        //System.out.println("rect modified "+e.getRect());
        info=(MandelInfo)rect.getOwner();
        //System.out.println("  owner="+info);
        created=false;
      }
      //System.out.println("RECT "+info);
      if (info!=null) {
        updateInfo(info,rect._getRect());
        ProportionProvider p=proportion.getProportionProvider();
        if (p!=imagepropprov) {
          if (!created) {
            MandelInfo dia=create.getInfo();
            info.setRX(dia.getRX());
            info.setRY(dia.getRY());
            System.out.println(""+dia.getRX()+"/"+dia.getRY());
          }
          // adjust pixel size according to selected proportion
          if (((double)info.getRX())/info.getRY()!=p.getProportion()) {
            info.setRY((int)((double)info.getRX()/p.getProportion()));
          }
        }
        updateDialog(rect,null,created);
      }
    }
  }

  private boolean inupdate;

  synchronized
  public void updateInfo(MandelInfo info, Rectangle rect)
  {
    if (getMandelData()==null) return;
    //System.out.println("parent is "+getMandelInfo());
    if (enable_refcoords.isSet()) {
      ToolUtils.updateInfo(info, rect, getMandelData(),filterscale);
    }
    else {
      ToolUtils.updateInfo(info, rect, getMandelInfo(),filterscale);
    }
  }

  synchronized
  public void updateRect(VisibleRect rect, MandelInfo info)
  {
    if (inupdate || getMandelInfo()==null) return;
    ToolUtils.updateRect(info,rect,getMandelInfo(),filterscale);
    //System.out.println("update rect "+rect+"("+inupdate+")");
  }

  public class SubAreaListener implements MandelAreaListener {

    public void areaActionPerformed(MandelAreaEvent e)
    {
      if (debug) System.out.println("sub area event "+e.getId());
      MandelName name=e.getName();
      if (e.getId()==MandelAreaEvent.MA_UPDATE) {
        updateSubRects();
      }
      if (e.getId()==MandelAreaEvent.MA_CREATED) {
        MandelInfo info=e.getInfo();
        VisibleRect rect=e.getRect();
        if (debug) try {
          System.out.println("area selected name="+name+", info="+info+
                  ",rect="+rect+" prop="+(rect.getWidth()/rect.getHeight()));
        }
        catch (RuntimeException ex) {
        }
        if (name!=null&&info!=null&&rect!=null) {
          // keep rect, notfy as handled
          if (!subAreasShown()) {
            System.out.println("*** keep rect");
            ((MandelSubAreaCreationDialog)e.getSource()).setRectHandled();
//          rect.discard();
            //MandelImagePanel.this.getImagePane().findRect(name.getMandelName());
            rect.setName(name.getName());
            rect.setLabel(getLabel(name));
            rect.setOwner(new MandelInfo(info));
            rect.setFixed(true);
            rect.activate();
          }
        }
      }
      if (e.getId()==MandelAreaEvent.MA_DELETED) {
        VisibleRect rect=e.getRect();
        if (rect!=null) {
          if (debug) System.out.println("discard rect");
          rect.discard();
        }
        updateSubRects();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Info Popup
  /////////////////////////////////////////////////////////////////////////
  
  public class InfoPopup extends RenderedComponent {
    private JLayeredPane active;

    private JTextField info;
    private JTextField magn;
    private JTextField min;
    private JTextField max;
    private JTextField time;
    private JTextField area;

    private Timer timer;
    private QualifiedMandelName name;
    private String location;
            
    public InfoPopup()
    {
      timer=new Timer(2000,new HideHandler());
      JPanel effective=new JPanel();
      FlowLayout l=(FlowLayout)effective.getLayout();
      l.setHgap(5);
      l.setVgap(5);
      
      effective.add(new JLabel("Info: "));
      effective.add(info=createField());
      effective.add(new JLabel("Magn: "));
      effective.add(magn=createField());
      effective.add(new JLabel("Min: "));
      effective.add(min=createField());
      effective.add(new JLabel("Max: "));
      effective.add(max=createField());
      effective.add(new JLabel("Time: "));
      effective.add(time=createField());
      effective.add(new JLabel("Areas: "));
      effective.add(area=createField());
      effective.validate();

      setEffectiveComponent(effective);
      setOpaque(false);
      addMouseListener(new MouseListener());
    }

    private JTextField createField()
    {
      JTextField f=new JTextField("");
      f.setEditable(false);
      f.setBorder(null);
      return f;
    }

    public boolean isActive()
    {
      return active!=null;
    }

    public void setData(MandelInfo i, QualifiedMandelName name)
    {
      
      int m=MandUtils.getMagnification(i);
      setMagnification(m);
      setInfo(name);
      setMin(i.getMinIt());
      setMax(i.getMaxIt());
      setTime(i.getTime());
      String l=i.getLocation();
      if (!l.isEmpty()) location=" ("+l+")";
      else location="";
    }

    public void setInfo(QualifiedMandelName name)
    {
      this.name=name;
      updateInfo();
    }

    private void updateInfo()
    {
      String info=MandelAreaViewDialog.getInfoString(getEnvironment(),name);
      int ic=count(name.getMandelName(), getEnvironment().getImageDataScanner());
      setInfo(info+location);
      setAreas(""+ic);
      validate();
    }

    public void setInfo(String v)
    {
      if (v==null || v.length()==0) v="     ";
      info.setText(v);
    }

    public void setAreas(String v)
    {
      if (v==null || v.length()==0) v="     ";
      area.setText(v);
    }
    
     public void setMagnification(int v)
    {
      magn.setText("1e"+v);
    }

    public void setMin(int v)
    {
      min.setText(""+v);
    }

    public void setMax(int v)
    {
      max.setText(""+v);
    }

    public void setTime(long t)
    {
      time.setText(MandelImageAreaDialog.formatTime(t));
    }


    public void showAt(Component c, Point p)
    {
      timer.start();
      updateInfo();

      JRootPane rp=SwingUtilities.getRootPane(c);
      Point rpp=SwingUtilities.convertPoint(c, p, rp);
      Dimension d=getPreferredSize();
      setVisible(true);
      setBounds(rpp.x, rpp.y, (int)d.getWidth(), (int)d.getHeight());
      if (debug) System.out.println("show "+getBounds());
      (active=rp.getLayeredPane()).add(this, JLayeredPane.PALETTE_LAYER+1);
      rp.repaint();
    }

    private void _hide()
    {
      if (debug) System.out.println("disable popup");
      timer.stop();
      setVisible(false);
      if (active!=null) {
        active.remove(InfoPopup.this);
      }
      active=null;
    }

    /////////////////////////////////////////////////
    private class MouseListener extends MouseAdapter {

      @Override
      public void mouseEntered(MouseEvent e)
      {
        timer.stop();
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        timer.restart();
      }
    }

    private class HideHandler implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
        _hide();
      }
    }
  }

  private InfoPopup infoPopup=new InfoPopup();

  public void showInfoPopup()
  {
    JViewport vp=scrollpane.getViewport();
    Rectangle vr=vp.getViewRect();
    int w=vr.width/2;
    Dimension d=infoPopup.getPreferredSize();
    Point dst=new Point(w-d.width/2/*+vr.x*/, 0/*vr.y*/);
    infoPopup.showAt(this, dst);
  }

  public class PixelListener extends MouseAdapter {

    @Override
    public void mouseMoved(MouseEvent e)
    {
      int px,py;

      /********************************************/
      JViewport vp=scrollpane.getViewport();
      Rectangle vr=vp.getViewRect();
      px=e.getX()-vr.x;
      py=e.getY()-vr.y;
      if (py<BufferedComponent.CORNER_RECT) {
        int w=vr.width/2;
        if (Math.abs(w-px)<BufferedComponent.CORNER_RECT*6) {
          if (debug) {
            JRootPane rp=SwingUtilities.getRootPane(e.getComponent());
            Point rpp=SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
                                                  rp);
            System.out.println(":"+e.getX()+","+e.getY()+":"+vp.getViewRect()
              +"--- "+rpp.x+","+rpp.y);
          }
          if (!infoPopup.isActive()) {
            showInfoPopup();
//            Dimension d=infoPopup.getPreferredSize();
//            Point dst=new Point(w-d.width/2+vr.x, vr.y);
//            infoPopup.showAt(e.getComponent(), dst);
          }
        }
      }


      /********************************************/
      px=(int)(buffer.translateX(e)/filterscale);
      py=(int)(buffer.translateY(e)/filterscale);

      if (e.isAltDown()) {
        // notify julia dialog
        if (juliaDialog.isVisible() || iterDialog.isVisible()) {
          //System.out.println("julia "+e.getX()+","+e.getY());
          MandelInfo parent=getMandelInfo();
          BigDecimal x=MandArith.sub(parent.getXM(),
                       MandArith.div(parent.getDX(), 2));
          BigDecimal y=MandArith.add(parent.getYM(),
                       MandArith.div(parent.getDY(), 2));

          BigDecimal jx=MandArith.add(x,MandArith.div(
                  MandArith.mul(parent.getDX(),px),parent.getRX()));
          BigDecimal jy=MandArith.sub(y,MandArith.div(
                  MandArith.mul(parent.getDY(),py),parent.getRY()));
          if (juliaDialog.isVisible())
            juliaDialog.update(jx.doubleValue(), jy.doubleValue());
          if (iterDialog.isVisible())
            iterDialog.update(parent.getLimitIt(),jx, jy);
        }
      }
      try {
        // notify colormap dialog to highlight colormap entry
        if (hasColormap()) {
          //System.out.println("x: "+e.getX()+" -> "+px);
          //System.out.println("y: "+e.getY()+" -> "+py);
          colormapDialog.hightLight(
              getMandelImage().getColormapIndex(px,py));
        }
      }
      catch (UnsupportedOperationException ex) {
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // temporary area highlighting
  ////////////////////////////////////////////////////////////////////////////

  private class HighlightHandler implements ActionListener {
    public void actionPerformed(ActionEvent e)
    {
      if (highlight_active) {
        stopHighLight();
        updateSubRects();
      }
    }
  }

  private void stopHighLight()
  {
    if (highlight_active) highlight_timer.stop();
    highlight_active=false;
    highlighted.clear();
  }

  ////////////////////////////////////////////////////////////////////////////
  // colormap rotation
  ////////////////////////////////////////////////////////////////////////////

  private static class Rotator extends Timer implements ActionListener {
    private ColormapModel model;
    private Colormap orig;
    private boolean active;
    private boolean updating;

    private Action rotate=new RotateAction();
    private Action stop=new StopAction();
    private ChangeListener listener=new StopListener();

    public Rotator(ColormapModel model)
    {
      super(100, null);
      addActionListener(this);
      this.model=model;
      setActive(false);
    }

    public JMenu createMenu()
    {
      JMenu menu=new JMenu("Rotate Colors");
      menu.add(new JMenuItem(rotate));
      menu.add(new JMenuItem(stop));
      return menu;
    }

    private void setActive(boolean b)
    {
      active=b;
      rotate.setEnabled(!b);
      stop.setEnabled(b);
    }

    public void rotate()
    {
      if (!active) {
        setActive(true);
        orig=model.getColormap();
        model.setColormap(new Colormap(orig));
        model.addChangeListener(listener);
        start();
      }
    }

    public void cancel()
    {
      cancel(true);
    }

    private void cancel(boolean reset)
    {
      if (active) {
        setActive(false);
        stop();
        model.removeChangeListener(listener);
        if (reset) model.setColormap(orig);
      }
    }

    public void actionPerformed(ActionEvent e)
    {
      Colormap cm=model.getColormap();
      updating=true;
      cm.startModification();
      Color old=cm.getColor(1);
      for (int i=2; i<cm.getSize(); i++) {
        cm.setColor(i-1, cm.getColor(i));
      }
      cm.setColor(cm.getSize()-1, old);
      cm.endModification();
      updating=false;
      start();
    }

    private class StopListener implements ChangeListener {
      public void stateChanged(javax.swing.event.ChangeEvent e)
      {
        if (!updating) cancel(false);
      }
    }

    private class RotateAction extends AbstractAction {
      public RotateAction()
      { super("Rotate");
      }

      public void actionPerformed(ActionEvent e)
      {
        rotate();
      }
    }

    private class StopAction extends AbstractAction {
      public StopAction()
      {
        super("Stop");
      }

      public void actionPerformed(ActionEvent e)
      {
        cancel();
      }
    }
  }
 
  ///////////////////////////////////////////////////////////////////////////
  // renew state listeners
  ///////////////////////////////////////////////////////////////////////////

  private List<RenewStateListener> rnlisteners=new ArrayList<RenewStateListener>();

  public void addRenewStateListener(RenewStateListener l)
  {
    rnlisteners.add(l);
  }
  public void removeRenewStateListener(RenewStateListener l)
  {
    rnlisteners.remove(l);
  }

  protected void fireRenewState()
  {
    for (RenewStateListener l:rnlisteners) {
      try {
       l.renewState(getWindow());
      }
      catch (Exception e) {
        e.printStackTrace(System.err);
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////////////////

  private static class TestFrame extends JFrame
                                 implements MandelWindowAccess {
    MandelImagePanel panel;

    public TestFrame(ToolEnvironment env, MandelAreaImage img) throws IOException
    {
      panel=new MandelImagePanel(env,img);
      add(panel);
      pack();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
    }

    public ColormapModel getColormapModel()
    {
      return panel.getColormapModel();
    }

    public ToolEnvironment getEnvironment()
    {
      return panel.getEnvironment();
    }

    public Window getMandelWindow()
    {
      return this;
    }

    public MandelData getMandelData()
    {
      return panel.getMandelData();
    }

    public MandelImage getMandelImage()
    {
      return panel.getMandelImage();
    }

    public MandelName getMandelName()
    {
      return panel.getMandelName();
    }

    public MapperModel getMapperModel()
    {
      return panel.getMapperModel();
    }

    public QualifiedMandelName getQualifiedName()
    {
      return panel.getQualifiedName();
    }

    public MandelImagePanel getMandelImagePane()
    {
      return panel;
    }

    public History getHistory()
    {
      return panel.getHistory();
    }
  }

  public static void main(final String[] args)
  {
    try {
      ToolEnvironment env;
      env=new ToolEnvironment(args);
      synchronized (env) {
        try {
          createWindow(env);
        }
        catch (IllegalArgumentException ia) {
          System.out.println("illegal mandel name");
        }
      }
    }
    catch (IllegalConfigurationException ex) {
      System.out.println("illegal config: "+ex);
    }
  }

  static void createWindow(final ToolEnvironment env)
  {

    EventQueue.invokeLater(new Runnable() {
      public void run()
      {
        synchronized (env) {
          try {
            MandelAreaImage img=env.getMandelImage(env.getInitialName());
            JFrame frame=new TestFrame(env, img);
          }
          catch (IOException io) {
            System.out.println("cannot read image");
          }
        }
      }
    });
  }
}
