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

package com.mandelsoft.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Uwe Krueger
 */
public class SHA1FingerPrint {
  private MessageDigest md;

  public SHA1FingerPrint() throws NoSuchAlgorithmException
  {
    md=MessageDigest.getInstance("SHA-1");
  }

  public void update(byte[] bytes, int start, int len)
  {
    md.update(bytes, start, len);
  }
  
  public void update(byte[] bytes)
  {
    md.update(bytes);
  }
  
  public void update(String text) throws UnsupportedEncodingException
  {
    md.update(text.getBytes("utf-8"));
  }

  private static char halfByte(int halfbyte)
  {
    if ((0<=halfbyte)&&(halfbyte<=9)) {
      return (char)('0'+halfbyte);
    }
    else {
      return (char)('A'+(halfbyte-10));
    }
  }

  public String getSHA1()
  {
    byte[] sha1hash=new byte[40];
    sha1hash=md.digest();
    return convertToHex(sha1hash);
  }

  private static String convertToHex(byte[] data)
  {
    StringBuilder buf=new StringBuilder();
    for (int i=0; i<data.length; i++) {
      buf.append(halfByte((data[i]>>4)&0xF));
      buf.append(halfByte((data[i])&0xF));
    }
    return buf.toString();
  }

  /////////////////////////////////////////////////////////////////////////
  
  public static String SHA1(String text)
    throws NoSuchAlgorithmException, UnsupportedEncodingException
  {
    MessageDigest md;
    md=MessageDigest.getInstance("SHA-1");
    byte[] sha1hash=new byte[40];
    md.update(text.getBytes("utf-8"), 0, text.length());
    sha1hash=md.digest();
    return convertToHex(sha1hash);
  }

  //////////////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    String expected="AFB3AB0688399BE23A3833B447F04F3C0420DC68";
    String text="This ia a simple demo text";
    try {
      String sha1=SHA1(text);
      System.out.println("SHA1: "+sha1);
      if (!sha1.equals(expected)) {
        System.err.println("Failed: expected "+expected);
      }
      SHA1FingerPrint fp=new SHA1FingerPrint();
      fp.update(text);
      sha1=fp.getSHA1();
      System.out.println("SHA1: "+sha1);
      if (!sha1.equals(expected)) {
        System.err.println("Failed: expected "+expected);
      }
    }
    catch (Exception ex) {
      System.err.println("failed "+ex);
    }
  }
}
