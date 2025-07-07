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
package com.mandelsoft.mand.util;

import com.mandelsoft.mand.scan.MandelHandle;
import static com.mandelsoft.mand.util.MandUtils.vers;
import static java.lang.Integer.parseInt;
import java.util.Set;
import java.util.regex.Matcher;

/**
 *
 * @author uwekr
 */
public class QualifierSelector {

  private int found_vers = -1;
  private  MandelHandle found = null;

  protected boolean isCandidate(MandelHandle h) 
  {
    return (h != null || h.getHeader().hasImageData());
  }

  public QualifierSelector()
  {}
  
  public void Reset()
  {
    found_vers = -1;
    found=null;
  }
  
  public QualifierSelector(Set<MandelHandle> set)
  {
    addCandidates(set);
  }
  
  public MandelHandle getSelected()
  {
    return found;
  }
  
  final public void addCandidates(Set<MandelHandle> set) {
    if (set != null) {
      for (MandelHandle h : set) {
        addCandidate(h);
      }
    }
  }
  final public void addCandidate(MandelHandle h) {
    if (h == null || !isCandidate(h)) {
      return;
    }
    
    String q = h.getQualifier();
    if (q == null || q.isEmpty()) {
      if (found_vers < 0) {
        found = h;
      }
    } else {
      int no = MandUtils.versionVariant(q);
      if (no>0) {
        if (no > found_vers) {
          found = h;
          found_vers = no;
        }
      } else {
        if (found == null) {
          found = h;
        }
      }
    }
  }
  
  public static class Colormap extends QualifierSelector {
    public Colormap()
    {
      super();
    }
    
    public Colormap(Set<MandelHandle> set)
    {
      super(set);
    }
    
    @Override
    protected boolean isCandidate(MandelHandle h) {
      if (h == null || MandUtils.versionVariant(h.getQualifier()) < 0) {
        return false;
      }
      if (h.getHeader().hasColormap()) {
        return true;
      }
      return false;
    }
  }
}
