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

/**
 *
 * @author Uwe Krueger
 */
public final class AbsoluteMandelName {

  private MandelImageDBContext context;
  private String sectionName;
  private AbsoluteMandelName parent;

  AbsoluteMandelName(MandelImageDBContext context, String sectionName,
                     AbsoluteMandelName parent)
  {
    this.context=context;
    this.sectionName=sectionName;
    this.parent=parent;
  }

  @Override
  public String toString()
  {
    return context.getMandelName(this).getName();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final AbsoluteMandelName other=(AbsoluteMandelName)obj;
    if (this.context!=other.context&&(this.context==null||!this.context.equals(
                                      other.context)))
      return false;
    if ((this.sectionName==null)?(other.sectionName!=null):!this.sectionName.
      equals(other.sectionName))
      return false;
    if (this.parent!=other.parent&&(this.parent==null||!this.parent.equals(
                                    other.parent)))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=7;
    hash=53*hash+(this.context!=null?this.context.hashCode():0);
    hash=53*hash+(this.sectionName!=null?this.sectionName.hashCode():0);
    hash=53*hash+(this.parent!=null?this.parent.hashCode():0);
    return hash;
  }

  /////////////////////////////////////////////////////////////////////////
  // factory
  /////////////////////////////////////////////////////////////////////////
  static public AbsoluteMandelName createAbsoluteMandelName(MandelName name,
                                         MandelImageDBContext context)
  {
    int ix,is;
    AbsoluteMandelName amn=null;
    MandelImageDBContext ctx;

    String n=name.getName();
    while (n.length()>0 && (ix=n.indexOf(ElementName.LABEL_START))>0) {
      String section=n.substring(0,ix);
      String label;
      if ((is=n.indexOf(ElementName.LABEL_END))>0) {
        label=n.substring(ix+1,is);
        n=n.substring(is+1);
      }
      else {
        label=n.substring(ix+1);
        n="";
      }
      ctx=context.getContext(label);
      if (ctx==null) {
        throw new IllegalArgumentException(
          "illegal mandel name: unknown label "+label);
      }
      amn=new AbsoluteMandelName(ctx,section,amn);
    }
    if (n.length()>0) {
      amn=new AbsoluteMandelName(context,n,amn);
    }
    return amn;
  }

  /////////////////////////////////////////////////////////////////////////

  static private void evaluate(StringBuilder sb,
                                 AbsoluteMandelName amn,
                                 MandelImageDBContext context,
                                 boolean local)
  {
    MandelImageDBContext ctx;
    String label;

    if (amn.parent!=null) {
      evaluate(sb,amn.parent,context,false);
      sb.append(ElementName.LABEL_END);
    }
    sb.append(amn.sectionName);
    ctx=amn.context;
    if (ctx==context) {
      if (!local) {
        throw new IllegalArgumentException("illegal absolute mandel name: "
          +"intermediate usage of outer mandel image db");
      }
    }
    else {
      label=context.getLabel(ctx);
      if (label==null) {
        throw new IllegalArgumentException("illegal absolute mandel name: "
          +"intermediate image db not part of outer mandel image db");
      }
      sb.append(ElementName.LABEL_START);
      sb.append(label);
    }
  }

  static public MandelName getMandelName(AbsoluteMandelName amn,
                                         MandelImageDBContext context)
  {
    StringBuilder sb=new StringBuilder();

    evaluate(sb,amn,context,true);
    return MandelName.create(sb.toString());
  }

  /////////////////////////////////////////////////////////////////////////
  // test
  /////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    MandelImageDBContext local=new MandelImageDBContext(new File("."));
    MandelImageDBContext tmp=new MandelImageDBContext(new File("root"));
    local.addContext(tmp, "root");
    tmp=new MandelImageDBContext(new File("ctx1"));
    local.addContext(tmp, "ctx1");
    tmp=new MandelImageDBContext(new File("ctx2"));
    local.addContext(tmp, "ctx2");

    MandelName n=MandelName.create("abc@ctx1~efg@ctx2~abc");
    System.out.println("mandel name: "+n);
    AbsoluteMandelName amn=createAbsoluteMandelName(n,local);
    System.out.println("back mapper: "+getMandelName(amn,local));
  }
}
