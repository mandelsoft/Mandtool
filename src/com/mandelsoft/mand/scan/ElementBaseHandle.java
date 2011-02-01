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

import com.mandelsoft.mand.ElementName;

/**
 *
 * @author Uwe Krueger
 */
public abstract class ElementBaseHandle<N extends ElementName<N>>
             implements ElementHandle<N> {
  private N name;

  protected ElementBaseHandle(N name)
  {
    this.name=name;
  }

  public N getName()
  {
    return name;
  }

  public String getLabel()
  {
    return getName().getLabel();
  }

  ////////////////////////////////////////////////////////////////////////////

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final ElementBaseHandle<N> other=(ElementBaseHandle<N>)obj;
    if (this.getFile()!=other.getFile()&&
       (this.getFile()==null||!this.getFile().equals(other.getFile())))
      return false;
    if (this.getName()!=other.getName()&&
       (this.getName()==null||!this.getName().equals(other.getName())))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=5;
    hash=37*hash+(this.getFile()!=null?this.getFile().hashCode():0);
    hash=37*hash+(this.getName()!=null?this.getName().hashCode():0);
    return hash;
  }

  @Override
  public String toString()
  {
    return getName()+": "+getFile();
  }

}
