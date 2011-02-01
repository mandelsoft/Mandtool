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
package com.mandelsoft.swing;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Uwe Krueger
 */
public class Utils {

  static public <T> T convertValueToValueClass(Object value, Class<T> valueClass)
  {
    if (value==null) {
      return null;
    }
    if (valueClass!=null) {
      if (valueClass.isAssignableFrom(value.getClass())) {
        return (T)value;
      }

      if (value instanceof Number) {
        if (valueClass==Integer.class) {
          return (T)new Integer(((Number)value).intValue());
        }
        else if (valueClass==Long.class) {
          return (T)new Long(((Number)value).longValue());
        }
        else if (valueClass==Float.class) {
          return (T)new Float(((Number)value).floatValue());
        }
        else if (valueClass==Double.class) {
          return (T)new Double(((Number)value).doubleValue());
        }
        else if (valueClass==Byte.class) {
          return (T)new Byte(((Number)value).byteValue());
        }
        else if (valueClass==Short.class) {
          return (T)new Short(((Number)value).shortValue());
        }
        else if (valueClass==BigDecimal.class) {
          if (value instanceof BigInteger) {
            return (T)new BigDecimal((BigInteger)value);
          }
          return (T)new BigDecimal(((Number)value).doubleValue());
        }
        else if (valueClass==BigInteger.class) {
          return (T)BigInteger.valueOf(((Number)value).longValue());
        }
      }
      for (Constructor<?> cr:valueClass.getConstructors()) {
        Class<?>[] params=cr.getParameterTypes();
        if (params!=null&&params.length==1&&
                params[0].isAssignableFrom(value.getClass())) {
          try {
            return valueClass.getConstructor(params).
                    newInstance(new Object[]{value});
          }
          catch (Exception ex) {
            // ignore
          }
        }
      }
      throw new IllegalArgumentException("inconvertibe types: "+
              value.getClass()+"/"+valueClass);
    }
    return (T)value;
  }

  
}
