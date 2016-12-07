// View
Ext.define("RP.Moca.Console.ClusterAsyncExecutor.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Cluster Async Executor',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.ClusterAsyncExecutor.Panel", {
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('clusterAsyncExecutorPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    },

    onActivate: function () {
        RP.Moca.Console.ClusterAsyncExecutor.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('clusterAsyncExecutorPanel').refresh();
        }

        this.activateCount++;
    }
});

// Panel
Ext.define("RP.Moca.Console.ClusterAsyncExecutor.Panel", {
    extend: "Ext.panel.Panel",
    id: 'clusterAsyncExecutorPanel',
    
    initComponent: function () {

        // Handler function for setting the title of the second store
        var setTitle1 = function (store, records, options) {
            Ext.getCmp('clusterAsyncGrid1').setTitle('Current Runner Tasks (' + store.getCount() + ')');
        };

        // Handler function for setting the title of the first store
        var setTitle2 = function (store, records, options) {
            Ext.getCmp('clusterAsyncGrid2').setTitle('Current Requests (' + store.getCount() + ')');
        };

        var myStore1 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['task_thread', 'task_name'],
            proxy: {
                extraParams: {
                    m: 'getClusterAsyncExecutionInfo'
                }
            },
            sorters: [{
                property: 'task_thread',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('clusterAsyncGrid1').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('clusterAsyncGrid1').setLoading(false);
                }
            }
        });

        var myStore2 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['queued', 'noderunning'],
            proxy: {
                extraParams: {
                    m: 'getClusterAsyncQueueInfo'
                }
            },
            sorters: [{
                property: 'noderunning',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('clusterAsyncGrid2').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('clusterAsyncGrid2').setLoading(false);
                }
            }
        });
        
        var myResultHandler = function (result) {
            Ext.getCmp('clusterAsyncGrid1').store.load();
            Ext.getCmp('clusterAsyncGrid2').store.load();
        };
        
        // Handler function to start a runner
        var startRunner = function() {
            // Make the call     
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'handleClusterAsyncRunnerRequest',
                    add: 'true'
                },
                success: myResultHandler,
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not start Runner Thread.'
                }
            });
        };
        
        // Handler function to stop a runner
        var stopRunner = function() {
            // Make the call     
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'handleClusterAsyncRunnerRequest',
                    add: 'false'
                },
                success: myResultHandler,
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not stop Runner Thread.'
                }
            });
        };

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Remove Idle Thread',
                scope: this,
                handler: stopRunner,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Add Runner Thread',
                scope: this,
                handler: startRunner,
                style: {
                    margin: '3px'
                }
            }]
        });        

        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.disable();
                }
            }
        });

        myStore1.on('load', setTitle1, this);
        myStore2.on('load', setTitle2, this);

        Ext.apply(this, {
            dockedItems: myToolbar,
            layout: {
                type: "hbox",
                align: "stretch"
            },
            bodyStyle: {
                background: '#ffffff'
            },
            items: [
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Runner Tasks',
                    id: 'clusterAsyncGrid1',
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    autoScroll: true,
                    store: myStore1,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [{ 
                        header: 'Task Thread', 
                        dataIndex: 'task_thread',
                        flex: 1,
                        minWidth: 120, 
                        renderer: RP.Moca.util.HyperLink.sessions
                     }, {
                         header: 'Task Name', 
                         dataIndex: 'task_name',
                         flex: 2,
                         minWidth: 150
                    }
                    ]
                }), 
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Requests',
                    id: 'clusterAsyncGrid2',
                    autoScroll: true,
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    store: myStore2,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [{ 
                        header: 'Queued Task', 
                        dataIndex: 'queued', 
                        minWidth: 120,
                        flex: 1
                    }, { 
                        header: 'Node Running', 
                        dataIndex: 'noderunning', 
                        renderer: RP.Moca.util.HyperLink.node,
                        flex: 2,
                        minWidth: 150
                    }]
                })
            ]
        });

        this.callParent(arguments);
    },

    refresh: function() {
        this.getComponent('clusterAsyncGrid1').store.load();
        this.getComponent('clusterAsyncGrid2').store.load();
    }
});
