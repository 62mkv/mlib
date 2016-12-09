Ext.define("RP.Moca.util.Store", {
    extend: "Ext.data.Store",
    
    constructor: function(config) {
        
        // Apply the config to ourselves
        Ext.apply(this, config);
        
        // Apply some universal things
        Ext.apply(this.proxy, {
            type: 'ajax',
            url: '/console',
            reader: new RP.Moca.Console.Results.ResultsReader({})       
        });

        this.callParent(arguments);
    },

    listeners: {
        exception: function () {
            Ext.Msg.show({
                title: 'Console',
                msg: 'A connection with the server could not be established.\n\n' + 
                     'Check to make sure the server has not been shutdown and try again.',
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    }
});
