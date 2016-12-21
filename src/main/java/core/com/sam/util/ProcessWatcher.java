package com.sam.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.sam.moca.MocaInterruptedException;

/**
 * A class that actively waits for a process to emit output and/or exit.  The
 * <code>run</code> method will read the output from a process line-by-line and
 * call the <code>handleOutput</code> method.
 * 
 * Assumptions -- the process handed to an instance of this class is expected to
 * not need any input, and to have its error stream redirected to the normal
 * output stream (see <code>ProcessBuilder.redirectErrorStream</code>).
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class ProcessWatcher implements Runnable {
    /**
     * Creates a new watcher for the given process.  The process should be
     * started, and should have its error stream redirected to its output
     * stream. 
     * @param p the process to watch.
     * @throws UnsupportedEncodingException 
     */
    public ProcessWatcher(Process p) {
        _processOutput = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
        _p = p;
    }

    /**
     * Watches the output from this process until the process ends, or the
     * running thread is interrupted.  If the thread is interrupted, the process
     * is forcibly killed (via <code>Process.destroy</code>).
     */
    public void run() {
        boolean normal = true;
        try {
            String line;
            while ((line = _processOutput.readLine()) != null) {
                handleOutput(line);
            }
        }
        catch (InterruptedIOException e) {
            normal = false;
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            normal = false;
        }
        finally {
            _p.destroy();
            int exitValue = -999;
            if (normal) {
                try {
                    _p.waitFor();
                }
                catch (InterruptedException e) {
                    normal = false;
                    throw new MocaInterruptedException(e);
                }
                exitValue = _p.exitValue();
            }
            
            try {
                _processOutput.close();
            }
            catch (IOException ignore) { }
            
            processExit(exitValue);
        }
    }
    
    //
    // Subclass methods.
    //
    
    /**
     * Called when the process has exited.  The exit value is system-specific,
     * but generally, zero indicates success.
     * 
     * @param exitValue the process exit value.
     */
    protected void processExit(int exitValue) {
        // Do nothing
    }
    
    /**
     * Called when a line of output is read from the process's output stream.
     * This can be used to write to a log file, or to perform more complex
     * logging, if desired.
     * @param line The line of text read from the process.  The string does not
     * include end-of-line markers.
     */
    protected void handleOutput(String line) {
        // Do nothing
    }
    
    //
    // Implementation
    //
    private final BufferedReader _processOutput;
    private final Process _p;
}