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

package com.mandelsoft.mand.util;

import java.util.HashMap;
import java.util.Map;
import com.mandelsoft.util.IteratorSource;

/**
 *
 * @author Uwe Krüger
 */
public class PropertyBag {
  private Map<String,String> properties=new HashMap<String,String>();

  public String getProperty(String name)
  {
    return properties.get(name);
  }

  public void setProperty(String name, String value)
  {
    if (value==null) properties.remove(name);
    else {
      value=value.trim();
      if (name.indexOf('=')>=0)
        throw new IllegalArgumentException("property name containing illegal =");
      if (value.indexOf('\n')>=0||value.indexOf('\r')>=0)
        throw new IllegalArgumentException(
                "property value containing illegal line feed");
      if (value.endsWith("{"))
        throw new IllegalArgumentException("property value ending with {");
        properties.put(name, value);
    }
  }

  public Iterable<String> propertyNames()
  {
    return new IteratorSource<String>(properties.keySet());
  }
}
