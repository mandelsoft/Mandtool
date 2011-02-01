
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
package com.mandelsoft.mand.mapping;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.mandelsoft.mand.MandelRaster;
import com.mandelsoft.util.Utils;

/**
 *
 * @author Uwe Krüger
 */

public interface Mapper {
  public String  getName();
  public String  getParamDesc();
  public Mapping createMapping(MandelRaster raster, int colmapsize);
  public boolean needsVersionUpdate();
  public void    write(DataOutputStream dos) throws IOException;
  public void    read(DataInputStream dis) throws IOException;

  public static class IO {
    static public Mapper read(DataInputStream dis) throws IOException
    {
      return read(dis,true);
    }

    static public Mapper read(DataInputStream dis, boolean verbose)
                            throws IOException
    {
      if (verbose) System.out.println("  reading mapper...");
      String cn=Utils.evaluateClassName(dis.readUTF(),Mapper.class);
      
      //System.out.println("    "+cn);
      Class<? extends Mapper> clazz;

      try {
        clazz=(Class<? extends Mapper>)Class.forName(cn);

        Mapper mapper=clazz.newInstance();
        mapper.read(dis);
        return mapper;
      }
      catch (Exception ex) {
        throw new IOException("cannot instatiate mapper class: "+cn,ex);
      }
    }

    static public void write(Mapper m, DataOutputStream dos) throws IOException
    {
      write(m,dos,true);
    }

    static public void write(Mapper m, DataOutputStream dos, boolean verbose)
                         throws IOException
    { String name=Utils.normalizeClassName(m.getClass(),Mapper.class);
      if (verbose) System.out.println("  writing mapper "+name+" ...");
      dos.writeUTF(name);
      m.write(dos);
    }

  }
}
