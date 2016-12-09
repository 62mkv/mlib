// View
Ext.define("RP.Moca.Console.Tasks.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    onActivate: function () {
        RP.Moca.Console.Tasks.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('tasksContainer').refresh();
        }

        this.activateCount++;
    },

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Tasks',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.Tasks.Container", {
                padding: '0px 5px 7px 0px',
                layout: 'border',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('tasksContainer').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

// Container
Ext.define("RP.Moca.Console.Tasks.Container", {
    extend: "Ext.panel.Panel",
    id: 'tasksContainer',
    
    initComponent: function (config) {
    
        Ext.define('TaskModel', {
            extend: 'Ext.data.Model',
            fields: [
                'task_id', 'role_id', 'name', 'cmd_line',
                'run_dir', 'log_file', 'restart', 'auto_start',
                'start_delay', 'running', 'grp_nam','task_typ', 'nodes', 'trace_level'
            ]
        });
        
        this.store = Ext.create('RP.Moca.util.Store', {
            autoLoad: true,
            storeId: 'masterTaskStore',
            proxy: {
                extraParams: {
                    m: 'getTasks'
                },
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({})
            },
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
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
                    Ext.getCmp('tasksContainer').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('tasksContainer').setLoading(false);
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
                handler: this.addTask
            },{
                text: 'Copy',
                handler: this.copyTask
            },{
                text: 'Modify',
                handler: this.modifyTask
            },{
                text: 'Delete',
                handler: this.deleteTask
            },{ 
                xtype: 'tbfill'
            },{
                text: 'Thread',
                id: 'threadTasksButton',
                pressed: true,
                enableToggle: true,
                toggleGroup: 'Tasks',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('tasksContainer').getLayout().setActiveItem('threadTasks');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Process',
                id: 'processTasksButton',
                enableToggle: true,
                toggleGroup: 'Tasks',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('tasksContainer').getLayout().setActiveItem('processTasks');
                },
                style: 'padding:0;margin:0;border-radius:0;border-width:0!important;border-right-color:transparent;border-left-color:transparent;'
            },{
                text: 'Daemon',
                id: 'daemonTasksButton',
                enableToggle: true,
                toggleGroup: 'Tasks',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('tasksContainer').getLayout().setActiveItem('daemonTasks');
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
                text: 'Start Task',
                scope: this,
                handler: this.startTaskHandler,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Stop Task',
                scope: this,
                handler: this.stopTaskHandler,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Restart Task',
                scope: this,
                handler: this.restartTaskHandler,
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
        
        // Apply some stuff
        Ext.apply(this, {
            layout: 'card',
            dockedItems: [myEditToolbar, myActionToolbar] ,
            items: [
                Ext.create('RP.Moca.Console.Tasks.Thread', {
                    border: false,
                    id: 'threadTasks',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Task ID:</b></td><td>{task_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Running:</b></td><td>{running}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Auto Start:</b></td><td>{auto_start}</td></tr><tr><td><b>Restart:</b></td><td>{restart}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                }),
                Ext.create('RP.Moca.Console.Tasks.Process', {
                    border: false,
                    id: 'processTasks',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Task ID:</b></td><td>{task_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command Line:</b></td><td>{cmd_line}</td></tr><tr><td><b>Running:</b></td><td>{running}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Start In:</b></td><td>{run_dir}</td></tr><tr><td><b>Auto Start:</b></td><td>{auto_start}</td></tr><tr><td><b>Restart:</b></td><td>{restart}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                }),
                Ext.create('RP.Moca.Console.Tasks.Daemon', {
                    border: false,
                    id: 'daemonTasks',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Task ID:</b></td><td>{task_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command Line:</b></td><td>{cmd_line}</td></tr><tr><td><b>Running:</b></td><td>{running}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Start In:</b></td><td>{run_dir}</td></tr><tr><td><b>Auto Start:</b></td><td>{auto_start}</td></tr><tr><td><b>Restart:</b></td><td>{restart}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                })
            ]
        });
        
        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
        Ext.getCmp('threadTasks').fireEvent('afterrender', this);
        Ext.getCmp('processTasks').fireEvent('afterrender', this);
        Ext.getCmp('daemonTasks').fireEvent('afterrender', this);
    },
    
    // Handler function for adding a Task
    addTask: function() {
        var addTaskPanel = Ext.create('Ext.form.Panel', {
            modal: true,
            id: 'addTaskPanel',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            width: 800,
            height: 625,
            floating: true,
            closable: true,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'add'
                }),
                Ext.create('RP.Moca.Console.Util.TasksInformation', {}) 
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveTask, scope: addTaskPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {addTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('threadTasksButton').pressed) {
            addTaskPanel.setTitle('Add Thread Based Task');
            addTaskPanel.down('#command').setFieldLabel('Class Name');
            addTaskPanel.down('#run_dir').hide();
            addTaskPanel.setHeight(500);
            addTaskPanel.down('#command').setHeight(230);
            addTaskPanel.down('#task_typ').setValue('T');
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            addTaskPanel.setTitle('Add Process Based Task');
            addTaskPanel.setHeight(560);
            addTaskPanel.down('#tracingPanel').setHeight(260);
            addTaskPanel.down('#command').setHeight(290);
            addTaskPanel.down('#task_typ').setValue('P');
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            addTaskPanel.setTitle('Add Daemon Based Task');
            addTaskPanel.setHeight(560);
            addTaskPanel.down('#tracingPanel').setHeight(260);
            addTaskPanel.down('#command').setHeight(290);
            addTaskPanel.down('#task_typ').setValue('D');
        }
        
        addTaskPanel.down('#job_id').hide();
        addTaskPanel.down('#overlap').hide();
        addTaskPanel.down('#enabled').hide();
        addTaskPanel.down('#timer').hide();
        addTaskPanel.down('#spacer1').hide();
        addTaskPanel.down('#action').setValue('add');
        
        var role_id = Ext.getCmp('threadTasks').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('processTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(Ext.getCmp('daemonTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             addTaskPanel.down('#role_id').store.add({field1:key});
        });
        
        addTaskPanel.show();
    },
    
    // Handler to modify selected Task
    modifyTask: function() {
        var selected;
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single task to modify and try again.');
            return;
        }  
        
        var taskTitle = selected[0].data.name;
        
        var modifyTaskPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'modifyTaskPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'edit'
                }),
                Ext.create('RP.Moca.Console.Util.TasksInformation', {
                    task_id: selected[0].data.task_id
                })
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveTask, scope: modifyTaskPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {modifyTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('threadTasksButton').pressed) {
            modifyTaskPanel.down('#command').setFieldLabel('Class Name');
            modifyTaskPanel.down('#run_dir').hide();
            modifyTaskPanel.setHeight(500);
            modifyTaskPanel.down('#command').setHeight(255);
        }
        else {
            modifyTaskPanel.setHeight(560);
            modifyTaskPanel.down('#tracingPanel').setHeight(260);
            modifyTaskPanel.down('#command').setHeight(315);
        }
        
        modifyTaskPanel.setTitle('Modify Task: ' + taskTitle);
        modifyTaskPanel.down('#task_id').setValue(selected[0].data.task_id);
        modifyTaskPanel.down('#task_id').disable();
        modifyTaskPanel.down('#log_file').setValue(selected[0].data.log_file);
        modifyTaskPanel.down('#name').setValue(selected[0].data.name);
        modifyTaskPanel.down('#action').setValue('edit');
        
        this.populate(modifyTaskPanel, selected[0].data);
        
        modifyTaskPanel.show();
    },
    
    // Handler to copy selected Task
    copyTask: function() {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single task to copy and try again.');
            return;
        }  
        
        var taskTitle = selected[0].data.name;
    
        var copyTaskPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'copyTaskPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'copy'
                }),
                Ext.create('RP.Moca.Console.Util.TasksInformation', {
                    task_id: selected[0].data.task_id
                })
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveTask, scope: copyTaskPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {copyTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('threadTasksButton').pressed) {
            copyTaskPanel.down('#command').setFieldLabel('Class Name');
            copyTaskPanel.down('#run_dir').hide();
            copyTaskPanel.setHeight(500);
            copyTaskPanel.down('#command').setHeight(255);
        }
        else {
            copyTaskPanel.setHeight(560);
            copyTaskPanel.down('#tracingPanel').setHeight(260);
            copyTaskPanel.down('#command').setHeight(315);
        }
        
        copyTaskPanel.setTitle('Copy Task: ' + taskTitle);
        copyTaskPanel.down('#task_id').setValue('COPY_' + selected[0].data.task_id);
        copyTaskPanel.down('#name').setValue('Copy of ' + selected[0].data.name);
        var logFile = selected[0].data.log_file;
        if (Ext.isEmpty(logFile)) {
            copyTaskPanel.down('#log_file').setValue('');
        }
        else {
            copyTaskPanel.down('#log_file').setValue(logFile + '.copy');
        }
        copyTaskPanel.down('#action').setValue('add');
        
        this.populate(copyTaskPanel, selected[0].data);
    
        copyTaskPanel.show();
    },
    
    // Handler to delete selected Task
    deleteTask: function() {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single task to delete and try again.');
            return;
        }
    
        var deleteTaskPanel = Ext.create('Ext.panel.Panel', {
            id: 'deleteTaskPanel',
            layout: 'fit',
            modal: true,
            floating: true,
            closable: true,
            width: 450,
            title: 'Delete Task',
            items: [{
                xtype: 'label',
                text: selected[0].data.name,
                style: 'font-weight:bold;font-size:12px;',
                margin: 30
            },{
                xtype: 'hidden',
                text: selected[0].data.task_id,
                itemId: 'task_id'
            }],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Delete', handler: this.removeTask },
                { xtype: 'button', text: 'Cancel', handler: function() {deleteTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
    
        deleteTaskPanel.show();
    },
    
    // Function to populate the copy and modify task popups.
    populate: function(panel, selected) {
    
        var role_id = Ext.getCmp('threadTasks').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('processTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(Ext.getCmp('daemonTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             panel.down('#role_id').store.add({field1:key});
        });
        
        panel.down('#job_id').hide();
        panel.down('#timer').hide();
        panel.down('#overlap').hide();
        panel.down('#enabled').hide();
        panel.down('#spacer1').hide();
        panel.down('#command').setValue(selected.cmd_line);
        panel.down('#role_id').setValue(selected.role_id);
        panel.down('#grp_nam').setValue(selected.grp_nam);
        panel.down('#task_typ').setValue(selected.task_typ);
        panel.down('#start_delay').setValue(selected.start_delay);
        panel.down('#run_dir').setValue(selected.run_dir);
        
        panel.down('#auto_start').setValue(selected.auto_start);
        panel.down('#restart').setValue(selected.restart);
        panel.down('#trace_level').setValue(selected.trace_level);
    },
    
    // Handler function to save a task
    saveTask: function() {
        var maintenance = Ext.getCmp('maintenance'),
            mode = maintenance.mode,
            task_id = maintenance.down('#task_id').value,
            name = maintenance.down('#name').value,
            cmd_line = maintenance.down('#command').value,
            task_typ = maintenance.down('#task_typ').value,
            run_dir = Ext.getCmp('tracingPanel').down('#run_dir').value,
            taskStore = Ext.getCmp('threadTasks').getStore(),
            taskEnvStore = Ext.getCmp('taskEnvPanel').taskEnvironmentStore;
        
        if (task_id === '') {
            RP.Moca.util.Msg.alert('Console', 'A Task ID is required.');
            return;
        }
        if (name === '') {
            RP.Moca.util.Msg.alert('Console', 'A name is required.');
            return;
        }
        if (cmd_line === '') {
            if (task_typ === 'T') {
                RP.Moca.util.Msg.alert('Console', 'A class name is required.');
                return;
            }
            else {
                RP.Moca.util.Msg.alert('Console', 'A command is required.');
                return;
            }
        }
        if (task_typ === 'P' && Ext.isEmpty(run_dir)) {
            RP.Moca.util.Msg.alert('Console', 'A starting directory is required.');
            return;
        }
        
        if (mode === undefined) {
            RP.Moca.util.Msg.alert('Console', 'Error: maintenance panel mode undefined.');
            return;
        }
        
        if (taskStore.find('task_id', task_id) !== -1 && mode !== 'edit') {
            RP.Moca.util.Msg.alert('Console', 'Cannot create duplicate task.');
            return;
        }
        
        var panel = this.up().up();
        
        var chkGroup = Ext.getCmp("traceCheckBoxGroup");
        
        var traceLevel = '';
        if (Ext.getCmp('tracingPanel').down('#trace_level').getValue()) {    
            traceLevel = '*';
        }
        
        var grp_nam = maintenance.down('#grp_nam').value;
        var role_id = maintenance.down('#role_id').lastValue;
        var action = maintenance.down('#action').value;
        var start_delay = Ext.getCmp('timerPanel').down('#start_delay').value;
        var log_file = Ext.getCmp('tracingPanel').down('#log_file').value;
        var auto_start = Ext.getCmp('behaviorPanel').down('#auto_start').value;
        var restart = Ext.getCmp('behaviorPanel').down('#restart').value;
        
        // Sync Task Environment variables
        var modifiedRecords = taskEnvStore.getModifiedRecords();
        var newRecords = taskEnvStore.getNewRecords();
        var removedRecords = taskEnvStore.getRemovedRecords();
        
        var ifBreak = false;
        taskEnvStore.each(function(record) {
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
                    task_id: task_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveTaskEnvironment',
                    action: 'add'
                }
            });
        }, this);
        
        // Sync modified
        Ext.each(modifiedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    task_id: task_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveTaskEnvironment',
                    action: 'edit'
                }/*,
                success : function(response){
                    jobStore.commitChanges();
                },
                failure : function(response){
                    jobStore.rejectChanges();
                }*/
            });
        }, this);
               
        // Sync removed
        Ext.each(removedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    task_id: task_id,
                    name: record.data.var_nam,
                    m: 'removeTaskEnvironment'
                }
                /*success : function(response){
                    jobStore.commitChanges();
                },
                failure : function(response){
                    jobStore.rejectChanges();
                }*/
            });
        }, this);
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                task_id: task_id,
                name: name,
                grp_nam: grp_nam,
                role_id: role_id,
                cmd_line: cmd_line,
                task_typ: task_typ,
                start_delay: start_delay,
                run_dir: run_dir,
                log_file: log_file,
                auto_start: auto_start,
                restart: restart,
                trace_level: traceLevel,
                action: action,
                m: 'saveTask'
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
                    Ext.getCmp('tasksContainer').refresh();
                    panel.close();
                }
            }
        });
    },
    
    // Handler function to delete a task
    removeTask: function() {
        var panel = Ext.getCmp('deleteTaskPanel');

        var task_id = panel.down('#task_id').text.trim();
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                task_id: task_id,
                m: 'removeTask'
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
                    Ext.getCmp('tasksContainer').refresh();
                    panel.close();
                }
            }
        });
    },
    
    // Handler function for the start task button
    startTaskHandler: function () {
        var selected;
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            //RP.Moca.util.Msg.alert('Console', 'Please select a task to start and try again.');
            Ext.Msg.alert('Console', 'Please select a task to start and try again.');
            return;
        }

        // This list will contain all the tasks that we need to work with.
        var task_id_list = '';
        
        // Build the list of tasks to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the task id and running flag for this task.
            var task_id = s.get('task_id'); // flag - maybe problem?
            var running = s.get('running');
            
            if (running === false) {
                if (task_id_list === '') {
                    task_id_list = task_id;
                }
                else {
                    task_id_list = task_id_list + ',' + task_id;
                }
            }
        }

        var msgAlreadyRunning;
        var msgStartingTask;
        var msgErrorStartingTask;

        if (selected.length == 1) {
            msgAlreadyRunning = 'The selected task is already running.';
            msgStartingTask = 'Starting task...';
            msgErrorStartingTask = 'An error occurred attempting to start the selected task.';
        }
        else {
            msgAlreadyRunning = 'The selected tasks are already running.';
            msgStartingTask = 'Starting tasks...';
            msgErrorStartingTask = 'An error occurred attempting to start the selected tasks.';   
        }
        
        // Make sure we have tasks that aren't already running to start.
        if (task_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgAlreadyRunning);
            return;
        }

        var myParams = {
            task_id_list: task_id_list,
            m: 'startTask'
        };
                 
        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgStartingTask + '<br><br>',
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
                            RP.Moca.util.Msg.alert('Console', msgErrorStartingTask + '\n\n' + message);
                        }
                        // Refresh the grid panel after we've given the task enough time to start.
                        Ext.getCmp('tasksContainer').refresh();
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
            msg: 'Could not send start task request to the server.'
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

    // Handler function for the stop task button
    stopTaskHandler: function () {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            RP.Moca.util.Msg.alert('Console', 'Please select a task to stop and try again.');
            return;
        }

        // This list will contain all the tasks that we need to work with.
        var task_id_list = '';
        
        // Build the list of tasks to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the task id and running flag for this task.
            var task_id = s.get('task_id');
            var running = s.get('running');
            
            if (running === true) {
                if (task_id_list === '') {
                    task_id_list = task_id;
                }
                else {
                    task_id_list = task_id_list + ',' + task_id;
                }
            }
        }

        var msgNotRunning;
        var msgStoppingTask;
        var msgErrorStoppingTask;
            
        if (selected.length == 1) {
            msgNotRunning = 'The selected task is not running.';
            msgStoppingTask = 'Stopping task...';
            msgErrorStoppingTask = 'An error occurred attempting to stop the selected task.';
        }
        else {
            msgNotRunning = 'The selected tasks are not running.';
            msgStoppingTask = 'Stopping tasks...';
            msgErrorStoppingTask = 'An error occurred attempting to stop the selected tasks.';   
        }
        
        // Make sure we have tasks that are actually running to stop.
        if (task_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgNotRunning);
            return;
        }

        var myParams = {
            task_id_list: task_id_list,
            m: 'stopTask'
        }; 

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgStoppingTask + '<br><br>',
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
                            RP.Moca.util.Msg.alert('Console', msgErrorStoppingTask + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to stop.
                        Ext.getCmp('tasksContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.Msg.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };


        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultHandler,
            failureAlert: {
                title: 'Console',
                msg: 'Could not send stop task request to the server.'
            }
        });
    },

    // Handler function for the restart task button
    restartTaskHandler: function () {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            RP.Moca.util.Msg.alert('Console', 'Please select a task to restart and try again.');
            return;
        }

        // This list will contain all the tasks that we need to work with.
        var task_id_list = '';
        
        // Build the list of tasks to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the task id and running flag for this task.
            var task_id = s.get('task_id');
            var running = s.get('running');
            
            if (running === true) {
                if (task_id_list === '') {
                    task_id_list = task_id;
                }
                else {
                    task_id_list = task_id_list + ',' + task_id;
                }
            }
        }

        var msgNotRunning;
        var msgRestartingTask;
        var msgErrorRestartingTask;
            
        if (selected.length == 1) {
            msgNotRunning = 'The selected task is not running.';
            msgRestartingTask = 'Restarting task...';
            msgErrorRestartingTask = 'An error occurred attempting to restart the selected task.';
        }
        else {
            msgNotRunning = 'The selected tasks are not running.';
            msgStoppingTask = 'Restarting tasks...';
            msgErrorStoppingTask = 'An error occurred attempting to restart the selected tasks.';   
        }
        
        // Make sure we have tasks that are actually running to restart.
        if (task_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgNotRunning);
            return;
        }

        var myParams = {
            task_id_list: task_id_list,
            m: 'restartTask'
        }; 

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgRestartingTask + '<br><br>',
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
                            RP.Moca.util.Msg.alert('Console', msgErrorRestartingTask + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to stop.
                        Ext.getCmp('tasksContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.Msg.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send restart task request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            //params: myParams,
            params: {
                task_id_list: task_id_list,
                m: 'restartTask'
            },
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    }
});

// Thread Tasks
Ext.define('RP.Moca.Console.Tasks.Thread', {
    extend: 'Ext.grid.Panel',
    
    initComponent: function() {
    
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
    
        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook in our local store to load when the master store loads.
        this.masterStore = Ext.getStore('masterTaskStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Running',
            dataIndex: 'running',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            minWidth: 150
        }, {
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            flex: 2,
            minWidth: 150
        }, {
            header: 'Class Name',
            dataIndex: 'cmd_line',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Start In',
            dataIndex: 'run_dir',
            hidden: true,
            flex: 1,
            minWidth: 150
        }, {
            header: 'Tracing',
            dataIndex: 'trace_level',
            hidden: true,
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Auto Start',
            dataIndex: 'auto_start',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Restart',
            dataIndex: 'restart',
            flex: 1,
            minWidth: 100
        }];

        // Apply some stuff
        Ext.apply(this, {
            id: 'threadTasks',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No thread based tasks currently exist.',
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // Refresh the Master Store which will
        // initiate a refresh to the local store.
        this.masterStore.load();
    },

    onMasterStoreLoad: function() {
        // Refresh the Thread Based Task Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('T');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
    
        // Refresh the Button Text.
        var button = Ext.getCmp('threadTasksButton');
        if (button.rendered) {
            button.setText('Thread (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Thread (' + this.store.getCount() +')');
            }, this);
        }
    }
});

// Process Tasks
Ext.define('RP.Moca.Console.Tasks.Process', {
    extend: 'Ext.grid.Panel',
    
    initComponent: function() {
    
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
    
        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook in our local store to load when the master store loads.
        this.masterStore = Ext.getStore('masterTaskStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Running',
            dataIndex: 'running',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Task ID',
            dataIndex: 'task_id',
            hidden: true,
            minWidth: 125
        }, {
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            minWidth: 150
        }, {
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
            dataIndex: 'cmd_line',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Start In',
            dataIndex: 'run_dir',
            flex: 1,
            minWidth: 125
        }, {
            xtype: 'checkcolumn',
            header: 'Auto Start',
            dataIndex: 'auto_start',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Restart',
            dataIndex: 'restart',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Start Delay',
            dataIndex: 'start_delay',
            hidden: true,
            align: 'right',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Log File',
            dataIndex: 'log_file',
            hidden: true,
            flex: 1,
            minWidth: 150
        }, {
            header: 'Tracing',
            dataIndex: 'trace_level',
            hidden: true,
            flex: 1,
            minWidth: 100
        }];

        // Apply some stuff
        Ext.apply(this, {
            id: 'processTasks',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No process based tasks currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // We want to reload the Master Store which will
        // initiate a refresh of the local store.
        this.masterStore.load();
    },

    onMasterStoreLoad: function() {
        // Refresh the Process Based Task Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('P');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
    
        // Refresh the Button Text.
        var button = Ext.getCmp('processTasksButton');
        if (button.rendered) {
            button.setText('Process (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Process (' + this.store.getCount() +')');
            }, this);
        }
    }
});

// Daemon Tasks
Ext.define('RP.Moca.Console.Tasks.Daemon', {
    extend: 'Ext.grid.Panel',
    
    initComponent: function() {
        
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
    
        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook in our local store to load when the master store loads.
        this.masterStore = Ext.getStore('masterTaskStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Running',
            dataIndex: 'running',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Task ID',
            dataIndex: 'task_id',
            hidden: true,
            minWidth: 125
        }, {
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            minWidth: 125
        }, {
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'cmd_line',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Start In',
            dataIndex: 'run_dir',
            flex: 1,
            minWidth: 125
        }, {
            xtype: 'checkcolumn',
            header: 'Auto Start',
            dataIndex: 'auto_start',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Restart',
            dataIndex: 'restart',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Start Delay',
            dataIndex: 'start_delay',
            hidden: true,
            align: 'right',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Log File',
            dataIndex: 'log_file',
            hidden: true,
            flex: 1,
            minWidth: 150
        }, {
            header: 'Tracing',
            dataIndex: 'trace_level',
            hidden: true,
            flex: 1,
            minWidth: 100
        }];

        // Apply some stuff
        Ext.apply(this, {
            id: 'daemonTasks',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No daemon based tasks currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        this.masterStore.load();
    },

    onMasterStoreLoad: function() {
        // Refresh the Daemon Based Task Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('D');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
        
        // Refresh the Button Text.
        var button = Ext.getCmp('daemonTasksButton');
        if (button.rendered) {
            button.setText('Daemon (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Daemon (' + this.store.getCount() +')');
            }, this);
        }
    }
});