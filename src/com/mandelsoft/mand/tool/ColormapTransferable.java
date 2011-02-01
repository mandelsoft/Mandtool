
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

import com.mandelsoft.mand.ColormapName;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.scan.ColormapHandle;
import com.mandelsoft.mand.util.ColormapList;

/**
 *
 * @author Uwe Krüger
 */

public class ColormapTransferable implements Transferable {

  public static final String colormapType=DataFlavor.javaJVMLocalObjectMimeType+
          ";class="+ColormapTransferable.class.getName();
  public static final DataFlavor colormapFlavor;


  static {
    try {
      colormapFlavor=new DataFlavor(colormapType);
    }
    catch (ClassNotFoundException ex) {
      System.err.println("illegal drag and drop class: "+ex);
      throw new IllegalArgumentException("illegal drag and drop class",ex);
    }
  }

  //////////////////////////////////////////////////////////////////////////

  static public class ColormapInfo {
    ColormapName   name;
    Colormap       colormap;
    ColormapHandle handle;

    public ColormapInfo(ColormapName name, Colormap colormap, ColormapHandle handle)
    {
      this.name=name;
      this.colormap=colormap;
      this.handle=handle;
    }

    public Colormap getColormap()
    {
      return colormap;
    }

    public ColormapHandle getColormapHandle()
    {
      return handle;
    }

    public ColormapName getName()
    {
      return name;
    }

    @Override
    public String toString()
    {
      if (handle==null || handle.getFile()==null) return getName().getName();
      return handle.getFile().toString();
    }
  }
  //////////////////////////////////////////////////////////////////////////

  private ColormapInfo      colormaps[];
  private ColormapList      source;

  public ColormapTransferable(ColormapInfo[] colormaps)
  {
    this.colormaps=colormaps;
  }

  public ColormapTransferable(ColormapList src, ColormapInfo[] colormaps)
  {
    this(colormaps);
    this.source=src;
  }

  public ColormapInfo[] getColormaps()
  {
    return colormaps;
  }

  public ColormapList getSource()
  {
    return source;
  }


  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    if (flavor.equals(colormapFlavor)) {
      return this;
    }
    if (flavor.equals(DataFlavor.stringFlavor)) {
      StringBuffer sb=new StringBuffer();
      String sep="";
      for (ColormapInfo n:colormaps) {
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
    flavors[0]=colormapFlavor;
    flavors[1]=DataFlavor.stringFlavor;
    //System.out.println("mandel query flavors");
    return flavors;
  }
  
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    //System.out.println("mandel check "+flavor);
    return flavor.equals(colormapFlavor) || flavor.equals(DataFlavor.stringFlavor);
  }
}
