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

package com.mandelsoft.util;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Uwe Krüger
 */
public class UnclosableInputStream extends InputStream {
  protected InputStream is;

  public UnclosableInputStream(InputStream is)
  {
    this.is=is;
  }

  @Override
  public long skip(long n) throws IOException
  {
    return is.skip(n);
  }

  @Override
  public synchronized void reset() throws IOException
  {
    is.reset();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    return is.read(b, off, len);
  }

  @Override
  public int read(byte[] b) throws IOException
  {
    return is.read(b);
  }

  public int read() throws IOException
  {
    return is.read();
  }

  @Override
  public boolean markSupported()
  {
    return is.markSupported();
  }

  @Override
  public synchronized void mark(int readlimit)
  {
    is.mark(readlimit);
  }

  @Override
  public int available() throws IOException
  {
    return is.available();
  }
}
