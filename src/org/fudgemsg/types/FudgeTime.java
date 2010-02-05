/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg.types;

/**
 * Dummy class for holding a time value on its own, as Java does not have a
 * standard type for doing so.
 * 
 * @author Andrew
 */
public class FudgeTime {
  
  private final DateTimeAccuracy _accuracy;
  
  private final boolean _hasTimezoneOffset;
  
  private final int _timezoneOffset;
  
  private final long _nanos;
  
  public FudgeTime (final DateTimeAccuracy accuracy, final int timezoneOffset, final long nanos) {
    this (accuracy, true, timezoneOffset, nanos);
  }
  
  public FudgeTime (final DateTimeAccuracy accuracy, final long nanos) {
    this (accuracy, false, 0, nanos);
  }
  
  private FudgeTime (final DateTimeAccuracy accuracy, final boolean hasTimezoneOffset, final int timezoneOffset, final long nanos) {
    if (accuracy == null) throw new NullPointerException ("accuracy cannot be null");
    if (accuracy.getEncodedValue () > DateTimeAccuracy.HOUR.getEncodedValue ()) throw new IllegalArgumentException ("accuracy is not valid");
    _accuracy = accuracy;
    _hasTimezoneOffset = hasTimezoneOffset;
    _timezoneOffset = timezoneOffset;
    _nanos = nanos;
  }
  
  public DateTimeAccuracy getAccuracy () {
    return _accuracy;
  }
  
  public boolean hasTimezoneOffset () {
    return _hasTimezoneOffset;
  }
  
  public int getTimezoneOffset () {
    return hasTimezoneOffset () ? _timezoneOffset : 0;
  }
  
  public long getNanos () {
    return _nanos;
  }
  
  @Override
  public String toString () {
    long n = getNanos ();
    final long fraction = (n % 1000000000l);
    n /= 1000000000l;
    final int seconds = (int)(n % 60l);
    n /= 60l;
    final int minutes = (int)(n % 60l);
    n /= 60l;
    final int hours = (int)n;
    final StringBuilder sb = new StringBuilder ();
    if (hours < 10) sb.append ('0');
    sb.append (hours);
    if (getAccuracy ().getEncodedValue () < DateTimeAccuracy.HOUR.getEncodedValue ()) {
      sb.append (':');
      if (minutes < 10) sb.append ('0');
      sb.append (minutes);
      if (getAccuracy ().getEncodedValue () < DateTimeAccuracy.MINUTE.getEncodedValue ()) {
        sb.append (':');
        if (seconds < 10) sb.append ('0');
        sb.append (seconds);
        if (getAccuracy ().getEncodedValue () < DateTimeAccuracy.SECOND.getEncodedValue ()) {
          sb.append ('.');
          sb.append (fraction);
        }
      }
    } else {
      sb.append ('h');
    }
    if (hasTimezoneOffset ()) {
      sb.append (' ');
      int tzMins = getTimezoneOffset () * 15;
      if (tzMins < 0) {
        tzMins = -tzMins;
        sb.append ('-');
      } else {
        sb.append ('+');
      }
      sb.append (tzMins).append ('m');
    }
    return sb.toString ();
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof FudgeTime)) return false;
    final FudgeTime other = (FudgeTime)o;
    if (getNanos () != other.getNanos ()) return false;
    if (hasTimezoneOffset ()) {
      if (!other.hasTimezoneOffset ()) return false;
      if (getTimezoneOffset () != other.getTimezoneOffset ()) return false;
    } else {
      if (other.hasTimezoneOffset ()) return false;
    }
    return true;
  }
  
  @Override
  public int hashCode () {
    int hc = 1;
    hc += (int)(getNanos () >> 32) * 31 + (int)getNanos ();
    if (hasTimezoneOffset ()) {
      hc *= 31;
      hc += getTimezoneOffset ();
    }
    return hc;
  }
  
}