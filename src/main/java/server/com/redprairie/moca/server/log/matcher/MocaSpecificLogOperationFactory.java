/*
 *  $URL:
 *  $Author:
 *  $Date:
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
package com.redprairie.moca.server.log.matcher;

import org.jdom.Element;

import ch.elca.el4j.services.xmlmerge.AbstractXmlMergeException;
import ch.elca.el4j.services.xmlmerge.Matcher;
import ch.elca.el4j.services.xmlmerge.Operation;
import ch.elca.el4j.services.xmlmerge.OperationFactory;
import ch.elca.el4j.services.xmlmerge.action.CompleteAction;
import ch.elca.el4j.services.xmlmerge.action.OrderedMergeAction;
import ch.elca.el4j.services.xmlmerge.action.ReplaceAction;
import ch.elca.el4j.services.xmlmerge.factory.StaticOperationFactory;
import ch.elca.el4j.services.xmlmerge.matcher.TagMatcher;

/**
 * OperationFactory implementation specific to MOCA logging xml format. <br/>
 * Features: <br/>
 *           * Adds elements from patches and original to create a new XML<br/>
 *           * Merges appenders and loggers and Routing, MocaRouting, routes elements<br/>
 *           * Allows overriding of original loggers and routes<br/>
 * <br/>
 * Note: This should only be used behind a TagMatcher, otherwise behavior is undefined. <br/>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author mdobrinin
 */
public class MocaSpecificLogOperationFactory implements OperationFactory {

    // @see ch.elca.el4j.services.xmlmerge.OperationFactory#getOperation(org.jdom.Element, org.jdom.Element)
    @Override
    public Operation getOperation(Element original, Element patch)
            throws AbstractXmlMergeException {
        if (original == null || patch == null) {
            return new CompleteAction();
        }

        if (areReplaceableElements(original, patch)) {
            // override an element
            return new ReplaceAction();
        }
        else if (areMergeableElements(original, patch)) {
            // merge elements
            // we have to be careful which elements we present to the OrderedMerge algorithm
            // we only match elements which have matching keys or names instead of a simple tag match
            // so that we can find overridden elements even if they are not first in the list
            // otherwise we would match on the first element and be forced to take action, losing the correct match later
            OrderedMergeAction o = new OrderedMergeAction();
            o.setMatcherFactory(new StaticOperationFactory(MocaSpecificTagMatcher.INSTANCE));
            o.setActionFactory(new MocaSpecificLogOperationFactory());
            return o;
        }
        else {
            // add both without overriding
            return new MocaAddAction();
        }
    }

    static boolean areReplaceableElements(Element original, Element patch) {
        return areMatchingLoggers(original, patch) ||
               areMatchingRouteElements(original, patch);
    }
    
    static boolean areMergeableElements(Element original, Element patch) {
        return areLoggersElements(original, patch) || 
               areAppendersElements(original, patch) || 
               areMatchingRoutesElements(original, patch) ||
               areMatchingRoutingAppenders(original, patch);
    }
    
    static boolean areMatchingRoutingAppenders(Element original, Element patch) {
        return areRoutingAppenders(original, patch) &&
               _nameMatcher.matches(original, patch);
    }

    static boolean areMatchingRoutesElements(Element original, Element patch) {
        return areRoutesElements(original, patch) &&
                _patternMatcher.matches(original, patch);
    }

    static boolean areMatchingRouteElements(Element original, Element patch) {
        return areRouteElements(original, patch) &&
                _keyMatcher.matches(original, patch);
    }

    static boolean areMatchingLoggers(Element original, Element patch) {
        return areLoggers(original, patch) &&
               _nameMatcher.matches(original, patch);
    }

    static boolean areRoutingAppenders(Element original, Element patch) {
        return ("Routing".equalsIgnoreCase(original.getName()) && "Routing".equalsIgnoreCase(patch.getName())) ||
                ("MocaRouting".equalsIgnoreCase(original.getName()) && "MocaRouting".equalsIgnoreCase(patch.getName()));
    }

    static boolean areRouteElements(Element original, Element patch) {
        return "Route".equalsIgnoreCase(original.getName()) &&
                "Route".equalsIgnoreCase(patch.getName());
    }

    static boolean areRoutesElements(Element original, Element patch) {
        return "Routes".equalsIgnoreCase(original.getName()) &&
                "Routes".equalsIgnoreCase(patch.getName());
    }

    static boolean areLoggers(Element original, Element patch) {
        return ("logger".equalsIgnoreCase(original.getName()) && "logger".equalsIgnoreCase(patch.getName()))
            || ("asyncLogger".equalsIgnoreCase(original.getName()) && "asyncLogger".equalsIgnoreCase(patch.getName()));
    }

    static boolean areAppendersElements(Element original, Element patch) {
        return "appenders".equalsIgnoreCase(original.getName()) && "appenders".equalsIgnoreCase(patch.getName());
    }

    static boolean areLoggersElements(Element original, Element patch) {
        return "loggers".equalsIgnoreCase(original.getName()) && "loggers".equalsIgnoreCase(patch.getName());
    }

    /**
     * Matcher which chooses which elements we are going to consider for merge/add/replace operations.
     *
     * Copyright (c) 2015 Sam Corporation
     * All Rights Reserved
     */
    public static class MocaSpecificTagMatcher implements Matcher {
        public static MocaSpecificTagMatcher INSTANCE = new MocaSpecificTagMatcher();

        private MocaSpecificTagMatcher() {}

        /**
         * We have to handle the following elements here: <br/>
         * * <em>loggers</em><br/>
         * * <em>appenders</em><br/>
         * * <em>routes</em><br/>
         * * <em>RoutingAppender</em> and <em>MocaRoutingAppender</em><br/>
         * * <em>logger</em> and <em>asyncLogger</em><br/>
         * * <em>route</em><br/>
         *
         * Default to regular tag matcher for future compatibility.
         * @return
         */
        public boolean matches(Element original, Element patch) {
            // identifiable elements must match by their ID
            if (areMatchingLoggers(original, patch) ||
                    areMatchingRouteElements(original, patch) ||
                    areMatchingRoutesElements(original, patch) ||
                    areMatchingRoutingAppenders(original, patch) ||
                    areLoggersElements(original, patch) ||
                    areAppendersElements(original, patch)) {
                return true;
            }

            // other known logging elements that do not have the same identifiable information -- don't match
            if (areLoggers(original, patch) ||
                    areRouteElements(original, patch) ||
                    areRoutesElements(original, patch) ||
                    areRoutingAppenders(original, patch)) {
                return false;
            }

            // otherwise revert to regular tag matcher
            return tagMatcher.matches(original, patch);
        }

        private final Matcher tagMatcher = new TagMatcher();
    }

    private static final Matcher _patternMatcher = new AttributeMatcher("pattern");
    private static final Matcher _nameMatcher = new AttributeMatcher("name");

    private static final Matcher _keyMatcher = new NullableAttributeMatcher("key");
}
