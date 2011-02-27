
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
import java.io.BufferedReader;
import java.io.File;
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

public interface MandelListFolderTree {
  public MandelListFolder getRoot();

  boolean valid();
  void refresh();
  void clear();
  void save() throws IOException;
  void save(File f) throws IOException;

  void read(InputStream is, String src) throws IOException;
  void write(OutputStream os, String dst) throws IOException;

  /**
   * reading and writing of MandelListFolderTrees
   */
  public static class IO {
    public interface Modifier {
      void                writeHeader(PrintWriter pw);
      QualifiedMandelName getThumbnailName(MandelListFolder f);
      Iterable<String>    propertyNames(MandelListFolder f);
      String              getProperty(MandelListFolder f, String name);
      MandelList          getMandelList(MandelListFolder f);
      Iterable<MandelListFolder> folders(MandelListFolder f);
    }

    public static void read(MandelListFolderTree tree,
                            InputStream is, String src) throws IOException
    {
      BufferedReader br=new BufferedReader(new InputStreamReader(is));
      try {
        tree.clear();
        readContent(tree.getRoot(), br);
      }
      finally {
        br.close();
      }
    }

    public static class ModifierAdapter implements Modifier {

      public void writeHeader(PrintWriter pw)
      {
      }

      public QualifiedMandelName getThumbnailName(MandelListFolder f)
      {
        return f.getThumbnailName();
      }

      public Iterable<String> propertyNames(MandelListFolder f)
      {
        return f.propertyNames();
      }

      public String getProperty(MandelListFolder f, String name)
      {
        return f.getProperty(name);
      }

      public MandelList getMandelList(MandelListFolder f)
      {
        return f.getMandelList();
      }

      public Iterable<MandelListFolder> folders(MandelListFolder f)
      {
        return f;
      }
    }

    static public final Modifier std=new ModifierAdapter();

    ////////////////////////////////////////////////////////////////////////
    // IO routines
    ////////////////////////////////////////////////////////////////////////

    public static void write(MandelListFolderTree tree,
                             OutputStream os, String dst) throws IOException
    {
      write(tree,std,os,dst);
    }

    public static void write(MandelListFolderTree tree, Modifier m,
                             OutputStream os, String dst) throws IOException
    {
      PrintWriter pw=new PrintWriter(new OutputStreamWriter(os));
      if (m==null) m=std;
      try {
        pw.println("#$ StandardTreeFormat");
        m.writeHeader(pw);
        writeContent("", m, tree.getRoot(), pw);
      }
      finally {
        pw.close();
      }
    }

    private static void writeContent(String gap, Modifier m, MandelListFolder f,
                                     PrintWriter w)
    {
      String ngap=gap+"  ";
      for (String name:m.propertyNames(f)) {
        w.println(gap+name+"="+m.getProperty(f,name));
      }
      if (f.hasMandelList()) {
        for (QualifiedMandelName name:m.getMandelList(f)) {
          w.println(gap+name);
        }
      }
      for (MandelListFolder folder:m.folders(f)) {
        String t="";
        if (m.getThumbnailName(folder)!=null) {
          t=" ["+folder.getThumbnailName()+"]";
        }
        w.println(gap+folder.getName()+t+" {");
        writeContent(ngap, m, folder, w);
        w.println(gap+"}");
      }
    }

    private static boolean readContent(MandelListFolder folder, BufferedReader r) throws IOException
    {
      String line;
      MandelList list=folder.getMandelList();

      while ((line=r.readLine())!=null) {
        line=line.trim();
        if (line.startsWith("#")||line.length()==0) continue;
        int ix=line.indexOf('=');
        if (line.endsWith("{")) {
          String n=line.substring(0, line.length()-1).trim();
          String t=null;
          //System.out.println("line: "+n);
          if (n.endsWith("]")) {
            ix=n.indexOf("[");
            if (ix>0) {
              t=n.substring(ix+1, n.length()-1).trim();
              n=n.substring(0, ix).trim();
              //System.out.println("thumb "+t);
            }
            else {
              throw new IOException("illegal sub folder name syntax");
            }
          }
          //System.out.println("->"+n);
          MandelListFolder f=folder.createSubFolder(n);
          if (t!=null) f.setThumbnailName(QualifiedMandelName.create(t));
          if (!readContent(f, r)) {
            throw new IOException("illegal sub folder syntax");
          }
        }
        else if (line.equals("}")) return true;
        else if (ix>0) {
          String name=line.substring(0, ix).trim();
          String value=line.substring(ix+1).trim();
          folder.setProperty(name, value);
        }
        else {
          QualifiedMandelName name=QualifiedMandelName.create(line);
          //System.out.println("put entry "+name+" into "+getMandelName());
          list.add(name);
        }
      }
      return false;
    }
  }
}
