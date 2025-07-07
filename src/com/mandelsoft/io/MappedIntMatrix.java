/*
 * Copyright 2025 uwekr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mandelsoft.io;

import com.mandelsoft.util.IntMatrix;
import static com.mandelsoft.util.IntMatrix.asArray;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author uwekr
 */
public class MappedIntMatrix implements IntMatrix {
  private int rx, ry;
  private IntBuffer buffer;
  
  public MappedIntMatrix(String path, int rx, int ry) throws IOException {
    this( new File(path), rx, ry);
  }
  
  public MappedIntMatrix(File file, int rx, int ry) throws IOException {
    int size = rx*ry * Integer.BYTES;
    boolean exists = file.exists();
    if (exists) {
      if (file.length() != 0 && file.length() != size) {
        throw new IOException(String.format("non-matching file size, expected %d, but found %d", size, file.length()));
      }
    }
    RandomAccessFile rfile = new RandomAccessFile(file, "rw");
    FileChannel channel = rfile.getChannel();
    MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, size * 4);
    this.buffer = buffer.asIntBuffer();
    this.rx = rx;
    this.ry = ry;
    rfile.close();
  }
  
  public MappedIntMatrix(String path, IntMatrix m) throws IOException {
    this(path, m.getRX(), m.getRY());
    IntMatrix.copy(m, this);
  }
   
  @Override
  public int getData(int x, int y) {
    return buffer.get(x + y * rx);
  }

  @Override
  public void setData(int x, int y, int val)
  { buffer.put(x+y*rx, val);
  }
 
  @Override
  public int getRX() {
    return rx;
  }

  @Override
  public int getRY() {
    return ry;
  }
}
