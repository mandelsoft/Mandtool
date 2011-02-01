
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

import java.util.ArrayList;
import java.util.List;
import com.mandelsoft.util.Queue;

/**
 *
 * @author Uwe Krüger
 */

public class RequestProcessor {
  private RequestBuffer<CalcRequest> buffer;
  private Queue<CalcRequest> queue;
  private Queue<CalcRequest> done;
  private List<Worker> workers;
  private boolean log;

  public RequestProcessor(int n)
  { buffer=new RequestBuffer<CalcRequest>();
    queue=buffer.getRequestView();
    done=buffer.getDoneView();
    workers=new ArrayList<Worker>();

    while (n-->0) {
      Worker w=new Worker();
      workers.add(w);
      w.start();
    }
  }

  private void log(String m)
  {
    if (log) System.out.println(m);
  }

  synchronized
  public void sendRequest(CalcRequest req)
  {
    queue.put(req);
  }


  synchronized
  public void syncEmpty() throws InterruptedException
  {
    buffer.syncEmpty();
  }

  synchronized
  public CalcRequest getNextAction() throws InterruptedException
  {
    return buffer.getNextAction();
  }

  synchronized
  public CalcRequest testAndGetAnswer()
  {
    return buffer.testAndGetDone();
  }

  synchronized
  protected Queue<CalcRequest> getDoneView()
  {
    return done;
  }

  private class Worker extends Thread {
    private boolean abort=false;

    public void abort()
    {
      abort=true;
    }

    @Override
    public void run()
    {
      try {
        do {
          try {
            log("worker: pull");
            CalcRequest req=queue.pull();
            log("worker: calc "+req.getReqId());
            req.calc();
            log("worker: done "+req.getReqId());
            done.put(req);
            log("worker: cont ");
          }
          catch (InterruptedException ex) {
            System.out.println("worker: interrupted");
          }
        }
        while (!abort);
      }
      finally {
        System.out.println("worker: abort");
      }
    }
  }
}
