/*
 * Copyright 2021 D021770.
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

import static com.mandelsoft.swing.BufferedComponent.toInt;
import com.mandelsoft.swing.Scale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 *
 * @author D021770
 */
public class DynamicColor implements Decoration.ColorHandler {
  public interface ImageSource {
    public BufferedImage getImage();
    public Scale getScale();
  }
 
  public static class StaticImage implements ImageSource {
    private BufferedImage image;
    
    public StaticImage(BufferedImage image) {
      this.image=image;
    }
    
    @Override
    public BufferedImage getImage()
    {
      return image;
    }
    
    @Override
    public Scale getScale()
    {
      return Scale.One;
    }
  }
  
  private ImageSource source;
  
  public DynamicColor(ImageSource source) {
    this.source=source;
  }
  
  @Override
  public Color getColor(int x, int y, int w, int h)
  {
    BufferedImage image=source.getImage();
    Scale scale=source.getScale();
   // System.out.printf("determine font color for (%d,%d))[%d,%d]\n", x,y,w,h);
    x=(int)(x/scale.getX());
    y=(int)(y/scale.getY());
    w=(int)(w/scale.getX());
    h=(int)(h/scale.getY());
    
    if (h==0) h=1;
    if (w==0) w=1;
    if (x+w > image.getWidth()) x=image.getWidth()-w;
    if (y>image.getHeight()) y=image.getHeight();
    
    System.out.printf("determine font color for (%d,%d))[%d,%d] (scale %s)\n", x,y,w,h,scale);
    int sum = 0;
    
    ColorModel m = image.getColorModel();
    int[] values = image.getRGB(x, y-h, w, h, null, 0, w);
    for (int v : values) {
      sum += m.getRed(v) + m.getGreen(v) + m.getBlue(v);
    }
    if (sum / 3 / values.length > 128) {
      return Color.BLACK;
    }
    else {
      return Color.WHITE;
    }
  }
}
