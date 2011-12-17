package com.mandelsoft.swing.worker;

import java.awt.Component;

abstract public class UIFunction<O extends Component, R> implements UIExecution<O> {

  private R result;
  private boolean ready;

  public synchronized boolean isReady()
  {
    return ready;
  }

  synchronized protected void setResult(R r)
  {
    this.result=r;
    done();
  }

  public synchronized R getResult()
  {
    while (!isReady()) {
      try {
        wait();
      }
      catch (InterruptedException ex) {
      }
    }
    return result;
  }

  protected synchronized void done()
  {
    ready=true;
    notify();
  }

  ////////////////////////////////////////////////////////////////////

  public <T,V> R call(CallbackWorker<T,V,O> worker)
  {
    return worker.call(this);
  }
}
