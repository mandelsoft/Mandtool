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

import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ElementName;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelHeader;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class ProxyElementHandle<N extends ElementName<N>,
                                H extends ElementHandle<N>>
                               extends ElementBaseHandle<N> {
  private H orig;

  public ProxyElementHandle(H orig, N name)
  {
    super(name);
    this.orig=orig;
  }

  public H getOrig()
  {
    return orig;
  }

  public MandelHeader getHeader()
  {
    return orig.getHeader();
  }

  public AbstractFile getFile()
  {
    return orig.getFile();
  }

  public MandelData getData() throws IOException
  {
    return orig.getData();
  }
}
