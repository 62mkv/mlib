package com.redprairie.moca.applications;

import com.redprairie.moca.applications.wsdeploy.WebServiceBuilder;

/**
 * Class to compile and deploy WebService code via Ant tasks.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author jbarnard
 */

public class WebServiceBuilderMain {

    
    /**
     * For each prod-dir, locate ALL war files for this prod-dir. For each war
     * in a prod-dir (EXCLUDING %LESDIR%) : if directory structure matches
     * war-name then call Ant tasks to compile and deploy web-services code.
     * 
     * @param args
     */
    public static void main(String[] args) {
        String arg = null;
        
        //This only happens if they're passing the 
        // prod dirs on the command line.
        if(args.length > 0) {
           arg = args[0];
       }
        
        WebServiceBuilder wsb = new WebServiceBuilder(arg);
        System.exit(wsb.build());
    }

}
