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
 * This is a test java component that simply returns MocaResults displaying if
 * there are MocaContext, MadFactory, and another argument present in the method
 * calls.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class TestJavaComponent {

    public MocaResults testMethodWithContext(MocaContext context) {
        return buildResults(context, null, null);
    }

    public MocaResults testMethodWithContextAndArgs(MocaContext context, String arg) {
        return buildResults(context, null, arg);
    }

    public MocaResults testMethodWithFactory(MadFactory factory) {
        return buildResults(null, factory, null);
    }

    public MocaResults testMethodWithContextAndFactory(MocaContext context,
                                                       MadFactory factory) {
        return buildResults(context, factory, null);
    }

    public MocaResults testMethodWithContextAndFactoryAndArgs(MocaContext context,
                                                              MadFactory factory,
                                                              String arg) {
        return buildResults(context, factory, arg);
    }

    private MocaResults buildResults(MocaContext context, MadFactory factory, Object arg) {
        EditableResults results = new SimpleResults();
        results.addColumn("Context-Present", MocaType.BOOLEAN);
        results.addColumn("Factory-Present", MocaType.BOOLEAN);
        results.addColumn("Arg-Present", MocaType.BOOLEAN);

        results.addRow();
        results.setBooleanValue("Context-Present", context != null ? true : false);
        results.setBooleanValue("Factory-Present", factory != null ? true : false);
        results.setBooleanValue("Arg-Present", arg != null ? true : false);

        return results;
    }

}
