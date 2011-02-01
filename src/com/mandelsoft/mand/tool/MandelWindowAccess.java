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
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.JPopupMenu;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.tool.mapper.MapperModel;

/**
 *
 * @author Uwe Krüger
 */

public interface MandelWindowAccess {
  Window               getMandelWindow();
  ToolEnvironment      getEnvironment();
  MandelName           getMandelName();
  QualifiedMandelName  getQualifiedName();
  MandelData           getMandelData();
  MandelImage          getMandelImage();
  MapperModel          getMapperModel();
  ColormapModel        getColormapModel();
  
  MandelImagePanel     getMandelImagePane();
  History              getHistory();

  static class Access {

    public static MandelWindowAccess getMandelWindowAccess(Component leaf)
    { Component c=leaf;

      while (c!=null) {
        if (c!=leaf) {
          if (c instanceof MandelWindowAccessSource) {
            return ((MandelWindowAccessSource)c).getMandelWindowAccess();
          }
        }
        if (c instanceof MandelWindowAccess) return (MandelWindowAccess)c;
        if (c instanceof JPopupMenu) {
          c=((JPopupMenu)c).getInvoker();
        }
        else {
          if (c instanceof Dialog) {
            c=((Dialog)c).getOwner();
          }
          else {
            c=c.getParent();
          }
        }
      }
      return null;

//      while (!(c instanceof Window)&&!(c instanceof MandelWindowAccess)
//              &&(c==leaf||!(c instanceof MandelWindowAccessSource))
//              &&c.getParent()!=null) {
//        if (c instanceof JPopupMenu) {
//          c=((JPopupMenu)c).getInvoker();
//        }
//        else {
//          c=c.getParent();
//        }
//      }
//
//      if (c==null) return null;
//      if (c instanceof MandelWindowAccessSource)
//        return ((MandelWindowAccessSource)c).getMandelWindowAccess();
//      if (c instanceof MandelWindowAccess) return (MandelWindowAccess)c;
//      if (c instanceof Dialog) c=((Dialog)c).getOwner();
//      if (c instanceof MandelWindowAccessSource)
//        return ((MandelWindowAccessSource)c).getMandelWindowAccess();
//      if (c instanceof MandelWindowAccess) return (MandelWindowAccess)c;
//      return null;
    }
  }
}
