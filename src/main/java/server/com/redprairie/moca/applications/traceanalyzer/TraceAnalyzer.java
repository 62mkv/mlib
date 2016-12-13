/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.applications.traceanalyzer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.redprairie.moca.applications.traceanalyzer.reporting.AllCommandStatusErrorsReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.AllErrorLinesReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.ClientTimeReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.ExecutionTreeHotspotReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.ExecutionTreeReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.FastestDeadTimeReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.IrregularPausesReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.Reporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.RequestReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.SlowRequestsReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.SmartDeadTimeReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.SqlHistogramReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.SummaryReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.TableOfContentsReporter;
import com.redprairie.moca.applications.traceanalyzer.reporting.TopCommandsByAppTime;
import com.redprairie.moca.applications.traceanalyzer.reporting.TopSqlStatementsReporter;
import com.redprairie.util.Pair;

/**
 * Analyzes trace files.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TraceAnalyzer {
    
    private static final List<TraceParser> AVAILABLE_PARSERS = new ArrayList<TraceParser>();
    
    static {
        // Format used from 2010.1-2012.2
        AVAILABLE_PARSERS.add(new Log4jOneTraceParser());
        // Format used in 2013.2+
        AVAILABLE_PARSERS.add(new JdaTraceParser());
    }

    // TODO: This is something quick I put together to analyze a trace file, we
    // should iterate it on it to make it more useful/robust/better code techniques!
    // The implementation right now is very naive, it assumes a single thread and
    // also relies on hardcoded messages/ordering.
    // In the future we should probably parse out the lines by thread/session
    // and go with a more object oriented approach
    
    /**
     * Creates a TraceAnalyzer for printing reports about trace files
     * @param file The name of the file (absolute or relative path)
     * @param pauseThreshold The threshold in milliseconds we look for in trace pauses
     * @param includeSql Whether to include SQL statements in the analysis
     */
    public TraceAnalyzer(String file) {
        _file = file;
    }
    
    public void printReport(PrintStream stream) throws IOException, ParseException {
        TraceParser parser = findTraceParser();
        int pauseThreshold = 5; // Find irregular pauses above 5 ms
        long parseTimeStart = System.currentTimeMillis();
        Stack<Execution> executionStack = new Stack<Execution>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_file), "UTF-8"));
        for (TraceLine currentLine = getNextLine(br, parser, null);
                currentLine != null;) {
           // Need to handle request start/end first cause it will
           // define what thread ID we should be looking for in the trace lines.
           Integer threadIdFilter = null;
           if (handleRequestStart(currentLine, executionStack)) {
               threadIdFilter = executionStack.firstElement().getStartLine().getThreadId(); 
           }
           else if (handleRequestEnd(currentLine, executionStack)) {
               threadIdFilter = null; // we won't filter by thread ID outside of requests
           }
           else if (!executionStack.isEmpty()) {
               threadIdFilter = executionStack.firstElement().getStartLine().getThreadId();
           }

           TraceLine nextLine = getNextLine(br, parser, threadIdFilter);
           long startTime = currentLine.getDate().getTime();
           
//           
//           if (handlePrintingArgumentsStart(currentLine, startTime)) {
//               
//           }
//           else if (handlePrintingArgumentsEnd(currentLine, nextLine, startTime)) {
//               
//           }
           
           // TODO - probably a better way to do this, basically just figuring out what the line is
           if (handleCommandStart(currentLine, executionStack)) {
               
           }
           else if (handleCommandEnd(currentLine, nextLine, executionStack)) {
               
           }
           else if (handleSqlStart(currentLine, nextLine, executionStack)) {
               
           } 
           else if (handleSqlEnd(currentLine, executionStack)) {
               
           }
           // Report all lines where they were logged as errors (warning or higher)
           else if (currentLine.isErrorLevel()) {
               _allErrorLines.add(currentLine);
           }
           
           if (nextLine != null) {
               long endTime = nextLine.getDate().getTime();
               long diff = endTime - startTime;
               
               if (diff >= pauseThreshold) {
                   // Ignore standard sql statement executions and client pausing
                   if (!shouldIgnore(currentLine, nextLine)) {
                       if (isNativeProcessBoundary(currentLine, nextLine)) {
                           NativeProcessBoundaryPause npBoundary = 
                                   new NativeProcessBoundaryPause(currentLine, getParentExecution(executionStack));
                           npBoundary.done(nextLine);
                           _allPausesAboveThreshold.add(npBoundary);
                       }
                       else {
                           UnknownPause pause = new UnknownPause(currentLine, getParentExecution(executionStack));
                           pause.done(nextLine);
                           _allPausesAboveThreshold.add(pause);
                       }
                   }
                   
               }
           }
           
           currentLine = nextLine;
        }
        
        TraceAnalysis analysis = new TraceAnalysis(_allCommandExecutions,
            _allSqlExecutions, _allDeadTime, _allRequests, _allExecutions,
            _allPausesAboveThreshold, pauseThreshold, _allErrorLines, _allCommandErrorStatusLines);
        List<Reporter> reports = new ArrayList<Reporter>();
        reports.add(new SummaryReporter(parseTimeStart, _file));
        reports.add(new SqlHistogramReporter());
        reports.add(new TopSqlStatementsReporter(25));
        reports.add(new TopCommandsByAppTime(25));
        reports.add(new SlowRequestsReporter(150));
        reports.add(new AllErrorLinesReporter());
        reports.add(new AllCommandStatusErrorsReporter());
        reports.add(new RequestReporter());
        reports.add(new ExecutionTreeHotspotReporter(150, 15));
        reports.add(new ExecutionTreeReporter());
        reports.add(new ClientTimeReporter());
        reports.add(new IrregularPausesReporter(pauseThreshold));
        reports.add(new FastestDeadTimeReporter(5));
        reports.add(new SmartDeadTimeReporter());
        
        Reporter toc = new TableOfContentsReporter(reports);
        printReport(stream, toc, analysis);
        for (Reporter report : reports) {
            printReport(stream, report, analysis);
        }
        
        stream.println("\nTotal parse and reporting time = " + formatTime(System.currentTimeMillis() - parseTimeStart));
     
    }
    
    private TraceLine getNextLine(BufferedReader br, TraceParser parser, Integer threadId) throws IOException {
        if (_lastLineRead == null) return null;
        
        StringBuilder nextLine = null;
        if (_lastLineRead.isEmpty()) {
            nextLine = new StringBuilder(br.readLine());
        }
        else {
            nextLine = new StringBuilder(_lastLineRead);
        }
        
        String tmpLine = null;
        while ((tmpLine = br.readLine()) != null) {
            if (parser.isNewLine(tmpLine)) {
                break;
            }
            
            nextLine.append(tmpLine);
        }
        _lastLineRead = tmpLine;

        TraceLine traceLine = parser.parseLine(nextLine.toString());
        if (threadId != null && threadId != traceLine.getThreadId()) {
            // Uncomment to show what lines are being skipped because they're async in a different thread
            // System.err.println(traceLine);
            traceLine = getNextLine(br, parser, threadId);
        }
        return traceLine;
    }
    
    private boolean shouldIgnore(TraceLine currentLine, TraceLine nextLine) {
        String line1 = currentLine.getMessage();
        String line2 = nextLine.getMessage();
        // Right now just ignoring dispatched-->dispatching and if SQL statements disabled
        if (line1.contains("Dispatched command")
                || line2.equals("Processing incoming request")
                || line2.equals("Dispatching command...")
                || line1.startsWith("Executing statement with")
                || line1.equals("Processed incoming request")
                || line2.equals("Parsing command...")) {
            return true;
        }
        
        return false;
    }
    
    private boolean isNativeProcessBoundary(TraceLine line1, TraceLine line2) {
        if (line1.getLogger().contains("(DefaultSer")
                && line2.getLogger().contains("(Sql")) {
            return true;
        }
        else if (line1.getLogger().contains("(Sql")
                && line2.getLogger().contains("(ContextMoc")) {
            return true;
        }
        
        return false;
    }

    private boolean handleCommandStart(TraceLine currentLine, Stack<Execution> executionStack) {
        int index = currentLine.getMessage().indexOf("Executing Command:");
        if (index > -1) {
            String command = currentLine.getMessage().substring(index + 19);
            CommandExecution commandExec = new CommandExecution(command, currentLine, getParentExecution(executionStack));
            pushExecutionStack(commandExec, executionStack);
                   
            return true;
        }
        
        return false;
    }
    
    
    private boolean handleCommandEnd(TraceLine currentLine, TraceLine nextLine, Stack<Execution> executionStack) {
        int index = currentLine.getMessage().indexOf("Executed Command:");
        if (index > -1) {
            if (!executionStack.isEmpty()) {
                CommandExecution endingCommand = (CommandExecution) popExecutionStack(currentLine, executionStack);
                endingCommand.done(currentLine);
                executionStack.peek().addSubExecution(endingCommand);
                _allCommandExecutions.add(endingCommand);
            }
            
            // Pick command error status numbers but skip -1403 as it's a common error and not necessarily an issue
            if (nextLine.getMessage().startsWith("*** RAISING ERROR") && !nextLine.getMessage().endsWith("-1403")) {
                _allCommandErrorStatusLines.add(new Pair<TraceLine, TraceLine>(currentLine, nextLine));
            }
            return true;
        }
        
        return false;
    }
    
    private boolean handleRequestStart(TraceLine currentLine, Stack<Execution> executionStack) {
        if (currentLine.getMessage().startsWith("Server got:")) {
            if (_lastRequest != null) {
                ClientExecution dExecution = new ClientExecution(_lastRequest.getEndLine(), currentLine, null);
                _allDeadTime.add(dExecution);
                _allExecutions.add(dExecution);
                _lastRequest = null;
            }
            
            String commandRequest = currentLine.getMessage().substring("Server got:".length());
            executionStack.clear();
            pushExecutionStack(new RequestExecution(commandRequest, currentLine, null), executionStack);
            
            return true;
        }
        
        return false;
    }
    
    private boolean handleRequestEnd(TraceLine currentLine, Stack<Execution> executionStack) {
        if (currentLine.getMessage().equals("Dispatched command")) {
            if (!executionStack.isEmpty()) {
                RequestExecution request = (RequestExecution) popExecutionStack(currentLine, executionStack);
                request.done(currentLine);
                _allRequests.add(request);
                _allExecutions.add(request);
                _lastRequest = request;
                
            }

            executionStack.clear();
            
            return true;
        }
        
        return false;
    }
    
//    // TODO - add in this to exclude stack printing time from command self time.
//    private boolean handlePrintingArgumentsStart(TraceLine currentLine) {
//        if (!_executionStack.isEmpty()
//                && !(_executionStack.peek() instanceof PrintingArgumentsExecution)
//                && currentLine.getLogger().contains("(Argument  )")) {
//            PrintingArgumentsExecution execution = new PrintingArgumentsExecution(currentLine, getParentExecution());
//            pushCommandStack(execution);
//            
//            return true;
//        }
//        
//        return false;
//    }
//    
//    private boolean handlePrintingArgumentsEnd(TraceLine currentLine, TraceLine nextLine) {
//        if (!_executionStack.isEmpty() && 
//                _executionStack.peek() instanceof PrintingArgumentsExecution && !nextLine.getMessage().contains("--------------------------------------")
//                && !nextLine.getLogger().contains("(Argument  )")) {
//            Execution exec = popCommandStack(currentLine);
//            exec.done(currentLine);
//            _executionStack.peek().addSubExecution(exec);
//            return true;
//        }
//        
//        return false;
//    }
    
    private boolean handleSqlStart(TraceLine currentLine, TraceLine nextLine, Stack<Execution> executionStack) {
        boolean found = false;
        if (currentLine.getMessage().contains("UNBIND:")) {
            int index = currentLine.getMessage().indexOf("UNBIND:");
            handleSqlStartRegister(currentLine.getMessage().substring(index + "UNBIND:".length() + 1),
                currentLine, executionStack);
            found = true;
        }
        else if (currentLine.getMessage().contains("XLATE:") && nextLine != null 
                && nextLine.getMessage().equals("Executing statement without parameters")) {
            int index = currentLine.getMessage().indexOf("XLATE:");
            handleSqlStartRegister(currentLine.getMessage().substring(index + "XLATE:".length() + 1),
                currentLine, executionStack);
            found = true;
            
        }
        else if (currentLine.getMessage().contains("JDBC: Connection.prepareStatement(")) {
            int index = currentLine.getMessage().indexOf("JDBC: Connection.prepareStatement(");
            handleSqlStartRegister(currentLine.getMessage().substring(index + "JDBC: Connection.prepareStatement(".length(), currentLine.getMessage().length() - 1),
                currentLine, executionStack);
            found = true;
        }
        
        return found;
    }
    
    private void handleSqlStartRegister(String sqlStatement, TraceLine currentLine, Stack<Execution> executionStack) {
        SqlExecution sql = new SqlExecution(sqlStatement, currentLine, getParentExecution(executionStack));
        pushExecutionStack(sql, executionStack);
    }
    
    private boolean handleSqlEnd(TraceLine currentLine, Stack<Execution> executionStack) {
        if (currentLine.getMessage().contains("Execute Time:") || currentLine.getMessage().equals("JDBC: ...Statement.close()")) {
            if (!executionStack.isEmpty()) {
                SqlExecution endingCommand = (SqlExecution) popExecutionStack(currentLine, executionStack);
                endingCommand.done(currentLine);
                _allSqlExecutions.add(endingCommand);
                executionStack.peek().addSubExecution(endingCommand);
            }
        }
        
        return false;
    }

    public static String formatTime(long timeInMs) {
        if (timeInMs >= 1000000) {
            return timeInMs/1000000.0 + "m";
        }
        if (timeInMs >= 1000) {
            return timeInMs/1000.0 + "s";
        }
        else {
            return timeInMs + "ms";
        }
    }
    
    private void printReport(PrintStream stream, Reporter report, TraceAnalysis analysis) {
        printBanner(stream, report);
        report.printReport(stream, analysis);
    }
    
    private void printBanner(PrintStream stream, Reporter reporter) {
        stream.println("***************************************************************************");
        stream.println("Report Section: " + reporter.getSectionName());
        stream.println(reporter.getDescription());
        stream.println("***************************************************************************\n");
    }
    
    private void pushExecutionStack(Execution execution, Stack<Execution> executionStack) {
        //System.err.println("push: " + execution.getStartLine());
        executionStack.push(execution);
    }
    
    private Execution popExecutionStack(TraceLine endLine, Stack<Execution> executionStack) {
        Execution exec = executionStack.pop(); 
        //System.err.println("pop:" + endLine);
        return exec;
    }
    
    private Execution getParentExecution(Stack<Execution> executionStack) {
        if (executionStack.isEmpty()) {
            return null;
        }
        else {
            return executionStack.peek();
        }
    }
    
    private TraceParser findTraceParser() {
        BufferedReader br = null;
        TraceParser foundParser = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(_file), "UTF-8"));
            String lineToTest = br.readLine();
            foundParser = null;
            for (TraceParser parser : AVAILABLE_PARSERS) {
                if (parser.isNewLine(lineToTest)) {
                    foundParser = parser;
                    break;
                }
            }
            
            if (foundParser == null) {
                throw new RuntimeException("The log format is unrecognized");
            }
        }
        catch (IOException e) {
            System.err.println("Unable to find an appropriate parser for the log file due to exception: " + e);
            throw new RuntimeException(e);
            
        }
        finally {
            if (br != null) try {
                br.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return foundParser;
    }
    
    
    
    // The file name (can be absolute or relative path)
    private final String _file;
    
    private String _lastLineRead = "";
    private RequestExecution _lastRequest;
    private final List<RequestExecution> _allRequests = new ArrayList<RequestExecution>();
    private final List<ClientExecution> _allDeadTime = new ArrayList<ClientExecution>();
    private final List<Execution> _allExecutions = new ArrayList<Execution>();
    private final List<CommandExecution> _allCommandExecutions = new ArrayList<CommandExecution>();
    private final List<SqlExecution> _allSqlExecutions = new ArrayList<SqlExecution>();
    private final List<TracePause> _allPausesAboveThreshold = new ArrayList<TracePause>();
    private final List<TraceLine> _allErrorLines = new ArrayList<TraceLine>();
    private final List<Pair<TraceLine, TraceLine>> _allCommandErrorStatusLines = new ArrayList<Pair<TraceLine, TraceLine>>();
    
    
}
