
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

import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.util.Utils;
import java.io.File;

/**
 *
 * @author Uwe Krüger
 */

public class QualifiedMandelName implements ElementName<QualifiedMandelName> {
  static public final QualifiedMandelName ROOT=
                                       new QualifiedMandelName(MandelName.ROOT);

  private MandelName name;
  private String qualifier;
  private String label;

  public QualifiedMandelName(MandelName name)
  {
    this(name,null);
  }

  public QualifiedMandelName(MandelName name, String qualifier)
  {
    this(name,qualifier,null);
  }

  public QualifiedMandelName(MandelName name, String qualifier, String label)
  {
    if (label==null) {
      if (qualifier==null) {
        label=name.getLabel();
      }
      else {
        int ix=qualifier.indexOf(LABEL_START);
        if (ix>0) {
          label=qualifier.substring(ix+1);
          qualifier=qualifier.substring(0,ix);
        }
      }
    }
    this.name=name;
    this.qualifier=qualifier;
    this.label=label;
    if (qualifier!=null && qualifier.indexOf(LABEL_START)>=0) {
      throw new IllegalArgumentException("illegal qualifier: "+qualifier);
    }
     if (label!=null && label.indexOf(LABEL_START)>=0) {
      throw new IllegalArgumentException("illegal label: "+label);
    }
  }

  public String getName()
  {
    String base;
    if (name.isRemoteName() && Utils.equals(name.getLabel(),label)) {
      base=name.getName();
      int ix=base.lastIndexOf(LABEL_START);
      base=base.substring(0,ix);
    }
    else {
      base =name.getName();
    }
    return base+(qualifier==null?"":("-"+qualifier))
                 +(label==null?"":(LABEL_START_STR+label));
  }

  public MandelName getMandelName()
  {
    return name;
  }

  public String getQualifier()
  {
    return qualifier;
  }

  public String getLabel()
  {
    return label;
  }

  public boolean isRoot()
  {
    return name.isRoot();
  }

  public QualifiedMandelName getBaseName()
  {
    if (qualifier==null) return this;
    return new QualifiedMandelName(name);
  }
  
  ////////////////////////////////////////////////////////////////////////
  public QualifiedMandelName get(String qualifier, boolean preserveLocation)
  {
    return new QualifiedMandelName(name,qualifier,
                 preserveLocation?name.getLabel():null);
  }

  public QualifiedMandelName get(MandelName name, boolean preserveLocation)
  {
    return new QualifiedMandelName(name,qualifier,
                 preserveLocation?name.getLabel():null);
  }

  ////////////////////////////////////////////////////////////////////////
  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final QualifiedMandelName other=(QualifiedMandelName)obj;
    if (this.name!=other.name&&(this.name==null||!this.name.equals(other.name)))
      return false;
    if ((this.qualifier==null)?(other.qualifier!=null):!this.qualifier.equals(other.qualifier))
      return false;
    if ((this.label==null)?(other.label!=null):!this.label.equals(other.label))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=3;
    hash=17*hash+(this.name!=null?this.name.hashCode():0);
    hash=17*hash+(this.qualifier!=null?this.qualifier.hashCode():0);
    hash=17*hash+(this.label!=null?this.label.hashCode():0);
    return hash;
  }

  @Override
  public String toString()
  { 
    return getName();
  }

  public int compareTo(QualifiedMandelName o)
  {
    int c=name.compareTo(o.getMandelName());
    if (c==0) {
      if (qualifier==null) {
        if (o.qualifier!=null) c=-1;
      }
      else {
        if (o.qualifier==null) c=1;
        else c=qualifier.compareTo(o.qualifier);
       }
    }
    return c;
  }
  
  ////////////////////////////////////////////////////////////////////////
  public static QualifiedMandelName create(AbstractFile f)
  {
    return qn(MandelFileName.create(f));
  }

  public static QualifiedMandelName create(File f)
  {
    return qn(MandelFileName.create(f));
  }

  public static QualifiedMandelName create(String n)
  {
    return qn(MandelFileName.create(n));
  }
  
  private static QualifiedMandelName qn(MandelFileName mfn)
  {
    return mfn==null?null:mfn.getQualifiedName();
  }

  ////////////////////////////////////////////////////////////////////////

  static public QualifiedMandelName _create(String n)
  {
    QualifiedMandelName qn=null;
    String base=n;
    String suffix=null;
    String qualifier=null;
    String qlabel=null;
    String label=null;
    MandelName mn;

    try {
      int ix=base.lastIndexOf('.');
      if (ix>=0) {
        suffix=base.substring(ix);
        base=base.substring(0,ix);
      }
      ix=base.indexOf('-');
      if (ix>=0) {
        qualifier=base.substring(ix+1);
        base=base.substring(0,ix);
        if ((ix=qualifier.indexOf(LABEL_START))>=0) {
          qlabel=qualifier.substring(ix);
          label=qlabel.substring(1);
          qualifier=qualifier.substring(0,ix);
          if (!MandelName.isRemoteName(base) && !MandelName.isRoot(base)) {
            base+=qlabel;
          }
        }
      }
      mn=new MandelName(base);
      qn=new QualifiedMandelName(mn,qualifier,label);
    }
    catch (IllegalArgumentException e) {
      qn=null;
    }
    //System.out.println("mfn: "+n+" -> "+mfn);
    return qn;
  }

  //////////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////////

  private static int failed=0;

  private static boolean check(String arg, String mn, String qualifier, String label)
  {
    try {
      QualifiedMandelName qn=QualifiedMandelName._create(arg);
      if (qn!=null) {
        if (!Utils.equals(qn.getMandelName(), new MandelName(mn))) {
          System.out.println(
            arg+" -> "+qn+
              " ("+qn.getMandelName()+"/"+qn.getQualifier()+"/"+qn.getLabel()+
              ") (FAILED mn) expected "+mn+"/"+qualifier+"/"+label);
          failed++;
          return false;
        }
        if (!Utils.equals(qn.getQualifier(), qualifier)) {
          System.out.println(
            arg+" -> "+qn+ " ("+qn.getMandelName()+"/"+qn.getQualifier()+"/"+qn.getLabel()+
              ") (FAILED qualifier) expected "+mn+"/"+qualifier+"/"+label);
          failed++;
          return false;
        }
        if (!Utils.equals(qn.getLabel(), label)) {
          System.out.println(
            arg+" -> "+qn+ " ("+qn.getMandelName()+"/"+qn.getQualifier()+"/"+qn.getLabel()+
              ") (FAILED label) expected "+mn+"/"+qualifier+"/"+label);
          failed++;
          return false;
        }
        System.out.println(arg+" -> "+qn+
              " ("+qn.getMandelName()+"/"+qn.getQualifier()+"/"+qn.getLabel()+
              ") (OK)");
        return true;
      }
      else {
        if (mn!=null) {
          failed++;
          System.out.println(arg+" -> <null> (FAILED) expected "
            +mn+"/"+qualifier+"/"+label);
        }
        else {
          System.out.println(arg+" -> <null> (OK)");
        }
        return mn==null;
      }
    }
    catch (IllegalArgumentException ex) {
       if (mn!=null) {
        failed++;
        System.out.println(arg+" -> <illegal> (FAILED) expected "
          +mn+"/"+qualifier+"/"+label+" ("+ex.getMessage()+")");
      }
      else {
        System.out.println(arg+" -> <illegal> (OK)");
      }
      return mn==null;
    }

  }

  public static int test()
  {
    System.out.println("starting QualifiedMandelName tests...");
    failed=0;
    check("0","0",null,null);
    check("ab","ab",null,null);

    check("0-qual","0","qual",null);
    check("ab-qual","ab","qual",null);

    check("0-qual@label","0","qual","label");
    check("ab@label-qual","ab@label","qual",null);
    check("ab-qual@label","ab@label","qual","label");
    check("ab@other-qual@label","ab@other","qual","label");

    System.out.println("failed: "+failed);
    return failed;
  }

  public static void main(String[] args)
  {
    test();
  }
}
