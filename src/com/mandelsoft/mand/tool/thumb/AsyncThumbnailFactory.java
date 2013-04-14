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

import com.mandelsoft.mand.cm.ColormapSourceFactory;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.image.MandelImage.Factory;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krueger
 */
public class AsyncThumbnailFactory extends AbstractThumbnailFactory {

  ////////////////////////////////////////////////////////////////////////

  private MandelImageRequestQueue   queue;
  private Listener listener;

  public AsyncThumbnailFactory(Client client)
  {
    super(client);
    listener=new Listener();
    queue=new MandelImageRequestQueue() {
      @Override
      public MandelScanner getMandelScanner()
      {
        return AsyncThumbnailFactory.this.client.getMandelScanner();
      }
    };
  }

  public AsyncThumbnailFactory(Client client, int max)
  {
    this(client);
    this.maxcache=max;
  }

  public void setColormapSourceFactory(ColormapSourceFactory colmapfac)
  {
    queue.setColormapSourceFactory(colmapfac);
  }

  public ColormapSourceFactory getColormapSourceFactory()
  {
    return queue.getColormapSourceFactory();
  }

  public void setFactory(MandelImage.Factory factory)
  {
    queue.setFactory(factory);
  }

  public Factory getFactory()
  {
    return queue.getFactory();
  }
  
  
  public BufferedImage getThumbnail(QualifiedMandelName n, Dimension max)
  {
    BufferedImage image=requestThumbnail(n,max).getImage();

    if (image!=null) {
      lifo.remove(n);
      lifo.add(n);
      if (lifo.size()>maxcache) {
        remove(lifo.get(0));
      }
    }

    return image;
  }

  public ImageSource<QualifiedMandelName> requestThumbnail(QualifiedMandelName n, Dimension max)
  {
    ImageSource<QualifiedMandelName> src;
    BufferedImage image;

    if (queue.getMaxSize()==null || !max.equals(queue.getMaxSize())) {
      thumbnails.clear();
      queue.clear();
      lifo.clear();
      queue.setMaxSize(max);
    }

    src=thumbnails.get(n);
    if (src==null) {
      src=queue.requestImage(n);
      src.addImageChangeListener(listener);
      thumbnails.put(n, src);
    }
    else {
      queue.reschedule(src);
    }
    return src;
  }
  
  private class Listener implements ImageChangeListener<QualifiedMandelName> {
    public void imageChanged(ImageSource<QualifiedMandelName> c)
    {
      fireImageChanged(c);
    }
  }
}
