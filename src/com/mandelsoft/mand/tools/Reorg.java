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
package com.mandelsoft.mand.tools;

import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.ToolEnvironment;
import com.mandelsoft.mand.util.MandelList;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class Reorg extends Command{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    try {
      ToolEnvironment env=new ToolEnvironment(args);
      MandelScanner s=env.getImageDataScanner();
      MandelList seen=env.getSeenRasters();
      QualifiedMandelName[] a=new QualifiedMandelName[0];
      env.startUpdate();
      for (QualifiedMandelName q:seen.toArray(a)) {
        Set<MandelHandle> set=s.getMandelHandles(q);
        for (MandelHandle h:set) {
          env.handleRasterSeen(h.getFile());
        }
      }
      env.finishUpdate();
    }
    catch (IllegalConfigurationException ex) {
      Error("illegal config: "+ex);
    }
  }
}
