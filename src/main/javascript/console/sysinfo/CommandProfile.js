Ext.define("RP.Moca.Console.Util", {
    singleton: true,
    
    // Handler function to add "ms"
    formatAndAddMS: function(number) {
        return RP.Moca.util.Format.truncateNumber(number) + " ms";
    }
});

// View
Ext.define("RP.Moca.Console.CommandProfile.View", {
    extend: "RP.util.taskflow.BaseTaskForm",
    
    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('commandProfileGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Command Profile',
            layout: 'fit',
            items: [ new RP.Moca.Console.CommandProfile.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander',
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Component Level:</b></td><td>{component_level}</td></tr><tr><td><b>Command:</b></td><td>{command}</td></tr><tr><td><b>Command Path:</b></td><td>{command_path}</td></tr><tr><td><b>Type:</b></td><td>{type}</td></tr><tr><td><b>Executions:</b></td><td>{execution_count}</td></tr><tr><td><b>Min:</b></td><td>{min_ms} ms</td></tr><tr><td><b>Max:</b></td><td>{max_ms} ms</td></tr><tr><td><b>Avg:</b></td><td>{avg_ms} ms</td></tr><tr><td><b>Total:</b></td><td>{total_ms} ms</td></tr><tr><td><b>Self Avg:</b></td><td>{avg_self_ms} ms</td></tr><tr><td><b>Self Total:</b></td><td>{self_ms} ms</td></tr></table>', {
                        blankNulls: true
                    })
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('commandProfileGrid').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Level',
                    dataIndex: 'component_level',
                    minWidth: 110,
                    flex: 1
                }, {
                    header: 'Command',
                    dataIndex: 'command',
                    minWidth: 150,
                    flex: 1
                }, {
                    header: 'Type',
                    dataIndex: 'type',
                    renderer: RP.Moca.util.Format.commandTypeString,
                    minWidth: 95,
                    flex: 1
                }, {
                    header: 'Executions',
                    dataIndex: 'execution_count',
                    align: 'right',
                    minWidth: 115,
                    flex: 1
                }, {
                    header: 'Min',
                    dataIndex: 'min_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 80,
                    flex: 1,
                    hidden: true
                }, {
                    header: 'Max',
                    dataIndex: 'max_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 80,
                    flex: 1
                }, {
                    header: 'Avg',
                    dataIndex: 'avg_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 100,
                    flex: 1
                }, {
                    header: 'Total',
                    dataIndex: 'total_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 80,
                    flex: 1
                }, {
                    header: 'Self Avg',
                    dataIndex: 'avg_self_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 100,
                    flex: 1
                }, {
                    header: 'Self Total',
                    dataIndex: 'self_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 100,
                    flex: 1
                }],
                store: new RP.Moca.util.Store({
                    autoLoad: true,
                    proxy: {
                        type: 'ajax',
                        url: '/console',
                        extraParams: {
                            m: 'getCommandProfile'
                        },
                        reader: new RP.Moca.Console.Results.ResultsReader({}),
                        simpleSortMode: true
                    },
                    fields: [
                        'component_level', 'command', 'type', 'command_path', 'execution_count',
                        'min_ms', 'max_ms', 'avg_ms', 'total_ms', 'self_ms', 'avg_self_ms'
                    ],
                    sorters: [{
                            property: 'self_ms',
                            direction: 'DESC'
                    }],
                    listeners: {
                        'beforeload' :  function() {
                            Ext.getCmp('commandProfileGrid').setLoading(true);
                        },
                        'load' :  function() {
                            Ext.getCmp('commandProfileGrid').setLoading(false);
                        }
                    }
                })
            }) ]
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.CommandProfile.Grid", {
    extend: "Ext.grid.Panel",
    id: 'commandProfileGrid',
    
    initComponent: function() {

        // Handler function for clearing the command profile
        var clearProfile = function() {

            // Make an Ajax call to the server.       
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'clearCommandProfile'
                },
                success: function (result) {
                    var status = Ext.decode(result.responseText).status;

                    if (status !== 0) {
                        var message = Ext.decode(result.responseText).message;
                        Ext.Msg.alert('Console', 'An error occurred attempting to clear the command profile.\n\n' + message);
                    }
                },
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not send the clear profile request to the server.'
                }
            });

            this.store.load();
        };

        // Handler function for downloading
        var downloadProfile = function () {
            var body = Ext.getBody();

            var frame = body.createChild({
                tag: 'iframe',
                cls: 'x-hidden',
                src: '/console/profile'
            });
        };

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Clear Command Profile',
                scope: this,
                handler: clearProfile,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Download Command Profile',
                scope: this,
                handler: downloadProfile,
                style: {
                    margin: '3px'
                }
            }]
        });

        var myTopToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'top',
            height: 1,
            items: [{
                text: 'Download Command Profile',
                scope: this,
                handler: downloadProfile,
                style: {
                    margin: '3px'
                }
            }]
        });

         RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.items.items[0].disable();
                }
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            dockedItems: [myToolbar, myTopToolbar],
            viewConfig: {
                emptyText: 'No command profiling is available.',
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