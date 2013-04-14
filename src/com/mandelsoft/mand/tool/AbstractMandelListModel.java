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

package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.cm.ColormapSourceFactory;
import com.mandelsoft.mand.tool.thumb.ImageSource;
import com.mandelsoft.swing.ThumbnailListener;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.AbstractListModel;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.MandelImage.Factory;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.thumb.AsyncThumbnailFactory;
import com.mandelsoft.mand.tool.thumb.ImageChangeListener;
import com.mandelsoft.swing.ThumbnailListModel;
import com.mandelsoft.swing.ThumbnailListenerSupport;

/**
 *
 * @author Uwe Krueger
 */
public abstract class AbstractMandelListModel<E> extends AbstractListModel
                                              implements ThumbnailListModel<E> {
  final protected AsyncThumbnailFactory factory;
  final private ThumbnailListenerSupport<E> listeners;
  final private Listener listener;
  private boolean modifiable;

  protected AbstractMandelListModel()
  {
    listeners=new ThumbnailListenerSupport<E>();
    factory=new AsyncThumbnailFactory(
            new AsyncThumbnailFactory.Client() {

      public boolean usesThumbnail(QualifiedMandelName name)
      {
        return AbstractMandelListModel.this.usesThumbnail(name);
      }

      public MandelScanner getMandelScanner()
      {
        return AbstractMandelListModel.this.getMandelScanner();
      }
    });
    listener=new Listener();
    factory.addImageChangeListener(listener);
  }

  public void removeThumbnailListener(ThumbnailListener<E> l)
  {
    listeners.removeThumbnailListener(l);
  }

  public void addThumbnailListener(ThumbnailListener<E> l)
  {
    listeners.addThumbnailListener(l);
  }

  public void refresh()
  {
    refresh(false);
  }

  abstract public void refresh(boolean soft);
  
  public void setModifiable(boolean m)
  { this.modifiable=m;
  }

  public void setColormapSourceFactory(ColormapSourceFactory colmapfac)
  {
    factory.setColormapSourceFactory(colmapfac);
  }

  public ColormapSourceFactory getColormapSourceFactory()
  {
    return factory.getColormapSourceFactory();
  }

  public void setFactory(Factory factory)
  {
    this.factory.setFactory(factory);
  }


  public boolean isModifiable()
  {
    return modifiable;
  }

  protected void checkModifiable()
  {
    if (!isModifiable())
      throw new UnsupportedOperationException();
  }

  protected void fireRefresh(int old, int n)
  {
    if (old>n) {
      fireIntervalRemoved(this, n, old-1);
      if (n>0) fireContentsChanged(this, 0, n-1);
    }
    else {
      if (old>0) fireContentsChanged(this, 0, old-1);
      fireIntervalAdded(this, old, n-1);
    }
  }

  protected E lookup(ImageSource<QualifiedMandelName> src)
  {
     return lookupElement(src.getImageSpec());
  }
  //////////////////////////////////////////////////////////////////////////

  protected abstract boolean usesThumbnail(QualifiedMandelName name);
  protected abstract MandelScanner getMandelScanner();
  protected abstract QualifiedMandelName getQualifiedName(E elem);
  protected abstract E lookupElement(QualifiedMandelName name);

  public BufferedImage getThumbnail(int index, Dimension max)
  {
    QualifiedMandelName n=getQualifiedName((E)getElementAt(index));
    return factory.getThumbnail(n,max);
  }

  public BufferedImage getThumbnail(E element, Dimension max)
  {
    return factory.getThumbnail(getQualifiedName(element),max);
  }

  public void requestThumbnail(int index, Dimension max)
  {
    QualifiedMandelName n=getQualifiedName((E)getElementAt(index));
    factory.requestThumbnail(n,max);
  }

  public void requestThumbnail(E element, Dimension max)
  {
    factory.requestThumbnail(getQualifiedName(element),max);
  }
  
  protected void cleanupThumbnails()
  {
    //System.out.println("cleanup thumbs "+factory);
    factory.cleanupThumbnails();
  }

  ////////////////////////////////////////////////////////////////////////

  private class Listener implements ImageChangeListener<QualifiedMandelName> {

    public void imageChanged(ImageSource<QualifiedMandelName> c)
    {
      listeners.fireThumbnailChanged(AbstractMandelListModel.this, lookup(c), c.getImage());
    }
  }
}
