/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
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

package com.sam.webservices.config;

/**
 * Constants used from XMLServiceConfigReader and XFireServiceGenerator
 * 
 * <b>
 * 
 * <pre>
 *  Copyright (c) 2016 Sam Corporation
 *  All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */

public class ServiceConfigConstants {

    public static final String PACKAGE = "package ";

    public static final String PUBLIC_CLASS = "public class ";

    public static final String PUBLIC = "public ";

    public static final String PRIVATE = "private ";

    public static final String PUBLIC_VOID = "public void ";

    public static final String GET = "get";

    public static final String SET = "set";

    public static final String IMPORTS = "import com.sam.moca.MocaException;\n"
            + "import com.sam.moca.MocaResults;\n"
            + "import com.sam.moca.client.MocaConnection;\n"
            + "import com.sam.moca.util.MocaUtils;\n"
            + "import com.sam.webservices.ServiceUtils;\n"
            + "import com.sam.webservices.UserToken;\n"
            + "import com.sam.webservices.ServiceException;\n"
            + "import javax.jws.WebMethod;\n"
            + "import javax.jws.WebParam;\n"
            + "import javax.jws.WebResult;\n"
            + "import javax.jws.WebService;";
    
    public static final String ANNOTATION_BEGIN = "@com.sam.moca.MocaColumn(name=\"";
    public static final String ANNOTATION_END = "\")";

//    public static final String MOCA_CONNECTION_STRING = "MocaConnection ___conn = ServiceUtils.getConnection(__token);";
//
    public static final String COMMAND_STRING = "StringBuffer ___command = new StringBuffer(\"noop \");";

    public static final String COMMAND_APPEND = "___command.append(\"";

    public static final String MOCA_RESULT_STRING = "MocaResults ___results = ServiceUtils.execute(__token, ___command.toString()); ";
    
    public static final String NEW_LINE="\n";
    
    public static final String FOUR_SPACES="    ";
    public static final String EIGHT_SPACES="        ";    

}
