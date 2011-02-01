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

import java.io.File;

/**
 *
 * @author Uwe Krueger
 */
public class LogicalFileMandelScanner extends FolderMandelScannerSupport {
  private boolean temp;

  public LogicalFileMandelScanner()
  {
    this(false);
  }

  public LogicalFileMandelScanner(boolean temp)
  {
    this(MandelScanner.HAS_IMAGEDATA);
    this.temp=temp;
  }

  public LogicalFileMandelScanner(Filter filter)
  { this(filter,true,false);
  }

  public LogicalFileMandelScanner(Filter filter, boolean setup)
  { this(filter,setup,false);
  }

  public LogicalFileMandelScanner(Filter filter, boolean setup,
                                                 boolean temp)
  { super(filter);
    this.temp=temp;
    if (setup) rescan(false);
  }

  //
  // file handling
  //
  
  public void rescan(boolean verbose)
  {
    if (temp) {
      if (verbose) System.out.println("cleanup logical settings ");
      clear();
    }
  }

  @Override
  public void add(File f)
  {
    System.out.println("adding explicit file "+f);
    super.add(f);
  }

  @Override
  public String toString()
  {
    return "Logical ("+super.toString()+")";
  }
}
