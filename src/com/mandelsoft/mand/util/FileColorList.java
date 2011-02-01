
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
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 *
 * @author Uwe Krüger
 */

public class FileColorList extends ArrayColorList {
  private AbstractFile file;
  
  public FileColorList(File f)
  {   
    setup(new FileAbstractFile(f));
  }

  public FileColorList(AbstractFile f)
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
          Color color=parseColor(line);
          add(color);
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

  protected Color parseColor(String line) throws IOException
  {
    StringTokenizer t=new StringTokenizer(line,",");
    String s;
    int r,g,b;

    try {
      if (t.hasMoreTokens()) {
        s=t.nextToken();
        r=Integer.parseInt(s.trim());
        if (t.hasMoreTokens()) {
          s=t.nextToken();
          g=Integer.parseInt(s.trim());
          if (t.hasMoreTokens()) {
            s=t.nextToken();
            b=Integer.parseInt(s.trim());
            if (!t.hasMoreTokens()) return new Color(r, g, b);
          }
        }
      }
    }
    catch (Exception e) {
      throw new IOException("cannot parse line: "+line+": "+e,e);
    }
    throw new IOException("cannot parse line: "+line);
  }

  @Override
  public void save() throws IOException
  {
    if (!file.isFile()) throw new UnsupportedOperationException("save on URL");
    PrintWriter w=new PrintWriter(new BufferedWriter(new FileWriter(file.getFile())));
    try {
      for (Color c: this) {
        w.println(c.getRed()+", "+c.getGreen()+", "+c.getBlue());
      }
    }
    finally {
      w.close();
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // main
  ////////////////////////////////////////////////////////////////////////

  static public void main(String[] args)
  {
    ColorList list=new FileColorList(new File("C:/work/AccuRev/test/Mandel/colors"));
    for (Color c:list) {
      System.out.println("  "+c);
    }
    try {
      list.add(Color.ORANGE);
      list.save();
    }
    catch (IOException io) {
      System.out.println("cannot save list: "+io);
    }
  }
}
