/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.server.exec;

import java.util.Collections;

import org.junit.Test;
import static org.mockito.Mockito.*;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.SimpleResults;

/**
 * 
 * Tests for {@link MocaCommandUnit}
 * 
 * Copyright (c) 2015 JDA Software
 * All Rights Reserved
 */
public class TU_MocaCommandUnit {
    
    @Test
    public void testLikeArgumentWithWildcard() throws MocaException {
        testLikeInCommandArgument("foo%", MocaOperator.LIKE);
        testLikeInCommandArgument("foo_", MocaOperator.LIKE);
        testLikeInCommandArgument("fo%o", MocaOperator.LIKE);
        testLikeInCommandArgument("fo_o", MocaOperator.LIKE);
    }
    
    /**
     * Even if @% (REFLIKE) is used as a MOCA command argument, if the value doesn't
     * contain a wildcard it should be changed to an equal operator.
     * @throws MocaException
     */
    @Test
    public void testLikeArgumentUsesEqualWhenNoWildcardInValue() throws MocaException {
        testLikeInCommandArgument("foo", MocaOperator.EQ);
    }
    
    private void testLikeInCommandArgument(String argValue, MocaOperator expectedOperator) throws MocaException {
        String fakeCommand = "list job executions";
        ServerContext mockContext = mock(ServerContext.class);
        MocaCommandUnit commandUnit = new MocaCommandUnit();
        commandUnit.setVerbNounClause(fakeCommand);
        
        // Add a @% (REFLIKE) operator to the argument list for the command
        CommandArg likeArg = new CommandArg();
        likeArg.setName("job_id");
        likeArg.setOperator(MocaOperator.REFLIKE);
        commandUnit.setArgList(Collections.singletonList(likeArg));
        
        // Mock that the variable is available in the context and that the command returns an empty result set
        when(mockContext.getVariableAsArgument("job_id", true, false)).thenReturn(new MocaArgument("job_id", argValue));
        when(mockContext.executeNamedCommand(eq(fakeCommand), anyBoolean())).thenReturn(new SimpleResults());
        
        commandUnit.execute(mockContext);
        verify(mockContext).addArg(eq("job_id"), eq(expectedOperator), any(MocaValue.class));
    }
    
}
