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

import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author Uwe Krüger
 */

public class DateTime implements Comparable<DateTime> {
  static DateFormat datefmt=DateFormat.getDateTimeInstance();

  private Date date;
  private DateFormat fmt=datefmt;

  public DateTime(long t, DateFormat fmt)
  {
    this.date=new Date(t);
    this.fmt=fmt==null?datefmt:fmt;
  }

  public DateTime(long t)
  {
    date=new Date(t);
  }

  public DateTime(Date d, DateFormat fmt)
  {
    this.date=d;
    this.fmt=fmt==null?datefmt:fmt;
  }

  public DateTime(Date d)
  {
    date=d;
  }

  public Date getDate()
  {
    return date;
  }

  public int compareTo(DateTime anotherTime)
  {
    return date.compareTo(anotherTime.date);
  }

  @Override
  public String toString()
  {
    return fmt.format(date);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (getClass()!=obj.getClass()) return false;
    final DateTime other=(DateTime)obj;
    if (this.date!=other.date&&(this.date==null||!this.date.equals(other.date)))
      return false;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash=7;
    hash=53*hash+(this.date!=null?this.date.hashCode():0);
    return hash;
  }

}
