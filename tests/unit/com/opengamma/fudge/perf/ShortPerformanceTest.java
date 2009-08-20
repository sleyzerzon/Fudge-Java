/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.perf;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.opengamma.fudge.FudgeMsg;
import com.opengamma.fudge.FudgeStreamDecoder;
import com.opengamma.fudge.FudgeStreamEncoder;

/**
 * A very short test just to establish some simple performance metrics
 * for Fudge encoding compared with Java Serialization. 
 *
 * @author kirk
 */
public class ShortPerformanceTest {
  private static final int HOT_SPOT_WARMUP_CYCLES = 1000;
  
  @BeforeClass
  public static void warmUpHotSpot() throws Exception {
    System.out.println("Fudge size, Names Only: " + fudgeCycle(true, false));
    System.out.println("Fudge size, Ordinals Only: " + fudgeCycle(false, true));
    System.out.println("Fudge size, Names And Ordinals: " + fudgeCycle(true, true));
    System.out.println("Serialization size: " + serializationCycle());
    for(int i = 0; i < HOT_SPOT_WARMUP_CYCLES; i++) {
      fudgeCycle(true, false);
      serializationCycle();
    }
  }
  
  @Test
  public void performanceVersusSerialization10000Cycles() throws Exception {
    performanceVersusSerialization(10000);
  }
  
  private static void performanceVersusSerialization(int nCycles) throws Exception {
    long startTime = 0;
    long endTime = 0;
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, false);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaNamesOnly = endTime - startTime;
    double fudgeSplitNamesOnly = convertToCyclesPerSecond(nCycles, fudgeDeltaNamesOnly);
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(false, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaOrdinalsOnly = endTime - startTime;
    double fudgeSplitOrdinalsOnly = convertToCyclesPerSecond(nCycles, fudgeDeltaOrdinalsOnly);
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaBoth = endTime - startTime;
    double fudgeSplitBoth = convertToCyclesPerSecond(nCycles, fudgeDeltaBoth);
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      serializationCycle();
    }
    endTime = System.currentTimeMillis();
    long serializationDelta = endTime - startTime;
    double serializationSplit = convertToCyclesPerSecond(nCycles, serializationDelta);
    
    StringBuilder sb = new StringBuilder();
    sb.append("For ").append(nCycles).append(" cycles");
    System.out.println(sb.toString());
    
    sb = new StringBuilder();
    sb.append("Fudge Names Only ").append(fudgeDeltaNamesOnly);
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Ordinals Only ").append(fudgeDeltaOrdinalsOnly);
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Names And Ordinals ").append(fudgeDeltaBoth);
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("ms, Serialization ").append(serializationDelta).append("ms");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Names Only: ").append(fudgeSplitNamesOnly).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Ordinals Only: ").append(fudgeSplitOrdinalsOnly).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Names And Ordinals: ").append(fudgeSplitBoth).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Serialization: ").append(serializationSplit).append("cycles/sec");
    System.out.println(sb.toString());
    assertTrue("Serialization faster by " + (fudgeDeltaNamesOnly - serializationDelta) + "ms.",
        serializationDelta > fudgeDeltaNamesOnly);
  }

  /**
   * @param nCycles
   * @param delta
   * @return
   */
  private static double convertToCyclesPerSecond(int nCycles, long delta) {
    double fudgeSplit = (double)delta;
    fudgeSplit = fudgeSplit / nCycles;
    fudgeSplit = fudgeSplit / 1000.0;
    fudgeSplit = 1/fudgeSplit;
    return fudgeSplit;
  }
  
  private static int fudgeCycle(final boolean useNames, final boolean useOrdinals) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    SmallFinancialTick tick = new SmallFinancialTick();
    FudgeMsg msg = new FudgeMsg();
    if(useNames && useOrdinals) {
      msg.add(tick.getAsk(), "ask", (short) 1);
      msg.add(tick.getAskVolume(), "askVolume", (short) 2);
      msg.add(tick.getBid(), "bid", (short) 3);
      msg.add(tick.getBidVolume(), "bidVolume", (short) 4);
      msg.add(tick.getTimestamp(), "ts", (short) 5);
    } else if(useNames) {
      msg.add(tick.getAsk(), "ask");
      msg.add(tick.getAskVolume(), "askVolume");
      msg.add(tick.getBid(), "bid");
      msg.add(tick.getBidVolume(), "bidVolume");
      msg.add(tick.getTimestamp(), "ts");
    } else if(useOrdinals) {
      msg.add(tick.getAsk(), (short)1);
      msg.add(tick.getAskVolume(), (short)2);
      msg.add(tick.getBid(), (short)3);
      msg.add(tick.getBidVolume(), (short)4);
      msg.add(tick.getTimestamp(), (short)5);
    }
    FudgeStreamEncoder.writeMsg(dos, msg);
    
    byte[] data = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    DataInputStream dis = new DataInputStream(bais);
    msg = FudgeStreamDecoder.readMsg(dis);
    
    tick = new SmallFinancialTick();
    if(useOrdinals) {
      tick.setAsk(msg.getDouble((short)1));
      tick.setAskVolume(msg.getDouble((short)2));
      tick.setBid(msg.getDouble((short)3));
      tick.setBidVolume(msg.getDouble((short)4));
      tick.setTimestamp(msg.getLong((short)5));
    } else if(useNames) {
      tick.setAsk(msg.getDouble("ask"));
      tick.setAskVolume(msg.getDouble("askVolume"));
      tick.setBid(msg.getDouble("bid"));
      tick.setBidVolume(msg.getDouble("bidVolume"));
      tick.setTimestamp(msg.getLong("ts"));
    } else {
      throw new UnsupportedOperationException("Names or ordinals, pick at least one.");
    }
    return data.length;
  }

  private static int serializationCycle() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    SmallFinancialTick tick = new SmallFinancialTick();
    oos.writeObject(tick);
    
    byte[] data = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ObjectInputStream ois = new ObjectInputStream(bais);
    ois.readObject();
    return data.length;
  }

}
