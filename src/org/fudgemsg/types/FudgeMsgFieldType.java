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
package org.fudgemsg.types;

import java.io.DataInput;
import java.io.DataOutput;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.wire.FudgeEncoded;
import org.fudgemsg.wire.FudgeSize;

/**
 * The type definition for a sub-message in a hierarchical message format.
 */
public class FudgeMsgFieldType extends FudgeFieldType {

  /**
   * Standard Fudge field type: embedded sub-message.
   * See {@link FudgeTypeDictionary#FUDGE_MSG_TYPE_ID}.
   */
  public static final FudgeMsgFieldType INSTANCE = new FudgeMsgFieldType();

  /**
   * Restricted constructor.
   */
  private FudgeMsgFieldType() {
    super(FudgeTypeDictionary.FUDGE_MSG_TYPE_ID, FudgeFieldContainer.class, true, 0);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getVariableSize(Object value, FudgeTaxonomy taxonomy) {
    FudgeFieldContainer data = (FudgeFieldContainer) value;
    if (value instanceof FudgeEncoded) {
      final FudgeEncoded fudgeEncoded = (FudgeEncoded) value;
      final byte[] encoded = fudgeEncoded.getFudgeEncoded();
      if (encoded != null) {
        return encoded.length;
      }
    }
    return FudgeSize.calculateMessageSize(taxonomy, data);
  }

  @Override
  public FudgeFieldContainer readValue(DataInput input, int dataSize) {
    throw new UnsupportedOperationException("Sub-messages can only be decoded from FudgeStreamReader");
  }

  @Override
  public void writeValue(DataOutput output, Object value) {
    throw new UnsupportedOperationException("Sub-messages can only be written using FudgeStreamWriter");
  }

}
