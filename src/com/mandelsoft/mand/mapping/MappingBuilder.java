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
package com.mandelsoft.mand.mapping;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Uwe Krüger
 */
public class MappingBuilder extends BalancedTreeSupport {

  private int minIt;
  private int maxIt;
  private int target;

  public MappingBuilder(int minIt, int maxIt, int target)
  {
    this.maxIt=maxIt;
    this.minIt=minIt;
    this.target=target;
  }

  public void setTarget(int target)
  {
    if (nodecount>0) throw new IllegalArgumentException("no target change during mapping");
    this.target=target;
  }

  public int getTargetSize()
  {
    return target;
  }

  public int getSourceSize()
  {
    return maxIt-minIt+1;
  }

  public int getMinIt()
  {
    return minIt;
  }

  public int getMaxIt()
  {
    return maxIt;
  }

  /////////////////////////////////////////////////////////////////////////
  private class Node extends TreeNode<Node>  {
    int value;
    int target;

    Node(int i, int c)
    {
      value=i;
      target=c;
    }

    public int compareTo(Node o)
    {
      if (this.value==o.value) return 0;
      if (this.value<o.value) return -1;
      return 1;
    }

    @Override
    public String toString()
    {
      return ""+value+"("+target+")";
    }
  }

  public MappingBuilder()
  {
  }

  public void add(int it, int target)
  {
    if (target>=this.target) {
      throw new IllegalArgumentException("illegal target size "+target+
                                         " max="+this.target);
    }
    root=add((Node)root, new Node(it, target));
  }

  private Node add(Node n, Node a)
  {
    if (n==null) {
      return a;
    }
    if (a.compareTo(n)==0) {
      throw new IllegalArgumentException("duplicate key "+a.value);
    }
    else {
      if (a.compareTo(n)>0) {
        n.left=add(n.left, a);
        n=n.balanceLeft();
      }
      else {
        n.right=add(n.right, a);
        n=n.balanceRight();
      }
      setDepth(n);
    }
    return n;
  }

  public int get(int i)
  {
    if (i>=getSourceSize()) {
      throw new IllegalArgumentException("illegal iteration "+i);
    }
    Node n=(Node)root;
    int upper=-1;
    while (n!=null) {
      if (i>n.value) n=n.left;
      else {
        upper=n.target;
        n=n.right;
      }
    }
    return upper;
  }

  //////////////////////////////////////////////////////////////////////////
  // creating tree mapping
  //////////////////////////////////////////////////////////////////////////

  TreeMapping createTreeMapping()
  {
    TreeMapping.Node tn=copy((Node)root);
    return new TreeMapping(tn);
  }

  private TreeMapping.Node copy(Node n)
  {
    if (n==null) return null;
    return  new TreeMapping.Node(n.value, n.target,
                                 copy(n.left), copy( n.right));
  }

  //////////////////////////////////////////////////////////////////////////
  // creating array mapping
  //////////////////////////////////////////////////////////////////////////

  ArrayMapping createArrayMapping()
  {
    int[] mapping=new int[getSourceSize()];
    fill(mapping,(Node)root,new Node(-1,-1));
//    for (int i=0; i<mapping.length;i++) {
//      System.out.println(""+i+": "+mapping[i]);
//    }
    return new ArrayMapping(mapping);
  }

  private static class Interval {
    Node top;
    Node bottom;

    public Interval(Node top, Node bottom)
    {
      this.top=top;
      this.bottom=bottom;
    }
  }

  private static Interval fill(int[] mapping, Node n, Node bottom)
  {
    Interval iv;
    Node top=n;
    Node ivtop=n;
    Node ivbottom=bottom;
    if (n.left!=null) {
      iv=fill(mapping,n.left,n);
      ivtop=iv.top;
      top=iv.bottom;
    }
    if (n.right!=null) {
      iv=fill(mapping,n.right,bottom);
      ivbottom=iv.bottom;
      bottom=iv.top;
    }
//    System.out.println("fill "+n.value+"->"+ivtop.value+","+ivbottom.value+
//             " ["+top.value+","+bottom.value+") with "+top.target);
    for (int i=top.value; i>bottom.value; i--) mapping[i]=top.target;
    return new Interval(ivtop,ivbottom);
  }

  //////////////////////////////////////////////////////////////////////////
  // main
  //////////////////////////////////////////////////////////////////////////

  static void compare(String msg, MappingTest a, MappingTest b)
  {
    System.out.println("comparing "+msg);
    if (a.getMinIt()!=b.getMinIt())
      System.out.println("MinIt mismatch: "+a.getMinIt()+"!="+b.getMinIt());
    if (a.getMaxIt()!=b.getMaxIt())
      System.out.println("MaxIt mismatch: "+a.getMaxIt()+"!="+b.getMaxIt());
    if (a.getTargetSize()!=b.getTargetSize())
      System.out.println("target mismatch: "+a.getTargetSize()+"!="+b.getTargetSize());
    for (int i=a.getMinIt(); i<=a.getMaxIt(); i++) {
      if (a.getColormapIndex(i)!=b.getColormapIndex(i))
        System.out.println("mapping mismatch: "+i+": "+
                a.getColormapIndex(i)+"!="+b.getColormapIndex(i));
    }
  }

  static private void write(MappingTest m, File f)
  {
    DataOutputStream dos=null;
    try {
      dos=new DataOutputStream(new FileOutputStream(f));
      try {
        m.write(dos, true);
        dos.flush();
      }
      finally {
        dos.close();
      }
    }
    catch (IOException ex) {
      System.out.println("cannot create "+f+": "+ex);
    }
  }

  static private MappingTest read(File f)
  {
    MappingTest m=new MappingTest();
    DataInputStream dis=null;
    try {
      dis=new DataInputStream(new FileInputStream(f));
      try {
        m.read(dis, true);
        return m;
      }
      finally {
        dis.close();
      }
    }
    catch (IOException ex) {
      System.out.println("cannot read "+f+": "+ex);
    }
    return null;
  }

  void print()
  {
    if (root!=null) root.print("");
  }
  
  static public void main(String[] args)
  {
    MappingBuilder mb=new MappingBuilder(1,20,8);
    mb.add(19, 7);
    mb.add(8, 6);
    mb.add(4, 5);
    mb.add(3, 4);
    mb.add(2, 3);
    mb.add(1, 2);
    mb.add(0, 1);

    mb.print();

    MappingRepresentation mra=mb.createArrayMapping();
    MappingRepresentation mrt=mb.createTreeMapping();

    MappingTest ma=new MappingTest(mb,mra);
    MappingTest mt=new MappingTest(mb,mrt);
    for (int i=0; i<mb.getSourceSize(); i++) {
      System.out.println(""+i+": "+mb.get(i)+"/ "+mra.getColormapIndex(i)+
                                             "/ "+mrt.getColormapIndex(i));
    }

    compare("ma<->mt",ma,mt);

    File f=new File("C:/work/AccuRev/test/testmapping");
    write(mt,f);
    MappingTest mr=read(f);
    System.out.println("read type "+mr.getType());
    compare("mr<->mt",mr,mt);
  }
}
