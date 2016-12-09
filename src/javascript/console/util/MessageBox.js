/*
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/javascript/console/util/MessageBox.js $
 *  $Author: mlange $
 *  $Date: 2010-10-28 14:19:19 -0500 (Thu, 28 Oct 2010) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

Ext.namespace("RP.Moca.util");

RP.Moca.util.Msg = {
    alert: function(title, msg) {
	var fixedMsg = msg.replace(/\n/g, '<br>');
	Ext.Msg.alert(title, fixedMsg + '<br>');
    }
};