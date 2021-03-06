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

import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.fudgemsg.mapping.UnmodifiableFudgeObjectDictionary;
import org.fudgemsg.taxonomy.TaxonomyResolver;

/**
 * An unmodifiable wrapper for {@code FudgeContext}.
 * <p>
 * This is a simple wrapper for a context that blocks the mutable methods.
 * This does not fully secure the context from editing, being intended simply
 * to stop common programming errors.
 */
public class UnmodifiableFudgeContext extends FudgeContext {

  /**
   * Creates an immutable version of an existing {@link FudgeContext}. Immutable copies of the type and object dictionaries
   * are taken from the source context.
   * 
   * @param context the {@code FudgeContext} to base this on
   */
  public UnmodifiableFudgeContext(final FudgeContext context) {
    super.setTaxonomyResolver(context.getTaxonomyResolver());
    super.setTypeDictionary(new UnmodifiableFudgeTypeDictionary(context.getTypeDictionary()));
    super.setObjectDictionary(new UnmodifiableFudgeObjectDictionary(context.getObjectDictionary()));
  }

  /**
   * Always throws an exception - this is an immutable context.
   */
  @Override
  public void setTaxonomyResolver(TaxonomyResolver taxonomyResolver) {
    throw new UnsupportedOperationException("setTaxonomyResolver called on an immutable Fudge context");
  }

  /**
   * Always throws an exception - this is an immutable context.
   */
  @Override
  public void setTypeDictionary(FudgeTypeDictionary typeDictionary) {
    throw new UnsupportedOperationException("setTypeDictionary called on an immutable Fudge context");
  }

  /**
   * Always throws an exception - this is an immutable context.
   */
  @Override
  public void setObjectDictionary(FudgeObjectDictionary objectDictionary) {
    throw new UnsupportedOperationException("setObjectDictionary called on an immutable Fudge context");
  }

}
