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

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.swing.ThumbnailListModel;

/**
 *
 * @author Uwe Krüger
 */
public interface MandelListListModel 
        extends  ThumbnailListModel<QualifiedMandelName>, MandelListModel  {

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