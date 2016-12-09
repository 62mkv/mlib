// View
Ext.define("RP.Moca.Console.Jobs.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    onActivate: function () {
        RP.Moca.Console.Jobs.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('jobsContainer').refresh();
        }

        this.activateCount++;
    },

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            itemId: 'jobsView',
            title: 'Jobs',
            layout: 'fit',
            border: false,
            items: Ext.create("RP.Moca.Console.Jobs.Container", {
                padding: '0px 5px 7px 0px',
                layout: 'border',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('jobsContainer').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

Ext.define('RP.Moca.Console.Jobs.Container', {
    extend: "Ext.panel.Panel",
    id: 'jobsContainer',
    
    /**
     * This is regex that tests if a job schedule is simple. The schedule format
     * for a cron expression is <seconds> <minutes> <hours> <day of month> <month> <day of week> [year]
     * Basically for the simple schedule the only fields that can be set are "hours" (1-23)
     * day of week (1-7 with repeating commas e.g. 1,2,3). Anything else is considered complex.
     * So dissecting this regex, we have the following:
     * 1) 0 for both seconds and minutes as we don't allow these to be configured in the simple settings
     * 2) A hours field that is either * (all values), ? (not specified), or 1-23
     * 3) For "day of month" a value of "?" (not specific) or "*" (all)
     * 4) For "month" a value of "*" (all)
     * 5) For "day of week" either * (all days), ? (unspecified), or a comma separated list of days 1-7 e.g. 1,2,3 or just a single day "1"
     * 6) Year is not included
     * Here's the regex with each above mentioned step highlighted:
     * [ 1 ][          2        ] [  3  ] [4] [            5               ]
     * /0 0 (\*|\?|\d|1\d|2[0-3]) (\*|\?) \*( (\*|\?|[1-7](\,[1-7]){0,6}))?$/
     */
    simpleScheduleRegex: /0 0 (\*|\?|\d|1\d|2[0-3]) (\*|\?) \*( (\*|\?|[1-7](\,[1-7]){0,6}))?$/,
    
    initComponent: function () {
    
        Ext.define('JobModel', {
            extend: 'Ext.data.Model',
            fields: [
                'job_id', 'role_id', 'name', 'scheduled', 'enabled',
                'type', 'command', 'log_file', 'trace_level', 'overlap',
                'schedule', 'start_delay', 'timer', 'grp_nam', 'nodes'
            ]
        });
    
        this.store = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            storeId: 'masterJobStore',
            proxy: {
                extraParams: {
                    m: 'getJobs'
                },
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({})
            },
            model: 'JobModel',
            groupers: [{
                property: 'type',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('jobsContainer').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('jobsContainer').setLoading(false);
                }
            }
        });
        
        var myEditToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{
                text: 'Add',
                handler: this.addJob
            }, {
                text: 'Copy',
                handler: this.copyJob
            }, {
                text: 'Modify',
                handler: this.modifyJob
            }, {
                text: 'Delete',
                handler: this.deleteJob
            }, {
                xtype: 'tbfill'
            },{
                text: 'Scheduled',
                id: 'scheduledJobsButton',
                enableToggle: true,
                toggleGroup: 'Jobs',
                allowDepress: false,
                pressed: true,
                handler: function() {
                    Ext.getCmp('jobsContainer').getLayout().setActiveItem('scheduledJobs');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Timer',
                id: 'timerJobsButton',
                enableToggle: true,
                toggleGroup: 'Jobs',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('jobsContainer').getLayout().setActiveItem('timedJobs');
                },
                style: 'padding:0;margin:0px 5px 0px 0px;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            },{ 
                xtype: 'tbfill'
            }]
        });

        var myActionToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Schedule Job',
                id: 'startScheduledJob',
                scope: this,
                handler: this.startJobHandler,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Unschedule Job',
                id: 'stopScheduledJob',
                scope: this,
                handler: this.stopJobHandler,
                style: {
                    margin: '3px'
                }
            }]
        });
        
        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    //Add
                    myEditToolbar.items.items[0].disable();
                    //Copy
                    myEditToolbar.items.items[1].disable();
                    //Modify
                    myEditToolbar.items.items[2].disable();
                    //Remove
                    myEditToolbar.items.items[3].disable();
                    myActionToolbar.disable();
                }
            }
        });

        Ext.apply(this, {
            layout: 'card',
            dockedItems: [myEditToolbar, myActionToolbar],
            items: [
                Ext.create("RP.Moca.Console.Jobs.Scheduled", {
                    border: false,
                    id: 'scheduledJobs',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Job ID:</b></td><td>{job_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command:</b></td><td>{command}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Scheduled:</b></td><td>{scheduled}</td></tr><tr><td><b>Schedule:</b></td><td>{schedule}</td></tr><tr><td><b>Enabled:</b></td><td>{enabled}</td></tr><tr><td><b>Overlap:</b></td><td>{overlap}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr><tr><td><b>Type:</b></td><td>{type}</td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                }),
                Ext.create("RP.Moca.Console.Jobs.Timed", {
                    border: false,
                    id: 'timedJobs',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Job ID:</b></td><td>{job_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command:</b></td><td>{command}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Scheduled:</b></td><td>{scheduled}</td></tr><tr><td><b>Enabled:</b></td><td>{enabled}</td></tr><tr><td><b>Overlap:</b></td><td>{overlap}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Timer:</b></td><td>{timer}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr><tr><td><b>Type:</b></td><td>{type}</td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                })
            ]
        });
        this.callParent(arguments);
    },
    refresh: function() {
        this.store.load();
        Ext.getCmp('scheduledJobs').fireEvent('afterrender', this);
        Ext.getCmp('timedJobs').fireEvent('afterrender', this);
    },
    
    // Handler function for the start job button
    startJobHandler: function () {
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            Ext.Msg.alert('Console', 'Please select a job to schedule and try again.');
            return;
        }

        // This list will contain all the jobs that we need to work with.
        var job_id_list = '';
        
        // Build the list of jobs to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the job id and scheduled flag for this job.
            var job_id = s.get('job_id');
            var scheduled = s.get('scheduled');
            
            if (scheduled === false) {
                if (job_id_list === '') {
                    job_id_list = job_id;
                }
                else {
                    job_id_list = job_id_list + ',' + job_id;
                }
            }
        }

        var msgAlreadyScheduled;
        var msgScheduledJob;
        var msgErrorSchedulingJob;
            
        if (selected.length == 1) {
            msgAlreadyScheduled = 'The selected job is already scheduled.';
            msgSchedulingJob = 'Scheduling job...';
            msgErrorSchedulingJob = 'An error occurred attempting to schedule the selected job.';
        }
        else {
            msgAlreadyScheduled = 'The selected jobs are already scheduled.';
            msgSchedulingJob = 'Scheduling jobs...';
            msgErrorSchedulingJob = 'An error occurred attempting to schedule the selected jobs.';   
        }
        
        // Make sure we don't have jobs that are already scheduled to schedule.
        if (job_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgAlreadyScheduled);
            return;
        }

        var myParams = {
            job_id_list: job_id_list,
            m: 'startSchedulingJob'
        };

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgSchedulingJob + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorSchedulingJob + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to start.
                        Ext.getCmp('jobsContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.MessageBox.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send schedule request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    },

    // Handler function for the stop job button
    stopJobHandler: function () {
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }
        
        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            RP.Moca.util.Msg.alert('Console', 'Please select a job to unschedule and try again.');
            return;
        }

        // This list will contain all the jobs that we need to work with.
        var job_id_list = '';
        
        // Build the list of jobs to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the job id and scheduled flag for this job.
            var job_id = s.get('job_id');
            var scheduled = s.get('scheduled');
            
            if (scheduled === true) {
                if (job_id_list === '') {
                    job_id_list = job_id;
                }
                else {
                    job_id_list = job_id_list + ',' + job_id;
                }
            }
        }
        
        var msgNotScheduled;
        var msgUnscheduledJob;
        var msgErrorUnschedulingJob;
            
        if (selected.length == 1) {
            msgNotScheduled = 'The selected job is not scheduled.';
            msgUnschedulingJob = 'Unscheduling job...';
            msgErrorUnschedulingJob = 'An error occurred attempting to unschedule the selected job.';
        }
        else {
            msgNotScheduled = 'The selected jobs are not scheduled.';
            msgUnschedulingJob = 'Unscheduling jobs...';
            msgErrorUnschedulingJob = 'An error occurred attempting to unschedule the selected jobs.';   
        }
        
        // Make sure we don't have jobs that are not scheduled to unschedule.
        if (job_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgNotScheduled);
            return;
        }

        var myParams = {
            job_id_list: job_id_list,
            m: 'stopSchedulingJob'
        };

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgUnschedulingJob + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorUnschedulingJob + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to start.
                        Ext.getCmp('jobsContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.MessageBox.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send unschedule request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: {
                job_id_list: job_id_list,
                m: 'stopSchedulingJob'
            },
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    },
    
    // Handler function to add a job
    addJob: function() {
        var addJobPanel = Ext.create('Ext.panel.Panel', {
            modal: true,
            id: 'addJobPanel',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            width: 800,
            height: 500,
            floating: true,
            closable: true,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'add'
                }),
                Ext.create('RP.Moca.Console.Util.JobsInformation', {})
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveJob, scope: addJobPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {addJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            addJobPanel.setTitle('Add Scheduled Based Job');
            addJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.ScheduledMaint', {}));
            addJobPanel.setHeight(525);
            addJobPanel.down('#command').setHeight(255);
            addJobPanel.down('#type').setValue('cron');
            addJobPanel.down('#informationPanel').getLayout().setActiveItem('schedulePanel');
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            addJobPanel.setHeight(500);
            addJobPanel.setTitle('Add Timer Based Job');
            addJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.TimerMaint', {}));
            addJobPanel.down('#command').setHeight(230);
            addJobPanel.down('#type').setValue('timer');
            addJobPanel.down('#informationPanel').getLayout().setActiveItem('timerPanel');
        }
        
        addJobPanel.down('#task_id').hide();
        addJobPanel.down('#auto_start').hide();
        addJobPanel.down('#restart').hide();
        addJobPanel.down('#run_dir').hide();
        addJobPanel.down('#spacer2').hide();
        addJobPanel.down('#tracingPanel').setHeight(200);
        addJobPanel.down('#action').setValue('add');
        
        var role_id = Ext.getCmp('timedJobs').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('scheduledJobs').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             addJobPanel.down('#role_id').store.add({field1:key});
        });
        
        addJobPanel.show();
    },
    
    // Handler to modify selected Job
    modifyJob: function() {
        var selected;
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single job to modify and try again.');
            return;
        }  
        
        var jobTitle = selected[0].data.name;
        
        var modifyJobPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'modifyJobPanel',
            id: 'modifyJobPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'edit'
                }),
                Ext.create('RP.Moca.Console.Util.JobsInformation', {
                    job_id: selected[0].data.job_id
                })
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveJob, scope: modifyJobPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {modifyJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
               
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            modifyJobPanel.setTitle('Modify Job: ' + jobTitle);
            modifyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.ScheduledMaint', {}));
            modifyJobPanel.setHeight(525);
            modifyJobPanel.down('#command').setHeight(255);
            modifyJobPanel.down('#type').setValue('cron');
            this.populateSchedule(modifyJobPanel, selected[0].data.schedule);
            modifyJobPanel.down('#informationPanel').getLayout().setActiveItem('schedulePanel');
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            modifyJobPanel.setTitle('Modify Job: ' + jobTitle);
            modifyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.TimerMaint', {}));
            modifyJobPanel.down('#start_delay').setValue(selected[0].data.start_delay);
            modifyJobPanel.down('#timer').setValue(selected[0].data.timer);
            modifyJobPanel.down('#type').setValue('timer');
            modifyJobPanel.setHeight(500);
            modifyJobPanel.down('#command').setHeight(230);
            modifyJobPanel.down('#informationPanel').getLayout().setActiveItem('timerPanel');
        }
        
        modifyJobPanel.down('#job_id').setValue(selected[0].data.job_id);
        modifyJobPanel.down('#job_id').disable();
        modifyJobPanel.down('#name').setValue(selected[0].data.name);
        modifyJobPanel.down('#log_file').setValue(selected[0].data.log_file);
        modifyJobPanel.down('#action').setValue('edit');
        
        this.populate(modifyJobPanel, selected[0].data);
        
        modifyJobPanel.show();
    },
    
    // Handler to copy selected Job
    copyJob: function() {
        var selected;
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single job to copy and try again.');
            return;
        }  
        
        var jobTitle = selected[0].data.name;
    
        var copyJobPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'copyJobPanel',
            id: 'copyJobPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'copy'
                }),
                Ext.create('RP.Moca.Console.Util.JobsInformation', {})
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveJob, scope: copyJobPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {copyJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            copyJobPanel.setTitle('Copy Job: ' + jobTitle);
            copyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.ScheduledMaint', {}));
            copyJobPanel.setHeight(525);
            copyJobPanel.down('#command').setHeight(255);
            copyJobPanel.down('#type').setValue('cron');
            this.populateSchedule(copyJobPanel, selected[0].data.schedule);
            copyJobPanel.down('#informationPanel').getLayout().setActiveItem('schedulePanel');
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            copyJobPanel.setTitle('Copy Job: ' + jobTitle);
            copyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.TimerMaint', {}));
            copyJobPanel.down('#start_delay').setValue(selected[0].data.start_delay);
            copyJobPanel.down('#timer').setValue(selected[0].data.timer);
            copyJobPanel.down('#type').setValue('timer');
            copyJobPanel.setHeight(500);
            copyJobPanel.down('#command').setHeight(230);
            copyJobPanel.down('#informationPanel').getLayout().setActiveItem('timerPanel');
        }
        
        copyJobPanel.down('#job_id').setValue('COPY_' + selected[0].data.job_id);
        copyJobPanel.down('#name').setValue('Copy of ' + selected[0].data.name);
        var logFile = selected[0].data.log_file;
        if (Ext.isEmpty(logFile)) {
            copyJobPanel.down('#log_file').setValue('');
        }
        else {
            copyJobPanel.down('#log_file').setValue(logFile + '.copy');
        }
        copyJobPanel.down('#action').setValue('add');
        
        this.populate(copyJobPanel, selected[0].data);
    
        copyJobPanel.show();
    },
        
    // Handler to delete selected Job
    deleteJob: function() {
        var selected;
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single job to delete and try again.');
            return;
        }
    
        var deleteJobPanel = Ext.create('Ext.panel.Panel', {
            layout: 'fit',
            modal: true,
            floating: true,
            closable: true,
            width: 450,
            title: 'Delete Job',
            id: 'deleteJobPanel',
            items: [{
                xtype: 'label',
                text: selected[0].data.name,
                style: 'font-weight:bold;font-size:12px;',
                margin: 30
            },{
                xtype: 'hidden',
                text: selected[0].data.job_id,
                itemId: 'job_id'
            }],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Delete', handler: this.removeJob },
                { xtype: 'button', text: 'Cancel', handler: function() {deleteJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
    
        deleteJobPanel.show();
    },
    
    populateSchedule: function(panel, schedule) {
        panel.down('#schedule').setValue(schedule);
        panel.down('#spacer2').hide();
        
        // If the schedule isn't simple we just show the user the
        // schedule otherwise populate the simple widgets (hours + day of week)
        if (!this.simpleScheduleRegex.test(schedule.trim())) {
            panel.down('#simpleScheduleButton').disable();
            panel.down('#tooAdvanced').show();
            panel.down('#simpleSchedule').hide();
            panel.down('#advancedSchedule').show();
        }
        else{ 
            var cron = schedule.split(' ');
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
                if (hours[0] === '*') {
                    Ext.getCmp('schedule_time').setValue('*');
                }
                else if (hours[0] >= 12) {
                    hours = hours - 12;
                    Ext.getCmp('pm_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
                else if (hours[0] < 12) {
                    Ext.getCmp('am_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
            }
        }
    },
    
    populate: function(panel, selected) {
        var role_id = Ext.getCmp('timedJobs').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('scheduledJobs').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             panel.down('#role_id').store.add({field1:key});
        });
        
        panel.down('#task_id').hide();
        panel.down('#auto_start').hide();
        panel.down('#restart').hide();
        panel.down('#run_dir').hide();
        panel.down('#tracingPanel').setHeight(200);
        panel.down('#command').setValue(selected.command);
        panel.down('#role_id').setValue(selected.role_id);
        panel.down('#grp_nam').setValue(selected.grp_nam);
        panel.down('#type').setValue(selected.type);
        
        panel.down('#enabled').setValue(selected.enabled);
        panel.down('#overlap').setValue(selected.overlap);
        panel.down('#trace_level').setValue(selected.trace_level);
    },
    
    // Handler function to save a job
    saveJob: function() {
        var maintenance = Ext.getCmp('maintenance'),
            job_id = maintenance.down('#job_id').value,
            name = maintenance.down('#name').value,
            command = maintenance.down('#command').value;
        
        if (job_id === '') {
            RP.Moca.util.Msg.alert('Console', 'A Job ID is required.');
            return;
        }
        if (name === '') {
            RP.Moca.util.Msg.alert('Console', 'A name is required.');
            return;
        }
        if (command === '') {
            RP.Moca.util.Msg.alert('Console', 'A command is required.');
            return;
        }
        
        var panel = this.up().up();
        
        var chkGroup = Ext.getCmp("traceCheckBoxGroup");
        
        var traceLevel = '';
        if (Ext.getCmp('tracingPanel').down('#trace_level').getValue()) {    
            traceLevel = '*';
        }
        
        var start_delay;
        var timer;
        var schedule;
        var timerPanel = Ext.getCmp('timerPanel');
        var schedulePanel = Ext.getCmp('schedulePanel');
        
        if (timerPanel) {
            start_delay = timerPanel.down('#start_delay').value;
            timer = timerPanel.down('#timer').value;
        }
        
        if (schedulePanel){
            if (schedulePanel.down('#advancedSchedule').hidden) {
                var array = schedulePanel.down('#schedule').value.split(' ');
                var seconds = array[0];
                var minutes = array[1];
                var hours;
                var dayMonth = array[3];
                var month = array[4];
                var dayWeek;
                if (schedulePanel.down('#am_time').value){
                    hours = schedulePanel.down('#schedule_time').value;
                }
                if (schedulePanel.down('#pm_time').value) {
                    hours = parseInt(schedulePanel.down('#schedule_time').value, 10)+12;
                }
                if (!hours) {
                    hours = '?';
                }
                dayWeek = schedulePanel.down('#schedule_repeat').value;
                if(!dayWeek) {
                    dayWeek = [''];
                }
                dayWeek.sort();
                if (dayWeek[0] === '') {
                    dayWeek = '?';
                }
                if (dayWeek.length === 7) {
                    dayWeek = '*';
                }
                if (dayWeek.length > 0 && dayWeek[0] !== '?') {
                    dayMonth = '?';
                }
                
                schedule = seconds + ' ' + minutes + ' ' + hours + ' ' + dayMonth + ' ' + month + ' ' + dayWeek;
            }
            else {
                schedule = schedulePanel.down('#schedule').value;
            }
        }

        var grp_nam = maintenance.down('#grp_nam').value,
            role_id = maintenance.down('#role_id').lastValue,
            type = maintenance.down('#type').value,
            action = maintenance.down('#action').value,
            log_file = Ext.getCmp('tracingPanel').down('#log_file').value,
            enabled = Ext.getCmp('behaviorPanel').down('#enabled').value,
            overlap = Ext.getCmp('behaviorPanel').down('#overlap').value,
            jobEnvStore = Ext.getCmp('envPanel').jobEnvironmentStore,
            jobStore = Ext.getStore('masterJobStore'),
            modifiedRecords = jobEnvStore.getModifiedRecords(),
            newRecords = jobEnvStore.getNewRecords(),
            removedRecords = jobEnvStore.getRemovedRecords(),
            mode = maintenance.mode;
        
        if (mode === undefined) {
            RP.Moca.util.Msg.alert('Console', 'Error: maintenance panel mode undefined.');
            return;
        }
        
        // Break if job already exists
        if (jobStore.find('job_id', job_id) !== -1 && mode !== 'edit') {
            RP.Moca.util.Msg.alert('Console', 'Cannot create duplicate job.');
            return;
        }
        
        // Sync environment variables
        var ifBreak = false;
        jobEnvStore.each(function(record) {
            if (Ext.isEmpty(record.get('var_nam')) || Ext.isEmpty(record.get('value'))) {
                Ext.Msg.show({
                    title: 'Warning',
                    width: 400,
                    msg: 'Invalid environment variable. Name and Value are required.',
                    buttons: Ext.MessageBox.OK,
                    icon: Ext.MessageBox.WARNING
                }); 
                ifBreak = true;
            }
        });
        
        // We don't want to post anything if there is an invalid variable
        if (ifBreak) {
            return;
        }
        // Sync created
        Ext.each(newRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    job_id: job_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveJobEnvironment',
                    action: 'add'
                }
            });
        }, this);
        
        // Sync modified
        Ext.each(modifiedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    job_id: job_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveJobEnvironment',
                    action: 'edit'
                }/*,
                success : function(response){
                    jobEnvStore.commitChanges();
                },
                failure : function(response){
                    jobEnvStore.rejectChanges();
                }*/
            });
        }, this);
               
        // Sync removed
        Ext.each(removedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    job_id: job_id,
                    name: record.data.var_nam,
                    m: 'removeJobEnvironment'
                }
                /*success : function(response){
                    jobEnvStore.commitChanges();
                },
                failure : function(response){
                    jobEnvStore.rejectChanges();
                }*/
            });
        }, this);
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                job_id: job_id,
                name: name,
                grp_nam: grp_nam,
                role_id: role_id,
                command: command,
                type: type,
                enabled: enabled,
                overlap: overlap,
                start_delay: start_delay,
                log_file: log_file,
                trace_level: traceLevel,
                schedule: schedule,
                timer: timer,
                action: action,
                m: 'saveJob'
            },
            success: function(response){
                var json = Ext.decode(response.responseText);
                this._status = parseInt(json.status, 10);
                this._message = json.message;
                
                if (this._status !== 0 && this._status !== null) {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: this._message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                }
                else {
                    Ext.getCmp('jobsContainer').refresh();
                    panel.close();
                }
            }
        });
    },
    
    // Handler function to delete a job
    removeJob: function() {
        var panel = Ext.getCmp('deleteJobPanel');

        var job_id = panel.down('#job_id').text.trim();
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                job_id: job_id,
                m: 'removeJob'
            },
            success: function(response){
                var json = Ext.decode(response.responseText);
                this._status = parseInt(json.status, 10);
                this._message = json.message;
                
                if (this._status !== 0 && this._status !== null) {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: this._message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                }
                else {
                    Ext.getCmp('jobsContainer').refresh();
                    panel.close();
                }
            }
        });
    }
});


// Timed Jobs
Ext.define("RP.Moca.Console.Jobs.Timed", {
    extend: "Ext.grid.Panel",

    initComponent: function () {
        // Handler function to render interval/schedule
        var intervalScheduleHandler = function(sprite, record, attributes, index, store) {
            return (attributes.data.schedule === null ? attributes.data.timer : attributes.data.schedule);
        };
        
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
        
        // FLAG new CheckBoxColumn works, but Ext.create doesn't. 
        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Scheduled',
            dataIndex: 'scheduled',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Name',
            dataIndex: 'name',
            id: 'timed_jobs_name',
            flex: 2,
            minWidth: 150
        },{
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'command',
            id: 'timed_jobs_command',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Interval',
            renderer: intervalScheduleHandler,
            flex: 1,
            align: 'right',
            minWidth: 125
        }, {
            xtype: 'checkcolumn',
            text: 'Enabled',
            dataIndex: 'enabled',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            text: 'Overlap',
            dataIndex: 'overlap',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Start Delay',
            dataIndex: 'start_delay',
            id: 'timed_jobs_start_delay',
            flex: 1,
            minWidth: 125,
            align: 'right'
        }];

        this.store = Ext.create("RP.Moca.util.Store", {
            viewConfig: {
                stripeRows: true
            },
            model: 'JobModel',
            groupers: [{
                property: 'type',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }, {
                property: 'command',
                direction: 'ASC',
                root: 'data'
            }]
        });
 
        // Hook into the Master Job Store to refresh the Local Store.
        this.masterStore = Ext.getStore('masterJobStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            id: 'timedJobs',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No timer based jobs currently exist.',
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // Refresh the Master Job Store which will
        // initiate a Local Job Store refresh.
        this.masterStore.load();
    },    
    
    onMasterStoreLoad: function() {
        // Refresh the Local Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('Timer-Based');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
        
        // Refresh the Button Text.
        var button = Ext.getCmp('timerJobsButton');
        if (button.rendered) {
            button.setText('Timer (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Timer (' + this.store.getCount() +')');
            }, this);
        }
    }
});

// Scheduled Jobs
Ext.define("RP.Moca.Console.Jobs.Scheduled", {
    extend: "Ext.grid.Panel",

    initComponent: function () {
    
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }

        // Handler function to render interval/schedule
        var intervalScheduleHandler = function(sprite, record, attributes, index, store) {
            return (attributes.data.schedule === null ? attributes.data.timer : attributes.data.schedule);
        };
        
        // FLAG new CheckBoxColumn works, but Ext.create doesn't. 
        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Scheduled',
            dataIndex: 'scheduled',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Name',
            dataIndex: 'name',
            id: 'schedule_jobs_name',
            flex: 2,
            minWidth: 150
        },{
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'command',
            id: 'schedule_jobs_command',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Schedule',
            renderer: intervalScheduleHandler,
            flex: 2,
            minWidth: 150
        }, {
            xtype: 'checkcolumn',
            text: 'Enabled',
            dataIndex: 'enabled',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            text: 'Overlap',
            dataIndex: 'overlap',
            flex: 1,
            minWidth: 100
        }];

        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'JobModel',
            viewConfig: {
                stripeRows: true
            },
            groupers: [{
                property: 'type',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }, {
                property: 'command',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook into the Master Job Store to refresh the local Store.
        this.masterStore = Ext.getStore('masterJobStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            id: 'scheduledJobs',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No schedule based jobs currently exist.',
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // Refresh the Master Job Store which will
        // initiate a local store refresh.
        this.masterStore.load();
    },
    
    onMasterStoreLoad: function() {
        // Refresh the Schedule Based Job Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('Schedule Based');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
    
        // Refresh the Button Text.
        var button = Ext.getCmp('scheduledJobsButton');
        if (button.rendered) {
            button.setText('Scheduled (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Scheduled (' + this.store.getCount() +')');
            }, this);
        }
    }
});