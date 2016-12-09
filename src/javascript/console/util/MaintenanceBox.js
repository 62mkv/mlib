// Maintenance
Ext.define('RP.Moca.Console.Util.Maintenance', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
    
        Ext.apply(this, {
            id: 'maintenance',
            layout: 'column',
            region: 'west',
            flex: 4,
            border: false,
            defaults: {
                xtype: 'textarea',
                labelAlign: 'top',
                padding: 9,
                height: 45
            },
            items: [{
                fieldLabel: 'Job ID',
                itemId: 'job_id',
                id: 'job_id',
                width: '95%'
            },{
                fieldLabel: 'Task ID',
                itemId: 'task_id',
                id: 'task_id',
                width: '95%'
            },{
                fieldLabel: 'Name',
                itemId: 'name',
                id: 'name',
                width: '95%'
            },{
                fieldLabel: 'Group Name',
                itemId: 'grp_nam',
                id: 'grp_nam',
                width: '46%'
            },{
                fieldLabel: 'Role ID',
                xtype: 'combobox',
                itemId: 'role_id',
                id: 'role_id',
                store: ['*']
            },{
                fieldLabel: 'Command',
                itemId: 'command',
                id: 'command',
                width: '95%',
                autoScroll: true,
                height: 380
            },{
                xtype: 'hidden',
                itemId: 'type',
                id: 'type'
            },{
                xtype: 'hidden',
                itemId: 'task_typ',
                id: 'task_typ'
            },{
                xtype: 'hidden',
                itemId: 'action',
                id: 'action'
            }]
        });
        
        this.callParent(arguments);
    
    }

});

// Scheduled Panel
Ext.define('RP.Moca.Console.Util.ScheduledMaint', {
    extend: 'Ext.form.Panel',
    
    initComponent: function () {
    
        var times = Ext.create('Ext.data.Store', {
            fields: ['time','hour'],
            data: [
                {'time':'Every Hour','hour':'*'},
                {'time':'12:00','hour':'0'},
                {'time':'1:00','hour':'1'},
                {'time':'2:00','hour':'2'},
                {'time':'3:00','hour':'3'},
                {'time':'4:00','hour':'4'},
                {'time':'5:00','hour':'5'},
                {'time':'6:00','hour':'6'},
                {'time':'7:00','hour':'7'},
                {'time':'8:00','hour':'8'},
                {'time':'9:00','hour':'9'},
                {'time':'10:00','hour':'10'},
                {'time':'11:00','hour':'11'}
            ]
        });
        
        var dates = Ext.create('Ext.data.Store', {
            fields: ['date','dateNum'],
            data: [
                {'date':'Sunday','dateNum':'1'},
                {'date':'Monday','dateNum':'2'},
                {'date':'Tuesday','dateNum':'3'},
                {'date':'Wednesday','dateNum':'4'},
                {'date':'Thursday','dateNum':'5'},
                {'date':'Friday','dateNum':'6'},
                {'date':'Saturday','dateNum':'7'}
            ]
        });
    
        Ext.apply(this, {
            width: '50%',
            border: false,
            id: 'schedulePanel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            region: 'east',
            flex: 7,
            padding: '5px 5px 5px 5px',
            defaults: {
                padding: '0 0 5px 0'
            },
            items: [
                Ext.create('Ext.form.Panel',{
                    id: 'simpleSchedule',
                    layout: 'column',
                    height: 125,
                    bodyStyle: 'background: #DEDEDE;',
                    defaults: {
                        labelAlign: 'top',
                        padding: 5
                    },
                    items: [{
                        xtype:'label',
                        text: 'Schedule',
                        style: 'font-weight:bold;font-size:14px;',
                        padding: '5px 0 0 5px'
                    }, {
                        xtype: 'hidden',
                        width: '55%',
                        height: 25,
                        padding: 0
                    }, {
                        xtype: 'combobox',
                        store: times,
                        displayField: 'time',
                        valueField: 'hour',
                        fieldLabel: 'Time',
                        itemId: 'schedule_time',
                        id: 'schedule_time',
                        editable: false,
                        listeners: {
                            'select': function(combo, row, index) {
                                if (row[0].data.time == RP.getMessage('rp.moca.common.everyHour')) {
                                    Ext.getCmp('am_time').disable();
                                    Ext.getCmp('am_time').setValue(true);
                                    Ext.getCmp('pm_time').disable();
                                }
                                else {
                                    Ext.getCmp('am_time').enable();
                                    Ext.getCmp('pm_time').enable();
                                }
                                
                                Ext.getCmp('hours').setValue(row[0].data.hour);
                            },
                            change: function(field, newValue, oldValue) {
                                if (Ext.getCmp('pm_time').value){
                                    Ext.getCmp('hours').setValue(parseInt(newValue, 10)+12);
                                }
                                else {
                                    Ext.getCmp('hours').setValue(newValue);
                                }
                            }
                        }
                    }, {
                        xtype: 'radiofield',
                        boxLabel: 'AM',
                        name: 'time',
                        inputValue: 'am',
                        itemId: 'am_time',
                        id: 'am_time',
                        checked: true,
                        padding: '30px 5px 0 0'
                    }, {
                        xtype: 'radiofield',
                        boxLabel: 'PM',
                        name: 'time',
                        inputValue: 'pm',
                        itemId: 'pm_time',
                        id: 'pm_time',
                        padding: '30px 15px 0 0',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                if (newValue) {
                                    Ext.getCmp('hours').setValue(parseInt(Ext.getCmp('hours').value, 10)+12);
                                }
                                if (oldValue) {
                                    Ext.getCmp('hours').setValue(parseInt(Ext.getCmp('hours').value, 10)-12);
                                }
                            }
                        }
                    }, {
                        xtype: 'combobox',
                        store: dates,
                        displayField: 'date',
                        valueField: 'dateNum',
                        fieldLabel: 'Repeat',
                        itemID: 'schedule_repeat',
                        id: 'schedule_repeat',
                        multiSelect: true,
                        editable: false,
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                Ext.getCmp('dayWeek').setValue(newValue);
                            }
                        }
                    }],
                    dockedItems: [{
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            { xtype: 'tbfill' },
                            { xtype: 'button', text: 'Advanced Schedule', handler: function() {
                                            Ext.getCmp('simpleSchedule').hide();
                                            Ext.getCmp('advancedSchedule').setHeight(125);
                                            Ext.getCmp('advancedSchedule').show(); } },
                            { xtype: 'tbfill' }
                        ],
                        style: 'background: #DEDEDE;'
                    }]
            }),
                Ext.create('Ext.form.Panel',{
                    id: 'advancedSchedule',
                    hidden: true,
                    layout: 'column',
                    height: 125,
                    bodyStyle: 'background: #DEDEDE;',
                    defaults: {
                        labelAlign: 'top',
                        padding: 5,
                        xtype: 'textarea',
                        hidden: true,
                        value: '0'
                    },
                    items: [{
                        xtype:'label',
                        text: 'Schedule',
                        style: 'font-weight:bold;font-size:14px;',
                        padding: '5px 0 0 5px',
                        hidden: false
                    }, {
                        xtype: 'hidden',
                        width: '55%',
                        height: 25,
                        padding: 0,
                        hidden: false
                    }, {
                        itemId: 'schedule',
                        id: 'schedule',
                        height: 30,
                        width: '60%',
                        hidden: false,
                        value: '0 0 0 * * ?',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                if (Ext.getCmp('simpleScheduleButton').disabled) {
                                    var schedule = newValue;
                                    var complicated = schedule.split(' ');
                                    complicated.splice(5, 1);
                                    complicated = complicated.join(' ');

                                    if (schedule.match('/') || complicated.match(',')){
                                        Ext.getCmp('simpleScheduleButton').disable();
                                        Ext.getCmp('tooAdvanced').show();
                                    }
                                    else {
                                        Ext.getCmp('simpleScheduleButton').enable();
                                        Ext.getCmp('tooAdvanced').hide();
                                    }
                                }
                            }
                        }
                    }, {
                        xtype: 'label',
                        cls: 'rp-form-field-help-link',
                        hidden: false,
                        style: 'float: left;',
                        margin: '9px 0 0 0',
                        listeners: {
                            click: {
                                element: 'el',
                                fn: function() {
                                    if (!Ext.getCmp('scheduleHelp')) {
                                        var panel = Ext.create('RP.Moca.Console.Util.ScheduleHelp', {});
                                        var topPanel;
                                        if (Ext.getCmp('addJobPanel')) {
                                            topPanel = Ext.getCmp('addJobPanel');
                                        }
                                        else if (Ext.getCmp('modifyJobPanel')) {
                                            topPanel = Ext.getCmp('modifyJobPanel');
                                        }
                                        else if (Ext.getCmp('copyJobPanel')) {
                                            topPanel = Ext.getCmp('copyJobPanel');
                                        }
                                        topPanel.setWidth(1000);
                                        topPanel.down('#advancedSchedule').setHeight(100);
                                        topPanel.down('#tracingPanel').setHeight(225);
                                        topPanel.down('#command').setHeight(200);
                                        topPanel.add(panel);
                                        topPanel.center();
                                        panel.animate({
                                            duration: 500,
                                            to: {
                                                width: 370
                                            },
                                            dynamic: true
                                        });
                                    }
                                }
                            }
                        }
                    }, {
                        xtype: 'label',
                        itemId: 'tooAdvanced',
                        id: 'tooAdvanced',
                        text: 'The schedule is too complicated for the simple view.',
                        width: '80%'
                    }, {
                        itemId: 'hours',
                        id: 'hours',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                if (parseInt(newValue, 10) < 12 && Ext.getCmp('pm_time').value) {
                                    field.setValue(oldValue);
                                }
                                else {
                                    field.setValue(newValue);
                                }
                                
                                var schedule = Ext.getCmp('schedule').value.split(' ');
                                schedule.splice(2, 1, field.value);
                                schedule = schedule.join(' ');
                                Ext.getCmp('schedule').setValue(schedule);
                            }
                        }
                    }, {
                        itemId: 'dayWeek',
                        id: 'dayWeek',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                var schedule = Ext.getCmp('schedule').value.split(' ');
                                if (newValue && newValue.length < 13 && newValue.length % 2 !== 0) {
                                    newValue = newValue.split(',').sort().toString();
                                    schedule.splice(5, 1, newValue);
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                                else if (newValue.length === 13) {
                                    schedule.splice(5, 1, '*');
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                                else if (newValue.length % 2 === 0 && newValue.length !== 0) {
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                                else {
                                    schedule.splice(5, 1, '?');
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                            }
                        }
                    }],
                    dockedItems: [{
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            { xtype: 'tbfill' },
                            { xtype: 'button', 
                                id: 'simpleScheduleButton',
                                itemId: 'simpleScheduleButton',
                                text: 'Simple Schedule', 
                                handler: this.populateSimple
                            },
                            { xtype: 'tbfill' }
                        ],
                        style: 'background: #DEDEDE;'
                    }]
            }),
                Ext.create('RP.Moca.Console.Util.Tracing', {}),
                Ext.create('RP.Moca.Console.Util.Behavior', {})
            ]
        });
        
        this.callParent(arguments);
    },
    
    populateSimple: function() {
        var panel = Ext.getCmp('advancedSchedule');
        var schedule = panel.down('#schedule').value;
        var complicated = schedule.split(' ');
        complicated.splice(5, 1);
        complicated = complicated.join(' ');
        
        if (schedule.match('/') || complicated.match(',')){
            panel.down('#simpleScheduleButton').disable();
            panel.down('#tooAdvanced').show();
        }
        else{ 
            Ext.getCmp('simpleSchedule').show();
            Ext.getCmp('advancedSchedule').hide();
            if (Ext.getCmp('scheduleHelp')) {
                Ext.getCmp('scheduleHelp').close();
            }
            Ext.getCmp('tracingPanel').setHeight(200);
            Ext.getCmp('command').setHeight(255);
            
            var cron = Ext.getCmp('schedule').value.split(' ');
            if (cron.length >= 6) {
                var days = cron.splice(5,1);
                var hours = cron.splice(2,1);
                if (days[0].length > 0) {
                    var weekDays;
                    if (days[0] === '*') { 
                        weekDays = ['1','2','3','4','5','6','7'];
                    }
                    else if (days[0] === '?') {
                        weekDays = '';
                    }
                    else if (days[0].length % 2 !== 0) {
                        weekDays = days[0].split(',');
                    }
                    else {
                        weekDays = days[0];
                    }
                    Ext.getCmp('schedule_repeat').setValue(weekDays);
                }
                if (hours === '*') {
                    Ext.getCmp('schedule_time').setValue('*');
                }
                else if (hours >= 12) {
                    hours = hours - 12;
                    Ext.getCmp('pm_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
                else if (hours < 12) {
                    Ext.getCmp('am_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
            }
        }
    } 
});

// Schedule Help Panel
Ext.define('RP.Moca.Console.Util.ScheduleHelp', {
    extend: 'Ext.panel.Panel',
    
    initComponent: function() {
        Ext.apply(this, {
            title: 'Schedule Helper',
            width: 0,
            height: '100%',
            closable: true,
            layout: 'vbox',
            autoScroll: true,
            id: 'scheduleHelp',
            items: [{
                xtype: 'label',
                text: 'The expression[cron] should be string comprising of 6 or 7 fields separated by white space. Fields can contain any of the allowed values, along with various combinations of the allowed special characters for that field.',
                margin: 5,
                width: 353
            }, {
                xtype: 'fieldset',
                title: 'Format',
                layout: {
                    type: 'table',
                    columns: 4
                },
                margin: 5,
                width: 353,
                height: 250,
                defaults: {
                    border: false,
                    padding: '0 10px 10px 0'
                },
                items: [{html: 'Field Name', style: 'font-weight:bold;'},{html: 'Mandatory', style: 'font-weight:bold;'},{html: 'Allowed Values', style: 'font-weight:bold;'},{html: 'Allowed Special Characters', style: 'font-weight:bold;'},
                        {html: 'seconds'},{html: 'YES'},{html: '0-59'},{html: ', - * /'},
                        {html: 'minutes'},{html: 'YES'},{html: '0-59'},{html: ', - * /'},
                        {html: 'hours'},{html: 'YES'},{html: '0-23'},{html: ', - * /'},
                        {html: 'Day of Month'},{html: 'YES'},{html: '1-31'},{html: ', - * ? / L W C'},
                        {html: 'Month'},{html: 'YES'},{html: '1-12 or JAN-DEC'},{html: ', - * /'},
                        {html: 'Day of Week'},{html: 'YES'},{html: '1-7 or SUN-SAT'},{html: ', - * ? / L C #'},
                        {html: 'Year (Optional)'},{html: 'NO'},{html: 'empty, 1970-2099'},{html: ', - * /'}]
            }, {
                xtype: 'fieldset',
                title: 'Special Characters',
                margin: 5,
                width: 353,
                items: [{
                    xtype: 'label',
                    html: '[*] - Expression will match all values <br/> "* * * * * ? 2010" - Execute every second in the year 2010 <br/><br/> [/] - Used to describe increments <br/> "10/15 * * * * ?" - Execute every 15 seconds starting at 10 seconds <br/><br/> [,] - Used to separate items in a list <br/> "0 0 0 1,15 * ?" - Execute every 1st and 15th of the month at midnight <br/><br/> [-] - Used to define a range <br/> "0 0 0 ? * MON-WED" - Execute at midnight Monday through Wednesday <br/><br/> [?] - Used to omit specification for day of week or month <br/> "0 0 0 1 * ?" - Execute at midnight on the first of the month regardless of day of week <br/><br/> [L] - Stands for "last" <br/> "0 0 0 L * ?" - Execute at midnight on the last day of the month <br/> "0 0 0 ? * 6L" - Execute at midnight on the last Friday of the month <br/><br/> [#] - Used to specify the occurence of a day in a month <br/> 0 0 0 ? * SUN#3" - Execute on the third Sunday of the month at midnight <br/><br/> [W] - Denotes the nearest weekday <br/> "0 0 0 15W * ?" - Execute on the weekday nearest to the 15th at midnight <br/> "0 0 0 LW * ?" - Execute on the weekday nearest to the end of the month at midnight'
                }]
            }],
            listeners: {
                close: {
                    fn: function(el, e) {
                        var panel = el.up();
                        panel.setWidth(800);
                        panel.down('#command').setHeight(255);
                        panel.center();
                    }
                }
            }
        });
        this.callParent(arguments);
    }
});

// Timer Panel
Ext.define('RP.Moca.Console.Util.TimerMaint', {
    extend: 'Ext.form.Panel',
    
    initComponent: function () {
        Ext.apply(this, {
            border: false,
            id: 'timerPanel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            region: 'east',
            padding: 5,
            flex: 5,
            defaults: {
                padding: '0 0 5px 0'
            },
            items: [
                Ext.create('Ext.form.Panel',{
                    itemID: 'timerSection',
                    layout: 'column',
                    height: 100,
                    bodyStyle: 'background: #DEDEDE;',
                    padding: '0 0 5px 0',
                    defaults: {
                        xtype: 'textarea',
                        height: 45,
                        labelAlign: 'top',
                        padding: 5
                    },
                    items: [{
                        xtype:'label',
                        text: 'Timer',
                        style: 'font-weight:bold;font-size:14px;',
                        height: 30
                    }, {
                        xtype: 'hidden',
                        width: '100%',
                        height: 10,
                        padding: 0
                    }, {
                        fieldLabel: 'Interval (Seconds)',
                        itemId: 'timer',
                        id: 'timer'
                    }, {
                        fieldLabel: 'Start Delay (Seconds)',
                        itemId: 'start_delay',
                        id: 'start_delay'
                    }]
            }),
                Ext.create('RP.Moca.Console.Util.Tracing', {}),
                Ext.create('RP.Moca.Console.Util.Behavior', {})
            ]
        });
        
        this.callParent(arguments);
    }
});

// Tracing Panel
Ext.define('RP.Moca.Console.Util.Tracing', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        Ext.apply(this, {
            id: 'tracingPanel',
            height: 200,
            bodyStyle: 'background: #DEDEDE;',
            defaults: {
                labelAlign: 'top',
                padding: '5px 5px 5px 5px',
                width: '92%'
            },
            items: [{
                xtype:'label',
                text: 'Tracing',
                style: 'font-weight:bold;font-size:14px;'
            }, {
                xtype: 'hidden',
                width: '100%',
                height: 20,
                padding: 0
            }, {
                xtype: 'rpuxToggleField',
                offText: 'Off',
                onText: 'On',
                itemId: 'trace_level',
                fieldLabel: 'Tracing Enabled',
                labelAlign: 'top'
            }, {
                fieldLabel: 'Start In Directory',
                xtype: 'textarea',
                itemId: 'run_dir',
                height: 50,
                width: '98%',
                id: 'run_dir'
            }, {
                fieldLabel: 'Trace File',
                xtype: 'textarea',
                height: 92,
                width: '98%',
                itemId: 'log_file',
                id: 'log_file'
            }]
        });
        this.callParent(arguments);
    }
});

// Behavior Panel
Ext.define('RP.Moca.Console.Util.Behavior', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        Ext.apply(this, {
            id: 'behaviorPanel',
            bodyStyle: 'background: #DEDEDE;',
            height: 100,
            defaults: {
                padding: 5
            },
            items: [{
                xtype:'label',
                text: 'Behavior',
                style: 'font-weight:bold;font-size:14px;'
            }, {
                xtype: 'hidden',
                width: '100%',
                height: 10,
                padding: 0
            }, {
                xtype: 'container',
                layout: 'hbox',
                items:[{
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Enabled',
                    labelAlign: 'top',
                    itemId: 'enabled',
                    id: 'enabled'
                }, {
                    itemId: 'spacer1',
                    width: 100
                }, {
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Overlap Executions',
                    labelAlign: 'top',
                    itemId: 'overlap',
                    id: 'overlap'
                }, {
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Auto Start',
                    labelAlign: 'top',
                    itemId: 'auto_start',
                    id: 'auto_start'
                }, {
                    itemId: 'spacer2',
                    width: 100
                }, {
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Restart on Termination',
                    labelAlign: 'top',
                    itemId: 'restart',
                    id: 'restart'
                }]
            }]
        });
        this.callParent(arguments);
    }
});

// Jobs Information Panel
Ext.define('RP.Moca.Console.Util.JobsInformation', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
    
        // The Information Panel Tool Bar
        var myInfoToolbar = [{
            xtype: 'toolbar',
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{ 
                xtype: 'tbfill'
            },{
                text: 'General',
                id: 'generalInfoButton',
                pressed: true,
                enableToggle: true,
                toggleGroup: 'JobGenEnv',
                handler: function() {
                    if (Ext.getCmp('scheduledJobsButton').pressed) {
                        Ext.getCmp('informationPanel').getLayout().setActiveItem('schedulePanel');
                    }
                    if (Ext.getCmp('timerJobsButton').pressed) {
                        Ext.getCmp('informationPanel').getLayout().setActiveItem('timerPanel');
                    }    
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Environment',
                id: 'environmentInfoButton',
                toggleGroup: 'JobGenEnv',
                enableToggle: true,
                handler: function() {
                    Ext.getCmp('informationPanel').getLayout().setActiveItem('envPanel');
                },
                style: 'padding:0;margin:0;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            }]
        }];
        
        // Apply some stuff
        Ext.apply(this, {
            id: 'informationPanel',
            layout: 'card',
            region: 'east',
            paddingz: 1,
            flex: 5,
            dockedItems: myInfoToolbar,
            items: [
                Ext.create('RP.Moca.Console.Util.JobEnvironment', {
                    job_id: this.initialConfig.job_id,
                    id: 'mocaJobEnvGrid'
                })
            ]
        });

        this.callParent(arguments);
    }
});

Ext.define('JobEnvDefinition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'job_id',     type: 'string'},
        {name: 'var_nam',      type: 'string'},
        {name: 'value',     type: 'string'},
        {name: 'isNew',     type: 'boolean'}
    ],
    validations: [
        {type: 'length',    field: 'job_id',     min: 1},
        {type: 'length',    field: 'var_nam',     min: 1},
        {type: 'length',    field: 'value',     min: 1}
    ]
});

// Environment Panel
Ext.define('RP.Moca.Console.Util.JobEnvironment', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        var me = this;

        me.jobEnvironmentStore = Ext.create('Ext.data.ArrayStore', {
            autoDestroy: true,
            model: JobEnvDefinition,
            fields: [
                {name: 'var_nam'},
                {name: 'value'},
                {name: 'isNew'}
            ],
            pruneModifiedRecords: true,
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({}),
                extraParams: {
                    m: 'getJobEnvironment',
                    job_id: this.initialConfig.job_id
                },
                simpleSortMode: true
            }
        });
        
        // Set overlay
        var loaded = false;
        me.on('render', function() {
            // only set the overlay when store isnt loaded yet
            // and this isnt an 'add' form
            if (!loaded && me.initialConfig.job_id !== undefined) {
                me.setLoading(true);
            }
        });
        
        // Load our store if job_id is fixed
        if (me.initialConfig.job_id) {
            me.jobEnvironmentStore.load({
                scope: me,
                callback: function(records, operation, success) {
                    loaded = true;
                    me.setLoading(false);
                }
            });
        }
        
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1,
            listeners: {
                beforeedit: function(editor, e)  {
                    // if record is old, disable editing of column 1
                    var isNew = e.record.get('isNew');
                    if (!(isNew) && e.colIdx === 0) {
                        return false;
                    }
                }   
            }
        });
        
        Ext.apply(this, {
            layout: 'fit',
            id: 'envPanel',
            height: 200,
            bodyStyle: 'background: #DEDEDE;',
            defaults: {
                xtype: 'textarea',
                height: 35,
                labelAlign: 'top',
                padding: 2,
                width: '92%'
            },
            bbar: [{ 
                xtype: 'tbfill'
            }, {
                text: 'Add Variable',
                handler : function(){
                    var jobId = Ext.getCmp('maintenance').down('#job_id').value;
                    
                    if (!jobId || Ext.isEmpty(jobId)) {
                        Ext.Msg.show({
                            title:'Warning',
                            msg: 'Job ID field is required to add variables.',
                            buttons: Ext.Msg.OK
                        });
                    }
                    else {
                        var r = Ext.create('JobEnvDefinition', {
                            job_id: jobId,
                            var_nam: '',
                            value: '',
                            isNew: true
                        });
                        this.jobEnvironmentStore.insert(0, r);
                        cellEditing.startEditByPosition({row: 0, column: 0});
                    }
                },
                scope: this
            }, {
                text: 'Remove Variable',
                handler : function() {
                    var record = this.down('#jobEnvGrid').getSelectionModel().getSelection()[0];
                    if (record) {
                        Ext.Msg.show({
                            title:'Confirm Delete',
                            msg: 'Are you sure you want to delete the selected variable?',
                            buttons: Ext.Msg.OKCANCEL,
                            fn: function(btn) {
                                if (btn == 'cancel') {
                                    //nothing
                                }
                                if (btn == 'ok') {
                                    this.jobEnvironmentStore.remove(record);
                                }
                            },
                            scope: this
                        });
                    }
                },
                scope: this
            },{ 
                xtype: 'tbfill'
            }],
            items: [
                Ext.create('Ext.grid.Panel', {
                    id: 'jobEnvGrid',
                    store: this.jobEnvironmentStore,
                    columns: [{
                        header   : 'Name',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'var_nam'
                    },{
                        header   : 'Value',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'value'
                    }],
                    height: '95%',
                    width: 440,
                    title: 'Environment Variables',
                    selModel: {
                        selType: 'cellmodel'
                    },
                    viewConfig: {
                        stripeRows: true
                    },
                    plugins: [cellEditing]
                })
            ]
        });
        this.callParent(arguments);
    }
});

// Tasks Information Panel
Ext.define('RP.Moca.Console.Util.TasksInformation', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
    
        // The Information Panel Tool Bar
        var myInfoToolbar = [{
            xtype: 'toolbar',
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{ 
                xtype: 'tbfill'
            },{
                text: 'General',
                id: 'generalTaskInfoButton',
                pressed: true,
                enableToggle: true,
                toggleGroup: 'TaskGenEnv',
                handler: function() {
                    Ext.getCmp('taskInformationPanel').getLayout().setActiveItem('timerPanel');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Environment',
                id: 'taskEnvironmentInfoButton',
                toggleGroup: 'TaskGenEnv',
                enableToggle: true,
                handler: function() {
                    Ext.getCmp('taskInformationPanel').getLayout().setActiveItem('taskEnvPanel');
                },
                style: 'padding:0;margin:0;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            }]
        }];
        
        // Apply some stuff
        Ext.apply(this, {
            id: 'taskInformationPanel',
            layout: 'card',
            region: 'east',
            paddingz: 1,
            flex: 5,
            dockedItems: myInfoToolbar,
            items: [
                Ext.create('RP.Moca.Console.Util.TimerMaint', {}),
                Ext.create('RP.Moca.Console.Util.TaskEnvironment', {
                    task_id: this.initialConfig.task_id
                })
            ]
        });

        this.callParent(arguments);
    }
});

Ext.define('TaskEnvDefinition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'var_nam',      type: 'string'},
        {name: 'value',     type: 'string'},
        {name: 'isNew',     type: 'boolean'}
    ],
    validations: [
        {type: 'length',    field: 'var_nam',     min: 1},
        {type: 'length',    field: 'value',     min: 1}
    ]
});

// Task Environment Panel
Ext.define('RP.Moca.Console.Util.TaskEnvironment', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        var me = this;

        me.taskEnvironmentStore = Ext.create('Ext.data.ArrayStore', {
            autoDestroy: true,
            model: TaskEnvDefinition,
            fields: [
                {name: 'var_nam'},
                {name: 'value'},
                {name: 'isNew'}
            ],
            pruneModifiedRecords: true,
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({}),
                extraParams: {
                    m: 'getTaskEnvironment',
                    task_id: this.initialConfig.task_id
                },
                simpleSortMode: true
            }
        });
        
        // Set overlay
        var loaded = false;
        me.on('render', function() {
            // only set the overlay when store isnt loaded yet
            // and this isnt an 'add' form
            if (!loaded && me.initialConfig.task_id !== undefined) {
                me.setLoading(true);
            }
        });
        
        // Load our store if task_id is fixed
        if (me.initialConfig.task_id) {
            me.taskEnvironmentStore.load({
                scope: me,
                callback: function(records, operation, success) {
                    loaded = true;
                    me.setLoading(false);
                }
            });
        }
        
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1,
            listeners: {
                beforeedit: function(editor, e)  {
                    // if record is old, disable editing of column 1
                    var isNew = e.record.get('isNew');
                    if (!(isNew) && e.colIdx === 0) {
                        return false;
                    }
                }   
            }
        });
        
        Ext.apply(me, {
            layout: 'fit',
            id: 'taskEnvPanel',
            height: 200,
            bodyStyle: 'background: #DEDEDE;',
            //padding: '0 0 5px 0',
            defaults: {
                xtype: 'textarea',
                height: 35,
                labelAlign: 'top',
                padding: 2,
                width: '92%'
            },
            bbar: [{ 
                xtype: 'tbfill'
            },{
                text: 'Add Variable',
                handler : function(){
                    var taskId = Ext.getCmp('maintenance').down('#task_id').value;
                    
                    if (!taskId || Ext.isEmpty(taskId)) {
                        Ext.Msg.show({
                            title:'Warning',
                            msg: 'Task ID field is required to add variables.',
                            buttons: Ext.Msg.OK
                        });
                    }
                    else {
                        var r = Ext.create('TaskEnvDefinition', {
                            task_id: taskId,
                            var_nam: '',
                            value: '',
                            isNew: true
                        });
                        me.taskEnvironmentStore.insert(0, r);
                        cellEditing.startEditByPosition({row: 0, column: 0});
                    }
                },
                scope: me
            }, {
                text: 'Remove Variable',
                handler : function() {
                    var record = me.down('#taskEnvGrid').getSelectionModel().getSelection()[0];
                    if (record) {
                        Ext.Msg.show({
                            title:'Confirm Delete',
                            msg: 'Are you sure you want to delete the selected variable?',
                            buttons: Ext.Msg.OKCANCEL,
                            fn: function(btn) {
                                if (btn == 'cancel') {
                                    //nothing
                                }
                                if (btn == 'ok') {
                                    me.taskEnvironmentStore.remove(record);
                                }
                            },
                            scope: me
                        });
                    }
                },
                scope: me
            },{ 
                xtype: 'tbfill'
            }],
            items: [
                Ext.create('Ext.grid.Panel', {
                    id: 'taskEnvGrid',
                    store: me.taskEnvironmentStore,
                    columns: [{
                        header   : 'Name',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'var_nam'
                    },{
                        header   : 'Value',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'value'
                    }],
                    height: '95%',
                    width: 440,
                    title: 'Environment Variables',
                    selModel: {
                        selType: 'cellmodel'
                    },
                    viewConfig: {
                        stripeRows: true
                    },
                    plugins: [cellEditing]
                })
            ]
        });
        me.callParent(arguments);
    }
});

