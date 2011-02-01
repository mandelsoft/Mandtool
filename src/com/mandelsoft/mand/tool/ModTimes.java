
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

import java.awt.Window;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;

/**
 *
 * @author Uwe Krüger
 */

public class ModTimes extends MandelSpecDialog<ModTimes.ModPanel>  {

  static private DateFormat datetime=
            new SimpleDateFormat("dd.MM.yyyy HH:mm.ss z");
  static private DefaultFormatter textfmt=new DefaultFormatter();

  public ModTimes(Window parent, String name)
  {
    super(parent,"Modification Times",name, false);
  }

  @Override
  protected ModPanel createPanel(String name, boolean change)
  {
    return new ModPanel(name);
  }



  public class ModPanel extends MandelSpecDialog.Panel {
    private JLabel label;
    private JFormattedTextField modtime;
    private JFormattedTextField infotime;
    private JFormattedTextField rastertime;
    private JFormattedTextField imagetime;

    public ModPanel(String name)
    {
      super(name,false);
      modtime=createTextField("File modification time",1);
      infotime=createTextField("Area creation time",2);
      rastertime=createTextField("Raster creation time",3);
      imagetime=createTextField("Image creation time",4);
      addBorder(0,1,2,4);
    }

    @Override
    public void setData(MandelData data)
    {
      MandelInfo info=data.getInfo();
      super.setInfo(info);
      if (data.getFile()!=null) {
        setValue(modtime,data.getFile().getLastModified());
      }
      else {
        setValue(modtime,0);
      }
    }

    @Override
    protected void _setInfo(MandelInfo info)
    {
      setValue(infotime,info.getCreationTime());
      setValue(rastertime,info.getRasterCreationTime());
      setValue(imagetime,info.getImageCreationTime());
    }

    private void setValue(JFormattedTextField field, long time)
    {
      if (time>0) {
        field.setEnabled(true);
        AbstractFormatterFactory f=new DefaultFormatterFactory(new DateFormatter
                                               (datetime));
        field.setFormatterFactory(f);
        field.setValue(new Date(time));
      }
      else {
        field.setEnabled(false);
        field.setFormatterFactory(new DefaultFormatterFactory(textfmt));
        field.setValue("Not set");
      }
    }
  }
}
