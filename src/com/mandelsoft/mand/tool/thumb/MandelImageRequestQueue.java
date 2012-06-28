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
import com.mandelsoft.mand.cm.ColormapSource;
import com.mandelsoft.mand.cm.ColormapSourceFactory;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.image.MandelImage.Factory;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.AbstractMandelListModel;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelImageRequestQueue<E> {
  private List<MandelImageSource> requests;
  private Dimension maxSize;
  private MandelImage.Factory factory;
  private ColormapSourceFactory colmapfac;
  private Worker worker;

  public MandelImageRequestQueue()
  {
    this.requests=new ArrayList<MandelImageSource>();
  }

  public MandelImageRequestQueue(Factory factory,
                                 ColormapSourceFactory colmapfac)
  {
    this.factory=factory;
    this.colmapfac=colmapfac;
  }

  public abstract MandelScanner getMandelScanner();

  synchronized
  public void setMaxSize(Dimension maxSize)
  {
    this.maxSize=maxSize;
  }

  synchronized
  public Dimension getMaxSize()
  {
    return maxSize;
  }

  public ColormapSourceFactory getColormapSourceFactory()
  {
    return colmapfac;
  }

  public void setColormapSourceFactory(ColormapSourceFactory colmapfac)
  {
    this.colmapfac=colmapfac;
  }

  synchronized
  public void setFactory(MandelImage.Factory factory)
  {
    this.factory=factory;
  }

  synchronized
  public MandelImage.Factory getFactory()
  {
    if (factory==null) factory=new MandelImage.Factory();
    return factory;
  }

  synchronized
  public void clear()
  {
    requests.clear();
  }

  synchronized
  public ImageSource<QualifiedMandelName> requestImage(QualifiedMandelName n)
  {
     MandelImageSource src=new MandelImageSource(n);
     requests.add(0,src);
     if (worker==null) {
       worker=new Worker();
       worker.execute();
     }
     return src;
  }

  synchronized
  public void reschedule(ImageSource<QualifiedMandelName> src)
  {
    if (requests.remove(src)) {
      requests.add(0, (MandelImageSource)src);
    }
  }

  synchronized
  public void removeRequest(ImageSource<QualifiedMandelName> req)
  {
    requests.remove(req);
  }
  
  synchronized
  private MandelImageSource getNextRequest()
  {
    if (requests.size()==0) {
      worker=null;
      return null;
    }
    MandelImageSource src=requests.get(0);
    requests.remove(0);
    return src;
  }

  ///////////////////////////////////////////////////////////////////////
  static private BufferedImage root;

  synchronized
  static private BufferedImage getDefaultImage()
  {
    if (root==null) {
      try {
        root=ImageIO.read(AbstractMandelListModel.class.
                getResourceAsStream("resc/0.png"));
        System.out.println("get default image: "+root);
      }
      catch (IOException ex) {
        System.out.println("cannot find root image from classpath");
      }
    }
    return root;
  }

  synchronized
  private BufferedImage getImage(QualifiedMandelName n)
  {
    BufferedImage image=null;
    ImageSource src=null;

    if (n==null) {
      return null;
    }

    MandelHandle mh=getMandelScanner().getMandelData(n);
    if (mh!=null) {
      try {
        ColormapSource cms=null;
        if (colmapfac!=null && !mh.getHeader().hasColormap()) {
          cms=colmapfac.getColormapSource(n);
        }
        MandelImage mi=getFactory().getImage(mh.getData(),cms);
        if (mi!=null) {
          image=mi.getImage();
        }
      }
      catch (IOException ex) {
        System.out.println("no image available for "+n+": "+ex);
      }
    }
    else {
      System.out.println("no image available for "+n);
    }

    if (image!=null) {
      return resize(image);
    }
    return null;
  }

  private BufferedImage resize(BufferedImage src)
  {
    if (src!=null && maxSize!=null) {
      int w=0;
      int h=0;
      if (src.getHeight()>src.getWidth()) {
        h=(int)maxSize.getHeight();
        w=(int)(((double)src.getWidth())
                /((double)src.getHeight())*h);
      }
      else {
        w=(int)maxSize.getWidth();
        h=(int)(((double)src.getHeight())
                /((double)src.getWidth())*w);
      }
      BufferedImage image=new BufferedImage(w, h,
                              BufferedImage.TYPE_INT_RGB);
      Graphics2D g=image.createGraphics();
      g.drawImage(src, 0, 0, w, h, null);
      return image;
    }
    return src;
  }

  ///////////////////////////////////////////////////////////////////////

  private class MandelImageSource extends AbstractImageSource<QualifiedMandelName> {
    private QualifiedMandelName spec;

    public MandelImageSource(QualifiedMandelName spec)
    {
      this.spec=spec;
      setImage(resize(getDefaultImage()));
    }

    public QualifiedMandelName getImageSpec()
    {
      return spec;
    }

    @Override
    public void cancel()
    {
      removeRequest(this);
    }

    MandelImageRequestQueue getQueue()
    {
      return MandelImageRequestQueue.this;
    }
  }

  //////////////////////////////////////////////////////////////////////////

  private class Result {
    MandelImageSource src;
    BufferedImage image;

    public Result(MandelImageSource src, BufferedImage image)
    {
      this.src=src;
      this.image=image;
    }
  }

  private class Worker extends SwingWorker<Void, Result> {

    @Override
    protected Void doInBackground() throws Exception
    {
      MandelImageSource src;

      while ((src=getNextRequest())!=null) {
        Result r=new Result(src,getImage(src.getImageSpec()));
        this.publish(r);
        Thread.sleep(10);
        //Thread.yield();
      }
      return null;
    }

    @Override
    protected void done()
    {
     
    }

    @Override
    protected void process(List<Result> chunks)
    {
      if (chunks!=null) {
        for (Result r:chunks) {
          if (r.image!=null) r.src.setImage(r.image);
        }
      }
    }
  }
}
