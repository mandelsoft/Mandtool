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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.image.MandelAreaImage;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerListenerAdapter;
import com.mandelsoft.mand.tool.lists.MandelListsMenuFactory;
import com.mandelsoft.mand.util.MandelColormapCache;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.swing.WindowControlAction;
import java.awt.SplashScreen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 *
 * @author Uwe Krueger
 */
public class ToolEnvironment extends Environment {
  private JFileChooser fileChooser;
  private InitialStartupFrame control;
  private ToolControlAction toolControl;
  private int windowcnt;
  private boolean doexit=false;

  private ColorListModel colors;
  private TagListModel   tags;
  private TagListModel   attrs;
  private MandelListFolderTreeModel tfavorites;
  private MandelListFolderTreeModel memory;
  private MandelListTableModel favorites;
  private MandelListFolderTreeModel ttodos;
  private MandelListTableModel todos;
  private MandelListFolderTreeModel tlinks;
  private AreasModel     areas;
  private NewRasterModel newrasters;
  private UnseenRasterModel unseenrasters;
  private MandelListTableModel variants;
  private MandelListTableModel leafs;
  private MandelListTableModel pending;
  private MandelListTableModel unseenrefinements;
  private MandelListTableModel refinerequests;
  private MandelListTableModel requests;

  private ComposedMandelListFolderTreeModel lists;
  private ColormapListModel colormaps;
  private ColormapListModel areacolormaps;

  private MandelListsMenuFactory listactions;

  private ImageBaseModel imagebase_model;
  private MandelColormapCache colormap_cache;

  ///////////////////////////////////////////////////////////////////////
  // Tool control
  ///////////////////////////////////////////////////////////////////////

  private class ToolControlAction extends WindowControlAction {
    public ToolControlAction()
    { super(null,"Tool Control", new Creator());
    }
  }

  private class Creator implements WindowControlAction.WindowCreator {
    @Override
    public Window createWindow(Window owner)
    {
      return new ToolControlFrame(ToolEnvironment.this);
    }
  }

  ///////////////////////////////////////////////////////////////////////

  public ToolEnvironment(String[] args) throws IllegalConfigurationException
  {
    super("mandtool",args);
    setup();
  }

  public ToolEnvironment(String[] args, File dir) throws IllegalConfigurationException
  {
    super("mandtool",args,dir);
    setup();
  }

  public ToolEnvironment(String[] args, URL dir) throws IllegalConfigurationException
  {
    super("mandtool",args,dir);
    setup();
  }

  private void setup()
  {
    ComposedMandelListFolderTreeModel userlists;
    List<MandelListFolderTree> tmp;

    int size=80;
    String v=getProperty(Settings.COLORMAP_CACHE_SIZE);
    if (v!=null) {
      try {
      size=Integer.parseInt(v);
      if (size<=0) size=80;
      }
      catch (NumberFormatException nfe) {
        System.out.println("illegal colormap cache size "+v+": "+nfe);
        // keep default
      }
    }

    colormap_cache=new MandelColormapCache(size);
    Colormap cm=getDefaultColormap();
    if (cm!=null) colormap_cache.lock(QualifiedMandelName.ROOT, cm);
    
    refresh_pending=new HashMap<MandelListTableModel,Boolean>();
    refresh_order=new ArrayList<MandelListTableModel>();
    toolControl = new ToolControlAction();
    listactions=new MandelListsMenuFactory(isReadonly());

    if (!isReadonly())
      fileChooser=new JFileChooser();
    if (getColors()!=null) {
      colors=new ColorListModel(getColors());
      colors.setAutosave(!isReadonly());
    }
    if (getTags()!=null) {
      tags=new TagListModel(getTags());
      tags.setAutosave(!isReadonly());
    }
    if (getAttrs()!=null) {
      attrs=new TagListModel(getAttrs());
      attrs.addUniqueElement(MandelInfo.ATTR_TITLE);
      attrs.addUniqueElement(MandelInfo.ATTR_ITERATONMETHOD);
      attrs.addUniqueElement(MandelInfo.ATTR_REFCOORD);
      attrs.addUniqueElement(MandelInfo.ATTR_REFPIXEL);
      attrs.setAutosave(!isReadonly());
    }
    if (getAreas()!=null) {
      areas=new AreasModel();
    }
    if (getFavorites()!=null) {
      tfavorites=getMandelListFolderTreeModel(getFavorites());
      favorites=tfavorites.getMandelListModel(tfavorites.getRoot());
    }
    memory=new GeneralFolderTreeModel(getMemory(),true);
    if (getTodos()!=null) {
      ttodos=getMandelListFolderTreeModel(getTodos());
      todos=ttodos.getMandelListModel(ttodos.getRoot());
    }
    if (getLinks()!=null) {
      tlinks=new LinkFolderTreeModel(getLinks());
    }
    if (getNewRasters()!=null) newrasters=new NewRasterModel();
    if (getUnseenRasters()!=null) unseenrasters=new UnseenRasterModel();
    variants=new VariantsModel();
    leafs=new LeafModel();
    pending=new PendingModel();
    // first refinement requests and then unseen unseenrefinements
    // refresh order of listeners is important
    if (getRefinementRequests()!=null) refinerequests=new RefinementRequestsModel();
    if (getUnseenRefinements()!=null) unseenrefinements=new UnseenRefinementModel();
    if (getRequests()!=null) requests=new RequestsModel();


    lists=new ComposedMandelListFolderTreeModel("lists",getAllScanner());
    lists.setModifiable(!isReadonly());
    if (unseenrasters!=null) lists.addListModel(unseenrasters, "unseen",
                                "list of image areas not yet seen");
    lists.addFolderTreeModel(memory, memory.getRoot().getName());
    lists.addListModel(variants, "variants");
    if (tfavorites!=null) lists.addFolderTreeModel(tfavorites, "favorites");
    if (ttodos!=null) lists.addFolderTreeModel(ttodos, "todo");
    if (tlinks!=null) lists.addFolderTreeModel(tlinks, "links");
    if (areas!=null) lists.addListModel(areas,"areas",
                             "list of marked area roots for backward navigation");
    if (newrasters!=null) lists.addListModel(newrasters, "new",
                             "list of finished areas not yet examined");
    if (leafs!=null) lists.addListModel(leafs, "leafs",
                             "list of areas without further sub areas");
    if (pending!=null) lists.addListModel(pending, "pending",
                             "list of areas with pending sub area calculations");

    tmp=getUserLists();
    if (!tmp.isEmpty()) {
      userlists=new ComposedMandelListFolderTreeModel("misc",getAllScanner());
      for (MandelListFolderTree t:tmp) {
        userlists.addFolderTreeModel(getMandelListFolderTreeModel(t), t.getRoot().getName());
      }
      lists.addFolderTreeModel(userlists,"misc");
    }

    colormaps=new ExtendedColormapListModel(getColormaps());
    areacolormaps=new DefaultColormapListModel(getAreaColormaps());

    imagebase_model=new ImageBaseModel(this);
  }

  public ImageBaseModel getImagebaseModel()
  {
    return imagebase_model;
  }

  //////////////////////////////////////////////////////////////////////////
  // link support

  public void addLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (tlinks==null || isReadonly()) return;
    done|=_addLink(src,dst);
    done|=_addLink(dst,src);
    if (done) handleAddLink(src,dst);
  }

  public void removeLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (tlinks==null || isReadonly()) return;
    done|=_removeLink(src,dst);
    done|=_removeLink(dst,src);
    if (done) handleRemoveLink(src,dst);
  }

  public boolean _removeLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (tlinks==null||isReadonly()) return done;
    MandelListFolder f=tlinks.getChild(tlinks.getRoot(), src.getName());
    if (f!=null) {
      MandelListModel m=tlinks.getMandelListModel(f);
      QualifiedMandelName qn=new QualifiedMandelName(dst);
      done=f.contains(qn);
      m.remove(qn);
      if (m.getList().isEmpty()) {
        tlinks.removeFolder(f);
      }
    }
    return done;
  }

  private boolean _addLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (tlinks==null || isReadonly()) return done;
     MandelListFolder f=tlinks.getChild(tlinks.getRoot(), src.getName());
     if (f==null) {
       f=tlinks.insertFolder(src.getName(), tlinks.getRoot());
       f.setThumbnailName(new QualifiedMandelName(src));
     }
     QualifiedMandelName qn=new QualifiedMandelName(dst);
     done=!f.contains(qn);
     tlinks.add(f, qn);
     return done;
  }

  public MandelListModel getLinkModel(MandelName n)
  {
    if (tlinks==null) return null;
    MandelListFolder f=tlinks.getChild(tlinks.getRoot(), n.getName());
    if (f==null) return null;
    return tlinks.getMandelListModel(f);
  }

  private Set<LinkListener> llisteners=new HashSet<LinkListener>();

  public void addLinkListener(LinkListener h)
  {
    llisteners.add(h);
  }

  public void removeLinkListener(LinkListener h)
  {
    llisteners.remove(h);
  }

  private void handleAddLink(MandelName src, MandelName dst)
  { 
    for (LinkListener h:llisteners) {
      h.linkAdded(src,dst);
    }
  }

  private void handleRemoveLink(MandelName src, MandelName dst)
  {
    for (LinkListener h:llisteners) {
      h.linkRemoved(src,dst);
    }
  }

  synchronized
  public MandelColormapCache getColormapCache()
  {
    return colormap_cache;
  }

  //////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////

  public WindowControlAction getToolControlAction()
  { return toolControl;
  }

  public MandelListsMenuFactory getListActions()
  {
    return listactions;
  }

  synchronized
  public JFileChooser getFileChooser()
  {
    return fileChooser;
  }

  public MandelListFolderTreeModel getMandelListFolderTreeModel()
  {
    return lists;
  }

  public ColorListModel getColorsModel()
  {
    return colors;
  }

  public TagListModel getTagsModel()
  {
    return tags;
  }
  
  public TagListModel getAttributeModel()
  {
    return attrs;
  }
  

  public MandelListTableModel getFavoritesModel()
  { return favorites;
  }

  public MandelListTableModel getTodosModel()
  { return todos;
  }

  public MandelListTableModel getMemoryModel()
  { return memory.getMandelListModel(memory.getRoot());
  }

  public MandelListTableModel getAreasModel()
  { return areas;
  }

  public MandelListTableModel getNewRastersModel()
  { return newrasters;
  }

  public MandelListTableModel getUnseenRastersModel()
  { return unseenrasters;
  }

  public MandelListTableModel getPendingModel()
  { return pending;
  }

  public MandelListTableModel getVariantsModel()
  { return variants;
  }

  public MandelListTableModel getUnseenRefinementsModel()
  { return unseenrefinements;
  }

  public MandelListTableModel getRefinementRequestsModel()
  { return refinerequests;
  }
  
  public MandelListTableModel getRequestsModel()
  { return requests;
  }

  public ColormapListModel getColormapListModel()
  { return colormaps;
  }
  
   public ColormapListModel getAreaColormapListModel()
  { return areacolormaps;
  }
  
  @Override
  public boolean handleRasterSeen(AbstractFile f)
  {
    boolean b;

    startUpdate();
    try {
      b=super.handleRasterSeen(f);
      if (b && newrasters!=null) {
        QualifiedMandelName name=QualifiedMandelName.create(f);
        System.out.println("remove from new rasters "+f);
        newrasters.setModifiable(true);
        newrasters.remove(name);
        newrasters.setModifiable(false);
      }
    }
    finally {
      finishUpdate();
    }
    return b;
  }

  @Override
  protected void seenModified()
  {
    super.seenModified();
    if (unseenrasters!=null) unseenrasters.fireTableDataChanged();
  }

  @Override
  protected void unseenRefinementsModified()
  {
    super.unseenRefinementsModified();
    if (unseenrefinements!=null) {
      if (debug) System.out.println("unseen refinements modified");
      unseenrefinements.fireTableDataChanged();
    }
  }
  
  private DefaultListModel frames=new DefaultListModel();
  
  public ListModel getMandelWindowsModel()
  {
    return frames;
  }
  

  private void setupFrame(JFrame frame)
  {
    new FrameListener(frame);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    incrementWindowCount();
  }

  private class FrameListener extends WindowAdapter {
    private JFrame frame;

    public FrameListener(JFrame frame)
    {
      this.frame=frame;
      if (frame instanceof MandelImageFrame) {
        frames.addElement(frame);
      }
      frame.addWindowListener(this);
    }

     @Override
    public void windowClosed(WindowEvent e)
    {
      decrementWindowCount();
      if (control==e.getSource()) control=null;
      frame.removeWindowListener(this);
      frames.removeElement(e.getSource());
    }
  }

  synchronized
  public void createControlFrame()
  {
    if (control==null) setupFrame(control=new InitialStartupFrame(this));
  }

  synchronized
  public void createMandelImageFrame(String n) throws IOException
  { File f=new File(n);
    QualifiedMandelName name;

    //System.out.println(f.getName()+": "+f);
    if (f.getName().equals(n)) {
      try {
        name=QualifiedMandelName.create(f);
        createMandelImageFrame(name);
        return;
      }
      catch (IllegalArgumentException ex) {
        throw new IOException(n+" is no mandel name.");
      }
    }
    String base=f.getName();

    try {
      name=QualifiedMandelName.create(f);
    }
    catch (IllegalArgumentException ex) {
      throw new IOException(n+" is no mandel name.");
    }

    MandelData md=new MandelData(f);
    MandelImage img=getFactory().getImage(md);
    if (img!=null) {
      setupFrame(new MandelImageFrame(this, new MandelAreaImage(name,img)));
    }
    else {
      throw new IOException(n+": no image.");
    }
  }

  synchronized
  public void createMandelImageFrame(MandelName mn) throws IOException
  {
    createMandelImageFrame(getMandelImage(mn));
  }

  synchronized
  public void createMandelImageFrame(MandelAreaImage img) throws IOException
  {
    setupFrame(new MandelImageFrame(this, img));
  }

  synchronized
  public void createMandelImageFrame(MandelAreaImage img, int maxx) throws IOException
  {
    setupFrame(new MandelImageFrame(this, img, maxx));
  }

  synchronized
  public void createMandelImageFrame(QualifiedMandelName mn) throws IOException
  {
    MandelAreaImage img=getMandelImage(mn);
    if (img!=null)
      createMandelImageFrame(img);
  }

  synchronized
  public void createMandelImageFrame(QualifiedMandelName mn,
                                     ColormapModel cm) throws IOException
  {
    MandelAreaImage img=getMandelImage(mn,cm);
    if (img!=null)
      createMandelImageFrame(img);
  }

  synchronized
  public void createMandelImageFrame(QualifiedMandelName mn, int maxx) throws IOException
  {
    MandelAreaImage img=getMandelImage(mn);
    if (img!=null)
      createMandelImageFrame(img,maxx);
  }

  synchronized
  public void createMandelImageFrame(QualifiedMandelName mn, ColormapModel cm,
                                     int maxx) throws IOException
  {
    MandelAreaImage img=getMandelImage(mn,cm);
    if (img!=null)
      createMandelImageFrame(img,maxx);
  }

  synchronized
  public void createMandelImageFrame(File f) throws IOException
  { MandelImage img;
    img=getFactory().getImage(f);
    if (img!=null) {
      QualifiedMandelName name=QualifiedMandelName.create(f);
      if (name==null) name=QualifiedMandelName.create("unknown-unknown");
      createMandelImageFrame(new MandelAreaImage(name,img));
    }
  }

  synchronized
  public void incrementWindowCount()
  { windowcnt++;
  }

  synchronized
  public void decrementWindowCount()
  { windowcnt--;
    if (windowcnt<=0 && doexit) System.exit(0);
  }

  synchronized
  public void startup()
  {
    doexit=true;
    SwingUtilities.invokeLater(new Runnable() {
      public void run()
      {
        SplashScreen sp=SplashScreen.getSplashScreen();
        if (sp!=null) sp.close();
        if (getInitialFile()!=null) {
          try {
            createMandelImageFrame(getInitialFile());
          }
          catch (IOException io) {
            System.err.println("cannot load file "+getInitialFile()+": "+io);
            System.exit(1);
          }
        }
        else if (getImageDataScanner().getMandelHandle(getInitialName())!=null) {
          try {
            createMandelImageFrame(getInitialName());
          }
          catch (IOException ex) {
            createControlFrame();
          }
        }
        else {
          createControlFrame();
        }
      }
    });
  }

//  public MandelListTableModel getMandelListModel(MandelList l)
//  {
//    return new GeneralModel(l,!isReadonly());
//  }

  public MandelListFolderTreeModel getMandelListFolderTreeModel(MandelListFolderTree l)
  {
    return new GeneralFolderTreeModel(l,!isReadonly());
  }

  /////////////////////////////////////////////////////////////////////////

  private class GeneralFolderTreeModel extends DefaultMandelListFolderTreeModel {

    public GeneralFolderTreeModel(MandelListFolderTree tree, boolean mod)
    {
      super(tree, getAllScanner());
      setModifiable(mod);
    }
  }

  private class LinkFolderTreeModel extends DefaultMandelListFolderTreeModel {
    private boolean mod;

    public LinkFolderTreeModel(MandelListFolderTree tree)
    {
      super(tree, getAllScanner());
      setModifiable(false);
      this.mod=!isReadonly();
    }

    @Override
    protected boolean isListModifiable(MandelListFolder f)
    {
      return mod;
    }
  }

  public MandelListTableModel getMandelListModel(String path)
  {
    MandelListFolderTreeModel fm=getMandelListFolderTreeModel();
    MandelListFolder f=fm.getRoot();
    MandelListTableModel model;

    String[] comps=path.split("/");
    for (String comp :comps) {
      if (debug) System.out.println("lookup "+comp);
      if (!comp.isEmpty()) {
        f=f.getSubFolder(comp);
        if (f==null) break;
      }
    }
    if (f==null) return null;
    model=fm.getMandelListModel(f);
    return model;
  }

  /////////////////////////////////////////////////////////////////////////
  // mandel list model wrapper
  /////////////////////////////////////////////////////////////////////////

  private Map<MandelListTableModel,Boolean> refresh_pending;
  private List<MandelListTableModel>        refresh_order;

  public void refresh(MandelListTableModel m, boolean soft)
  {
    if (!isInUpdate()) m.refresh(soft);
    else {
      Boolean v=refresh_pending.get(m);
      if (v==null) {
        refresh_pending.put(m,soft);
        refresh_order.add(m);
      }
      else {
        if (v.booleanValue() && !soft) {
          refresh_pending.put(m,soft);  // force hard reset
        }
      }
    }
  }

  @Override
  protected void handleUpdate()
  {
    super.handleUpdate();
    while (!refresh_order.isEmpty()) {
      MandelListTableModel m=refresh_order.get(0);
      m.refresh(refresh_pending.get(m));
      refresh_order.remove(0);
      refresh_pending.remove(m);
    }
    imagebase_model.handleUpdate();
  }

  /////////////////////////////////////////////////////////////////////////

  private class AreasModel extends DefaultMandelListTableModel {

    public AreasModel()
    {
      super(getAreas(),getImageDataScanner());
      setModifiable(!isReadonly());
    }
  }

  //////////////////////////////////////////////////////////////////////////
  public class AutoRefreshMandelListTableModel
               extends DefaultMandelListTableModel {
    private MandelScanner refresh;

    public AutoRefreshMandelListTableModel(MandelList list, MandelScanner scanner)
    {
      this(list,scanner,scanner);
    }

    public AutoRefreshMandelListTableModel(MandelList list,
                                    MandelScanner listscanner,
                                    MandelScanner refreshscanner)
    {
      super(list,listscanner);
      refresh=refreshscanner;
      if (refresh!=null) {
        refresh.addMandelScannerListener(new Listener());
      }
    }

    private class Listener extends MandelScannerListenerAdapter {

      @Override
      public void addMandelFile(MandelScanner s, MandelHandle h)
      {
        handleEnvironmentChange();
      }

      @Override
      public void removeMandelFile(MandelScanner s, MandelHandle h)
      {
        handleEnvironmentChange();
      }

      @Override
      public void scannerChanged(MandelScanner s)
      {
        handleEnvironmentChange();
      }

      private void handleEnvironmentChange()
      {
        if (!isInUpdate()) {
          if (debug) {
            System.out.println("soft refresh for scanner refresh: "+
                                  AutoRefreshMandelListTableModel.this);
          }
          //new Throwable().printStackTrace(System.out);

          ToolEnvironment.this.refresh(AutoRefreshMandelListTableModel.this,true);
        }
      }
    }
  }

//  private class GeneralModel extends DefaultMandelListTableModel {
//
//    public GeneralModel(MandelList list, boolean mod)
//    {
//      super(list, getAllScanner());
//      setModifiable(mod);
//    }
//  }

  /////////////////////////////////////////////////////////////////////////

  private class NewRasterModel extends AutoRefreshMandelListTableModel {

    public NewRasterModel()
    {
      super(getNewRasters(),getNewRasterScanner());
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private class UnseenRasterModel extends AutoRefreshMandelListTableModel {

    public UnseenRasterModel()
    {
      super(getUnseenRasters(),getImageDataScanner());
      addAction(new SyncNewAction());
    }

    private class SyncNewAction extends AbstractAction {

      public SyncNewAction()
      {
        super("Sync New");
        putValue(SHORT_DESCRIPTION,"Synchoronize with new images");
      }

      public void actionPerformed(ActionEvent e)
      {
        MandelList unseen=getList();
        MandelList n=getNewRasters();
        n.refresh(false);

        ToolEnvironment.this.refresh(unseen);
        unseen.retainAll(n);
        try {
          unseen.save();
        }
        catch (IOException ex) {
          System.err.println("cannot write seen: "+ex);
        }
        fireTableDataChanged();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private class VariantsModel extends AutoRefreshMandelListTableModel {

    public VariantsModel()
    {
      super(getVariants(),getImageDataScanner());
    }
  }

  private class LeafModel extends AutoRefreshMandelListTableModel {

    public LeafModel()
    {
      super(getLeafs(),getImageDataScanner(),getAllScanner());
    }

  }

  private class PendingModel extends AutoRefreshMandelListTableModel {

    public PendingModel()
    {
      super(getPending(),getImageDataScanner(),getAllScanner());
    }
  }

  private class UnseenRefinementModel extends AutoRefreshMandelListTableModel {

    public UnseenRefinementModel()
    {
      super(getUnseenRefinements(),getImageDataScanner());
    }
  }

  private class RefinementRequestsModel extends AutoRefreshMandelListTableModel {

    public RefinementRequestsModel()
    {
      super(getRefinementRequests(),getInfoScanner());
    }
  }
  
  private class RequestsModel extends AutoRefreshMandelListTableModel {

    public RequestsModel()
    {
      super(getRequests() ,getInfoScanner(), getAllScanner());
    }
  }
  
  
  /////////////////////////////////////////////////////////////////////////

  static public interface Listener {
    void mandelListDeleted(MandelList list);
  }

  static public class ListenerAdapter implements Listener {
    public void mandelListDeleted(MandelList list)
    {
    }
  }

  private Set<Listener> listeners=new HashSet<Listener>();
  private boolean listenerActive;

  synchronized
  public void addEnvironmentListener(Listener l)
  {
    if (listenerActive) listeners=new HashSet<Listener>(listeners);
    listeners.add(l);
  }

   synchronized
  public void removeEnvironmentListener(Listener l)
  {
    if (listenerActive) listeners=new HashSet<Listener>(listeners);
    listeners.remove(l);
  }

   synchronized
  public void fireMandelListDeleted(MandelList list)
  {
     try {
      listenerActive=true;
      for (Listener l :listeners) l.mandelListDeleted(list);
    }
    finally {
      listenerActive=false;
    }
  }
}
