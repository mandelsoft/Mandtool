/*
 * Copyright 2025 uwekr.
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
package com.mandelsoft.mand.tool;

import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.mand.util.MandelList;
import javax.swing.ListModel;

/**
 *
 * @author uwe krueger
 */
public interface MandelListBaseModel extends ListModel {
  public MandelScanner getMandelScanner();
  public MandelList getList();
  public void addMandelListListener(MandelListListener l);
  public void removeMandelListListener(MandelListListener l);
}
