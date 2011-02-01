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
public class DefaultElementName<N extends ElementName<N>> implements ElementName<N> {

  //////////////////////////////////////////////////////////////////////////

  protected String name;
  protected String label;

  protected DefaultElementName()
  {
  }

  protected DefaultElementName(String n)
  {
    this.name=n;
    this.label=label(n);
  }

  protected DefaultElementName(String name, String label)
  {
    String tmp=label(name);
    if (tmp!=null) {
      throw new IllegalArgumentException("no remote name for explicitly labeled name");
    }
    this.name=addLabel(name,label);
    this.label=label;
  }

  public String getLabel()
  {
    return label;
  }

  public String getName()
  {
    return name;
  }

  public String getBasename()
  {
    return base(name);
  }
  
  public boolean isRemoteName()
  {
    return label!=null;
  }

  public boolean isLocalName()
  {
    return !isRemoteName();
  }
  
  //////////////////////////////////////////////////////////////////////

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) {
      return false;
    }
    if (getClass()!=obj.getClass()) {
      return false;
    }
    final N other=(N)obj;
    if ((name==null)?(other.getName()!=null):
                     !name.equals(other.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=7;
    hash=59*hash+(this.name!=null?this.name.hashCode():0);
    return hash;
  }


  @Override
  public String toString()
  { return getName();
  }

  //////////////////////////////////////////////////////////////////////
  
  public int compareTo(N o)
  {
    if (o==null) return 1;
    return name.compareTo(o.getName());
  }

  //////////////////////////////////////////////////////////////////////

  static String addLabel(String name, String label)
  {
    return name+(label==null?"":(LABEL_START_STR+label));
  }

  static String label(String name)
  {
    int ix=name.lastIndexOf(LABEL_START);
    if (ix>=0) {
      int is=name.indexOf(LABEL_END,ix+1);
      if (is<0) {
        return name.substring(ix+1);
      }
    }
    return null;
  }

  static String base(String name)
  {
    int ix=name.lastIndexOf(LABEL_START);
    if (ix>=0) {
      int is=name.indexOf(LABEL_END,ix+1);
      if (is<0) {
        return name.substring(0,ix);
      }
    }
    return name;
  }

  static boolean isRemoteName(String name)
  {
    int ix=name.lastIndexOf(LABEL_START);
    if (ix>=0) {
      int is=name.indexOf(LABEL_END,ix+1);
      return is<0;
    }
    return false;
  }
}
