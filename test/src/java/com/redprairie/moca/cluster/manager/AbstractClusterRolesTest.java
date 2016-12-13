/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.cluster.manager;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;


/**
 * 
 * This abstract class assists with testing of cluster based integration
 * tests for roles managers. It handles creating 3 jobes with unique roles for testing.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public abstract class AbstractClusterRolesTest extends AbstractClusterManagerTest {
    
    @Override
    protected void mocaSetUp() throws Exception {
        super.mocaSetUp();
        // Bootstrap job data here
        deleteJobData();
        for (int i = 0; i < 3; i++) {
            _moca.executeCommand("add job where job_id = @role and name = @role and role_id = @role and enabled = 1 and type = 'timer' " +
                                     "and command = 'noop' and timer = 30",
                                     new MocaArgument("role", "node" + i));
                    
        }
        _moca.commit();
    }
    
    @Override
    protected void mocaTearDown() throws Exception {
        deleteJobData();
        _moca.commit();
        super.mocaTearDown();
    }
    
    private void deleteJobData() throws MocaException {
        _moca.executeCommand("[ delete from job_definition where job_id like 'node%' ] catch (-1403)");
        _moca.commit();
    }

}
