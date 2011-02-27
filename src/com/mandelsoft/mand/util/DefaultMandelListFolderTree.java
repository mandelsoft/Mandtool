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

package com.mandelsoft.mand.util;

import com.mandelsoft.mand.QualifiedMandelName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Uwe Krueger
 */
public class DefaultMandelListFolderTree implements MandelListFolderTree {
  static public boolean debug;

  private MandelListFolder root;


  public DefaultMandelListFolderTree(String name)
  {
    root=createRootFolder(name);
  }

  protected DefaultMandelListFolderTree(MandelListFolder root)
  {
    this.root=root;
  }

  protected MandelListFolder createRootFolder(String name)
  {
    return new NestedMandelListFolder(this,name);
  }

  public MandelListFolder getRoot()
  {
    return root;
  }

  public boolean valid()
  {
    return true;
  }

  public void clear()
  {
    MandelListFolder f=getRoot();
    MandelList       l=f.getMandelList();
    f.clear();
    if (l!=null) l.clear();
  }

  public void refresh()
  {
  }

  public void save() throws IOException
  {
    new UnsupportedOperationException("save of tree");
  }

  public void save(File f) throws IOException
  {
    write(new FileOutputStream(f),f.getPath());
  }

  public void read(InputStream is, String src) throws IOException
  {
    IO.read(this, is, src);
  }

  public final void write(OutputStream os, String dst) throws IOException
  {
    write(os,null,dst);
  }

  public void write(OutputStream os, IO.Modifier m, String dst) throws IOException
  {
    IO.write(this, m, os, dst);
  }

   /////////////////////////////////////////////////////////////
  // main
  /////////////////////////////////////////////////////////////

  public static void print(String gap, MandelListFolder f)
  {
    System.out.println(gap+"-> "+f.getName());
    gap+="  ";
    for (QualifiedMandelName n:f.getMandelList()) {
      System.out.println(gap+n);
    }
    for (MandelListFolder s:f) {
      print(gap,s);
    }
  }


}
