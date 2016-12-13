/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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
package com.redprairie.moca.util;

import junit.framework.TestCase;

public class TU_MD5Utils extends TestCase {

    public void testEncode() {
        byte[] byteArray = {
                (byte) 0x19, (byte) 0xf2, (byte) 0x23, (byte) 0x34,
                (byte) 0xd5, (byte) 0x2a, (byte) 0x46, (byte) 0xbe
        };

        String test = MD5Utils.encode(byteArray, 5);
        assertTrue(test.equals("PGS72QKQAHHSB"));
    }

}
