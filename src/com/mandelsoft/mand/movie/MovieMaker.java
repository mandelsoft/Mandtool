/*
 *  Copyright 2012 Uwe Krueger.
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
package com.mandelsoft.mand.movie;

import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.IllegalConfigurationException;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.Settings;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.image.MandelImage;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.tool.Decoration;
import com.mandelsoft.mand.tools.Command;
import com.mandelsoft.mand.util.MandUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

/**
 *
 * @author Uwe Krueger
 */
public class MovieMaker {

  private static final int GAP=5000;
  private static final int FRAME_RATE=25;
  private static final int MILLISECONDS_PER_FRAME=1000/FRAME_RATE;
  private static final int ALPHA_TIME=2000;
  private static final double ALPHA_DELTA=255.0/((double)ALPHA_TIME/MILLISECONDS_PER_FRAME);

  private static String outputFilename="zoom.mp4";
  private static double scale=1;

  public static MandelImage getImage(MandelScanner scanner,
                                     MandelImage.Factory f,
                                     MandelName n)
  {
    MandelHandle h=scanner.getMandelData(n);
    if (h==null) return null;
    try {
      MandelData md=h.getData();
      MandelData pure=new MandelData(md.getInfo());
      pure.setRaster(md.getRaster());
      MandelImage image=f.getImage(pure);
      //f.setColormapSource(image.getColormap());
      return image;
    }
    catch (IOException ex) {
      System.out.println("cannt read "+h.getFile()+": "+ex);
      return null;
    }
  }

  private static class MovieWriter {
    IMediaWriter writer;
    double fps;
    MovieDecoration deco;

    public MovieWriter(int fps, int width, int height, String outputFilename)
    {
      this.fps=fps;
      writer=ToolFactory.makeWriter(outputFilename);
      writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
                          IRational.make(fps, 1),
                          width, height);
    }

    public void setDecoration(MovieDecoration deco)
    {
      this.deco=deco;
    }

    public void encodeVideo(long time, BufferedImage im)
    {
      BufferedImage bgr=convertToType(im, BufferedImage.TYPE_3BYTE_BGR);
      if (deco!=null && deco.isActive(time)) {
        if (bgr==im) {
          bgr=new BufferedImage(im.getWidth(),
                                im.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
          bgr.getGraphics().drawImage(im, 0, 0, null);
        }
        Graphics g=bgr.getGraphics();
        deco.paintDecoration(time, g, bgr.getWidth(), bgr.getHeight());
      }
      writer.encodeVideo(0, bgr, time, TimeUnit.MILLISECONDS);
    }

    public void close()
    {
      writer.close();
    }
  }

  public static void main(String[] args)
  {
    try {
      outputFilename=new File(".").getCanonicalFile().getName()+".mp4";
      System.out.println("OUT: "+outputFilename);
    }
    catch (IOException ex) {
      Command.Error("cannot determine directory name");
    }

    Environment env=null;
    try {
      env=new Environment(args);
    }
    catch (IllegalConfigurationException ex) {
      System.out.println("cannot initial image base: "+ex);
      System.exit(1);
    }

    Decoration decoration=new Decoration();
    decoration.setShowDecoration(true);
    decoration.setDecoration(env.getCopyright());

    Decoration mag=new Decoration("magnification");
    mag.setShowDecoration(true);
    mag.setFont("Times Roman Bold-20");

    Decoration url=new Decoration("url");
    url.setHAlign(Decoration.ALIGN_LEFT);
    url.setFont("Times Roman-15");
    String u=env.getProperty(Settings.HOMEPAGE);
    if (u!=null) {
      url.setDecoration(u);
      url.setShowDecoration(true);
    }


    MandelScanner scanner=env.getImageDataScanner();
    Colormap cm=env.getDefaultColormap();
    MandelImage.Factory f=new MandelImage.Factory(cm);
    MandelName name=MandelName.ROOT;

    MandelImage image=getImage(scanner,f,name);
    SimpleMovieDecoration mmag;

    MovieWriter mw=new MovieWriter(FRAME_RATE,
                                   (int)(image.getImage().getWidth()*scale),
                                   (int)(image.getImage().getHeight()*scale),
                                   outputFilename);
    CompoundMovieDecoration d=new CompoundMovieDecoration();
    d.addMovieDecoration(new SimpleMovieDecoration(decoration,0,1000,4000,4000));

    VerticalListMovieDecoration v=new VerticalListMovieDecoration(40,0, 500, 4000,4000);
    
    v.addDecoration(createTitleLine("The Beauty"));
    v.addDecoration(createTitleLine("of Chaos"));
    d.addMovieDecoration(v);
    d.addMovieDecoration(mmag=new SimpleMovieDecoration(mag,9000,1000,Long.MAX_VALUE,GAP/3));
    mw.setDecoration(d);


    long time=0;
    double alpha=255;
    BufferedImage im=image.getImage();

    while (time<GAP) {
      mw.encodeVideo(time, im);
      time+=MILLISECONDS_PER_FRAME;
    }

    while (image!=null) {
// take the screen shot
      im=image.getImage();
      mag.setDecoration("10^-"+MandUtils.getMagnification(image.getInfo()));
      mw.encodeVideo(time, im);
      time+=MILLISECONDS_PER_FRAME;

      name=name.sub('z');
      image=getImage(scanner,f,name);
      if (image==null) {
        long end=time+GAP;
        mmag.setShowTime(time-mmag.getStart()-mmag.getFadeInTime());
        d.addMovieDecoration(new SimpleMovieDecoration(url,time,GAP/3,GAP,0));
        while (time<end) {
           mw.encodeVideo(time, im);
           time+=MILLISECONDS_PER_FRAME;
        }
      }
    }
// tell the writer to close and write the trailer if  needed
    mw.close();
  }

  public static Decoration createTitleLine(String txt)
  {
    Decoration t=new Decoration(txt);
    t.setHAlign(Decoration.ALIGN_CENTER);
    t.setVAlign(Decoration.ALIGN_CENTER);
    t.setHInset(80);
    t.setVInset(-5);
    t.setFontSize(45);
    t.setColor(new Color(0x77,0x77,0));
    t.setShowDecoration(true);
    return t;
  }

  public static BufferedImage convertToType(BufferedImage sourceImage,
                                            int targetType)
  {
    BufferedImage image;
    // if the source image is already the target type, return the source image
    if (sourceImage.getType()==targetType) {
      image=sourceImage;
    }
// otherwise create a new image of the target type and draw the new image
    else {
      image=new BufferedImage(sourceImage.getWidth(),
                              sourceImage.getHeight(), targetType);
      image.getGraphics().drawImage(sourceImage, 0, 0, null);
    }
    return image;
  }
}
