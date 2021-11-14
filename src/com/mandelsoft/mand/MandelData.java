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

package com.mandelsoft.mand;

import com.mandelsoft.io.FileAbstractFile;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.tools.Command;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import com.mandelsoft.mand.cm.Colormap;
import com.mandelsoft.mand.mapping.IdentityMapper;
import com.mandelsoft.mand.mapping.Mapper;
import com.mandelsoft.mand.mapping.Mapping;
import com.mandelsoft.mand.cm.ColormapModel;
import com.mandelsoft.mand.cm.ColormapModel.ResizeMode;
import com.mandelsoft.mand.scan.MandelFolder;
import com.mandelsoft.mand.util.MandUtils;
import java.util.Map;

/**
 *
 * @author Uwe Krueger
 */
public class MandelData extends Command implements MandelConstants {
  public interface Part {
    boolean needsVersionUpdate();
  }

  private AbstractFile   file;

  private MandelInfo     info;
  private Colormap       colormap;
  private Mapping        mapping;
  private Mapper         mapper;
  private MandelRaster   raster;
  private BufferedImage  image;

  private boolean        incomplete; // incomplete raster
  private boolean        modified;
  private boolean        partial;
  private boolean        temporary;
  private int            origtype;
  private MandelData     origdata;

  public MandelData(Colormap cm)
  { this.colormap=cm;
  }

  public MandelData(MandelInfo info)
  {
    this.info=info;
  }

  public MandelData(MandelInfo info, MandelHeader h, AbstractFile f)
  {
    this.info=info;
    if (h.getType()!=MandelData.C_INFO) {
      origtype=h.getType();
      partial=true;
    }
    this.setFile(f);
  }

  public MandelData(MandelData data)
  { this(data.getInfo());
    if (data.getColormap()!=null)
      this.colormap=new Colormap(data.getColormap());
    this.mapping=data.getMapping();
    this.mapper=data.getMapper();
    this.raster=data.getRaster();
    this.file=data.file;
    this.temporary=true;
    this.origdata=data;
  }

  public MandelData(File f) throws IOException
  { this(false,f);
  }

  public MandelData(File f, boolean verbose) throws IOException
  { this(false,f,verbose);
  }

  public MandelData(boolean infoonly, File f) throws IOException
  { this(infoonly,f,true);
  }

  public MandelData(boolean infoonly, File f, boolean verbose) throws IOException
  { this(infoonly, new FileAbstractFile(f), verbose);
  }

  public MandelData(AbstractFile f) throws IOException
  { this(false,f);
  }

  public MandelData(AbstractFile f, boolean verbose) throws IOException
  { this(false,f,verbose);
  }

  public MandelData(boolean infoonly, AbstractFile f) throws IOException
  { this(infoonly,f,true);
  }

  public MandelData(boolean infoonly, AbstractFile f, boolean verbose)
            throws IOException
  { read(infoonly,f,verbose);
    this.file=f;
  }

  public MandelData(AbstractFile f, int requested) throws IOException
  { this(f,requested,true);
  }

  public MandelData(AbstractFile f, int requested, boolean verbose)
          throws IOException
  { read(f,requested,verbose);
    this.file=f;
  }

  public MandelData(InputStream is) throws IOException
  { this(false,is);
  }

  public MandelData(InputStream is, boolean verbose) throws IOException
  { this(false,is,verbose);
  }

  public MandelData(boolean infoonly, InputStream is) throws IOException
  { this(infoonly,is,true);
  }

  public MandelData(boolean infoonly, InputStream is, boolean verbose)
          throws IOException
  { DataInputStream dis=getDataInputStream(is);
    try {
      read(infoonly,dis,verbose);
    }
    finally {
      dis.close();
    }
  }

  public MandelData(InputStream is, int requested) throws IOException
  { this(is,requested,true);
  }

  public MandelData(InputStream is, int requested, boolean verbose)
          throws IOException
  { DataInputStream dis=getDataInputStream(is);
    try {
      read(dis,requested,verbose);
    }
    finally {
      dis.close();
    }
  }

  public boolean needsVersionUpdate()
  {
    return  (info==null?false:info.needsVersionUpdate()) ||
            (colormap==null?false:colormap.needsVersionUpdate()) ||
            (mapping==null?false:mapping.needsVersionUpdate()) ||
            (mapper==null?false:mapper.needsVersionUpdate()) ||
            (raster==null?false:raster.needsVersionUpdate())
            ;
  }

  public boolean isTemporary()
  {
    return temporary;
  }

  public void setTemporary(boolean temporary)
  {
    this.temporary=temporary;
  }

  public boolean isIncomplete()
  {
    return incomplete;
  }

  public void setIncomplete(boolean incomplete)
  {
    this.incomplete=incomplete;
  }

  public MandelData getOriginalData()
  {
    return origdata;
  }
 
  public boolean isModified()
  {
    return modified;
  }

  public void setModified(boolean modified)
  {
    this.modified=modified;
  }

  public boolean isPartial()
  {
    return partial;
  }

 
  public int getType()
  { return (info!=null?C_INFO:0)|
           (raster!=null?C_RASTER:0)|
           (colormap!=null?C_COLMAP:0)|
           (mapping!=null?C_MAPPING:0)|
           (mapper!=null?C_MAPPER:0)|
           (image!=null?C_IMAGE:0)|

           (incomplete?C_INCOMPLETE:0);
  }
  
  public MandelHeader getHeader()
  {
    return new MandelHeader(getType());
  }

  public MandelHeader getOrigHeader()
  {
    if (origdata!=null) return origdata.getOrigHeader();
    if (isPartial()) return new MandelHeader(origtype);
    return getHeader();
  }

  public MandelData getOrigData()
  {
    return origdata;
  }

  public String getTypeDesc()
  {
    return getHeader().getTypeDesc();
  }

  public AbstractFile getFile()
  { return file;
  }

  public MandelInfo getInfo()
  {
    return info;
  }

  public Colormap getColormap()
  {
    return colormap;
  }

  public Mapping getMapping()
  {
    return mapping;
  }

  public Mapper getMapper()
  {
    return mapper;
  }

  public MandelRaster getRaster()
  {
    return raster;
  }

  public void setFile(AbstractFile f)
  { this.file=f;
  }

  public void setColormap(ResizeMode mode, Colormap colormap)
  {
    if (this.colormap==colormap) return;
    if (mapping!=null && colormap!=null &&
        mapping.getTargetSize()!=colormap.getSize()) {
      if (mapper==null) {
        if (mapping.getTargetSize()>colormap.getSize()) {
          throw new MandelException("size does not match: "+
                               colormap.getSize()+"!="+mapping.getTargetSize());
        }
        else {
          System.out.println("keep smaller mapping "+mapping.getTargetSize()+
                             "<"+colormap.getSize());
        }
      }
      else {
        // create new mapping
        System.out.println("create new matching mapping "+
                               colormap.getSize()+"<-"+mapping.getTargetSize());
        updateMapping(mode, colormap.getSize(), colormap,
                          "size does not match and no new mapping creatable: "+
                          colormap.getSize()+"!="+mapping.getTargetSize());
      }
    }
//    else {
      this.colormap=colormap;
//      if (colormap!=null && colormap.getSize()>=info.getTargetSize() &&
//          mapping==null) {
//        System.out.println("create identity mapping");
//        setMapping(new IdentityMapper().createMapping(raster, colormap.getSize()));
//      }
      setModified(true);
//    }
  }

  public Colormap resizeColormap(ResizeMode mode, int size)
  {
    if (colormap==null) return null;
    if (colormap.getSize()==size) return colormap;
    if (mapping==null) mapping=_createMapping(colormap.getSize());
    if (mapping==null) return null;
    updateMapping(mode,size,colormap,"cannot resize colormap");
    return colormap;
  }

  private void updateMapping(ResizeMode mode)
  {
    if (colormap!=null)
      updateMapping(mode, colormap.getSize(), colormap,"mapping update failed");
  }

  private Mapping _createMapping(int size)
  {
    System.out.println("create mapping for size "+size);
    //new Throwable().printStackTrace(System.out);
    return mapper.createMapping(raster,size);
  }

  private void updateMapping(ResizeMode mode, int size, Colormap colormap,
                             String errmsg)
  {
    // create new mapping
    Mapping nm=_createMapping(size);
    if (nm==null) {
      throw new MandelException(errmsg);
    }
    System.out.println("got mapping "+nm.getTargetSize());
    Mapping old=mapping;
    mapping=nm;
    if (size!=nm.getTargetSize()) {
      size=nm.getTargetSize();
      System.out.println("mapping requests different colormap size: "+size);
    }
    if (mapping.getTargetSize()!=colormap.getSize()) {
//      ColormapDialog d=new ColormapDialog(null,"new",colormap,false);
//      d.setVisible(true);
      ColormapModel m=new ColormapModel(colormap);
      m.setResizeMode(mode);
      if (old==null || old.getTargetSize()!=colormap.getSize()) {
        System.out.println("proportional resize raster colormap to "+size);
        m.resize(size);
      }
      else {
        System.out.println("resize raster colormap to "+size);
        m.resize(size, old, mapping);
      }
      colormap=m.getColormap();
      System.out.println("raster colormap resized");
    }
    setColormap(mode,colormap);
    setModified(true);
  }

  public void setMapping(Mapping mapping)
  {
    if (colormap!=null && mapping!=null &&
        mapping.getTargetSize()>colormap.getSize()) {
      throw new MandelException("target size: "+
                               mapping.getTargetSize()+">"+colormap.getSize());
    }

    if (info!=null && mapping!=null) {
      if (mapping.getMinIt()>info.getMinIt()) {
        String msg="min iteration value does not match: "+
                                mapping.getMinIt()+">"+info.getMinIt();
        System.out.println("*** "+msg);
        if (false) {
          throw new MandelException(msg);
        }

      }
      
      if (mapping.getMaxIt()!=info.getMaxIt()) {
        String msg="max iteration value does not match: "+
                                mapping.getMaxIt()+"!="+info.getMaxIt();
        System.out.println("*** "+msg);
        if (false) {
          throw new MandelException(msg);
        }
      } 
    }

    this.mapping=mapping;
    setModified(true);
  }

  public void setMapper(ResizeMode mode, Mapper m)
  {
    this.mapper=m;
    if (m!=null) updateMapping(mode);
    setModified(true);
  }

  public void createMapping(ResizeMode mode)
  {
    if (mapper==null)
      throw new MandelException("cannot create mapping: no mapper set");
    if (raster==null)
      throw new MandelException("cannot create mapping: no raster set");
    if (colormap==null)
      throw new MandelException("cannot create mapping: no colormap set");
    updateMapping(mode);
  }
  
  public void setRaster(MandelRaster raster)
  {
    if (raster!=null) {
      if (info.getRX()!=raster.getRX()) {
        throw new MandelException("raster size does not match "+
                  raster.getRX()+" != "+info.getRX());
      }
      if (info.getRY()!=raster.getRY()) {
        throw new MandelException("raster size does not match "+
                  raster.getRY()+" != "+info.getRY());
      }
    }
    this.raster=raster;
    setModified(true);
  }

  public MandelRaster createRaster()
  {
    if (raster==null) {
      raster=new MandelRaster(info.getRX(),info.getRY());
      setModified(true);
    }
    return raster;
  }

  public BufferedImage getImage()
  {
    return image;
  }

  public void setImage(BufferedImage image)
  {
    this.image=image;
    setModified(true);
  }

  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  public void write(DataOutputStream dos) throws IOException
  {
    write(dos,true);
  }

  public void write(DataOutputStream dos, boolean verbose) throws IOException
  { int flags=getType();

    if (verbose) System.out.println("writing mandel ("+flags+")...");
    dos.writeInt(MAGIC);
    dos.writeInt(flags);

    if (info!=null)     info.write(dos);
    if (colormap!=null) colormap.write(dos);
    if (mapping!=null)  mapping.write(dos);
    if (raster!=null)   raster.write(dos);
    if (mapper!=null)   Mapper.IO.write(mapper, dos);
    if (image!=null) {
      System.out.println("  writing image...");
      ImageIO.write(image, "png", dos);
    }
  }

  public void write(File f) throws IOException
  {
    write(f,true);
  }

  public void write(File f, boolean verbose) throws IOException
  {
    File backup=null;

    if (f.exists() && !f.isDirectory()) {
      // first save creation times in not yet set (for old format versions)
      MandelHeader h=getHeader();
      if (h.isRaster()) {
        if (info.getRasterCreationTime()==0) {
            info.setRasterCreationTime(f.lastModified());
        }
      }
      else {
        if (h.isImage()) {
          if (info.getImageCreationTime()==0) {
              info.setImageCreationTime(f.lastModified());
          }
        }
      }
      // create backup old old file version
      backup=new File(f.getParentFile(), f.getName()+"~");
      f.renameTo(backup);
    }

    try {
      DataOutputStream dos=new DataOutputStream(
              new BufferedOutputStream(
              new FileOutputStream(f)));
      try {
        write(dos, verbose);
        if (backup!=null) {
          backup.delete();
          backup=null;
        }
      }
      finally {
        dos.close();
      }
    }
    finally {
      if (backup!=null) {
        f.delete();
        backup.renameTo(f);
      }
      else {
        MandelFolder.Util.add(f);
      }
    }
  }


  public void read(DataInputStream dis) throws IOException
  {
    read(dis,true);
  }

  public void read(DataInputStream dis, boolean verbose) throws IOException
  { read(false,dis,verbose);
  }
  
  public void read(boolean infoonly, DataInputStream dis) throws IOException
  { read(infoonly,dis,true);
  }

  public void read(boolean infoonly, DataInputStream dis, boolean verbose)
              throws IOException
  {
    read(dis,infoonly?C_INFO:C_ALL, verbose);
  }

  public void read(DataInputStream dis, int requested) throws IOException
  {
    read(dis,requested,true);
  }

  public void read(DataInputStream dis, int requested, boolean verbose)
                   throws IOException
  {
    if (verbose) System.out.println("reading mandel "+requested+"...");
    int magic=dis.readInt();
    if (magic!=MAGIC) throw new IOException("illegal format "+magic+"!="+MAGIC);
    int flags=origtype=dis.readInt();
    //if (verbose) System.out.println("  found "+flags);
    partial=((flags&~M_META)&~requested)!=0;
    incomplete=(flags&C_INCOMPLETE)!=0;
    if (requested!=0 && (flags&C_INFO)!=0) {
      info=new MandelInfo();
      info.read(dis,verbose);
      requested&=~C_INFO;
    }
    else info=null;

    if (requested!=0 && (flags&C_COLMAP)!=0) {
      colormap=new Colormap(dis,verbose);
      requested&=~C_COLMAP;
    }
    else colormap=null;

    if (requested!=0 && (flags&C_MAPPING)!=0) {
      mapping=new Mapping();
      mapping.read(dis,verbose);
      requested&=~C_MAPPING;
    }
    else mapping=null;
    
    if (requested!=0 && (flags&C_RASTER)!=0) {
      raster=new MandelRaster();
      raster.read(dis,verbose);
      requested&=~C_RASTER;
    }
    else raster=null;

    if (requested!=0 && (flags&C_MAPPER)!=0) {
      mapper=Mapper.IO.read(dis,verbose);
      requested&=~C_MAPPER;
    }
    else mapper=null;

    if (requested!=0 && (flags&C_IMAGE)!=0) {
      if (verbose) System.out.println("  reading image...");
      image=ImageIO.read(dis);
      requested&=~C_IMAGE;
    }
    else image=null;

    // handle updates
    if (info!=null && info.needsVersionUpdate()) info.updateData(this);
  }


  public void read(AbstractFile f) throws IOException
  { read(false,f);
  }

  public void read(AbstractFile f, boolean verbose) throws IOException
  { read(false,f,verbose);
  }

  public void read(AbstractFile f, int requested) throws IOException
  { read(f,requested,true);
  }

  public void read(AbstractFile f, int requested, boolean verbose)
          throws IOException
  { 
    DataInputStream dis=getDataInputStream(f.getInputStream());
    try {
      read(dis,requested,verbose);
    }
    finally {
      dis.close();
    }
  }

  public void read(boolean infoonly, AbstractFile f) throws IOException
  { read(infoonly,f,true);
  }
  
  public void read(boolean infoonly, AbstractFile f, boolean verbose)
          throws IOException
  {
    if (verbose) System.out.println("reading "+f);
    DataInputStream dis=getDataInputStream(f.getInputStream());
    try {
      read(infoonly,dis,verbose);
    }
    finally {
      dis.close();
    }
  }

  public void write() throws IOException
  {
    if (getFile()==null) throw new IOException("no file set");
    if (isPartial()) throw new IOException("partial data");
    if (isTemporary()) throw new IOException("temporary data");
    if (!getFile().isFile()) throw new IOException("no file");
    write(getFile().getFile());
    setModified(false);
  }

  public void reset() throws IOException
  {
    if (getFile()==null) throw new IOException("no file set");
    read(getFile());
    setModified(false);
  }

  ///////////////////////////////////////////////////////////////
  // static utilities
  ///////////////////////////////////////////////////////////////

  
  static public void createRoot(File f) throws IOException
  {
    System.out.println("Creating root data...");
    MandelData md=new MandelData(MandUtils.createRoot());
    md.write(f);
  }

  static private DataInputStream getDataInputStream(InputStream is)
  {
    if (is instanceof DataInputStream) return (DataInputStream)is;
    if (is instanceof BufferedInputStream) return new DataInputStream(is);
    return new DataInputStream(new BufferedInputStream(is));
  }
  
  ///////////////////////////////////////////////////////////////
  // main
  ///////////////////////////////////////////////////////////////
 
  static public void print(String name, MandelData md)
  {
    System.out.println(md.getTypeDesc()+": "+name);
    MandelInfo mi = md.getInfo();
    System.out.println("xm    = " + mi.getXM());
    System.out.println("ym    = " + mi.getYM());
    System.out.println("dx    = " + mi.getDX());
    System.out.println("dy    = " + mi.getDY());
    System.out.println("limit = " + mi.getLimitIt());
    System.out.println("rx    = " + mi.getRX());
    System.out.println("ry    = " + mi.getRY());
    if (mi.getMaxIt() > 0) {
      System.out.println("max it= " + mi.getMaxIt());
      System.out.println("min it= " + mi.getMinIt());
      System.out.println("num it= " + mi.getNumIt());
      System.out.println("time  = " + mi.getTime());
    }
    if (mi.getProperties()!=null) {
      for (Map.Entry<String,String> e : mi.getProperties().entrySet()) {
        System.out.println(e.getKey()+" = " + e.getValue());
      }
    }
  }
  
  static public void print(File f)
  { 
    try {
      MandelData md=new MandelData(new FileAbstractFile(f));
      print(f.toString(), md);
    }
    catch (IOException io) {
      io.printStackTrace(System.err);
      Error("cannot read "+f+": "+io);
    }
  }

  static public void print(String s)
  {
    File f=new File(s);
    print(f);
  }

  static public void main(String[] args)
  { int c=0;

    while (args.length>c && args[c].charAt(0)=='-') {
      for (int i=1; i<args[c].length(); i++) {
        char opt;
        switch (opt=args[c].charAt(i)) {
          case 'r': try {
                      createRoot(new File("0.md"));
                    }
                    catch (IOException io) {
                      Error("cannot create root: "+io);
                    }
                    break;
          default: Error("illegal option '"+opt+"'");
        }
      }
      c++;
    }

    while (args.length>c) {
      print(args[c++]);
    }
  }
}