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
package org.fudgemsg.wire;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.FudgeStreamReader.FudgeStreamElement;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * A reader for returning whole Fudge messages ({@link FudgeMsg} instances) from an
 * underlying {@link FudgeStreamReader} instance.
 * <p>
 * This implementation constructs the whole Fudge message in memory before returning
 * to the caller. This is provided for convenience - greater runtime efficiency may be
 * possible by working directly with the {@link FudgeStreamReader} to process stream
 * elements as they are decoded.
 */
public class FudgeMsgReader {

  /**
   * The underlying source of Fudge elements.
   */
  private final FudgeStreamReader _streamReader;
  /**
   * An envelope buffer for reading in the current message.
   * Calls to {@link #hasNext} will read the envelope header, and create this object
   * with a {@link MutableFudgeMsg} attached to it. The full call to {@link nextMessage}
   * or {@link #nextMessageEnvelope} will process the message fields.
   */
  private FudgeMsgEnvelope _currentEnvelope;
  /**
   * Whether to decode sub-messages as they arrive, or defer until later.
   * Lazy reads are only possible if the reading stream supports
   * {@link FudgeStreamReader#skipMessageField}.
   */
  private boolean _lazyReads;

  /**
   * Creates a new reader around an existing stream reader.
   * 
   * @param streamReader  the source of Fudge stream elements to read
   */
  public FudgeMsgReader(final FudgeStreamReader streamReader) {
    if (streamReader == null) {
      throw new NullPointerException("FudgeStreamReader must not be null");
    }
    _streamReader = streamReader;
  }

  //-------------------------------------------------------------------------
  /**
   * Indicates whether sub-messages are being decoded as they arrive, or deferred.
   * If the underlying stream does not support {@link FudgeStreamReader#skipMessageField}
   * then {@code isLazyReads} may return {@code true} but the messages will be
   * decoded as they are received.
   * 
   * @return {@code true} if deferred decoding of messages will be attempted, {@code false} to not attempt it
   */
  public boolean isLazyReads() {
    return _lazyReads;
  }

  /**
   * Controls whether to decode sub-messages as they arrive, or defer until later.
   * Lazy reads are only possible if the underlying stream supports
   * {@link FudgeStreamReader#skipMessageField}.
   * 
   * @param lazyReads  {@code true} to defer message decoding, or {@code false} to decode sub-message fields as they arrive
   */
  public void setLazyReads(final boolean lazyReads) {
    _lazyReads = lazyReads;
  }

  /**
   * Returns the {@link FudgeContext} associated with the underlying source.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext() {
    final FudgeStreamReader reader = getStreamReader();
    if (reader == null) {
      return null;
    }
    return reader.getFudgeContext();
  }

  /**
   * Returns the underlying stream reader.
   * 
   * @return the stream reader, may be null
   */
  protected FudgeStreamReader getStreamReader() {
    return _streamReader;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns true if there are more messages to read from the underlying source.
   * 
   * @return true if {@link #nextMessage()} or {@link #nextMessageEnvelope()} will return data
   */
  public boolean hasNext() {
    if (_currentEnvelope != null) {
      return true;
    }
    _currentEnvelope = readMessageEnvelope();
    return (_currentEnvelope != null);
  }

  /**
   * Reads the next message, discarding the envelope.
   * 
   * @return the message read without the envelope
   */
  public FudgeMsg nextMessage() {
    final FudgeMsgEnvelope msgEnv = nextMessageEnvelope();
    if (msgEnv == null) {
      return null;
    }
    return msgEnv.getMessage();
  }

  /**
   * Reads the next message, returning the envelope.
   * 
   * @return the {@link FudgeMsgEnvelope}
   */
  public FudgeMsgEnvelope nextMessageEnvelope() {
    FudgeMsgEnvelope msgEnv;
    if (_currentEnvelope == null) {
      msgEnv = readMessageEnvelope();
      if (msgEnv == null) {
        return null;
      }
    } else {
      msgEnv = _currentEnvelope;
      _currentEnvelope = null;
    }
    processFields((MutableFudgeMsg) msgEnv.getMessage());
    return msgEnv;
  }

  /**
   * Reads the next message envelope from the underlying stream. No fields are read.
   * 
   * @return the {@link FudgeMsgEnvelope} read
   */
  protected FudgeMsgEnvelope readMessageEnvelope() {
    if (getStreamReader().hasNext() == false) {
      return null;
    }
    FudgeStreamElement element = getStreamReader().next();
    if (element == null) {
      return null;
    }
    if (element != FudgeStreamElement.MESSAGE_ENVELOPE) {
      throw new IllegalArgumentException("First element in encoding stream wasn't a message element.");
    }
    MutableFudgeMsg msg = getFudgeContext().newMessage();
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg, getStreamReader().getSchemaVersion(), getStreamReader()
        .getProcessingDirectives());
    return envelope;
  }

  /**
   * Processes all of the fields from the current message (or sub-message) in the stream, adding them to the supplied container.
   * 
   * @param msg container to add fields read to
   */
  protected void processFields(MutableFudgeMsg msg) {
    final FudgeStreamReader reader = getStreamReader();
    while (reader.hasNext()) {
      FudgeStreamElement element = reader.next();
      switch (element) {
        case SIMPLE_FIELD:
          msg.add(reader.getFieldName(), reader.getFieldOrdinal(), reader.getFieldType(), reader.getFieldValue());
          break;
        case SUBMESSAGE_FIELD_START:
          if (isLazyReads()) {
            try {
              final EncodedFudgeMsg subMsg = new EncodedFudgeMsg(reader.skipMessageField());
              msg.add(reader.getFieldName(), reader.getFieldOrdinal(), FudgeWireType.SUB_MESSAGE, subMsg);
              continue;
            } catch (UnsupportedOperationException e) {
              // The stream doesn't support lazy reads, so turn it off again
              setLazyReads(false);
            }
          }
          final MutableFudgeMsg subMsg = getFudgeContext().newMessage();
          msg.add(reader.getFieldName(), reader.getFieldOrdinal(), FudgeWireType.SUB_MESSAGE, subMsg);
          processFields(subMsg);
          break;
        case SUBMESSAGE_FIELD_END:
          return;
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Closes this reader and the underlying stream reader.
   */
  public void close() {
    if (_streamReader == null)
      return;
    _streamReader.close();
  }

}
