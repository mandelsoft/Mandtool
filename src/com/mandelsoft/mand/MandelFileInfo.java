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

package com.mandelsoft.mand;

import com.mandelsoft.io.AbstractFile;

/**
 *
 * @author Uwe Krueger
 */
public class MandelFileInfo {
  private MandelHeader header;
  private AbstractFile file;
  private QualifiedMandelName name;

  public MandelFileInfo(MandelHeader header, AbstractFile file,
                        QualifiedMandelName name)
  {
    this.header=header;
    this.file=file;
    this.name=name;
  }

  public MandelFileInfo(MandelFileInfo info, QualifiedMandelName name)
  {
    this.header=info.header;
    this.file=info.file;
    this.name=name;
  }

  public AbstractFile getFile()
  {
    return file;
  }

  public MandelHeader getHeader()
  {
    return header;
  }

  public QualifiedMandelName getName()
  {
    return name;
  }
}
