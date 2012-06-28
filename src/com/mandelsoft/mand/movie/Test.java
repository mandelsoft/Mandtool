/*
 *  Copyright 2012 Uwe Krueger.
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

package com.mandelsoft.mand.movie;

/**
 *
 * @author Uwe Krueger
 */
public class Test {

  static public void main(String[] args)
  {
    double start=10;
    double end=2;
    double limit =0.5;
    
    double f=end/start;

    double n=Math.log(f)/Math.log(limit);
    n=Math.ceil(n);
    double t=Math.pow(f, 1/n);
    System.out.println("n="+n+"  factor="+t);
    for (int i=0; i<=n; i++) {
      System.out.println(""+i+": "+start*Math.pow(t, i));
    }


    System.out.println("********************************");

    double fps=25;
    double zpf=0.98;
    double zoom=1.1852104E-10/4.000000;

    n=Math.log(zoom)/Math.log(zpf);


    t=n/fps;

    double s=t/(Math.log10(zoom)/Math.log10(0.1));
    System.out.println("frame rate:     "+fps);
    System.out.println("total zoom:     "+zoom);
    System.out.println("zoom per frame: "+zpf);
    System.out.println("frames:         "+n);
    System.out.println("time:           "+t);
    System.out.println("speed t/0.1:    "+s);


    System.out.println("********************************");
    s=4;  // spoeed 4 seconds per zoom factor 10
    zoom=0.1;
    n=s*fps; // frames per zoom;
    zpf=Math.pow(zoom,1/n);

    System.out.println("frame rate:     "+fps);
    System.out.println("time:           "+s);
    System.out.println("zoom:           "+zoom);
    System.out.println("required frames:"+n);
    System.out.println("zoom per frame: "+zpf);
  }
}
/*
target area:                         babf6a
target folder:                       movies\babf6a
interpolation limit:                 0.5
frame rate (frames per second):      25.0
speed (seconds per zoom factor 0.1): 0.98
speed (frames per zoom factor 0.1):  113.97408559184939
total zoom:                          2.963026E-11
total frames:                        1199.9493235477796
total movie length (seconds):        47.997972941911186
 */
