// View
Ext.define("RP.Moca.Console.Registry.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    initComponent: function () {
        Ext.apply(this, {
            title: 'Registry',
            layout: 'fit',
            items: new RP.Moca.Console.Registry.Panel({
                padding: '0px 5px 7px 0px'
            })
        });

        this.callParent(arguments);
    }
});

// Panel
Ext.define("RP.Moca.Console.Registry.Panel", {
    extend: "Ext.panel.Panel",
 
    initComponent: function () {

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: {
                m: 'getRegistry'
            },
            scope: this,
            success: function (result) {
                var content = Ext.decode(result.responseText).data;
                var format = '<pre>{0}</pre>';
                this.update(Ext.String.format(format, content));
            },
            failureAlert: {
                title: 'Console',
                msg: 'Could not get registry information from the server.'
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            bodyStyle: {
                background: '#ffffff'
            }
        });

        this.callParent(arguments);
    }
});
