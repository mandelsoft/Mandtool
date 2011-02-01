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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author Uwe Krüger
 */

public class FileTagList extends ArrayTagList {
  private AbstractFile file;
  
  public FileTagList(File f)
  {   
    setup(new FileAbstractFile(f));
  }

  public FileTagList(AbstractFile f)
  {
    setup(f);
  }

  protected void setup(AbstractFile f)
  {
    this.file=f;
    refresh(true);
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
    clear();
    if (valid()) try {
      String line;
      BufferedReader r=new BufferedReader(new InputStreamReader(file.getInputStream()));
      try {
        while ((line=r.readLine())!=null) {
          line=line.trim();
          if (line.startsWith("#")||line.length()==0) continue;
          add(line);
        }
      }
      finally {
        r.close();
      }
    }
    catch (IOException ex) {
      System.out.println("ERROR in "+file+": "+ex);
    }
  }

  @Override
  public void save() throws IOException
  {
    if (!file.isFile()) throw new UnsupportedOperationException("save on URL");
    PrintWriter w=new PrintWriter(new BufferedWriter(new FileWriter(file.getFile())));
    try {
      for (String c: this) {
        w.println(c);
      }
    }
    finally {
      w.close();
    }
  }
}
