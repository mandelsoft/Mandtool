
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
import com.mandelsoft.io.FileAbstractFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Uwe Krüger
 */

public class FileMandelList extends UniqueArrayMandelList {
  private AbstractFile file;
  private long lastModified;
  
  public FileMandelList(File f)
  { this(new FileAbstractFile(f));
  }

  public FileMandelList(AbstractFile f)
  { this.file=f;
    refresh(false);
  }

  public FileMandelList(AbstractFile f, MandelList src)
  { this.file=f;
    addAll(src);
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
  public void refresh(boolean soft)
  {
    if (!soft) try {
      if (file.isFile()) {
        if (file.getLastModified()==lastModified) return;
        lastModified=file.getLastModified();
      }
      read(file.getInputStream(),file.toString());
    }
    catch (IOException ex) {
      System.out.println("cannot read "+file+": "+ex);
    }
  }
  
  @Override
  public void save() throws IOException
  {
    if (!file.isFile()) throw new UnsupportedOperationException("save on URL");
    write(new FileOutputStream(file.getFile()),file.getPath());
  }
}
