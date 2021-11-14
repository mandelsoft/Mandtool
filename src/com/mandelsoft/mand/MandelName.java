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

import com.mandelsoft.util.Utils;
import java.io.File;

/**
 *
 * @author Uwe Krueger
 */
public class MandelName extends DefaultElementName<MandelName> {
  static public final String ROOT_NAME="0";
  static public final MandelName ROOT=new MandelName(ROOT_NAME);

  protected String effective;

  public MandelName(String n)
  {
    super(compress(n));
    if (label!=null) {
      n=name.substring(0,name.length()-label.length()-1);
    }
    this.effective=uncompress(n);
  }

  MandelName(String n, String label)
  {
    String tmp=label(n);
    if (tmp!=null) {
      throw new IllegalArgumentException("no remote area for explicitly labeled name");
    }
    this.name=addLabel(compress(n),label);
    this.label=label;
    this.effective=uncompress(n);
  }

  public String getEffective()
  {
    return effective;
  }
  
  public String getSubAreaName()
  {
    String n=effective.substring(effective.length()-1);
    if (getLabel()==null) return n;
    return n+LABEL_START+getLabel();
  }

  public char getSubAreaChar()
  {
    return effective.charAt(effective.length()-1);
  }

  public boolean isRoot()
  { //return isRoot(name);
    return name.equals(ROOT_NAME);
  }

  public boolean isAbove(MandelName n)
  {
    if (isRoot()) return true;
    if (n.effective.startsWith(effective)) {
      int start=effective.length();
      int ix=n.effective.indexOf(LABEL_START, start);
      if (ix<0) {
        return Utils.equals(n.label, label);
      }
      else {
        start=ix;
        ix=n.effective.indexOf(LABEL_END, start+1);
        if (ix>=0) {
          return Utils.equals(n.effective.substring(start+1,ix),label);
        }
        else {
          return Utils.equals(n.effective.substring(start+1), label);
        }
      }
    }
    return false;
  }

  public boolean isHigher(MandelName n)
  {
    if (isRoot()) return !n.equals(this);
    return  (isAbove(n) && !name.equals(n.name));
//    String sn=n.getEffective();
//    String st=getEffective();
//    return sn.startsWith(st) && sn.length()!=st.length();
  }

  public MandelName sub()
  {
    if (label==null) {
      return new MandelName((isRoot()?"":getName())+"a");
    }
    else {
      return new MandelName(getName()+LABEL_END_STR+"a");
    }
  }

  public MandelName sub(char sub)
  {
    if (label==null) {
      return new MandelName((isRoot()?"":getName())+sub);
    }
    else {
      return new MandelName(getName()+LABEL_END_STR+sub);
    }
  }

  public MandelName subAt(String nlabel)
  {
    if (nlabel==null) return sub();
    if (label==null) {
      if (isRoot()) {
        return new MandelName("a",nlabel);
      }
      throw new IllegalArgumentException("no remote area for local name");
    }
    else {
      if (nlabel.equals(label)) {
        return new MandelName((isRoot()?"":effective)+"a",nlabel);
      }
      else {
        return new MandelName(getName()+LABEL_END_STR+"a"+LABEL_START_STR+nlabel);
      }
    }
  }

  public MandelName sub(MandelName n)
  {
    if (!isHigher(n)) {
      throw new IllegalArgumentException(getName()+" is not a parent of "+
                                         n.getName());
    }
    int l,e,ix,is;
    String nlabel;
    String base;
    if (isRoot()) {
      l=0;
      e=1;
      base="";
    }
    else {
      l=effective.length();
      e=0;
      base=effective;
    }
    if (n.effective.charAt(l)==LABEL_START) {
      e=n.effective.indexOf(LABEL_END,l+1)+2;
    }
    if (e>0) {
      ix=n.effective.indexOf(LABEL_START,e);
      if (ix>=0) {
        is=n.effective.indexOf(LABEL_END,ix+1);
        if (is>=0) {
          nlabel=n.effective.substring(ix+1,is);
        }
        else {
          nlabel=n.effective.substring(ix+1);
        }
      }
      else nlabel=n.label;
    }
    else {
      e=l+1;
      nlabel=label;
    }
    return new MandelName(base+n.effective.substring(l,e),nlabel);
  }

  public MandelName next()
  {
    if (isRoot()) return null;
    String eff=effective;
    char n=next(eff.charAt(eff.length()-1));
    if (n==0) return null;
    return new MandelName(eff.substring(0,eff.length()-1)+Character.toString(n),
                          label);
  }

  public MandelName prev()
  {
    if (isRoot()) return null;
    String eff=effective;
    char n=prev(eff.charAt(eff.length()-1));
    if (n==0) return null;
    return new MandelName(eff.substring(0,eff.length()-1)+Character.toString(n),
                          label);
  }
  
  public boolean isChildOf(MandelName name)
  {
    if (isRoot()) return false;
    String e=effective;
    String nlabel=label;
    int l=e.length()-1;
    if (l==0) {
      e=ROOT_NAME;
      nlabel=null;
    }
    else {
      e=e.substring(0,l);
      if (e.charAt(l-1)==LABEL_END) {
        int ix=e.lastIndexOf(LABEL_START);
        nlabel=e.substring(ix+1,l-1);
        e=e.substring(0,ix);
      }
    }
    return e.equals(name.effective) && sameLabel(name.label, nlabel);
  }
  
  
  static private boolean sameLabel(String l1, String l2)
  {
    if (l1==l2) return true;
    if (l1==null || l2 == null) return false;
    return l1.equals(l2);
  }
  
  public MandelName getParentName()
  { 
    if (isRoot()) return null;
    String e=effective;
    String nlabel=label;
    int l=e.length()-1;
    if (l==0) {
      e=ROOT_NAME;
      nlabel=null;
    }
    else {
      e=e.substring(0,l);
      if (e.charAt(l-1)==LABEL_END) {
        int ix=e.lastIndexOf(LABEL_START);
        nlabel=e.substring(ix+1,l-1);
        e=e.substring(0,ix);
      }
    }
    return new MandelName(e,nlabel);
  }

  ///////////////////////////////////////////////////////////////////////
  @Override
  public int compareTo(MandelName o)
  {
    if (o==null) return 1;
    int c=effective.compareTo(o.effective);
    if (c!=0) return c;
    if (label==null) {
      if (o.label==null) return 0;
      return -1;
    }
    return label.compareTo(o.label);
  }

  ///////////////////////////////////////////////////////////////////////
  // general name utils
  ///////////////////////////////////////////////////////////////////////

  static public String compress(String n)
  { int cnt=0;
    char r=0;
    char l=0;
    boolean label=false;
    boolean first=false;
    boolean digit=false;

    if (isRoot(n)) return n;
    StringBuffer sb=new StringBuffer();
//    if (n.startsWith(ROOT_NAME+"@")) {
//      sb.append(ROOT_NAME);
//      sb.append(LABEL_START);
//      n=n.substring(ROOT_NAME.length()+1);
//      label=true;
//      first=true;
//    }
    for (char c:n.toCharArray()) {
      if (label) {
        if (first) {
          if (!Character.isJavaIdentifierStart(c)) {
            throw new IllegalArgumentException(n+" is no valid mandel name");
          }
          first=false;
        }
        sb.append(c);
        if (c==LABEL_END) label=false;
        else {
          if (!Character.isJavaIdentifierPart(c)) {
            throw new IllegalArgumentException(n+" is no valid mandel name");
          }
        }
        continue;
      }
      if (c==r) cnt++;
      else {
        if (r>0) {
          appendC(cnt,r,sb);
          l=r;
          r=0;
          cnt=0;
          digit=false;
        }
        if (Character.isDigit(c)) {
          digit=true;
          cnt=cnt*10+c-'0';
        }
        else {
          if (c==LABEL_START) {
            if (sb.length()==0) {
              throw new IllegalArgumentException(n+" is no valid mandel name");
            }
            sb.append(c);
            first=true;
            label=true;
            l=0;
          }
          else {
            if (!isValid(c)) {
              throw new IllegalArgumentException(n+" is no valid mandel name");
            }
            if (l==c) {
              throw new IllegalArgumentException(n+" is no valid mandel name");
            }
            l=r;
            r=c;
            if (cnt==0) {
              if (digit) {
                throw new IllegalArgumentException(n+" is no valid mandel name");
              }
              cnt=1;
            }
          }
        }
      }
    }
    if (r>0) appendC(cnt,r,sb);
    else {
      if (cnt>0) {
        throw new IllegalArgumentException(n+" is no valid mandel name");
      }
    }
    return sb.toString();
  }

  static public String uncompress(String n)
  { int cnt=0;
    char r=0;
    boolean label=false;
    boolean first=false;
    boolean digit=false;

    if (isRoot(n)) return n;
    StringBuffer sb=new StringBuffer();
//    if (n.startsWith(ROOT_NAME+"@")) {
//      sb.append(ROOT_NAME);
//      sb.append(LABEL_START);
//      n=n.substring(ROOT_NAME.length()+1);
//      label=true;
//      first=true;
//    }

    for (char c:n.toCharArray()) {
      if (label) {
        if (first) {
          if (!Character.isJavaIdentifierStart(c)) {
            throw new IllegalArgumentException(n+" is no valid mandel name");
          }
          first=false;
        }
        sb.append(c);
        if (c==LABEL_END) label=false;
        else {
          if (!Character.isJavaIdentifierPart(c)) {
            throw new IllegalArgumentException(n+" is no valid mandel name");
          }
        }
        continue;
      }

      if (c==r) cnt++;
      else {
        if (r>0) {
          appendU(cnt,r,sb);
          r=0;
          cnt=0;
        }
        if (Character.isDigit(c)) {
          cnt=cnt*10+c-'0';
        }
        else {
          if (c==LABEL_START) {
            if (sb.length()==0) {
              throw new IllegalArgumentException(n+" is no valid mandel name");
            }
            sb.append(c);
            first=true;
            label=true;
          }
          else {
            if (!isValid(c)) {
              throw new IllegalArgumentException(n+" is no valid mandel name");
            }
            r=c;
            if (cnt==0) cnt=1;
          }
        }
      }
    }
    if (r>0) appendU(cnt,r,sb);
    return sb.toString();
  }

  public static boolean isRoot(String n)
  {
    if (n.equals(ROOT_NAME)) return true;
//    if (n.startsWith(ROOT_NAME)) {
//      int ix=ROOT_NAME.length();
//      if (n.charAt(ix++)==LABEL_START) {
//        if (ix==n.length() || !Character.isJavaIdentifierStart(n.charAt(ix++))) return false;
//        while (ix<n.length()) {
//          if (!Character.isJavaIdentifierPart(n.charAt(ix++))) return false;
//        }
//        return true;
//      }
//    }
    return false;
  }

  public static boolean isValid(char c)
  {
    return (c>='a' && c<='z');
         //  (c>='A' && c<='Z') not possible on Windows
  }

  public static char next(char c)
  { if (isValid(c) && isValid((char)(c+1))) return (char)(c+1);
    return (char)0;
  }

  public static char prev(char c)
  { if (isValid(c) && isValid((char)(c-1))) return (char)(c-1);
    return (char)0;
  }

  public static boolean isMandelName(String n)
  {
    try {
      new MandelName(n);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static MandelName create(String n)
  {
    try {
      return new MandelName(n);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  public static MandelName create(File f)
  {
    try {
      String base=f.getName();
      int ix=base.lastIndexOf('.');
      if (ix>=0) base=base.substring(0,ix);
      ix=base.indexOf('-');
      if (ix>=0) base=base.substring(0,ix);
      return new MandelName(base);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  ///////////////////////////////////////////////////////////////////////
  // private name utils
  ///////////////////////////////////////////////////////////////////////

  private static void appendC(int cnt, char c, StringBuffer sb)
  {
    if (cnt>1) sb.append(cnt);
    if (c==0 || cnt==0)
      throw new IllegalArgumentException(sb+" is no valid mandel name");
    sb.append(c);
  }

  private static void appendU(int cnt, char c, StringBuffer sb)
  {
    if (c!=0) {
      while (cnt-->0) {
        sb.append(c);
      }
    }
    else throw new IllegalArgumentException(sb+" is no valid mandel name");
  }

  ///////////////////////////////////////////////////////////////////////
  // main (test)
  ///////////////////////////////////////////////////////////////////////

  private interface Operation {
    String op(String a);
    String getOp();
  }

  private interface BinaryOperation extends Operation {
    void setBase(String base);
  }
  
  private static class Compress implements Operation {
    public String op(String a)
    {
      return compress(a);
    }

    public String getOp()
    {
      return "compress";
    }
  }

  private static class UnCompress implements Operation {
    public String op(String a)
    {
      return uncompress(a);
    }

    public String getOp()
    {
      return "uncompress";
    }
  }

  private static class SubAt implements Operation {
    String label;

    public String op(String a)
    {
      MandelName mn=new MandelName(a);
      return mn.subAt(label).getName();
    }

    public String getOp()
    {
      return "sub("+label+")";
    }
  }

  private static class Above implements BinaryOperation {
    MandelName base;

    public void setBase(String base)
    {
      this.base=new MandelName(base);
    }

    public String op(String a)
    {
      MandelName mn=new MandelName(a);
      return mn.isAbove(base)?"true":"false";
    }

    public String getOp()
    {
      return "above("+base+")";
    }
  }

  private static class Down implements BinaryOperation {
    MandelName base;

    public void setBase(String base)
    {
      this.base=new MandelName(base);
    }

    public String op(String a)
    {
      MandelName mn=new MandelName(a);
      return mn.sub(base).getName();
    }

    public String getOp()
    {
      return "down("+base+")";
    }
  }

   private static class IsChild implements BinaryOperation {
    MandelName base;

    public void setBase(String base)
    {
      this.base=new MandelName(base);
    }

    public String op(String a)
    {
      MandelName mn=new MandelName(a);
      return mn.isChildOf(base)?"true":"false";
    }

    public String getOp()
    {
      return "isChildOf("+base+")";
    }
  }
   
  private static class Parent implements Operation {
    public String op(String a)
    {
      return new MandelName(a).getParentName().getName();
    }

    public String getOp()
    {
      return "parent";
    }
  }


  static Operation compress=new Compress();
  static Operation uncompress=new UnCompress();
  static SubAt     sub=new SubAt();
  static Above     above=new Above();
  static Down      down=new Down();
  static Parent    parent=new Parent();
  static IsChild   ischild=new IsChild();
  static int failed=0;

  private static boolean check(BinaryOperation op, String arg, String base, String exp)
  {
    op.setBase(base);
    return check(op, arg, exp);
  }
  
  private static boolean check(Operation op, String arg, String exp)
  {
    try {
      String res=op.op(arg);
      if (res.equals(exp)) {
        System.out.println(op.getOp()+": "+arg+" -> "+res+" (OK)");
        return true;
      }
      else {
        System.out.println(
          op.getOp()+": "+arg+" -> "+res+" (FAILED) expected "+exp);
        failed++;
        return false;
      }
    }
    catch (IllegalArgumentException ex) {
      if (exp!=null) {
        failed++;
        System.out.println(op.getOp()+": "+arg+" -> <illegal> (FAILED) expected "
          +exp+" ("+ex.getMessage()+")");
      }
      else {
        System.out.println(op.getOp()+": "+arg+" -> <illegal> (OK)");
      }
      return exp==null;
    }
  }

  static public int test()
  {
    System.out.println("starting MandelName tests...");
    failed=0;
    check(compress,"0","0");
    check(compress,"a","a");
    check(compress,"ab","ab");
    check(compress,"abc","abc");
    check(compress,"aa","2a");
    check(compress,"abbc","a2bc");
    check(compress,"aabc","2abc");
    check(compress,"aabbc","2a2bc");
    check(compress,"aabbcc","2a2b2c");
    check(compress,"2abbcc","2a2b2c");
    check(compress,"aa2bcc","2a2b2c");
    check(compress,"aabb2c","2a2b2c");
    check(compress,"a@x","a@x");
    check(compress,"a1",null);
    check(compress,"10",null);

    check(uncompress,"0","0");
    check(uncompress,"a","a");
    check(uncompress,"ab","ab");
    check(uncompress,"abc","abc");
    check(uncompress,"aa","aa");
    check(uncompress,"abbc","abbc");
    check(uncompress,"aabc","aabc");
    check(uncompress,"aabbc","aabbc");
    check(uncompress,"aabbcc","aabbcc");
    check(uncompress,"2abbcc","aabbcc");
    check(uncompress,"aa2bcc","aabbcc");
    check(uncompress,"aabb2c","aabbcc");
    check(uncompress,"a@x","a@x");
    check(uncompress,"a@x@y",null);

    //check("compress","2aa2a",compress("2aa2a"),"5a");
    check(uncompress,"2aa2a","aaaaa");

    check(uncompress,"2a@laber~2a","aa@laber~aa");
    check(uncompress,"aa@laber~aa","aa@laber~aa");

    check(uncompress,"1@laber",null);
    check(uncompress,"aa@5aber~aa",null);
    check(uncompress,"aa@la@er~aa",null);



    check(compress,"0aabb2c",null);
    check(compress,"a1abb2c",null);
    check(compress,"a0abb2c",null);
    check(compress,"aa0b2c",null);
    check(compress,"2a2a",null);

    //check(compress,"0@laber","0@laber");
    check(compress,"0@laber",null);
    //check(compress,"0@laber~2a","0@laber~2a");
    check(compress,"0@laber~2a",null);
    check(compress,"2a@laber~2a","2a@laber~2a");
    check(compress,"aa@laber~aa","2a@laber~2a");

    check(compress,"1@laber",null);
    check(compress,"aa@5aber~aa",null);
    check(compress,"aa@la@er~aa",null);

    ////////////////////////////////////////////////////////////
    sub.label=null;
    check(sub,"0","a");
    check(sub,"a","2a");
    //check(sub,"0@laber","0@laber~a");
    //check(sub,"0@laber~a","0@laber~2a");
    check(sub,"a@laber","a@laber~a");
    check(sub,"a@laber~a","a@laber~2a");
    sub.label="laber";
    //check(sub,"0@laber","a@laber");
    check(sub,"0","a@laber");
    check(sub,"a@laber","2a@laber");
    sub.label="other";
    //check(sub,"0@laber","0@laber~a@other");
    check(sub,"a@laber","a@laber~a@other");
    check(sub,"a@laber~a",null);

    ////////////////////////////////////////////////////////////
    above.setBase("5a");
    check(above,"4abc","false");
    check(above,"6a","false");
    check(above,"5a","true");
    check(above,"4a","true");
    above.setBase("5a@laber");
    check(above,"4a","false"); // no common root
    check(above,"4a@other","false");
    check(above,"4a@laber","true");
    check(above,"4a@laber~a","false");
    check(above,"4a@laber~a@other","false");
    above.setBase("5a@laber~2a");
    check(above,"4a@laber~a@other","false");
    check(above,"4a@laber~a","false");
    check(above,"5a@laber~a","true");
    above.setBase("5a@laber~2a@other");
    check(above,"4a@laber~a@other","false");
    check(above,"5a@laber~a@other","true");
    check(above,"5a@laber","true");

    ////////////////////////////////////////////////////////////
    down.setBase("abc");
    check(down,"0","a");
    check(down,"a","ab");
    check(down,"ab","abc");
    check(down,"abc",null);
    check(down,"b",null);

    down.setBase("abc@laber");
    check(down,"0","a@laber");
    check(down,"a@laber","ab@laber");
    check(down,"ab@laber","abc@laber");

    down.setBase("abc@laber~def");
    //check(down,"0@laber","a@laber");
    check(down,"0","a@laber");
    check(down,"a@laber","ab@laber");
    check(down,"ab@laber","abc@laber");
    check(down,"abc@laber","abc@laber~d");
    check(down,"abc@laber~d","abc@laber~de");

    down.setBase("abc@laber~def@other");
    check(down,"a@laber","ab@laber");
    check(down,"ab@laber","abc@laber");
    check(down,"abc@laber","abc@laber~d@other");
    check(down,"abc@laber~d@other","abc@laber~de@other");
    check(down,"abc@laber~d",null);

    down.setBase("abc@laber~def@other~gh");
    check(down,"a@laber","ab@laber");
    check(down,"ab@laber","abc@laber");
    check(down,"abc@laber","abc@laber~d@other");
    check(down,"abc@laber~def@other","abc@laber~def@other~g");
    check(down,"abc@laber~d",null);
    check(down,"abc@laber~def",null);

    ////////////////////////////////////////////////////////////
    check(parent,"a","0");
    check(parent,"ab","a");
    check(parent,"a@laber","0");
    check(parent,"ab@laber","a@laber");
    check(parent,"ab@laber~ab","ab@laber~a");
    check(parent,"ab@laber~a","ab@laber");
    check(parent,"ab@laber~ab@other","ab@laber~a@other");
    check(parent,"ab@laber~a@other","ab@laber");

    check(ischild, "a", "0", "true");
    check(ischild, "a", "a", "false");
    check(ischild, "a", "b", "false");
    check(ischild, "2a", "a", "true");
    check(ischild, "3a", "a", "false");
    check(ischild, "a@test", "a", "false");
    check(ischild, "a@test~a", "a@test", "true");
    check(ischild, "ab@test", "a@test", "true");
    check(ischild, "ab@test", "a@other", "false");
    check(ischild, "ab@test~ab@other", "ab@test~a", "false");
    check(ischild, "ab@test~ab@other", "ab@test~a@other", "true");
     
    System.out.println("failed: "+failed);
    return failed;
  }

  static public void main(String[] args)
  {
    test();
  }
}