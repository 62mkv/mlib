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

package com.redprairie.moca.components.mocatest;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Tests @% (like reference) behavior in command arguments.
 * 
 * Copyright (c) 2015 JDA Software
 * All Rights Reserved
 */
public class TU_TestLikeInCommandArgument extends AbstractMocaTestCase {
	
	/**
	 * Called by command "test like in command"
	 * @param moca
	 * @param usedWildcard
	 * @param value
	 */
	public static void testLikeInCommandArgument(MocaContext moca, boolean usedWildcard, String value) {
		for (MocaArgument argument : moca.getArgs(true)) {
			if (argument.getName().equals("value")) {
				if (usedWildcard) {
					assertEquals(MocaOperator.LIKE, argument.getOper());
				}
				else {
					assertEquals(MocaOperator.EQ, argument.getOper());
				}
			}
		}
	}
	
	public void testLikeWithWildcardInValue() throws MocaException {
		testLikeRefBehavior("foo%", true);
		testLikeRefBehavior("foo_", true);
	}
	
	public void testLikeWithoutWildcardInValue() throws MocaException {
		testLikeRefBehavior("foo", false);
	}
	
	private void testLikeRefBehavior(String value, boolean wildcardUsed) throws MocaException {
		_moca.executeCommand("test like in command where @%value and usedWildcard = false",
				new MocaArgument("value", "foo"), new MocaArgument("usedWildcard", wildcardUsed));
	}

}
