
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

/**
 *
 * @author Uwe Krüger
 */

public interface Constants {
  static public final int PORT = 8181;
  static public final String PROTOCOL = "MandelRequestProtocol";
  static public final int VERSION = 1;
  //static public final int TIMEOUT = 20*60*1000;
  static public final int TIMEOUT = 60*1000;
  static public final int TIMEOUTSLEEP = TIMEOUT/2;

  static public final int REQ_STAT = 0;
  static public final int REQ_GET  = 1;
  static public final int REQ_ANS  = 2;

  static public final int MODE_CLIENTS  = 0x01;
  static public final int MODE_IMAGES   = 0x02;
  static public final int MODE_ALL      = 0xFFFF;

  static public final String OK = "OK";
  static public final String RESET = "RESET";
  static public final String EMPTY = "EMPTY";
  static public final String FOUND = "FOUND";
}
