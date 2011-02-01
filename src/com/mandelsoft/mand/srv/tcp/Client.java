
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
package com.mandelsoft.mand.srv.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import com.mandelsoft.mand.srv.CalcRequest;
import com.mandelsoft.mand.srv.RequestProcessor;

/**
 *
 * @author Uwe Krüger
 */

public class Client implements Constants, Runnable {

  private int MAX=30*60;
  private InetAddress host;
  private int port=PORT;

  private Socket socket;
  private DataInputStream is;
  private DataOutputStream os;
  private RequestProcessor proc;
  private long cnt=0;
  private int version;
  private int servertimeout;
  private boolean log=false;
  private boolean verb=true;

  public Client(String[] args) throws IOException
  {
    int c=0;
    int n=1;

    while (args.length>c&&args[c].charAt(0)=='-') {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt;
        switch (opt=arg.charAt(i)) {
          case 'h': // host
                   if (args.length>c) {
                     try {
                       host=InetAddress.getByName(args[c++]);
                     }
                     catch (UnknownHostException ex) {
                       throw new IllegalArgumentException("unknown host");
                     }
                   }
                   else throw new IllegalArgumentException("host missing");
                   break;
          case 'p': // port
                   if (args.length>c) {
                     try {
                       port=Integer.parseInt(args[c++]);
                     }
                     catch (Exception ex) {
                       throw new IllegalArgumentException("port number expected");
                     }
                   }
                   else throw new IllegalArgumentException("port missing");
                   break;
          case 'n': // processors
                   if (args.length>c) {
                     try {
                       n=Integer.parseInt(args[c++]);
                     }
                     catch (Exception ex) {
                       throw new IllegalArgumentException("number for processors expected");
                     }
                   }
                   else throw new IllegalArgumentException("number of processors missing");
                   break;
          default:
            throw new IllegalArgumentException("illegal option '"+opt+"'");
        }
      }
    }
    setup(n);
  }

  public Client(InetAddress host, int port, int n)
  {
    this.host=host;
    this.port=port;
    setup(n);
  }

  private void setup(int n)
  {
    proc=new RequestProcessor(n);
  }

  private void log(String m)
  {
    if (log) System.out.println(m);
  }

  private void verb(String m)
  {
    if (verb) System.out.println(m);
  }

  public void run()
  { int sleep;

    do {
      CalcRequest req;
      try {
        log("next action");
        req=proc.getNextAction();
        log(req==null?"-> get next":"-> send answer");
      }
      catch (InterruptedException ex) {
        return;
      }

      sleep=10;
      do {
        if (connect()) {
          if (req==null) {
            req=getRequest();
            if (req!=null) {
              sleep=0;
              proc.sendRequest(req);
            }
            else {
              sleep=servertimeout;
            }
          }
          else {
            if (sendAnswer(req)) sleep=0;
            else sleep=servertimeout;
          }
        }
        
        if (sleep>0) {
          if (req!=null || (req=proc.testAndGetAnswer())==null) {
            try {
              verb("sleep "+sleep);
              Thread.sleep(sleep*1000);
              sleep=sleep*2;
              if (sleep>MAX) sleep=MAX;
            }
            catch (InterruptedException ex) {
            }
          }
          if (req!=null) verb("continue with "+req.getReqId());
        }
      }
      while (sleep>0);
    }
    while (true);
  }

  private CalcRequest getRequest()
  {  String stat;

     try {
       os.writeInt(REQ_GET);
       stat=is.readUTF();
       log("get "+stat);
       if (stat.equals(Constants.FOUND)) {
         CalcRequest req=new CalcRequest();
         req.read(is,false);
         verb("got "+req.getReqId());
         cnt++;
//         if (cnt%1000==0) {
//           verb("request lost "+req.getReqId());
//           return null;
//         }
         return req;
       }
       else {
         if (stat.equals(Constants.EMPTY)) {
           servertimeout=is.readInt();
         }
       }
     }
     catch (IOException io) {
       System.out.println("get failed: "+io);
       close();
     }
     return null;
  }

  private boolean sendAnswer(CalcRequest req)
  { String stat;

    try {
      verb("send answer "+req.getReqId());
      os.writeInt(REQ_ANS);
      req.write(os,false);
      stat=is.readUTF();
      return true;
    }
    catch (IOException io) {
      System.out.println("put failed: "+io);
      close();
      return false;
    }
  }

  private boolean connect()
  {
    if (socket!=null) {
      if (socket.isInputShutdown() || socket.isOutputShutdown()) {
        close("shutdown");
      }
      if (socket!=null && !socket.isConnected()) close("not connected");
      if (socket!=null && socket.isClosed()) close("closed");
    }
    if (socket==null) {
      try {
        if (host==null) host=InetAddress.getLocalHost();
        verb("connecting "+host+" ...");
        socket=new Socket(host, port);
        try {
          is=new DataInputStream(socket.getInputStream());
          os=new DataOutputStream(socket.getOutputStream());
          os.writeUTF(PROTOCOL);
          os.writeInt(VERSION);
          String stat=is.readUTF();
          if (!stat.equals(OK)) {
            System.out.println(stat);
            System.exit(1);
          }
          version=is.readInt();
          log("protocol version is "+version);
        }
        catch (IOException ex) {
          if (is!=null) is.close();
          is=null;
          os=null;
          return false;
        }
      }
      catch (IOException io) {
        System.err.println("connect failed: "+io);
        is=null;
        os=null;
        return false;
      }
    }
    return true;
  }

  private void close()
  {
    close("");
  }

  private void close(String msg)
  {
    verb("close "+msg);
    if (is!=null) try {
      is.close();
    }
    catch (IOException ex) {
    }
    if (os!=null) try {
      os.close();
    }
    catch (IOException ex) {
    }
    is=null;
    os=null;
    socket=null;
  }

  static public void main(String[] args)
  {
    try {
      new Client(args).run();
    }
    catch (Exception ex) {
      System.err.println("Fail: "+ex);
    }
  }
}
