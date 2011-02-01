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

import java.io.File;
import java.net.URL;

/**
 *
 * @author Uwe Krueger
 */
public class Environment extends Environment1  {

  public Environment(String tool, String[] args, URL dir) throws IllegalConfigurationException
  {
    super(tool,args,dir);
  }

  public Environment(String tool, String[] args, File dir) throws IllegalConfigurationException
  {
    super(tool,args,dir);
  }

  public Environment(String[] args, File dir) throws IllegalConfigurationException
  {
    super(args,dir);
  }

  public Environment(String[] args) throws IllegalConfigurationException
  {
    super(args);
  }

  public Environment(String tool, String[] args) throws IllegalConfigurationException
  {
    super(tool,args);
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    try {
      Environment env=new Environment(null);
      //    System.out.println(s.getColormapNames());
      //    System.out.println(s.getColormapNames());
    }
    catch (IllegalConfigurationException ex) {
      System.err.println("failed: "+ex);
    }
  }
}
