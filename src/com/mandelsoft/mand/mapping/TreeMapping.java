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
package com.mandelsoft.mand.mapping;

/**
 *
 * @author Uwe Krüger
 */

class TreeMapping implements MappingRepresentation {
  static class Node {
    Node left;
    Node right;
    int value;
    int target;

    Node(int i, int t, Node left, Node right)
    {
      this.value=i;
      this.target=t;
      this.left=left;
      this.right=right;
    }
  }

  ///////////////////////////////////////////////////

  private Node root;
  private int size;

  TreeMapping(Node n)
  {
    root=n;
    while (n!=null) {
      size=n.value;
      n=n.left;
    }
    size++;
  }

  public int getColormapIndex(int it)
  {
    Node n=root;
    int upper=-1;
    while (n!=null) {
      if (it>n.value) n=n.left;
      else {
        upper=n.target;
        n=n.right;
      }
    }
    return upper;
  }

  public int getSize()
  {
    return size;
  }
}
