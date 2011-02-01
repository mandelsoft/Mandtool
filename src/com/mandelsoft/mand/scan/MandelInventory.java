
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
package com.mandelsoft.mand.scan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import com.mandelsoft.mand.MandelConstants;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.scan.MandelInventory.Entry;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Uwe Krüger
 */
public class MandelInventory implements MandelConstants, Iterable<Entry> {

  static private final int MAGIC=0x763198;

  public static class Entry {
    private String     filename;
    private int        flags;
    private MandelInfo info;
    private long       lastmod;

    public Entry(String filename, int flags, MandelInfo info, long lm)
    {
      this.filename=filename;
      this.flags=flags;
      this.info=info;
      this.lastmod=lm;
    }

    public String getFilename()
    {
      return filename;
    }

    public MandelInfo getInfo()
    {
      return info;
    }

    public int getType()
    {
      return flags&~M_META;
    }

    public int getCacheType()
    {
      return flags&M_META;
    }

    public long getLastModified()
    {
      return lastmod;
    }
  }

  //////////////////////////////////////////////////////////////////////////

  public MandelInventory()
  {
  }

  public void read(InputStream is, String src) throws IOException
  {
    DataInputStream dis=new DataInputStream(new BufferedInputStream(is));
    try {
      read(dis,src);
    }
    finally {
      dis.close();
    }
  }

  public void write(OutputStream os, String dest) throws IOException
  {
    DataOutputStream dos=new DataOutputStream(new BufferedOutputStream(os));
    try {
      write(dos, dest);
    }
    finally {
      dos.close();
    }
  }

  //////////////////////////////////////////////////////////////////////////

  protected HashMap<String, Entry> cache=new HashMap<String, Entry>();

  public boolean remove(String name)
  {
    return cache.remove(name)!=null;
  }

  public Entry get(String name)
  {
    return cache.get(name);
  }

  public Entry add(String filename, MandelData md, long modified)
  {
    MandelHeader h=md.getOrigHeader();
    //System.out.println("add file type "+h.getTypeDesc());
    return add(filename, h.getType(), md.getInfo(), modified);
  }

  public Entry add(String filename, int flags, MandelInfo info, long lm)
  { 
    Entry e=new Entry(filename, flags, info, lm);
    cache.put(filename, e);
    return e;
  }

  public Iterator<Entry> iterator()
  {
    return cache.values().iterator();
  }

  private void read(DataInputStream dis, String msg) throws IOException
  {
    System.out.println("reading cache ... ("+msg+")");
    int magic=dis.readInt();
    if (magic!=MAGIC) throw new IOException("illegal format "+magic+"!="+MAGIC);

    while (true) {
      int flags;
      MandelInfo info=null;
      String filename;
      long lm;

      try {
        flags=dis.readInt();
      }
      catch (EOFException eof) {
        dis.close();
        //System.out.println("done.");
        return;
      }
      filename=dis.readUTF();
      lm=dis.readLong();
      if ((flags&M_INFOOMITTED)==0) {
        if ((flags&C_INFO)!=0) {
          info=new MandelInfo();
          info.read(dis, false);
        }
      }
      cache.put(filename, new Entry(filename, flags, info, lm));
    }
  }

  private void write(DataOutputStream dos, String msg) throws IOException
  {
    System.out.println("writing cache ...("+msg+")");
    dos.writeInt(MAGIC);

    for (Entry e:this) {
      if (e.info==null && (e.flags&C_INFO)!=0) {
        e.flags|=M_INFOOMITTED;
      }
      dos.writeInt(e.flags);
      dos.writeUTF(e.filename);
      dos.writeLong(e.getLastModified());
      if ((e.flags&C_INFO)!=0 && (e.flags&M_INFOOMITTED)==0) {
        e.info.write(dos,false);
      }
    }
    System.out.println("done.");
  }
}
