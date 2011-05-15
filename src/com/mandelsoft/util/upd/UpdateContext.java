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

/**
 *
 * @author Uwe Krueger
 */
public class UpdateContext {
  private Map<Class<?>,Object> entries;
  private UpdateSource source;
  private UpdateReason reason;

  private UpdateContext()
  {
    entries=new HashMap<Class<?>,Object>();
  }

  public UpdateContext(UpdateSource source)
  {
    this();
    this.source=source;
    this.reason=new UpdateReason(source);
  }

  public UpdateContext(UpdateSource source, Object ctxObject)
  {
    this();
    this.source=source;
    this.reason=new UpdateReason(ctxObject);
  }

  public UpdateContext(UpdateSource source, UpdateReason reason)
  {
    this();
    this.source=source;
    this.reason=reason;
  }

  public <T> T getEntry(Class<T> c)
  {
    return (T)entries.get(c);
  }

  public <T> T setEntry(Class<T> c, T o)
  {
    entries.put(c,o);
    return o;
  }

  public UpdateSource getSource()
  {
    return source;
  }

  public UpdateReason getReason()
  {
    return reason;
  }

  public Object getContextObject()
  {
    return reason.getContextObject();
  }
}
