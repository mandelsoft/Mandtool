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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JButton;
import com.mandelsoft.mand.MandelConstants;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.tool.MandelAreaCreationDialog.CreationView;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEvent;
import com.mandelsoft.swing.BufferedComponent.RectModifiedEventListener;
import com.mandelsoft.swing.BufferedComponent.VisibleRect;

/**
 *
 * @author Uwe Krueger
 */
public class MandelVariationCreationDialog extends MandelAreaCreationDialog {

  public MandelVariationCreationDialog(MandelWindowAccess owner, String title)
  {
    super(owner, title,null, owner.getMandelName(),
                             owner.getMandelData().getInfo());
    getDialog().addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e)
      {
        handleClose();
      }
      @Override
      public void windowClosed(WindowEvent e)
      {
        System.out.println("closed variant area");
      }
    });
  }

  @Override
  public void setVisible(boolean b)
  {
    super.setVisible(b);
    if (!b) handleClose();
  }

  protected void handleClose()
  {
    ((SubAreaView)getView()).handleClose();
  }

  @Override
  protected MandelAreaView createView(QualifiedMandelName name, Object info,
                                      boolean change, boolean readonly)
  {
    System.out.println("mandel frame is "+getMandelFrame());
    return new SubAreaView(name,(MandelInfo)info);
  }

  
  ///////////////////////////////////////////////////////////////////////
  // view
  ///////////////////////////////////////////////////////////////////////

  protected class SubAreaView extends CreationView {
    private VisibleRect rect;

    private JButton namebutton;
    private JButton showbutton;
    private JButton resetbutton;

    private MandelName base;
    private MandelInfo initial;

    public SubAreaView(QualifiedMandelName name, MandelInfo info)
    {
      super(name, new MandelInfo(info));
      initial=info;
      base=getMandelFrame().getMandelName();
      determineFilename();
    }

    protected void handleClose()
    {
      System.out.println("closing variant area");
      if (rect!=null) {
        System.out.println(" discard old rect");
        rect.discard();
        if (rect.getName()!=null) {
          fireMandelAreaEvent(
                  new MandelAreaEvent(MandelVariationCreationDialog.this,
                                      MandelAreaEvent.MA_UPDATE));
        }
        rect=null;
      }
    }

    @Override
    protected void setupButtons()
    {
      super.setupButtons();
      namebutton=createButton("Name", "Determine variation name",
                   new NameAction());
      showbutton=createButton("Show", "Show variation area",
                   new ShowAction());
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

    private class ModifiedListener implements RectModifiedEventListener {
      public void rectModified(RectModifiedEvent e)
      {
        MandelInfo info=getInfo();
        System.out.println("info is "+info);
        updateInfo(info,rect._getRect());
        //TODO!!
//        ProportionProvider p=getMandelWindowAccess().getMandelImagePane().getProportionSelectionModel().getProportionProvider();
//        if (p!=imagepropprov) {
//          // adjust pixel size according to selected proportion
//          if (((double)info.getRX())/info.getRY()!=p.getProportion()) {
//            info.setRY((int)((double)info.getRX()/p.getProportion()));
//          }
//        }
        MandUtils.round(info);
        setInfo(info);
      }
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

    private class ShowAction implements ActionListener {

      public void actionPerformed(ActionEvent e)
      {
        if (rect==null) {
          rect=getMandelWindowAccess().getMandelImagePane().getImagePane().
                createRect(getTitle(), getTitle());
          rect.addRectModifiedEventListener(new ModifiedListener());
        }
        getMandelWindowAccess().getMandelImagePane().hideSubRects();
        rect.activate();
        updateSlave();
        rect.setVisible(true);
      }
    }

    @Override
    protected void updateSlave()
    {
      System.out.println("update slave");
      if (rect!=null) updateRect(rect,getInfo());
    }

    private void updateRect(VisibleRect rect, MandelInfo info)
    {
      getMandelWindowAccess().getMandelImagePane()
      .updateRect(rect,info);
    }

    synchronized public void updateInfo(MandelInfo info, Rectangle rect)
    {
      getMandelWindowAccess().getMandelImagePane()
      .updateInfo(info, rect);
    }
  }
}
