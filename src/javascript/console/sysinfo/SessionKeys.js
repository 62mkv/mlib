// View
Ext.define("RP.Moca.Console.SessionKeys.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('sessionKeysGridPanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Connected Users',
            itemId: 'sessionView',
            layout: 'fit',
            items: new RP.Moca.Console.SessionKeys.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('sessionKeysGridPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

Ext.define("RP.Moca.Console.SessionKeys.ResultsReader", {
    extend: "Ext.data.reader.Json",

    root: 'data',
    _status: null,
    _message: "",

    /**
     * read
     * Override the read method of the JsonReader to grab some additional data
     * before passing the data back to the handler.
     * @param {Object} response
     */
    read: function(response) {
        var json = Ext.decode(response.responseText);
        if (json !== null) {
            this._status = parseInt(json.status, 10);
            this._message = json.message;
            
            this.checkOkStatus();
        }

        // Parse the data and return it.
        var data = this.callParent(arguments);
        
        var realData = data.records;
        
        for (var i = 0; i < realData.length; i++) {
            realData[i].data.ip = realData[i].data.environment.WEB_CLIENT_ADDR;
            delete realData[i].data.environment.WEB_CLIENT_ADDR;
        }
        
        return data;
    },

    /**
     * Checks if this has a status of 0 or null. If not, displays an error
     * message.
     */
    checkOkStatus: function() {
        if (this._status !== 0 && this._status !== null) {
            Ext.Msg.show({
                title: 'Console',
                msg: this._message,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },

    getStatus: function() { return this._status; },
    getMessage: function() { return this._message; }
});

// Grid
Ext.define("RP.Moca.Console.SessionKeys.Grid", {
    extend: "Ext.grid.Panel",
    id: 'sessionKeysGridPanel',
    
    initComponent: function() {
        var setTitle = function (store, records, options) {
            var me = this;
            var sessionView= me.up('#sessionView');
            if (sessionView.rendered) { 
                sessionView.getHeader().items.items[0].update('Connected Users (' + store.getCount() + ')'); 
            } else { 
                sessionView.on('afterrender', function() { 
                    sessionView.getHeader().items.items[0].update('Connected Users (' + store.getCount() +   ')'); 
                }); 
            }
        };
        
        // Handler function for downloading
        var revokeAuthentication = function() {
            var selected = this.selModel.selected.items[0];

            // Make sure a row was selected first.
            if (!selected || selected.length === 0) {
                Ext.Msg.alert('Console', 'Please select a key to revoke.');
                return;
            }
            
            var sessionId = selected.get("sessionId");
            
            // Make the call     
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'revokeSessionKey',
                    sessionId: sessionId
                },
                success: function (result) {
                    Ext.getCmp('sessionKeysGridPanel').refresh();
                },
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not send user revocation request to the server.'
                }
            });
        };

        var myColumns = [{
            header: 'User',
            dataIndex: 'userId',
            flex: 1,
            minWidth: 150
            }, {
            header: 'Start Date',
            dataIndex: 'createdDate',
            flex: 1,
            minWidth: 175,
            renderer: RP.Moca.util.Format.datetimeString
            }, {
            header: 'Last Request Date',
            dataIndex: 'lastAccess',
            flex: 1,
            minWidth: 175,
            renderer: RP.Moca.util.Format.datetimeString
            }, {
            header: 'IP Address',
            dataIndex: 'ip',
            flex: 1,
            minWidth: 175
            }, {
            header: 'Key',
            dataIndex: 'sessionId',
            flex: 1,
            minWidth: 150,
            hidden: true
            }, {
            header: 'Environment',
            dataIndex: 'environment',
            renderer: RP.Moca.util.Format.objectToString,
            flex: 2,
            minWidth: 150
		    }, {
            header: 'Console Role',
			renderer: function(value, meta, record) {
                            return '<div>' + RP.Moca.util.Format.consoleRoleString(record.getData().role) + '</div>';
            },
            flex: 1,
            minWidth: 150
        }];
        
        Ext.define('SessionKeys', {
            extend: 'Ext.data.Model',
            fields: ['sessionId', {name: 'createdDate',   type: 'date'}, 
            'userId', 'ip', 'lastAccess', 'environment','role']
        });
        
        var myReader = new RP.Moca.Console.SessionKeys.ResultsReader({});
        
        var myStore = new Ext.data.Store({
            autoLoad: true,
            model: 'SessionKeys',
            proxy: {
                type: 'ajax',
                url: '/console',
                extraParams: {
                    m: 'getSessionKeys'
                },
                reader: myReader
            },
            sorters: [{
                property: 'userId',
                direction: 'ASC'
            }],
            listeners: {
                'exception' : function () {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: 'A connection with the server could not be established.\n\n' + 
                             'Check to make sure the server has not been shutdown and try again.',
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                },
                'beforeload' :  function() {
                    Ext.getCmp('sessionKeysGridPanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('sessionKeysGridPanel').setLoading(false);
                }
            }
        });

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Revoke Authentication',
                scope: this,
                handler: revokeAuthentication,
                style: {
                    margin: '3px'
                }
            }]
        });
        var myTopToolbar = Ext.create('Ext.toolbar.Toolbar', { 
            xtype: 'toolbar',
            dock: 'top',
            height: 1
        });
        
        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.disable();
                }
            }
        });

        myStore.on('load', setTitle, this);

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            dockedItems: [myToolbar, myTopToolbar],
            store: myStore,
            columns: myColumns,
            viewConfig: {
                emptyText: 'No logged in sessions exist.',
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
