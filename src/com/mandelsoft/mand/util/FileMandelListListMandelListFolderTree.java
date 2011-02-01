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

import com.mandelsoft.io.AbstractFile;
import java.io.File;

/**
 *
 * @author Uwe Krueger
 */
public class FileMandelListListMandelListFolderTree extends FileMandelListFolderTree {

  public FileMandelListListMandelListFolderTree(String name, AbstractFile f)
  {
    super(name,f);
  }

  public FileMandelListListMandelListFolderTree(AbstractFile f)
  {
    this(f.getName(),f);
  }

  public FileMandelListListMandelListFolderTree(File f)
  {
    super(AbstractFile.Factory.create(f));
  }

  @Override
  protected MandelListFolder createRootFolder(String name)
  {
    return new FolderListFolder(name);
  }

  protected NestedMandelListFolder createNestedFolder(String name)
  {
    return new LeafNestedMandelListFolder(this, name);
  }

  ///////////////////////////////////////////////////////////////////////////
  
  protected class FolderListFolder extends IntermediateNestedMandelListFolder {

    public FolderListFolder(String name)
    {
      super(FileMandelListListMandelListFolderTree.this, name);
    }

    @Override
    protected NestedMandelListFolder createNestedFolder(String name)
    {
      return FileMandelListListMandelListFolderTree.this.createNestedFolder(name);
    }
  }
}
