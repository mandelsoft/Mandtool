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
package com.mandelsoft.swing.colorchooser;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Uwe Krüger
 */

public class ColorImageModel  {
  public static final String PROP_FILENAME="filename";
  public static final String PROP_IMAGE="image";

  private PropertyChangeSupport listeners;

  private BufferedImage image;
  private String filename;

  public ColorImageModel()
  { this("");
  }

  public ColorImageModel(String filename)
  { this(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB),filename);
  }

  public ColorImageModel(BufferedImage image, String filename)
  { 
    this.image=image;
    this.filename=filename;
    listeners=new PropertyChangeSupport(this);
  }

  synchronized
  public String getFilename()
  {
    return filename;
  }

  synchronized
  public void setFilename(String filename)
  {
    String old=this.filename;
    this.filename=filename;
    firePropertyChange(PROP_FILENAME,old,filename);
  }

  synchronized
  public BufferedImage getImage()
  {
    return image;
  }

  synchronized
  public void setImage(BufferedImage image)
  {
    BufferedImage old=this.image;
    this.image=image;
    firePropertyChange(PROP_IMAGE,old,image);
  }

  synchronized
  public void removePropertyChangeListener(String propertyName,
                                           PropertyChangeListener listener)
  {
    listeners.removePropertyChangeListener(propertyName, listener);
  }

  synchronized
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    listeners.removePropertyChangeListener(listener);
  }

  synchronized
  public boolean hasListeners(String propertyName)
  {
    return listeners.hasListeners(propertyName);
  }

  synchronized
  public PropertyChangeListener[] getPropertyChangeListeners(String propertyName)
  {
    return listeners.getPropertyChangeListeners(propertyName);
  }

  protected void firePropertyChange(String propertyName, Object oldValue,
                                 Object newValue)
  {
    listeners.firePropertyChange(propertyName, oldValue, newValue);
  }

  synchronized
  public void addPropertyChangeListener(String propertyName,
                                        PropertyChangeListener listener)
  {
    listeners.addPropertyChangeListener(propertyName, listener);
  }

  synchronized
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    listeners.addPropertyChangeListener(listener);
  }
  
  public static void main(String argv[])
  {
    System.out.println("Hello!");
  }
}
