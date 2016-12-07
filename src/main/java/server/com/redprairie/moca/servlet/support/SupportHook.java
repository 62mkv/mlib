/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.servlet.support;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;

/**
 * An interface that is implemented in order to generate data into the support zip
 * from the console. Any implementation class should be registered with hooks.xml
 * or statically through the SupportZip class.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author j1014843
 * @see SupportZip
 */
public interface SupportHook {
    
    /***
     * The types of support data allowed to be returned.
     * @author j1014843
     */
    public enum SupportType { MOCA_RESULTS, STRING };
    
    /***
     * 
     * This should be a descriptive name of the hook.  This will be used to 
     * name the file that's generated in the support zip.
     * 
     * @return name the name of the hook
     */
    public String getName();
    
    /***
     * Returns the type of data that the hook will return.
     * 
     * @return supportType generated 
     */
    public SupportType getType();
    
    /***
     * 
     * When using SupportType.MOCA_RESULTS, this method will be called to 
     * generate the data for the hook.
     * 
     * @param context MocaContext
     * @return the results of hook run
     * @throws Exception
     */
    public MocaResults getMocaResults(MocaContext context) throws Exception;
    
    /***
     * When using SupportType.STRING, this method will be called to 
     * generate the data for the hook.
     * 
     * @return the string of data
     * @throws Exception
     */
    public String getString(MocaContext context) throws Exception;
}
