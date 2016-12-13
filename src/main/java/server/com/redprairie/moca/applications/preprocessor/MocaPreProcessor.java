/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.applications.preprocessor;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.server.db.DBType;

/**
 * This class handles the C code Pre Processing.  This should really be
 * genericised to be an abstract class, since the whole framework can be reused
 * with a different processer by providing a different FileProcesser 
 * implementation
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public abstract class MocaPreProcessor {
    
    /**
     * This will preprocess a given set of files for the given database type.
     * A stream is returned automatically and the stream will be populated as
     * the running is going.
     * @param moca The moca context to get variables from
     * @param fileNames The list of files to preprocess
     * @return The stream that has the output
     * @throws IOException If the pipe couldn't be setup correctly
     */
    public Reader process(List<String> fileNames, DBType dbType)
            throws IOException {
        
        if (fileNames == null || fileNames.size() == 0) {
            throw new IllegalArgumentException("There should be at least 1 " +
            		"file in the list");
        }
        
        // Link the 2 pipes up and create a writer so we can write to ours
        // By using PipedReader and PipedWriter
        // 
        // We allow the pipe to be # of files we have times 4092 characters plus
        // an additional 16384 buffer to ensure large files don't overwrite the
        // buffer if there are quite a few to begin with
        PipedWriter writer = new PipedWriter();
        PreprocessorReader in = new PreprocessorReader(writer, 
                fileNames.size() * _charactersPerFile + _charactersExtra);
        
        // Create a single thread executor
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        service.execute(new PreprocesserWorker(fileNames, dbType, writer, in));
        
        // Tell it to shutdown, which will terminate after the runnable is done
        service.shutdown();

        return in;
    }
    
    private class PreprocesserWorker implements Runnable {
        
        public PreprocesserWorker(List<String> fileNames, DBType type, 
                Writer writer, PreprocessorReader preprocessorReader) {
            super();
            _fileNames = fileNames;
            _dbType = type;
            _writer = writer;
            _prePreprocessorReader = preprocessorReader;
        }

        @Override
        public void run() {
            try {
                for (String fileName : _fileNames) {
                    parseFile(fileName, _dbType, _writer);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                _prePreprocessorReader.setThrowable(e);
            }
            finally {
                try {
                    _writer.close();
                }
                catch (IOException e) {
                    _logger.warn("There was a problem closing the writer :" + 
                            e.getMessage());
                }
            }
        }
        
        private final List<String> _fileNames;
        private final DBType _dbType;
        private final Writer _writer;
        private final PreprocessorReader _prePreprocessorReader;
    }
    
    protected static class PreprocessorReader extends PipedReader {
        
        public PreprocessorReader(PipedWriter src, int pipeSize)
                throws IOException {
            super(src, pipeSize);
        }

        @Override
        public synchronized int read() throws IOException {
            try {
                return super.read();
            }
            finally {
                checkThrowable();
            }
        }
        
        @Override
        public synchronized int read(char[] cbuf, int off, int len)
                throws IOException {
            try {
                return super.read(cbuf, off, len);
            }
            finally {
                checkThrowable();
            }
        }

        @Override
        public synchronized boolean ready() throws IOException {
            try {
                return super.ready();
            }
            finally {
                checkThrowable();
            }
        }

        private void checkThrowable() throws IOException {
            if (_throwable != null) {
                // We close ourselves as well
                close();
                throw new IOException(_throwable);
            }
        }
        
        private void setThrowable(Throwable throwable) {
            _throwable = throwable; 
        }
        
        Throwable _throwable;
    }
    
    /**
     * This method is to be overridden by subclasses providing a way to create
     * a new parsing task.  These task should be completely independant of
     * each other.
     * @param fileName The name of the file to preprocess
     * @param type The database type
     * @param appendable The appendable to write the preprocessor output to
     * @throws Exception If any problem preprocessing the file an exception
     *         should be thrown signifying this
     */
    protected abstract void parseFile(String fileName, DBType type, 
            Appendable appendable) throws Exception;
    
    protected enum ReadMode {
        INCOMMENT,
        NORMAL
    }

    protected final int _charactersExtra = 16384;
    protected final int _charactersPerFile = 4092;
    protected final Logger _logger = LogManager.getLogger(this.getClass());
}
