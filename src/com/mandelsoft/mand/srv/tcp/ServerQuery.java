
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
import java.util.Date;
import com.mandelsoft.mand.srv.ImageData;

/**
 *
 * @author Uwe Krüger
 */

public class ServerQuery implements Constants, Runnable {
  private InetAddress host;
  private int port=PORT;

  private Socket socket;
  private DataInputStream is;
  private DataOutputStream os;
  private int version;
  private boolean log=false;
  private boolean verb=true;

  public ServerQuery(String[] args)
  {
    int c=0;

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
          default:
            throw new IllegalArgumentException("illegal option '"+opt+"'");
        }
      }
    }
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
  {
    ServerInfo info=new ServerInfo();

    try {
      verb("connecting ...");
      if (host==null)
        host=InetAddress.getLocalHost();
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
        verb("protocol version is "+version);
        try {
          os.writeInt(REQ_STAT);
          os.writeInt(MODE_ALL);
          info.read(is);
          ServerData server=info.getServer();
          System.out.println("total number of requests:    "+server.getRequestCnt());
          System.out.println("number of active requests:   "+server.getPending());
          System.out.println("last request:                "+new Date(server.getLastRequest()));
          System.out.println("number of timeouts:          "+server.getTimeouts());
          System.out.println("last timeout:                "+new Date(server.getLastTimeout()));
          System.out.println("total number of connections: "+server.getTotalConCnt());
          System.out.println("number of connections:       "+server.getConCnt());
          System.out.println("last connection:             "+new Date(server.getLastConnected()));
          System.out.println("last contact:                "+new Date(server.getLastContact()));
          System.out.println("total number of images:      "+server.getTotalImageCnt());
          System.out.println("number of images:            "+server.getImageCnt());
          System.out.println("server weight:               "+info.getWeight());
          System.out.println("server timeout:              "+info.getTimeout());
          System.out.println("Clients");
          for (ClientData c:info.getClients()) {
            System.out.println("  "+c.getHost());
          }
          System.out.println("Images");
          for (ImageData c:info.getActiveImages()) {
            System.out.println("  "+c.getName()+": "+
                                      (c.isRecalc()?"recalc ":"")+
                                      c.getPrecision()+
                                     "("+c.getMagnification()+") "+
                                     new Date(c.getStartTime()));
          }
          close("");
        }
        catch (IOException io) {
          System.out.println("get failed: "+io);
          close("");
        }
      }
      catch (IOException ex) {
        close();
      }
    }
    catch (IOException io) {
      System.out.println("connect failed: "+io);
      is=null;
      os=null;
    }
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
      new ServerQuery(args).run();
    }
    catch (Exception ex) {
      System.out.println("Fail: "+ex);
    }
  }
}
