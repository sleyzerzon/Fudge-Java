/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
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
package org.fudgemsg.wire.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.fudgemsg.taxonomy.FudgeTaxonomy;

/**
 * The type definition for an array of double-precision floating point numbers.
 */
final class DoubleArrayWireType extends FudgeWireType {

  /**
   * Standard Fudge field type: arbitrary length 64-bit floating point array.
   * See {@link FudgeWireType#DOUBLE_ARRAY_TYPE_ID}.
   */
  public static final DoubleArrayWireType INSTANCE = new DoubleArrayWireType();

  /**
   * Restricted constructor.
   */
  private DoubleArrayWireType() {
    super(FudgeWireType.DOUBLE_ARRAY_TYPE_ID, double[].class);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getSize(Object value, FudgeTaxonomy taxonomy) {
    double[] data = (double[]) value;
    return data.length * 8;
  }

  @Override
  public double[] readValue(DataInput input, int dataSize) throws IOException {
    //Reading this in one go is faster, but increases memory requirement by x2. Should do it in buffered chunks
    byte[] bytes = new byte[dataSize];
    input.readFully(bytes);
    
    int nDoubles = dataSize / 8;
    double[] result = new double[nDoubles];
    for (int i = 0; i < nDoubles; i++) {
      long l = LongArrayWireType.readLong(bytes, (i*8));
      result[i] = Double.longBitsToDouble(l);
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    double[] data = (double[]) value;
    for (double d : data) {
      output.writeDouble(d);
    }
  }

}
