
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

import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krüger
 */

public class IndexedMandelListModel extends DefaultMandelListTableModel {

  public IndexedMandelListModel(MandelList list, MandelScanner scanner)
  {
    super(list, scanner);
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if (columnIndex==0) return rowIndex+1;
    return super.getValueAt(rowIndex, columnIndex-1);
  }

  @Override
  public Class getColumnClass(int column)
  {
    if (column==0) return Integer.class;
    return super.getColumnClass(column-1);
  }

  @Override
  public int getColumnCount()
  {
    return super.getColumnCount()+1;
  }

  @Override
  public String getColumnName(int column)
  {
    if (column==0) return "Index";
    return super.getColumnName(column-1);
  }
}
