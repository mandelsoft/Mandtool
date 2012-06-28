
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
import com.mandelsoft.util.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 *
 * @author Uwe Krüger
 */

public interface MandelList extends BaseList<QualifiedMandelName> {
  void write(OutputStream os, String dst) throws IOException;
  void read(InputStream is, String src) throws IOException ;
  QualifiedMandelName get(MandelName n);

  public static class IO {

    public static void write(MandelList ml, OutputStream os, String msg) throws IOException
    {
      System.out.println("write "+msg+" "+Utils.sizeString(ml.size(),"entry"));
      PrintWriter w=new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(os)));
      try {
        for (QualifiedMandelName name :ml) {
          w.println(name);
        }
      }
      finally {
        w.close();
      }
    }

    public static void read(MandelList ml, InputStream is, String msg)
                            throws IOException
    {
      String line;
      BufferedReader r=new BufferedReader(new InputStreamReader(is));
      try {
        ml.clear();
        while ((line=r.readLine())!=null) {
          line=line.trim();
          if (line.startsWith("#")||line.length()==0) continue;
          QualifiedMandelName name=QualifiedMandelName.create(line);
          ml.add(name);
        }
      }
      finally {
        r.close();
      }
      System.out.println("reading "+msg+" "+Utils.sizeString(ml.size(),"entry"));
    }
  }
}
