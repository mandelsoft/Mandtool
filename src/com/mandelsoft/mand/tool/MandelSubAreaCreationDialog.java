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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import com.mandelsoft.mand.MandelConstants;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;

/**
 *
 * @author Uwe Krueger
 */
public class MandelSubAreaCreationDialog extends MandelAreaCreationDialog {

  public MandelSubAreaCreationDialog(MandelWindowAccess owner, String title)
  {
    super(owner, title);
  }

  @Override
  public void setVisible(boolean b)
  {
    super.setVisible(b);
    if (!b) handleClose();
  }

  @Override
  protected MandelAreaView createView(QualifiedMandelName name, Object info,
                                      boolean change, boolean readonly)
  {
    return new SubAreaView(name,(MandelInfo)info);
  }

  public void setRect(VisibleRect rect)
  {
    ((SubAreaView)getView()).setRect(rect);
  }

  public void setRectHandled()
  {
    ((SubAreaView)getView()).setRectHandled();
  }
  
  ///////////////////////////////////////////////////////////////////////
  // view
  ///////////////////////////////////////////////////////////////////////

  protected class SubAreaView extends CreationView {
    private JButton namebutton;
    private JButton nextnamebutton;

    public SubAreaView(QualifiedMandelName name, MandelInfo info)
    {
      super(name, info);
    }

    @Override
    protected void setupButtons()
    {
      super.setupButtons();
      namebutton=createButton("Name", "Determine next free sub area name",
                   new NameAction());
      nextnamebutton=createButton("Next", "Determine next free area name for given name",
                   new NextNameAction());
      addShowButton("Show sub area", true);
    }

    @Override
    public void setFilename(String n, boolean modifiable)
    {
      super.setFilename(n,modifiable);
      namebutton.setEnabled(modifiable);
      nextnamebutton.setEnabled(modifiable);
    }

    final protected void determineNextFilename()
    {
      ToolEnvironment env=getMandelFrame().getEnvironment();
      System.out.println("env="+env);
      MandelFileName n=MandelFileName.create(new File(getFilename()));
      if (n==null) {
        Error("Sub Area Creation Problem", "Filename is no mandel file name");
        return;
      }
      MandelName sub=MandUtils.getNextName(n.getName(), env.getAutoMetaScanner());
      if (sub==null) {
        Error("Sub Area Creation Problem", "No further names available.");
      }
      else {
        File path=env.getInfoFolder(null);
        if (path==null) path=new File(".");
        setFilename(
                new File(path,
                         sub.getName()+MandelConstants.INFO_SUFFIX).getPath());
      }
    }

    final protected void determineFilename()
    {
      ToolEnvironment env=getMandelFrame().getEnvironment();
      System.out.println("env="+env);
      MandelName parent=getMandelFrame().getMandelName();
      MandelInfo info=(MandelInfo)getInfo();
      MandelName root=MandUtils.lookupRoot(env.getMetaScanner(), parent, info.getSpec());
      if (root==null) root=parent;
      MandelName sub=MandUtils.getNextSubName(root, env.getAutoMetaScanner());
      if (sub==null) {
        Error("Sub Area Creation Problem", "No further names available.");
      }
      else {
        File path=env.getInfoFolder(null);
        if (path==null) path=new File(".");
        setFilename(
                new File(path,
                         sub.getName()+MandelConstants.INFO_SUFFIX).getPath());
      }
    }
    
    private class NameAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        determineFilename();
      }
    }

    private class NextNameAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        determineNextFilename();
      }
    }

    public void setRectHandled()
    {
      if (this.rect!=null) {
        this.rect.setProportionProvider(null);
      }
      this.rect=null;
    }

    public void setRect(VisibleRect rect)
    {
      
      if (this.rect!=null && rect!=this.rect) {
        handleClose();
      }

      if (rect==null) {
        setRectHandled();
        return;
      }

      MandelInfo info=(MandelInfo)rect.getOwner();
      if (rect.getName()!=null) {
        if (rect==this.rect) MandUtils.round(info);
        setInfo("Subarea "+rect.getName(),info);
      }
      else {
        MandUtils.round(info);
        setInfo("New Subarea",info);
      }
      
      if (automode && rect!=this.rect) determineFilename();
      _setRect(rect);
    }

    protected void _setRect(VisibleRect rect)
    {
      this.rect=rect;
    }
    
    @Override
    protected VisibleRect getSelectedRect()
    { return rect;
    }
  }
}
