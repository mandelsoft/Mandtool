
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/**
 *
 * @author Uwe Krüger
 */

public interface AbstractFile {
  public boolean isFile();

  public InputStream getInputStream() throws IOException;
  public long getLastModified();
  public URL getURL();
  public Proxy getProxy();
  public File getFile();
  public String getPath();
  public String getName();
  public boolean lock() throws IOException;
  public boolean tryLock() throws IOException;
  public void releaseLock() throws IOException;
  public AbstractFile getParent();
  public AbstractFile getSub(String name);
  
  public static class Factory {
    static public AbstractFile create(File f)
    {
      return new FileAbstractFile(f);
    }

    static public AbstractFile create(File folder, String name)
    {
      return new FileAbstractFile(new File(folder,name));
    }

    static public AbstractFile create(Proxy proxy, URL url, long lastModified)
    {
      return new URLAbstractFile(proxy,url,lastModified);
    }

    static public AbstractFile create(Proxy proxy, URL url)
    {
      return new URLAbstractFile(proxy,url);
    }

    static public AbstractFile create(String path, Proxy proxy, boolean local)
    {
      try {
        URL url=new URL(path);
        if (url.getProtocol().equals("file")) {
          if (!local)
              throw new RuntimeException(
                      "file paths not allowed for remote repositories");
          return create(new File(url.getPath()));
        }
        else {
          return create(proxy,url);
        }
      }
      catch (MalformedURLException ex) {
        if (!local)
              throw new RuntimeException(
                      "file paths not allowed for remote repositories");
        return create(new File(path));
      }
    }
  }
}
