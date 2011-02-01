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

package com.mandelsoft.mand.tool.lists;

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.lists.MandelListFactory;
import com.mandelsoft.mand.util.lists.SubAreaFactory;

/**
 *
 * @author Uwe Krueger
 */
public class SubAreaListAction extends MandelListViewAction {

  public SubAreaListAction()
  {
    super("Trans. Sub Areas");
  }

  @Override
  protected MandelListFactory createFactory(QualifiedMandelName n)
  {
    return new SubAreaFactory(getMandelScanner(),n.getMandelName(),getName());
  }
}
