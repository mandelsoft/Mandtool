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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.ctx.ContextProvider;
import com.mandelsoft.mand.tool.ctx.DeleteFolderAction;
import com.mandelsoft.mand.tool.ctx.MandelListFolderContextMenuHandler;
import com.mandelsoft.mand.tool.ctx.NewFolderAction;
import com.mandelsoft.mand.util.FileMandelListFolderTree;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.swing.ActionPanel;
import com.mandelsoft.swing.DnDJTree;
import com.mandelsoft.swing.GBC;

/**
 *
 * @author Uwe Krüger
 */

public class MandelListFolderPanel extends ActionPanel
                                   implements MandelListFolderTreeModelSource {
  private static final boolean debug=false;

  private MandelListFolderTreeModel     model;
  private DnDJTree                  tree;
  private JScrollPane               view;
  private boolean                   modifiable;
  private JButton                   add;
  private JButton                   remove;
  private MandelListFolder          active;
  private ContextHandler            contextHandler;

  public MandelListFolderPanel(MandelListFolderTree ftree,
                               MandelScanner scanner)
  {
    this(new DefaultMandelListFolderTreeModel(ftree, scanner),scanner);
  }

  public MandelListFolderPanel(MandelListFolderTreeModel model,
                               MandelScanner scanner)
  {
    // super(new GridLayout(1,0));
    Dimension minimumSize = new Dimension(10, 10);
    this.model=model;
    modifiable=model.isModifiable();
    tree = new DnDJTree(this.model);
    tree.setCellRenderer(new FolderTreeRenderer());
    tree.setEditable(true);
    tree.addTreeSelectionListener(new TSL());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    view = new JScrollPane(tree);
    view.setMinimumSize(minimumSize);

    JLabel c=new JLabel("height");
    FontMetrics m=c.getFontMetrics(c.getFont());
    view.setPreferredSize(new Dimension(
            (int)(m.charWidth('W')*10), (int)(m.getHeight()*6)));
//    view.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//    view.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    //Add the scroll panes to a split pane.
//    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//    splitPane.setTopComponent(view);
//    splitPane.setBottomComponent(new JLabel("Test"));
//    splitPane.setMinimumSize(minimumSize);

//    setContentPane(new JPanel());
//    addContent(view,null);
//    setContentPane(new JPanel());

    addContent(view,new GBC(0,0).setWeight(10, 10).setFill(GBC.BOTH));

    tree.setToggleClickCount(3);
    tree.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent event)
        { // check for double
          // click
          if (event.getClickCount()<2)
            return;

          TreePath p=tree.getPathForLocation(event.getX(), event.getY());
          if (p!=null) {
            if (debug) System.out.println("click at "+DefaultMandelListFolderTreeModel.path(p));
            fireActionPerformed(ActionEvent.ACTION_PERFORMED, null);
          }
        }
      });
    tree.setContextMenuHandler(contextHandler=new ContextHandler());

    add=addButton("new folder",new AddActionListener());
    remove=addButton("delete folder",new DeleteActionListener());
    add.setEnabled(false);
    remove.setEnabled(false);
    handleModifiable(modifiable);
  }

  @Override
  protected void panelBound()
  {
    super.panelBound();
    ToolTipManager.sharedInstance().registerComponent(tree);
  }

  @Override
  protected void panelUnbound()
  {
    super.panelUnbound();
    ToolTipManager.sharedInstance().unregisterComponent(tree);
  }


  public MandelListFolderTreeModel getModel()
  {
    return model;
  }

  public void setModel(MandelListFolderTreeModel model)
  {
    this.model=model;
    tree.setModel(model);
  }

  public void setActiveFolder(MandelListFolder folder)
  {
    if (active!=folder) {
      MandelListFolder old=active;
      active=folder;
      firePropertyChange("activeFolder",old,active);
    }
  }

  public MandelListFolder getActiveFolder()
  {
    return active;
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public void setModifiable(boolean modifiable)
  {
    if (this.modifiable!=modifiable) {
      handleModifiable(this.modifiable=modifiable);
      firePropertyChange("modifiable",!modifiable,modifiable);
    }
  }

   protected void handleModifiable(boolean modifiable)
  {

    remove.setVisible(modifiable);
    add.setVisible(modifiable);

    tree.setEditable(modifiable);
    model.setModifiable(modifiable);
  }

  public TreePath getSelectedPath()
  {
    return tree.getSelectionPath();
  }
  
  public MandelListFolder getSelectedFolder()
  {
    TreePath p=tree.getSelectionPath();
    if (p==null) return null;
    return (MandelListFolder)p.getLastPathComponent();
  }

  public void setRootVisible(boolean b)
  {
    tree.setRootVisible(b);
  }

  private boolean folderMetaModifiable(TreePath p)
  {
    return MandelListFolderTreeModel.Util.folderMetaModifiable(getModel(), p);
  }

  private boolean folderModifiable(TreePath p)
  {
    return MandelListFolderTreeModel.Util.folderModifiable(getModel(), p);
  }

  private boolean folderContentModifiable(TreePath p)
  {
    return MandelListFolderTreeModel.Util.folderContentModifiable(getModel(), p);
  }

  ////////////////////////////////////////////////////////////////
  // tree
  ////////////////////////////////////////////////////////////////

  private class TSL implements TreeSelectionListener {

    public void valueChanged(TreeSelectionEvent e)
    {
      boolean mod=false;
      MandelListFolderTreeModel m=null;
      TreePath p=e.getNewLeadSelectionPath();

      if (p!=null) {
        if (debug) System.out.println("TSE: "+DefaultMandelListFolderTreeModel.path(p));
        //new Throwable().printStackTrace(System.out);

        m=(MandelListFolderTreeModel)getModel();
        mod=m.isPathModifiable(p)&&
                !((MandelListFolder)p.getLastPathComponent()).isLeaf();
      }
      else {
        if (debug) System.out.println("TSE: remove selection");
      }
      add.setEnabled(folderModifiable(p));
      remove.setEnabled(folderModifiable(p)&&folderMetaModifiable(p));
    }
  }

  private class FolderTreeRenderer extends DefaultTreeCellRenderer {
    private String desc;

    public FolderTreeRenderer()
    {
      setIcon(folderIcon);
      setLeafIcon(listIcon);
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
    {
      MandelListFolder f=(MandelListFolder)value;
//      System.out.println("render "+f.getName()+
//                         " active:"+(active==null?"none":active.getName())+
//                         " list:"+(f.getMandelList()!=null)+
//                         " leaf:"+f.isLeaf()+
//                         " tleaf:"+leaf+" sel:"+sel);
      desc=f.getProperty(MandelListFolder.DESCRIPTION);
      if (!f.isLeaf()) {
        if (f.getMandelList()==null) {
          setInfo(folderIcon,"This folder has no list.");
        }
        else {
          if (active==value) {
            setInfo(activeFolderlistIcon,
                      "this folder has a list, which is displayed");
          }
          else {
            setInfo(folderlistIcon, "this folder has a list");
          }
        }
      }
      else {
        if (active==value) {
          //System.out.println("render active list "+f.getName());
          setInfo(activeListIcon, "this is a plain list, which is displayed");
        }
        else {
          //System.out.println("render inactive list "+f.getName());
          setInfo(listIcon, "this is a plain list");
        }
      }
      //String tt=getToolTipText();
      return super.getTreeCellRendererComponent(
              tree, value, sel,
              expanded, leaf, row,
              hasFocus);
//      setToolTipText(tt);
//      return c;
    }

    private String desc(String desc, String def)
    {
      if (desc==null || desc.length()==0) return def;
      return desc;
    }

    private void setInfo(Icon icon, String tooltip)
    {
      setOpenIcon(icon);
      setClosedIcon(icon);
      setLeafIcon(icon);
      setToolTipText(desc(desc,tooltip));
    }
  }

  ////////////////////////////////////////////////////////////////
  // actions
  ////////////////////////////////////////////////////////////////
  private ContextProvider<MandelListFolder,
                          TreePath,
                          MandelListFolderTreeModel> prov=
   new ContextProvider<MandelListFolder,TreePath,MandelListFolderTreeModel>() {

    public TreePath getSelectionSpec()
    {
      return null;
    }

    public MandelListFolder getSelectedItem()
    {
      return MandelListFolderPanel.this.getSelectedFolder();
    }

    public Window getWindow()
    {
      return MandelListFolderPanel.this.getWindow();
    }

    public MandelListFolderTreeModel getModel()
    {
      return MandelListFolderPanel.this.getModel();
    }
  };

  private class DeleteActionListener extends DeleteFolderAction {

    public DeleteActionListener()
    {
      super(prov);
    }
  }

  private class AddActionListener extends NewFolderAction {

    public AddActionListener()
    {
      super(prov);
    }
  }

  //////////////////////////////////////////////////////////////////////
  // Conext menu
  //////////////////////////////////////////////////////////////////////


  private class ContextHandler extends MandelListFolderContextMenuHandler
                               implements DnDJTree.ContextMenuHandler {

    @Override
    public MandelList getSelectedMandelList()
    {
      return getSelectedItem().getMandelList();
    }

    public void handleContextMenu(DnDJTree table, MouseEvent evt, TreePath path)
    {
      handleContextMenu((JComponent)table,evt,path);
    }
  }

  ////////////////////////////////////////////////////////////////
  // listener support
  ////////////////////////////////////////////////////////////////

  public void addActionListener(ActionListener l)
  {
    listenerList.add(ActionListener.class, l);
  }

  public void removeActionListener(ActionListener l)
  {
    listenerList.remove(ActionListener.class, l);
  }

  public ActionListener[] getActionListeners()
  {
    return getListeners(ActionListener.class);
  }

  protected void fireActionPerformed(int id, String cmd)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    ActionEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==ActionListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new ActionEvent(this, id, cmd);
        ((ActionListener)listeners[i+1]).actionPerformed(e);
      }
    }
  }

 

  /////////////////////////////////////////////////////////////
  // icons
  /////////////////////////////////////////////////////////////

  protected static ImageIcon createImageIcon(String path)
  {
    java.net.URL imgURL=MandelListFolderPanel.class.getResource(path);
    if (imgURL!=null) {
      return new ImageIcon(imgURL);
    }
    else {
      System.err.println("Couldn't find file: "+path);
      return null;
    }
  }

  static public final ImageIcon listIcon=
          createImageIcon("resc/list2.gif");
  static public final ImageIcon activeListIcon=
          createImageIcon("resc/alist2.gif");
  static public final ImageIcon folderIcon=
          createImageIcon("resc/folder2.gif");
  static public final ImageIcon folderlistIcon=
          createImageIcon("resc/folderlist2.gif");
  static public final ImageIcon activeFolderlistIcon=
          createImageIcon("resc/afolderlist2.gif");

  /////////////////////////////////////////////////////////////
  // test
  /////////////////////////////////////////////////////////////

  static class TestFrame extends JFrame {
    TestFrame(MandelListFolderTree ftree)
    {
      MandelListFolderPanel panel=new MandelListFolderPanel(
                                          ftree,
                                          null);
      panel.setModifiable(true);
      add(panel);
      pack();
    }
  }

  public static void main(String[] args)
  {
    File file = new File(args[0]);
    final FileMandelListFolderTree f=new FileMandelListFolderTree(file);
    f.print("",f.getRoot());

    SwingUtilities.invokeLater(new Runnable() {

      public void run()
      {
        JFrame frame=new TestFrame(f);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });
  }

 
}
