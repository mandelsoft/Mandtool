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
import com.mandelsoft.mand.util.FileMandelListListMandelListFolderTree;
import com.mandelsoft.mand.util.LeafNestedMandelListFolder;
import com.mandelsoft.mand.util.MandelListFolder;
import com.mandelsoft.mand.util.NestedMandelListFolder;
import com.mandelsoft.util.Utils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Uwe Krueger
 */
public class LinkTree extends FileMandelListListMandelListFolderTree {
  static private IO.Modifier modifier=new LinkModifier();

  static class LinkModifier extends IO.ModifierAdapter {
    //
    // don't write thumbnails
    //
    @Override
    public QualifiedMandelName getThumbnailName(MandelListFolder f)
    {
      return null;
    }

    @Override
    public void writeHeader(PrintWriter pw)
    {
      pw.println("#");
      pw.println("# list of inter area links");
      pw.println("#");
    }
  }

  //////////////////////////////////////////////////////////////////////////

  public LinkTree(AbstractFile f)
  {
    super("links",f);
  }

  @Override
  public void save() throws IOException
  {
    if (!getFile().isFile()) throw new UnsupportedOperationException("save on URL");
    write(new FileOutputStream(getFile().getFile()),modifier,getFile().getPath());
  }

  @Override
  protected NestedMandelListFolder createNestedFolder(String name)
  {
    return new Links(name);
  }

  private class Links extends LeafNestedMandelListFolder {
    private QualifiedMandelName qname;

    public Links(String name)
    {
      super(LinkTree.this,name);
      try {
        qname=QualifiedMandelName.create(name);
      }
      catch (IllegalArgumentException ia) {
        System.err.println("irgoring illegal mandel name "+name+" for link folder");
      }
    }

    @Override
    public QualifiedMandelName getThumbnailName()
    {
      return qname;
    }

    @Override
    public void setThumbnailName(QualifiedMandelName thumb)
    {
      if (!Utils.equals(qname, thumb)) {
        System.err.println("thumbnail cannot be set for link folder");
      }
    }
  }
}
