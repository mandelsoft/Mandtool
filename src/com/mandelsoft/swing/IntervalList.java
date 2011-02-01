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
package com.mandelsoft.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Uwe Krüger
 */

public class IntervalList implements Iterable<Interval2D> {
  private List<Interval2D> list=new ArrayList<Interval2D>();

  public IntervalList()
  {
  }

  public IntervalList(Interval2D iv)
  {
    this();
    add(iv);
  }

  public IntervalList(Collection<Interval2D> l)
  {
    this();
    for (Interval2D iv:l) add(iv);
  }

  public IntervalList(IntervalList l)
  {
    this();
    for (Interval2D iv:l) add(iv);
  }


  public Iterator<Interval2D> iterator()
  {
    return list.iterator();
  }

  public void add(Interval2D a)
  {
    Interval2D next=null;
    int in=0;
    int i;

    for (i=0; i<list.size(); i++) {
      Interval2D iv=list.get(i);
      if (iv.getMax()>=a.getMin()) {
        next=iv; // first potentially involved
        in=i;
        break;
      }
    }
    if (next==null) {
      list.add(a);
    }
    else {
      // join
      Interval2D end=null;
      int ie=list.size();
      for (i=in; i<list.size(); i++) {
        Interval2D iv=list.get(i);
        if (iv.getMin()>a.getMax()) {
          ie=i;
          break;
        }
        end=iv;
      }
      Interval2D n=new Interval2D(next.getMin()<a.getMin()?next.getMin():
                                                           a.getMin(),
                                  end==null?a.getMax():end.getMax());
      list.add(ie, n);
      while (ie-->in) list.remove(in);
    }
  }

  public void sub(Interval2D a)
  {
    Interval2D next=null;
    int in=0;
    int i;

    for (i=0; i<list.size(); i++) {
      Interval2D iv=list.get(i);
      if (iv.getMax()>=a.getMin()) {
        next=iv; // first potentially involved
        in=i;
        break;
      }
    }
    if (next!=null) {
      if (next.getMin()<a.getMin()) {
        list.add(in++,new Interval2D(next.getMin(),a.getMin()));
      }
      for (; next!=null && next.getMax()<=a.getMax();) {
        list.remove(in);
        next=in<list.size()?list.get(in):null;
      }
      if (next!=null && next.getMin()<a.getMax()) {
        list.remove(in);
        list.add(in,new Interval2D(next.getMax(),a.getMax()));
      }
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb=new StringBuilder();
    for (Interval2D iv:list) {
      sb.append("[");
      sb.append(iv.getMin());
      sb.append("-");
      sb.append(iv.getMax());
      sb.append("]");
    }
    return sb.toString();
  }
  ////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    IntervalList list=new IntervalList();
    Interval2D i1=new Interval2D(3,4);
    list.add(i1);
    System.out.println("a: "+list);
    i1=new Interval2D(11,20);
    list.add(i1);
    System.out.println("b: "+list);
    i1=new Interval2D(6,8);
    list.add(i1);
    System.out.println("c: "+list);
    i1=new Interval2D(1,2);
    list.add(i1);
    System.out.println("d: "+list);
    i1=new Interval2D(2,3);
    list.add(i1);
    System.out.println("e: "+list);
    i1=new Interval2D(2,11);
    list.add(i1);
    System.out.println("f: "+list);
    ////
    i1=new Interval2D(6,8);
    list.sub(i1);
    System.out.println("sa: "+list);
    i1=new Interval2D(10,12);
    list.sub(i1);
    System.out.println("sb: "+list);
    i1=new Interval2D(5,9);
    list.sub(i1);
    System.out.println("sc: "+list);
    i1=new Interval2D(0,2);
    list.sub(i1);
    System.out.println("sd: "+list);
    i1=new Interval2D(2,3);
    list.sub(i1);
    System.out.println("se: "+list);
    i1=new Interval2D(19,21);
    list.sub(i1);
    System.out.println("sf: "+list);
    i1=new Interval2D(15,19);
    list.sub(i1);
    System.out.println("sg: "+list);
    i1=new Interval2D(4,13);
    list.sub(i1);
    System.out.println("sh: "+list);
    i1=new Interval2D(0,20);
    list.sub(i1);
    System.out.println("si: "+list);

  }
}
