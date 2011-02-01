
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
package com.mandelsoft.mand.srv;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.mandelsoft.mand.MandelFileName;

/**
 *
 * @author Uwe Krüger
 */

public abstract class AbstractServer implements Server {
  private Map<MandelFileName,ImageData> images;

  protected AbstractServer()
  {
    images=new HashMap<MandelFileName,ImageData>();
  }

  synchronized
  public void addImage(ImageData d)
  {
    images.put(d.getName(), d);
  }

  synchronized
  public ImageData removeImage(MandelFileName n)
  {
    return images.remove(n);
  }

  public Collection<ImageData> getActiveImages()
  {
    return Collections.unmodifiableCollection(images.values());
  }

}
