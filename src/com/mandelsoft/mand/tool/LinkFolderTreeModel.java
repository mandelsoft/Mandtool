/*
 * Copyright 2021 Uwe Krueger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.DefaultMandelList;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.MandelListFolderTree;
import com.mandelsoft.util.HashList;
import java.util.HashSet;
import java.util.Set;
  
public class LinkFolderTreeModel extends DefaultMandelListFolderTreeModel {
  private boolean mod;
  private Set<LinkListener> llisteners=new HashSet<LinkListener>();

  public LinkFolderTreeModel(MandelListFolderTree tree, MandelScanner all,
                             boolean readonly)
  {
    super(tree, all);
    setModifiable(false);
    this.mod = !readonly;
  }

  @Override
  protected boolean isListModifiable(MandelListFolder f)
  {
    return mod;
  }

  ////////////////////////////////////////////////////////////////////////////
  public Set<MandelName> getLinkSources()
  {
    Set<MandelName> set=new HashSet<>();
    for (MandelListFolder f:getRoot()) {
      MandelName mn=MandelName.create(f.getName());
      if (mn!=null) set.add(mn);
    }
    return null;
  }
  
  public MandelListFolder getLinkFolder(MandelName n)
  {
    return getChild(getRoot(), n.getName());
  }
  
  public MandelList getLinks(MandelName n)
  {
    MandelListFolder f = getLinkFolder(n);
    if (f==null) return new DefaultMandelList();
    return f.getMandelList();
  }
  
  public MandelList getLinkClosure(MandelName n)
  {
    MandelList list = new DefaultMandelList();
    HashList<MandelName> pending=new HashList<>();
    Set<MandelName> closure = new HashSet<>();
    list.add(new QualifiedMandelName(n));
    closure.add(n);
    pending.add(n);

    while (!pending.isEmpty()) {
      MandelName next = pending.get(0);
      pending.remove(next);
      MandelListFolder f = getLinkFolder(next);
      if (f != null) {
        for (QualifiedMandelName qmn : f.getMandelList()) {
          MandelName mn = qmn.getMandelName();
          if (!closure.contains(mn)) {
            closure.add(mn);
            pending.add(mn);
            list.add(new QualifiedMandelName(mn));
          }
        }
      }
    }
    return list;
  }
  
  public MandelListModel getLinkModel(MandelName n)
  {
    MandelListFolder f = getLinkFolder(n);
    if (f == null) {
      return null;
    }
    return getMandelListModel(f);
  }

  public void addLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (!mod) return;
    done|=_addLink(src,dst);
    done|=_addLink(dst,src);
    if (done) handleAddLink(src,dst);
  }

  public void removeLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (!mod) return;
    done|=_removeLink(src,dst);
    done|=_removeLink(dst,src);
    if (done) handleRemoveLink(src,dst);
  }

  private boolean _removeLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (!mod) return done;
    MandelListFolder f = getLinkFolder(src);
    if (f!=null) {
      MandelListModel m=getMandelListModel(f);
      QualifiedMandelName qn=new QualifiedMandelName(dst);
      done=f.contains(qn);
      m.remove(qn);
      if (m.getList().isEmpty()) {
        removeFolder(f);
      }
    }
    return done;
  }

  private boolean _addLink(MandelName src, MandelName dst)
  { boolean done=false;

    if (!mod) return done;
     MandelListFolder f = getLinkFolder(src);
     if (f==null) {
       f=insertFolder(src.getName(), getRoot());
       f.setThumbnailName(new QualifiedMandelName(src));
     }
     QualifiedMandelName qn=new QualifiedMandelName(dst);
     done=!f.contains(qn);
     add(f, qn);
     return done;
  }
  
  public void addLinkListener(LinkListener h)
  {
    llisteners.add(h);
  }

  public void removeLinkListener(LinkListener h)
  {
    llisteners.remove(h);
  }

  private void handleAddLink(MandelName src, MandelName dst)
  { 
    for (LinkListener h:llisteners) {
      h.linkAdded(src,dst);
    }
  }

  private void handleRemoveLink(MandelName src, MandelName dst)
  {
    for (LinkListener h:llisteners) {
      h.linkRemoved(src,dst);
    }
  }
}
