/*
 * Copyright 2021 d021770.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.mand.scan;

import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.ColormapName;
import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.cm.Colormap;
import java.io.IOException;

/**
 *
 * @author d021770
 */
public class ProxyColormapHandleForMandelHandle implements ColormapHandle {
  private MandelHandle handle;
  private ColormapName name;
  
  public ProxyColormapHandleForMandelHandle(MandelHandle h)
  {
    this(h, new ColormapName(h.getName().toString()));
  }
  
  public ProxyColormapHandleForMandelHandle(MandelHandle h, ColormapName name)
  {
    this.name=name;
    this.handle=h;
  }
  
  @Override
  public Colormap getColormap() throws IOException
  {
    return handle.getData().getColormap();
  }

  @Override
  public ColormapName getName()
  {
     return name;
  }

  @Override
  public String getLabel()
  {
    return handle.getLabel();
  }

  @Override
  public AbstractFile getFile()
  {
    return handle.getFile();
  }

  @Override
  public MandelHeader getHeader()
  {
    return handle.getHeader();
  }

  @Override
  public MandelData getData() throws IOException
  {
    return handle.getData();
  }
}
