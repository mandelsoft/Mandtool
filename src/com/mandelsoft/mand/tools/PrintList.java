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
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import java.io.File;

/**
 *
 * @author Uwe Krueger
 */
public class PrintList extends Command {

  public static void print(String gap, boolean list, MandelListFolder f)
  {
    String ngap=gap+"  ";
    System.out.println(gap+"folder "+f.getName());
    for (MandelListFolder s:f) {
      print(ngap,list,s);
    }
  }

  public static void main(String[] args)
  { int c=0;

    Environment env;

    if (args.length>c) {
      try {
        env=new Environment("mandtool", null, new File(args[c++]));
        if (args.length>c) {
        }
        else {
          for (MandelListFolderTree t:env.getUserLists()) {
            System.out.println("user list "+t.getRoot().getName());
            print("  ", false, t.getRoot());
          }
        }
      }
      catch (IllegalConfigurationException ex) {
        Error("illegal config: "+ex);
      }
    }
    else {
      Error("printlist <mandeldir> {<list>}");
    }

  }

}
