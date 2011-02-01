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

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.QualifiedMandelName;
import java.io.IOException;

/**
 *
 * @author Uwe Krueger
 */
public class ProxyMandelHandle extends ProxyElementHandle<QualifiedMandelName,
                                                          MandelHandle>
                               implements MandelHandle {
 
  public ProxyMandelHandle(MandelHandle orig, QualifiedMandelName name)
  {
    super(orig,name);
  }

  public String getQualifier()
  {
    return getName().getQualifier();
  }
  
  public MandelData getInfo() throws IOException
  {
    return getOrig().getInfo();
  }

}
