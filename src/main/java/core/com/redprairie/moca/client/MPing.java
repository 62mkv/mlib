/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.client;

import java.util.HashMap;
import java.util.Random;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * Simple MOCA ping program.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class MPing {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // Get options
        Options opts = null;
        try {
            opts = Options.parse("s:c:i:fh?", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            shortUsage();
            return;
        }
        
        if (opts.getRemainingArgs().length != 1) {
            shortUsage();
            return;
        }
        
        if (opts.isSet('h') || opts.isSet('?')) {
            shortUsage();
            return;
        }
        
        int size = 32;
        if (opts.isSet('s')) {
            size = Integer.parseInt(opts.getArgument('s'));
        }
        
        double delay = 1.0;
        if (opts.isSet('i')) {
            delay = Double.parseDouble(opts.getArgument('i'));
        }
        
        int count = Integer.MAX_VALUE;
        if (opts.isSet('c')) {
            count = Integer.parseInt(opts.getArgument('c'));
        }
        
        // Options are set, load up test data
        char[] characterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~`!@#$%^&*()_-+={[}]|\\:;\"<>,.?/".toCharArray();
        StringBuilder buf = new StringBuilder(size);
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            buf.append(characterSet[r.nextInt(characterSet.length)]);
        }
        
        String element = buf.toString();
        
        String serverConnection = opts.getRemainingArgs()[0];
        
        // Print out banner
        System.out.println("mping " + serverConnection + ": " + size + " data bytes");
        
        Stats s = new Stats();
        Runtime.getRuntime().addShutdownHook(s);
        
        try {
            MocaConnection conn = ConnectionUtils.createConnection(serverConnection, new HashMap<String, String>());
            
            // Throw away the first ping time.
            pingTime(conn, "");
            
            for (int i = 0; i < count; i++) {
                if (opts.isSet('f') && i != 0) {
                    conn.close();
                    conn = ConnectionUtils.createConnection(serverConnection, new HashMap<String, String>());
                }
                
                if (i != 0) Thread.sleep((long)(delay * 1000.0));
                
                long time = pingTime(conn, element);
                if (time < 0L) {
                    System.out.println("Server Not Responding");
                }
                else {
                    s.recordPingTime(time);
                    System.out.println("Time = " + ((double) time / 1000000.0) + "ms");
                }
            }
        }
        catch (MocaException e) {
            System.out.println("Error: " + e.getMessage());
        }
        catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
    
    public static void shortUsage()
    {
        System.out.print(
                "Usage: mping [ -f ][ -c <count> ] [ -i <interval> ] [ -s <size> ]\n");
    }

    private static class Stats extends Thread {
        @Override
        public void run() {
            System.out.println();
            System.out.format("%d commands%n", _count);
            System.out.format("round-trip min/avg/max = %.2f/%.2f/%.2f ms",
                (double) _min / 1000000.0,
                (double) _total / _count / 1000000.0,
                (double) _max / 1000000.0
                );
            System.out.println();
        }
        
        public void recordPingTime(long nanos) {
            _count++;
            _total += nanos;
            if (nanos < _min) _min = nanos;
            if (nanos > _max) _max = nanos;
        }
        int _count;
        long _total;
        long _min = Long.MAX_VALUE;
        long _max = 0L;
    }

    
    private static long pingTime(MocaConnection conn, String element) {
        long begin = System.nanoTime();
        try {
            conn.executeCommand("ping where blah = '" + element + "'");
        }
        catch (MocaException e) {
            return -1L;
        }
        long end = System.nanoTime();
        return end - begin;
    }
}
