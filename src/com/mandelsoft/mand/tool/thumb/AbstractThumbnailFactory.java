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

package com.mandelsoft.mand.tool.thumb;

import com.mandelsoft.mand.QualifiedMandelName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Uwe Krueger
 */
public abstract class AbstractThumbnailFactory
                extends ImageChangeListenerSupport<QualifiedMandelName>
                implements ThumbnailFactory {

  protected Client client;
  protected List<QualifiedMandelName> lifo;
  protected int maxcache=20;
  protected HashMap<QualifiedMandelName, ImageSource<QualifiedMandelName>> thumbnails;

  protected AbstractThumbnailFactory(Client client)
  {
    this.client=client;
    thumbnails=new HashMap<QualifiedMandelName,ImageSource<QualifiedMandelName>>();
    lifo=new ArrayList<QualifiedMandelName>();
  }


  public void setMaxcache(int maxcache)
  {
    this.maxcache=maxcache;
  }

  public int getMaxcache()
  {
    return maxcache;
  }

  public void cleanupThumbnails()
  {
    Set<QualifiedMandelName> set=new HashSet<QualifiedMandelName>(thumbnails.keySet());
    for (QualifiedMandelName n:set) {
      if (!client.usesThumbnail(n)) {
        remove(n);
      }
    }
  }

  public void remove(QualifiedMandelName name)
  {
    ImageSource<QualifiedMandelName> src=thumbnails.get(name);
    if (src!=null) {
      src.cancel();
      thumbnails.remove(name);
    }
    lifo.remove(name);
  }
}
