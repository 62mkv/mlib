// View
Ext.define("RP.Moca.Console.EnvironmentVariables.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('environmentVariablesGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Environment Variables',
            layout: 'fit',
            items: new RP.Moca.Console.EnvironmentVariables.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    rowBodyTpl: new Ext.XTemplate('<div class="rp-expanded-value">', '{value}', '</div>')
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('environmentVariablesGrid').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Name',
                    dataIndex: 'name',
                    flex: 1,
                    minWidth: 150
                },{
                    header: 'Value',
                    dataIndex: 'value',
                    flex: 3,
                    minWidth: 300
                }]
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.EnvironmentVariables.Grid", {
    extend: "Ext.grid.Panel",
    id: 'environmentVariablesGrid',
    
    initComponent: function () {

        var myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getEnvironmentVariables'
                }
            },
            fields: [ 'name', 'value' ],
            sorters: [{
                property: 'name',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('environmentVariablesGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('environmentVariablesGrid').setLoading(false);
                }
            }
        });

        // Call our superclass.
        Ext.apply(this, {
            autoScroll: true,
            store: myStore,
            viewConfig: {
                preserveScrollOnRefresh: true
            },
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
