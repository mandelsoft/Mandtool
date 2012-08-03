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
package com.mandelsoft.mand.tools;

import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.scan.ElementHandle;

/**
 *
 * @author Uwe Krüger
 */

public interface ExecutionHandler {

  void copy(ElementHandle<?> src, AbstractFile dst);
  void backupInfoFile(AbstractFile mf);
  void backupRasterFile(AbstractFile mf);
  void backupAreaColormapFile(AbstractFile mf);
  void finish();
}
