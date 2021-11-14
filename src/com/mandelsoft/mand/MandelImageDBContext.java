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
import com.mandelsoft.io.FileAbstractFile;
import com.mandelsoft.util.Utils;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class MandelImageDBContext {
  private static class LabelMapping {
    String src;
    String dst;
    String src_pattern;
    String dst_pattern;
    int len_src;
    int len_dst;

    public LabelMapping(String src, String dst)
    {
      this.src=src;
      this.dst=dst;
      this.src_pattern="@"+src+"~";
      this.dst_pattern=":"+dst+"~";
      this.len_src=src_pattern.length();
      this.len_dst=dst_pattern.length();
    }

  }
  
  public class ContextMapping implements ElementNameMapper {
    private boolean complete;
    private MandelImageDBContext context;
    private String label;
    private boolean direct;
    private List<LabelMapping> mapping_in;
    private List<LabelMapping> mapping_out;

    ContextMapping(String label, MandelImageDBContext context, boolean direct)
    {
      this.context=context;
      this.label  = label;
      this.direct = direct;
      mapping_in  = new ArrayList<LabelMapping>();
      mapping_out = new ArrayList<LabelMapping>();
    }

    public String getLabel()
    {
      return label;
    }

    public MandelImageDBContext getContext()
    {
      return context;
    }

    public boolean isDirect()
    {
      return direct;
    }

    void complete() throws IllegalConfigurationException
    {
      if (complete) return;
      context.complete();
      for (ContextMapping m:context.mappings()) {
        String l=MandelImageDBContext.this.getLabel(m.getContext());
        if (l==null) {
          addContext(m.getContext(),m.getLabel(),false);
        }
        else {
          if (!l.equals(m.getLabel())) {
            System.out.println("label mapping "+l+"->"+m.getLabel()+" for "+getContext().getRoot());
            mapping_in.add(new LabelMapping(l, m.getLabel()));
            mapping_out.add(new LabelMapping(m.getLabel(), l));
//            throw new IllegalConfigurationException("inconsistent labels for "
//              +m.getContext().getRoot());
          }
        }
      }
      complete=true;
    }

    //////////////////////////////////////////////////////////////////////

    private StringBuilder sb=new StringBuilder();

    private String mapLabelIn(String label)
    {
      if (label==null) {
        throw new IllegalArgumentException("not in sub context");
      }
      if (!mapping_in.isEmpty()) for (LabelMapping m:mapping_in) {
        if (m.src.equals(label)) {
          return m.dst;
        }
      }
      if (context.getContextMapping(label)!=null)
        return label;
      if (label.equals(this.label)) return null;
      throw new IllegalArgumentException("unknown label in sub context");
    }

    private String mapLabelOut(String label)
    {
      if (label==null) return this.label;
      if (!mapping_out.isEmpty()) for (LabelMapping m:mapping_out) {
        if (m.src.equals(label)) {
          return m.dst;
        }
      }
      if (context.getContextMapping(label)!=null)
        return label;
      throw new IllegalArgumentException("unknown label in sub context");
    }

    private MandelName map(MandelName n, List<LabelMapping> mapping, boolean in)
    {
      String oe=n.getEffective();
      String ol=n.getLabel();
      String ne=oe;
      String nl;
      boolean changed=false;

      if (n.isRoot()) return n;

      if (ol==null) {
        nl=label;
        changed=true;
      }
      else {
        if (in && ol.equals(label)) {
          nl=null;
          changed=true;
        }
        else {
          nl=ol;
        }
      }

      if (!mapping_out.isEmpty()) {
        int ix;
        boolean subst=false;

        sb.setLength(0);
        sb.append(n.getEffective());
        for (LabelMapping m:mapping) {
          while ((ix=sb.indexOf(m.src_pattern))>=0) {
            sb.replace(ix, ix+m.len_src, m.dst_pattern);
            subst=true;
          }
          if (ol!=null && ol.equals(m.src)) {
            nl=m.dst;
            changed=true;
          }
        }
        if (subst) {
          while ((ix=sb.indexOf(":"))>=0) {
            sb.setCharAt(ix, ElementName.LABEL_START);
          }
          ne=sb.toString();
          changed=true;
        }
      }

      if (changed) {
        n=new MandelName(ne, nl);
      }
      return n;
    }

    public QualifiedMandelName mapOut(QualifiedMandelName n)
    {
      MandelName mn=mapOut(n.getMandelName());
      boolean maplabel=!n.isRoot()||(n.getQualifier()!=null);
      return new QualifiedMandelName(mn,n.getQualifier(),
             maplabel?mapLabelOut(n.getLabel()):null);
    }

    public QualifiedMandelName mapIn(QualifiedMandelName n)
    {
      MandelName mn=mapIn(n.getMandelName());
      boolean maplabel=!n.isRoot()||(n.getQualifier()!=null);
      return new QualifiedMandelName(mn,n.getQualifier(),
             maplabel?mapLabelIn(n.getLabel()):null);
    }

    public MandelName mapOut(MandelName n)
    {
      return map(n,mapping_out,false);
    }

    public MandelName mapIn(MandelName n)
    {
      if (n.isRoot()) return n;
      if ( n.isLocalName()) {
        throw new IllegalArgumentException("no local name possible");
      }
      if (!label.equals(n.getLabel()) && mapLabelIn(n.getLabel())==null) {
         throw new IllegalArgumentException("unknown label in sub context");
      }
      return map(n,mapping_in,true);
    }

    ////////////////////////////////////////////////////////////////////////

    public ColormapName mapOut(ColormapName n)
    {
      String nlabel=mapLabelOut(n.getLabel());
      if (Utils.equals(n.getLabel(), nlabel)) return n;
      return new ColormapName(n,nlabel);
    }

    public ColormapName mapIn(ColormapName n)
    {
      if (n.isLocalName()) {
        throw new IllegalArgumentException("no local name possible");
      }
      String nlabel=mapLabelIn(n.getLabel());
      if (!label.equals(n.getLabel()) && nlabel==null) {
         throw new IllegalArgumentException("unknown label in sub context");
      }
      if (Utils.equals(n.getLabel(), nlabel)) return n;
      return new ColormapName(n,nlabel);
    }
  }

  //////////////////////////////////////////////////////////////////////
  // context
  //////////////////////////////////////////////////////////////////////

  private boolean complete;
  private MandelImageDB        database;
  private Map<String,ContextMapping> remote;
  private Map<MandelImageDBContext,String> labels;
  private Set<MandelImageDBContext> containers;
  private AbstractFile root;

  /*
   * for testing
   */
  public MandelImageDBContext(File root)
  {
    this(new FileAbstractFile(root),null);
  }

  public MandelImageDBContext(AbstractFile root, MandelImageDB db)
  {
    this.root=root;
    this.database=db;
    remote=new HashMap<String,ContextMapping>();
    labels=new HashMap<MandelImageDBContext,String>();
    containers=new HashSet<MandelImageDBContext>();
  }

  public MandelImageDB getDatabase()
  {
    return database;
  }

  void complete() throws IllegalConfigurationException
  {
    if (complete) return;
    complete=true;
    for (ContextMapping m: remote.values()) {
      m.complete();
    }
  }

  public boolean isComplete()
  {
    return complete;
  }

  public boolean hasNested()
  {
    return !remote.isEmpty();
  }

  public void addContext(MandelImageDBContext ctx, String label)
  {
    addContext(ctx,label,true);
  }

  void addContext(MandelImageDBContext ctx, String label, boolean direct)
  {
    if (ctx==this) return;
    
    if (complete) {
      throw new IllegalStateException("context for "+root+" already completed");
    }
    ContextMapping m=new ContextMapping(label,ctx,direct);
    labels.put(ctx, label);
    remote.put(label, m);
    ctx.addContainer(this);
  }

  void addContainer(MandelImageDBContext ctx)
  {
    containers.add(ctx);
  }

  public AbstractFile getRoot()
  {
    return root;
  }

  public Set<String> getContextLabels()
  {
    return remote.keySet();
  }
  
  public MandelImageDBContext getContext(String label)
  {
    ContextMapping m=getContextMapping(label);
    return m==null?null:m.getContext();
  }

  public ContextMapping getContextMapping(String label)
  {
    return remote.get(label);
  }

  public String getLabel(MandelImageDBContext ctx)
  {
    return labels.get(ctx);
  }

  public ContextMapping getContextMapping(MandelImageDBContext ctx)
  {
    String label=getLabel(ctx);
    if (label!=null) {
      return getContextMapping(label);
    }
    return null;
  }

  //////////////////////////////////////////////////////////////////////////

  public MandelName getMandelName(AbsoluteMandelName name)
  {
    return null;
  }

  //////////////////////////////////////////////////////////////////////////

  private Iterable<ContextMapping> imappings=new Iterable<ContextMapping>() {
    public Iterator<ContextMapping> iterator()
    {
      return remote.values().iterator();
    }
  };

  public Iterable<ContextMapping> mappings()
  {
    return imappings;
  }

  private Iterable<MandelImageDBContext> icontainers=new Iterable<MandelImageDBContext>() {
    public Iterator<MandelImageDBContext> iterator()
    {
      return containers.iterator();
    }
  };

  public Iterable<MandelImageDBContext> containers()
  {
    return icontainers;
  }

  ////////////////////////////////////////////////////////////////////////

  public void print(PrintStream out, String gap)
  {
    String ngap=gap+"  ";
    out.println(gap+root);
    if (!containers.isEmpty()) {
      out.println(ngap+"containing databases:");
      for (MandelImageDBContext ctx:containers) {
        out.println(ngap+"- "+ctx.getRoot());
      }
    }
    if (!remote.isEmpty()) {
      out.println(ngap+"nested databases:");
      for (String label:remote.keySet()) {
        ContextMapping sub=remote.get(label);
        out.println(ngap+"- "+label+" ("+(sub.isDirect()?"direct":"indirect")+")");
        sub.getContext().print(out, ngap+"  ");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final MandelImageDBContext other=(MandelImageDBContext)obj;
    if (this.root!=other.root&&(this.root==null||!this.root.equals(other.root)))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=5;
    hash=19*hash+(this.root!=null?this.root.hashCode():0);
    return hash;
  }

  ///////////////////////////////////////////////////////////////////////////

  static int failed=0;

  static boolean checkM(ContextMapping m, boolean in, String arg, String exp)
  {
    try {
      QualifiedMandelName res;
      QualifiedMandelName n=QualifiedMandelName._create(arg);
      if (in) res=m.mapIn(n);
      else    res=m.mapOut(n);
      if (res.toString().equals(exp)) {
        System.out.println((in?"in":"out")+": "+arg+" -> "+res+" (OK)");
        return true;
      }
      else {
        System.out.println(
          (in?"in":"out")+": "+arg+" -> "+res+" (FAILED) expected "+exp);
        failed++;
        return false;
      }
    }
    catch (IllegalArgumentException ex) {
      if (exp!=null) {
        failed++;
        System.out.println((in?"in":"out")+": "+arg+" -> <illegal> (FAILED) expected "
          +exp+" ("+ex.getMessage()+")");
      }
      else {
        System.out.println((in?"in":"out")+": "+arg+" -> <illegal> (OK)"
          +" ("+ex.getMessage()+")");
      }
      return exp==null;
    }
  }

  static boolean checkC(ContextMapping m, boolean in, String arg, String exp)
  {
    try {
      ColormapName res;
      ColormapName n=new ColormapName(arg);
      if (in) res=m.mapIn(n);
      else    res=m.mapOut(n);
      if (res.toString().equals(exp)) {
        System.out.println((in?"in":"out")+": "+arg+" -> "+res+" (OK)");
        return true;
      }
      else {
        System.out.println(
          (in?"in":"out")+": "+arg+" -> "+res+" (FAILED) expected "+exp);
        failed++;
        return false;
      }
    }
    catch (IllegalArgumentException ex) {
      if (exp!=null) {
        failed++;
        System.out.println((in?"in":"out")+": "+arg+" -> <illegal> (FAILED) expected "
          +exp+" ("+ex.getMessage()+")");
      }
      else {
        System.out.println((in?"in":"out")+": "+arg+" -> <illegal> (OK)"
          +" ("+ex.getMessage()+")");
      }
      return exp==null;
    }
  }

  public static void main(String[] args)
  {
    test();
  }
  
  public static int test()
  {
    System.out.println("starting MandelImageDBContext test...");
    failed=0;
    MandelImageDBContext root=new MandelImageDBContext(new File("root)"));
    MandelImageDBContext lvl1=new MandelImageDBContext(new File("lvl1)"));
    MandelImageDBContext lvl2=new MandelImageDBContext(new File("lvl2)"));
    MandelImageDBContext lvl2a=new MandelImageDBContext(new File("lvl2)"));

    lvl1.addContext(root, "root");
    ContextMapping m1=lvl1.getContextMapping(root);

    lvl2.addContext(root, "ROOT");
    lvl2.addContext(lvl1, "lvl1");
    ContextMapping m2=lvl2.getContextMapping(lvl1);
    try {
      lvl2.complete();
    }
    catch (IllegalConfigurationException ex) {
      System.out.println("illegal config "+ex);
      System.exit(1);
    }

    lvl2a.addContext(lvl1, "lvl1");
    ContextMapping m2a=lvl2a.getContextMapping(lvl1);

    //checkM(m1,false,"0","0@root");
    checkM(m1, false, "0", "0");
    checkM(m1, false, "abc", "abc@root");

    checkM(m1, false, "0-qual", "0-qual@root");
    checkM(m1, false, "abc-qual", "abc-qual@root");


    checkM(m1, true, "abc", null);
    //checkM(m1,true,"0@root","0");
    checkM(m1, true, "0", "0");
    checkM(m1, true, "abc@root", "abc");

    checkM(m1, true, "abc-qual", null);
    checkM(m1, true, "abc@root-qual", null);
    checkM(m1, true, "0-qual@root", "0-qual");
    //checkM(m1,true,"0@root-qual@root","0-qual");
    checkM(m1, true, "0-qual@root", "0-qual");
    checkM(m1, true, "abc-qual@root", "abc-qual");


    //checkM(m2,false,"0@root","0@ROOT");
    //checkM(m2,false,"0@root~a","0@ROOT~a@lvl1");
    checkM(m2, false, "0", "0");
    checkM(m2, false, "a", "a@lvl1");
    checkM(m2, false, "a@root", "a@ROOT");
    checkM(m2, false, "a@root~a", "a@ROOT~a@lvl1");

//     checkM(m2,true,"0@ROOT","0@root");
//     checkM(m2,true,"0@ROOT~a@lvl1","0@root~a");
//     checkM(m2,true,"0@ROOT~a@lvl1~a",null);
//     checkM(m2,true,"0@other",null);
    checkM(m2, true, "0", "0");
    checkM(m2, true, "a@lvl1", "a");
    checkM(m2, true, "a@ROOT", "a@root");
    checkM(m2, true, "a@ROOT~a@lvl1", "a@root~a");
    checkM(m2, true, "a@ROOT~a@lvl1~a", null);
    checkM(m2, true, "a@other", null);

//     checkM(m2,true,"0@ROOT-qual",null);
//     checkM(m2,true,"0@ROOT-qual@lvl1","0@root-qual");
    checkM(m2, true, "a@ROOT-qual", null);
    checkM(m2, true, "a@ROOT-qual@lvl1", "a@root-qual");
    checkM(m2, true, "a-qual@lvl1", "a-qual");
    checkM(m2, true, "a-qual@ROOT", "a-qual@root");

    //////////////////////////////////////////////////////////////////////
    checkC(m1, false, "cm1", "cm1@root");
    checkC(m1, true, "cm1@root", "cm1");

    //////////////////////////////////////////////////////////////////////
    checkM(m2a,false, "a@root~a","a@root~a@lvl1");
    checkM(m2a,true, "a@root~a@lvl1","a@root~a");
    checkM(m2a,true, "a@root","a@root");

    //////////////////////////////////////////////////////////////////////
    System.out.println("failed: "+failed);
    return failed;
  }
}
