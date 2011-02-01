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
package com.mandelsoft.mand.tools;

import com.mandelsoft.mand.IllegalConfigurationException;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.scan.MandelScannerUtils;
import com.mandelsoft.mand.util.MandArith;
import com.mandelsoft.mand.util.MandUtils;
import com.mandelsoft.mand.util.Move;

/**
 *
 * @author Uwe Krüger
 */
public class Join extends MandArith {
  static HashSet<MandelName> ignore=new HashSet<MandelName>();
  static HashSet<MandelName> abort=new HashSet<MandelName>();

  static {
    abort.add(MandelName.create("ba2b9a"));
    abort.add(MandelName.create("2ab6a"));
  }

  MandelScanner src;
  MandelScanner dst;
  File target;
  HashMap<MandelName, Relocation> moves=new HashMap<MandelName, Relocation>();
  Move move;
  boolean verbose;

  public static class Relocation {

    MandelName src;
    MandelName dst;
    String reason;

    public Relocation(MandelName src, MandelName dst, String reason)
    {
      this.src=src;
      this.dst=dst;
      this.reason=reason;
    }

    @Override
    public String toString()
    {
      return "move "+src+" to "+dst+": "+reason;
    }
  }

  public Join(MandelScanner src, MandelScanner dst, File target)
  {
    this.src=src;
    this.dst=dst;
    this.target=target;
    this.move=new Move(src,target,false);
  }

  public void setVerbose(boolean verbose)
  {
    this.verbose=verbose;
  }

  public void join()
  {
    MandelName n=MandelName.ROOT;

    handle(n);
  }

  private boolean equals(MandelData sd, MandelData dd)
  {
    MandelInfo si=sd.getInfo();
    MandelInfo di=dd.getInfo();

    if (si==null||di==null) return false;
    if (equals(si, di)) return true;
    MandUtils.round(si);
    MandUtils.round(di);
    return equals(si, di);
  }

  private boolean equals(MandelInfo si, MandelInfo di)
  {
    if (diff(si.getXM(),di.getXM(),di.getDX())>1) return false;
    if (diff(si.getYM(),di.getYM(),di.getDY())>1) return false;
    
    return true;
  }

  private String diff(MandelInfo si, MandelInfo di)
  {
    double dx=diff(si.getXM(),di.getXM(),di.getDX());
    double dy=diff(si.getYM(),di.getYM(),di.getDY());

    return dx+"x"+dy;
  }

  private double diff(BigDecimal a, BigDecimal b, BigDecimal p)
  {
    return mul(div(sub(a,b),p),100).abs().doubleValue();
  }

  private void handle(MandelName s)
  {
    MandelName next=MandUtils.getNextSubName(s, dst, false);
    MandelName n=s.sub();
    while (n!=null) {
      MandelData sd=MandelScannerUtils.getMandelInfo(src,n);
      MandelData dd=MandelScannerUtils.getMandelInfo(dst,n);
      if (sd!=null) {
        System.out.println("HANDLE "+n);
        if (dd==null) {
          if (n.getSubAreaChar()>next.getSubAreaChar()) next=n;
          next=move(n, next,"new");
        }
        else {
          if (!abort.contains(n)) {
            if (!ignore.contains(n) && !equals(sd, dd)) {
              next=move(n, next, diff(sd.getInfo(), dd.getInfo()));
            }
            else {
              handle(n);
            }
          }
        }
      }
      n=n.next();
    }
  }

  private MandelName move(MandelName s, MandelName d, String reason)
  {
    System.out.println("*** move "+s+" to "+d);
    moves.put(s, new Relocation(s,d,reason));
    if (!verbose) {
      move.move(s, d);
    }
    return d.next();
  }

  private void print()
  {
    for (MandelName s:moves.keySet()) {
      System.out.println(moves.get(s));
    }
  }
  ////////////////////////////////////////////////////////////////////////////

  static public void main(String[] args)
  {
    try {
      Environment src=new Environment(null, new File("F://Mandel2"));
      Environment dst=new Environment(null, new File("F://Mandel"));
      Join j=new Join(src.getMetaScanner(), dst.getMetaScanner(),
                      new File("F://Mandel/merge"));
      j.setVerbose(true);
      j.join();
      j.print();
    }
    catch (IllegalConfigurationException ex) {
      Command.Error("illegal config: "+ex);
    }
  }
}
