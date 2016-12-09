// View
Ext.define("RP.Moca.Console.ResourceUsage.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('resourceUsagePanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Resource Usage',
            layout: 'fit',
            items: new RP.Moca.Console.ResourceUsage.Panel({
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('resourceUsagePanel').refresh();
                        }
                    })
                ]
           })
       });
 
       this.callParent(arguments);
   }
});

// Panel
Ext.define("RP.Moca.Console.ResourceUsage.Panel", {
    extend: "Ext.Panel",
    id: 'resourceUsagePanel',
    
    initComponent: function () {
 
        this.store = Ext.create("RP.Moca.util.Store", { 
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getResourceUsage'
                }
            },
            fields: [
              'current_heap_used', 'current_heap_size', 'max_heap_size', 'delta_current_heap_size', 
              'delta_max_heap_size', 'current_sessions', 'peak_sessions', 'max_sessions', 
              'delta_peak_sessions', 'delta_max_sessions', 'current_native_processes', 
              'peak_native_processes', 'max_native_processes', 'delta_peak_processes', 
              'delta_max_processes', 'current_db_connections', 'peak_db_connections', 
              'max_db_connections', 'delta_peak_db_connections', 'delta_max_db_connections'
            ],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('resourceUsagePanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('resourceUsagePanel').setLoading(false);
                }
            }
        });

        this.store.on("load", this.onStoreLoad, this);
 
        var myMemoryUsage = new RP.Moca.Console.UsagePanel({
            title: 'Memory',
            currentName: 'Current Heap Used',
            peakName: 'Current Heap Size',
            maxName: 'Max Heap Size',
            current: 'current_heap_used',
            peak: 'current_heap_size',
            max: 'max_heap_size',
            delta_peak: 'delta_current_heap_size',
            delta_max: 'delta_max_heap_size',
            units: 'mb',
            store: this.store
        });

        var mySessionUsage = new RP.Moca.Console.UsagePanel({
            title: 'Sessions',
            currentName: 'Current Sessions',
            peakName: 'Peak Sessions',
            maxName: 'Max Sessions',
            current: 'current_sessions',
            peak: 'peak_sessions',
            max: 'max_sessions',
            delta_peak: 'delta_peak_sessions',
            delta_max: 'delta_max_sessions',
            store: this.store,
            style: 'padding-left: 5px;'
        });

        var myNativeProcessUsage = new RP.Moca.Console.UsagePanel({
            title: 'Native Processes',
            currentName: 'Current Native Processes',
            peakName: 'Peak Native Processes',
            maxName: 'Max Native Processes',
            current: 'current_native_processes',
            peak: 'peak_native_processes',
            max: 'max_native_processes',
            delta_peak: 'delta_peak_processes',
            delta_max: 'delta_max_processes',
            store: this.store
        });

        var myDatabaseConnectionUsage = new RP.Moca.Console.UsagePanel({
            title: 'Database Connections',
            currentName: 'Current Database Connections',
            peakName: 'Peak Database Connections',
            maxName: 'Max Database Connections',
            current: 'current_db_connections',
            peak: 'peak_db_connections',
            max: 'max_db_connections',
            delta_peak: 'delta_peak_db_connections',
            delta_max: 'delta_max_db_connections',
            store: this.store,
            style: 'padding-left: 5px;'
        });

        Ext.apply(this, {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'panel',
                border: false,
                padding: 5,
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                flex: 1,
                items: [
                    myMemoryUsage, mySessionUsage
                ]
            }, {
                xtype: 'panel',
                border: false,
                padding: 5,
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                flex: 1,
                items: [
                    myNativeProcessUsage, myDatabaseConnectionUsage
                ]
            }]
        });

        this.callParent(arguments);
   },

   refresh: function() {
        this.store.load();
   },

   onStoreLoad: function() {
        this.items.items[0].items.items[0].refresh();
        this.items.items[0].items.items[1].refresh();
        this.items.items[1].items.items[0].refresh();
        this.items.items[1].items.items[1].refresh();
   }
});
 
// Memory Usage Panel
Ext.define("RP.Moca.Console.UsagePanel", {
    extend: "Ext.panel.Panel",
    units: '',
 
    initComponent: function() {

        this.store.on('load', this.refresh, this);

        this.fieldsPanel = new Ext.form.Panel({
            flex: 1,
            border: false,
            defaultType: 'displayfield',
            layout: 'anchor',
            defaults: {
                labelWidth: this.lwidth
            },
            padding: '10px 20px',
            items: [{
                fieldLabel: this.currentName,
                itemId: 'current'
            }, {
                fieldLabel: this.peakName,
                itemId: 'peak'
            }, {
                fieldLabel: this.maxName,
                itemId: 'max'
            }]
        });

        this.chart = Ext.create("Ext.chart.Chart", {
            xtype: 'chart',
            height: 100,
            animate: true,
            shadow: false,
            store: this.store,
            axes: [{
                type: 'Numeric',
                position: 'bottom',
                fields: [this.current, this.delta_peak, this.delta_max],
                title: false,
                grid: true
            }],
            gradients: [{
                id: 'greenGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#89e746'
                    },
                    100: {
                        color: '#3ba100'
                    }
                }
            },  {
                id: 'yellowGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#f8fd77'
                    },
                    100: {
                        color: '#fff600'
                    }
                }
            },  {
                id: 'redGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#ff4a4c'
                    },
                    100: {
                        color: '#c41515'
                    }
                }
            }],
            series: [{
                type: 'bar',
                axis: 'bottom',
                gutter: 80,
                yField: [this.current, this.delta_peak, this.delta_max],
                stacked: true,
                showInLegend: false,
                renderer: function(sprite, record, attr, index, store) {
                    var color = ['url(#redGradient)',
                                 'url(#yellowGradient)',
                                 'url(#greenGradient)'][index];
                    return Ext.apply(attr, {
                        fill: color
                    });
                },
                tips: {
                    trackMouse: true,
                    width: 255,
                    height: 28,
                    currentName: this.currentName,
                    current: this.current,
                    peakName: this.peakName,
                    peak: this.peak,
                    maxName: this.maxName,
                    max: this.max,
                    delta_max: this.delta_max,
                    delta_peak: this.delta_peak,
                    units: this.units,
                    renderer: function(storeItem, item) {
                        var select, amount;
                        if (item.value[1] === storeItem.data[this.current]) {
                            select = this.currentName;
                            amount = storeItem.data[this.current];
                        }
                        else if (item.value[1] == storeItem.data[this.delta_max]) {
                            select = this.maxName;
                            amount = storeItem.data[this.max];
                        }
                        else {
                            select = this.peakName;
                            amount = storeItem.data[this.peak];
                        }
                        this.setTitle(select + ': ' + amount + this.units);
                    }
                }
            }]
        });

        Ext.apply(this, {
            title: this.title,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            border: true,
            flex: 1,
            items: [
                this.chart, this.fieldsPanel
            ]
        });

        this.callParent(arguments);
    },

    refresh: function (scope, records, successful, eOpts) {
        if (this.store.totalCount === 0) {
            return;
        }

        if (successful) {
            var myRecord = this.store.getAt(0).data;

            this.fieldsPanel.getComponent('current').setValue(myRecord[this.current] + this.units);
            this.fieldsPanel.getComponent('peak').setValue(myRecord[this.peak] + this.units);
            this.fieldsPanel.getComponent('max').setValue(myRecord[this.max] + this.units);
        }
    }
});