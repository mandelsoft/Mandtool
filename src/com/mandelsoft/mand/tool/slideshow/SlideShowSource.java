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

import com.mandelsoft.mand.QualifiedMandelName;
import com.mandelsoft.mand.util.MandelList;

/**
 *
 * @author Uwe Krueger
 */
public interface SlideShowSource {
  public interface OneMode {
    QualifiedMandelName getSingleName(SlideShowModel model);
  }

  public interface TwoMode {
     QualifiedMandelName getFirstName(SlideShowModel model);
     QualifiedMandelName getSecondName(SlideShowModel model);
  }

  public interface ListMode {
     MandelList getMandelList(SlideShowModel model);
  }

  /**
   * Determine the source mode.
   * If generic is true, the potential mode has to be
   * returned, otherwise the current mode for the current state of
   * the source.
   *
   * @param model
   * @param generic return the potential mode instead of the actual one.
   * @return the possible slide show parameterization type.
   */
  int getSourceMode(SlideShowModel model, boolean generic);

  /**
   * source for mode ONE
   */
  OneMode getOneMode(SlideShowModel model);

  /**
   * source for mode TWO
   */
  TwoMode getTwoMode(SlideShowModel model);

  /**
   * source for mode LIST
   */
  ListMode getListMode(SlideShowModel model);
}
