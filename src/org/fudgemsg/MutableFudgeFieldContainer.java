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
package org.fudgemsg;

/**
 * An extension for {@link FudgeFieldContainer} that allows the contents of a message
 * to be mutated.
 * Note that although this interface is currently extremely sparse, eventually
 * there will be substantially more functionality added.
 *
 * @author Kirk Wylie
 */
public interface MutableFudgeFieldContainer extends FudgeFieldContainer {

  /**
   * Adds a field to this container.
   * 
   * @param field field to add
   */
  public void add(FudgeField field);

  /**
   * Adds a field to this container with a name, no ordinal, and type determined by the context's type dictionary.
   * 
   * @param name name of the field, or {@code null} for none
   * @param value field value
   */
  public void add(String name, Object value);

  /**
   * Adds a field to this container with an ordinal, no name, and type determined by the context's type dictionary.
   * 
   * @param ordinal ordinal index for the field, or {@code null} for none
   * @param value field value
   */
  public void add(Integer ordinal, Object value);

  /**
   * Adds a field to this container with the given name, ordinal and type determined by the context's type dictionary.
   * 
   * @param name name of the field, or {@code null} for none
   * @param ordinal ordinal index for the field, or {@code null} for none
   * @param value field value
   */
  public void add(String name, Integer ordinal, Object value);

  /**
   * Adds a field to this container with the given name, ordinal, and type.
   * 
   * @param name name of the field, or {@code null} for none
   * @param ordinal ordinal index for the field, or {@code null} for none
   * @param type the {@link FudgeFieldType} for the field
   * @param value field value
   */
  public void add(String name, Integer ordinal, FudgeFieldType<?> type, Object value);
  
  /**
   * Removes all fields with the given ordinal. Passing {@code null} will remove all fields with no ordinal index.
   * 
   * @param ordinal ordinal index of fields
   */
  public void remove (Short ordinal);
  
  /**
   * Removes all fields with the given name. Passing {@code null} will remove all fields with no name.
   * 
   * @param name name of the fields
   */
  public void remove (String name);
  
  /**
   * Removes all fields matching both the name and ordinal supplied.
   * 
   * @param name name of the fields
   * @param ordinal ordinal index of the fields
   */
  public void remove (String name, Short ordinal);
  
  /**
   * Removes all fields from the message.
   */
  public void clear ();

}
