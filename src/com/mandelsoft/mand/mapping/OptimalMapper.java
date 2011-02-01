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

/**
 *
 * @author Uwe Krueger
 */
public class OptimalMapper extends StatisticMapper {
  static public final int VERSION=2;

  private int minarea;
  private int minimize;

  public OptimalMapper()
  { this(10);
  }

  public OptimalMapper(int area)
  { super();
    this.minarea=area;
  }

  public OptimalMapper(int area, int minimize)
  { super();
    this.minarea=area;
    this.minimize=minimize;
  }

  @Override
  public String getName()
  {
    return "Optimal";
  }

  @Override
  public String getParamDesc()
  {
    return "a="+minarea+",m="+minimize+","+super.getParamDesc();
  }

  public double getMinArea()
  {
    return minarea;
  }

  public boolean isMinimize()
  {
    return minimize!=0;
  }

  public int getMinimize()
  {
    return minimize;
  }

  ///////////////////////////////////////////////////////////////
  // mapping
  ///////////////////////////////////////////////////////////////

  @Override
  protected BreakCondition createBreakCondition(StatisticRasterInfo info)
  {
    return new AreaCondition(info);
  }

  @Override
  protected int adjustColmapSize(int colmapsize)
  {
    if (minimize>0 && colmapsize>minimize) colmapsize=minimize;
    return colmapsize;
  }

  protected class AreaCondition implements BreakCondition {
    private int size;

    public AreaCondition(StatisticRasterInfo info)
    {
      this.size=minarea;
    }

    public boolean done(int num, int accu)
    {
      return accu>=size;
    }

  }
  ///////////////////////////////////////////////////////////////
  // io info
  ///////////////////////////////////////////////////////////////

  @Override
  protected int getDefaultVersion()
  {
    return VERSION;
  }

  @Override
  protected boolean validVersion(int v)
  {
    return 1<=v && v<=VERSION;
  }


  /////////////////////////////////////////////////////////////////////////
  // IO
  /////////////////////////////////////////////////////////////////////////

  @Override
  protected void _write(DataOutputStream dos, int v) throws IOException
  {
    switch (v) {
      case 1:
        writeV1(dos);
        break;
      case 2:
        writeV2(dos);
        break;
      default:
        throw new IOException("unknown cyclic mapping version "+v);
    }
  }

  @Override
  protected void writeV1(DataOutputStream dos) throws IOException
  { 
    dos.writeInt(minarea);
  }

  protected void writeV2(DataOutputStream dos) throws IOException
  {
    writeV1(dos);
    dos.writeInt(minimize);
  }

  @Override
  protected void _read(DataInputStream dis, int v) throws IOException
  {
    switch (v) {
      case 1:
        readV1(dis);
        break;
      case 2:
        readV2(dis);
        break;
      default:
        throw new IOException("unknown cyclic mapping version "+v);
    }
  }

  @Override
  protected void readV1(DataInputStream dis) throws IOException
  {
    minarea=dis.readInt();
  }

  protected void readV2(DataInputStream dis) throws IOException
  {
    readV1(dis);
    minimize=dis.readInt();
  }
}
