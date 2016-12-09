// View
Ext.define("RP.Moca.Console.Sessions.SessionsViewer", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('mocaSessionViewerContainer').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {

        Ext.apply(this, {
            allowRefresh: true,
            title: 'Sessions',
            layout: 'fit',
            border: false,
            items: Ext.create('RP.Moca.Console.Sessions.SessionViewerContainer', {
                padding: '0px 5px 7px 0px',
                layout: 'border',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('mocaSessionViewerContainer').refresh();
                        }
                    })
                ]
            })
        });

        // Call our superclass.
        this.callParent(arguments);
    }
});

Ext.define('RP.Moca.Console.Sessions.SessionViewerContainer', {
    extend: "Ext.panel.Panel",
    id: 'mocaSessionViewerContainer',
    
    initComponent: function () {
    
        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Interrupt Server Thread',
                scope: this,
                handler: this.interruptSession,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Enable/Disable Tracing',
                scope: this,
                handler: this.tracing,
                style: {
                    margin: '3px'
                }
            }]
        });

        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.hide();
                }
            }
        });
    
        Ext.apply(this, {
            dockedItems: myToolbar,
            layout: {
                type: "hbox",
                align: "stretch"
            },
            bodyStyle: {
                background: '#ffffff'
            },
            items: [
                Ext.create("RP.Moca.Console.Sessions.SessionList", {
                    itemId: 'sessionListGrid',
                    //split: true,
                    region: 'west',
                    flex: 7
                }),
                Ext.create("RP.Moca.Console.Sessions.SessionDetails", {
                    region: 'east',
                    flex: 5
                })
            ]
        });
        this.callParent(arguments);
    },
    
    // Handler function to refresh
    refresh: function() {
        var me = Ext.getCmp('sessionList');
        me.store.load();
        me.fireEvent('afterrender', this);
        Ext.getCmp('sessionDetails').clear();
    },

    makeDataRequest: function (config) {
        var defaultConfig = {
            url: '/console',
            scope: this,
            autoLoad: true
        };

        config = Ext.applyIf(defaultConfig, config);

        RP.Moca.util.Ajax.request(config);
    },
    
    // Handler function to interrupt a selected session
    interruptSession: function() {
        var selected = Ext.getCmp('sessionList').selModel.selected.items[0];
        
        // Make sure a row was selected first.
        if (!selected || selected.get("session")) {
            RP.Moca.util.Msg.alert('Console', 'Please select an active session to interrupt and try again.');
            return;
        }
        var sessionName = selected.get("name");
        var threadId = selected.get("threadId");
        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not interrupt server thread.'
        };

        this.makeDataRequest({
            params: {
                name: sessionName,
                threadId: threadId,
                m: 'interruptSession'
            },
            success: function(response, options) {
                Ext.define('interrupt', {
                    extend: 'Ext.data.Model',
                    fields: ['status', 'message', 'data'],
                
                    belongsTo: 'Post'
                });
                var reader = new RP.Moca.Console.Results.ResultsReader({ 
                    model : "interrupt" 
                });

                var records = reader.read(response);

                if (reader.getStatus() === 0) {
                    var me = Ext.getCmp('sessionList');
                    me.store.load();
                    me.fireEvent('afterrender', this);
                }
            },
            failureAlert: myFailureAlert
        });
    },

    // Handler function for enabling tracing
    tracing: function() {
        var selected = Ext.getCmp('sessionList').selModel.selected.items[0];
        
        // Make sure a row was selected first.
        if (!selected) {
            RP.Moca.util.Msg.alert('Console', 'Please select a session to configure tracing on and try again.');
            return;
        }
        
        var sessionName = selected.get("name"),
            traceName = selected.get("traceName"),
            w = new RP.Moca.util.TracingBox(sessionName, traceName);
            
        w.show();
    }
});

// Session List
Ext.define("RP.Moca.Console.Sessions.SessionList", {
    extend: "Ext.grid.Panel",

    // Delegate refresh to parent container
    refresh: function() {
        Ext.getCmp('mocaSessionViewerContainer').refresh();
    },
    
    initComponent: function() {
        // Handler function to display things in the details
        var display = function(sm, records) {
            if (records.length > 0) {
                Ext.getCmp("sessionDetails").display(records[0]);
            }
        };

        var myColumns = [{
            header: 'Session',
            dataIndex: 'name',
            minWidth: 150,
            flex: 2
        }, {
            header: 'Thread ID',
            dataIndex: 'threadId',
            id: 'threadId',
            align: 'right',
            minWidth: 100,
            flex: 1
        }, {
            header: 'Type / Status',
            renderer: function(value, meta, record) {
                return '<div>' + RP.Moca.util.Format.sessionTypeString(record.getData().sessionType) + '</div> <div>' + RP.Moca.util.Format.sessionStatusString(record.getData().status) + '</div>';
            },
            minWidth: 125,
            flex: 2
        }, {
            header: 'Type',
            dataIndex: 'sessionType',
            hidden: true,
            minWidth: 125,
            flex: 2
        }, {
            header: 'Status',
            dataIndex: 'status',
            hidden: true,
            minWidth: 150,
            flex: 2
        }, {
            header: 'Started / Last Command Time',
            minWidth: 220,
            flex: 2,
            renderer: function(value, meta, record) {
                return '<div>' + RP.Moca.util.Format.datetimeString(record.getData().startedTime) + '</div> <div>' + RP.Moca.util.Format.datetimeString(record.getData().lastCommandTime) + '</div>';
            }
        }, {
            header: 'Started Command Time',
            dataIndex: 'startedTime',
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Last Command Time',
            dataIndex: 'lastCommandTime',
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Environment',
            dataIndex: 'environment',
            renderer: RP.Moca.util.Format.objectToString,
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Last Script Time',
            dataIndex: 'lastScriptTime',
            renderer: RP.Moca.util.Format.datetimeString,
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Last SQL Time',
            dataIndex: 'lastSQLTime',
            renderer: RP.Moca.util.Format.datetimeString,
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Connected IP Address',
            dataIndex: 'connectedIpAddress',
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            xtype: 'checkcolumn',
            header: "Tracing",
            dataIndex: "traceName",
            minWidth: 85,
            flex: 1
        }, {
            header: 'Is Idle',
            dataIndex: 'session',
            minWidth: 85,
            flex: 1,
            hidden: true
        }];

        this.myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({}),
                extraParams: {
                    m: 'getSessions'
                },
                simpleSortMode: true
            },
            fields: [
                'name', 'threadId', 'environment', 'status', 'connectedIpAddress', 'lastCommand',
                'lastSQL', 'lastScript', 'commandPath', 'traceName', 'session', 'sessionType', {
                    name: 'startedTime', type: 'date'
                }, {
                    name: 'lastCommandTime', type: 'date'
                }, {
                    name: 'lastSQLTime', type: 'date'
                }, {
                    name: 'lastScriptTime', type: 'date'
            }],
            sorters: [{
                property: 'status',
                direction: 'DESC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('mocaSessionViewerContainer').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('mocaSessionViewerContainer').setLoading(false);
                }
            }
        });

        // Load our store
        this.myStore.on('load', display, this);

        // Apply some stuff
        Ext.apply(this, {
            border: false,
            id: 'sessionList',
            columns: myColumns,
            store: this.myStore,
            loadMask: false,
            style: 'border-right:1px solid #dedede',
            viewConfig: {
                stripeRows: true,
                emptyText: 'No sessions to display.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            },
            listeners: {
                selectionchange: display
            }
        });

        // Call our superclass
        this.callParent(arguments);
    }    
});

// Session Details
Ext.define("RP.Moca.Console.Sessions.SessionDetails", {
    extend: "Ext.panel.Panel",
    id: 'sessionDetails',
     
    constructor: function (config) {
        var serverThreadStore = Ext.create("RP.Moca.util.Store", {
            proxy: {            
                extraParams: {
                    m: 'getServerThreadInformation'
                }
            },
            fields: [
                { name: 'blockedTime'},
                { name: 'lockOwnerId'}, 
                { name: 'threadState'}, 
                { name: 'blockedCount' },
                { name: 'suspended' },
                { name: 'threadId' },
                { name: 'threadName' },
                { name: 'waitedCount' },
                { name: 'inNative' },
                { name: 'waitedTime' },
                { name: 'lockedSynchronizers' },
                { name: 'lockedMontiors' },
                { name: 'lockOwnerName' },
                { name: 'stackTrace' },
                { name: 'lockName' },
                { name: 'lockInfo' }
            ],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('sessionDetails').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('sessionDetails').setLoading(false);
                }
            }
        });

        var myToolbar = [{
            xtype: 'toolbar',
            dock: 'top',
            height: 35,
            defaults: {
                scope: this,
                enableToggle: true,
                toggleGroup: 'sessionDetails',
                allowDepress: false
            },
            items: [{
                xtype: 'tbfill'
            },{
                text: 'Execution',
                pressed: true,
                handler: function() {
                    this.getLayout().setActiveItem('execution');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Environment',
                handler: function() {
                    this.getLayout().setActiveItem('environment');
                },
                style: 'padding:0;margin:0;border-radius:0;border-width:0!important;border-right-color:transparent;border-left-color:transparent;'
            },{
                text: 'Server Thread',
                handler: function() {
                    this.getLayout().setActiveItem('serverThread');
                },
                style: 'padding:0;margin:0;border-radius:0;border-width:0!important;border-right-color:transparent;border-left-color:transparent;'
            },{
                text: 'Call Stack',
                handler: function() {
                    this.getLayout().setActiveItem('callStack');
                },
                style: 'padding:0;margin:0px 5px 0px 0px;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{
                xtype: 'tbfill'
            }]
        }];

        // Apply some stuff
        Ext.apply(this, {
            border: false,
            dockedItems: myToolbar,
            layout: 'card',
            items: [
                Ext.create("RP.Moca.Console.Sessions.Execution", {
                    id: 'execution',
                    layout: 'fit'
                }), 
                Ext.create("RP.Moca.Console.Sessions.Environment", {
                    id: 'environment',
                    layout: 'fit'
                }), 
                Ext.create("RP.Moca.Console.Sessions.ServerThread", {
                    id: 'serverThread',
                    layout: 'form',
                    store: serverThreadStore
                }), 
                Ext.create("RP.Moca.Console.Sessions.CallStack", {
                    id: 'callStack',
                    store: serverThreadStore
                })
            ]
        });

        // Call our superclass
        this.callParent(arguments);
    },

    /**
     * displaySession
     * @param {Object} record
     */
    display: function(record) {
        this.clear();

        var execution = Ext.getCmp('execution');
        var environment = Ext.getCmp('environment');

        //this.fireEvent('afterLayout');
        execution.loadRecord(record);
        environment.loadRecord(record);

        var sessionName = record.get('name');
        var threadId = record.get('threadId');

        Ext.getCmp('callStack').store.load({
            params: {
                m: 'getServerThreadInformation',
                name: sessionName,
                threadId: threadId
            }
        });
        
        Ext.getCmp('serverThread').store.load({
            params: {
                name: sessionName,
                threadId: threadId
            }
        });
        
    },

    clear: function () {
        Ext.getCmp('execution').clear();
        Ext.getCmp('environment').clear();
        Ext.getCmp('serverThread').clear();
        Ext.getCmp('callStack').clear();  
    }
});