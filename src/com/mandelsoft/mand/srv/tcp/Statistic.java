
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

public class Statistic {
  public static final int VERSION=1;

  private long totcon;      // total number of connections
  private long concnt;      // number of active connections
  private long lastcon;     // time of last connection
  private long reqcnt;      // number of requests
  private long lastreq;     // time of last request
  private long pending;     // number of active requests
  private long lastcontact; // time of last contact
  private long timeouts;    // number of timeouts
  private long lasttimeout; // time of last timeout
  private long errors;      // number errors
  private long lasterror;   // time of last error

  synchronized
  protected void addConnection()
  {
    concnt++;
    totcon++;
    lastcon=notifyContact();
  }

  synchronized
  protected void removeConnection()
  {
    concnt--;
  }

  synchronized
  protected void addRequest()
  {
    reqcnt++;
    pending++;
    lastreq=notifyContact();
  }

  synchronized
  protected void addTimeout()
  {
    pending--;
    timeouts++;
    lasttimeout=System.currentTimeMillis();
  }

  synchronized
  protected void requestDone()
  {
    pending--;
    notifyContact();
  }

  synchronized
  protected void addError()
  {
    errors++;
    lasterror=System.currentTimeMillis();
  }

  synchronized
  protected long notifyContact()
  {
    return lastcontact=System.currentTimeMillis();
  }


  ///////////////////////////////////////////////////////////////
  // getter
  ///////////////////////////////////////////////////////////////

  synchronized
  public long getTotalConCnt()
  {
    return totcon;
  }

  synchronized
  public long getConCnt()
  {
    return concnt;
  }

  synchronized
  public long getLastConnected()
  {
    return lastcon;
  }

  synchronized
  public long getPending()
  {
    return pending;
  }

  synchronized
  public long getRequestCnt()
  {
    return reqcnt;
  }

  synchronized
  public long getLastRequest()
  {
    return lastreq;
  }

  synchronized
  public long getTimeouts()
  {
    return timeouts;
  }

  synchronized
  public long getLastTimeout()
  {
    return lasttimeout;
  }

  synchronized
  public long getErrors()
  {
    return errors;
  }

  synchronized
  public long getLastError()
  {
    return lasterror;
  }

  synchronized
  public long getLastContact()
  {
    return lastcontact;
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
    dos.writeLong(totcon);
    dos.writeLong(concnt);
    dos.writeLong(lastcon);
    dos.writeLong(reqcnt);
    dos.writeLong(lastreq);
    dos.writeLong(pending);
    dos.writeLong(lastcontact);
    dos.writeLong(timeouts);
    dos.writeLong(lasttimeout);
    dos.writeLong(errors);
    dos.writeLong(lasterror);
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
    totcon=dis.readLong();
    concnt=dis.readLong();
    lastcon=dis.readLong();
    reqcnt=dis.readLong();
    lastreq=dis.readLong();
    pending=dis.readLong();
    lastcontact=dis.readLong();
    timeouts=dis.readLong();
    lasttimeout=dis.readLong();
    errors=dis.readLong();
    lasterror=dis.readLong();
  }
}
