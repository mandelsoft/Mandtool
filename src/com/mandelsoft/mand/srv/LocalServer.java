
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

/**
 *
 * @author Uwe Krüger
 */

public class LocalServer extends AbstractServer {
  private Handler handler;
  private RequestProcessor processor;

  public LocalServer(String[] args)
  {
    int proc=1;
    int c=0;

    while (args.length>c&&args[c].charAt(0)=='-') {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt;
        switch (opt=arg.charAt(i)) {
          case 'p': // port
                   if (args.length>c) {
                     try {
                       proc=Integer.parseInt(args[c++]);
                     }
                     catch (Exception ex) {
                       throw new IllegalArgumentException("port number expected");
                     }
                   }
                   else throw new IllegalArgumentException("port missing");
                   break;
          default:
            throw new IllegalArgumentException("illegal option '"+opt+"'");
        }
      }
    }
    processor=new RequestProcessor(proc);
  }

  public LocalServer(int n)
  {
    processor=new RequestProcessor(n);

    handler=new Handler();
    handler.start();
  }

  public void sendRequest(CalcRequest req)
  {
    processor.sendRequest(req);
  }

  public void syncEmpty() throws InterruptedException
  {
    processor.syncEmpty();
  }

  private class Handler extends Thread {
    private boolean abort=false;

    public void abort()
    {
      abort=true;
    }

    @Override
    public void run()
    {
      do {
        try {
          CalcRequest req=processor.getDoneView().pull();
          req.fireChangeEvent();
        }
        catch (InterruptedException ex) {
        }
      }
      while (!abort);
    }
  }
}
