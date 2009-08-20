/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.fudge.types.ByteArrayFieldType;
import com.opengamma.fudge.types.DoubleArrayFieldType;
import com.opengamma.fudge.types.FloatArrayFieldType;
import com.opengamma.fudge.types.FudgeMsgFieldType;
import com.opengamma.fudge.types.PrimitiveFieldTypes;
import com.opengamma.fudge.types.StringFieldType;

/**
 * Contains all the {@link FudgeFieldType} definitions for a particular
 * Fudge installation.
 * This class will usually be used as a classic Singleton, although the constructor
 * is public so that it can be used in a Dependency Injection framework. 
 *
 * @author kirk
 */
public final class FudgeTypeDictionary {
  public static final FudgeTypeDictionary INSTANCE = new FudgeTypeDictionary();
  
  private volatile FudgeFieldType<?>[] _typesById = new FudgeFieldType<?>[0];
  private final Map<Class<?>, FudgeFieldType<?>> _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType<?>>();
  
  public void addType(FudgeFieldType<?> type, Class<?>... alternativeTypes) {
    if(type == null) {
      throw new NullPointerException("Must not provide a null FudgeFieldType to add.");
    }
    synchronized(this) {
      int newLength = Math.max(type.getTypeId() + 1, _typesById.length);
      FudgeFieldType<?>[] newArray = Arrays.copyOf(_typesById, newLength);
      newArray[type.getTypeId()] = type;
      _typesById = newArray;
      
      _typesByJavaType.put(type.getJavaType(), type);
      for(Class<?> alternativeType : alternativeTypes) {
        _typesByJavaType.put(alternativeType, type);
      }
    }
  }
  
  public FudgeFieldType<?> getByJavaType(Class<?> javaType) {
    if(javaType == null) {
      return null;
    }
    return _typesByJavaType.get(javaType);
  }
  
  public FudgeFieldType<?> getByTypeId(int typeId) {
    if(typeId >= _typesById.length) {
      return null;
    }
    return _typesById[typeId];
  }
  
  // --------------------------
  // STANDARD FUDGE FIELD TYPES
  // --------------------------
  
  public static final byte BOOLEAN_TYPE_ID = (byte)0;

  public static final byte BYTE_TYPE_ID = (byte)1;
  public static final byte SHORT_TYPE_ID = (byte)2;
  public static final byte INT_TYPE_ID = (byte)3;
  public static final byte LONG_TYPE_ID = (byte)4;
  public static final byte FLOAT_TYPE_ID = (byte)17;
  public static final byte FLOAT_ARRAY_TYPE_ID = (byte)18;
  public static final byte DOUBLE_TYPE_ID = (byte)19;
  public static final byte DOUBLE_ARRAY_TYPE_ID = (byte)20;
  public static final byte BYTE_ARRAY_TYPE_ID = (byte)21;
  public static final byte STRING_TYPE_ID = (byte)22;
  public static final byte FUDGE_MSG_TYPE_ID = (byte)23;
  
  static {
    INSTANCE.addType(PrimitiveFieldTypes.BOOLEAN_TYPE, Boolean.class);
    INSTANCE.addType(PrimitiveFieldTypes.BYTE_TYPE, Byte.class);
    INSTANCE.addType(PrimitiveFieldTypes.SHORT_TYPE, Short.class);
    INSTANCE.addType(PrimitiveFieldTypes.INT_TYPE, Integer.class);
    INSTANCE.addType(PrimitiveFieldTypes.LONG_TYPE, Long.class);
    INSTANCE.addType(PrimitiveFieldTypes.FLOAT_TYPE, Float.class);
    INSTANCE.addType(FloatArrayFieldType.INSTANCE);
    INSTANCE.addType(PrimitiveFieldTypes.DOUBLE_TYPE, Double.class);
    INSTANCE.addType(DoubleArrayFieldType.INSTANCE);
    INSTANCE.addType(ByteArrayFieldType.INSTANCE);
    INSTANCE.addType(StringFieldType.INSTANCE);
    INSTANCE.addType(FudgeMsgFieldType.INSTANCE);
  }

}
