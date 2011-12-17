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

package com.mandelsoft.swing.worker;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author Uwe Krueger
 */
public abstract class CallbackWorker<T,V,O extends Component>
                extends SwingWorker<T,UIExecution<? super O>> {
  private O owner;

  public CallbackWorker(O owner)
  {
    this.owner=owner;
  }

  /**
   * Asynchronous execution in UI Thread.
   * @param exec
   */
  protected final void call(UIExecution<? super O> exec)
  {
    publish(exec);
  }

  /**
   * Synchronous execution in UI thread with return of result
   * @param <R>
   * @param exec
   * @return
   */
  protected final <R> R call(UIFunction<? super O, R> exec)
  {
    publish(exec);
    return exec.getResult();
  }
  
  protected final void publishProgress(V... chunks)
  {
    publish(new IntermediateResultNotification(chunks));
  }

  protected void processProgress(List<V> chunks)
  {
  }

  @Override
  protected final void process(List<UIExecution<? super O>> chunks)
  {
    List<V> list=null;

    for (UIExecution<? super O> n:chunks) {
      if (n instanceof IntermediateResultNotification) {
        V[] ichunks=((IntermediateResultNotification<V,O>)n).getChunks();
        if (ichunks!=null) {
          if (list==null) list=new ArrayList<V>();
          for (V v:ichunks) {
            list.add(v);
          }
        }
      }
      else {
        if (list!=null) {
          processProgress(list);
          list=null;
        }
       n.execute(owner);
      }
    }
    if (list!=null) {
      processProgress(list);
      list=null;
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private static class IntermediateResultNotification<V,O extends Component>
                implements UIExecution<O> {
    private V[] chunks;

    public IntermediateResultNotification(V[] chunks)
    {
      this.chunks=chunks;
    }

    public V[] getChunks()
    {
      return chunks;
    }

    public void execute(O owner)
    {
    }
  }
}
