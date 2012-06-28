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

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

/**
 *
 * @author Uwe Krueger
 */
public class ScreenMovie {

  private static final double FRAME_RATE=50;
  private static final int SECONDS_TO_RUN_FOR=20;
  private static final String outputFilename="mydesktop.mp4";
  private static Dimension screenBounds;

  public static void main(String[] args)
  {
// let's make a IMediaWriter to write the file.
    final IMediaWriter writer=ToolFactory.makeWriter(outputFilename);
    screenBounds=Toolkit.getDefaultToolkit().getScreenSize();
// We tell it we're going to add one video stream, with id 0,
// at position 0, and that it will have a fixed frame rate of FRAME_RATE.
    writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
                          screenBounds.width/2, screenBounds.height/2);
    long startTime=System.nanoTime();
    for (int index=0; index<SECONDS_TO_RUN_FOR*FRAME_RATE; index++) {
// take the screen shot
      BufferedImage screen=getDesktopScreenshot();
      // convert to the right image type
      BufferedImage bgrScreen=convertToType(screen,
                                            BufferedImage.TYPE_3BYTE_BGR);
      // encode the image to stream #0
      writer.encodeVideo(0, bgrScreen, System.nanoTime()-startTime,
                         TimeUnit.NANOSECONDS);
// sleep for frame rate milliseconds
      try {
        Thread.sleep((long)(1000/FRAME_RATE));
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
// tell the writer to close and write the trailer if  needed
    writer.close();
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

  private static BufferedImage getDesktopScreenshot()
  {
    try {
      Robot robot=new Robot();
      Rectangle captureSize=new Rectangle(screenBounds);
      return robot.createScreenCapture(captureSize);
    }
    catch (AWTException e) {
      e.printStackTrace();
      return null;
    }

  }
}
