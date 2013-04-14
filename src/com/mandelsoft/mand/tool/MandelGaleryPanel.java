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
import javax.swing.Icon;
import com.mandelsoft.swing.GaleryPanel;
import com.mandelsoft.swing.ThumbnailListModel;

/**
 *
 * @author Uwe Krueger
 */
public abstract class MandelGaleryPanel<E,M extends ThumbnailListModel<E>>
                      extends GaleryPanel<E,M> {
  private boolean modifiable;;

  public MandelGaleryPanel(M model)
  {
    this(model,1,null);
  }
  
  public MandelGaleryPanel(M model, int rows, Dimension d)
  {
    super(model,rows,d);
  }

  public boolean isModifiable()
  {
    return modifiable;
  }

  public void setModifiable(boolean modifiable)
  {
    if (this.modifiable!=modifiable) {
      handleModifiable(this.modifiable=modifiable);
      firePropertyChange("modifiable",!modifiable,modifiable);
    }
  }

  protected void handleModifiable(boolean modifiable)
  {
  }

  ///////////////////////////////////////////////////////////////////////////
  public MandelWindowAccess getMandelWindowAccess()
  {
    return MandelWindowAccess.Access.getMandelWindowAccess(this);
  }

  @Override
  protected Icon getIcon(E elem)
  {
    return null;
  }

  @Override
  protected String getLabel(E elem)
  {
    return null;
  }
}
