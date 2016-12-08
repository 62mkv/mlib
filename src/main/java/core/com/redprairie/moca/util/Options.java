/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Option processing, similar to the old-fashioned unix getopt API.
 *
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author dinksett
 * @version $Revision$
 */
public class Options {

    /**
     * Parses an argument list according to a given option spec.  The
     * option spec consists of a sequence of option characters, each followed
     * by a colon (:) if the option takes an argument. The character (;) is 
     * now supported for instances where you have an action that you want to take
     * even if an argument isn't passed. (I.E <code>-t</code> does default 
     * <code>-t argument</code> might do something different)
     *
     * For example, <code>"p:t:TJx"</code> would allow the options
     * -p arg -t arg  -T -J -x
     *
     * @param spec a getopt-style option specification.
     * @param args the set of arguments to be parsed.
     * @return an instance of this class that represents the arguments
     * that were passed in as <code>args</code>.
     * @throws OptionsException if a set of arguments were passed that
     * does not conform to the option spec.
     */
    public static Options parse(String spec, String[] args)
            throws OptionsException {
        return new Options(spec, args);
    }

    /**
     * Determines if an option has been passed in this option set.
     * @param option an option character to test for.
     * @return <code>true</code> if the option was set in the arguments
     * that created this option set.
     */
    public boolean isSet(char option) {
        return _optionSet.contains(Character.valueOf(option));
    }

    /**
     * Returns an option argument passed in this option set for a given option.
     * 
     * This may now return null if an option was passed that was parsed using the
     * (;) operator.
     * @param option an option character to look for.
     * @return the argument given for the specified option.
     */
    public String getArgument(char option) {
        return _argMap.get(Character.valueOf(option));
    }

    /**
     * Gets the leftover arguments that were not parsed for option processing.
     * Anything after the last argument processed or after the "end of
     * arguments" marker (--) will be returned as remaining arguments.
     * @return the arguments that were unused in option processing.
     */
    public String[] getRemainingArgs() {
        return Arrays.copyOf(_remainingArgs, _remainingArgs.length);
    }

    //
    // Implementation
    //

    // Private constructor to force use of factory method
    private Options(String spec, String[] args) throws OptionsException {

        int a;
        for (a = 0; a < args.length; a++) {
            System.out.println("Parsing option char:" + args[a]);
            if (args[a].equals("--")) {
                a++;
                break;
            }
            else if (args[a].startsWith("-")) {
                for (int c = 1; c < args[a].length(); c++) {
                    char argChar = args[a].charAt(c);
                    int specPos = spec.indexOf(argChar);
                    if (specPos == -1) {
                        throw new OptionsException(
                                "unrecognized option: " + argChar + ".");
                    }

                    System.out.println("Putting option char:" + argChar);
                    _optionSet.add(Character.valueOf(argChar));

                    if (spec.length() > (specPos + 1) &&
                        spec.charAt(specPos + 1) == ':') {

                        // Is there more to this argument string?  If so,
                        // use it.  If not, take the next one.
                        if (args[a].length() > (c + 1)) {
                            _argMap.put(Character.valueOf(argChar), args[a].substring(c + 1));
                        }
                        else if (a + 1 < args.length) {
                            _argMap.put(Character.valueOf(argChar), args[a + 1]);
                            a++;
                        }
                        else {
                            throw new OptionsException(
                                    "option " + argChar + " requires an argument");
                        }
                        break;
                    }
                    
                    if (spec.length() > (specPos + 1) &&
                            spec.charAt(specPos + 1) == ';') {

                            // Is there more to this argument string?  If so,
                            // use it.  If not, take the next one.
                            if (args[a].length() > (c + 1)) {
                                _argMap.put(Character.valueOf(argChar), args[a].substring(c + 1));
                            }
                            
                            //If we are checking the next argument in the list, we have 
                            //to make sure it's not a flag.  Otherwise, we'll be all messed up.
                            else if (a + 1 < args.length && !args[a+1].startsWith("-")) {
                                _argMap.put(Character.valueOf(argChar), args[a + 1]);
                                a++;
                            }
                            
                            break;
                        }
                }
            }
            else {
                break;
            }
        }
        
        int remainingLength = args.length - a;
        _remainingArgs = new String[remainingLength];
        System.arraycopy(args, a, _remainingArgs, 0, remainingLength);
    }

    private Set<Character> _optionSet = new HashSet<Character>();
    private Map<Character, String> _argMap = new HashMap<Character, String>();
    private String[] _remainingArgs;
}
