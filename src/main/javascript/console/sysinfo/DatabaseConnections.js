// View
Ext.define("RP.Moca.Console.DatabaseConnections.View", {
    extend: "RP.util.taskflow.BaseTaskForm",
    id: 'databaseConnectionView',
    
    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('databaseConnectionsGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Database Connections',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.DatabaseConnections.Grid", {
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Connection:</b></td><td>{id}</td></tr><tr><td><b>Thread ID:</b></td><td>{thread_id}</td></tr><tr><td><b>Last SQL Time:</b></td><td>{last_sql_dt}</td></tr><tr><td><b>Last SQL:</b></td><td>{last_sql}</td></tr><tr><td><b>Executions:</b></td><td>{executions}</td></tr><tr><td><b>Last Command Path:</b></td><td>{command_path}</td></tr></table>', {
                        blankNulls: true
                    })
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('databaseConnectionsGrid').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Connection',
                    dataIndex: 'id',
                    minWidth: 150,
                    flex: 1
                }, {
                    header: 'Thread ID',
                    dataIndex: 'thread_id',
                    renderer: RP.Moca.util.HyperLink.sessions,
                    minWidth: 100,
                    flex: 1
                }, {
                    header: 'Last SQL Time',
                    dataIndex: 'last_sql_dt',
                    renderer: RP.Moca.util.Format.datetimeString,
                    minWidth: 175,
                    flex: 1
                }, {
                    header: 'Last SQL',
                    dataIndex: 'last_sql',
                    minWidth: 300,
                    flex: 2
                }, {
                    header: 'Executions',
                    dataIndex: 'executions',
                    minWidth: 80,
                    flex: 1,
                    align: 'right'
                }]
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.DatabaseConnections.Grid", {
    extend: "Ext.grid.Panel",
    id: 'databaseConnectionsGrid',
    
    initComponent: function () {
        var myStore = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getDatabaseConnections'
                }
            },
            fields: ['id', 'thread_id',/*'threat_id',*/ 'last_sql', 'executions', 'command_path', {
                name: 'last_sql_dt',
                type: 'date'
            }],
            sorters: [{
                property: 'id',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('databaseConnectionsGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('databaseConnectionsGrid').setLoading(false);
                }
            }
        });

        Ext.apply(this, {
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }],
            autoScroll: true,
            store: myStore,
            viewConfig: {
                emptyText: 'No database connections exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});