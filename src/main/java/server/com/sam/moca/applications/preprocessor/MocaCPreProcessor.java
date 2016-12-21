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

package com.sam.moca.applications.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.db.DBType;
import com.sam.moca.util.MocaUtils;

/**
 * This class is used to preprocess C files
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaCPreProcessor extends MocaPreProcessor {
    
    public MocaCPreProcessor(String directory) {
        super();
        _currentDirectory = directory;
    }

    // @see com.sam.moca.applications.preprocessor.MocaPreProcessor#parseFile(java.lang.String, com.sam.moca.server.db.DBType, java.lang.Appendable)
    @Override
    protected void parseFile(String fileName, DBType type, Appendable appendable)
            throws Exception {
        new CPreProcessor(fileName, type, appendable).parse();
    }
    
    /**
     * This class actually handles the processing of the File to the appendable
     * 
     * <b><pre>
     * Copyright (c) 2016 Sam Corporation
     * All Rights Reserved
     * </pre></b>
     * 
     * @author wburns
     * @version $Revision$
     */
    protected class CPreProcessor {
        
        public CPreProcessor(String name, DBType type, 
                Appendable appendable) {
            super();
            _fileName = name;
            _dbType = type;
            // By default the . search path is used
            _searchPath.add(_currentDirectory);
            _appendable = appendable;
        }
        
        /**
         * This will parse the file provided in the constructor
         * @throws Exception If any exception occurs while processing
         */
        public void parse() throws Exception {
            // Define the database type
            if (_dbType == DBType.MSSQL) {
                _defines.put("SQL_SERVER", null);
            }
            else {
                _defines.put(_dbType.toString(), null);
            }
            
            parseFile(_fileName);
        }
        
        /**
         * This parses the given file.  This is used to nest file parsing
         * in themselves
         * @param fileName The file to parse
         * @throws Exception If any exception occurs while processing
         */
        private void parseFile(String fileName) throws Exception {
            File foundFile = null;
            
            for (String path : _searchPath) {
                File testFile = new File(path + File.separator + fileName);
                
                // If the file exists and is a real file then we use that one
                if (testFile.exists() && testFile.isFile()) {
                    foundFile = testFile;
                    break;
                }
            }
            
            if (foundFile == null) {
                foundFile = new File(fileName);
                
                // If the file doesn't exist or it isn't a file then we
                // have to throw an exception
                if (!foundFile.exists() || !foundFile.isFile()) {
                    throw new FileNotFoundException("File " + fileName + 
                            " not found or is a directory.");
                }
            }
            
            // Initialize values that only matter for each file individually
            String currentDirectory = foundFile.getParent();
            boolean passedIfTests = true;
            LinkedList<Boolean> ifTests = new LinkedList<Boolean>();
            ReadMode mode = ReadMode.NORMAL;
            
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(foundFile), "UTF-8"));
            try {
                String lineRead;
                int lineCount = 0;
                
                // Loop through each line
                while ((lineRead = fileReader.readLine()) != null) {
                    lineCount++;
                    
                    if (mode == ReadMode.INCOMMENT) {
                        // If we end the comment, revert back to normal mode
                        if (lineRead.matches(".*?\\*/\\s*$"))  {
                            mode = ReadMode.NORMAL;
                        }
                        // If not then continue to next line
                        // TODO what about if we have code after the comment Example: *****/ Something
                        continue;
                    }
                    
                    // Now we are not in a comment so actually do stuff
                    
                    // If the line contins /*NOCONV*/ then just keep as is
                    if (lineRead.matches(".*?/\\*NOCONV\\*/.*")) {
                        _appendable.append(lineRead);
                        _appendable.append('\n');
                        continue;
                    }
                    // Now we remove any commments from the line 
                    // Example: /* any char */
                    lineRead = lineRead.replaceAll("/\\*.*?\\*/", "");
                    
                    // Ignore lines that start with REM or -- ignoring extra
                    // whitespace that line may have
                    if (lineRead.matches("^\\s*REM.*") || 
                            lineRead.matches("^\\s*--.*")) {
                        continue;
                    }
                    
                    Matcher inlineCommentMatcher = 
                        _inlineCommentPattern.matcher(lineRead);
                    // If we are starting a new comment block
                    // Example: /* some stuff or create table /* comment
                    if (inlineCommentMatcher.matches()) {
                        mode = ReadMode.INCOMMENT;
                        // We now set the line to what it is without the comments
                        lineRead = inlineCommentMatcher.group(1);
                    }
                    
                    // Now we have to keep going to next line if there was a
                    // backslash at the end until we find a line without one
                    // We also want to match new lines
                    while (_backslashEndingPattern.matcher(lineRead).matches()) {
                        String nextLine = fileReader.readLine();
                        
                        // We want to remove the ending \ with a new line and
                        // the next value read from the reader
                        lineRead = lineRead.substring(0, lineRead.length() - 1)
                                .concat('\n' + nextLine);
                        lineCount++;
                        
                    }
                    
                    // If the line starts with a # in the beginning and has
                    // some kind of words (lowercase) then do processing for it
                    // We also keep the grouping of the name of the # as
                    // well as it's value if required
                    // Example: # include Foo or #endif
                    Matcher poundMatcher = _poundPattern.matcher(lineRead);
                    if (poundMatcher.matches()) {
                        // [a-z]+ part
                        String poundName = poundMatcher.group(1);
                        // .* part
                        String poundValue = poundMatcher.group(2);
                        
                        // If it was a # define do this stuff
                        if (passedIfTests && poundName.equals("define")) {
                            // Find the groupings for the following pattern
                            // Example: CREATE_TABLE_TABLESPACE(name, storage) in name
                            // Group 1: CREATE_TABLE_TABLESPACE
                            // Group 2: name, storage
                            // Group 3: in name
                            Matcher defineMatcherWithArguments = _definePatternWithArguments
                                    .matcher(poundValue);
                            // Find the groupings for the following pattern
                            // Example: DBANSI_FLOAT real
                            // Group 1: DBANSI_FLOAT
                            // Group 2: real
                            Matcher defineMatcherWithNoArguments = _definePatternWithNoArguments
                                    .matcher(poundValue);
                            
                            if (defineMatcherWithArguments.matches()) {
                                String function = defineMatcherWithArguments.group(1);
                                String arguments = defineMatcherWithArguments.group(2);
                                String values = defineMatcherWithArguments.group(3);
                                
                                _functionArguments.put(function, arguments);
                                _functionValues.put(function, values);
                            }
                            else if (defineMatcherWithNoArguments.matches()){
                                String define = defineMatcherWithNoArguments.group(1);
                                String value = defineMatcherWithNoArguments.group(2);
                                
                                // If our value is already previously defined
                                // replace it with it's defintion.  If not
                                // present then just leave it
                                Matcher innerDefineMatcher = 
                                    _defineReplacementPattern.matcher(value);
                                
                                while (innerDefineMatcher.find()) {
                                    String word = innerDefineMatcher.group(1);
                                    
                                    if (_defines.containsKey(word)) {
                                        value = value.replaceFirst("\\b" + word 
                                                + "\\b", _defines.get(word));
                                    }
                                }
                                    
                                _defines.put(define, value.trim());
                            }
                        }
                        else if (passedIfTests && poundName.equals("undef")) {
                            // Just undefine the desired value
                            _defines.remove(poundValue);
                        }
                        else if (passedIfTests && poundName.equals("include")) {
                            // Find the grouping for the following pattern
                            // Example: #include <mocacolwid.h>
                            // Group 1: <
                            // Group 2: mocacolwid.h
                            Matcher defineIncludeMatcher = 
                                _defineIncludePattern.matcher(poundValue);
                            // If it doesn't match then throw an error
                            if (!defineIncludeMatcher.matches()) {
                                _logger.warn("Encountered " + poundValue + 
                                        " after #include, excpect <..> or \"..\"");
                                throw new ParseException("#include incorrect "
                                        + "syntax found " + fileName + ":"
                                        + lineCount, lineCount);
                            }
                            
                            String type = defineIncludeMatcher.group(1);
                            String file = defineIncludeMatcher.group(2);
                            
                            try {
                                // If it is a " type then put the current 
                                // directory at the front
                                if (type.equals("\"")) {
                                    _searchPath.push(currentDirectory);
                                }
                                // Now we parse the next file to make sure
                                parseFile(file);
                            }
                            finally {
                                // We have to remove that from the search path
                                // since our parent file shouldn't have that
                                if (type.equals("\"")) {
                                    _searchPath.pop();
                                }
                            }
                        }
                        else if (poundName.equals("use")) {
                            String value = MocaUtils
                                    .expandEnvironmentVariables(ServerUtils
                                            .globalContext(), poundValue);

                            File useFile = new File(value);
                            
                            // If the path is a directory use that at the
                            // end of the search path
                            if (useFile.exists() && useFile.isDirectory()) {
                                _searchPath.add(useFile.getAbsolutePath());
                            }
                            // otherwise we assume it is a file relative to
                            // our current file directory and add to the end
                            else {
                                _searchPath.add(currentDirectory + 
                                        File.separator + value);
                            }
                        }
                        else if (poundName.equals("ifdef")) {
                            Boolean ifTest = Boolean.FALSE;
                            if (_defines.containsKey(poundValue)) {
                                ifTest = Boolean.TRUE;
                            }
                            
                            ifTests.push(ifTest);
                            passedIfTests = determinePass(ifTests);
                        }
                        else if (poundName.equals("ifndef")) {
                            Boolean ifTest = Boolean.TRUE;
                            if (_defines.containsKey(poundValue)) {
                                ifTest = Boolean.FALSE;
                            }
                            
                            ifTests.push(ifTest);
                            passedIfTests = determinePass(ifTests);
                        }
                        else if (poundName.equals("error")) {
                            throw new ParseException(poundValue + " at " + 
                                    fileName + ":" + lineCount, lineCount);
                        }
                        else if (poundName.equals("else")) {
                            if (ifTests.isEmpty()) {
                                throw new ParseException("Unmatched #endif at " 
                                        + fileName + ":" + lineCount, lineCount);
                            }
                            
                            // Now we remove the top and invert the value of
                            // the last test since it is an else; treat null as
                            // false so it becomes true
                            Boolean test = ifTests.poll();
                            
                            ifTests.push(test == null ? Boolean.TRUE : !test);
                            passedIfTests = determinePass(ifTests);
                        }
                        else if (poundName.equals("endif")) {
                            if (ifTests.isEmpty()) {
                                throw new ParseException("Unmatched #endif at " 
                                        + fileName + ":" + lineCount, lineCount);
                            }
                            
                            // Remove the last test result and update
                            ifTests.pop();
                            passedIfTests = determinePass(ifTests);
                        }
                        else if (poundName.equals("if")) {
                            throw new ParseException(
                                    "Unable to parse condition: " + lineRead,
                                    lineCount);
                        }
                        else if (poundName.equals("elif")) {
                            throw new ParseException(
                                    "Unable to parse condition: " + lineRead,
                                    lineCount);
                        }
                    }
                    // This is where actual code is at
                    else if (passedIfTests) {
                        
                        // If the line was empty just ignore it
                        if (lineRead.trim().length() == 0) {
                            continue;
                        }

                        String replacedLine = lineRead;
                        LinkedList<String> replacements = new LinkedList<String>();

                        // Now we loop through until the last replacement had
                        // no effect
                        while (!replacedLine.equals(replacements.peekLast())) {
                            replacements.addLast(replacedLine);

                            replacedLine = replaceFunctions(replacedLine,
                                    fileName, lineCount);
                            replacedLine = replaceDefines(replacedLine);
                        }

                        // If the line was empty after replacement also ignore
                        // it so we don't have extra whitespace
                        if (replacedLine.trim().length() == 0) {
                            continue;
                        }
                        
                        _appendable.append(replacedLine);
                        _appendable.append('\n');
                    }
                }
            }
            finally {
                if (fileReader != null) {
                    fileReader.close();
                }
            }
        }
        
        /**
         * This will check the list of booleans.  If all of them are true
         * it will return true.  If any are false or null it will return false
         * @param ifTests The list of booleans to check
         * @return whether or not the list is all true or not
         */
        private boolean determinePass(List<Boolean> ifTests) {
            
            for (Boolean test : ifTests) {
                if (test == null || !test) {
                    return false;
                }
            }
            return true;
        }

        private String replaceFunctions(String lineRead, String fileName,
                int lineCount) throws ParseException, MocaException {
            Matcher functionReplacementMatcher = _functionReplacementPattern
                    .matcher(lineRead);
            StringBuilder replacedBuilder = new StringBuilder();
            int previousMatch = 0;
            // Find the grouping for the following pattern
            // Example: CREATE_TABLE(moca_dbversion)
            // Group 1: CREATE_TABLE
            // Group 2: moca_dbversion
            while (functionReplacementMatcher.find()) {
                String function = functionReplacementMatcher.group(1);
                String arguments = functionReplacementMatcher.group(2);
                
                replacedBuilder.append(lineRead.substring(previousMatch,
                        functionReplacementMatcher.start()));

                previousMatch = functionReplacementMatcher.end();

                if (_functionValues.containsKey(function)) {
                    String functionReplaced = replaceFunction(function,
                            arguments, fileName, lineCount);
                    // Now we actually replace the old function
                    // with the new values for it

                    replacedBuilder.append(functionReplaced);
                    replacedBuilder.append(' ');
                }
                else if (function.equals("__MOCA")) {
                    String replacedValues = replaceDefines(arguments);

                    MocaContext moca = MocaUtils.currentContext();

                    MocaResults results = moca
                            .executeCommand(replacedValues);

                    if (results.getColumnCount() != 1
                            || results.getRowCount() != 1) {
                        throw new ParseException("Command execution returned ["
                                + results.getColumnCount() + "] columns and ["
                                + results.getRowCount()
                                + "] rows.  Only 1 of each is allowed. At "
                                + fileName + ":" + lineCount, lineCount);
                    }

                    // This should be good since we know we have only 1 row
                    results.next();

                    Object value = results.getValue(0);
                    // Now we just append it and use the to string method on it
                    replacedBuilder.append(value);
                }
                else {
                    replacedBuilder.append(lineRead.substring(
                            functionReplacementMatcher.start(),
                            functionReplacementMatcher.end()));
                }
            }

            // Now we append the end that didn't match
            replacedBuilder.append(lineRead.substring(previousMatch));

            return replacedBuilder.toString();
        }

        /**
         * This will replace the given function and values with the defined
         * values in the preprocessed code to replace with
         * 
         * Example for SQL SERVER: CREATE_TABLE(comp_ver) -> create table
         * dbo.comp_ver
         * 
         * @param function
         *            The function encountered ie. CREATE_TABLE
         * @param values
         *            The arguments of that function ie. comp_ver If these
         *            values are mapped to environment variables they will be
         *            replaced with the environment equivalent value
         * @param fileName
         *            The name of the file this function came from, this is
         *            primarily used for proper exception messages
         * @param lineCount
         *            The line the function was encountered on was
         * @return The function as it is replaced with the proper define
         * @throws ParseException
         *             This is thrown if the number of arguments provided
         *             doesn't match how many the define was determined to have
         */
        private String replaceFunction(String function, String values,
                String fileName, int lineCount) throws ParseException {
            String replacedFunction = _functionValues.get(function);
            
            // If the function isn't available just short circuit
            if (replacedFunction.trim().length() == 0) {
                return replacedFunction;
            }
            
            replacedFunction = replacedFunction.replaceAll("\\n\\s+", "");
            
            String[] splitArguments = _functionArguments.get(function).trim().split(",");
            String[] splitValues = values.trim().split(",");
            
            if (splitArguments.length != splitValues.length) {
                throw new ParseException("There was an unmatched argument for " +
                                "function " + function + ".  Expected " + 
                                splitArguments.length + " arguments but got " + splitValues.length + 
                                " at " + fileName + ":" + lineCount, lineCount);
            }
            
            // Loop through all of the arguments
            for (int i = 0; i < splitArguments.length; i++) {
                String argument = splitArguments[i].trim();
                String value = splitValues[i].trim();
                
                // Now we
                String replacedValue = MocaUtils.expandEnvironmentVariables(
                        ServerUtils.globalContext(), value);
                
                replacedValue = replaceDefines(replacedValue);
                
                // Replace the argument with the actual value but only if it 
                // itself is a word
                replacedFunction = replacedFunction.replaceAll("\\b" + argument 
                        + "\\b", replacedValue);
            }
            
            replacedFunction = replacedFunction.replaceAll("##", "");
            return replacedFunction;
        }
        
        /**
         * This will replace any simple defines left over that just define a
         * name to a value.  If a match for a define name is in quotes ('' or "")
         * then it will not replace the value and leave it as is.
         * 
         * Example:
         *     #define CMPLVL_LEN                100
         *     
         * @param line The line to check if there are any values that match the
         *        define name ie. CMPLVL_LEN
         * @return The line with the define names replaced with their values
         */
        private String replaceDefines(String line) {
            StringBuilder stringBuilder = new StringBuilder();
            ArrayList<String> matchList = new ArrayList<String>();
            
            Matcher quoteLimiterMatcher = _quoteLimiterPattern.matcher(line);
            int index = 0;
            
            // Loop through all of the matches, for each one put in the string
            // up to the quote as well as the quote in the list
            while (quoteLimiterMatcher.find()) {
                int start = quoteLimiterMatcher.start();
                int end = quoteLimiterMatcher.end();
                
                String match = line.subSequence(index, start).toString();
                matchList.add(match);
                index = end;
                matchList.add(line.subSequence(start, end).toString());
            }
            
            // If there were any matches add the last sequence
            if (matchList.size() != 0) {
                matchList.add(line.subSequence(index, line.length()).toString());
            }
            // If there were no matches just add the line
            else {
                matchList.add(line);
            }
            
            boolean inQuote = false;
            
            for (String word : matchList) {
                if (word.equals("'") || word.equals("\"")) {
                    inQuote = !inQuote;
                }
                else if (!inQuote) {
                    Matcher defineReplacementMatcher = 
                        _defineReplacementPattern.matcher(word);
                    
                    String wordReplaced = word;
                    while (defineReplacementMatcher.find()) {
                        String replace = word.substring(
                                defineReplacementMatcher.start(),
                                defineReplacementMatcher.end());
                        
                        if (_defines.containsKey(replace)) {
                            String replacedString = _defines.get(replace);
                            
                            // Now we loop through our defines in case if we
                            // were unable to replace the value earlier.  This
                            // could possibly get stuck in an infinite loop if
                            // we have defines that reference each other
                            while (_defines.containsKey(replacedString)) {
                                replacedString = _defines.get(replacedString);
                            }
                            
                            wordReplaced = wordReplaced.replaceFirst("\\b" + 
                                    replace + "\\b", replacedString);   
                        }
                    }
                    word = wordReplaced;
                }
                
                stringBuilder.append(word);
            }
            
            return stringBuilder.toString();
        }

        LinkedList<String> _searchPath = new LinkedList<String>();
        Map<String, String> _defines = new HashMap<String, String>();
        Map<String, String> _functionValues = new HashMap<String, String>();
        Map<String, String> _functionArguments = new HashMap<String, String>();
        
        private final Appendable _appendable;
        private final String _fileName;
        private final DBType _dbType;
    }
    
    private final static Pattern _poundPattern = Pattern.compile(
            "\\s*#\\s*([a-z]+)\\s*(.*)", Pattern.DOTALL);

    private final static Pattern _definePatternWithArguments = Pattern.compile(
            "^(\\S+)\\(([^)]*)\\)\\s*(.*)", Pattern.DOTALL);

    private final static Pattern _definePatternWithNoArguments = Pattern
            .compile("^(\\S+)\\s*(.*)", Pattern.DOTALL);

    private final static Pattern _defineIncludePattern = Pattern
            .compile("([<\"])([^>\"]+).*");

    /**
     * This looks for the following things
     * 1. 3 letter word or more
     * 2. Left Parenthesis '('
     * 3a. Word characters, ',', !, =, @, <, >, ', ", |, :
     * 3b. Wrapped brackets with anything in it [ * ]
     * 4. Right Parenthesis ')'
     */
    private final static Pattern _functionReplacementPattern = Pattern
            .compile("(\\b[\\w]{3,}\\b)\\s*\\(\\s*((?:[\\w,:\'<>\\|\"!=@\\s]+)|(?:\\[.*?\\]))\\s*\\)\\s*",
                    Pattern.DOTALL);

    private final static Pattern _defineReplacementPattern = Pattern.compile(
            "(\\b[\\w]{3,}\\b)", Pattern.DOTALL);

    private final static Pattern _backslashEndingPattern = Pattern.compile(
            ".*?\\\\$", Pattern.DOTALL);
    
    private final static Pattern _inlineCommentPattern = Pattern.compile(
            "^(.*?)/\\*.*", Pattern.DOTALL);

    private final static Pattern _quoteLimiterPattern = Pattern
            .compile("['\"]");
    
    private final String _currentDirectory;
}
