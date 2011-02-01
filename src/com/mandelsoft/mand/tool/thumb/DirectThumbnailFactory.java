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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.tool.AbstractMandelListModel;

/**
 *
 * @author Uwe Krueger
 */
public class DirectThumbnailFactory  extends AbstractThumbnailFactory  {
  protected Dimension thumbnailMax;
  protected MandelImage.Factory factory;

  public DirectThumbnailFactory(Client client)
  {
    super(client);
  }

  public DirectThumbnailFactory(Client client, int max)
  {
    this(client);
    this.maxcache=max;
  }

  public void setFactory(MandelImage.Factory factory)
  {
    this.factory=factory;
  }

  public MandelImage.Factory getFactory()
  {
    if (factory==null) factory=new MandelImage.Factory();
    return factory;
  }

  static private BufferedImage root;
  static private BufferedImage getDefaultImage()
  {
    if (root==null) {
      try {
        root=ImageIO.read(AbstractMandelListModel.class.
                getResourceAsStream("resc/0.png"));
      }
      catch (IOException ex) {
        System.out.println("cannot find root image from classpath");
      }
    }
    System.out.println("get default image: "+root);
    return root;
  }

  public BufferedImage getThumbnail(QualifiedMandelName n, Dimension max)
  {
    BufferedImage image=null;
    BufferedImage def=null;
    ImageSource src=null;

    if (thumbnailMax==null || !max.equals(thumbnailMax)) {
      thumbnails.clear();
      lifo.clear();
      thumbnailMax=max;
    }

    if (n==null) {
      def=getDefaultImage();
      if (def==null) return null;
    }
    else {
      src=thumbnails.get(n);
      if (src!=null) image=src.getImage();
    }

    if (image==null) {
      if (def==null&&n!=null) {
        MandelData md=null;
        MandelHandle mh=client.getMandelScanner().getMandelData(n);
        if (mh!=null) try {
          md=mh.getData();
        }
        catch (IOException ex) {
          assert false;
        }
        if (md!=null) {
          try {
            MandelImage mi=getFactory().getImage(md);
            if (mi!=null) {
              def=mi.getImage();
            }
          }
          catch (IOException ex) {
            System.out.println("no image available for "+n+": "+ex);
            def=getDefaultImage();
          }
        }
        else {
          System.out.println("no image available for "+n);
          def=getDefaultImage();
        }
      }
      if (def!=null) {
        int w=0;
        int h=0;
        if (def.getHeight()>def.getWidth()) {
          h=(int)thumbnailMax.getHeight();
          w=(int)(((double)def.getWidth())
                  /((double)def.getHeight())*h);
        }
        else {
          w=(int)thumbnailMax.getWidth();
          h=(int)(((double)def.getHeight())
                  /((double)def.getWidth())*w);
        }
        image=new BufferedImage(w, h,
                                BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();
        g.drawImage(def, 0, 0, w, h, null);
        thumbnails.put(n, new ConstantImageSource<QualifiedMandelName>(image,n));
      }
    }

    if (image!=null) {
      lifo.remove(n);
      lifo.add(n);
      if (lifo.size()>maxcache) {
        remove(lifo.get(0));
      }
    }

    return image;
  }
}
