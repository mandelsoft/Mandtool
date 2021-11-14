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

import com.mandelsoft.io.AbstractFile;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.swing.GBC;
import com.mandelsoft.swing.NumberField;
import com.mandelsoft.util.Utils;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 *
 * @author Uwe Krueger
 */
public class MandelImageAreaDialog extends MandelAreaViewDialog {
 
  public MandelImageAreaDialog(MandelWindowAccess owner, String title, String file,
                                 QualifiedMandelName name,
                                 MandelInfo info, boolean readonly)
  {
    super(owner, title, name, info, false, readonly);
    ((View)getView()).setFilename(file);
  }

  public MandelImageAreaDialog(MandelWindowAccess owner, String title,
                                 QualifiedMandelName name,
                                 MandelData data)
  { super(owner,title);
    AbstractFile f=data.getFile();
    if (f!=null) System.out.println("data file "+f+" ("+f.isFile()+")");
    setup(name, data, false, isReadonly(owner,f,name));

    if (f!=null)
      ((View)getView()).setFilename(f.toString());
  }

  @Override
  protected MandelAreaView createView(QualifiedMandelName name, Object info,
                                      boolean change, boolean readonly)
  {
    return new View(name,(MandelData)info, readonly);
  }

  public void setFilename(String file)
  {
    ((View)getView()).setFilename(file);
  }

  ///////////////////////////////////////////////////////////////////////
  // view
  ///////////////////////////////////////////////////////////////////////

  protected class View extends MandelAreaView {
    protected JTextField filename;
    protected JButton    save;

    public View(QualifiedMandelName name, MandelInfo info, boolean readonly)
    {
      super(name, info, false, readonly);
    }
    
    public View(QualifiedMandelName name, MandelData data, boolean readonly)
    {
      super(name, data, false, readonly);
    }

    public void setFilename(String n)
    {
      filename.setText(n);
    }

    @Override
    protected void setupFields()
    { NumberFormat fmt;
      NumberField field;
      MandelHeader h=null;
      MandelHeader oh=null;
      int row;

      if (data!=null) {
        h=data.getHeader();
        oh=data.getOrigHeader();
      }

      super.setupFields();

      if (data!=null) {
        row=getRow();
        createInfoField("creator", getInfo().getCreator());
        createInfoField("site", getInfo().getSite());
        addBorder(0,row,1,2);
      }
      else skipRows(2);

      setCol(1);
      row=getRow();
      
      if (data!=null) {
        if (data.isModified()) {
          createInfoField("type","Unsaved Raster");
        }
        else {
          createInfoField("type",oh.getTypeDesc());
        }
      }
      else {
        createInfoField("type","unknown");
      }
      createField("maximum iteration", "MaxIt");
      createField("minimum iteration", "MinIt");
      createInfoField("time", formatTime(getInfo().getTime()));
      createField("number of iterations", "NumIt");
      addBorder(1, row, 1, 5);
      
      if (data!=null) {
        row=this.getRow();
        if (data.getInfo().hasMandelCount()) {
          //System.out.println("row="+row);
          createField("mandel set pixels", "MCnt");
          double cov=((double)getInfo().getMCnt())/getInfo().getRX()/getInfo().
                  getRY();
          cov=((double)Math.round(cov*1000))/10;
          createInfoField("mandel set coverage", ""+cov+"%");
        }
        else skipRows(2);
        addBorder(1, row, 1, 2);

        row=getRow();
        if (h.hasImageData()) {
          String mapping="";
          if (h.hasMapping()) {
            mapping=" ("+data.getMapping().getType()+")";
          }
          if (h.hasMapper()) {
            createInfoField("mapper", data.getMapper().getName()+mapping);
            createInfoField("parameter", data.getMapper().getParamDesc());
          }
          else {
            createInfoField("mapper", "-");
            createInfoField("parameter", "-");
          }
        }
        else {
          String image="no image data";

          if (qname!=null) {
            MandelHandle i;
            i=getEnvironment().getImageDataScanner().getMandelHandle(qname);
            if (i!=null) {
              if (i.getHeader().hasRaster()) image="raster available";
              else image="image available";
            }
          }
          createInfoField("image", image);  
        }
        addBorder(1, row, 1, 2);
      }

      if (data!=null) { 
        if (h.hasImageData()) {
          row=getRow();
          createInfoField("image",h.hasRaster()?"raster":"image");
          createInfoField("colormap size",(h.hasColormap()?
                           (""+data.getColormap().getSize()):"-"));
          addBorder(1,row,1,2);
        }
      }
      
     // addBorder(1,4,1,3);

      row=getMaxGridRow();
      add(filename=new JTextField(),GBC(0,row).setSpanW(getMaxGridCol()+1).
                                               setFill(GBC.HORIZONTAL).
                                               setInsets(10,0,0,0).
                                               setWeight(200, 0));
      filename.setEditable(false);
      filename.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    @Override
    protected void setupButtons()
    {
      super.setupButtons();
      addShowButton("Show area",false);
                  
      if (!readonlyMode) {
        save=this.createButton("Save", "Save changes", new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            try {
              MandelData orig=new MandelData(data.getFile());
              if (Utils.equals(orig.getInfo().getLocation(),
                               data.getInfo().getLocation())
                      &&Utils.equals(orig.getInfo().getKeywords(),
                                     data.getInfo().getKeywords())
                      &&Utils.equals(orig.getInfo().getProperties(),
                                     data.getInfo().getProperties())) {
                mandelInfo("data not changed");
              }
              else {
                try {
                  orig.getInfo().setLocation(data.getInfo().getLocation());
                  orig.getInfo().setKeywords(data.getInfo().getKeywords());
                  orig.getInfo().setProperties(data.getInfo().getProperties());
                  orig.write();
                }
                catch (IOException ex) {
                  mandelError("cannot write "+data.getFile()+": "+ex);
                }
              }
            }
            catch (IOException ex) {
              mandelError("cannot read "+data.getFile()+": "+ex);
            }
          }
        });
      }
    }
  }

  static String formatTime(long t)
  {
    StringWriter sw=new StringWriter();
    PrintWriter pw=new PrintWriter(sw);
    pw.printf("%d:%02d:%02d", t/3600,(t/60)%60,t%60);
    return sw.toString();
  }
}
