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
package com.mandelsoft.swing;

import java.util.EventListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import com.mandelsoft.util.PropertyContainer;

/**
 *
 * @author Uwe Krüger
 */

public class TreeModelListenerSupportBase extends PropertyContainer {
  private Object source;
  protected EventListenerList listenerList=new EventListenerList();

  protected TreeModelListenerSupportBase()
  {
    this.source=this;
  }

  protected TreeModelListenerSupportBase(Object source)
  {
    this.source=source;
  }

  public void addTreeModelListener(TreeModelListener l)
  {
    listenerList.add(TreeModelListener.class, l);
  }

  public void removeTreeModelListener(TreeModelListener l)
  {
    listenerList.remove(TreeModelListener.class, l);
  }

  public TreeModelListener[] getTreeModelListeners()
  {
    return getListeners(TreeModelListener.class);
  }

  protected <T extends EventListener> T[] getListeners(Class<T> listenerType)
  {
    return listenerList.getListeners(listenerType);
  }


  protected void fireTreeNodesChanged(TreePath path,
                                      int[] childIndices,
                                      Object[] children)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    TreeModelEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new TreeModelEvent(source, path,
                               childIndices, children);
        ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
      }
    }
  }

  protected void fireTreeNodesChanged(TreeModelEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
      }
    }
  }

  protected void fireTreeNodesInserted(TreePath path,
                                       int[] childIndices,
                                       Object[] children)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    TreeModelEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new TreeModelEvent(source, path,
                               childIndices, children);
        ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
      }
    }
  }

  protected void fireTreeNodesInserted(TreeModelEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
      }
    }
  }

  protected void fireTreeNodesRemoved(TreePath path,
                                      int[] childIndices,
                                      Object[] children)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    TreeModelEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new TreeModelEvent(source, path,
                               childIndices, children);
        ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
      }
    }
  }

  protected void fireTreeNodesRemoved(TreeModelEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
      }
    }
  }

  /**
   * Notifies all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   *
   * @param source the node where the tree model has changed
   * @param path the path to the root node
   * @param childIndices the indices of the affected elements
   * @param children the affected elements
   * @see EventListenerList
   */
  protected void fireTreeStructureChanged(Object[] path,
                                          int[] childIndices,
                                          Object[] children)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    TreeModelEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new TreeModelEvent(source, path,
                               childIndices, children);
        ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
      }
    }
  }

  protected void fireTreeStructureChanged(TreeModelEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
      }
    }
  }
  
  protected void fireTreeStructureChanged(TreePath path)
  {
    // Guaranteed to return a non-null array
    Object[] listeners=listenerList.getListenerList();
    TreeModelEvent e=null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==TreeModelListener.class) {
        // Lazily create the event:
        if (e==null)
          e=new TreeModelEvent(source, path);
        ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
      }
    }
  }
}
