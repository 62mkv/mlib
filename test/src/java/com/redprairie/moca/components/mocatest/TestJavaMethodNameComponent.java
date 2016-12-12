/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.components.mocatest;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;

/**
 * This Class is used to test which method is chosen by MOCA.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class TestJavaMethodNameComponent {

    /*
     * Covers the case when there is solely a MocaContext arg.
     */
    public MocaResults testMethod(MocaContext context) {
        return buildResults("MocaContext");
    }

    /*
     * Covers the case when there is a MocaContext and a MadFactory arg.
     */
    public MocaResults testMethod(MocaContext context, MadFactory factory) {
        return buildResults("MocaContext MadFactory");
    }

    /*
     * Covers the case when there is an additional arg beyond MocaContext and
     * MadFactory args.
     */
    public MocaResults testMethod(MocaContext context, MadFactory factory, String arg) {
        return buildResults("MocaContext MadFactory String");
    }

    /*
     * Covers the case when there are two additional args beyond MocaContext and
     * MadFactory args.
     */
    public MocaResults testMethod(MocaContext context, MadFactory factory, String arg1,
                                  String arg2) {
        return buildResults("MocaContext MadFactory String String");
    }

    /*
     * Covers the case when there are MocaContext and an additional arg.
     */
    public MocaResults testMethod(MocaContext context, String arg) {
        return buildResults("MocaContext String");
    }

    /*
     * Covers the case when there are two additonal args other than MocaContext.
     */
    public MocaResults testMethod(MocaContext context, String arg1, String arg2) {
        return buildResults("MocaContext String String");
    }

    /*
     * Covers the case when there is solely a MadFactory arg.
     */
    public MocaResults testMethod(MadFactory factory) {
        return buildResults("MadFactory");
    }

    /*
     * Covers the case when there is one additonal arg beyond the MadFactory arg.
     */
    public MocaResults testMethod(MadFactory factory, String arg) {
        return buildResults("MadFactory String");
    }

    /*
     * Covers the case when there are two additonal args beyond the MadFactory arg.
     */
    public MocaResults testMethod(MadFactory factory, String arg1, String arg2) {
        return buildResults("MadFactory String String");
    }

    /*
     * Covers the case when a single string arg is provided.
     */
    public MocaResults testMethod(String arg) {
        return buildResults("String");
    }

    /*
     * Covers the case when two String args are provided.
     */
    public MocaResults testMethod(String arg1, String arg2) {
        return buildResults("String String");
    }

    /*
     * Covers the case when three string args are provided.
     */
    public MocaResults testMethod(String arg1, String arg2, String arg3) {
        return buildResults("String String String");
    }
    
    private MocaResults buildResults(String method) {
        EditableResults results = new SimpleResults();
        results.addColumn("Method Chosen", MocaType.STRING);

        results.addRow();
        results.setStringValue("Method Chosen", method);

        return results;
    }

}
