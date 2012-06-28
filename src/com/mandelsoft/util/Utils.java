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

package com.mandelsoft.util;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Uwe Krüger
 */
public class Utils {

  static public final String mypackage=parent(parent(Utils.class.getName()));

  static private String parent(String cn)
  {
    return cn.substring(0,cn.lastIndexOf('.'));
  }

  static public String evaluateClassName(String cn, Class ctx)
  {
    if (cn.startsWith("my.")) {
      cn=mypackage+cn.substring(2);
    }
    else if(cn.startsWith("com.mandelsoft.")) {
      cn=mypackage+cn.substring(14);
    }
    else if (cn.indexOf('.')<0) cn=parent(ctx.getName())+"."+cn;
    return cn;
  }

  static public String normalizeClassName(Class clazz, Class ctx)
  {
    String cn=clazz.getName();
    String pkg=parent(ctx.getName());
    if (parent(cn).equals(pkg)) {
      cn=cn.substring(cn.lastIndexOf('.')+1);
    }
    return cn;
  }

  public static boolean equals(Object a, Object b)
  {
    if (a==b) return true;
    if (a==null || b==null) return false;
    return a.equals(b);
  }

  static public boolean isEmpty(String s)
  {
    return s==null || s.equals("");
  }

  public static boolean parseBoolean(String s, boolean def)
  {
    if (isEmpty(s)) return def;
    s=s.toLowerCase();
    if (s.equals("true") || s.equals("on")) return true;
    if (s.equals("false") || s.equals("off")) return false;
    try {
      int i=Integer.parseInt(s);
      return i!=0;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  static public URL subURL(URL folder, String name) throws MalformedURLException

  {
    return new URL(folder.getProtocol(),folder.getHost(),folder.getPort(),
                       folder.getPath()+"/"+name);
  }
  
  static public URL parentURL(URL folder) throws MalformedURLException

  {
    String path=folder.getPath();
    int ix=path.lastIndexOf("/");
    if (ix>0) {
      return new URL(folder.getProtocol(),folder.getHost(),folder.getPort(),
                       folder.getPath().substring(0,ix));
    }
    else {
      return new URL(folder.getProtocol(),folder.getHost(),folder.getPort(),
                       ".");
    }
  }

  static public String getObjectIdentifier(String base)
  {
    String identifier=base;
    int ix=identifier.lastIndexOf('.');
    if (ix>0) identifier=identifier.substring(ix+1);
    return identifier;
  }

  static public String getObjectIdentifier(Object o)
  {
    if (o==null) return "<null>";
    String identifier=o.toString();
    if (identifier.indexOf('@')>0) return getObjectIdentifier(identifier);
    return identifier;
  }

  static public String getObjectIdentifier(Object o, int cnt)
  {
    String identifier=getObjectIdentifier(o.getClass().getName());
    return identifier+"#"+(++cnt);
  }

  static private Method toString;
  static private Object[] empty=new Object[0];
  static {
    try {
      toString=Object.class.getMethod("toString", new Class[0]);
    }
    catch (NoSuchMethodException ex) {
    }
    catch (SecurityException ex) {
    }
  }

  static public String plural(String txt)
  {
    if (txt.endsWith("y")) return txt.substring(0,txt.length()-1)+"ies";
    if (txt.endsWith("h")) return txt+"es";
    return txt+"s";
  }

  static public String sizeString(int n, String elemtype)
  {
    return ""+n+" "+(n==1?elemtype:plural(elemtype));
  }

  //////////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////////

  static private class Test {
    @Override
    public String toString()
    {
      return "test";
    }
  }

  static public void main(String[] args)
  {
    Test test=new Test();
    System.out.println("Starting");
    try {
      System.out.println(":"+toString.invoke(test, empty));
    }
    catch (Exception ex) {
    }
  }
}
