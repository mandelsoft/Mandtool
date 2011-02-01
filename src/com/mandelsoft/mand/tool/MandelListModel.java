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
package com.mandelsoft.mand.tool;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;

/**
 *
 * @author Uwe Krüger
 */
public interface MandelListModel {
  public MandelScanner getMandelScanner();
  public void setDuplicates(boolean m);
  public void setModifiable(boolean m);
  public boolean isModifiable();
  public boolean allowDuplicates();
  
  public void addAction(Action a);
  public void removeAction(Action a);
  public List<Action> getActions();

  public void add(QualifiedMandelName name);
  public void add(int index, QualifiedMandelName name);
  public void addAll(QualifiedMandelName[] names);
  public void addAll(int index, QualifiedMandelName[] names);
  public void remove(QualifiedMandelName name);

  public MandelList getList();
  public void setList(MandelList list);
  public void refresh();
  public void refresh(Environment env);
  public void refresh(boolean soft);
  public void clear();

  public BufferedImage getThumbnail(int index, Dimension max);
  public BufferedImage getThumbnail(QualifiedMandelName name, Dimension max);

  //////////////////////////////////////////////////////////////////
 
  public MandelName getName(int index);
  public String getQualifier(int index);
  public QualifiedMandelName getQualifiedName(int index);
  public MandelHandle getMandelHandle(int index);
  public MandelHandle getMandelData(int index) throws IOException;

  ////////////////////////////////////////////////////////////////////

  public void addMandelListListener(MandelListListener l);
  public void removeMandelListListener(MandelListListener l);
}