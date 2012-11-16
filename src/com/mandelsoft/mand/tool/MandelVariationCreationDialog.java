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
import com.mandelsoft.mand.tool.MandelAreaCreationDialog.CreationView;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;
import com.mandelsoft.swing.ProportionProvider;

/**
 *
 * @author Uwe Krueger
 */
public class MandelVariationCreationDialog extends MandelAreaCreationDialog {

  public MandelVariationCreationDialog(MandelWindowAccess owner, String title)
  {
    super(owner, title,null, owner.getMandelName(),
                             owner.getMandelData().getInfo());
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
    System.out.println("mandel frame is "+getMandelFrame());
    return new VariationView(name,(MandelInfo)info);
  }

  
  ///////////////////////////////////////////////////////////////////////
  // view
  ///////////////////////////////////////////////////////////////////////

  protected class VariationView extends CreationView 
                                implements ProportionProvider {
    private JButton namebutton;
    private JButton resetbutton;

    private MandelName base;
    private MandelInfo initial;

    public VariationView(QualifiedMandelName name, MandelInfo info)
    {
      super(name, new MandelInfo(info));
      initial=info;
      base=getMandelFrame().getMandelName();
      determineFilename();
    }

    public double getProportion()
    {
      MandelInfo info=getInfo();
      if (info!=null) {
        MandelInfo cur=MandelVariationCreationDialog.this.
                            getMandelWindowAccess().getMandelData().getInfo();
        return MandUtils.getProportion(info.getRX(), info.getRY(), cur);
      }
      return 1.0;
    }

    @Override
    protected void _setRect(VisibleRect rect)
    {
      super._setRect(rect);
      if (rect!=null) {
        rect.setProportionProvider(this);
      }
    }

    
    @Override
    protected void setupButtons()
    {
      super.setupButtons();
      namebutton=createButton("Name", "Determine variation name",
                   new NameAction());
      addShowButton("Show variation area", false);
      resetbutton=createButton("Reset", "Reset to initial values",
                   new ResetAction());
    }

    @Override
    public void setFilename(String n, boolean modifiable)
    {
      super.setFilename(n,modifiable);
      namebutton.setEnabled(modifiable);
    }

    protected void determineFilename()
    {
      MandelFileName mfn=new MandelFileName(base,
                            this.getInfo().getRX()+"x"+this.getInfo().getRY(),
                            MandelConstants.INFO_SUFFIX);
      File path=getEnvironment().getInfoFolder(null);
      if (path==null) path=new File(".");
      setFilename(new File(path,mfn.toString()).getPath());
    }

    private class ResetAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        setInfo(initial);
        updateSlave();
      }
    }

    private class NameAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        determineFilename();
      }
    }

    @Override
     protected String getRectLabel()
    {
      return getTitle();
    }
  }
}
