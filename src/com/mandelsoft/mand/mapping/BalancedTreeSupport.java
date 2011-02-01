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
public class BalancedTreeSupport {

  protected int nodecount;
  protected TreeNode root;

  protected class TreeNode<T extends TreeNode>  {
    protected int depth;
    protected T left;
    protected T right;

    protected TreeNode()
    {
      depth=0;
      nodecount++;
    }

    protected T balanceLeft()
    {
      TreeNode n=this;
      TreeNode r;

      if (n.left.depth>depth(n.right)+1) {
        if (depth(n.left.right)>depth(n.left.right)) {
          r=n.left.right;
          n.left.right=r.left;
          r.left=n.left;
          setDepth(r.left);
        }
        else {
          r=n.left;
        }
        n.left=r.right;
        r.right=n;
        setDepth(n);
        n=r;
      }
      setDepth(n);
      return (T)n;
    }

  protected T balanceRight()
  {
    TreeNode n=this;
    TreeNode r;

    if (n.right.depth>depth(n.left)+1) {
      if (depth(n.right.left)>depth(n.right.left)) {
        r=n.right.left;
        n.right.left=r.right;
        r.right=n.right;
        setDepth(r.right);
      }
      else {
        r=n.right;
      }
      n.right=r.left;
      r.left=n;
      setDepth(n);
      n=r;
    }
    setDepth(n);
    return (T)n;
  }

    void print(String gap)
    {
      if (right!=null) right.print(gap+"  ");
      System.out.println(gap+this);
      if (left!=null) left.print(gap+"  ");

    }
  }

  

  protected int depth(TreeNode n)
  {
    if (n==null) return -1;
    return n.depth;
  }

  protected void setDepth(TreeNode n)
  {
    n.depth=max(n.right==null?-1:n.right.depth,
                n.left==null?-1:n.left.depth)+1;
  }

  protected int max(int a, int b)
  {
    if (a>b) return a;
    return b;
  }
}
