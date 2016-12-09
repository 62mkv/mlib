// View
Ext.define("RP.Moca.Console.LogFiles.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('logFilesGridPanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Log Files',
            layout: 'fit',
            items: new RP.Moca.Console.LogFiles.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('logFilesGridPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.LogFiles.Grid", {
    extend: "Ext.grid.Panel",
    id: 'logFilesGridPanel',
    
    initComponent: function() {

        // Handler function for downloading
        var downloadLogFile = function() {
            var selected = this.selModel.selected.items;

            // Make sure a row was selected first.
            if (!selected || selected.length === 0) {
                Ext.Msg.alert('Console', 'Please select a log file to download and try again.');
                return;
            }

            var filename = "";

            for (var i = 0, len = selected.length; i < len; i++) {
                var s = selected[i];

                // Get the filename for this log file.
                var tmpFilename = s.get("pathname");
                if (i === 0) {
                    filename = tmpFilename;
                }
                else {
                    filename = filename + "," + tmpFilename;
                }
            }

            var body = Ext.getBody();

            var frame = body.createChild({
                tag: 'iframe',
                cls: 'x-hidden',
                src: '/console/download?filename=' + filename
            });
        };

        var myColumns = [{
            header: 'Filename',
            dataIndex: 'filename',
            minWidth: 125,
            flex: 4
            }, {
            header: 'Pathname',
            dataIndex: 'pathname',
            minWidth: 150,
            hidden: true,
            flex: 4
            }, {
            header: 'Last Modified',
            dataIndex: 'modified',
            minWidth: 150,
            flex: 2,
            renderer: RP.Moca.util.Format.datetimeString
            }, {
            header: 'Size',
            dataIndex: 'size',
            minWidth: 150,
            flex: 1,
            align: 'right',
            renderer: RP.Moca.util.Format.convertBytesBase10
        }];

        var myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getLogFiles'
                }
            },
            fields:[
                'filename', 'pathname', 'size', {
                name: 'modified',
                type: 'date'
            }],
            sorters: [{
                property: 'pathname',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('logFilesGridPanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('logFilesGridPanel').setLoading(false);
                }
            }
        });
            
        var myToolbar = [{
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Download Log File(s)',
                scope: this,
                handler: downloadLogFile,
                style: {
                    margin: '3px'
                }
            }]
        }, {
            xtype: 'toolbar',
            dock: 'top',
            height: 1
        }];

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            selModel: Ext.create("Ext.selection.RowModel", {
                mode: "MULTI"
            }),
            dockedItems: myToolbar,
            store: myStore,
            columns: myColumns,
            viewConfig: {
                emptyText: 'No log files currently exist.',
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
