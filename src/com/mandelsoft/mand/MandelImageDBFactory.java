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
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 *
 * @author Uwe Krueger
 */
public class MandelImageDBFactory {
  private Map<AbstractFile,MandelImageDBContext> cache;
  private Map<AbstractFile,MandelImageDBContext> aliases;
  private String tool;

  public MandelImageDBFactory(String tool)
  {
    this.tool=tool;
    this.cache=new HashMap<AbstractFile, MandelImageDBContext>();
    this.aliases=new HashMap<AbstractFile, MandelImageDBContext>();
  }

  public String getTool()
  {
    return tool;
  }

  public void addAlias(MandelImageDBContext ctx, AbstractFile f)
  {
    aliases.put(f,ctx);
  }

  public void put(AbstractFile root, MandelImageDBContext ctx)
  {
    cache.put(root, ctx);
  }

  public MandelImageDBContext get(AbstractFile root)
                                 throws IllegalConfigurationException
  {
    MandelImageDBContext ctx=_get(root, new Stack<AbstractFile>());
    ctx.complete();
    return ctx;
  }

  private MandelImageDBContext _get(AbstractFile root, Stack<AbstractFile> stack)
                                 throws IllegalConfigurationException
  {
    MandelImageDBContext ctx= cache.get(root);

    if (ctx==null) {
      ctx=aliases.get(root);
    }
    if (ctx==null) {
      ctx=create(root, stack);
    }
    return ctx;
  }

  private MandelImageDBContext create(AbstractFile root, Stack<AbstractFile> stack)
                                 throws IllegalConfigurationException
  {
    if (stack.contains(root)) {
      throw new IllegalArgumentException("cycle in nested dbs: "+root);
    }

    stack.push(root);
    try {
      MandelImageDB db=null;
      if (root.isFile()) {
        db=new MandelImageDB(this, root.getFile());
      }
      else {
        db=new MandelImageDB(this, root);
      }

      MandelImageDBContext ctx=new MandelImageDBContext(root, db);
//      put(root, ctx); // allow to find as nested ones to enable (experimental)
//                      // usage cycles (the stack ist not required anymore)

      // create proxy
      Proxy proxy=root.getProxy();
      if (proxy==null) {
        proxy=db.getProxy();
      }

      StringTokenizer t;
      // create aliases
      String aliases=db.getProperty(Settings.ALIASES);
      if (!Utils.isEmpty(aliases)) {
        System.out.println("found aliases: "+aliases);
        t=new StringTokenizer(aliases, ";");
        while (t.hasMoreTokens()) {
          String a=t.nextToken().trim();
          AbstractFile alias=AbstractFile.Factory.create(a, proxy, false);
          addAlias(ctx,alias);
        }
      }

      // create nested ones
      String path=db.getProperty(Settings.NESTED);
      if (!Utils.isEmpty(path)) {
        System.out.println("found nested dbs: "+path);
        t=new StringTokenizer(path, ";");
        while (t.hasMoreTokens()) {
          String p=t.nextToken().trim();
          if (p.equals("")) continue;
          String l=db.getProperty("db.location."+p);
          if (!Utils.isEmpty(l)) {
            AbstractFile sub=AbstractFile.Factory.create(l, proxy,
                                                         true);
            System.out.println("  location: "+l+" proxy: "+proxy);
            MandelImageDBContext nested=_get(sub,stack);
            ctx.addContext(nested, p);
          }
          else {
            throw new IllegalConfigurationException(root+": location missing for label "+p);
          }
        }
      }

      put(root, ctx); // regular put to avoid cycles (regular)
      return ctx;
    }
    finally {
      stack.pop();
    }
  }
}
