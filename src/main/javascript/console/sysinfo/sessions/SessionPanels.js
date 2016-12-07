// Execution panel
Ext.define("RP.Moca.Console.Sessions.Execution", {
    extend: "Ext.form.Panel",

    initComponent: function () {

        // Apply some stuff
        Ext.apply(this, {
            border: false,
            autoScroll: true,
            layout: 'anchor',
            anchor: '100%',
            xtype: 'panel',
            defaults: {
                xtype: 'textarea',
                labelAlign: 'top',
                readOnly: true,
                padding: 5,
                width: '95%',
                grow: true,
                maxHeight: 230
            },
            items: [
                { hidden: true }, // helps with the layout
                {
                    fieldLabel: 'Connected IP Address',
                    name: 'connectedIpAddress',
                    height: 45
                },{
                    fieldLabel: 'Current Command Path',
                    name: 'commandPath'
                },{
                    fieldLabel: 'Last Command Time',
                    name: 'lastCommandTime',
                    height: 55
                }, {
                    fieldLabel: 'Last Command',
                    name: 'lastCommand'
                },{
                    name: 'lastSQLTime',
                    fieldLabel: 'Last SQL Time',
                    height: 55
                }, {
                    fieldLabel: 'Last SQL',
                    name: 'lastSQL'
                }
            ]
        });

        this.callParent(arguments);
    },

    /**
     * loadRecord
     * @desc Load the record for the current thread as passed.
     * @param Object record The data for the current thread.
     * @return void
     */
    loadRecord: function (record) {
        this.getForm().loadRecord(record);
    },

    /**
     * getValue
     * @desc Retrieve the value of a field in the form.
     * @param Object fieldName The name of the field to retrieve from the form.
     * @return String The value of the field as specified by fieldName.
     */
    getValue: function (fieldName) {
        if (this.getForm().findField(fieldName)) {
            return this.getForm().findField(fieldName).getValue();
        }
        return null;
    },

    clear: function () {
        this.getForm().reset();
    }
});

// CallStack panel
Ext.define("RP.Moca.Console.Sessions.CallStack", {
    extend: "Ext.panel.Panel", 

    initComponent: function () {
        var execution = Ext.getCmp('execution');

        // register the listeners to the store for rendering the data when it changes
        this.store.on('load', this.display, this);
        this.store.on("loadException", this.clear, this);

        Ext.apply(this, {
            border: false,
            autoScroll: true,
            tpl: new Ext.XTemplate(
                    '<ul>',
                        '<tpl for = "stackTrace">',
                            '<li style="padding:6px 0 6px 15px;font-size:11px">{.}</li>',
                        '</tpl>',
                    '</ul>'
                )
        });

        // Call our superclass.
        this.callParent(arguments);
    },
    
    display: function(store, records) {
        if (typeof records[0] != "undefined") {
            this.update(records[0].data);
        }
    },
    
    clear: function() {
        this.update();
    }
});

// Environment panel
Ext.define("RP.Moca.Console.Sessions.Environment", {
    extend: "Ext.grid.Panel",

    initComponent: function (config) {

        this.environmentGrid = new Ext.data.Store({
            fields: ['name','value'],
            layout: 'fit',
            autoScroll: true,
            enableColumnResize: true,
            viewConfig: {
                stripeRows: true
            },
            source: {}
        });

        Ext.apply(this, {
            border: false,
            layout: 'fit',
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            columns: [{header: 'Name', dataIndex:'name',flex: 1},{header:'Value', dataIndex:'value', flex: 2}],
            store: this.environmentGrid
        });
        
        this.callParent(arguments);
    },

    /**
     * Load the record for the current thread as passed.
     * 
     * @param {Object} record The data for the current thread.
     */
    loadRecord: function (record) {
        if (typeof record != "undefined") {
            var records = record.get("environment");
            var arr = []; 
            Ext.Object.each(records, function(key, value) {  
              arr.push({ 
                name: key, 
                value: value 
              }); 
             }, this); 
             
            this.environmentGrid.add(arr);
        }
    },

    clear: function () {
        this.environmentGrid.removeAll();
    }
});

// ServerThread panel
Ext.define("RP.Moca.Console.Sessions.ServerThread", {
    extend: "Ext.grid.Panel",
    
    initComponent: function () {
    
        this.store.on("load", this.display, this);
        this.store.on("loadException", this.clear, this);
    
        this.serverThreadDetailsGrid = new Ext.data.Store({
            fields: ['name','value'],
            autoScroll: true,
            id: "threadPropertiesGrid",
            listeners: {
                validateedit: function () { return false; }
            },
            viewConfig: {
                stripeRows: true
            },
            source: {}
        });

        Ext.apply(this, {
            border: false,
            layout: 'fit',
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            columns: [{header: 'Name', dataIndex:'name',flex: 1},{header:'Value', dataIndex:'value', flex: 2}],
            store: this.serverThreadDetailsGrid
        });

        this.callParent(arguments);
    },

    /**
     * displayDetails
     * Displays the details of the thread in the grid to the user.
     * @param {Object} store The store.
     * @param {Object} record The records returned during the load.
     * @param {Object} options The options used to load the store.
     */
    display: function (store, records, options) {
        var detailSpec, index;
        var values = {};

        var propertyFields = [{
            fieldLabel: 'Thread ID',
            dataIndex: 'threadId',
            name: 'threadId'
        }, {
            fieldLabel: 'Thread Name',
            dataIndex: 'threadName',
            name: 'threadName'
        }, {
            fieldLabel: 'Thread State',
            dataIndex: 'threadState',
            name: 'threadState'
        }, {
            fieldLabel: 'In Native',
            dataIndex: 'inNative',
            name: 'inNative'
        }, {
            fieldLabel: 'Suspended',
            dataIndex: 'suspended',
            name: 'suspended'
        }, {
            fieldLabel: 'Blocked Time',
            dataIndex: 'blockedTime',
            name: 'blockedTime'
        }, {
            fieldLabel: 'Lock Owner ID',
            dataIndex: 'lockOwnerId',
            name: 'lockOwnerId'
        }, {
            fieldLabel: 'Blocked Count',
            dataIndex: 'blockedCount',
            name: 'blockedCount'
        }, {
            fieldLabel: 'Waited Count',
            dataIndex: 'waitedCount',
            name: 'waitedCount'
        }, {
            fieldLabel: 'Waited Time',
            dataIndex: 'waitedTime',
            name: 'waitedTime'
        }];

        // if the store return an non-emtpy set, then grab the 
        // data portion of the object and process it. 
        var data = {};
        if (typeof records[0] != "undefined") {
            data = records[0].data;

            // build up an object that the grid can used and pass it
            for (var i = 0; i < propertyFields.length; i++) {
                if (!Ext.isFunction(propertyFields[i])) {
                    detailSpec = propertyFields[i];
                    values[detailSpec.fieldLabel] = data[detailSpec.dataIndex];
                }
            }
        }
        
        var arr = [];
        Ext.Object.each(values, function(key, value) {
            arr.push({
                name: key,
                value: value
            });
        }, this);
        
        this.serverThreadDetailsGrid.add(arr);
    },

    /**
     * clear
     * Clear the form out in the case a load exception occurs
     * or there was no data retrieved from the store.
     * @return void
     */
    clear: function () {
        this.serverThreadDetailsGrid.removeAll();
    }
});