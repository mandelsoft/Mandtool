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

/**
 *
 * @author Uwe Krueger
 */
public class SiteLabel {
  private String label;

  public SiteLabel(String label)
  {
    this.label=label;
  }

  public String getLabel()
  {
    return label;
  }

  public boolean isLocal()
  {
    return label==null;
  }

  @Override
  public String toString()
  {
    return label==null?"":label.toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final SiteLabel other=(SiteLabel)obj;
    if ((this.label==null)?(other.label!=null):!this.label.equals(other.label))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=7;
    hash=31*hash+(this.label!=null?this.label.hashCode():0);
    return hash;
  }
}
