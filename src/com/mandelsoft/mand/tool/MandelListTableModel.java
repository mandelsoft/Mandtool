
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

import java.io.IOException;
import com.mandelsoft.mand.util.MandelList;
import com.mandelsoft.mand.MandelName;
import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.swing.DnDTableModel;

/**
 *
 * @author Uwe Krüger
 */
public interface MandelListTableModel extends  DnDTableModel, MandelListModel  {

  public void setDuplicates(boolean m);
  public void setModifiable(boolean m);
  public boolean isModifiable();
  
  public void setList(MandelList list);

  //////////////////////////////////////////////////////////////////
 
  public MandelName getName(int index);
  public String getQualifier(int index);
  public QualifiedMandelName getQualifiedName(int index);
  public MandelHandle getMandelHandle(int index);
  public MandelHandle getMandelData(int index) throws IOException;

// Table Model
//  public int getRowCount();
//  public int getColumnCount();
//
//  public Object getValueAt(int rowIndex, int columnIndex);
//  public String getColumnName(int column);
//  public Class getColumnClass(int column);

  public void fireTableDataChanged();

  ///////////////////////////////////////////////////////////////////////////
  // Drag & Drop support by model
  ///////////////////////////////////////////////////////////////////////////

//  public DropMode getDropMode();
//  public int getSourceActions();
//  public Transferable createTransferable(DragLocation loc);
//  public void exportDone(Transferable data, int action);
//  public boolean canImport(TransferSupport info);
//  public boolean importData(TransferSupport info);
  
}