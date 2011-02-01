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

import com.mandelsoft.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Uwe Krüger
 */

public class URLAbstractFile extends AbstractFileSupport  {
  private Proxy proxy;
  private URL url;
  private URLConnection connection;

  public URLAbstractFile(Proxy proxy, URL url)
  {
    this(proxy,url,0);
  }

  public URLAbstractFile(Proxy proxy, URL url, long lastModified)
  {
    super(lastModified);
    this.proxy=proxy;
    this.url=url;
  }

  public boolean isFile()
  {
   return false;
  }

  public URLConnection getConnection()
  {
    if (connection==null) {
      if (url!=null) {
        try {
          if (proxy==null) connection=url.openConnection();
          else connection=url.openConnection(proxy);
          
          if (connection!=null) lastModified=connection.getLastModified();
        }
        catch (IOException ex) {
          System.err.println("cannot open connection "+url);
        }
      }
    }
    try {
      return connection;
    }
    finally {
      connection=null;
    }
  }

  protected long _getLastModified()
  {
     connection=getConnection(); // keep connection
     return lastModified;
  }

  public InputStream getInputStream() throws IOException
  {
    return getConnection().getInputStream();
  }

  public AbstractFile getParent()
  {
    try {
      return new URLAbstractFile(getProxy(),
                               Utils.parentURL(getURL()));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public AbstractFile getSub(String name)
  {
    try {
      return new URLAbstractFile(getProxy(),
                               Utils.subURL(getURL(),name));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public URL getURL()
  {
    return url;
  }

  public Proxy getProxy()
  {
    return proxy;
  }

  public File getFile()
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public String getPath()
  {
    return url.getPath();
  }

  public String getName()
  {
    return new File(url.getPath()).getName();
  }

  public boolean lock()
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public boolean tryLock()
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public void releaseLock() throws IOException
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public String toString()
  {
    return url.toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final URLAbstractFile other=(URLAbstractFile)obj;
    if (this.url!=other.url&&(this.url==null||!this.url.equals(other.url)))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=7;
    hash=17*hash+(this.url!=null?this.url.hashCode():0);
    return hash;
  }
}
