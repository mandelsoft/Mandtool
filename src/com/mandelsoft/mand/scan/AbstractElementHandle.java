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
public class AbstractElementHandle<N extends ElementName<N>>
             extends ElementBaseHandle<N> {
  private AbstractFile file;
  private MandelHeader header;

  protected AbstractElementHandle(AbstractFile file, N name, MandelHeader header)
  {
    super(name);
    this.file=file;
    this.header=header;
  }

  public AbstractFile getFile()
  {
    return file;
  }

  public MandelHeader getHeader()
  {
    return header;
  }

  public MandelData getData() throws IOException
  {
    try {
      return new MandelData(getFile());
    }
    catch (IOException ex) {
      System.out.println("*** cannot read data for "+getFile()+": "+ex);
      throw ex;
    }
  }
}
