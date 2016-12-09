/*
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/javascript/console/Console.js $
 *  $Author: wburns $
 *  $Date: 2012-04-03 14:07:13 -0500 (Tue, 03 Apr 2012) $
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

Ext.namespace("RP.globals");
Ext.namespace("RP.globals.paths");

/* We blank out the fix ajax response so it doesn't mess with JSON */
RP.core.fixAjaxResponses = function(){};

RP.upb.PageBootstrapper.bootstrap({ 
	"pathInfo": "",
	"queryString": "",
	"showLogout": true,
	"currentModule" : "console.do" 
});

// This along with css defines allow for grid rows to be selectable
if(typeof Ext != 'undefined') {
  Ext.core.Element.prototype.unselectable = function() {
      return this;
  };
  Ext.view.TableChunker.metaRowTpl = [
   '<tr class="' + Ext.baseCSSPrefix + 'grid-row {addlSelector} {[this.embedRowCls()]}" {[this.embedRowAttr()]}>',
    '<tpl for="columns">',
     '<td class="{cls} ' + Ext.baseCSSPrefix + 'grid-cell ' + Ext.baseCSSPrefix + 'grid-cell-{columnId} {{id}-modified} {{id}-tdCls} {[this.firstOrLastCls(xindex, xcount)]}" {{id}-tdAttr}><div class="' + Ext.baseCSSPrefix + 'grid-cell-inner ' + Ext.baseCSSPrefix + 'unselectable" style="{{id}-style}; text-align: {align};">{{id}}</div></td>',
    '</tpl>',
   '</tr>'
  ];
}

Ext.getDoc().on("keydown", function(e) {
    if (e.ctrlKey && e.shiftKey) {
        if (e.getKey() === e.F10) {
            Ext.MessageBox.confirm("Support", "<br>Are you sure you want to create a support file for download?",
                function(btn) {
                    if (btn == 'yes') {
                        var body = Ext.getBody();

                        var frame = body.createChild({
                            tag: 'iframe',
                            cls: 'x-hidden',
                            src: '/console/support'
                        });
                    }
                });

            e.stopEvent();
        }
    }
});