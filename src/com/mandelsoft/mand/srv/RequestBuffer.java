
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
package com.mandelsoft.mand.srv;

import java.util.Collection;
import com.mandelsoft.util.Queue;

/**
 *
 * @author Uwe Krüger
 */

public class RequestBuffer<T> {
  private Requests requests;
  private Done done;
  private boolean log;

  public RequestBuffer()
  {
    requests =new Requests();
    done = new Done();
  }

  private void log(String m)
  {
    if (log) System.out.println(m);
  }

  synchronized
  public Requests getRequestView()
  {
    return requests;
  }

  synchronized
  public Done getDoneView()
  {
    return done;
  }
  
  synchronized
  protected void donotify()
  {
    notify();
  }

  synchronized
  public void syncEmpty() throws InterruptedException
  {
    while (!requests.isEmpty()) {
      wait();
    }
  }

  synchronized
  public T getNextAction() throws InterruptedException
  {
    log("get next action");
    while (true) {
      log("  check done");
      T e=done.testAndPull();
      if (e!=null) return e;
      log("  check requests");
      if (requests.isEmpty()) return null;
      log("  wait for next action");
      wait();
    }
  }

  synchronized
  public T testAndGetDone()
  {
    log("test done");
    T e= done.testAndPull();
    log(e==null?"nothing done":"found done ");
    return e;
  }

  public class Requests extends Queue<T> {

    private Requests()
    {
    }

    @Override
    public T pull() throws InterruptedException
    {
      T e=super.pull();
      if (isEmpty()) donotify();
      return e;
    }

    @Override
    public boolean remove(Object o)
    {
      boolean e=super.remove(o);
      if (isEmpty()) donotify();
      return e;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
      boolean e=super.removeAll(c);
      if (isEmpty()) donotify();
      return e;
    }

    @Override
    public T testAndPull()
    {
      T e=super.testAndPull();
      if (isEmpty()) donotify();
      return e;
    }

  }

  public class Done extends Queue<T> {
    private Done()
    {
    }

    @Override
    public void put(T e)
    {
      super.put(e);
      donotify();
    }

    @Override
    public boolean putAll(Collection<? extends T> c)
    {
      boolean e=super.putAll(c);
      if (!isEmpty()) donotify();
      return e;
    }

    @Override
    public void putTop(T e)
    {
      super.putTop(e);
      donotify();
    }
  }
}
