/*
 *  $URL$
 *  $Author$
 *  $Date$
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

Ext.define("RP.Moca.util.GroupingStore", {
    extend: "Ext.data.Store",

    initComponent: function () {
        Ext.apply(this, {
            listeners: {
                exception: function () {
                    window.alert('A connection with the server could not be established.\n\n' +
                    'Check to make sure the server has not been shutdown and try again.\n\n');
                }
            }
        });

        this.callParent(arguments);
    },
    
    constructor: function() {
      console.log("I don't work yet");
    },
    
    clearGrouping: function() {
      console.log("I don't work yet");
    },
    
    groupBy: function(field, forceRegroup) {
      console.log("I don't work yet");
    }
});
