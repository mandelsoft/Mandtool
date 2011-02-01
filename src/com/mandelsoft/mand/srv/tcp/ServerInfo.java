
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.mandelsoft.mand.srv.ImageData;

/**
 *
 * @author Uwe Krüger
 */

public class ServerInfo {
  public static final int VERSION=1;

  private ServerData server;
  private List<ClientData> clients;
  private List<ImageData> images;
  private int weight;
  private int timeout;

  public ServerInfo()
  {
    this.server=new ServerData();
  }

  public ServerInfo(ServerData server)
  {
    this.server=server;
  }

  synchronized
  protected void setWeight(int w)
  {
    weight=w;
  }

  synchronized
  protected void setTimeout(int t)
  {
    timeout=t;
  }

  synchronized
  protected void addClientData(ClientData c)
  {
    if (clients==null) clients=new ArrayList<ClientData>();
    clients.add(c);
  }

  synchronized
  protected void addImageData(ImageData i)
  {
    if (images==null) images=new ArrayList<ImageData>();
    images.add(i);
  }

  synchronized
  public ServerData getServer()
  {
    return server;
  }

  public Collection<ClientData> getClients()
  {
    if (clients==null) return new ArrayList<ClientData>();
    return Collections.unmodifiableCollection(clients);
  }

  public Collection<ImageData> getActiveImages()
  {
    if (images==null) return new ArrayList<ImageData>();
    return Collections.unmodifiableCollection(images);
  }

  public int getTimeout()
  {
    return timeout;
  }

  public int getWeight()
  {
    return weight;
  }

  
  ///////////////////////////////////////////////////////////////
  // forward
  ///////////////////////////////////////////////////////////////

  protected synchronized void addRequest(ClientData client)
  {
    server.addRequest(client);
  }

  protected synchronized void addConnection(ClientData client)
  {
    server.addConnection(client);
  }


  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  synchronized
  public void write(DataOutputStream dos) throws IOException
  {
    write(dos,VERSION);
  }

  synchronized
  public void write(DataOutputStream dos, int v)
              throws IOException
  {
    switch (v) {
       case 1: dos.writeInt(v);
               writeV1(dos);
               break;
      default: throw new IOException("unknown host info version "+v);
    }
  }

  private void writeV1(DataOutputStream dos) throws IOException
  {
    server.write(dos);
    dos.writeInt(weight);
    dos.writeInt(timeout);

    if (clients==null) dos.writeInt(0);
    else {
      dos.writeInt(clients.size());
      for (ClientData c:clients) {
        c.write(dos);
      }
    }

    if (images==null) dos.writeInt(0);
    else {
      dos.writeInt(images.size());
      for (ImageData c:images) {
        c.write(dos);
      }
    }
  }

  public void read(DataInputStream dis) throws IOException
  {
    int version=dis.readInt();
    switch (version) {
      case 1: readV1(dis);
              break;
      default: throw new IOException("unknown host data version "+version);
    }
  }

  private void readV1(DataInputStream dis) throws IOException
  {
    server.read(dis);
    weight=dis.readInt();
    timeout=dis.readInt();

    int n=dis.readInt();
    if (n==0) clients=null;
    else {
      if (clients!=null) clients.clear();
      while (n-->0) {
        ClientData c=new ClientData(dis);
        addClientData(c);
      }
    }
    
    n=dis.readInt();
    if (n==0) images=null;
    else {
      if (images!=null) images.clear();
      while (n-->0) {
        ImageData c=new ImageData(dis);
        addImageData(c);
      }
    }
  }
}
