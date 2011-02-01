
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
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.QualifiedMandelName;

/**
 *
 * @author Uwe Krüger
 */

public interface MandelListFolder extends BaseList<MandelListFolder> {
  //public boolean isLocalFolder(MandelListFolder f);
  public boolean containsTransitively(MandelListFolder f);
  public boolean containsTransitively(MandelList l);
  public void setName(String name);
  public String getName();
  public void setThumbnailName(QualifiedMandelName thumb);
  public QualifiedMandelName getThumbnailName();
  public boolean isLeaf();
  public String getPath();
  public void setParent(MandelListFolder f);
  public MandelListFolder getParent();
  public MandelListFolder createSubFolder(String name);
  public MandelListFolder createSubFolder(int index, String name);
  public MandelListFolder getSubFolder(String name);
  
  public MandelList getMandelList();
  public boolean hasMandelList();
  public MandelListFolderTree getMandelListFolderTree();
  public Iterable<QualifiedMandelName> allentries();
  public Iterable<MandelListFolder> allfolders();


  static public final String DESCRIPTION= "description";
  
  public String getProperty(String name);
  public void setProperty(String name, String value);
  public Iterable<String> propertyNames();
}
