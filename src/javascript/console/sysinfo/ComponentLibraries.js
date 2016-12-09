// View
Ext.define("RP.Moca.Console.ComponentLibraries.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('componentLibrariesGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            title: 'Component Libraries',
            layout: 'fit',
            items: new RP.Moca.Console.ComponentLibraries.Grid({
                padding: '0px 5px 7px 0px'
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.ComponentLibraries.Grid", {
    extend: "RP.ui.GridPanel",
    id: 'componentLibrariesGrid',
    
    initComponent: function () {
        var myColumns = [{
            header: 'Component Library',
            dataIndex: 'category',
            flex: 2,
            minWidth: 155
            }, {
            header: 'Version',
            dataIndex: 'version',
            flex: 2,
            minWidth: 125
            }, {
            header: 'Java Package',
            dataIndex: 'package_name',
            flex: 2,
            minWidth: 300
            }, {
            header: 'C Library',
            dataIndex: 'library_name',
            flex: 1,
            minWidth: 100
            }, {
            header: 'Precedence',
            dataIndex: 'sort_seq',
            align: 'right',
            flex: 1,
            minWidth: 110
            }, {
            header: 'Product',
            dataIndex: 'product',
            flex: 1,
            minWidth: 75
        }];

        //Create and load store
        var myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getComponentLibraries'
                }
            },
            fields: [
                'category',
                {name: 'progid', mapping: 'progid',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'progid', mapping: 'progid',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'library_type', mapping: 'library_type',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'library_name', mapping: 'library_name',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'package_name', mapping: 'package_name',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'namespace', mapping: 'namespace',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'sort_seq', mapping: 'sort_seq',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'version', mapping: 'version',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'product', mapping: 'product',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                }
            ],
            sorters: [{
                property: 'category',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('componentLibrariesGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('componentLibrariesGrid').setLoading(false);
                }
            }
        });

        // Apply config information
        Ext.apply(this, {
            autoScroll: true, 
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }],
            store: myStore,
            columns: myColumns,
            viewConfig: {
                emptyText: 'No component libraries currently exist. This is likely a configuration problem.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        // Call super
        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});
