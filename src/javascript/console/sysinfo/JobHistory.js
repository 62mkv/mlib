// View
Ext.define("RP.Moca.Console.JobHistory.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('jobHistoryGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Job Execution History',
            layout: 'fit',
            padding: '0px 5px 7px 0px',
            items: [
                this.createGrid()
            ]
        });

        this.callParent(arguments);
    },
    
    
    createGrid: function() {
        return new RP.Moca.Console.JobHistory.Grid({
            plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr>' +
                    '<td><b>Job ID:</b></td><td>{job_id}</td>' +
                    '</tr><tr>' +
                    '<td><b>Node URL:</b></td><td>{node_url}</td>' +
                    '</tr><tr>' +
                    '<td><b>Status:</b></td><td>{status}</td>' +
                    '</tr><tr>' +
                    '<td><b>Message:</b></td><td>{message}</td>' +
                    '</tr><tr>' +
                    '<td><b>Start Date:</b></td><td>{start_dte}</td>' +
                    '</tr><tr>' +
                    '<td><b>End Date:</b></td><td>{end_dte}</td>' +
                    '</tr></table>',
                    {
                        blankNulls: true
                    })
                },
                new RP.ui.RefreshablePlugin({
                    performRefresh: function() {
                        Ext.getCmp('jobHistoryGrid').refresh();
                    }
                })
            ],
            columns: [{
                header: 'Job ID',
                dataIndex: 'job_id',
                flex: 2
            },{
                header: 'Node URL',
                dataIndex: 'node_url',
                flex: 3
            },{
                header: 'Status',
                dataIndex: 'status',
                flex: 1
            },{
                header: 'Message',
                dataIndex: 'message',
                flex: 2
            },{
                header: 'Start Date',
                dataIndex: 'start_dte',
                renderer: RP.Moca.util.Format.datetimeString,
                flex: 3
            },{
                header: 'End Date',
                dataIndex: 'end_dte',
                renderer: RP.Moca.util.Format.datetimeString,
                flex: 3
            }],
            bbar: this.createToolbar(),
            store: this.getStore()
        });
    },
    
    createToolbar: function() {
        if (!this.jobPageToolbar) {
            this.jobPageToolbar =  Ext.create('Ext.PagingToolbar', {
                store: this.getStore(),
                displayInfo: true,
                inputItemWidth: 70,
                displayMsg: 'Displaying {0} - {1} of {2}',
                emptyMsg: 'No data.'
            });
            this.jobPageToolbar.down('#refresh').hide();
            this.jobPageToolbar.on({
                change: function( pagingToolBar, changeEvent ) {
                    var t = pagingToolBar.down('#inputItem'),
                        m = new Ext.util.TextMetrics(t.getEl()),
                        s = m.getSize(t.getValue());
                    t.setWidth(s.width + 22);
                }
            }); 
        }
        return this.jobPageToolbar;
    },
    
    getStore: function() {
        if (!this.store) {
            this.store = new RP.Moca.util.Store({
                autoLoad: true,
                pageSize: 25,
                remoteSort: true,
                proxy: {
                    type: 'ajax',
                    url: '/console',
                    extraParams: {
                        m: 'getJobHistory'
                    },
                    reader: new RP.Moca.Console.Results.ResultsReader()
                },
                fields: [ 'job_id', 'node_url', { name: 'start_dte', type: 'date' }, 'status', 'message', { name: 'end_dte', type: 'date' } ],
                sorters: [{
                    property: 'start_dte',
                    direction: 'DESC'
                }],
                listeners: {
                    'beforeload' :  function() {
                        Ext.getCmp('jobHistoryGrid').setLoading(true);
                    },
                    'load' :  function() {
                        Ext.getCmp('jobHistoryGrid').setLoading(false);
                    }
                }
            });
        }
        return this.store;
    }
});

// Grid
Ext.define("RP.Moca.Console.JobHistory.Grid", {
    extend: "Ext.grid.Panel",
    id: 'jobHistoryGrid',
    
    initComponent: function () {

        // Call our superclass.
        Ext.apply(this, {
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }]
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});
