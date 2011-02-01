
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

/**
 *
 * @author Uwe Krüger
 */

public class ClientData {
  public static final int VERSION=1;

  private String host;
  private Statistic stat;
  private boolean active;

  public ClientData()
  {
    this.stat=new Statistic();
  }

  public ClientData(DataInputStream dis) throws IOException
  {
    this();
    read(dis);
  }

  public ClientData(String host)
  {
    this();
    this.host=host;
  }

  synchronized
  public boolean isActive()
  {
    return active;
  }

  public String getHost()
  {
    return host;
  }

  protected synchronized void requestDone()
  {
    stat.requestDone();
  }

  protected synchronized void removeConnection()
  {
    stat.removeConnection();
    active=false;
  }

  protected synchronized long notifyContact()
  {
    return stat.notifyContact();
  }

  public synchronized long getTimeouts()
  {
    return stat.getTimeouts();
  }

  public synchronized long getRequestCnt()
  {
    return stat.getRequestCnt();
  }

  public synchronized long getPending()
  {
    return stat.getPending();
  }

  public synchronized long getLastTimeout()
  {
    return stat.getLastTimeout();
  }

  public synchronized long getLastRequest()
  {
    return stat.getLastRequest();
  }

  public synchronized long getLastError()
  {
    return stat.getLastError();
  }

  public synchronized long getLastContact()
  {
    return stat.getLastContact();
  }

  public synchronized long getLastConnected()
  {
    return stat.getLastConnected();
  }

  public synchronized long getErrors()
  {
    return stat.getErrors();
  }

  public synchronized long getTotalConCnt()
  {
    return stat.getTotalConCnt();
  }

  public synchronized long getConCnt()
  {
    return stat.getConCnt();
  }

  protected synchronized void addTimeout()
  {
    stat.addTimeout();
  }

  protected synchronized void addRequest()
  {
    stat.addRequest();
  }

  protected synchronized void addError()
  {
    stat.addError();
  }

  protected synchronized void addConnection()
  {
    stat.addConnection();
    active=true;
  }



  ///////////////////////////////////////////////////////////////
  // io
  ///////////////////////////////////////////////////////////////

  public void write(DataOutputStream dos) throws IOException
  {
    write(dos,VERSION);
  }

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
    stat.write(dos);
    dos.writeUTF(host);
    dos.writeBoolean(active);
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
    stat.read(dis);
    host=dis.readUTF();
    active=dis.readBoolean();
  }
}
