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

import com.mandelsoft.io.AbstractFile;
import java.io.File;

/**
 *
 * @author Uwe Krüger
 */

public class MandelFileName {
  private QualifiedMandelName name;
  private String suffix;

  public MandelFileName(MandelName name, String qualifier, String suffix)
  {
    this(new QualifiedMandelName(name,qualifier),suffix);
  }

  public MandelFileName(QualifiedMandelName name, String suffix)
  {
    this.name=name;
    this.suffix=suffix;
  }

  public QualifiedMandelName getQualifiedName()
  {
    return name;
  }
  
  public MandelName getName()
  {
    return name.getMandelName();
  }

  public String getSuffix()
  {
    return suffix;
  }

  public String getQualifier()
  {
    return name.getQualifier();
  }

  public String getFileName()
  {
    return name.toString()+(suffix==null?"":suffix);
  }

  ////////////////////////////////////////////////////////////////////////
  public MandelFileName getQ(String qualifier, boolean preserveLocation)
  {
    return new MandelFileName(name.get(qualifier,preserveLocation),suffix);
  }

  public MandelFileName getS(String suffix)
  {
    return new MandelFileName(name,suffix);
  }

  public MandelFileName get(String qualifier, String suffix,
                            boolean preserveLocation)
  {
    return new MandelFileName(name.get(qualifier,preserveLocation),suffix);
  }

  public MandelFileName get(MandelName name,
                            boolean preserveLocation)
  {
    return new MandelFileName(this.name.get(name,preserveLocation),suffix);
  }

  ////////////////////////////////////////////////////////////////////////
  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final MandelFileName other=(MandelFileName)obj;
    if (this.name!=other.name&&(this.name==null||!this.name.equals(other.name)))
      return false;
    if ((this.suffix==null)?(other.suffix!=null):!this.suffix.equals(other.suffix))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=5;
    hash=43*hash+(this.name!=null?this.name.hashCode():0);
    hash=43*hash+(this.suffix!=null?this.suffix.hashCode():0);
    return hash;
  }

  @Override
  public String toString()
  {
    return getFileName();
  }

  ////////////////////////////////////////////////////////////////////////

  public boolean isRasterFileName()
  { return MandelConstants.RASTER_SUFFIX.equals(getSuffix());
  }

  public boolean isInfoFileName()
  { return MandelConstants.INFO_SUFFIX.equals(getSuffix());
  }

  public boolean isRasterImageFileName()
  { return MandelConstants.RASTERIMAGE_SUFFIX.equals(getSuffix());
  }

  public boolean isImageFileName()
  { return MandelConstants.IMAGE_SUFFIX.equals(getSuffix());
  }

  ////////////////////////////////////////////////////////////////////////

  public static MandelFileName create(File f)
  { return create(f.getName());
  }

  public static MandelFileName create(AbstractFile f)
  { return create(f.getName());
  }

  public static MandelFileName create(String n)
  { MandelFileName mfn=null;

    try {
      String base=n;
      String suffix=null;
      int ix=base.lastIndexOf('.');
      if (ix>=0) {
        suffix=base.substring(ix);
        base=base.substring(0,ix);
      }
      QualifiedMandelName qn=QualifiedMandelName._create(base);
      if (qn!=null) mfn=new MandelFileName(qn,suffix);
    }
    catch (IllegalArgumentException e) {
      mfn=null;
    }
    //System.out.println("mfn: "+n+" -> "+mfn);
    return mfn;
  }
}
