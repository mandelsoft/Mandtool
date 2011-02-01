
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
import com.mandelsoft.io.AbstractFile;
import java.io.File;
import java.io.IOException;


/**
 *
 * @author Uwe Krüger
 */

public class FileMandelListFolderTree extends DefaultMandelListFolderTree {
  static public boolean debug;

  private AbstractFile file;
  

  public FileMandelListFolderTree(File f)
  {
    this(f.getName(),new FileAbstractFile(f));
  }

  public FileMandelListFolderTree(AbstractFile f)
  {
    this(f.getName(),f);
  }

  
  public FileMandelListFolderTree(String name, AbstractFile f)
  {
    super(name);
    setup(f);
  }

  protected FileMandelListFolderTree(MandelListFolder root, AbstractFile f)
  {
    super(root);
    setup(f);
  }

  private void setup(AbstractFile f)
  {
    this.file=f;
    refresh();
  }

  @Override
  public boolean valid()
  {
    if (file.isFile()) {
      return file.getFile().isFile();
    }
    return file.getLastModified()!=0;
  }

  
  @Override
  public void refresh()
  {
    if (valid()) try {
      read(file.getInputStream(),file.toString());
    }
    catch (IOException ex) {
    }
  }
  
  @Override
  public void save() throws IOException
  {
    if (!file.isFile()) throw new UnsupportedOperationException("save on URL");
    save(file.getFile());
  }

  public AbstractFile getFile()
  {
    return file;
  }
  
  /////////////////////////////////////////////////////////////
  // main
  /////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    File file = new File(args[0]);
    System.out.println("file "+file);
    FileMandelListFolderTree f=new FileMandelListFolderTree(file);
    print("",f.getRoot());
    if (args.length>1) {
      try {
        f.save(new File(args[1]));
      }
      catch (IOException ex) {
        System.out.println(ex);
      }
    }
  }
}
