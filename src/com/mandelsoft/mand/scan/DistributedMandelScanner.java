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

package com.mandelsoft.mand.scan;

import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelImageDB;
import com.mandelsoft.mand.MandelImageDBContext;
import com.mandelsoft.mand.MandelImageDBContext.ContextMapping;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public class DistributedMandelScanner extends CompoundMandelScannerSupport
                                      implements ContextMandelScanner {

  public interface ScannerAccess {
    MandelScanner getScanner(MandelImageDB db);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Scanner
  ///////////////////////////////////////////////////////////////////////////

  private MandelImageDBContext root;
  private MandelScanner scanner;
  private Map<MandelImageDB,MandelScanner> nested;
  private Map<String,List<MandelScanner>> scanners;
  private ScannerAccess access;

  public DistributedMandelScanner(MandelImageDBContext ctx, ScannerAccess acc)
  {
    this.root=ctx;
    this.access=acc;
    this.scanner=access.getScanner(ctx.getDatabase());
    if (scanner!=null) addScanner(scanner);

    nested=new HashMap<MandelImageDB,MandelScanner>();
    scanners=new HashMap<String,List<MandelScanner>>();

    for (ContextMapping m:root.mappings()) {
      MandelImageDBContext sctx=m.getContext();
      MandelImageDB sdb=sctx.getDatabase();
      MandelScanner s=access.getScanner(sdb);
      if (s!=null) {
        s=new MappedMandelScanner(s,ctx.getContextMapping(sctx));
        nested.put(sdb, s);
        addScanner(s);
      }
    }
    for (ContextMapping m:root.mappings()) {
      String label=m.getLabel();
      MandelImageDBContext sub=m.getContext();
      List<MandelScanner> subs=new ArrayList<MandelScanner>();
      if (scanner!=null) subs.add(scanner);
      for (MandelImageDBContext c:sub.containers()) {
        if (sub!=ctx) {
          MandelScanner s=nested.get(sub.getDatabase());
          if (s!=null) subs.add(s);
        }
      }
      scanners.put(label, subs);
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // basic methods to be implemented for support class

  @Override
  protected Set<MandelHandle> _getMandelHandles(MandelName name)
  {
    String label=name.getLabel();
    if (label==null) {
      Set<MandelHandle> set=scanner.getMandelHandles(name);
      if (name.isRoot()) {
        for (String l: scanners.keySet()) {
          add(set, _getMandelHandles(scanners.get(l),MandelName.ROOT));
        }
      }
      return set;
    }
    return _getMandelHandles(scanners.get(label),name);
  }

  //////////////////////////////////////////////////////////////////////////

  @Override
  protected Set<ColormapHandle> _getColormapHandles(ColormapName name)
  {
    String label=name.getLabel();
    if (label==null) return scanner.getColormapHandles(name);
    return _getColormapHandles(scanners.get(label),name);
  }


  //////////////////////////////////////////////////////////////////////////
  // optimized access
  //////////////////////////////////////////////////////////////////////////


  @Override
  public MandelHandle getMandelData(MandelName name)
  {
    String label=name.getLabel();
    if (label==null) {
      MandelHandle h=scanner.getMandelData(name);
      if (h==null && name.isRoot()) {
        for (String l: scanners.keySet()) {
          h=_getMandelData(scanners.get(l),MandelName.ROOT);
          if (h!=null) {
            break;
          }
        }
      }
      return h;
    }
    return _getMandelData(scanners.get(label),name);
  }

  @Override
  public MandelHandle getMandelData(QualifiedMandelName name)
  {
    String label=name.getLabel();
    if (label==null) {
      MandelHandle h=scanner.getMandelData(name);
      if (h==null && name.isRoot()) {
        for (String l: scanners.keySet()) {
          h=_getMandelData(scanners.get(l),QualifiedMandelName.ROOT);
          if (h!=null) {
            break;
          }
        }
      }
      return h;
    }
    return _getMandelData(scanners.get(label),name);
  }

  @Override
  public MandelHandle getMandelHandle(QualifiedMandelName name)
  {
    String label=name.getLabel();
    if (label==null) {
      MandelHandle h=scanner.getMandelHandle(name);
      if (h==null && name.isRoot()) {
        for (String l: scanners.keySet()) {
          h=_getMandelHandle(scanners.get(l),QualifiedMandelName.ROOT);
          if (h!=null) {
            break;
          }
        }
      }
      return h;
    }
    return _getMandelHandle(scanners.get(label),name);
  }

  @Override
  public synchronized Set<MandelHandle> getMandelHandles(MandelName name)
  {
    return _getMandelHandles(name);
  }

  @Override
  public Set<MandelHandle> getMandelHandles(QualifiedMandelName name)
  {
    return super.getMandelHandles(name);
  }

  @Override
  public MandelHandle getMandelInfo(MandelName name)
  {
    String label=name.getLabel();
    if (label==null) {
      MandelHandle h=scanner.getMandelInfo(name);
      if (h==null && name.isRoot()) {
        for (String l: scanners.keySet()) {
          h=_getMandelInfo(scanners.get(l),MandelName.ROOT);
          if (h!=null) {
            break;
          }
        }
      }
      return h;
    }
    return _getMandelInfo(scanners.get(label),name);
  }

  @Override
  public MandelHandle getMandelInfo(QualifiedMandelName name)
  {
    String label=name.getLabel();
    if (label==null) {
      MandelHandle h=scanner.getMandelInfo(name);
      if (h==null && name.isRoot()) {
        for (String l: scanners.keySet()) {
          h=_getMandelInfo(scanners.get(l),QualifiedMandelName.ROOT);
          if (h!=null) {
            break;
          }
        }
      }
      return h;
    }
    return _getMandelInfo(scanners.get(label),name);
  }

  ///////////////////////////////////////////////////////////////////////////

  @Override
  public synchronized Set<ColormapHandle> getColormapHandles(ColormapName name)
  {
    String label=name.getLabel();
    if (label==null) return scanner.getColormapHandles(name);
    MandelImageDBContext ctx=root.getContext(label);
    if (ctx!=null) {
      return nested.get(ctx.getDatabase()).getColormapHandles(name);
    }
    return new HashSet<ColormapHandle>();
  }

  @Override
  public synchronized boolean hasColormap(ColormapName name)
  {
    String label=name.getLabel();
    if (label==null) return scanner.hasColormap(name);
    MandelImageDBContext ctx=root.getContext(label);
    if (ctx!=null) {
      return nested.get(ctx.getDatabase()).hasColormap(name);
    }
    return false;
  }

  ///////////////////////////////////////////////////////////////////////////
  // EnvScanner
  ///////////////////////////////////////////////////////////////////////////

  public MandelImageDBContext getContext()
  {
    return root;
  }

  public Set<MandelName> getSubNames(MandelName n)
  {
    return MandelScannerUtils.getSubNames(n, root, this);
  }

  public Set<MandelName> getSubNames(MandelName n, Filter f)
  {
    return MandelScannerUtils.getSubNames(n, root, this, f);
  }

  public boolean hasSubNames(MandelName n)
  {
    return MandelScannerUtils.hasSubNames(n, root, this);
  }

  public boolean hasSubNames(MandelName n, Filter f)
  {
    return MandelScannerUtils.hasSubNames(n, root, this, f);
  }


}
