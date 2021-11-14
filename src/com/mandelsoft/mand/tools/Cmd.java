
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

import com.mandelsoft.util.Utils;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @author Uwe Krüger
 */
public class Cmd {

  static public String toString(String[] args)
  {
    String gap = "";
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      sb.append(gap);
      sb.append(arg);
      gap = " ";
    }
    return sb.toString();
  }

  static public void main(String[] args)
  {
    Class c = MandTool.class;
    if (args.length > 1 && args[0].equals("-c")) {
      String cn = Utils.evaluateClassName(args[1], Cmd.class);

      try {
        c = Class.forName("com.mandelsoft.mand.tools."+cn);
      }
      catch (ClassNotFoundException ex) {
        try {
          c = Class.forName(cn);
        }
        catch (ClassNotFoundException ex2) {
          System.err.println("class " + cn + " not found: " + ex2);
          System.exit(1);
        }
      }
      args = Arrays.copyOfRange(args, 2, args.length);
    }
    try {
      System.out.println("calling " + c + " " + toString(args));
      Method m = c.getMethod("main", new Class[]{String[].class});
      try {
        m.invoke(null, new Object[]{args});
      }
      catch (Exception ex) {
        System.err.println("error calling " + c + ": " + ex);
        ex.printStackTrace(System.out);
        System.exit(1);
      }
    }
    catch (Exception ex) {
      System.err.println(c + " has no main method: " + ex);
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }
}
