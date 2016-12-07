Ext.define("RP.Moca.Console.Clustering.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Clustering',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.Clustering.Grid", {
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    header: '+',
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value" style="table-layout: fixed; width: 100%;"><tr><td style="padding-bottom:5px;padding-right:5px;width:45px;" valign="top"><b>Name:</b></td><td style="padding-bottom:5px;" valign="top">{name}</td></tr><tr><td style="padding-right:5px; "  valign="top"><b>Roles:</b></td><td style= "word-wrap: break-word;">{value}</td></tr></table>', '<br>')
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('clusteringGrid').refresh();
                        }
                    })
                ],
                columns:  [
                    { header: 'Name', dataIndex: 'name', width: 300},
                    { header: 'Value', dataIndex: 'value'}
                ]
            })
        });

        this.callParent(arguments);
    },

    onActivate: function () {
        RP.Moca.Console.Clustering.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('clusteringGrid').refresh();
        }

        this.activateCount++;
    }
});

// Grid
Ext.define("RP.Moca.Console.Clustering.Grid", {
    extend: "Ext.grid.Panel",
    id: 'clusteringGrid',

    constructor: function (config) {

        var myStore = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getClusterRoles'
                }
            },
           fields: [
                { name: 'name' },
                { name: 'value' }
            ],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('clusteringGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('clusteringGrid').setLoading(false);
                }
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            store: myStore,
            autoExpandColumn: 'roleColumn',
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