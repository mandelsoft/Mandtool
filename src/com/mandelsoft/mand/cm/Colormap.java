

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

package com.mandelsoft.mand.cm;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.ChangeListener;
import com.mandelsoft.util.StateChangeSupport;

/**
 *
 * @author Uwe Krueger
 */
public class Colormap extends StateChangeSupport
                      implements ColormapSource {

  static public boolean debug=false;

  public static abstract class ChangeHandler implements ChangeListener {

    public void stateChanged(ChangeEvent e)
    {
      handle((Colormap)e.getSource());
    }
    
    abstract public void handle(Colormap cm);
  }

  
  static final public int VERSION=1;

  private int version=VERSION;
  private int size;
  private Color[] colormap;
   
  protected Colormap(int size, boolean setup)
  { this.size=size;
    if (debug) System.out.println("create color map size "+size);
    colormap=new Color[size];
    if (setup) setup();
  }
  
  public Colormap(int size)
  { this(size,true);
  }
  

  public Colormap(Colormap map)
  { this(map.getSize(),false);
    for (int i=0; i<size; i++) colormap[i]=map.colormap[i];
  }

  public Colormap(DataInputStream dis) throws IOException
  { this(dis, true);
  }

  public Colormap(DataInputStream dis, boolean verbose) throws IOException
  { read(dis, verbose);
  }
  
  protected void setup()
  { int max=256*256*256-1;
    int n=size-1;
    
    for (int i=0; i<=n; i++) {
      colormap[i]=new Color(max*i/n);
    }
  }

  public Colormap getColormap()
  {
    return this;
  }

  public int getSize()
  { return size;
  }
  
  public Color getColor(int i)
  { return colormap[i];
  }

  public int getRed(int i)
  { return colormap[i].getRed();
  }

  public int getGreen(int i)
  { return colormap[i].getGreen();
  }

  public int getBlue(int i)
  { return colormap[i].getBlue();
  }

  public void setColor(int i, Color c)
  { colormap[i]=c;
    modify();
  }
  
  public void setColor(int i, int rgb)
  { setColor(i, new Color(rgb));
  }
  
  public void setRed(int i, int v)
  { Color old=colormap[i];
    colormap[i]=new Color(v,old.getGreen(),old.getBlue());
    modify();
  }
   
  public void setGreen(int i, int v)
  { Color old=colormap[i];
    colormap[i]=new Color(old.getRed(),v,old.getBlue());
    modify();
  }
   
  public void setBlue(int i, int v)
  { Color old=colormap[i];
    colormap[i]=new Color(old.getRed(),old.getGreen(),v);
    modify();
  }


  ///////////////////////////////////////////////////////////////
  // modification handling
  ///////////////////////////////////////////////////////////////

  private boolean modifying;
  private boolean modified;

  public void endModification()
  {
    modifying=false;
    if (modified) {
      modify();
    }
  }

  protected void modify()
  {
    if (!modifying) {
      modified=false;
      fireChangeEvent();
    }
    else {
      modified=true;
    }
  }

  public void startModification()
  {
    modifying=true;
  }

  
  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  public boolean needsVersionUpdate()
  { return version!=VERSION;
  }

  public void write(DataOutputStream dos) throws IOException
  { write(dos,true);
  }

  public void write(DataOutputStream dos, boolean verbose) throws IOException
  { write(dos,VERSION, verbose);
  }
  
  public void write(DataOutputStream dos, int v, boolean verbose)
                throws IOException
  { 
    if (verbose) System.out.println("  writing colormap ("+size+")...");
    switch (v) {
       case 1: dos.writeInt(v);
               writeV1(dos);
               break;
      default: throw new IOException("unknown colormap version "+v);
    }
  }
  
  protected void writeV1(DataOutputStream dos) throws IOException
  { 
    dos.writeInt(size);
    for (int i=0; i<size; i++) {
      dos.writeInt(colormap[i].getRGB());
    }
  }

  public void read(DataInputStream dis) throws IOException
  {
    read(dis,true);
  }

  public void read(DataInputStream dis, boolean verbose) throws IOException
  { 
    if (verbose) System.out.println("  reading colormap ...");
    startModification();
    try {
      version=dis.readInt();
      switch (version) {
        case 1: readV1(dis);
                break;
        default: throw new IOException("unknown colormap version "+version);
      }
    }
    finally {
      endModification();
    }
  }

  protected void readV1(DataInputStream dis) throws IOException
  {
    size=dis.readInt();
    if (debug) System.out.println("    size "+size);
    colormap=new Color[size];
    for (int i=0; i<size; i++) {
      colormap[i]=new Color(dis.readInt());
    }
  }
}