
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

import com.mandelsoft.mand.IllegalConfigurationException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.mandelsoft.mand.tools.Command;
import com.mandelsoft.mand.Environment;
import com.mandelsoft.io.AbstractFile;
import com.mandelsoft.mand.scan.MandelHandle;
import com.mandelsoft.mand.scan.MandelScanner;
import com.mandelsoft.util.ChangeEvent;
import com.mandelsoft.util.ChangeListener;

/**
 *
 * @author Uwe Krüger
 */

public class Daemon extends Command {

  static public void main(String[] args)
  {
    int c=0;
    boolean dflag=false; // delete obsolete
    boolean fflag=false; // fast mode
    List<String> sargs=new ArrayList<String>();
    Class<? extends Server> serverc=DirectServer.class;
    Server server=null;

    while (args.length>c&&args[c].charAt(0)=='-') {
      String arg=args[c++];
      for (int i=1; i<arg.length(); i++) {
        char opt;
        switch (opt=arg.charAt(i)) {
          case 'd':
            dflag=true;
            break;
          case 'f':
            fflag=true;
            break;
          case 's':
            if (args.length>c) {
              String n=args[c++];
              try {
                serverc=(Class<? extends Server>)Class.forName(n);
                System.out.println("server class "+serverc);
              }
              catch (ClassNotFoundException ex) {
                Error("cannot load server class "+n+": "+ex);
              }
            }
            else Error("server class name missing");
            break;
          default:
            Error("illegal option '"+opt+"'");
        }
      }
      c++;
    }

    while (args.length>c) {
      sargs.add(args[c++]);
    }

    Constructor<? extends Server> cr=null;
    try {
      cr=serverc.getConstructor(new Class[]{String[].class});
      try {
        server=cr.newInstance(new Object[]{sargs.toArray(new String[sargs.size()])});
       }
      catch (Exception ex) {
        Error("server creation failed: "+ex);
      }
    }
    catch (Exception ex) {
      if (sargs.size()>0) {
        Error("no arguments expected by server class "+serverc);
      }
      try {
        server=serverc.newInstance();
      }
      catch (Exception ex1) {
        Error("server creation failed: "+ex1);
      }
    }
    try {
      service(server, dflag, fflag);
    }
    catch (IllegalConfigurationException ex) {
      Error("service creation failed: "+ex);
    }
  }

  static private void service(Server server, boolean dflag, boolean fflag)
                      throws IllegalConfigurationException
  {
    Service srv=new Service(server,dflag,fflag);
    srv.run();
  }
  ////////////////////////////////////////////////////////////////////////////
  private static class Service {
    Environment env;
    Set<AbstractFile> ignored;
    MandelScanner imagescan;
    Server server;
    boolean dflag;
    boolean fflag;

    public Service(Server server, boolean dflag, boolean fflag)
           throws IllegalConfigurationException
    {
      this.server=server;
      this.dflag=dflag;
      this.fflag=fflag;
      env=new Environment(null);
      ignored=new HashSet<AbstractFile>();
      imagescan=env.getImageDataScanner();
    }

    public Environment getEnvironment()
    { return env;
    }

    public void run()
    { 
      MandelScanner scan=env.getInfoScanner();
      MandelScanner prioscan=env.getPrioInfoScanner();
      SyncListener listener=new SyncListener();

      Iterator<MandelHandle> fallback=scan.getMandelHandles().iterator();

      while (true) {
        ImageHandler handler;
        int found=0;
        for (MandelHandle h:prioscan.getMandelHandles()) {
          if (h.getLabel()!=null) continue;
          System.out.println("handle "+h.getFile());
          handler=new ImageHandler(env,h.getFile());
          handler.addChangeListener(listener);
          handler.send(server);
          if (handler.isAccepted()) {
            listener.sync();
            found++;
          }
        }
        if (found==0) {
          if (!fallback.hasNext()) {
            System.out.println("rescan standard scanner");
            scan.rescan(false);
            fallback=scan.getMandelHandles().iterator();
          }
          while (found==0 && fallback.hasNext()) {
            MandelHandle h=fallback.next();
            if (h.getLabel()!=null) continue;
            System.out.println("handle "+h.getFile());
            handler=new ImageHandler(env,h.getFile());
            handler.addChangeListener(listener);
            handler.send(server);
            if (handler.isAccepted()) {
              listener.sync();
              found++; 
            }
          }
        }

        if (found==0) {
          System.out.println("nothing found");
          try {
            Thread.sleep(1000*20);
          }
          catch (InterruptedException ie) {
            System.exit(1);
          }
        }
        else System.out.println(""+found+" files processed");

        //System.out.println("rescan prio scanner");
        prioscan.rescan(false);
      }
    }

    private class SyncListener implements ChangeListener {
      boolean ready;

      synchronized
      public void stateChanged(ChangeEvent e)
      {
        ready=true;
        notify();
      }

      synchronized
      public void reset()
      {
        ready=false;
      }

      synchronized
      public boolean sync()
      {
        try {
          if (!ready) wait();
        }
        catch (InterruptedException ex) {
          return false;
        }
        reset();
        return true;
      }

    }
  }
}
