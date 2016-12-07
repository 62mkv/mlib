// View
Ext.define("RP.Moca.Console.SystemProperties.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('systemPropertiesGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'System Properties',
            layout: 'fit',
            items: [ new RP.Moca.Console.SystemProperties.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander',
                    rowBodyTpl: new Ext.XTemplate('<div class="rp-expanded-value">', '{value}', '</div>')
                },
                new RP.ui.RefreshablePlugin({
                    performRefresh: function() {
                        Ext.getCmp('systemPropertiesGrid').refresh();
                    }
                })],
                columns: [{
                    header: 'Name',
                    dataIndex: 'name',
                    minWidth: 150,
                    flex: 1
                },{
                    header: 'Value',
                    dataIndex: 'value',
                    minWith: 300,
                    flex: 3
                }],
                store: new RP.Moca.util.Store({
                    autoLoad: true,
                    proxy: {
                        type: 'ajax',
                        url: '/console',
                        extraParams: {
                            m: 'getSystemProperties'
                        },
                        reader: new RP.Moca.Console.Results.ResultsReader({})
                    },
                    fields: [ 'name', 'value' ],
                    sorters: [{
                        property: 'name',
                        direction: 'ASC'
                    }],
                    listeners: {
                        'beforeload' :  function() {
                            Ext.getCmp('systemPropertiesGrid').setLoading(true);
                        },
                        'load' :  function() {
                            Ext.getCmp('systemPropertiesGrid').setLoading(false);
                        }
                    }
                })
            })]
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.SystemProperties.Grid", {
    extend: "Ext.grid.Panel",
    id: 'systemPropertiesGrid',
    
    initComponent: function () {

        // Call our superclass.
        Ext.apply(this, {
            autoScroll: true,
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
