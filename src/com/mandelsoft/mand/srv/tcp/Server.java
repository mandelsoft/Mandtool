
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.srv.AbstractServer;
import com.mandelsoft.mand.srv.CalcRequest;
import com.mandelsoft.mand.srv.ImageData;
import com.mandelsoft.util.Queue;

/**
 *
 * @author Uwe Krüger
 */
public class Server extends AbstractServer implements Constants, Runnable {

  private ServerSocket socket;
  private volatile Queue<CalcRequest> requests;
  private volatile Queue<CalcRequest> done;
  private volatile ActiveList active;
  private Handler handler;
  private Timeout timeout;
  private Thread server;
  private volatile StatisticHandler stat;

  private boolean log=false;
  private boolean verb=true;

  public Server(String[] args) throws IOException
  {
    int port=PORT;
    int c=0;

    while (args.length>c&&args[c].charAt(0)=='-') {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt;
        switch (opt=arg.charAt(i)) {
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
    setup(true,port);
  }

  public Server(boolean run) throws IOException
  {
    setup(run,PORT);
  }

  public Server(boolean run, int port) throws IOException
  {
    setup(run,port);
  }

  private void setup(boolean run,int port) throws IOException
  {
    stat=new StatisticHandler(20);
    requests=new Queue<CalcRequest>();
    done=new Queue<CalcRequest>();
    active=new ActiveList();

    System.out.println("starting server at "+port);
    socket=new ServerSocket(port);

    handler=new Handler();
    handler.start();
    timeout=new Timeout();
    timeout.start();
    if (run) {
      server=new Thread(this);
      server.start();
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

  synchronized
  public void sendRequest(CalcRequest req)
  {
    requests.put(req);
  }

  synchronized
  public void syncEmpty() throws InterruptedException
  {
    while (!requests.isEmpty()) wait();
  }

  synchronized
  private void doNotify()
  {
    notify();
  }

  @Override
  public synchronized void addImage(ImageData d)
  {
    super.addImage(d);
    stat.addImage();
  }

  @Override
  public synchronized ImageData removeImage(MandelFileName n)
  {
    stat.removeImage();
    return super.removeImage(n);
  }

  ///////////////////////////////////////////////////////////
  // connection handling
  ///////////////////////////////////////////////////////////
  private boolean abort;

  public void run()
  {
    do {
      try {
        Socket client=socket.accept();
        Connection conn=new Connection(client);
        conn.start();
      }
      catch (IOException ex) {
        System.out.println("accept faild: "+ex);
      }
    }
    while (!abort);
  }

  private class Connection extends Thread {

    private boolean abort;
    private Socket socket;
    private ClientData client;
    private DataInputStream is;
    private DataOutputStream os;
    private int vers;

    public Connection(Socket s) throws IOException
    {
      this.socket=s;
      is=new DataInputStream(socket.getInputStream());
      os=new DataOutputStream(socket.getOutputStream());
      this.client=stat.addConnection(this);
      verb("connection from "+client.getHost());
    }

    public Socket getSocket()
    {
      return socket;
    }

    public ClientData getClientData()
    {
      return client;
    }

    @Override
    public void run()
    {
      try {
        try {
          String prot=is.readUTF();
          if (!PROTOCOL.equals(prot)) {
            os.writeUTF("wrong protocol");
            close();
            return;
          }
          vers=is.readInt();
          verb("accept "+prot+": "+vers);
          os.writeUTF(OK);
          os.writeInt(vers);
        }
        catch (IOException ex) {
          System.out.println("protocol failed: "+ex);
          stat.addError(client);
          close();
          return;
        }

        // execution loop
        do {
          try {
            int cmd=is.readInt();
            log("read cmd "+cmd);
            switch (cmd) {
              case REQ_STAT:
                handleStat();
                break;
              case REQ_GET:
                handleGet();
                break;
              case REQ_ANS:
                handleAnswer();
                break;
              default:
                stat.addError(client);
                os.writeUTF("illegal command.");
                break;
            }
          }
          catch (IOException ex) {
            abort=true;
            System.out.println("connection closed: "+ex);
          }
        }
        while (!abort);
        close();
      }
      finally {
        stat.removeConnection(this);
      }
    }

    private void handleStat()
    {
      try {
        int mode=is.readInt();

        ServerInfo info=new ServerInfo(stat.getServerData());
        info.setWeight(stat.getWeight());
        info.setTimeout(stat.getTimeout());

        if ((mode&MODE_CLIENTS)!=0) {
          for (ClientData c:stat.getClients()) {
            info.addClientData(c);
          }
        }
        if ((mode&MODE_IMAGES)!=0) {
          for (ImageData c:getActiveImages()) {
            info.addImageData(c);
          }
        }
        info.write(os);
      }
      catch (IOException io) {
        close();
      }
    }

    private void handleGet()
    {
      CalcRequest req=requests.testAndPull();
      if (req==null) {
        try {
          os.writeUTF(EMPTY);
          os.writeInt(stat.getTimeout());
          stat.notifyContact(client, true);
        }
        catch (IOException ex) {
          close();
        }
      }
      else {
        ActiveRequest a=active.put(req,client);
        try {
          log("sending "+req.getReqId());
          os.writeUTF(FOUND);
          req.write(os,false);
          stat.addRequest(client);
          if (requests.isEmpty()) doNotify();
        }
        catch (IOException ex) {
          active.remove(req);
          requests.putTop(req);
          close();
        }
      }
    }

    private void handleAnswer()
    { CalcRequest req=new CalcRequest();
      try {
        req.read(is,false);
        stat.requestDone(client);
        log("got answer "+req.getReqId());
      }
      catch (IOException ex) {
        stat.addError(client);
        try {
          os.writeUTF(RESET);
        }
        catch (IOException ex1) {
        }
        close();
        return;
      }

      ActiveRequest a=active.get(req.getReqId());
      if (a!=null) {
        log("done "+req.getReqId());
        a.receive(req);
        active.remove(a);
        done.put(a.getRequest());
      }
      try {
        os.writeUTF(OK);
      }
      catch (IOException ex) {
        stat.addError(client);
        close();
      }
    }

    private void close()
    {
      try {
        is.close();
      }
      catch (IOException ex) {
      }
      try {
        os.close();
      }
      catch (IOException ex) {
      }
      abort=true;
    }
  }

  private class Timeout extends Thread {
    private boolean abort=false;
     
    public void abort()
    {
      abort=true;
    }
    
    @Override
    public void run()
    { boolean look;

      do {
        try {
          sleep(TIMEOUTSLEEP);
          long cur=System.currentTimeMillis();
          verb("checking timeouts "+cur);
          look=true;
          while (look) {
            ActiveRequest a=active.peek();
            // if (a!=null) verb("  found "+a.getReqId()+": "+a.getTimeout());
            if (a!=null && a.getTimeout()<cur) {
              active.remove(a);
              a.getClient().addTimeout();
              verb("repeat "+a.getReqId());
              requests.putTop(a.getRequest());
            }
            else look=false;
          }
        }
        catch (InterruptedException ex) {
        }
      }
      while (!abort);
    }
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
          CalcRequest req=done.pull();
          req.fireChangeEvent();
        }
        catch (InterruptedException ex) {
        }
      }
      while (!abort);
    }
  }

  private static class ActiveRequest {
    private CalcRequest request;
    private ClientData client;
    private long timeout;

    public ActiveRequest(CalcRequest request, ClientData client)
    {
      this.request=request;
      this.client=client;
      timeout=System.currentTimeMillis()+TIMEOUT;
    }

    public CalcRequest getRequest()
    {
      return request;
    }

    public ClientData getClient()
    {
      return client;
    }

    public long getReqId()
    {
      return request.getReqId();
    }

    public long getTimeout()
    {
      return timeout;
    }

    synchronized
    public void receive(CalcRequest req)
    {
        request.setData(req.getData());
        request.setNumIt(req.getNumIt());
        request.setCCnt(req.getCCnt());
        request.setMCnt(req.getMCnt());
        request.setMTime(req.getMTime());
        request.setMaxIt(req.getMaxIt());
        request.setMinIt(req.getMinIt());
    }
  }

  static private class ActiveList {
    private HashMap<Long,ActiveRequest> active;
    private List<ActiveRequest> sequence;

    public ActiveList()
    {
      active=new HashMap<Long,ActiveRequest>();
      sequence=new ArrayList<ActiveRequest>();
    }

    synchronized
    public ActiveRequest put(CalcRequest req, ClientData client)
    { ActiveRequest a=active.get(req.getReqId());

      if (a==null) {
        a=new ActiveRequest(req,client);
        active.put(req.getReqId(),a);
        sequence.add(a);
      }
      return a;
    }

    synchronized
    public ActiveRequest get(long reqid)
    {
      ActiveRequest a=active.get(reqid);
      if (a!=null) sequence.remove(a);
      return a;
    }

    synchronized
    public ActiveRequest peek()
    {
      if (sequence.isEmpty()) return null;
      return sequence.get(0);
    }

    synchronized
    public void remove(CalcRequest req)
    {
      remove(req.getReqId());
    }

    synchronized
    public void remove(ActiveRequest req)
    {
      remove(req.getReqId());
    }

    synchronized
    public void remove(long reqid)
    {
      ActiveRequest req=active.get(reqid);
      if (req!=null) {
        active.remove(reqid);
        sequence.remove(req);
      }
    }
  }

  static public void main(String[] args)
  {
    try {
      new Server(false).run();
    }
    catch (IOException ex) {
      System.out.println("cannot start server: "+ex);
    }
  }

  private class StatisticHandler {
    private List<Connection> connections;
    private Map<InetAddress,ClientData> clients;
    private ServerData stat;
    private List<Entry> hist;
    private Entry last;
    private int weight;
    private int max;


    public StatisticHandler(int max)
    {
      this.stat=new ServerData();
      this.clients=new HashMap<InetAddress,ClientData>();
      this.connections=new ArrayList<Connection>();
      this.hist=new ArrayList<Entry>();
      this.max=max;
    }

    synchronized
    private ClientData getClientData(InetAddress addr)
    {
      ClientData data=clients.get(addr);
      if (data==null) {
        data=new ClientData(addr.getCanonicalHostName());
        clients.put(addr, data);
      }
      return data;
    }

    synchronized
    public Collection<ClientData> getClients()
    {
      return Collections.unmodifiableCollection(clients.values());
    }

    synchronized
    public int getWeight()
    {
      return weight;
    }

    synchronized
    public int getTimeout()
    {
      if (last==null) return 1;
      long diff=System.currentTimeMillis()-hist.get(0).getTime();
      if (diff<1000) return 1;
      int t=(int)(weight*1000*60/(diff));
      if (t<=0) t=1;
      verb("t="+t+" diff="+diff+" weight="+weight);
      return t;
    }

    synchronized
    protected void addRequest(ClientData client)
    {
      stat.addRequest(client);
      last.addWeight(-1);
    }

    synchronized
    protected ClientData addConnection(Connection conn)
    {
      ClientData client=getClientData(conn.getSocket().getInetAddress());
      stat.addConnection(client);
      connections.add(conn);
      new Entry();
      return client;
    }

    protected synchronized void addTimeout(ClientData client)
    {
      stat.addTimeout(client);
    }

    protected synchronized void addImage()
    {
      stat.addImage();
    }

    protected synchronized void addError(ClientData client)
    {
      stat.addError(client);
    }

    protected synchronized void requestDone(ClientData client)
    {
      stat.requestDone(client);
    }

    protected synchronized void removeImage()
    {
      stat.removeImage();
    }

    protected synchronized void removeConnection(Connection conn)
    {
      stat.removeConnection(conn.getClientData());
      connections.remove(conn);
    }

    protected synchronized long notifyContact(ClientData client,
                                              boolean req)
    {
      if (req) last.addWeight(1);
      return stat.notifyContact(client);
    }

    public ServerData getServerData()
    {
      return stat;
    }

    //////////////////
    // history entries

    private class Entry {
      private long time;
      private int weight;

      public Entry()
      {
        time=System.currentTimeMillis();
        last=this;
        hist.add(this);
        addWeight(1);
        if (hist.size()>max) {
          hist.get(0).remove();
        }
      }

      public void addWeight(int w)
      {
        weight+=w;
        StatisticHandler.this.weight+=w;
      }

      public void remove()
      {
        if (hist.contains(this)) {
          addWeight(-weight);
          hist.remove(this);
        }
      }

      public long getTime()
      {
        return time;
      }

      public int getWeight()
      {
        return weight;
      }
    }
  }
}
