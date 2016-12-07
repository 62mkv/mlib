/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial Software License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
// feature idea to enable Ajax loading and then the content
// cache would actually make sense. Should we dictate that they use
// data or support raw html as well?

/**
 * @class Ext.ux.RowExpander
 * @extends Ext.AbstractPlugin
 * Plugin (ptype = 'rowexpander') that adds the ability to have a Column in a grid which enables
 * a second row body which expands/contracts.  The expand/contract behavior is configurable to react
 * on clicking of the column, double click of the row, and/or hitting enter while a row is selected.
 *
 * @ptype rowexpander
 */
Ext.define('Ext.ux.RowExpander', {
    extend: 'Ext.AbstractPlugin',

    requires: [
        'Ext.grid.feature.RowBody',
        'Ext.grid.feature.RowWrap'
    ],

    alias: 'plugin.rowexpander',

    /**
     * @cfg {String/Ext.Template} rowBodyTpl
     * A string or template to use for the row body 
     */
    rowBodyTpl: null,

    /**
     * @cfg {Boolean} expandOnEnter
     * <tt>true</tt> to toggle selected row(s) between expanded/collapsed when the enter
     * key is pressed (defaults to <tt>true</tt>).
     */
    expandOnEnter: true,

    /**
     * @cfg {Boolean} expandOnDblClick
     * <tt>true</tt> to toggle a row between expanded/collapsed when double clicked
     * (defaults to <tt>true</tt>).
     */
    expandOnDblClick: true,

    /**
     * @cfg {Boolean} selectRowOnExpand
     * <tt>true</tt> to select a row when clicking on the expander icon
     * (defaults to <tt>false</tt>).
     */
    selectRowOnExpand: false,

    /**
     * @cfg {String} rowBodyTrSelector
     * The seletor string to use to grab the row body elements. (defaults to '.x-grid-rowbody-tr') 
     */
    rowBodyTrSelector: '.x-grid-rowbody-tr',

    /**
     * @cfg {String} rowBodyHiddenCls
     * The class to add to the row body when it should be hidden. (defaults to 'x-grid-row-body-hidden')
     */
    rowBodyHiddenCls: 'x-grid-row-body-hidden',

    /**
     * @cfg {String} rowCollapsedCls
     * The class to add to a collapsed row element. (defaults to 'x-grid-row-collapsed') 
     */
    rowCollapsedCls: 'x-grid-row-collapsed',



    /**
     * The renderer for the expand/collapse column.
     *
     * @param {Object} value The value of the current cell
     * @param {Object} metaData A collection of metadata about the current cell; can be used or modified
     * by the renderer. Recognized properties are: tdCls, tdAttr, and style
     * @param {Ext.data.Model} record The record for the current row
     * @param {Number} rowIdx The index of the current row
     * @param {Number} colIdx The Index of the current column
     */
    renderer: function(value, metadata, record, rowIdx, colIdx) {
        if (colIdx === 0) {
            metadata.tdCls = 'x-grid-td-expander';
        }
        return '<div class="x-grid-row-expander">&#160;</div>';
    },

    /**
     * @event expandbody
     * <b<Fired through the grid's View</b>
     * @param {HtmlElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HtmlElement} expandRow The &lt;tr> element containing the expanded data.
     */
    /**
     * @event collapsebody
     * <b<Fired through the grid's View.</b>
     * @param {HtmlElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HtmlElement} expandRow The &lt;tr> element containing the expanded data.
     */

    constructor: function() {
        this.callParent(arguments);
        var grid = this.getCmp();
        this.recordsExpanded = {};
        // <debug>
        if (!this.rowBodyTpl) {
            Ext.Error.raise("The 'rowBodyTpl' config is required and is not defined.");
        }
        // </debug>
        
        var rowBodyTpl = this.rowBodyTpl instanceof Ext.XTemplate ? this.rowBodyTpl : 
            Ext.create('Ext.XTemplate', this.rowBodyTpl),
            features = [{
                ftype: 'rowbody',
                columnId: this.getHeaderId(),
                recordsExpanded: this.recordsExpanded,
                rowBodyHiddenCls: this.rowBodyHiddenCls,
                rowCollapsedCls: this.rowCollapsedCls,
                getAdditionalData: this.getRowBodyFeatureData,
                getRowBodyContents: function(data) {
                    return rowBodyTpl.applyTemplate(data);
                }
            },{
                ftype: 'rowwrap'
            }];

        if (grid.features) {
            grid.features = features.concat(grid.features);
        } else {
            grid.features = features;
        }

        // NOTE: features have to be added before init (before Table.initComponent)
    },

    /**
     * Initialize the plugin
     * @param {Ext.grid.Panel} grid The grid this plugin was added to. 
     */
    init: function(grid) {
        this.callParent(arguments);

        // Columns have to be added in init (after columns has been used to create the
        // headerCt). Otherwise, shared column configs get corrupted, e.g., if put in the
        // prototype.
        grid.headerCt.insert(0, this.getHeaderConfig());
        grid.on('render', this.bindView, this, {single: true});
    },

    /**
     * Get the identifier for the expand/collapse column. This method will generate a unique
     * id string if one does not exist for the header yet.
     * @return {String} The identifier
     */
    getHeaderId: function() {
        if (!this.headerId) {
            this.headerId = Ext.id();
        }
        return this.headerId;
    },

    /**
     * @inheritdoc Ext.grid.feature.RowBody#getAdditionalData
     */
    getRowBodyFeatureData: function(data, idx, record, orig) {
        var o = Ext.grid.feature.RowBody.prototype.getAdditionalData.apply(this, arguments),
            id = this.columnId;
        o.rowBodyColspan = o.rowBodyColspan - 1;
        o.rowBody = this.getRowBodyContents(data);
        o.rowCls = this.recordsExpanded[record.internalId] ? '' : this.rowCollapsedCls;
        o.rowBodyCls = this.recordsExpanded[record.internalId] ? '' : this.rowBodyHiddenCls;
        o[id + '-tdAttr'] = ' valign="top" rowspan="2" ';
        if (orig[id+'-tdAttr']) {
            o[id+'-tdAttr'] += orig[id+'-tdAttr'];
        }
        return o;
    },

    /**
     * Attach keyboard and click event listeners to the view. If the view is not yet rendered when this
     * method is called, it will listen for the render event and attach the listeners at that point.
     */
    bindView: function() {
        var view = this.getCmp().getView(),
            viewEl;

        if (!view.rendered) {
            view.on('render', this.bindView, this, {single: true});
        } else {
            viewEl = view.getEl();
            if (this.expandOnEnter) {
                this.keyNav = Ext.create('Ext.KeyNav', viewEl, {
                    'enter' : this.onEnter,
                    scope: this
                });
            }
            if (this.expandOnDblClick) {
                view.on('itemdblclick', this.onDblClick, this);
            }
            this.view = view;
        }
    },

    /**
     * Handle the ENTER key event on the view. If {@link #expandOnEnter} is true, the row
     * will be toggled between the expanded and collapsed states when ENTER is pressed. Otherwise
     * ENTER will be ignored.
     * @param {Ext.EventObject} e The event object  
     */
    onEnter: function(e) {
        var view = this.view,
            ds   = view.store,
            sm   = view.getSelectionModel(),
            sels = sm.getSelection(),
            ln   = sels.length,
            i = 0,
            rowIdx;

        for (; i < ln; i++) {
            rowIdx = ds.indexOf(sels[i]);
            this.toggleRow(rowIdx);
        }
    },

    /**
     * Toggle the specified row between being expanded or collapsed.
     *
     * @param {Number} rowIdx The index of the row to expand or collapse
     */
    toggleRow: function(rowIdx) {
        var rowNode = this.getCmp().view.getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector),
            record = this.getCmp().view.getRecord(rowNode),
            grid = this.getCmp();

        if (row.hasCls(this.rowCollapsedCls)) {
            row.removeCls(this.rowCollapsedCls);
            nextBd.removeCls(this.rowBodyHiddenCls);
            this.recordsExpanded[record.internalId] = true;
            this.view.fireEvent('expandbody', rowNode, record, nextBd.dom);
        } else {
            row.addCls(this.rowCollapsedCls);
            nextBd.addCls(this.rowBodyHiddenCls);
            this.recordsExpanded[record.internalId] = false;
            this.view.fireEvent('collapsebody', rowNode, record, nextBd.dom);
        }

        this.view.up('gridpanel').view.refresh();
    },

    /**
     * Handle the double click event on a row. If {@link #expandOnDblClick} is true, the row will be
     * expanded or collapsed (depending on its previous state). See also {@link Ext.view.View#itemdblclick}
     *
     * @param {Ext.grid.View} view The view the click event occured on
     * @param {Ext.data.Model} record The record that belongs to the item that was clicked on
     * @param {HTMLElement} item The clicked item's element
     * @param {Number} rowIdx The item's index
     * @param {Ext.EventObject} e The raw event object
     */
    onDblClick: function(view, record, item, rowIdx, e) {
        this.toggleRow(rowIdx);
    },

    /**
     * @return {Object} the configuration for the expand/collapse column
     */
    getHeaderConfig: function() {
        var me                = this,
            toggleRow         = Ext.Function.bind(me.toggleRow, me),
            selectRowOnExpand = me.selectRowOnExpand;

        return {
            id: this.getHeaderId(),
            width: 24,
            sortable: false,
            resizable: true,
            draggable: false,
            hideable: false,
            menuDisabled: true,
            cls: Ext.baseCSSPrefix + 'grid-header-special',
            renderer: this.renderer,
            // changed this from mousedown to click for Jesse / testing
            processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
                if (type == "click" && e.getTarget('.x-grid-row-expander')) {
                    var row = e.getTarget('.x-grid-row');
                    toggleRow(row);
                    return selectRowOnExpand;
                }
            }
        };
    }
});