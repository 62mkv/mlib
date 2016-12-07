// View
Ext.define("RP.Moca.Console.NativeProcesses.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('nativeGridPanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Native Processes',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.NativeProcesses.Grid", {
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value" style="table-layout: fixed"><tr><td><b>Native Process:</b></td><td>{moca_prc}</td></tr><tr><td><b>Thread ID:</b></td><td>{threadId}</td></tr><tr><td><b>Created Time:</b></td><td>{created_dt}</td></tr><tr><td><b>Last Request Time:</b></td><td>{last_call_dt}</td></tr><tr><td><b>Last Request:</b></td><td>{last_call}</td></tr><tr><td><b>Requests:</b></td><td>{requests}</td></tr><tr><td><b>Last Command Path:</b></td><td>{command_path}</td></tr></table>', {
                        blankNulls: true
                    })
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('nativeGridPanel').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Native Process',
                    dataIndex: 'moca_prc',
                    flex: 1,
                    minWidth: 150
                }, {
                    header: 'Thread ID',
                    dataIndex: 'thread_id',
                    renderer: RP.Moca.util.HyperLink.sessions,
                    flex: 1,
                    minWidth: 100
                }, {
                    header: 'Created Time',
                    dataIndex: 'created_dt',
                    renderer: RP.Moca.util.Format.datetimeString,
                    flex: 1,
                    minWidth: 175
                }, {
                    header: 'Last Request Time',
                    dataIndex: 'last_call_dt',
                    renderer: RP.Moca.util.Format.datetimeString,
                    flex: 1,
                    minWidth: 175
                }, {
                    header: 'Last Request',
                    dataIndex: 'last_call',
                    flex: 3,
                    minWidth: 200
                }, {
                    header: 'Requests',
                    dataIndex: 'requests',
                    align: 'right',
                    flex: 1,
                    minWidth: 100
                }, {
                    header: 'Temporary',
                    dataIndex: 'temp',
                    xtype: 'checkcolumn',
                    hidden: true,
                    flex: 1,
                    minWidth: 100
                }]
            }) 
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.NativeProcesses.Grid", {
    extend: "Ext.grid.Panel",
    id: 'nativeGridPanel',
    
    initComponent: function() {

        // Handler function for Stop Native Process button.
        var stopProcessHandler = function() {
            var selected = this.selModel.selected.items[0];

            // Make sure a row was selected first.
            if (!selected) {
                Ext.Msg.alert('Console', 'Please select a native process to stop and try again.');
                return;
            }
            
            var thread = selected.get("thread_id");
            var moca_prc = selected.get("moca_prc");
            
            var myParams = {
                moca_prc: moca_prc,
                m: 'stopNativeProcess'
            };
            
            var myResultsHandler = function (result) {
                var response = Ext.decode(result.responseText);
                var message = response.message;

                if (response.status !== 0) {
                    Ext.Msg.alert('Console', 'An error occurred attempting to stop the selected native process.\n\n' + message);
                }

                // Refresh the grid panel after we've stopped the process.
                Ext.getCmp('nativeGridPanel').refresh();
            };
            
            var myFailureAlert = {
                title: 'Console',
                msg: 'Could not stop the selected native process.'
            };
            
            // Make sure the task isn't already running.
            if (thread) {
                // Show confirmation dialog
                Ext.Msg.show({
                    title: 'Stop Native Process?',
                    msg: 'You are closing a native process that is running.  This can cause instability.  Are you sure you want to do this?',
                    buttons: Ext.Msg.YESNO,
                    fn: function(result) {
                        if (result == 'no') {
                            return;
                        }
                        
                        Ext.getCmp('nativeGridPanel').realStopProcess(myParams, 
                            myResultsHandler, myFailureAlert);
                    }
                });
            }
            else {
                this.realStopProcess(myParams, myResultsHandler, myFailureAlert);
            }
        };

        var myStore = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getNativeProcesses'
                }
            },
            fields: [ 'moca_prc', 'requests', 'last_call', 'thread_id', 
                      'command_path', 'temp', {
                name: 'created_dt',
                type: 'date'
            }, {
                name: 'last_call_dt',
                type: 'date'
            }],
            sorters: [{
                property: 'moca_prc',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('nativeGridPanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('nativeGridPanel').setLoading(false);
                }
            }
        });

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Stop Native Process',
                scope: this,
                handler: stopProcessHandler,
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

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            dockedItems: [myToolbar, myTopToolbar],
            store: myStore,
            viewConfig: {
                emptyText: 'No native processes currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        // Call our superclass
        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    },
    realStopProcess: function(myParams, myResultsHandler, myFailureAlert) {
        // Make an Ajax call to the server.
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultsHandler,
            failureAlert: myFailureAlert
        });
    
    }
});