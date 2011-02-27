
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

import com.mandelsoft.mand.*;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Uwe Krüger
 */

public class NestedMandelListFolder extends ArrayMandelListFolder {
  private MandelListFolderTree tree;

  public NestedMandelListFolder(MandelListFolderTree tree, String name)
  { super(name);
    this.tree=tree;
    //System.out.println("created "+this);
  }
  
  @Override
  protected MandelList createMandelList()
  {
    return new FolderMandelList();
  }

  public MandelListFolderTree getMandelListFolderTree()
  {
    return tree;
  }

  @Override
  public void refresh(boolean soft)
  {
    getMandelListFolderTree().refresh();
  }

  @Override
  public void save() throws IOException
  {
    getMandelListFolderTree().save();
  }

  @Override
  public boolean valid()
  {
    if (getParent()!=null) return getParent().valid();
    else return super.valid();
  }

  public NestedMandelListFolder createSubFolder(String name)
  {
    NestedMandelListFolder f=createNestedFolder(name);
    super.add(f);
    return f;
  }

  public NestedMandelListFolder createSubFolder(int index, String name)
  {
    NestedMandelListFolder f=createNestedFolder(name);
    if (index<0) index=this.size();
    super.add(index,f);
    return f;
  }

  protected NestedMandelListFolder createNestedFolder(String name)
  {
    return new NestedMandelListFolder(getMandelListFolderTree(), name);
  }

  //////////////////////////////////////////////////////////////
  // assure consistency
  //////////////////////////////////////////////////////////////
  
  // base class maps all other add method flavors to one of those two
  
  @Override
  public boolean add(MandelListFolder f)
  {
    if (!(f instanceof NestedMandelListFolder))
      throw new IllegalArgumentException("illegal folder type "+f.getClass());
    return super.add(f);
  }

  @Override
  public void add(int index, MandelListFolder f)
  {
    if (!(f instanceof NestedMandelListFolder))
      throw new IllegalArgumentException("illegal folder type "+f.getClass());
    super.add(index, f);
  }
 
  //////////////////////////////////////////////////////////////
  // MandelList
  //////////////////////////////////////////////////////////////

  private class FolderMandelList extends UniqueArrayMandelList {

    @Override
    public void refresh(boolean soft)
    {
      NestedMandelListFolder.this.refresh(soft);
    }

    @Override
    public void save() throws IOException
    {
      NestedMandelListFolder.this.save();
    }

    @Override
    public boolean valid()
    {
      return NestedMandelListFolder.this.valid();
    }
  }
}
