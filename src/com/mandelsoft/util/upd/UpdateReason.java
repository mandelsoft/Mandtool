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

package com.mandelsoft.util.upd;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class UpdateReason {
  static private Map<String,Object> empty=new HashMap<String,Object>();

  private Map<String,Object> properties;
  private Object ctxObject;
  private Object reason;

  public UpdateReason(Object ctxObject, Object reason)
  {
    this(null,ctxObject,reason);
  }

  public UpdateReason(Object ctxObject)
  {
    this(null,ctxObject,null);
  }

  public UpdateReason(Map<String, Object> properties, Object ctxObject,
                      Object reason)
  {
    this.properties=properties==null?empty:properties;
    this.ctxObject=ctxObject;
    this.reason=reason;
  }

  public UpdateReason(Map<String, Object> properties, Object ctxObject)
  {
    this(properties,ctxObject,null);
  }

  /////////////////////////////////////////////////////////////////////////

  public Object getContextObject()
  {
    return ctxObject;
  }

  public Object getReason()
  {
    return reason;
  }

  /////////////////////////////////////////////////////////////////////////

  public Set<String> propertySet()
  {
    return properties.keySet();
  }

  public Object getProperty(String key)
  {
    return properties.get(key);
  }

  public boolean containsProperty(String key)
  {
    return properties.containsKey(key);
  }

  /////////////////////////////////////////////////////////////////////////

  public static class Factory {
    private Map<String,Object> properties;
    private Object ctxObject;
    private Object reason;

    public Factory(Object ctxObject)
    {
      this.ctxObject=ctxObject;
    }

    public Factory(Object ctxObject, Object reason)
    {
      this.ctxObject=ctxObject;
      this.reason=reason;
    }

    public Factory setProperty(String name, Object value)
    {
      if (properties==null) properties=new HashMap<String,Object>();
      properties.put(name, value);
      return this;
    }

    public UpdateReason getReason()
    {
      try {
        return new UpdateReason(properties,ctxObject,reason);
      }
      finally {
        properties=null;
      }
    }
  }
}
