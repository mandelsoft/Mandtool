/*
 * Copyright 2021 D021770.
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
package com.mandelsoft.mand.tools;

import com.mandelsoft.mand.MandelData;
import com.mandelsoft.mand.MandelFileName;
import com.mandelsoft.mand.MandelHeader;
import com.mandelsoft.mand.MandelInfo;
import com.mandelsoft.mand.MandelRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author D021770
 */
public class Info extends Command {
  
  static public void main(String[] args)
  { boolean iflag=false;
    boolean cflag=false;
    boolean sflag=false;

    boolean fix=false;
    int depth=-1;

  
    List<String> list=new ArrayList<String>();
    for (int i=0; i< args.length; i++) {
      String a=args[i];
      if (a.startsWith("-")) {
        for (char c: a.substring(1).toCharArray()) {
          switch (c) {
            case 'i': iflag=true;
                      break;
            case 's': sflag=true;
                      break;
            case 'c': if (cflag) {
                        fix=true;
                      }
                      cflag=true;
                      break;
            case 'd': if (i == args.length - 1) {
                       Error("it missing for -d");
                      }
                      try {
                        depth = Integer.parseInt(args[++i]);
                      }
                      catch (NumberFormatException e) {
                        Error("argument for -d must be an integer");
                      }
              break;
            default:
              Error("invalid option %s", c);
          }
        }
      }
      else {
        list.add(a);
      }
    }
    
     for (String fn : list) {
       MandelFileName mfn = MandelFileName.create(new File(fn).getName());
       System.out.printf("%s\n", fn);
       try {
         MandelData md=new MandelData(iflag, new File(fn));
         MandelInfo info=md.getInfo();
         MandelHeader h=md.getHeader();
         System.out.printf("version: %d\n", info.getVersion());
         System.out.printf("type:    %s\n", h);
         System.out.printf("rx:      %d\n", info.getRX());
         System.out.printf("ry:      %d\n", info.getRY());
         System.out.printf("limit:   %d\n", info.getLimitIt());
         System.out.printf("black:   %d\n", info.getMCnt());
         if (h.hasImage()) {
           System.out.println("has image");
         }
         if (h.hasRaster()) {
           System.out.println("has raster");
         }
         if (h.hasColormap()) {
           System.out.println("has colormap");
         }
         if (h.hasMapper()) {
           System.out.println("has mapper");
         }
         if (h.hasMapping()) {
           System.out.println("has mapping");
         }
         if (info.getKeywords() != null) {
           System.out.println("keywords:");
           for (String e : info.getKeywords()) {
             System.out.println(" - "+e);
           }
         }
         if (info.getProperties() != null) {
           System.out.println("attributes:");
           for (Map.Entry<String, String> e : info.getProperties().entrySet()) {
             System.out.println("  "+e.getKey() + " = " + e.getValue());
           }
         }
         if (depth>0) {
           if (!h.hasRaster()) {
             Error("no raster data available");
           }
           int[][] raster = md.getRaster().getRaster();
           int cnt = 0;
           for (int y = 0; y < info.getRY(); y++) {
             int[] line = raster[y];
             for (int x = 0; x < info.getRX(); x++) {
               if (line[x] > depth) {
                 cnt++;
               }
             }
           }
           System.out.printf("found %d points deeper than %d\n", cnt, depth);
         }
         if (cflag) {
           if (iflag || sflag) {
             Error("-c only without -i and -s");
           }
           if (!h.hasRaster()) {
             Error("no raster data available");
           }
           int[][] raster=md.getRaster().getRaster();
           boolean incomplete=false;
           outer:
           for (int y = 0; y < info.getRY(); y++) {
             int[] line = raster[y];
             for (int x = 0; x < info.getRX(); x++) {
               if (line[x] > info.getLimitIt()) {
                 incomplete = true;
                 break outer;
               }
             }
           }
           if (incomplete) {
             System.out.println("raster is incomplete");
           }
           else {
             System.out.println("raster is complete");
           }
           if (fix && (md.getHeader().isIncomplete()!=incomplete || md.getInfo().getLimitIt()<depth)) {
             if (md.getHeader().isIncomplete() != incomplete) {
               System.out.println("fixing incomplete");
               md.setIncomplete(incomplete);
             }
             if (md.getInfo().getLimitIt() < depth) {
               System.out.println("fixing limit it");
               md.getInfo().setLimitIt(depth);
             }
             md.write();
           }
         }
         if (sflag) {
           if (cflag) {
             Error("-s only without -i and -c");
           }

           String qual = String.format("%d-%d", info.getRX() / 2, info.getRY() / 2);
           File nf = new File(new File(fn).getParentFile(), new MandelFileName(mfn.getName(), qual, mfn.getSuffix()).toString());
           if (nf.exists()) {
             Error("file %s already exists", nf.toString());
           }
           MandelRaster r= md.getRaster();
           if (r==null) {
             Error("no raster info for %s", fn);
           }
           MandelRaster n=new MandelRaster((info.getRX()+1)/2,(info.getRY()+1)/2);
           for (int y=0; y<info.getRY(); y=y+2) {
             int[] ry = r.getRaster()[y];
             int[] ny = n.getRaster()[y/2];

             for (int x=0; x<info.getRX(); x=x+2) {
                ny[x/2]=ry[x];
             }
           }
           info.setRX(n.getRX());
           info.setRY(n.getRY());
           md.setRaster(n);
           try {
             md.write(nf, true);
           }
           catch (IOException ex) {
             Error("cannot write %s: %s", nf, ex);
           }
         }
       }
       catch (IOException ex) {
         Error("cannot load %s: %s", fn, ex);
       }
     }
  }
}
