
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

package com.mandelsoft.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.FileLock;

/**
 *
 * @author Uwe Krüger
 */

public class FileAbstractFile extends AbstractFileSupport {
  private File file;
  private RandomAccessFile access;
  private FileLock lock;

  public FileAbstractFile(File file)
  {
    try {
      this.file=file.getCanonicalFile();
    }
    catch (IOException ex) {
     this.file=file;
    }
  }

  public boolean isFile()
  {
    return true;
  }

  public Proxy getProxy()
  {
    return null;
  }

  public AbstractFile getParent()
  {
    return new FileAbstractFile(getFile().getParentFile());
  }

  public AbstractFile getSub(String name)
  {
    return new FileAbstractFile(new File(getFile(),name));
  }

  public InputStream getInputStream() throws IOException
  {
    return new BufferedInputStream(new FileInputStream(file));
  }

  public long getLastModified()
  {
    return _getLastModified();
  }
  
  protected long _getLastModified()
  {
    return file.lastModified();
  }

  public URL getURL()
  {
    try {
      return file.toURI().toURL();
    }
    catch (MalformedURLException ex) {
     return null;
    }
  }

  public File getFile()
  {
    return file;
  }

  public String getPath()
  {
    return file.getPath();
  }

  public String getName()
  {
    return file.getName();
  }

  private void assertAccess() throws FileNotFoundException
  {
    if (access==null) {
      if (!file.exists()) {
        throw new FileNotFoundException(file.toString());
      }
      access=new RandomAccessFile(file,"rw");
      if (file.length()==0) {
        try {
          access.close();
          access=null;
        }
        catch (IOException io) {
        }
        finally {
          throw new FileNotFoundException(file.toString());
        }
      }
    }
  }

  private void releaseAccess() throws IOException
  {
    if (access!=null) {
      access.close();
      access=null;
    }
  }

  public boolean lock() throws IOException
  {
    assertAccess();
    access.getChannel().lock(Long.MAX_VALUE-1,1,false);
    return true;
  }

  public boolean tryLock() throws IOException
  {
    assertAccess();
    lock=access.getChannel().tryLock(Long.MAX_VALUE-1,1,false);
    if (lock==null) {
      releaseAccess();
      return false;
    }
    return true;
  }

  public void releaseLock() throws IOException
  {
    if (lock!=null) {
      lock.release();
      lock=null;
      releaseAccess();
    }
  }

  @Override
  public String toString()
  {
    return file.toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final FileAbstractFile other=(FileAbstractFile)obj;
    if (this.file!=other.file&&(this.file==null||!this.file.equals(other.file)))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=5;
    hash=23*hash+(this.file!=null?this.file.hashCode():0);
    return hash;
  }
}
