/*
 * Copyright 2023 D021770.
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
package com.mandelsoft.mand;

//import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MandRasterFile implements Closeable {
    private static final int MAPPING_SIZE = 1 << 30;
    private final RandomAccessFile raf;
    private final int width;
    private final int height;
    private final List<MappedByteBuffer> mappings = new ArrayList();

    public MandRasterFile(String filename, int width, int height) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.width = width;
            this.height = height;
            long size = 8L * width * height;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }
    }

    protected long position(int x, int y) {
        return (long) y * width + x;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public double get(int x, int y) {
        assert x >= 0 && x < width;
        assert y >= 0 && y < height;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        return mappings.get(mapN).getDouble(offN);
    }

    public void set(int x, int y, double d) {
        assert x >= 0 && x < width;
        assert y >= 0 && y < height;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        mappings.get(mapN).putDouble(offN, d);
    }

    public void close() throws IOException {
        for (MappedByteBuffer mapping : mappings)
            clean(mapping);
        mappings.clear();
        raf.close();
    }

    private void clean(MappedByteBuffer mapping) {
      /*
        if (mapping == null) return;
        jdk.internal.ref.Cleaner cleaner = ((DirectBuffer) mapping).cleaner();
        if (cleaner != null) cleaner.clean();
      */
    }
  
}
