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

import com.mandelsoft.io.FileAbstractFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Uwe Krüger
 */

public class LeafMandelListFolderTree implements MandelListFolderTree {

  private MandelListFolder root;
  private MandelList list;

  public LeafMandelListFolderTree(String name, MandelList list)
  {
    this.list=list;
    this.root=new LeafMandelListFolder(this, name,list);
  }

  public MandelListFolder getRoot()
  {
    return root;
  }

  public void clear()
  {
    list.clear();
  }
  
  public void refresh()
  {
    list.refresh(false);
  }

  public void read(InputStream is, String src) throws IOException
  {
    if (src==null) src=getRoot().getPath();
    list.read(is, src);
  }

  public void write(OutputStream os, String dst) throws IOException
  {
    if (dst==null) dst=getRoot().getPath();
    list.write(os,dst);
  }

  public void save() throws IOException
  {
    list.save();
  }

  public void save(File f) throws IOException
  {
    FileMandelList fl=new FileMandelList(new FileAbstractFile(f),list);
    fl.save();
  }

  public boolean valid()
  {
    return list.valid();
  }
}
