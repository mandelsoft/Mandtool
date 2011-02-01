
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
package com.mandelsoft.mand.tool.slideshow;

import javax.swing.Action;

/**
 *
 * @author Uwe Krüger
 */

public interface SlideShowAction extends Action {
  static public final int NONE=0;
  static public final int ONE=1;  // one name required  / current or selected
  static public final int TWO=2;  // two names required / current+selected
  static public final int LIST=4; // mandel list required
  static public final int ALL=ONE|TWO|LIST;

  int getMode();
}
