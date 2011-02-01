
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;

/**
 *
 * @author Uwe Krüger
 */

public class MandelFolderTransferable implements Transferable {
  public static final String folderType=DataFlavor.javaJVMLocalObjectMimeType+
          ";class="+MandelFolderTransferable.class.getName();
  public static final DataFlavor folderFlavor;


  static {
    try {
      folderFlavor=new DataFlavor(folderType);
    }
    catch (ClassNotFoundException ex) {
      System.out.println("illegal drag and drop class: "+ex);
      throw new IllegalArgumentException("illegal drag and drop class",ex);
    }
  }

  //////////////////////////////////////////////////////////////////////////

  private MandelListFolder folders[];
  private MandelListFolderTree source;

  public MandelFolderTransferable(MandelListFolderTree source,
                                  MandelListFolder[] folders)
  {
    System.out.println("create folder transfer");
    this.folders=folders;
    this.source=source;
  }

  public MandelListFolderTree getSource()
  {
    return source;
  }
  
  public MandelListFolder[] getFolders()
  {
    return folders;
  }

  private void add(List<QualifiedMandelName> list, MandelListFolder f)
  {
    list.addAll(f.getMandelList());
    for (MandelListFolder s:f) {
      add(list,s);
    }
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    if (flavor.equals(folderFlavor)) {
      return this;
    }
    if (flavor.equals(MandelTransferable.mandelFlavor)) {
      List<QualifiedMandelName> list=new ArrayList<QualifiedMandelName>();
      for (MandelListFolder f:folders) {
        add(list,f);
      }
      return new MandelTransferable(
                     list.toArray(new QualifiedMandelName[list.size()]));
    }
    if (flavor.equals(DataFlavor.stringFlavor)) {
      StringBuffer sb=new StringBuffer();
      String sep="";
      for (MandelListFolder f:folders) {
        sb.append(sep);
        sb.append(f.getName());
        sep="\n";
      }
      return sb.toString();
    }
    throw new UnsupportedFlavorException(flavor);
  }

  public DataFlavor[] getTransferDataFlavors()
  { DataFlavor[] flavors=new DataFlavor[3];
    flavors[0]=folderFlavor;
    flavors[1]=MandelTransferable.mandelFlavor;
    flavors[2]=DataFlavor.stringFlavor;
    return flavors;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    //System.out.println("folder check "+flavor);
    return flavor.equals(folderFlavor) || 
           flavor.equals(DataFlavor.stringFlavor) ||
           flavor.equals(MandelTransferable.mandelFlavor);
  }

  
}
