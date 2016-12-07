/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.webservices.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main object having collection of all the services
 * 
 * <b>
 * 
 * <pre>
 *   Copyright (c) 2005 RedPrairie Corporation
 *   All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */

public class ServiceConfiguration {

    /**
     * @return Returns the _services.
     */
    public Service[] getServices() {
        return _services.values().toArray(
                new Service[_services.values().size()]);
    }

    /**
     * @param service
     *            The service to add to services.
     */
    public void addService(Service service) {
        Service currentService = _services.get(service.getName());
        if (currentService != null) {
            currentService.addAllOperations(service.getOperations());
        }
        else {
            _services.put(service.getName(), service);
        }
    }

    // -----------------------------
    // implementation:
    // -----------------------------

    private Map<String, Service> _services = new LinkedHashMap<String, Service>();

}
