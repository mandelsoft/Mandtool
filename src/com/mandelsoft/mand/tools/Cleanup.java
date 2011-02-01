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

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.FilteredMandelScanner;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import java.io.File;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class Cleanup extends Cmd {

  static public void main(String[] args)
  {
    try {
      boolean doit=true;
      String root=".";
      if (args.length>0) {
        root=args[0];
      }
      Environment env=new Environment("mandtool", null, new File(root));
      MandelScanner scanner=env.getInfoScanner();
      MandelScanner ref=env.getImageDataScanner();
      Set<QualifiedMandelName> refset=ref.getQualifiedMandelNames();
      for (MandelHandle h:scanner.getMandelHandles()) {
        QualifiedMandelName n=h.getName();
        if (refset.contains(n)) {
          System.out.println("obsolete "+h.getFile());
          if (doit) env.backupInfoFile(h.getFile());
        }
      }
      scanner=new FilteredMandelScanner(env.getImageDataScanner(),
                                        MandelScanner.IS_RASTER);
      ref=env.getRasterImageScanner();
      refset=ref.getQualifiedMandelNames();
      for (MandelHandle h:scanner.getMandelHandles()) {
        QualifiedMandelName n=h.getName();
        if (refset.contains(n)) {
          System.out.println("obsolete "+h.getFile());
          if (doit) env.backupRasterFile(h.getFile());
        }
      }
    }
    catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: "+ex);
    }
  }
}
