// View
Ext.define("RP.Moca.Console.AsyncExecutor.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Asynchronous Executor',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.AsyncExecutor.Panel", {
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('asyncExecutorPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    },

    onActivate: function () {
        RP.Moca.Console.AsyncExecutor.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('asyncExecutorPanel').refresh();
        }

        this.activateCount++;
    }
});

// Panel
Ext.define("RP.Moca.Console.AsyncExecutor.Panel", {
    extend: "Ext.panel.Panel",
    id: 'asyncExecutorPanel',

    initComponent: function () {

        // Handler function for setting the title of the second store
        var setTitle1 = function (store, records, options) {
            Ext.getCmp('asyncGrid1').setTitle('Current Asynchronous Executions (' + store.getCount() + ')');
        };

        // Handler function for setting the title of the first store
        var setTitle2 = function (store, records, options) {
            Ext.getCmp('asyncGrid2').setTitle('Current Queued Executions (' + store.getCount() + ')');
        };

        var myStore1 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['task_thread', 'task_name'],
            proxy: {
                extraParams: {
                    m: 'getAsyncExecutionInfo'
                }
            },
            sorters: [{
                property: 'task_thread',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('asyncGrid1').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('asyncGrid1').setLoading(false);
                }
            }
        });

        var myStore2 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['queued', 'order'],
            proxy: {
                extraParams: {
                    m: 'getAsyncQueueInfo'
                }
            },
            sorters: [{
                property: 'order',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('asyncGrid2').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('asyncGrid2').setLoading(false);
                }
            }
        });

        myStore1.on('load', setTitle1, this);
        myStore2.on('load', setTitle2, this);

        Ext.apply(this, {
            layout: {
                type: "hbox",
                align: "stretch"
            },
            bodyStyle: {
                background: '#ffffff'
            },
            items: [
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Asynchronous Executions',
                    id: 'asyncGrid1',
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    autoScroll: true,
                    store: myStore1,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [
                        { header: 'Task Thread', dataIndex: 'task_thread',
                          flex: 1, minWidth: 150, renderer: RP.Moca.util.HyperLink.sessions },
                        { header: 'Task Name', dataIndex: 'task_name', flex: 2, minWidth: 150 }
                    ]
                }), 
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Queued Executions',
                    id: 'asyncGrid2',
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    autoScroll: true,
                    store: myStore2,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [
                        { header: 'Sequence', dataIndex: 'order', flex: 1, minWidth: 120 },
                        { header: 'Queued Task', dataIndex: 'queued', flex: 2, minWidth: 150 }
                    ]
                })
            ]
        });

        this.callParent(arguments);
    },

    refresh: function() {
        this.getComponent('asyncGrid1').store.load();
        this.getComponent('asyncGrid2').store.load();
    }
});