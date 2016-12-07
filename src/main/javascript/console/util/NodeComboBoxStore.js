Ext.ns('RP.util');

clusterNodeId = undefined;

// Store
Ext.define("RP.util.NodeComboBoxStore", {
    extend: "RP.Moca.util.Store",

    constructor: function (config) {

        // Apply the given configuration to ourselves.
        Ext.apply(this, config);

        // Apply shtuff
        Ext.apply(this, {
            model: "RP.util.NodeComboBoxStore.Model",
            storeId: "clusterComboBox",
            proxy: {
                extraParams: {
                    m: 'getClusterHosts'
                },
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({
                    model: "RP.util.NodeComboBoxStore.Model"
                })
            }
        });

        this.callParent(arguments);
    },

    load: function (config) {
        RP.util.NodeComboBoxStore.superclass.load.call(this, config);
    }
});

Ext.define("RP.util.NodeComboBoxStore.Model", {
    extend: "Ext.data.Model",
    fields: [
        { name: 'node' }, 
        { name: 'url' }, 
        { name: 'current' }
    ]
});