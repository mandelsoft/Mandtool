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
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */

public class MandelTransferable implements Transferable {

  public static final String mandelType=DataFlavor.javaJVMLocalObjectMimeType+
          ";class="+MandelTransferable.class.getName();
  public static final DataFlavor mandelFlavor;


  static {
    try {
      mandelFlavor=new DataFlavor(mandelType);
    }
    catch (ClassNotFoundException ex) {
      System.out.println("illegal drag and drop class: "+ex);
      throw new IllegalArgumentException("illegal drag and drop class",ex);
    }
  }

  //////////////////////////////////////////////////////////////////////////

  private QualifiedMandelName names[];
  private MandelList          source;

  public MandelTransferable(QualifiedMandelName[] names)
  {
    this.names=names;
  }

  public MandelTransferable(MandelList src, QualifiedMandelName[] names)
  {
    this(names);
    this.source=src;
  }

  public QualifiedMandelName[] getNames()
  {
    return names;
  }

  public MandelList getSource()
  {
    return source;
  }


  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    if (flavor.equals(mandelFlavor)) {
      return this;
    }
    if (flavor.equals(DataFlavor.stringFlavor)) {
      StringBuilder sb=new StringBuilder();
      String sep="";
      for (QualifiedMandelName n:names) {
        sb.append(sep);
        sb.append(n.toString());
        sep="\n";
      }
      return sb.toString();
    }
    throw new UnsupportedFlavorException(flavor);
  }

  public DataFlavor[] getTransferDataFlavors()
  { DataFlavor[] flavors=new DataFlavor[2];
    flavors[0]=mandelFlavor;
    flavors[1]=DataFlavor.stringFlavor;
    //System.out.println("mandel query flavors");
    return flavors;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    //System.out.println("mandel check "+flavor);
    return flavor.equals(mandelFlavor) || flavor.equals(DataFlavor.stringFlavor);
  }
}
